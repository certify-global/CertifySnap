package com.certify.snap.bluetooth.printer;

import android.content.Context;
import android.graphics.Bitmap;

public class ImagePrint extends BasePrint {

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