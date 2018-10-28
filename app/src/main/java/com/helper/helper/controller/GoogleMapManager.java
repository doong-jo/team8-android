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

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.helper.helper.view.main.InfoFragment;

import java.util.List;

public class GoogleMapManager {
    private final static String TAG = InfoFragment.class.getSimpleName() + "/DEV";
    public static final LatLng DEFAULT_LOCATION = new LatLng(37.56, 126.97);
    private static final int UPDATE_INTERVAL_MS = 3000;
    private static final int FASTEST_UPDATE_INTERVAL_MS = 3000;

    private static GoogleApiClient m_googleApiClient;
    private static GoogleMap m_googleMap;
    private static float m_zoomLevel;
    private static LocationRequest m_locationReq;
    private static Location m_curLocation;
    private static Activity m_activity;
    private static LocationCallback m_locatinCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            List<Location> locationList = locationResult.getLocations();
            if (locationList.size() > 0) {
                //The last location in the list is the newest
                Location location = locationList.get(locationList.size() - 1);
                setCurrentLocation(location, "myLocation", "location");
//                Log.i("MapsActivity", "Location: " + location.getLatitude() + " " + location.getLongitude());
//                mLastLocation = location;
//                if (mCurrLocationMarker != null) {
//                    mCurrLocationMarker.remove();
//                }
//
//                //Place current location marker
//                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
//                MarkerOptions markerOptions = new MarkerOptions();
//                markerOptions.position(latLng);
//                markerOptions.title("Current Position");
//                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
//                mCurrLocationMarker = mGoogleMap.addMarker(markerOptions);
//
//                //move map camera
//                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 11));
            }
        }
    };

    public static GoogleMap getGoogleMap() { return m_googleMap; }

    public static Location getCurLocation() { return m_curLocation; }

    public static float getZoomLevel() { return m_zoomLevel; }

    public static void initGoogleMap(Activity activity) {
        m_activity = activity;

        buildGoogleApiClient();
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//
//        } else {
//
//            if ( m_googleApiClient == null) {
//                buildGoogleApiClient();
//            }
//        }
    }

    private static synchronized void buildGoogleApiClient() {
        if(m_googleApiClient == null) {
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
        m_locationReq.setInterval(UPDATE_INTERVAL_MS);//3000
        m_locationReq.setFastestInterval(FASTEST_UPDATE_INTERVAL_MS);//1500
        m_locationReq.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        requsetLocation();
    }


    public static void setCurrentLocation(Location location, String markerTitle, String markerSnippet) {
//        if (m_curLocationMarker != null) m_curLocationMarker.remove();

        m_curLocation = location;

//        if (location != null) {
//            LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());

//            MarkerOptions markerOptions = new MarkerOptions();
//            markerOptions.position(currentLocation);
//            markerOptions.title(markerTitle);
//            markerOptions.snippet(markerSnippet);
//            markerOptions.draggable(true);
//            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
////            m_curLocationMarker = this.m_googleMap.addMarker(markerOptions);
//
//            m_googleMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
//            return;
//        }

//        MarkerOptions markerOptions = new MarkerOptions();
//        markerOptions.position(DEFAULT_LOCATION);
//        markerOptions.title(markerTitle);
//        markerOptions.snippet(markerSnippet);
//        markerOptions.draggable(true);
//        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
////        m_curLocationMarker = this.m_googleMap.addMarker(markerOptions);
//
//        m_googleMap.moveCamera(CameraUpdateFactory.newLatLng(DEFAULT_LOCATION));
    }

    @SuppressLint("MissingPermission")
    public static void requsetLocation() {
        if (!PermissionManager.checkPermissions(m_activity, Manifest.permission.ACCESS_FINE_LOCATION) &&
                !PermissionManager.checkPermissions(m_activity, Manifest.permission.ACCESS_COARSE_LOCATION)) {
            return;
        }

        LocationServices.getFusedLocationProviderClient(m_activity).getLastLocation().addOnSuccessListener(m_activity, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    m_curLocation = location;

                    setCurrentLocation(m_curLocation, "GPS Position", "GPS Position");
                }
            }
        });

        LocationServices.getFusedLocationProviderClient(m_activity).requestLocationUpdates(m_locationReq, m_locatinCallback, Looper.myLooper());
    }

    /** Override googlemap **/
//    @Override
//    public void onMapReady(GoogleMap googleMap) {
//        m_googleMap = googleMap;
//
//        setCurrentLocation(null, "Unknown GPS signal", "Check your GPS permission");
//
//        m_googleMap.getUiSettings().setCompassEnabled(true);
//        m_googleMap.getUiSettings().setMyLocationButtonEnabled(true);
//        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return;
//        }
//        m_googleMap.setMyLocationEnabled(true);
//
//        m_googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
//
//        if( m_curLocation == null) {
//            m_googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 15));
//        } else {
//            m_googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(m_curLocation.getLatitude(), m_curLocation.getLongitude()), 15));
//            setCurrentLocation(m_curLocation, "Current Position", "GPS Position");
//        }
//    }
//
//    public boolean checkLocationServicesStatus() {
//        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
//
//        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
//                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
//    }
//
//    @Override
//    public void onConnected(@Nullable Bundle bundle) {
//        m_googleMap.getUiSettings().setCompassEnabled(true);
//        m_googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
//    }
//x
//    @Override
//    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
//        Location location = new Location("");
//        location.setLatitude(DEFAULT_LOCATION.latitude);
//        location.setLongitude((DEFAULT_LOCATION.longitude));
//
//        setCurrentLocation(null, "Unknown GPS signal", "Check your GPS permission");
//    }
//
//    @Override
//    public void onLocationChanged(Location location) {
//        m_curLocation = location;
//        Toast.makeText(getContext(), "LocationChaged : " + location.getSpeed(), Toast.LENGTH_SHORT).show();
//        setCurrentLocation(m_curLocation, "내 위치", "GPS Position");
//    }
//
//    public void recordStopAndEraseLocationList() {
//        m_lCurRecordedLocation.clear();
//    }
//
//    @Override
//    public void onConnectionSuspended(int connect) {
//
//    }

}
