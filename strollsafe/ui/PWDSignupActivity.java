package com.example.strollsafe.ui;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.BatteryManager;
import android.os.Bundle;

import com.example.strollsafe.R;
import com.example.strollsafe.pwd.PWD;
import com.example.strollsafe.utils.DatabaseManager;
import com.example.strollsafe.utils.location.BackgroundLocationWork;
import com.example.strollsafe.utils.location.LocationManager;
import com.example.strollsafe.utils.location.LocationPermissionManager;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.BackoffPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.bson.types.ObjectId;

import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.mongodb.App;
import io.realm.mongodb.Credentials;
import io.realm.mongodb.User;
import io.realm.mongodb.sync.SyncConfiguration;

@RequiresApi(api = Build.VERSION_CODES.Q)
public class PWDSignupActivity extends AppCompatActivity {

    public static final String PWD_CODE_PREFS_KEY = "PWDCODE";
    public static final String FIRST_NAME_PREFS_KEY = "FIRSTNAME";
    public static final String LAST_NAME_PREFS_KEY = "LASTNAME";
    public static final String PHONE_NUMBER_PREFS_KEY = "PHONENUMBER";
    public static final String BATTERY_LIFE_PREFS_KEY = "BATTERY";
    public static final String EMAILS_PREFS_KEY = "EMAIL";
    public static final String PASSWORD_PREFS_KEY = "PASSWORD";
    public static final String REALM_OBJECT_ID_PREFS_KEY = "REALMOBJECTID";
    DatabaseManager databaseManager;
    App app;
    private final String APP_ID = "strollsafe-pjbnn";
    private RealmConfiguration config;
    Realm realmDatabase;

