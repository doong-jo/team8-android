/*
 * Copyright (c) 2018.
 * Written by Sungdong Jo
 * Description:
 */

package com.helper.helper.util;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class HttpManagerUtil {
    private final static String TAG = HttpManagerUtil.class.getSimpleName() + "/DEV";
    private static String m_serverURI;

    public static void setServerURI(String uri) {
        m_serverURI = uri;
    }

    public static String requestHttp(final String uri, final String method) {

        final String[] resBuffer = {""};

        if( uri == null ||
            !(method.equals("PUT") || method.equals("GET") || method.equals("POST") || method.equals("DELETE"))
        ) {
            Log.d(TAG, "requestHttp: Invalid parameters");
            return "";
        }

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                //////////////////// Create URL ////////////////////
                URL githubEndpoint = null;
                try {

                    githubEndpoint = new URL(m_serverURI + uri);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }

                //////////////////// Create connection ///////////////////
                HttpsURLConnection myConnection =
                        null;
                try {
                    myConnection = (HttpsURLConnection) githubEndpoint.openConnection();
                    myConnection.setDoOutput(true);
                    myConnection.setRequestMethod(method);

                } catch (IOException e) {
                    e.printStackTrace();
                }

                // Set request header.
                if (myConnection != null) {
//                    myConnection.setRequestProperty("Content-Type", "application/json");
                    myConnection.setRequestProperty("User-Agent", "my-rest-app-v0.1");
                }
//                myConnection.setRequestProperty("Accept",
//                        "application/vnd.github.v3+json");
//                myConnection.setRequestProperty("Contact-Me",
//                        "hathibelagal@example.com");

                //////////////////// Create the data ////////////////////
//                String myData = "sdong001";
//                JSONObject json = new JSONObject();
//                try {
//                    json.put("emergency", false);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//
//                String body = json.toString();

// Enable writing


// Write the data
//                try {
//                    myConnection.getOutputStream().write(json.toString().getBytes());
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }

                //////////////////// Request result ////////////////////
                try {
                    if (myConnection.getResponseCode() == 200) {
                        // Success
                        // Further processing here
                        String contentType = myConnection.getHeaderField("Content-Type");
                        String charset = null;
                        InputStream response = myConnection.getInputStream();

                        for (String param : contentType.replace(" ", "").split(";")) {
                            if (param.startsWith("charset=")) {
                                charset = param.split("=", 2)[1];
                                break;
                            }
                        }

                        if (charset != null) {
                            try (BufferedReader reader = new BufferedReader(new InputStreamReader(response, charset))) {
                                String buf = "";

                                for (String line; (line = reader.readLine()) != null;) {
                                    Log.d(TAG, "requestHttp / uri: " + uri + " / method: " + method + " / line: " + line);
                                    buf = buf.concat(line);
                                    // ... System.out.println(line) ?
                                }
                                resBuffer[0] = buf;

                            }
                        } else {
                            // It's likely binary content, use InputStream/OutputStream.
                        }

                        Log.d(TAG, "request rest run: success");
                    } else {
                        // Error handling code goes here
                        Log.d(TAG, "request rest run: error");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        });
        return resBuffer[0];
    }
}
