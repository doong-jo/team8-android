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
import android.graphics.Color;
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
import android.support.v4.app.Fragment;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

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
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.hawk.battery.widget.BatteryView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class InfoFragment extends Fragment
        implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
    private final static String TAG = InfoFragment.class.getSimpleName() + "/DEV";

    private BatteryView batView;
    private MapView mapView;
    private GoogleApiClient mGoogleApiClient;
    private GoogleMap mMap;
    private LocationRequest mLocationRequset;
    private Location mCurrentLocation;
    private Marker mCurrLocationMarker;
    private LatLng mBeforeLatlng = null;
    private boolean mIsRecorded = false;

    public double getmCurTrackingDistance() {
        return mCurTrackingDistance;
    }

    private double mCurTrackingDistance = 0.0;

    public List<LatLng> getmCurrRecordedLocationList() {
        return mCurrRecordedLocationList;
    }

    private List<LatLng> mCurrRecordedLocationList;

    private static final LatLng DEFAULT_LOCATION = new LatLng(37.56, 126.97);
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 2002;
    private static final int UPDATE_INTERVAL_MS = 15000;
    private static final int FASTEST_UPDATE_INTERVAL_MS = 15000;

    public InfoFragment() {

    }

    @SuppressWarnings("MissingPermission")
    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {

            List<Location> locationList = locationResult.getLocations();
            if (locationList.size() > 0) {
                //The last location in the list is the newest
                Location location = locationList.get(locationList.size() - 1);
                Toast.makeText(getActivity(), "new position : " + location.getLatitude() + ", " + location.getLongitude(), Toast.LENGTH_SHORT).show();
                mCurrentLocation = location;

                if (mCurrLocationMarker != null) {
                    mCurrLocationMarker.remove();
                }



                if (mMap != null) {
                    //Place current location marker
                    setCurrentLocation(location, "Current Position", "GPS Position");
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

                    if( mIsRecorded ) {
                        mCurrRecordedLocationList.add(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()));
                        Log.d(TAG, "onLocationChanged location list : " + mCurrRecordedLocationList.toString());

                        if( mBeforeLatlng != null ) {
                            Toast.makeText(getContext(), "draw polyline", Toast.LENGTH_SHORT).show();
                            mMap.addPolyline((new PolylineOptions())
                                    .add(
                                            mBeforeLatlng,
                                            latLng
                                    ).width(12).color(Color.BLUE)
                                    .geodesic(true));

                            mCurTrackingDistance += CalculationByDistance(mBeforeLatlng, latLng);

                            Log.d(TAG, "onLocationChanged mCurTrackingDistance : " + mCurTrackingDistance);
                            Toast.makeText(getContext(), "mCurTrackingDistance : " + mCurTrackingDistance, Toast.LENGTH_SHORT).show();
                        }
                    }
                    mBeforeLatlng = latLng;
                }
            }
        }
    };

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
        Log.i("Radius Value", "" + valueResult + "   KM  " + kmInDec
                + " Meter   " + meterInDec);

        Log.d(TAG, "CalculationByDistance Meter : " + meterInDec);

        Toast.makeText(getContext(), "Meter : " + meter, Toast.LENGTH_LONG).show();
        Toast.makeText(getContext(), "Meter desc : " + meterInDec, Toast.LENGTH_LONG).show();

        return meter;
    }

    public void setBluetoothReadData(String readData) {
        Log.d(TAG, "setBluetoothReadData : " + readData);
    }

    public void setCurrentLocation(Location location, String markerTitle, String markerSnippet) {
        if (mCurrLocationMarker != null) mCurrLocationMarker.remove();

        if (location != null) {
            //현재위치의 위도 경도 가져옴
            LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView called!");
        View view = inflater.inflate(R.layout.fragment_info, container, false);

        mCurrRecordedLocationList = new ArrayList<LatLng>();

        batView = (BatteryView) view.findViewById(R.id.batView);
        batView.setPower(78);

        //MapView 초기화 작업
        mapView = (MapView) view.findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        mapView.getMapAsync(this);

        //MapView
        adjustMapVerticalTouch(view);
        return view;
    }

    @Override
    public void onAttachFragment(Fragment childFragment) {
        super.onAttachFragment(childFragment);

        Toast.makeText(getContext(), "onAttachFragment InfoFragment", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @SuppressLint("ClickableViewAccessibility")
    public void adjustMapVerticalTouch(View view) {
        final NestedScrollView mainScrollView = (NestedScrollView) view.findViewById(R.id.infoFragment);
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

    public void requsetLocation() {
        if (mGoogleApiClient == null) {
            Log.d(TAG, "onMapReady have auth called!");
            buildGoogleApiClient();

            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            LocationServices.getFusedLocationProviderClient(getActivity()).getLastLocation().addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    // Got last known location. In some rare situations, this can be null.
                    if (location != null) {
                        // Logic to handle location object
                        Log.d(TAG, "onMapReady get location called!");
                        mCurrentLocation = location;

                        setCurrentLocation(mCurrentLocation, "GPS Position", "GPS Position");
                    }
                }
            });

            LocationServices.getFusedLocationProviderClient(getActivity()).requestLocationUpdates(mLocationRequset, mLocationCallback, Looper.myLooper());
        }

        if ( ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            mMap.setMyLocationEnabled(true);
        }
    }

    public void toggleRecordLocation(boolean toggle) {
        mIsRecorded = toggle;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady called!");

        mMap = googleMap;

        setCurrentLocation(null, "위치정보 가져올 수 없음", "위치 권한과 GPS 활성 여부 확인");

        //나침반이 나타나도록 설정
        mMap.getUiSettings().setCompassEnabled(true);
        //매끄럽게 이동함
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        if( mCurrentLocation == null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 15));
        } else {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()), 15));
            setCurrentLocation(mCurrentLocation, "Current Position", "GPS Position");
        }

        //updateLocationUI();

        //  API 23 이상이면 런타임 퍼미션 처리 필요
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 사용권한체크
            int hasFineLocationPermission = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION);

            if ( hasFineLocationPermission == PackageManager.PERMISSION_DENIED) {
                /*
                    사용권한이 없을경우
                    권한 재요청
                */
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            } else {
                //사용권한이 있는경우
                requsetLocation();
            }
        } else {

            if ( mGoogleApiClient == null) {
                buildGoogleApiClient();
            }

            googleMap.setMyLocationEnabled(true);
        }
    }

    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected called!");
