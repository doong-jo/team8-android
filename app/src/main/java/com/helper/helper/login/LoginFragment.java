package com.helper.helper.login;

import android.media.Image;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.helper.helper.R;

public class LoginFragment extends Fragment {
    private final static String TAG = LoginFragment.class.getSimpleName() + "/DEV";


    ////////////Define widgtes in view//////////////
    private ImageView m_loginWithFbImg;

    ////////////////////////////////////////////////

    public LoginFragment() {

    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
        View view = inflater.inflate( R.layout.fragment_login, container, false );

        m_loginWithFbImg = view.findViewById(R.id.loginWithFbImg);

        m_loginWithFbImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(view.getContext(), "touch fbImage onClick", Toast.LENGTH_SHORT).show();


            }
        });

        return view;
    }
}