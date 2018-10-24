package com.helper.helper.view.login;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.helper.helper.R;
import com.helper.helper.controller.CircleTransform;
import com.helper.helper.controller.FormManager;
import com.helper.helper.controller.HttpManager;
import com.helper.helper.controller.PermissionManager;
import com.helper.helper.interfaces.HttpCallback;
import com.helper.helper.interfaces.ValidateCallback;
import com.helper.helper.model.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import me.iwf.photopicker.PhotoPicker;

public class MakeProfileFragment extends Fragment {
    private final static String TAG = MakeProfileFragment.class.getSimpleName() + "/DEV";
    private static final int PHOTO_PICK = 772;
    private ImageView m_previewImage;
    private ImageView m_backImg;

    public MakeProfileFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
        View view = inflater.inflate( R.layout.fragment_make_profile, container, false );

        /******************* Check & Request permissions *******************/
        if (!PermissionManager.checkPermissions(getActivity(), Manifest.permission.CAMERA) ||
                !PermissionManager.checkPermissions(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
                !PermissionManager.checkPermissions(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) ) {

            /* Result about user selection -> onActivityResult in ScrollActivity */
            PermissionManager.requestPermissions(getActivity(), new String[]{
                            Manifest.permission.CAMERA,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE},
                    PermissionManager.REQUEST_CAMERA_EXTERNAL_STORAGE);
        }
        /*******************************************************************/

        /******************* Connect widgtes with layout *******************/
        Button addPhotoBtn = view.findViewById(R.id.addPhotoBtn);
        m_previewImage = view.findViewById(R.id.previewImage);
        m_backImg = view.findViewById(R.id.backJoinFragment);
        /*******************************************************************/

        /******************* Make Listener in View *******************/

        addPhotoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent intent = new Intent();
//                intent.setType("image/*");
//                intent.setAction(Intent.ACTION_GET_CONTENT);
//                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PHOTO_PICK);

                Intent intent = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                intent.setType("image/*");
                intent.putExtra("crop", "true");
                intent.putExtra("scale", true);
                intent.putExtra("outputX", 256);
                intent.putExtra("outputY", 256);
                intent.putExtra("aspectX", 1);
                intent.putExtra("aspectY", 1);
                intent.putExtra("return-data", true);
                startActivityForResult(intent, PHOTO_PICK);

//                PhotoPicker.builder()
//                        .setPhotoCount(1)
//                        .setShowCamera(true)
//                        .setShowGif(true)
//                        .setPreviewEnabled(false)
//                        .start((LoginActivity)getActivity(), PhotoPicker.REQUEST_CODE);
            }

//            backJoinFragment.
        });

        m_backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginActivity activity = (LoginActivity)getActivity();

                if( activity != null ) {
                    activity.moveToJoinFragment(view);
                }
            }
        });


        /*************************************************************/

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        int a =1;
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case PHOTO_PICK:
                if( data == null ) { return; }
                Bundle dataExtras = data.getExtras();
                Bitmap photo = dataExtras.getParcelable("data");
                m_previewImage.setImageBitmap(
                        new CircleTransform().transform(photo)
                );

                Toast.makeText(getActivity(), "PhotoPicker Get!!", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void InsertUserinServer(User user, final ValidateCallback callback) throws JSONException {
        if( HttpManager.useCollection("user") ) {

            JSONObject reqObject = user.getTransformUserToJSON();
//            reqObject.put("lastAccess", new Date().toString());

            HttpManager.requestHttp(reqObject, "POST", new HttpCallback() {
                @Override
                public void onSuccess(JSONArray jsonArray) throws JSONException {
                    JSONObject resultObj = (JSONObject)jsonArray.get(0);
                    boolean result = resultObj.getBoolean("result");
                    if( result ) {
                        callback.onDone(FormManager.RESULT_VALIDATION_SUCCESS);
                    } else {
                        callback.onDone(FormManager.RESULT_VALIDATION_ERROR);
                    }
                }

                @Override
                public void onError(String err) {

                }
            });
        }
    }
}
