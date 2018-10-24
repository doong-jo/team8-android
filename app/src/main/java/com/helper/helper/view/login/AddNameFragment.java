package com.helper.helper.view.login;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import com.helper.helper.R;
import com.helper.helper.controller.HttpManager;
import com.helper.helper.controller.UserManager;
import com.helper.helper.interfaces.HttpCallback;
import com.helper.helper.interfaces.ValidateCallback;
import com.helper.helper.model.User;
import com.helper.helper.view.widget.FloatingEditTextAddonControl;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AddNameFragment extends Fragment {
    private final static String TAG = JoinFragment.class.getSimpleName() + "/DEV";

    private FloatingEditTextAddonControl m_nameInput;
    private Button m_nextBtn;

    public AddNameFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
        View view = inflater.inflate( R.layout.fragment_add_name, container, false );

        // Solve : bug first touch not working
        getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        /******************* Connect widgtes with layout *******************/
        m_nameInput = view.findViewById(R.id.nameInput);
        m_nextBtn = view.findViewById(R.id.nextBtn);
        ImageView backBtn = view.findViewById(R.id.backMakeProfileFragment);
        /*******************************************************************/

        /******************* Make Listener in View *******************/
        m_nextBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View view) {
//                m_nameInput.setChecked(true);
                User user = new User.Builder()
                        .name(m_nameInput.getText())
                        .build();

                try {
                    getResultExistName(user, new ValidateCallback() {
                        @Override
                        public void onDone(int resultCode) throws JSONException {
                            if (resultCode == 1) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {

                                    }
                                });
                            } else {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        // TODO: 24/10/2018 Valid UserName Input

                                        m_nameInput.setChecked(true);
                                        UserManager.setUserName(m_nameInput.getText());

                                        View focusView = getActivity().getCurrentFocus();

                                        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
                                        if (focusView != null) {
                                            imm.hideSoftInputFromWindow(focusView.getWindowToken(), 0);
                                        }

                                        LoginActivity activity = (LoginActivity)getActivity();
                                        activity.moveToFragment(new MakeProfileFragment(), false);
                                    }
                                });
                            }
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        backBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                View focusView = getActivity().getCurrentFocus();

                InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
                if (focusView != null) {
                    imm.hideSoftInputFromWindow(focusView.getWindowToken(), 0);
                }

                LoginActivity activity = (LoginActivity)getActivity();
                activity.moveToFragment(new JoinFragment(), false);
            }
        });


        /*************************************************************/
        return view;
    }

    private void getResultExistName(User user, final ValidateCallback callback) throws JSONException {
        if( HttpManager.useCollection("user") ) {

            JSONObject reqObject = user.getTransformUserToJSON();
            reqObject.remove("emergency");
            reqObject.remove("lastAccess");
//            reqObject.put("lastAccess", new Date().toString());

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
