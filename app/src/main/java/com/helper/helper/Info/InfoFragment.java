package com.helper.helper.Info;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.NestedScrollView;
import android.util.Log;
import android.view.LayoutInflater;
import android.support.v4.app.Fragment;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
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

import org.w3c.dom.Text;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class InfoFragment extends Fragment
        implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
    private final static String TAG = InfoFragment.class.getSimpleName() + "/DEV";
    private static final LatLng DEFAULT_LOCATION = new LatLng(37.56, 126.97);
    private static final int GPS_ENABLE_REQUEST_CODE = 2002;
    private static final int SEND_SMS_REQUEST_CODE = 947;
    private static final int UPDATE_INTERVAL_MS = 1500;
    private static final int FASTEST_UPDATE_INTERVAL_MS = 1000;
    private static final float EMENRGENCY_SPPED_PIVOT = 0.8f;

    private static final int ORIENTATION_LEFT = 944;
    private static final int ORIENTATION_RIGHT = 344;
    private static final int ORIENTATION_NONE = 892;
    private static final int EMERGENCY = 121;

    private BatteryView m_batView;
    private MapView m_mapView;
    private GoogleApiClient m_googleApiClient;
    private GoogleMap m_googleMap;
    private LocationRequest m_locationReq;
    private Location m_curLocation;
    private Marker m_curLocationMarker;
    private static LatLng m_beforeLatlng = null;
    private float m_curSpeed;

    private double m_fCurDistance = 0.0;
    private List<LatLng> m_lCurRecordedLocation;

    private TextView m_textViewTiltX;
    private TextView m_textViewTiltY;
    private TextView m_textViewTiltZ;

    private TextView m_textBeforeSpeed;
    private TextView m_textCurSpeed;

    private SeekBar m_brightSeekbar;
    private SeekBar m_speedSeekbar;

    private ImageView m_curLEDView;

    public double getCurTrackingDistance() {
        return m_fCurDistance;
    }

    public List<LatLng> getCurrRecordedLocationList() {
        return m_lCurRecordedLocation;
    }

    public void setCurLEDView(int gifDrawable) {
        GlideDrawableImageViewTarget gifimage = new GlideDrawableImageViewTarget(m_curLEDView);
        Glide.with(this).load(gifDrawable).into(gifimage);
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
                    m_curLocation = location;

                    ((ScrollingActivity)getActivity()).setMapPosition(location.getLatitude(), location.getLongitude(), location);

                    String writeStr = "";

                    m_textCurSpeed.setText("현재 속도 : " + String.format("%f", location.getSpeed()));
                    m_textBeforeSpeed.setText("이전 속도 : " + String.format("%f", m_curSpeed));

//                    if( location.getSpeed() >= 1 && location.getSpeed() * EMENRGENCY_SPPED_PIVOT < m_curSpeed ) {
                    if( location.getSpeed() < m_curSpeed ) {
                        writeStr = "0-08-1";
                        Toast.makeText(getContext(), "EMENRGENCY_SPPED : " + location.getSpeed(), Toast.LENGTH_SHORT).show();

                        ((ScrollingActivity)getActivity()).setcurInterrupt(EMERGENCY);
                    }

                    int curInterrutState = ((ScrollingActivity)getActivity()).getcurInterruptState();

                    if ( curInterrutState == EMERGENCY && location.getSpeed() >= m_curSpeed ) {
                        writeStr = "0-01-0";

                        ((ScrollingActivity)getActivity()).setcurInterrupt(ORIENTATION_NONE);
                    }

                    try{
                        ((ScrollingActivity)getActivity()).sendToBluetoothDevice(writeStr.getBytes());
                    }
                    catch (NullPointerException e) {
                        e.printStackTrace();
                    }


//                    Toast.makeText(getContext(), "C: " + location.getSpeed() + " / B: " + m_curSpeed, Toast.LENGTH_SHORT).show();

                    m_curSpeed = location.getSpeed();
//                    Toast.makeText(getContext(), "onLocationResult : "+ location.getSpeed(), Toast.LENGTH_SHORT).show();

                    if (m_curLocationMarker != null) {
                        m_curLocationMarker.remove();
                    }

                    setCurrentLocation(location, "Current Position", "GPS Position");
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

                    try{
                        if (((ScrollingActivity) getActivity()).getIsRecorded()) {
                            m_lCurRecordedLocation.add(new LatLng(m_curLocation.getLatitude(), m_curLocation.getLongitude()));

                            if (m_beforeLatlng != null) {
                                m_googleMap.addPolyline((new PolylineOptions())
                                        .add(m_beforeLatlng, latLng)
                                        .width(R.dimen.google_polyline_width).color(Color.BLUE)
                                        .geodesic(true));

                                m_fCurDistance += CalculationByDistance(m_beforeLatlng, latLng);
                            }
                        }
                        m_beforeLatlng = latLng;
                    }
                    catch (NullPointerException e) {
                        e.printStackTrace();
                    }

                }
            }
        };
    }

    private SeekBar.OnSeekBarChangeListener m_seekBarBrightChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            String str = "2-" + String.format("%02d-%d", i/10, i%10);
            ((ScrollingActivity)getActivity()).sendToBluetoothDevice(str.getBytes());
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    private SeekBar.OnSeekBarChangeListener m_seekBarSpeedChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            String str = "1-" + String.format("%02d-%d", i/10, i%10);
            ((ScrollingActivity)getActivity()).sendToBluetoothDevice(str.getBytes());
        }


        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

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

        return meter;
    }

    public void setCurrentLocation(Location location, String markerTitle, String markerSnippet) {
        if (m_curLocationMarker != null) m_curLocationMarker.remove();

        if (location != null) {
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

        if (m_mapView == null) {
            m_mapView = (MapView) view.findViewById(R.id.map);
            m_mapView.onCreate(savedInstanceState);
            m_mapView.onResume();
            m_mapView.getMapAsync(this);
        }


        adjustMapVerticalTouch(view);

        m_textViewTiltX = (TextView) view.findViewById(R.id.TiltX);
        m_textViewTiltY = (TextView) view.findViewById(R.id.TiltY);
        m_textViewTiltZ = (TextView) view.findViewById(R.id.TiltZ);

        m_textCurSpeed = (TextView) view.findViewById(R.id.curSpeed);
        m_textBeforeSpeed = (TextView) view.findViewById(R.id.beforeSpeed);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if ( !PermissionUtil.checkPermissions(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) ||
                    !PermissionUtil.checkPermissions(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)) {

                /* Result about user selection -> onActivityResult in ScrollActivity */
                PermissionUtil.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, GPS_ENABLE_REQUEST_CODE);
            } else {
                if ( m_googleApiClient == null) {
                    buildGoogleApiClient();
                }
                requsetLocation();
            }

            if ( !PermissionUtil.checkPermissions(getActivity(), Manifest.permission.SEND_SMS) ) {

                /* Result about user selection -> onActivityResult in ScrollActivity */
                PermissionUtil.requestPermissions(getActivity(), new String[]{Manifest.permission.SEND_SMS}, SEND_SMS_REQUEST_CODE);
            } else {

            }


        } else {

            if ( m_googleApiClient == null) {
                buildGoogleApiClient();
            }
        }

        m_brightSeekbar = (SeekBar) view.findViewById(R.id.brightSeek);
        m_speedSeekbar = (SeekBar) view.findViewById(R.id.speedSeek);

        m_brightSeekbar.setOnSeekBarChangeListener(m_seekBarBrightChangeListener);
        m_speedSeekbar.setOnSeekBarChangeListener(m_seekBarSpeedChangeListener);

        m_curLEDView = (ImageView) view.findViewById(R.id.InfoFragment_curLED);

        return view;
    }


    @Override
    public void onAttachFragment(Fragment childFragment) {
        super.onAttachFragment(childFragment);
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
        if (!PermissionUtil.checkPermissions(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) &&
                !PermissionUtil.checkPermissions(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)) {
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        m_googleMap = googleMap;

        setCurrentLocation(null, "Unknown GPS signal", "Check your GPS permission");

        m_googleMap.getUiSettings().setCompassEnabled(true);
        m_googleMap.getUiSettings().setMyLocationButtonEnabled(true);
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
        m_googleMap.setMyLocationEnabled(true);

        m_googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));

        if( m_curLocation == null) {
            m_googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 15));
        } else {
            m_googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(m_curLocation.getLatitude(), m_curLocation.getLongitude()), 15));
            setCurrentLocation(m_curLocation, "Current Position", "GPS Position");
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

        setCurrentLocation(null, "Unknown GPS signal", "Check your GPS permission");
    }

    @Override
    public void onLocationChanged(Location location) {
        m_curLocation = location;
        Toast.makeText(getContext(), "LocationChaged : " + location.getSpeed(), Toast.LENGTH_SHORT).show();
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
        if( m_googleApiClient != null ) {
            m_googleApiClient.stopAutoManage(getActivity());
            m_googleApiClient.disconnect();
        }

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

    public void setTextTiltXYZ(float[] values) {

        if( m_textViewTiltX == null || m_textViewTiltY == null || m_textViewTiltZ == null ) {
            return;
        }

        m_textViewTiltX.setText("방위 : " + String.format("%f", values[0]));
        m_textViewTiltY.setText("경사도 : " +String.format("%f", values[1]));
        m_textViewTiltZ.setText("좌우 회전 : " + String.format("%f", values[2]));
    }
}