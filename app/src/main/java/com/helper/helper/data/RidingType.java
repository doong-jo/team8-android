/*
 * Copyright (c) 10/11/18 10:49 AM
 * Written by Sungdong Jo
 * Description:
 */

package com.helper.helper.data;

public enum RidingType {
    BICYCLE("bicycle"),
    MOTORCYCLE("motorcycle"),
    SMART_MOBILITY("smart_mobility");

    private String value;

    RidingType(String value) {
        this.value = value;
    }
}
