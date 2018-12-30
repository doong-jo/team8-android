package com.helper.helper.model;


import com.google.android.gms.maps.model.LatLng;

import java.util.Date;
import lombok.Builder;

@Builder
public class Accident {
    public static final String ACCIDENT_USER_ID="user_id";
    public static final String ACCIDENT_RIDING_TYPE = "riding_type";
    public static final String ACCIDENT_HAS_ALERTED = "has_alerted";
    public static final String ACCIDENT_ACCEL = "accel";
    public static final String ACCIDENT_ROLLOVER = "rollover";
    public static final String ACCIDENT_OCCURED_DATE = "occured_date";
    public static final String ACCIDENT_POSITION = "position";
    public static final String ACCIDENT_POSITION_LATITUDE = "latitude";
    public static final String ACCIDENT_POSITION_LONGITUDE = "longitude";


    private final String m_userId;
    private final String m_ridingType;
    private final boolean m_hasAlerted;
    private final double m_accel;
    private final double m_rollover;
    private final Date m_occuredDate;
    private final LatLng m_position;

    public LatLng getPosition(){
        return m_position;
    }
}
