package com.certify.snap.activity;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.PaintDrawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
import android.view.View;
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
import com.certify.snap.printer.usb.PrintExecuteTask;
import com.certify.snap.printer.usb.util;
import com.certify.snap.common.AppSettings;
import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.Util;
import com.certify.snap.controller.PrinterController;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.certify.snap.printer.usb.Defines.PORTSETTING_PORT_MODE_KEYNAME;
import static com.certify.snap.printer.usb.Defines.PRINTER_LIST;
import static com.certify.snap.printer.usb.Defines.PRINTER_TYPE_KEYNAME;

public class PrinterSettingsActivity extends SettingsBaseActivity implements PrinterController.PrinterCallbackListener, PrintStatusCallback {

    private static final String TAG = PrinterSettingsActivity.class.getSimpleName();

    TextView titleBrotherBluetoothPrinter, enableBrotherPrinterTextView, brotherBluetoothPrinterConnect, brotherBluetoothPrinterConnection,
            brotherBluetoothPrinterStatus, brotherTestPrint,
            titleToshibaBluetoothPrinter, enableToshibaPrinterTextView,
            printerOptionsTitle, printAllScanTitle, printAccessCardTitle, printQRCodeTitle, printWaveUsersTitle, printHighTemperatureTitle,
            titleLabelOptions,
            printLabelFaceTitle, printLabelNameTitle, printLabelNormalTemp, printLabelHighTemp, printLabelWaveAnswers;
    RadioGroup radioGroupPrinter, radioGroupToshibaPrinter, radioGroupPrintAllScan, radioGroupPrintAccessCard, radioGroupPrintQRCode,
            radioGroupPrintWave, radioGroupPrintHighTemperature,
            radioGroupPrintLabelFace, radioGroupPrintLabelName, radioGroupPrintLabelNormalTemp, radioGroupPrintLabelHighTemp,
            radioGroupPrintLabelWaveAnswers;
    RadioButton radioEnableBrotherPrinter, radioDisableBrotherPrinter, radioEnableToshibaPrinter, radioDisableToshibaPrinter,
            radioButtonYesPrintAllScans, radioButtonNoPrintAllScans, radioButtonYesPrintAccessCard, radioButtonNoPrintAccessCard,
            radioButtonYesPrintQRCode, radioButtonNoPrintQRCode, radioButtonYesPrintWave, radioButtonNoPrintWave,
            radioButtonYesPrintHighTemperature, radioButtonNoPrintHighTemperature, radioYesPrintLabelFace, radioNoPrintLabelFace,
            radioYesPrintLabelName, radioNoPrintLabelName, radioYesPrintLabelNormalTemp, radioNoPrintLabelNormalTemp,
            radioYesPrintLabelHighTemp, radioNoPrintLabelHighTemp, radioYesPrintLabelWaveAnswers, radioNoPrintLabelWaveAnswers;
    Button brotherPrintButton;
    ImageView brotherImageView;
    EditText editTextNameLabel;

