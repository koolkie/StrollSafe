package com.example.strollsafe.utils.location;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.work.ForegroundInfo;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.strollsafe.R;

@RequiresApi(api = Build.VERSION_CODES.O)
public class BackgroundLocationWork extends Worker {

    private NotificationManager notificationManager;
    private Context context;
    private String progress = "Starting work...";
    int NOTIFICATION_ID = 1;
    private LocationManager locationManager;
    private IntentFilter localBroadcastIntentFilter;



    public BackgroundLocationWork(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        locationManager = LocationManager.getInstance(context);

        localBroadcastIntentFilter = new IntentFilter();
        localBroadcastIntentFilter.addAction("background_location");
        LocalBroadcastManager.getInstance(context).registerReceiver(backgroundLocationBroadcastReceiver, localBroadcastIntentFilter);
    }

    @NonNull
    @Override
    public Result doWork() {
        setForegroundAsync(showNotification(progress));

        // infinite loop to keep it running
        while (true) {
            if (1 > 2) {
                break;
            }
            locationManager.startLocationUpdates();

            try {
                Thread.sleep(5000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return Result.success();
    }

    @NonNull
    private ForegroundInfo showNotification(String message) {
        return new ForegroundInfo(NOTIFICATION_ID, createNotification(message));
    }

    private Notification createNotification(String message) {
        String CHANNEL_ID = "100";
        String title = "Location is being tracked";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(
                    new NotificationChannel(CHANNEL_ID, title, NotificationManager.IMPORTANCE_HIGH));
        }

        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(title)
                .setTicker(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .build();

        return notification;
    }

    private void updateNotification(String message) {
        Notification notification = createNotification(message);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    BroadcastReceiver backgroundLocationBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("Broadcast", "Broadcasted");
            progress = intent.getStringExtra("location");
            updateNotification(progress);
        }
    };

    @Override
    public void onStopped() {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(backgroundLocationBroadcastReceiver);
        super.onStopped();
    }

}