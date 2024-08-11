package com.example.strollsafe.utils.location;


import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.strollsafe.R;
import com.example.strollsafe.pwd.PWDLocation;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.Priority;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.O)
public class LocationManager {
    private static LocationManager instance = null;
    private Context context;

    public static final int DEFAULT_UPDATE_INTERVAL = 10; // seconds
    public static final int FAST_UPDATE_INTERVAL = 1; // seconds

    private FusedLocationProviderClient fusedLocationProviderClient;
    private static int REQUEST_CHECK_SETTINGS = 200;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private Intent backgroundLocationIntent = new Intent("background_location");
    private StringBuilder stringBuilder = new StringBuilder();
    private Activity activity;

    private static final String SHARED_PREFS = "StrollSafe: LocationList";
    private static final int  MAX_SAVED_LOCATIONS = 5;
    private static final long IDLE_MINUTES = 2L;

    private ArrayList<PWDLocation> PWDLocationList;

    private NotificationManagerCompat notificationManagerCompat;
    private Notification notification;

    private LocationManager() {
    }

    public static LocationManager getInstance(Context context) {
        if (instance == null) {
            instance = new LocationManager();
        }
        instance.init(context);
        return instance;
    }

    private void init(Context context) {
        this.context = context;
        this.fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
        if (context instanceof Activity) {
            activity = (Activity) context;
        }
        instance.loadData();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        String address;
                        Geocoder geocoder = new Geocoder(context);
                        try {
                            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(),
                                    location.getLongitude(), 1);
                            address = addresses.get(0).getAddressLine(0);
                        } catch (Exception e) {
                            address = ("Unable to get street address");
                        }

                        if ( PWDLocationList.size() > 0 &&
                                address.equals(PWDLocationList.get(PWDLocationList.size() - 1).getAddress()) &&
                                !address.equals("Unable to get street address"))
                        {
                            PWDLocation lastLocation = PWDLocationList.get(PWDLocationList.size() - 1);
                            lastLocation.setLastHereDateTime(LocalDateTime.now());

                            // after IDLE_MINUTES, if the location has not changed, notify user
                            Duration duration = Duration.between(lastLocation.getInitialDateTime(),
                                    lastLocation.getLastHereDateTime());
                            if (duration.toMinutes() == IDLE_MINUTES) {
                                NotificationChannel channel = new NotificationChannel("idle_alert",
                                        "PWD Idle", NotificationManager.IMPORTANCE_DEFAULT);
                                NotificationManager manager = context.getSystemService(NotificationManager.class);
                                manager.createNotificationChannel(channel);

                                NotificationCompat.Builder builder = new NotificationCompat.Builder(
                                        context,"idle_alert");
                                builder.setSmallIcon(R.drawable.ic_launcher_background);
                                builder.setContentTitle("PWD Idle Alert");
                                builder.setContentText("PWD has been idle for " + IDLE_MINUTES + " minutes!");

                                notification = builder.build();
                                notificationManagerCompat = NotificationManagerCompat.from(context);
                                notificationManagerCompat.notify("idle_alert", 1, notification);
                            }
                        } else {
                            PWDLocation newLocation = new PWDLocation(location.getLatitude(),
                                    location.getLongitude(), location.getAccuracy(), address);
                            if (PWDLocationList.size() >= MAX_SAVED_LOCATIONS) {
                                PWDLocationList.remove(0);
                            }
                            PWDLocationList.add(newLocation);

                        }
                        Log.i("Location", "Location updated");
                        instance.saveData();
                        LocalBroadcastManager.getInstance(context).sendBroadcast(backgroundLocationIntent);
                    }
                }
            }
        };
        createLocationRequest();
    }

    protected void createLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(DEFAULT_UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FAST_UPDATE_INTERVAL);
        locationRequest.setPriority(Priority.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        SettingsClient client = LocationServices.getSettingsClient(context);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener( new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse response) {

            }
        });

        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    try {
                        ResolvableApiException resovlable = (ResolvableApiException) e;
                        resovlable.startResolutionForResult(activity, REQUEST_CHECK_SETTINGS);
                    } catch (Exception sendEx) {

                    }
                }
            }
        });
    }

    public void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback,
                Looper.getMainLooper());
    }

    public void stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }


    private void loadData() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);

        // creating a variable for gson.
        Gson gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class,
                new TypeAdapter<LocalDateTime>() {
                    @Override
                    public void write(JsonWriter jsonWriter, LocalDateTime date) throws IOException {
                        jsonWriter.value(date.toString());
                    }
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

    private void saveData() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        Gson gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class,
                new TypeAdapter<LocalDateTime>() {
                    @Override
                    public void write(JsonWriter jsonWriter, LocalDateTime date) throws IOException {
                        jsonWriter.value(date.toString());
                    }
                    @Override
                    public LocalDateTime read(JsonReader jsonReader) throws IOException {
                        return LocalDateTime.parse(jsonReader.nextString());
                    }
                }).setPrettyPrinting().create();

        String json = gson.toJson(PWDLocationList);
        editor.putString("Locations", json);
        editor.apply();
    } // end of saveData()


} // end of LocationManager.java
