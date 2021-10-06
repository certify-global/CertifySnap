package com.certify.snap.activity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;

import com.certify.snap.R;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.BarcodeView;
import com.journeyapps.barcodescanner.DefaultDecoderFactory;

import java.util.ArrayList;
import java.util.Collection;

public class QrCodeScannerActivity extends Activity {

    private static final String TAG = QrCodeScannerActivity.class.getSimpleName();
    private BarcodeView barcodeScanner;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_code_reader);

        initView();
        barcodeScanner.getCameraSettings().setRequestedCameraId(0);
        initQrCodeScanner();
    }

    @Override
    protected void onResume() {
        super.onResume();
        barcodeScanner.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    private void initView() {
        barcodeScanner = findViewById(R.id.barcode_scanner);
    }

    private void initQrCodeScanner() {
        BarcodeCallback barcodeCallback = new BarcodeCallback() {

            @Override
            public void barcodeResult(BarcodeResult result) {
                Log.d(TAG, "barcode result ");
            }
        };
        Collection<BarcodeFormat> decodeFormats = new ArrayList<>();
        decodeFormats.add(BarcodeFormat.AZTEC);
        decodeFormats.add(BarcodeFormat.QR_CODE);
        barcodeScanner.setDecoderFactory(new DefaultDecoderFactory(decodeFormats));
        barcodeScanner.decodeContinuous(barcodeCallback);
    }
}
