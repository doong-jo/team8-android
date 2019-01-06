package com.helper.helper.view.main.tracking;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.helper.helper.R;
import com.helper.helper.controller.GoogleMapManager;
import com.helper.helper.controller.HttpManager;
import com.helper.helper.enums.RidingType;
import com.helper.helper.interfaces.HttpCallback;
import com.helper.helper.interfaces.ValidateCallback;
import com.helper.helper.model.Accident;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HeatMapFragment extends Fragment implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    private final static String TAG = HeatMapFragment.class.getSimpleName() + "/DEV";
    private final int PROXIMITY_RADIUS = 10000;
    private final int DEFAULT_ZOOM_LEVEL = 15;

    private static final String TYPE_DEFALT = "All";
    private static final String TYPE_BICYCLE = "Bicycle";
    private static final String TYPE_MOTORCYCLE = "Motorcycle";
    private static final String TYPE_SMART_MOBILITY = "Smart Mobility";

    private static final String ALARM_DEFAULT = "All";
    private static final String ALARM_DANGER = "Danger";
    private static final String ALARM_WARNING = "Warning";

    private static final String DATE_DEFAULT_MONTH = "6개월";
    private static final String DATE_THREE_MONTH = "3개월";
    private static final String DATE_ONE_MONTH = "1개월";

    /******************* Define widgtes in view *******************/
    private MapView m_mapView;
    private GoogleMap m_map;
    private GoogleApiClient m_googleApiClient;
    private SupportMapFragment m_mapFragment;

    protected GeoDataClient geoDataClient;
    protected PlaceDetectionClient placeDetectionClient;

    private HeatmapTileProvider mProvider;
    private TileOverlay mOverlay;

    private Spinner m_spinnerDate;
    private Spinner m_spinnerAlarm;
    private Spinner m_spinnerType;

    /**************************************************************/
    private List<LatLng> m_trackingList = new ArrayList<>();
    private List<Accident> accidentData = new ArrayList<>();
    private AdapterView.OnItemSelectedListener m_typeListener;
    private AdapterView.OnItemSelectedListener m_alarmListener;

    private String m_date;
    private String m_alarm;
    private String m_type;

    public HeatMapFragment() {
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tracking_heatmap, container, false);

        initLayout(view);

        m_mapView.onCreate(savedInstanceState);
        m_mapView.onResume();
        m_mapView.getMapAsync(this);

        /******************* Connect widgtes with layout *******************/
        m_spinnerDate = view.findViewById(R.id.spinnerDate);
        m_spinnerAlarm = view.findViewById(R.id.spinnerAlarm);
        m_spinnerType = view.findViewById(R.id.spinnerType);
        /*******************************************************************/

        m_spinnerType.setSelection(0,false);

        /******************* Make Listener in View *******************/
        m_typeListener = new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                String selectedType = (String)parent.getItemAtPosition(position);
                switch (selectedType){
                    case TYPE_DEFALT :
                        m_type = TYPE_DEFALT;
                        break;
                    case TYPE_BICYCLE :
                        m_type = RidingType.BICYCLE.value;
                        break;
                    case TYPE_MOTORCYCLE :
                        m_type = RidingType.MOTORCYCLE.value;
                        break;
                    case TYPE_SMART_MOBILITY :
                        m_type = RidingType.SMART_MOBILITY.value;
                        break;
                }

                setHeatMap();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };
        m_spinnerType.setOnItemSelectedListener(m_typeListener);

        m_alarmListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedType = (String)parent.getItemAtPosition(position);
                switch (selectedType){
                    case ALARM_DEFAULT:
                        m_alarm = ALARM_DEFAULT;
                        break;
                    case ALARM_WARNING:
                        m_alarm = "false";
                        break;
                    case ALARM_DANGER:
                        m_alarm = "true";
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        };
        m_spinnerAlarm.setOnItemSelectedListener(m_alarmListener);

        /*************************************************************/
        adjustMapVerticalTouch(view);
        return view;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && getActivity() != null) {
            RelativeLayout bottomNavLayout = getActivity().findViewById(R.id.bottomNavigationViewLayout);
            bottomNavLayout.setVisibility(View.GONE);

            ImageView toolbar_option_btn = getActivity().findViewById(R.id.toolbar_option_btn);
            toolbar_option_btn.setVisibility(View.GONE);
        }
    }


    private void initLayout(View view) {
        m_mapView = view.findViewById(R.id.heatMapView);
    }

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
        final NestedScrollView mainScrollView = (NestedScrollView) getActivity().findViewById(R.id.app_nestedScroll);
        ImageView transparentImageView = (ImageView) view.findViewById(R.id.heatMap_transparent_image);

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
        startMap();
        m_date = DATE_DEFAULT_MONTH;
        m_alarm = ALARM_DEFAULT;
        m_type = TYPE_DEFALT;
        setHeatMap();
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
        if (m_googleApiClient != null) {
            m_googleApiClient.stopAutoManage(getActivity());
            m_googleApiClient.disconnect();
        }

    }

    // Datasets from http://data.gov.au
    private ArrayList<LatLng> readItems() {
        ArrayList<LatLng> list = new ArrayList<>();

        for(Accident accident : accidentData){
            list.add(accident.getPosition());
        }

        return list;
    }

    private void startMap(){
        Location location = GoogleMapManager.getCurLocation();
        if (location != null) {
            m_map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), DEFAULT_ZOOM_LEVEL));
        }

