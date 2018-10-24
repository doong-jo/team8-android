package com.helper.helper.view.login;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.helper.helper.R;
import com.helper.helper.controller.CircleTransform;
import com.helper.helper.controller.UserManager;
import com.helper.helper.view.ScrollingActivity;

public class CongrateFragment extends Fragment {
    private final static String TAG = JoinFragment.class.getSimpleName() + "/DEV";

    public CongrateFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
        View view = inflater.inflate( R.layout.fragment_congrate, container, false );

        /******************* Connect widgtes with layout *******************/
        Button startBtn = view.findViewById(R.id.startBtn);
        ImageView userImgView = view.findViewById(R.id.previewImage);
        TextView userName = view.findViewById(R.id.userName);

        if ( UserManager.getUserProfileBitmap() != null ) {
            userImgView.setImageBitmap(
                    new CircleTransform().transform(UserManager.getUserProfileBitmap())
            );
        }

        userName.setText(UserManager.getUserName());

        /*******************************************************************/


        /******************* Make Listener in View *******************/
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent intent = new Intent(LoginActivity.this, ScrollingActivity.class);
//            intent.putExtra("text",String.valueOf(editText.getText()));
//                startActivity(intent);
            }
        });
        /*************************************************************/

        return view;
    }
}
