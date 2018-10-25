
/*
 * Created by Sungdong Jo on 10/9/18 5:02 PM
 * Copyright (c) 2018 . All rights reserved.
 * Description: LoginActivity > JoinFragment
 *              View of join page
 */

package com.helper.helper.view.login;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatCheckBox;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ahmadrosid.library.FloatingLabelEditText;
import com.helper.helper.R;
import com.helper.helper.controller.FormManager;
import com.helper.helper.controller.UserManager;
import com.helper.helper.interfaces.Command;
import com.helper.helper.interfaces.ValidateCallback;
import com.helper.helper.model.User;
import com.helper.helper.interfaces.HttpCallback;
import com.helper.helper.controller.HttpManager;
import com.helper.helper.controller.PermissionManager;
import com.helper.helper.view.widget.FloatingEditTextAddonControl;
import com.helper.helper.view.widget.SnackBar;

import org.json.JSONArray;
import org.json.JSONException;

import org.json.JSONObject;

/** Get Phone Number **/
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

public class JoinFragment extends Fragment {
    private final static String TAG = JoinFragment.class.getSimpleName() + "/DEV";

    /******************* Define widgtes in view *******************/
    private static final int SNACKBAR_INVALID_EMAIL = 225;
    private static final int SNACKBAR_INVALID_PW = 226;
    private static final int SNACKBAR_EXIST_EAMIL = 227;
    private static final int SNACKBAR_INFO_PW = 228;
    private static final int SNACKBARK_NOT_CHECKED_TERM = 231;

    private AppCompatCheckBox m_termChkBox;
    private SnackBar m_snackBar;

    private FloatingEditTextAddonControl m_emailInputTxt;
    private FloatingEditTextAddonControl m_pwInputTxt;

    /**************************************************************/

    public JoinFragment() {

    }

