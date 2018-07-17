package com.helper.helper;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.app.Fragment;
import android.view.View;
import android.view.ViewGroup;

public class InfoFragment extends Fragment {

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
        View view = inflater.inflate( R.layout.fragment_info, container, false );
        return view;
    }
}