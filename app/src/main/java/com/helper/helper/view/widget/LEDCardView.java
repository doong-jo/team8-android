package com.helper.helper.view.widget;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.helper.helper.R;
import com.helper.helper.controller.BTManager;
import com.helper.helper.model.LED;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class LEDCardView extends FrameLayout {

    public static final int NORMAL_DIALOG_TYPE = 0;
    public static final int DETAIL_DIALOG_TYPE = 1;
    public static final int DOWNLOAD_DIALOG_TYPE = 2;

    private LinearLayout m_cardLayout;
    private TextView m_cardNameTxt;
    private ImageView m_cardImage;
    private SweetAlertDialog m_detailDlg;

    public LEDCardView(Context context) {

        super(context);
        initView();

    }

    public LEDCardView(Context context, AttributeSet attrs) {

        super(context, attrs);

        initView();
        getAttrs(attrs);
    }

    public LEDCardView(Context context, AttributeSet attrs, int defStyle) {

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
        m_cardImage = v.findViewById(R.id.cardViewImage);
        m_cardNameTxt = v.findViewById(R.id.cardNameText);

        /** Do something about child widget **/
    }

    private void getAttrs(AttributeSet attrs) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.LEDCardView);
        setTypeArray(typedArray);
    }


    private void getAttrs(AttributeSet attrs, int defStyle) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.LEDCardView, defStyle, 0);
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

    private SweetAlertDialog makeDownloadDlg(Context context, LED ledData) {
        return
                new SweetAlertDialog(context, SweetAlertDialog.NORMAL_TYPE)
                        .setTitleText(ledData.getIndex().split("_")[1])
                        .setCancelText(context.getString(R.string.led_dialog_cancel))
                        .setConfirmButton(context.getString(R.string.led_dialog_download), new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                m_detailDlg.dismissWithAnimation();
                            }
                        });
    }

    private SweetAlertDialog makeDetailDlg(final Context context, final LED ledData) {

        return
                new SweetAlertDialog(context, SweetAlertDialog.NORMAL_TYPE)
                        .setTitleText(ledData.getIndex().split("_")[1])
                        .setCancelText(context.getString(R.string.led_dialog_cancel))
                        .setConfirmButton(context.getString(R.string.led_dialog_showon), new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                BTManager.setShowOnDevice(context, ledData.getIndex());
                                m_detailDlg.dismissWithAnimation();
                            }
                        });
    }

    public void setCardImageView(Bitmap bitmap) {
        m_cardImage.setImageBitmap(bitmap);
    }

    public void setCardNameText(String txt) {
        m_cardNameTxt.setText(txt);
    }

    /** LED Dialog **/
    public void setOnClickCustomDialogEnable(final int mode, final LED ledModel, final Activity activity) {
        if( mode == NORMAL_DIALOG_TYPE ) { return; }
//        m_dlgTargetContxt = context;

        m_cardLayout.setOnClickListener(new OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View view) {

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if( mode == DETAIL_DIALOG_TYPE ) {
                            m_detailDlg = makeDetailDlg(activity, ledModel);
                        } else if( mode == DOWNLOAD_DIALOG_TYPE ) {
                            m_detailDlg = makeDownloadDlg(activity, ledModel);
                        }

                        m_detailDlg.setCustomView(new DialogLED(activity, mode, ledModel));
                        m_detailDlg.show();
                        TextView titleText = m_detailDlg.findViewById(R.id.title_text);
                        titleText.setTextAppearance(R.style.HeadlineTypo);
                    }
                });
            }
        });
    }
}
