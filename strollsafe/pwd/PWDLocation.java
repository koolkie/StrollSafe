package com.example.strollsafe.pwd;

import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.time.LocalDateTime;


/**
 * PWDLocation.java
 *
 * Description: Class that store the most recent location of the PWD's device
 *
 * @since June 6, 2022
 * @author  Alvin Tsang
 *
 * Last modified date: June 30, 2022
 * Last modified by: Alvin Tsang
 * */

public class PWDLocation {

    private final double latitude;
    private final double longitude;
    private final float accuracy;
    private final String address;
    private final LocalDateTime initalDateTime;
    private LocalDateTime lastHereDateTime;

    /**
     * Description: Parameterized constructor
     *
     * @param latitude latitude of the pwd location
     * @param longitude longitude of the pwd location
     * @param accuracy accuracy of the location based on the permission and phone sensors
     * @param address street address of the latitude and longitude if it exists. If a street address
     *                does not exist, save the latitude and longitude as a string
     * */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public PWDLocation(double latitude, double longitude, float accuracy, String address) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.accuracy = accuracy;
        this.address = address;
        this.initalDateTime = LocalDateTime.now();
        this.lastHereDateTime = initalDateTime;
    } // end of constructor

    /**
     * Description: Returns the latitude of the PWD's location in degrees
     *
     * @return the latitude of the saved pwd location
     * */
    public double getLatitude() {
        return latitude;
    } // end of getLatitude()

    /**
     * Description: Returns the longitude of teh PWD's location in degrees
     *
     * @return the longitude of the saved pwd location
     * */
    public double getLongitude() {
        return longitude;
    } // end of getLongitude()

    /**
     * Description: Returns the estimated horizontal accuracy radius in meters of
     *              this location at the 68th percentile confidence level
     *
     * @return the accuracy of the saved pwd location
     * */
    public float getAccuracy() {
        return accuracy;
    } // end of getAccuracy()

    /**
     * Description: Returns the first address line of the location
     *
     * @return the Address of the saved pwd location
     * */
    public String getAddress() {
        return address;
    } // end of getAddresss()

    /**
     * Description: Returns the initial location save date and time
     *
     * @return the date and time when the pwd location was saved
     * */
    public LocalDateTime getInitialDateTime() {
        return initalDateTime;
    } // end of getInitialDateTime()

    /**
     * Description: Returns the last saved date and time of the location
     *
     * @return the date and time when the pwd location was saved
     * */
    public LocalDateTime getLastHereDateTime() {
        return lastHereDateTime;
    } // end of getLastHereDateTime()

    /**
     * Description: Update the dateTime to be the the last date and time this location was saved
     *
     * @param lastHereDateTime most recent date and time this location was logged
     * */
    public void setLastHereDateTime(LocalDateTime lastHereDateTime) {
        this.lastHereDateTime = lastHereDateTime;
    } // end of setDateTime()

    /**
     * Description: Convert the class into a string
     *
     * @return string of the data fields in the class
     * */
    @NonNull
    @Override
    public String toString() {
        return(
                "Latitude: " + this.latitude + "\n" +
                "Longitude: " + this.longitude + "\n" +
                "Accuracy: " + this.accuracy + "\n" +
                "Address: " + this.address + "\n" +
                "Initial Date" + this.initalDateTime.toString() + "\n" +
                "Last Here Date: " + this.lastHereDateTime.toString() + "\n"
                );
    } // end of toString()

} // end of PWDLocation.java


