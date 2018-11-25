/*
 * Copyright (c) 10/15/18 1:54 PM
 * Written by Sungdong Jo
 * Description:
 */

package com.helper.helper.view;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bcgdv.asia.lib.ticktock.TickTockView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.helper.helper.R;
import com.helper.helper.controller.AddressManager;
import com.helper.helper.controller.BTManager;
import com.helper.helper.controller.DownloadImageTask;
import com.helper.helper.controller.EmergencyManager;
import com.helper.helper.controller.FileManager;
import com.helper.helper.controller.GoogleMapManager;
import com.helper.helper.controller.SMSManager;
import com.helper.helper.controller.SharedPreferencer;
import com.helper.helper.controller.UserManager;
import com.helper.helper.controller.ViewStateManager;
import com.helper.helper.interfaces.HttpCallback;
import com.helper.helper.interfaces.ValidateCallback;
import com.helper.helper.model.LEDCategory;
import com.helper.helper.model.User;
import com.helper.helper.view.assist.AssistActivity;
import com.helper.helper.view.main.myeight.EightFragment;
import com.helper.helper.view.main.myeight.InfoFragment;
import com.helper.helper.view.contact.ContactActivity;
import com.helper.helper.controller.GyroManager;
import com.helper.helper.controller.HttpManager;
import com.helper.helper.controller.PermissionManager;
import com.helper.helper.view.widget.DialogTickTock;
import com.helper.helper.view.widget.WrapContentViewPager;
import com.snatik.storage.Storage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

import static android.support.design.widget.TabLayout.*;

