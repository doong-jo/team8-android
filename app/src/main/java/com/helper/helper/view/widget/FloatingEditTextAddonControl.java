package com.helper.helper.view.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import com.ahmadrosid.library.FloatingLabelEditText;
import com.helper.helper.R;

public class FloatingEditTextAddonControl extends FrameLayout {

    private static final int STATE_CLEAR = 293;
    private static final int STATE_CHECK = 585;
    private static final int STATE_INVISIBLE = 775;

    FloatingLabelEditText m_floatingLblEditTxt;
    Button m_controlBtn;
    OnClickListener m_clearListener;

    int m_state;
    boolean m_hasClear;
    boolean m_hasCheck;
    boolean m_checked;

    public FloatingEditTextAddonControl(Context context) {

        super(context);
        initView();

    }

    public FloatingEditTextAddonControl(Context context, AttributeSet attrs) {

        super(context, attrs);

        initView();
        getAttrs(attrs);

    }

    public FloatingEditTextAddonControl(Context context, AttributeSet attrs, int defStyle) {

        super(context, attrs);
        initView();
        getAttrs(attrs, defStyle);

    }


    private void initView() {

        String infService = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater li = (LayoutInflater) getContext().getSystemService(infService);
        View v = li.inflate(R.layout.widget_floating_edit_control, this, false);
        addView(v);

        m_floatingLblEditTxt = findViewById(R.id.floatingLblEditTxt);
        m_controlBtn = findViewById(R.id.controlBtn);

        m_clearListener = new OnClickListener() {
            @Override
            public void onClick(View view) {
                m_floatingLblEditTxt.setText("");
                m_state = STATE_INVISIBLE;
                m_controlBtn.setVisibility(View.INVISIBLE);
            }
        };

        m_floatingLblEditTxt.getmEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if( !getText().equals("") && m_hasClear && m_state != STATE_CLEAR) {
                    setControlStateClear();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        m_floatingLblEditTxt.getmEditText().setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean focused) {
                if ( !m_hasClear && !m_hasCheck ) { return; }

                if( focused ) {
                    if( !getText().equals("") && m_hasClear ) {
                        setControlStateClear();
                    } else {
                        m_controlBtn.setVisibility(View.INVISIBLE);
                    }
                }
            }
        });
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
        m_hasClear = typedArray.getBoolean(R.styleable.FloatingEditTextAddonControl_clear_control, false);
        m_hasCheck = typedArray.getBoolean(R.styleable.FloatingEditTextAddonControl_check_control, false);
        String hintText = typedArray.getString(R.styleable.FloatingEditTextAddonControl_hint_text);

        m_floatingLblEditTxt.setHint(hintText);

        typedArray.recycle();
    }

    public String getText() {
        return m_floatingLblEditTxt.getmEditText().getText().toString();
    }

    public void setInputType(int type) {
        m_floatingLblEditTxt.getmEditText().setInputType(type);
    }

    public void setChecked(boolean checked) {
        m_checked = checked;
        setControlStateCheck();
    }

    private void setControlStateClear() {
        if( m_state != STATE_CLEAR ) {
            m_state = STATE_CLEAR;
            m_controlBtn.setVisibility(View.VISIBLE);
            m_controlBtn.setBackgroundResource(R.drawable.ic_delete);
            m_controlBtn.setOnClickListener(m_clearListener);
        }
    }

    private void setControlStateCheck() {
        if( m_state != STATE_CHECK ) {
            m_state = STATE_CHECK;
            m_controlBtn.setVisibility(View.VISIBLE);
            m_controlBtn.setBackgroundResource(R.drawable.ic_check);
            m_controlBtn.setOnClickListener(null);
        }
    }
}
