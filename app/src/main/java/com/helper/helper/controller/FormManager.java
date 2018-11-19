/*
 * Copyright (c) 10/16/18 5:14 PM
 * Written by Sungdong Jo
 * Description:
 */

package com.helper.helper.controller;

import android.util.Log;

import com.helper.helper.interfaces.HttpCallback;
import com.helper.helper.model.User;
import org.apache.commons.validator.routines.EmailValidator;
import com.helper.helper.interfaces.ValidateCallback;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FormManager {
    /******************* Result of validation *******************/
    public static final int RESULT_VALIDATION_EMAIL_WRONG = 205;
    public static final int RESULT_VALIDATION_EMAIL_EXIST = 905;

    public static final int RESULT_VALIDATION_PW_WRONG = 316;
    public static final int RESULT_VALIDATION_PW_INCORRECT = 375;

    public static final int RESULT_VALIDATION_NAME_WRONG = 42;
    public static final int RESULT_VALIDATION_NAME_EXIST = 307;

    public static final int RESULT_VALIDATION_ERROR = 909;
    public static final int RESULT_VALIDATION_SUCCESS = 231;
    /************************************************************/


    public static int emailCharValidate(String email) {
        boolean vaildEmail = EmailValidator.getInstance().isValid(email);

        if( !vaildEmail ) {
            /** email wrong **/
            return RESULT_VALIDATION_EMAIL_WRONG;
        } else {
            return RESULT_VALIDATION_SUCCESS;
        }
    }

    public static int passwordCharValidate(String passwd) {
        ///////////////////////////////////////////////////////////////////////////
        //(?=.*[0-9]) a digit must occur at least once
        //(?=.*[a-z]) a lower case letter must occur at least once -> not use
        //(?=.*[A-Z]) an upper case letter must occur at least once -> not use
        //(?=.*[a-zA-Z]) an engilsh letter must occur at least once
        //(?=.*[@#$%^&+=]) a special character must occur at least once -> not use
        //(?=\\S+$) no whitespace allowed in the entire string
        //.{8,} at least 8 characters
        ///////////////////////////////////////////////////////////////////////////
        String pattern = "(?=.*[0-9])(?=.*[a-zA-Z])(?=\\S+$).{8,}";
        return passwd.matches(pattern) ? RESULT_VALIDATION_SUCCESS : RESULT_VALIDATION_PW_WRONG;
    }

    public static int passwordValidate(String passwd) {
        /** Password **/

//        if( !passwd.equals(pwConfirm) ) {
//            /** password incorrect **/
//            return RESULT_VALIDATION_PW_INCORRECT;
//        }

        /** password wrong **/
        return passwordCharValidate(passwd);
    }

    public static boolean nameCharValidate(String name) {
        ///////////////////////////////////////////////////////////////////////////
        //(?=\\S+$) no whitespace allowed in the entire string
        //.{2,} at least 2 characters
        ///////////////////////////////////////////////////////////////////////////
        String pattern = "(?=\\S+$).{2,}";
        return !name.matches(pattern);
    }
}
