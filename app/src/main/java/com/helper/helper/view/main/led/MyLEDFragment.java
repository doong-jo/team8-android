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
import android.widget.ImageView;

//import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.helper.helper.R;

public class MyLEDFragment extends Fragment {

    /******************* Define widgtes in view *******************/
    private ImageView m_ledGridToggle;
    private ImageView m_bookmarkToggle;

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
        /*******************************************************************/

        m_bIsBookmarkView = false;

        /******************* Make Listener in View *******************/

        m_ledGridToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ( m_bIsBookmarkView ) {
                    m_bIsBookmarkView = !m_bIsBookmarkView;
                    m_ledGridToggle.setImageResource(R.drawable.ic_border_all_black_selected);
                    m_bookmarkToggle.setImageResource(R.drawable.ic_bookmark_black);
                }
            }
        });

        m_bookmarkToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ( !m_bIsBookmarkView ) {
                    m_bIsBookmarkView = !m_bIsBookmarkView;
                    m_ledGridToggle.setImageResource(R.drawable.ic_border_all_black);
                    m_bookmarkToggle.setImageResource(R.drawable.ic_bookmark_black_selected);
                }
            }
        });

        /*************************************************************/

        return view;
    }
}
