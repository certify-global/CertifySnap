package com.certify.snap.controller;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.IBinder;
import android.util.Log;

import com.certify.snap.bluetooth.bleCommunication.BluetoothGattAttributes;
import com.certify.snap.bluetooth.bleCommunication.BluetoothLeService;
import com.certify.snap.bluetooth.data.DeviceInfoManager;

import java.util.ArrayList;
import java.util.List;

public class BLEController {
    private static BLEController bleServiceInstance = null;
    private boolean mConnected = false;
    private BluetoothLeService mBluetoothLeService;
    private byte[] ledrgb = new byte[3];

    public static BLEController getInstance() {
        if (bleServiceInstance == null)
            bleServiceInstance = new BLEController();

        return bleServiceInstance;
    }

    public BluetoothLeService getmBluetoothLeService() {
        return mBluetoothLeService;
    }

    public void setmBluetoothLeService(BluetoothLeService mBluetoothLeService) {
        this.mBluetoothLeService = mBluetoothLeService;
    }

    /**
     * receive connection state
     */
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final Intent mIntent = intent;
            final String action = intent.getAction();

            // connected
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                Log.e("TAG", "BroadcastReceiver : Connected!");
                mConnected = true;
                //Toast.makeText(getBaseContext(), R.string.ble_connect_success, Toast.LENGTH_SHORT).show();
            }
            // disconnected
            else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                Log.e("TAG", "BroadcastReceiver : Disconnected!");
                mConnected = false;
                //Toast.makeText(getBaseContext(), R.string.ble_disconnected, Toast.LENGTH_SHORT).show();
            }
            // found GATT service
            else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                Log.e("TAG", "BroadcastReceiver : Found GATT!");
            }
        }
    };

    /**
     * bluetooth service connection
     */
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                //Toast.makeText( R.string.ble_not_find, Toast.LENGTH_SHORT).show();
            }
            mBluetoothLeService.connect(DeviceInfoManager.getInstance().getDeviceAddress());
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };


    /**
     * Red Value--> 0xffff0000
     * Green Value --->-16711936
     * Blue light value ---> -16776961
     */
    public void bleLightColor(int colorValue) {
        int val = setColorLight(colorValue, 0.0f);
        byte[] rgb = {6, 1, intToByte(Color.red(val)), intToByte(Color.green(val)), intToByte(Color.blue(val))};
        controlLed(rgb);
        for (int i = 0; i < 3; i++)
            ledrgb[i] = rgb[i + 1];
    }

    public static byte intToByte(int i) {
        return (byte) (i & 255);
    }

    private List<byte[]> colorDataList = new ArrayList<>();

    public void setColor(byte[] bArr) {
        int i = -1;
        byte[] bArr1 = {6, 1, intToByte(Color.red(i)), intToByte(Color.green(i)), intToByte(Color.blue(i))};
        this.colorDataList.clear();
        this.colorDataList.add(bArr1);
    }

    public void data(){
        byte[] bArr = new byte[(this.colorDataList.size() * 5)];
        int i = 0;
        for (byte[] bArr2 : this.colorDataList) {
            System.arraycopy(bArr2, 0, bArr, i, bArr2.length);
            i += bArr2.length;
        }
    }

    public int setColorLight(int i, float f) {
        int i2;
        int i3;
        int i4;
        if (((double) f) <= 0.0d) {
            int red = (int) (((float) Color.red(i)) - (((float) (255 - Color.red(i))) * f));
            i4 = (int) (((float) Color.green(i)) - (((float) (255 - Color.green(i))) * f));
            i3 = (int) (((float) Color.blue(i)) - (f * ((float) (255 - Color.blue(i)))));
            i2 = red;
        } else {
            float f2 = 1.0f - f;
            i2 = (int) (((float) Color.red(i)) * f2);
            i4 = (int) (((float) Color.green(i)) * f2);
            i3 = (int) (f2 * ((float) Color.blue(i)));
        }
        return Color.rgb(i2, i4, i3);
    }

    /**
     * send rgb byte array to ble device
     * @param rgb
     * @return
     */
    private boolean controlLed(byte[] rgb) {
        // get bluetoothGattCharacteristic
        BluetoothGattCharacteristic characteristic = mBluetoothLeService.getGattCharacteristic(BluetoothGattAttributes.LED_CHARACTERISTIC);

        if (characteristic != null) {
            // check connection
            if (!mConnected) {
                //Toast.makeText(this, R.string.ble_not_connected, Toast.LENGTH_SHORT).show();
                return false;
            }
            // send characteristic data
            mBluetoothLeService.sendDataCharacteristic(characteristic,rgb );
            return true;
        }
        Log.e("TAG", "Not founded characteristic");
        return false;
    }
}
