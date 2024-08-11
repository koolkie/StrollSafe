package com.example.strollsafe.utils;

import android.Manifest.permission;
import android.annotation.SuppressLint;

import com.example.strollsafe.R;
import com.example.strollsafe.pwd.PWDLocation;
import com.example.strollsafe.ui.CaregiverPwdListActivity;
import com.example.strollsafe.ui.location.ShowSavedLocationsList;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.bson.Document;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import io.realm.mongodb.App;
import io.realm.mongodb.mongo.MongoCollection;

@RequiresApi(api = Build.VERSION_CODES.O)
public class GeofencingMapsActivity extends AppCompatActivity implements GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener, OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback {

    private DatabaseManager databaseManager;
    private GoogleMap map;
    private MongoCollection userCollection;
    private String userId = "";
    private List<SafeZone> safeZoneList = new ArrayList<>();
    private List<Marker> markerList = new ArrayList<>();
    LatLngBounds.Builder allMarkers;
    private double customSafeZoneRadius = 0;
    private String customSafeZoneName = "";
    private final int CIRCLE_FILL_COLOUR = 0x750000FF;

    // pwd location
    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final String SHARED_PREFS = "StrollSafe: LocationList";
    private ArrayList<PWDLocation> PWDLocationList;

    /**
     * Flag indicating whether a requested permission has been denied after returning in {@link
     * #onRequestPermissionsResult(int, String[], int[])}.
     */
    private boolean permissionDenied = false;

    /**
     * Request code for location permission request.
     *
     * @see #onRequestPermissionsResult(int, String[], int[])
     */
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loadLocationData();

