package com.helper.helper.view;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class LedMenuAdapter extends FragmentPagerAdapter {
    private int l_menuCont;

    public LedMenuAdapter(FragmentManager fm) {
        super(fm);
    }
    @Override
    public Fragment getItem(int position) {
        return null;
    }

    @Override
    public int getCount() {
        return l_menuCont;
    }
/*    // Count number of tabs
    private int m_tabCount;

    public TabPagerAdapter(FragmentManager fm, int tabCount) {
        super(fm);
        this.m_tabCount = tabCount;
    }

    @Override
    public Fragment getItem(int position) {

        Log.d("DEV", "getItem called! posistion : " + position);
        // Returning the current tabs
        switch (position) {
            case 0:
                InfoFragment infoFragment = new InfoFragment();
                return infoFragment;
            case 1:
                LEDFragment LEDfragment = new LEDFragment();
                return LEDfragment;
            case 2:
                TrackingFragment trackingFragment = new TrackingFragment();
                return trackingFragment;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return m_tabCount;
    }*/
}
