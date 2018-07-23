package com.helper.helper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
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
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ScrollingActivity extends AppCompatActivity {

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

    private static final UUID UUID_SPP = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

//    public final static UUID HM_RX_TX = UUID.fromString(BluetoothAttributes.HM_RX_TX);

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(intentAction);
                Log.i("DEV", "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                Log.i("DEV", "Attempting to start service discovery:" +
                        mBluetoothGatt.discoverServices());

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                Log.i("DEV", "Disconnected from GATT server.");
                broadcastUpdate(intentAction);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            } else {
                Log.w("DEV", "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }
    };

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            Log.e("DEV", "mBluetoothLeService to initialize Bluetooth");
            if (!mBluetoothLeService.initialize()) {
                Log.e("DEV", "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
//            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };


    public boolean getIsPairing() {
        return IsPairingDevice;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.addTab(tabLayout.newTab().setText("Status"));
        tabLayout.addTab(tabLayout.newTab().setText("LED"));
        tabLayout.addTab(tabLayout.newTab().setText("Tracking"));

        viewPager = (ViewPager) findViewById(R.id.pager);

        TabPagerAdapter pagerAdapter = new TabPagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(pagerAdapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        // Set TabSelectedListener
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

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "기기가 블루투스를 지원하지 않습니다.", Toast.LENGTH_LONG).show();
            // Device does not support Bluetooth
            IsSupportedBTDevice = false;
        }else {
            IsSupportedBTDevice = true;
        }

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

//        Fragment fragment = new InfoFragment();
//        FragmentManager fragmentManager = getFragmentManager();
//        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//        fragmentTransaction.add( R.id.fragment_place, fragment );
//        fragmentTransaction.commit();
    }

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action,final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);

        // For all other profiles, writes the data formatted in HEX.
        final byte[] data = characteristic.getValue();
        Log.i("DEV", "data"+characteristic.getValue());

        if (data != null && data.length > 0) {
            final StringBuilder stringBuilder = new StringBuilder(data.length);
            for(byte byteChar : data)
                stringBuilder.append(String.format("%02X ", byteChar));
            Log.d("DEV", String.format("%s", new String(data)));
            // getting cut off when longer, need to push on new line, 0A
            intent.putExtra(EXTRA_DATA,String.format("%s", new String(data)));

        }
        sendBroadcast(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case REQUEST_ENABLE_BT:
                if( resultCode == Activity.RESULT_OK ) {
                    Log.d("DEV", "Enable Bluetooth");
//                    AlertDialog.Builder alert = new AlertDialog.Builder(this);
//                    alert.setTitle("Bluetooth List");


                    //만약 페어링 기기들 리스트에 있다면 바로 연결

                    // BondDevices : 이전에 페어링 되었었던 기기들 리스트
//                    Log.d("DEV", mBluetoothAdapter.getBondedDevices().toString());
//                    Log.d("DEV", mBluetoothAdapter.getDe)

                    List<BluetoothDevice> devices = new ArrayList<BluetoothDevice>(mBluetoothAdapter.getBondedDevices());
                    String[] deviceLabels = new String[devices.size()];
                    for (int i = 0; i < deviceLabels.length; ++i) {
                        deviceLabels[i] = devices.get(i).getName() + "\n" + devices.get(i).getAddress();
                        Log.d("DEV", "deviceLabel : " + deviceLabels[i].toString());
                    }

                    //페어링 기기가 없다면 새로 연결

                    if( mBluetoothAdapter.startDiscovery() ) {
                        BroadcastReceiver mDiscoveryReceiver = new BroadcastReceiver() {

                            @Override

                            public void onReceive(Context context, Intent intent) {

                                if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {

                                    BluetoothDevice searchedDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                                    Log.d("DEV", "searchedDevice : " + searchedDevice.getName() + "\n" + searchedDevice.getAddress());


                                    if( searchedDevice.getName().toString().equals("HELPER") && connect(searchedDevice.getAddress()) ) {
                                        BluetoothGattCharacteristic mSCharacteristic;
                                        String str = "R";
                                        final byte[] tx = str.getBytes();

                                        mSCharacteristic = new BluetoothGattCharacteristic(UUID_HM_RX_TX,
                                                BluetoothGattCharacteristic.PROPERTY_READ
                                                        | BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                                                BluetoothGattCharacteristic.PERMISSION_READ);

                                        mSCharacteristic.setValue(tx);
                                        mBluetoothLeService.writeCharacteristic(mSCharacteristic);
                                    }
//                                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(searchedDevice.getAddress());
//                                    device.connectGatt(this, false, mGattCallback);

//                                    if( searchedDevice.getName().toString().equals("HELPER") && requestConnect(searchedDevice, UUID_SPP) ) {
//                                        Log.d("DEV", "connected : " + searchedDevice.getName() + "\n" + searchedDevice.getAddress());
//                                    }
                                }

                            }

                        };

                        registerReceiver(mDiscoveryReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
                    }


//                    alert.show();
                } else {
                    Log.d("DEV", "Disable Bluetooth");
                }
                break;
        }
    }

    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w("DEV", "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.d("DEV", "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                return true;
            } else {
                final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
                mBluetoothDeviceAddress = address;
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w("DEV", "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        Log.d("DEV", "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        return true;
    }

//    void connectToSelectedDevice(String selectedDeviceName) {
//        // BluetoothDevice 원격 블루투스 기기를 나타냄.
//        m_RemoteDeivce = getDeviceFromBondedList(selectedDeviceName);
//        // java.util.UUID.fromString : 자바에서 중복되지 않는 Unique 키 생성.
//        UUID uuid = java.util.UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
//
//        try {
//            // 소켓 생성, RFCOMM 채널을 통한 연결.
//            // createRfcommSocketToServiceRecord(uuid) : 이 함수를 사용하여 원격 블루투스 장치와 통신할 수 있는 소켓을 생성함.
//            // 이 메소드가 성공하면 스마트폰과 페어링 된 디바이스간 통신 채널에 대응하는 BluetoothSocket 오브젝트를 리턴함.
//            m_Socket = m_RemoteDeivce.createRfcommSocketToServiceRecord(uuid);
//            m_Socket.connect(); // 소켓이 생성 되면 connect() 함수를 호출함으로써 두기기의 연결은 완료된다.
//
//            // 데이터 송수신을 위한 스트림 얻기.
//            // BluetoothSocket 오브젝트는 두개의 Stream을 제공한다.
//            // 1. 데이터를 보내기 위한 OutputStrem
//            // 2. 데이터를 받기 위한 InputStream
////            m_InputStream = m_Socket.getInputStream();
//
//            // 데이터 수신 준비.
//            btConnection_receive();
//
//        }catch(Exception e) { // 블루투스 연결 중 오류 발생
//            Toast.makeText(this, "블루투스 연결 중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
//            this.finish();  // App 종료
//        }
//    }

    public BluetoothSocket getBluetoothSocket(BluetoothDevice device, UUID uuid) throws IOException {

        return device.createInsecureRfcommSocketToServiceRecord(uuid);

    }

    public boolean requestConnect(BluetoothDevice device, UUID uuid) {

        try {

            mConnectSocket = getBluetoothSocket(device, uuid);

            if (mConnectSocket != null) {

                new Thread(new Runnable() {

                    @Override

                    public void run() {

                        try {

                            Log.d("DEV", "Try connect Socket");
                            mConnectSocket.connect();

                            mIsConnected = true;

                        } catch (IOException e) {

                            Log.d("DEV", "Fail connect Socket");
                            e.printStackTrace();

                            mIsConnected = false;

                        }
                    }

                }).start();

            }

        } catch (IOException e) {

            e.printStackTrace();

            mIsConnected = false;

        }

        return mIsConnected;

    }

    public void enableBluetooth(View v) {
        if( !IsSupportedBTDevice ) { return; }


        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    public void moveToLEDDash(View v) {
        Log.d("DEV", "moveToLEDDash is called!!");
        viewPager.setCurrentItem(TAB_LED);
    }

    public void moveToTrackingDash(View v) {
        Log.d("DEV", "moveToTrackingDash is called!!");
        viewPager.setCurrentItem(TAB_TRACKING);
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

    //  ToolBar Reference End
}
