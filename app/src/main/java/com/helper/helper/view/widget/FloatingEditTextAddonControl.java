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

public class FloatingEditTextAddonControl extends FrameLayout {

    private static final int STATE_CLEAR = 293;
    private static final int STATE_CHECK = 585;
    private static final int STATE_INVISIBLE = 775;

    FloatingLabelEditText m_floatingLblEditTxt;
    Button m_controlBtn;
    OnClickListener m_clearListener;

    int m_state;
    int m_textLength;
    boolean m_hasClear;
    boolean m_hasCheck;
    boolean m_checked;
    Command m_leaveFocusCmd;
    Command m_enterFocusCmd;

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
                if( !getText().equals("") && m_hasClear ) {
                    setControlStateClear();
                }

                if( getText().length() > m_textLength) {
                    String maximumAllowedCharacters = getText().substring(0, m_textLength);

                    m_floatingLblEditTxt.getmEditText().setText(maximumAllowedCharacters);
                    m_floatingLblEditTxt.getmEditText().setSelection(m_textLength);
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
                    if( m_enterFocusCmd != null ) {
                        m_enterFocusCmd.execute();
                    }

                    if( !getText().equals("") && m_hasClear ) {
                        setControlStateClear();
                    } else {
                        m_controlBtn.setVisibility(View.INVISIBLE);
                    }
                } else {
                    m_controlBtn.setVisibility(View.INVISIBLE);
//                    if ( m_leaveFocusCmd != null && m_hasCheck ) {
//                        m_leaveFocusCmd.execute();
//                    }
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
        m_textLength = typedArray.getInteger(R.styleable.FloatingEditTextAddonControl_text_length, 30);

        m_floatingLblEditTxt.setHint(hintText);

        typedArray.recycle();
    }

    public String getText() {
        return m_floatingLblEditTxt.getmEditText().getText().toString();
    }

    public void setText(String str) {m_floatingLblEditTxt.getmEditText().setText(str);}

    public void setInputType(int type) {
        m_floatingLblEditTxt.getmEditText().setInputType(type);
    }

    public void setImeOption(int type) {
        m_floatingLblEditTxt.getmEditText().setImeOptions(type);
    }

    public void setOnEditorActionListener(TextView.OnEditorActionListener listener) {
        m_floatingLblEditTxt.getmEditText().setOnEditorActionListener(listener);
    }

    public void setEnterFocusCmd(Command cmd) {
        m_enterFocusCmd = cmd;
    }

    public void setLeaveFocusCheckToggleCmd(Command cmd) {
        m_leaveFocusCmd = cmd;
    }

    public void setFocus(boolean focus, final Activity activity) {
        if( focus ) {
            m_floatingLblEditTxt.getmEditText().post(new Runnable() {
                @Override
                public void run() {
                    m_floatingLblEditTxt.getmEditText().requestFocusFromTouch();
                    InputMethodManager lManager = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                    lManager.showSoftInput(m_floatingLblEditTxt.getmEditText(), 0);
                }
            });
        } else {
            // TODO: 25/10/2018 Out focus
        }
    }

    public void setChecked(boolean checked) {
        m_checked = checked;
        setControlStateCheck();
    }

    public void setEdixTextColor(int colorResId) {
        m_floatingLblEditTxt.getmEditText().setTextColor(colorResId);
    }

    private void setControlStateClear() {
        m_controlBtn.setVisibility(View.VISIBLE);

        if( m_state != STATE_CLEAR ) {
            m_state = STATE_CLEAR;
            m_controlBtn.setVisibility(View.VISIBLE);
            m_controlBtn.setBackgroundResource(R.drawable.ic_delete);
            m_controlBtn.setOnClickListener(m_clearListener);
        }
    }

    private void setControlStateCheck() {
        if( !m_checked ) {
            m_controlBtn.setVisibility(View.INVISIBLE);
        }
        if( m_checked && m_state != STATE_CHECK ) {
            m_state = STATE_CHECK;
            m_controlBtn.setVisibility(View.VISIBLE);
            m_controlBtn.setBackgroundResource(R.drawable.ic_check);
            m_controlBtn.setOnClickListener(null);
        }
    }
}
