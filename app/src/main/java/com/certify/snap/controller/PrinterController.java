package com.certify.snap.controller;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import com.certify.snap.printer.BasePrint;
import com.certify.snap.printer.ImagePrint;
import com.certify.snap.printer.usb.ConnectionData;
import com.certify.snap.printer.usb.ConnectionDelegate;
import com.certify.snap.printer.usb.PrintData;
import com.certify.snap.printer.usb.PrintDialogDelegate;
import com.certify.snap.printer.usb.util;
import com.certify.snap.common.AppSettings;
import com.certify.snap.view.PrinterMsgDialog;
import com.certify.snap.view.PrinterMsgHandle;

import java.io.File;
import java.util.HashMap;

import jp.co.toshibatec.bcp.library.BCPControl;

import static com.certify.snap.printer.usb.Defines.AsynchronousMode;
import static com.certify.snap.printer.usb.Defines.PORTSETTING_FILE_PATH_KEYNAME;
import static com.certify.snap.printer.usb.Defines.PORTSETTING_PORT_MODE_KEYNAME;
import static com.certify.snap.printer.usb.Defines.PRINTER_TYPE_KEYNAME;
import static com.certify.snap.printer.usb.Defines.SynchronousMode;

public class PrinterController implements BCPControl.LIBBcpControlCallBack {
    private static final String TAG = PrinterController.class.getSimpleName();
    private static PrinterController instance = null;
    private PrinterCallbackListener listener;
    private BasePrint mPrint = null;
    private Bitmap printImage = null;
    private PrinterMsgHandle mHandle;
    private PrinterMsgDialog mDialog;
    private Context context;
    private PrintData mPrintData = null;
    private BCPControl mUsbPrintControl = null;
    private ConnectionData mConnectData = null;
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
        initWifiBTPrinter();
        initUsbPrint();
    }

    private void initWifiBTPrinter() {
        mDialog = new PrinterMsgDialog(context);
        mDialog.setActivity(activity);
        mHandle = new PrinterMsgHandle(context, mDialog);
        mPrint = new ImagePrint(context, mHandle, mDialog);
        setBluetoothAdapter();
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

    public void initUsbPrint() {
        mPrintData = new PrintData();
        mConnectData = new ConnectionData();
        String item = "B-FV4D";
        util.setPreferences(context, PRINTER_TYPE_KEYNAME, item);
        util.setPreferences(context, PORTSETTING_PORT_MODE_KEYNAME, "FILE");
        initFile();
        copyIniFile();
        final String myMemotyPath = Environment.getDataDirectory().getPath() + "/data/" + context.getPackageName();
        try {
            util.asset2file(context, "SmpFV4D.lfm", myMemotyPath, "tempLabel.lfm");
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (mUsbPrintControl == null) {
            mUsbPrintControl = new BCPControl(this);
            util.SetPropaty(context, mUsbPrintControl);
           /* String strPrinterType = util.getPreferences(context, PRINTER_TYPE_KEYNAME);
            if (strPrinterType == null || strPrinterType.length() == 0){
                strPrinterType = "B-FV4D";
            }*/
           mConnectionDelegate = new ConnectionDelegate();
           mPrintDialogDelegate = new PrintDialogDelegate(activity, mUsbPrintControl, mPrintData);
           this.openUsbPort(SynchronousMode);
        }
    }

    private void initFile() {
        String filePath = util.getPreferences(context, PORTSETTING_FILE_PATH_KEYNAME);
        if (filePath.length() == 0) {
            filePath = Environment.getExternalStorageDirectory().getPath() + "/PrintImageFile.txt";
        }
        util.setPreferences(context, PORTSETTING_FILE_PATH_KEYNAME, filePath);
    }

    private boolean copyIniFile() {
        String myMemotyPath = Environment.getDataDirectory().getPath() + "/data/" + context.getPackageName();

        File newfile = new File(myMemotyPath);
        if (newfile.exists() == false) {
            if (newfile.mkdirs()) {
            }
        }
        try {
            util.asset2file(context, "ErrMsg0.ini", myMemotyPath, "ErrMsg0.ini");
            util.asset2file(context, "ErrMsg1.ini", myMemotyPath, "ErrMsg1.ini");
            util.asset2file(context, "PRTEP2G.ini", myMemotyPath, "PRTEP2G.ini");
            util.asset2file(context, "PRTEP2GQM.ini", myMemotyPath,
                    "PRTEP2GQM.ini");
            util.asset2file(context, "PRTEP4GQM.ini", myMemotyPath,
                    "PRTEP4GQM.ini");
            util.asset2file(context, "PRTEP4T.ini", myMemotyPath, "PRTEP4T.ini");
            util.asset2file(context, "PRTEV4TT.ini", myMemotyPath, "PRTEV4TT.ini");
            util.asset2file(context, "PRTEV4TG.ini", myMemotyPath, "PRTEV4TG.ini");
            util.asset2file(context, "PRTLV4TT.ini", myMemotyPath, "PRTLV4TT.ini");
            util.asset2file(context, "PRTLV4TG.ini", myMemotyPath, "PRTLV4TG.ini");
            util.asset2file(context, "PRTFP3DGQM.ini", myMemotyPath, "PRTFP3DGQM.ini");
            //ADD 03/12/2018
            util.asset2file(context, "PRTBA400TG.ini", myMemotyPath, "PRTBA400TG.ini");
            util.asset2file(context, "PRTBA400TT.ini", myMemotyPath, "PRTBA400TT.ini");
            util.asset2file(context, "PrtList.ini", myMemotyPath, "PrtList.ini");
            util.asset2file(context, "resource.xml", myMemotyPath, "resource.xml");
            util.asset2file(context, "PRTFP2DG.ini", myMemotyPath, "PRTFP2DG.ini");
            util.asset2file(context, "PRTFV4D.ini", myMemotyPath, "PRTFV4D.ini");
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    public PrintData getPrintData() {
        return mPrintData;
    }

    public void setPrintData(String name, String dateTime) {
        HashMap<String , String> labelItemList = new HashMap<>();
        labelItemList.put( "Name Data",  name );
        labelItemList.put( "TimeScan Data",  dateTime );
        labelItemList.put( "Status Data", "   PASS   " );
        labelItemList.put( "Type Data",  "  Thermal Scan  " );
        mPrintData.setObjectDataList(labelItemList);
    }

    public BCPControl getUsbPrintControl() {
        return mUsbPrintControl;
    }

    private void openUsbPort( int issueMode ) {
        if(!mConnectData.getIsOpen().get()){
            mConnectionDelegate.openPort(activity ,  mUsbPrintControl , mConnectData , issueMode );
            this.mCurrentIssueMode = issueMode;
        }
    }

    public void printUsb() {
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

    public void clearData() {
        mPrintData = null;
        mUsbPrintControl = null;
        mConnectData = null;
        mConnectionDelegate = null;
        mPrintDialogDelegate = null;
    }
}
