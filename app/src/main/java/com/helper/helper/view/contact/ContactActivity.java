package com.helper.helper.view.contact;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.widget.AppCompatCheckBox;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.helper.helper.R;
import com.helper.helper.controller.EmergencyManager;
import com.helper.helper.controller.FileManager;
import com.helper.helper.model.ContactItem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ContactActivity extends ListActivity {

    private final static String TAG = ContactActivity.class.getSimpleName() + "/DEV";

    private static final int REQUEST_ENABLE_CONTACTS = 2001;

    /******************* Define widgtes in view *******************/
    private List<ContactItem> m_listViewData;
    private ListView m_listView;
    private ItemListAdapter adapter;

    private TextView m_deleteToggle;
    private ImageView m_deleteTrigger;
    /**************************************************************/

    private List<Integer> m_chekcedDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        m_chekcedDelete = new ArrayList<>();

        /******************* Connect widgtes with layout *******************/
        m_deleteTrigger = findViewById(R.id.backImg);
        m_deleteToggle = findViewById(R.id.contactDeleteToggle);
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
        try {
            m_listViewData = FileManager.readXmlEmergencyContacts(this);
            EmergencyManager.setEmergencycontacts(m_listViewData);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if( m_listViewData == null ) {
            m_listViewData = new ArrayList<ContactItem>();
        }
        adapter = new ItemListAdapter(this, m_listViewData);
        setListAdapter(adapter);

        toggleShowNotExistsContactsText();

        /******************* Make Listener in View *******************/
        m_deleteTrigger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean bIsDelete = m_deleteToggle.getText().toString().equals(getString(R.string.contact_delete));

                if( bIsDelete ) { finish(); }
                else {
                    final int firstListItemPosition = m_listView.getFirstVisiblePosition();
                    final int lastListItemPosition = firstListItemPosition + m_listView.getChildCount() - 1;

                    m_chekcedDelete.clear();
                    for (int i = firstListItemPosition; i <= lastListItemPosition; i++) {
                        View itemView = m_listView.getChildAt(i);

                        if( itemView == null ) { continue; }

                        AppCompatCheckBox chkBox =  itemView.findViewById(R.id.deleteChk);
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
                boolean bIsDelete = m_deleteToggle.getText().toString().equals(getString(R.string.contact_delete));
                adapter.setDeleteMode(bIsDelete);
                adapter.notifyDataSetChanged();

                if ( bIsDelete ) {
                    m_deleteToggle.setText(getString(R.string.contact_cancel));
                    m_deleteTrigger.setImageResource(R.drawable.ic_trashcan);
                } else {
                    m_deleteToggle.setText(getString(R.string.contact_delete));
                    m_deleteTrigger.setImageResource(R.drawable.ic_back);
                }
            }
        });
        /*************************************************************/
    }

    private void toggleShowNotExistsContactsText() {
        TextView noExistsTextView = (TextView) findViewById(R.id.textViewNoContactsList);

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
            ContactItem item = adapter.getItem(I-removeCnt++);
            adapter.remove(item);
        }

        adapter.notifyDataSetChanged();
        toggleShowNotExistsContactsText();

        EmergencyManager.setEmergencycontacts(m_listViewData);
        try {
            FileManager.writeXmlEmergencyContacts(this, m_listViewData);
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
                    if (cursor != null) {
                        cursor.moveToFirst();
                    } else {
                        return;
                    }

                    String name = cursor.getString(0);        //0 : get Name
                    String number = cursor.getString(1);      //1 : get Phone

                    cursor.close();

                    m_listViewData.add(new ContactItem(name, number));

                    adapter.notifyDataSetChanged();

                    toggleShowNotExistsContactsText();

                    try {
                        FileManager.writeXmlEmergencyContacts(this, m_listViewData);
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
}
