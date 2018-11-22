package com.helper.helper.view.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.helper.helper.R;

public class LEDRankCardView extends LinearLayout {
    private TextView m_cardviewNameTxt;
    private TextView m_cardviewNumTxt;
    private ImageView m_cardViewImg;
    private CardView m_cardView;

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

        m_cardView = v.findViewById(R.id.rankCardView);
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
        m_cardviewNumTxt.setText(num);
    }

    public void setCardViewImg(Bitmap bitmap) {
        m_cardViewImg.setImageBitmap(bitmap);
    }
}
