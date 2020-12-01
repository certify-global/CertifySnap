package com.certify.snap.gesture;

import android.util.Log;

import com.certify.snap.controller.GestureController;
import com.common.pos.api.util.PosUtil;

public class GestureConfiguration {
    private static final String TAG = GestureConfiguration.class.getSimpleName();
    private static GestureConfiguration instance = null;

    public static GestureConfiguration getInstance() {
        if (instance == null) {
            instance = new GestureConfiguration();
        }
        return instance;
    }

    public void initGestureRangeValue() {
        getVendorValue();
    }

    private void getVendorValue() {
        int leftVendorValue = getRKvendorCompensate(31);
        int rightVendorValue = getRKvendorCompensate(32);

        if (leftVendorValue != -1 && rightVendorValue != -1) {
            writeVendor(1, leftVendorValue+"");
            writeVendor(2, rightVendorValue+"");
            GestureController.getInstance().setLeftHandRangeVal(leftVendorValue);
            GestureController.getInstance().setRightHandRangeVal(rightVendorValue);
        }

        if (leftVendorValue == -1 || rightVendorValue == -1) {
            Log.d(TAG, "Gesture Config Failed to read partition value, Please calibrate");
        } else {
            Log.d(TAG, "Gesture Config Left value:" +leftVendorValue + " Right value:" +rightVendorValue);
        }
    }

    private int getRKvendorCompensate(int storageID){
        String value = null;
        for(int j=0; j<3; j++){
            value = PosUtil.read_VendorStorage(storageID);
            if (value != null) {
                if(value.equals("null") || value.equals("NULL") || value.equals("")){
                    value = null;
                    sleepThread(5);
                    continue;
                }

                for(int i=0; i<value.length(); i++) {
                    int chr=value.charAt(i);
                    if(chr < 0 || chr > 127){
                        sleepThread(5);
                        value = null;
                        continue;
                    }
                }
                break;
            } else {
                sleepThread(5);
            }
        }

        if(value == null){
            value = "-1";
        }
        return Integer.valueOf(value);
    }

    private void sleepThread(int delay){
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public int writeVendor(int handside, String value){
        if (handside == 1) {
            return PosUtil.write_VendorStorage(value, 31);
        } else if (handside == 2) {
            return PosUtil.write_VendorStorage(value, 32);
        }
        return -1;
    }
}
