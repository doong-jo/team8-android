package com.helper.helper.view.main.tracking;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
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
import com.google.maps.android.heatmaps.Gradient;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.helper.helper.R;
import com.helper.helper.controller.GoogleMapManager;
import com.helper.helper.controller.HttpManager;
import com.helper.helper.controller.SharedPreferencer;
import com.helper.helper.controller.UserManager;
import com.helper.helper.enums.RidingType;
import com.helper.helper.interfaces.HttpCallback;
import com.helper.helper.interfaces.ValidateCallback;
import com.helper.helper.model.Accident;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class HeatMapFragment extends Fragment implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    private final static String TAG = HeatMapFragment.class.getSimpleName() + "/DEV";
    private final int DEFAULT_ZOOM_LEVEL = 15;
    private static final int DEFAULT_SELECTED_DATE = 6;
    private static final int THREE_MONTH_SELECTED_DATE = 3;
    private static final int ONE_MONTH_SELECTED_DATE = 1;
    private static final int MAX_ACCIDENT_DATA_DOWNLOAD_TERM = 30;
//    private static final int[] ALT_HEATMAP_GRADIENT_COLORS = {
//            Color.argb(0, 0, 255, 255),// transparent
//            Color.argb(255 / 3 * 2, 0, 255, 255),
//            Color.rgb(0, 191, 255),
//            Color.rgb(0, 0, 127),
//            Color.rgb(255, 0, 0)
//    };
    private static final int[] WARNING_HEATMAP_GRADIENT_COLORS = {
        Color.argb(0, 108, 255, 15),
        Color.rgb(122,243,17),
        Color.rgb(137, 232, 20),
        Color.rgb(152, 220, 22),
        Color.rgb(166, 209, 25),
        Color.rgb(181, 198, 28)
    };
    private static final float[] WARNING_HEATMAP_GRADIENT_START_POINTS = {
            0.1f, 0.20f, 0.30f, 0.40f, 0.50f, 1.0f
    };
    private static final int[] DANGER_HEATMAP_GRADIENT_COLORS = {
            Color.argb(255, 141, 41, 0),
            Color.rgb(255, 141, 41),
            Color.rgb(255, 132, 41),
            Color.rgb(255, 123, 41),
            Color.rgb(255, 114, 41),
            Color.rgb(255, 105, 28),
            Color.rgb(255, 97, 41),
            Color.rgb(255, 88, 41),
            Color.rgb(255, 79, 41),
            Color.rgb(255, 70, 41),
            Color.rgb(255, 61, 41),
            Color.rgb(255, 53, 41)
    };
    private static final float[] DANGER_HEATMAP_GRADIENT_START_POINTS = {
            0.1f, 0.20f, 0.25f, 0.30f, 0.35f, 0.45f, 0.60f, 0.70f, 0.80f, 0.90f, 0.95f, 1.0f
    };

    /******************* Define widgtes in view *******************/
    private MapView m_mapView;
    private GoogleMap m_map;
    private GoogleApiClient m_googleApiClient;
    private SupportMapFragment m_mapFragment;

    protected GeoDataClient geoDataClient;
    protected PlaceDetectionClient placeDetectionClient;

    private HeatmapTileProvider m_warningProvider;
    private HeatmapTileProvider m_dangerProvider;
    private TileOverlay mOverlay;

    private Spinner m_spinnerDate;
    private Spinner m_spinnerAlarm;
    private Spinner m_spinnerType;

    private SweetAlertDialog m_loadingDialog;
    /**************************************************************/
    private List<LatLng> m_warningList = new ArrayList<>();
    private List<LatLng> m_dangerList = new ArrayList<>();
    private List<Accident> accidentData = new ArrayList<>();
    private AdapterView.OnItemSelectedListener m_typeListener;
    private AdapterView.OnItemSelectedListener m_alarmListener;
    private AdapterView.OnItemSelectedListener m_dateListener;

    private String m_date;
    private String m_alarm;
    private String m_type;
    private int m_selectedDate;

    private String[] m_spinnerTypeTexts;
    private String[] m_spinnerAlarmTexts;
    private String[] m_spinnerDateTexts;

    private SharedPreferences pref;

    public static final Gradient WARNING_HEATMAP_GRADIENT = new Gradient(WARNING_HEATMAP_GRADIENT_COLORS, WARNING_HEATMAP_GRADIENT_START_POINTS);
    public static final Gradient DANGER_HEATMAP_GRADIENT = new Gradient(DANGER_HEATMAP_GRADIENT_COLORS, DANGER_HEATMAP_GRADIENT_START_POINTS);

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

        m_spinnerDate.setSelection(0,false);
        m_spinnerAlarm.setSelection(0,false);
        m_spinnerType.setSelection(0,false);

        m_spinnerTypeTexts = getResources().getStringArray(R.array.spinner_vehicle_type);
        m_spinnerAlarmTexts = getResources().getStringArray(R.array.spinner_alarm);
        m_spinnerDateTexts = getResources().getStringArray(R.array.spinner_date);

        m_type = m_spinnerTypeTexts[m_spinnerType.getSelectedItemPosition()];
        m_alarm = m_spinnerAlarmTexts[m_spinnerAlarm.getSelectedItemPosition()];
        m_date = m_spinnerDateTexts[m_spinnerDate.getSelectedItemPosition()];
        getSelectedDate(m_date);

        pref = SharedPreferencer.getSharedPreferencer(getContext(), SharedPreferencer.ACCIDENT_DATA_DOWNLAOD_DATE, Activity.MODE_PRIVATE);

        /******************* Make Listener in View *******************/
        m_typeListener = new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                String selectedData = (String)parent.getItemAtPosition(position);
                getSelectedRidingType(selectedData);
                setHeatMap();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };
        m_spinnerType.setOnItemSelectedListener(m_typeListener);

        m_alarmListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedData = (String)parent.getItemAtPosition(position);
                getSelectedAlerted(selectedData);
                setHeatMap();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };
        m_spinnerAlarm.setOnItemSelectedListener(m_alarmListener);

        m_dateListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedData = (String)parent.getItemAtPosition(position);
                getSelectedDate(selectedData);
                setHeatMap();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        };
        m_spinnerDate.setOnItemSelectedListener(m_dateListener);

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
    private void readItems() {
        for(Accident accident : accidentData){
            if(accident.getHasAlerted() == true){
                m_dangerList.add(accident.getPosition());
            }
            else {
                m_warningList.add(accident.getPosition());
            }
        }
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
            m_loadingDialog = makeLoadingDialog();
            m_loadingDialog.show();
            m_loadingDialog.findViewById(R.id.confirm_button).setVisibility(View.GONE);

            setTrackingData(new ValidateCallback() {
                @Override
                public void onDone(final int resultCode) throws JSONException {
                    if (getActivity() == null) return;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (resultCode == 1) {
                                if (m_warningProvider == null && m_dangerProvider == null) {
                                    m_warningProvider = new HeatmapTileProvider.Builder().data(m_warningList).build();
                                    m_warningProvider.setGradient(WARNING_HEATMAP_GRADIENT);
                                    m_dangerProvider = new HeatmapTileProvider.Builder().data(m_dangerList).build();
                                    m_dangerProvider.setGradient(DANGER_HEATMAP_GRADIENT);
                                    mOverlay = m_map.addTileOverlay(new TileOverlayOptions().tileProvider(m_warningProvider));
                                    mOverlay = m_map.addTileOverlay(new TileOverlayOptions().tileProvider(m_dangerProvider));
                                } else {
                                    if(m_warningList.size() != 0) {
                                        m_warningProvider.setData(m_warningList);
                                    }
                                    else{
                                        m_warningList.add(new LatLng(0,0));
                                        m_warningProvider.setData(m_warningList);
                                    }

                                    if(m_dangerList.size() != 0){
                                        m_dangerProvider.setData(m_dangerList);
                                    }
                                    else{
                                        m_dangerList.add(new LatLng(0,0));
                                        m_dangerProvider.setData(m_dangerList);
                                    }

                                    mOverlay.clearTileCache();
                                    mOverlay = m_map.addTileOverlay(new TileOverlayOptions().tileProvider(m_warningProvider));
                                    mOverlay = m_map.addTileOverlay(new TileOverlayOptions().tileProvider(m_dangerProvider));
                                }

                            } else {
                                if (m_warningProvider != null && m_dangerProvider != null) {
                                    m_dangerList.add(new LatLng(0,0));
                                    m_warningList.add(new LatLng(0,0));
                                    m_dangerProvider.setData(m_dangerList);
                                    m_warningProvider.setData(m_warningList);
                                    mOverlay.clearTileCache();
                                    mOverlay = m_map.addTileOverlay(new TileOverlayOptions().tileProvider(m_warningProvider));
                                    mOverlay = m_map.addTileOverlay(new TileOverlayOptions().tileProvider(m_dangerProvider));
                                }
                            }
                            m_loadingDialog.dismissWithAnimation();
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
                m_warningList.clear();
                m_dangerList.clear();
                JSONObject reqObject = new JSONObject();
                if(!m_type.equals(m_spinnerTypeTexts[0])){
                    reqObject.put(Accident.ACCIDENT_RIDING_TYPE, m_type);
                }
                if(!m_alarm.equals(m_spinnerAlarmTexts[0])){
                    reqObject.put(Accident.ACCIDENT_HAS_ALERTED, m_alarm);
                }

                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.MONTH, -m_selectedDate);
                String date = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).format(cal.getTime());


                reqObject.put("gte", date);
//                Date occDate = new Date();
//                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.KOREA);
//                String dateStr = sdf.format(occDate);

//                reqObject.put("gte", "2018-01-10");

                HttpManager.requestHttp(reqObject, "", "GET", "gte", new HttpCallback() {
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
                                        .m_hasAlerted(object.getBoolean(Accident.ACCIDENT_HAS_ALERTED))
                                        .m_occuredDate(convertDate)
                                        .m_position(position)
                                        .build()
                                );
                            }
                            readItems();
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

    private void getSelectedRidingType(String selectedData){
        if(selectedData.equals(m_spinnerTypeTexts[0])){
            m_type = m_spinnerTypeTexts[0];
        }
        else if(selectedData.equals(m_spinnerTypeTexts[1])){
            m_type = RidingType.BICYCLE.value;
        }
        else if(selectedData.equals(m_spinnerTypeTexts[2])){
            m_type = RidingType.MOTORCYCLE.value;
        }
        else{
            m_type = RidingType.SMART_MOBILITY.value;
        }
    }

    private void getSelectedDate(String selectedData){
        if(selectedData.equals(m_spinnerDateTexts[0])){
            m_selectedDate = DEFAULT_SELECTED_DATE;
        }
        else if(selectedData.equals(m_spinnerDateTexts[1])){
            m_selectedDate = THREE_MONTH_SELECTED_DATE;
        }
        else{
            m_selectedDate = ONE_MONTH_SELECTED_DATE;
        }

    }

    private void getSelectedAlerted(String selectedData){
        if(selectedData.equals(m_spinnerAlarmTexts[0])){
            m_alarm = m_spinnerAlarmTexts[0];
        }
        else if(selectedData.equals(m_spinnerAlarmTexts[1])){
            m_alarm = "false";
        }
        else{
            m_alarm = "true";
        }
    }

    private void checkAccidentDataDownloadDate(){
        String downloadDateStr = pref.getString("downloadDate","");

        if(downloadDateStr.equals("")){
            //download 다이얼로그
        }
        else{
            //날짜
            Calendar cal = Calendar.getInstance();
            Date currentDate = cal.getTime();
            Date downloadDate;
            int downloadDateTerm = 0;
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
            try {
                currentDate = format.parse(currentDate.toString());
                downloadDate = format.parse(downloadDateStr);
                downloadDateTerm = currentDate.compareTo(downloadDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if(downloadDateTerm > MAX_ACCIDENT_DATA_DOWNLOAD_TERM){
                //download 다이얼로그 -> 다운받기 누를 시 Server에서 xml에 데이터 저장
            }
            else{
                //xml 파일에서 읽어오기
            }
        }
    }

    /** Dialog **/
    private SweetAlertDialog makeLoadingDialog() {
        SweetAlertDialog dlg = new SweetAlertDialog(getContext(), SweetAlertDialog.PROGRESS_TYPE);
        dlg.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        dlg.setTitleText(getString(R.string.loading_dialog_accident_data));
        dlg.setCancelable(false);
        return dlg;
    }

    private void setAccidentDataToFile(){

    }
}