        Intent intent = getIntent();
        userId = intent.getStringExtra("userId");
        databaseManager = new DatabaseManager(this);
        userCollection = databaseManager.getUsersCollection();
        userCollection.findOne(new Document("userId", userId)).getAsync(callback -> {
            if(callback.isSuccess()) {
                Document userInfo = (Document) callback.get();
                setTitle((String) userInfo.get("firstName") + " " + userInfo.get("lastName") + "' Safe Zones");

                ArrayList<Document> safeZones = (ArrayList<Document>) userInfo.get("safezones");
                if(safeZones != null && safeZones.size() > 0) {
                    for(Document safeZone : safeZones) {
                        safeZoneList.add(new SafeZone((String) safeZone.get("name"), (Double) safeZone.get("lat"), (Double) safeZone.get("lng"), (Double) safeZone.get("radius")));
                    }
                }

                setContentView(R.layout.activity_geofencing_map);
                Toolbar topBar = (Toolbar) findViewById(R.id.toolbar);
                setSupportActionBar(topBar);

                SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                mapFragment.getMapAsync(this);
                Toast.makeText(GeofencingMapsActivity.this, "Safe Zones has been loaded successfully.", Toast.LENGTH_LONG).show();

            } else {
                setContentView(R.layout.activity_geofencing_map);
                Toolbar topBar = (Toolbar) findViewById(R.id.toolbar);
                setSupportActionBar(topBar);

                SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                mapFragment.getMapAsync(this);
                Toast.makeText(GeofencingMapsActivity.this, "Error. Unable to load patients Safe Zones.", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.geofencing_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.back:
                startActivity(new Intent(GeofencingMapsActivity.this, CaregiverPwdListActivity.class));
                return true;

            case R.id.showAllMarkersOption:
                showAllMarkersOnclick();
                return true;

            case R.id.refreshCurrentLocation:
                // call refresh on the database for more recent information
                break;

            case R.id.item_seeLocationList:
                startActivity(new Intent(GeofencingMapsActivity.this, ShowSavedLocationsList.class));
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    public void showAllMarkersOnclick() {
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(allMarkers.build(), 200);
        map.animateCamera(cu);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        map.setOnMyLocationButtonClickListener(this);
        map.setOnMyLocationClickListener(this);

        // convert last location to LatLng
        PWDLocation location = PWDLocationList.get(PWDLocationList.size() - 1);
        if (location != null) {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);

            // show address and last access details when the marker is touched
            markerOptions.title(location.getAddress());
            markerOptions.snippet("Last here on " + location.getLastHereDateTime().format(DATE_FORMAT));
            // place location as a pin on the map
            map.addMarker(markerOptions);
        }

        map.setOnCircleClickListener(new GoogleMap.OnCircleClickListener() {
            @Override
            public void onCircleClick(@NonNull Circle circle) {
                SafeZone selectedSafeZone = findSafeZoneFromCircle(circle);
                new AlertDialog.Builder(GeofencingMapsActivity.this)
                        .setTitle(selectedSafeZone.getSafeZoneName())
                        .setMessage(String.format("Latitude: %f\nLongitude: %f\nRadius: %f", selectedSafeZone.getLat(), selectedSafeZone.getLng(), selectedSafeZone.getRadius()))
                        .setCancelable(true)
                        .setNeutralButton("Close", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        })
                        .setNegativeButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                userCollection.findOne(new Document("userId", userId)).getAsync(findResult -> {
                                    if (findResult.isSuccess()) {
                                        Document patientUser = (Document) findResult.get();
                                        ArrayList<Document> safezones = (ArrayList<Document>) patientUser.get("safezones");
                                        for(Document safeZone : safezones) {
                                            if(circle.getCenter().latitude == (Double) safeZone.get("lat") && circle.getCenter().longitude == (Double) safeZone.get("lng")) {
                                                Document pwdData = new Document("userId", userId);
                                                userCollection.updateOne(pwdData, new Document("$pull", new Document("safezones", safeZone))).getAsync(new App.Callback() {

                                                    @Override
                                                    public void onResult(App.Result result) {
                                                        if(result.isSuccess()) {
                                                            // remove the marker, remove the circle
                                                            Marker marker = findMarkerFromCenter(circle.getCenter());
                                                            marker.remove();
                                                            safeZoneList.remove(marker);
                                                            circle.remove();
                                                            Toast.makeText(GeofencingMapsActivity.this, "Safe Zone has been removed.", Toast.LENGTH_LONG).show();
                                                            map.animateCamera(getBoundsAllSafeZones());
                                                        }
                                                    }
                                                });
                                                return;
                                            }
                                        }
                                    }
                                });
                                dialogInterface.cancel();
                            }
                        })
                        .show();
            }
        });
        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 18));
                return true;
            }
        });
        map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(@NonNull LatLng latLng) {
                AlertDialog.Builder safeZoneDialog = new AlertDialog.Builder(GeofencingMapsActivity.this);
                safeZoneDialog.setTitle("New Safe Zone");
                safeZoneDialog.setMessage(String.format("Latitude: %f\nLongitude: %f", latLng.latitude, latLng.longitude));
                View dialogView = getLayoutInflater().inflate(R.layout.new_safezone_dialog, null);
                safeZoneDialog.setView(dialogView);
                safeZoneDialog.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
                safeZoneDialog.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        EditText safeZoneNameInput = (EditText) dialogView.findViewById(R.id.safezoneNameInput);
                        EditText safeZoneRadiusInput = (EditText) dialogView.findViewById(R.id.safezoneRadiusInput);

                        // Find our pwds existing safezones
                        userCollection.findOne(new Document("userId", userId)).getAsync(findResult -> {
                            if(findResult.isSuccess()) {
                                Document patientUser = (Document) findResult.get();
                                ArrayList<Document> safezones = (ArrayList<Document>) patientUser.get("safezones");

                                // Check to ensure that the safezone we are trying to add does not exist already.
                                for(Document safeZone : safezones) {
                                    if(latLng.latitude == (Double) safeZone.get("lat") && latLng.longitude == (Double) safeZone.get("lng")) {
                                        Toast.makeText(GeofencingMapsActivity.this, "There exists a Safe Zone at this positions already.", Toast.LENGTH_LONG).show();
                                        dialogInterface.cancel();
                                        return;
                                    }
                                }
                                Document pwdData = new Document("userId", userId);
                                Document newSafeZone = new Document().append("name", safeZoneNameInput.getText().toString()).append("lat", (Double) latLng.latitude).append("lng", (Double) latLng.longitude).append("radius", Double.parseDouble(safeZoneRadiusInput.getText().toString()));
                                Document update =  new Document("safezones", newSafeZone);

                                // Add the new safezone to the pwds safezone array
                                userCollection.updateOne(pwdData, new Document("$push", update)).getAsync(new App.Callback() {

                                    @Override
                                    public void onResult(App.Result result) {
                                        if(result.isSuccess()) {
                                            Marker newSafeZoneMarker = map.addMarker(new MarkerOptions().position(latLng).title(safeZoneNameInput.getText().toString()));
                                            Circle newSafeZoneCircle = map.addCircle(new CircleOptions().clickable(true).center(latLng).radius(Double.parseDouble(safeZoneRadiusInput.getText().toString())).fillColor(CIRCLE_FILL_COLOUR));
                                            safeZoneList.add(new SafeZone(safeZoneNameInput.getText().toString(), latLng, Double.parseDouble(safeZoneRadiusInput.getText().toString())));
                                            getBoundsAllSafeZones();
                                            Toast.makeText(GeofencingMapsActivity.this, "Safe Zone added.", Toast.LENGTH_LONG).show();
                                            dialogInterface.cancel();
                                        }
                                    }
                                });
                            }
                        });

                    }
                });
                safeZoneDialog.show();
            }
        });

        drawSafeZonesAndMarkers();
        enableMyLocation();

        CameraUpdate allSafeZonesCamUpdate = getBoundsAllSafeZones();
        if(allSafeZonesCamUpdate != null) {
            map.moveCamera(allSafeZonesCamUpdate);
        }
    }

    private SafeZone findSafeZoneFromCircle(Circle circle) {
        for(SafeZone safeZone : safeZoneList) {
            if(safeZone.getLatLng().equals(circle.getCenter())) {
                return safeZone;
            }
        }
        return null;
    }

    private Marker findMarkerFromCenter(LatLng latLng) {
        for(Marker marker : markerList) {
            if(marker.getPosition().equals(latLng)) {
                return marker;
            }
        }
        return null;
    }

    private CameraUpdate getBoundsAllSafeZones() {
        allMarkers = new LatLngBounds.Builder();
        if(safeZoneList.size() < 1) {
            return null;
        }

        for (SafeZone safeZone : safeZoneList) {
            allMarkers.include(safeZone.getLatLng());
        }

        LatLngBounds bounds = allMarkers.build();
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 200);
        return cu;
    }

    private void drawSafeZonesAndMarkers() {
        for(SafeZone safeZone : safeZoneList) {
            LatLng center = new LatLng(safeZone.getLat(), safeZone.getLng());
            markerList.add(map.addMarker(new MarkerOptions().title(safeZone.getSafeZoneName()).position(center)));
            map.addCircle(new CircleOptions().center(center).radius(safeZone.getRadius()).clickable(true).fillColor(CIRCLE_FILL_COLOUR));
        }
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    @SuppressLint("MissingPermission")
    private void enableMyLocation() {
        // [START maps_check_location_permission]
        // 1. Check if permissions are granted, if so, enable the my location layer
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            map.setMyLocationEnabled(true);
            return;
        }

        // 2. Otherwise, request location permissions from the user.
        PermissionUtils.requestLocationPermissions(this, 4444, true);
        // [END maps_check_location_permission]
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "Moving map to your location.", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(this, String.format("Current location:\n%f\n%f", location.getLatitude(), location.getLongitude()), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION) || PermissionUtils
                .isPermissionGranted(permissions, grantResults,
                        Manifest.permission.ACCESS_COARSE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Permission was denied. Display an error message
            // [START_EXCLUDE]
            // Display the missing permission error dialog when the fragments resume.
            permissionDenied = true;
            // [END_EXCLUDE]
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (permissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            permissionDenied = false;
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }

    private boolean requestBackgroundLocation() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED ? false : true;
    }


    /**
     * Description: Read the shared preference folder for the list of saved locations and
     *              store them in an arraylist
     * */
    private void loadLocationData() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);

        Gson gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class,
                new TypeAdapter<LocalDateTime>() {
                    @Override
                    public void write(JsonWriter jsonWriter, LocalDateTime date) throws IOException {
                        jsonWriter.value(date.toString());
                    }
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public LocalDateTime read(JsonReader jsonReader) throws IOException {
                        return LocalDateTime.parse(jsonReader.nextString());
                    }
                }).setPrettyPrinting().create();

        Type type = new TypeToken<ArrayList<PWDLocation>>() {}.getType();
        String json = sharedPreferences.getString("Locations", null);
        PWDLocationList = gson.fromJson(json, type);

        // checking below if the array list is empty or not
        if (PWDLocationList == null) {
            PWDLocationList = new ArrayList<>();
        }
    } // end of loadData()

}