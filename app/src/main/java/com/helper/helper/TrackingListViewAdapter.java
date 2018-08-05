package com.helper.helper;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class TrackingListViewAdapter extends BaseAdapter{
    private final static String TAG = TrackingListViewAdapter.class.getSimpleName() + "/DEV";

    public TrackingListViewAdapter(Context mListViewContext, ArrayList<TrackingListViewItem> itemArrList) {
        this.mListViewContext = mListViewContext;
        this.mItemArrList = itemArrList;
    }

    private Context mListViewContext;
    private ArrayList<TrackingListViewItem> mItemArrList;

    private TextView dateTextView;
    private TextView startTimeTextView;
    private TextView endTimeTextView;
    private TextView distanceTextView;

    @Override
    public int getCount() {
        return this.mItemArrList.size();
    }

    @Override
    public Object getItem(int i) {
        return this.mItemArrList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if( view == null ){
            view = LayoutInflater.from(mListViewContext).inflate(R.layout.listviewitem_tracking,null);

            dateTextView = (TextView)view.findViewById(R.id.recordedDate);
            startTimeTextView = (TextView)view.findViewById(R.id.recordedStartTime);
            endTimeTextView = (TextView)view.findViewById(R.id.recordedEndTime);
            distanceTextView = (TextView)view.findViewById(R.id.recordedDistance);

            dateTextView.setText(mItemArrList.get(i).getDate().toString());
            startTimeTextView.setText(mItemArrList.get(i).getTime());
            endTimeTextView.setText(mItemArrList.get(i).getTime());
            distanceTextView.setText(String.format("%d", mItemArrList.get(i).getDistnace()));

            view = LayoutInflater.from(mListViewContext).inflate(R.layout.listviewitem_tracking,null);
        }

        return view;
    }
}
