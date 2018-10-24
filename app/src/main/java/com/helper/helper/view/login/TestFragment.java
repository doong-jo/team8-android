package com.helper.helper.view.login;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.helper.helper.R;

public class TestFragment extends Fragment {
    private final static String TAG = JoinFragment.class.getSimpleName() + "/DEV";

    public TestFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
        View view = inflater.inflate( R.layout.fragment_test, container, false );

        return view;
    }
}
