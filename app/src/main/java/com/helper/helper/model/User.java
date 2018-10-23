/*
 * Copyright (c) 10/11/18 10:32 AM
 * Written by Sungdong Jo
 * Description: User data class
 */

package com.helper.helper.model;

import android.location.Location;

import com.helper.helper.enums.RidingType;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.ArrayList;

public class User {
    private String m_userEmail;
    private String m_userPw;
    private String m_userPhone;
    private String m_userName;
    private String m_userRidingType;
    private Boolean m_userEmergency;
    private Date m_userLastAccess;
    private Location m_lastPosition;
    private ArrayList<Location> m_accPosition;
    private ArrayList<String> m_ledIndicies;
    private ArrayList<String> m_trackIndicies;

    public static class Builder {

        private String m_userEmail;
        private String m_userPw;
        private String m_userPhone;
        private String m_userName;

        public Builder() {
            m_userEmail = "";
            m_userPhone = "";
            m_userPw = "";
            m_userPhone = "";
            m_userName = "";
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

        public User build() {
            return new User(this);
        }
    }

    public User(Builder builder) {
        m_userEmail = builder.m_userEmail;
        m_userPw = builder.m_userPw;
        m_userPhone = builder.m_userPhone;
        m_userName = builder.m_userName;
        m_userRidingType = "";
        m_userLastAccess = new Date();
        m_userEmergency = false;
        m_lastPosition = new Location("");
        m_accPosition = new ArrayList<Location>();
        m_ledIndicies = new ArrayList<String>();
        m_trackIndicies = new ArrayList<String>();
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

    public String getUserName() { return m_userName; }

    public Location getUserPosition() { return m_lastPosition; }

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
            if( !m_userName.equals("")) {
                obj.put("name", m_userPhone);
            }
            if( !m_userRidingType.equals("")) {
                obj.put("riding_type", m_userRidingType);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return obj;
    }
}
