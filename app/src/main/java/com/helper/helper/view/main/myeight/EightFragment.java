package com.helper.helper.view.main.myeight;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.helper.helper.R;
import com.helper.helper.controller.BTManager;

public class EightFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_eight, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Fragment childFragment;

        if( BTManager.getConnected() ) {
            childFragment = new InfoFragment();
        } else {
            childFragment = new PairingFragment();
        }

        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.eight_fragment_container, childFragment).commit();
    }

    public void moveToFragment(Fragment targetFragment) {
        Fragment childFragment = targetFragment;
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.eight_fragment_container, childFragment).commit();
    }

    @Override
    public void onResume() {
        super.onResume();
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