//        if ( !checkLocationServicesStatus()) {
//            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//            builder.setTitle("위치 서비스 비활성화");
//            builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n" +
//                    "위치 설정을 수정하십시오.");
//            builder.setCancelable(true);
//            builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialogInterface, int i) {
//                    Intent callGPSSettingIntent =
//                            new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//                    startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
//                }
//            });
//            builder.setNegativeButton("취소", new DialogInterface.OnClickListener(){
//                @Override
//                public void onClick(DialogInterface dialogInterface, int i) {
//                    dialogInterface.cancel();
//                }
//            });
//            builder.create().show();
//        }
//

        //getDeviceLocation();
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));

//        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if ( ActivityCompat.checkSelfPermission(getActivity(),
//                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//
//                LocationServices.FusedLocationApi
//                        .requestLocationUpdates(mGoogleApiClient, locationRequest, this);
//            }
//        } else {
//            LocationServices.FusedLocationApi
//                    .requestLocationUpdates(mGoogleApiClient, locationRequest, this);
//
//            mMap.getUiSettings().setCompassEnabled(true);
//            mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
//        }


//        getDeviceLocation();
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
        Toast.makeText(getActivity(), "onLocationChanged!", Toast.LENGTH_LONG).show();
        Log.d(TAG, "onLocationChanged called!");
        mCurrentLocation = location;
        setCurrentLocation(mCurrentLocation, "내 위치", "GPS Position");
    }

    public void recordStopAndEraseLocationList() {
        mCurrRecordedLocationList.clear();
    }

    @Override
    public void onConnectionSuspended(int connect) {

    }

    private void createLocationRequest() {
        Log.d(TAG, "createLocationRequest called!");

        mLocationRequset = new LocationRequest();
        mLocationRequset.setInterval(UPDATE_INTERVAL_MS);//3000
        mLocationRequset.setFastestInterval(FASTEST_UPDATE_INTERVAL_MS);//1500
        mLocationRequset.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    protected synchronized  void buildGoogleApiClient() {
        Log.d(TAG, "buildGoogleApiClient called!");
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

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onLowMemory();
    }
//
//    @Override
//    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
//        super.onActivityCreated(savedInstanceState);
//
////액티비티가 처음 생성될 때 실행되는 함수
//
//        if(mapView != null)
//        {
//            mapView.onCreate(savedInstanceState);
//        }
//    }
//
//    @Override
//    public void onLocationChanged(Location location) {
//        mCurrentLocation = location;
//    }
//
//    @Override
//    public void onMapReady(GoogleMap googleMap) {
//        Log.d(TAG, "onMapReady: called!");
//        mMap = googleMap;
//        //updateLocationUI();
//    }
//

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
    //  Map Reference End
}