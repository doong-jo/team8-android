/*
 * Copyright (c) 10/17/18 2:26 PM
 * Written by Sungdong Jo
 * Description:
 */

package com.helper.helper.controller;


import java.util.regex.Pattern;

public class CommonManager {
    private final static String TAG = CommonManager.class.getSimpleName() + "/DEV";
    private final static String ARRAY_SPLIT_COMMA_REGEX = ",\\s*";

    public static String[] splitNoWhiteSpace(String str) {
        final Pattern p = Pattern.compile(ARRAY_SPLIT_COMMA_REGEX);

        return p.split(str);
    }
}
