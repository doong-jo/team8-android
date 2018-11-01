package com.helper.helper.view.main.led;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.support.v4.app.Fragment;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

//import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.helper.helper.R;

public class LEDFragment extends Fragment {
    private BottomNavigationView bottomNavigationView;


    public LEDFragment() {

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
                        switch (item.getItemId()) {
                            case R.id.item_ledshop:
                                // TODO
                                setChildFragment(new LEDShopFragment());
                                return true;
                            case R.id.item_create:
                                // TODO
                                setChildFragment(new LEDCreateFragment());
                                return true;
                            case R.id.item_myled:
                                setChildFragment(new MyLEDFragment());
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