    Typeface rubiklight;
    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_printer_settings);
        sp = Util.getSharedPreferences(this);

        initView();
        printerOptionCheck();
        initBrotherPrinter();
        initToshibaPrinter();
        printerLabelOptionsCheck();

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
        radioGroupPrinter = findViewById(R.id.radio_group_brother_printer);
        radioEnableBrotherPrinter = findViewById(R.id.radio_yes_bother_printer);
        radioDisableBrotherPrinter = findViewById(R.id.radio_no_bother_printer);
        radioGroupToshibaPrinter = findViewById(R.id.radio_group_toshiba_printer);
        radioEnableToshibaPrinter = findViewById(R.id.radio_yes_toshiba_printer);
        radioDisableToshibaPrinter = findViewById(R.id.radio_no_toshiba_printer);

        // printer Options
        printerOptionsTitle = findViewById(R.id.title_printer_options);
        printAllScanTitle = findViewById(R.id.print_all_scan_title);
        radioGroupPrintAllScan = findViewById(R.id.radio_group_print_all_scan);
        radioButtonYesPrintAllScans = findViewById(R.id.radio_yes_print_all_scan);
        radioButtonNoPrintAllScans = findViewById(R.id.radio_no_print_all_scan);
        //Access Card
        printAccessCardTitle = findViewById(R.id.print_access_card_title);
        radioGroupPrintAccessCard = findViewById(R.id.radio_group_print_access_card);
        radioButtonYesPrintAccessCard = findViewById(R.id.radio_yes_print_access_card);
        radioButtonNoPrintAccessCard = findViewById(R.id.radio_no_print_access_card);
        //QR Code
        printQRCodeTitle = findViewById(R.id.print_qr_code_title);
        radioGroupPrintQRCode = findViewById(R.id.radio_group_print_qr_code);
        radioButtonYesPrintQRCode = findViewById(R.id.radio_yes_print_qr_code);
        radioButtonNoPrintQRCode = findViewById(R.id.radio_no_print_qr_code);
        //Wave Users
        printWaveUsersTitle = findViewById(R.id.print_wave_users_title);
        radioGroupPrintWave = findViewById(R.id.radio_group_print_wave_users);
        radioButtonYesPrintWave = findViewById(R.id.radio_yes_print_wave_users);
        radioButtonNoPrintWave = findViewById(R.id.radio_no_print_wave_users);
        //High Temperature
        printHighTemperatureTitle = findViewById(R.id.print_high_temperature_title);
        radioGroupPrintHighTemperature = findViewById(R.id.radio_group_print_high_temperature);
        radioButtonYesPrintHighTemperature = findViewById(R.id.radio_yes_print_high_temperature);
        radioButtonNoPrintHighTemperature = findViewById(R.id.radio_no_print_high_temperature);
        // Label options
        titleLabelOptions = findViewById(R.id.title_label_options);
        //print label face
        printLabelFaceTitle = findViewById(R.id.print_label_face_title);
        radioGroupPrintLabelFace = findViewById(R.id.radio_group_print_label_face);
        radioYesPrintLabelFace = findViewById(R.id.radio_yes_print_label_face);
        radioNoPrintLabelFace = findViewById(R.id.radio_no_print_label_face);
        //Print label name
        printLabelNameTitle = findViewById(R.id.print_label_name_title);
        radioGroupPrintLabelName = findViewById(R.id.radio_group_print_label_name);
        radioYesPrintLabelName = findViewById(R.id.radio_yes_print_label_name);
        radioNoPrintLabelName = findViewById(R.id.radio_no_print_label_name);
        //Print label normal temp Value
        printLabelNormalTemp = findViewById(R.id.print_label_normal_temp);
        radioGroupPrintLabelNormalTemp = findViewById(R.id.radio_group_print_label_normal_temp);
        radioYesPrintLabelNormalTemp = findViewById(R.id.radio_yes_print_label_normal_temp);
        radioNoPrintLabelNormalTemp = findViewById(R.id.radio_no_print_label_normal_temp);
        //Print label high temp value
        printLabelHighTemp = findViewById(R.id.print_label_high_temp);
        radioGroupPrintLabelHighTemp = findViewById(R.id.radio_group_print_label_high_temp);
        radioYesPrintLabelHighTemp = findViewById(R.id.radio_yes_print_label_high_temp);
        radioNoPrintLabelHighTemp = findViewById(R.id.radio_no_print_label_high_temp);
        //Print label wave answers
        printLabelWaveAnswers = findViewById(R.id.print_label_wave_answers);
        radioGroupPrintLabelWaveAnswers = findViewById(R.id.radio_group_print_label_wave_answers);
        radioYesPrintLabelWaveAnswers = findViewById(R.id.radio_yes_print_label_wave_answers);
        radioNoPrintLabelWaveAnswers = findViewById(R.id.radio_no_print_label_wave_answers);

        editTextNameLabel = findViewById(R.id.edit_text_name_label);

        brotherTestPrint.setText("Brother Printer");

        titleToshibaBluetoothPrinter = findViewById(R.id.title_toshiba_bluetooth_printer);
        enableToshibaPrinterTextView = findViewById(R.id.enable_toshiba_printer_textview);

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

        printerOptionsTitle.setTypeface(rubiklight);
        printAllScanTitle.setTypeface(rubiklight);
        printAccessCardTitle.setTypeface(rubiklight);
        printQRCodeTitle.setTypeface(rubiklight);
        printWaveUsersTitle.setTypeface(rubiklight);
        printHighTemperatureTitle.setTypeface(rubiklight);

        titleLabelOptions.setTypeface(rubiklight);
        printLabelFaceTitle.setTypeface(rubiklight);
        printLabelNameTitle.setTypeface(rubiklight);
        printLabelNormalTemp.setTypeface(rubiklight);
        printLabelHighTemp.setTypeface(rubiklight);
        printLabelWaveAnswers.setTypeface(rubiklight);

        editTextNameLabel.setText(sp.getString(GlobalParameters.PRINT_LABEL_WAVE_EDIT_NAME, "Screened"));

        String printerSettings = "<a style='text-decoration:underline' href='http://www.sample.com'>Settings</a>";
        if (Build.VERSION.SDK_INT >= 24) {
            brotherBluetoothPrinterConnection.setText(Html.fromHtml(printerSettings, Html.FROM_HTML_MODE_LEGACY));
        } else {
            brotherBluetoothPrinterConnection.setText(Html.fromHtml(printerSettings));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPreferences != null) {
            if (!sharedPreferences.getString("printer", "NONE").equals("NONE")) {
                brotherBluetoothPrinterStatus.setTextColor(getResources().getColor(R.color.green));
                brotherPrintButton.setBackgroundColor(getResources().getColor(R.color.colorBgBlue));
            } else {
                brotherBluetoothPrinterStatus.setTextColor(getResources().getColor(R.color.colorRed));
                brotherPrintButton.setBackgroundColor(getResources().getColor(R.color.colorGray));
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
        if (sp.getBoolean(GlobalParameters.BROTHER_BLUETOOTH_PRINTER, false))
            radioEnableBrotherPrinter.setChecked(true);
        else radioDisableBrotherPrinter.setChecked(true);

        radioGroupPrinter.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radio_yes_bother_printer) {
                    Util.writeBoolean(sp, GlobalParameters.BROTHER_BLUETOOTH_PRINTER, true);
                    radioDisableToshibaPrinter.setChecked(true);
                } else Util.writeBoolean(sp, GlobalParameters.BROTHER_BLUETOOTH_PRINTER, false);
            }
        });
    }

    private void initToshibaPrinter() {
        toshibaPrinterCheck();
        try {
            printerList(this.getApplicationContext());
            portList(this.getApplicationContext());
        } catch (Exception e) {
            Log.e(TAG, "Exception in initializing Toshiba Printer " + e.getMessage());
        }
    }

    private void toshibaPrinterCheck() {
        if (sp.getBoolean(GlobalParameters.TOSHIBA_USB_PRINTER, false))
            radioEnableToshibaPrinter.setChecked(true);
        else radioDisableToshibaPrinter.setChecked(true);

        radioGroupToshibaPrinter.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radio_yes_toshiba_printer) {
                    Util.writeBoolean(sp, GlobalParameters.TOSHIBA_USB_PRINTER, true);
                    radioDisableBrotherPrinter.setChecked(true);
                } else Util.writeBoolean(sp, GlobalParameters.TOSHIBA_USB_PRINTER, false);
            }
        });
    }

    private void printerOptionCheck() {
        //All Scan
        if (sp.getBoolean(GlobalParameters.PRINT_ALL_SCAN, false))
            radioButtonYesPrintAllScans.setChecked(true);
        else radioButtonNoPrintAllScans.setChecked(true);

        radioGroupPrintAllScan.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radio_yes_print_all_scan) {
                    Util.writeBoolean(sp, GlobalParameters.PRINT_ALL_SCAN, true);
                } else Util.writeBoolean(sp, GlobalParameters.PRINT_ALL_SCAN, false);
            }
        });

        // Access card users
        if (sp.getBoolean(GlobalParameters.PRINT_ACCESS_CARD_USERS, false))
            radioButtonYesPrintAccessCard.setChecked(true);
        else radioButtonNoPrintAccessCard.setChecked(true);

        radioGroupPrintAccessCard.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radio_yes_print_access_card) {
                    Util.writeBoolean(sp, GlobalParameters.PRINT_ACCESS_CARD_USERS, true);
                } else Util.writeBoolean(sp, GlobalParameters.PRINT_ACCESS_CARD_USERS, false);
            }
        });

        //QrCode

        if (sp.getBoolean(GlobalParameters.PRINT_QR_CODE_USERS, false))
            radioButtonYesPrintQRCode.setChecked(true);
        else radioButtonNoPrintQRCode.setChecked(true);

        radioGroupPrintQRCode.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radio_yes_print_qr_code) {
                    Util.writeBoolean(sp, GlobalParameters.PRINT_QR_CODE_USERS, true);
                } else Util.writeBoolean(sp, GlobalParameters.PRINT_QR_CODE_USERS, false);
            }
        });

        //Wave users
        if (sp.getBoolean(GlobalParameters.PRINT_WAVE_USERS, false))
            radioButtonYesPrintWave.setChecked(true);
        else radioButtonNoPrintWave.setChecked(true);

        radioGroupPrintWave.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radio_yes_print_wave_users) {
                    Util.writeBoolean(sp, GlobalParameters.PRINT_WAVE_USERS, true);
                } else Util.writeBoolean(sp, GlobalParameters.PRINT_WAVE_USERS, false);
            }
        });

        //High Temperature
        if (sp.getBoolean(GlobalParameters.PRINT_HIGH_TEMPERATURE, false))
            radioButtonYesPrintHighTemperature.setChecked(true);
        else radioButtonNoPrintHighTemperature.setChecked(true);

        radioGroupPrintHighTemperature.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radio_yes_print_high_temperature) {
                    Util.writeBoolean(sp, GlobalParameters.PRINT_HIGH_TEMPERATURE, true);
                } else Util.writeBoolean(sp, GlobalParameters.PRINT_HIGH_TEMPERATURE, false);
            }
        });

    }

    private void printerLabelOptionsCheck() {
        //Print Label Face
        if (sp.getBoolean(GlobalParameters.PRINT_LABEL_FACE, false))
            radioYesPrintLabelFace.setChecked(true);
        else radioNoPrintLabelFace.setChecked(true);

        radioGroupPrintLabelFace.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radio_yes_print_label_face) {
                Util.writeBoolean(sp, GlobalParameters.PRINT_LABEL_FACE, true);
            } else Util.writeBoolean(sp, GlobalParameters.PRINT_LABEL_FACE, false);
        });

        // Print Label Name
        if (sp.getBoolean(GlobalParameters.PRINT_LABEL_NAME, false))
            radioYesPrintLabelName.setChecked(true);
        else radioNoPrintLabelName.setChecked(true);

        radioGroupPrintLabelName.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radio_yes_print_label_name) {
                Util.writeBoolean(sp, GlobalParameters.PRINT_LABEL_NAME, true);
            } else Util.writeBoolean(sp, GlobalParameters.PRINT_LABEL_NAME, false);
        });

        // Print Normal Temperature
        if (sp.getBoolean(GlobalParameters.PRINT_LABEL_NORMAL_TEMPERATURE, false))
            radioYesPrintLabelNormalTemp.setChecked(true);
        else radioNoPrintLabelNormalTemp.setChecked(true);

        radioGroupPrintLabelNormalTemp.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radio_yes_print_label_normal_temp) {
                Util.writeBoolean(sp, GlobalParameters.PRINT_LABEL_NORMAL_TEMPERATURE, true);
            } else
                Util.writeBoolean(sp, GlobalParameters.PRINT_LABEL_NORMAL_TEMPERATURE, false);
        });

        // Print High Temperature
        if (sp.getBoolean(GlobalParameters.PRINT_LABEL_HIGH_TEMPERATURE, false))
            radioYesPrintLabelHighTemp.setChecked(true);
        else radioNoPrintLabelHighTemp.setChecked(true);

        radioGroupPrintLabelHighTemp.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radio_yes_print_label_high_temp) {
                Util.writeBoolean(sp, GlobalParameters.PRINT_LABEL_HIGH_TEMPERATURE, true);
            } else
                Util.writeBoolean(sp, GlobalParameters.PRINT_LABEL_HIGH_TEMPERATURE, false);
        });

        //Print Wave Answers
        if (sp.getBoolean(GlobalParameters.PRINT_LABEL_WAVE_ANSWERS, false))
            radioYesPrintLabelWaveAnswers.setChecked(true);
        else radioNoPrintLabelWaveAnswers.setChecked(true);

        radioGroupPrintLabelWaveAnswers.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radio_yes_print_label_wave_answers) {
                Util.writeBoolean(sp, GlobalParameters.PRINT_LABEL_WAVE_ANSWERS, true);
            } else
                Util.writeBoolean(sp, GlobalParameters.PRINT_LABEL_WAVE_ANSWERS, false);
        });
    }

    public void selectBluetoothPrinter(View view) {
        startActivity(new Intent(this, PrinterWifiBTSettingsActivity.class));
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
        PrinterController.getInstance().setPrinting(false);
    }

    @Override
    public void onPrintError() {
        PrinterController.getInstance().setPrinting(false);
    }

    @Override
    public void onPrintUsbCommand() {
        runOnUiThread(() -> new PrintExecuteTask(this,
                PrinterController.getInstance().getUsbPrintControl(), this)
                .execute(PrinterController.getInstance().getPrintData()));
    }

    @Override
    public void onPrintUsbSuccess(String status, long resultCode) {
        PrinterController.getInstance().setPrinting(false);
    }

    public void saveAudioSettings(View view) {
        if (!editTextNameLabel.getText().toString().isEmpty())
            Util.writeString(sp, GlobalParameters.PRINT_LABEL_WAVE_EDIT_NAME, editTextNameLabel.getText().toString());

        Util.showToast(PrinterSettingsActivity.this, getString(R.string.save_success));
        finish();
    }

    // TOSHIBA PRINTER
    public void selectToshibaBluetoothPrinter(View view) {
        startActivity(new Intent(this, PrinterUsbSettingsActivity.class));
    }

    private void printerList(Context context) {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_single_choice);
        String orginalPrinterType = util.getPreferences(this, PRINTER_TYPE_KEYNAME);
        int position = 0;
        int selectPosition = 13;
        for (int i = 0; i < PRINTER_LIST.length; i++) {
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

    public void onClickButtonPrint(View view) {
        new Thread(() -> {
            if (AppSettings.isPrintUsbEnabled()) {
                String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
                String date = new SimpleDateFormat("MM/dd/yy", Locale.getDefault()).format(new Date());
                String dateTime = date + " " + currentTime;
                PrinterController.getInstance().setPrintData("Name:", "Test", dateTime, "Thermal Scan", false);
                PrinterController.getInstance().printUsb();
            }
        }).start();
    }

    @Override
    public void onPrintStatus(String status, int code) {
        Log.d(TAG, "Print Status " + status);
        PrinterController.getInstance().setPrinting(false);
    }
}