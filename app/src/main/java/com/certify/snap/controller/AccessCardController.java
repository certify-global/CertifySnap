package com.certify.snap.controller;

import android.util.Log;

import com.certify.snap.model.AccessControlModel;
import com.common.pos.api.util.PosUtil;

import java.util.Timer;
import java.util.TimerTask;

public class AccessCardController  {
    private static final String TAG = AccessCardController.class.getSimpleName();
    private static AccessCardController mInstance = null;
    private boolean mEnableRelay = false;
    private boolean mAllowAnonymous = false;
    private boolean mNormalRelayMode = false;
    private boolean mReverseRelayMode = false;
    private boolean mStopRelayOnHighTemp = false;
    private boolean mEnableWeigan = false;
    private int mRelayTime = 0;
    private Timer mRelayTimer;
    private int mWeiganControllerFormat = 26;

    private String mAccessCardID = "";
    private String mAccessIdDb = "";

    public static AccessCardController getInstance() {
        if (mInstance == null) {
            mInstance = new AccessCardController();
        }
        return mInstance;
    }

    public void init() {
        //mNfcAdapter = M1CardUtils.isNfcAble(context);
        AccessControlModel.getInstance().clearData();
        mAccessCardID = "";
        mAccessIdDb = "";
    }

    public boolean isEnableRelay() {
        return mEnableRelay;
    }

    public void setEnableRelay(boolean mEnableRelay) {
        this.mEnableRelay = mEnableRelay;
    }

    public boolean isAllowAnonymous() {
        return mAllowAnonymous;
    }

    public void setAllowAnonymous(boolean mAllowAnonymous) {
        this.mAllowAnonymous = mAllowAnonymous;
    }

    public void setNormalRelayMode(boolean mNormalRelayMode) {
        this.mNormalRelayMode = mNormalRelayMode;
    }

    public void setReverseRelayMode(boolean mReverseRelayMode) {
        this.mReverseRelayMode = mReverseRelayMode;
    }

    public void setStopRelayOnHighTemp(boolean mStopRelayOnHighTemp) {
        this.mStopRelayOnHighTemp = mStopRelayOnHighTemp;
    }

    public void setEnableWeigan(boolean mEnableWeigan) {
        this.mEnableWeigan = mEnableWeigan;
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

    public String getAccessCardID() {
        return mAccessCardID;
    }

    public void setAccessIdDb(String mAccessIdDb) {
        this.mAccessIdDb = mAccessIdDb;
    }

    public void unlockDoor() {
        if (mNormalRelayMode) {
            unLockStandAloneDoor();
            unLockWeiganDoorController();
        }
    }

    public void unlockDoorOnHighTemp() {
        if (mNormalRelayMode) {
            if (!mStopRelayOnHighTemp) {
                unlockDoor();
            }
            return;
        }
        if (mReverseRelayMode) {
            unLockStandAloneDoor();
            unLockWeiganDoorController();
        }
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
        if (!mEnableRelay) return;
        int result = PosUtil.setRelayPower(1);
        if (result != 0) {
            Log.d(TAG, "Error in opening the door");
            return;
        }
        startRelayTimer();
    }

    private void unLockWeiganDoorController() {
        if (!mEnableWeigan) return;
        //Check if its 34 Bit or 26 Bit Weigan controller and send signal accordingly
        if (mWeiganControllerFormat == 26) {
            unlock26BitDoorController();
        } else if (mWeiganControllerFormat == 34) {
            unlock34BitDoorController();
        }
    }

    public void lockStandAloneDoor() {
        if (mRelayTimer != null) mRelayTimer.cancel();
        int result = PosUtil.setRelayPower(0);
        if (result != 0) {
            Log.d(TAG, "Error in closing the door");
        }
    }

    private void unlock26BitDoorController() {
        if (mAccessCardID.isEmpty()) {
            if (!mAccessIdDb.isEmpty()) {
                sendWg26BitSignal(mAccessIdDb);
            }
            return;
        }
        sendWg26BitSignal(mAccessCardID);
    }

    private void sendWg26BitSignal(String cardId) {
        if (!mEnableWeigan) return;
        int result = PosUtil.getWg26Status(Long.parseLong(cardId));
        if (result != 0) {
            Log.d(TAG, "Error in opening the door");
        }
    }

    private void unlock34BitDoorController() {
        if (mAccessCardID.isEmpty()) {
            if (!mAccessIdDb.isEmpty()) {
                sendWg34BitSignal(mAccessIdDb);
            }
            return;
        }
        sendWg34BitSignal(mAccessCardID);
    }

    private void sendWg34BitSignal(String cardId) {
        if (!mEnableWeigan) return;
        int result = PosUtil.getWg34Status(Long.parseLong(cardId));
        if (result != 0) {
            Log.d(TAG, "Error in opening the door");
        }
    }
}
