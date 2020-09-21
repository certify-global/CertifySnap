package com.certify.snap.bluetooth.printer.toshiba;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;

import com.certify.snap.R;

import jp.co.toshibatec.bcp.library.BCPControl;
import jp.co.toshibatec.bcp.library.LongRef;
import jp.co.toshibatec.bcp.library.StringRef;

/**
 * PrintMode取得スレッドクラス
 * @author 
 * 
 */
public class ChangePrintModeExecuteTask extends AsyncTask<String, Void, String  >{

	BCPControl m_bcpObj = null;

	Activity    mContext = null;
	ProgressDialog mProgressDlg = null;
	boolean	mIsOpen = false;
	
	
    // コンストラクタ
    public ChangePrintModeExecuteTask(Activity conText , BCPControl bcpcontrol ) {
    	mContext = conText;
    	m_bcpObj = bcpcontrol;
    }
	
    protected void onPreExecute() {
    	mProgressDlg = new ProgressDialog( mContext );
    	// タイトル, 本文を設定
    	mProgressDlg.setTitle( R.string.printerMode);
    	mProgressDlg.setMessage(mContext.getString(R.string.wait));
    	// スタイルを設定
    	mProgressDlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		mProgressDlg.setCancelable(true);
		mProgressDlg.show();
    	
    	
    }

    /** バックグラウンドで実行する処理 */
	@Override
	protected String doInBackground(String... params) {
		
    	String printModeName = params[0];
    	LongRef Result = new LongRef(0);
		StringRef message = new StringRef("");
		/** spinnerの選択文字列をprintermode valueに変換 */
		int iPrintMode = getSelectedPrintMode( printModeName );
		
		StringRef printStatus = new StringRef("");
        /** 発行完了復帰モードでオープン */
    	if( false == m_bcpObj.OpenPort( 2 , Result ) ) {
    		if( false == m_bcpObj.GetMessage( Result.getLongValue() , message ) ) {
    			return mContext.getString(R.string.msg_OpenPorterror);
    		}
    		return message.getStringValue();
    	} else {
    		mIsOpen = true;
    	}
    	/** get printer mode name */
    	if( false == m_bcpObj.SetPrintMode( iPrintMode , Result) ) {
    		if( false == m_bcpObj.GetMessage( Result.getLongValue() , message ) ) {
    			return mContext.getString(R.string.msg_PrinterModeerror);
    		}
    		return message.getStringValue();
    	}
        return mContext.getString(R.string.msg_success);
        
        
        
    }
	
    /**
     *  メインスレッドで実行する処理
     * @param result
     */
    @Override  
    protected void onPostExecute(String result) {
    	mProgressDlg.dismiss();
    	if( mIsOpen ) {
        	LongRef Result = new LongRef(0);
    		m_bcpObj.ClosePort( Result );
    		mIsOpen = false;
    	}
    	
    	util.showAlertDialog( mContext , result );

    }
	/**
	 * 
	 * @param printMode
	 * @return
	 */
	private int getSelectedPrintMode(String printModeName) {
		int value;
		if( printModeName.equals("0x30: LABEL") ){
            value = 0x30;
        } else if( printModeName.equals("0x31: RECEIPT") ){
            value = 0x31;
        } else if( printModeName.equals("0x32: RECEIPT1") ){
            value = 0x32;
        } else if( printModeName.equals("0x34: ESC/POS") ){
            value = 0x34;
        } else if( printModeName.equals("0x41: TPCL") ){
            value = 0x41;
        } else if( printModeName.equals("0x42: TPCL1") ){
            value = 0x42;
        } 
	 	else if( printModeName.equals("0x43: C Mode") ){			//CPCL Mode
         value = 0x43;
     	} 
	 	else if( printModeName.equals("0x44: Z Mode") ){			//ZPL Mode
	 		value = 0x44;
	 	} 
        else {
            value = 0x41;
        }
		return value;
	}	
}
