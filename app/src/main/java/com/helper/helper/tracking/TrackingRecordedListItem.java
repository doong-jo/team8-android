package com.helper.helper.tracking;

import android.graphics.Color;

import java.util.Date;


/**
* 트래킹 리스트의 개별 아이템 데이터
* @author 조성동
* @version 1.0.0
* @since 2018. 8. 6. AM 12:03
**/

public class TrackingRecordedListItem {


    private int color;
    private String date;
    private String startTime;
    private String endTime;
    private String distance;

    public TrackingRecordedListItem(int color, String date, String startTime, String endTime, String distance) {
        this.color = color;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.distance = distance;
    }


    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getColor() {
        return color;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {

        return endTime;
    }

    public String getDistance() {
        return distance;
    }
}
