

/*
 * Copyright (c) 10/10/18 5:06 PM
 * Written by Sungdong Jo
 * Description: Save user Information
 */

package com.helper.helper.util;

import android.util.Log;

import com.helper.helper.data.User;

public class UserManagerUtil {
    private final static String TAG = UserManagerUtil.class.getSimpleName() + "/DEV";
    private static String m_userId;
    private static String m_userEmail;

    public static void setUserId(String userId) {
        m_userId = userId;
    }

    public static String getUserId() {
        return m_userId;
    }

    public static void setUserEmail(String userEmail) {
        m_userEmail = userEmail;
    }

    public static String getUserEmail() {
        return m_userEmail;
    }

    public static void doLogin(User user) {
        String userId = user.getUserEmail();
        String userPw = user.getUserPw();

//        String response = HttpManagerUtil.requestHttp("user?id="+userId+"&passwd="+userPw, "GET");

        Log.d(TAG, "doLogin: ");
    }

    public static void doLogout() {

    }

    public static void doJoin(User user) {
//        HttpManagerUtil.requestHttp(
//                "user?email="+user.getUserEmail()
//                +"&passwd="+user.getUserPw()
//                +"&phone="+user.getUserPhone()
//                +"&name"+user.getUserName(),
//                "POST"
//        );
    }
}
