package com.certify.snap.activity;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.certify.snap.R;
import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.Util;
import com.certify.snap.controller.PrinterController;

public class PrinterViewSettingsActivity extends SettingBaseActivity implements PrinterController.PrinterCallbackListener{

    TextView title_bluetooth_printer, enable_printer_textview, bluetooth_printer_connect, tv_bluetooth_printer_connection,
            tv_bluetooth_printer_status, testPrint;
    ImageView imageView;
    Button print_button;
    Typeface rubiklight;
    private SharedPreferences sp ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_printer_view_settings);
        sp = Util.getSharedPreferences(this);

        initView();
        printerCheck();

        initBluetoothPrinter();
        PrinterController.getInstance().setPrinterListener(this);
    }

    private void initView(){
        title_bluetooth_printer = findViewById(R.id.title_bluetooth_printer);
        enable_printer_textview = findViewById(R.id.enable_printer_textview);
        tv_bluetooth_printer_status = findViewById(R.id.tv_bluetooth_printer_status);
        bluetooth_printer_connect = findViewById(R.id.bluetooth_printer_connect);
        tv_bluetooth_printer_connection = findViewById(R.id.tv_bluetooth_printer_connection);
        testPrint = findViewById(R.id.test_print);
        imageView = findViewById(R.id.imageView);
        print_button = findViewById(R.id.print_button);
        testPrint.setText("Test Printer");

        rubiklight = Typeface.createFromAsset(getAssets(),
                "rubiklight.ttf");
        title_bluetooth_printer.setTypeface(rubiklight);
        enable_printer_textview.setTypeface(rubiklight);
        bluetooth_printer_connect.setTypeface(rubiklight);
        tv_bluetooth_printer_connection.setTypeface(rubiklight);
        tv_bluetooth_printer_status.setTypeface(rubiklight);
        testPrint.setTypeface(rubiklight);

        String printerSettings = "<a style='text-decoration:underline' href='http://www.sample.com'>Settings</a>";
        if (Build.VERSION.SDK_INT >= 24) {
            tv_bluetooth_printer_connection.setText(Html.fromHtml(printerSettings, Html.FROM_HTML_MODE_LEGACY));
        } else {
            tv_bluetooth_printer_connection.setText(Html.fromHtml(printerSettings));
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if(sharedPreferences != null){
            if(!sharedPreferences.getString("printer", "NONE").equals("NONE")){
                tv_bluetooth_printer_status.setTextColor(getResources().getColor(R.color.green));
                print_button.setBackgroundColor(getResources().getColor(R.color.bg_blue));
            }
            else
            {
                tv_bluetooth_printer_status.setTextColor(getResources().getColor(R.color.red));
                print_button.setBackgroundColor(getResources().getColor(R.color.gray));
            }
            tv_bluetooth_printer_status.setText(sharedPreferences.getString("printer", "NONE"));
        }
    }

    private void printerCheck(){
        RadioGroup radio_group_printer = findViewById(R.id.radio_group_printer);
        RadioButton radio_enable_printer = findViewById(R.id.radio_yes_printer);
        RadioButton radio_disable_printer = findViewById(R.id.radio_no_printer);

        if (sp.getBoolean(GlobalParameters.BLUETOOTH_PRINTER, false))
            radio_enable_printer.setChecked(true);
        else radio_disable_printer.setChecked(true);

        radio_group_printer.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId==R.id.radio_yes_printer)
                    Util.writeBoolean(sp, GlobalParameters.BLUETOOTH_PRINTER, true);
                else Util.writeBoolean(sp, GlobalParameters.BLUETOOTH_PRINTER, false);
            }
        });
    }

    public void selectBluetoothPrinter(View view) {
        startActivity(new Intent(this, PrinterSettingsActivity.class));
    }

    private void initBluetoothPrinter() {
        // initialization for printing
        PrinterController.getInstance().init(this);
        PrinterController.getInstance().setPrinterListener(this);
        PrinterController.getInstance().setBluetoothAdapter();
    }

    public static Bitmap getBitmapFromView(View visitorLayout, ImageView imageView) {
        Bitmap bitmap = Bitmap.createBitmap(visitorLayout.getWidth(), visitorLayout.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        visitorLayout.draw(canvas);
        //imageView.setImageBitmap(bitmap);
        return bitmap;
    }

    public void printImage(View view){
        PrinterController.getInstance().setPrintImage(getBitmapFromView(testPrint, imageView));
        PrinterController.getInstance().print();
    }

    @Override
    public void onBluetoothDisabled() {
        final Intent enableBtIntent = new Intent(
                BluetoothAdapter.ACTION_REQUEST_ENABLE);
        enableBtIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(enableBtIntent);
    }

    @Override
    public void onPrintComplete() {
        //add code here
    }

    @Override
    public void onPrintError() {
        //add code here
    }

    public void saveAudioSettings(View view){
        Util.showToast(PrinterViewSettingsActivity.this, getString(R.string.save_success));
        finish();
    }
}