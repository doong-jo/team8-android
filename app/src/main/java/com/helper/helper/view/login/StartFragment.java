package com.helper.helper.view.login;

import android.os.Bundle;
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
import com.daimajia.slider.library.Tricks.ViewPagerEx;
import com.helper.helper.R;


public class StartFragment extends Fragment implements BaseSliderView.OnSliderClickListener, ViewPagerEx.OnPageChangeListener{
    private final static String TAG = LoginFragment.class.getSimpleName() + "/DEV";

    private static final int DELAY_START = 7000;
    private static final int DELAY_SHOING = 5000;

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

        // Solve : bug first touch not working
        getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        /******************* Connect widgtes with layout *******************/
        m_slider = (SliderLayout)view.findViewById(R.id.slider);
        m_loginText = (TextView)view.findViewById(R.id.loginText);
        m_signupWithEmailLayout = (LinearLayout)view.findViewById(R.id.signupWithEmail);
        /*******************************************************************/

        /******************* Slider *******************/

        String[] imgUrlArr = new String[]{
                getString(R.string.intro_service_url).concat("1").concat(getString(R.string.png_format)),
                getString(R.string.intro_service_url).concat("2").concat(getString(R.string.png_format)),
                getString(R.string.intro_service_url).concat("3").concat(getString(R.string.png_format)),
        };

        for (int i = 0; i < imgUrlArr.length; i++) {

            DefaultSliderView defaultSliderView = new DefaultSliderView(getContext());
            defaultSliderView
                    .image(imgUrlArr[i])
                    .setScaleType(BaseSliderView.ScaleType.CenterCrop)
                    .setOnSliderClickListener(this);

            defaultSliderView.bundle(new Bundle());

            m_slider.addSlider(defaultSliderView);
        }

        m_slider.setPresetTransformer(SliderLayout.Transformer.Default);
        m_slider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
        m_slider.setCustomIndicator((PagerIndicator) view.findViewById(R.id.custom_indicator));
        m_slider.setCustomAnimation(new DescriptionAnimation());
        m_slider.setCurrentPosition(0);
        m_slider.startAutoCycle(DELAY_START, DELAY_SHOING, false);
        m_slider.addOnPageChangeListener(this);

        /**********************************************/

        /******************* Make Listener in View *******************/
        m_loginText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginActivity activity = (LoginActivity)getActivity();
                activity.moveToFragment(new LoginFragment(), false);
            }
        });

        m_signupWithEmailLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginActivity activity = (LoginActivity)getActivity();
                activity.moveToFragment(new JoinFragment(), false);
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