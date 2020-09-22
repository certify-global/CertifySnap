package com.certify.snap.controller;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import com.certify.snap.bluetooth.printer.BasePrint;
import com.certify.snap.bluetooth.printer.ImagePrint;
import com.certify.snap.bluetooth.printer.toshiba.ConnectionData;
import com.certify.snap.bluetooth.printer.toshiba.ConnectionDelegate;
import com.certify.snap.bluetooth.printer.toshiba.PrintData;
import com.certify.snap.bluetooth.printer.toshiba.PrintDialogDelegate;
import com.certify.snap.bluetooth.printer.toshiba.util;
import com.certify.snap.common.AppSettings;
import com.certify.snap.view.PrinterMsgDialog;
import com.certify.snap.view.PrinterMsgHandle;

import java.util.HashMap;

import jp.co.toshibatec.bcp.library.BCPControl;

import static com.certify.snap.bluetooth.printer.toshiba.Defines.AsynchronousMode;
import static com.certify.snap.bluetooth.printer.toshiba.Defines.PORTSETTING_FILE_PATH_KEYNAME;
import static com.certify.snap.bluetooth.printer.toshiba.Defines.PORTSETTING_PORT_MODE_KEYNAME;
import static com.certify.snap.bluetooth.printer.toshiba.Defines.PRINTER_TYPE_KEYNAME;
import static com.certify.snap.bluetooth.printer.toshiba.Defines.SynchronousMode;

public class PrinterController implements BCPControl.LIBBcpControlCallBack {
    private static final String TAG = PrinterController.class.getSimpleName();
    private static PrinterController instance = null;
    private PrinterCallbackListener listener;
    private BasePrint mPrint = null;
    private Bitmap printImage = null;
    private PrinterMsgHandle mHandle;
    private PrinterMsgDialog mDialog;
    private Context context;
    private PrintData mPrintData = new PrintData();
    private BCPControl mUsbPrintControl = null;
    private ConnectionData mConnectData = new ConnectionData();
    private ConnectionDelegate mConnectionDelegate = null;
    private PrintDialogDelegate mPrintDialogDelegate = null;
    private int mCurrentIssueMode = AsynchronousMode;
    private Activity activity;


    public interface PrinterCallbackListener {
        void onBluetoothDisabled();
        void onPrintComplete();
        void onPrintError();
        void onPrintUsbCommand();
        void onPrintUsbSuccess(String status, long resultCode);
    }

    public static PrinterController getInstance() {
        if (instance == null)
            instance = new PrinterController();

        return instance;
    }

    public void init(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;
        mDialog = new PrinterMsgDialog(context);
        mDialog.setActivity(activity);
        mHandle = new PrinterMsgHandle(context, mDialog);
        mPrint = new ImagePrint(context, mHandle, mDialog);
        initUsbPrint();
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

        new Thread(() -> {
            if (AppSettings.isPrintUsbEnabled()) {
                printUsb();
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
        } else if (AppSettings.isPrintUsbEnabled()) {
            result = true;
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

    public PrintDialogDelegate getPrintDialogDelegate() {
        return mPrintDialogDelegate;
    }

    private void initUsbPrint(){
        String item = "B-FV4D";
        util.setPreferences(context, PRINTER_TYPE_KEYNAME, item);
        util.setPreferences(context, PORTSETTING_PORT_MODE_KEYNAME, "FILE");
        initFile();
        final String myMemotyPath = Environment.getDataDirectory().getPath() + "/data/" + context.getPackageName();
        try {
            util.asset2file(context, "SmpFV4D.lfm", myMemotyPath, "tempLabel.lfm");
        } catch (Exception e) {
            e.printStackTrace();
        }

        if( mUsbPrintControl == null ) {
            mUsbPrintControl = new BCPControl( this );
            util.SetPropaty( context , mUsbPrintControl );
           /* String strPrinterType = util.getPreferences(context, PRINTER_TYPE_KEYNAME);
            if (strPrinterType == null || strPrinterType.length() == 0){
                strPrinterType = "B-FV4D";
            }*/
            mConnectionDelegate = new ConnectionDelegate();
            mPrintDialogDelegate = new PrintDialogDelegate( activity , mUsbPrintControl, mPrintData );
            this.openUsbPort( SynchronousMode );
        }
    }

    private void initFile() {
        String filePath = util.getPreferences(context, PORTSETTING_FILE_PATH_KEYNAME);
        if (filePath.length() == 0) {
            filePath = Environment.getExternalStorageDirectory().getPath() + "/PrintImageFile.txt";
        }
        util.setPreferences(context, PORTSETTING_FILE_PATH_KEYNAME, filePath);
    }

    public PrintData getPrintData() {
        return mPrintData;
    }

    public void setPrintData(String data) {
        HashMap<String , String> labelItemList = new HashMap<>();
        labelItemList.put("Name Data",  data);
        mPrintData.setObjectDataList(labelItemList);
    }

    public BCPControl getUsbPrintControl() {
        return mUsbPrintControl;
    }

    private void openUsbPort( int issueMode ) {
        if( mConnectData.getIsOpen().get() == false ){
            mConnectionDelegate.openPort(activity ,  mUsbPrintControl , mConnectData , issueMode );
            this.mCurrentIssueMode = issueMode;
        }
    }

    private void printUsb() {
        mPrintData.setCurrentIssueMode( mCurrentIssueMode );
        int printCount = 1;
        mPrintData.setPrintCount( printCount );
        String filePathName = Environment.getDataDirectory().getPath() + "/data/" + context.getPackageName() + "/" + "tempLabel.lfm";
        mPrintData.setLfmFileFullPath( filePathName );
        if (listener != null) {
            listener.onPrintUsbCommand();
        }
    }

    @Override
    public void BcpControl_OnStatus(String status, long resultCode) {
       if(listener!= null){
           Log.d(TAG, "Deep onPrintUsb Success");
           listener.onPrintUsbSuccess(status, resultCode);
       }
    }
}
