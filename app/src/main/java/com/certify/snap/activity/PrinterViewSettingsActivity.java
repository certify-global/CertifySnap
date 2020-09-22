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
import android.os.Environment;
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
import com.certify.snap.bluetooth.printer.toshiba.ConnectionData;
import com.certify.snap.bluetooth.printer.toshiba.ConnectionDelegate;
import com.certify.snap.bluetooth.printer.toshiba.PrintData;
import com.certify.snap.bluetooth.printer.toshiba.PrintDialogDelegate;
import com.certify.snap.bluetooth.printer.toshiba.PrintExecuteTask;
import com.certify.snap.bluetooth.printer.toshiba.ToshibaPrinterSettingsActivity;
import com.certify.snap.bluetooth.printer.toshiba.util;
import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.Util;
import com.certify.snap.controller.PrinterController;

import java.io.File;
import java.util.HashMap;

import jp.co.toshibatec.bcp.library.BCPControl;
import jp.co.toshibatec.bcp.library.LongRef;

import static com.certify.snap.bluetooth.printer.toshiba.Defines.AsynchronousMode;
import static com.certify.snap.bluetooth.printer.toshiba.Defines.PORTSETTING_FILE_PATH_KEYNAME;
import static com.certify.snap.bluetooth.printer.toshiba.Defines.PORTSETTING_PORT_MODE_KEYNAME;
import static com.certify.snap.bluetooth.printer.toshiba.Defines.PRINTER_LIST;
import static com.certify.snap.bluetooth.printer.toshiba.Defines.PRINTER_TYPE_KEYNAME;
import static com.certify.snap.bluetooth.printer.toshiba.Defines.SynchronousMode;

public class PrinterViewSettingsActivity extends SettingBaseActivity implements PrinterController.PrinterCallbackListener, BCPControl.LIBBcpControlCallBack, PrintStatusCallback {

    TextView titleBrotherBluetoothPrinter, enableBrotherPrinterTextView, brotherBluetoothPrinterConnect, brotherBluetoothPrinterConnection,
            brotherBluetoothPrinterStatus, brotherTestPrint,
            titleToshibaBluetoothPrinter, enableToshibaPrinterTextView, toshibaBluetoothPrinterConnect, toshibaBluetoothPrinterConnection,
            toshibaBluetoothPrinterStatus, toshibaTestPrint;
    ImageView brotherImageView, toshibaImageView;
    Button brotherPrintButton, toshibaPrintButton;
    Typeface rubiklight;
    private SharedPreferences sp;

    private BCPControl m_bcpControl = null;
    private ConnectionData mConnectData = new ConnectionData();
    private int mCurrentIssueMode = AsynchronousMode;
    private PrintData m_LabelData = new PrintData();

