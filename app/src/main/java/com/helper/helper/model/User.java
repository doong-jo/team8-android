/*
 * Copyright (c) 10/11/18 10:32 AM
 * Written by Sungdong Jo
 * Description: User data class
 */

package com.helper.helper.model;

import android.location.Location;

import com.helper.helper.controller.CommonManager;
import com.helper.helper.enums.RidingType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Pattern;

/** Compatible Collection **/
public class User {
    private static final String SPLIT_COMMA_REGEX = ",\\s*";

    private String m_userEmail;
    private String m_userPw;
    private String m_userPhone;
    private String m_userName;
    private String m_userRidingType;
    private Boolean m_userEmergency;
    private Date m_userLastAccess;
    private Location m_lastPosition;
    private ArrayList<String> m_ledIndicies;
    private ArrayList<String> m_ledBookmarked;
    private ArrayList<String> m_trackIndicies;

    /** Not Collection field **/
    private String m_userLEDIndex;

    public static final String KEY_LASTPOSITON = "lastPosition";
    public static final String KEY_LED_INDICIES = "ledIndicies";
    public static final String KEY_LED_BOOKMARKED = "ledBookmarked";
    public static final String KEY_TRACK_INDICIES = "trackIndicies";
    public static final String KEY_EMERGENCY = "emergency";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_PASSWORD = "passwd";
    public static final String KEY_NAME = "name";
    public static final String KEY_PHONE = "phone";
    public static final String KEY_RIDING_TYPE = "riding_type";
    public static final String KEY_LAST_ACCESS = "lastAccess";

    public static class Builder {

        private String m_userEmail;
        private String m_userPw;
        private String m_userPhone;
        private String m_userName;
        private String m_userRidingType;
        private ArrayList<String> m_ledIndicies = new ArrayList<>();
        private ArrayList<String> m_ledBookmarked = new ArrayList<>();

        public Builder() {
            m_userEmail = "";
            m_userPhone = "";
            m_userPw = "";
            m_userPhone = "";
            m_userName = "";
            m_userRidingType = "";
        }
        public Builder email(String emailStr) {
            this.m_userEmail = emailStr;
            return this;
        }

        public Builder pw(String pwStr) {
            this.m_userPw = pwStr;
            return this;
        }

        public Builder phone(String phoneStr) {
            this.m_userPhone = phoneStr;
            return this;
        }

        public Builder name(String nameStr) {
            this.m_userName = nameStr;
            return this;
        }

        public Builder ridingType(String ridingTypeStr) {
            this.m_userRidingType = ridingTypeStr;
            return this;
        }

