package com.helper.helper;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;



public class ScrollingActivity extends AppCompatActivity implements SensorEventListener{
    private final static String TAG = ScrollingActivity.class.getSimpleName() + "/DEV";
    private static final int TAB_STATUS = 0;
    private static final int TAB_LED = 1;
    private static final int TAB_TRACKING = 2;

    private static final int REQUEST_ENABLE_BT = 2001;

    private TabLayout m_tabLayout;
    private TabPagerAdapter m_pagerAdapter;
    private ViewPager m_viewPager;
    private InfoFragment m_infoFrag;

    private boolean m_IsSupportedBT = false;
    private BluetoothAdapter m_bluetoothAdapter;
    private BluetoothLeService m_bluetoothLeService;
    private BluetoothGattCharacteristic m_characteristicTX;
    private BluetoothGattCharacteristic m_characteristicRX;

    private SensorManager m_sensorManager;
    private Sensor m_sensorAccel;
    private Sensor m_sensorMag;
    private float[] m_fMag;
    private float[] m_fAccel;

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

        /* Set GATT Interface start */
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        /* Set GATT Interface end */

        /* Sensor start */
        m_sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        m_sensorAccel = m_sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        m_sensorMag = m_sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
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
                connectDevice();
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
            connectDevice();
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

    public void connectDevice() {
        // 만약 페어링 기기들 리스트에 있다면 바로 연결
        List<BluetoothDevice> devices = new ArrayList<BluetoothDevice>(m_bluetoothAdapter.getBondedDevices());

        String[] deviceLabels = new String[devices.size()];
        for (int i = 0; i < deviceLabels.length; ++i) {
            if (devices.get(i).getName() == getString(R.string.device_bluetooth_name) &&
                    m_bluetoothLeService.connect(devices.get(i).getAddress())) {
                Toast.makeText(this, "connected paired device HELPER!", Toast.LENGTH_SHORT).show();
                InitializeSignal();
                break;
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

                        // HELPER
                        if (searchedDevice.getName().toString().equals(getString(R.string.device_bluetooth_name)) &&
                                m_bluetoothLeService.connect(searchedDevice.getAddress())) {
                            Toast.makeText(getApplicationContext(), "connected device HELPER!", Toast.LENGTH_SHORT).show();
                            InitializeSignal();
                        }
                    }
                }

            };

            registerReceiver(mDiscoveryReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        }
    }

    public void InitializeSignal() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                List<BluetoothGattService> gattServices = m_bluetoothLeService.getSupportedGattServices();

                for (BluetoothGattService gattService : gattServices) {
                    // get characteristic when UUID matches RX/TX UUID
                    m_characteristicTX = gattService.getCharacteristic(BluetoothLeService.UUID_HM_RX_TX);
                    m_characteristicRX = gattService.getCharacteristic(BluetoothLeService.UUID_HM_RX_TX);
                }

                m_bluetoothLeService.setCharacteristicNotification(m_characteristicRX, true);

                updateConnectionLayout(true);

                Toast.makeText(getApplicationContext(), "InitializeSignal called!", Toast.LENGTH_SHORT).show();
            }
        }, 3000);


    }

    public void sendSignal(View v) {
        String str;
        switch (v.getResources().getResourceName(v.getId())) {
            case "com.helper.helper:id/img1":
                str = "1";
                break;

            case "com.helper.helper:id/img2":
                str = "2";
                break;

            case "com.helper.helper:id/img3":
                str = "3";
                break;

            case "com.helper.helper:id/img4":
                str = "4";
                break;

            case "com.helper.helper:id/battery":
                str = "5";
                break;

            default:
                str = "1";
        }

        final byte[] tx = str.getBytes();



        m_characteristicTX.setValue(tx);

        m_bluetoothLeService.writeCharacteristic(m_characteristicTX);
        m_bluetoothLeService.readCharacteristic(m_characteristicRX);

        Log.d("DEV", "sendSignal called! TX : " + new String(m_characteristicTX.getValue()));
        Log.d("DEV", "sendSignal called! RX : " + new String(m_characteristicRX.getValue()));
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
                    connectDevice();
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
        m_sensorManager.registerListener(this, m_sensorAccel, SensorManager.SENSOR_DELAY_NORMAL);
        m_sensorManager.registerListener(this, m_sensorMag, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        m_sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        final long curMillis = System.currentTimeMillis();
        if ( System.currentTimeMillis() - GyroManagerUtil.getTimerStartTime() > 3000 ) {
            GyroManagerUtil.setTimerStartTime(curMillis);
        }

        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            m_fAccel = sensorEvent.values.clone();
        }
        else if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            m_fMag = sensorEvent.values.clone();
        }

        float[] resultValues = GyroManagerUtil.getOrientation(m_fAccel, m_fMag);

        //5

        m_infoFrag = (InfoFragment) getSupportFragmentManager().findFragmentByTag(
                "android:switcher:" + m_viewPager.getId() + ":" + ((TabPagerAdapter)m_viewPager.getAdapter())
                        .getItemId(TAB_STATUS));

        if(m_infoFrag != null) { m_infoFrag.setTextTiltXYZ(resultValues); }
    }

    /**

     *  Get orientation

     *

     * @param gravity

     * @param geomagnetic

     * @return orientation

     *   values[0] : azimuth (axis : Z)

     *   values[1] : pitch (axis : X)

     *   values[2] : roll (axis : Y)

     */

    /** */


    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}