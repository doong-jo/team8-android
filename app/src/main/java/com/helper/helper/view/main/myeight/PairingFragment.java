package com.helper.helper.view.main.myeight;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.helper.helper.R;
import com.helper.helper.view.looking.LookingActivity;

public class PairingFragment extends Fragment {
    private final static String TAG = PairingFragment.class.getSimpleName() + "/DEV";

    public PairingFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
        View view = inflater.inflate( R.layout.fragment_pairing, container, false );

        Button startBtn = view.findViewById(R.id.startPairingBtn);

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), LookingActivity.class);
                startActivity(intent);

//                EightFragment fragment = (EightFragment)getParentFragment();
//                if( fragment != null ) {
//                    fragment.moveToFragment(new InfoFragment());
//                }
            }
        });

        return view;
    }
}
