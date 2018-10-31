package com.helper.helper.view.main.myeight;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import com.google.android.gms.maps.model.LatLng;
import com.helper.helper.R;
import com.helper.helper.controller.BTManager;
import com.helper.helper.interfaces.BluetoothReadCallback;
import com.helper.helper.interfaces.ValidateCallback;

import java.text.DecimalFormat;

public class InfoFragment extends Fragment {
    private final static String TAG = InfoFragment.class.getSimpleName() + "/DEV";

    private BluetoothReadCallback m_bluetoothReadCallback;

    /******************* Define widgtes in view *******************/
    private LinearLayout m_myledsLayout;
    private SeekBar m_brightnessSeek;
    private SeekBar m_speedSeek;

    /**************************************************************/

    public InfoFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_info, container, false);

        /******************* Connect widgtes with layout *******************/
        m_myledsLayout = view.findViewById(R.id.myledsLayout);
        m_brightnessSeek = view.findViewById(R.id.brightnessSeek);
        m_speedSeek = view.findViewById(R.id.speedSeek);

        m_myledsLayout.bringToFront();
        /*******************************************************************/

        /******************* Make Listener in View *******************/
        m_brightnessSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                final String seekValToString = setSeekValueCalculate(i);

                String resultStr =
                        BTManager.BLUETOOTH_SIGNAL_BRIGHTNESS
                                + BTManager.BLUETOOTH_SIGNAL_SEPERATE
                                + seekValToString
                                + "-0"; // option

                BTManager.writeToBluetoothDevice(resultStr.getBytes());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        m_speedSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                final String seekValToString = setSeekValueCalculate(i);

                String resultStr =
                        BTManager.BLUETOOTH_SIGNAL_SPEED
                                + BTManager.BLUETOOTH_SIGNAL_SEPERATE
                                + seekValToString
                                + "-0"; // option

                BTManager.writeToBluetoothDevice(resultStr.getBytes());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        /*************************************************************/


        /** Read Bluetooth Signal -> Callback **/
        m_bluetoothReadCallback = new BluetoothReadCallback() {
            @Override
            public void onResult(String signalStr) {
                if( signalStr.split("info").length != 0 ) {
                    setDeviceInfo(signalStr);
                }
            }
        };

        BTManager.setReadResultCb(m_bluetoothReadCallback);

        /** Initialize Device info from bluetooth signal **/
        String lastSignalStr = BTManager.getLastSignalStr();

        if( lastSignalStr.split("/").length != 0 ) {
            setDeviceInfo(BTManager.getLastSignalStr());
        }

        return view;
    }

    private void setDeviceInfo(String signalStr) {
        if( signalStr.equals("") ) { return; }
        String[] splitStr = signalStr.split("/");

        int ledInd = Integer.parseInt(splitStr[1]);
        float spdVal = Float.parseFloat(splitStr[2]);
        float brtVal = Float.parseFloat(splitStr[3]);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            m_brightnessSeek.setProgress((int)(brtVal*100), true);
            m_speedSeek.setProgress((int)(spdVal*100), true);
        } else {
            m_brightnessSeek.setProgress((int)(brtVal*100));
            m_speedSeek.setProgress((int)(spdVal*100));
        }
    }

    private String setSeekValueCalculate(int val) {
        String seekValToString = "";
        val = val / 10;

        if( val < 10 ) {
            seekValToString = "0" + String.valueOf(val);
        }

        return seekValToString;
    }

    public double CalculationByDistance(LatLng StartP, LatLng EndP) {
        int Radius = 6371;// radius of earth in Km
        double lat1 = StartP.latitude;
        double lat2 = EndP.latitude;
        double lon1 = StartP.longitude;
        double lon2 = EndP.longitude;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        double valueResult = Radius * c;
        double km = valueResult / 1;
        DecimalFormat newFormat = new DecimalFormat("####");
        int kmInDec = Integer.valueOf(newFormat.format(km));
        double meter = valueResult % 1000;
        int meterInDec = Integer.valueOf(newFormat.format(meter));

        return meter;
    }

    public interface OnFragmentInteractionListener {
        void messageFromChildFragment(Uri uri);
    }
}