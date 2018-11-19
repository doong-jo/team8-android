package com.helper.helper.view.main.led;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.support.v4.app.Fragment;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.helper.helper.R;
import com.helper.helper.controller.ViewStateManager;
import com.helper.helper.view.search.SearchActivity;

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

        setChildFragment(new LEDShopFragment());

        /******************* Connect widgtes with layout *******************/
        bottomNavigationView = getActivity().findViewById(R.id.bottomNavigationView_led_menu);
        ImageView toolbarSearch = getActivity().findViewById(R.id.toolbar_option_btn);
        /*******************************************************************/


        /******************* Make Listener in View *******************/

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

        toolbarSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getActivity(),SearchActivity.class);
                startActivity(intent);
            }
        });

        /*************************************************************/

        return view;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && getActivity() != null) {
            RelativeLayout bottomNavLayout = getActivity().findViewById(R.id.bottomNavigationViewLayout);
            bottomNavLayout.setVisibility(View.VISIBLE);

            ImageView toolbar_option_btn = getActivity().findViewById(R.id.toolbar_option_btn);
            toolbar_option_btn.setVisibility(View.VISIBLE);
//            Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
//            AppBarLayout.LayoutParams params =
//                    (AppBarLayout.LayoutParams) toolbar.getLayoutParams();
//            params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
//                    | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS
//                    | AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP);
//
//            toolbar.requestLayout();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        bottomNavigationView.setSelectedItemId(ViewStateManager.getSavedBottomNavPosition());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ViewStateManager.saveBottomNavigationPosition(bottomNavigationView.getSelectedItemId());
    }

    private void setChildFragment(Fragment child) {
        FragmentTransaction childFt = getChildFragmentManager().beginTransaction();

        if (!child.isAdded()) {
            childFt.replace(R.id.ledFragment, child);
            childFt.addToBackStack(null);
            childFt.commit();
        }
    }
}
