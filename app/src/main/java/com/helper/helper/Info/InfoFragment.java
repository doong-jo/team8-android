package com.helper.helper.Info;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.NestedScrollView;
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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.hawk.battery.widget.BatteryView;
import com.helper.helper.R;
import com.helper.helper.ScrollingActivity;
import com.helper.helper.util.PermissionUtil;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class InfoFragment extends Fragment
        implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
    private final static String TAG = InfoFragment.class.getSimpleName() + "/DEV";

    private BatteryView m_batView;
    private MapView m_mapView;

    private GoogleApiClient m_googleApiClient;
    private GoogleMap m_googleMap;
    private LocationRequest m_locationReq;
    private Location m_curLocation;
    private Marker m_curLocationMarker;
    private static LatLng beforeLatlng = null;

    private double m_fCurDistance = 0.0;
    private List<LatLng> m_lCurRecordedLocation;

    private static final LatLng DEFAULT_LOCATION = new LatLng(37.56, 126.97);
    private static final int GPS_ENABLE_REQUEST_CODE = 2002;
    private static final int UPDATE_INTERVAL_MS = 15000;
    private static final int FASTEST_UPDATE_INTERVAL_MS = 15000;

    public double getCurTrackingDistance() {
        return m_fCurDistance;
    }
    public List<LatLng> getCurrRecordedLocationList() {
        return m_lCurRecordedLocation;
    }

    public InfoFragment() {

    }

    @SuppressWarnings("MissingPermission")
    private LocationCallback mLocationCallback;
    {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {

                List<Location> locationList = locationResult.getLocations();
                if (locationList.size() > 0) {
                    //The last location in the list is the newest
                    Location location = locationList.get(locationList.size() - 1);
                    Toast.makeText(getActivity(), "new position : " + location.getLatitude() + ", " + location.getLongitude(), Toast.LENGTH_SHORT).show();
                    m_curLocation = location;

                    if (m_curLocationMarker != null) {
                        m_curLocationMarker.remove();
                    }

                    setCurrentLocation(location, "Current Position", "GPS Position");
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

                    if (((ScrollingActivity) getActivity()).getIsRecorded()) {
                        m_lCurRecordedLocation.add(new LatLng(m_curLocation.getLatitude(), m_curLocation.getLongitude()));

                        if (beforeLatlng != null) {
                            m_googleMap.addPolyline((new PolylineOptions())
                                    .add(beforeLatlng, latLng)
                                    .width(R.dimen.google_polyline_width).color(Color.BLUE)
                                    .geodesic(true));

                            m_fCurDistance += CalculationByDistance(beforeLatlng, latLng);
                        }
                    }
                    beforeLatlng = latLng;
                }
            }
        };
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
        Log.i("Radius Value", "" + valueResult + "   KM  " + kmInDec
                + " Meter   " + meterInDec);

        Toast.makeText(getContext(), "Meter : " + meter, Toast.LENGTH_LONG).show();
        Toast.makeText(getContext(), "Meter desc : " + meterInDec, Toast.LENGTH_LONG).show();

        return meter;
    }

    public void setCurrentLocation(Location location, String markerTitle, String markerSnippet) {
        if (m_curLocationMarker != null) m_curLocationMarker.remove();

        if (location != null) {
            //현재위치의 위도 경도 가져옴
            LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());

            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(currentLocation);
            markerOptions.title(markerTitle);
            markerOptions.snippet(markerSnippet);
            markerOptions.draggable(true);
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
            m_curLocationMarker = this.m_googleMap.addMarker(markerOptions);

            this.m_googleMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
            return;
        }

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(DEFAULT_LOCATION);
        markerOptions.title(markerTitle);
        markerOptions.snippet(markerSnippet);
        markerOptions.draggable(true);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        m_curLocationMarker = this.m_googleMap.addMarker(markerOptions);

        this.m_googleMap.moveCamera(CameraUpdateFactory.newLatLng(DEFAULT_LOCATION));
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_info, container, false);

        m_lCurRecordedLocation = new ArrayList<LatLng>();

        m_batView = (BatteryView) view.findViewById(R.id.batView);
        m_batView.setPower(78);

        //MapView 초기화 작업
        m_mapView = (MapView) view.findViewById(R.id.map);
        m_mapView.onCreate(savedInstanceState);
        m_mapView.onResume();
        m_mapView.getMapAsync(this);

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
        m_mapView.onSaveInstanceState(outState);
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

    @SuppressLint("MissingPermission")
    public void requsetLocation() {
        if (m_googleApiClient == null) {
            buildGoogleApiClient();

            if( !PermissionUtil.checkPermissions(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) &&
                    !PermissionUtil.checkPermissions(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) ) {
                return;
            }

            LocationServices.getFusedLocationProviderClient(getActivity()).getLastLocation().addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        m_curLocation = location;

                        setCurrentLocation(m_curLocation, "GPS Position", "GPS Position");
                    }
                }
            });

            LocationServices.getFusedLocationProviderClient(getActivity()).requestLocationUpdates(m_locationReq, mLocationCallback, Looper.myLooper());
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        m_googleMap = googleMap;

        setCurrentLocation(null, "Unknown GPS signal", "Check your GPS permission");

        m_googleMap.getUiSettings().setCompassEnabled(true);
        m_googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        if( m_curLocation == null) {
            m_googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 15));
        } else {
            m_googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(m_curLocation.getLatitude(), m_curLocation.getLongitude()), 15));
            setCurrentLocation(m_curLocation, "Current Position", "GPS Position");
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if ( !PermissionUtil.checkPermissions(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) ||
                    !PermissionUtil.checkPermissions(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)) {

                /* Result about user selection -> onActivityResult in ScrollActivity */
                PermissionUtil.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, GPS_ENABLE_REQUEST_CODE);
            } else {
                requsetLocation();
            }
        } else {

            if ( m_googleApiClient == null) {
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
        m_googleMap.getUiSettings().setCompassEnabled(true);
        m_googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
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
        m_curLocation = location;
        setCurrentLocation(m_curLocation, "내 위치", "GPS Position");
    }

    public void recordStopAndEraseLocationList() {
        m_lCurRecordedLocation.clear();
    }

    @Override
    public void onConnectionSuspended(int connect) {

    }

    private void createLocationRequest() {
        m_locationReq = new LocationRequest();
        m_locationReq.setInterval(UPDATE_INTERVAL_MS);//3000
        m_locationReq.setFastestInterval(FASTEST_UPDATE_INTERVAL_MS);//1500
        m_locationReq.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    protected synchronized  void buildGoogleApiClient() {
        if(m_googleApiClient == null) {
            m_googleApiClient = new GoogleApiClient.Builder(getActivity())
                    .enableAutoManage((FragmentActivity) getActivity(), this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            m_googleApiClient.connect();
        }

        createLocationRequest();
    }

    @Override
    public void onStart() {
        super.onStart();
        m_mapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        m_mapView.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        m_mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        m_mapView.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        m_mapView.onLowMemory();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        m_mapView.onLowMemory();
    }
}