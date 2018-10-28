package com.helper.helper.view.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.content.res.TypedArray;

import com.helper.helper.R;

public class LoadingButton extends  RelativeLayout {
    private RelativeLayout m_layout;
    private Button m_button;
    private ImageView m_icon;

    public LoadingButton(Context context){
        super(context);
        initView();
    }

    public LoadingButton(Context context, AttributeSet attrs){
        super(context, attrs);

        initView();
        getAttrs(attrs);
    }

    public LoadingButton(Context context, AttributeSet attrs, int defStyle){
        super(context, attrs);
        initView();
        getAttrs(attrs, defStyle);
    }

    private void initView(){
        String infService = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater li = (LayoutInflater) getContext().getSystemService(infService);
        View v = li.inflate(R.layout.widget_loading_btn, this, false);
        addView(v);

        m_layout = findViewById(R.id.loadingBtnLayout);
        m_button = findViewById(R.id.loadingBtn);
        m_icon = findViewById(R.id.loadingIcon);
    }

    private void getAttrs(AttributeSet attrs){
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.LoadingButton);
        setTypeArray(typedArray);
    }

    private void getAttrs(AttributeSet attrs, int defStyle){
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.LoadingButton, defStyle, 0);
        setTypeArray(typedArray);
    }


    private void setTypeArray(TypedArray typedArray){
        typedArray.recycle();
    }

    public void setLoadingIconVisible(boolean visible){
        if(visible){
            m_icon.setVisibility(View.VISIBLE);
            Animation anim = AnimationUtils.loadAnimation(
                    getContext().getApplicationContext(), R.anim.rotate_anim);
                m_icon.startAnimation(anim);
        }
        else{ m_icon.setVisibility(View.INVISIBLE); }
    }

    public void setText(String text) { m_button.setText(text); }

    public void setIcon(int resId) {
        m_icon.setImageResource(resId);
    }

    public void setButtonOnClickListener(OnClickListener listener) {
        m_button.setOnClickListener(listener);
    }
}
