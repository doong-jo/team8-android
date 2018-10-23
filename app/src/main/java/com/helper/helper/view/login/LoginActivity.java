package com.helper.helper.view.login;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.Toast;

import com.helper.helper.R;
import com.helper.helper.controller.HttpManager;
import com.helper.helper.controller.HttpManager;
import com.helper.helper.controller.PermissionManager;

import java.util.ArrayList;

public class LoginActivity extends FragmentActivity {
    private final static String TAG = LoginActivity.class.getSimpleName() + "/DEV";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Fragment fragment = new MakeProfileFragment();

        getSupportFragmentManager().beginTransaction().add(R.id.fragmentPlace, fragment).commit();

        HttpManager.setServerURI(getString(R.string.server_uri));
    }


    public void moveToLoginFragment(View v) {
        Fragment fragment = new LoginFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();

        for(int i = 0; i < fragmentManager.getBackStackEntryCount(); ++i) {
            fragmentManager.popBackStack();
        }

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragmentTransaction.replace(R.id.fragmentPlace, fragment);
        fragmentTransaction.commit();
    }

    public void moveToJoinFragment(View v) {
        Fragment fragment = new JoinFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();

        for(int i = 0; i < fragmentManager.getBackStackEntryCount(); ++i) {
            fragmentManager.popBackStack();
        }

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragmentTransaction.replace(R.id.fragmentPlace, fragment);
//        fragmentTransaction.add( R.id.fragmentPlace, fragment );
        fragmentTransaction.addToBackStack(null);

        fragmentTransaction.commit();
    }

    public void moveToStartFragment(View v) {

        Fragment fragment = new StartFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();

        for(int i = 0; i < fragmentManager.getBackStackEntryCount(); ++i) {
            fragmentManager.popBackStack();
        }

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragmentTransaction.replace(R.id.fragmentPlace, fragment);
        fragmentTransaction.addToBackStack(null);

        fragmentTransaction.commit();
    }

    public void moveToPrivacyTermFragment(View v) {

        Fragment fragment = new TermFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragmentTransaction.add(R.id.fragmentPlace, fragment);
        fragmentTransaction.addToBackStack(null);

        fragmentTransaction.commit();
    }

    public void moveToMakeProfileFragment(View v) {
        Fragment fragment = new MakeProfileFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragmentTransaction.add(R.id.fragmentPlace, fragment);
        fragmentTransaction.addToBackStack(null);

        fragmentTransaction.commit();
    }
}
