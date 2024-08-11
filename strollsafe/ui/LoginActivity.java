package com.example.strollsafe.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.strollsafe.R;
import com.example.strollsafe.utils.DatabaseManager;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.mongodb.App;
import io.realm.mongodb.Credentials;
import io.realm.mongodb.User;
import io.realm.mongodb.sync.SyncConfiguration;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG ="";
    private static final String APP_ID =  "strollsafe-pjbnn";

    App app;
    Realm realmDatabase;
    private RealmConfiguration config;
    private User user;
    DatabaseManager databaseManager;
    ProgressDialog progressSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        databaseManager = new DatabaseManager(this);
        app = databaseManager.getApp();
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);
        Toolbar topBar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(topBar);

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
                startActivity(new Intent(LoginActivity.this, SettingsActivity.class));
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    public void loginOnClick(View view) {
        EditText caregiverLoginEmail = findViewById(R.id.caregiverLoginEmail);
        EditText caregiverLoginPassword = findViewById(R.id.caregiverLoginPassword);
        String password = caregiverLoginPassword.getText().toString();
        String email = caregiverLoginEmail.getText().toString();

        progressSpinner = new ProgressDialog(this);
        progressSpinner.setIndeterminate(false);
        progressSpinner.setCancelable(true);
        progressSpinner.show();

        try {
            Credentials emailPasswordCredentials = Credentials.emailPassword(email, password);
            AtomicReference<User> user = new AtomicReference<User>();
            app.loginAsync(emailPasswordCredentials, it -> {
                if (it.isSuccess()) {
                    Log.i(TAG + "asyncLoginToRealm", "Successfully authenticated using an email and password: " + email);
                    user.set(app.currentUser());
                    RealmConfiguration config = new SyncConfiguration.Builder(Objects.requireNonNull(app.currentUser()), Objects.requireNonNull(app.currentUser()).getId())
                            .name(APP_ID)
                            .schemaVersion(2)
                            .allowQueriesOnUiThread(true)
                            .allowWritesOnUiThread(true)
                            .build();
                    Realm.getInstanceAsync(config, new Realm.Callback() {
                        @Override
                        public void onSuccess(@NonNull Realm realm) {
                            Log.v(TAG, "Successfully opened a realm with the given config.");
                            realmDatabase = realm;
                            progressSpinner.dismiss();
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));

                        }
                    });
                } else {
                    Log.e(TAG + "asyncLoginToRealm", "" + it.getError().toString());
                    Toast.makeText(this, "" + it.getError().toString(), Toast.LENGTH_LONG).show();
                    progressSpinner.dismiss();
                }
            });
        } catch (Exception e) {
            Log.e(TAG + "asyncLoginToRealm", "" + e.getLocalizedMessage());
            Toast.makeText(this, "" + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            progressSpinner.dismiss();
        }

    }

}
