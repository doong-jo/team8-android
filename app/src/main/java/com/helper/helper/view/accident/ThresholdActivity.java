package com.helper.helper.view.accident;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.helper.helper.R;
import com.helper.helper.controller.BTManager;
import com.helper.helper.controller.SharedPreferencer;
import com.helper.helper.controller.UserManager;

import android.view.View.OnClickListener;

import cn.pedant.SweetAlert.SweetAlertDialog;


public class ThresholdActivity  extends AppCompatActivity {

    private final static String TAG = ThresholdActivity.class.getSimpleName()+"/DEV";

    private final static String HIGH_LEVEL = "HIGH";
    private final static String MEDIUM_LEVEL = "MEDIUM";
    private final static String LOW_LEVEL = "LOW";

    private final static String ENABLE = "ENABLE";
    private final static String DISABLE = "DISABLE";

    private Button m_beforeSelectedLevelBtn;
    private Button m_anotherToggleBtn;

    /******************* Define widgtes in view *******************/


    /**************************************************************/

    private SharedPreferences m_sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accident_threshold);

        /** Set Data SharedPreferencer Empty Values**/
        m_sharedPref = SharedPreferencer.getSharedPreferencer(getApplicationContext(), UserManager.getUserEmail(), MODE_PRIVATE);
        String accidentLevel = m_sharedPref.getString(SharedPreferencer.ACCIDENT_LEVEL, "HIGH");
        final boolean isEnabled = m_sharedPref.getBoolean(SharedPreferencer.ACCIDENT_ENABLE, true);

        // TODO: 19/12/2018 read data from server

        if( accidentLevel.equals("") ) {
            SharedPreferencer.putString(SharedPreferencer.ACCIDENT_LEVEL, HIGH_LEVEL);
            accidentLevel = HIGH_LEVEL;
        }

        /******************* Connect widgtes with layout *******************/
        ImageView backBtn = findViewById(R.id.backBtn);

        Button highBtn = findViewById(R.id.high_button);
        Button mediumBtn = findViewById(R.id.medium_button);
        Button lowBtn = findViewById(R.id.low_button);

        Button[] levelBtns = new Button[] {highBtn, mediumBtn, lowBtn};

        Button enableBtn = findViewById(R.id.enableBtn);
        Button disableBtn = findViewById(R.id.disableButton);
        /*******************************************************************/

        for (Button levelBtn :
                levelBtns) {
            if( levelBtn.getTag().equals(accidentLevel) ) {
                levelBtn.setBackgroundColor(getColor(R.color.accent_orange));
                m_beforeSelectedLevelBtn = levelBtn;
            }
        }

        if( isEnabled ) {
            m_anotherToggleBtn = enableBtn;
            disableBtn.setVisibility(View.VISIBLE);
        } else {
            m_anotherToggleBtn = disableBtn;
            enableBtn.setVisibility(View.VISIBLE);
        }

        /******************* Make Listener in View *******************/

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        OnClickListener m_levelButtonListener = makeLevelButtonListener();

        highBtn.setOnClickListener(m_levelButtonListener);
        mediumBtn.setOnClickListener(m_levelButtonListener);
        lowBtn.setOnClickListener(m_levelButtonListener);

        OnClickListener m_toggleButtonListener = makeToggleButtonListener();

        enableBtn.setOnClickListener(m_toggleButtonListener);
        disableBtn.setOnClickListener(m_toggleButtonListener);
        /*************************************************************/
    }

    private OnClickListener makeLevelButtonListener() {
        return
            new OnClickListener() {
                @Override
                public void onClick(View view) {
                    final Button viewBtn = ((Button)view);
                    final String curLevel = m_sharedPref.getString(SharedPreferencer.ACCIDENT_LEVEL, "");
                    final String viewLevelTag = ((String)(view.getTag()));

                    if ( curLevel.equals(viewLevelTag) ) { return; }

                    SharedPreferencer.putString(SharedPreferencer.ACCIDENT_LEVEL, viewLevelTag);

                    final String resultStr =
                            BTManager.BT_SIGNAL_THRESHOLD_ENABLE + BTManager.BLUETOOTH_SIGNAL_SEPARATE + viewLevelTag;
                    BTManager.writeToBluetoothDevice(resultStr.getBytes());

                    // TODO: 19/12/2018 save Server (accident level)

                    m_beforeSelectedLevelBtn.setBackgroundColor(getColor(R.color.gray));
                    viewBtn.setBackgroundColor(getColor(R.color.accent_orange));
                    m_beforeSelectedLevelBtn = viewBtn;
                }
            };
    }

    private OnClickListener makeToggleButtonListener() {
        return
                new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final Button viewBtn = ((Button)view);
                        final String curState = ((String)view.getTag());

                        final boolean isEnabledClicked = curState.equals(ENABLE);

                        new SweetAlertDialog(view.getContext(), SweetAlertDialog.WARNING_TYPE)
                                .setTitleText("Is it sure?")
                                .setContentText("Accident detection will be ".concat(curState))
                                .setCancelButton("No", new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                                        sweetAlertDialog.dismissWithAnimation();
                                    }
                                })
                                .setConfirmButton("Yes", new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                                        SharedPreferencer.putBoolean(SharedPreferencer.ACCIDENT_ENABLE, isEnabledClicked);

                                        final String resultStr =
                                                BTManager.BT_SIGNAL_THRESHOLD_LEVEL + BTManager.BLUETOOTH_SIGNAL_SEPARATE + isEnabledClicked;

                                        BTManager.writeToBluetoothDevice(resultStr.getBytes());

                                        // TODO: 19/12/2018 save server (enable state)

                                        viewBtn.setVisibility(View.GONE);
                                        m_anotherToggleBtn.setVisibility(View.VISIBLE);

                                        m_anotherToggleBtn = viewBtn;

                                        sweetAlertDialog.dismissWithAnimation();
                                    }
                                })
                                .show();
                    }
                };
    }
}
