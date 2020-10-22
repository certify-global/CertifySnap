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
import android.view.View;
import android.widget.Toast;

import com.certify.snap.R;
import com.certify.snap.bluetooth.bleCommunication.BluetoothGattAttributes;
import com.certify.snap.bluetooth.bleCommunication.BluetoothLeService;
import com.certify.snap.bluetooth.data.DeviceInfoManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.certify.snap.common.Constants.MEASURED_STATE_MASK;

public class BLEController {
    private static final String TAG = BLEController.class.getSimpleName();
    private static BLEController bleServiceInstance = null;
    private BluetoothLeService mBluetoothLeService;
    private byte[] ledrgb = new byte[3];
    private int mRelayTime = 2;
    private Timer mRelayTimer;
    private boolean isNormalTempLightEnabled =false;
    private boolean isHighTempLightEnabled = false;
    private int NORMAL_TEMP_COLOR =-16711936;
    private int HIGH_TEMP_COLOR = 0xffff0000;
    public ServiceConnection mServiceConnection = null;

        public static BLEController getInstance() {
        if (bleServiceInstance == null)
            bleServiceInstance = new BLEController();

        return bleServiceInstance;
    }

    public void setBluetoothLeService(BluetoothLeService mBluetoothLeService) {
        this.mBluetoothLeService = mBluetoothLeService;
    }

    public boolean isNormalTempLightEnabled() {
        return isNormalTempLightEnabled;
    }

    public void setNormalTempLightEnabled(boolean normalTempLightEnabled) {
        isNormalTempLightEnabled = normalTempLightEnabled;
    }

    public boolean isHighTempLightEnabled() {
        return isHighTempLightEnabled;
    }

    public void setHighTempLightEnabled(boolean highTempLightEnabled) {
        isHighTempLightEnabled = highTempLightEnabled;
    }

    private void startBleLightTimer() {
        mRelayTimer = new Timer();
        mRelayTimer.schedule(new TimerTask() {
            public void run() {
                bleLightOff();
                this.cancel();
            }
        }, mRelayTime * 1000);
    }

    public void setLightOnNormalTemperature() {
        if (isNormalTempLightEnabled)
            bleLightColor(NORMAL_TEMP_COLOR);
    }

    public void setLightOnHighTemperature() {
        if (isHighTempLightEnabled)
            bleLightColor(HIGH_TEMP_COLOR);
    }
    /**
     * Red Value--> 0xffff0000
     * Green Value --->-16711936
     * Blue light value ---> -16776961
     */
    private void bleLightColor(int colorValue) {
        int val = setColorLight(colorValue, 0.0f);
        byte[] rgb = {6, 1, intToByte(Color.red(val)), intToByte(Color.green(val)), intToByte(Color.blue(val))};
        controlLed(rgb);
        for (int i = 0; i < 3; i++)
            ledrgb[i] = rgb[i + 1];

        startBleLightTimer();
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

    public void data() {
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
     *
     * @param rgb
     * @return
     */
    private boolean controlLed(byte[] rgb) {
        if(mBluetoothLeService == null)
            return false;
        BluetoothGattCharacteristic characteristic = mBluetoothLeService.getGattCharacteristic(BluetoothGattAttributes.LED_CHARACTERISTIC);
        Log.d("TAG", "controlLed: " + characteristic);
        if (characteristic != null) {
            mBluetoothLeService.sendDataCharacteristic(characteristic, rgb);
            return true;
        }
        Log.e("TAG", "Not founded characteristic");
        return false;
    }

    public void bleLightOff() {
        byte[] rgb = {6, 1, intToByte(Color.red(MEASURED_STATE_MASK)), intToByte(Color.green(MEASURED_STATE_MASK)), intToByte(Color.blue(MEASURED_STATE_MASK))};
        controlLed(rgb);
        for (int i = 0; i < 3; i++)
            ledrgb[i] = rgb[i + 1];
    }

    public void initServiceConnection() {
        mServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder service) {
                mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
                if (!mBluetoothLeService.initialize()) {
                    Log.d(TAG, "onServiceConnected: No BLE Device");
                }
                mBluetoothLeService.connect(DeviceInfoManager.getInstance().getDeviceAddress());
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                mBluetoothLeService = null;
            }
        };
    }

    public void connectToDevice() {
        if (mBluetoothLeService != null)
            mBluetoothLeService.connect(DeviceInfoManager.getInstance().getDeviceAddress());
    }

    private void  clearData(){
        mBluetoothLeService = null;
    }
}
