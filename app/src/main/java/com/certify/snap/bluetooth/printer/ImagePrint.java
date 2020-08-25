package com.certify.snap.bluetooth.printer;

import android.content.Context;
import android.graphics.Bitmap;

import com.certify.snap.view.PrinterMsgDialog;
import com.certify.snap.view.PrinterMsgHandle;

public class ImagePrint extends BasePrint {

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

        mPrintResult = mPrinter.printImage(printBitmap);
    }

}