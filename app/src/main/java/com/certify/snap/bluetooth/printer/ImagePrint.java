package com.certify.snap.bluetooth.printer;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.brother.ptouch.sdk.PrinterInfo;
import com.certify.snap.view.PrinterMsgDialog;
import com.certify.snap.view.PrinterMsgHandle;

public class ImagePrint extends BasePrint {
    private static final String TAG = ImagePrint.class.getSimpleName();

    public ImagePrint(Context context, PrinterMsgHandle mHandle, PrinterMsgDialog mDialog) {
        super(context, mHandle, mDialog);
    }

    public ImagePrint(Context context) {
        super(context);
    }

    private Bitmap printBitmap ;
    public void setBitmap(Bitmap bitmap)
    {
        printBitmap = bitmap;
    }
    /**
     * do the particular print
     */
    @Override
    protected void doPrint() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        mPrintResult = mPrinter.printImage(printBitmap);

        if (mPrintResult.errorCode != PrinterInfo.ErrorCode.ERROR_NONE) {
            Log.d(TAG, "Print result " + mPrintResult.errorCode);
        }
    }

}