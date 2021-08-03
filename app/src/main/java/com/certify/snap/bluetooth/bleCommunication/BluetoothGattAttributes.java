package com.certify.snap.bluetooth.bleCommunication;

import java.util.HashMap;

public class BluetoothGattAttributes {
    private static HashMap<String, String> attributes = new HashMap<>();

    // services
    //private static final String LED_SERVICE = "0000ffe5-0000-1000-8000-00805f9b34fb";
    private static final String LED_SERVICE = "0000fff5-0000-1000-8000-00805f9b34fb";
    public static final String UUID_VALUE = "CF9346F3-BC1E-4354-BD6C-196B27C97A2A";

    public static final String UUID_FFF5 = "0000fff5-0000-1000-8000-00805f9b34fb";
    public static final String UUID_FFF3 = "0000fff3-0000-1000-8000-00805f9b34fb";
    public static final String UUID_FFF4 = "0000fff4-0000-1000-8000-00805f9b34fb";


    // services => characteristics
    public static final String LED_CHARACTERISTIC = "0000fff3-0000-1000-8000-00805f9b34fb";

    static {
        // services
        //attributes.put(LED_SERVICE, "Led Service");
        attributes.put(UUID_VALUE, "UUID Value");

        // characteristics
        attributes.put(LED_CHARACTERISTIC, "Led Characteristic");
    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}