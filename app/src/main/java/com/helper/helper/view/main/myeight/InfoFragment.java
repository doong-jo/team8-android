package com.helper.helper.view.main.myeight;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import com.google.android.gms.maps.model.LatLng;
import com.helper.helper.R;

import java.text.DecimalFormat;

public class InfoFragment extends Fragment {
    private final static String TAG = InfoFragment.class.getSimpleName() + "/DEV";

    private LinearLayout m_myledsLayout;

    public InfoFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_info, container, false);
        m_myledsLayout = view.findViewById(R.id.myledsLayout);

        m_myledsLayout.bringToFront();

        return view;
    }

    private SeekBar.OnSeekBarChangeListener m_seekBarBrightChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            String str = "2-" + String.format("%02d-%d", i/10, i%10);
//            ((ScrollingActivity)getActivity()).sendToBluetoothDevice(str.getBytes());
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    private SeekBar.OnSeekBarChangeListener m_seekBarSpeedChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            String str = "1-" + String.format("%02d-%d", i/10, i%10);
//            ((ScrollingActivity)getActivity()).sendToBluetoothDevice(str.getBytes());
        }


        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

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