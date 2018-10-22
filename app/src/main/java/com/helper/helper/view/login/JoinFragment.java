
/*
 * Created by Sungdong Jo on 10/9/18 5:02 PM
 * Copyright (c) 2018 . All rights reserved.
 * Description: LoginActivity > JoinFragment
 *              View of join page
 */

package com.helper.helper.view.login;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatCheckBox;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ahmadrosid.library.FloatingLabelEditText;
import com.helper.helper.R;
import com.helper.helper.controller.FormManager;
import com.helper.helper.interfaces.ValidateCallback;
import com.helper.helper.view.ScrollingActivity;
import com.helper.helper.model.User;
import com.helper.helper.interfaces.HttpCallback;
import com.helper.helper.controller.HttpManager;
import com.helper.helper.controller.PermissionManager;
import com.helper.helper.view.widget.CheckableRelativeLayout;
import com.liuguangqiang.cookie.CookieBar;

import org.apache.commons.validator.Form;
import org.apache.commons.validator.routines.EmailValidator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JoinFragment extends Fragment {
    private final static String TAG = JoinFragment.class.getSimpleName() + "/DEV";

    /******************* Define widgtes in view *******************/
    private static final int SNACKBAR_INVALID_EMAIL = 225;
    private static final int SNACKBAR_INVALID_PW = 226;
    private static final int SNACKBAR_EXIST_EAMIL = 227;
    private static final int SNACKBAR_INFO_PW = 228;
    private static final int SNACKBARK_NOT_CHECKED_TERM = 231;

    private static final int MAX_PW_LENGTH = 15;
    private static final int EDITTEXT_CONTROL_CLEAR = 229;
    private static final int EDITTEXT_CONTROL_CHECK = 230;

    private FloatingLabelEditText m_emailInput;
    private FloatingLabelEditText m_pwInput;
    private Button m_emailInputClear;
    private Button m_pwInputClear;
    private FloatingLabelEditText m_nameInput;
    private AppCompatCheckBox m_termChkBox;
    private LinearLayout m_snackBar;
    private TextView m_snackBarMsg;
    private ImageView m_snackBarIcon;
    private OnClickListener m_emailInputClearClickListener;
    private OnClickListener m_pwInputClearClickListener;

    /**************************************************************/

    public JoinFragment() {

    }

    @SuppressLint("ResourceAsColor")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_join, container, false);


        /******************* Connect widgtes with layout *******************/
        Button joinBtn = view.findViewById(R.id.joinBtn);
        m_emailInput = view.findViewById(R.id.emailInput);
        m_pwInput = view.findViewById(R.id.pwInput);
        m_emailInputClear = view.findViewById(R.id.emailInput_clear);
        m_pwInputClear = view.findViewById(R.id.pwInput_clear);
        m_termChkBox = view.findViewById(R.id.termChkBox);
        TextView termText = view.findViewById(R.id.termText);
        ImageView backStartFragment = view.findViewById(R.id.backStartFragment);
        m_snackBar = view.findViewById(R.id.snackBar);
        m_snackBarMsg = view.findViewById(R.id.snackBarMsg);
        m_snackBarIcon = view.findViewById(R.id.snackBarIcon);

        m_emailInput.getmEditText().setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        m_pwInput.getmEditText().setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        m_pwInput.getmEditText().setImeOptions(EditorInfo.IME_ACTION_DONE);

