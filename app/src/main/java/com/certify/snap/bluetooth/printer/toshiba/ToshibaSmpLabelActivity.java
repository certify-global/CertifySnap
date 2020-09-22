package com.certify.snap.bluetooth.printer.toshiba;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.certify.snap.R;

import java.util.HashMap;

import jp.co.toshibatec.bcp.library.BCPControl;
import jp.co.toshibatec.bcp.library.LongRef;

import static com.certify.snap.bluetooth.printer.toshiba.Defines.AsynchronousMode;
import static com.certify.snap.bluetooth.printer.toshiba.Defines.PRINTER_TYPE_KEYNAME;
import static com.certify.snap.bluetooth.printer.toshiba.Defines.SynchronousMode;

public class ToshibaSmpLabelActivity extends AppCompatActivity implements BCPControl.LIBBcpControlCallBack {

    private BCPControl m_bcpControl = null;
    private ConnectionData mConnectData = new ConnectionData();
    private int mCurrentIssueMode = AsynchronousMode;
    private PrintData m_LabelData = new PrintData();

    private ConnectionDelegate mConnectionDelegate = null;
    private PrintDialogDelegate mPrintDialogDelegate = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_toshiba_smp_label);

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

        String codeData = util.getLavelDataForEditText( this , R.id.EditTextCode, "21052355");
        if( codeData.length() > 8 ) {
            labelItemList.put( getString(R.string.productCodeName),  codeData.substring(0, 7));
            labelItemList.put( getString(R.string.barCode),  codeData.substring(0, 7));

        } else {
            labelItemList.put( getString(R.string.productCodeData),  codeData );
            labelItemList.put( getString(R.string.barCode),  codeData );
        }
        m_LabelData.setObjectDataList( labelItemList );

        String filePathName = Environment.getDataDirectory().getPath() + "/data/" + this.getPackageName() + "/" + "tempLabel.lfm";
        m_LabelData.setLfmFileFullPath( filePathName );

        m_LabelData.setObjectDataList( labelItemList );

        new PrintExecuteTask( this , m_bcpControl ).execute( m_LabelData );
    }

}
