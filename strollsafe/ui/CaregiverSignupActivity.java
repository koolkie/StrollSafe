package com.example.strollsafe.ui;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.strollsafe.R;
import com.example.strollsafe.utils.DatabaseManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.mongodb.App;
import io.realm.mongodb.Credentials;
import io.realm.mongodb.User;
import io.realm.mongodb.sync.SyncConfiguration;

public class CaregiverSignupActivity extends AppCompatActivity {
    private int mYear,mMonth,mDay;
    DatabaseManager databaseManager;
    App app;
    int battery =100;
    private User user;
    private final String APP_ID = "strollsafe-pjbnn";
    private RealmConfiguration config;
    Realm realmDatabase;
    String TAG = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        databaseManager = new DatabaseManager(this);
        app = databaseManager.getApp();
        setContentView(R.layout.activity_caregiver_signup);

        final TextView pickDate = (TextView) findViewById(R.id.caregiverBirthday);
        final TextView textView = (TextView) findViewById(R.id.caregiverBirthday);

        final Calendar myCalendar = Calendar.getInstance();
        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                // myCalendar.add(Calendar.DATE, 0);
                String myFormat = "yyyy-MM-dd"; //In which you need put here
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.CANADA);
                textView.setText(sdf.format(myCalendar.getTime()));
            }
        };

        pickDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                final Calendar c = Calendar.getInstance();
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);

                // Launch Date Picker Dialog
                DatePickerDialog dpd = new DatePickerDialog(CaregiverSignupActivity.this,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                // Display Selected date in textbox

                                if (year < mYear)
                                    view.updateDate(mYear,mMonth,mDay);

                                if (monthOfYear < mMonth && year == mYear)
                                    view.updateDate(mYear,mMonth,mDay);

                                if (dayOfMonth < mDay && year == mYear && monthOfYear == mMonth)
                                    view.updateDate(mYear,mMonth,mDay);

                                textView.setText(dayOfMonth + "-"
                                        + (monthOfYear + 1) + "-" + year);

                            }
                        }, mYear, mMonth, mDay);
                dpd.getDatePicker().setMinDate(System.currentTimeMillis());
                dpd.show();

            }
        });
    }


    public void createUserLoginAndUploadObjectFromFields(View view) {
        EditText emailEditText = findViewById(R.id.caregiverEmail);
        EditText passwordEditText = findViewById(R.id.caregiverPassword);
        EditText firstNameEditText = findViewById(R.id.caregiverFirstName);
        EditText lastNameEditText = findViewById(R.id.caregiverLastName);
        EditText phoneNumberEditText = findViewById(R.id.caregiverPhoneNumber);
        EditText addressEditText = findViewById(R.id.caregiverAddress);

        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        String firstName = firstNameEditText.getText().toString();
        String lastName = lastNameEditText.getText().toString();
        String phoneNumber = phoneNumberEditText.getText().toString();
        String address = addressEditText.getText().toString();

        app.getEmailPassword().registerUserAsync(email, password, createResult -> {
            if (createResult.isSuccess()) {
                Log.i(TAG, "Successfully registered user: " + email);
                Credentials emailPasswordCredentials = Credentials.emailPassword(email, password);
                AtomicReference<User> user = new AtomicReference<User>();

                app.loginAsync(emailPasswordCredentials, loginResult -> {
                    if (loginResult.isSuccess()) {
                        Log.i(TAG + "asyncLoginToRealm", "Successfully authenticated using an email and password: " + email);
                        user.set(app.currentUser());
                        databaseManager.addCustomerUserData(user.get(), DatabaseManager.CAREGIVER_ACCOUNT_TYPE, email, phoneNumber, new Date(), address, firstName, lastName,battery);

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
                                startActivity(new Intent(CaregiverSignupActivity.this, CaregiverPwdListActivity.class));
                            }
                        });
                    } else {
                        Log.e(TAG + "asyncLoginToRealm", "email: " + loginResult.getError().toString());
                        Toast.makeText(this, "email: " + loginResult.getError().toString(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Log.e(TAG, "Failed to register user: " + email + "\t" + createResult.getError().getErrorMessage());
                Toast.makeText(this, "Failed to register user: " + email + "\t" + createResult.getError().getErrorMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}
