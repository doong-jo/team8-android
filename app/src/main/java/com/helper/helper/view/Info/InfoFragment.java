package com.helper.helper.view.Info;

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
import com.helper.helper.view.ScrollingActivity;
import com.helper.helper.controller.PermissionManager;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class InfoFragment extends Fragment
        implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
    private final static String TAG = InfoFragment.class.getSimpleName() + "/DEV";
    private static final LatLng DEFAULT_LOCATION = new LatLng(37.56, 126.97);
    private static final int UPDATE_INTERVAL_MS = 1500;
    private static final int FASTEST_UPDATE_INTERVAL_MS = 1000;

    private BatteryView m_batView;
    private MapView m_mapView;
    private GoogleApiClient m_googleApiClient;
    private GoogleMap m_googleMap;
    private LocationRequest m_locationReq;
    private Location m_curLocation;
    private Marker m_curLocationMarker;

    private double m_fCurDistance = 0.0;
    private List<LatLng> m_lCurRecordedLocation;

    private SeekBar m_brightSeekbar;
    private SeekBar m_speedSeekbar;

    private ImageView m_curLEDView;


    public void setCurLEDView(final int ind, boolean selectable) {
        final int[] images = {
                R.drawable.bird,
                R.drawable.characters,
                R.drawable.windy,
                R.drawable.snow,
                R.drawable.rain,
                R.drawable.cute,
                R.drawable.moving_arrow_left_blink,
                R.drawable.moving_arrow_right_blink,
                R.drawable.emergency_blink,
                R.drawable.mario,
                R.drawable.boy,
        };

        if( selectable ) {
            GlideDrawableImageViewTarget gifimage = new GlideDrawableImageViewTarget(m_curLEDView);
            Glide.with(this).load(ind).into(gifimage);
        } else {
            getActivity().runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            GlideDrawableImageViewTarget gifimage = new GlideDrawableImageViewTarget(m_curLEDView);
                            Glide.with(getActivity()).load(images[ind]).into(gifimage);
                        }
                    }
            );
        }


//        if( selectable ) {
//            GlideDrawableImageViewTarget gifimage = new GlideDrawableImageViewTarget(m_curLEDView);
//            Glide.with(this).load(selectable).into(gifimage);
//        } else {
//            GlideDrawableImageViewTarget gifimage = new GlideDrawableImageViewTarget(m_curLEDView);
//            Glide.with(this).load(images[ind]).into(gifimage);
//        }
    }

    public InfoFragment() {

    }

    @SuppressWarnings("MissingPermission")
