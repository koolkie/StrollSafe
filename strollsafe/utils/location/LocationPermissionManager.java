package com.example.strollsafe.utils.location;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;

public class LocationPermissionManager {
    private static LocationPermissionManager instance = null;
    private Context context;

    private LocationPermissionManager() {
    }

    public static LocationPermissionManager getInstance(Context context) {
        if (instance == null) {
            instance = new LocationPermissionManager();
        }
        instance.init(context);
        return instance;
    }

    private void init(Context context) {
        this.context = context;
    }

    public boolean checkPermissions(String[] permissions) {
        int size = permissions.length;

        for(int i = 0; i < size; i++) {
            if (ContextCompat.checkSelfPermission(context,
                    permissions[i]) == PermissionChecker.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }

    public void askPermissions(Activity activity, String[] permissions, int requestCode) {
        ActivityCompat.requestPermissions(activity, permissions, requestCode);
    }

    public boolean handlePermissionResult(Activity activity, int requestCode, String[] permissions,
                                          int[] grantResults) {
        boolean allPermissionsGranted = true;

        if (grantResults.length > 0) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("permission", "permission " + i + " granted");
                } else {
                    allPermissionsGranted = false;
                    Log.d("permission", "permission " + i + " denied");
                    showPermissionRational(activity, requestCode, permissions, permissions[i]);
                    break;
                }
            }
        } else {
            allPermissionsGranted = false;
        }
        return allPermissionsGranted;
    }

    private void showPermissionRational(Activity activity, int requestCode, String[] permissions,
                                        String deniedPermission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, deniedPermission)) {
                showMessageOKCancel("You need to allow access to the permission(s)!",
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    askPermissions(activity,permissions, requestCode);
                                }
                            }
                        });
            }
        }
    }

    void showMessageOKCancel(String message, DialogInterface.OnClickListener onClickListener) {
        new AlertDialog.Builder(context)
                .setMessage(message)
                .setPositiveButton("OK", onClickListener)
                .setNegativeButton("Cancel", onClickListener)
                .create()
                .show();
    }
}
