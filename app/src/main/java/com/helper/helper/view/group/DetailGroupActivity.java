package com.helper.helper.view.group;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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
import com.helper.helper.model.MemberList;
import com.helper.helper.model.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.acl.Group;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class DetailGroupActivity extends ListActivity {

    private final static String TAG = DetailGroupActivity.class.getSimpleName() + "/DEV";

    public static final int REQUSET_DETAIL_GROUP = 2002;

    private List<Member> m_listViewData = new ArrayList<>();
    private ListView m_listView;

    /******************* Define widgtes in view *******************/
    private DetailMemberListAdapter m_adapter;
    private FloatingActionButton m_patternBtn;
    /**************************************************************/

    private List<String> m_addedMembersArr = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_group);

        String names = getIntent().getStringExtra("memberNames");
        String nameArrStr = names.replace("[","").replace("]","");
        List<String> nameList = Arrays.asList(nameArrStr.split(","));


        for (int i = 0; i < nameList.size(); i++) {
            Member member = new Member("bicycle", nameList.get(i));
            m_listViewData.add(member);
        }

        /******************* Connect widgtes with layout *******************/
        ImageView backImg = findViewById(R.id.backImg);
        m_patternBtn = findViewById(R.id.setPatternFab);
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

        m_adapter = new DetailMemberListAdapter(this, m_listViewData);
        m_adapter.notifyDataSetChanged();
        setListAdapter(m_adapter);

        /******************* Make Listener in View *******************/
        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        m_patternBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: 06/01/2019 execute pattern
            }
        });
        /*************************************************************/
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUSET_DETAIL_GROUP:
                // TODO: 04/01/2019 make group list
                String memberArrStr = data.getStringExtra("membersInfo");
                memberArrStr = memberArrStr.replace("[","").replace("]","");

                List<String> listMembers = Arrays.asList(memberArrStr.split(","));

//                addGroup(new MemberList(listMembers.size(), UserManager.getUserName()));

                Log.d(TAG, "onActivityResult: " + memberArrStr);
                break;
        }
    }
}
