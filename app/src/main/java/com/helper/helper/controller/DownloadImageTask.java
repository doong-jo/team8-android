package com.helper.helper.controller;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import com.helper.helper.interfaces.ValidateCallback;
import com.snatik.storage.Storage;

import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class DownloadImageTask extends AsyncTask<String, Integer, Bitmap> {
    public final static String DOWNLOAD_PATH = "user_data/LED";
    private final static String TAG = DownloadImageTask.class.getSimpleName() + "/DEV";

    public final static int DONE_LOAD_LED_IMAGES = 1;

    private Context m_parentContext;
    private ValidateCallback m_doneCallback;

    public DownloadImageTask(Context context, ValidateCallback callback) {
        m_parentContext = context;
        m_doneCallback = callback;
    }

    @Override
    protected void onProgressUpdate(Integer... params) {
        Log.d(TAG, "onProgressUpdate: " + params[0] + "%");
    }

    @Override
    protected Bitmap doInBackground(String... urls) {
        Bitmap bitmap = null;
        FileOutputStream filestream = null;
        InputStream in = null;

        Storage internalStorage = new Storage(m_parentContext);
        String path = internalStorage.getInternalFilesDirectory();
        String dir = path + File.separator + DOWNLOAD_PATH;

        final boolean dirExists = internalStorage.isDirectoryExists(dir);

        if( !dirExists ) {
            internalStorage.createDirectory(dir);
        }

        for (String urldisplay :
                urls) {
            String saveFilePath = dir + File.separator + urldisplay.split("LED/")[1];

            /** exist image -> pass **/
            if( internalStorage.isFileExist(saveFilePath) ) {
                continue;
            }

            /** set file stream **/
            File file = new File(saveFilePath);

            try {
                filestream = new FileOutputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            /** load image by url **/
            try {
                in = new java.net.URL(urldisplay).openStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if( saveFilePath.contains(".png") ) {
                try {
                    bitmap = BitmapFactory.decodeStream(in);
                } catch (Exception e) {
                    Log.e("Error", e.getMessage());
                    e.printStackTrace();
                }

                /** Bitmap to png file **/
                bitmap.compress(Bitmap.CompressFormat.PNG, 0, filestream);

            } else if( saveFilePath.contains(".gif") ) {
                BufferedInputStream bis = new BufferedInputStream(in);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                int current;

                try {
                    while ((current = bis.read()) != -1) {
                        baos.write(current);
                    }
                    filestream.write(baos.toByteArray());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        /** close stream **/
        try {
            if( filestream != null ) {
                filestream.flush();
                filestream.close();
            }

            if( in != null ) {
                in.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            m_doneCallback.onDone(DONE_LOAD_LED_IMAGES);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return bitmap;
    }


    @Override
    protected void onPostExecute(Bitmap result) {

    }
}