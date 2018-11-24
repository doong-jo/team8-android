package com.helper.helper.view.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bcgdv.asia.lib.ticktock.TickTockView;
import com.bumptech.glide.Glide;
import com.helper.helper.R;
import com.helper.helper.controller.DownloadImageTask;
import com.helper.helper.controller.UserManager;
import com.helper.helper.model.LED;
import com.helper.helper.model.User;
import com.snatik.storage.Storage;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Calendar;

public class DialogTickTock extends FrameLayout {

    private long m_endTime;
    private Context m_context;

    private TickTockView m_ticktock;
    private TextView m_content;

    public DialogTickTock(Context context, int endTime) {
        super(context);
        m_context = context;
        m_endTime = endTime;

        initView();
    }

    public DialogTickTock(Context context, AttributeSet attrs) {

        super(context, attrs);

        initView();
        getAttrs(attrs);
    }

    public DialogTickTock(Context context, AttributeSet attrs, int defStyle) {

        super(context, attrs);
        initView();
        getAttrs(attrs, defStyle);
    }

    private void initView() {

        String infService = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater li = (LayoutInflater) getContext().getSystemService(infService);
        View v = li.inflate(R.layout.widget_ticktock, this, false);
        addView(v);

        /****************** Connect widgtes with layout ********************/
        m_ticktock = v.findViewById(R.id.tickTock);
        m_content = v.findViewById(R.id.content);
        /*******************************************************************/


        /******************* Make Listener in View *******************/
//        m_ticktock.setOnTickListener(new TickTockView.OnTickListener() {
//            @Override
//            public String getText(long timeRemainingInMillis) {
//                int seconds = (int) (timeRemainingInMillis / 1000) % 60;
//
//                return String.valueOf(seconds).concat("s");
//            }
//        });
        /*************************************************************/

        m_content.setText(m_context.getString(R.string.emergency_dialog_content));

        Calendar start = Calendar.getInstance();
        start.add(Calendar.SECOND, 0);

        Calendar end = Calendar.getInstance();
        end.add(Calendar.SECOND, (int)m_endTime);

        m_ticktock.start(start, end);
    }

    private void getAttrs(AttributeSet attrs) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.DialogTickTock);
        setTypeArray(typedArray);
    }


    private void getAttrs(AttributeSet attrs, int defStyle) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.DialogTickTock, defStyle, 0);
        setTypeArray(typedArray);
    }

    private void setTypeArray(TypedArray typedArray) {
        typedArray.recycle();
    }

    public void setOnTickListener(TickTockView.OnTickListener onTickListener) {
        m_ticktock.setOnTickListener(onTickListener);
    }
}