    private ConnectionDelegate mConnectionDelegate = null;
    private PrintDialogDelegate mPrintDialogDelegate = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_printer_view_settings);
        sp = Util.getSharedPreferences(this);

        final Context context = this.getApplicationContext();

        initView();
        brotherPrinterCheck();
        toshibaPrinterCheck();

        try {
            resizeReturnButton();
            printerList(context);
            portList(context);
            copyIniFile();
            initPrint();
        } catch( Exception e ) {
            Log.d("TAG", "onCreate: ");
        }

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
        //do noop
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

    private boolean copyIniFile() {

        String myMemotyPath = Environment.getDataDirectory().getPath()
                + "/data/" + this.getPackageName();

        File newfile = new File(myMemotyPath);
        if (newfile.exists() == false) {
            if (newfile.mkdirs()) {

            }
        }
        try {

            util.asset2file(this, "ErrMsg0.ini", myMemotyPath, "ErrMsg0.ini");
            util.asset2file(this, "ErrMsg1.ini", myMemotyPath, "ErrMsg1.ini");
            util.asset2file(this, "PRTEP2G.ini", myMemotyPath, "PRTEP2G.ini");
            util.asset2file(this, "PRTEP2GQM.ini", myMemotyPath,
                    "PRTEP2GQM.ini");
            util.asset2file(this, "PRTEP4GQM.ini", myMemotyPath,
                    "PRTEP4GQM.ini");
            util.asset2file(this, "PRTEP4T.ini", myMemotyPath, "PRTEP4T.ini");
            util.asset2file(this, "PRTEV4TT.ini", myMemotyPath, "PRTEV4TT.ini");
            util.asset2file(this, "PRTEV4TG.ini", myMemotyPath, "PRTEV4TG.ini");
            util.asset2file(this, "PRTLV4TT.ini", myMemotyPath, "PRTLV4TT.ini");
            util.asset2file(this, "PRTLV4TG.ini", myMemotyPath, "PRTLV4TG.ini");
            util.asset2file(this, "PRTFP3DGQM.ini", myMemotyPath, "PRTFP3DGQM.ini");
            //ADD 03/12/2018
            util.asset2file(this, "PRTBA400TG.ini", myMemotyPath, "PRTBA400TG.ini");
            util.asset2file(this, "PRTBA400TT.ini", myMemotyPath, "PRTBA400TT.ini");
            util.asset2file(this, "PrtList.ini", myMemotyPath, "PrtList.ini");
            util.asset2file(this, "resource.xml", myMemotyPath, "resource.xml");
            util.asset2file(this, "PRTFP2DG.ini", myMemotyPath, "PRTFP2DG.ini");

            util.asset2file(this, "PRTFV4D.ini", myMemotyPath, "PRTFV4D.ini");

        } catch (Exception e) {

            util.showAlertDialog(this,
                    getString(R.string.msg_CopyPrinterConfigFile));
            return false;
        }

        return true;
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
        callFILE();
    }

    private void initPrint(){
        final String myMemotyPath = Environment.getDataDirectory().getPath() + "/data/" + this.getPackageName();
        try {
            util.asset2file(this, "SmpFV4D.lfm", myMemotyPath, "tempLabel.lfm");
        } catch (Exception e) {
            e.printStackTrace();
        }
        printLabel();
    }

    private void resizeReturnButton() {
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        Button btnReturn = (Button)this.findViewById(R.id.BttonReturn);
        Point size = new Point();
        display.getSize(size);
        btnReturn.setWidth(size.x);
    }

    private void callFILE() {
        String filePath = util.getPreferences(this, PORTSETTING_FILE_PATH_KEYNAME);
        if (filePath.length() == 0) {
            filePath = Environment.getExternalStorageDirectory().getPath() + "/PrintImageFile.txt";
        }
        util.setPreferences(this, PORTSETTING_FILE_PATH_KEYNAME, filePath);
    }

    // Print Label
    private void printLabel(){
        if( m_bcpControl == null ) {

            m_bcpControl = new BCPControl( this );

            util.SetPropaty( this , m_bcpControl );

            String srcData = "";

            String strPrinterType = util.getPreferences(this, PRINTER_TYPE_KEYNAME);
            if (strPrinterType == null || strPrinterType.length() == 0){
                strPrinterType = "B-FV4D";
            }
            loadEditTextItem(srcData, R.id.EditTextName, strPrinterType );
            loadEditTextItem(srcData, R.id.EditTextCode, "21052355" );
            loadEditTextItem(srcData, R.id.EditTextPrintNum, "1" );


            mConnectionDelegate = new ConnectionDelegate();
            mPrintDialogDelegate = new PrintDialogDelegate( this , m_bcpControl, m_LabelData );

            this.openBluetoothPort( SynchronousMode );
        }
    }

    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        dialog = mPrintDialogDelegate.createDialog( id );
        if( null == dialog ) {
            dialog = super.onCreateDialog( id );
        }
        return dialog;
    }

    protected void onPrepareDialog(int id, Dialog dialog) {
        if( false == mPrintDialogDelegate.PrepareDialog(  id, dialog) ) {
            super.onPrepareDialog(id, dialog );
        }
    }

    private void loadEditTextItem(String srcData, int resourceID, String defaultData) {
        if( srcData == null || srcData.compareTo("Default") == 0 || srcData.length() == 0 ) {
            ((EditText)this.findViewById( resourceID )).setText( defaultData );
        } else {
            (( EditText )this.findViewById( resourceID )).setText( srcData );
        }
    }

    public void onClickButtonPrint( View view ) {
        try{
            callPrintThread();
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    private void openBluetoothPort( int issueMode ) {

        if( mConnectData.getIsOpen().get() == false ){
            mConnectionDelegate.openPort(this ,  m_bcpControl , mConnectData , issueMode );
            this.mCurrentIssueMode = issueMode;
        }
    }

    @Override
    public void BcpControl_OnStatus(String PrinterStatus, long Result) {
        // TODO Auto-generated method stub
        String strMessage = String.format(getString(R.string.statusReception) + " %s : %08x ", PrinterStatus , Result );
        util.showAlertDialog(this, strMessage );
    }

    private void callPrintThread() {

        LongRef result = new LongRef( 0 );

        m_LabelData.setCurrentIssueMode( this.mCurrentIssueMode );
        int printCount = Integer.parseInt( util.getLavelDataForEditText( this , R.id.EditTextPrintNum, "1" ) );
        //
        if( printCount < 0 || 10 < printCount ) {
            printCount = 1;
        }
        m_LabelData.setPrintCount( printCount );
        HashMap<String , String> labelItemList = new HashMap<String , String>();

        String hinName = util.getLavelDataForEditText( this , R.id.EditTextName, "B-FV4D");
        if( hinName.length() > 10 ){
            labelItemList.put( getString(R.string.dataName) ,  hinName.substring(0, 9) );
        } else {
            labelItemList.put( getString(R.string.dataName) ,  hinName );
        }

        m_LabelData.setObjectDataList( labelItemList );

        String filePathName = Environment.getDataDirectory().getPath() + "/data/" + this.getPackageName() + "/" + "tempLabel.lfm";
        m_LabelData.setLfmFileFullPath( filePathName );

        m_LabelData.setObjectDataList( labelItemList );

        new PrintExecuteTask( this , m_bcpControl, this).execute( m_LabelData );
    }

    @Override
    public void onPrintStatus(String status, int code) {
        //do noop
    }
}