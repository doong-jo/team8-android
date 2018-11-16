package com.helper.helper.view.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.helper.helper.R;

public class LEDCategoryCardView extends FrameLayout{
    private TextView m_cardViewTxt;
    private ImageView m_cardViewImg;
    private CardView m_cardView;

    public LEDCategoryCardView(Context context){
        super(context);
        initView();
    }

    public LEDCategoryCardView(Context context, AttributeSet attrs){
        super(context, attrs);

        initView();
        getAttrs(attrs);
    }

    public LEDCategoryCardView(Context context, AttributeSet attrs, int defStyle) {

        super(context, attrs);
        initView();
        getAttrs(attrs, defStyle);

    }

    private void initView() {

        String infService = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater li = (LayoutInflater) getContext().getSystemService(infService);
        View v = li.inflate(R.layout.widget_cardview_addon_long, this, false);
        addView(v);

        m_cardViewTxt = v.findViewById(R.id.categoryCardText);
        m_cardViewImg = v.findViewById(R.id.categoryCardImg);
        m_cardView = v.findViewById(R.id.categoryCardView);
    }

    private void getAttrs(AttributeSet attrs) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.LEDCategoryCardView);
        setTypeArray(typedArray);
    }

    private void getAttrs(AttributeSet attrs, int defStyle) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.LEDCategoryCardView, defStyle, 0);
        setTypeArray(typedArray);
    }

    private void setTypeArray(TypedArray typedArray) {

        typedArray.recycle();
    }

    public void setCategoryName(String str) {
        m_cardViewTxt.setText(str);
    }

    public void setCategoryImg(Bitmap bitmap) {
        m_cardViewImg.setImageBitmap(bitmap);
    }

    public void setBkgColor(int color) {
        m_cardView.setCardBackgroundColor(color);
    }
}
