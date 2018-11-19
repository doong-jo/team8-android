package com.helper.helper.view.main.led;

import android.os.Bundle;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;

import com.helper.helper.R;
import com.helper.helper.controller.CommonManager;
import com.helper.helper.controller.DownloadImageTask;
import com.helper.helper.controller.FileManager;
import com.helper.helper.controller.HttpManager;
import com.helper.helper.interfaces.HttpCallback;
import com.helper.helper.model.LED;
import com.helper.helper.model.LEDCategory;
import com.helper.helper.view.category.CategoryActivity;
import com.helper.helper.view.widget.DialogLED;
import com.helper.helper.view.widget.LEDCardView;
import com.helper.helper.view.widget.LEDCategoryCardView;
import com.snatik.storage.Storage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LEDShopFragment extends Fragment {
    private final static String TAG = LEDShopFragment.class.getSimpleName() + "/DEV";

    /******************* Define widgtes in view *******************/
    private GridLayout m_newGrid;
    private GridLayout m_caregoryGrid;
    /**************************************************************/

    public LEDShopFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
        View view = inflater.inflate( R.layout.fragment_shop, container, false );
        /******************* Connect widgtes with layout *******************/
        m_caregoryGrid = view.findViewById(R.id.categoryGrid);
        m_newGrid = view.findViewById(R.id.newGrid);
        /*******************************************************************/

        /******************* Make Listener in View *******************/

        /*************************************************************/


        setNewLEDCards();
        setDataToCategoryCards();

        return view;
    }

    private void setNewLEDCards() {
        if( HttpManager.useCollection(getString(R.string.collection_led)) ) {
            JSONObject jsonObject = new JSONObject();

            try {
                HttpManager.requestHttp(jsonObject, "", "GET", "", new HttpCallback() {
                    @Override
                    public void onSuccess(JSONArray jsonArray) throws JSONException {

                        LED[] ledList = new LED[jsonArray.length()];
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject object = (JSONObject) jsonArray.get(i);
                            Date convertDate = null;
                            try {
                                convertDate = new SimpleDateFormat("yyyy-mm-dd", Locale.KOREA).parse(object.getString(LED.KEY_CREATE_TIME));

                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            ledList[i] = new LED(
                                    new LED.Builder()
                                            .index(object.getString(LED.KEY_INDEX))
                                            .name(object.getString(LED.KEY_NAME))
                                            .creator(object.getString(LED.KEY_CREATOR))
                                            .createDate(convertDate)
                                            .category(object.getString(LED.KEY_CATEGORY))
                                            .downloadCnt(object.getInt(LED.KEY_DOWNLOADCNT))
                                            .type(object.getString(LED.KEY_TYPE))
                            );
                        }

                        final LED[] finalLEDList = ledList;
                        // sort by create date ascending
                        Arrays.sort(ledList, new Comparator<LED>() {
                            @Override
                            public int compare(LED l1, LED l2) {
                                return l1.getCreateDate().compareTo(l2.getCreateDate());
                            }
                        });
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                makeNewCard(m_newGrid, finalLEDList);
                            }
                        });

                    }

                    @Override
                    public void onError(String err) throws JSONException {

                    }
                });
            } catch (JSONException e ) {
                e.printStackTrace();
            }
        }
    }

    private void makeNewCard(GridLayout grid, LED[] list) {
        for (LED ledData :
                list) {
            LEDCardView cardViewLED = new LEDCardView(getActivity());

            /** set Bitmap Image (character) **/
            File f=new File(CommonManager.getOpenLEDFilePath(
                    getActivity(),
                    ledData.getIndex(),
                    getString(R.string.gif_format)));
            try {
                Bitmap cardImageBitmap = BitmapFactory.decodeStream(new FileInputStream(f));
                cardViewLED.setCardImageView(cardImageBitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            cardViewLED.setCardNameText(ledData.getName());
            cardViewLED.setOnClickCustomDialogEnable(LEDCardView.DOWNLOAD_DIALOG_TYPE, ledData, getActivity());
            grid.addView(cardViewLED);
        }
    }

    private void setDataToCategoryCards() {
        // 1. Read Xml of category data
        List<LEDCategory> ledCategoryList;
        try {
            ledCategoryList = FileManager.readXmlCategory(getActivity());
        } catch (IOException e) {
            Log.e(TAG, "setDataToCategoryCards: " + "IOException Error.");
            return;
        }

        // 2. Add Cards into gridview layout
        makeCategoryCard(m_caregoryGrid, ledCategoryList);
    }

    private void makeCategoryCard(GridLayout grid, List<LEDCategory> list) {
        for (final LEDCategory categoryData :
                list) {
            LEDCategoryCardView cardViewLED = new LEDCategoryCardView(getActivity());

            /** set Bitmap Image (character) **/
            File f=new File(CommonManager.getOpenLEDFilePath(
                    getActivity(),
                    categoryData.getCharacter(),
                    getString(R.string.gif_format)));
            try {
                Bitmap cardImageBitmap = BitmapFactory.decodeStream(new FileInputStream(f));
                cardViewLED.setCategoryImg(cardImageBitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            cardViewLED.setCategoryName(categoryData.getName());
            cardViewLED.setBkgColor(Color.parseColor(categoryData.getBkgColor()));
            cardViewLED.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent=new Intent(getActivity(),CategoryActivity.class);
                    intent.putExtra(LEDCategory.KEY_NAME, categoryData.getName());
                    intent.putExtra(LEDCategory.KEY_BKGCOLOR, categoryData.getBkgColor());
                    intent.putExtra(LEDCategory.KEY_NOTICE, categoryData.getNotice());
                    intent.putExtra(LEDCategory.KEY_CHARACTER, categoryData.getCharacter());
                    startActivity(intent);
                }
            });
            grid.addView(cardViewLED);
        }
    }

    private String getOpenFilePath(String ledIndex) {
        Storage internalStorage = new Storage(getActivity());
        String path = internalStorage.getInternalFilesDirectory();
        String dir = path + File.separator + DownloadImageTask.DOWNLOAD_PATH;
        String openFilePath = dir + File.separator + ledIndex + getString(R.string.gif_format);

        return openFilePath;
    }
}

