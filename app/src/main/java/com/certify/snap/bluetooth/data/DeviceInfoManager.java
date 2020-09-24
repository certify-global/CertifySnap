package com.certify.snap.bluetooth.data;

import android.bluetooth.BluetoothDevice;

import com.certify.snap.common.Application;

public class DeviceInfoManager {

    private static DeviceInfoManager manager = new DeviceInfoManager();

    private SimplePreference preference;
    //private DeviceData deviceData;

    private final static String keyName = "deviceName";
    private final static String keyAddress = "deviceAddress";

    private DeviceInfoManager() {
        preference = Application.getPreference();
    }

    public static DeviceInfoManager getInstance() {
        return manager;
    }

    public void setDeviceInfo(BluetoothDevice deviceInfo) {
        preference.putString(keyName, deviceInfo.getName());
        preference.putString(keyAddress, deviceInfo.getAddress());
        //deviceData = new DeviceData(deviceInfo.getName(), deviceInfo.getAddress());
    }

    public String getDeviceName() {
        return preference.getString(keyName, null);
        //return deviceData.getDeviceName();
    }

    public String getDeviceAddress() {
        return preference.getString(keyAddress, null);
       /* if (deviceData == null)
            return "";
        return deviceData.getDeviceAddress();*/
    }
}
