package com.certify.snap.controller;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;

public class PrinterController {

    private static PrinterController instance = null;
    private PrinterCallbackListener listener;

    public interface PrinterCallbackListener {
        void onBluetoothDisabled();
    }

    public static PrinterController getInstance() {
        if (instance == null)
            instance = new PrinterController();

        return instance;
    }

    public BluetoothAdapter getBluetoothAdapter() {
        final BluetoothAdapter bluetoothAdapter = BluetoothAdapter
                .getDefaultAdapter();
        if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled()) {
           if(listener != null){
               listener.onBluetoothDisabled();
           }
        }
        return bluetoothAdapter;
    }

    public void setPrinterListener (PrinterCallbackListener callbackListener) {
        this.listener = callbackListener;
    }

}