public class ScrollingActivity extends AppCompatActivity
        implements SensorEventListener,
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        NavigationView.OnNavigationItemSelectedListener,
        EightFragment.OnFragmentInteractionListener,
        InfoFragment.OnFragmentInteractionListener {
    private final static String TAG = ScrollingActivity.class.getSimpleName() + "/DEV";

//    private static final int TAB_STATUS = 0;
//    private static final int TAB_LED = 1;
//    private static final int TAB_TRACKING = 2;

    private static final int PERMISSION_REQUEST = 267;

    /***************** Define widgtes in view *******************/
    private NavigationView m_navigationView;

    private TabLayout m_tabLayout;
    private TabPagerAdapter m_pagerAdapter;
    private ViewPager m_viewPager;

    private NestedScrollView m_nestedScroll;

    private SweetAlertDialog m_accDialog;
    private SweetAlertDialog m_loadingDialog;
    /**************************************************************/



    public ViewPager getViewPager() {
        return m_viewPager;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /** Http Server **/
        HttpManager.setServerURI(getString(R.string.server_uri));

        final Activity activity = this;

        Log.d(TAG, "onCreate: ");

        /******************* Connect widgtes with layout *******************/
        setContentView(R.layout.activity_scrolling);

        /** Set Emergency Contacts **/
        if ( EmergencyManager.getEmergencyContacts() == null ) {
            try {
                EmergencyManager.setEmergencycontacts(FileManager.readXmlEmergencyContacts(this));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /** Set User Info **/
        try {
            m_loadingDialog = makeLoadingDialog();
            m_loadingDialog.show();
            m_loadingDialog.findViewById(R.id.confirm_button).setVisibility(View.GONE);
            LinearLayout parentOfConfirmBtn = (LinearLayout)m_loadingDialog.findViewById(R.id.confirm_button).getParent();
            parentOfConfirmBtn.setVisibility(View.GONE);

            UserManager.setUser(FileManager.readXmlUserInfo(this), new ValidateCallback() {
                @Override
                public void onDone(int resultCode) {
                    if( resultCode == UserManager.DONE_SET_USER ) {
                        DownloadImageTask downloadUserDataLED = new DownloadImageTask(activity, new ValidateCallback() {
                            @Override
                            public void onDone(int resultCode) {
                                if( resultCode == DownloadImageTask.DONE_LOAD_LED_IMAGES ) {
                                    m_loadingDialog.dismissWithAnimation();
                                }
                            }
                        });
                        /** Download User's LED **/
                        downloadUserDataLED.execute(UserManager.getUser().getUserLEDIndiciesURI(getString(R.string.server_uri)));
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        /** Set SharedPreferencer **/
        SharedPreferencer.getSharedPreferencer(this, UserManager.getUserEmail(), MODE_PRIVATE);

        /** Set Shop Data **/
        startInitializeShopData();

        /** ToolBar **/
        Toolbar toolbar = findViewById(R.id.toolbar);
        TextView mTitle = toolbar.findViewById(R.id.toolbar_title);

        setSupportActionBar(toolbar);
        mTitle.setText(toolbar.getTitle());

        getSupportActionBar().setDisplayShowTitleEnabled(false);

        /** Navigation **/
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        m_navigationView = findViewById(R.id.nav_view);
        m_navigationView.setNavigationItemSelectedListener(this);

        View headerView = m_navigationView.getHeaderView(0);

        TextView navUserName = headerView.findViewById(R.id.navUserName);
        navUserName.setText(UserManager.getUserName());
        ImageView navUserProfile = headerView.findViewById(R.id.navUserProfile);

        // TODO: 03/11/2018 set User Profile Bitmap.
//        navUserProfile.setImageBitmap(UserManager.getUserProfileBitmap());
        

        /** Tab **/
        m_tabLayout = findViewById(R.id.tabLayout);
        m_tabLayout.addTab(m_tabLayout.newTab().setText("MY EIGHT"));
        m_tabLayout.addTab(m_tabLayout.newTab().setText("LED"));
//        m_tabLayout.addTab(m_tabLayout.newTab().setText("TRACKING"));

        m_pagerAdapter = new TabPagerAdapter(getSupportFragmentManager(), m_tabLayout.getTabCount());

        m_viewPager = findViewById(R.id.pager);
        m_viewPager.setAdapter(m_pagerAdapter);
        m_viewPager.addOnPageChangeListener(new TabLayoutOnPageChangeListener(m_tabLayout));
        m_tabLayout.addOnTabSelectedListener(new OnTabSelectedListener() {
            @Override
            public void onTabSelected(Tab tab) {
                Log.d("DEV", "onTabSelected called! posistion : " + tab.getPosition());
                m_viewPager.setCurrentItem(tab.getPosition());
//                m_viewPager.reMeasureCurrentPage(tab.getPosition());
                /*
                if (tab.getPosition() == TAB_STATUS) {

                } else if (tab.getPosition() == TAB_TRACKING) {

                }
                */
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


        /** Request permissions **/
        if (!PermissionManager.checkPermissions(this, Manifest.permission.ACCESS_FINE_LOCATION) ||
                !PermissionManager.checkPermissions(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {

            /* Result about user selection -> onActivityResult in ScrollActivity */
            PermissionManager.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.SEND_SMS}, PERMISSION_REQUEST);
        }

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

    private void startInitializeShopData() {
        if( HttpManager.useCollection(getString(R.string.collection_category)) ) {

            JSONObject reqObject = new JSONObject();
            String str = reqObject.toString();
            try {
                HttpManager.requestHttp(reqObject, "", "GET", "", new HttpCallback() {
                    @Override
                    public void onSuccess(JSONArray existIdjsonArray) throws JSONException {
                        int arrLen = existIdjsonArray.length();
                        // write category xml file
                        List<LEDCategory> ledCategoryList = new ArrayList<>();

                        for (int i = 0; i < existIdjsonArray.length(); i++) {
                            JSONObject categoryObj = existIdjsonArray.getJSONObject(i);

                            LEDCategory category = new LEDCategory(
                                    categoryObj.getString("name"),
                                    categoryObj.getString("backgroundColor"),
                                    categoryObj.getString("notice"),
                                    categoryObj.getString("character")
                            );
                            ledCategoryList.add(category);
                        }
                        try {
                            FileManager.writeXmlCategory(getApplicationContext(), ledCategoryList);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(String err) {
                        Log.d(TAG, "startInitializeShopData onError: " + err);
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /** Dialog **/
    private SweetAlertDialog resetAccDialog() {
        final Context thisContext = this;
        return
            new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText(getString(R.string.emergency_dialog_title))
                    .setCancelText(getString(R.string.emergency_dialog_cancel))
//                    .setNeutralText(getString(R.string.emergency_dialog_nearby))
//                    .setNeutralClickListener(new SweetAlertDialog.OnSweetClickListener() {
//                        @Override
//                        public void onClick(SweetAlertDialog sweetAlertDialog) {
//                            sweetAlertDialog.dismissWithAnimation();
//                            Intent intent = new Intent(thisContext, AssistActivity.class);
//                            startActivity(intent);
//                        }
//                    })
                    .setConfirmText(getString(R.string.emergency_dialog_send))
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(final SweetAlertDialog sDialog) {
                            startAlertEmergencyContacts();
                            try {
                                EmergencyManager.insertAccidentinServer(thisContext, UserManager.getUser(), EmergencyManager.getAccLocation(), true);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
    }

    private SweetAlertDialog makeLoadingDialog() {
        SweetAlertDialog dlg = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        dlg.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        dlg.setTitleText(getString(R.string.loading_dialog_user_data));
        dlg.setCancelable(false);
        return dlg;
    }

    private void startAlertEmergencyContacts() {
        SMSManager.sendEmergencyMessages(
                this,
                EmergencyManager.getEmergencyContacts(),
                EmergencyManager.getAccLocation(),
                AddressManager.getConvertLocationToAddress());



        m_accDialog
                .setTitleText(getString(R.string.emergency_dialog_send_completely))
                .setContentText(getString(R.string.emergency_dialog_coming))
                .setConfirmText(getString(R.string.dialog_ok))
                .showCancelButton(false)
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        m_accDialog.dismissWithAnimation();
                    }
                })
                .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                m_accDialog.dismissWithAnimation();
            }
        }, 3000);
    }

    /** Result handler **/
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
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
    }

    /** GoogleMap callback **/

    @Override
    public void onLocationChanged(Location location) {
        GoogleMapManager.setCurrentLocation(location, "내 위치", "GPS Position");
        Toast.makeText(this, "LocationChaged : " + location.getSpeed(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

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
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart: ");
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");

        TabLayout.Tab tab = m_tabLayout.getTabAt(ViewStateManager.getSavedTabPosition());
        if (tab != null) {
            tab.select();
        }

//        GyroManager.m_sensorManager.registerListener(this, GyroManager.m_sensorAccel, SensorManager.SENSOR_DELAY_UI);
//        GyroManager.m_sensorManager.registerListener(this, GyroManager.m_sensorMag, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onStop() {
        super.onStop();
        ViewStateManager.saveTabPosition(m_tabLayout.getSelectedTabPosition());
//        ViewStateManager.stop ++;
//        Log.d(TAG, "onStop: ");
    }

    @Override
    protected void onPause() {
        super.onPause();
//        Log.d(TAG, "onPause: ");
//        GyroManager.m_sensorManager.unregisterListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

//        Log.d(TAG, "onDestroy: ");

        BTManager.closeBluetoothSocket();
    }

//    /** GyroSensor **/

//    @Override
//    public void onSensorChanged(SensorEvent sensorEvent) {
//
//    }
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        if( sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER ) {
            final Activity activity = this;
            try {
                /** shock detect **/
                GyroManager.shockStateDetector(activity, sensorEvent, new ValidateCallback() {
                    @Override
                    public void onDone(int resultCode) {
                        if( resultCode == GyroManager.DETECT_ACCIDENT ) {

                            /** permission (location) **/
                            if ( !PermissionManager.checkPermissions(activity, Manifest.permission.ACCESS_COARSE_LOCATION) ||
                                    !PermissionManager.checkPermissions(activity, Manifest.permission.ACCESS_FINE_LOCATION) ) {
                                Toast.makeText(activity, "사고를 인지했지만 위치 정보 권한이 허용되지 않아 제대로 동작하지 않습니다.", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            final Location accLocation = GoogleMapManager.getCurLocation();
                            AddressManager.startAddressIntentService(activity, accLocation);
                            EmergencyManager.setAccLocation(accLocation);

                            EmergencyManager.startValidationAccident(new ValidateCallback() {
                                @Override
                                public void onDone(int resultCode) throws JSONException {
                                    /** Consider accident **/
                                    if (resultCode == EmergencyManager.EMERGENCY_VALIDATE_LOCATION_WAITNG_FINISH &&
                                            EmergencyManager.validateLocation(GoogleMapManager.getCurLocation())) {

                                        EmergencyManager.insertAccidentinServer(activity, UserManager.getUser(), accLocation, false);

                                        m_accDialog = resetAccDialog();
                                        DialogTickTock tickTockDlg = new DialogTickTock(activity, EmergencyManager.EMERGENCY_WAITING_ALERT_SECONDS);
                                        tickTockDlg.setOnTickListener(new TickTockView.OnTickListener() {
                                            @Override
                                            public String getText(long timeRemainingInMillis) {
                                                int seconds = (int) (timeRemainingInMillis / 1000) % 60;

                                                if( seconds == 0) {
                                                    startAlertEmergencyContacts();
                                                    try {
                                                        EmergencyManager.insertAccidentinServer(activity, UserManager.getUser(), EmergencyManager.getAccLocation(), true);
                                                    } catch (JSONException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                                return String.valueOf(seconds).concat("s");
                                            }
                                        });
                                        m_accDialog.setCustomView(tickTockDlg);

                                        m_accDialog.show();

                                        EmergencyManager.startWaitingUserResponse(new ValidateCallback() {
                                            @Override
                                            public void onDone(int resultCode) {
                                                if( resultCode == EmergencyManager.EMERGENCY_WAITING_USER_RESPONSE ) {
                                                    if( m_accDialog.isShowing() ) {
                                                        startAlertEmergencyContacts();
                                                        try {
                                                            EmergencyManager.insertAccidentinServer(activity, UserManager.getUser(), accLocation, true);
                                                        } catch (JSONException e) {
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                }
                                            }
                                        });
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
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    /** Navigation **/
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
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

        Intent intent;
        switch (id) {
            case R.id.nav_trackingRecords:

                break;

            case R.id.nav_emergencyContacts:
                intent = new Intent(this, ContactActivity.class);
                startActivity(intent);

                break;

//            case R.id.nav_assistPlaces:
//                intent = new Intent(this, AssistActivity.class);
//                startActivity(intent);
//                break;
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    public void messageFromParentFragment(Uri uri) {
        Log.i("TAG", "received communication from parent fragment");
    }

    @Override
    public void messageFromChildFragment(Uri uri) {
        Log.i("TAG", "received communication from child fragment");
    }

    /*
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

