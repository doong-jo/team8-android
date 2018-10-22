package com.helper.helper.view.login;

import android.os.Bundle;
import android.transition.Slide;
import android.util.Log;
import android.view.LayoutInflater;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.Indicators.PagerIndicator;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.DefaultSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.daimajia.slider.library.Tricks.ViewPagerEx;
import com.helper.helper.R;

import java.util.HashMap;

public class StartFragment extends Fragment implements BaseSliderView.OnSliderClickListener, ViewPagerEx.OnPageChangeListener{
    private final static String TAG = LoginFragment.class.getSimpleName() + "/DEV";
    private static final int MAX_INTRO_IMGS = 3;
    private SliderLayout m_slider;

    /******************* Define widgtes in view *******************/

    private TextView m_loginText;
    private LinearLayout m_signupWithEmailLayout;
    /**************************************************************/

    public StartFragment() {

    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
        View view = inflater.inflate( R.layout.fragment_start, container, false );

        /******************* Connect widgtes with layout *******************/
        m_slider = (SliderLayout)view.findViewById(R.id.slider);
        m_loginText = (TextView)view.findViewById(R.id.loginText);
        m_signupWithEmailLayout = (LinearLayout)view.findViewById(R.id.signupWithEmail);
        /*******************************************************************/

        /******************* Slider *******************/
        HashMap<String,Integer> file_maps = new HashMap<String, Integer>();
        file_maps.put("서비스 소개1",R.drawable.naver);
        file_maps.put("서비스 소개2",R.drawable.naver);
        file_maps.put("서비스 소개3",R.drawable.naver);


        int[] imgs = new int[]{R.drawable.naver, R.drawable.naver, R.drawable.naver};

        for (int i = 0; i < imgs.length; i++) {

            DefaultSliderView defaultSliderView = new DefaultSliderView(getContext());
            defaultSliderView
                    .image(imgs[i])
                    .setScaleType(BaseSliderView.ScaleType.Fit)
                    .setOnSliderClickListener(this);

            defaultSliderView.bundle(new Bundle());

            m_slider.addSlider(defaultSliderView);
        }

        //Building TextSlider
//        for(String name : file_maps.keySet()){
//            TextSliderView textSliderView = new TextSliderView(getContext());
//            // initialize a SliderLayout
//            textSliderView
////                    .description(name)
//                    .image(file_maps.get(name))
//                    .setScaleType(BaseSliderView.ScaleType.Fit)
//                    .setOnSliderClickListener(this);
//
//            //add your extra information
//            textSliderView.bundle(new Bundle());
////            textSliderView.getBundle()
////                    .putString("extra",name);
//
//            m_slider.addSlider(textSliderView);
//        }

        m_slider.setPresetTransformer(SliderLayout.Transformer.Default);
        m_slider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
        m_slider.setCustomIndicator((PagerIndicator) view.findViewById(R.id.custom_indicator));
        m_slider.setCustomAnimation(new DescriptionAnimation());
        m_slider.setCurrentPosition(0);
        m_slider.startAutoCycle(7000, 7000, false);
        m_slider.addOnPageChangeListener(this);

        /**********************************************/

        /******************* Make Listener in View *******************/
        m_loginText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginActivity activity = (LoginActivity)getActivity();
                if (activity != null) {
                    activity.moveToLoginFragment(view);
                }
            }
        });

        m_signupWithEmailLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginActivity activity = (LoginActivity)getActivity();
                if (activity != null) {
                    activity.moveToJoinFragment(view);
                }
            }
        });
        /*************************************************************/

        return view;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onSliderClick(BaseSliderView slider) {

    }
}