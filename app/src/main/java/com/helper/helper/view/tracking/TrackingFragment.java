package com.helper.helper.view.tracking;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.support.v4.app.Fragment;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

//import com.github.brnunes.swipeablerecyclerview.SwipeableRecyclerViewTouchListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.helper.helper.R;
import com.helper.helper.controller.FileManager;
import com.helper.helper.model.TrackingData;
import com.helper.helper.model.TrackingRecordedListItem;
import com.helper.helper.view.Info.InfoFragment;
import com.helper.helper.view.TabPagerAdapter;

public class TrackingFragment extends Fragment
        implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    private final static String TAG = TrackingFragment.class.getSimpleName() + "/DEV";
    private static final float MIN_BRIGHTNESS = 0.8f;

    private MapView m_mapView;
    private GoogleApiClient m_googleApiClient;
    private GoogleMap m_map;
    private int m_nTrackingIndex;
    private TrackingRecordedListAdapter m_recordedListAdapter;
    private Map m_cardLocationListDic = new HashMap();
    private RecyclerView m_recyclerView;
    private List<TrackingRecordedListItem> m_recordedItemList;


    public TrackingFragment() {

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
        View view = inflater.inflate( R.layout.fragment_tracking, container, false );

        if( m_recordedItemList == null ) {
            m_recordedItemList = new ArrayList<>();
            m_cardLocationListDic = new HashMap();
        }

        m_recordedItemList.clear();
        m_nTrackingIndex = 0;

        try {
            List<TrackingData> lReadTrackigData = FileManager.readXMLTrackingData(getContext());
            if( lReadTrackigData != null ) {
                for (TrackingData trackingData:
                        lReadTrackigData) {
                    makeRecordedCard(
                            trackingData.getDate(),
                            trackingData.getStartTime(),
                            trackingData.getEndTime(),
                            trackingData.getDistance(),
                            trackingData.getLocationData());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        initLayout(view);

        m_mapView.onCreate(savedInstanceState);
        m_mapView.onResume();
        m_mapView.getMapAsync(this);

        adjustMapVerticalTouch(view);

        m_recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        m_recordedListAdapter = new TrackingRecordedListAdapter(m_recordedItemList, R.layout.cardview_recorded_tracking, getContext());
        m_recyclerView.setAdapter(m_recordedListAdapter);
//        m_recyclerView.setItemAnimator(new DefaultItemAnimator());

//        SwipeableRecyclerViewTouchListener swipeTouchListener =
//                new SwipeableRecyclerViewTouchListener(m_recyclerView,
//                        new SwipeableRecyclerViewTouchListener.SwipeListener() {
//                            @Override
//                            public boolean canSwipeLeft(int position) {
//                                return true;
//                            }
//
//                            @Override
//                            public boolean canSwipeRight(int position) {
//                                return true;
//                            }
//
//                            @Override
//                            public void onDismissedBySwipeLeft(RecyclerView m_recyclerView, int[] reverseSortedPositions) {
//                                for (int position : reverseSortedPositions) {
//                                    m_recordedItemList.remove(position);
//                                    m_recordedListAdapter.notifyItemRemoved(position);
//                                }
//                                m_recordedListAdapter.notifyDataSetChanged();
//                            }
//
//                            @Override
//                            public void onDismissedBySwipeRight(RecyclerView m_recyclerView, int[] reverseSortedPositions) {
//                                for (int position : reverseSortedPositions) {
//                                    m_recordedItemList.remove(position);
//                                    m_recordedListAdapter.notifyItemRemoved(position);
//                                }
//                                m_recordedListAdapter.notifyDataSetChanged();
//                            }
//                        });
//
//        m_recyclerView.addOnItemTouchListener(swipeTouchListener);

        return view;
    }

    private void initLayout(View view) {
        m_recyclerView = view.findViewById(R.id.recordedRecyclerView);
        m_mapView = view.findViewById(R.id.recordedMap);
    }

    private void initData() {
        for (int i = 0; i < 10; i++) {
            Date date = new Date();

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String dateString = "Tracking#" + String.format("%d", i+1) + " " + dateFormat.format(date);

            SimpleDateFormat startTimeFormat = new SimpleDateFormat("hh:mm");
            String startTimeString = "start time. " + startTimeFormat.format(date);

            SimpleDateFormat endTimeFormat = new SimpleDateFormat("hh:mm");
            String endTimeString = "end time. " + endTimeFormat.format(date);

            Random rnd = new Random();
            int r = rnd.nextInt(128) + 90; // 128 ... 255
            int g = rnd.nextInt(128) + 90; // 128 ... 255
            int b = rnd.nextInt(128) + 90; // 128 ... 255

            TrackingRecordedListItem recordedItem = new TrackingRecordedListItem(
                    Color.rgb(r, g, b),
                    dateString,
                    startTimeString,
                    endTimeString,
                    "Distance. " + String.format("%.2f", rnd.nextFloat() * 10) + "km");

            m_recordedItemList.add(recordedItem);

            m_nTrackingIndex = i;
        }
    }

    public void setSelectedMapLoad (String itemPosition) {
        List<LatLng> locationList = (List<LatLng>) m_cardLocationListDic.get(Integer.parseInt(itemPosition)-1);

        this.m_map.clear();

        this.m_map.moveCamera(CameraUpdateFactory.newLatLngZoom(locationList.get(0), 20));
        this.m_map.animateCamera(CameraUpdateFactory.zoomTo(15));

        List<Polyline> polylines = new ArrayList<>();

        polylines.clear();

        m_map.clear();

        for (int i = 1; i < locationList.size(); i++) {
            polylines
                    .add(this.m_map.addPolyline(new PolylineOptions().add(
                            locationList.get(i-1),
                            locationList.get(i)
                    ).width(12).color(Color.BLUE)
                            .geodesic(true)));
        }
        

    }
//    public static String hsvToRgb(float hue, float saturation, float value) {
//
//        int h = (int)(hue * 6);
//        float f = hue * 6 - h;
//        float p = value * (1 - saturation);
//        float q = value * (1 - f * saturation);
//        float t = value * (1 - (1 - f) * saturation);
//
//        switch (h) {
//            case 0: return rgbToString(value, t, p);
//            case 1: return rgbToString(q, value, p);
//            case 2: return rgbToString(p, value, t);
//            case 3: return rgbToString(p, q, value);
//            case 4: return rgbToString(t, p, value);
//            case 5: return rgbToString(value, p, q);
//            default: throw new RuntimeException("Something went wrong when converting from HSV to RGB. Input was " + hue + ", " + saturation + ", " + value);
//        }
//    }
//
//    public static String rgbToString(float r, float g, float b) {
//        String rs = Integer.toHexString((int)(r * 256));
//        String gs = Integer.toHexString((int)(g * 256));
//        String bs = Integer.toHexString((int)(b * 256));
//        return rs + gs + bs;
//    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @SuppressLint("ClickableViewAccessibility")
    public void adjustMapVerticalTouch(View view) {
        final NestedScrollView mainScrollView = (NestedScrollView) view.findViewById(R.id.trackingFragment);
        ImageView transparentImageView = (ImageView) view.findViewById(R.id.recordedMap_transparent_image);

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
        m_map = googleMap;

        //buildGoogleApiClient();
    }

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
    }

    public void makeRecordedCard(String date, String startTime, String endTime, String Distance, List<LatLng> latLngs) {

        Random rnd = new Random();
        int r = rnd.nextInt(128) + 90; // 128 ... 255
        int g = rnd.nextInt(128) + 90; // 128 ... 255
        int b = rnd.nextInt(128) + 90; // 128 ... 255

        TrackingRecordedListItem recordedItem = new TrackingRecordedListItem(
                Color.rgb(r, g, b),
                "Tracking#" + String.format("%d", (++m_nTrackingIndex)) + " " + date,
                "start time. " + startTime,
                "end time. " + endTime,
                "Distance. " + Distance + "km");

        m_recordedItemList.add(recordedItem);
        m_cardLocationListDic.put(m_nTrackingIndex-1, latLngs);
    }
}



//    public void toggleRecord(View v) {
//        if (m_viewPager.getCurrentItem() != TAB_STATUS) {
//            return;
//        }
//
//        m_IsRecorded = !m_IsRecorded;
//
//        if (m_IsRecorded) {
//            Date date = new Date();
//            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
//            m_recordStartDate = dateFormat.format(date);
//
//            SimpleDateFormat startTimeFormat = new SimpleDateFormat("hh:mm");
//            m_recordStartTime = startTimeFormat.format(date);
//
//            ((FloatingActionButton) v).setImageDrawable(getResources().getDrawable(R.drawable.ic_stop_solid, getApplicationContext().getTheme()));
//        } else {
//            ((FloatingActionButton) v).setImageDrawable(getResources().getDrawable(R.drawable.ic_circle_solid, getApplicationContext().getTheme()));
//
//            Date date = new Date();
//
//            SimpleDateFormat endTimeFormat = new SimpleDateFormat("hh:mm");
//            m_recordEndTime = endTimeFormat.format(date);
//
//            if (m_infoFrag == null) {
//                m_infoFrag = (InfoFragment) getSupportFragmentManager().findFragmentByTag(
//                        "android:switcher:" + m_viewPager.getId() + ":" + ((TabPagerAdapter) m_viewPager.getAdapter())
//                                .getItemId(TAB_STATUS));
//            }
//
//            try {
//                TrackingData trackingData = new TrackingData(
//                        m_recordStartDate,
//                        m_recordStartTime,
//                        m_recordEndTime,
//                        String.format("%f", m_infoFrag.getCurTrackingDistance()),
//                        m_infoFrag.getCurrRecordedLocationList());
//
//                FileManager.writeXmlTrackingData(getApplicationContext(), trackingData);
//            } catch (IOException e) {
//                e.printStackTrace();
//            } catch (IllegalStateException e) {
//                e.printStackTrace();
//            }
//
//            m_infoFrag.recordStopAndEraseLocationList();
//        }
//    }