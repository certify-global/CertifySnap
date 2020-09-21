package com.certify.snap.bluetooth.printer.toshiba;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.widget.Spinner;

import com.certify.snap.R;

import jp.co.toshibatec.bcp.library.BCPControl;
import jp.co.toshibatec.bcp.library.LongRef;
import jp.co.toshibatec.bcp.library.StringRef;

/**
 * PrintMode取得スレッドクラス
 * @author 
 * 
 */
public class GetPrintModeExecuteTask extends AsyncTask<Integer, Void, String  >{

	BCPControl m_bcpObj = null;
	Activity    mContext = null;
	ProgressDialog mProgressDlg = null;
	private Spinner mSpPrintMode = null;
	boolean mIsOpen = false;
	StringRef mPrintModeName = new StringRef("");
	
	
	/**
	 * コンストラクタ
	 * @param conText : Activityのインスタンス
	 * @param spPrintMode : 
	 * @param bcpcontrol : BCPControlのインスタンス
	 */
	public GetPrintModeExecuteTask(Activity conText , Spinner spPrintMode , BCPControl bcpcontrol) {
    	mContext = conText;
    	m_bcpObj = bcpcontrol;
    	mSpPrintMode = spPrintMode;
    	mPrintModeName = new StringRef("");
    }
	/**
	 * 
	 */
    protected void onPreExecute() {
    	mProgressDlg = new ProgressDialog( mContext );
    	// タイトル, 本文を設定
    	mProgressDlg.setTitle( R.string.printMode);
    	mProgressDlg.setMessage(mContext.getString(R.string.wait));
    	// スタイルを設定
    	mProgressDlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		mProgressDlg.setCancelable(true);
		mProgressDlg.show();
    	
    }

    /** バックグラウンドで実行する処理 */
	@Override
	protected String doInBackground(Integer... params) {
		
    	LongRef Result = new LongRef(0);
		StringRef message = new StringRef("");

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
    	if( false == m_bcpObj.GetPrinterMode( 3000,  mPrintModeName, Result) ) {
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
    		m_bcpObj.ClosePort(Result);
    		mIsOpen = false;
    	}
    	if( result.equalsIgnoreCase("Success") ) {
        	/** update spinner position */
        	mSpPrintMode.setSelection( getPrintModeSpinnerPosition( mPrintModeName.getStringValue() ) );
    	}

		util.showAlertDialog( mContext , result );
    	
    	m_bcpObj = null;
    	mContext = null;
    	mProgressDlg = null;
    	mSpPrintMode = null;
    	mPrintModeName = null;
    }

	private int getPrintModeSpinnerPosition(String printModeName ) {
		int spinnerPos = 0;

		if( printModeName.equalsIgnoreCase("LABEL") ) {					//selected value is LABEL from the PrintMode Spinner
            spinnerPos = 0;
		} else if( printModeName.equalsIgnoreCase("RECEIPT") ) {		//selected value is RECEIPT from the PrintMode Spinner
            spinnerPos = 1;
		} else if( printModeName.equalsIgnoreCase("RECEIPT1") ) {		//selected value is RECEIPT1 from the PrintMode Spinner
            spinnerPos = 2;
		} else if( printModeName.equalsIgnoreCase("ESC/POS") ) {		//selected value is ESC/POS from the PrintMode Spinner
            spinnerPos = 3;
		} else if( printModeName.equalsIgnoreCase("TPCL") ) {			//selected value is TPCL from the PrintMode Spinner
            spinnerPos = 4;
		} else if( printModeName.equalsIgnoreCase("TPCL1") ) {			//selected value is TPCL1 from the PrintMode Spinner
            spinnerPos = 5;
		}
		else if(printModeName.equalsIgnoreCase("C Mode")){				//selected value is C Mode from the PrintMode Spinner
			spinnerPos = 6;
		}
		else if(printModeName.equalsIgnoreCase("Z Mode")){				//selected value is Z Mode from the PrintMode Spinner
			spinnerPos = 7;
		}
		else {
            spinnerPos = 4;												// Default selection is TPCL Mode.
		}
        return spinnerPos;
	}    
}
