
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
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
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
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ahmadrosid.library.FloatingLabelEditText;
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
    private static final int EDITTEXT_CONTROL_CLEAR = 229;
    private static final int EDITTEXT_CONTROL_CHECK = 230;

    /******************* Define widgtes in view *******************/
    private FloatingLabelEditText m_emailInput;
    private FloatingLabelEditText m_pwInput;
    private Button m_loginBtn;
    private Button m_emailInputClear;
    private Button m_pwInputClear;
    private OnClickListener m_emailInputClearClickListener;
    private OnClickListener m_pwInputClearClickListener;

    private static final int MAX_EMAIL_LENGTH = 40;
    private static final int MAX_PW_LENGTH = 15;

    // save original pixel(after convert dp) of editText control marginEnd
    private int m_editTextControlMarginEnd;
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
        m_emailInputClear = view.findViewById(R.id.loginEmailInput_clear);
        m_pwInputClear = view.findViewById(R.id.loginPwInput_clear);

        m_emailInput.getmEditText().setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        m_pwInput.getmEditText().setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        m_pwInput.getmEditText().setImeOptions(EditorInfo.IME_ACTION_DONE);





        /*******************************************************************/

        /******************* Make Listener in View *******************/
        m_emailInput.getmEditText().setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if( !hasFocus ) {
                    m_emailInputClear.setVisibility(View.INVISIBLE);
                    return;
                }

                if( !m_emailInput.getmEditText().getText().equals("") ) {
                    m_emailInputClear.setOnClickListener(m_emailInputClearClickListener);
                    setControlEditText(EDITTEXT_CONTROL_CLEAR, m_emailInputClear);
                }
            }

        });

        m_emailInput.getmEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if( m_emailInput.getText().length() > MAX_EMAIL_LENGTH) {
                    String maximumAllowedCharacters = m_emailInput.getmEditText().getText().toString().substring(0, MAX_EMAIL_LENGTH);

                    m_emailInput.getmEditText().setText(maximumAllowedCharacters);
                    m_emailInput.getmEditText().setSelection(MAX_EMAIL_LENGTH);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if( !m_emailInput.getmEditText().getText().toString().equals("") ) {
                    m_emailInputClear.setVisibility(View.VISIBLE);
                } else {
                    m_emailInputClear.setVisibility(View.INVISIBLE);
                }
            }
        });
        /*************************************************************/

        return view;
    }

    private void setControlEditText(int typeCode, Button control) {
        control.setVisibility(View.VISIBLE);

        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)control.getLayoutParams();
        final int marginEndDp = params.getMarginEnd();

        if( typeCode == EDITTEXT_CONTROL_CLEAR ) {
            control.setBackgroundResource(R.drawable.ic_delete);
            params.setMarginEnd(m_editTextControlMarginEnd);
            control.setLayoutParams(params);
        } else if ( typeCode == EDITTEXT_CONTROL_CHECK ) {
            control.setBackgroundResource(R.drawable.ic_check);
            control.setOnClickListener(null);
            params.setMarginEnd(m_editTextControlMarginEnd*2);
            control.setLayoutParams(params);
        }
    }

    private void tryLogin() {
        View focusView = getActivity().getCurrentFocus();

        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(focusView.getWindowToken(), 0);

        String email = m_emailInput.getmEditText().getText().toString();
        String pw = m_pwInput.getmEditText().getText().toString();

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

        //sdong001@gmail.com
        //1234567890
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