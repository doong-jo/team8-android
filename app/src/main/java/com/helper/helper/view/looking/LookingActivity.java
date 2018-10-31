package com.helper.helper.view.looking;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.helper.helper.R;
import com.helper.helper.controller.BTManager;
import com.helper.helper.interfaces.ValidateCallback;
import com.helper.helper.view.ScrollingActivity;

import org.json.JSONException;


public class LookingActivity extends Activity {
    private final static String TAG = LookingActivity.class.getSimpleName() + "/DEV";

    private static final int MOVE_MAINACTIVITY_TIME = 2000;
    private static final int INTERVAL_CIRCLE_OCCURE = 500;

    /******************* Define widgtes in view *******************/
    private ImageView m_pairingCircle1;
    private ImageView m_pairingCircle2;
    private ImageView m_device;
    private TextView m_title;

    private TextView m_resultTitle;
    private ImageView m_resultSymbol;
    private ImageView m_resultCircle;

    private Button m_retryBtn;
    private ImageView m_backMainImg;
    /**************************************************************/

    public LookingActivity() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_looking);

        final Activity activity = this;

        /******************* Connect widgtes with layout *******************/
        m_pairingCircle1 = findViewById(R.id.pairing_circle1);
        m_pairingCircle2 = findViewById(R.id.pairing_circle2);
        m_device = findViewById(R.id.pairing_device);
        m_title = findViewById(R.id.lookingTitle);

        m_resultTitle = findViewById(R.id.resultTitle);
        m_resultSymbol = findViewById(R.id.pairing_result_symbol);
        m_resultCircle = findViewById(R.id.pairing_result_circle);

        m_retryBtn = findViewById(R.id.retryBtn);

        m_backMainImg = findViewById(R.id.backMainActivity);
        /*******************************************************************/

        /******************* Make Listener in View *******************/

        m_retryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                retryConnectAnimation();
                startLookingforAnimation();
                Thread findingThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        BTManager.initBluetooth(activity);
                    }
                });
                findingThread.start();
            }
        });

        m_backMainImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        /*************************************************************/

        startLookingforAnimation();

//        Handler findDeviceHandler = new Handler();
//        findDeviceHandler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                Thread findingThread = new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        BTManager.initBluetooth(activity);
//                    }
//                });
//                findingThread.start();
//            }
//        }, 2000);


        Thread findingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                BTManager.initBluetooth(activity);
            }
        });
        findingThread.start();

        BTManager.setConnectionResultCb(new ValidateCallback() {
            @Override
            public void onDone(final int resultCode) throws JSONException {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if( resultCode == BTManager.SUCCESS_BLUETOOTH_CONNECT ) {
                            transformResult(true);
                        } else if( resultCode == BTManager.FAIL_BLUETOOTH_CONNECT ) {
                            transformResult(false);
                        }
                    }
                });
            }
        });

//        transformPairingSuccessful();
//        Handler hideHandler = new Handler();
//        hideHandler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                transformResult(false);
//            }
//        }, 5000);

    }

    private void retryConnectAnimation() {
        m_title.setAnimation(null);
        m_device.setAnimation(null);
        m_pairingCircle1.setAnimation(null);
        m_pairingCircle2.setAnimation(null);

        m_resultCircle.setAnimation(null);
        m_resultSymbol.setAnimation(null);
        m_resultTitle.setAnimation(null);
        m_retryBtn.setAnimation(null);

        m_backMainImg.setVisibility(View.INVISIBLE);
        m_title.setVisibility(View.VISIBLE);
        m_device.setVisibility(View.VISIBLE);
        m_resultCircle.setVisibility(View.INVISIBLE);
        m_resultSymbol.setVisibility(View.INVISIBLE);
        m_resultTitle.setVisibility(View.INVISIBLE);
        m_retryBtn.setVisibility(View.INVISIBLE);
    }

    private void startLookingforAnimation() {
        m_pairingCircle1.setVisibility(View.VISIBLE);
        m_pairingCircle2.setVisibility(View.VISIBLE);

        m_pairingCircle1.startAnimation(AnimationUtils
                .loadAnimation(getApplicationContext(),
                        R.anim.pairing_anim));


        final Activity activity = this;
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
        }, INTERVAL_CIRCLE_OCCURE);
    }

    private void transformResult(final boolean bIsSuccess) {
        m_pairingCircle1.setAnimation(null);
        m_pairingCircle2.setAnimation(null);
        m_pairingCircle1.setVisibility(View.INVISIBLE);
        m_pairingCircle2.setVisibility(View.INVISIBLE);

        m_resultTitle.setVisibility(View.VISIBLE);
        m_resultSymbol.setVisibility(View.VISIBLE);
        m_resultCircle.setVisibility(View.INVISIBLE);

        Animation deviceAnim = AnimationUtils
                .loadAnimation(getApplicationContext(),
                        R.anim.pairing_fade_out);

        Animation titleAnim = AnimationUtils
                .loadAnimation(getApplicationContext(),
                        R.anim.pairing_bottom_hide);

        m_device.startAnimation(deviceAnim);
        m_title.startAnimation(titleAnim);

        Animation checkAnim = AnimationUtils
                .loadAnimation(getApplicationContext(),
                        R.anim.pairing_scale_up);

        final Activity activity = this;
        checkAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Animation circleAnim = AnimationUtils
                        .loadAnimation(getApplicationContext(),
                                R.anim.pairing_scale_up_slow);

                circleAnim.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        /** move MainActivity **/
                        if( bIsSuccess ) {
                            Handler hideHandler = new Handler();
                            hideHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    finish();
                                }
                            }, MOVE_MAINACTIVITY_TIME);
                        }

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });

                m_resultCircle.setVisibility(View.VISIBLE);
                m_resultCircle.startAnimation(circleAnim);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        Animation successTitleAnim = AnimationUtils
                .loadAnimation(getApplicationContext(),
                        R.anim.pairing_top_show);

        Animation retryBtnAnim = AnimationUtils
                .loadAnimation(getApplicationContext(),
                        R.anim.pairing_bottom_show);

        if ( bIsSuccess ) {
            m_retryBtn.setVisibility(View.INVISIBLE);
            m_resultTitle.setText(getString(R.string.pairing_succesful));
            m_resultSymbol.setImageResource(R.drawable.ic_check_circle);
            m_resultCircle.setImageResource(R.drawable.pairing_success_circle);
        } else {
            m_retryBtn.setVisibility(View.VISIBLE);
            m_retryBtn.startAnimation(retryBtnAnim);
            m_resultTitle.setText(getString(R.string.pairing_fail));
            m_resultSymbol.setImageResource(R.drawable.ic_warning_circle);
            m_resultCircle.setImageResource(R.drawable.pairing_fail_circle);
            m_backMainImg.setVisibility(View.VISIBLE);
        }

        m_resultTitle.startAnimation(successTitleAnim);
        m_resultSymbol.startAnimation(checkAnim);
    }

    private void hideResult() {

    }
}
