package com.helper.helper;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
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
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.os.ResultReceiver;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.helper.helper.Info.InfoFragment;
import com.helper.helper.ble.BluetoothLeService;
import com.helper.helper.led.LEDFragment;
import com.helper.helper.location.Constants;
import com.helper.helper.location.FetchAddressIntentService;
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
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;


public class ScrollingActivity extends AppCompatActivity implements SensorEventListener {
    private final static String TAG = ScrollingActivity.class.getSimpleName() + "/DEV";
    private static final int TAB_STATUS = 0;
    private static final int TAB_LED = 1;
    private static final int TAB_TRACKING = 2;
    private static final int REQUEST_ENABLE_BT = 2001;
    private static final int AZIMUTH_PIVOT = 20;
    private static final int PITCH_PIVOT = 5;
    private static final int ROLL_PIVOT = 20;
    private static final String BT_UUID = "94f39d29-7d6d-437d-973b-fba39e49d4ee";
    private static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_WRITE = 2;
    public static final int MESSAGE_READ = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    private static final int ORIENTATION_LEFT = 944;
    private static final int ORIENTATION_RIGHT = 344;
    private static final int ORIENTATION_NONE = 892;
    private static final int EMERGENCY = 121;

    private static final int SHAKE_THRESHOLD = 5000;

    private static final String SENT = "SMS_SENT_ACTION";
    private static final String DELIVERED = "SMS_DELIVERED_ACTION";

    private TabLayout m_tabLayout;
    private TabPagerAdapter m_pagerAdapter;
    private ViewPager m_viewPager;

    private InfoFragment m_infoFrag;
    private LEDFragment m_ledFrag;

    private boolean m_IsSupportedBT = false;
    private BluetoothAdapter m_bluetoothAdapter;
    private BluetoothLeService m_bluetoothLeService;
    private BluetoothGattCharacteristic m_characteristicTX;
    private BluetoothGattCharacteristic m_characteristicRX;
    private BluetoothDevice m_pairedDevice;

    private BluetoothSocket m_bluetoothSocket;
    private InputStream m_bluetoothInput;
    private OutputStream m_bluetoothOutput;

    public String getCurLED() {
        return m_curLED;
    }

    private String m_curLED;
    private byte[] m_curSignalStr;
    private int m_curInterrupt;

    //    SMS
    private long m_shockStateLastTime;
    private float m_beforeAccelX;
    private float m_beforeAccelY;
    private float m_beforeAccelZ;
    private String m_strAddressOutput;
    private String m_strLatitude;
    private String m_strLogitude;
    private Location m_curLocation;
    private AddressResultReceiver m_resultAddressReceiver;

    private boolean m_bInitialize = false;

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

    public void setMapPosition(double latitude, double longitude, Location curLocation) {
        m_strLatitude = String.format("%f", latitude);
        m_strLogitude = String.format("%f", longitude);
        m_curLocation = curLocation;

        startAddressIntentService();
    }

    private void startAddressIntentService() {
        // Create an intent for passing to the intent service responsible for fetching the address.
        Intent intent = new Intent(this, FetchAddressIntentService.class);

        // Pass the result receiver as an extra to the service.
        intent.putExtra(Constants.RECEIVER, m_resultAddressReceiver);

        // Pass the location data as an extra to the service.
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, m_curLocation);

