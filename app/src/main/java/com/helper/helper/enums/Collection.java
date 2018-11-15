/*
 * Copyright (c) 10/15/18 2:31 PM
 * Written by Sungdong Jo
 * Description:
 */

package com.helper.helper.enums;

public enum Collection {
    USER("user"),
    ACCIDENT("accident"),
    LED("led"),
    TRACKING("tracking"),
    CATEGORY("category");

    private String value;

    Collection(String value) {
        this.value = value;
    }

    public String getValue() { return value; }
}