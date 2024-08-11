package com.example.strollsafe.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.widget.Toolbar;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.mongodb.App;

import com.example.strollsafe.R;
import com.example.strollsafe.utils.DatabaseManager;


public class MainActivity extends AppCompatActivity {
    // Global variables for the database access
    Realm database;
    App app;
    private final String appId = "strollsafe-pjbnn";
    RealmConfiguration config;
    DatabaseManager databaseManager;

    private final String TAG = "MainActivity/";

    // Shared preferences for storing caregiver information locally
    SharedPreferences caregiverPreferences;
    SharedPreferences.Editor caregiverPreferencesEditor;

    // Shared preferences for storing PWD information locally
    SharedPreferences pwdPreferences;
    SharedPreferences.Editor pwdPreferenceEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup the realm database
        databaseManager = new DatabaseManager(this);
        checkUserAccountType(databaseManager);

        // Setup the layout and UI of the activity
        setContentView(R.layout.activity_main);
        Toolbar topBar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(topBar);

        configureNewPwdButton();
        configureNewCaregiverButton();
        configureCaregiverLoginButton();

        // Setup the shared preferences
        caregiverPreferences = getSharedPreferences("CAREGIVER", MODE_PRIVATE);
        caregiverPreferencesEditor = caregiverPreferences.edit();

        pwdPreferences = getSharedPreferences("PWD", MODE_PRIVATE);
        pwdPreferenceEditor = pwdPreferences.edit();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        checkUserAccountType(databaseManager);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkUserAccountType(databaseManager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.optionsMenuItem:
                Log.i(TAG, "Starting SettingsActivity form MainActivity");
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    public void configureNewPwdButton(){
        Button PWD = (Button) findViewById(R.id.button_new_PWD);
        PWD.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Log.i(TAG, "Starting PWDLoginActivity form MainActivity");
                startActivity(new Intent(MainActivity.this, PWDSignupActivity.class));
            }
        });
    }

    public void configureNewCaregiverButton(){
        Button PWD = (Button) findViewById(R.id.button_new_caregiver);
        PWD.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Log.i(TAG, "Starting NewCaregiverActivity form MainActivity");
                startActivity(new Intent(MainActivity.this, CaregiverSignupActivity.class));
            }
        });
    }


    public void configureCaregiverLoginButton(){
        Button PWD = (Button) findViewById(R.id.button_toLogin);
        PWD.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Log.i(TAG, "Starting CaregiverLoginActivity form MainActivity");
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
        });
    }


    public void addObject() {
        database.executeTransaction(t -> {
            //Method 1: (May not work since we have to pass it a new ObjectId() as a primary key)
//             Adding objects to the database
//            Caregiver caregiver = new Caregiver();
//            t.insert(caregiver);

            //Method 2: should work fine
//            Caregiver testCaregiver = database.createObject(Caregiver.class, new ObjectId());
//            testCaregiver.setFirstName("Brittany");
//            testCaregiver.setLastName("Spears");
//            database.insert(testCaregiver);
//            Log.e("Object added", testCaregiver.toString());
        });
    }

    public void retrieveFromDatabase() {
//        RealmResults<PWD> caregiverRealmResults = database.where(PWD.class).findAll();
//        Log.d("",caregiverRealmResults.asJSON());

    }

    private void checkUserAccountType(DatabaseManager databaseManager) {
        if(databaseManager.isUserLoggedIn()) {
            switch(databaseManager.getUserAccountType()) {
                case DatabaseManager.CAREGIVER_ACCOUNT_TYPE:
                    Log.i(TAG, "Starting CaregiverPwdListActivity form MainActivity");
                    startActivity(new Intent(MainActivity.this, CaregiverPwdListActivity.class));
                    break;

                case DatabaseManager.PWD_ACCOUNT_TYPE:
                    Log.i(TAG, "Starting PwdHomeActivity form MainActivity");
                    startActivity(new Intent(MainActivity.this, PwdHomeActivity.class));
                    break;

                default:
                    Log.e(TAG, "Account type is not set in custom user data.");
                    break;

            }
        } else {
            Log.i(TAG, "No user is currently logged in.");
        }
    }

}
