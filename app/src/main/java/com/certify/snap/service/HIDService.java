package com.certify.snap.service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.certify.snap.common.Logger;
import com.telpo.tps550.api.DeviceAlreadyOpenException;
import com.telpo.tps550.api.serial.Serial;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class HIDService extends IntentService {

    private static final String TAG = HIDService.class.getSimpleName();
    private Serial serial = null;
    private InputStream inputStream = null;
    private OutputStream outputStream = null;
    public static boolean readTerminal = true;
    public static final String HID_DATA = "HidData";
    public static final String HID_BROADCAST_ACTION = "com.action.hid.reader";
    public static final String HID_RESTART_SERVICE = "RESTART_HID_SERVICE";

    public HIDService() {
        super("HID Service");
        if (!initHidReader()) {
            readTerminal = false;
        }
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.d(TAG, "HID Service started");
        while (readTerminal) {
            if (inputStream != null) {
                int size = 0;
                try {
                    size = inputStream.available();
                    if (size > 0 && size <= 64) {
                        byte[] buffer = new byte[64];
                        size = inputStream.read(buffer);
                        if (size > 0) {
                            String cardData = new String(buffer, 0, size, "UTF-8");
                            Log.d(TAG, "HID Card data " + cardData);
                            if (cardData.contains("\r")) {
                                cardData = cardData.replace("\r", "");
                            }
                            sendBroadcastMessage(cardData);
                        }
                    }
                } catch (Exception e) {
                    Logger.warn(TAG, "HID Reading data from port exception " + e.getMessage());
                    Log.e(TAG, "HID Size " + size);
                    sendRestartServiceMessage();
                }
            }
        }
        closeHidReader();
        stopSelf();
    }

    /**
     * Method that initiates the serial port
     */
    private boolean initHidReader() {
        boolean result = false;
        try {
            serial = new Serial("/dev/ttyS0", 9600, 0);
        } catch (DeviceAlreadyOpenException | IOException e) {
            Log.e(TAG, "HID Exception while instantiating Serial port");
            Log.e(TAG, e.getMessage());
        }
        if (serial != null) {
            inputStream = serial.getInputStream();
            outputStream = serial.getOutputStream();
            readTerminal = true;
            result = true;
            Log.d(TAG, "HID Port initialized successfully");
        }
        return result;
    }

    /**
     * Method that closes HID serial port
     */
    private void closeHidReader() {
        readTerminal = false;
        try {
            if (inputStream != null) {
                inputStream.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
        } catch (IOException e) {
            Log.e(TAG, "HID Error in closing the serial port stream");
        }
        try {
            if (serial != null) {
                serial.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "HID Error in closing the serial port");
        }
        Log.d(TAG, "HID Serial port closed");
    }

    private void sendBroadcastMessage(String cardId) {
        Intent intent = new Intent();
        intent.setAction(HID_BROADCAST_ACTION);
        intent.putExtra(HID_DATA, cardId);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    private void sendRestartServiceMessage() {
        Intent intent = new Intent();
        intent.setAction(HID_BROADCAST_ACTION);
        intent.putExtra(HID_DATA, HID_RESTART_SERVICE);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        stopSelf();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "HID Service stopped");
        super.onDestroy();
    }
}
