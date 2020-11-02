package com.certify.snap.controller;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import com.certify.snap.common.Util;
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
import java.io.FileOutputStream;
import java.io.IOException;
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
    private boolean isUserPrintEnabled = false;
    private boolean isPrinting = false;


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
        if (AppSettings.isPrintUsbEnabled())
            initUsbPrint();
        initUserPrintSettings();
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

    public void setPrinting(boolean printing) {
        isPrinting = printing;
    }

    public void print() {
        if (isPrinting) return;
        if(printImage!= null) {
            ((ImagePrint) mPrint).setBitmap(printImage);
            isPrinting = true;
            mPrint.print();
        }
    }

    private void initUserPrintSettings() {
        if (AppSettings.isPrintQrCodeUsers() || AppSettings.isPrintAccessCardUsers() ||
                AppSettings.isPrintWaveUsers() || AppSettings.isPrintAllScan() ) {
            isUserPrintEnabled = true;
        }
    }

    public void printOnNormalTemperature() {
        if (isPrintForUser()) {
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
    }

    public void printOnHighTemperature() {
        if (AppSettings.isPrintHighTemperatureUsers()) {
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
    }

    public boolean isPrintScan() {
        boolean result = false;
        if (isPrintForUser() || AppSettings.isPrintHighTemperatureUsers()) {
            if (AppSettings.isEnablePrinter()) {
                try {
                    if (mPrint != null && !mPrint.getPrinterInfo().macAddress.isEmpty()) {
                        result = true;
                    }
                } catch (Exception e) {
                    Log.e(TAG, "isPrintScan exception occurred");
                }
            } else if (AppSettings.isPrintUsbEnabled()) {
                result = true;
            }
        }
        return result;
    }

    private boolean isPrintForUser() {
        boolean result = false;
        String triggerType = CameraController.getInstance().getTriggerType();
        if (triggerType.equals(CameraController.triggerValue.ACCESSID.toString())) {
            if (AppSettings.isPrintAccessCardUsers() || AppSettings.isPrintAllScan()) {
                result = true;
            }
        } else if (triggerType.equals(CameraController.triggerValue.CODEID.toString())) {
            if (AppSettings.isPrintQrCodeUsers() || AppSettings.isPrintAllScan()) {
                result = true;
            }
        } else if (triggerType.equals(CameraController.triggerValue.WAVE.toString())) {
            if (AppSettings.isPrintWaveUsers() || AppSettings.isPrintAllScan()) {
                result = true;
            }
        } else {
            if (AppSettings.isPrintAllScan()) {
                result= true;
            }
        }
        return result;
    }

    public void printComplete() {
        if (listener != null) {
            listener.onPrintComplete();
            isPrinting = false;
        }
    }

    public void printError() {
        Log.e(TAG, "Print Error");
        if (listener != null) {
            listener.onPrintError();
            isPrinting = false;
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

    public void setPrintData(String nameTitle, String name, String dateTime, String thermalText) {
        if(AppSettings.isPrintUsbEnabled()){
            HashMap<String , String> labelItemList = new HashMap<>();
            labelItemList.put( "Name",  nameTitle );
            labelItemList.put( "Name Data",  name );
            labelItemList.put( "TimeScan Data",  dateTime );
            labelItemList.put( "Status Data", "PASS" );
            labelItemList.put( "Type Data",  thermalText);
            mPrintData.setObjectDataList(labelItemList);
        }
    }

    public void setPrintWaveData(String name, String dateTime, String waveData) {
        if(AppSettings.isPrintUsbEnabled()){
            HashMap<String , String> labelItemList = new HashMap<>();
            labelItemList.put("TimeScan Data", dateTime);
            labelItemList.put("Status Data", "PASS");
            labelItemList.put("Type Data", waveData);
            mPrintData.setObjectDataList(labelItemList);
        }
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
        if (isPrinting) return;
        mPrintData.setCurrentIssueMode( mCurrentIssueMode );
        int printCount = 1;
        mPrintData.setPrintCount( printCount );
        String filePathName = Environment.getDataDirectory().getPath() + "/data/" + context.getPackageName() + "/" + "tempLabel.lfm";
        mPrintData.setLfmFileFullPath( filePathName );
        isPrinting = true;
        if (listener != null) {
            listener.onPrintUsbCommand();
        }
    }

    @Override
    public void BcpControl_OnStatus(String status, long resultCode) {
       if(listener!= null){
           listener.onPrintUsbSuccess(status, resultCode);
       }
    }

    public void updateImageForPrint(Bitmap bitmap){
        if(AppSettings.isPrintUsbEnabled()){
            try {
                Util.saveBitmapImage(bitmap, "image.jpg");
                filterImage("image.jpg","image.jpg","bayer4x4_1",60);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //example: filterImage("image.png","image.jpg","bayer4x4_1",-1);
    //example: filterImage("image.png","image.jpg","none",60);
    private void filterImage(String input, String output, String method , int threshold ) {
        int i, j, idx;

        int newpixel;

        String inFile = Environment.getExternalStorageDirectory().getPath() + "/CertifySnap/Pic/" + input;
        Bitmap hbmp = BitmapFactory.decodeFile(inFile);

        hbmp = hbmp.copy(Bitmap.Config.ARGB_8888, true);
        int width = hbmp.getWidth();
        int height = hbmp.getHeight();

        int[] pixels = new int[width * height];
        hbmp.getPixels(pixels, 0, width, 0, 0, width, height);

        int total_observations = 0;
        int[] observations = new int[256];

        if (method.equals("bayer4x4_1")) {
            int[] bayer4x4 = new int[]{
                    1, 9, 3, 11,
                    13, 5, 15, 7,
                    4, 12, 2, 10,
                    16, 8, 14, 6};

            idx = 0;
            for (j = 0; j < height; j++) {
                for (i = 0; i < width; i++) {

                    int pixel = pixels[idx];
                    byte alpha = (byte) (pixel >> 24 & 0xFF);
                    byte red1 = (byte) (pixel >> 16 & 0xFF);
                    byte green1 = (byte) (pixel >> 8 & 0xFF);
                    byte blue1 = (byte) (pixel);

                    double red = (red1 >= 0) ? red1 : (256 + red1);
                    double green = (green1 >= 0) ? green1 : (256 + green1);
                    double blue = (blue1 >= 0) ? blue1 : (256 + blue1);

                    double mul = bayer4x4[((j % 4) * 4) + (i % 4)];
                    double div = 10.0;
                    red /= div;
                    green /= div;
                    blue /= div;

                    red *= mul;
                    green *= mul;
                    blue *= mul;

                    double new_pixel = (0.2126 * red + 0.7152 * green + 0.0722 * blue);

                    newpixel = (int) new_pixel;
                    if (newpixel > 255) newpixel = 255;

                    observations[newpixel]++;
                    total_observations++;

                    pixels[idx++] = newpixel;
                }
            }
        }

        // find the threshold automatically if set to auto
        int monochrome_threshold = 255;
        if (threshold < 0) {
            int mid_observations = 0;
            for (i = 0; i < 256; i++) {
                // find the mid point
                if (mid_observations + observations[i] >= total_observations / 2) {
                    monochrome_threshold = i;
                    break;
                }
                mid_observations += observations[i];
            }

            if (monochrome_threshold == 255) {
                monochrome_threshold = 254;
            }

            if (monochrome_threshold == 0) {
                monochrome_threshold = 1;
            }
        } else {

            monochrome_threshold = threshold;
        }

        // recreate pixels
        idx = 0;
        for (j = 0; j < height; j++) {
            for (i = 0; i < width; i++) {

                int pixel = pixels[idx];
                if (pixel > monochrome_threshold) pixel = 255;
                else pixel = 0;

                pixels[idx++] = android.graphics.Color.argb(255, pixel, pixel, pixel);
                ;
            }
        }

        hbmp.setPixels(pixels, 0, width, 0, 0, width, height);

        //save to the file
        String filename = Environment.getExternalStorageDirectory().getPath() + "/CertifySnap/Pic/" + output;
        try (FileOutputStream out = new FileOutputStream(filename)) {
            hbmp.compress(Bitmap.CompressFormat.JPEG, 100, out); // bmp is your Bitmap instance
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void clearData() {
        mPrintData = null;
        mUsbPrintControl = null;
        mConnectData = null;
        mConnectionDelegate = null;
        mPrintDialogDelegate = null;
        isPrinting = false;
    }
}
