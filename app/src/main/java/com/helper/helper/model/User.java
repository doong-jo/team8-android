/*
 * Copyright (c) 10/11/18 10:32 AM
 * Written by Sungdong Jo
 * Description: User data class
 */

package com.helper.helper.model;

import android.location.Location;

import com.helper.helper.controller.CommonManager;
import com.helper.helper.enums.RidingType;
import com.helper.helper.view.accident.ThresholdActivity;

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
    private String m_userName;
    private String m_userRidingType;
    private Boolean m_userAccEnabled;
    private String m_userAccLevel;
    private Location m_lastPosition;
    private ArrayList<String> m_ledIndicies;
    private ArrayList<String> m_ledBookmarked;

    /** Not Collection field **/
    private String m_userLEDIndex;

    public static final String KEY_LED_INDICIES = "ledIndicies";
    public static final String KEY_LED_BOOKMARKED = "ledBookmarked";
    public static final String KEY_EMERGENCY = "emergency";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_PASSWORD = "passwd";
    public static final String KEY_NAME = "name";
    public static final String KEY_RIDING_TYPE = "riding_type";
    public static final String KEY_LAST_ACCESS = "lastAccess";
    public static final String KEY_ACC_ENABLED = "acc_enabled";
    public static final String KEY_ACC_LEVEL = "acc_level";

    public static class Builder {

        private String m_buildEmail;
        private String m_buildPw;
        private String m_buildName;
        private String m_buildRidingType;
        private Boolean m_buildAccEnabled;
        private String m_buildAccLevel;
        private ArrayList<String> m_buildLEDIndicies = new ArrayList<>();
        private ArrayList<String> m_buildLEDBookmarked = new ArrayList<>();

        public Builder() {
            m_buildEmail = "";
            m_buildPw = "";
            m_buildName = "";
            m_buildRidingType = "";
            m_buildAccEnabled = false;
            m_buildAccLevel = ThresholdActivity.HIGH_LEVEL;
        }
        public Builder email(String emailStr) {
            this.m_buildEmail = emailStr;
            return this;
        }

        public Builder pw(String pwStr) {
            this.m_buildPw = pwStr;
            return this;
        }

        public Builder name(String nameStr) {
            this.m_buildName = nameStr;
            return this;
        }

        public Builder ridingType(String ridingTypeStr) {
            this.m_buildRidingType = ridingTypeStr;
            return this;
        }

        public Builder accEnabled(String accEnabledStr) {
            this.m_buildAccEnabled = Boolean.valueOf(accEnabledStr);
            return this;
        }

        public Builder accLevel(String accLevelStr) {
            this.m_buildAccLevel = accLevelStr;
            return this;
        }

        public Builder ledIndicies(JSONArray ledIndicies) {
            if (ledIndicies != null) {
                int len = ledIndicies.length();
                for (int i=0; i<len; i++){
                    try {
                        m_buildLEDIndicies.add(ledIndicies.get(i).toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            return this;
        }

        public Builder ledBookmarked(JSONArray ledBookmarked) {
            if (ledBookmarked != null) {
                int len = ledBookmarked.length();
                for (int i=0; i<len; i++){
                    try {
                        m_buildLEDBookmarked.add(ledBookmarked.get(i).toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            return this;
        }

        public User build() {
            return new User(this);
        }
    }


    public User(Builder builder) {
        m_userEmail = builder.m_buildEmail;
        m_userPw = builder.m_buildPw;
        m_userName = builder.m_buildName;
        m_userRidingType = builder.m_buildRidingType;
        m_lastPosition = new Location("");
        m_ledIndicies = builder.m_buildLEDIndicies;
        m_ledBookmarked = builder.m_buildLEDBookmarked;
        m_userAccEnabled = builder.m_buildAccEnabled;
        m_userAccLevel = builder.m_buildAccLevel;
    }

    public void addBookmarkLEDIndex(String ledIndex) { m_ledBookmarked.add(ledIndex); }

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

    public void setUserAccEnabled(Boolean enabled) {
        m_userAccEnabled = enabled;
    }

    public void setUserAccLevel(String level) { m_userAccLevel = level; }

    public String getUserEmail() {
        return m_userEmail;
    }

    public String getUserPw() {
        return m_userPw;
    }

    public String getUserRidingType() { return m_userRidingType; }

    public Boolean getUserAccEnabled() { return m_userAccEnabled; }

    public String getUserAccLevel() { return m_userAccLevel; }

    public String getUserLEDIndicies() {
        return m_ledIndicies.toString();
    }

    public int getUserLEDIndiciesSize() {
        return m_ledIndicies.size();
    }

    public String getUserBookmarked() {
        return m_ledBookmarked.toString();
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
                obj.put(KEY_EMAIL, m_userEmail);
            }
            if( !m_userName.equals("") ) {
                obj.put(KEY_NAME, m_userName);
            }
            if( !m_userRidingType.equals("")) {
                obj.put(KEY_RIDING_TYPE, m_userRidingType);
            }
            if( !m_userPw.equals("")) {
                obj.put(KEY_PASSWORD, m_userPw);
            }

            obj.put(KEY_ACC_ENABLED, true);
            obj.put(KEY_ACC_LEVEL, ThresholdActivity.HIGH_LEVEL);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return obj;
    }
}
