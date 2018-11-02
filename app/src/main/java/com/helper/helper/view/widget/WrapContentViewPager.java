package com.helper.helper.view.widget;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;

public class WrapContentViewPager extends ViewPager {

    private int mCurrentPagePosition = 0;

    public WrapContentViewPager(Context context) {
        super(context);
    }

    public WrapContentViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    int getMeasureExactly(View child, int widthMeasureSpec) {
        child.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        int height = child.getMeasuredHeight();
        return MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        boolean wrapHeight = MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST;

        final View tab = getChildAt(0);
        if (tab == null) {
            return;
        }

        int width = getMeasuredWidth();
        if (wrapHeight) {
            // Keep the current measured width.
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
        }
        Fragment fragment = ((Fragment) getAdapter().instantiateItem(this, getCurrentItem()));
        heightMeasureSpec = getMeasureExactly(fragment.getView(), widthMeasureSpec);

        //Log.i(Constants.TAG, "item :" + getCurrentItem() + "|height" + heightMeasureSpec);
        // super has to be called again so the new specs are treated as
        // exact measurements.
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void reMeasureCurrentPage(int position) {
        mCurrentPagePosition = position;
        requestLayout();
    }
}