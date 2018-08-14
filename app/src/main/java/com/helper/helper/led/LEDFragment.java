package com.helper.helper.led;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.helper.helper.R;

public class LEDFragment extends Fragment {

    public LEDFragment() {

    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
        View view = inflater.inflate( R.layout.fragment_led, container, false );


        ImageView img1 = (ImageView) view.findViewById(R.id.img1);
        GlideDrawableImageViewTarget gifImage1 = new GlideDrawableImageViewTarget(img1);
        Glide.with(this).load(R.drawable.butterfly).into(gifImage1);

        ImageView img2 = (ImageView) view.findViewById(R.id.img2);
        GlideDrawableImageViewTarget gifImage2 = new GlideDrawableImageViewTarget(img2);
        Glide.with(this).load(R.drawable.battery).into(gifImage2);

        ImageView img3 = (ImageView) view.findViewById(R.id.img3);
        GlideDrawableImageViewTarget gifImage3 = new GlideDrawableImageViewTarget(img3);
        Glide.with(this).load(R.drawable.bird).into(gifImage3);

        ImageView img4 = (ImageView) view.findViewById(R.id.img4);
        GlideDrawableImageViewTarget gifImage4 = new GlideDrawableImageViewTarget(img4);
        Glide.with(this).load(R.drawable.heart).into(gifImage4);

        return view;
    }
}