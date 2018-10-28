/*
 * Copyright (c) 10/15/18 1:54 PM
 * Written by Sungdong Jo
 * Description:
 */

package com.helper.helper.view;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.helper.helper.R;
import com.helper.helper.controller.AddressManager;
import com.helper.helper.controller.BTManager;
import com.helper.helper.controller.EmergencyManager;
import com.helper.helper.controller.GoogleMapManager;
import com.helper.helper.controller.UserManager;
import com.helper.helper.interfaces.ValidateCallback;
import com.helper.helper.view.main.InfoFragment;
import com.helper.helper.view.contact.ContactActivity;
import com.helper.helper.controller.GyroManager;
import com.helper.helper.controller.HttpManager;
import com.helper.helper.controller.PermissionManager;
import com.snatik.storage.Storage;

import org.json.JSONException;

import java.io.File;

import static android.support.design.widget.TabLayout.*;

public class ScrollingActivity extends AppCompatActivity
        implements SensorEventListener,
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        NavigationView.OnNavigationItemSelectedListener{
    private final static String TAG = ScrollingActivity.class.getSimpleName() + "/DEV";
    private static final int TAB_STATUS = 0;
    private static final int TAB_LED = 1;
    private static final int TAB_TRACKING = 2;
    private static final int PERMISSION_REQUEST = 267;

    private TabLayout m_tabLayout;
    private TabPagerAdapter m_pagerAdapter;
    private ViewPager m_viewPager;

    private InfoFragment m_infoFrag;

    private NavigationView m_navigationView;

    public void setMapPosition(double latitude, double longitude, Location curLocation) {
        AddressManager.startAddressIntentService(this, curLocation);
    }

    public ViewPager getViewPager() {
        return m_viewPager;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /******************* Connect widgtes with layout *******************/
        setContentView(R.layout.activity_scrolling);
//        getSupportActionBar().setDisplayShowTitleEnabled(false);
//        getSupportActionBar().setTitle("asdf");

        /** ToolBar **/
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView mTitle = (TextView) toolbar.findViewById(R.id.toolbar_title);

        setSupportActionBar(toolbar);
        mTitle.setText(toolbar.getTitle());

        getSupportActionBar().setDisplayShowTitleEnabled(false);

        /** Navigation **/
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        m_navigationView = (NavigationView) findViewById(R.id.nav_view);
        m_navigationView.setNavigationItemSelectedListener(this);

        /** Tab **/
        m_tabLayout = findViewById(R.id.tabLayout);
        m_tabLayout.addTab(m_tabLayout.newTab().setText("MY EIGHT"));
        m_tabLayout.addTab(m_tabLayout.newTab().setText("LED"));
        m_tabLayout.addTab(m_tabLayout.newTab().setText("TRACKING"));

        m_pagerAdapter = new TabPagerAdapter(getSupportFragmentManager(), m_tabLayout.getTabCount());

        m_viewPager = findViewById(R.id.pager);
        m_viewPager.setAdapter(m_pagerAdapter);
        m_viewPager.addOnPageChangeListener(new TabLayoutOnPageChangeListener(m_tabLayout));
        m_tabLayout.addOnTabSelectedListener(new OnTabSelectedListener() {
            @Override
            public void onTabSelected(Tab tab) {
                Log.d("DEV", "onTabSelected called! posistion : " + tab.getPosition());
                m_viewPager.setCurrentItem(tab.getPosition());

                if (tab.getPosition() == TAB_STATUS) {

//                    if (m_IsRecorded) {
//                        ((FloatingActionButton) m_infoFrag.getView().findViewById(R.id.recordToggleBtn)).setImageDrawable(getResources().getDrawable(R.drawable.ic_stop_solid, getApplicationContext().getTheme()));
//                    } else {
//                        ((FloatingActionButton) m_infoFrag.getView().findViewById(R.id.recordToggleBtn)).setImageDrawable(getResources().getDrawable(R.drawable.ic_circle_solid, getApplicationContext().getTheme()));
//                    }
                } else if (tab.getPosition() == TAB_TRACKING) {


                }
            }

            @Override
            public void onTabUnselected(Tab tab) {

            }

            @Override
            public void onTabReselected(Tab tab) {

            }
        });


        /*******************************************************************/

        /******************* Make Listener in View *******************/

        /*************************************************************/


        /** Http Server **/
        HttpManager.setServerURI(getString(R.string.server_uri));

        /** Request permissions **/
        if (!PermissionManager.checkPermissions(this, Manifest.permission.ACCESS_FINE_LOCATION) ||
                !PermissionManager.checkPermissions(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {

            /* Result about user selection -> onActivityResult in ScrollActivity */
            PermissionManager.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.SEND_SMS}, PERMISSION_REQUEST);
        }
//        else {
//            if ( m_googleApiClient == null) {
//                buildGoogleApiClient();
//            }
//            requsetLocation();
//        }
//
//        if ( !PermissionManager.checkPermissions(getActivity(), Manifest.permission.SEND_SMS) ) {
//
//            /* Result about user selection -> onActivityResult in ScrollActivity */
//            PermissionManager.requestPermissions(getActivity(), new String[]{Manifest.permission.SEND_SMS}, SEND_SMS_REQUEST_CODE);
//        } else {
//
//        }

        /** Internal FileStorage **/
        Storage storage = new Storage(getApplicationContext());
        String path = storage.getInternalFilesDirectory();
        String newDir = path + File.separator + "user_data";

        boolean dirExists = storage.isDirectoryExists(path);

        if (!dirExists) {
            storage.createDirectory(newDir);
        }

        /** GoogleMap **/
        GoogleMapManager.initGoogleMap(this);

        /** Bluetooth  **/
//        BTManager.initBluetooth(this);

        /** Gyro **/
        GyroManager.m_sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        GyroManager.m_sensorAccel = GyroManager.m_sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        GyroManager.m_sensorMag = GyroManager.m_sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        /* Sensor end */
    }

    /** Result handler **/
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case BTManager.SUCCESS_BLUETOOTH_CONNECT:
                Toast.makeText(this, "디바이스 블루투스 연결 성공", Toast.LENGTH_SHORT).show();
                break;

            case BTManager.FAIL_BLUETOOTH_CONNECT:
                break;

            case BTManager.REQUEST_ENABLE_BT:
                BTManager.initBluetooth(this);
                break;

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSION_REQUEST:
                if (!PermissionManager.checkPermissions(
                        this, Manifest.permission.ACCESS_FINE_LOCATION) ||
                        !PermissionManager.checkPermissions(
                                this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    Toast.makeText(this, getString(R.string.not_grant_location_permission), Toast.LENGTH_SHORT).show();
                }

                if (!PermissionManager.checkPermissions(
                        this, Manifest.permission.SEND_SMS)) {
                    Toast.makeText(this, getString(R.string.not_grant_contacts_permission), Toast.LENGTH_SHORT).show();
                }

                Toast.makeText(this, "권한요청 프로세스 완료", Toast.LENGTH_SHORT).show();
                break;
        }
//        if (requestCode == PermissionManager.REQUEST_LOCATION) {
//            if (PermissionManager.verifyPermission(grantResults)) {
//                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
//                        ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                    return;
//                }
//
//                if (m_viewPager.getCurrentItem() == TAB_STATUS) {
//                    if (m_infoFrag == null) {
//                        m_infoFrag = (InfoFragment) getSupportFragmentManager().findFragmentByTag(
//                                "android:switcher:" + m_viewPager.getId() + ":" + ((TabPagerAdapter) m_viewPager.getAdapter())
//                                        .getItemId(TAB_STATUS));
//                    }
//
//                    m_infoFrag.requsetLocation();
//                }
//
//
//            } else {
//                for (String permissino : permissinos) {
//                    Log.d(TAG, "verifyPermission fail : " + permissino.toString());
//                }
//            }
//        } else {
//            super.onRequestPermissionsResult(requestCode, permissinos, grantResults);
//        }
    }

    /** GoogleMap callback **/

    @Override
    public void onLocationChanged(Location location) {
        GoogleMapManager.setCurrentLocation(location, "내 위치", "GPS Position");
        Toast.makeText(this, "LocationChaged : " + location.getSpeed(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        GoogleMapManager.setCurrentLocation(null, "Unknown GPS signal", "Check your GPS permission");

        googleMap.getUiSettings().setCompassEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            Toast.makeText(this, "위치 서비스 권한을 확인해주세요.", Toast.LENGTH_SHORT).show();
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        googleMap.setMyLocationEnabled(true);
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));

        final Location location = GoogleMapManager.getCurLocation();
        final float zoomLevel = GoogleMapManager.getZoomLevel();

        if( GoogleMapManager.getCurLocation() == null) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(GoogleMapManager.DEFAULT_LOCATION, zoomLevel));
        } else {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), zoomLevel));
            GoogleMapManager.setCurrentLocation(location, "Current Position", "GPS Position");
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Toast.makeText(this, "GoogleMap connected!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "GoogleMap connection failed!", Toast.LENGTH_SHORT).show();
    }

    /** Life cycle **/
    @Override
    protected void onResume() {
        super.onResume();
        GyroManager.m_sensorManager.registerListener(this, GyroManager.m_sensorAccel, SensorManager.SENSOR_DELAY_UI);
        GyroManager.m_sensorManager.registerListener(this, GyroManager.m_sensorMag, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onStop() {
        super.onStop();
        BTManager.closeBluetoothSocket();
    }

    @Override
    protected void onPause() {
        super.onPause();
        GyroManager.m_sensorManager.unregisterListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BTManager.closeBluetoothSocket();
    }

    /** GyroSensor **/
    public void onSensorChanged(SensorEvent sensorEvent) {
        if( sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER ) {
            try {
                GyroManager.shockStateDetector(this, sensorEvent, new ValidateCallback() {
                    @Override
                    public void onDone(int resultCode) throws JSONException {
                        if( resultCode == GyroManager.DETECT_ACCIDENT ) {
                            final Location accLocation = GoogleMapManager.getCurLocation();

                            EmergencyManager.setAccLocation(accLocation);
                            EmergencyManager.startValidationAccident(new ValidateCallback() {
                                @Override
                                public void onDone(int resultCode) throws JSONException {
                                    if (resultCode == EmergencyManager.EMERGENCY_VALIDATE_LOCATION_WATING_FINISH &&
                                            EmergencyManager.validateLocation(GoogleMapManager.getCurLocation())) {
                                        // insert data to server
//                                        EmergencyManager.insertAccidentinServer(UserManager.getUser(), accLocation);

                                    }
                                }
                            });
                        }
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


//        if (!m_bInitialize) { return; }
//
//        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
//            m_fAccel[0] = sensorEvent.values[0];
//            m_fAccel[1] = sensorEvent.values[1];
//            m_fAccel[2] = sensorEvent.values[2];
//
//            shockStateDetector(m_fAccel[0], m_fAccel[1], m_fAccel[2]);
//        }
//        else if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
//            m_fMag[0] = sensorEvent.values[0];
//            m_fMag[1] = sensorEvent.values[1];
//            m_fMag[2] = sensorEvent.values[2];
//        }
//
//        float[] resultValues = GyroManager.getOrientation(m_fAccel, m_fMag);
//
//        changeLeftOrRightLEDOfRoll(resultValues[2]);
//
//        GyroManager.setPivotRoll(resultValues[2]);
//
//        if( m_infoFrag == null) {
//            m_infoFrag = (InfoFragment) getSupportFragmentManager().findFragmentByTag(
//                    "android:switcher:" + m_viewPager.getId() + ":" + ((TabPagerAdapter)m_viewPager.getAdapter())
//                            .getItemId(TAB_STATUS));
//
////            m_infoFrag = (InfoFragment)m_viewPager
////                    .getAdapter()
////                    .instantiateItem(m_viewPager, TAB_STATUS);
//
//        } else {
////            m_infoFrag.setTextTiltXYZ(resultValues);
//        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    /** Navigation **/
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /** Top-Right control **/
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        m_navigationView.getMenu().findItem(id).setCheckable(false);
        m_navigationView.getMenu().findItem(id).setChecked(false);

        switch (id) {
            case R.id.nav_trackingRecords:

                break;

            case R.id.nav_emergencyContacts:
                Intent intent = new Intent(this, ContactActivity.class);
                startActivity(intent);

                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    /*
    public void shockStateDetector(float accelX, float accelY, float accelZ) {
        long currentTime = System.currentTimeMillis();
        long gabOfTime = (currentTime - m_shockStateLastTime);
        float speed = 0;
        if (gabOfTime > 100) {
            m_shockStateLastTime = currentTime;

            speed = Math.abs(accelX + accelY + accelZ - m_beforeAccelX - m_beforeAccelY - m_beforeAccelZ) / gabOfTime * 10000;

//            if(count++ < 3 ) {
//                sum_speed += speed;
//                return;
//            } else {
//                count = 0;
//                speed = sum_speed/3;
//                sum_speed = 0;
//            }
//            if (speed > 700 && speed < SHAKE_THRESHOLD) {
//                Log.e("speed", "speed : " + speed);
//                if(bProcessing) {// 사용자 실수시 가볍게 흔들어서 취소
//                    hanSensor.removeMessages(1);
//                    hanSensor.sendEmptyMessageDelayed(2, 1000);
//                    showMsgl("취소 " + speed);
//                    return;
//                }
            if (speed > SHAKE_THRESHOLD) {
                Log.d(TAG, "shockStateDetector: ");
                try {
                    String strSMS1 = getString(R.string.sms_content) + "\n\n" + m_strAddressOutput;
                    String strSMS2 = "https://google.com/maps?q=" + m_strLatitude + "," + m_strLogitude;

                    List<ContactItem> contactItems;
                    try {
                        contactItems = FileManager.readXmlEmergencyContacts(this);

                        for (ContactItem item :
                                contactItems) {
                            sendSMS(item.getPhoneNumber(), strSMS1);
                            sendSMS(item.getPhoneNumber(), strSMS2);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

//                    sendSMS("+8201034823161", strSMS1);
//                    sendSMS("+8201034823161", strSMS2);
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }
//                if(!bProcessing) {
//                    bProcessing = true;
//                    hanSensor.sendEmptyMessage(0);
//                    showMsgl("충격 발생 " + speed);

            }
//                else { // 2차 충격
//                    bProcessing = true;
//                    hanSensor.removeMessages(1); // 다이얼로그 연장
//                    hanSensor.sendEmptyMessageDelayed(1, send_time * 1000);
//                    showMsgl("2,3차 충격 " + speed);
//                }
//            }


            m_beforeAccelX = accelX;
            m_beforeAccelY = accelY;
            m_beforeAccelZ = accelZ;

        }
    }

    public void changeLeftOrRightLEDOfRoll(float roll) {
        String writeStr = "";

        if (m_curInterrupt == EMERGENCY) {
            return;
        }

        if (roll >= ROLL_PIVOT) {
            Log.d(TAG, "onSensorChanged: right");
            writeStr = "0-07-1";
            sendToBluetoothDevice(writeStr.getBytes());

            m_curInterrupt = ORIENTATION_RIGHT;
        } else if (roll <= -ROLL_PIVOT) {
            Log.d(TAG, "onSensorChanged: left");
            writeStr = "0-06-1";
            sendToBluetoothDevice(writeStr.getBytes());

            m_curInterrupt = ORIENTATION_LEFT;
        }

        if (Math.abs(GyroManager.getPivotRoll()) >= 20 &&
                Math.abs(roll) < 20) {
            writeStr = m_curLED;

            if (writeStr == null) {
                return;
            }


            sendToBluetoothDevice(writeStr.getBytes());

            m_curInterrupt = ORIENTATION_NONE;
        }
    }
    */
}

