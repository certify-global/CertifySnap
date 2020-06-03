package com.certify.snap.controller;

import android.util.Log;

import com.common.pos.api.util.PosUtil;

import java.util.Timer;
import java.util.TimerTask;

public class AccessCardController  {
    private static final String TAG = AccessCardController.class.getSimpleName();
    private static AccessCardController mInstance = null;
    private int mRelayTime = 0;
    private boolean isAccessControlEnabled = false;
    private boolean mBlockAccessHighTemp = false;
    private Timer mRelayTimer;
    private boolean isAutomaticDoorAccess = false;
    private int mWeiganControllerFormat = 26;
    private String mAccessCardID = "";

    public static AccessCardController getInstance() {
        if (mInstance == null) {
            mInstance = new AccessCardController();
        }
        return mInstance;
    }

    /*public void init(Activity context) {
        mNfcAdapter = M1CardUtils.isNfcAble(context);
    }*/

    public void setAccessControlEnabled(boolean value) {
        isAccessControlEnabled = value;
    }

    public boolean isAccessControlEnabled() {
        return isAccessControlEnabled;
    }

    public void setBlockAccessOnHighTemp(boolean value) {
        mBlockAccessHighTemp = value;
    }

    public void setAutomaticDoorEnabled(boolean value) {
        isAutomaticDoorAccess = value;
    }

    public boolean isAutomaticDoorEnabled() {
        return isAutomaticDoorAccess;
    }

    public void unlockDoor() {
        if(!isAutomaticDoorAccess) return;
        unLockStandAloneDoor();
        unLockWeiganDoorController();
    }

    public void unlockDoorOnHighTemp() {
        if(mBlockAccessHighTemp) return;
        unlockDoor();
    }

    public void setRelayTime(int time) {
        mRelayTime = time;
    }

    public void setWeiganControllerFormat(int value) {
        mWeiganControllerFormat = value;
    }

    public void setAccessCardId(String cardId) {
        mAccessCardID = cardId;
    }

    private void startRelayTimer() {
        mRelayTimer = new Timer();
        mRelayTimer.schedule(new TimerTask() {
            public void run() {
                lockStandAloneDoor();
                this.cancel();
            }
        }, mRelayTime * 1000);
    }

    private void unLockStandAloneDoor() {
        if (!isAutomaticDoorAccess) return;
        int result = PosUtil.setRelayPower(1);
        if (result != 0) {
            Log.d(TAG, "Error in opening the door");
            return;
        }
        startRelayTimer();
    }

    private void unLockWeiganDoorController() {
        //Check if its 34 Bit or 26 Bit Weigan controller and send signal accordingly
        if (mWeiganControllerFormat == 26) {
            unlock26BitDoorController();
        } else if (mWeiganControllerFormat == 34) {
            unlock34BitDoorController();
        }
    }

    public void lockStandAloneDoor() {
        if (!isAutomaticDoorAccess) return;
        if (mRelayTimer != null) mRelayTimer.cancel();
        int result = PosUtil.setRelayPower(0);
        if (result != 0) {
            Log.d(TAG, "Error in closing the door");
        }
    }

    private void unlock26BitDoorController() {
        if(mAccessCardID.isEmpty()) return;
        int result = PosUtil.getWg26Status(Long.parseLong(mAccessCardID));
        if (result != 0) {
            Log.d(TAG, "Error in opening the door");
        }
    }

    private void unlock34BitDoorController() {
        if(mAccessCardID.isEmpty()) return;
        int result = PosUtil.getWg34Status(Long.parseLong(mAccessCardID));
        if (result != 0) {
            Log.d(TAG, "Error in opening the door");
        }
    }
}
