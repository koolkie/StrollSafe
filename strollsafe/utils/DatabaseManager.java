package com.example.strollsafe.utils;

import android.content.Context;
import android.util.Log;

import org.bson.Document;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.AppException;
import io.realm.mongodb.Credentials;
import io.realm.mongodb.User;
import io.realm.mongodb.mongo.MongoClient;
import io.realm.mongodb.mongo.MongoCollection;
import io.realm.mongodb.mongo.MongoDatabase;
import io.realm.mongodb.sync.SyncConfiguration;

/**
 * The DatabaseManager class is responsible for handling the login, account creation and login, and
 * various database configuration functions.
 *
 * @author Steve Statia
 * @since 2022-07-19
 */
public class DatabaseManager {
    public static final String CAREGIVER_ACCOUNT_TYPE = "caregiver";
    public static final String PWD_ACCOUNT_TYPE = "pwd";
    private Realm realmDatabase;
    private App app;
    private final String APP_ID = "strollsafe-pjbnn";
    private final String TAG = "DatabaseManager/";
    private boolean isUserLoggedIn = false;

    /**
     * Initializes a new DatabaseManager
     * @param context The activity context this is being called in.
     */
    public DatabaseManager(Context context) {
        Realm.init(context);
        app = new App(new AppConfiguration.Builder(APP_ID).build());
    }


    /**
     * Creates an account for the database, and leaves it in a pending state. To activate the
     * account and remove it from the pending state, the account must be used to login.
     * App users can be viewed here:
     * https://realm.mongodb.com/groups/62ab69d80c54cf25ba32b567/apps/62c53c26ebb532e9b5847441/auth/users
     * @param email email for the user account being created.
     * @param password password for the user account being created.
     */
    public void createRealmUserAccount(String email, String password) {
        app.getEmailPassword().registerUserAsync(email, password, it -> {
            if (it.isSuccess()) {
                Log.i(TAG, "Successfully registered user: " + email);
            } else {
                Log.e(TAG, "Failed to register user: " + email + "\t" + it.getError().getErrorMessage());
            }
        });
    }

    /**
     * Creates a user and logs them into the app
     * @param email
     * @param password
     */
    public void createRealmUserAndLoginAsync(String email, String password, String accountType, String phoneNumber, Date dateOfBirth, String address, String firstName, String lastName,int batteryLife) {
        app.getEmailPassword().registerUserAsync(email, password, registerResult -> {
            if (registerResult.isSuccess()) {
                Log.i(TAG, "Successfully registered user: " + email);
                Log.i(TAG, "Logging in...");
                try {
                    Credentials emailPasswordCredentials = Credentials.emailPassword(email, password);
                    AtomicReference<User> user = new AtomicReference<User>();
                    app.loginAsync(emailPasswordCredentials, loginResult -> {
                        if (loginResult.isSuccess()) {
                            Log.i(TAG + "asyncLoginToRealm", "Successfully authenticated using an email and password: " + email);
                            user.set(app.currentUser());
                            isUserLoggedIn = true;
                            RealmConfiguration config = new SyncConfiguration.Builder(Objects.requireNonNull(app.currentUser()), Objects.requireNonNull(app.currentUser()).getId())
                                    .name(APP_ID)
                                    .schemaVersion(2)
                                    .allowQueriesOnUiThread(true)
                                    .allowWritesOnUiThread(true)
                                    .build();
                            getRealmInstance(config);
                            addCustomerUserData(user.get(), accountType, email, phoneNumber, dateOfBirth, address, firstName, lastName,batteryLife);
                        } else {
                            Log.e(TAG + "asyncLoginToRealm", "email: " + loginResult.getError().toString());
                            isUserLoggedIn = false;
                        }
                    });
                } catch (Exception e) {
                    Log.e(TAG + "asyncLoginToRealm", "" + e.getLocalizedMessage());
                }
            } else {
                Log.e(TAG, "Failed to register user: " + email + "\t" + registerResult.getError().getErrorMessage());
            }
        });
    }

    /**
     * Login to the realm database.
     * @param email to login with
     * @param password to login with (at least 6 characters)
     */
    public void asyncLoginToRealm(String email, String password) {
        try {
            Credentials emailPasswordCredentials = Credentials.emailPassword(email, password);
            AtomicReference<User> user = new AtomicReference<User>();
            app.loginAsync(emailPasswordCredentials, it -> {
                if (it.isSuccess()) {
                    Log.i(TAG + "asyncLoginToRealm", "Successfully authenticated using an email and password: " + email);
                    user.set(app.currentUser());
                    isUserLoggedIn = true;
                    RealmConfiguration config = new SyncConfiguration.Builder(Objects.requireNonNull(app.currentUser()), Objects.requireNonNull(app.currentUser()).getId())
                            .name(APP_ID)
                            .schemaVersion(2)
                            .allowQueriesOnUiThread(true)
                            .allowWritesOnUiThread(true)
                            .build();
                    getRealmInstance(config);
                } else {
                    Log.e(TAG + "asyncLoginToRealm", "email: " + it.getError().toString());
                    isUserLoggedIn = false;
                }
            });
        } catch (Exception e) {
            Log.e(TAG + "asyncLoginToRealm", "" + e.getLocalizedMessage());
        }
    }

