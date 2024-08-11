package com.example.strollsafe.utils;

import com.google.android.gms.maps.model.LatLng;

public class SafeZone {
    private String safeZoneName;
    private double lat;
    private double lng;
    private double radius;
    private LatLng latLng;

    public SafeZone() {

    }

    public SafeZone(String safeZoneName, double lat, double lng, double radius) {
        this.safeZoneName = safeZoneName;
        this.lat = lat;
        this.lng = lng;
        this.radius = radius;
        this.latLng = new LatLng(lat, lng);
    }

    public SafeZone(String safeZoneName, LatLng latLng, double radius) {
        this.safeZoneName = safeZoneName;
        this.latLng = latLng;
        this.lat = latLng.latitude;
        this.lng = latLng.longitude;
        this.radius = radius;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public String getSafeZoneName() {
        return safeZoneName;
    }

    public void setSafeZoneName(String safeZoneName) {
        this.safeZoneName = safeZoneName;
    }

    public LatLng getLatLng() {
        return latLng;
    }
}
