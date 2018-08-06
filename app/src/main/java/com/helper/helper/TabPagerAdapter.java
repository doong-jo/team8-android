package com.helper.helper;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;         // 이전 Fragment로 돌아갈 수 있음 (삭제X)
import android.support.v4.app.FragmentStatePagerAdapter;    // 이전 Fragment 메모리 삭제
import android.util.Log;

public class TabPagerAdapter extends FragmentPagerAdapter {

    // Count number of tabs
    private int tabCount;

    public TabPagerAdapter(FragmentManager fm, int tabCount) {
        super(fm);
        this.tabCount = tabCount;
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
        return tabCount;
    }
}