/*
 * Copyright (c) 10/12/18 11:29 PM
 * Written by Sungdong Jo
 * Description:
 */

package com.helper.helper.util;

import org.json.JSONArray;
import org.json.JSONException;

public interface HttpCallback {
    void onSuccess(JSONArray jsonArray) throws JSONException;
    void onError(String err) throws JSONException;
}
