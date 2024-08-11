package com.example.strollsafe.utils;

public class Location {
    private double longitude;
    private double latitude;

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(long longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(long latitude) {
        this.latitude = latitude;
    }

    public Location(double latitude, double longitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }
}
