package com.helper.helper.view.search;

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
        //settingList();

        m_adapter = new SearchItemListAdapter(this, m_searchLEDItems);
        m_listView.setAdapter(m_adapter);

        /******************* Make Listener in View *******************/
        m_searchInput.setEditTextChangedEvent(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {

                if ( !m_searchInput.getText().equals("") ) {
                    trySearch();
                } else {
                    m_searchLEDItems.clear();
                    m_adapter.notifyDataSetChanged();
                }
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

    // search method
    public void search(String charText) {
        m_listItems.clear();

        if (charText.length() != 0) {
            trySearch();
        }

        m_adapter.notifyDataSetChanged();
    }

    public void trySearch() {
        String name = m_searchInput.getText();

        if (HttpManager.useCollection(getString(R.string.collection_led))) {
            JSONObject reqObject = new JSONObject();
            try {
                name = "^"+name;
                reqObject.put(LED.KEY_NAME, name);
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                HttpManager.requestHttp(reqObject, "", "GET", "regex", new HttpCallback() {

                    @Override
                    public void onSuccess(JSONArray searchNamejsonArray) throws JSONException {
                        int arrLen = searchNamejsonArray.length();
                        // clear
                        m_searchLEDItems.clear();

                        if (arrLen != 0) {
                            for (int i = 0; i < arrLen; ++i) {
                                JSONObject object = searchNamejsonArray.getJSONObject(i);
                                LED led = new LED.Builder()
                                        .index(object.getString(LED.KEY_INDEX))
                                        .name(object.getString(LED.KEY_NAME))
                                        .creator(object.getString(LED.KEY_CREATOR))
                                        .downloadCnt(object.getInt(LED.KEY_DOWNLOADCNT))
                                        .type(object.getString(LED.KEY_TYPE))
                                        .build();

                                m_searchLEDItems.add(led);
                            }

                            //nodify
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    m_adapter.notifyDataSetChanged();
                                }
                            });

                        }
                    }

                    @Override
                    public void onError(String err) throws JSONException {
                        Log.d(TAG, "SearchActivity onError: " + err); }});
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
