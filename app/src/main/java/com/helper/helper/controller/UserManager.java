/*
 * Copyright (c) 10/15/18 1:50 PM
 * Written by Sungdong Jo
 * Description:
 */

package com.helper.helper.controller;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import com.helper.helper.R;
import com.helper.helper.enums.Collection;
import com.helper.helper.interfaces.HttpCallback;
import com.helper.helper.interfaces.ValidateCallback;
import com.helper.helper.model.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class UserManager {
    private final static String TAG = UserManager.class.getSimpleName() + "/DEV";
    private static Bitmap m_userProfileBitmap;
    private static User m_user;
    private static ImageView m_userLEDcurShowOn;

    public static int DONE_SET_USER = 1;

    public static User getUser() { return m_user; }

    public static void setUser(User user) {
        m_user = user;
    }

    public static void setUser(User user, ValidateCallback callback) {
        m_user = user;

        try {
            callback.onDone(DONE_SET_USER);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void setUser(final JSONObject jsonObject) throws JSONException {
        m_user = new User.Builder()
                .email(jsonObject.getString("email"))
                .name(jsonObject.getString("name"))
                .phone(jsonObject.getString("phone"))
                .ridingType(jsonObject.getString("riding_type"))
                .ledIndicies(jsonObject.getJSONArray("ledIndicies"))
                .build();
    }

    public static void setUserName(String name) { m_user.setUserName(name); }

    public static void setUserEmail(String email) { m_user.setUserEmail(email); }

    public static void setUserPassword(String pw) { m_user.setUserPassword(pw); }

    public static void setRidingType(String ridingType) { m_user.setUserRidingType(ridingType); }

    public static void setUserProfileBitmap(Bitmap bitmap) { m_userProfileBitmap = bitmap; }

    public static void setUserLEDDeviceShowOnThumb(ImageView view ) {
        m_userLEDcurShowOn = view;
    }

    public static void setUserLEDcurShowOn(Context context, String ledIndex) {
        m_user.setLEDIndex(ledIndex);

        if ( m_userLEDcurShowOn != null ) {
            File f=new File(
                    CommonManager.getOpenLEDFilePath(
                        context,
                        ledIndex,
                        context.getString(R.string.gif_format))
            );
            try {
                Bitmap imageBitmap = BitmapFactory.decodeStream(new FileInputStream(f));
                UserManager.getuserLEDcurShowOn().setImageBitmap(imageBitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public static String getUserEmail() {
        return m_user.getUserEmail();
    }

    public static String getUserName() {
        return m_user.getUserName();
    }

    public static String getUserPassword() { return m_user.getUserPw(); }

    public static String getUserPhone() {
        return m_user.getUserPhone();
    }

    public static String getUserRidingType() {
        return m_user.getUserRidingType();
    }

    public static Bitmap getUserProfileBitmap() {
        return m_userProfileBitmap;
    }

    public static ImageView getuserLEDcurShowOn() {
        return m_userLEDcurShowOn;
    }

    public static void updateUserInfoServerAndXml(Context context) {
        User curUser = UserManager.getUser();
        try {
            FileManager.writeXmlUserInfo(context, curUser);
        } catch (IOException e) {
            e.printStackTrace();
        }

        JSONObject jsonQuery = new JSONObject();
        try {
            jsonQuery.put("email", curUser.getUserEmail());
            jsonQuery.put("ledIndicies", curUser.getUserLEDIndicies());
        } catch (JSONException e) {
            e.printStackTrace();
        }
//        jsonQuery.put("ledIndicies", UserManager.getUser().getled);

        if ( HttpManager.useCollection(context.getString(R.string.collection_user)) ) {
            try {
                HttpManager.requestHttp(jsonQuery, "email", "PUT", "", new HttpCallback() {
                    @Override
                    public void onSuccess(JSONArray jsonArray) throws JSONException {
                        int a = 1;
//                        jsonArray
                    }

                    @Override
                    public void onError(String err) throws JSONException {
//                        err
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
