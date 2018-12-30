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
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.NestedScrollView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
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
import com.helper.helper.interfaces.HttpCallback;
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

    /******************* Define widgtes in view *******************/
    private MapView m_mapView;
    private GoogleMap m_map;
    private GoogleApiClient m_googleApiClient;
    private SupportMapFragment m_mapFragment;

    protected GeoDataClient geoDataClient;
    protected PlaceDetectionClient placeDetectionClient;

    private HeatmapTileProvider mProvider;
    private TileOverlay mOverlay;
    /**************************************************************/
    private List<LatLng> m_trackingList = new ArrayList<>();
    private List<Accident> accidentData = new ArrayList<>();

    public HeatMapFragment() {
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tracking_heatmap, container, false);

        setTrackingData();
        initLayout(view);

        m_mapView.onCreate(savedInstanceState);
        m_mapView.onResume();
        m_mapView.getMapAsync(this);


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
        final NestedScrollView mainScrollView = (NestedScrollView) view.findViewById(R.id.heatMapFragment);
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


        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            Toast.makeText(getActivity(), "위치 서비스 권한을 확인해주세요.", Toast.LENGTH_SHORT).show();

            return;
        }


        mProvider = new HeatmapTileProvider.Builder().data(m_trackingList).build();
        mOverlay = m_map.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
        Location location = GoogleMapManager.getCurLocation();
        if (location != null) {
            m_map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), DEFAULT_ZOOM_LEVEL));
        }
        GoogleMapManager.setLocationCallbackClone(new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                List<Location> locationList = locationResult.getLocations();
                if (locationList.size() > 0) {
                    Location location = locationList.get(locationList.size() - 1);
                    m_map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), DEFAULT_ZOOM_LEVEL));

                }
            }
        });

        googleMap.getUiSettings().setCompassEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        googleMap.setMyLocationEnabled(true);
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(DEFAULT_ZOOM_LEVEL));
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

        for (Accident accident : accidentData) {
            list.add(accident.getPosition());
        }

        return list;
    }

    private void setTrackingData() {
        if (HttpManager.useCollection(getString(R.string.collection_accident))) {
//            Accident.AccidentBuilder accBuilder = Accident.builder();
            try {
                JSONObject reqObject = new JSONObject();


                HttpManager.requestHttp(reqObject, "", "GET", "", new HttpCallback() {
                    @Override
                    public void onSuccess(JSONArray jsonArray) throws JSONException {
                        int arrLen = jsonArray.length();

                        if (arrLen != 0) {

                            for (int i = 0; i < arrLen; ++i) {
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

                            m_trackingList = readItems();
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