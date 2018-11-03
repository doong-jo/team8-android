/*
 * Copyright (c) 10/15/18 6:15 PM
 * Written by Sungdong Jo
 * Description:
 */

package com.helper.helper.controller;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.helper.helper.interfaces.BluetoothReadCallback;
import com.helper.helper.interfaces.ValidateCallback;

import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BTManager {
    private final static String TAG = BTManager.class.getSimpleName() + "/DEV";

    /******************* Definition constants *******************/
    /*
    // UUID that specifies a protocol for generic bluetooth serial communication
    private static final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
     */

    /** Bluetooth Signal Rules **/
    public static final String BLUETOOTH_SIGNAL_SEPERATE = "-";

    public static final String BLUETOOTH_SIGNAL_LED = "0";
    public static final String BLUETOOTH_SIGNAL_SPEED = "1";
    public static final String BLUETOOTH_SIGNAL_BRIGHTNESS = "2";

    private static final String BLUETOOTH_UUID = "94f39d29-7d6d-437d-973b-fba39e49d4ee";
    private static final String DEVICE_ALIAS = "EIGHT_";

    public static final int SUCCESS_BLUETOOTH_CONNECT = 1001;
    public static final int FAIL_BLUETOOTH_CONNECT = 1002;
    public static final int REQUEST_ENABLE_BT = 2001;

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
                        bluetoothSignalHandler(result);
                    }
                });
            }

        }
    }

    private static Activity m_activity;
    private static BluetoothAdapter m_bluetoothAdapter;
    private static BluetoothDevice m_pairedDevice;
    private static BluetoothSocket m_bluetoothSocket;
    private static InputStream m_bluetoothInput;
    private static OutputStream m_bluetoothOutput;
    private static BroadcastReceiver m_discoveryReceiver = makeBroadcastReceiver();
    private static BluetoothReadThread m_bluetoothReadthread;
    private static ValidateCallback m_connectionResultCb;
    private static BluetoothReadCallback m_readResultCb;
    private static String m_lastSignalStr = "";
    private static ProgressDialog m_loadingDlg;

    public static boolean getConnected() {
        if( m_bluetoothSocket == null ) {
            return false;
        }
        return m_bluetoothSocket.isConnected();
    }

    public static void setConnectionResultCb(ValidateCallback callback) {
        m_connectionResultCb = callback;
    }

    public static String getLastSignalStr() { return m_lastSignalStr; }

    public static void setReadResultCb(BluetoothReadCallback callback) { m_readResultCb = callback; }

    public static void initBluetooth(final Activity activity) {
        if( getConnected() ) {
            Log.d(TAG, "initBluetooth: Already connect");
            return;
        }

        if( activity != null) {
            m_activity = activity;
        }

        /** create bluetooth (GATT) service **/
//        Intent bluetoothServiceIntent = new Intent(m_activity, BluetoothLeService.class);
//        m_activity.bindService(bluetoothServiceIntent, m_serviceConnection, Context.BIND_AUTO_CREATE);
//        m_activity.startService()


        /** create adapter and return enablable device **/
        m_bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (m_bluetoothAdapter == null) {
            try {
                m_connectionResultCb.onDone(FAIL_BLUETOOTH_CONNECT);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return;
        }

        if (!m_bluetoothAdapter.isEnabled()) {
            try {
                m_connectionResultCb.onDone(FAIL_BLUETOOTH_CONNECT);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            requestEnableBluetooth();
        } else {
            prepareDevice();
        }
    }

    private static void bluetoothSignalHandler(String signalMsg) {
        m_lastSignalStr = signalMsg;
        if ( signalMsg.equals("EMERGENCY") ) {
            try {
                EmergencyManager.startEmergencyProcess(m_activity);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            if( m_readResultCb != null ) {
                m_readResultCb.onResult(signalMsg);
            }
        }
    }

    /** Find Bluetooth Device **/
    private static void prepareDevice()  {

        /** 만약 페어링 기기들 리스트에 있다면 바로 연결 **/
        List<BluetoothDevice> devices = new ArrayList<>(m_bluetoothAdapter.getBondedDevices());

        String[] deviceLabels = new String[devices.size()];
        for (int i = 0; i < deviceLabels.length; ++i) {
            if (devices.get(i).getName().contains(DEVICE_ALIAS)) {
                connectDevice(devices.get(i));
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

            m_bluetoothSocket.connect();

            m_bluetoothInput = m_bluetoothSocket.getInputStream();
            m_bluetoothOutput = m_bluetoothSocket.getOutputStream();

            /** create bluetooth read thread **/
            m_bluetoothReadthread = new BluetoothReadThread();
            m_bluetoothReadthread.start();

            try {
                m_connectionResultCb.onDone(SUCCESS_BLUETOOTH_CONNECT);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
            try {
                m_connectionResultCb.onDone(FAIL_BLUETOOTH_CONNECT);
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        }
    }

    /** Find Bluetooth **/
    private static BroadcastReceiver makeBroadcastReceiver() {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {

                    BluetoothDevice searchedDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                    String searchDeviceName = searchedDevice.getName();
                    if ( searchDeviceName == null ) { return; }
                    Log.d(TAG, "searchedDeviceName onReceive: " + searchDeviceName);
                    if (searchedDevice.getName().contains(DEVICE_ALIAS)) {
                        connectDevice(searchedDevice);
                    }

                } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(intent.getAction()) ) {
                    try {
                        m_connectionResultCb.onDone(FAIL_BLUETOOTH_CONNECT);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
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

    public static void writeToBluetoothDevice(byte[] bytes) {
        if( !getConnected() ) {
            stopReadThread();
            try {
                m_connectionResultCb.onDone(FAIL_BLUETOOTH_CONNECT);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return;
        }

        try {
            m_bluetoothOutput.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
            stopReadThread();
//            Toast.makeText(m_activity, "블루투스 신호 전송에 실패했습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    public static void readFromBluetoothDevice(BluetoothReadCallback callback) {
        if (!getConnected()) {
            stopReadThread();
            try {
                m_connectionResultCb.onDone(FAIL_BLUETOOTH_CONNECT);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return;
        }

        byte[] buffer = new byte[256];
        int bytes;

        try {
            bytes = m_bluetoothInput.read(buffer);
            String readMessage = new String(buffer, 0, bytes);
            callback.onResult(readMessage);
        } catch (IOException e) {
            stopReadThread();
            e.printStackTrace();
        }
    }

    public static void closeBluetoothSocket() {
        try {
            if( m_bluetoothSocket != null && m_bluetoothSocket.isConnected() ) {
                m_bluetoothSocket.close();
                stopReadThread();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void stopReadThread() {
        if( m_bluetoothReadthread != null ) {
            m_bluetoothReadthread.interrupt();
            m_bluetoothReadthread = null;
        }
    }
}
