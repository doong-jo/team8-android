package com.helper.helper.view.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.helper.helper.R;

public class SnackBar extends FrameLayout {

    private LinearLayout m_layout;
    private TextView m_text;
    private ImageView m_icon;

    public SnackBar(Context context) {

        super(context);
        initView();

    }

    public SnackBar(Context context, AttributeSet attrs) {

        super(context, attrs);

        initView();
        getAttrs(attrs);

    }

    public SnackBar(Context context, AttributeSet attrs, int defStyle) {

        super(context, attrs);
        initView();
        getAttrs(attrs, defStyle);

    }


    private void initView() {

        String infService = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater li = (LayoutInflater) getContext().getSystemService(infService);
        View v = li.inflate(R.layout.widget_snackbar, this, false);
        addView(v);

        m_layout = findViewById(R.id.snackBar);
        m_text = findViewById(R.id.snackBarMsg);
        m_icon = findViewById(R.id.snackBarIcon);
    }

    private void getAttrs(AttributeSet attrs) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.Snackbar);
        setTypeArray(typedArray);
    }


    private void getAttrs(AttributeSet attrs, int defStyle) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.Snackbar, defStyle, 0);
        setTypeArray(typedArray);

    }

    private void setTypeArray(TypedArray typedArray) {
        typedArray.recycle();
    }

    public void setVisible(boolean visible) {
        if( visible ) { m_layout.setVisibility(View.VISIBLE); }
        else { m_layout.setVisibility(View.INVISIBLE); }
    }

    public void setIcon(int resId) {
        m_icon.setImageResource(resId);
    }

    public void setText(String text) {
        m_text.setText(text);
    }
}
