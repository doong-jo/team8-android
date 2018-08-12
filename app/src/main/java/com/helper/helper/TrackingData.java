package com.helper.helper;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class TrackingData {
    private String date;
    private String startTime;
    private String endTime;
    private String distance;

    private List<LatLng> locationData;

    public TrackingData(String date, String startTime, String endTime, String distance, List<LatLng> locationData) {
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.distance = distance;
        this.locationData = locationData;
    }

    public String getDate() {
        return date;
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

    public List<LatLng> getLocationData() {
        return locationData;
    }
}
