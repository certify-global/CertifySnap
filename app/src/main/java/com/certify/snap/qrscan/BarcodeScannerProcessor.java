/*
 * Copyright 2020 Google LLC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.certify.snap.qrscan;

import android.content.Context;
import android.graphics.Point;
import android.util.Log;

import androidx.annotation.NonNull;

import com.certify.callback.BarcodeSendData;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.barcode.Barcode;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.common.InputImage;

import java.util.List;

/**
 * Barcode Detector Demo.
 */
public class BarcodeScannerProcessor extends VisionProcessorBase<List<Barcode>> {

    private static final String TAG = "BarcodeProcessor";

    private final BarcodeScanner barcodeScanner;
    private BarcodeSendData barcodeSendData;

    public BarcodeScannerProcessor(Context context, BarcodeSendData barcodeSendData) {
        super(context);
        // Note that if you know which format of barcode your app is dealing with, detection will be
        // faster to specify the supported barcode formats one by one, e.g.
        BarcodeScannerOptions options =
                new BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(
                                Barcode.FORMAT_QR_CODE,
                                Barcode.FORMAT_AZTEC,
                                Barcode.FORMAT_DATA_MATRIX,
                                Barcode.FORMAT_ALL_FORMATS)
                        .build();
        barcodeScanner = BarcodeScanning.getClient(options);
        this.barcodeSendData=barcodeSendData;
    }

    public void setListener (BarcodeSendData listener) {
        barcodeSendData = listener;
    }

    @Override
    public void stop() {
        super.stop();
        barcodeScanner.close();
    }

    @Override
    protected Task<List<Barcode>> detectInImage(InputImage image) {
        return barcodeScanner.process(image);
    }

    @Override
    protected void onSuccess(
            @NonNull List<Barcode> barcodes, @NonNull GraphicOverlay graphicOverlay) {
        if (barcodes.isEmpty()) {
          //  Log.v(MANUAL_TESTING_LOG, "No barcode has been detected");
        }
        for (int i = 0; i < barcodes.size(); ++i) {
            Barcode barcode = barcodes.get(i);
            if (barcodeSendData != null) {
                barcodeSendData.onBarcodeData(barcode.getDisplayValue());
                barcodeSendData = null;     //Set to null to process the bar code only once
            }
           // graphicOverlay.add(new BarcodeGraphic(graphicOverlay, barcode));
        }
    }


    @Override
    protected void onFailure(@NonNull Exception e) {
      //  Log.e(TAG, "Barcode detection failed " + e);
    }
}
