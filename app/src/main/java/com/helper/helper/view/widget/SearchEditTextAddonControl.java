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

import com.helper.helper.R;
import com.helper.helper.interfaces.Command;

public class SearchEditTextAddonControl extends FrameLayout {
    private static final int STATE_CLEAR = 293;
    private static final int STATE_INVISIBLE = 775;

    private EditText m_searchEditTxt;
    private Button m_controlBtn;
    private View.OnClickListener m_clearListener;

    int m_state;
    boolean m_hasClear;
    Command m_leaveFocusCmd;
    Command m_enterFocusCmd;

    public SearchEditTextAddonControl(Context context){
        super(context);
        initView();
    }

    public SearchEditTextAddonControl(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
        getAttrs(attrs);
    }

    public SearchEditTextAddonControl(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs);
        initView();
        getAttrs(attrs, defStyle);
    }

    private void initView() {
        String infService = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater li = (LayoutInflater) getContext().getSystemService(infService);
        View v = li.inflate(R.layout.widget_search_edit_control, this, false);
        addView(v);

        m_searchEditTxt = findViewById(R.id.searchEditTxt);
        m_controlBtn = findViewById(R.id.searchControlBtn);

        m_clearListener = new OnClickListener() {
            @Override
            public void onClick(View view) {
                m_searchEditTxt.setText("");
                m_state = STATE_INVISIBLE;
                m_controlBtn.setVisibility(View.INVISIBLE);
            }
        };

        m_searchEditTxt.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean focused) {
                if ( !m_hasClear ) { return; }


                if( focused ) {
                    if( m_enterFocusCmd != null ) {
                        m_enterFocusCmd.execute();
                    }
                } else {
                    m_controlBtn.setVisibility(View.INVISIBLE);
                }
            }
        });

        m_searchEditTxt.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if( m_hasClear ) {
                    if (!getText().equals("")) {
                        setControlStateClear();
                    } else {
                        m_controlBtn.setVisibility(View.INVISIBLE);
                    }
                }
            }
        });
    }

    private void getAttrs(AttributeSet attrs) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.SearchEditTextAddonControl);
        setTypeArray(typedArray);
    }


    private void getAttrs(AttributeSet attrs, int defStyle) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.SearchEditTextAddonControl, defStyle, 0);
        setTypeArray(typedArray);

    }

    private void setTypeArray(TypedArray typedArray) {
        m_hasClear = typedArray.getBoolean(R.styleable.SearchEditTextAddonControl_search_clear_control, false);
        String hintText = typedArray.getString(R.styleable.SearchEditTextAddonControl_search_hint_text);

        m_searchEditTxt.setHint(hintText);

        typedArray.recycle();
    }

    public String getText() {
        return m_searchEditTxt.getText().toString();
    }

    public void setInputType(int type) {
        m_searchEditTxt.setInputType(type);
    }

    public void setImeOption(int type) {
        m_searchEditTxt.setImeOptions(type);
    }

    public void setOnEditorActionListener(TextView.OnEditorActionListener listener) {
        m_searchEditTxt.setOnEditorActionListener(listener);
    }

    public void setEnterFocusCmd(Command cmd) {
        m_enterFocusCmd = cmd;
    }

    public void setFocus(boolean focus, final Activity activity) {
        if( focus ) {
            m_searchEditTxt.post(new Runnable() {
                @Override
                public void run() {
                    m_searchEditTxt.requestFocusFromTouch();
                    InputMethodManager lManager = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                    lManager.showSoftInput(m_searchEditTxt, 0);
                }
            });
        } else {
        }
    }

    public void setEditTextColor(int colorResId) {
        m_searchEditTxt.setTextColor(colorResId);
    }

    public void setEditTextChangedEvent(TextWatcher watcher) {
        m_searchEditTxt.addTextChangedListener(watcher);
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

}
