/*
 * Copyright (c) 10/17/18 2:26 PM
 * Written by Sungdong Jo
 */

package com.helper.helper.controller;

import android.content.Context;

import com.helper.helper.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.Timer;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class SocketManager {

    private final static String TAG = SocketManager.class.getSimpleName() + "/DEV";

    private static Socket m_socket;
    private static Emitter.Listener m_onConnect;
    private static Emitter.Listener m_onDisconnect;

    private static Emitter.Listener m_onRequsetJoinGroup;
    private static Emitter.Listener m_onAcceptJoinGroup;
    private static Emitter.Listener m_onSetGroupPattern;

    public static void startSocket(final Context context) {
        connectSocket(context);

        makeListener();
        listen(context);
    }

    private static void listen(Context context) {
        makeListener();

        m_socket.on(Socket.EVENT_CONNECT, m_onConnect);
        m_socket.on(Socket.EVENT_DISCONNECT, m_onDisconnect);

        m_socket.on(context.getString(R.string.requset_join_group), m_onRequsetJoinGroup);
        m_socket.on(context.getString(R.string.accept_join_group), m_onAcceptJoinGroup);
        m_socket.on(context.getString(R.string.set_group_pattern), m_onSetGroupPattern);
    }

    private static void makeListener() {
        m_onConnect = new Emitter.Listener() {
            @Override
            public void call(Object... args) {

            }
        };

        m_onDisconnect = new Emitter.Listener() {
            @Override
            public void call(Object... args) {

            }
        };

        m_onRequsetJoinGroup = new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject receivedData = (JSONObject) args[0];
            }
        };

        m_onAcceptJoinGroup = new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject receivedData = (JSONObject) args[0];
            }
        };

        m_onSetGroupPattern = new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject receivedData = (JSONObject) args[0];
            }
        };

    }

    private static void sendToServer(String eventName, JSONObject data) {
        m_socket.emit(eventName, data);
    }

    private static void connectSocket(final Context context) {
        try {
            m_socket = IO.socket(context.getString(R.string.server_uri));
            m_socket.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
}
