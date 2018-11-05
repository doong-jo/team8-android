package com.helper.helper.controller;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.InputStream;


public class DownloadImageTask extends AsyncTask<String, Integer, Bitmap> {
    private final static String TAG = DownloadImageTask.class.getSimpleName() + "/DEV";
    ImageView bmImage;

    public DownloadImageTask() {

    }

    @Override
    protected void onProgressUpdate(Integer... params) {
        Log.d(TAG, "onProgressUpdate: " + params[0] + "%");
    }

    @Override
    protected Bitmap doInBackground(String... urls) {
        String urldisplay = urls[0];
        Bitmap mIcon11 = null;
        try {
            InputStream in = new java.net.URL(urldisplay).openStream();
            mIcon11 = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
        return mIcon11;
    }


    @Override
    protected void onPostExecute(Bitmap result) {

    }
}