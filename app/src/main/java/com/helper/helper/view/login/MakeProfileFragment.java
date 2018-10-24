package com.helper.helper.view.login;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.helper.helper.R;
import com.helper.helper.controller.CircleTransform;
import com.helper.helper.controller.FormManager;
import com.helper.helper.controller.HttpManager;
import com.helper.helper.controller.PermissionManager;
import com.helper.helper.controller.UserManager;
import com.helper.helper.interfaces.HttpCallback;
import com.helper.helper.interfaces.ValidateCallback;
import com.helper.helper.model.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MakeProfileFragment extends Fragment {
    private final static String TAG = MakeProfileFragment.class.getSimpleName() + "/DEV";
    private static final int PHOTO_PICK = 772;
    private ImageView m_previewImage;
    private ImageView m_beforeImgView;

    public MakeProfileFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
        View view = inflater.inflate( R.layout.fragment_make_profile, container, false );

        // Solve : bug first touch not working
        getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

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
        m_previewImage = view.findViewById(R.id.previewImage);
        ImageView backImg = view.findViewById(R.id.backJoinFragment);
        TextView hyperTextEdit = view.findViewById(R.id.hyperTextEdit);
        Button nextBtn = view.findViewById(R.id.nextBtn);

        ImageView [] vehicleImgs = {
                view.findViewById(R.id.bicycle),
                view.findViewById(R.id.motorcycle),
                view.findViewById(R.id.kickboard),
        };
        /*******************************************************************/

        /******************* Make Listener in View *******************/

        for (ImageView imgView :
                vehicleImgs) {
            imgView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    toggleImageColorChanger((ImageView)view);
                }
            });
        }
        m_previewImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPhotoPicker();
            }
        });

        hyperTextEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPhotoPicker();
            }
        });

        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginActivity activity = (LoginActivity)getActivity();
                activity.moveToFragment(new JoinFragment(), false);
            }
        });

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                View focusView = getActivity().getCurrentFocus();
                LoginActivity activity = (LoginActivity)getActivity();
                activity.moveToFragment(new CongrateFragment(), true);
            }
        });


        /*************************************************************/

        return view;
    }

    private void toggleImageColorChanger(ImageView img) {

        @SuppressLint("ResourceType") int blueColor = Color.parseColor(getResources().getString(R.color.accent_blue));
        @SuppressLint("ResourceType") int grayColor = Color.parseColor(getResources().getString(R.color.half_black));

        if( m_beforeImgView != null ) {
            final PorterDuffColorFilter colorFilter = new PorterDuffColorFilter(grayColor, PorterDuff.Mode.SRC_ATOP);
            m_beforeImgView.setColorFilter(colorFilter);
        }

        final PorterDuffColorFilter colorFilter = new PorterDuffColorFilter(blueColor, PorterDuff.Mode.SRC_ATOP);
        img.setColorFilter(colorFilter);

        m_beforeImgView = img;
    }

    private void startPhotoPicker() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        intent.setType("image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("scale", true);
        intent.putExtra("outputX", 256);
        intent.putExtra("outputY", 256);
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("noFaceDetection", true);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, PHOTO_PICK);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case PHOTO_PICK:
                if( data == null ) { return; }
                Bundle dataExtras = data.getExtras();
                Bitmap photo = dataExtras.getParcelable("data");
                UserManager.setUserProfileBitmap(photo);

                m_previewImage.setImageBitmap(
                        new CircleTransform().transform(photo)
                );
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
