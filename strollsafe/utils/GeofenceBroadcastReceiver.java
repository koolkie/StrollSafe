package com.example.strollsafe.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import androidx.core.app.NotificationCompat;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {
    private final String TAG = "GBR";

    public void onReceive(Context context, Intent intent) {
        Log.e(TAG, "TEST");
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            String errorMessage = GeofenceStatusCodes
                    .getStatusCodeString(geofencingEvent.getErrorCode());
            Log.e(TAG, errorMessage);
            return;
        }

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            // Get the transition details as a String.
            String geofenceTransitionDetails = getGeofenceTransitionDetails(geofencingEvent);

            // Send notification and log the transition details.
            //sendNotification(geofenceTransitionDetails);
            Log.i(TAG, geofenceTransitionDetails);
        } else {
            // Log the error.
            Log.e(TAG, "ERROR");
        }
    }

    private static String getGeofenceTransitionDetails(GeofencingEvent event) {
        String transitionString;
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("mm-ss");
        String formattedDate = df.format(c.getTime());

        int geofenceTransition = event.getGeofenceTransition();
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            transitionString = "IN-" + formattedDate;
        } else if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            transitionString = "OUT-" + formattedDate;
        } else {
            transitionString = "OTHER-" + formattedDate;
        }
        List<String> triggeringIDs;
        triggeringIDs = new ArrayList<>();
        for (Geofence geofence : event.getTriggeringGeofences()) {
            triggeringIDs.add(geofence.getRequestId());
        }
        return String.format("%s: %s", transitionString, TextUtils.join(", ", triggeringIDs));
    }

//    private void sendNotification(String notificationDetails) {
//
//        Intent notificationIntent = new Intent(Intent.ACTION_VIEW);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
////
////        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
////
////        builder.setColor(Notification.COLOR_DEFAULT)
////                .setContentTitle(notificationDetails)
////                .setContentText("Click notification to remove")
////                .setContentIntent(pendingIntent)
////                .setDefaults(Notification.DEFAULT_SOUND)
////                .setSmallIcon(R.mipmap.ic_launcher)
////                .setVibrate(new long[]{1000, 1000})
////                .setAutoCancel(true);
//
////        NotificationManager notificationManager =
////                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
////        notificationManager.notify(generateRandom(), builder.build());
//    }
}
