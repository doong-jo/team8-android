/*
 * Copyright (c) 10/18/18 10:54 AM
 * Written by Sungdong Jo
 * Description:
 */

package com.helper.helper.controller;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.location.Location;
import android.os.Looper;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.helper.helper.view.main.myeight.InfoFragment;

import java.text.DecimalFormat;
import java.util.List;

public class GoogleMapManager {
    private final static String TAG = InfoFragment.class.getSimpleName() + "/DEV";
    public static final LatLng DEFAULT_LOCATION = new LatLng(37.56, 126.97);
    private static final int UPDATE_INTERVAL_MS = 3000;
    private static final int FASTEST_UPDATE_INTERVAL_MS = 3000;

    private static GoogleApiClient m_googleApiClient;
    private static LocationRequest m_locationReq;
    private static Location m_curLocation;
    private static Activity m_activity;
    private static LocationCallback m_cloneLocationCallback;
    private static LocationCallback m_locatinCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (m_cloneLocationCallback != null) {
                m_cloneLocationCallback.onLocationResult(locationResult);
            }

            List<Location> locationList = locationResult.getLocations();
            if (locationList.size() > 0) {
                Location location = locationList.get(locationList.size() - 1);
                setCurrentLocation(location);
            }
        }
    };

    public static void setLocationCallbackClone(LocationCallback callback) {
        m_cloneLocationCallback = callback;
    }

    public static Location getCurLocation() {
        return m_curLocation;
    }

    public static void initGoogleMap(Activity activity) {
        m_activity = activity;

        buildGoogleApiClient();
    }

    private static synchronized void buildGoogleApiClient() {
        if (m_googleApiClient == null) {
            m_googleApiClient = new GoogleApiClient.Builder(m_activity)
                    .enableAutoManage((FragmentActivity) m_activity, (GoogleApiClient.OnConnectionFailedListener) m_activity)
                    .addConnectionCallbacks((GoogleApiClient.ConnectionCallbacks) m_activity)
                    .addOnConnectionFailedListener((GoogleApiClient.OnConnectionFailedListener) m_activity)
                    .addApi(LocationServices.API)
                    .build();
            m_googleApiClient.connect();
        }

        createLocationRequest();
    }

    private static void createLocationRequest() {
        m_locationReq = new LocationRequest();
        m_locationReq.setInterval(UPDATE_INTERVAL_MS);
        m_locationReq.setFastestInterval(FASTEST_UPDATE_INTERVAL_MS);
        m_locationReq.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        requsetLocation();
    }


    public static void setCurrentLocation(Location location) {
        m_curLocation = location;
    }

    @SuppressLint("MissingPermission")
    private static void requsetLocation() {
        if (!PermissionManager.checkPermissions(m_activity, Manifest.permission.ACCESS_FINE_LOCATION) &&
                !PermissionManager.checkPermissions(m_activity, Manifest.permission.ACCESS_COARSE_LOCATION)) {
            return;
        }

        LocationServices.getFusedLocationProviderClient(m_activity).getLastLocation().addOnSuccessListener(m_activity, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    m_curLocation = location;
                }
            }
        });

        LocationServices.getFusedLocationProviderClient(m_activity).requestLocationUpdates(m_locationReq, m_locatinCallback, Looper.myLooper());
    }

    private static double getSpeed(Location curL, Location beforeL) {
        // meter / millisecond
        Log.d(TAG + "/speed", "getSpeed curTime: " + curL.getTime());
        Log.d(TAG + "/speed", "getSpeed befTime: " + beforeL.getTime());
        Log.d(TAG + "/speed", "distanceTo: " + curL.distanceTo(beforeL));
        // meter - > km, second -> hour
        final double speed = (curL.distanceTo(beforeL) / 1000) / ((curL.getTime() - beforeL.getTime()) / 1000 * 60 * 60);
        return speed;
    }

    public double CalculationByDistance(LatLng StartP, LatLng EndP) {
        int Radius = 6371;// radius of earth in Km
        double lat1 = StartP.latitude;
        double lat2 = EndP.latitude;
        double lon1 = StartP.longitude;
        double lon2 = EndP.longitude;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        double valueResult = Radius * c;
        double km = valueResult / 1;
        DecimalFormat newFormat = new DecimalFormat("####");
        int kmInDec = Integer.valueOf(newFormat.format(km));
        double meter = valueResult % 1000;
        int meterInDec = Integer.valueOf(newFormat.format(meter));

        return meter;
    }
}