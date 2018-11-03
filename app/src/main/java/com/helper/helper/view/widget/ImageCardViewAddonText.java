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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ahmadrosid.library.FloatingLabelEditText;
import com.helper.helper.R;
import com.helper.helper.controller.EmergencyManager;
import com.helper.helper.controller.UserManager;
import com.helper.helper.interfaces.Command;

import org.json.JSONException;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class ImageCardViewAddonText extends FrameLayout {

    private LinearLayout m_cardLayout;
    private TextView m_cardNameTxt;
    private SweetAlertDialog m_detailDlg;
    private Context m_dlgTargetContxt;

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

        m_cardLayout = v.findViewById(R.id.cardLayout);
        m_cardNameTxt = v.findViewById(R.id.cardNameText)

        /** Do something about child widget **/
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

    private SweetAlertDialog makeDetailDlg() {
        if( m_dlgTargetContxt == null ) { return null; }

        return
                new SweetAlertDialog(m_dlgTargetContxt, SweetAlertDialog.WARNING_TYPE)
                        .setCustomView()
    }

    public void setOnClickDialogEnable(boolean enable, Context context) {
        if( !enable ) { return; }

        m_dlgTargetContxt = context;

        m_cardLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                m_detailDlg.show();
            }
        });
    }
}
