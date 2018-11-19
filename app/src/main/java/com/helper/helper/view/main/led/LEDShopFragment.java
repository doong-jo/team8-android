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
import com.helper.helper.controller.DownloadImageTask;
import com.helper.helper.controller.FileManager;
import com.helper.helper.model.LEDCategory;
import com.helper.helper.view.category.CategoryActivity;
import com.helper.helper.view.widget.LEDCategoryCardView;
import com.snatik.storage.Storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LEDShopFragment extends Fragment {
    private final static String TAG = LEDShopFragment.class.getSimpleName() + "/DEV";

    /******************* Define widgtes in view *******************/
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

        /*******************************************************************/

        /******************* Make Listener in View *******************/

        /*************************************************************/


        setDataToCategoryCards();

        return view;
    }

    private void setDataToCategoryCards() {
        // 1. Read Xml of category data
        List<LEDCategory> ledCategoryList = new ArrayList<>();
        try {
            ledCategoryList = FileManager.readXmlCategory(getActivity());
        } catch (IOException e) {
            Log.e(TAG, "setDataToCategoryCards: " + "IOException Error.");
            return;
        }

        // 2. Add Cards into gridview layout
        for (final LEDCategory categoryData :
                ledCategoryList) {
            LEDCategoryCardView cardViewLED = new LEDCategoryCardView(getActivity());
            cardViewLED.setCategoryName(categoryData.getName());

            /** set Bitmap Image (character) **/
            File f=new File(getOpenFilePath(categoryData.getCharacter()));
            try {
                Bitmap cardImageBitmap = BitmapFactory.decodeStream(new FileInputStream(f));
                cardViewLED.setCategoryImg(cardImageBitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

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
            m_caregoryGrid.addView(cardViewLED);
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

