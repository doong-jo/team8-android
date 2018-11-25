package com.helper.helper.view.popup;


import android.app.Activity;
import android.os.Bundle;
import android.view.WindowManager;

import com.helper.helper.R;
import com.helper.helper.view.widget.DialogAccident;


public class PopupActivity extends Activity
{
    private final static String TAG = PopupActivity.class.getSimpleName()+"/DEV";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popup);

        DialogAccident dlgAccident = new DialogAccident(this, true);
        dlgAccident.showDialog();

        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED|
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD|
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON|
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        );
    }
}
