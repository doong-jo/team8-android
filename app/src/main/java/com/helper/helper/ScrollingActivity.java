package com.helper.helper;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.helper.helper.Info.InfoFragment;
import com.helper.helper.ble.BluetoothLeService;
import com.helper.helper.tracking.TrackingData;
import com.helper.helper.util.FileManagerUtil;
import com.helper.helper.util.GyroManagerUtil;
import com.helper.helper.util.PermissionUtil;
import com.snatik.storage.Storage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static java.lang.Math.abs;


public class ScrollingActivity extends AppCompatActivity implements SensorEventListener{
    private final static String TAG = ScrollingActivity.class.getSimpleName() + "/DEV";
    private static final int TAB_STATUS = 0;
    private static final int TAB_LED = 1;
    private static final int TAB_TRACKING = 2;
    private static final int REQUEST_ENABLE_BT = 2001;
    private static final int AZIMUTH_PIVOT = 20;
    private static final int PITCH_PIVOT = 5;
    private static final String BT_UUID = "94f39d29-7d6d-437d-973b-fba39e49d4ee";
    private static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_WRITE = 2;
    public static final int MESSAGE_READ = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    private TabLayout m_tabLayout;
    private TabPagerAdapter m_pagerAdapter;
    private ViewPager m_viewPager;
    private InfoFragment m_infoFrag;

    private boolean m_IsSupportedBT = false;
    private BluetoothAdapter m_bluetoothAdapter;
    private BluetoothLeService m_bluetoothLeService;
    private BluetoothGattCharacteristic m_characteristicTX;
    private BluetoothGattCharacteristic m_characteristicRX;
    private BluetoothDevice m_pairedDevice;
    private ConnectedThread m_btThread;

    private BluetoothSocket m_bluetoothSocket;
    private InputStream m_bluetoothInput;
    private OutputStream m_bluetoothOutput;
    @SuppressLint("HandlerLeak")
    private final Handler m_handle = new Handler() {


        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
//                    if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    switch (msg.arg1) {

                    }
                    break;
                case MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
//                    mConversationArrayAdapter.add("Me:  " + writeMessage);
                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
//                    mConversationArrayAdapter.add(mConnectedDeviceName+":  " + readMessage);
                    break;
                case MESSAGE_DEVICE_NAME:
                    // save the connected device's name
//                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
//                    Toast.makeText(getApplicationContext(), "Connected to "
//                            + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
//                    Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
//                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    private SensorManager m_sensorManager;
    private Sensor m_sensorAccel;
    private Sensor m_sensorMag;
    private float[] m_fMag = new float[3];
    private float[] m_fAccel = new float[3];

    private boolean m_IsRecorded = false;

    private String m_recordStartDate;
    private String m_recordStartTime;
    private String m_recordEndTime;

