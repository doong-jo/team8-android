package com.helper.helper.controller.ble;

import java.util.HashMap;



///////////////////////////////////////////////////////////////////////////
// UUID 정의 클래스
// S7 MAC : E4:FA:ED:F5:AE:FE
// BLE MAC : B8:27:EB:D7:A1:34
///////////////////////////////////////////////////////////////////////////

public class BluetoothAttributes {
    private static HashMap<String, String> attributes = new HashMap();
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00001801-0000-1000-8000-00805f9b34fb";
//    public static String CLIENT_CHARACTERISTIC_CONFIG = "00001801-0000-1000-8000-00805f9b34fb";
//    public static String HM_RX_TX = "0000ffe1-0000-1000-8000-00805f9b34fb";
    public static String HM_RX_TX = "94f39d29-7d6d-437d-973b-fba39e49d4ee";
    static {
        // Sample Services.
        attributes.put("0000ffe0-0000-1000-8000-00805f9b34fb", "HM 10 Serial");
        attributes.put("00001800-0000-1000-8000-00805f9b34fb", "Device Information Service");
        // Sample Characteristics.
        attributes.put(HM_RX_TX,"RX/TX data");
        attributes.put("00002a29-0000-1000-8000-00805f9b34fb", "Manufacturer Name String");
    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}