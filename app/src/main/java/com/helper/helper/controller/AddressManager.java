/*
 * Copyright (c) 10/17/18 2:56 PM
 * Written by Sungdong Jo
 * Description:
 */

package com.helper.helper.controller;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

import com.helper.helper.Constants;

public class AddressManager {
    public static AddressResultReceiver m_resultAddressReceiver;
    private static String m_strAddrOutput;

    public static String getConvertLocationToAddress() {
        return m_strAddrOutput;
    }

    public static class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        /**
         * Receives data sent from FetchAddressIntentService and updates the UI in MainActivity.
         */
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            m_strAddrOutput = resultData.getString(Constants.RESULT_DATA_KEY);
            m_strAddrOutput = m_strAddrOutput.replaceAll("대한민국 ", "");
        }
    }

    public static void startAddressIntentService(Context context, Location location) {
        if( m_resultAddressReceiver == null ) {
            m_resultAddressReceiver = new AddressResultReceiver(new Handler());
        }
        Intent intent = new Intent(context, FetchAddressIntentService.class);

        intent.putExtra(Constants.RECEIVER, m_resultAddressReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, location);

        // Start the service. If the service isn't already running, it is instantiated and started
        // (creating a process for it if needed); if it is running then it remains running. The
        // service kills itself automatically once all intents are processed.
        try {
            context.startService(intent);
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }

    }
}
