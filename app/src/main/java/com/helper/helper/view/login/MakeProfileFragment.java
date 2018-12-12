package com.helper.helper.view.login;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.helper.helper.R;
import com.helper.helper.controller.CircleTransform;
import com.helper.helper.controller.FileManager;
import com.helper.helper.controller.HttpManager;
import com.helper.helper.controller.PermissionManager;
import com.helper.helper.controller.SharedPreferencer;
import com.helper.helper.controller.UserManager;
import com.helper.helper.enums.RidingType;
import com.helper.helper.interfaces.HttpCallback;
import com.soundcloud.android.crop.Crop;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MakeProfileFragment extends Fragment {
    private final static String TAG = MakeProfileFragment.class.getSimpleName() + "/DEV";
    private static final int PHOTO_PICK = 9162;
    private ImageView m_previewImage;
    private ImageView m_beforeImgView;
    private LinearLayout m_makeProfileLayout;

    public MakeProfileFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_make_profile, container, false);

        // Solve : bug first touch not working
        getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        /******************* Check & Request permissions *******************/
        if (!PermissionManager.checkPermissions(getActivity(), Manifest.permission.CAMERA) ||
                !PermissionManager.checkPermissions(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
                !PermissionManager.checkPermissions(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)) {

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
        m_makeProfileLayout = view.findViewById(R.id.makeProfileLayout);
        ImageView backImg = view.findViewById(R.id.backJoinFragment);
        TextView hyperTextEdit = view.findViewById(R.id.hyperTextEdit);
        Button nextBtn = view.findViewById(R.id.nextBtn);

        ImageView[] vehicleImgs = {
                view.findViewById(R.id.bicycle),
                view.findViewById(R.id.motorcycle),
                view.findViewById(R.id.kickboard),
        };
        /*******************************************************************/



        for (final ImageView imgView :
                vehicleImgs) {
            imgView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String ridingType = (String) imgView.getTag();
                    UserManager.setRidingType(ridingType);

                    toggleImageColorChanger((ImageView) view);
                }
            });
        }

        // Default riding_type is bicycle
        toggleImageColorChanger(vehicleImgs[0]);
        m_beforeImgView = vehicleImgs[0];
        RidingType type;
        type = RidingType.BICYCLE;

        UserManager.setRidingType(type.value);
        /******************* Make Listener in View *******************/
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
                LoginActivity activity = (LoginActivity) getActivity();
                activity.moveToFragment(new AddNameFragment(), m_makeProfileLayout, false);
            }
        });

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
//                UserManager.setRidingType();
                JSONObject jsonObject = UserManager.getUser().getTransformUserToJSON();


                try {
                    HttpManager.requestHttp(jsonObject, "", "POST", "", new HttpCallback() {
                        @Override
                        public void onSuccess(JSONArray jsonArray) throws JSONException {
                            JSONObject obj = (JSONObject) jsonArray.get(0);


                            if (obj.getBoolean("result")) {

                                try {
                                    FileManager.writeUserProfile(getActivity(), UserManager.getUserProfileBitmap(), UserManager.getUser().getUserName());
                                    FileManager.writeXmlUserInfo(getActivity(), UserManager.getUser());

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                LoginActivity activity = (LoginActivity) getActivity();
                                if (activity != null) {
                                    activity.moveToFragment(new CongrateFragment(), m_makeProfileLayout,true);
                                }
                            } else {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getActivity(), "회원가입에 실패하였습니다.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }

                        @Override
                        public void onError(String err) throws JSONException {

                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        /*************************************************************/
        LoginActivity loginActivity = (LoginActivity)getActivity();
        loginActivity.setFragmentBackPressed(new AddNameFragment(), m_makeProfileLayout, false);

        return view;
    }

    private void toggleImageColorChanger(ImageView img) {

        @SuppressLint("ResourceType") int blueColor = Color.parseColor(getResources().getString(R.color.accent_blue));
        @SuppressLint("ResourceType") int grayColor = Color.parseColor(getResources().getString(R.color.half_black));

        if (m_beforeImgView != null) {
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
    public void onActivityResult(int requestCode, int resultCode, Intent result) {
        super.onActivityResult(requestCode, resultCode, result);
        if (requestCode == Crop.REQUEST_PICK && resultCode == Activity.RESULT_OK) {
            Log.d("onActivityResult", "request pick");
            beginCrop(result.getData());
        } else if (requestCode == Crop.REQUEST_CROP) {
            Log.d("onActivityResult", "request crop");
            handleCrop(resultCode, result);
        }
    }

    private void beginCrop(Uri source) {
        Log.d("beginCrop", "start");
        Uri destination = Uri.fromFile(new File(getActivity().getCacheDir(), "cropped"));
        Crop.of(source, destination).asSquare().start(getContext(), this);
    }

    private void handleCrop(int resultCode, Intent result) {
        if (resultCode == Activity.RESULT_OK) {
            Log.d("handleCrop", "RESULT_OK");
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), Crop.getOutput(result));
                Bitmap savedBitmap = bitmap.copy(bitmap.getConfig(), false);

                Bitmap circleBitmap = new CircleTransform().transform(bitmap);
                m_previewImage.setImageBitmap(
                        circleBitmap
                );
                UserManager.setUserProfileBitmap(savedBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }


        } else if (resultCode == Crop.RESULT_ERROR) {
            Log.d("handleCrop", "RESULT_ERROR");
            Toast.makeText(getActivity(), Crop.getError(result).getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
