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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.helper.helper.interfaces.BluetoothReadCallback;
import com.helper.helper.interfaces.ValidateCallback;
import com.snatik.storage.Storage;

import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
    public static final String BLUETOOTH_SIGNAL_SEPARATE = "!S!";

    public static final String BT_SIGNAL_ASK_LED = "AL";
    public static final String BT_SIGNAL_RESPONSE_LED = "RL";
    public static final String BT_SIGNAL_RES_DOWNLOAD_LED = "RDL";
    public static final String BT_SIGNAL_RES_EXIST_LED = "REL";
    public static final String BT_SIGNAL_DOWNLOAD_LED = "DL";
    public static final String BT_SIGNAL_DOWNLOAD_DONE_LED = "DDL";

    public static final String BT_SIGNAL_BRIGHTNESS = "B";
    public static final String BT_SIGNAL_SPEED = "S";

    public static final String BLUETOOTH_UUID = "94f39d29-7d6d-437d-973b-fba39e49d4ee";
    /** Android RFCOMM = 990byte(PAYLOAD) + 34byte(LC2CAP) **/
    public static final int BLUETOOTH_RFCOMM_PAYLOAD = 990;
    public static final String DEVICE_ALIAS = "EIGHT_";

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
    private static BluetoothReadCallback m_downloadLEDResultCb;
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
        } else if( signalMsg.split(BLUETOOTH_SIGNAL_SEPARATE)[0].equals(BT_SIGNAL_RESPONSE_LED) ||
                signalMsg.split(BLUETOOTH_SIGNAL_SEPARATE)[0].equals(BT_SIGNAL_DOWNLOAD_LED) ){
            if( m_downloadLEDResultCb != null ) {
                m_downloadLEDResultCb.onResult(signalMsg);
            }
        } else {
            if (m_readResultCb != null) {
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

            try {
                m_connectionResultCb.onDone(SUCCESS_BLUETOOTH_CONNECT);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            /** create bluetooth read thread **/
            m_bluetoothReadthread = new BluetoothReadThread();
            m_bluetoothReadthread.start();

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
        if( m_bluetoothOutput == null ) {
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
        if (m_bluetoothInput == null) {
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
            m_bluetoothInput = null;
            m_bluetoothOutput = null;

        }
    }

    private static String getOpenFilePath(Context context, String ledIndex) {
        Storage internalStorage = new Storage(context);
        String path = internalStorage.getInternalFilesDirectory();
        String dir = path + File.separator + DownloadImageTask.DOWNLOAD_PATH;
        String openFilePath = dir + File.separator + ledIndex + ".png";

        return openFilePath;
    }

    private static void sendBitmapByteArray(Context context, String ledIndex) {
        Bitmap imageBitmap = null;

        /** 1. Read Image File **/
        try {
            File f=new File(getOpenFilePath(context, ledIndex));
            imageBitmap = BitmapFactory.decodeStream(new FileInputStream(f));
//            cardViewLED.setCardImageView(cardImageBitmap);

            /** Send File Data(byte) to Device **/
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);


            byte[] signalByteArray = (BT_SIGNAL_DOWNLOAD_LED+BLUETOOTH_SIGNAL_SEPARATE).getBytes();
            byte[] bitmapByteArray = stream.toByteArray();


            /** Divide 990byte(PAYLOAD) **/
            byte[] resultByteArray;

            if( bitmapByteArray.length > BLUETOOTH_RFCOMM_PAYLOAD ) {
                int divideSize = bitmapByteArray.length / BLUETOOTH_RFCOMM_PAYLOAD;

                if( divideSize % bitmapByteArray.length != 0 ) { divideSize++; }

                for (int i = 0; i < divideSize; i++) {
                    byte[] subBitmapByteArray;

                    int copySize;
                    if( i+1 == divideSize ) {
                        copySize = bitmapByteArray.length % BLUETOOTH_RFCOMM_PAYLOAD;
                        resultByteArray = new byte[signalByteArray.length + copySize];
                    } else {
                        copySize = BLUETOOTH_RFCOMM_PAYLOAD;
                        resultByteArray = new byte[signalByteArray.length + copySize];
                    }

                    subBitmapByteArray = new byte[copySize];
                    System.arraycopy(bitmapByteArray, copySize * i, subBitmapByteArray, 0, copySize);

                    System.arraycopy(signalByteArray, 0, resultByteArray, 0, signalByteArray.length);
                    System.arraycopy(subBitmapByteArray, 0, resultByteArray, signalByteArray.length, subBitmapByteArray.length);

                    writeToBluetoothDevice(resultByteArray);
                }
            } else {
                resultByteArray = new byte[signalByteArray.length + bitmapByteArray.length];
                System.arraycopy(signalByteArray, 0, resultByteArray, 0, signalByteArray.length);
                System.arraycopy(bitmapByteArray, 0, resultByteArray, signalByteArray.length, bitmapByteArray.length);

                writeToBluetoothDevice(resultByteArray);
            }

            // TODO: 13/11/2018 After write(DOWNLOAD_LED) signal receive
            writeToBluetoothDevice(
                    BT_SIGNAL_DOWNLOAD_DONE_LED
                    .getBytes()
            );
            m_downloadLEDResultCb = null;

            imageBitmap.recycle();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
    }

    public static void setShowOnDevice(final Context context, final String ledIndex) {
        m_downloadLEDResultCb = new BluetoothReadCallback() {
            @Override
            public void onResult(String result) {
                String[] splitData = result.split(BLUETOOTH_SIGNAL_SEPARATE);

                String valueData = "";

                switch(splitData[0]) {
                    case BT_SIGNAL_RESPONSE_LED:
                        valueData = splitData[1];

                        if( valueData.equals(BT_SIGNAL_RES_EXIST_LED) ) {
                            break;
                        } else if ( valueData.equals(BT_SIGNAL_RES_DOWNLOAD_LED) ) {
                            sendBitmapByteArray(context, ledIndex);
                        }
                        break;

                    default:
                        break;
                }
            }
        };

        /** 0. Ask Device about exists **/
        writeToBluetoothDevice(BT_SIGNAL_ASK_LED
                .concat(BLUETOOTH_SIGNAL_SEPARATE)
                .concat(ledIndex)
                .getBytes());
    }
}
