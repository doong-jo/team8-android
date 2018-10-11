/*
 * Copyright (c) 10/11/18 10:32 AM
 * Written by Sungdong Jo
 * Description: User data class
 */

package com.helper.helper.data;

import android.location.Location;

import java.sql.Date;
import java.util.ArrayList;

public class User {
    private String m_userEmail;
    private String m_userPw;
    private String m_userPhone;
    private RidingType m_userRidingType;
    private String m_userEmergency;
    private Date m_userLastAccess;
    private ArrayList<Location> m_lastPosition;
    private ArrayList<Location> m_accPosition;
    private ArrayList<String> m_ledIndicies;
    private ArrayList<String> m_trackIndicies;

    public User(String userEmail, String userPw, String userPhone) {
        m_userEmail = userEmail;
        m_userPw = userPw;
        m_userPhone = userPhone;
    }

    public String getUserEmail() {

        return m_userEmail;
    }

    public String getUserPw() {
        return m_userPw;
    }

    public String getUserPhone() {
        return m_userPhone;
    }


}
