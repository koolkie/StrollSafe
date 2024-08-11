/*
 * MyLocations.java
 *
 * Description: Store the location of the PWD as list
 *
 * Created on: July 18, 2022
 * Created by: Alvin Tsang
 *
 * */

package com.example.strollsafe.pwd;

import android.app.Application;
import android.location.Location;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.ArrayList;


public class PWDLocations extends Application {

    private static PWDLocations singleton;
    private ArrayList<Location> myLocations;
    private static final File FILE_NAME = new File("myLocations.json");



    /**
     * Description: Initialize the activity
     * */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void onCreate() {
        super.onCreate();
        singleton = this;


        // File handling: verify that the file exists. If not, create a .json file
        if (FILE_NAME.exists()) {
            try {

                FileReader fileReader = new FileReader(FILE_NAME);
                Type typeToken = new TypeToken<ArrayList<Location>>() {}.getType();


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
                        }).create();
                myLocations = gson.fromJson(fileReader, typeToken);
                fileReader.close();

            } catch (FileNotFoundException e) {
                System.err.println("Error: Can not create file reader object");
            } catch (IOException e) {
                System.err.println("Error: Closing file failed");
            }
        } else { // if file does not exist
            myLocations = new ArrayList<>();
            try {
                FileWriter fileWriter = new FileWriter(FILE_NAME);

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
                        }).create();
                gson.toJson(myLocations, fileWriter);
                fileWriter.close();
            } catch (IOException e) {
                System.err.println("Error: Could not write in file");
            }
        }

    } // end of onCreate()


    /**
     * Description: Get the instance of the activity
     * */
    public PWDLocations getInstance() {
        return singleton;
    } // end of getInstance()

    /**
     * Description get the list of saved locations
     *
     * @return list of locations
     * */
    public ArrayList<Location> getMyLocations() {
        return myLocations;
    } // end of getMyLocations()




} // end of MyLocations.java
