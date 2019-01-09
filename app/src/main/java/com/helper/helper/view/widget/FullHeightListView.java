/*
 * Copyright (c) 1/4/19 4:28 PM
 * Written By Sungdong Jo
 */

package com.helper.helper.view.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

public class FullHeightListView extends ListView {

    public FullHeightListView (Context context) {
        super(context);
    }

    public FullHeightListView (Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FullHeightListView (Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld) {
        super.onSizeChanged(xNew, yNew, xOld, yOld);

        setSelection(getCount());

    }
}