package com.helper.helper.view.looking;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.helper.helper.R;


public class LookingActivity extends Activity {
    private final static String TAG = LookingActivity.class.getSimpleName() + "/DEV";

    public LookingActivity() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_looking);


    }
}
