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
import android.widget.Toast;

import com.helper.helper.R;
import com.helper.helper.interfaces.BluetoothReadCallback;
import com.helper.helper.interfaces.ValidateCallback;
import com.helper.helper.view.ScrollingActivity;

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

    private static boolean m_IsProcessing = false;
    private static boolean m_IsReadProcessing = false;
    private static boolean m_bIsPairing = false;

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
        return m_bIsPairing;
    }

    public static void setConnectionResultCb(ValidateCallback callback) {
        m_connectionResultCb = callback;
    }

    public static String getLastSignalStr() { return m_lastSignalStr; }

    public static void setReadResultCb(BluetoothReadCallback callback) { m_readResultCb = callback; }

    public static void initBluetooth(final Activity activity) {
        if( activity != null) {
            m_activity = activity;
        }

        m_IsProcessing = true;

        /** create bluetooth (GATT) service **/
//        Intent bluetoothServiceIntent = new Intent(m_activity, BluetoothLeService.class);
//        m_activity.bindService(bluetoothServiceIntent, m_serviceConnection, Context.BIND_AUTO_CREATE);
//        m_activity.startService()


        /** create bluetooth read thread **/
        m_bluetoothReadthread = new BluetoothReadThread();

        /** create adapter and return enablable device **/
        m_bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (m_bluetoothAdapter == null) {
            Toast.makeText(activity, "블루투스를 지원하지 않는 기기입니다.", Toast.LENGTH_SHORT).show();
            try {
                m_connectionResultCb.onDone(FAIL_BLUETOOTH_CONNECT);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return;
        }

        if (!m_bluetoothAdapter.isEnabled()) {
            Toast.makeText(m_activity, "블루투스가 비활성화 되어있습니다.", Toast.LENGTH_SHORT).show();
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
//        else if( signalMsg.split("info").length != 0 ) {
//            String[] splitStr = signalMsg.split("/");
//
//            int ledInd = Integer.parseInt(splitStr[1]);
//            float spdVal = Float.parseFloat(splitStr[2]);
//            float brtVal = Float.parseFloat(splitStr[3]);
//        }
    }

    /** Find Bluetooth Device **/
    private static void prepareDevice()  {
//        if (m_pairedDevice != null) {
//            m_bIsPairing = true;
//            try {
//                m_connectionResultCb.onDone(SUCCESS_BLUETOOTH_CONNECT);
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
////            Toast.makeText(m_activity, "이미 디바이스와 연결되어 있습니다.", Toast.LENGTH_SHORT).show();
//            return;
//        }

//        m_loadingDlg = ProgressDialog.show(m_activity, m_activity.getString(R.string.bluetooth_loading_title), m_activity.getString(R.string.bluetooth_loading_message), true);

        /** 만약 페어링 기기들 리스트에 있다면 바로 연결 **/
        List<BluetoothDevice> devices = new ArrayList<>(m_bluetoothAdapter.getBondedDevices());

        String[] deviceLabels = new String[devices.size()];
        for (int i = 0; i < deviceLabels.length; ++i) {
            if (devices.get(i).getName().contains(DEVICE_ALIAS)) {
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

            m_bluetoothSocket.connect();

            m_bluetoothInput = m_bluetoothSocket.getInputStream();
            m_bluetoothOutput = m_bluetoothSocket.getOutputStream();

            m_bluetoothReadthread.start();
//            Thread bluetoothReadThread = new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    while (true) {
//                        BTManager.readFromBluetoothDevice(new BluetoothReadCallback() {
//                            @Override
//                            public void onResult(String result) {
//                                bluetoothSignalHandler(result);
//                            }
//                        });
//                    }
//                }
//            });

            try {
                m_connectionResultCb.onDone(SUCCESS_BLUETOOTH_CONNECT);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            m_bIsPairing = true;
//            Toast.makeText(m_activity, "디바이스 연결에 성공했습니다.", Toast.LENGTH_SHORT).show();


        } catch (IOException e) {
            e.printStackTrace();
            try {
                m_connectionResultCb.onDone(FAIL_BLUETOOTH_CONNECT);
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
            m_bIsPairing = false;
//            Toast.makeText(m_activity, "디바이스 연결에 실패했습니다.", Toast.LENGTH_SHORT).show();
        }
//        m_loadingDlg.dismiss();
    }

    /** Find Bluetooth **/
    private static BroadcastReceiver makeBroadcastReceiver() {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {

                    BluetoothDevice searchedDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//                    if (searchedDevice.getName() == null) {
//                        Toast.makeText(m_activity, "디비이스를 찾지 못했습니다.", Toast.LENGTH_SHORT).show();
//                        ScrollingActivity scrollingActivity = (ScrollingActivity) m_activity;
//                        scrollingActivity.updateConnectionLayout(false);
//                        m_loadingDlg.dismiss();
//                        return;
//                    }

                    String searchDeviceName = searchedDevice.getName();
                    if ( searchDeviceName == null ) { return; }
                    Log.d(TAG, "searchedDeviceName onReceive: " + searchDeviceName);
                    if (searchedDevice.getName().contains(DEVICE_ALIAS)) {
                        connectDevice(searchedDevice);
                    }

                } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(intent.getAction()) ) {
                    Toast.makeText(m_activity, "디비이스를 찾지 못했습니다.", Toast.LENGTH_SHORT).show();
                    m_bIsPairing = false;
//                    ScrollingActivity scrollingActivity = (ScrollingActivity) m_activity;
//                    m_loadingDlg.dismiss();
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
//        if (m_bluetoothInput == null || !m_bluetoothSocket.isConnected()) {
//            return;
//        }

        try {
            m_bluetoothOutput.write(bytes);
        } catch (IOException e) {
            m_bIsPairing = false;
            e.printStackTrace();
            Toast.makeText(m_activity, "블루투스 신호 전송에 실패했습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    public static void readFromBluetoothDevice(BluetoothReadCallback callback) {
        if (m_bluetoothReadthread != null && !m_bluetoothSocket.isConnected()) {
            m_bluetoothReadthread.interrupt();
            m_bluetoothReadthread = null;
            try {
                m_connectionResultCb.onDone(FAIL_BLUETOOTH_CONNECT);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            m_bIsPairing = false;
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

    public static void closeBluetoothSocket() {
        try {
            if( m_bluetoothSocket != null && m_bluetoothSocket.isConnected() ) {
                m_bluetoothSocket.close();
                m_bluetoothReadthread.interrupt();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