    /**
     * Have not tested it yet
     * @param email
     * @param password
     */
    public void loginToRealm(String email, String password) {
        new Thread() {
            public void run() {
                Credentials emailPasswordCredentials = Credentials.emailPassword(email, password);
                try {
                    AtomicReference<User> user = new AtomicReference<User>();
                    user.set(app.login(emailPasswordCredentials));
                    RealmConfiguration config = new SyncConfiguration.Builder(Objects.requireNonNull(app.currentUser()), Objects.requireNonNull(app.currentUser()).getId())
                            .name(APP_ID)
                            .schemaVersion(2)
                            .allowQueriesOnUiThread(true)
                            .allowWritesOnUiThread(true)
                            .build();
                    getRealmInstance(config);

                } catch (AppException e) {
                    Log.e("AUTH", "ERROR:" + e.getErrorMessage());
                }
            }
        }.start();
    }

    /**
     * Checks the app to see if the user is logged in.
     * @return if the user is currently logged into realm.
     */
    public boolean isUserLoggedIn() {
        if(app.currentUser() == null) {
            isUserLoggedIn = false;
            return false;
        } else {
            isUserLoggedIn = true;
            return true;
        }
    }

    /**
     * Logs the current user our of the Realm instance.
     * @return if logout was successful.
     */
    public boolean logoutOfRealm() {
        AtomicBoolean loggedOut = new AtomicBoolean(false);
        app.currentUser().logOutAsync(callback -> {
            if(callback.isSuccess()) {
                // User logged out
                loggedOut.set(true);
            } else {
                // User did not log out
                loggedOut.set(false);
            }
        });
        return loggedOut.get();
    }

    /**
     * @return User that is currently logged in
     */
    public User getLoggedInUser() {
        return app.currentUser();
    }

    /**
     * @return Realm object for database transactions
     */
    public Realm getRealmDatabase() {
        return realmDatabase;
    }

    /**
     * @return App that is used by the database.
     */
    public App getApp() {
        return app;
    }

    private void getRealmInstance(RealmConfiguration configuration) {
        Realm.getInstanceAsync(configuration, new Realm.Callback() {
            @Override
            public void onSuccess(@NonNull Realm realm) {
                Log.v(TAG, "Successfully opened a realm with the given config.");
                realmDatabase = realm;
            }
        });
    }

    public void addCustomerUserData(User currentUser, String accountType, String email, String phoneNumber, Date dateOfBirth, String address, String firstName, String lastName,int batteryLife) {
        MongoClient mongoClient = currentUser.getMongoClient("user-data");
        MongoDatabase mongoDatabase = mongoClient.getDatabase("strollSafeTest");
        MongoCollection<Document> mongoCollection = mongoDatabase.getCollection("users");
        if(accountType == CAREGIVER_ACCOUNT_TYPE) {
            mongoCollection.insertOne(new Document("userId", currentUser.getId())
                            .append("accountType", accountType)
                            .append("email", email)
                            .append("phoneNumber", phoneNumber)
                            .append("address", address)
                            .append("firstName", firstName)
                            .append("lastName", lastName)
                            .append("dateOfBirth", dateOfBirth)
                            .append("patients", new ArrayList<String>()))
                    .getAsync(result -> {
                        if (result.isSuccess()) {
                            Log.d(TAG, String.format("User %s has been added to the database", email));
                        } else {
                            Log.e(TAG, String.format("Unable to insert custom user data for user %s. Error: %s", email, result.getError()));
                        }
                    });

        } else if(accountType == PWD_ACCOUNT_TYPE) {
            mongoCollection.insertOne(new Document("userId", currentUser.getId())
                            .append("accountType", accountType)
                            .append("email", email)
                            .append("phoneNumber", phoneNumber)
                            .append("address", address)
                            .append("firstName", firstName)
                            .append("lastName", lastName)
                            .append("dateOfBirth", dateOfBirth)
                            .append("batteryLife", batteryLife)
                            .append("safezones", new ArrayList<Double[][]>())
                            .append("caregivers", new ArrayList<String>()))
                    .getAsync(result -> {
                        if (result.isSuccess()) {
                            Log.d(TAG, String.format("User %s has been added to the database", email));
                        } else {
                            Log.e(TAG, String.format("Unable to insert custom user data for user %s. Error: %s", email, result.getError()));
                        }
                    });
        }

    }

    /**
     * Queries the custom data that is set at sing up for the user for the account type.
     * @return string with the type of user account currently logged in (caregiver, pwd, null)
     */
    public String getUserAccountType() {
        String accountType = getLoggedInUser().getCustomData().getString("accountType");
        if(isUserLoggedIn() && accountType != null) {
            switch (accountType.toLowerCase()) {
                case DatabaseManager.CAREGIVER_ACCOUNT_TYPE:
                    return DatabaseManager.CAREGIVER_ACCOUNT_TYPE;

                case DatabaseManager.PWD_ACCOUNT_TYPE:
                    return DatabaseManager.PWD_ACCOUNT_TYPE;

                default:
                    return "null";
            }
        }
        return "null";
    }

    public MongoCollection getUsersCollection() {
        MongoClient mongoClient = app.currentUser().getMongoClient("user-data");
        MongoDatabase mongoDatabase = mongoClient.getDatabase("strollSafeTest");
        return mongoDatabase.getCollection("users");
    }

}
