package com.helper.helper;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.support.v4.app.Fragment;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import com.github.brnunes.swipeablerecyclerview.SwipeableRecyclerViewTouchListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;

public class TrackingFragment extends Fragment
        implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    private final static String TAG = TrackingFragment.class.getSimpleName() + "/DEV";
    private static final float MIN_BRIGHTNESS = 0.8f;

    private MapView mapView;
    private GoogleApiClient mGoogleApiClient;
    private GoogleMap mMap;
    private int mTrackingIndex;
    private TrackingRecordedListAdapter mAdapter;


//    private CardViewAdapter mAdapter;
//    private ArrayList<String> mItems;

    private RecyclerView recyclerView;
    private List<TrackingRecordedListItem> mRecordedItemList;


    public TrackingFragment() {

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "Tracking onActivityCreated!");
    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
        View view = inflater.inflate( R.layout.fragment_tracking, container, false );

//        mItems = new ArrayList<>(30);
//        for (int i = 0; i < 30; i++) {
//            mItems.add(String.format("Card number %02d", i));
//        }
//
//        mAdapter = new CardViewAdapter(mItems, itemTouchListener);
//
//        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recordedRecyclerView);
//
//        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
//        recyclerView.setAdapter(mAdapter);
//

//
//        recyclerView.addOnItemTouchListener(swipeTouchListener);

//        if( ((ScrollingActivity)getActivity()).ismHasRecordData() ) {
//            ((ScrollingActivity)getActivity()).setmHasRecordData(false);
//
//            Toast.makeText(getContext(), "Has Record Data!", Toast.LENGTH_LONG).show();
//        }
        Log.d(TAG, "Tracking onCreateView!");
        Log.d(TAG, "Tracking onCreateView! List? " + mRecordedItemList);
        if( mRecordedItemList == null ) {
            Log.d(TAG, "~~~ mRecordedItemList == null ~~~");

            /* Read XML Map data start */

            /* Read XML Map data end */

            mRecordedItemList = new ArrayList<TrackingRecordedListItem>();
            initData();
        }

        initLayout(view);

        //MapView 초기화 작업
        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        mapView.getMapAsync(this);

        adjustMapVerticalTouch(view);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new TrackingRecordedListAdapter(mRecordedItemList, R.layout.cardview_recorded_tracking);
        recyclerView.setAdapter(mAdapter);
//        recyclerView.setItemAnimator(new DefaultItemAnimator());

        SwipeableRecyclerViewTouchListener swipeTouchListener =
                new SwipeableRecyclerViewTouchListener(recyclerView,
                        new SwipeableRecyclerViewTouchListener.SwipeListener() {
                            @Override
                            public boolean canSwipeLeft(int position) {
                                return true;
                            }

                            @Override
                            public boolean canSwipeRight(int position) {
                                return true;
                            }

                            @Override
                            public void onDismissedBySwipeLeft(RecyclerView recyclerView, int[] reverseSortedPositions) {
                                for (int position : reverseSortedPositions) {
                                    Toast.makeText(getContext(), mRecordedItemList.get(position) + " swiped left", Toast.LENGTH_SHORT).show();
                                    mRecordedItemList.remove(position);
                                    mAdapter.notifyItemRemoved(position);
                                }
                                mAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onDismissedBySwipeRight(RecyclerView recyclerView, int[] reverseSortedPositions) {
                                for (int position : reverseSortedPositions) {
                                    Toast.makeText(getContext(), mRecordedItemList.get(position) + " swiped right", Toast.LENGTH_SHORT).show();
                                    mRecordedItemList.remove(position);
                                    mAdapter.notifyItemRemoved(position);
                                }
                                mAdapter.notifyDataSetChanged();
                            }
                        });

        recyclerView.addOnItemTouchListener(swipeTouchListener);

        return view;
    }

    private void initLayout(View view) {
        recyclerView = (RecyclerView) view.findViewById(R.id.recordedRecyclerView);
        mapView = (MapView) view.findViewById(R.id.recordedMap);
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

//            Random random = new Random();
//            float h = MIN_BRIGHTNESS + ((1f - MIN_BRIGHTNESS) * random.nextFloat());
//            float s = random.nextFloat();
//            float b = random.nextFloat();
//
//            float[] hsb = new float[3]; hsb[0] = h; hsb[1] = s; hsb[2] = b;
//
//            Log.d(TAG, "h : " + h);
//            Log.d(TAG, "s : " + s);
//            Log.d(TAG, "b : " + b);

            Random rnd = new Random();
            int r = rnd.nextInt(128) + 90; // 128 ... 255
            int g = rnd.nextInt(128) + 90; // 128 ... 255
            int b = rnd.nextInt(128) + 90; // 128 ... 255

//            Color clr = ;

            TrackingRecordedListItem recordedItem = new TrackingRecordedListItem(
                    Color.rgb(r, g, b),
                    dateString,
                    startTimeString,
                    endTimeString,
                    "Distance. " + String.format("%.2f", rnd.nextFloat() * 10) + "km");

            mRecordedItemList.add(recordedItem);

            mTrackingIndex = i;
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
        mMap = googleMap;

        //buildGoogleApiClient();
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
    }

    public void makeRecordedCard(String date, String startTime, String endTime, String Distance, List<LatLng> latLngs) {

        Log.d(TAG, "onLocationChanged location list : " + latLngs.toString());

        Random rnd = new Random();
        int r = rnd.nextInt(128) + 90; // 128 ... 255
        int g = rnd.nextInt(128) + 90; // 128 ... 255
        int b = rnd.nextInt(128) + 90; // 128 ... 255

        TrackingRecordedListItem recordedItem = new TrackingRecordedListItem(
                Color.rgb(r, g, b),
                "Tracking#" + String.format("%d", (++mTrackingIndex)+1) + " " + date,
                "start time. " + startTime,
                "end time. " + endTime,
                "Distance. " + Distance + "km");

        mRecordedItemList.add(recordedItem);

        ((ScrollingActivity) getActivity()).setmHasRecordData(false);

        Log.d(TAG, "Add Card!");
        Log.d(TAG, "mRecordedItemList size : " + mRecordedItemList.size());
        Log.d(TAG, "mTrackingIndex : " + mTrackingIndex);
    }
}