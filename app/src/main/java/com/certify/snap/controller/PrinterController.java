package com.certify.snap.controller;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.graphics.Bitmap;

import com.certify.snap.bluetooth.printer.BasePrint;
import com.certify.snap.bluetooth.printer.ImagePrint;
import com.certify.snap.common.AppSettings;

public class PrinterController {

    private static PrinterController instance = null;
    private PrinterCallbackListener listener;
    private BasePrint mPrint = null;
    private Bitmap printImage = null;

    public interface PrinterCallbackListener {
        void onBluetoothDisabled();
    }

    public static PrinterController getInstance() {
        if (instance == null)
            instance = new PrinterController();

        return instance;
    }

    public void init(Context context) {
        mPrint = new ImagePrint(context);
    }

    public void setBluetoothAdapter() {
        final BluetoothAdapter bluetoothAdapter = BluetoothAdapter
                .getDefaultAdapter();
        if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled()) {
           if(listener != null){
               listener.onBluetoothDisabled();
           }
        }
        mPrint.setBluetoothAdapter(bluetoothAdapter);
    }

    public void setPrinterListener (PrinterCallbackListener callbackListener) {
        this.listener = callbackListener;
    }

    public void setPrintImage(Bitmap image) {
        this.printImage = image;
    }

    public void print() {
        if(printImage!= null) {
            ((ImagePrint) mPrint).setBitmap(printImage);
            mPrint.print();
        }
    }

    public void printOnNormalTemperature() {
        if (AppSettings.isEnablePrinter()) {
            print();
        }
    }
}
