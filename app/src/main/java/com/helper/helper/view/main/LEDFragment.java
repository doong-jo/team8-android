package com.helper.helper.view.main;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.support.v4.app.Fragment;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

//import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.helper.helper.R;
import com.helper.helper.view.led.LEDShopFragment;

public class LEDFragment extends Fragment {
    private BottomNavigationView bottomNavigationView;

    public static LEDFragment newInstance() {
        return new LEDFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
        View view = inflater.inflate( R.layout.fragment_led, container, false );

        bottomNavigationView = (BottomNavigationView) view.findViewById(R.id.bottomNavigationView_led_menu);


        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        Fragment fg;
                        switch (item.getItemId()) {
                            case R.id.item_ledshop:
                                // TODO
                                InfoFragment infoFragment = new InfoFragment();
                                fg = infoFragment;
                                setChildFragment(fg);
                                return true;
                            case R.id.item_create:
                                // TODO
                                LEDFragment ledFragment = new LEDFragment();
                                fg = ledFragment;
                                setChildFragment(fg);
                                return true;
                            case R.id.item_myled:
                                TrackingFragment trackingFragment = new TrackingFragment();
                                fg = trackingFragment;
                                setChildFragment(fg);
                                return true;
                        }
                        return false;
                    }
                });

        return view;
    }

    private void setChildFragment(Fragment child) {
        FragmentTransaction childFt = getChildFragmentManager().beginTransaction();

        if (!child.isAdded()) {
            childFt.replace(R.id.ledFragment, child);
            childFt.addToBackStack(null);
            childFt.commit();
        }
    }
    /*----------- listener---------------- */

}