    public ViewPager getViewPager() { return m_viewPager; }
    public boolean getIsRecorded() { return m_IsRecorded; }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);

        /* ToolBar UI start */
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        /* ToolBar UI end */

        /* Tab start */
        m_tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        m_tabLayout.addTab(m_tabLayout.newTab().setText("Status"));
        m_tabLayout.addTab(m_tabLayout.newTab().setText("LED"));
        m_tabLayout.addTab(m_tabLayout.newTab().setText("Tracking"));

        m_viewPager = (ViewPager) findViewById(R.id.pager);

        m_pagerAdapter = new TabPagerAdapter(getSupportFragmentManager(), m_tabLayout.getTabCount());
        m_viewPager.setAdapter(m_pagerAdapter);
        m_viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(m_tabLayout));

        m_tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Log.d("DEV", "onTabSelected called! posistion : " + tab.getPosition());
                m_viewPager.setCurrentItem(tab.getPosition());

                if( tab.getPosition() == TAB_STATUS ) {

                    if( m_IsRecorded ) {
                        ((FloatingActionButton) m_infoFrag.getView().findViewById(R.id.recordToggleBtn)).setImageDrawable(getResources().getDrawable(R.drawable.ic_stop_solid, getApplicationContext().getTheme()));
                    }
                    else {
                        ((FloatingActionButton) m_infoFrag.getView().findViewById(R.id.recordToggleBtn)).setImageDrawable(getResources().getDrawable(R.drawable.ic_circle_solid, getApplicationContext().getTheme()));
                    }
                }
                else if( tab.getPosition() == TAB_TRACKING ) {


                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        /* Tab end */

        /* Valid Bluetooth supports start */
        m_bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (m_bluetoothAdapter == null) {
            m_IsSupportedBT = false;
        } else {
            m_IsSupportedBT = true;
        }
        /* Valid Bluetooth supports end */

        /* Internal File Storage setup start */
        Storage storage = new Storage(getApplicationContext());
        String path = storage.getInternalFilesDirectory();
        String newDir = path + File.separator + "user_data";

        boolean dirExists = storage.isDirectoryExists(path);

        if( dirExists ) {
            Toast.makeText(this, newDir + " is exist", Toast.LENGTH_SHORT).show();
        } else {
            storage.createDirectory(newDir);
        }

        /* Internal File Storage setup end */

        /* Set Bluetooth start */
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        /* Set Bluetooth end */

        /* Sensor start */
        GyroManagerUtil.m_sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        GyroManagerUtil.m_sensorAccel = GyroManagerUtil.m_sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        GyroManagerUtil.m_sensorMag = GyroManagerUtil.m_sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        /* Sensor end */
    }

    public void moveToLEDDash(View v) {
        m_viewPager.setCurrentItem(TAB_LED);
    }

    public void moveToTrackingDash(View v) {
        m_viewPager.setCurrentItem(TAB_TRACKING);
    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            m_bluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            Log.d(TAG, "m_bluetoothLeService to initialize Bluetooth");
            if (!m_bluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }

            m_bluetoothLeService.setInfoFragment(m_infoFrag);

            /* Connect Bluetooth Device Start
               (Automatically connects to the device upon successful start-up initialization.) */
            if (!m_bluetoothAdapter.isEnabled()) {
                Toast.makeText(getApplicationContext(), "disable bluetooth", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "Already enable bluetooth", Toast.LENGTH_SHORT).show();
                try {
                    connectDevice();
                    updateConnectionLayout(true);
                } catch (IOException e) {
                    e.printStackTrace();
                    updateConnectionLayout(false);
                }
            }
            /* Connect Bluetooth Device End */
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            m_bluetoothLeService = null;
        }
    };

    public void enableBluetooth(View v) {
        if (!m_IsSupportedBT) {
            return;
        }

        if (!m_bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            //call onActivityResult
        } else {
            try {
                connectDevice();
                updateConnectionLayout(true);
            } catch (IOException e) {
                e.printStackTrace();
                updateConnectionLayout(false);
            }
        }
    }

    public void updateConnectionLayout(boolean IsConnected) {
        Toast.makeText(this, "connected paired device HELPER!", Toast.LENGTH_SHORT).show();

        LinearLayout connectLayout = (LinearLayout) findViewById(R.id.connect_layout);
        TextView connectDiscription = (TextView) findViewById(R.id.connect_desc_text);
        TextView connectToggle = (TextView) findViewById(R.id.connect_toggle_text);

        if (IsConnected) {
            connectLayout.setOnClickListener(null);
            connectDiscription.setText(getString(R.string.connect_state));
            connectToggle.setText("");
        } else {
            connectLayout.setOnClickListener(enableBluetoothView);
            connectDiscription.setText(getString(R.string.disconnect_state));
            connectToggle.setText(getString(R.string.connect_device));
        }

    }

    View.OnClickListener enableBluetoothView = new View.OnClickListener() {
        public void onClick(View v) {
            enableBluetooth(v);
        }
    };

    public void connectDevice() throws IOException {
        // 만약 페어링 기기들 리스트에 있다면 바로 연결
        List<BluetoothDevice> devices = new ArrayList<BluetoothDevice>(m_bluetoothAdapter.getBondedDevices());

        String[] deviceLabels = new String[devices.size()];
        for (int i = 0; i < deviceLabels.length; ++i) {
            Log.d(TAG, "connectDevice: "+ devices.get(i).getName());
            Log.d(TAG, "connectDevice: "+ getString(R.string.device_bluetooth_name));
            String deviceSSID = new String(getString(R.string.device_bluetooth_name));

            if( devices.get(i).getName().equals(deviceSSID) ) {
                Log.d(TAG, "connectDevice: equal");
            } else {
                Log.d(TAG, "connectDevice: not equal");
            }

            if (devices.get(i).getName().equals(deviceSSID)
//                    &&  m_bluetoothLeService.connect(devices.get(i).getAddress()
            ) {
                m_pairedDevice = devices.get(i);
                m_bluetoothSocket = m_pairedDevice.createRfcommSocketToServiceRecord(UUID.fromString(BT_UUID));
                m_bluetoothInput = m_bluetoothSocket.getInputStream();
                m_bluetoothOutput = m_bluetoothSocket.getOutputStream();

                m_bluetoothSocket.connect();

                m_btThread = new ConnectedThread(m_bluetoothSocket);
                m_btThread.run();

//                InitializeBLESignal();
                return;
            }
        }

        // 페어링 기기가 없다면 새로 찾아서 연결
        if (m_bluetoothAdapter.startDiscovery()) {
            BroadcastReceiver mDiscoveryReceiver = new BroadcastReceiver() {

                @Override

                public void onReceive(Context context, Intent intent) {

                    if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {

                        BluetoothDevice searchedDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        if (searchedDevice.getName() == null) {
                            return;
                        }

                        Log.d(TAG, "searchedDevice : " + searchedDevice.getName() + "\n" + searchedDevice.getAddress());

                        String deviceSSID = new String(getString(R.string.device_bluetooth_name));

                        // HELPER
                        if (searchedDevice.getName().equals(deviceSSID)
//                                && m_bluetoothLeService.connect(searchedDevice.getAddress()
                                ) {
                            m_pairedDevice = searchedDevice;
                            try {
                                m_bluetoothSocket = m_pairedDevice.createRfcommSocketToServiceRecord(UUID.fromString(BT_UUID));
                                m_bluetoothInput = m_bluetoothSocket.getInputStream();
                                m_bluetoothOutput = m_bluetoothSocket.getOutputStream();

                                m_bluetoothSocket.connect();

                                m_btThread = new ConnectedThread(m_bluetoothSocket);
                                m_btThread.run();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            Toast.makeText(getApplicationContext(), "connected device HELPER!", Toast.LENGTH_SHORT).show();
//                            InitializeBLESignal();
                        }
                    }
                }

            };

            registerReceiver(mDiscoveryReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        }
    }

    public void sendSignal(View v) {
//        if( m_characteristicTX == null ) { return; }

        String str = "helper";

//        write(str.getBytes());

        switch (v.getResources().getResourceName(v.getId())) {
            case "com.helper.helper:id/img1":
                str = "LED_CHARACTERS";
                break;

            case "com.helper.helper:id/img2":
                str = "LED_WINDY";
                break;

            case "com.helper.helper:id/img3":
                str = "LED_SNOW";
                break;

            case "com.helper.helper:id/img4":
                str = "LED_RAIN";
                break;

            case "com.helper.helper:id/img5":
                str = "LED_CUTE";
                break;

            case "com.helper.helper:id/img6":
                str = "LED_LEFT";
                break;

            case "com.helper.helper:id/img7":
                str = "LED_RIGHT";
                break;

            case "com.helper.helper:id/img8":
                str = "LED_EMERGENCY";
                break;
        }

        final byte[] tx = str.getBytes();

        write(str.getBytes());

//        m_characteristicTX.setValue(tx);
//
//        m_bluetoothLeService.writeCharacteristic(m_characteristicTX);
//        m_bluetoothLeService.readCharacteristic(m_characteristicRX);
//
//        Log.d("DEV", "sendSignal called! TX : " + new String(m_characteristicTX.getValue()));
//        Log.d("DEV", "sendSignal called! RX : " + new String(m_characteristicRX.getValue()));
    }

    public void toggleRecord(View v) {
        if( m_viewPager.getCurrentItem() != TAB_STATUS ) {
            return;
        }

        m_IsRecorded = !m_IsRecorded;

        if( m_IsRecorded ) {
            Date date = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            m_recordStartDate = dateFormat.format(date);

            SimpleDateFormat startTimeFormat = new SimpleDateFormat("hh:mm");
            m_recordStartTime = startTimeFormat.format(date);

            ((FloatingActionButton) v).setImageDrawable(getResources().getDrawable(R.drawable.ic_stop_solid, getApplicationContext().getTheme()));
        } else {
            ((FloatingActionButton) v).setImageDrawable(getResources().getDrawable(R.drawable.ic_circle_solid, getApplicationContext().getTheme()));

            Date date = new Date();

            SimpleDateFormat endTimeFormat = new SimpleDateFormat("hh:mm");
            m_recordEndTime = endTimeFormat.format(date);

            try {
                TrackingData trackingData = new TrackingData(
                        m_recordStartDate,
                        m_recordStartTime,
                        m_recordEndTime,
                        String.format("%f", m_infoFrag.getCurTrackingDistance()),
                        m_infoFrag.getCurrRecordedLocationList());

                FileManagerUtil.writeTrackingDataInternalStorage(getApplicationContext(), trackingData);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if( m_infoFrag == null ) {
                m_infoFrag = (InfoFragment)m_viewPager
                        .getAdapter()
                        .instantiateItem(m_viewPager, TAB_STATUS);
            }

            m_infoFrag.recordStopAndEraseLocationList();
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                // Allow Bluetooth

                if (resultCode == Activity.RESULT_OK) {
                    try {
                        connectDevice();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(this, "You can't use interaction helper device", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissinos,
                                           @NonNull int[] grantResults) {

        if (requestCode == PermissionUtil.REQUEST_LOCATION) {
            if (PermissionUtil.verifyPermission(grantResults)) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }

                if( m_viewPager.getCurrentItem() == TAB_STATUS ) {
                    if( m_infoFrag == null ) {
                        m_infoFrag = (InfoFragment)m_viewPager
                                .getAdapter()
                                .instantiateItem(m_viewPager, m_viewPager.getCurrentItem());
                    }

                    m_infoFrag.requsetLocation();
                }


            } else {
                for (String permissino : permissinos) {
                    Log.d(TAG, "verifyPermission fail : " + permissino.toString());
                }
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissinos, grantResults);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_scrolling, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        GyroManagerUtil.m_sensorManager.registerListener(this, GyroManagerUtil.m_sensorAccel, SensorManager.SENSOR_DELAY_UI);
        GyroManagerUtil.m_sensorManager.registerListener(this, GyroManagerUtil.m_sensorMag, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        GyroManagerUtil.m_sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            m_fAccel[0] = sensorEvent.values[0];
            m_fAccel[1] = sensorEvent.values[1];
            m_fAccel[2] = sensorEvent.values[2];

        }
        else if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            m_fMag[0] = sensorEvent.values[0];
            m_fMag[1] = sensorEvent.values[1];
            m_fMag[2] = sensorEvent.values[2];
        }

        float[] resultValues = GyroManagerUtil.getOrientation(m_fAccel, m_fMag);

        long curMillis = System.currentTimeMillis();
//        Log.d(TAG, "onSensorChanged: diff milles -> " + String.format("%d", curMillis - GyroManagerUtil.getTimerStartTime()));
//        Log.d(TAG, "onSensorChanged: cur milles -> " + String.format("%d", curMillis));
//        Log.d(TAG, "onSensorChanged: start milles -> " + String.format("%d", GyroManagerUtil.getTimerStartTime()));

        if(GyroManagerUtil.getPivotPitch() == 0.0f) {
            GyroManagerUtil.setPivotPitch(resultValues[1]);
        }
        if ( curMillis - GyroManagerUtil.getTimerStartTime() > 5000 ) {
            GyroManagerUtil.setTimerStartTime(curMillis);
//            Log.d(TAG, "onSensorChanged: 3000 millis after!");
            GyroManagerUtil.setPivotAzimuth(resultValues[0]);
            Toast.makeText(this, "Change pivot!", Toast.LENGTH_SHORT).show();
        }


        else {
            if( /*직진상태다*/ resultValues[1] - GyroManagerUtil.getPivotPitch() < 5 )  {
//                Log.d(TAG, "onSensorChanged: 직진상태다");
//                Toast.makeText(this, "직진상태다", Toast.LENGTH_SHORT).show();
            }
            /*회전상태다*/
            else  {
//                Log.d(TAG, "onSensorChanged: 회전상태다");

                // 방위 값으로 좌우 방향 검사
                // 서로의 부호가 다를 때.
                if( resultValues[0] * GyroManagerUtil.getPivotAzimuth() < 0 ) {
                    //현재 값이 양수
                    float convertPivot;
                    float convertAfeter;

                    if (resultValues[0] > 0 && GyroManagerUtil.getPivotAzimuth() < 0) {
                        convertPivot = 180 - resultValues[0];
                        convertAfeter = 180 - abs(GyroManagerUtil.getPivotAzimuth());

                        if (convertPivot + convertAfeter >= AZIMUTH_PIVOT) {
                            //우회전상태
                            Log.d(TAG, "onSensorChanged: 부호가 다르고 현재값 양수 Right!");
//                            Toast.makeText(this, "onSensorChanged : 부호가 다르고 현재값 양수 Right!", Toast.LENGTH_SHORT).show();
//                            GyroManagerUtil.setPivotAzimuth(resultValues[0]);
                        }
                    }
                    //현재 값이 음수
                    else if (resultValues[0] < 0 && GyroManagerUtil.getPivotAzimuth() > 0) {
                        convertPivot = 180 - abs(resultValues[0]);
                        convertAfeter = 180 - abs(GyroManagerUtil.getPivotAzimuth());

                        if (convertPivot + convertAfeter >= AZIMUTH_PIVOT) {
                            //좌회전상태
                            Log.d(TAG, "onSensorChanged: 부호가 다르고 현재값 음수 Left!");
//                            Toast.makeText(this, "onSensorChanged : 부호가 다르고 현재값 음수 Left!", Toast.LENGTH_SHORT).show();
//                            GyroManagerUtil.setPivotAzimuth(resultValues[0]);
                        }
                    }
                }

                //서로의 부호가 같을 때
                else if( resultValues[0] * GyroManagerUtil.getPivotAzimuth() > 0 ) {
                    //양수일 때
                    if (resultValues[0] > 0 ) {
                        if( resultValues[0] - GyroManagerUtil.getPivotAzimuth() >= AZIMUTH_PIVOT ) {
                            //우회전 상태
                            Log.d(TAG, "onSensorChanged: 부호가 같고 현재값 양수 Right!");
//                            Toast.makeText(this, "onSensorChanged : 부호가 같고 양수 Left!", Toast.LENGTH_SHORT).show();
                            GyroManagerUtil.setPivotAzimuth(resultValues[0]);
                        }
                        else if( GyroManagerUtil.getPivotAzimuth() - resultValues[0] >= AZIMUTH_PIVOT ) {
                            //좌회전 상태
                            Log.d(TAG, "onSensorChanged: 부호가 같고 현재값 양수 Left!");
//                            Toast.makeText(this, "onSensorChanged : 부호가 같고 양수 Right!", Toast.LENGTH_SHORT).show();
                            GyroManagerUtil.setPivotAzimuth(resultValues[0]);
                        }
                    }
                    //음수일 때
                    else if ( resultValues[0] < 0 ) {
                        if( resultValues[0] - GyroManagerUtil.getPivotAzimuth() >= AZIMUTH_PIVOT ) {
                            //좌회전 상태
                            Log.d(TAG, "onSensorChanged: 부호가 같고 음수 Left!");
//                            Toast.makeText(this, "onSensorChanged : 부호가 같고 음수 Left!", Toast.LENGTH_SHORT).show();
                            GyroManagerUtil.setPivotAzimuth(resultValues[0]);
                        }
                        else if( GyroManagerUtil.getPivotAzimuth() - resultValues[0] >= AZIMUTH_PIVOT ) {
                            //우회전 상태
                            Log.d(TAG, "onSensorChanged: 부호가 같고 음수 Right!");
//                            Toast.makeText(this, "onSensorChanged : 부호가 같고 음수 Right!", Toast.LENGTH_SHORT).show();
//                            GyroManagerUtil.setPivotAzimuth(resultValues[0]);
                        }
                    }
                }

            }
        }

//        resultValues[0] = resultValues[0] - GyroManagerUtil.getPivotAzimuth();
//        resultValues[1] = resultValues[1] - GyroManagerUtil.getPivotPitch();
        m_infoFrag = (InfoFragment) getSupportFragmentManager().findFragmentByTag(
                "android:switcher:" + m_viewPager.getId() + ":" + ((TabPagerAdapter)m_viewPager.getAdapter())
                        .getItemId(TAB_STATUS));

        if(m_infoFrag != null) { m_infoFrag.setTextTiltXYZ(resultValues); }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket m_socket;
        private final InputStream m_input;
        private final OutputStream m_output;

        public ConnectedThread(BluetoothSocket socket) {
            m_socket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {

            }

            m_input = tmpIn;
            m_output = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

//            while(true) {
////                try {
//////                    bytes = m_input.read(buffer);
//////                    m_handle.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
//////                            .sendToTarget();
////                } catch (IOException e){
////                    break;
////                }
//            }
        }
    }

    public void write(byte[] bytes) {
        try {
            m_bluetoothOutput.write(bytes);
        } catch (IOException e) { }
    }

    public void cancel() {
        try {
            m_bluetoothSocket.close();
        } catch (IOException e) { }
    }

    //    public void InitializeBLESignal() {
//        Log.d(TAG, "InitializeSignal: Start!");
//
//
//        /*
//        final Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            @Override
//            //First uuid : GAP (Generic Acess Profile)
//            //Second uuid : GATT (Generic Attribute Profile)
//            public void run() {
//                List<BluetoothGattService> gattServices = m_bluetoothLeService.getSupportedGattServices();
//
//                if( gattServices.size() == 0 ) {
//                    return;
//                }
//
//                for (BluetoothGattService gattService : gattServices) {
//                    String str = gattService.toString();
//                    String uuid = gattService.getUuid().toString();
//                    // get chara cteristic when UUID matches RX/TX UUID
//                    m_characteristicTX = gattService.getCharacteristic(BluetoothLeService.UUID_HM_RX_TX);
//                    m_characteristicRX = gattService.getCharacteristic(BluetoothLeService.UUID_HM_RX_TX);
////                    Log.d(TAG, "run: UUID is gattService.getUuid()");
////                    m_characteristicTX = gattService.getCharacteristic(gattService.getUuid());
////                    m_characteristicRX = gattService.getCharacteristic(gattService.getUuid());
//                }
//
//                m_bluetoothLeService.setCharacteristicNotification(m_characteristicRX, true);
//
//                updateConnectionLayout(true);
//
//                Toast.makeText(getApplicationContext(), "InitializeSignal called!", Toast.LENGTH_SHORT).show();
//            }
//        }, 3000);
//
//        */
//    }
}

