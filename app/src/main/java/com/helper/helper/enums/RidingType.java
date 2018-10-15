/*
 * Copyright (c) 10/15/18 1:55 PM
 * Written by Sungdong Jo
 * Description:
 */

package com.helper.helper.enums;

public enum RidingType {
    BICYCLE("bicycle"),
    MOTORCYCLE("motorcycle"),
    SMART_MOBILITY("smart_mobility");

    public String value;

    RidingType(String value) {
        this.value = value;
    }
}
