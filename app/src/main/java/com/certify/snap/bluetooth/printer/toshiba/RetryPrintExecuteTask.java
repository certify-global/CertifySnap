package com.certify.snap.bluetooth.printer.toshiba;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;

import com.certify.snap.R;

import jp.co.toshibatec.bcp.library.BCPControl;
import jp.co.toshibatec.bcp.library.LongRef;
import jp.co.toshibatec.bcp.library.StringRef;

public class RetryPrintExecuteTask extends AsyncTask<PrintData, Void, String  >{

	final String bcpFolderPath = "/TOSHIBATEC/BCP_Print_for_Android";
	
	BCPControl m_bcpObj = null;
	// PrintData  m_printData = null;
	Activity    mContext = null;
	ProgressDialog mProgressDlg = null;
	String mGetStatus = "";
	
	// 復帰モード
	final int AsynchronousMode = 1;	// 送信完了復帰（非同期）
	final int SynchronousMode = 2;		// 発行完了復帰（同期）
	
	
    // コンストラクタ
    public RetryPrintExecuteTask(Activity conText , BCPControl bcpcontrol ) {
    	mContext = conText;
    	m_bcpObj = bcpcontrol;
    	
    }
  
    protected void onPreExecute() {
    	mProgressDlg = new ProgressDialog( mContext );
    	// タイトル, 本文を設定
    	mProgressDlg.setTitle(R.string.runPrint);
    	mProgressDlg.setMessage(mContext.getString(R.string.wait));
    	// mProgressDlg.setIndeterminate(false);        
    	// スタイルを設定
    	mProgressDlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		mProgressDlg.setCancelable(false);
		mProgressDlg.show();
    	
    	
    }
    
    
    // バックグラウンドで実行する処理   
    @Override  
    protected String doInBackground(PrintData... urls) {

    	 PrintData printData = urls[0];
    	
    	long ret = 0;
    	
		mGetStatus = "";

		printData.setResult( 0 );
		printData.setStatusMessage("");
        
    	StringRef printerStatus = new StringRef(" ");
    	LongRef result = new LongRef( 0 );
		// 
		if( false == m_bcpObj.RetryIssue( printerStatus , result ) ) {
			ret = result.getLongValue();
			//
			printData.setResult( ret );

			StringRef message = new StringRef("");
			if( ret == 0x800A044EL ) {
				String errCode = printerStatus.getStringValue().substring(0, 2);
				m_bcpObj.GetMessage( errCode , message );  
			} else {
				if( false == m_bcpObj.GetMessage( ret , message ) ) {
					message.setStringValue( String.format(mContext.getString(R.string.msg_executePrintError) +"= %08x " , ret ) );
				}
				else
				{
					message.setStringValue( String.format(mContext.getString(R.string.msg_executePrintError) +"= %08x " , ret ));
				}
			}
			printData.setStatusMessage( message.getStringValue() );
			
			// リトライ可能エラー有無の確認
			if( m_bcpObj.IsIssueRetryError() ) {
				return mContext.getString(R.string.msg_RetryError);
			} else {
				return mContext.getString(R.string.error);
			}
		}
		
		// 復帰モードが発行完了モードの場合は、
		if( printData.getCurrentIssueMode() ==  SynchronousMode ) {
			// GetStatusメソッドを呼び出してプリンタから受信した文字列と実行結果を取得します。
			mGetStatus = this.getPrinterStatus();
			
		}
		printData.setStatusMessage( mContext.getString(R.string.msg_success) );
        return mContext.getString(R.string.msg_success);
    }
  
    // メインスレッドで実行する処理
    @Override  
    protected void onPostExecute(String result) {
    	mProgressDlg.dismiss();
    	
    	if( result.equals("Success") ) {
    		mContext.showDialog( PrintDialogDelegate.PRINT_COMPLETEMESSAGE_DIALOG );
    	
    	} else if( result.equals("RetryError") ) {
    		mContext.showDialog( PrintDialogDelegate.RETRYERRORMESSAGE_DIALOG );
    	} else {
    		mContext.showDialog( PrintDialogDelegate.ERRORMESSAGE_DIALOG );
    	}
    	mContext = null;
    	m_bcpObj = null;
    }
    
    /**--------------------------------------------------------------------------
     * 
     --------------------------------------------------------------------------*/
    /**
     * 
     * @return
     */
    private String getPrinterStatus( ) {
    	
    	StringRef printerStatus = new StringRef(" ");
    	LongRef result = new LongRef( 0 );
    	
    	if( false == this.m_bcpObj.GetStatus( printerStatus, result  ) ) {
    		
        	return mContext.getString(R.string.msg_GetStatuscallError);
        	
    	} else {
    		
        	return String.format( R.string.msg_GetStatus +"= %s : result = %08x ", printerStatus.getStringValue(), result.getLongValue() );
    	}
    }

	/**
     * 
     * @return
     */
    private long openPort() {

    	LongRef result = new LongRef( 0 );
		int issueMode = 1;	// 1:送信完了モード , 2:発行完了モード
		if( false == m_bcpObj.OpenPort(issueMode, result ) ) {
			return result.getLongValue();
		}
		return 0;
    	
    }
    
}
