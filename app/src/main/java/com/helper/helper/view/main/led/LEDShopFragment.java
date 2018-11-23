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
import com.helper.helper.view.widget.LEDRankCardView;
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
    private final static int LED_SHOW_MAX = 30;

    /******************* Define widgtes in view *******************/
    private GridLayout m_newGrid;
    private GridLayout m_caregoryGrid;
    private GridLayout m_freeGrid;
    private GridLayout m_paidGrid;
    /**************************************************************/

    public LEDShopFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
        View view = inflater.inflate( R.layout.fragment_shop, container, false );
        /******************* Connect widgtes with layout *******************/
        m_caregoryGrid = view.findViewById(R.id.categoryGrid);
        m_newGrid = view.findViewById(R.id.newGrid);
        m_freeGrid = view.findViewById(R.id.freeGrid);
        m_paidGrid = view.findViewById(R.id.paidGrid);
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
            try{
                jsonObject.put("limit", LED_SHOW_MAX);
                jsonObject.put("order",-1);
                jsonObject.put("sort", LED.KEY_CREATE_TIME);
            } catch (JSONException e) {
                e.printStackTrace();
            }

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

            jsonObject = new JSONObject();
            try{
                jsonObject.put("limit", LED_SHOW_MAX);
                jsonObject.put("order",-1);
                jsonObject.put("sort", LED.KEY_DOWNLOADCNT);
            } catch (JSONException e) {
                e.printStackTrace();
            }

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

                        List<LED> ledFreeList = new ArrayList<>();
                        List<LED> ledPaidList = new ArrayList<>();

                        for(LED led:ledList){
                            if(led.getType().equals(LED.LED_TYPE_FREE)){
                                ledFreeList.add(led);
                            }
                            else{
                                ledPaidList.add(led);
                            }
                        }

                        LED[] ledFreeArr = ledFreeList.toArray(new LED[ledFreeList.size()]);
                        LED[] ledPaidArr = ledPaidList.toArray(new LED[ledPaidList.size()]);

                        final LED[] finalLEDFreeList = ledFreeArr;
                        final LED[] finalLEDPaidList = ledPaidArr;

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                makeRankCard(m_freeGrid, finalLEDFreeList);
                                makeRankCard(m_paidGrid, finalLEDPaidList);
                            }
                        });
                    }

                    @Override
                    public void onError(String err) {

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

    private void makeRankCard(GridLayout grid, LED[] list) {
        int ledCnt = 1;

        for (LED ledData : list) {
            LEDRankCardView rankCardViewLED = new LEDRankCardView(getActivity());

            /** set Bitmap Image (character) **/
            File f=new File(CommonManager.getOpenLEDFilePath(
                    getActivity(),
                    ledData.getIndex(),
                    getString(R.string.gif_format)));
            try {
                Bitmap cardImageBitmap = BitmapFactory.decodeStream(new FileInputStream(f));
                rankCardViewLED.setCardViewImg(cardImageBitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            rankCardViewLED.setCardviewNumTxt(ledCnt++);
            rankCardViewLED.setCardviewNameTxt(ledData.getName());
            rankCardViewLED.setOnClickCustomDialogEnable(LEDCardView.DOWNLOAD_DIALOG_TYPE, ledData, getActivity());
            grid.addView(rankCardViewLED);
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
}