        // Start the service. If the service isn't already running, it is instantiated and started
        // (creating a process for it if needed); if it is running then it remains running. The
        // service kills itself automatically once all intents are processed.
        try {
            startService(intent);
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }

    }

    private class AddressResultReceiver extends ResultReceiver {
        AddressResultReceiver(Handler handler) {
            super(handler);
        }

        /**
         *  Receives data sent from FetchAddressIntentService and updates the UI in MainActivity.
         */
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            // Display the address string or an error message sent from the intent service.
            m_strAddressOutput = resultData.getString(Constants.RESULT_DATA_KEY);
            m_strAddressOutput = m_strAddressOutput.replaceAll("대한민국 ", "");
        }
    }

    public ViewPager getViewPager() {
        return m_viewPager;
    }

    public boolean getIsRecorded() {
        return m_IsRecorded;
    }

    public int getcurInterruptState() {
        return m_curInterrupt;
    }

    public void setcurInterrupt(int m_curInterrupt) {
        this.m_curInterrupt = m_curInterrupt;
    }

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

                if (tab.getPosition() == TAB_STATUS) {

                    if (m_IsRecorded) {
                        ((FloatingActionButton) m_infoFrag.getView().findViewById(R.id.recordToggleBtn)).setImageDrawable(getResources().getDrawable(R.drawable.ic_stop_solid, getApplicationContext().getTheme()));
                    } else {
                        ((FloatingActionButton) m_infoFrag.getView().findViewById(R.id.recordToggleBtn)).setImageDrawable(getResources().getDrawable(R.drawable.ic_circle_solid, getApplicationContext().getTheme()));
                    }
                } else if (tab.getPosition() == TAB_TRACKING) {


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

        if (!dirExists) {
            storage.createDirectory(newDir);
        }

        /* Internal File Storage setup end */

        /* Set Bluetooth start */
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        /* Set Bluetooth end */

        /* Sensor start */
        GyroManagerUtil.m_sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        GyroManagerUtil.m_sensorAccel = GyroManagerUtil.m_sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        GyroManagerUtil.m_sensorMag = GyroManagerUtil.m_sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        /* Sensor end */

        m_resultAddressReceiver = new AddressResultReceiver(new Handler());

        m_bInitialize = true;
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

            if( m_infoFrag == null ) {
                m_infoFrag = (InfoFragment) getSupportFragmentManager().findFragmentByTag(
                        "android:switcher:" + m_viewPager.getId() + ":" + ((TabPagerAdapter)m_viewPager.getAdapter())
                                .getItemId(TAB_STATUS));
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
            Log.d(TAG, "connectDevice: " + devices.get(i).getName());
            Log.d(TAG, "connectDevice: " + getString(R.string.device_bluetooth_name));
            String deviceSSID = new String(getString(R.string.device_bluetooth_name));

            if (devices.get(i).getName().equals(deviceSSID)) {
                Log.d(TAG, "connectDevice: equal");
            } else {
                Log.d(TAG, "connectDevice: not equal");
            }

            if ( devices.get(i).getName().equals(deviceSSID) ) {
                m_pairedDevice = devices.get(i);
                m_bluetoothSocket = m_pairedDevice.createRfcommSocketToServiceRecord(UUID.fromString(BT_UUID));
                m_bluetoothInput = m_bluetoothSocket.getInputStream();
                m_bluetoothOutput = m_bluetoothSocket.getOutputStream();

                m_bluetoothSocket.connect();
//                m_btThread = new ConnectedThread(m_bluetoothSocket);
//                m_btThread.run();
                return;
            }
        }

        // 페어링 기기가 없다면 새로 찾아서 연결
        if (m_bluetoothAdapter.startDiscovery()) {
            BroadcastReceiver mDiscoveryReceiver = new BroadcastReceiver() {

                @Override

                public void onReceive(Context context, Intent intent) {

                    if ( BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {

                        BluetoothDevice searchedDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        if (searchedDevice.getName() == null) {
                            return;
                        }

                        Log.d(TAG, "searchedDevice : " + searchedDevice.getName() + "\n" + searchedDevice.getAddress());

                        String deviceSSID = new String(getString(R.string.device_bluetooth_name));

                        if (searchedDevice.getName().equals(deviceSSID)
                                ) {
                            m_pairedDevice = searchedDevice;
                            try {
                                m_bluetoothSocket = m_pairedDevice.createRfcommSocketToServiceRecord(UUID.fromString(BT_UUID));
                                m_bluetoothInput = m_bluetoothSocket.getInputStream();
                                m_bluetoothOutput = m_bluetoothSocket.getOutputStream();

                                m_bluetoothSocket.connect();

                                Toast.makeText(getApplicationContext(), "connected device HELPER!", Toast.LENGTH_SHORT).show();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

            };

            registerReceiver(mDiscoveryReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        }
    }

    public void ledImageListener(View v) {

        String str = "helper";

        String resName = v.getResources().getResourceName(v.getId());
        String imgName = resName.split(String.format("%s", '/'))[1];

        if( m_infoFrag == null ) {
            m_infoFrag = (InfoFragment) getSupportFragmentManager().findFragmentByTag(
                    "android:switcher:" + m_viewPager.getId() + ":" + ((TabPagerAdapter)m_viewPager.getAdapter())
                            .getItemId(TAB_STATUS));
        }

        if( m_ledFrag == null ) {
            m_ledFrag = (LEDFragment) getSupportFragmentManager().findFragmentByTag(
                    "android:switcher:" + m_viewPager.getId() + ":" + ((TabPagerAdapter)m_viewPager.getAdapter())
                            .getItemId(TAB_LED));
        }



        switch (imgName) {
            case "img0":
                str = "0-00-0";
                m_infoFrag.setCurLEDView(R.drawable.bird);
                m_ledFrag.setCurLEDView(R.drawable.bird);
                break;

            case "img1":
                str = "0-01-0";
                m_infoFrag.setCurLEDView(R.drawable.characters);
                m_ledFrag.setCurLEDView(R.drawable.characters);
                break;

            case "img2":
                str = "0-02-0";
                m_infoFrag.setCurLEDView(R.drawable.windy);
                m_ledFrag.setCurLEDView(R.drawable.windy);
                break;

            case "img3":
                str = "0-03-0";
                m_infoFrag.setCurLEDView(R.drawable.snow);
                m_ledFrag.setCurLEDView(R.drawable.snow);
                break;

            case "img4":
                str = "0-04-0";
                m_infoFrag.setCurLEDView(R.drawable.rain);
                m_ledFrag.setCurLEDView(R.drawable.rain);
                break;

            case "img5":
                str = "0-05-0";
                m_infoFrag.setCurLEDView(R.drawable.cute);
                m_ledFrag.setCurLEDView(R.drawable.cute);
                break;

            case "img6":
                str = "0-06-1";
                m_infoFrag.setCurLEDView(R.drawable.moving_arrow_left_blink);
                m_ledFrag.setCurLEDView(R.drawable.moving_arrow_left_blink);
                break;

            case "img7":
                str = "0-07-1";
                m_infoFrag.setCurLEDView(R.drawable.moving_arrow_right_blink);
                m_ledFrag.setCurLEDView(R.drawable.moving_arrow_right_blink);
                break;

            case "img8":
                str = "0-08-1";
                m_infoFrag.setCurLEDView(R.drawable.emergency_blink);
                m_ledFrag.setCurLEDView(R.drawable.emergency_blink);
                break;

            case "img9":
                str = "0-09-0";
                m_infoFrag.setCurLEDView(R.drawable.mario);
                m_ledFrag.setCurLEDView(R.drawable.mario);
                break;

            case "img10":
                str = "0-10-0";
                m_infoFrag.setCurLEDView(R.drawable.boy);
                m_ledFrag.setCurLEDView(R.drawable.boy);
                break;
        }

        m_curLED = str;



        final byte[] tx = str.getBytes();

        sendToBluetoothDevice(str.getBytes());

//        m_characteristicTX.setValue(tx);
//
//        m_bluetoothLeService.writeCharacteristic(m_characteristicTX);
//        m_bluetoothLeService.readCharacteristic(m_characteristicRX);
//
//        Log.d("DEV", "sendSignal called! TX : " + new String(m_characteristicTX.getValue()));
//        Log.d("DEV", "sendSignal called! RX : " + new String(m_characteristicRX.getValue()));
    }

    public void toggleRecord(View v) {
        if (m_viewPager.getCurrentItem() != TAB_STATUS) {
            return;
        }

        m_IsRecorded = !m_IsRecorded;

        if (m_IsRecorded) {
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

            if( m_infoFrag == null ) {
                m_infoFrag = (InfoFragment) getSupportFragmentManager().findFragmentByTag(
                        "android:switcher:" + m_viewPager.getId() + ":" + ((TabPagerAdapter)m_viewPager.getAdapter())
                                .getItemId(TAB_STATUS));
            }

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
            } catch (IllegalStateException e) {
                e.printStackTrace();
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

                if (m_viewPager.getCurrentItem() == TAB_STATUS) {
                    if( m_infoFrag == null ) {
                        m_infoFrag = (InfoFragment) getSupportFragmentManager().findFragmentByTag(
                                "android:switcher:" + m_viewPager.getId() + ":" + ((TabPagerAdapter)m_viewPager.getAdapter())
                                        .getItemId(TAB_STATUS));
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
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        GyroManagerUtil.m_sensorManager.unregisterListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        String writeStr = "-1";
//        sendToBluetoothDevice(writeStr.getBytes());
        try {
            m_bluetoothSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void changeLeftOrRightLEDOfRoll(float roll) {
        String writeStr = "";

        if( m_curInterrupt == EMERGENCY) {
            return;
        }

        if ( roll >= ROLL_PIVOT ) {
            Log.d(TAG, "onSensorChanged: right");
            writeStr = "0-07-1";
            sendToBluetoothDevice(writeStr.getBytes());

            m_curInterrupt = ORIENTATION_RIGHT;
        }

        else if ( roll <=  -ROLL_PIVOT ) {
            Log.d(TAG, "onSensorChanged: left");
            writeStr = "0-06-1";
            sendToBluetoothDevice(writeStr.getBytes());

            m_curInterrupt = ORIENTATION_LEFT;
        }

        if( Math.abs(GyroManagerUtil.getPivotRoll()) >= 20 &&
                Math.abs(roll) < 20 ) {
            writeStr = m_curLED;

            if ( writeStr == null) {
                return;
            }


            sendToBluetoothDevice(writeStr.getBytes());

            m_curInterrupt = ORIENTATION_NONE;
        }
    }

    private void sendSMS(String num, String txt) {

        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent(SENT), 0);
        PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0, new Intent(DELIVERED), 0);

        SmsManager sms = SmsManager.getDefault();
        if(sms == null) {
            Log.e(TAG, "sms == null");
            return;
        }
        sms.sendTextMessage(num, "ME", txt, sentPI, deliveredPI);
        //sms.sendTextMessage(num, null, txt, null, null);
    }

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
                    String strSMS1 = getString(R.string.sms_content) + "\n\n" +  m_strAddressOutput;
                    String strSMS2 = "https://google.com/maps?q=" + m_strLatitude + "," + m_strLogitude;

                    sendSMS("+8201034823161", strSMS1);
                    sendSMS("+8201034823161", strSMS2);
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
    public void onSensorChanged(SensorEvent sensorEvent) {

        if (!m_bInitialize) { return; }

        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            m_fAccel[0] = sensorEvent.values[0];
            m_fAccel[1] = sensorEvent.values[1];
            m_fAccel[2] = sensorEvent.values[2];

            shockStateDetector(m_fAccel[0], m_fAccel[1], m_fAccel[2]);
        }
        else if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            m_fMag[0] = sensorEvent.values[0];
            m_fMag[1] = sensorEvent.values[1];
            m_fMag[2] = sensorEvent.values[2];
        }

        float[] resultValues = GyroManagerUtil.getOrientation(m_fAccel, m_fMag);

        changeLeftOrRightLEDOfRoll(resultValues[2]);

        GyroManagerUtil.setPivotRoll(resultValues[2]);

        if( m_infoFrag == null) {
            m_infoFrag = (InfoFragment) getSupportFragmentManager().findFragmentByTag(
                    "android:switcher:" + m_viewPager.getId() + ":" + ((TabPagerAdapter)m_viewPager.getAdapter())
                            .getItemId(TAB_STATUS));

//            m_infoFrag = (InfoFragment)m_viewPager
//                    .getAdapter()
//                    .instantiateItem(m_viewPager, TAB_STATUS);

        } else {
//            m_infoFrag.setTextTiltXYZ(resultValues);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public void sendToBluetoothDevice(byte[] bytes) {
        if(Arrays.equals(m_curSignalStr, bytes) || m_bluetoothOutput == null) {
            return;
        }

        try{
            m_bluetoothOutput.write(bytes);
            m_curSignalStr = bytes.clone();
        } catch (NullPointerException e ) {
            e.printStackTrace();
        }
        catch (IOException e) {
            updateConnectionLayout(false);
            e.printStackTrace();
        }
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

