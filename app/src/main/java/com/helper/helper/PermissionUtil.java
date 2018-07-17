package com.helper.helper;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

public class PermissionUtil {
    public static final int REQUEST_STORAGE = 1;
    public static final int REQUEST_LOCATION = 2;
    private static String[] PERMISSIONS_LOCATION = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public static int checkSelfPermission(@NonNull Context context, @NonNull String permission) {
        if (permission == null) {
            throw new IllegalArgumentException("permission is null");
        }

        return context.checkPermission(permission, android.os.Process.myPid(), android.os.Process.myUid());
    }

    public static boolean checkPermissions(Activity activity, String permission) {
        int permissionResult = ActivityCompat.checkSelfPermission(activity, permission);
        if (permissionResult == PackageManager.PERMISSION_GRANTED) return true;
        else return false;
    }

    public static void requestLocationsPermissions(Activity activity) {
        ActivityCompat.requestPermissions(activity, PERMISSIONS_LOCATION, REQUEST_LOCATION);
    }

    public static void requestExternalPermissions(Activity activity) {
        ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, REQUEST_STORAGE);
    }

    public static void requestPermissions(final @NonNull Activity activity,
                                          final @NonNull String[] permissions, final int requestCode) {
        if (Build.VERSION.SDK_INT >= 23) {
            ActivityCompat.requestPermissions(activity, permissions, requestCode);
        } else if (activity instanceof ActivityCompat.OnRequestPermissionsResultCallback) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    final int[] grantResults = new int[permissions.length];

                    PackageManager packageManager = activity.getPackageManager();
                    String packageName = activity.getPackageName();

                    final int permissionCount = permissions.length;
                    for (int i = 0; i < permissionCount; i++) {
                        grantResults[i] = packageManager.checkPermission(
                                permissions[i], packageName);
                    }

                    ((ActivityCompat.OnRequestPermissionsResultCallback) activity).onRequestPermissionsResult(
                            requestCode, permissions, grantResults);
                }
            });
        }
    }
    public static boolean verifyPermission(int[] grantresults) {
        if (grantresults.length < 1) {
            return false;
        }
        for(int result : grantresults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        return true;
    }
}
