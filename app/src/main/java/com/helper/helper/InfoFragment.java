package com.helper.helper;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.app.Fragment;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.hawk.battery.widget.BatteryView;

import java.util.List;

public class InfoFragment extends Fragment
        implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
    private BatteryView batView;
    private MapView mapView;
    private GoogleApiClient mGoogleApiClient;
    private GoogleMap mMap;
    NestedScrollView mScrollView;
    private LocationRequest mLocationRequset;
    private Location mCurrentLocation;
    private Marker mCurrLocationMarker;

    private static final LatLng DEFAULT_LOCATION = new LatLng(37.56, 126.97);
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 2002;
    private static final int UPDATE_INTERVAL_MS = 15000;
    private static final int FASTEST_UPDATE_INTERVAL_MS = 15000;

//    private LocationCallback mLocationCallback = new LocationCallback() {
//        @Override
//        public void onLocationResult(LocationResult locationResult) {
//            List<Location> locationList = locationResult.getLocations();
//            if (locationList.size() > 0) {
//                //The last location in the list is the newest
//                Location location = locationList.get(locationList.size() - 1);
//                mCurrentLocation = location;
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
//
//                if(mMap != null) {
//                    mCurrLocationMarker = mMap.addMarker(markerOptions);
//
//                    //move map camera
//                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
//                }
//
//            }
//        }
//    };

    public void setCurrentLocation(Location location, String markerTitle, String markerSnippet) {
        if ( mCurrLocationMarker != null ) mCurrLocationMarker.remove();

        if ( location != null) {
            //현재위치의 위도 경도 가져옴
            LatLng currentLocation = new LatLng( location.getLatitude(), location.getLongitude());

            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(currentLocation);
            markerOptions.title(markerTitle);
            markerOptions.snippet(markerSnippet);
            markerOptions.draggable(true);
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
            mCurrLocationMarker = this.mMap.addMarker(markerOptions);

            this.mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
            return;
        }

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(DEFAULT_LOCATION);
        markerOptions.title(markerTitle);
        markerOptions.snippet(markerSnippet);
        markerOptions.draggable(true);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        mCurrLocationMarker = this.mMap.addMarker(markerOptions);

        this.mMap.moveCamera(CameraUpdateFactory.newLatLng(DEFAULT_LOCATION));
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("ClickableViewAccessibility")
    public void adjustMapVerticalTouch(View view) {
        final NestedScrollView mainScrollView = (NestedScrollView) getActivity().findViewById(R.id.scrollView);
        ImageView transparentImageView = (ImageView) view.findViewById(R.id.transparent_image);

        transparentImageView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        // Disallow ScrollView to intercept touch events.
                        mainScrollView.requestDisallowInterceptTouchEvent(true);
                        // Disable touch on transparent view
                        return false;

                    case MotionEvent.ACTION_UP:
                        // Allow ScrollView to intercept touch events.
                        mainScrollView.requestDisallowInterceptTouchEvent(false);
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        mainScrollView.requestDisallowInterceptTouchEvent(true);
                        return false;

                    default:
                        return true;
                }
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d("DEV", "onMapReady: called!");
        mMap = googleMap;

        setCurrentLocation(null, "위치정보 가져올 수 없음", "위치 권한과 GPS 활성 여부 확인");

        //나침반이 나타나도록 설정
        mMap.getUiSettings().setCompassEnabled(true);
        //매끄럽게 이동함
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        //updateLocationUI();

        //  API 23 이상이면 런타임 퍼미션 처리 필요
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 사용권한체크
            int hasFineLocationPermission = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION);

            if ( hasFineLocationPermission == PackageManager.PERMISSION_DENIED) {
                //사용권한이 없을경우
                //권한 재요청
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            } else {
                //사용권한이 있는경우
                if ( mGoogleApiClient == null) {
                    buildGoogleApiClient();
                }

                if ( ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                {
                    googleMap.setMyLocationEnabled(true);
                }
            }
        } else {

            if ( mGoogleApiClient == null) {
                buildGoogleApiClient();
            }

            googleMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
        View view = inflater.inflate( R.layout.fragment_info, container, false );

        mapView = (MapView)view.findViewById(R.id.map);
        mapView.getMapAsync(this);

        batView = (BatteryView) view.findViewById(R.id.batView);
        batView.setPower(78);

//        adjustMapVerticalTouch(view);
        return view;
    }

    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if ( !checkLocationServicesStatus()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("위치 서비스 비활성화");
            builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n" +
                    "위치 설정을 수정하십시오.");
            builder.setCancelable(true);
            builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent callGPSSettingIntent =
                            new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
                }
            });
            builder.setNegativeButton("취소", new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }
            });
            builder.create().show();
        }

        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL_MS);
        locationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_MS);

        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if ( ActivityCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                LocationServices.FusedLocationApi
                        .requestLocationUpdates(mGoogleApiClient, locationRequest, this);
            }
        } else {
            LocationServices.FusedLocationApi
                    .requestLocationUpdates(mGoogleApiClient, locationRequest, this);

            mMap.getUiSettings().setCompassEnabled(true);
            mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        }

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Location location = new Location("");
        location.setLatitude(DEFAULT_LOCATION.latitude);
        location.setLongitude((DEFAULT_LOCATION.longitude));

        setCurrentLocation(location, "위치정보 가져올 수 없음",
                "위치 퍼미션과 GPS활성 여부 확인");
    }

    @Override
    public void onLocationChanged(Location location) {
//        Log.i(TAG, "onLocationChanged call..");
//        searchCurrentPlaces();
    }

