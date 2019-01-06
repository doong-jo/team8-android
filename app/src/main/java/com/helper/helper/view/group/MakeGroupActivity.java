package com.helper.helper.view.group;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.AppCompatCheckBox;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.helper.helper.R;
import com.helper.helper.controller.HttpManager;
import com.helper.helper.controller.UserManager;
import com.helper.helper.interfaces.CheckedInterface;
import com.helper.helper.interfaces.HttpCallback;
import com.helper.helper.model.Member;
import com.helper.helper.model.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.acl.Group;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class MakeGroupActivity extends ListActivity {

    private final static String TAG = MakeGroupActivity.class.getSimpleName() + "/DEV";

    private List<Member> m_listViewData;
    private ListView m_listView;

    /******************* Define widgtes in view *******************/
    private MemberItemListAdapter m_adapter;
    private TextView m_addedMembers;
    private TextView m_makeDone;
    private EditText m_searchUserInput;
    /**************************************************************/

    private List<String> m_addedMembersArr = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_group);

        /******************* Connect widgtes with layout *******************/
        m_searchUserInput = findViewById(R.id.searchUser);
        ImageView backImg = findViewById(R.id.backImg);
        m_addedMembers = findViewById(R.id.added_members);
        m_makeDone = findViewById(R.id.doneMakeGroup);
        /*******************************************************************/

        /** Listview **/
        // Create the m_adapter to render our data
        // --

        // Get some views for later use
        // --
        m_listView = getListView();
        m_listView.setItemsCanFocus(false);

        ////////////////Mode Define//////////////////
        // ListView.CHOICE_MODE_SINGLE             //
        // ListView.CHOICE_MODE_MULTIPLE           //
        // ListView.CHOICE_MODE_NONE               //
        /////////////////////////////////////////////

        m_listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
//        try {
//            m_listViewData = FileManager.readXmlEmergencyContacts(this);
//            EmergencyManager.setEmergencycontacts(m_listViewData);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        if( m_listViewData == null ) {
            m_listViewData = new ArrayList<Member>();
        }
        m_adapter = new MemberItemListAdapter(this, m_listViewData, new CheckedInterface() {
            @Override
            public void onResult(String name, boolean isChecked) {
                if( isChecked ) {
                    addIntoAddedMemberList(name);
                } else {
                    removeIntoAddedMemberList(name);
                }
            }
        });
        setListAdapter(m_adapter);

        /******************* Make Listener in View *******************/
        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        m_searchUserInput.addTextChangedListener(makeSearchInputWatcher());

        final Activity activity = this;
        m_makeDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if( m_listViewData.size() == 0 ) {
                    new SweetAlertDialog(activity)
                            .setTitleText("Sorry!")
                            .setContentText("Please Add your members.")
                            .setConfirmButton("Close", new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                    sweetAlertDialog.dismissWithAnimation();
                                }
                            })
                            .show();
                    return;
                }
                Intent resultIntent = new Intent();
                resultIntent.putExtra("membersNames", m_addedMembersArr.toString());
                setResult(GroupActivity.REQUEST_MAKE_GROUP, resultIntent);
                finish();
            }
        });
        /*************************************************************/
    }

    private void addIntoAddedMemberList(String name) {
        if( m_addedMembersArr.size() == 0 ) {
            m_addedMembers.setAlpha(0.79f);
        }

        m_addedMembersArr.add(name);
        m_addedMembers.setText(m_addedMembersArr.toString());
    }

    private void removeIntoAddedMemberList(String name) {
        for (String str :
                m_addedMembersArr) {
            if( str.equals(name) ) {
                m_addedMembersArr.remove(str);
                break;
            }
        }

        m_addedMembers.setText(m_addedMembersArr.toString());

        if( m_addedMembersArr.size() == 0 ) {
            m_addedMembers.setText(getString(R.string.plz_add_member));
            m_addedMembers.setAlpha(0.49f);
        }
    }

    private TextWatcher makeSearchInputWatcher() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            private Timer timer=new Timer();
            private final long DELAY = 400; // milliseconds

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                timer.cancel();
                timer = new Timer();
                timer.schedule(
                        new TimerTask() {
                            @Override
                            public void run() {
                                final String input = m_searchUserInput.getText().toString();
                                if( input.equals("") ) {
                                    m_listViewData.clear();
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            m_adapter.notifyDataSetChanged();
                                        }
                                    });
                                    return;
                                }
                                if( HttpManager.useCollection(getString(R.string.collection_user))) {
                                    final String name = "^" + input;

                                    JSONObject reqObject = new JSONObject();
                                    try {
                                        reqObject.put(User.KEY_NAME, name);
                                    }
                                    catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    try {
                                        HttpManager.requestHttp(reqObject, "", "GET", "regex", new HttpCallback() {

                                            @Override
                                            public void onSuccess(JSONArray jsonArray) throws JSONException {
                                                final int arrLen = jsonArray.length();

                                                m_listViewData.clear();

                                                if (arrLen != 0) {

                                                    for (int i = 0; i < arrLen; i++) {
                                                        JSONObject object = jsonArray.getJSONObject(i);

                                                        final String memberName = object.getString("name");

                                                        m_listViewData.add(new Member(
                                                                object.getString("riding_type"),
                                                                memberName
                                                        ));
                                                    }
                                                }
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        m_adapter.notifyDataSetChanged();
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onError(String err) {
                                                Log.i(TAG, "onError: getNames");
                                            }
                                        });
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                };
                            }
                        },
                        DELAY
                );
            }

            @Override
            public void afterTextChanged(final Editable s) { }
        };
    }

    /**
     * Uncheck all the items
     */
    public View getViewByPosition(int pos, ListView listView) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

        if (pos < firstListItemPosition || pos > lastListItemPosition ) {
            return listView.getAdapter().getView(pos, null, listView);
        } else {
            final int childIndex = pos - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
    }
}
