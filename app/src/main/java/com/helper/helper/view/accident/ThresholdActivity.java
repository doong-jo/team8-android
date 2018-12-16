package com.helper.helper.view.accident;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.helper.helper.R;
import com.helper.helper.controller.SharedPreferencer;
import com.helper.helper.controller.UserManager;
import com.xw.repo.BubbleSeekBar;


public class ThresholdActivity  extends AppCompatActivity {

    private final static String TAG = ThresholdActivity.class.getSimpleName()+"/DEV";
    private static final String PREFERENCER_ACCIDENT_THRESHOLD = "ACCIDENT_THRESHOLD";

    /******************* Define widgtes in view *******************/
    private ImageView m_backBtn;
    private BubbleSeekBar m_seekbar;
    /**************************************************************/

    private SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accident_threshold);
        final Activity thisActvity = this;

        /******************* Connect widgtes with layout *******************/
        m_backBtn = findViewById(R.id.backBtn);
        m_seekbar = findViewById(R.id.thresholdSeekbar);
        /*******************************************************************/

        pref = SharedPreferencer.getSharedPreferencer(this, UserManager.getUserEmail(), MODE_PRIVATE);
        final int accidentThresholdNum = pref.getInt(PREFERENCER_ACCIDENT_THRESHOLD, 0);

        m_seekbar.setProgress(accidentThresholdNum);

        /******************* Make Listener in View *******************/
        m_backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        m_seekbar.setOnProgressChangedListener(new BubbleSeekBar.OnProgressChangedListener(){
            @Override
            public void onProgressChanged(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat, boolean fromUser){
                SharedPreferencer.putInt(PREFERENCER_ACCIDENT_THRESHOLD, progress);
            }

            @Override
            public void getProgressOnActionUp(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat) {
            }

            @Override
            public void getProgressOnFinally(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat, boolean fromUser) {
            }
        });
        /*************************************************************/
    }
}
