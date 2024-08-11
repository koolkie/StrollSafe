/**
 * ShowSavedLocationsList.java
 *
 * Description: Show the list of saved locations as addresses
 *
 * Created on: July 18, 2022
 * Created by: Alvin Tsang
 *
 * Last modified on; July 21, 2022
 * Last modified by: Alvin Tsang
 *
 * */

package com.example.strollsafe.ui.location;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import com.example.strollsafe.R;
import com.example.strollsafe.pwd.PWDLocation;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.ArrayList;

@RequiresApi(api = Build.VERSION_CODES.O)
public class ShowSavedLocationsList extends AppCompatActivity {

    private static final String SHARED_PREFS = "StrollSafe: LocationList";
    private ArrayList<PWDLocation> PWDLocationList;
    private LocationListViewAdapter adapter;
    private RecyclerView rv_locationList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_saved_locations_list);

        rv_locationList = findViewById(R.id.rv_locationList);

        // get list of saved locations
        loadData();
        buildRecyclerView();

    } // end of onCreate()


    private void buildRecyclerView() {
        // initializing our adapter class.
        adapter = new LocationListViewAdapter(PWDLocationList, ShowSavedLocationsList.this);

        // adding layout manager to our recycler view.
        LinearLayoutManager manager = new LinearLayoutManager(this);
        rv_locationList.setHasFixedSize(true);

        // setting layout manager to our recycler view.
        rv_locationList.setLayoutManager(manager);

        // setting adapter to our recycler view.
        rv_locationList.setAdapter(adapter);
    }



    /**
     * Description: Read the shared preference folder for the list of saved locations and
     *              store them in an arraylist
     * */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void loadData() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);

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
} // end of ShowSavedLocationsList.java
