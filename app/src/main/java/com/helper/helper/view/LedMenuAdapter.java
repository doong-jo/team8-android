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
}
