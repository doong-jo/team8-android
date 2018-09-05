package com.helper.helper.contact;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.helper.helper.R;
import com.helper.helper.util.FileManagerUtil;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ContactActivity extends ListActivity {
    private final static String TAG = ContactActivity.class.getSimpleName() + "/DEV";

    private static final int REQUEST_ENABLE_CONTACTS = 2001;

    private List<ContactItem> listViewData;
    private ListView listView;
    private ItemListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        listViewData = new ArrayList<ContactItem>();

        // Create the adapter to render our data
        // --

        // Get some views for later use
        // --
        listView = getListView();
        listView.setItemsCanFocus(false);

        ////////////////Mode Define//////////////////
        // ListView.CHOICE_MODE_SINGLE             //
        // ListView.CHOICE_MODE_MULTIPLE           //
        // ListView.CHOICE_MODE_NONE               //
        /////////////////////////////////////////////

        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        try {
            listViewData = FileManagerUtil.readXmlEmergencyContacts(this);
        } catch (IOException e) {
            e.printStackTrace();
        }

        adapter = new ItemListAdapter(this, listViewData);
        setListAdapter(adapter);

        toggleShowNotExistsContactsText();
    }

    private void toggleShowNotExistsContactsText() {
        TextView noExistsTextView = (TextView) findViewById(R.id.textViewNoContactsList);

        if( listViewData.size() != 0 ) {
            noExistsTextView.setVisibility(View.GONE);
        } else {
            noExistsTextView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Uncheck all the items
     */
    private void clearSelection() {
        final int itemCount = listView.getCount();
        for (int i = 0; i < itemCount; ++i) {
            listView.setItemChecked(i, false);
        }
    }

    private void removeSeletedItems() {
        final StringBuffer sb = new StringBuffer("Selection: ");

        final long[] checkedItemIds = listView.getCheckedItemIds();

        if (checkedItemIds == null) {
            Toast.makeText(this, "No selection", Toast.LENGTH_LONG).show();
            return;
        }

        clearSelection();

        for (long i :
                checkedItemIds) {
            int I = (int)i;

            ContactItem item = adapter.getItem((int) checkedItemIds[I]-I);
            adapter.remove(item);
        }

        adapter.notifyDataSetChanged();

        toggleShowNotExistsContactsText();

        try {
            FileManagerUtil.writeXmlEmergencyContacts(this, listViewData);
        } catch (IOException e) {
            Log.e(TAG, "removeSeletedItems: Can not write Emergency contacts into xml", e);
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode) {
            case REQUEST_ENABLE_CONTACTS:
                if (resultCode == RESULT_OK) {
                    Cursor cursor = getContentResolver().query(data.getData(),
                            new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                                    ContactsContract.CommonDataKinds.Phone.NUMBER}, null, null, null);
                    cursor.moveToFirst();

                    String name = cursor.getString(0);        //0은 이름을 얻어옵니다.
                    String number = cursor.getString(1);      //1은 번호를 받아옵니다.

                    cursor.close();

                    listViewData.add(new ContactItem(name, number));

                    adapter.notifyDataSetChanged();

                    toggleShowNotExistsContactsText();

                    try {
                        FileManagerUtil.writeXmlEmergencyContacts(this, listViewData);
                    } catch (IOException e) {
                        Log.e(TAG, "removeSeletedItems: Can not write Emergency contacts into xml", e);
                        e.printStackTrace();
                    }

                }
                break;
        }
    }

    /**
     * Touch floating button event, Move contact page.
     * @param view
     */

    public void showInternalContacts(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setData(ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        startActivityForResult(intent, REQUEST_ENABLE_CONTACTS);
    }

    /**
     * Touch remove contact button event, Remove listview items.
     * @param view
     */
    public void removeContactsClick(View view) {
        removeSeletedItems();
    }
}
