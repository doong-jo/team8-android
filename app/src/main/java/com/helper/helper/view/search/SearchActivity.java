package com.helper.helper.view.search;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import com.fasterxml.jackson.core.JsonParser;
import com.helper.helper.R;
import com.helper.helper.controller.BTManager;
import com.helper.helper.controller.CommonManager;
import com.helper.helper.controller.DownloadImageTask;
import com.helper.helper.controller.HttpManager;
import com.helper.helper.controller.SharedPreferencer;
import com.helper.helper.controller.UserManager;
import com.helper.helper.enums.Collection;
import com.helper.helper.interfaces.HttpCallback;
import com.helper.helper.interfaces.ValidateCallback;
import com.helper.helper.model.LED;
import com.helper.helper.model.User;
import com.helper.helper.view.widget.DialogLED;
import com.helper.helper.view.widget.SearchEditTextAddonControl;
import com.snatik.storage.Storage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class SearchActivity extends AppCompatActivity
{

    private final static String TAG = SearchActivity.class.getSimpleName()+"/DEV";
    public static final int NORMAL_DIALOG_TYPE = 0;
    public static final int DETAIL_DIALOG_TYPE = 1;
    public static final int DOWNLOAD_DIALOG_TYPE = 2;
    public static final int MAX_RECORDS = 5;

    private static final String PREFERENCER_SEARCH_RECORD = "SEARCH_RECORD";

    /******************* Define widgtes in view *******************/
    private List<LED> m_searchLEDItems;
    private ListView m_listView;
    private SearchEditTextAddonControl m_searchInput;
    private ImageView m_backLEDShopFragment;
    private SweetAlertDialog m_detailDlg;
    /**************************************************************/

    private SearchItemListAdapter m_adapter;
    private JSONArray m_searchItemArr;
    private List<LED> m_searchLEDRecord;
    private SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        final Activity thisActvity = this;

        /******************* Connect widgtes with layout *******************/
        m_listView = (ListView) findViewById(R.id.searchList);
        m_searchInput = findViewById(R.id.searchInput);
        m_backLEDShopFragment = (ImageView) findViewById(R.id.backLEDShopFragment);
        /*******************************************************************/

        m_searchInput.setInputType(InputType.TYPE_CLASS_TEXT);
        m_searchInput.setImeOption(EditorInfo.IME_ACTION_DONE);

        m_searchLEDItems = new ArrayList<>();
        m_searchLEDRecord = new ArrayList<>();
        // TODO: 2018-11-25 get preferencer data (key : search_record)
        m_searchItemArr = new JSONArray();
        pref = SharedPreferencer.getSharedPreferencer(this, UserManager.getUserEmail(), MODE_PRIVATE);
        final String strItemRecord = pref.getString(PREFERENCER_SEARCH_RECORD, "");

        getLastestSearch(strItemRecord);

        m_adapter = new SearchItemListAdapter(this, m_searchLEDItems);
        m_searchLEDItems.addAll(m_searchLEDRecord);


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
                    if( m_searchItemArr.length() != 0 ) {
                        m_searchLEDRecord.clear();
                        getLastestSearch(m_searchItemArr.toString());
                        m_searchLEDItems.addAll(m_searchLEDRecord);
                    }
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

        m_listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                LED ledInfo= m_searchLEDItems.get(position);
                m_detailDlg = makeDownloadDlg(thisActvity, ledInfo);

                saveJSONSearchRecordItem(ledInfo);

                m_detailDlg.setCustomView(new DialogLED(thisActvity, DOWNLOAD_DIALOG_TYPE, ledInfo));
                m_detailDlg.show();
            }
        });

        /*************************************************************/
    }

     private SweetAlertDialog makeDownloadDlg(final Activity activity, final LED ledData) {
        // TODO: 16/11/2018 if exist LED -> disable confirm button
        Storage internalStorage = new Storage(activity);
        String path = internalStorage.getInternalFilesDirectory();
        String dir = path + File.separator + DownloadImageTask.DOWNLOAD_PATH;
        String openFilePathGif = dir.concat(File.separator)
                .concat(ledData.getIndex())
                .concat(activity.getString(R.string.gif_format));

        String openFilePathpng = dir.concat(File.separator)
                .concat(ledData.getIndex())
                .concat(activity.getString(R.string.png_format));

        SweetAlertDialog downloadDlg = new SweetAlertDialog(activity, SweetAlertDialog.NORMAL_TYPE)
                .setTitleText(ledData.getIndex().split("_")[1]);


        if( !internalStorage.isFileExist(openFilePathGif) || !internalStorage.isFileExist(openFilePathpng) ) {
            downloadDlg/** Click Download **/
                    .setCancelText(activity.getString(R.string.led_dialog_cancel))
                    .setConfirmButton(activity.getString(R.string.led_dialog_download), new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(final SweetAlertDialog sweetAlertDialog) {
                            DownloadImageTask downloadUserDataLED = new DownloadImageTask(activity, new ValidateCallback() {
                                @Override
                                public void onDone(int resultCode) throws JSONException {
                                    if( resultCode == DownloadImageTask.DONE_LOAD_LED_IMAGES ) {
                                        /** Update user info **/
                                        User user = UserManager.getUser();
                                        if( !user.getUserLEDIndicies().contains(ledData.getIndex()) ) {
                                            user.addBookmarkLEDIndex(ledData.getIndex());

                                            JSONObject jsonObj = new JSONObject();
                                            jsonObj.put(User.KEY_LED_INDICIES, user.getUserLEDIndicies());

                                            UserManager.updateUserInfoServerAndXml(activity, jsonObj);
                                        }

                                        JSONObject jsonObj = new JSONObject();
                                        jsonObj.put(LED.KEY_INDEX, ledData.getIndex());
                                        jsonObj.put(LED.KEY_DOWNLOADCNT, 1);

                                        HttpManager.useCollection(activity.getString(R.string.collection_led));

                                        /** Increase LED's downloadcount **/
                                        HttpManager.requestHttp(jsonObj, "index", "PUT", "downloadcount/", new HttpCallback() {
                                            @Override
                                            public void onSuccess(JSONArray jsonArray) { }

                                            @Override
                                            public void onError(String err) { }
                                        });

                                        activity.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                sweetAlertDialog
                                                        .setTitleText(activity.getString(R.string.led_download_complete))
                                                        /** Click Show 8 **/
                                                        .setConfirmButton(activity.getString(R.string.led_dialog_showon), new SweetAlertDialog.OnSweetClickListener()
                                                        {
                                                            @Override
                                                            public void onClick(final SweetAlertDialog sweetAlertDialog) {
                                                                BTManager.setShowOnDevice(activity, ledData.getIndex());
                                                                sweetAlertDialog.dismissWithAnimation();
                                                            }})
                                                        .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                                            }
                                        });
                                    }
                                }
                            });
                            /** Download User's LED **/
                            // uri + ledIndex
                            downloadUserDataLED.execute(CommonManager.getUriStringArrOfLED(activity.getString(R.string.server_uri), ledData.getIndex()));

                        }
                    });
        } else {
            downloadDlg.showCancelButton(false);
            downloadDlg.setConfirmButton("Already downloaded", null);
        }

        return downloadDlg;
    }

    private void getLastestSearch(String strItemRecord){
        List<JSONObject> jsonList = new ArrayList<>();

        try {
            m_searchItemArr = new JSONArray(strItemRecord);
            for (int i = m_searchItemArr.length()-1; i >=0 ; --i) {
                jsonList.add(new JSONObject(m_searchItemArr.get(i).toString()));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        for(JSONObject object:jsonList){
            try {
                LED ledInfo = new LED.Builder()
                        .index(object.getString(LED.KEY_INDEX))
                        .name(object.getString(LED.KEY_NAME))
                        .category(object.getString(LED.KEY_CATEGORY))
                        .build();

                m_searchLEDRecord.add(ledInfo);
            }
            catch (JSONException e){
                e.printStackTrace();
            }
        }
    }

    private void trySearch() {
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
                        final int arrLen = searchNamejsonArray.length();


                        if(m_searchInput.getText().equals("")){
                            return;
                        }

                        m_searchLEDItems.clear();

                        if (arrLen != 0) {

                            for (int i = 0; i < arrLen; i++) {
                                JSONObject object = searchNamejsonArray.getJSONObject(i);
                                LED led = new LED.Builder()
                                        .index(object.getString(LED.KEY_INDEX))
                                        .name(object.getString(LED.KEY_NAME))
                                        .category(object.getString(LED.KEY_CATEGORY))
                                        .build();

                                m_searchLEDItems.add(led);
                            }
                        }

                         //nodify
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                m_adapter.notifyDataSetChanged();
                            }});
                    }

                    @Override
                    public void onError(String err) {
                        Log.d(TAG, "SearchActivity onError: " + err);
                    }});
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveJSONSearchRecordItem(LED ledInfo) {

        JSONObject json = new JSONObject();
        try {
            json.put("name", ledInfo.getName());
            json.put("category", ledInfo.getCategory());
            json.put("index", ledInfo.getIndex());
            json.put("date", new Date());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        addJSONSearchRecordItem(json);
        SharedPreferencer.putJSONObject(PREFERENCER_SEARCH_RECORD, m_searchItemArr);
    }

    private void addJSONSearchRecordItem(JSONObject json) {
        // TODO: 2018-11-25 limit : 5
        int length = m_searchItemArr.length();

        if(m_searchItemArr !=null) {
            for (int i=0; i<length; i++) {
                try {
                    if (m_searchItemArr.getJSONObject(i).getString("index").equals(json.getString("index"))){
                        m_searchItemArr.remove(i);
                    }
                }
                catch (JSONException e){
                    e.printStackTrace();
                }
            }

            if(length >= MAX_RECORDS){
                m_searchItemArr.remove(0);
            }
        }
        m_searchItemArr.put(json);
    }
}