//    private LocationCallback mLocationCallback;
//
//    {
//        mLocationCallback = new LocationCallback() {
//            @Override
//            public void onLocationResult(LocationResult locationResult) {
//
//                List<Location> locationList = locationResult.getLocations();
//                if (locationList.size() > 0) {
//                    //The last location in the list is the newest
//                    Location location = locationList.get(locationList.size() - 1);
//                    m_curLocation = location;
//
////                    Log.d(TAG, "" + location.getLatitude() + " " + location.getLongitude());
//
//                    try {
//                        ((ScrollingActivity)getActivity()).setMapPosition(location.getLatitude(), location.getLongitude(), location);
//                    } catch (NullPointerException e) {
//                        e.printStackTrace();
//                    }
//
//                    String writeStr = "";
//
////                    m_textCurSpeed.setText("현재 속도 : " + String.format("%f", location.getSpeed()));
////                    m_textBeforeSpeed.setText("이전 속도 : " + String.format("%f", m_curSpeed));
//
////                    if( location.getSpeed() >= 1 && location.getSpeed() * EMENRGENCY_SPPED_PIVOT < m_curSpeed ) {
////                    if( location.getSpeed() < m_curSpeed ) {
////                        writeStr = "0-08-1";
////
////                        ((ScrollingActivity)getActivity()).setcurInterrupt(EMERGENCY);
////                    }
//
//
//                    int curInterrutState;
//                    try {
//                        curInterrutState = ((ScrollingActivity) Objects.requireNonNull(getActivity())).getcurInterruptState();
//                    } catch (NullPointerException e){
//                        curInterrutState = ORIENTATION_NONE;
//                        e.printStackTrace();
//                    }
//
//
//                    if ( curInterrutState == EMERGENCY && location.getSpeed() >= m_curSpeed ) {
//                        writeStr = ((ScrollingActivity)getActivity()).  getCurLED();
//
//                        ((ScrollingActivity)getActivity()).setcurInterrupt(ORIENTATION_NONE);
//                    }
//
//                    try{
//                        if( writeStr != "" ) {
//                            ((ScrollingActivity)getActivity()).sendToBluetoothDevice(writeStr.getBytes());
//                        }
//                    }
//                    catch (NullPointerException e) {
//                        e.printStackTrace();
//                    }
//
//
////                    Toast.makeText(getContext(), "C: " + location.getSpeed() + " / B: " + m_curSpeed, Toast.LENGTH_SHORT).show();
//
//                    m_curSpeed = location.getSpeed();
////                    Toast.makeText(getContext(), "onLocationResult : "+ location.getSpeed(), Toast.LENGTH_SHORT).show();
//
//                    if (m_curLocationMarker != null) {
//                        m_curLocationMarker.remove();
//                    }
//
//                    setCurrentLocation(location, "Current Position", "GPS Position");
//                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
//
//                    try{
//                        if (((ScrollingActivity) getActivity()).getIsRecorded()) {
//                            m_lCurRecordedLocation.add(new LatLng(m_curLocation.getLatitude(), m_curLocation.getLongitude()));
//
//                            if (m_beforeLatlng != null) {
//                                m_googleMap.addPolyline((new PolylineOptions())
//                                        .add(m_beforeLatlng, latLng)
//                                        .width(R.dimen.google_polyline_width).color(Color.BLUE)
//                                        .geodesic(true));
//
//                                m_fCurDistance += CalculationByDistance(m_beforeLatlng, latLng);
//                            }
//                        }
//                        m_beforeLatlng = latLng;
//                    }
//                    catch (NullPointerException e) {
//                        e.printStackTrace();
//                    }
//
//                }
//            }
//        };
//    }

    private SeekBar.OnSeekBarChangeListener m_seekBarBrightChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            String str = "2-" + String.format("%02d-%d", i/10, i%10);
//            ((ScrollingActivity)getActivity()).sendToBluetoothDevice(str.getBytes());
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
//            ((ScrollingActivity)getActivity()).sendToBluetoothDevice(str.getBytes());
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



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_info, container, false);

        m_lCurRecordedLocation = new ArrayList<LatLng>();

        m_batView = (BatteryView) view.findViewById(R.id.batView);
        m_batView.setPower(100);

        adjustMapVerticalTouch(view);

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

    /** support mapview in scrollview touch **/
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




    /** Life Cycle **/
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

    public void setLEDAttribute(int ledInd, float spdVal, float brtVal) {
        setCurLEDView(ledInd, false);

        m_brightSeekbar = (SeekBar) getView().findViewById(R.id.brightSeek);
        m_speedSeekbar = (SeekBar) getView().findViewById(R.id.speedSeek);

        m_speedSeekbar.setProgress((int)(spdVal*100));
        m_brightSeekbar.setProgress((int)(brtVal*100));
    }

//    public void setTextTiltXYZ(float[] values) {
//
//        if( m_textViewTiltX == null || m_textViewTiltY == null || m_textViewTiltZ == null ) {
//            return;
//        }
//
//        m_textViewTiltX.setText("방위 : " + String.format("%f", values[0]));
//        m_textViewTiltY.setText("경사도 : " +String.format("%f", values[1]));
//        m_textViewTiltZ.setText("좌우 회전 : " + String.format("%f", values[2]));
//    }
}