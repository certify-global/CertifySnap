package com.certify.snap.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.budiyev.android.codescanner.AutoFocusMode;
import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.budiyev.android.codescanner.ErrorCallback;
import com.budiyev.android.codescanner.ScanMode;
import com.certify.snap.R;
import com.certify.snap.controller.QrCodeController;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;

import java.util.ArrayList;
import java.util.List;

public class EUCodeDemoActivity extends AppCompatActivity {

    private CodeScanner mCodeScanner;
    List<BarcodeFormat> barcodeFormatArrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eucode_demo);
        CodeScannerView scannerView = findViewById(R.id.scanner_view);
        barcodeFormatArrayList.add(BarcodeFormat.QR_CODE);
        barcodeFormatArrayList.add(BarcodeFormat.DATA_MATRIX);
//        barcodeFormatArrayList.add(BarcodeFormat.AZTEC);
//        barcodeFormatArrayList.add(BarcodeFormat.PDF_417);
//        barcodeFormatArrayList.add(BarcodeFormat.EAN_13);
//        barcodeFormatArrayList.add(BarcodeFormat.EAN_8);
//        barcodeFormatArrayList.add(BarcodeFormat.UPC_E);
//        barcodeFormatArrayList.add(BarcodeFormat.UPC_A);
//        barcodeFormatArrayList.add(BarcodeFormat.CODE_128);
//        barcodeFormatArrayList.add(BarcodeFormat.CODE_93);
//        barcodeFormatArrayList.add(BarcodeFormat.CODE_39);
//        barcodeFormatArrayList.add(BarcodeFormat.CODABAR);
//        barcodeFormatArrayList.add(BarcodeFormat.ITF);
        mCodeScanner = new CodeScanner(this, scannerView);
        mCodeScanner.setCamera(CodeScanner.CAMERA_BACK);
        mCodeScanner.setScanMode(ScanMode.SINGLE);
        mCodeScanner.setAutoFocusMode(AutoFocusMode.SAFE);
        mCodeScanner.setAutoFocusEnabled(true);
        mCodeScanner.setFormats(barcodeFormatArrayList);
        mCodeScanner.setFlashEnabled(true);
        mCodeScanner.setTouchFocusEnabled(false);
        mCodeScanner.setDecodeCallback(new DecodeCallback() {
            @Override
            public void onDecoded(@NonNull Result result) {
                String guid = result.getText();
                Log.i("BBBBBBBBBBBBBBBB 0 ", guid);
                if (guid.startsWith("shc:")) {
                    QrCodeController.getInstance().smartHealthCard(guid, EUCodeDemoActivity.this);
                } else if (guid.startsWith("HC1:")) {
                    QrCodeController.getInstance().parseQrText(guid, EUCodeDemoActivity.this);
                } else {
                    Log.i("BBBBBBBBBBBBBBBB", guid);
                }

            }
        });
        mCodeScanner.setErrorCallback(new ErrorCallback() {
            @Override

            public void onError(@NonNull Exception error) {
                Log.i("onError", error.getMessage());
            }
        });

        mCodeScanner.startPreview();
    }

    @Override
    protected void onPause() {
        mCodeScanner.releaseResources();
        super.onPause();
    }

    @Override
    protected void onResume() {
        mCodeScanner.startPreview();
        super.onResume();
    }
}