package com.helper.helper.view.login;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.helper.helper.R;

public class TermFragment extends Fragment {
    private final static String TAG = JoinFragment.class.getSimpleName() + "/DEV";

    private LinearLayout m_termLayout;

    public TermFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
        View view = inflater.inflate( R.layout.fragment_privacy_term, container, false );


        /******************* Connect widgtes with layout *******************/
        final Button accepBtn = view.findViewById(R.id.acceptBtn);
        m_termLayout = view.findViewById(R.id.termLayout);
        /*******************************************************************/
        accepBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginActivity activity = (LoginActivity)getActivity();
                if( activity != null ) {
                    activity.moveToFragment(new JoinFragment(), m_termLayout,true);
                }
            }
        });
        LoginActivity loginActivity = (LoginActivity)getActivity();
        loginActivity.setFragmentBackPressed(new JoinFragment(), m_termLayout, false);
        return view;
    }
}
