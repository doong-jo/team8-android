package com.helper.helper.view.main.led;

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
import com.helper.helper.model.LED;
import com.helper.helper.view.ScrollingActivity;
import com.helper.helper.view.widget.ImageCardViewAddonText;

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

        /** Add cardview in gridlayout **/
        LED ledModel = new LED.Builder()
                .name("Character")
                .creator("Xman")
                .bookmarked(true)
                .downloadCnt(2432)
                .build();

        ImageCardViewAddonText cardViewLED = new ImageCardViewAddonText(getActivity());
        cardViewLED.setOnClickCustomDialogEnable(ImageCardViewAddonText.DETAIL_DIALOG_TYPE, ledModel, mainActivity);
        cardViewLED.setName("Character");
        cardViewLED.setImage(R.drawable.characters);
        m_ledGridLayout.addView(cardViewLED);

        return view;
    }
}
