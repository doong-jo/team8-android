package com.helper.helper.view.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.helper.helper.R;
import com.helper.helper.controller.DownloadImageTask;
import com.helper.helper.model.LED;
import com.snatik.storage.Storage;

import java.io.File;

public class DialogLED extends FrameLayout {

    private Context m_context;
    private int m_dlgMode;
    private LED m_ledData;

    private ImageView m_bookmarkToggle;
    private boolean m_bIsBookmarked;

    private ImageView m_ImageLED;
    private TextView m_createText;
    private TextView m_downloadCnt;

    public DialogLED(Context context, int mode, LED ledData) {
        super(context);
        m_context = context;
        m_dlgMode = mode;
        m_ledData = ledData;
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
        if( m_dlgMode == LEDCardView.DETAIL_DIALOG_TYPE ) {
            v = li.inflate(R.layout.widget_detail_myled_dialog, this, false);
        } else if( m_dlgMode == LEDCardView.DOWNLOAD_DIALOG_TYPE ) {
            v = li.inflate(R.layout.widget_detail_ledshop_dialog, this, false);
        }
        addView(v);

        /****************** Connect widgtes with layout ********************/
        m_bookmarkToggle = v.findViewById(R.id.bookmarkToggle);
        m_ImageLED = v.findViewById(R.id.dlgImageLED);
        m_createText = v.findViewById(R.id.ledCreatorText);
        m_downloadCnt = v.findViewById(R.id.ledDownloadCnt);
        /*******************************************************************/

        Storage internalStorage = new Storage(m_context);
        String path = internalStorage.getInternalFilesDirectory();
        String dir = path + File.separator + DownloadImageTask.DOWNLOAD_PATH;
        String openFilePath = dir + File.separator + m_ledData.getIndex() + ".gif";
        File f=new File(openFilePath);

        Glide.with(m_context)
                .load(f)
                .into(m_ImageLED);

        m_bIsBookmarked = m_ledData.getBookmarked();
        if( m_bIsBookmarked ) {
            m_bookmarkToggle.setImageResource(R.drawable.ic_bookmark_black_selected);
        } else {
            m_bookmarkToggle.setImageResource(R.drawable.ic_bookmark_black);
        }

        m_downloadCnt.setText(String.valueOf(m_ledData.getDownloadCnt()));

        /******************* Make Listener in View *******************/
        m_bookmarkToggle.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if( m_bIsBookmarked ) {
                    m_bookmarkToggle.setImageResource(R.drawable.ic_bookmark_black);
                } else {
                    m_bookmarkToggle.setImageResource(R.drawable.ic_bookmark_black_selected);
                }

                m_bIsBookmarked = !m_bIsBookmarked;
            }
        });
        /*************************************************************/




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
