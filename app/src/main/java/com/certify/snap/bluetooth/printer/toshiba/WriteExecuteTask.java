package com.certify.snap.bluetooth.printer.toshiba;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;

import com.certify.snap.R;

import jp.co.toshibatec.bcp.library.BCPControl;
import jp.co.toshibatec.bcp.library.LongRef;
import jp.co.toshibatec.bcp.library.StringRef;

public class WriteExecuteTask extends AsyncTask<String, Void, String  >{

	BCPControl m_bcpObj = null;
	// ConnectionData  m_connectData = null;
	Activity    mContext = null;
	ProgressDialog mProgressDlg = null;
    // コンストラクタ
    public WriteExecuteTask(Activity conText , BCPControl bcpcontrol ) {
    	mContext = conText;
    	m_bcpObj = bcpcontrol;
    	
    }
    @Override
    protected void onPreExecute() {
    	mProgressDlg = new ProgressDialog( mContext );
    	// タイトル, 本文を設定
    	mProgressDlg.setTitle( R.string.runWritePort);
    	mProgressDlg.setMessage(mContext.getString(R.string.wait));
    	// スタイルを設定
    	mProgressDlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		mProgressDlg.setCancelable(true);
		mProgressDlg.show();
    	
    }

    // バックグラウンドで実行する処理 
	@Override
	protected String doInBackground(String... params) {
		
		String writeData = params[0];
		
    	LongRef Result = new LongRef( 0 );
    	
    	boolean resultWritePort = m_bcpObj.WritePort( writeData , Result );
        
    	if( false ==  resultWritePort ) {

    		StringRef message = new StringRef("");
    		m_bcpObj.GetMessage( Result.getLongValue() , message );
    		return String.format( mContext.getString(R.string.msg_WritePortError) + "= %s ", message.getStringValue() );
    		
    	}

        return mContext.getString(R.string.msg_success);
    }
	
    // メインスレッドで実行する処理
    @Override  
    protected void onPostExecute(String result) {
    	mProgressDlg.dismiss();
    	
    	util.showAlertDialog( mContext , result );

    }
	
}
