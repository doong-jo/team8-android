package com.helper.helper.view.login;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.helper.helper.R;
import com.helper.helper.controller.HttpManager;
import com.helper.helper.controller.SharedPreferencer;
import com.helper.helper.enums.RidingType;

import java.util.List;

public class LoginActivity extends FragmentActivity {
    private final static String TAG = LoginActivity.class.getSimpleName() + "/DEV";

    private boolean m_savePopStackState;
    private Fragment m_backTargetFragment;
    private ViewGroup m_backOldTargetViewGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Solve : bug first touch not working
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        setContentView(R.layout.activity_login);

        Fragment fragment = new StartFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentPlace, fragment).commit();

        HttpManager.setServerURI(getString(R.string.server_uri));
    }

    public void moveToFragment(Fragment targetFragment, ViewGroup oldTargetFragment, boolean popStack) {
        FragmentManager fragmentManager = getSupportFragmentManager();

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);


        if( popStack ) {
            for(int i = 0; i < fragmentManager.getBackStackEntryCount(); ++i) {
                fragmentManager.popBackStack();
            }
            fragmentTransaction.replace(R.id.fragmentPlace, targetFragment);
        }else {
            oldTargetFragment.setVisibility(View.GONE);
            fragmentTransaction.add(R.id.fragmentPlace, targetFragment);
            fragmentTransaction.addToBackStack(null);
        }

        fragmentTransaction.commit();
    }

    public void setFragmentBackPressed(Fragment targetFragment, ViewGroup oldTargetViewGroup, boolean popStack) {
        m_savePopStackState = popStack;
        m_backTargetFragment = targetFragment;
        m_backOldTargetViewGroup = oldTargetViewGroup;
    }

    @Override
    public void onBackPressed() {

            FragmentManager fragmentManager = getSupportFragmentManager();

            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

        if(m_backOldTargetViewGroup != null) {
            if (m_savePopStackState) {
                for (int i = 0; i < fragmentManager.getBackStackEntryCount(); ++i) {
                    fragmentManager.popBackStack();
                }
                fragmentTransaction.replace(R.id.fragmentPlace, m_backTargetFragment);
            } else {
                m_backOldTargetViewGroup.setVisibility(View.GONE);
                fragmentTransaction.add(R.id.fragmentPlace, m_backTargetFragment);
                fragmentTransaction.addToBackStack(null);
            }
            fragmentTransaction.commit();
        }
        else{
            this.finish();
        }

    }
}