
/*
 * Created by Sungdong Jo on 10/9/18 5:02 PM
 * Copyright (c) 2018 . All rights reserved.
 * Description: LoginActivity > JoinFragment
 *              View of join page
 */

package com.helper.helper.login;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatCheckBox;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.helper.helper.R;
import com.helper.helper.ScrollingActivity;
import com.helper.helper.data.User;
import com.helper.helper.util.HttpCallback;
import com.helper.helper.util.HttpManagerUtil;
import com.helper.helper.util.PermissionUtil;
import com.helper.helper.util.UserManagerUtil;
import org.apache.commons.validator.routines.EmailValidator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

interface ValidateCallback {
    void onDone(int resultCode) throws JSONException;
}

public class JoinFragment extends Fragment {
    private final static String TAG = JoinFragment.class.getSimpleName() + "/DEV";

    /******************* Define widgtes in view *******************/
    private EditText m_emailInput;
    private EditText m_pwInput;
    private EditText m_pwConfirmInput;
    private EditText m_nameInput;
    private AppCompatCheckBox m_termChkBox;
    /**************************************************************/

    /******************* Result of validation *******************/
    private static final int RESULT_VALIDATION_EMAIL_WRONG = 205;
    private static final int RESULT_VALIDATION_EMAIL_EXIST = 905;

    private static final int RESULT_VALIDATION_PW_WRONG = 316;
    private static final int RESULT_VALIDATION_PW_INCORRECT = 375;

    private static final int RESULT_VALIDATION_NAME_WRONG = 42;
    private static final int RESULT_VALIDATION_NAME_EXIST = 307;

    private static final int RESULT_VALIDATION_ERROR = 909;
    private static final int RESULT_VALIDATION_SUCCESS = 231;
    /************************************************************/



