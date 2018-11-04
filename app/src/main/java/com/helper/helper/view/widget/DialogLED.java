package com.helper.helper.view.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.helper.helper.R;

public class DialogLED extends FrameLayout {

    private int m_dlgMode;

    public DialogLED(Context context, int mode) {
        super(context);
        m_dlgMode = mode;
        initView();

    }

    public DialogLED(Context context, AttributeSet attrs) {

        super(context, attrs);

        initView();
        getAttrs(attrs);
    }

    public DialogLED(Context context, AttributeSet attrs, int defStyle) {

        super(context, attrs);
        initView();
        getAttrs(attrs, defStyle);
    }

    private void initView() {

        String infService = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater li = (LayoutInflater) getContext().getSystemService(infService);

        View v = null;
        if( m_dlgMode == ImageCardViewAddonText.DETAIL_DIALOG_TYPE ) {
            v = li.inflate(R.layout.widget_detail_myled_dialog, this, false);
        } else if( m_dlgMode == ImageCardViewAddonText.DOWNLOAD_DIALOG_TYPE ) {
            v = li.inflate(R.layout.widget_detail_ledshop_dialog, this, false);
        }
        addView(v);

        /** Do something about child widget **/
    }

    private void getAttrs(AttributeSet attrs) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.DialogLED);
        setTypeArray(typedArray);
    }


    private void getAttrs(AttributeSet attrs, int defStyle) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.DialogLED, defStyle, 0);
        setTypeArray(typedArray);
    }

    private void setTypeArray(TypedArray typedArray) {
        typedArray.recycle();
    }
}
