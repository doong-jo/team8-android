package com.helper.helper.view.main.myeight;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.helper.helper.R;
import com.helper.helper.controller.BTManager;
import com.helper.helper.interfaces.ValidateCallback;

import org.json.JSONException;

public class EightFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private ValidateCallback m_bluetoothConnectionCallback;

    private String m_curFragmentClassName;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
//        Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
//        AppBarLayout.LayoutParams params =
//                (AppBarLayout.LayoutParams) toolbar.getLayoutParams();
//        params.setScrollFlags(0);
//
//        toolbar.requestLayout();
        // Inflate the layout for this fragment

        m_bluetoothConnectionCallback = new ValidateCallback() {
            @Override
            public void onDone(int resultCode) {
                if (resultCode == BTManager.SUCCESS_BLUETOOTH_CONNECT) {
                    moveToFragment(new InfoFragment());
                } else if (resultCode == BTManager.FAIL_BLUETOOTH_CONNECT) {
                    moveToFragment(new PairingFragment());
                }
            }
        };

        BTManager.setConnectionResultCb(m_bluetoothConnectionCallback);

        return inflater.inflate(R.layout.fragment_eight, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        moveToFragment(new PairingFragment());
    }

    public void moveToFragment(Fragment targetFragment) {
        if( targetFragment.getClass().getName().equals(m_curFragmentClassName) ) { return; }
        m_curFragmentClassName = targetFragment.getClass().getName();

        Fragment childFragment = targetFragment;
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.eight_fragment_container, childFragment).commit();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && getActivity() != null) {
            RelativeLayout bottomNavLayout = getActivity().findViewById(R.id.bottomNavigationViewLayout);
            bottomNavLayout.setVisibility(View.GONE);

            ImageView toolbar_option_btn = getActivity().findViewById(R.id.toolbar_option_btn);
            toolbar_option_btn.setVisibility(View.GONE);
//            Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
//            AppBarLayout.LayoutParams params =
//                    (AppBarLayout.LayoutParams) toolbar.getLayoutParams();
//            params.setScrollFlags(0);
//
//            toolbar.requestLayout();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        BTManager.setConnectionResultCb(m_bluetoothConnectionCallback);

        if ( BTManager.getConnected() ) {
            moveToFragment(new InfoFragment());
        } else {
            moveToFragment(new PairingFragment());
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void messageFromParentFragment(Uri uri);
    }
}