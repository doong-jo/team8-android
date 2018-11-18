package com.helper.helper.view.layout;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

public class ContentsLayoutBehavior extends ViewOffsetBehavior {
    private static final String TAG = ContentsLayoutBehavior.class.getSimpleName();

    public ContentsLayoutBehavior() {
        Log.d("decurd", "ContentsLayoutBehavior");
    }

    public ContentsLayoutBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.d("decurd", "ContentsLayoutBehavior");
    }

    @Override
    public boolean onLayoutChild(CoordinatorLayout parent, View child, int layoutDirection) {
        return super.onLayoutChild(parent, child, layoutDirection);
    }

    @Override
    public boolean onMeasureChild(CoordinatorLayout parent, View child, int parentWidthMeasureSpec, int widthUsed, int parentHeightMeasureSpec, int heightUsed) {
        if(child.getLayoutParams().height == ViewGroup.LayoutParams.MATCH_PARENT) {
            List dependencies = parent.getDependencies(child);
            if(dependencies.isEmpty()) {
                return false;
            }

            HeaderLayout headerLayout = findHeaderLayout(dependencies);
            if(headerLayout != null && ViewCompat.isLaidOut(headerLayout)) {
                int scrollRange = headerLayout.getScrollRange();
                int height = parent.getHeight() - headerLayout.getMeasuredHeight() + scrollRange;
                int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.AT_MOST);
                parent.onMeasureChild(child, parentWidthMeasureSpec, widthUsed, heightMeasureSpec, heightUsed);
                return true;
            }
        }



        return false;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
        CoordinatorLayout.Behavior behavior = ((CoordinatorLayout.LayoutParams)dependency.getLayoutParams()).getBehavior();
        if(behavior instanceof HeaderScrollBehavior) {
            int headerOffset = ((HeaderScrollBehavior)behavior).getTopAndBottomOffset();
            int contentsOffset = dependency.getHeight() + headerOffset;
            setTopAndBottomOffset(contentsOffset);
        }

        return false;
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
        return dependency instanceof HeaderLayout;
    }

    private static HeaderLayout findHeaderLayout(List<View> views) {
        int i = 0;


        for(int z = views.size(); i < z; ++i) {
            View view = (View)views.get(i);
            if(view instanceof HeaderLayout) {
                return (HeaderLayout)view;
            }
        }

        return null;
    }
}