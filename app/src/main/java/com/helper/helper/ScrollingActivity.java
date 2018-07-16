package com.helper.helper;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApi;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.tasks.OnSuccessListener;
import com.hawk.battery.widget.BatteryView;

public class ScrollingActivity extends AppCompatActivity implements OnMapReadyCallback,
                                                                    GoogleApiClient.ConnectionCallbacks,
                                                                    GoogleApiClient.OnConnectionFailedListener,
                                                                    LocationListener {
    private BatteryView batView;
    private GoogleApiClient mGoogleApiClient;
    private GoogleMap mMap;
    LocationRequest mLocationRequset;
    Location mCurrentLocation;
    private Boolean mLocationPermissionGranted = false;

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        getDeviceLocation();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {

    }

    @Override
    public void onConnectionSuspended(int connect) {

    }

    private void createLocationRequest() {
        mLocationRequset = new LocationRequest();
        mLocationRequset.setInterval(10000);
        mLocationRequset.setFastestInterval(5000);
        mLocationRequset.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    protected synchronized  void buildGoogleApiClient() {
        if(mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .enableAutoManage(this, this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        createLocationRequest();
    }

    @SuppressWarnings("MissingPermission")
    private void getDeviceLocation() {
        if (PermissionUtil.checkPermissions(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            {
                mLocationPermissionGranted = true;
                LocationServices.getFusedLocationProviderClient(this).getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations, this can be null.
                        if (location != null) {
                            // Logic to handle location object
                            mCurrentLocation = location;
                        }
                    }
                });
                mCurrentLocation = LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(mLocationRequset);
//                mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
//                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequset, this);
            }
        } else PermissionUtil.requestLocationsPermissions(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //updateLocationUI();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissinos,
                                           @NonNull int[] grantResults) {
        if (requestCode == PermissionUtil.REQUEST_LOCATION) {
            if( PermissionUtil.verifyPermission(grantResults)) {
                mLocationPermissionGranted = true;
            } else {
                showRequestAgainDialog();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissinos, grantResults);
        }
    }

    public void showRequestAgainDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("원활한 앱 사용을 위해서는 꼭 필요한 권한이므로 설정에서 권한을 사용으로 설정해주시기 바랍니다.");
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                try {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                .setData(Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
                    startActivity(intent);
                }
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //취소했음
            }
        });
        builder.create();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        toolbar.setTitleTextColor(0x161616);
        setSupportActionBar(toolbar);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        batView = (BatteryView) findViewById(R.id.batView);
        batView.setPower(78);

        if(PermissionUtil.checkPermissions(this, Manifest.permission.ACCESS_FINE_LOCATION)
                || (PermissionUtil.checkPermissions(this, Manifest.permission.ACCESS_COARSE_LOCATION))) {

        } else {
            PermissionUtil.requestLocationsPermissions(this);
        }

        buildGoogleApiClient();
        mGoogleApiClient.connect();

//        mGoogleApiClient = new GoogleApiClient.Builder(this)
//                .enableAutoManage(this /* FragmentActivity */,
//                        this /* OnConnectionFailedListener */)
//                .addConnectionCallbacks(this)
//                .addApi(LocationServices.API)
//                .addApi(Places.GEO_DATA_API)
//                .addApi(Places.PLACE_DETECTION_API)
//                .build();
//        mGoogleApiClient.connect();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_scrolling, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
