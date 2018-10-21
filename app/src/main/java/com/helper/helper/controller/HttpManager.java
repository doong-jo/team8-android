/*
 * Copyright (c) 10/15/18 1:50 PM
 * Written by Sungdong Jo
 * Description:
 */

package com.helper.helper.controller;

import android.os.AsyncTask;
import android.util.Log;

import com.helper.helper.enums.Collection;
import com.helper.helper.interfaces.HttpCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

public class HttpManager {
    private final static String TAG = HttpManager.class.getSimpleName() + "/DEV";

    private static String m_serverURI;
    private static JSONArray m_resultJsonArray;
    private static Collection m_collection;


    public static boolean useCollection(String coll) {
        switch(coll)
        {
            case "user" :
                m_collection = Collection.USER;
                return true;

            case "accident":
                m_collection = Collection.ACCIDENT;
                return true;

            case "led" :
                m_collection = Collection.LED;
                return true;

            case "tracking" :
                m_collection = Collection.TRACKING;
                return true;

            default:
                return false;
        }

    }

    public static void setServerURI(String uri) {
        m_serverURI = uri;
    }

    private static String getAllKeyValueJSONObject(JSONObject obj) {
        StringBuilder resultStr = new StringBuilder();

        Iterator<String> keys = obj.keys();
        while(keys.hasNext()) {
            String key = keys.next();
            String value = "";
            try {
                try {
                    value = (String) obj.get(key);
                } catch (ClassCastException e) {
                    if( obj.get(key).getClass().getName().equals("java.lang.Boolean") ) {
                        value = String.valueOf(obj.get(key));
                    }
                    else if ( obj.get(key).getClass().getName().equals("org.json.JSONObject")) {
                        value = obj.get(key).toString();
                        Log.d(TAG, "getAllKeyValueJSONObject: " + value);
                    }
                }


                if( !resultStr.toString().equals("") ) {
                    resultStr.append("&");
                }
                resultStr.append(key).append("=").append(value);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return resultStr.toString();
    }

    public static void requestHttp(final JSONObject query, final String method, final HttpCallback callback) throws JSONException {

        final String[] resBuffer = {""};
        final String charset = "UTF-8";


        if( query == null ||
            !(method.equals("PUT") || method.equals("GET") || method.equals("POST") || method.equals("DELETE"))
        ) {
            callback.onError("requestHttp: Invalid parameters");
        }

        final String queryString = getAllKeyValueJSONObject(query);

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                /** Create URL **/
                URL serverEndPoint = null;
                try {
                    serverEndPoint = new URL(m_serverURI + "/" + m_collection.getValue() + "?" + queryString);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }

                /** Set Connection **/
                HttpURLConnection connection = null;
                try {
                    connection = (HttpURLConnection) serverEndPoint.openConnection();

                    // TODO: 2018. 10. 12. Not GET Method
                    if( !method.equals("GET") && !method.equals("DELETE")) {
                        connection.setDoOutput(true);
                    }

                    connection.setRequestMethod(method);


                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (connection != null) {
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setRequestProperty("Accept-Charset", charset);
//                    connection.setRequestProperty("User-Agent", "my-rest-app-v0.1");
                }

                /** Request result **/
                try {
                    int resultResponseCode = connection.getResponseCode();

                    if (resultResponseCode == HttpURLConnection.HTTP_OK ||
                            resultResponseCode == HttpURLConnection.HTTP_NOT_MODIFIED
                            ) {

                        String contentType = connection.getHeaderField("Content-Type");
                        String charset = null;
                        InputStream response = connection.getInputStream();

                        for (String param : contentType.replace(" ", "").split(";")) {
                            if (param.startsWith("charset=")) {
                                charset = param.split("=", 2)[1];
                                break;
                            }
                        }

                        if (charset != null) {
                            try (BufferedReader reader = new BufferedReader(new InputStreamReader(response, charset))) {

                                String readResponse = reader.readLine();

                                if( method.equals("GET") ) {
                                    m_resultJsonArray = new JSONArray(readResponse);
                                } else {
                                    m_resultJsonArray = new JSONArray("[{result:"+readResponse+"}]");
                                    callback.onSuccess(m_resultJsonArray);
                                }

                                callback.onSuccess(m_resultJsonArray);

                            } catch (JSONException e) {
                                callback.onError(e.getMessage());
                                e.printStackTrace();
                            }
                        } else {
                            // It's likely binary content, use InputStream/OutputStream.
                        }

                        Log.d(TAG, "request rest run: success");
                    } else {
                        // Error handling code goes here
                        String errMsg = connection.getResponseMessage();

                        callback.onError(errMsg);
                        Log.d(TAG, "HttpManagerUtil Error : " + errMsg);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}