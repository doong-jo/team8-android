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
    private static final int EMERGENCY_LOCATION_WATING_TIME = 10000; // 4min 240000
    private static final int EMERGENCY_LOCATION_DISTANCE_RANGE = 50; // 50m
    private static final int EMERGENCY_WATING_RESPONSE_TIME = 30000; // 30S

    public static final int EMERGENCY_VALIDATE_LOCATION_WAITNG_FINISH = 901;
    public static final int EMERGENCY_WAITING_USER_RESPONSE = 231;

    private static Location m_accLocation;
    private static boolean m_bIsAccidentProcessing;
    private static List<ContactItem> m_emergencyContacts;


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

    public static void startValidationAccident(final ValidateCallback callback) {
        if( m_bIsAccidentProcessing ) { return; }

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    callback.onDone(EMERGENCY_VALIDATE_LOCATION_WAITNG_FINISH);
                    m_bIsAccidentProcessing = false;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, EMERGENCY_LOCATION_WATING_TIME);

        m_bIsAccidentProcessing = true;
    }

    public static void startWaitingUserResponse(final ValidateCallback callback) {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    callback.onDone(EMERGENCY_WAITING_USER_RESPONSE);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, EMERGENCY_WATING_RESPONSE_TIME);
    }

    public static boolean validateLocation(Location curLocation) {
        double distance = curLocation.distanceTo(m_accLocation);
        if( curLocation.distanceTo(m_accLocation) < EMERGENCY_LOCATION_DISTANCE_RANGE ) {
            return true;
        }

        return false;
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

    public static void insertAccidentinServer(Context context, User user, Location accLocation, boolean bIsAlerted) throws JSONException {
        JSONObject locationObject = new JSONObject();
        locationObject.put("latitude", accLocation.getLatitude());
        locationObject.put("longitude", accLocation.getLongitude());

        if (HttpManager.useCollection(context.getString(R.string.collection_accident))) {
            JSONObject reqObject = new JSONObject();
            reqObject.put("user_id", user.getUserEmail());
            reqObject.put("riding_type", user.getUserRidingType());
            reqObject.put("has_alerted", bIsAlerted);

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
}
