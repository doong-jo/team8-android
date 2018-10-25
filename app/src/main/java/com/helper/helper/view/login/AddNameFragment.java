package com.helper.helper.view.login;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.helper.helper.R;
import com.helper.helper.controller.FormManager;
import com.helper.helper.controller.HttpManager;
import com.helper.helper.controller.UserManager;
import com.helper.helper.interfaces.Command;
import com.helper.helper.interfaces.HttpCallback;
import com.helper.helper.interfaces.ValidateCallback;
import com.helper.helper.model.User;
import com.helper.helper.view.widget.FloatingEditTextAddonControl;
import com.helper.helper.view.widget.SnackBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AddNameFragment extends Fragment {
    private final static String TAG = JoinFragment.class.getSimpleName() + "/DEV";

    private static final int SNACKBAR_INVALID_NAME = 841;
    private static final int SNACKBAR_INFO_NAME = 992;
    private static final int SNACKBAR_EXIST_NAME = 161;

    private FloatingEditTextAddonControl m_nameInput;
    private SnackBar m_snackBar;
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
        m_snackBar = view.findViewById(R.id.addNameSnackBar);
        ImageView backBtn = view.findViewById(R.id.backMakeProfileFragment);

        m_nameInput.setInputType(InputType.TYPE_CLASS_TEXT);
        m_nameInput.setImeOption(EditorInfo.IME_ACTION_DONE);
        m_nameInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if(i == EditorInfo.IME_ACTION_DONE) {
                    tryNext();
                    return true;
                }
                return false;
            }
        });
        //setSnackBarStatus

        m_nameInput.setEnterFocusCmd(new Command() {
            @Override
            public void execute() {
                setSnackBarStatus(SNACKBAR_INFO_NAME);
            }
        });
        /*******************************************************************/

        /******************* Make Listener in View *******************/
        m_nextBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View view) {
                tryNext();
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

    private void tryNext() {
        User user = new User.Builder()
                .name(m_nameInput.getText())
                .build();

        try {
            if( FormManager.nameCharValidate(user.getUserName()) ) {
                setSnackBarStatus(SNACKBAR_INVALID_NAME);
                return;
            }
            getResultExistName(user, new ValidateCallback() {
                @Override
                public void onDone(final int resultCode) throws JSONException {
                    if( getActivity() == null ) return;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (resultCode == 1) {
                                setSnackBarStatus(SNACKBAR_EXIST_NAME);
                            } else {
                                UserManager.setUserName(m_nameInput.getText());

                                View focusView = getActivity().getCurrentFocus();

                                InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
                                if (focusView != null) {
                                    imm.hideSoftInputFromWindow(focusView.getWindowToken(), 0);
                                }

                                LoginActivity activity = (LoginActivity)getActivity();
                                activity.moveToFragment(new MakeProfileFragment(), false);
                            }
                        }
                    });

                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
    private void setSnackBarStatus(int visibleCode) {
        m_snackBar.setVisible(true);

        switch (visibleCode) {
            case SNACKBAR_INVALID_NAME:
                m_snackBar.setText(getString(R.string.invalid_name));
                m_snackBar.setIcon(R.drawable.ic_warning);
                break;

            case SNACKBAR_INFO_NAME:
                m_snackBar.setText(getString(R.string.info_name));
                m_snackBar.setIcon(R.drawable.ic_inf);
                break;

            case SNACKBAR_EXIST_NAME:
                m_snackBar.setText(getString(R.string.exist_name));
                m_snackBar.setIcon(R.drawable.ic_warning);
                break;
        }
    }
}
