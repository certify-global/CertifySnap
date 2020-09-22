package com.certify.snap.bluetooth.printer.toshiba;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.PaintDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.certify.callback.PrintStatusCallback;
import com.certify.snap.R;

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

public class ToshibaPrinterSettingsActivity extends AppCompatActivity implements BCPControl.LIBBcpControlCallBack, PrintStatusCallback {

    private BCPControl m_bcpControl = null;
    private ConnectionData mConnectData = new ConnectionData();
    private int mCurrentIssueMode = AsynchronousMode;
    private PrintData m_LabelData = new PrintData();

    private ConnectionDelegate mConnectionDelegate = null;
    private PrintDialogDelegate mPrintDialogDelegate = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_toshiba_printer_settings);
        final Context context = this.getApplicationContext();

     /*   try {
            resizeReturnButton();
            printerList(context);
            portList(context);
            copyIniFile();
            initPrint();
        } catch( Exception e ) {
            Log.d("TAG", "onCreate: ");
        }*/

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

    public void testPrint(View view){
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