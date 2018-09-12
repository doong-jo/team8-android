package com.helper.helper.led;

import android.media.Image;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.helper.helper.R;

public class LEDFragment extends Fragment {

    private ImageView m_curLEDView;

    public LEDFragment() {

    }

    public void setCurLEDView(final int ind, boolean selectable) {
        final int[] images = {
                R.drawable.bird,
                R.drawable.characters,
                R.drawable.windy,
                R.drawable.snow,
                R.drawable.rain,
                R.drawable.cute,
                R.drawable.moving_arrow_left_blink,
                R.drawable.moving_arrow_right_blink,
                R.drawable.emergency_blink,
                R.drawable.mario,
                R.drawable.boy,
        };

        if (selectable) {
            GlideDrawableImageViewTarget gifimage = new GlideDrawableImageViewTarget(m_curLEDView);
            Glide.with(this).load(ind).into(gifimage);
        } else {
            getActivity().runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            GlideDrawableImageViewTarget gifimage = new GlideDrawableImageViewTarget(m_curLEDView);
                            Glide.with(getActivity()).load(images[ind]).into(gifimage);
                        }
                    }
            );
        }
    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
        View view = inflater.inflate( R.layout.fragment_led, container, false );

        m_curLEDView = (ImageView) view.findViewById(R.id.ledFragment_curLED);

        // TODO: 2018. 8. 14. 아래는 임시코드로써 서버에서 LED 리스트를 불러와 그려줘야함.

//        ImageView curImg = (ImageView) view.findViewById(R.id.curImg);
//        GlideDrawableImageViewTarget gifImage0 = new GlideDrawableImageViewTarget(curImg);
//        Glide.with(this).load(R.drawable.characters).into(gifImage0);

        ImageView img0 = (ImageView) view.findViewById(R.id.img0);
        GlideDrawableImageViewTarget gifImage0 = new GlideDrawableImageViewTarget(img0);
        Glide.with(this).load(R.drawable.bird).into(gifImage0);

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
        Glide.with(this).load(R.drawable.moving_arrow_left_blink).into(gifImage6);

        ImageView img7 = (ImageView) view.findViewById(R.id.img7);
        GlideDrawableImageViewTarget gifImage7 = new GlideDrawableImageViewTarget(img7);
        Glide.with(this).load(R.drawable.moving_arrow_right_blink).into(gifImage7);

        ImageView img8 = (ImageView) view.findViewById(R.id.img8);
        GlideDrawableImageViewTarget gifImage8 = new GlideDrawableImageViewTarget(img8);
        Glide.with(this).load(R.drawable.emergency_blink).into(gifImage8);

        ImageView img9 = (ImageView) view.findViewById(R.id.img9);
        GlideDrawableImageViewTarget gifImage9 = new GlideDrawableImageViewTarget(img9);
        Glide.with(this).load(R.drawable.mario).into(gifImage9);

        ImageView img10 = (ImageView) view.findViewById(R.id.img10);
        GlideDrawableImageViewTarget gifImage10 = new GlideDrawableImageViewTarget(img10);
        Glide.with(this).load(R.drawable.boy).into(gifImage10);

        return view;
    }
}