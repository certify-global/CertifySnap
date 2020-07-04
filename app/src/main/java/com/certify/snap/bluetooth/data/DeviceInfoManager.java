package com.certify.snap.bluetooth.data;

import android.bluetooth.BluetoothDevice;
import android.util.Log;

import com.certify.snap.common.Application;

public class DeviceInfoManager {

    private static DeviceInfoManager manager = new DeviceInfoManager();

    private SimplePreference preference;

    private final static String keyName = "deviceName";
    private final static String keyAddress = "deviceAddress";

    private DeviceInfoManager() {
        preference = Application.getPreference();
        Log.d("TAG", "Naga.....deviceInfomanager...preference  " + Application.getPreference());
    }

    public static DeviceInfoManager getInstance() {
        return manager;
    }

    public void setDeviceInfo(BluetoothDevice deviceInfo) {
        preference.putString(keyName, deviceInfo.getName());
        preference.putString(keyAddress, deviceInfo.getAddress());
        Log.d("TAG", "Naga......onItemClick.... device : "+deviceInfo.getName() );
        Log.d("TAG", "Naga......onItemClick.... device : "+ deviceInfo.getAddress());
    }

    public String getDeviceName() {
        return preference.getString(keyName, null);
    }

    public String getDeviceAddress() {
        return preference.getString(keyAddress, null);
    }
}
