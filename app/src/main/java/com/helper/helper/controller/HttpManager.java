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
import java.util.Arrays;
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

            case "category" :
                m_collection = Collection.CATEGORY;
                return true;

            default:
                return false;
        }

    }

    public static void setServerURI(String uri) {
        m_serverURI = uri;
    }

    public static String getAllKeyValueJSONObject(JSONObject obj) throws JSONException {
        StringBuilder resultStr = new StringBuilder();

        Iterator<String> keys = obj.keys();
        while(keys.hasNext()) {
            String key = keys.next();
            String value = "";

            try {
                String className = obj.get(key).getClass().getName();

                if( className.equals("java.lang.Integer") ) {
                    value = String.valueOf(obj.get(key));
                } else {
                    value = (String) obj.get(key);
                }

            } catch (ClassCastException e) {
                if( obj.get(key).getClass().getName().equals("java.lang.Boolean") ) {
                    value = String.valueOf(obj.get(key));
                }
                /** mongoDB ($in ...) syntax case **/
                else if ( obj.get(key).getClass().getName().equals("org.json.JSONObject")) {
                    JSONObject jsonObject = obj.getJSONObject(key);

                    Iterator<String> keysInValue = jsonObject.keys();
                    if(keysInValue.hasNext()) {
                        while( keysInValue.hasNext() ) {
                            String keyOfValue = keysInValue.next();
                            String[] ValueOfKeyOfValue = (String[])jsonObject.get(keyOfValue);
                            for (int i = 0; i < ValueOfKeyOfValue.length; i++) {
                                ValueOfKeyOfValue[i] = "\"" + ValueOfKeyOfValue[i] + "\"";
                            }

                            value = "{\"" + keyOfValue + "\":" + Arrays.deepToString(ValueOfKeyOfValue) + "}";
                        }
                    }
                     else {
                        value = obj.get(key).toString();
                    }
                }
            }


            if( !resultStr.toString().equals("") ) {
                resultStr.append("&");
            }
            resultStr.append(key).append("=").append(value);
        }

        return resultStr.toString();
    }

    public static String getParamOfKeyValueJSONObject(JSONObject obj, String paramStr) throws JSONException {
        String resultStr = "";
        String keyStr = obj.getString(paramStr);

        obj.remove(paramStr);

        resultStr = resultStr.concat(keyStr)
                .concat("?")
                .concat(getAllKeyValueJSONObject(obj));

        return resultStr;
    }

    public static void requestHttp(final JSONObject query, String key, final String method, final String subURI, final HttpCallback callback) throws JSONException {

        final String charset = "UTF-8";

        if( query == null ||
                !(method.equals("PUT") || method.equals("GET") || method.equals("POST") || method.equals("DELETE"))
                ) {
            callback.onError("requestHttp: Invalid parameters");
            return;
        }

        String queryStr;
        final URL serverEndPoint;

        if( method.equals("GET") || method.equals("POST") ) {
            queryStr = getAllKeyValueJSONObject(query);
            serverEndPoint = getGETmethodServerEndPoint(queryStr, subURI);
        } else { /** PUT, DELETE **/
            queryStr = getParamOfKeyValueJSONObject(query, key);
            serverEndPoint = getPUTmethodServerEndPoint(queryStr, subURI);
        }


        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                /** Create URL **/


                /** Set Connection **/
                HttpURLConnection connection = null;
                try {
                    connection = (HttpURLConnection) serverEndPoint.openConnection();

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
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private static URL getGETmethodServerEndPoint(String queryString, String subURI) {
        URL serverEndPoint = null;
        String uriStr = null;

        try {
            if (!queryString.equals("")) {
                uriStr = m_serverURI.concat("/")
                        .concat(m_collection.getValue());

                if( !subURI.equals("") ) {
                    uriStr = uriStr.concat("/")
                            .concat(subURI);
                }
                uriStr = uriStr.concat("?").concat(queryString);
            } else {
                uriStr = m_serverURI
                        .concat("/")
                        .concat(m_collection.getValue());
            }

            serverEndPoint = new URL(uriStr);
        }
        catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return serverEndPoint;
    }

    private static URL getPUTmethodServerEndPoint(String queryString, String subURI) {
        URL serverEndPoint = null;

        String uriStr = m_serverURI.concat("/")
                .concat(m_collection.getValue())
                .concat("/");

        if( !subURI.equals("") ) {
            uriStr = uriStr.concat(subURI);
        }

        uriStr = uriStr.concat(queryString);


        try {
            serverEndPoint = new URL(uriStr);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return serverEndPoint;
    }
}
