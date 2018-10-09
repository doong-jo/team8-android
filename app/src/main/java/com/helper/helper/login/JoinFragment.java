
/*
 * Created by Sungdong Jo on 10/9/18 5:02 PM
 * Copyright (c) 2018 . All rights reserved.
 * Last modified 10/9/18 5:02 PM
 *
 * Description: LoginActivity > JoinFragment
 *              View of join page
 */

package com.helper.helper.login;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.helper.helper.R;

public class JoinFragment extends Fragment {
    private final static String TAG = JoinFragment.class.getSimpleName() + "/DEV";

    public JoinFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
        View view = inflater.inflate( R.layout.fragment_join, container, false );

        return view;
    }
}
