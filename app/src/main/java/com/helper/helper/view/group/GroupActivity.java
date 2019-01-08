package com.helper.helper.view.group;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.AppCompatCheckBox;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.helper.helper.R;
import com.helper.helper.controller.HttpManager;
import com.helper.helper.interfaces.HttpCallback;
import com.helper.helper.model.Member;
import com.helper.helper.controller.UserManager;
import com.helper.helper.model.MemberList;
import com.helper.helper.model.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GroupActivity extends ListActivity {

    private final static String TAG = GroupActivity.class.getSimpleName() + "/DEV";

    public static final int REQUEST_MAKE_GROUP = 2001;
    public static final int REQUSET_DETAIL_GROUP = 2002;

    /******************* Define widgtes in view *******************/
    private List<MemberList> m_listViewData;
    private ListView m_listView;
    private MemberListItemListAdapter m_adapter;

    private TextView m_deleteToggle;
    private ImageView m_deleteTrigger;
    private TextView m_noExistsTv;
    /**************************************************************/

    private List<Integer> m_chekcedDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        final Activity thisActivity = this;

        m_chekcedDelete = new ArrayList<>();

        /******************* Connect widgtes with layout *******************/
        m_deleteTrigger = findViewById(R.id.backImg);
        m_deleteToggle = findViewById(R.id.memberDeleteToggle);
        m_noExistsTv = findViewById(R.id.textViewNoMemberList);
        /*******************************************************************/

        /** Listview **/
        // Create the adapter to render our data
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
            m_listViewData = new ArrayList<MemberList>();
        }
        m_adapter = new MemberListItemListAdapter(this, m_listViewData);
        m_adapter.setTouchItemListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                Intent intent = new Intent(thisActivity, DetailGroupActivity.class);

                String members = view.getTag(R.string.group_list_item_key_members).toString();
                String groupIdx = view.getTag(R.string.group_list_item_key_groupIdx).toString();
                intent.putExtra("memberNames", members);
                intent.putExtra("groupIdx", groupIdx);

                setResult(GroupActivity.REQUEST_MAKE_GROUP, intent);
                startActivityForResult(intent, REQUSET_DETAIL_GROUP);
                return false;
            }
        });
        setListAdapter(m_adapter);

        toggleShowNotExistsGroupText();

        /******************* Make Listener in View *******************/
        m_deleteTrigger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean bIsDelete = m_deleteToggle.getText().toString().equals(getString(R.string.group_delete));

                if( bIsDelete ) { finish(); }
                else {
                    final int firstListItemPosition = m_listView.getFirstVisiblePosition();
                    final int lastListItemPosition = firstListItemPosition + m_listView.getChildCount() - 1;

                    m_chekcedDelete.clear();
                    for (int i = firstListItemPosition; i <= lastListItemPosition; i++) {
                        View itemView = m_listView.getChildAt(i);

                        if( itemView == null ) { continue; }

                        AppCompatCheckBox chkBox = itemView.findViewById(R.id.selectChk);
                        boolean checked = chkBox.isChecked();

                        if( checked ) {
                            m_chekcedDelete.add(i);
                        }
                    }

                    removeCheckedItems();
                }
            }
        });

        m_deleteToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean bIsDelete = m_deleteToggle.getText().toString().equals(getString(R.string.group_delete));
                m_adapter.setDeleteMode(bIsDelete);
                m_adapter.notifyDataSetChanged();

                if ( bIsDelete ) {
                    m_deleteToggle.setText(getString(R.string.group_cancle));
                    m_deleteTrigger.setImageResource(R.drawable.ic_trashcan);
                } else {
                    m_deleteToggle.setText(getString(R.string.group_delete));
                    m_deleteTrigger.setImageResource(R.drawable.ic_back);
                }
            }
        });

        FloatingActionButton btn = findViewById(R.id.addGroupFab);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                makeGroup();
            }
        });
        /*************************************************************/

        setGroupList();
    }

    private void findGroup(final String groupIdx) {
        final Activity activity = this;

        if (HttpManager.useCollection(activity.getString(R.string.collection_group))) {
            JSONObject reqObject = new JSONObject();

            try {
                reqObject.put("index", groupIdx);

                HttpManager.requestHttp(reqObject, "", "GET", "", new HttpCallback() {
                    @Override
                    public void onSuccess(JSONArray jsonArray) throws JSONException {
                        JSONObject object = (JSONObject)jsonArray.get(0);

                        String idx = object.getString(MemberList.KEY_INDEX);

                        JSONArray members = object.getJSONArray(MemberList.KEY_MEMBERS);
                        String membersStr = members.toString();
                        membersStr = membersStr.replace("[","").replace("]","");
                        List<String> listNames = Arrays.asList(membersStr.split(","));

                        String masterStr = object.getString(MemberList.KEY_MASTER);
                        final MemberList memberList = new MemberList(listNames, masterStr, idx);

                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                addGroup(memberList);
                            }
                        });
                    }

                    @Override
                    public void onError(String err) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(activity, "Check your network connection", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void setGroupList() {
        final Activity activity = this;

        if (HttpManager.useCollection(activity.getString(R.string.collection_user))) {
            JSONObject reqObject = new JSONObject();

            try {
                reqObject.put(User.KEY_NAME, UserManager.getUserName());

                HttpManager.requestHttp(reqObject, "", "GET", "", new HttpCallback() {
                    @Override
                    public void onSuccess(JSONArray jsonArray) {
                        for (int i = 0; i < jsonArray.length(); i++) {
                            try {
                                JSONObject obj = jsonArray.getJSONObject(i);
                                JSONArray groups = obj.getJSONArray(User.KEY_GROUPS);
                                for (int j = 0; j < groups.length(); j++) {
                                    String groupIdx = String.valueOf(groups.get(0));
                                    findGroup(groupIdx);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onError(String err) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(activity, "Check your network connection", Toast.LENGTH_LONG).show();
                            }
                        });

                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void toggleShowNotExistsGroupText() {
        TextView noExistsTextView = (TextView) findViewById(R.id.textViewNoMemberList);

        if( m_listViewData.size() != 0 ) {
            noExistsTextView.setVisibility(View.GONE);
        } else {
            noExistsTextView.setVisibility(View.VISIBLE);
        }
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


    private void removeCheckedItems() {
        int removeCnt = 0;
        for (int i = 0; i <m_chekcedDelete.size(); i++) {
            int I = m_chekcedDelete.get(i);
            MemberList item = m_adapter.getItem(I-removeCnt++);
//            removeGroupInServer(item.getIndex());

            m_adapter.remove(item);
        }

        m_adapter.notifyDataSetChanged();
        toggleShowNotExistsGroupText();

//        EmergencyManager.setEmergencycontacts(m_listViewData);
//        try {
//            FileManager.writeXmlEmergencyContacts(this, m_listViewData);
//        } catch (IOException e) {
//            Log.e(TAG, "removeSeletedItems: Can not write Emergency contacts into xml", e);
//            e.printStackTrace();
//        }
    }

    private void makeGroup() {
        Intent intent = new Intent(this, MakeGroupActivity.class);
        startActivityForResult(intent, REQUEST_MAKE_GROUP);
    }

    private void addGroup(MemberList item) {
        if ( m_listViewData.size() == 0 ) {
            m_noExistsTv.setVisibility(View.GONE);
        }
        m_listViewData.add(item);
        m_adapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_MAKE_GROUP:
//                setGroupList();
                // TODO: 04/01/2019 make group list
//                String nameArrStr = data.getStringExtra("membersNames");
//                nameArrStr = nameArrStr.replace("[","").replace("]","");
//                List<String> listNames = Arrays.asList(nameArrStr.split(","));
//
//                addGroup(new MemberList(listNames, UserManager.getUserName()));
                break;
        }
    }
}