    public JoinFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_join, container, false);


        /******************* Define widgtes in view *******************/
        Button joinBtn = view.findViewById(R.id.joinBtn);
        m_emailInput = view.findViewById(R.id.emailInput);
        m_pwInput = view.findViewById(R.id.pwInput);
        m_pwConfirmInput = view.findViewById(R.id.pwConfirmInput);
        m_nameInput = view.findViewById(R.id.nameInput);
        m_termChkBox = view.findViewById(R.id.termChkBox);
        /**************************************************************/


        /******************* Make Listener in View *******************/
        OnClickListener makeTryJoinListener = makeTryJoinListener();

        joinBtn.setOnClickListener(makeTryJoinListener);
        /*************************************************************/

        return view;
    }

    private OnClickListener makeTryJoinListener() {
        return
            new OnClickListener() {
                @Override
                public void onClick(View view) {
                    String email = m_emailInput.getText().toString();
                    String pw = m_pwInput.getText().toString();
                    String pwConfirm = m_pwConfirmInput.getText().toString();
                    String name = m_nameInput.getText().toString();
                    String phone = "";

                    TelephonyManager tMgr = (TelephonyManager) getActivity().getSystemService(getActivity().TELEPHONY_SERVICE);

                    PermissionUtil.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_PHONE_STATE}, PermissionUtil.READ_PHONE_STATE);

                    if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(getActivity(), "디바이스 정보를 알 수 없습니다. 허용 권한을 확인해주세요.", Toast.LENGTH_SHORT).show();
                        return;
                    } else {
                        phone = tMgr.getLine1Number();
                    }

                    final User joinUser = new User.Builder()
                            .email(email)
                            .pw(pw)
                            .name(name)
                            .phone(phone)
                            .build();

                    try {
                        validateJoinForm(joinUser, pwConfirm, new ValidateCallback() {
                            @Override
                            public void onDone(int resultCode) throws JSONException {
                                if( resultCode == RESULT_VALIDATION_SUCCESS && m_termChkBox.isChecked() ) {
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(getActivity(), "가입 유효성 검사 통과!", Toast.LENGTH_SHORT).show();
                                        }
                                    });

                                    InsertUserinServer(joinUser, new ValidateCallback() {
                                        @Override
                                        public void onDone(int resultCode) {
                                            if( resultCode == RESULT_VALIDATION_SUCCESS ) {
                                                Intent intent=new Intent(getActivity(),ScrollingActivity.class);
                                                startActivity(intent);
                                            } else {
                                                getActivity().runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Toast.makeText(getActivity(), "서버 전송 실패.", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            }
                                        }
                                    });
                                } else if( resultCode == RESULT_VALIDATION_SUCCESS && !m_termChkBox.isChecked() ) {
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(getActivity(), "이용약관에 동의해주세요.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                } else {
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(getActivity(), "입력을 다시 확인해주세요.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            };
    }

    private int emailCharValidate(String email) {
        boolean vaildEmail = EmailValidator.getInstance().isValid(email);

        if( !vaildEmail ) {
            /** email wrong **/
            return RESULT_VALIDATION_EMAIL_WRONG;
        } else {
            return RESULT_VALIDATION_SUCCESS;
        }
    }

    private boolean passwordCharValidate(String passwd) {
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
        return passwd.matches(pattern);
    }

    private int passwordValidate(String passwd, String pwConfirm) {
        /** Password **/

        if( !passwd.equals(pwConfirm) ) {
            /** password incorrect **/
            return RESULT_VALIDATION_PW_INCORRECT;
        }

        if( !passwordCharValidate(passwd) ) {
            /** password wrong **/
            return RESULT_VALIDATION_PW_WRONG;
        }

        return RESULT_VALIDATION_SUCCESS;
    }

    private boolean nameCharValidate(String name) {
        ///////////////////////////////////////////////////////////////////////////
        //(?=\\S+$) no whitespace allowed in the entire string
        //.{2,} at least 2 characters
        ///////////////////////////////////////////////////////////////////////////
        String pattern = "(?=\\S+$).{2,}";
        return !name.matches(pattern);
    }

    private void nameValidate(String name, final ValidateCallback callback) throws JSONException {
        if( nameCharValidate(name) ) {
            /** name wrong **/
            callback.onDone(RESULT_VALIDATION_NAME_WRONG);
        } else {
            if( HttpManagerUtil.useCollection("user") ) {
                JSONObject reqObject = new JSONObject();
                reqObject.put("name", name);
                HttpManagerUtil.requestHttp(reqObject, "GET", new HttpCallback() {

                    @Override
                    public void onSuccess(JSONArray jsonArray) throws JSONException {
                        int arrLen = jsonArray.length();

                        if( arrLen > 0 ) {
                            /** name exist **/
                            callback.onDone(RESULT_VALIDATION_NAME_EXIST);
                        } else {
                            callback.onDone(RESULT_VALIDATION_SUCCESS);
                        }
                    }

                    @Override
                    public void onError(String err) throws JSONException {
                        callback.onDone(RESULT_VALIDATION_NAME_WRONG);
                    }
                });
            }
        }
    }

    public void validateJoinForm(User user, final String pwConfirm, final ValidateCallback callback) throws JSONException {
        /** Email **/
        final String email = user.getUserEmail();
        final String passwd = user.getUserPw();
        final String name = user.getUserName();

        /** Description :
         *  Validate Flow : Validate Email(has httpCallback) {
         *      Validate Password ? Validate Name(has httpCallback) { return result } : return fail
         *  }
         */

        if( emailCharValidate(email) != RESULT_VALIDATION_SUCCESS ) {
            return;
        }

        /** Find exist an email **/
        if( HttpManagerUtil.useCollection("user") ) {
            JSONObject reqObject = new JSONObject();
            reqObject.put("email", email);

            HttpManagerUtil.requestHttp(reqObject, "GET", new HttpCallback() {
                @Override
                public void onSuccess(JSONArray existIdjsonArray) throws JSONException {
                    int arrLen = existIdjsonArray.length();
                    if( arrLen == 0 ) {
                        /** Validate password **/
                        int passwdResult = passwordValidate(passwd, pwConfirm);
                        if(passwdResult != RESULT_VALIDATION_SUCCESS) {
                            callback.onDone(passwdResult);
                        } else {
                            nameValidate(name, new ValidateCallback() {
                                @Override
                                public void onDone(int resultCode) throws JSONException {
                                    callback.onDone(resultCode);
                                }
                            });
                        }
                    } else {
                        /** email exist **/
                        callback.onDone(RESULT_VALIDATION_EMAIL_EXIST);
                    }
                }

                @Override
                public void onError(String err) {
                    Log.d(TAG, "JoinFragment onError: " + err);
                }
            });
        }
    }

    public void InsertUserinServer(User user, final ValidateCallback callback) throws JSONException {
        if( HttpManagerUtil.useCollection("user") ) {

            JSONObject reqObject = new JSONObject();
            reqObject.put("email", user.getUserEmail());
            reqObject.put("passwd", user.getUserPw());
            reqObject.put("name", user.getUserEmail());
            reqObject.put("phone", user.getUserPhone());
//            reqObject.put("lastAccess", new Date().toString());

            HttpManagerUtil.requestHttp(reqObject, "POST", new HttpCallback() {
                @Override
                public void onSuccess(JSONArray jsonArray) throws JSONException {
                    if( jsonArray.get(0) == "1" ) {
                        callback.onDone(RESULT_VALIDATION_SUCCESS);
                    } else {
                        callback.onDone(RESULT_VALIDATION_ERROR);
                    }
                }

                @Override
                public void onError(String err) {

                }
            });
        }
    }

    public void createUser(User formUser) {
        return
                new User.Builder()
    }
}
