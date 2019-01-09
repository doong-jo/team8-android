/*
 * Copyright (c) 10/17/18 2:26 PM
 * Written by Sungdong Jo
 */

package com.helper.helper.controller;

import android.content.Context;

import com.helper.helper.R;
import com.helper.helper.model.MemberList;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Timer;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class SocketManager {

    private final static String TAG = SocketManager.class.getSimpleName() + "/DEV";

    private final static String ON_SYNC_PATTERN = "ON_SYNC_PATTERN";
    private final static String EMIT_SYNC_PATTERN = "EMIT_SYNC_PATTERN";

    private static Socket m_socket;
    private static Emitter.Listener m_onConnect;
    private static Emitter.Listener m_onDisconnect;

    private static Emitter.Listener m_onSyncPattern;

    public static void startSocket(final Context context) {
        connectSocket(context);

        listen(context);
    }

    private static void listen(final Context context) {
        m_socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {

            }
        });

        m_socket.on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {

            }
        });

    }

    public static void makePatternSyncListenter(MemberList room) {
        final String onRoomName = ON_SYNC_PATTERN.concat("_").concat(room.getIndex());

        m_socket.on(onRoomName, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                BTManager.setShowOnDevice((String)args[0], (String)args[1]);
            }
        });
    }

    public static void doSyncPattern(JSONObject data) {
        Date date = new Date();
        Calendar calendar = GregorianCalendar.getInstance(); // creates a new calendar instance
        calendar.setTime(date);   // assigns calendar to given date
//        int hour = calendar.get(Calendar.HOUR);        // gets hour in 12h format
//        int min = calendar.get(Calendar.MINUTE) + 1;

        int addSec = calendar.get(Calendar.SECOND) + 15;
        int sec = addSec >= 60 ? 60 - addSec : addSec;

        try {
            m_socket.emit(EMIT_SYNC_PATTERN,
                    data.getString("name"),
                    data.getString("roomname"),
                    data.getString("pattern"),
                    String.valueOf(sec));

        } catch (JSONException e) {
            e.printStackTrace();
        }
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
