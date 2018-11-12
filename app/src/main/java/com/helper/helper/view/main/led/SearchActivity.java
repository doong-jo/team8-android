package com.helper.helper.view.main.led;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

import com.helper.helper.R;
import com.helper.helper.controller.HttpManager;
import com.helper.helper.interfaces.HttpCallback;
import com.helper.helper.model.LED;
import com.helper.helper.model.SearchItem;
import com.helper.helper.view.widget.SearchEditTextAddonControl;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity
{

    private final static String TAG = SearchActivity.class.getSimpleName()+"/DEV";

    /******************* Define widgtes in view *******************/
    private List<SearchItem> m_listItems;
    private List<LED> m_searchLEDItems;
    private ListView m_listView;
    private SearchEditTextAddonControl m_searchInput;
    private ImageView m_backLEDShopFragment;
    /**************************************************************/

    private ArrayList<SearchItem> m_arrayList;
    private SearchItemListAdapter m_adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        /******************* Connect widgtes with layout *******************/
        m_listView = (ListView) findViewById(R.id.searchList);
        m_searchInput = (SearchEditTextAddonControl) findViewById(R.id.searchInput);
        m_backLEDShopFragment = (ImageView) findViewById(R.id.backLEDShopFragment);
        /*******************************************************************/

        m_listItems = new ArrayList<SearchItem>();
        m_arrayList = new ArrayList<SearchItem>();
        m_searchLEDItems = new ArrayList<LED>();
        settingList();

        m_adapter = new SearchItemListAdapter(this, m_listItems);
        m_listView.setAdapter(m_adapter);

        /******************* Make Listener in View *******************/
        m_searchInput.setEditTextChangedEvent(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                String inputText = m_searchInput.getText();
                    search(inputText);
            }
        });

        m_backLEDShopFragment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        /*************************************************************/
    }

    // search method
    public void search(String charText) {
        m_listItems.clear();
        if (charText.length() != 0) {

            for(int i = 0;i < m_arrayList.size(); i++)
            {
                if (m_arrayList.get(i).getTitle().toLowerCase().contains(charText))
                {
                    m_listItems.add(m_arrayList.get(i));
                }
            }
        }

        m_adapter.notifyDataSetChanged();
    }

    private void settingList(){
        m_arrayList.add(new SearchItem("girl","character"));
        m_arrayList.add(new SearchItem("girl&boy","character"));
        m_arrayList.add(new SearchItem("blue hat girl","My LED"));
        m_arrayList.add(new SearchItem("boy","My LED"));
        m_arrayList.add(new SearchItem("matt","character"));
        m_arrayList.add(new SearchItem("한글","캐릭터"));
        m_arrayList.add(new SearchItem("girl","character"));
        m_arrayList.add(new SearchItem("girl&boy","character"));
        m_arrayList.add(new SearchItem("blue hat girl","My LED"));
        m_arrayList.add(new SearchItem("boy","My LED"));
        m_arrayList.add(new SearchItem("matt","character"));
        m_arrayList.add(new SearchItem("girl","character"));
        m_arrayList.add(new SearchItem("girl&boy","character"));
        m_arrayList.add(new SearchItem("blue hat girl","My LED"));
        m_arrayList.add(new SearchItem("boy","My LED"));
        m_arrayList.add(new SearchItem("matt","character"));
        m_arrayList.add(new SearchItem("girl","character"));
        m_arrayList.add(new SearchItem("girl&boy","character"));
        m_arrayList.add(new SearchItem("blue hat girl","My LED"));
        m_arrayList.add(new SearchItem("boy","My LED"));
        m_arrayList.add(new SearchItem("matt","character"));
        m_arrayList.add(new SearchItem("girl&boy","character"));
        m_arrayList.add(new SearchItem("blue hat girl","My LED"));
        m_arrayList.add(new SearchItem("boy","My LED"));
        m_arrayList.add(new SearchItem("matt","character"));
        m_arrayList.add(new SearchItem("girl","character"));
        m_arrayList.add(new SearchItem("girl&boy","character"));
        m_arrayList.add(new SearchItem("blue hat girl","My LED"));
        m_arrayList.add(new SearchItem("boy","My LED"));
        m_arrayList.add(new SearchItem("matt","character"));
        m_arrayList.add(new SearchItem("girl&boy","character"));
        m_arrayList.add(new SearchItem("blue hat girl","My LED"));
        m_arrayList.add(new SearchItem("boy","My LED"));
        m_arrayList.add(new SearchItem("matt","character"));
        m_arrayList.add(new SearchItem("girl","character"));
        m_arrayList.add(new SearchItem("girl&boy","character"));
        m_arrayList.add(new SearchItem("blue hat girl","My LED"));
        m_arrayList.add(new SearchItem("boy","My LED"));
        m_arrayList.add(new SearchItem("matt","character"));
    }

    public void trySearch(){
        String name = m_searchInput.getText();

        if(HttpManager.useCollection("user")){
            JSONObject reqObject = new JSONObject();
            try{
                reqObject.put("name", name);
            }
            catch (JSONException e){
                e.printStackTrace();
            }

            try{
                HttpManager.requestHttp(reqObject, "GET", new HttpCallback() {
                    @Override
                    public void onSuccess(JSONArray searchNamejsonArray) throws JSONException {
                        int arrLen = searchNamejsonArray.length();

                        if(arrLen != 0){
                            for(int i=0; i<arrLen; ++i){
                                JSONObject object = searchNamejsonArray.getJSONObject(i);
                                LED led = new LED.Builder()
                                        .index(object.getString("index"))
                                        .name(object.getString("name"))
                                        .creator(object.getString("creator"))
                                        .downloadCnt(object.getInt("downloadCnt"))
                                        .type(object.getString("type"))
                                        .build();

                                m_searchLEDItems.add(led);
                             }
                        }
                        else{
                            m_searchLEDItems.clear();
                        }
                        m_adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onError(String err) throws JSONException {
                        Log.d(TAG, "SearchActivity onError: " + err);
                    }
                });
            }
            catch (JSONException e){
                e.printStackTrace();
            }
        }
    }
}
