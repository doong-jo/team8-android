package com.helper.helper.view.widget;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.helper.helper.R;
import com.helper.helper.controller.BTManager;
import com.helper.helper.controller.CommonManager;
import com.helper.helper.controller.DownloadImageTask;
import com.helper.helper.controller.HttpManager;
import com.helper.helper.controller.UserManager;
import com.helper.helper.interfaces.HttpCallback;
import com.helper.helper.interfaces.ValidateCallback;
import com.helper.helper.model.LED;
import com.helper.helper.model.User;
import com.snatik.storage.Storage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class LEDRankCardView extends LinearLayout {

    public static final int NORMAL_DIALOG_TYPE = 0;
    public static final int DETAIL_DIALOG_TYPE = 1;
    public static final int DOWNLOAD_DIALOG_TYPE = 2;

    private LinearLayout m_cardLayout;
    private TextView m_cardviewNameTxt;
    private TextView m_cardviewNumTxt;
    private ImageView m_cardViewImg;
    private SweetAlertDialog m_detailDlg;

    public LEDRankCardView(Context context){
        super(context);
        initView();
    }

    public LEDRankCardView(Context context, AttributeSet attrs){
        super(context, attrs);

        initView();
        getAttrs(attrs);
    }

    public LEDRankCardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs);
        initView();
        getAttrs(attrs, defStyle);
    }

    private void initView() {

        String infService = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater li = (LayoutInflater) getContext().getSystemService(infService);
        View v = li.inflate(R.layout.widget_cardview_addon_rank, this, false);
        addView(v);

        m_cardLayout = v.findViewById(R.id.cardLayout);
        m_cardViewImg = v.findViewById(R.id.rankCardImg);
        m_cardviewNumTxt = v.findViewById(R.id.rankNumTxt);
        m_cardviewNameTxt = v.findViewById(R.id.rankCardNameTxt);
    }

    private void getAttrs(AttributeSet attrs) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.LEDRankCardView);
        setTypeArray(typedArray);
    }

    private void getAttrs(AttributeSet attrs, int defStyle) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.LEDRankCardView, defStyle, 0);
        setTypeArray(typedArray);
    }

    private void setTypeArray(TypedArray typedArray) {

        typedArray.recycle();
    }

    public void setCardviewNameTxt(String str){
        m_cardviewNameTxt.setText(str);
    }

    public void setCardviewNumTxt(int num){
        m_cardviewNumTxt.setText(String.valueOf(num));
    }

    public void setCardViewImg(Bitmap bitmap) {
        m_cardViewImg.setImageBitmap(bitmap);
    }

    private SweetAlertDialog makeDownloadDlg(final Activity activity, final LED ledData) {
        // TODO: 16/11/2018 if exist LED -> disable confirm button
        Storage internalStorage = new Storage(activity);
        String path = internalStorage.getInternalFilesDirectory();
        String dir = path + File.separator + DownloadImageTask.DOWNLOAD_PATH;
        String openFilePathGif = dir.concat(File.separator)
                .concat(ledData.getIndex())
                .concat(activity.getString(R.string.gif_format));

        String openFilePathpng = dir.concat(File.separator)
                .concat(ledData.getIndex())
                .concat(activity.getString(R.string.png_format));

        boolean IsNotDownloaded = false;
        if( !internalStorage.isFileExist(openFilePathGif) || !internalStorage.isFileExist(openFilePathpng) ) {
            IsNotDownloaded = true;
        }

        SweetAlertDialog downloadDlg = new SweetAlertDialog(activity, SweetAlertDialog.NORMAL_TYPE)
                .setTitleText(ledData.getIndex().split("_")[1]);


        if( IsNotDownloaded ) {
            downloadDlg/** Click Download **/
                    .setCancelText(activity.getString(R.string.led_dialog_cancel))
                    .setConfirmButton(activity.getString(R.string.led_dialog_download), new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(final SweetAlertDialog sweetAlertDialog) {
                            DownloadImageTask downloadUserDataLED = new DownloadImageTask(activity, new ValidateCallback() {
                                @Override
                                public void onDone(int resultCode) throws JSONException {
                                    if( resultCode == DownloadImageTask.DONE_LOAD_LED_IMAGES ) {
                                        /** Update user info **/
                                        User user = UserManager.getUser();
                                        if( !user.getUserLEDIndicies().contains(ledData.getIndex()) ) {
                                            user.addBookmarkLEDIndex(ledData.getIndex());

                                            JSONObject jsonObj = new JSONObject();
                                            jsonObj.put(User.KEY_LED_INDICIES, user.getUserLEDIndicies());

                                            UserManager.updateUserInfoServerAndXml(activity, jsonObj);
                                        }

                                        JSONObject jsonObj = new JSONObject();
                                        jsonObj.put(LED.KEY_INDEX, ledData.getIndex());
                                        jsonObj.put(LED.KEY_DOWNLOADCNT, 1);

                                        HttpManager.useCollection(activity.getString(R.string.collection_led));

                                        /** Increase LED's downloadcount **/
                                        HttpManager.requestHttp(jsonObj, "index", "PUT", "downloadcount/", new HttpCallback() {
                                            @Override
                                            public void onSuccess(JSONArray jsonArray) { }

                                            @Override
                                            public void onError(String err) { }
                                        });

                                        activity.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                sweetAlertDialog
                                                        .setTitleText(activity.getString(R.string.led_download_complete))
                                                        /** Click Show 8 **/
                                                        .setConfirmButton(activity.getString(R.string.led_dialog_showon), new SweetAlertDialog.OnSweetClickListener()
                                                        {
                                                            @Override
                                                            public void onClick(final SweetAlertDialog sweetAlertDialog) {
                                                                BTManager.setShowOnDevice(activity, ledData.getIndex());
                                                                sweetAlertDialog.dismissWithAnimation();
                                                            }})
                                                        .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                                            }
                                        });
                                    }
                                }
                            });
                            /** Download User's LED **/
                            // uri + ledIndex
                            downloadUserDataLED.execute(CommonManager.getUriStringArrOfLED(activity.getString(R.string.server_uri), ledData.getIndex()));

                        }
                    });
        } else {
            downloadDlg.showCancelButton(false);
            downloadDlg.setConfirmButton("Already downloaded", null);
        }

        return downloadDlg;
    }

    /** LED Dialog **/
    public void setOnClickCustomDialogEnable(final int mode, final LED ledModel, final Activity activity) {
        if( mode == NORMAL_DIALOG_TYPE ) { return; }

        m_cardLayout.setOnClickListener(new OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View view) {

                m_detailDlg = makeDownloadDlg(activity, ledModel);

                m_detailDlg.setCustomView(new DialogLED(activity, mode, ledModel));
                m_detailDlg.show();

                TextView titleText = m_detailDlg.findViewById(R.id.title_text);
                titleText.setTextAppearance(R.style.HeadlineTypo);
            }
        });
    }
}
