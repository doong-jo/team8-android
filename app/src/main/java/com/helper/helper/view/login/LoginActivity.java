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
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Toast;

import com.helper.helper.R;
import com.helper.helper.controller.HttpManager;
import com.helper.helper.controller.HttpManager;
import com.helper.helper.controller.PermissionManager;
import com.helper.helper.controller.UserManager;
import com.helper.helper.model.User;

import java.util.ArrayList;

public class LoginActivity extends FragmentActivity {
    private final static String TAG = LoginActivity.class.getSimpleName() + "/DEV";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        User user = new User.Builder()
                .build();

        Fragment fragment = new LoginFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add( R.id.fragmentPlace, fragment );
        fragmentTransaction.commit();

        UserManager.setUser(user);

        // Solve : bug first touch not working
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        printSecreenInfo();
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        setContentView(R.layout.activity_login);

        Fragment fragment = new StartFragment();

        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentPlace, fragment).commit();

        HttpManager.setServerURI(getString(R.string.server_uri));
    }

    void printSecreenInfo(){

        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);

        Log.i(TAG, "density :" +  metrics.density);

        // density interms of dpi
        Log.i(TAG, "D density :" +  metrics.densityDpi);

        // horizontal pixel resolution
        Log.i(TAG, "width pix :" +  metrics.widthPixels);

        Log.i(TAG, "height pix :" +  metrics.heightPixels);

        // actual horizontal dpi
        Log.i(TAG, "xdpi :" +  metrics.xdpi);

        // actual vertical dpi
        Log.i(TAG, "ydpi :" +  metrics.ydpi);

    }

    public void moveToFragment(Fragment targetFragment, boolean popStack) {
        FragmentManager fragmentManager = getSupportFragmentManager();

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

        if( popStack ) {
            for(int i = 0; i < fragmentManager.getBackStackEntryCount(); ++i) {
                fragmentManager.popBackStack();
            }
            fragmentTransaction.replace(R.id.fragmentPlace, targetFragment);
        }else {
            fragmentTransaction.add(R.id.fragmentPlace, targetFragment);
            fragmentTransaction.addToBackStack(null);
        }

        fragmentTransaction.commit();
    }
}