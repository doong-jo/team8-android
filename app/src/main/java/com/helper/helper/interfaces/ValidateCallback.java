/*
 * Copyright (c) 10/16/18 5:16 PM
 * Written by Sungdong Jo
 * Description:
 */

package com.helper.helper.interfaces;

import org.json.JSONException;

public interface ValidateCallback {
    void onDone(int resultCode) throws JSONException;
}
