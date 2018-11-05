package com.helper.helper.view.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.helper.helper.R;

public class ImageCardViewAddonRank extends LinearLayout {
    private TextView cardviewTxt;
    private ImageView cardviewImg;

    public ImageCardViewAddonRank(Context context){
        super(context);
        initView();
    }

    public ImageCardViewAddonRank(Context context, AttributeSet attrs){
        super(context, attrs);

        initView();
        getAttrs(attrs);
    }

    public ImageCardViewAddonRank(Context context, AttributeSet attrs, int defStyle) {

        super(context, attrs);
        initView();
        getAttrs(attrs, defStyle);

    }

    private void initView() {

        String infService = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater li = (LayoutInflater) getContext().getSystemService(infService);
        View v = li.inflate(R.layout.widget_cardview_addon_rank, this, false);
        addView(v);
    }

    private void getAttrs(AttributeSet attrs) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.LedRankCardView);
        setTypeArray(typedArray);
    }

    private void getAttrs(AttributeSet attrs, int defStyle) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.LedRankCardView, defStyle, 0);
        setTypeArray(typedArray);
    }

    private void setTypeArray(TypedArray typedArray) {

        typedArray.recycle();
    }
}
