package com.helper.helper.login;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.helper.helper.R;
import com.helper.helper.util.FileManagerUtil;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends FragmentActivity {
    private final static String TAG = LoginActivity.class.getSimpleName() + "/DEV";

    private ViewPager m_loginViewPager;             //검색밑에 있는 뷰페이저

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Fragment fragment = new LoginFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add( R.id.fragmentPlace, fragment );
        fragmentTransaction.commit();

//        m_loginViewPager.setAdapter(new pagerAdapter(this.getSupportFragmentManager()));

//        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

    public void moveToLoginFragment(View v) {
        Fragment fragment = new LoginFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();

        for(int i = 0; i < fragmentManager.getBackStackEntryCount(); ++i) {
            fragmentManager.popBackStack();
        }

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragmentTransaction.add( R.id.fragmentPlace, fragment );
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
        fragmentTransaction.add( R.id.fragmentPlace, fragment );
        fragmentTransaction.addToBackStack(null);

        fragmentTransaction.commit();
    }

    public void moveToPrivacyTermFragment(View v) {

        Fragment fragment = new TermFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragmentTransaction.add( R.id.fragmentPlace, fragment );
        fragmentTransaction.addToBackStack(null);

        fragmentTransaction.commit();
    }
}