    @SuppressLint({"ResourceAsColor", "RestrictedApi"})
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_join, container, false);


        // Solve : bug first touch not working
        getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        /******************* Connect widgtes with layout *******************/
        m_emailInputTxt = view.findViewById(R.id.emailInputTxt);
        m_pwInputTxt = view.findViewById(R.id.pwInputTxt);

        m_emailInputTxt.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        m_emailInputTxt.setImeOption(EditorInfo.IME_ACTION_NEXT);

        m_pwInputTxt.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        m_pwInputTxt.setImeOption(EditorInfo.IME_ACTION_DONE);

        LinearLayout parentLayout = view.findViewById(R.id.parentLayout);

        m_emailInputTxt.setEnterFocusCmd(new Command() {
            @Override
            public void execute() {
                if( m_emailInputTxt.getText().equals("") || FormManager.emailCharValidate(m_emailInputTxt.getText()) == FormManager.RESULT_VALIDATION_SUCCESS ) {
                    m_snackBar.setVisible(false);
                }
            }
        });

        m_emailInputTxt.setLeaveFocusCheckToggleCmd(new Command() {
            @Override
            public void execute() {
                final User user = new User.Builder()
                        .email(m_emailInputTxt.getText())
                        .build();
                try {
                    if( FormManager.emailCharValidate(user.getUserEmail()) != FormManager.RESULT_VALIDATION_SUCCESS ) {
                        setSnackBarStatus(SNACKBAR_INVALID_EMAIL);
                        return;
                    }
                    getResultExistEmail(user, new ValidateCallback() {
                        @Override
                        public void onDone(final int resultCode) {
                            if( getActivity() == null ) return;
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (resultCode == 1) {
                                        setSnackBarStatus(SNACKBAR_EXIST_EAMIL);
                                    }
                                }
                            });
                        }});
                    } catch (JSONException e) { e.printStackTrace(); }
                }
        });

        m_pwInputTxt.setEnterFocusCmd(new Command() {
            @Override
            public void execute() {
                final User user = new User.Builder()
                        .email(m_emailInputTxt.getText())
                        .build();

                if( FormManager.emailCharValidate(user.getUserEmail()) != FormManager.RESULT_VALIDATION_SUCCESS ) {
                    setSnackBarStatus(SNACKBAR_INVALID_EMAIL);
//                    m_emailInputTxt.setEdixTextColor(Color.parR.color.accent_red);
                } else {
                    setSnackBarStatus(SNACKBAR_INFO_PW);
                }
            }
        });

        Button joinBtn = view.findViewById(R.id.joinBtn);
        m_termChkBox = view.findViewById(R.id.termChkBox);
        TextView termText = view.findViewById(R.id.termText);
        ImageView backStartFragment = view.findViewById(R.id.backStartFragment);
        m_snackBar = view.findViewById(R.id.joinSnackBar);
        /*******************************************************************/

        /******************* Make Listener in View *******************/
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
                    return true;
                }
                return false;
            }
        });

        backStartFragment.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                View focusView = getActivity().getCurrentFocus();

                InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
                if (focusView != null) { imm.hideSoftInputFromWindow(focusView.getWindowToken(), 0); }

                LoginActivity activity = (LoginActivity)getActivity();
                activity.moveToFragment(new StartFragment(), true);
            }
        });

        joinBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View view) {
                tryNext();
            }
        });

        m_termChkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                View focusView = getActivity().getCurrentFocus();

                InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
                if (focusView != null) { imm.hideSoftInputFromWindow(focusView.getWindowToken(), 0); }
            }
        });

        termText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                View focusView = getActivity().getCurrentFocus();

                InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
                if (focusView != null) { imm.hideSoftInputFromWindow(focusView.getWindowToken(), 0); }

                LoginActivity activity = (LoginActivity)getActivity();
                activity.moveToFragment(new TermFragment(), false);
            }
        });

        parentLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                View focusView = getActivity().getCurrentFocus();
                InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
                if (focusView != null) { imm.hideSoftInputFromWindow(focusView.getWindowToken(), 0); }
            }
        });
        /*************************************************************/

        return view;
    }

    private void tryNext() {
        View focusView = getActivity().getCurrentFocus();
        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
        if (focusView != null) { imm.hideSoftInputFromWindow(focusView.getWindowToken(), 0); }

        final User user = new User.Builder()
                .email(m_emailInputTxt.getText())
                .pw(m_pwInputTxt.getText())
                .build();
        try {
            validateCharacterForm(user, new ValidateCallback() {
                @Override
                public void onDone(final int validateCode) {
                    try {
                        getResultExistEmail(user, new ValidateCallback() {
                            @Override
                            public void onDone(final int resultCode) throws JSONException {
                                if( getActivity() == null ) return;
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if( resultCode == 1 ) {
                                            setSnackBarStatus(SNACKBAR_EXIST_EAMIL);
                                        }
                                        else if( validateCode == FormManager.RESULT_VALIDATION_SUCCESS && m_termChkBox.isChecked() ){
                                            // not exist
                                            UserManager.setUserEmail(user.getUserEmail());
                                            UserManager.setUserPassword(user.getUserPw());

                                            LoginActivity activity = (LoginActivity)getActivity();
                                            activity.moveToFragment(new AddNameFragment(), false);
                                        } else {
                                            if( validateCode == FormManager.RESULT_VALIDATION_PW_WRONG ) {
                                                setSnackBarStatus(SNACKBAR_INVALID_PW);
                                            } else if ( validateCode == FormManager.RESULT_VALIDATION_EMAIL_WRONG ) {
                                                setSnackBarStatus(SNACKBAR_INVALID_EMAIL);
                                            } else if ( !m_termChkBox.isChecked() ) {
                                                setSnackBarStatus(SNACKBARK_NOT_CHECKED_TERM);
                                            }
                                        }
                                    }
                                });
                            }
                        });
                    } catch (JSONException e) { e.printStackTrace(); }
                }
            });
        } catch (JSONException e) { e.printStackTrace(); }
    }

    private void setSnackBarStatus(int visibleCode) {
        m_snackBar.setVisible(true);
        switch (visibleCode) {
            case SNACKBAR_INVALID_EMAIL:
                m_snackBar.setText(getString(R.string.invalid_email));
                m_snackBar.setIcon(R.drawable.ic_warning);
                break;

            case SNACKBAR_EXIST_EAMIL:
                m_snackBar.setText(getString(R.string.exist_email));
                m_snackBar.setIcon(R.drawable.ic_warning);
                break;

            case SNACKBAR_INVALID_PW:
                m_snackBar.setText(getString(R.string.invalid_password));
                m_snackBar.setIcon(R.drawable.ic_warning);
                break;

            case SNACKBAR_INFO_PW:
                m_snackBar.setText(getString(R.string.info_password));
                m_snackBar.setIcon(R.drawable.ic_inf);
                break;

            case SNACKBARK_NOT_CHECKED_TERM:
                m_snackBar.setText(getString(R.string.check_term));
                m_snackBar.setIcon(R.drawable.ic_warning);
                break;
            
        }
    }
    
    private void validateCharacterForm(User user, final ValidateCallback callback) throws JSONException {
        final String email = user.getUserEmail();
        final String passwd = user.getUserPw();

        if( FormManager.emailCharValidate(email) != FormManager.RESULT_VALIDATION_SUCCESS ) {  callback.onDone(FormManager.RESULT_VALIDATION_EMAIL_WRONG); }
        else if ( FormManager.passwordValidate(passwd) != FormManager.RESULT_VALIDATION_SUCCESS ) {callback.onDone(FormManager.RESULT_VALIDATION_PW_WRONG); }
        else { callback.onDone(FormManager.RESULT_VALIDATION_SUCCESS); }
    }

    private void getResultExistEmail(User user, final ValidateCallback callback) throws  JSONException {
        if( HttpManager.useCollection("user") ) {

            JSONObject reqObject = user.getTransformUserToJSON();
            reqObject.remove("emergency");
            reqObject.remove("lastAccess");

            HttpManager.requestHttp(reqObject, "GET", new HttpCallback() {
                @Override
                public void onSuccess(JSONArray jsonArray) throws JSONException {
                    if( jsonArray.length() != 0  ) {
                        callback.onDone(1);
                    } else {
                        callback.onDone(0);
                    }
                }

                @Override
                public void onError(String err) {

                }
            });
        }
    }

}
