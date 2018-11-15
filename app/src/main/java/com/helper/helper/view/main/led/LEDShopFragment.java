package com.helper.helper.view.main.led;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.view.LayoutInflater;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.helper.helper.R;
import com.helper.helper.view.search.SearchActivity;

public class LEDShopFragment extends Fragment {

    private TextInputEditText m_ledShopSearchEditTxt;
    private ImageView m_ledShopSearchBtn;

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
        m_ledShopSearchEditTxt = view.findViewById(R.id.ledShopSearchEditTxt);
        m_ledShopSearchBtn = view.findViewById(R.id.ledShopSearchBtn);

        /******************* Make Listener in View *******************/
        m_ledShopSearchEditTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getActivity(),SearchActivity.class);
                startActivity(intent);
            }
        });

        m_ledShopSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getActivity(),SearchActivity.class);
                startActivity(intent);
            }
        });

        setCategoryData();

        return view;
    }

    private void setCategoryData() {

    }
}
