package com.helper.helper.view.login;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.helper.helper.R;

public class TermFragment extends Fragment {
    private final static String TAG = JoinFragment.class.getSimpleName() + "/DEV";

    public TermFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
        View view = inflater.inflate( R.layout.fragment_privacy_term, container, false );

        final Button accepBtn = view.findViewById(R.id.acceptBtn);

        accepBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginActivity activity = (LoginActivity)getActivity();
                if( activity != null ) {
                    activity.moveToFragment(new JoinFragment(), true);
                }
            }
        });

        return view;
    }
}