//        m_pwInput.getmEditText().setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_check, 0);
//        m_pwInput.getmEditText().setCompoundDrawablePadding(10);
//        m_pwInput.getmEditText().getBackground().setColorFilter(R.color.accent_red, PorterDuff.Mode.SRC_ATOP);
//        m_pwInput.getmEditText().set(R.color.accent_red);

        /*******************************************************************/

        /******************* Make Listener in View *******************/
        backStartFragment.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                View focusView = getActivity().getCurrentFocus();

                InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(focusView.getWindowToken(), 0);
                }

                LoginActivity activity = (LoginActivity)getActivity();
                if (activity != null) {
                    activity.moveToStartFragment(view);
                }
            }
        });

        m_emailInput.getmEditText().setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean bIsFocus) {
                if( !bIsFocus ) {
                    m_emailInputClear.setVisibility(View.INVISIBLE);
                    return;
                }
                if( !m_emailInput.getmEditText().getText().toString().equals("") ) {
                    m_emailInputClear.setOnClickListener(m_emailInputClearClickListener);
                    setControlEditText(EDITTEXT_CONTROL_CLEAR, m_emailInputClear);
                }
            }
        });

        m_pwInput.getmEditText().setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean bIsFocus) {
                if( !bIsFocus ) {
                    m_pwInputClear.setVisibility(View.INVISIBLE);
                    return;
                }
                if( !m_pwInput.getmEditText().getText().toString().equals("") ) {
                    m_pwInputClear.setOnClickListener(m_pwInputClearClickListener);
                    setControlEditText(EDITTEXT_CONTROL_CLEAR, m_pwInputClear);
                }

                m_snackBar.setVisibility(View.VISIBLE);

                if (FormManager.emailCharValidate(m_emailInput.getText().toString()) != FormManager.RESULT_VALIDATION_SUCCESS) {
                    setSnackBarStatus(SNACKBAR_INVALID_EMAIL);
                } else {
                    setSnackBarStatus(SNACKBAR_INFO_PW);
                    setControlEditText(EDITTEXT_CONTROL_CHECK, m_emailInputClear);
                }
            }
        });

        m_emailInput.getmEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

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

        m_pwInput.getmEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int before, int after) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int after) {
                if( m_pwInput.getText().length() > MAX_PW_LENGTH) {
                    String maximumAllowedCharacters = m_pwInput.getmEditText().getText().toString().substring(0, MAX_PW_LENGTH);

                    m_pwInput.getmEditText().setText(maximumAllowedCharacters);
                    m_pwInput.getmEditText().setSelection(MAX_PW_LENGTH);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if( !m_pwInput.getmEditText().getText().toString().equals("") ) {
                    m_pwInputClear.setVisibility(View.VISIBLE);
                } else {
                    m_pwInputClear.setVisibility(View.INVISIBLE);
                }
            }
        });

        m_emailInputClearClickListener = new OnClickListener() {
            @Override
            public void onClick(View view) {
                m_emailInput.getmEditText().setText("");
            }
        };

        m_pwInputClearClickListener = new OnClickListener() {
            @Override
            public void onClick(View view) {
                m_pwInput.getmEditText().setText("");
            }
        };

        m_emailInputClear.setOnClickListener(m_emailInputClearClickListener);
        m_pwInputClear.setOnClickListener(m_pwInputClearClickListener);

        joinBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (FormManager.emailCharValidate(m_emailInput.getText().toString()) != FormManager.RESULT_VALIDATION_SUCCESS) {
                    m_snackBar.setVisibility(View.VISIBLE);
                    setSnackBarStatus(SNACKBAR_INVALID_EMAIL);
                } else if (FormManager.passwordCharValidate(m_pwInput.getText().toString()) != FormManager.RESULT_VALIDATION_SUCCESS) {
                    m_snackBar.setVisibility(View.VISIBLE);
                    setSnackBarStatus(SNACKBAR_INVALID_PW);
                    setControlEditText(EDITTEXT_CONTROL_CHECK, m_emailInputClear);
                } else {
                    setControlEditText(EDITTEXT_CONTROL_CHECK, m_emailInputClear);
                    setControlEditText(EDITTEXT_CONTROL_CHECK, m_pwInputClear);

                    if( !m_termChkBox.isChecked() ){
                        m_snackBar.setVisibility(View.VISIBLE);
                        setSnackBarStatus(SNACKBARK_NOT_CHECKED_TERM);
                    } else {
                        // success
                    }
                }
