package com.helper.helper.view.main.led;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.support.v4.app.Fragment;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;

//import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.helper.helper.R;
import com.helper.helper.controller.DownloadImageTask;
import com.helper.helper.model.LED;
import com.helper.helper.view.ScrollingActivity;
import com.helper.helper.view.widget.ImageCardViewAddonText;
import com.snatik.storage.Storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class MyLEDFragment extends Fragment {

    /******************* Define widgtes in view *******************/
    private ImageView m_ledGridToggle;
    private ImageView m_bookmarkToggle;
    private GridLayout m_ledGridLayout;

    private boolean m_bIsBookmarkView;
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

        m_bIsBookmarkView = false;

        /******************* Make Listener in View *******************/

        m_ledGridToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ( !m_bIsBookmarkView ) {
                    m_bIsBookmarkView = !m_bIsBookmarkView;
                    m_ledGridToggle.setImageResource(R.drawable.ic_cloud_download_black_selected);
                    m_bookmarkToggle.setImageResource(R.drawable.ic_bookmark_black);
                }
            }
        });

        m_bookmarkToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ( m_bIsBookmarkView ) {
                    m_bIsBookmarkView = !m_bIsBookmarkView;
                    m_ledGridToggle.setImageResource(R.drawable.ic_cloud_download_black);
                    m_bookmarkToggle.setImageResource(R.drawable.ic_bookmark_black_selected);
                }
            }
        });

        ScrollingActivity mainActivity = (ScrollingActivity)getActivity();



        /*************************************************************/

        String ledName = "team8_bird";
        String creator = "Xman";
        int downloadCnt = 2432;
        boolean bookmarked = true;

        /** Add cardview in gridlayout **/
        LED ledModel = new LED.Builder()
                .name(ledName)
                .creator(creator)
                .bookmarked(bookmarked)
                .downloadCnt(downloadCnt)
                .build();

        ImageCardViewAddonText cardViewLED = new ImageCardViewAddonText(getActivity());
        cardViewLED.setCardNameText(ledModel.getName().split("_")[1]);

        Storage internalStorage = new Storage(getActivity());
        String path = internalStorage.getInternalFilesDirectory();
        String dir = path + File.separator + DownloadImageTask.DOWNLOAD_PATH;
        String openFilePath = dir + File.separator + ledName + ".gif";

        try {
            File f=new File(openFilePath);
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
            cardViewLED.setCardImageView(b);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }


        cardViewLED.setOnClickCustomDialogEnable(ImageCardViewAddonText.DETAIL_DIALOG_TYPE, ledModel, mainActivity);
        m_ledGridLayout.addView(cardViewLED);

        return view;
    }
}
