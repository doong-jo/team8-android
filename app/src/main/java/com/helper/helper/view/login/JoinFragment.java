
/*
 * Created by Sungdong Jo on 10/9/18 5:02 PM
 * Copyright (c) 2018 . All rights reserved.
 * Description: LoginActivity > JoinFragment
 *              View of join page
 */

package com.helper.helper.view.login;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatCheckBox;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.helper.helper.R;
import com.helper.helper.controller.FormManager;
import com.helper.helper.interfaces.ValidateCallback;
import com.helper.helper.view.ScrollingActivity;
import com.helper.helper.model.User;
import com.helper.helper.interfaces.HttpCallback;
import com.helper.helper.controller.HttpManager;
import com.helper.helper.controller.PermissionManager;

import org.apache.commons.validator.routines.EmailValidator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JoinFragment extends Fragment {
    private final static String TAG = JoinFragment.class.getSimpleName() + "/DEV";

    /******************* Define widgtes in view *******************/
//    private EditText m_emailInput;
//    private EditText m_pwInput;
//    private EditText m_pwConfirmInput;
//    private EditText m_nameInput;
//    private AppCompatCheckBox m_termChkBox;
    /**************************************************************/

    public JoinFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_join, container, false);


        /******************* Connect widgtes with layout *******************/
//        Button joinBtn = view.findViewById(R.id.joinBtn);
//        m_emailInput = view.findViewById(R.id.emailInput);
//        m_pwInput = view.findViewById(R.id.pwInput);
//        m_pwConfirmInput = view.findViewById(R.id.pwConfirmInput);
//        m_nameInput = view.findViewById(R.id.nameInput);
//        m_termChkBox = view.findViewById(R.id.termChkBox);
        /*******************************************************************/

        /******************* Make Listener in View *******************/
//        joinBtn.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                tryJoin();
//            }
//        });
//
//        m_nameInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
//                if(actionId == EditorInfo.IME_ACTION_DONE) {
//                    tryJoin();
//                    return true;
//                }
//                return false;
//            }
//        });
        /*************************************************************/

        return view;
    }

//    private void tryJoin() {
//        View focusView = getActivity().getCurrentFocus();
//
//        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
//        imm.hideSoftInputFromWindow(focusView.getWindowToken(), 0);
//
//        String email = m_emailInput.getText().toString();
//        String pw = m_pwInput.getText().toString();
//        String pwConfirm = m_pwConfirmInput.getText().toString();
//        String name = m_nameInput.getText().toString();
//        String phone = "";
//
//        TelephonyManager tMgr = (TelephonyManager) getActivity().getSystemService(getActivity().TELEPHONY_SERVICE);
//
//        PermissionManager.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_PHONE_STATE}, PermissionManager.READ_PHONE_STATE);
//
//        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED
//                && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED &&
//                ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
//            Toast.makeText(getActivity(), "디바이스 정보를 알 수 없습니다. 허용 권한을 확인해주세요.", Toast.LENGTH_SHORT).show();
//            return;
//        } else {
//            phone = tMgr.getLine1Number();
//        }
//
//        final User joinUser = new User.Builder()
//                .email(email)
//                .pw(pw)
//                .name(name)
//                .phone(phone)
//                .build();
//
//        try {
//            validateJoinForm(joinUser, pwConfirm, new ValidateCallback() {
//                @Override
//                public void onDone(int resultCode) throws JSONException {
//                    if( resultCode == FormManager.RESULT_VALIDATION_SUCCESS && m_termChkBox.isChecked() ) {
//                        getActivity().runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                Toast.makeText(getActivity(), "가입 유효성 검사 통과!", Toast.LENGTH_SHORT).show();
//                            }
//                        });
//
//                        InsertUserinServer(joinUser, new ValidateCallback() {
//                            @Override
//                            public void onDone(int resultCode) {
//                                if( resultCode == FormManager.RESULT_VALIDATION_SUCCESS ) {
//                                    getActivity().runOnUiThread(new Runnable() {
//                                        @Override
//                                        public void run() {
//                                            new Handler().postDelayed(new Runnable() {
//                                                @Override
//                                                public void run() {
//                                                    ProgressDialog dialog = ProgressDialog.show(getActivity(), getString(R.string.join_loading_title), getString(R.string.join_loading_message), true);
//
//                                                    Intent intent = new Intent(getActivity(), ScrollingActivity.class);
//                                                    startActivity(intent);
//                                                }
//                                            }, 1000);
//                                        }
//                                    });
//                                } else {
//                                    getActivity().runOnUiThread(new Runnable() {
//                                        @Override
//                                        public void run() {
//                                            Toast.makeText(getActivity(), "서버 전송 실패.", Toast.LENGTH_SHORT).show();
//                                        }
//                                    });
//                                }
//                            }
//                        });
//                    } else if( resultCode == FormManager.RESULT_VALIDATION_SUCCESS && !m_termChkBox.isChecked() ) {
//                        getActivity().runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                Toast.makeText(getActivity(), "이용약관에 동의해주세요.", Toast.LENGTH_SHORT).show();
//                            }
//                        });
//                    } else {
//                        getActivity().runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                Toast.makeText(getActivity(), "입력을 다시 확인해주세요.", Toast.LENGTH_SHORT).show();
//                            }
//                        });
//                    }
//                }
//            });
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    };

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

        if( FormManager.emailCharValidate(email) != FormManager.RESULT_VALIDATION_SUCCESS ) {
            return;
        }

        /** Find exist an email **/
        if( HttpManager.useCollection("user") ) {
            JSONObject reqObject = new JSONObject();
            reqObject.put("email", email);

            HttpManager.requestHttp(reqObject, "GET", new HttpCallback() {
                @Override
                public void onSuccess(JSONArray existIdjsonArray) throws JSONException {
                    int arrLen = existIdjsonArray.length();
                    if( arrLen == 0 ) {
                        /** Validate password **/
                        int passwdResult = FormManager.passwordValidate(passwd, pwConfirm);
                        if(passwdResult != FormManager.RESULT_VALIDATION_SUCCESS) {
                            callback.onDone(passwdResult);
                        } else {
                            FormManager.nameValidate(name, new ValidateCallback() {
                                @Override
                                public void onDone(int resultCode) throws JSONException {
                                    callback.onDone(resultCode);
                                }
                            });
                        }
                    } else {
                        /** email exist **/
                        callback.onDone(FormManager.RESULT_VALIDATION_EMAIL_EXIST);
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
        if( HttpManager.useCollection("user") ) {

            JSONObject reqObject = user.getTransformUserToJSON();
//            reqObject.put("lastAccess", new Date().toString());

            HttpManager.requestHttp(reqObject, "POST", new HttpCallback() {
                @Override
                public void onSuccess(JSONArray jsonArray) throws JSONException {
                    JSONObject resultObj = (JSONObject)jsonArray.get(0);
                    boolean result = resultObj.getBoolean("result");
                    if( result ) {
                        callback.onDone(FormManager.RESULT_VALIDATION_SUCCESS);
                    } else {
                        callback.onDone(FormManager.RESULT_VALIDATION_ERROR);
                    }
                }

                @Override
                public void onError(String err) {

                }
            });
        }
    }

//    public void createUser(User formUser) {
//        return
//                new User.Builder()
//    }
}
