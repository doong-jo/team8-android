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
import android.widget.Toast;

import com.helper.helper.R;

import java.util.ArrayList;
import java.util.List;

public class ContactActivity extends ListActivity {
    private final static String TAG = ContactActivity.class.getSimpleName() + "/DEV";

    private static final int REQUEST_ENABLE_CONTACTS = 2001;

    private List<Item> listViewData;
    private ListView listView;
    private ItemListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        listViewData = new ArrayList<Item>();

        // Create the adapter to render our data
        // --
        adapter = new ItemListAdapter(this, listViewData);
        setListAdapter(adapter);

        // Get some views for later use
        // --
        listView = getListView();
        listView.setItemsCanFocus(false);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
    }

//    public void onButtonClick(View v) {
//        switch (v.getId()) {
//            case R.id.viewCheckedIdsButton:
//                showSelectedItemIds();
//                break;
//            case R.id.viewCheckedItemsButton:
//                showSelectedItems();
//                break;
//            case R.id.toggleChoiceModeButton:
////                toggleChoiceMode();
//                break;
//        }
//    }

    /**
     * Change the list selection mode
     */
//    private void toggleChoiceMode() {
//        clearSelection();
//
//        final int currentMode = listView.getChoiceMode();
//        switch (currentMode) {
//            case ListView.CHOICE_MODE_NONE:
//                listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
//                Toast.makeText(this, "List choice mode: SINGLE", Toast.LENGTH_SHORT).show();
//                break;
//            case ListView.CHOICE_MODE_SINGLE:
//                listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
//                Toast.makeText(this, "List choice mode: MULTIPLE", Toast.LENGTH_SHORT).show();
//                break;
//            case ListView.CHOICE_MODE_MULTIPLE:
//                listView.setChoiceMode(ListView.CHOICE_MODE_NONE);
//                Toast.makeText(this, "List choice mode: NONE", Toast.LENGTH_SHORT).show();
//                break;
//        }
//    }

    /**
     * Show a message giving the selected item captions
     */
    private void showSelectedItems() {
        final StringBuffer sb = new StringBuffer("Selection: ");

        // Get an array that tells us for each position whether the item is
        // checked or not
        // --
        final SparseBooleanArray checkedItems = listView.getCheckedItemPositions();
        if (checkedItems == null) {
            Toast.makeText(this, "No selection info available", Toast.LENGTH_LONG).show();
            return;
        }

        // For each element in the status array
        // --
        boolean isFirstSelected = true;
        final int checkedItemsCount = checkedItems.size();
        for (int i = 0; i < checkedItemsCount; ++i) {
            // This tells us the item position we are looking at
            // --
            final int position = checkedItems.keyAt(i);

            // This tells us the item status at the above position
            // --
            final boolean isChecked = checkedItems.valueAt(i);

            if (isChecked) {
                if (!isFirstSelected) {
                    sb.append(", ");
                }
                sb.append(listViewData.get(position).getName());
                isFirstSelected = false;
            }
        }

        // Show a message with the countries that are selected
        // --
        Toast.makeText(this, sb.toString(), Toast.LENGTH_LONG).show();
    }

    /**
     * Show a message giving the selected item IDs. There seems to be a bug with ListView#getCheckItemIds() on Android
     * 1.6 at least @see http://code.google.com/p/android/issues/detail?id=6609
     */
    private void showSelectedItemIds() {
        final StringBuffer sb = new StringBuffer("Selection: ");

        // Get an array that contains the IDs of the list items that are checked
        // --
        final long[] checkedItemIds = listView.getCheckItemIds();
        if (checkedItemIds == null) {
            Toast.makeText(this, "No selection", Toast.LENGTH_LONG).show();
            return;
        }

        // For each ID in the status array
        // --
        boolean isFirstSelected = true;
        final int checkedItemsCount = checkedItemIds.length;
        for (int i = 0; i < checkedItemsCount; ++i) {
            if (!isFirstSelected) {
                sb.append(", ");
            }
            sb.append(checkedItemIds[i]);
            isFirstSelected = false;
        }

        // Show a message with the country IDs that are selected
        // --
        Toast.makeText(this, sb.toString(), Toast.LENGTH_LONG).show();
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
                    String number = cursor.getString(1);   //1은 번호를 받아옵니다.

                    Log.d(TAG, "onActivityResult contacts name : " + name);
                    Log.d(TAG, "onActivityResult contacts number : " + number);

                    cursor.close();

                    listViewData.add(new Item(name, number));
                    setListAdapter(adapter);
                } else {
                    Toast.makeText(this, "You can't add contact (Please allow permisson)", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    public void showInternalContacts(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setData(ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        startActivityForResult(intent, REQUEST_ENABLE_CONTACTS);
    }
}
