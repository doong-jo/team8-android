/*
 * Copyright (c) 10/17/18 2:45 PM
 * Written by Sungdong Jo
 * Description:
 */

package com.helper.helper.controller;

import android.content.Context;
import android.location.Location;
import android.os.Handler;
import android.util.Log;

import com.helper.helper.R;
import com.helper.helper.interfaces.DistanceCallback;
import com.helper.helper.interfaces.HttpCallback;
import com.helper.helper.interfaces.ValidateCallback;
import com.helper.helper.model.ContactItem;
import com.helper.helper.model.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EmergencyManager {
    private final static String TAG = EmergencyManager.class.getSimpleName() + "/DEV";
    private static final int EMERGENCY_LOCATION_WATING_TIME = 5000; // 4min 240000
    private static final int EMERGENCY_LOCATION_DISTANCE_RANGE = 50; // 50m
    private static final int EMERGENCY_WATING_RESPONSE_TIME = 30000; // 30S

    public static final int EMERGENCY_WAITING_USER_RESPONSE = 231;
    public static final int EMERGENCY_WAITING_ALERT_SECONDS = 60;

    /** FUZZY LOGIC **/
    public static final double FUZZY_LOGIC_LOW = 0.5;
    public static final double FUZZY_LOGIC_MEDIUM = 1.0;
    public static final double FUZZY_LOGIC_HIGH = 1.5;

    private static final double ROLLOVER_LOW = 15.0;
    private static final double ROLLOVER_HIGH = 45.0;

    private static final double ACCEL_LOW = 10.0;
    private static final double ACCEL_HIGH = 12.0;

    private static final float DIS_LOW = 40.0f;
    private static final float DIS_HIGH = 20.0f;

    private static Location m_accLocation;
    private static boolean m_bIsAccidentProcessing;
    private static boolean m_bIsDoneEmergencyAlert;
    private static List<ContactItem> m_emergencyContacts;

    private static double m_accidentAccel;
    private static double m_accidentRollover;

    public static void setEmergencycontacts(List<ContactItem> list) {
        m_emergencyContacts = list;
    }

    public static List<ContactItem> getEmergencyContacts() {
        return m_emergencyContacts;
    }

    public static void setAccLocation(Location accLocation) {
        m_accLocation = accLocation;
    }

    public static Location getAccLocation() {
        return m_accLocation;
    }

    public static boolean getAccidentProcessing() { return m_bIsAccidentProcessing; }

    public static void setEmergencyAlertState(boolean IsAlerted) {
        m_bIsDoneEmergencyAlert = IsAlerted;
    }

    public static boolean getEmergencyAlertState() {
        return m_bIsDoneEmergencyAlert;
    }

    public static void startValidationAccident(final ValidateCallback callback) {
        if( m_bIsAccidentProcessing ) { return; }

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                validateLocation(m_accLocation, callback);

            }
        }, EMERGENCY_LOCATION_WATING_TIME);

        m_bIsAccidentProcessing = true;
    }

    public static boolean validateLocation(final Location loc, final ValidateCallback callback) {
//        double distance = curLocation.distanceTo(m_accLocation);
        if( loc.distanceTo(m_accLocation) < EMERGENCY_LOCATION_DISTANCE_RANGE ) {
            try {
                m_bIsAccidentProcessing = false;
                callback.onDone(EMERGENCY_WAITING_USER_RESPONSE);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return true;
        }

        return false;
    }

    public static void getDistanceToAccident(final DistanceCallback callback) {
        if( m_bIsAccidentProcessing ) { return; }

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    callback.onDone(GoogleMapManager.getCurLocation().distanceTo(m_accLocation));
                    m_bIsAccidentProcessing = false;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, EMERGENCY_LOCATION_WATING_TIME);

        m_bIsAccidentProcessing = true;
    }

    // Only use Bluetooth (deprecated)
    public static void startEmergencyProcess(Context context) throws JSONException {
        /** Send SMS to EmergencyContacts **/
        List<ContactItem> contactItems;

        Location accLocation = UserManager.getUser().getUserPosition();

        final String strSMS1 = context.getString(R.string.sms_content) + "\n\n" + AddressManager.getConvertLocationToAddress();
        final String strSMS2 = "https://google.com/maps?q=" + accLocation.getLatitude() + "," + accLocation.getLongitude();

        try {
            contactItems = FileManager.readXmlEmergencyContacts(context);
            for(ContactItem item:
                    contactItems) {
                SMSManager.sendMessage(context, item.getPhoneNumber(), strSMS1);
                SMSManager.sendMessage(context, item.getPhoneNumber(), strSMS2);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        /** Insert data in server **/
//        insertAccidentinServer(UserManager.getUser(), accLocation);
    }

    public static void insertAccidentinServer(Context context, User user, double accel, double rollover,Location accLocation, boolean bIsAlerted)  {
        JSONObject locationObject = new JSONObject();
        try {
            locationObject.put("latitude", accLocation.getLatitude());
            locationObject.put("longitude", accLocation.getLongitude());

            if (HttpManager.useCollection(context.getString(R.string.collection_accident))) {
                JSONObject reqObject = new JSONObject();
                reqObject.put("user_id", user.getUserEmail());
                reqObject.put("riding_type", user.getUserRidingType());
                reqObject.put("has_alerted", bIsAlerted);
                reqObject.put("accel", accel);
                reqObject.put("rollover", rollover);

                Date occDate = new Date();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.KOREA);
                String dateStr = sdf.format(occDate);

                reqObject.put("occured_date", dateStr);
                reqObject.put("position", locationObject);

                HttpManager.requestHttp(reqObject, "", "POST", "", new HttpCallback() {
                    @Override
                    public void onSuccess(JSONArray jsonArray) throws JSONException {
                        Log.d(TAG, "insertAccidentinServer: onSuccess!");
                    }

                    @Override
                    public void onError(String err) throws JSONException {

                    }
                });
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void insertAccidentTestDatainServer(Context context, String deviceName, double rollover, double accel, Date occuredDate) {
        if (HttpManager.useCollection(context.getString(R.string.collection_devicetest))) {

            JSONObject reqObject = new JSONObject();
            try {
                reqObject.put("device_name", deviceName);
                reqObject.put("roll", rollover);
                reqObject.put("accel", accel);

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.KOREA);
                String dateStr = sdf.format(occuredDate);

                reqObject.put("occured_date", dateStr);

                HttpManager.requestHttp(reqObject, "", "POST", "", new HttpCallback() {
                    @Override
                    public void onSuccess(JSONArray jsonArray) throws JSONException {
                        Log.d(TAG, "insertAccidentTestDatainServer: onSuccess!");
                    }

                    @Override
                    public void onError(String err) throws JSONException {

                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public static double getCalcSensorFuzzyLogicResult(double rollover, double accel) {
        double fuzzyRollover;
        double fuzzyAccel;

        if( rollover <= ROLLOVER_LOW ) { fuzzyRollover = FUZZY_LOGIC_LOW; }
        else if( rollover >= ROLLOVER_HIGH ) { fuzzyRollover = FUZZY_LOGIC_HIGH; }
        else  { fuzzyRollover = FUZZY_LOGIC_MEDIUM; }

        if( accel <= ACCEL_LOW ) { fuzzyAccel = FUZZY_LOGIC_LOW; }
        else if( accel >= ACCEL_HIGH ) { fuzzyAccel = FUZZY_LOGIC_HIGH; }
        else  { fuzzyAccel = FUZZY_LOGIC_MEDIUM; }

        return (fuzzyRollover + fuzzyAccel) / 2.0;
    }

    public static double getCalcGPSFuzzyLogicResult(float dis) {
        double fuzzyDistance;

        if( dis >= DIS_LOW ) { fuzzyDistance = FUZZY_LOGIC_LOW; }
        else if( dis <= DIS_HIGH ) { fuzzyDistance = FUZZY_LOGIC_HIGH; }
        else { fuzzyDistance = FUZZY_LOGIC_MEDIUM; }

        return fuzzyDistance;
    }

    public static void setAccidentSensorData(double accel, double rollover) {
        m_accidentAccel = accel;
        m_accidentRollover = rollover;
    }

    public static double getAccidentAccel() {
        return m_accidentAccel;
    }

    public static double getAccidentRolllover() {
        return m_accidentRollover;
    }
}
