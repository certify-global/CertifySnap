package com.certify.snap.bluetooth.printer.toshiba;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.widget.EditText;

import com.certify.snap.R;

import jp.co.toshibatec.bcp.library.BCPControl;
import jp.co.toshibatec.bcp.library.LongRef;
import jp.co.toshibatec.bcp.library.StringRef;

public class ReadExecuteTask extends AsyncTask<Void, Void, String  >{

	BCPControl m_bcpObj = null;
	Activity    mContext = null;
	ProgressDialog mProgressDlg = null;
	EditText mReadDataEditText = null;
	String mReceiveData = "";
	
    // コンストラクタ
    public ReadExecuteTask(Activity conText , EditText readDataEditText , BCPControl bcpcontrol ) {
    	mContext = conText;
    	m_bcpObj = bcpcontrol;
		mReadDataEditText = readDataEditText;

    }
    @Override
    protected void onPreExecute() {
    	mProgressDlg = new ProgressDialog( mContext );
    	// タイトル, 本文を設定
    	mProgressDlg.setTitle( R.string.runReadPort);
    	mProgressDlg.setMessage(mContext.getString(R.string.wait));
    	// スタイルを設定
    	mProgressDlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		mProgressDlg.setCancelable(true);
		mProgressDlg.show();
    	
    }

    // バックグラウンドで実行する処理 
	@Override
	protected String doInBackground(Void... params) {
		
    	LongRef Result = new LongRef( 0 );
    	
    	// String Data = "";
    	StringRef receiveData = new StringRef("");
    	
    	
    	
    	int receiveSize = m_bcpObj.ReadPort(receiveData, Result);
    	if( 0 == receiveSize ) {
    		
    		StringRef message = new StringRef("");
     		m_bcpObj.GetMessage( Result.getLongValue() , message );
        	mReceiveData = "";
    		
    		return String.format( mContext.getString(R.string.msg_ReadPortError) +"= %s ", message.getStringValue() );
    		
    	} else {
    		/** 制御コードをはずす */
    		mReceiveData = util.trimControlChars( receiveData.getStringValue() );
    	}
        return mContext.getString(R.string.msg_success);
    }
	
    // メインスレッドで実行する処理
    @Override  
    protected void onPostExecute(String result) {
    	mProgressDlg.dismiss();

    	if( result.equals("Success") ) {
    		mReadDataEditText.setText( mReceiveData );
    	} else {
            mReadDataEditText.setText( "" );
    	}
    	util.showAlertDialog( mContext , result );

    }
	
}
