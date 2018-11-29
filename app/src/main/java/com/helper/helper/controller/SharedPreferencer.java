package com.helper.helper.controller;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;


import org.json.JSONArray;
import org.json.JSONObject;

public class SharedPreferencer {
    private final static String TAG = SharedPreferencer.class.getSimpleName()+"/DEV";
    private final static int MAX_SEARCH = 5;
    private static SharedPreferences pref;

    public static SharedPreferences getSharedPreferencer(Context context, String name, int mode){
        pref = context.getSharedPreferences(name, mode);
        return pref;
    }

    public static void putJSONObject(final String key, final JSONArray jsonArr){
        SharedPreferences.Editor editor= pref.edit();

        // TODO: 2018-11-25 JSONARRY -> String
        editor.putString(key, jsonArr.toString());

        editor.apply();
        Log.d(TAG, "jsonArr toString: " + jsonArr.toString());
        Log.d(TAG, "pref getAll: " + pref.getAll());
    }
}
