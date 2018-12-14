
/*
 * Copyright (c) 10/11/18 1:19 PM
 * Written by Sungdong Jo
 * Description: LoginActivity > LoginFragment
 *              View of Login Page
 */

package com.helper.helper.view.login;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.helper.helper.R;
import com.helper.helper.controller.FileManager;
import com.helper.helper.controller.FormManager;
import com.helper.helper.controller.SharedPreferencer;
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

import java.io.IOException;

public class LoginFragment extends Fragment {
    private final static String TAG = LoginFragment.class.getSimpleName() + "/DEV";
    private final static int SNACKBAR_DENYING_LOGIN = 1000;
    private final static int DEFAULT_LOGIN = 1000;
    private final static int DELAY_LOGIN = 1001;

    /******************* Define widgtes in view *******************/
    private FloatingEditTextAddonControl m_emailInputTxt;
    private FloatingEditTextAddonControl m_pwInputTxt;
    private LoadingButton m_loginBtn;
    private RelativeLayout m_loginLayout;

    private SnackBar m_snackBar;
    /**************************************************************/

    public LoginFragment() {

    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
        // Solve : bug first touch not working
        getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        View view = inflater.inflate( R.layout.fragment_login, container, false );

        /******************* Connect widgtes with layout *******************/
        m_loginLayout = view.findViewById(R.id.loginLayout);

        m_emailInputTxt = view.findViewById(R.id.loginEmailInput);
        m_pwInputTxt = view.findViewById(R.id.loginPwInput);
        m_loginBtn = view.findViewById(R.id.loginLoadingBtn);

        m_emailInputTxt.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        m_emailInputTxt.setImeOption(EditorInfo.IME_ACTION_NEXT);

        m_pwInputTxt.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        m_pwInputTxt.setImeOption(EditorInfo.IME_ACTION_DONE);

        m_snackBar = view.findViewById(R.id.loginSnackBar);

        ImageView backStartFragment = view.findViewById(R.id.backStartFragment);

        /*******************************************************************/

        setLoginBtnStatus(DEFAULT_LOGIN);

        /******************* Make Listener in View *******************/

        m_loginBtn.setButtonOnClickListener(new OnClickListener() {
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

        m_pwInputTxt.setEnterFocusCmd(new Command() {
            @Override
            public void execute() {
                m_snackBar.setVisible(false);
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

        backStartFragment.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginActivity activity = (LoginActivity)getActivity();
                if( activity != null ) {
                    activity.moveToFragment(new StartFragment(), m_loginLayout,true);
                }
            }
        });
        /*************************************************************/
        LoginActivity loginActivity = (LoginActivity)getActivity();
        loginActivity.setFragmentBackPressed(new StartFragment(), m_loginLayout, false);

        return view;
    }


    private void tryLogin() {
        View focusView = getActivity().getCurrentFocus();

        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(focusView.getWindowToken(), 0);

        String email = m_emailInputTxt.getText();
        String pw = m_pwInputTxt.getText();


        if(FormManager.emailCharValidate(email) == FormManager.RESULT_VALIDATION_EMAIL_WRONG) {
            setSnackBarStatus(SNACKBAR_DENYING_LOGIN);
            clearAllInputFocus();
            return;
        }

        if(FormManager.passwordCharValidate(pw) == FormManager.RESULT_VALIDATION_PW_WRONG) {
            setSnackBarStatus(SNACKBAR_DENYING_LOGIN);
            clearAllInputFocus();
            return;
        }

        if( HttpManager.useCollection(getString(R.string.collection_user)) ) {
            JSONObject reqObject = new JSONObject();
            try {
                reqObject.put(User.KEY_EMAIL, email);
                reqObject.put(User.KEY_PASSWORD, pw);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                HttpManager.requestHttp(reqObject, "", "GET", "", new HttpCallback() {
                    @Override
                    public void onSuccess(JSONArray existIdjsonArray) throws JSONException {
                        int arrLen = existIdjsonArray.length();


                        /** account exist **/
                        if( arrLen != 0 ) {
                            JSONObject object = existIdjsonArray.getJSONObject(0);
                            User user = new User.Builder()
                                    .email(object.getString(User.KEY_EMAIL))
                                    .name(object.getString(User.KEY_NAME))
                                    .ridingType(object.getString(User.KEY_RIDING_TYPE))
                                    .ledIndicies(object.getJSONArray(User.KEY_LED_INDICIES))
                                    .ledBookmarked(object.getJSONArray(User.KEY_LED_BOOKMARKED))
                                    .build();

                            UserManager.setUser(existIdjsonArray.getJSONObject(0));

                            try {
                                FileManager.writeXmlUserInfo(getActivity(), user);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            setLoginBtnStatus(DELAY_LOGIN);
                                            m_snackBar.setVisible(false);

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
                                    clearAllInputFocus();
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

    private void clearAllInputFocus() {
        m_pwInputTxt.clearFocus();
        m_emailInputTxt.clearFocus();
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
        switch(visibleCode){
            case DELAY_LOGIN:
                m_loginBtn.setText("");
                m_loginBtn.setLoadingIconVisible(true);
                break;
            case DEFAULT_LOGIN:
                m_loginBtn.setText(getString(R.string.login));
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