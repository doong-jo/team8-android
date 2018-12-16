package com.helper.helper.controller;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.ArraySet;
import android.util.Log;


import com.helper.helper.model.User;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Set;

public class SharedPreferencer {
    private final static String TAG = SharedPreferencer.class.getSimpleName()+"/DEV";
    public final static String JOINPREFNAME= "JOIN_FORM";
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
    }

    public static void putString(final String key, final String str){
        SharedPreferences.Editor editor= pref.edit();
        editor.putString(key, str);
        editor.apply();
    }

    public static void putInt(final String key, final int num){
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(key, num);
        editor.apply();
    }

    public static void clear(){
        if( pref == null ) { return; }
        SharedPreferences.Editor editor= pref.edit();
        editor.clear();
        editor.apply();
    }

    public static void remove(String key){
        if( pref == null ) { return; }
        SharedPreferences.Editor editor = pref.edit();
        editor.remove(key);
        editor.apply();
    }

}
