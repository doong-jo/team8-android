package com.helper.helper.view.main.tracking;

import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.helper.helper.R;
import com.helper.helper.controller.HttpManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

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
    private Location m_location;

    protected GeoDataClient geoDataClient;
    protected PlaceDetectionClient placeDetectionClient;

    private HeatmapTileProvider mProvider;
    private TileOverlay mOverlay;
    /**************************************************************/
    private ArrayList<LatLng> m_trackingList= new ArrayList<>();

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

        return view;
    }

    private void initLayout(View view) {
        m_mapView = view.findViewById(R.id.trackingMapView);
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        m_map = googleMap;

        try {
            m_trackingList=readItems(R.raw.police);
        } catch (JSONException e) {
            Toast.makeText(getContext(), "Problem reading list of markers.", Toast.LENGTH_LONG).show();
        }


            mProvider = new HeatmapTileProvider.Builder().data(m_trackingList).build();
            mOverlay = m_map.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
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

    // Datasets from http://data.gov.au
    private ArrayList<LatLng> readItems(int resource) throws JSONException {
        ArrayList<LatLng> list = new ArrayList<LatLng>();
        InputStream inputStream = getResources().openRawResource(resource);
        String json = new Scanner(inputStream).useDelimiter("\\A").next();
        JSONArray array = new JSONArray(json);
        for (int i = 0; i < array.length(); i++) {
            JSONObject object = array.getJSONObject(i);
            double lat = object.getDouble("lat");
            double lng = object.getDouble("lng");
            list.add(new LatLng(lat, lng));
        }
        return list;
    }

    private void setTrackingData(){
        if( HttpManager.useCollection(getString(R.string.collection_accident)) ) {

        }
    }
}