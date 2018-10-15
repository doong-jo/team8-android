/*
 * Copyright (c) 10/15/18 1:52 PM
 * Written by Sungdong Jo
 * Description:
 */

package com.helper.helper.interfaces;

import org.json.JSONArray;
import org.json.JSONException;

public interface HttpCallback {
    void onSuccess(JSONArray jsonArray) throws JSONException;
    void onError(String err) throws JSONException;
}
