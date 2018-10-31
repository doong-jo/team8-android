/*
 * Copyright (c) 10/15/18 1:54 PM
 * Written by Sungdong Jo
 * Description:
 */

package com.helper.helper.view;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;         // 이전 Fragment로 돌아갈 수 있음 (삭제X)
import android.util.Log;

import com.helper.helper.view.main.myeight.EightFragment;
import com.helper.helper.view.main.LEDFragment;
import com.helper.helper.view.main.TrackingFragment;

public class TabPagerAdapter extends FragmentPagerAdapter {

    // Count number of tabs
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
                EightFragment eightFragment = new EightFragment();
                return eightFragment;
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
    }
}