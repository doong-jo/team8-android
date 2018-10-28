
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
import com.helper.helper.interfaces.Command;
import com.helper.helper.interfaces.ValidateCallback;
import com.helper.helper.model.User;
import com.helper.helper.view.ScrollingActivity;
import com.helper.helper.interfaces.HttpCallback;
import com.helper.helper.controller.HttpManager;
import com.helper.helper.controller.UserManager;
import com.helper.helper.view.widget.FloatingEditTextAddonControl;
import com.helper.helper.view.widget.LoadingButton;
import com.helper.helper.view.widget.SnackBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LoginFragment extends Fragment {
    private final static String TAG = LoginFragment.class.getSimpleName() + "/DEV";
    private final static int SNACKBAR_DENYING_LOGIN = 1000;
    private final static int DEFAULT_LOGIN = 1000;
    private final static int DELAY_LOGIN = 1001;
    private static final int MAX_EMAIL_LENGTH = 40;
    private static final int MAX_PW_LENGTH = 15;

    /******************* Define widgtes in view *******************/
    private FloatingEditTextAddonControl m_emailInputTxt;
    private FloatingEditTextAddonControl m_pwInputTxt;
    private LoadingButton m_loginBtn;

    private SnackBar m_snackBar;

//    private Button m_emailInputClear;
//    private Button m_pwInputClear;
//    private OnClickListener m_emailInputClearClickListener;
//    private OnClickListener m_pwInputClearClickListener;


    // save original pixel(after convert dp) of editText control marginEnd
    private int m_editTextControlMarginEnd;
    /**************************************************************/

    public LoginFragment() {

    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
        View view = inflater.inflate( R.layout.fragment_login, container, false );

        /******************* Connect widgtes with layout *******************/
        m_emailInputTxt = view.findViewById(R.id.loginEmailInput);
        m_pwInputTxt = view.findViewById(R.id.loginPwInput);
        m_loginBtn = view.findViewById(R.id.loadingBtn);

        m_emailInputTxt.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        m_emailInputTxt.setImeOption(EditorInfo.IME_ACTION_NEXT);

        m_pwInputTxt.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        m_pwInputTxt.setImeOption(EditorInfo.IME_ACTION_DONE);

        m_snackBar = view.findViewById(R.id.loginSnackBar);




        /*******************************************************************/

        setLoginBtnStatus(DEFAULT_LOGIN);

        /******************* Make Listener in View *******************/
//        m_emailInput.getmEditText().setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                if( !hasFocus ) {
//                    m_emailInputClear.setVisibility(View.INVISIBLE);
//                    return;
//                }
//
//                if( !m_emailInput.getmEditText().getText().equals("") ) {
//                    m_emailInputClear.setOnClickListener(m_emailInputClearClickListener);
//                    setControlEditText(EDITTEXT_CONTROL_CLEAR, m_emailInputClear);
//                }
//            }
//
//        });


//        m_emailInput.getmEditText().addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//                if( m_emailInput.getText().length() > MAX_EMAIL_LENGTH) {
//                    String maximumAllowedCharacters = m_emailInput.getmEditText().getText().toString().substring(0, MAX_EMAIL_LENGTH);
//
//                    m_emailInput.getmEditText().setText(maximumAllowedCharacters);
//                    m_emailInput.getmEditText().setSelection(MAX_EMAIL_LENGTH);
//                }
//            }
//
//            @Override
//            public void afterTextChanged(Editable editable) {
//                if( !m_emailInput.getmEditText().getText().toString().equals("") ) {
//                    m_emailInputClear.setVisibility(View.VISIBLE);
//                } else {
//                    m_emailInputClear.setVisibility(View.INVISIBLE);
//                }
//            }
//        });


        m_loginBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                tryLogin();
            }
        });

        m_emailInputTxt.setEnterFocusCmd(new Command() {
            @Override
            public void execute() {
                if(m_emailInputTxt.getText().equals("") || FormManager.emailCharValidate(m_emailInputTxt.getText()) == FormManager.RESULT_VALIDATION_SUCCESS){
                    m_snackBar.setVisible(false);
                }
            }
        });

        m_emailInputTxt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if( i == EditorInfo.IME_ACTION_NEXT ) { m_pwInputTxt.setFocus(true, getActivity()); }
                return false;
            }
        });
        m_pwInputTxt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if( i == EditorInfo.IME_ACTION_DONE ) {
                    View focusView = getActivity().getCurrentFocus();
                    InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
                    if (focusView != null) { imm.hideSoftInputFromWindow(focusView.getWindowToken(), 0); }
                    tryLogin();
                    return true;
                }
                return false;
            }
        });

        /*************************************************************/

        return view;
    }


    private void tryLogin() {
        View focusView = getActivity().getCurrentFocus();

        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(focusView.getWindowToken(), 0);

        String email = m_emailInputTxt.getText().toString();
        String pw = m_pwInputTxt.getText().toString();


        if(FormManager.emailCharValidate(email) == FormManager.RESULT_VALIDATION_EMAIL_WRONG) {
            setSnackBarStatus(SNACKBAR_DENYING_LOGIN);
            return;
        }

        if(FormManager.passwordCharValidate(pw) == FormManager.RESULT_VALIDATION_PW_WRONG) {
             setSnackBarStatus(SNACKBAR_DENYING_LOGIN);
            return;
        }

        //sdong001@gmail.com
        //team12345
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


                        /** account exist **/
                        if( arrLen != 0 ) {
                            UserManager.setUser(existIdjsonArray.getJSONObject(0));


                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
//                                            ProgressDialog dialog = ProgressDialog.show(getActivity(), getString(R.string.login_loading_title), getString(R.string.login_loading_message), true);
                                            setLoginBtnStatus(DEFAULT_LOGIN);

                                            Intent intent=new Intent(getActivity(),ScrollingActivity.class);
                                            startActivity(intent);
                                            getActivity().finish();
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
                                    setSnackBarStatus(SNACKBAR_DENYING_LOGIN);
                                    //Toast.makeText(getActivity(), "아이디 혹은 비밀번호가 올바르지 않습니다.", Toast.LENGTH_SHORT).show();
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

    private void setSnackBarStatus(int visibleCode) {
        m_snackBar.setVisible(true);
        switch (visibleCode) {
            case SNACKBAR_DENYING_LOGIN:
                m_snackBar.setText(getString(R.string.not_match_login));
                m_snackBar.setIcon(R.drawable.ic_warning);
                break;
        }
    }

    private void setLoginBtnStatus(int visibleCode){
        m_loginBtn.setIcon(R.drawable.ic_spinner_solid);

        switch(visibleCode){
            case DELAY_LOGIN:
                m_loginBtn.setText("");
                m_loginBtn.setLoadingIconVisible(true);
                break;
            case DEFAULT_LOGIN:
                m_loginBtn.setText("Log In");
                m_loginBtn.setLoadingIconVisible(false);
        }
    }



    private void validateCharacterForm(User user, final ValidateCallback callback) throws JSONException {
        final String email = user.getUserEmail();
        final String passwd = user.getUserPw();

        if( FormManager.emailCharValidate(email) != FormManager.RESULT_VALIDATION_SUCCESS ) {  callback.onDone(FormManager.RESULT_VALIDATION_EMAIL_WRONG); }
        else if ( FormManager.passwordValidate(passwd) != FormManager.RESULT_VALIDATION_SUCCESS ) {callback.onDone(FormManager.RESULT_VALIDATION_PW_WRONG); }
        else { callback.onDone(FormManager.RESULT_VALIDATION_SUCCESS); }
    }
}