//    private void searchCurrentPlaces() {
//        @SuppressWarnings("MissingPermission")
//        PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi
//                .getCurrentPlace(googleApiClient, null);
//        result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>(){
//
//            @Override
//            public void onResult(@NonNull PlaceLikelihoodBuffer placeLikelihoods) {
//                int i = 0;
//                LikelyPlaceNames = new String[MAXENTRIES];
//                LikelyAddresses = new String[MAXENTRIES];
//                LikelyAttributions = new String[MAXENTRIES];
//                LikelyLatLngs = new LatLng[MAXENTRIES];
//
//                for(PlaceLikelihood placeLikelihood : placeLikelihoods) {
//                    LikelyPlaceNames[i] = (String) placeLikelihood.getPlace().getName();
//                    LikelyAddresses[i] = (String) placeLikelihood.getPlace().getAddress();
//                    LikelyAttributions[i] = (String) placeLikelihood.getPlace().getAttributions();
//                    LikelyLatLngs[i] = placeLikelihood.getPlace().getLatLng();
//
//                    i++;
//                    if(i > MAXENTRIES - 1 ) {
//                        break;
//                    }
//                }
//
//                placeLikelihoods.release();
//
//                Location location = new Location("");
//                location.setLatitude(LikelyLatLngs[0].latitude);
//                location.setLongitude(LikelyLatLngs[0].longitude);
//
//                setCurrentLocation(location, LikelyPlaceNames[0], LikelyAddresses[0]);
//            }
//        });
//
//    }

    @Override
    public void onConnectionSuspended(int connect) {

    }

    private void createLocationRequest() {
        mLocationRequset = new LocationRequest();
        mLocationRequset.setInterval(3000);//10000
        mLocationRequset.setFastestInterval(1500);//5000
        mLocationRequset.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    protected synchronized  void buildGoogleApiClient() {
        if(mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                    .enableAutoManage((FragmentActivity) getActivity(), this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            mGoogleApiClient.connect();
        }

        createLocationRequest();
    }
//
//    @Override
//    public void onLocationChanged(Location location) {
//        mCurrentLocation = location;
//    }
//
//    @Override
//    public void onMapReady(GoogleMap googleMap) {
//        Log.d("DEV", "onMapReady: called!");
//        mMap = googleMap;
//        //updateLocationUI();
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode,
//                                           @NonNull String[] permissinos,
//                                           @NonNull int[] grantResults) {
//        if (requestCode == PermissionUtil.REQUEST_LOCATION) {
//            if( PermissionUtil.verifyPermission(grantResults)) {
//                mLocationPermissionGranted = true;
//
//                adjustMapVerticalTouch();
//                buildGoogleApiClient();
//                mGoogleApiClient.connect();
//            } else {
//                showRequestAgainDialog();
//            }
//        } else {
//            super.onRequestPermissionsResult(requestCode, permissinos, grantResults);
//        }
//    }
//
//    public void showRequestAgainDialog() {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setMessage("원활한 앱 사용을 위해서는 꼭 필요한 권한이므로 설정에서 권한을 사용으로 설정해주시기 바랍니다.");
//        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//                try {
//                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
//                            .setData(Uri.parse("package:" + getPackageName()));
//                    startActivity(intent);
//                } catch (ActivityNotFoundException e) {
//                    e.printStackTrace();
//                    Intent intent = new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
//                    startActivity(intent);
//                }
//            }
//        });
//        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//                //취소했음
//            }
//        });
//        builder.create();
//    }
//
//    public void adjustMapVerticalTouch() {
//        final NestedScrollView mainScrollView = (NestedScrollView) findViewById(R.id.scrollView);
//        ImageView transparentImageView = (ImageView) findViewById(R.id.transparent_image);
//
//        transparentImageView.setOnTouchListener(new View.OnTouchListener() {
//
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                int action = event.getAction();
//                switch (action) {
//                    case MotionEvent.ACTION_DOWN:
//                        // Disallow ScrollView to intercept touch events.
//                        mainScrollView.requestDisallowInterceptTouchEvent(true);
//                        // Disable touch on transparent view
//                        return false;
//
//                    case MotionEvent.ACTION_UP:
//                        // Allow ScrollView to intercept touch events.
//                        mainScrollView.requestDisallowInterceptTouchEvent(false);
//                        return true;
//
//                    case MotionEvent.ACTION_MOVE:
//                        mainScrollView.requestDisallowInterceptTouchEvent(true);
//                        return false;
//
//                    default:
//                        return true;
//                }
//            }
//        });
//
//        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
//        mapFragment.getMapAsync(this);
//    }
    //  Map Reference End
}