package com.certify.snap.activity;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.PaintDrawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.certify.callback.PrintStatusCallback;
import com.certify.snap.R;
import com.certify.snap.bluetooth.printer.toshiba.ConnectionDelegate;
import com.certify.snap.bluetooth.printer.toshiba.PrintDialogDelegate;
import com.certify.snap.bluetooth.printer.toshiba.PrintExecuteTask;
import com.certify.snap.bluetooth.printer.toshiba.ToshibaPrinterSettingsActivity;
import com.certify.snap.bluetooth.printer.toshiba.util;
import com.certify.snap.common.AppSettings;
import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.Util;
import com.certify.snap.controller.PrinterController;

import static com.certify.snap.bluetooth.printer.toshiba.Defines.PORTSETTING_PORT_MODE_KEYNAME;
import static com.certify.snap.bluetooth.printer.toshiba.Defines.PRINTER_LIST;
import static com.certify.snap.bluetooth.printer.toshiba.Defines.PRINTER_TYPE_KEYNAME;

public class PrinterViewSettingsActivity extends SettingBaseActivity implements PrinterController.PrinterCallbackListener, PrintStatusCallback {

    private static final String TAG = PrinterViewSettingsActivity.class.getSimpleName();
    TextView titleBrotherBluetoothPrinter, enableBrotherPrinterTextView, brotherBluetoothPrinterConnect, brotherBluetoothPrinterConnection,
            brotherBluetoothPrinterStatus, brotherTestPrint,
            titleToshibaBluetoothPrinter, enableToshibaPrinterTextView, toshibaBluetoothPrinterConnect, toshibaBluetoothPrinterConnection,
            toshibaBluetoothPrinterStatus, toshibaTestPrint;
    ImageView brotherImageView, toshibaImageView;
    Button brotherPrintButton, toshibaPrintButton;
    Typeface rubiklight;
    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_printer_view_settings);
        sp = Util.getSharedPreferences(this);

        final Context context = this.getApplicationContext();

        initView();
        initBrotherPrinter();
        initToshibaPrinter();

        initBluetoothPrinter();
        PrinterController.getInstance().setPrinterListener(this);
    }

    private void initView() {
        titleBrotherBluetoothPrinter = findViewById(R.id.title_bother_bluetooth_printer);
        enableBrotherPrinterTextView = findViewById(R.id.enable_bother_printer_textview);
        brotherBluetoothPrinterStatus = findViewById(R.id.tv_bluetooth_bother_printer_status);
        brotherBluetoothPrinterConnect = findViewById(R.id.bluetooth_bother_printer_connect);
        brotherBluetoothPrinterConnection = findViewById(R.id.tv_bluetooth_bother_printer_connection);
        brotherTestPrint = findViewById(R.id.bother_test_print);
        brotherImageView = findViewById(R.id.bother_imageView);
        brotherPrintButton = findViewById(R.id.bother_print_button);
        brotherTestPrint.setText("Brother Printer");

        titleToshibaBluetoothPrinter = findViewById(R.id.title_toshiba_bluetooth_printer);
        enableToshibaPrinterTextView = findViewById(R.id.enable_toshiba_printer_textview);
        toshibaBluetoothPrinterStatus = findViewById(R.id.tv_bluetooth_toshiba_printer_status);
        toshibaBluetoothPrinterConnect = findViewById(R.id.bluetooth_toshiba_printer_connect);
        toshibaBluetoothPrinterConnection = findViewById(R.id.tv_bluetooth_toshiba_printer_connection);
        toshibaTestPrint = findViewById(R.id.toshiba_test_print);
        toshibaImageView = findViewById(R.id.toshiba_imageView);
        toshibaPrintButton = findViewById(R.id.toshiba_print_button);
        toshibaTestPrint.setText("Toshiba Printer");

        rubiklight = Typeface.createFromAsset(getAssets(),
                "rubiklight.ttf");
        titleBrotherBluetoothPrinter.setTypeface(rubiklight);
        enableBrotherPrinterTextView.setTypeface(rubiklight);
        brotherBluetoothPrinterConnect.setTypeface(rubiklight);
        brotherBluetoothPrinterConnection.setTypeface(rubiklight);
        brotherBluetoothPrinterStatus.setTypeface(rubiklight);
        brotherTestPrint.setTypeface(rubiklight);

        titleToshibaBluetoothPrinter.setTypeface(rubiklight);
        enableToshibaPrinterTextView.setTypeface(rubiklight);
        toshibaBluetoothPrinterConnect.setTypeface(rubiklight);
        toshibaBluetoothPrinterConnection.setTypeface(rubiklight);
        toshibaBluetoothPrinterStatus.setTypeface(rubiklight);
        toshibaTestPrint.setTypeface(rubiklight);

        String printerSettings = "<a style='text-decoration:underline' href='http://www.sample.com'>Settings</a>";
        if (Build.VERSION.SDK_INT >= 24) {
            brotherBluetoothPrinterConnection.setText(Html.fromHtml(printerSettings, Html.FROM_HTML_MODE_LEGACY));
            toshibaBluetoothPrinterConnection.setText(Html.fromHtml(printerSettings, Html.FROM_HTML_MODE_LEGACY));
        } else {
            brotherBluetoothPrinterConnection.setText(Html.fromHtml(printerSettings));
            toshibaBluetoothPrinterConnection.setText(Html.fromHtml(printerSettings));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPreferences != null) {
            if (!sharedPreferences.getString("printer", "NONE").equals("NONE")) {
                brotherBluetoothPrinterStatus.setTextColor(getResources().getColor(R.color.green));
                brotherPrintButton.setBackgroundColor(getResources().getColor(R.color.bg_blue));
            } else {
                brotherBluetoothPrinterStatus.setTextColor(getResources().getColor(R.color.red));
                brotherPrintButton.setBackgroundColor(getResources().getColor(R.color.gray));
            }
            brotherBluetoothPrinterStatus.setText(sharedPreferences.getString("printer", "NONE"));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PrinterController.getInstance().clearData();
    }

    private void initBrotherPrinter() {
        brotherPrinterCheck();
    }

    private void brotherPrinterCheck() {
        RadioGroup radio_group_printer = findViewById(R.id.radio_group_brother_printer);
        RadioButton radio_enable_printer = findViewById(R.id.radio_yes_bother_printer);
        RadioButton radio_disable_printer = findViewById(R.id.radio_no_bother_printer);

        if (sp.getBoolean(GlobalParameters.BROTHER_BLUETOOTH_PRINTER, false))
            radio_enable_printer.setChecked(true);
        else radio_disable_printer.setChecked(true);

        radio_group_printer.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radio_yes_bother_printer)
                    Util.writeBoolean(sp, GlobalParameters.BROTHER_BLUETOOTH_PRINTER, true);
                else Util.writeBoolean(sp, GlobalParameters.BROTHER_BLUETOOTH_PRINTER, false);
            }
        });
    }

    private void initToshibaPrinter() {
        toshibaPrinterCheck();
        try {
            resizeReturnButton();
            printerList(this.getApplicationContext());
            portList(this.getApplicationContext());
        } catch (Exception e) {
            Log.e(TAG, "Exception in initializing Toshiba Printer " + e.getMessage());
        }
    }

    private void toshibaPrinterCheck() {
        RadioGroup radioGroupToshibaPrinter = findViewById(R.id.radio_group_toshiba_printer);
        RadioButton enableToshiba_printer = findViewById(R.id.radio_yes_toshiba_printer);
        RadioButton disableToshibaPrinter = findViewById(R.id.radio_no_toshiba_printer);

        if (sp.getBoolean(GlobalParameters.TOSHIBA_USB_PRINTER, false))
            enableToshiba_printer.setChecked(true);
        else disableToshibaPrinter.setChecked(true);

        radioGroupToshibaPrinter.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radio_yes_toshiba_printer)
                    Util.writeBoolean(sp, GlobalParameters.TOSHIBA_USB_PRINTER, true);
                else Util.writeBoolean(sp, GlobalParameters.TOSHIBA_USB_PRINTER, false);
            }
        });
    }

    public void selectBluetoothPrinter(View view) {
        startActivity(new Intent(this, PrinterSettingsActivity.class));
    }

    private void initBluetoothPrinter() {
        // initialization for printing
        PrinterController.getInstance().init(this, this);
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

    public void printImage(View view) {
        PrinterController.getInstance().setPrintImage(getBitmapFromView(brotherTestPrint, brotherImageView));
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

    @Override
    public void onPrintUsbCommand() {
        runOnUiThread(() -> new PrintExecuteTask(this,
                PrinterController.getInstance().getUsbPrintControl(), this)
                .execute(PrinterController.getInstance().getPrintData()));
    }

    @Override
    public void onPrintUsbSuccess(String status, long resultCode) {
        //add code here
    }

    public void saveAudioSettings(View view) {
        Util.showToast(PrinterViewSettingsActivity.this, getString(R.string.save_success));
        finish();
    }

    // TOSHIBA PRINTER
    public void selectToshibaBluetoothPrinter(View view) {
        startActivity(new Intent(this, ToshibaPrinterSettingsActivity.class));
    }

    private void printerList(Context context){
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_single_choice );
        String orginalPrinterType = util.getPreferences(this, PRINTER_TYPE_KEYNAME);
        int position = 0;
        int selectPosition = 13;
        for(int i=0; i<PRINTER_LIST.length; i++){
            adapter.add(PRINTER_LIST[i]);

            if (orginalPrinterType != null && orginalPrinterType.length() != 0
                    && PRINTER_LIST[i].compareTo(orginalPrinterType) == 0) {
                selectPosition = position;
            }
            position += 1;
        }
        ListView listView = (ListView) findViewById(R.id.StartMenuButtonlist1);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setAdapter(adapter);
        listView.setSelector(new PaintDrawable(Color.BLUE));
        listView.setItemChecked(selectPosition, true);

        String item = "B-FV4D";
        util.setPreferences(context, PRINTER_TYPE_KEYNAME, item);
    }

    private void portList(Context context) {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_single_choice);
        adapter.add("FILE");
        ListView listView = (ListView) findViewById(R.id.port_menu_list);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setAdapter(adapter);

        listView.setSelector(new PaintDrawable(Color.BLUE));

        util.setPreferences(context, PORTSETTING_PORT_MODE_KEYNAME, "FILE");
    }

    private void resizeReturnButton() {
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        Button btnReturn = (Button)this.findViewById(R.id.BttonReturn);
        Point size = new Point();
        display.getSize(size);
        btnReturn.setWidth(size.x);
    }

    public void onClickButtonPrint( View view ) {
        new Thread(() -> {
            if (AppSettings.isPrintUsbEnabled()) {
                PrinterController.getInstance().setPrintData("Test Usb Print");
                PrinterController.getInstance().printUsb();
            }
        }).start();
    }

    @Override
    public void onPrintStatus(String status, int code) {
        Log.d(TAG, "Print Status " + status);
    }
}