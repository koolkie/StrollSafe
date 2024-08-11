package com.example.strollsafe.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import com.example.strollsafe.utils.GeofencingMapsActivity;
import com.example.strollsafe.R;

import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.EditText;
import android.widget.Toast;

import com.example.strollsafe.utils.DatabaseManager;

import org.bson.Document;

import java.util.ArrayList;

import io.realm.mongodb.App;
import io.realm.mongodb.mongo.MongoCollection;


public class CaregiverPwdListActivity extends AppCompatActivity {
    App app;
    DatabaseManager databaseManager;
    String TAG = "ListOfPWDActivity";
    SharedPreferences pwdPreferences;
    SharedPreferences.Editor pwdPreferenceEditor;
    ProgressDialog progressDialog;
    public NotificationCompat.Builder builder = new NotificationCompat.Builder(this,"Battery Notification");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pwdPreferences = getSharedPreferences("PWD", MODE_PRIVATE);
        pwdPreferenceEditor = pwdPreferences.edit();
        databaseManager = new DatabaseManager(this);
        app = databaseManager.getApp();
        setContentView(R.layout.activity_listofpwd);
        Toolbar topBar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(topBar);

        /*builder.setSmallIcon(R.drawable.ic_launcher_background);
        builder.setContentTitle("Battery Notification");
        builder.setContentText(" has low battery of ");builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        builder.setAutoCancel(true);

        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(CaregiverPwdListActivity.this);
        managerCompat.notify(1, builder.build());

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel Channel = new NotificationChannel("Battery Notification","Battery notification",NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(Channel);
        }*/
        batteryNotification();
        configureMap1();
        configureMap2();
        configureMap3();
        configureDelete1();
        configureDelete2();
        configureDelete3();
        stylePage();
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_options, menu);
        return true;
    }

    public void batteryNotification() {
        /*EditText editText = (EditText) findViewById(R.id.pwdListEmailEntry);
        String code = editText.getText().toString();
        MongoCollection userCollection = databaseManager.getUsersCollection();
        userCollection.findOne(new Document("email",code)).getAsync(new App.Callback() {
            @Override
            public void onResult(App.Result result) {
                Document pwdInfo = (Document) result.get();
                if (pwdInfo == null) {
                    dismissProgressDialog();
                    Toast.makeText(CaregiverPwdListActivity.this, "PWD can not be found.", Toast.LENGTH_SHORT).show();
                    return;
                }*/
                //if(Integer.parseInt(pwdInfo.get("batteryLife").toString())<=15){
        builder.setSmallIcon(R.drawable.ic_launcher_background);
        builder.setContentTitle("Battery Notification");
        builder.setContentText( " has low battery of ");
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        builder.setAutoCancel(true);

        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(CaregiverPwdListActivity.this);
        managerCompat.notify(1, builder.build());

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel Channel = new NotificationChannel("Battery Notification","Battery notification",NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(Channel);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.optionsMenuItem:
                Log.i(TAG, "Starting SettingsActivity form CaregiverPwdListActivity");
                startActivity(new Intent(CaregiverPwdListActivity.this, SettingsActivity.class));
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    public void configureMap1(){
        ImageButton PWD = (ImageButton) findViewById(R.id.Map1);
        PWD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startProgressDialog("Opening Safe Zone Manager...", false);
                app.currentUser().refreshCustomData(refreshResult -> {
                    if(refreshResult.isSuccess()) {
                        ArrayList<String> patientsList = (ArrayList<String>) app.currentUser().getCustomData().get("patients");
                        String userId = patientsList.get(0);
                        Intent intent = new Intent(CaregiverPwdListActivity.this, GeofencingMapsActivity.class);
                        intent.putExtra("userId", userId);
                        startActivity(intent);
                    }
                    dismissProgressDialog();
                });
            }
        });
    }

    public void configureMap2(){
        ImageButton PWD = (ImageButton) findViewById(R.id.Map2);
        PWD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startProgressDialog("Opening Safe Zone Manager...", false);
                app.currentUser().refreshCustomData(refreshResult -> {
                    if(refreshResult.isSuccess()) {
                        ArrayList<String> patientsList = (ArrayList<String>) app.currentUser().getCustomData().get("patients");
                        String userId = patientsList.get(1);
                        Intent intent = new Intent(CaregiverPwdListActivity.this, GeofencingMapsActivity.class);
                        intent.putExtra("userId", userId);
                        startActivity(intent);
                    }
                    dismissProgressDialog();
                });
            }
        });
    }

    public void configureMap3(){
        ImageButton PWD = (ImageButton) findViewById(R.id.Map3);
        PWD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startProgressDialog("Opening Safe Zone Manager...", false);
                app.currentUser().refreshCustomData(refreshResult -> {
                    if(refreshResult.isSuccess()) {
                        ArrayList<String> patientsList = (ArrayList<String>) app.currentUser().getCustomData().get("patients");
                        String userId = patientsList.get(2);
                        Intent intent = new Intent(CaregiverPwdListActivity.this, GeofencingMapsActivity.class);
                        intent.putExtra("userId", userId);
                        startActivity(intent);
                    }
                    dismissProgressDialog();
                });
            }
        });
    }

    public void configureDelete1(){
        ImageButton PWD = (ImageButton) findViewById(R.id.delete1);
        ImageButton A = (ImageButton) findViewById(R.id.Map1);
        Button B = (Button) findViewById(R.id.NAME1);

        PWD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                app.currentUser().refreshCustomData(refreshResult -> {
                    if(refreshResult.isSuccess()) {
                        ArrayList<String> patientsList = (ArrayList<String>) app.currentUser().getCustomData().get("patients");
                        String userId = patientsList.get(0);
                        remove(userId);

                        B.setText("CLICK HERE TO ACTIVATE");
                        PWD.setVisibility(View.GONE);
                        A.setVisibility(View.GONE);
                        B.setVisibility(View.GONE);
                    }
                });
            }
        });
    }

    public void configureDelete2(){
        ImageButton PWD = (ImageButton) findViewById(R.id.delete2);
        ImageButton A = (ImageButton) findViewById(R.id.Map2);
        Button B = (Button) findViewById(R.id.NAME2);

        PWD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                app.currentUser().refreshCustomData(refreshResult -> {
                    if(refreshResult.isSuccess()) {
                        ArrayList<String> patientsList = (ArrayList<String>) app.currentUser().getCustomData().get("patients");
                        String userId = patientsList.get(1);
                        remove(userId);

                        B.setText("CLICK HERE TO ACTIVATE");
                        PWD.setVisibility(View.GONE);
                        A.setVisibility(View.GONE);
                        B.setVisibility(View.GONE);
                    }
                });
            }
        });
    }

    public void configureDelete3(){
        ImageButton PWD = (ImageButton) findViewById(R.id.delete3);
        ImageButton A = (ImageButton) findViewById(R.id.Map3);
        Button B = (Button) findViewById(R.id.NAME3);

        PWD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                app.currentUser().refreshCustomData(refreshResult -> {
                    if(refreshResult.isSuccess()) {
                        ArrayList<String> patientsList = (ArrayList<String>) app.currentUser().getCustomData().get("patients");
                        String userId = patientsList.get(2);
                        remove(userId);

                        B.setText("CLICK HERE TO ACTIVATE");
                        PWD.setVisibility(View.GONE);
                        A.setVisibility(View.GONE);
                        B.setVisibility(View.GONE);
                    }
                });
            }
        });
    }

    public void stylePage() {
        startProgressDialog("Loading patients...", false);
        app.currentUser().refreshCustomData(refreshResult -> {
            ArrayList<String> patientsList = (ArrayList<String>) app.currentUser().getCustomData().get("patients");
            if(patientsList != null) {
                if(patientsList.size() >= 3) {
                    findViewById(R.id.pwdListTitle).setVisibility(View.INVISIBLE);
                    findViewById(R.id.pwdListEmailEntry).setVisibility(View.INVISIBLE);
                    findViewById(R.id.pwdListAddButton).setVisibility(View.INVISIBLE);
                    dismissProgressDialog();

                } else {
                    findViewById(R.id.pwdListTitle).setVisibility(View.VISIBLE);
                    findViewById(R.id.pwdListEmailEntry).setVisibility(View.VISIBLE);
                    findViewById(R.id.pwdListAddButton).setVisibility(View.VISIBLE);
                    dismissProgressDialog();
                }
                for(int x = 0; x < patientsList.size(); x++) {
                    MongoCollection userCollection = databaseManager.getUsersCollection();
                    int finalX = x;
                    userCollection.findOne(new Document("userId", patientsList.get(x))).getAsync(new App.Callback() {
                        @Override
                        public void onResult(App.Result result) {
                            if(result.isSuccess()) {
                                Document pwdInfo = (Document) result.get();
                                Button name1 = (Button) findViewById(R.id.NAME1);
                                ImageButton delete1 = (ImageButton) findViewById(R.id.delete1);

                                Button name2 = (Button) findViewById(R.id.NAME2);
                                ImageButton delete2 = (ImageButton) findViewById(R.id.delete2);
                                ImageButton map2 = (ImageButton) findViewById(R.id.Map2);

                                ImageButton map1 = (ImageButton) findViewById(R.id.Map1);
                                Button name3 = (Button) findViewById(R.id.NAME3);
                                ImageButton delete3 = (ImageButton) findViewById(R.id.delete3);
                                ImageButton map3 = (ImageButton) findViewById(R.id.Map3);

                                switch (finalX) {
                                    case 0:
                                        Log.i(TAG, "STYLING THE FIRST BUTTON");
                                        name1.setText(pwdInfo.get("firstName") + " " + pwdInfo.get("lastName"));
                                        name1.setVisibility(View.VISIBLE);
                                        delete1.setVisibility(View.VISIBLE);
                                        map1.setVisibility(View.VISIBLE);

                                        if(finalX == patientsList.size() - 1) {
                                            dismissProgressDialog();
                                            delete2.setVisibility(View.INVISIBLE);
                                            name2.setVisibility(View.INVISIBLE);
                                            map2.setVisibility(View.INVISIBLE);

                                            delete3.setVisibility(View.INVISIBLE);
                                            name3.setVisibility(View.INVISIBLE);
                                            map3.setVisibility(View.INVISIBLE);
                                        }

                                        break;

                                    case 1:
                                        Log.i(TAG, "STYLING THE SECOND BUTTON");
                                        name2.setText(pwdInfo.get("firstName") + " " + pwdInfo.get("lastName"));
                                        delete2.setVisibility(View.VISIBLE);
                                        name2.setVisibility(View.VISIBLE);
                                        map2.setVisibility(View.VISIBLE);

                                        if(finalX == patientsList.size() - 1) {
                                            dismissProgressDialog();
                                            delete3.setVisibility(View.INVISIBLE);
                                            name3.setVisibility(View.INVISIBLE);
                                            map3.setVisibility(View.INVISIBLE);
                                        }
                                        break;

                                    case 2:
                                        Log.i(TAG, "STYLING THE THIRD BUTTON");
                                        name3.setText(pwdInfo.get("firstName") + " " + pwdInfo.get("lastName"));
                                        delete3.setVisibility(View.VISIBLE);
                                        name3.setVisibility(View.VISIBLE);
                                        map3.setVisibility(View.VISIBLE);

                                        if(finalX == patientsList.size() - 1) {
                                            dismissProgressDialog();
                                        }
                                        break;
                                }
                            }
                        }
                    });
                }
            }
            dismissProgressDialog();
        });
    }

    public void add(View view){
        startProgressDialog("Adding patient...", false);
        app.currentUser().refreshCustomData(refreshResult -> {
            ArrayList<String> patientsArray = (ArrayList<String>) app.currentUser().getCustomData().get("patients");
            if(patientsArray == null || patientsArray.size() >= 3) {
                dismissProgressDialog();
                Toast.makeText(CaregiverPwdListActivity.this, "PWD limit max already reached.", Toast.LENGTH_SHORT).show();
                return;
            }

            EditText editText = (EditText) findViewById(R.id.pwdListEmailEntry);
            String code = editText.getText().toString();

            MongoCollection userCollection = databaseManager.getUsersCollection();
            userCollection.findOne(new Document("email", code)).getAsync(new App.Callback() {
                @Override
                public void onResult(App.Result result) {
                    Document pwdInfo = (Document) result.get();
                    if(pwdInfo == null) {
                        dismissProgressDialog();
                        Toast.makeText(CaregiverPwdListActivity.this, "PWD can not be found.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if(patientsArray.contains(pwdInfo.get("userId"))) {
                        dismissProgressDialog();
                        Toast.makeText(CaregiverPwdListActivity.this, "This PWD is already linked to your account.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Document careGiverData = new Document("userId", app.currentUser().getId());
                    Document update =  new Document("patients", pwdInfo.get("userId"));
                    userCollection.updateOne(careGiverData, new Document("$push", update)).getAsync(new App.Callback() {
                        @Override
                        public void onResult(App.Result result) {
                            if(result.isSuccess()) {
                                dismissProgressDialog();
                                Toast.makeText(CaregiverPwdListActivity.this, "PWD has been linked to your account.", Toast.LENGTH_SHORT).show();
                                EditText enterPatientEmail = findViewById(R.id.pwdListEmailEntry);
                                enterPatientEmail.getText().clear();
                                stylePage();
                            } else {
                                dismissProgressDialog();
                                Toast.makeText(CaregiverPwdListActivity.this, "Error linked PWD to your account.", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            });
        });
    }

    public void remove(String userId) {
        startProgressDialog("Removing patient...", false);
        app.currentUser().refreshCustomData(refreshResult -> {
            MongoCollection userCollection = databaseManager.getUsersCollection();
            Document careGiverData = new Document("userId", app.currentUser().getId());
            Document update =  new Document("patients", userId);
            userCollection.updateOne(careGiverData, new Document("$pull", update)).getAsync(new App.Callback() {
                @Override
                public void onResult(App.Result result) {
                    if(result.isSuccess()) {
                        dismissProgressDialog();
                        Toast.makeText(CaregiverPwdListActivity.this, "PWD has been removed to your account.", Toast.LENGTH_SHORT).show();
                        stylePage();
                    } else {
                        dismissProgressDialog();
                        Toast.makeText(CaregiverPwdListActivity.this, "Error removing PWD to your account.", Toast.LENGTH_LONG).show();
                    }
                }
            });
        });
    }

    private void startProgressDialog(String message, boolean cancelable) {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(message);
        progressDialog.setIndeterminate(false);
        progressDialog.setCancelable(cancelable);
        progressDialog.show();
    }

    private void dismissProgressDialog() {
        progressDialog.dismiss();
    }

}