//        GoogleMapManager.setLocationCallbackClone(new LocationCallback() {
//            @Override
//            public void onLocationResult(LocationResult locationResult) {
//                List<Location> locationList = locationResult.getLocations();
//                if (locationList.size() > 0) {
//                    Location location = locationList.get(locationList.size() - 1);
//                    m_map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), DEFAULT_ZOOM_LEVEL));
//
//                }
//            }
//        });

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            Toast.makeText(getActivity(), "위치 서비스 권한을 확인해주세요.", Toast.LENGTH_SHORT).show();

            return;
        }

        m_map.getUiSettings().setCompassEnabled(true);
        m_map.getUiSettings().setMyLocationButtonEnabled(true);
        m_map.setMyLocationEnabled(true);
        m_map.animateCamera(CameraUpdateFactory.zoomTo(DEFAULT_ZOOM_LEVEL));
    }

    private void setHeatMap() {
        try {
            setTrackingData(new ValidateCallback() {
                @Override
                public void onDone(final int resultCode) throws JSONException {
                    if (getActivity() == null) return;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (resultCode == 1) {
                                if (mProvider == null) {
                                    mProvider = new HeatmapTileProvider.Builder().data(m_trackingList).build();
                                    mOverlay = m_map.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
                                } else {
                                    mProvider.setData(m_trackingList);
                                    mOverlay = m_map.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
                                }

                            } else {
                                if (mProvider != null) {
                                    m_trackingList.add(new LatLng(0,0));
                                    mProvider.setData(m_trackingList);
                                    mOverlay.clearTileCache();
                                }
                            }
                        }
                    });
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setTrackingData(final ValidateCallback callback) throws JSONException {
        if (HttpManager.useCollection(getString(R.string.collection_accident))) {
            try {
                accidentData.clear();
                m_trackingList.clear();
                JSONObject reqObject = new JSONObject();
                if(!m_type.equals(TYPE_DEFALT)){
                    reqObject.put(Accident.ACCIDENT_RIDING_TYPE, m_type);
                }
                if(!m_alarm.equals(ALARM_DEFAULT)){
                    reqObject.put(Accident.ACCIDENT_HAS_ALERTED, m_alarm);
                }

                HttpManager.requestHttp(reqObject, "", "GET", "", new HttpCallback() {
                    @Override
                    public void onSuccess(JSONArray jsonArray) throws JSONException {
                        if (jsonArray.length() != 0) {
                            for (int i = 0; i < jsonArray.length(); ++i) {
                                JSONObject object = (JSONObject) jsonArray.get(i);
                                Date convertDate = null;
                                try {
                                    convertDate = new SimpleDateFormat("yyyy-mm-dd", Locale.KOREA).parse(object.getString(Accident.ACCIDENT_OCCURED_DATE));
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }

                                JSONObject positionObject = object.getJSONObject(Accident.ACCIDENT_POSITION);

                                LatLng position = new LatLng(positionObject.getDouble(Accident.ACCIDENT_POSITION_LATITUDE), positionObject.getDouble(Accident.ACCIDENT_POSITION_LONGITUDE));

                                accidentData.add(Accident.builder()
                                        .m_ridingType(object.getString(Accident.ACCIDENT_RIDING_TYPE))
                                        .m_occuredDate(convertDate)
                                        .m_position(position)
                                        .build()
                                );
                            }
                            m_trackingList = readItems();
                            Log.d(TAG, "onSuccess: accidentData" + accidentData);
                            callback.onDone(1);
                        } else {
                            callback.onDone(0);
                        }
                    }

                    @Override
                    public void onError(String err) throws JSONException {
                        Log.d(TAG, "onError: error");
                    }
                });

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}