package com.helper.helper.view.looking;


import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.helper.helper.R;


public class LookingActivity extends Activity {
    private final static String TAG = LookingActivity.class.getSimpleName() + "/DEV";

    /******************* Define widgtes in view *******************/
    private ImageView m_pairingCircle1;
    private ImageView m_pairingCircle2;
    private ImageView m_device;
    private TextView m_title;

    private TextView m_success;
    private ImageView m_successCheck;
    /**************************************************************/

    public LookingActivity() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_looking);

        /******************* Connect widgtes with layout *******************/
        m_pairingCircle1 = findViewById(R.id.pairing_circle1);
        m_pairingCircle2 = findViewById(R.id.pairing_circle2);
        m_device = findViewById(R.id.pairing_device);
        m_title = findViewById(R.id.lookingTitle);
        m_success = findViewById(R.id.lookingSuccess);
        m_successCheck = findViewById(R.id.pairing_success_check);
        /*******************************************************************/

        startLookingforAnimation();

        Handler hideHandler = new Handler();
        hideHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                transformPairingSuccessful();
            }
        }, 5000);

    }

    private void startLookingforAnimation() {
        m_pairingCircle1.startAnimation(AnimationUtils
                .loadAnimation(getApplicationContext(),
                        R.anim.pairing_anim));


        Handler circleHandler = new Handler();
        circleHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        m_pairingCircle2.startAnimation(AnimationUtils
                                .loadAnimation(getApplicationContext(),
                                        R.anim.pairing_anim));
                    }
                });
            }
        }, 500);
    }

    private void transformPairingSuccessful() {

        Animation deviceAnim = AnimationUtils
                .loadAnimation(getApplicationContext(),
                        R.anim.pairing_fade_out);

        Animation titleAnim = AnimationUtils
                .loadAnimation(getApplicationContext(),
                        R.anim.pairing_bottom_hide);

        m_pairingCircle1.setAnimation(null);
        m_pairingCircle2.setAnimation(null);
        m_pairingCircle1.setVisibility(View.INVISIBLE);
        m_pairingCircle2.setVisibility(View.INVISIBLE);

        m_device.startAnimation(deviceAnim);
        m_title.startAnimation(titleAnim);

        Animation checkAnim = AnimationUtils
                .loadAnimation(getApplicationContext(),
                        R.anim.pairing_scale_up);

        Animation successTitleAnim = AnimationUtils
                .loadAnimation(getApplicationContext(),
                        R.anim.pairing_top_show);

        m_success.setVisibility(View.VISIBLE);
        m_successCheck.setVisibility(View.VISIBLE);

        m_success.startAnimation(successTitleAnim);
        m_successCheck.startAnimation(checkAnim);
    }
}
