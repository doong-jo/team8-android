package com.helper.helper.view.login;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.helper.helper.R;
import com.helper.helper.controller.FormManager;
import com.helper.helper.controller.HttpManager;
import com.helper.helper.controller.PermissionManager;
import com.helper.helper.interfaces.HttpCallback;
import com.helper.helper.interfaces.ValidateCallback;
import com.helper.helper.model.User;
import com.sangcomz.fishbun.FishBun;
import com.sangcomz.fishbun.adapter.image.impl.PicassoAdapter;
import com.sangcomz.fishbun.define.Define;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MakeProfileFragment extends Fragment {
    private final static String TAG = MakeProfileFragment.class.getSimpleName() + "/DEV";

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

        /*******************************************************************/

        /******************* Make Listener in View *******************/

        final MakeProfileFragment fragment = this;
        addPhotoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FishBun.with(MakeProfileFragment.this)
                        .setImageAdapter(new PicassoAdapter())
                        .setIsUseDetailView(false)
                        .setCamera(true)
                        .setMinCount(1)
                        .setMaxCount(1)
                        .exceptGif(true)
                        .startAlbum();

            }
        });

        /*************************************************************/

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Define.ALBUM_REQUEST_CODE:
                if (resultCode == getActivity().RESULT_OK) {
//                    path = data.getParcelableArrayListExtra(Define.INTENT_PATH);
//                    imageAdapter.changePath(path);
                    break;
                }
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
