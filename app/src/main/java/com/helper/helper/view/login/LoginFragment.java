
/*
 * Copyright (c) 10/11/18 1:19 PM
 * Written by Sungdong Jo
 * Description: LoginActivity > LoginFragment
 *              View of Login Page
 */

package com.helper.helper.view.login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.support.v4.app.Fragment;
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
import com.helper.helper.view.ScrollingActivity;
import com.helper.helper.interfaces.HttpCallback;
import com.helper.helper.controller.HttpManager;
import com.helper.helper.controller.UserManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LoginFragment extends Fragment {
    private final static String TAG = LoginFragment.class.getSimpleName() + "/DEV";

    /******************* Define widgtes in view *******************/
    private EditText m_emailInput;
    private EditText m_pwInput;
    private Button m_loginBtn;
    /**************************************************************/

    public LoginFragment() {

    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
        View view = inflater.inflate( R.layout.fragment_login, container, false );

        /******************* Connect widgtes with layout *******************/
        m_emailInput = view.findViewById(R.id.loginEmailInput);
        m_pwInput = view.findViewById(R.id.loginPwInput);
        m_loginBtn = view.findViewById(R.id.loginBtn);
        /*******************************************************************/

        /******************* Make Listener in View *******************/

        /*************************************************************/

        return view;
    }

    private void tryLogin() {
        View focusView = getActivity().getCurrentFocus();

        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(focusView.getWindowToken(), 0);

        String email = m_emailInput.getText().toString();
        String pw = m_pwInput.getText().toString();

        if(FormManager.emailCharValidate(email) == FormManager.RESULT_VALIDATION_EMAIL_WRONG) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getActivity(), "올바르지 않은 아이디입니다.", Toast.LENGTH_SHORT).show();
                }
            });
            return;
        }

//        if(FormManager.passwordCharValidate(pw) == FormManager.RESULT_VALIDATION_PW_WRONG) {
//            getActivity().runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    Toast.makeText(getActivity(), "올바르지 않은 비밀번호입니다.", Toast.LENGTH_SHORT).show();
//                }
//            });
//            return;
//        }


        if( HttpManager.useCollection("user") ) {
            JSONObject reqObject = new JSONObject();
            try {
                reqObject.put("email", email);
                reqObject.put("passwd", pw);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                HttpManager.requestHttp(reqObject, "GET", new HttpCallback() {
                    @Override
                    public void onSuccess(JSONArray existIdjsonArray) throws JSONException {
                        int arrLen = existIdjsonArray.length();
                        UserManager.setUser(existIdjsonArray.getJSONObject(0));

                        /** account exist **/
                        if( arrLen != 0 ) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            ProgressDialog dialog = ProgressDialog.show(getActivity(), getString(R.string.login_loading_title), getString(R.string.login_loading_message), true);

                                            Intent intent=new Intent(getActivity(),ScrollingActivity.class);
                                            startActivity(intent);
                                        }
                                    }, 1000);
                                }
                            });
                        }
                        /** account not exist **/
                        else {

                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getActivity(), "아이디 혹은 비밀번호가 올바르지 않습니다.", Toast.LENGTH_SHORT).show();
                                }
                            });

                        }
                    }

                    @Override
                    public void onError(String err) {
                        Log.d(TAG, "JoinFragment onError: " + err);
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}