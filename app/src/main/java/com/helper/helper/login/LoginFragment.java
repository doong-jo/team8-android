
/*
 * Copyright (c) 10/11/18 1:19 PM
 * Written by Sungdong Jo
 * Description: LoginActivity > LoginFragment
 *              View of Login Page
 */

package com.helper.helper.login;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Image;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.helper.helper.R;
import com.helper.helper.ScrollingActivity;
import com.helper.helper.contact.ContactActivity;
import com.helper.helper.data.User;
import com.helper.helper.util.HttpCallback;
import com.helper.helper.util.HttpManagerUtil;
import com.helper.helper.util.PermissionUtil;

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

        OnClickListener makeTryLoginListener = makeTryLoginListener();

        m_loginBtn.setOnClickListener(makeTryLoginListener);

        return view;
    }

    private OnClickListener makeTryLoginListener() {
        return
                new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String email = m_emailInput.getText().toString();
                        String pw = m_pwInput.getText().toString();


                        if( HttpManagerUtil.useCollection("user") ) {
                            JSONObject reqObject = new JSONObject();
                            try {
                                reqObject.put("email", email);
                                reqObject.put("passwd", pw);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            try {
                                HttpManagerUtil.requestHttp(reqObject, "GET", new HttpCallback() {
                                    @Override
                                    public void onSuccess(JSONArray existIdjsonArray) throws JSONException {
                                        int arrLen = existIdjsonArray.length();
                                        if( arrLen != 0 ) {
                                            View focusView = getActivity().getCurrentFocus();

                                            InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
                                            imm.hideSoftInputFromWindow(focusView.getWindowToken(), 0);

                                            Intent intent=new Intent(getActivity(),ScrollingActivity.class);
                                            startActivity(intent);
                                        } else {
                                            /** email exist **/
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
                };
    }
}