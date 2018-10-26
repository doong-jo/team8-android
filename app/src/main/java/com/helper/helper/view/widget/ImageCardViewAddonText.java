package com.helper.helper.view.widget;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.ahmadrosid.library.FloatingLabelEditText;
import com.helper.helper.R;
import com.helper.helper.interfaces.Command;

public class ImageCardViewAddonText extends FrameLayout {

    FloatingLabelEditText m_floatingLblEditTxt;
    Button m_controlBtn;
    OnClickListener m_clearListener;

    public ImageCardViewAddonText(Context context) {

        super(context);
        initView();

    }

    public ImageCardViewAddonText(Context context, AttributeSet attrs) {

        super(context, attrs);

        initView();
        getAttrs(attrs);
    }

    public ImageCardViewAddonText(Context context, AttributeSet attrs, int defStyle) {

        super(context, attrs);
        initView();
        getAttrs(attrs, defStyle);

    }


    private void initView() {

        String infService = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater li = (LayoutInflater) getContext().getSystemService(infService);
        View v = li.inflate(R.layout.widget_cardview_addon_text, this, false);
        addView(v);
//
//        m_floatingLblEditTxt = findViewById(R.id.floatingLblEditTxt);
//        m_controlBtn = findViewById(R.id.controlBtn);
//
//        m_clearListener = new OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                m_floatingLblEditTxt.setText("");
//                m_state = STATE_INVISIBLE;
//                m_controlBtn.setVisibility(View.INVISIBLE);
//            }
//        };
    }

    private void getAttrs(AttributeSet attrs) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.FloatingEditTextAddonControl);
        setTypeArray(typedArray);
    }


    private void getAttrs(AttributeSet attrs, int defStyle) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.FloatingEditTextAddonControl, defStyle, 0);
        setTypeArray(typedArray);

    }

    private void setTypeArray(TypedArray typedArray) {
//        m_hasClear = typedArray.getBoolean(R.styleable.FloatingEditTextAddonControl_clear_control, false);
//        m_hasCheck = typedArray.getBoolean(R.styleable.FloatingEditTextAddonControl_check_control, false);
//        String hintText = typedArray.getString(R.styleable.FloatingEditTextAddonControl_hint_text);
//        m_textLength = typedArray.getInteger(R.styleable.FloatingEditTextAddonControl_text_length, 30);

//        m_floatingLblEditTxt.setHint(hintText);

        typedArray.recycle();
    }
}
