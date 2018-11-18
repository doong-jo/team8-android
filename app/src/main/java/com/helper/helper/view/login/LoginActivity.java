package com.helper.helper.view.login;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

import com.helper.helper.R;
import com.helper.helper.controller.HttpManager;

public class LoginActivity extends FragmentActivity {
    private final static String TAG = LoginActivity.class.getSimpleName() + "/DEV";

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