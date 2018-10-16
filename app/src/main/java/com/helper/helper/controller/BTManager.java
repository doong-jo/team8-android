/*
 * Copyright (c) 10/15/18 6:15 PM
 * Written by Sungdong Jo
 * Description:
 */

package com.helper.helper.controller;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.helper.helper.R;
import com.helper.helper.controller.ble.BluetoothLeService;
import com.helper.helper.interfaces.BluetoothReadCallback;
import com.helper.helper.view.Info.InfoFragment;
import com.helper.helper.view.ScrollingActivity;
import com.helper.helper.view.TabPagerAdapter;

import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class BTManager {
    private final static String TAG = BTManager.class.getSimpleName() + "/DEV";

    /******************* Definition constants *******************/
    public static final String BLUETOOTH_UUID = "94f39d29-7d6d-437d-973b-fba39e49d4ee";
    public static final String DEVICE_ALIAS = "helper-1";

    private static final int REQUEST_ENABLE_BT = 2001;
    /************************************************************/

    private static class BluetoothReadThread extends Thread {

        public BluetoothReadThread() {
            // 초기화 작업
        }

        public void run() {
            while (true) {
                BTManager.readFromBluetoothDevice(new BluetoothReadCallback() {
                    @Override
                    public void onResult(String result) {
                        (ScrollingActivity)
                    }
                });
            }

        }
    }

    private static boolean m_IsProcessing = false;
    private static boolean m_IsReadProcessing = false;
    private static Activity m_activity;
    private static BluetoothAdapter m_bluetoothAdapter;
    private static BluetoothLeService m_bluetoothLeService;
    private static BluetoothDevice m_pairedDevice;
    private static BluetoothSocket m_bluetoothSocket;
    private static InputStream m_bluetoothInput;
    private static OutputStream m_bluetoothOutput;
    private static BroadcastReceiver m_discoveryReceiver = makeBroadcastReceiver();
    private static BluetoothReadThread m_bluetoothReadthread;

    public static void initBluetooth(final Activity activity) {
        m_activity = activity;
        m_IsProcessing = true;

        /** create bluetooth service **/
        Intent bluetoothServiceIntent = new Intent(m_activity, BluetoothLeService.class);
        m_activity.bindService(bluetoothServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);

        /** create adapter and return enablable device **/
        m_bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (m_bluetoothAdapter == null) {
            Toast.makeText(activity, "블루투스를 지원하지 않는 기기입니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        /** create bluetooth read thread (not running)**/
        m_bluetoothReadthread = new BluetoothReadThread();

    }

    private final static ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            if (!m_bluetoothAdapter.isEnabled()) {
                Toast.makeText(m_activity, "블루투스가 비활성화 되어있습니다.", Toast.LENGTH_SHORT).show();
                requestEnableBluetooth();
            } else {
                prepareDevice();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            m_bluetoothLeService = null;
        }
    };

    /** Find Bluetooth Device **/
    private static void prepareDevice()  {
        if (m_pairedDevice != null) {
            Toast.makeText(m_activity, "이미 디바이스와 연결되어 있습니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        /** 만약 페어링 기기들 리스트에 있다면 바로 연결 **/
        List<BluetoothDevice> devices = new ArrayList<>(m_bluetoothAdapter.getBondedDevices());

        String[] deviceLabels = new String[devices.size()];
        for (int i = 0; i < deviceLabels.length; ++i) {
            if (devices.get(i).getName().equals(DEVICE_ALIAS)) {
                connectDevice(devices.get(i));
                // updateConnectionLayout(true);
                return;
            }
        }

        /** 페어링 기기 리스트에 없다면 새로 찾아서 연결 **/
        if (m_bluetoothAdapter.startDiscovery()) {
            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

            m_activity.registerReceiver(m_discoveryReceiver, filter);
        }
    }

    private static void connectDevice(BluetoothDevice device) {
        m_pairedDevice = device;

        try {
            m_bluetoothSocket = m_pairedDevice.createRfcommSocketToServiceRecord(UUID.fromString(BTManager.BLUETOOTH_UUID));
            m_bluetoothInput = m_bluetoothSocket.getInputStream();
            m_bluetoothOutput = m_bluetoothSocket.getOutputStream();

            m_bluetoothSocket.connect();
            m_bluetoothReadthread.start();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(m_activity, "디바이스 연결에 실패했습니다.", Toast.LENGTH_SHORT).show();
        }

    }

    /** Find Bluetooth **/
    private static BroadcastReceiver makeBroadcastReceiver() throws IOException {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {

                    BluetoothDevice searchedDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (searchedDevice.getName() == null) {
                        return;
                    }

                    if (searchedDevice.getName().equals(DEVICE_ALIAS)) {
                        connectDevice(searchedDevice);
                    }

                } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(intent.getAction())) {
                    Toast.makeText(m_activity, "디비이스를 찾지 못했습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        };
    }

    public static void requestEnableBluetooth() {
        if (!m_bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            m_activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    public void sendToBluetoothDevice(byte[] bytes) {
        if (m_bluetoothInput == null || !m_bluetoothSocket.isConnected()) {
            return;
        }

        try {
            m_bluetoothOutput.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(m_activity, "블루투스 신호 전송에 실패했습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    public static void readFromBluetoothDevice(BluetoothReadCallback callback) {
        if (m_bluetoothInput == null || !m_bluetoothSocket.isConnected()) {
            m_bluetoothReadthread.interrupt();
            return;
        }

        byte[] buffer = new byte[256];
        int bytes;

        try {
            bytes = m_bluetoothInput.read(buffer);
            String readMessage = new String(buffer, 0, bytes);
            callback.onResult(readMessage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
