/*
 * Copyright (c) 10/15/18 1:56 PM
 * Written by Sungdong Jo
 * Description:
 */

package com.helper.helper.model;


/**
* 트래킹 리스트의 개별 아이템 데이터
* @author 조성동
* @version 1.0.0
* @since 2018. 8. 6. AM 12:03
**/

public class TrackingRecordedListItem {


    private int m_color;
    private String m_date;
    private String m_startTime;
    private String m_endTime;
    private String m_distance;

    public TrackingRecordedListItem(int color, String date, String startTime, String endTime, String distance) {
        this.m_color = color;
        this.m_date = date;
        this.m_startTime = startTime;
        this.m_endTime = endTime;
        this.m_distance = distance;
    }


    public String getDate() {
        return m_date;
    }

    public void setDate(String m_date) {
        this.m_date = m_date;
    }

    public int getColor() {
        return m_color;
    }

    public String getStartTime() {
        return m_startTime;
    }

    public String getEndTime() {

        return m_endTime;
    }

    public String getDistance() {
        return m_distance;
    }
}
