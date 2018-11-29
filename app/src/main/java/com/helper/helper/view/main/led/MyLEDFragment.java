package com.helper.helper.view.main.led;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;

import com.google.android.gms.common.internal.service.Common;
import com.helper.helper.R;
import com.helper.helper.controller.CommonManager;
import com.helper.helper.controller.DownloadImageTask;
import com.helper.helper.controller.HttpManager;
import com.helper.helper.controller.UserManager;
import com.helper.helper.interfaces.HttpCallback;
import com.helper.helper.model.LED;
import com.helper.helper.view.widget.LEDCardView;
import com.snatik.storage.Storage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

public class MyLEDFragment extends Fragment {
    private final static String TAG = MyLEDFragment.class.getSimpleName() + "/DEV";

    /******************* Define widgtes in view *******************/
    private ImageView m_ledGridToggle;
    private ImageView m_bookmarkToggle;
    private GridLayout m_ledGridLayout;
    /**************************************************************/

    private boolean m_bIsBookmarkView = true;
    private Map<String, LED> m_mapDataLED;
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

        final String[] ledIndicies = CommonManager.splitNoWhiteSpace(UserManager.getUser()
                .getUserLEDIndicies()
                .split("\\[")[1]
                .split("]")[0]);

        for (int i = 0; i < ledIndicies.length; i++) {
            ledIndicies[i] = ledIndicies[i].replaceAll("\"", "");
        }

        m_ledGridToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ( !m_bIsBookmarkView ) {
                    m_ledGridToggle.setImageResource(R.drawable.ic_cloud_download_black_selected);
                    m_bookmarkToggle.setImageResource(R.drawable.ic_bookmark_black);
                    startChangingLEDinGrid(m_ledGridLayout, ledIndicies);
                }
            }
        });

        m_bookmarkToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ( m_bIsBookmarkView ) {
                    m_ledGridToggle.setImageResource(R.drawable.ic_cloud_download_black);
                    m_bookmarkToggle.setImageResource(R.drawable.ic_bookmark_black_selected);
                    startChangingLEDinGrid(m_ledGridLayout, ledIndicies);
                }
            }
        });

        /** get LED Information from server **/

        m_mapDataLED = new HashMap<>();

        if( HttpManager.useCollection(getString(R.string.collection_led)) ) {
            try {
                JSONObject reqObject = new JSONObject();
                JSONObject inObject = new JSONObject();

                inObject.put("$in", ledIndicies);
                reqObject.put("index", inObject);

                HttpManager.requestHttp(reqObject, "", "GET", "", new HttpCallback() {
                    @Override
                    public void onSuccess(JSONArray jsonArray) throws JSONException {
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            LED led = new LED(
                                    new LED.Builder()
                                            .index(jsonObject.getString(LED.KEY_INDEX))
                                            .name(jsonObject.getString(LED.KEY_NAME))
                                            .creator(jsonObject.getString(LED.KEY_CREATOR))
                                            .downloadCnt(jsonObject.getInt(LED.KEY_DOWNLOADCNT))
                                            .type(jsonObject.getString(LED.KEY_TYPE))
                            );
                            m_mapDataLED.put(led.getIndex(), led);
                        }

                        /** Add GridView (Bookmared or Downloaded) **/
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                startChangingLEDinGrid(m_ledGridLayout, ledIndicies);
                            }
                        });
                    }

                    @Override
                    public void onError(String err) {
                        // TODO: 26/11/2018 OFFLINE LED Info
                        startChangingLEDinGrid(m_ledGridLayout, ledIndicies);
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        /*************************************************************/

        return view;
    }

    private void startChangingLEDinGrid(GridLayout grid, String[] userIndicies) {
        m_ledGridLayout.removeAllViews();

        String[] bookmakred = CommonManager.splitNoWhiteSpace(UserManager.getUser()
                .getUserBookmarked()
                .split("\\[")[1]
                .split("]")[0]);

        for (int i = 0; i < userIndicies.length; i++) {
            String ledIndex = userIndicies[i].replaceAll("\"", "");

            LED led = m_mapDataLED.get(ledIndex);
            if( m_bIsBookmarkView ) {
                showBookmarkedLED(grid, bookmakred, led);
            } else {
                showDownloadedLED(grid, bookmakred, led);
            }
        }
        m_bIsBookmarkView = !m_bIsBookmarkView;
    }

    private void showBookmarkedLED(GridLayout grid, String[] bookmarkedArr, LED ledInfo) {
        for (String bookmarkedStr:
                bookmarkedArr) {
            if( ledInfo.getIndex().equals(bookmarkedStr)) {
                ledInfo.setBookmarked(true);

                addLEDinGrid(ledInfo, grid);
            }
        }
    }

    private void showDownloadedLED(GridLayout grid, String[] bookmarkedArr, LED ledInfo) {
        boolean IsBookmakred = false;

        for (String bookmarkedStr:
                bookmarkedArr) {
            if( ledInfo.getIndex().equals(bookmarkedStr)) {
                IsBookmakred = true;
            }
        }

        ledInfo.setBookmarked(IsBookmakred);

        addLEDinGrid(ledInfo, grid);
    }

    private void addLEDinGrid(LED ledInfo, GridLayout grid) {
        LEDCardView cardViewLED = new LEDCardView(getActivity());
        cardViewLED.setCardNameText(ledInfo.getIndex().split("_")[1]);

        try {
            File f=new File(CommonManager.getOpenLEDFilePath(getActivity(), ledInfo.getIndex(), getActivity().getString(R.string.gif_format)));
            Bitmap cardImageBitmap = BitmapFactory.decodeStream(new FileInputStream(f));
            cardViewLED.setCardImageView(cardImageBitmap);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }

        cardViewLED.setOnClickCustomDialogEnable(LEDCardView.DETAIL_DIALOG_TYPE, ledInfo, getActivity());
        grid.addView(cardViewLED);
    }
}
