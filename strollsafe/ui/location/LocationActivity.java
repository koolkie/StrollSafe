package com.example.strollsafe.ui.location;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.ActivityResultRegistry;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.strollsafe.utils.GeofenceBroadcastReceiver;
import com.example.strollsafe.utils.Location;
import com.example.strollsafe.R;
import com.example.strollsafe.utils.SafeZoneManager;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

public class LocationActivity extends AppCompatActivity {
    SafeZoneManager safezoneManager;
    GeofencingClient geofencingClient;
    private List<Geofence> geofenceList = new ArrayList<>();
    private PendingIntent geofencePendingIntent;
    Location sfuAvacadoArea = new Location(49.278965, -122.916582);
    private ActivityResultRegistry activityResultRegistry;
    private ActivityResultLauncher<String[]> locationPermissionRequest = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
        Boolean fineLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
        Boolean coarseLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);
        if (fineLocationGranted != null && fineLocationGranted) {
            // Precise location access granted.
        } else if (coarseLocationGranted != null && coarseLocationGranted) {
            // Only approximate location access granted.
        } else {
            // No location access granted.
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        configureBack();
        geofencingClient = LocationServices.getGeofencingClient(this);
        askPermissions();


    }

    @Override
    public void onResume() {
        super.onResume();
        askPermissions();
    }

    @Override
    public void onStart() {
        super.onStart();
        askPermissions();
    }

    public void configureBack() {
        Button PWD = (Button) findViewById(R.id.backbutton);
        PWD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void askPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            locationPermissionRequest.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
            });
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            locationPermissionRequest.launch(new String[]{
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
            });
        }
    }

    public void addGeofence(View v) {
        geofenceList.add(new Geofence.Builder()
                .setRequestId("avocado")
                .setCircularRegion(sfuAvacadoArea.getLatitude(), sfuAvacadoArea.getLongitude(), 100)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                        Geofence.GEOFENCE_TRANSITION_EXIT)
                .build());

        geofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
                .addOnSuccessListener(this, aVoid -> {
                    Toast.makeText(getApplicationContext()
                            , "Geofencing has started", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(this, e -> {
                    Toast.makeText(getApplicationContext()
                            , e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();

                })
                .addOnCompleteListener( this, e -> {
                    Toast.makeText(getApplicationContext()
                            , e.toString(), Toast.LENGTH_SHORT).show();
                });
    }

    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (geofencePendingIntent != null) {
            return geofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceBroadcastReceiver.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            geofencePendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        } else {
            geofencePendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        return geofencePendingIntent;
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(geofenceList);
        return builder.build();
    }

} // end of LocationActivity.java

