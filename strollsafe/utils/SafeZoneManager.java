package com.example.strollsafe.utils;

import android.content.Context;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class SafeZoneManager {
    private GeofencingClient geofencingClient;
    private List<Geofence> geofenceList = new ArrayList<>();

    public SafeZoneManager(Context context, String userId) {
        geofencingClient = LocationServices.getGeofencingClient(context);

    }

    public void addNewGeofence(String name, double lat, double lng, float radius) {
        geofenceList.add(new Geofence.Builder()
                .setRequestId(name)
                .setCircularRegion(lat, lng, radius)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                .build());
    }

    public void removeGeofence(String name) {
        geofenceList.removeIf(new Predicate<Geofence>() {
            @Override
            public boolean test(Geofence geofence) {
                return geofence.getRequestId().equals(name);
            }
        });
    }

    public GeofencingClient getGeofencingClient() {
        return geofencingClient;
    }
    public void setGeofencingClient(GeofencingClient geofencingClient) {
        this.geofencingClient = geofencingClient;
    }
    public List<Geofence> getGeofenceList() {
        return geofenceList;
    }

    public void setGeofenceList(List<Geofence> geofenceList) {
        this.geofenceList = geofenceList;
    }

}