        public Builder ledIndicies(JSONArray ledIndicies) {
            if (ledIndicies != null) {
                int len = ledIndicies.length();
                for (int i=0; i<len; i++){
                    try {
                        m_ledIndicies.add(ledIndicies.get(i).toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            return this;
        }

        public Builder ledIndicies(String ledIndicies) {

            if( !ledIndicies.contains(",") ) {
                m_ledIndicies.add(ledIndicies);
            } else {
                String[] ledStrArr = ledIndicies.split(", ");

                for (String str :
                        ledStrArr) {
                    m_ledIndicies.add(str);
                }
            }
            return this;
        }

        public Builder ledBookmarked(JSONArray ledBookmarked) {
            if (ledBookmarked != null) {
                int len = ledBookmarked.length();
                for (int i=0; i<len; i++){
                    try {
                        m_ledBookmarked.add(ledBookmarked.get(i).toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            return this;
        }

        public Builder ledBookmarked(String ledBookmarked) {

            if( !ledBookmarked.contains(",") ) {
                m_ledBookmarked.add(ledBookmarked);
            } else {
                String[] ledBookmakredArr = ledBookmarked.split(",");

                for (String str :
                        ledBookmakredArr) {
                    m_ledBookmarked.add(str);
                }
            }
            return this;
        }


        public User build() {
            return new User(this);
        }
    }


    public User(Builder builder) {
        m_userEmail = builder.m_userEmail;
        m_userPw = builder.m_userPw;
        m_userPhone = builder.m_userPhone;
        m_userName = builder.m_userName;
        m_userRidingType = builder.m_userRidingType;
        m_userLastAccess = new Date();
        m_userEmergency = false;
        m_lastPosition = new Location("");
        m_ledIndicies = builder.m_ledIndicies;
        m_ledBookmarked = builder.m_ledBookmarked;
        m_trackIndicies = new ArrayList<String>();
    }

    public void addLEDIndex(String ledIndex) {
        m_ledIndicies.add(ledIndex);
    }

    public void addBookmarkLEDIndex(String ledIndex) { m_ledBookmarked.add(ledIndex); }

    /*
    for (Iterator i = data.iterator(); i.hasNext(); ) {
    Object element = i.next();

    if ((..your conition..)) {
       i.remove();
    }
}
     */
    public void removeLEDIndex(String targetIndex) {
        for (Iterator i = m_ledIndicies.iterator(); i.hasNext(); ) {
            String listOfIndex = (String) i.next();

            if ( targetIndex.equals(listOfIndex) ) {
                i.remove();
            }
        }
    }

    public void removeBookmarkLEDIndex(String targetIndex) {
        for (Iterator i = m_ledBookmarked.iterator(); i.hasNext(); ) {
            String listOfIndex = (String) i.next();

            if ( targetIndex.equals(listOfIndex) ) {
                i.remove();
            }
        }
    }

    public void setUserName(String name) {
        m_userName = name;
    }

    public void setUserEmail(String userEmail) {
        m_userEmail = userEmail;
    }

    public void setUserPassword(String userPassword) { m_userPw = userPassword; }

    public void setUserRidingType(String ridingType) {
        m_userRidingType = ridingType;
    }

    public void setLEDIndex(String ledIndex) {
        m_userLEDIndex = ledIndex;
    }

    public String getUserEmail() {
        return m_userEmail;
    }

    public String getUserPw() {
        return m_userPw;
    }

    public String getUserRidingType() { return m_userRidingType; }

    public String getUserPhone() {
        return m_userPhone;
    }

    public String getUserLEDIndicies() {
        return m_ledIndicies.toString();
    }

    public String getUserBookmarked() {
        return m_ledBookmarked.toString();
    }

    public ArrayList<String> getUserLEDArray() {
        return m_ledIndicies;
    }

    public String[] getUserLEDIndiciesURI(String baseUri) {
        String pureStr = getUserLEDIndicies();
        String ledStr = pureStr.split("\\[")[1].split("]")[0];

        String[] ledArrStr = CommonManager.splitNoWhiteSpace(ledStr);
        String[] resultArr = new String[ledArrStr.length*2];

        int cnt = 0;
        for (int i = 0; i < ledArrStr.length*2; i+=2) {
            resultArr[i] =  baseUri + "/images/LED/" + ledArrStr[cnt] + ".png";
            resultArr[i+1] = baseUri + "/images/LED/" + ledArrStr[cnt] + ".gif";
            cnt++;
        }

        return resultArr;
    }

    public String getUserName() { return m_userName; }

    public Location getUserPosition() { return m_lastPosition; }

    /** User LoginActivity **/
    public JSONObject getTransformUserToJSON() {
        JSONObject obj = new JSONObject();

        try {
            if( !m_userEmail.equals("")) {
                obj.put("email", m_userEmail);
            }
            if( !m_userPw.equals("")) {
                obj.put("passwd", m_userPw);
            }
            if( !m_userName.equals("") ) {
                obj.put("name", m_userName);
            }
            if( !m_userPhone.equals("")) {
                obj.put("phone", m_userPhone);
            }
            if( !m_userRidingType.equals("")) {
                obj.put("riding_type", m_userRidingType);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return obj;
    }

    public String getUserLEDIndex() {
        return m_userLEDIndex;
    }
}
