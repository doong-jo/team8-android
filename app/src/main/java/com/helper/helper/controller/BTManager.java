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
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

import com.google.android.gms.common.internal.service.Common;
import com.helper.helper.R;
import com.helper.helper.interfaces.BluetoothReadCallback;
import com.helper.helper.interfaces.DistanceCallback;
import com.helper.helper.interfaces.EmergencyCallback;
import com.helper.helper.interfaces.ValidateCallback;
import com.snatik.storage.Storage;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
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
    public static final String BLUETOOTH_SIGNAL_SEPARATE = "!S!";

    private static final String BT_SIGNAL_ASK_LED = "AL";
    private static final String BT_SIGNAL_RESPONSE_LED = "RL";
    private static final String BT_SIGNAL_RES_DOWNLOAD_LED = "RDL";
    private static final String BT_SIGNAL_RES_EXIST_LED = "REL";
    private static final String BT_SIGNAL_DOWNLOAD_LED = "DL";
    private static final String BT_SIGNAL_DOWNLOAD_DONE_LED = "DDL";

    public static final String BT_SIGNAL_BRIGHTNESS = "B";
    public static final String BT_SIGNAL_SPEED = "S";

    private static final String BT_SIGNAL_FILTER = "F";
    private static final String BT_SIGNAL_RES_FILTER = "RES";
    private static final String BT_SIGNAL_CONNECTED = "CONNECTED";

    public static final String BT_SIGNAL_EMERGENCY = "E";

    private static final int BT_READ_CLOCK_SLEEP = 100;

    private static final String BLUETOOTH_UUID = "94f39d29-7d6d-437d-973b-fba39e49d4ee";
    /** Android RFCOMM = 990byte(PAYLOAD) + 34byte(LC2CAP) **/
    private static final int BLUETOOTH_RFCOMM_PAYLOAD = 900;
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
                readFromBluetoothDevice(new BluetoothReadCallback() {
                    @Override
                    public void onResult(String result) {
                        bluetoothSignalHandler(result);
                    }

                    @Override
                    public void onError(String result) {
                        return;
                    }
                });

                SystemClock.sleep(BT_READ_CLOCK_SLEEP);
            }
        }
    }

    private static Activity m_activity;
    private static String m_connectedDeviceName;
    private static BluetoothAdapter m_bluetoothAdapter;
    private static BluetoothDevice m_pairedDevice;
    private static BluetoothSocket m_bluetoothSocket;
    private static InputStream m_bluetoothInput;
    private static OutputStream m_bluetoothOutput;
    private static BroadcastReceiver m_discoveryReceiver = makeBroadcastReceiver();
    private static BluetoothReadThread m_bluetoothReadthread;
    private static ValidateCallback m_connectionResultCb;
    private static BluetoothReadCallback m_infoReadCb;
    private static BluetoothReadCallback m_activityReadCb;
    private static BluetoothReadCallback m_downloadLEDResultCb;

    /** Send bytearray of Bitmap (LED) **/
    private static byte[] m_outBitmapByteArr;
    private static int m_cntSendByteArr;
    private static int m_copyCursor;

    public static boolean getConnected() {
        if( m_bluetoothSocket == null ) {
            return false;
        }
        return m_bluetoothSocket.isConnected();//Attrubute Error
    }

    public static void setConnectionResultCb(ValidateCallback callback) {
        m_connectionResultCb = callback;
    }

    public static void setInfoReadCb(BluetoothReadCallback callback) {
        m_infoReadCb = callback;

        /** create bluetooth read thread **/
        m_bluetoothReadthread = new BluetoothReadThread();
        m_bluetoothReadthread.start();
    }

    public static void setActivityReadCb(BluetoothReadCallback callback) {
        m_activityReadCb = callback;
    }

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
       if( signalMsg.startsWith(BT_SIGNAL_RESPONSE_LED + BLUETOOTH_SIGNAL_SEPARATE) ||
               signalMsg.startsWith(BT_SIGNAL_DOWNLOAD_LED + BLUETOOTH_SIGNAL_SEPARATE) ){
            m_downloadLEDResultCb.onResult(signalMsg);
       } else if ( signalMsg.contains("info") ){
            m_infoReadCb.onResult(signalMsg);
       } else if ( signalMsg.startsWith(BT_SIGNAL_FILTER + BLUETOOTH_SIGNAL_SEPARATE) ) {
            final double rollover = Double.valueOf(signalMsg.split(BLUETOOTH_SIGNAL_SEPARATE)[1]);
            final double accel = Double.valueOf(signalMsg.split(BLUETOOTH_SIGNAL_SEPARATE)[2]);

            // TODO: 03/12/2018 For "device test log" code
            EmergencyManager.insertAccidentTestDatainServer(m_activity, m_connectedDeviceName, rollover, accel, new Date());

            final double sensorFuzzy = EmergencyManager.getCalcSensorFuzzyLogicResult(rollover, accel);
            Log.d(TAG, "bluetoothSignalHandler: " + sensorFuzzy);

            if( sensorFuzzy >= EmergencyManager.FUZZY_LOGIC_WARNING ) {
                EmergencyManager.setAccLocation(GoogleMapManager.getCurLocation());
                m_activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        /** handler **/
                        EmergencyManager.getDistanceToAccident(new DistanceCallback() {
                            @Override
                            public void onDone(float dis) {
                                if( EmergencyManager.getCalcGPSFuzzyLogicResult(dis) ) {
                                    EmergencyManager.setAccidentSensorData(accel, rollover);
                                    m_activityReadCb.onResult(BT_SIGNAL_EMERGENCY);
                                }
                            }
                        });
                    }
                });
            }
        } else if ( signalMsg.startsWith(BT_SIGNAL_EMERGENCY + BLUETOOTH_SIGNAL_SEPARATE) ) {
           final double acc = Double.valueOf(signalMsg.split(BLUETOOTH_SIGNAL_SEPARATE)[1]);
           final double similarity = Double.valueOf(signalMsg.split(BLUETOOTH_SIGNAL_SEPARATE)[2]);
           final double rollover = Double.valueOf(signalMsg.split(BLUETOOTH_SIGNAL_SEPARATE)[3]);

           EmergencyManager.setAccLocation(GoogleMapManager.getCurLocation());
           m_activity.runOnUiThread(new Runnable() {
               @Override
               public void run() {
                   /** handler **/
                   EmergencyManager.getDistanceToAccident(new DistanceCallback() {
                       @Override
                       public void onDone(float dis) {
                           if( EmergencyManager.getCalcGPSFuzzyLogicResult(dis) ) {
                               EmergencyManager.setAccidentSensorData(rollover, acc);
                               m_activityReadCb.onResult(BT_SIGNAL_EMERGENCY);
                           }
                       }
                   });
               }
           });

       }
        writeToBluetoothDevice(BT_SIGNAL_RES_FILTER.getBytes());
    }

    /** Find Bluetooth Device **/
    private static void prepareDevice()  {

        /** Find exist bluetooth device and connect **/
        List<BluetoothDevice> devices = new ArrayList<>(m_bluetoothAdapter.getBondedDevices());

        String[] deviceLabels = new String[devices.size()];
        for (int i = 0; i < deviceLabels.length; ++i) {
            if (devices.get(i).getName().contains(DEVICE_ALIAS)) {
                connectDevice(devices.get(i));

                if( getConnected() ) {
                    return;
                }
                /** Remove bond device **/
//                if( !getConnected() ) {
//                    Method m = null;
//                    try {
//                        m = devices.get(i).getClass()
//                                .getMethod("removeBond", (Class[]) null);
//                    } catch (NoSuchMethodException e) {
//                        e.printStackTrace();
//                    }
//                    try {
//                        m.invoke(devices.get(i), (Object[]) null);
//                    } catch (IllegalAccessException | InvocationTargetException e) {
//                        e.printStackTrace();
//                    }
//                } else {
//                    return;
//                }
            }
        }

        /** Find device non connection **/
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

            m_connectedDeviceName = device.getName();

            writeToBluetoothDevice(BT_SIGNAL_CONNECTED.getBytes());
            try {
                m_connectionResultCb.onDone(SUCCESS_BLUETOOTH_CONNECT);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                m_activity.unregisterReceiver(m_discoveryReceiver);
            } catch (IllegalArgumentException e) {
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

                } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(intent.getAction()) && !getConnected()) {
                    try {
                        m_connectionResultCb.onDone(FAIL_BLUETOOTH_CONNECT);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
    }

    private static void requestEnableBluetooth() {
        if (!m_bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            m_activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    public static boolean writeToBluetoothDevice(byte[] bytes) {
        if( m_bluetoothOutput == null ) {
            return false;
        }

        try {
            m_bluetoothOutput.write(bytes);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            stopReadThread();
            return false;
        }
    }

    private static void readFromBluetoothDevice(BluetoothReadCallback callback) {
        if ( !getConnected() ) {
            stopReadThread();
            return;
        }

        byte[] buffer = new byte[256];
        int bytes;

        try {
            bytes = m_bluetoothInput.read(buffer);
            String readMessage = new String(buffer, 0, bytes);
            callback.onResult(readMessage);
        } catch (IOException e) {
            // TODO: 28/11/2018 Bluetooth connection is disconnected state
            try {
                m_connectionResultCb.onDone(FAIL_BLUETOOTH_CONNECT);
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
            closeBluetoothSocket();
            stopReadThread();
        }
    }

    private static void stopReadThread() {
        if( m_bluetoothReadthread != null ) {
            try {
                m_bluetoothReadthread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            m_bluetoothReadthread = null;
            m_bluetoothInput = null;
            m_bluetoothOutput = null;
        }
    }

    private static void setBitmapByteArray(Context context, String ledIndex) throws IOException {
        String path = CommonManager.getOpenLEDFilePath(context, ledIndex, context.getString(R.string.gif_format));

        File file = new File(path);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            m_outBitmapByteArr = Files.readAllBytes(file.toPath());
        } else {
            FileInputStream fis = new FileInputStream(file);

            m_outBitmapByteArr = new byte[(int) file.length()];
            DataInputStream dis = new DataInputStream(fis);
            dis.readFully(m_outBitmapByteArr);
        }
    }

    private static void sendBitmapByteArray(int cntByteArray) {
        byte[] signalByteArray = (BT_SIGNAL_DOWNLOAD_LED+BLUETOOTH_SIGNAL_SEPARATE).getBytes();

        /** Divide 990byte(PAYLOAD) **/
        byte[] resultByteArray;

        int divideSize = m_outBitmapByteArr.length / BLUETOOTH_RFCOMM_PAYLOAD;

        if( m_outBitmapByteArr.length > BLUETOOTH_RFCOMM_PAYLOAD ) {
            if( divideSize % m_outBitmapByteArr.length != 0 ) { divideSize++; }

            byte[] subBitmapByteArray;
            int copySize = 0;

            /** Done Sending **/
            if( cntByteArray == divideSize ) {
                writeToBluetoothDevice(
                        BT_SIGNAL_DOWNLOAD_DONE_LED.getBytes()
                );
                doneOutputBitmap();
                return;
            }

            /** Send Last ByteArray **/
            else if( cntByteArray+1 == divideSize ) {
                copySize = m_outBitmapByteArr.length % BLUETOOTH_RFCOMM_PAYLOAD;
            } else {
                copySize = BLUETOOTH_RFCOMM_PAYLOAD;
            }

            resultByteArray = new byte[signalByteArray.length + copySize];
            subBitmapByteArray = new byte[copySize];

            System.arraycopy(m_outBitmapByteArr, m_copyCursor, subBitmapByteArray, 0, copySize);

            System.arraycopy(signalByteArray, 0, resultByteArray, 0, signalByteArray.length);
            System.arraycopy(subBitmapByteArray, 0, resultByteArray, signalByteArray.length, subBitmapByteArray.length);

            writeToBluetoothDevice(resultByteArray);

            m_copyCursor += BLUETOOTH_RFCOMM_PAYLOAD;

        } else {
            /** Done Sending **/
            if( cntByteArray == 1 ) {
                writeToBluetoothDevice(
                        BT_SIGNAL_DOWNLOAD_DONE_LED.getBytes()
                );
                doneOutputBitmap();
            } else {
                resultByteArray = new byte[signalByteArray.length + m_outBitmapByteArr.length];
                System.arraycopy(signalByteArray, 0, resultByteArray, 0, signalByteArray.length);
                System.arraycopy(m_outBitmapByteArr, 0, resultByteArray, signalByteArray.length, m_outBitmapByteArr.length);
                writeToBluetoothDevice(resultByteArray);
            }
        }
    }

    private static void doneOutputBitmap(){
        m_cntSendByteArr = 0;
        m_copyCursor = 0;
        m_downloadLEDResultCb = null;
        m_outBitmapByteArr = null;
    }

    public static void setShowOnDevice(final Context context, final String ledIndex) {

        m_downloadLEDResultCb = new BluetoothReadCallback() {
            @Override
            public void onResult(String result) {
                String[] splitData = result.split(BLUETOOTH_SIGNAL_SEPARATE);

                String valueData;

                switch(splitData[0]) {
                    case BT_SIGNAL_RESPONSE_LED:
                        valueData = splitData[1];

                        if( valueData.equals(BT_SIGNAL_RES_EXIST_LED) ) {
                            break;
                        } else if ( valueData.equals(BT_SIGNAL_RES_DOWNLOAD_LED) ) {
                            try {
                                setBitmapByteArray(context, ledIndex);
                                sendBitmapByteArray(m_cntSendByteArr++);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        break;

                    case BT_SIGNAL_DOWNLOAD_LED:
                        sendBitmapByteArray(m_cntSendByteArr++);
                        break;

                    default:
                        break;
                }
            }

            @Override
            public void onError(String result) {

            }
        };

        if( writeToBluetoothDevice(BT_SIGNAL_ASK_LED
                .concat(BLUETOOTH_SIGNAL_SEPARATE)
                .concat(ledIndex)
                .getBytes()) ) {
            UserManager.setUserLEDcurShowOn(context, ledIndex);
        }
    }

    public static void closeBluetoothSocket() {
        try {
            if( m_bluetoothSocket != null && m_bluetoothSocket.isConnected() ) {
                m_bluetoothSocket.close();

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        stopReadThread();
    }
}
