package com.helper.helper.tracking;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.support.v4.app.Fragment;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.github.brnunes.swipeablerecyclerview.SwipeableRecyclerViewTouchListener;
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
import com.helper.helper.ScrollingActivity;
import com.snatik.storage.Storage;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

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
    private Map cardTrackingLocationDic = new HashMap();
    private List<Polyline> polylines;
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
            cardTrackingLocationDic = new HashMap();
            polylines = new ArrayList<Polyline>();
//            initData();
        }

        mRecordedItemList.clear();
        mTrackingIndex = 0;

        try {
            readMapDataXML();
        } catch (IOException e) {
            e.printStackTrace();
        }

        initLayout(view);

        //MapView 초기화 작업
        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        mapView.getMapAsync(this);

        adjustMapVerticalTouch(view);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new TrackingRecordedListAdapter(mRecordedItemList, R.layout.cardview_recorded_tracking, getContext());
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

    private void readMapDataXML () throws IOException {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            Storage internalStorage = new Storage(getContext());

            String path = internalStorage.getInternalFilesDirectory();
            String dir = path + File.separator + "user_data";
            String xmlFilePath = dir + File.separator + "tracking.xml";

            boolean fileExists = internalStorage.isFileExist(xmlFilePath);

            Document doc = docBuilder.newDocument();

            Element rootElement;
            if (fileExists) {
                doc = docBuilder.parse(new File(xmlFilePath));
                rootElement = (Element) doc.getDocumentElement();
            } else {
                rootElement = doc.createElement("tracking");
            }

            NodeList maps = doc.getElementsByTagName("map");

            int length = maps.getLength();
            Log.d(TAG, "readMapDataXML maps size : " + maps.getLength());

            String date = "";
            String startTime = "";
            String endTime = "";
            String distance = "";
            List<LatLng> locations = new ArrayList<LatLng>();

            for (int i = 0; i < maps.getLength(); i++) {
                Node map = maps.item(i);

                date = map.getAttributes().getNamedItem("date").getNodeValue();
                startTime = map.getAttributes().getNamedItem("start_time").getNodeValue();
                endTime = map.getAttributes().getNamedItem("end_time").getNodeValue();
                distance = map.getAttributes().getNamedItem("distance").getNodeValue();

                NodeList locationList = map.getChildNodes();

                int locationInd = 1;
                String lat = "";
                String log = "";

                Node location;

                for (int j = 1; j < locationList.getLength(); j+=2) {
                    location = locationList.item(j);

                    lat = location.getChildNodes().item(1).getChildNodes().item(0).getNodeValue();
                    log = location.getChildNodes().item(3).getChildNodes().item(0).getNodeValue();
                }

                if( locationList.getLength() > 0 ) {
                    locations.add(new LatLng(Double.parseDouble(lat), Double.parseDouble(log)));
                }
                //            for (int j = 0; j < location.getLength(); j++) {
                //                Node loc = location.item(i);
                //                String latitude = loc.getChildNodes().item(1).getNodeValue();
                //                String logitude = loc.getChildNodes().item(2).getNodeValue();
                //            }

                makeRecordedCard(date, startTime, endTime, distance, locations);
            }


            int a = 1;
        }

        catch (ParserConfigurationException pce)
        {
            pce.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
    }

    public void setSelectedMapLoad (String itemPosition) {
        Toast.makeText(getContext(), "setSelectedMapLoad itemPosition : " + itemPosition, Toast.LENGTH_LONG).show();



        List<LatLng> locationList = (List<LatLng>) cardTrackingLocationDic.get(Integer.parseInt(itemPosition)-1);

        this.mMap.clear();

        this.mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(locationList.get(0), 20));
        this.mMap.animateCamera(CameraUpdateFactory.zoomTo(20));

        for(Polyline line : polylines)
        {
            line.remove();
        }

        polylines.clear();

        for (int i = 1; i < locationList.size(); i++) {
            polylines
                    .add(this.mMap.addPolyline(new PolylineOptions().add(
                            locationList.get(i-1),
                            locationList.get(i)
                    ).width(12).color(Color.BLUE)
                            .geodesic(true)));
        }



//        for (int i = 1; i < locationList.size(); i++) {
//            mMap.addPolyline((new PolylineOptions())
//                    .add(
//                            locationList.get(i-1),
//                            locationList.get(i)
//                    ).width(12).color(Color.BLUE)
//                    .geodesic(true));
//        }

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

        Random rnd = new Random();
        int r = rnd.nextInt(128) + 90; // 128 ... 255
        int g = rnd.nextInt(128) + 90; // 128 ... 255
        int b = rnd.nextInt(128) + 90; // 128 ... 255

        TrackingRecordedListItem recordedItem = new TrackingRecordedListItem(
                Color.rgb(r, g, b),
                "Tracking#" + String.format("%d", (++mTrackingIndex)) + " " + date,
                "start time. " + startTime,
                "end time. " + endTime,
                "Distance. " + Distance + "km");

        mRecordedItemList.add(recordedItem);
        cardTrackingLocationDic.put(mTrackingIndex-1, latLngs);
    }
}