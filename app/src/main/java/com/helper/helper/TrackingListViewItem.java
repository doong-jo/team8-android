package com.helper.helper;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TrackingListViewItem {
    private Date mDate;
    private int mDistance;

    public TrackingListViewItem(Date date, int distance) {
        this.mDate = date;
        this.mDistance = distance;
    }

    public Date getDate() {
        return mDate;
    }

    public void setDate(Date mDate) {
        this.mDate = mDate;
    }

    public String getTime() {
        SimpleDateFormat sdf=new SimpleDateFormat("hh:mm a");
        String currentDateTimeString = sdf.format(mDate);

        return currentDateTimeString;
    }

    public int getDistnace() {
        return mDistance;
    }

    public void setDistnace(int mDistnace) {
        this.mDistance = mDistnace;
    }
}
