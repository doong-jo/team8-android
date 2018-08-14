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

import com.google.android.gms.maps.model.LatLng;
import com.helper.helper.Info.InfoFragment;
import com.helper.helper.ble.BluetoothLeService;
import com.helper.helper.tracking.TrackingData;
import com.helper.helper.util.PermissionUtil;
import com.snatik.storage.Storage;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class ScrollingActivity extends AppCompatActivity {
    private final static String TAG = ScrollingActivity.class.getSimpleName() + "/DEV";
    private static final int TAB_STATUS = 0;
    private static final int TAB_LED = 1;
    private static final int TAB_TRACKING = 2;

    private static final int REQUEST_ENABLE_BT = 2001;

    private TabLayout m_tabLayout;
    private ViewPager m_viewPager;
    private InfoFragment m_infoFrag;

    private boolean m_IsSupportedBT = false;
    private BluetoothAdapter m_bluetoothAdapter;
    private BluetoothLeService m_bluetoothLeService;
    private BluetoothGattCharacteristic m_characteristicTX;
    private BluetoothGattCharacteristic m_characteristicRX;

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

        TabPagerAdapter pagerAdapter = new TabPagerAdapter(getSupportFragmentManager(), m_tabLayout.getTabCount());
        m_viewPager.setAdapter(pagerAdapter);
        m_viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(m_tabLayout));

        m_tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Log.d("DEV", "onTabSelected called! posistion : " + tab.getPosition());
                m_viewPager.setCurrentItem(tab.getPosition());

                if( tab.getPosition() == TAB_STATUS ) {
                    m_infoFrag = (InfoFragment)m_viewPager
                            .getAdapter()
                            .instantiateItem(m_viewPager, m_viewPager.getCurrentItem());

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
            Toast.makeText(this, "Device does not support Bluetooth", Toast.LENGTH_SHORT).show();
            m_IsSupportedBT = false;
        } else {
            Toast.makeText(this, "Device support Bluetooth", Toast.LENGTH_SHORT).show();
            m_IsSupportedBT = true;
        }
        /* Valid Bluetooth supports end */

        /* Internal File Storage setup start */

        // init
        Storage storage = new Storage(getApplicationContext());

        // get external storage
        String path = storage.getInternalFilesDirectory();

        // new dir
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
            Toast.makeText(this, "Already enable bluetooth", Toast.LENGTH_SHORT).show();
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
                writeTrackingDataInternalStorage();
            } catch (IOException e) {
                e.printStackTrace();
            }

            m_infoFrag = (InfoFragment)m_viewPager
                    .getAdapter()
                    .instantiateItem(m_viewPager, m_viewPager.getCurrentItem());

            m_infoFrag.recordStopAndEraseLocationList();
        }
    }

    private void writeTrackingDataInternalStorage() throws IOException {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            Storage internalStorage = new Storage(getApplicationContext());

            String path = internalStorage.getInternalFilesDirectory();
            String dir = path + File.separator + R.string.internal_directory;
            String xmlFilePath =  dir + File.separator + R.string.file_name_tracking_data;

            final String fDistance = String.format("%.2f", m_infoFrag.getCurTrackingDistance());
            String elemTracking = String.format("%s", R.string.tracking_xml_elem_root);
            String elemMap = String.format("%s", R.string.tracking_xml_elem_map);
            String elemLocation = String.format("%s", R.string.tracking_xml_elem_location);
            String elemLatitude = String.format("%s", R.string.tracking_xml_elem_latitude);
            String elemLongitude = String.format("%s", R.string.tracking_xml_elem_longitude);
            String attrDate = String.format("%s", R.string.tracking_xml_elem_attr_date);
            String attrStartTime = String.format("%s", R.string.tracking_xml_elem_attr_start_time);
            String attrEndTime = String.format("%s", R.string.tracking_xml_elem_attr_end_time);
            String attrDistance = String.format("%s", R.string.tracking_xml_elem_attr_distance);

            boolean fileExists = internalStorage.isFileExist(xmlFilePath);

            Document doc = docBuilder.newDocument();

            Element rootElement;
            if( fileExists ) {
                doc = docBuilder.parse(new File(xmlFilePath));
                rootElement = (Element) doc.getDocumentElement();
            } else {
                rootElement = doc.createElement(elemTracking);
            }

            /* Make elements start */
            Element mapElement = doc.createElement(elemMap);
            /* Make elements end */

            /* Define attributes start */
            mapElement.setAttribute(attrDate, m_recordStartDate);
            mapElement.setAttribute(attrStartTime, m_recordStartTime);
            mapElement.setAttribute(attrEndTime, m_recordEndTime);
            mapElement.setAttribute(attrDistance, fDistance);
            /* Define attributes end */

            if( m_infoFrag.getCurrRecordedLocationList().size() <= 0 ) {
                return;
            }

            for(LatLng currentLat : m_infoFrag.getCurrRecordedLocationList()) {
                Element locationElement = doc.createElement(elemLocation);

                    Element latitudeElement = doc.createElement(elemLatitude);
                    latitudeElement.appendChild(doc.createTextNode(String.format("%f", currentLat.latitude)));

                    Element longitudeElement = doc.createElement(elemLongitude);
                    longitudeElement.appendChild(doc.createTextNode(String.format("%f", currentLat.longitude)));

                locationElement.appendChild(latitudeElement);
                locationElement.appendChild(longitudeElement);

                mapElement.appendChild(locationElement);
            }

            rootElement.appendChild(mapElement);

            if( !fileExists ) {
                doc.appendChild(rootElement);
            }

            // XML 파일로 쓰기
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();

            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(doc);

            final boolean dirExists = internalStorage.isDirectoryExists(dir);

            if( !dirExists ) {
                internalStorage.createDirectory(dir);
            }

            StreamResult result = new StreamResult(new FileOutputStream(new File(xmlFilePath), false));

            transformer.transform(source, result);
        }
        catch (ParserConfigurationException | TransformerException | SAXException pce)
        {
            pce.printStackTrace();
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

                m_infoFrag = (InfoFragment)m_viewPager
                        .getAdapter()
                        .instantiateItem(m_viewPager, m_viewPager.getCurrentItem());
                m_infoFrag.requsetLocation();

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
}