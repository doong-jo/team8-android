package com.helper.helper.view.layout;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.design.widget.TabLayout;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.helper.helper.R;


public class HeaderLayout extends LinearLayout {
    private TextView mTextView;
    private TabLayout mTabLayout;
    private Toolbar mToolbar;

    public HeaderLayout(Context context) {
        super(context);
    }

    public HeaderLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HeaderLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public HeaderLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        //mTextView = (TextView) findViewById(R.id.text_view);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mTabLayout = (TabLayout) findViewById(R.id.tabLayout);
    }

    public int getScrollRange() {

        // return getHeight() - mTextView.getHeight();
        return getHeight() - mToolbar.getHeight();

    }
}