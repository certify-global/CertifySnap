package com.certify.snap.controller;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.certify.snap.bluetooth.printer.BasePrint;
import com.certify.snap.bluetooth.printer.ImagePrint;
import com.certify.snap.common.AppSettings;
import com.certify.snap.view.PrinterMsgDialog;
import com.certify.snap.view.PrinterMsgHandle;

public class PrinterController {
    private static final String TAG = PrinterController.class.getSimpleName();
    private static PrinterController instance = null;
    private PrinterCallbackListener listener;
    private BasePrint mPrint = null;
    private Bitmap printImage = null;
    private PrinterMsgHandle mHandle;
    private PrinterMsgDialog mDialog;

    public interface PrinterCallbackListener {
        void onBluetoothDisabled();
        void onPrintComplete();
        void onPrintError();
    }

    public static PrinterController getInstance() {
        if (instance == null)
            instance = new PrinterController();

        return instance;
    }

    public void init(Context context, Activity activity) {
        mDialog = new PrinterMsgDialog(context);
        mDialog.setActivity(activity);
        mHandle = new PrinterMsgHandle(context, mDialog);
        mPrint = new ImagePrint(context, mHandle, mDialog);
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
        new Thread(() -> {
            if (AppSettings.isEnablePrinter()) {
                print();
            }
        }).start();
    }

    public boolean isPrintScan() {
        boolean result = false;
        if (AppSettings.isEnablePrinter()) {
            try {
                if (mPrint != null && !mPrint.getPrinterInfo().macAddress.isEmpty()) {
                    result = true;
                }
            } catch (Exception e) {
                Log.e(TAG, "isPrintScan exception occurred");
                if (listener != null) {
                    listener.onPrintError();
                }
            }
        }
        return result;
    }

    public void printComplete() {
        if (listener != null) {
            listener.onPrintComplete();
        }
    }

    public void printError() {
        Log.e(TAG, "Print Error");
        if (listener != null) {
            listener.onPrintError();
        }
    }
}
