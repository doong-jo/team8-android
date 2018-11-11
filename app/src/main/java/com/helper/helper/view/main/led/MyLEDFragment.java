package com.helper.helper.view.main.led;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;

import com.helper.helper.R;
import com.helper.helper.controller.CommonManager;
import com.helper.helper.controller.DownloadImageTask;
import com.helper.helper.controller.HttpManager;
import com.helper.helper.controller.UserManager;
import com.helper.helper.interfaces.HttpCallback;
import com.helper.helper.model.LED;
import com.helper.helper.view.widget.ImageCardViewAddonText;
import com.snatik.storage.Storage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class MyLEDFragment extends Fragment {
    private final static String TAG = MyLEDFragment.class.getSimpleName() + "/DEV";

    /******************* Define widgtes in view *******************/
    private ImageView m_ledGridToggle;
    private ImageView m_bookmarkToggle;
    private GridLayout m_ledGridLayout;

    private boolean m_bIsBookmarkView = true;
    /**************************************************************/

    public MyLEDFragment() {

    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
        View view = inflater.inflate( R.layout.fragment_myled, container, false );

        /******************* Connect widgtes with layout *******************/
        m_ledGridToggle = view.findViewById(R.id.ledGridView);
        m_bookmarkToggle = view.findViewById(R.id.bookmarkView);
        m_ledGridLayout = view.findViewById(R.id.ledGridLayout);
        /*******************************************************************/

        /******************* Make Listener in View *******************/

        m_ledGridToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ( !m_bIsBookmarkView ) {
                    m_ledGridToggle.setImageResource(R.drawable.ic_cloud_download_black_selected);
                    m_bookmarkToggle.setImageResource(R.drawable.ic_bookmark_black);
                    startchangingLEDinGrid();
                }
            }
        });

        m_bookmarkToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ( m_bIsBookmarkView ) {
                    m_ledGridToggle.setImageResource(R.drawable.ic_cloud_download_black);
                    m_bookmarkToggle.setImageResource(R.drawable.ic_bookmark_black_selected);
                    startchangingLEDinGrid();
                }
            }
        });

        startchangingLEDinGrid();

        /*************************************************************/

        return view;
    }

    private void startchangingLEDinGrid() {
        m_ledGridLayout.removeAllViews();

        String[] ledIndicies = CommonManager.splitNoWhiteSpace(UserManager.getUser()
                .getUserLEDIndicies()
                .split("\\[")[1]
                .split("]")[0]);

        String[] bookmakred = CommonManager.splitNoWhiteSpace(UserManager.getUser()
                .getUserBookmarked()
                .split("\\[")[1]
                .split("]")[0]);

        // TODO: 11/11/2018 get LED Information from server
        // name, creator, type, downloadCnt


        try {
            JSONObject reqObject = new JSONObject();
            JSONObject inObject = new JSONObject();

            inObject.put("$in", ledIndicies);
            reqObject.put("name", inObject);

            HttpManager.requestHttp(reqObject, "GET", new HttpCallback() {
                @Override
                public void onSuccess(JSONArray jsonArray) {
                    Log.d(TAG, "onSuccess: " + jsonArray.toString());
                }

                @Override
                public void onError(String err) {

                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < ledIndicies.length; i++) {
            String ledName = ledIndicies[i];

            if( m_bIsBookmarkView ) {
                showBookmarkedLED(bookmakred, ledName);
            } else {
                showDownloadedLED(bookmakred, ledName);
            }
        }
        m_bIsBookmarkView = !m_bIsBookmarkView;
    }

    private void showBookmarkedLED(String[] bookmarked, String ledName) {
        String creator = "Xman";
        int downloadCnt = 2432;

        for (int j = 0; j < bookmarked.length; j++) {
            if( ledName.equals(bookmarked[j])) {
                LED ledModel = new LED.Builder()
                        .name(ledName)
                        .creator(creator)
                        .bookmarked(true)
                        .type(LED.LED_TYPE_FREE)
                        .downloadCnt(downloadCnt)
                        .build();

                ImageCardViewAddonText cardViewLED = new ImageCardViewAddonText(getActivity());
                cardViewLED.setCardNameText(ledModel.getName().split("_")[1]);

                try {
                    File f=new File(getOpenFilePath(ledName));
                    Bitmap cardImageBitmap = BitmapFactory.decodeStream(new FileInputStream(f));
                    cardViewLED.setCardImageView(cardImageBitmap);
                }
                catch (FileNotFoundException e)
                {
                    e.printStackTrace();
                }

                cardViewLED.setOnClickCustomDialogEnable(ImageCardViewAddonText.DETAIL_DIALOG_TYPE, ledModel, getActivity());
                m_ledGridLayout.addView(cardViewLED);
            }
        }
    }

    private void showDownloadedLED(String[] bookmarked, String ledName) {
        String creator = "Xman";
        int downloadCnt = 2432;
        boolean IsBookmakred = false;

        for (int j = 0; j < bookmarked.length; j++) {
            if( ledName.equals(bookmarked[j])) {
                IsBookmakred = true;
            }
        }

        LED ledModel = new LED.Builder()
                .name(ledName)
                .creator(creator)
                .bookmarked(IsBookmakred)
                .type(LED.LED_TYPE_FREE)
                .downloadCnt(downloadCnt)
                .build();

        ImageCardViewAddonText cardViewLED = new ImageCardViewAddonText(getActivity());
        cardViewLED.setCardNameText(ledModel.getName().split("_")[1]);

        try {
            File f=new File(getOpenFilePath(ledName));
            Bitmap cardImageBitmap = BitmapFactory.decodeStream(new FileInputStream(f));
            cardViewLED.setCardImageView(cardImageBitmap);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }

        cardViewLED.setOnClickCustomDialogEnable(ImageCardViewAddonText.DETAIL_DIALOG_TYPE, ledModel, getActivity());
        m_ledGridLayout.addView(cardViewLED);
    }

    private String getOpenFilePath(String ledName) {
        Storage internalStorage = new Storage(getActivity());
        String path = internalStorage.getInternalFilesDirectory();
        String dir = path + File.separator + DownloadImageTask.DOWNLOAD_PATH;
        String openFilePath = dir + File.separator + ledName + ".gif";

        return openFilePath;
    }
}
