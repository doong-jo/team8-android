package com.helper.helper.tracking;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class TrackingData {
    private String m_sDate;
    private String m_sStartTime;
    private String m_sEndTime;
    private String m_sDistance;

    private List<LatLng> locationData;

    public TrackingData(String date, String startTime, String endTime, String distance, List<LatLng> locationData) {
        this.m_sDate = date;
        this.m_sStartTime = startTime;
        this.m_sEndTime = endTime;
        this.m_sDistance = distance;
        this.locationData = locationData;
    }

    public String getDate() {
        return m_sDate;
    }

    public String getStartTime() {
        return m_sStartTime;
    }

    public String getEndTime() {
        return m_sEndTime;
    }

    public String getDistance() {
        return m_sDistance;
    }

    public List<LatLng> getLocationData() {
        return locationData;
    }
}