//                CookieBar cookie = new CookieBar.Builder(getActivity())
//                        .setTitle("Not a valid email address")
//                        .setIcon(R.drawable.ic_warning)
//                        .setLayoutGravity(Gravity.BOTTOM)
//                        .setBackgroundColor(R.color.snackbarbkg)
//                        .show();

//                tryJoin(m_emailInput.getText().toString(), m_pwInput.getText().toString(), m_nameInput.getText().toString());
            }
        });

        termText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginActivity activity = (LoginActivity)getActivity();
                if (activity != null) {
                    activity.moveToPrivacyTermFragment(view);
                }
            }
        });

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

    private void tryNext(String email, String pw) {
        //m_snackBar

        View focusView = getActivity().getCurrentFocus();

        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(focusView.getWindowToken(), 0);

        String phone = "";

        TelephonyManager tMgr = (TelephonyManager) getActivity().getSystemService(getActivity().TELEPHONY_SERVICE);

        PermissionManager.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_PHONE_STATE}, PermissionManager.READ_PHONE_STATE);

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
                .phone(phone)
                .build();

        try {
            validateJoinForm(joinUser, new ValidateCallback() {
                @Override
                public void onDone(int resultCode) throws JSONException {
                    if (resultCode == FormManager.RESULT_VALIDATION_SUCCESS && m_termChkBox.isChecked()) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getActivity(), "가입 유효성 검사 통과!", Toast.LENGTH_SHORT).show();
                            }
                        }); 
                    } else if( resultCode == FormManager.RESULT_VALIDATION_SUCCESS && !m_termChkBox.isChecked() ) {
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

//    private void tryJoin(String email, String pw, String name) {
//        View focusView = getActivity().getCurrentFocus();
//
//        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
//        imm.hideSoftInputFromWindow(focusView.getWindowToken(), 0);
//
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
//            validateJoinForm(joinUser, new ValidateCallback() {
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

    public void setControlEditText(int typeCode, Button control) {
        control.setVisibility(View.VISIBLE);

        if( typeCode == EDITTEXT_CONTROL_CLEAR ) {
            control.setBackgroundResource(R.drawable.ic_delete);
        } else if ( typeCode == EDITTEXT_CONTROL_CHECK ) {
            control.setBackgroundResource(R.drawable.ic_check);
            control.setOnClickListener(null);
        }
    }

    public void setSnackBarStatus(int visibleCode) {
        m_snackBar.setVisibility(View.VISIBLE);
        switch (visibleCode) {
            case SNACKBAR_INVALID_EMAIL:
                m_snackBarMsg.setText(R.string.invalid_email);
                m_snackBarIcon.setImageResource(R.drawable.ic_warning);
                break;


            case SNACKBAR_INVALID_PW:
                m_snackBarMsg.setText(R.string.invalid_password);
                m_snackBarIcon.setImageResource(R.drawable.ic_warning);
                break;

            case SNACKBAR_INFO_PW:
                m_snackBarMsg.setText(R.string.info_password);
                m_snackBarIcon.setImageResource(R.drawable.ic_inf);
                break;

            case SNACKBARK_NOT_CHECKED_TERM:
                m_snackBarMsg.setText(R.string.check_term);
                m_snackBarIcon.setImageResource(R.drawable.ic_warning);
                break;
            
        }
    }
    
    public void validateJoinForm(User user, final ValidateCallback callback) throws JSONException {
        /** Email **/
        final String email = user.getUserEmail();
        final String passwd = user.getUserPw();

        /** Description :
         *  Validate Flow : Validate Email(has httpCallback) {
         *      Validate Password ? Validate Name(has httpCallback) { return result } : return fail
         *  }
         */

        if( FormManager.emailCharValidate(email) != FormManager.RESULT_VALIDATION_SUCCESS ) {
            // fail email form
//            setSnackBarStatus(SNACKBAR_INVALID_EMAIL);
        } else if ( FormManager.passwordValidate(passwd) != FormManager.RESULT_VALIDATION_SUCCESS ) {
//            setSnackBarStatus(SNACKBAR_INVALID_EMAIL);
            // fail password form
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
