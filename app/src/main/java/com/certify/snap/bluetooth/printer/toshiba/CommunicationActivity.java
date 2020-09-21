package com.certify.snap.bluetooth.printer.toshiba;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.certify.snap.R;

import java.io.UnsupportedEncodingException;

import jp.co.toshibatec.bcp.library.BCPControl;
import jp.co.toshibatec.bcp.library.LongRef;
import jp.co.toshibatec.bcp.library.StringRef;

import static com.certify.snap.bluetooth.printer.toshiba.Defines.PORTSETTING_PORT_MODE_KEYNAME;
import static com.certify.snap.bluetooth.printer.toshiba.Defines.PRINTER_TYPE_KEYNAME;

public class CommunicationActivity extends AppCompatActivity  implements BCPControl.LIBBcpControlCallBack {

    private static final String OPTION_PRINTMODE_KEYNAME = "OGPM";
    private BCPControl m_bcpControl = null;
    private ConnectionData mConnectData = new ConnectionData();

    private String mIssueMode = "";

    private String mExecuteFunctionName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_communication);
        final Context mCon = this;

        if (null == m_bcpControl) {
            m_bcpControl = new BCPControl(this);
            String mSystemPath = Environment.getDataDirectory().getPath() + "/data/" + this.getPackageName();
            m_bcpControl.setSystemPath(mSystemPath);
        }
        // プロパティ値を設定
        util.SetPropaty(this, m_bcpControl);

        if (mIssueMode.length() == 0) {
            mIssueMode = "Issue";
        }


        // WritePort メソッドで出力するデータのデフォルト値は“{WV|}”です。
        ((TextView) this.findViewById(R.id.WriteDataEditText)).setText("{WV|}");

        ((TextView) this.findViewById(R.id.ReadDataEditText)).setText("");
        ((EditText) this.findViewById(R.id.StatusDataEditText)).setText("");
        Spinner sp = (Spinner) this.findViewById(R.id.spinnerMode);

        sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            /**
             *
             */
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                Spinner spinner = (Spinner) parent;
                mIssueMode = (String) spinner.getSelectedItem();
            }

            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO

            }
        });
        int position = 0;
        if (mIssueMode.equals("Send")) {
            position = 0;
        } else if (mIssueMode.equals("Issue")) {
            position = 1;

        } else {
            position = 0;
            mIssueMode = "Send";
        }
        sp.setSelection(position);


    }

    /**
     *
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (mConnectData.getIsOpen().get() == true) {
            ((Button) this.findViewById(R.id.BttonPortOpen)).setText(R.string.msg_PortClose);
        } else {
            ((Button) this.findViewById(R.id.BttonPortOpen)).setText(R.string.msg_PortOpen);
        }
    }

    /**
     *
     */
    public void onPause() {
        super.onPause();
    }

    /**
     * キーイベントの取得
     *
     * @param event
     */
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                this.confirmActivityFinish();

                return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    /**
     * [Port Open / Port Close]ボタン処理
     *
     * @param view
     */
    public void onClickButtonPortOpen(View view) {
        try {
            if (mConnectData.getIsOpen().get() == true) {// port closeボタンが押された時

                LongRef Result = new LongRef(0);
                if (false == m_bcpControl.ClosePort(Result)) {

                    util.showAlertDialog(this, String.format(R.string.msg_PortCloseError + "= %08x", Result.getLongValue()));
                } else {
                    mConnectData.getIsOpen().set(false);

                    ((Button) this.findViewById(R.id.BttonPortOpen)).setText(R.string.msg_PortOpen);


                }
            } else {                            // port openボタンが押された時

                portOpen();

            }

        } catch (Exception ex) {

        }

    }

    /**
     * Port Open
     */
    private void portOpen() {
        String portSetting = "";

        String portMode = util.getPreferences(this, PORTSETTING_PORT_MODE_KEYNAME);

        portSetting = util.getPortSetting(this);
        if (portSetting.length() == 0) {
            return;
        }
        /** 送信完了復帰モードの場合 */
        if (mIssueMode.equalsIgnoreCase("Send")) {
            mConnectData.setIssueMode(1);
        } else { /** 発行完了復帰モードの場合 */
            mConnectData.setIssueMode(2);
        }
        /** 接続文字列の設定 */
        mConnectData.setPortSetting(portSetting);

        // プリンタNo設定
        String printerType = util.getPreferences(this, PRINTER_TYPE_KEYNAME);
        int usePrinter = Defines.getPrinterNo(printerType);
        m_bcpControl.setUsePrinter(usePrinter);

        ConnectExecuteTask task = new ConnectExecuteTask(this, ((Button) this.findViewById(R.id.BttonPortOpen)), m_bcpControl);
        task.execute(mConnectData);


    }

    /**
     * [Write Data]ボタン処理
     *
     * @param view
     */
    public void onClickButtonWrite(View view) {

        if (mConnectData.getIsOpen().get() == false) {
            util.showAlertDialog(this, getString(R.string.portOpenFailed));
            return;
        }
        ((EditText) this.findViewById(R.id.StatusDataEditText)).setText("");

        String writeData = ((EditText) this.findViewById(R.id.WriteDataEditText)).getText().toString();
        if (writeData.length() == 0) {
            util.showAlertDialog(this, this.getString(R.string.noWriteTargetData));
            return;
        }

        WriteExecuteTask task = new WriteExecuteTask(this, m_bcpControl);
        task.execute(writeData);

    }

    /**
     * [Read Data]ボタン処理
     *
     * @param view
     */
    public void onClickButtonRead(View view) {

        if (mConnectData.getIsOpen().get() == false) {
            //util.showAlertDialog(this, getString(R.string.portOpenFailed ) );

            return;
        }
        ((EditText) this.findViewById(R.id.ReadDataEditText)).setText("");
        ReadExecuteTask task = new ReadExecuteTask(this, ((EditText) this.findViewById(R.id.ReadDataEditText)), m_bcpControl);
        task.execute();

    }

    /**
     * @param view
     */
    public void onClickButtonStatus(View view) {

        if (mConnectData.getIsOpen().get() == false) {
            util.showAlertDialog(this, this.getString(R.string.portOpenFailed));
            return;
        }
        /** 発行完了復帰モード以外の場合は、実行不可とする */
        if (mIssueMode.equalsIgnoreCase("Issue") == false) {
            util.showAlertDialog(this, this.getString(R.string.openPortInIssueMode));
            return;
        }
        ((EditText) this.findViewById(R.id.StatusDataEditText)).setText("");

    /*	WriteExecuteTask task = new WriteExecuteTask(this , m_bcpControl );
    	task.execute( "{WS|}" );*/
        StringRef PrinterStatus = new StringRef("");
        LongRef Result = new LongRef(0);

        String printMode = util.getPreferences(this, OPTION_PRINTMODE_KEYNAME);

        if (printMode.equals("0x30: LABEL") || printMode.equals("0x41: TPCL") || printMode.equals("0x42: TPCL1") || printMode.equals("0x31: RECEIPT") || printMode.equals("0x32: RECEIPT1")) {
            m_bcpControl.WritePort("{WS|}", Result);
        } else {
            //Amir
            byte[] escPOSStatusCommand = {'{', 'W', 'S', '|', '}'};
			/*
    		byte[] controlCode = { 0x1b, 0x0a, 0x00 };
    		String status = "WS";
    		byte[] statusString = status.getBytes();

    		byte[] escPOSStatusCommand = new byte[controlCode.length + 2];
    		escPOSStatusCommand[0] =  controlCode[0];
    		escPOSStatusCommand[1] =  statusString[0];
    		escPOSStatusCommand[2] =  statusString[1];
    		escPOSStatusCommand[3] =  controlCode[1];
    		escPOSStatusCommand[4] =  controlCode[2];
    		*/
            String str1 = null;
            try {
                str1 = new String(escPOSStatusCommand, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            m_bcpControl.WritePort(str1, Result);
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (m_bcpControl.ReadStatus(PrinterStatus, Result)) {
            ((EditText) this.findViewById(R.id.StatusDataEditText)).setText(PrinterStatus.getStringValue());
        } else {
            StringRef message = new StringRef("");
            if (m_bcpControl.GetMessage(Result.getLongValue(), message)) {
                util.showAlertDialog(view.getContext(), String.format(R.string.msg_ReadStatusError + "= %s ", message.getStringValue()));
            } else {
                util.showAlertDialog(view.getContext(), String.format(R.string.msg_ReadStatusError + "= %d ", Result.getLongValue()));
            }
        }
    }

    /**
     * [前画面に戻る]ボタン処理
     *
     * @param view
     */
    public void onClickButtonReturn(View view) {
        this.confirmActivityFinish();
    }

    /**
     * 終了確認
     */
    protected void confirmActivityFinish() {
        final Context con = this;
        util.comfirmDialog(this, this.getString(R.string.confirmBack),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // アプリケーションを終了させる
                        if (mConnectData.getIsOpen().get() == true) {


                            LongRef Result = new LongRef(0);
                            m_bcpControl.ClosePort(Result);
                        }
                        finish();
                    }
                }
                , new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // いいえを押した場合なので何もしないでDialogを閉じる

                    }
                });

    }


    @Override
    public void BcpControl_OnStatus(String statusMessage, long errorCode) {
        // TODO Auto-generated method stub

        StringRef PrinterStatus = new StringRef("");
        LongRef Result = new LongRef(0);
        /** 送信完了復帰モードの場合 */
        if (mIssueMode.equalsIgnoreCase("Send")) {
            ((EditText) this.findViewById(R.id.StatusDataEditText)).setText(statusMessage);
        }


    }
}
