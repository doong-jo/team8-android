package com.helper.helper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.icu.util.Output;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ScrollingActivity extends AppCompatActivity {
    private final static String TAG = ScrollingActivity.class.getSimpleName() + "/DEV";

    private TabLayout tabLayout;
    private ViewPager viewPager;

    private boolean IsSupportedBTDevice = false;
    private boolean IsPairingDevice = false;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeService mBluetoothLeService;
    private BluetoothSocket mConnectSocket;
    private BluetoothGatt mBluetoothGatt;
    private String mBluetoothDeviceAddress;
    private BluetoothGattCharacteristic characteristicTX;
    private BluetoothGattCharacteristic characteristicRX;
    private String mDeviceName;
    private String mDeviceAddress;

    private boolean mIsConnected = false;

    private int mConnectionState = STATE_DISCONNECTED;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED =
            "com.helper.helper.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.helper.helper.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.helper.helper.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.helper.helper.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.helper.helper.EXTRA_DATA";
    public final static UUID UUID_HM_RX_TX =
            UUID.fromString(BluetoothAttributes.HM_RX_TX);

    private static final int TAB_STATUS = 0;
    private static final int TAB_LED = 1;
    private static final int TAB_TRACKING = 2;
    private static final int REQUEST_ENABLE_BT = 2001;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);

        /* ToolBar UI start */
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        /* ToolBar UI end */

        /* Tab start */
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.addTab(tabLayout.newTab().setText("Status"));
        tabLayout.addTab(tabLayout.newTab().setText("LED"));
        tabLayout.addTab(tabLayout.newTab().setText("Tracking"));

        viewPager = (ViewPager) findViewById(R.id.pager);

        TabPagerAdapter pagerAdapter = new TabPagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(pagerAdapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Log.d("DEV", "onTabSelected called! posistion : " + tab.getPosition());
                viewPager.setCurrentItem(tab.getPosition());
                //                view
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        /* Tab end /*

        /* Valid Bluetooth supports start */
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Device does not support Bluetooth", Toast.LENGTH_LONG).show();
            IsSupportedBTDevice = false;
        } else {
            Toast.makeText(this, "Device support Bluetooth", Toast.LENGTH_LONG).show();
            IsSupportedBTDevice = true;
        }
        /* Valid Bluetooth supports end */



        /* Set GATT Interface start */
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        /* Set GATT Interface end */
    }

    /* Tab Move Start */
    public void moveToLEDDash(View v) {
        viewPager.setCurrentItem(TAB_LED);
    }

    public void moveToTrackingDash(View v) {
        viewPager.setCurrentItem(TAB_TRACKING);
    }
    /* Tab Move End */

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            Log.d(TAG, "mBluetoothLeService to initialize Bluetooth");
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }

            /* Connect Bluetooth Device Start
               (Automatically connects to the device upon successful start-up initialization.) */
            if (!mBluetoothAdapter.isEnabled()) {
                Toast.makeText(getApplicationContext(), "disable bluetooth", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), "Already enable bluetooth", Toast.LENGTH_LONG).show();
                connectDevice();
            }
            /* Connect Bluetooth Device End */
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    public void enableBluetooth(View v) {
        if (!IsSupportedBTDevice) {
            return;
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            //call onActivityResult
        } else {
            Toast.makeText(this, "Already enable bluetooth", Toast.LENGTH_LONG).show();
            connectDevice();
        }
    }

    public void updateConnectionLayout(boolean IsConnected) {
        LinearLayout connectLayout = (LinearLayout) findViewById(R.id.connect_layout);
        TextView connectDiscription = (TextView) findViewById(R.id.connect_desc_text);
        TextView connectToggle = (TextView) findViewById(R.id.connect_toggle_text);

        if( IsConnected ) {
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
        List<BluetoothDevice> devices = new ArrayList<BluetoothDevice>(mBluetoothAdapter.getBondedDevices());

        String[] deviceLabels = new String[devices.size()];
        for (int i = 0; i < deviceLabels.length; ++i) {
            if( devices.get(i).getName() == getString(R.string.device_bluetooth_name) &&
                    mBluetoothLeService.connect(devices.get(i).getAddress()) ) {
                Toast.makeText(this, "connected paired device HELPER!", Toast.LENGTH_LONG).show();
                updateConnectionLayout(true);
                break;
            }
        }

        // 페어링 기기가 없다면 새로 찾아서 연결
        if (mBluetoothAdapter.startDiscovery()) {
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
                                mBluetoothLeService.connect(searchedDevice.getAddress())) {
                            Toast.makeText(getApplicationContext(), "connected device HELPER!", Toast.LENGTH_LONG).show();
                            updateConnectionLayout(true);
                        }
                    }
                }

            };

            registerReceiver(mDiscoveryReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
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
                    Toast.makeText(this, "You can't use interaction helper device", Toast.LENGTH_LONG).show();
                }
                break;
        }
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

        List<BluetoothGattService> gattServices = mBluetoothLeService.getSupportedGattServices();

        for (BluetoothGattService gattService : gattServices) {
            // get characteristic when UUID matches RX/TX UUID
            characteristicTX = gattService.getCharacteristic(BluetoothLeService.UUID_HM_RX_TX);
            characteristicRX = gattService.getCharacteristic(BluetoothLeService.UUID_HM_RX_TX);
        }

        characteristicTX.setValue(tx);
        Log.d("DEV", "sendSignal called! tx : " + tx);
        mBluetoothLeService.writeCharacteristic(characteristicTX);
    }

    //  ToolBar Reference Start
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_scrolling, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    // ToolBar Reference End
}