    String TAG = "PWDSignupActivity/";
    private Button createPwdAccountButton;
    private EditText editPassword, editEmail, editPhoneNumber, editLastName, editFirstName;
    public int battery;
    private BroadcastReceiver mBatInfoReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL,0);
            battery = level;
        }
    };
    SharedPreferences pwdPreferences;
    SharedPreferences.Editor pwdPreferenceEditor;

    // location permissions
    private final int PERMISSION_REQUEST_CODE = 200;
    private String[] locationPermissions = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION};
    private String[] backgroundPermission = {Manifest.permission.ACCESS_BACKGROUND_LOCATION};
    private LocationPermissionManager locationPermissionManager;
    private LocationManager locationManager;
    private WorkRequest backgroundWorkRequest;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerReceiver(mBatInfoReciever,new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        databaseManager = new DatabaseManager(this);
        app = databaseManager.getApp();

        pwdPreferences = getSharedPreferences("PWD", MODE_PRIVATE);
        pwdPreferenceEditor = pwdPreferences.edit();

        locationPermissionManager = LocationPermissionManager.getInstance(PWDSignupActivity.this);
        locationManager = LocationManager.getInstance(PWDSignupActivity.this);

        setContentView(R.layout.activity_pwd_signup);
        createPwdAccountButton = (Button) findViewById(R.id.createPwdAccountButton);
        editFirstName = (EditText) findViewById(R.id.editFirstNamePWD);
        editLastName = (EditText) findViewById(R.id.editLastNamePWD);
        editPhoneNumber = (EditText) findViewById(R.id.editPhoneNumber);
        editEmail = (EditText) findViewById(R.id.editEmailAddress);
        editPassword = (EditText) findViewById(R.id.editPassword);


        configureBackButton();
        configureSignUpButton();
    } // end of onCreate()

    private static String getRandomString(int i){
        final String characters = "abcdefghiklmnopqrstuvwxyz0123456789";
        StringBuilder result = new StringBuilder();
        while( i > 0 ){
            Random rand = new Random();
            result.append(characters.charAt(rand.nextInt(characters.length())));
            i--;
        }
        return result.toString().toUpperCase(Locale.ROOT);
    }

    public void configureBackButton() {
        Button PWD = (Button) findViewById(R.id.pwdBackButton);
        PWD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    public void configureSignUpButton(){
        createPwdAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String pwdCode = getRandomString(4);
                ObjectId id = new ObjectId();
                String email = editEmail.getText().toString();
                String password = editPassword.getText().toString();
                String firstName = editFirstName.getText().toString();
                String lastName = editLastName.getText().toString();
                String phoneNumber = editPhoneNumber.getText().toString();

                app.getEmailPassword().registerUserAsync(email, password, createResult -> {
                    if (createResult.isSuccess()) {
                        Log.i(TAG, "Successfully registered user: " + email);
                        Credentials emailPasswordCredentials = Credentials.emailPassword(email, password);
                        AtomicReference<User> user = new AtomicReference<User>();

                        app.loginAsync(emailPasswordCredentials, loginResult -> {
                            if (loginResult.isSuccess()) {
                                Log.i(TAG + "asyncLoginToRealm", "Successfully authenticated using an email and password: " + email);

                                Log.i(TAG, "Adding user information to shared preferences.");


                                user.set(app.currentUser());
                                databaseManager.addCustomerUserData(user.get(), DatabaseManager.PWD_ACCOUNT_TYPE, email, phoneNumber, new Date(), "address", firstName, lastName,battery);
                                // ADD NEWLY CREATED PWD TO USER PREFS PUT IN A FUNCTION LATER
                                pwdPreferenceEditor.putString(PWD_CODE_PREFS_KEY, pwdCode);
                                pwdPreferenceEditor.putString(FIRST_NAME_PREFS_KEY, firstName);
                                pwdPreferenceEditor.putString(LAST_NAME_PREFS_KEY, lastName);
                                pwdPreferenceEditor.putString(PHONE_NUMBER_PREFS_KEY, phoneNumber);
                                pwdPreferenceEditor.putString(EMAILS_PREFS_KEY, email);
                                pwdPreferenceEditor.putString(PASSWORD_PREFS_KEY, password);
                                pwdPreferenceEditor.putString(REALM_OBJECT_ID_PREFS_KEY, id.toString());
                                pwdPreferenceEditor.apply();

                                config = new SyncConfiguration.Builder(Objects.requireNonNull(app.currentUser()), Objects.requireNonNull(app.currentUser()).getId())
                                        .name(APP_ID)
                                        .schemaVersion(2)
                                        .allowQueriesOnUiThread(true)
                                        .allowWritesOnUiThread(true)
                                        .build();

                                Realm.getInstanceAsync(config, new Realm.Callback() {
                                    @Override
                                    public void onSuccess(@NonNull Realm realm) {
                                        Log.v(TAG, "Successfully opened a realm with reads and writes allowed on the UI thread.");
                                        realmDatabase = realm;
                                        startActivity(new Intent(PWDSignupActivity.this, PwdHomeActivity.class));
                                    }
                                });

                                if (!locationPermissionManager.checkPermissions(locationPermissions)) {
                                    locationPermissionManager.askPermissions(PWDSignupActivity.this,
                                            locationPermissions, PERMISSION_REQUEST_CODE);
                                    if (!locationPermissionManager.checkPermissions(backgroundPermission)) {
                                        locationPermissionManager.askPermissions(PWDSignupActivity.this,
                                                locationPermissions, PERMISSION_REQUEST_CODE);
                                    }
                                } else {
                                    startLocationWork();
                                }

                            } else {
                                Log.e(TAG + "asyncLoginToRealm", "email: " + loginResult.getError().toString());
                                Toast.makeText(PWDSignupActivity.this, "email: " + loginResult.getError().toString(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Log.e(TAG, "Failed to register user: " + email + "\t" + createResult.getError().getErrorMessage());
                        Toast.makeText(PWDSignupActivity.this, "Failed to register user: " + email + "\t" + createResult.getError().getErrorMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, locationPermissions, grantResults);
        if (!locationPermissionManager.handlePermissionResult(PWDSignupActivity.this, requestCode,
                locationPermissions, grantResults)) {
            startLocationWork();
        }
    }

    private void startLocationWork() {
        backgroundWorkRequest = new OneTimeWorkRequest.Builder(BackgroundLocationWork.class)
                .addTag("LocationWork")
                .setBackoffCriteria(BackoffPolicy.LINEAR, OneTimeWorkRequest.MAX_BACKOFF_MILLIS, TimeUnit.SECONDS)
                .build();
        WorkManager.getInstance(PWDSignupActivity.this).enqueue(backgroundWorkRequest);
    }

}
