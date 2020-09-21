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

import jp.co.toshibatec.bcp.library.BCPControl;

import static com.certify.snap.bluetooth.printer.toshiba.Defines.OPTION_CONTROLCODE_KEYNAME;
import static com.certify.snap.bluetooth.printer.toshiba.Defines.OPTION_GRAPHICTYPE_KEYNAME;
import static com.certify.snap.bluetooth.printer.toshiba.Defines.OPTION_LANGUAGE_KEYNAME;
import static com.certify.snap.bluetooth.printer.toshiba.Defines.OPTION_PRINTMODE_KEYNAME;
import static com.certify.snap.bluetooth.printer.toshiba.Defines.OPTION_RECVTIME_DEFALUTVALUE;
import static com.certify.snap.bluetooth.printer.toshiba.Defines.OPTION_RECVTIME_KEYNAME;
import static com.certify.snap.bluetooth.printer.toshiba.Defines.PRINTER_LIST;
import static com.certify.snap.bluetooth.printer.toshiba.Defines.PRINTER_TYPE_KEYNAME;

public class OptionActivity extends AppCompatActivity {

    private BCPControl m_bcpControl = null;
    private String mPrintMode = "";
    private String mControlCode = "";
    private String mLanguage = "";
    private String mGraphic = "";
    String myMemotyPath ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        myMemotyPath = Environment.getDataDirectory().getPath()
                + "/data/" + this.getPackageName();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_option);
        if( null == m_bcpControl ) {
            String portSetting = util.getPortSetting( this );
            if( portSetting.length() == 0 ) {
                return;
            }

            m_bcpControl = new BCPControl( null );

            String mSystemPath = Environment.getDataDirectory().getPath() + "/data/" + this.getPackageName();
            m_bcpControl.setSystemPath( mSystemPath );
            m_bcpControl.setPortSetting( portSetting );

            /** プロパティ設定 */
            util.SetPropaty(this, m_bcpControl);

            String printerType = util.getPreferences( this , PRINTER_TYPE_KEYNAME );
            int usePrinter = Defines.getPrinterNo(printerType);
            m_bcpControl.setUsePrinter(usePrinter);

            String receiveTime = util.getPreferences(this , OPTION_RECVTIME_KEYNAME );
            if( receiveTime.length() == 0 ) {
                receiveTime = String.valueOf(OPTION_RECVTIME_DEFALUTVALUE);
            }
            ((EditText)this.findViewById( R.id.ReceiveTimeoutEditText ) ).setText( receiveTime );
            /** spinnerコントロールの初期化処理 */
            initializeSpinner();

            String strPrinterType = util.getPreferences(this, PRINTER_TYPE_KEYNAME);
            if (strPrinterType != null && strPrinterType.length() != 0 &&
                    (PRINTER_LIST[4].compareTo(strPrinterType) == 0 || PRINTER_LIST[5].compareTo(strPrinterType) == 0)) {
                TextView txtPrintMode = (TextView)this.findViewById( R.id.txtPrintMode );
                Spinner spPrintMode = (Spinner)this.findViewById( R.id.spinnerPrintMode );
                Button btnChange = (Button)this.findViewById(R.id.BttonChange);

                txtPrintMode.setVisibility(View.INVISIBLE);
                spPrintMode.setVisibility(View.INVISIBLE);
                btnChange.setVisibility(View.INVISIBLE);
            }else{
                Spinner spPrintMode = (Spinner)this.findViewById( R.id.spinnerPrintMode );

                GetPrintModeExecuteTask task = new GetPrintModeExecuteTask( this , spPrintMode , m_bcpControl);

                // task.execute(null);
                task.execute((Integer)null);
            }
        }

    }
    /**
     * spinnerコントロールの初期化処理
     */
    private void initializeSpinner() {

        // Spinner の選択されているアイテムを設定
        final Context con = this;


        String strPrinterType = util.getPreferences(this, PRINTER_TYPE_KEYNAME);
        if (strPrinterType != null && strPrinterType.length() != 0 &&
                (PRINTER_LIST[4].compareTo(strPrinterType) == 0 || PRINTER_LIST[5].compareTo(strPrinterType) == 0 ||
                        PRINTER_LIST[6].compareTo(strPrinterType) == 0 || PRINTER_LIST[7].compareTo(strPrinterType) == 0)) {
            // B-EV4/B-LV4シリーズPrintMode使用不可
        }else{
            /** PrintMode Spinner の初期化 */
            initializePrintModeSinner( con );
        }
        /** ControlCode Spinner の初期化 */
        initializeControlCodeSpinner( con );
        /** Language Spinner の初期化 */
        initializeLanguageSpinner( con );
        /** GraphicType Spinner の初期化 */
        initializeGraphicTypeSpinner( con );
    }
    /**
     * GraphicType Spinner の初期化
     * @param con
     */
    private void initializeGraphicTypeSpinner(final Context con) {
        Spinner spGraphicType = (Spinner)this.findViewById( R.id.spinnerGraphicType );
        int default_graphicTypeItem = 1;

        String graphictype = util.getPreferences(con, OPTION_GRAPHICTYPE_KEYNAME );
        if( graphictype.equals("Nibble") ) {
            default_graphicTypeItem = 0;
        } else if( graphictype.equals("Hex") ) {
            default_graphicTypeItem = 1;
        } else if( graphictype.equals("Topix") ) {
            default_graphicTypeItem = 2;
        } else {
            default_graphicTypeItem = 1;
        }

        spGraphicType.setSelection(default_graphicTypeItem);

        spGraphicType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            /**
             *
             */
            public void onItemSelected(AdapterView<?> parent, View view,  int pos, long id) {
                Spinner spinner = (Spinner)parent;
                mGraphic = (String)spinner.getSelectedItem();

            }

            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO

            }
        });
    }
    /**
     * Language Spinner の初期化
     * @param con
     */
    private void initializeLanguageSpinner(final Context con) {
        Spinner spLanguage = (Spinner)this.findViewById( R.id.spinnerLanguage );
        int default_languageItem = 1;

        String languageItem = util.getPreferences(con, OPTION_LANGUAGE_KEYNAME );

        if( languageItem.equals("Japanese") ) {
            default_languageItem = 0;
        } else if ( languageItem.equals("English") ) {
            default_languageItem = 1;
        } else {
            default_languageItem = 1;
        }
        spLanguage.setSelection(default_languageItem);
        spLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            /**
             *
             */
            public void onItemSelected(AdapterView<?> parent, View view,  int pos, long id) {
                Spinner spinner = (Spinner)parent;
                mLanguage = (String)spinner.getSelectedItem();
            }

            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO

            }
        });
    }

    /**
     * PrintMode Spinner の初期化
     * @param con
     */
    private void initializePrintModeSinner(final Context con) {
        Spinner spPrintMode = (Spinner)this.findViewById( R.id.spinnerPrintMode );
        int value = 0 ;
        String printMode = util.getPreferences(con, OPTION_PRINTMODE_KEYNAME );
        if(printMode.equals("0x30: LABEL"))
        {
            util.setPreferences(this, OPTION_PRINTMODE_KEYNAME, printMode );
            value = 0;
        }
        else if(printMode.equals("0x31: RECEIPT"))
        {
            util.setPreferences(this, OPTION_PRINTMODE_KEYNAME, printMode );
            value = 1;
        }
        else if(printMode.equals("0x32: RECEIPT1"))
        {
            util.setPreferences(this, OPTION_PRINTMODE_KEYNAME, printMode );
            value = 2;
        }
        else if(printMode.equals("0x34: ESC/POS"))
        {
            util.setPreferences(this, OPTION_PRINTMODE_KEYNAME, printMode );
            value = 3;
        }
        else if(printMode.equals("0x41: TPCL"))
        {
            util.setPreferences(this, OPTION_PRINTMODE_KEYNAME, printMode );
            value = 4;
        }
        else if(printMode.equals("0x42: TPCL1"))
        {
            util.setPreferences(this, OPTION_PRINTMODE_KEYNAME, printMode );
            value = 5;
        }
        else if(printMode.equals("0x43: C Mode"))							//Add C Mode to the PrintMode Spinner
        {
            util.setPreferences(this, OPTION_PRINTMODE_KEYNAME, printMode );
            value = 6;
        }
        else if(printMode.equals("0x44: Z Mode"))							//Add Z Mode to the PrintMode Spinner
        {
            util.setPreferences(this, OPTION_PRINTMODE_KEYNAME, printMode );
            value = 7;
        }


        /** PrintModeは、ポータブルプリンタに問い合わせる必要があるので、ここではspinnerの初期設定のみを実施 */
        spPrintMode.setSelection(value);

        spPrintMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            /**
             *
             */
            public void onItemSelected(AdapterView<?> parent, View view,  int pos, long id) {
                Spinner spinner = (Spinner)parent;
                mPrintMode = (String)spinner.getSelectedItem();
            }

            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO

            }
        });
    }
    /**
     * ControlCode Spinner の初期化
     * @param con
     */
    private void initializeControlCodeSpinner(final Context con) {
        int value;
        Spinner spControlCode = (Spinner)this.findViewById( R.id.spinnerControlCode );

        String controlCode = util.getPreferences(con, OPTION_CONTROLCODE_KEYNAME );
        if( controlCode.equals("ESC,LF,NULL") ) {
            value = 0;
        } else if( controlCode.equals("{,|,}") ) {
            value = 1;
        } else {
            value = 0;
        }
        spControlCode.setSelection( value );

        spControlCode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            /**
             *
             */
            public void onItemSelected(AdapterView<?> parent, View view,  int pos, long id) {
                Spinner spinner = (Spinner)parent;
                mControlCode = (String)spinner.getSelectedItem();

            }

            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO

            }
        });
    }
    @Override
    protected void onResume(){
        super.onResume();
    }
    /**
     *
     */
    public void onPause(){
        super.onPause();
    }
    /**
     *
     * @param event
     */
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction()==KeyEvent.ACTION_DOWN ) {
            if( event.getKeyCode() == KeyEvent.KEYCODE_BACK ) {
                confirmActivityFinish();
                return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }


    /**
     *  [Change]ボタン処理
     * @param view
     */
    public void onClickButtonChange(View view){
        util.setPreferences(this, OPTION_PRINTMODE_KEYNAME, mPrintMode );
        String portSetting = util.getPortSetting( view.getContext() );
        if( portSetting.length() == 0 ) {
            return;
        }
        //
        ChangePrintModeExecuteTask task = new ChangePrintModeExecuteTask(this , m_bcpControl );
        task.execute( mPrintMode );
    }
    /**
     *  [保存]ボタン処理
     * @param view
     */
    public void onClickButtonSave(View view){
        String receiveTime = ((EditText)this.findViewById( R.id.ReceiveTimeoutEditText ) ).getText().toString();

        if( receiveTime.length() == 0 ) {
            util.showAlertDialog(this, this.getString(R.string.notEnteredReceiveTime));
            return;
        }
        int receiveTimeValue = Integer.parseInt( receiveTime );
        if( receiveTimeValue < 0 || receiveTimeValue > 9999 ) {
            util.showAlertDialog(this,this.getString(R.string.receiveTimeOutOfrange));
            return;
        }
        /** */
        util.setPreferences(this, OPTION_RECVTIME_KEYNAME, receiveTime );
        /** ControlCode */
        util.setPreferences(this, OPTION_CONTROLCODE_KEYNAME, mControlCode );
        /** Language */
        util.setPreferences(this, OPTION_LANGUAGE_KEYNAME, mLanguage );
        /** GraphicType */
        util.setPreferences(this, OPTION_GRAPHICTYPE_KEYNAME, mGraphic );

        util.setPreferences(this, OPTION_PRINTMODE_KEYNAME, mPrintMode );

        /** Priferendceに保存したデータをBCPControlのプロパティに設定  */
        util.SetPropaty(this, m_bcpControl );

        util.showAlertDialog(this, this.getString(R.string.saveComlete));

    }
    /**
     * [前画面に戻る]ボタン処理
     *      * @param view
     */
    public void onClickButtonReturn(View view) {
        confirmActivityFinish();
    }
    /**
     * 確認メッセージ表示
     */
    protected void confirmActivityFinish() {
        util.comfirmDialog( this , this.getString(R.string.confirmBack),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // アプリケーションを終了させる
                        finish();
                    }
                }
                , new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // いいえを押した場合なので何もしないでDialogを閉じる

                    }
                } );

    }
}