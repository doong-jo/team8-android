package com.helper.helper.led;

import android.media.Image;
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

        // TODO: 2018. 8. 14. 아래는 임시코드로써 서버에서 LED 리스트를 불러와 그려줘야함.

        ImageView curImg = (ImageView) view.findViewById(R.id.curImg);
        GlideDrawableImageViewTarget gifImage0 = new GlideDrawableImageViewTarget(curImg);
        Glide.with(this).load(R.drawable.characters).into(gifImage0);

        ImageView img1 = (ImageView) view.findViewById(R.id.img1);
        GlideDrawableImageViewTarget gifImage1 = new GlideDrawableImageViewTarget(img1);
        Glide.with(this).load(R.drawable.characters).into(gifImage1);

        ImageView img2 = (ImageView) view.findViewById(R.id.img2);
        GlideDrawableImageViewTarget gifImage2 = new GlideDrawableImageViewTarget(img2);
        Glide.with(this).load(R.drawable.windy).into(gifImage2);

        ImageView img3 = (ImageView) view.findViewById(R.id.img3);
        GlideDrawableImageViewTarget gifImage3 = new GlideDrawableImageViewTarget(img3);
        Glide.with(this).load(R.drawable.snow).into(gifImage3);

        ImageView img4 = (ImageView) view.findViewById(R.id.img4);
        GlideDrawableImageViewTarget gifImage4 = new GlideDrawableImageViewTarget(img4);
        Glide.with(this).load(R.drawable.rain).into(gifImage4);

        ImageView img5 = (ImageView) view.findViewById(R.id.img5);
        GlideDrawableImageViewTarget gifImage5 = new GlideDrawableImageViewTarget(img5);
        Glide.with(this).load(R.drawable.cute).into(gifImage5);

        ImageView img6 = (ImageView) view.findViewById(R.id.img6);
        GlideDrawableImageViewTarget gifImage6 = new GlideDrawableImageViewTarget(img6);
        Glide.with(this).load(R.drawable.moving_arrow_left).into(gifImage6);

        ImageView img7 = (ImageView) view.findViewById(R.id.img7);
        GlideDrawableImageViewTarget gifImage7 = new GlideDrawableImageViewTarget(img7);
        Glide.with(this).load(R.drawable.moving_arrow_right).into(gifImage7);

        ImageView img8 = (ImageView) view.findViewById(R.id.img8);
        GlideDrawableImageViewTarget gifImage8 = new GlideDrawableImageViewTarget(img8);
        Glide.with(this).load(R.drawable.emergency_blink).into(gifImage8);


        return view;
    }
}