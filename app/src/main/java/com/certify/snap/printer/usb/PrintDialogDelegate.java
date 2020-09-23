package com.certify.snap.printer.usb;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

import com.certify.snap.R;

import jp.co.toshibatec.bcp.library.BCPControl;


public class PrintDialogDelegate {
	public final static int RETRYERRORMESSAGE_DIALOG = 1;		// retry error messsage dialog
	public final static int PRINT_COMPLETEMESSAGE_DIALOG = 2;	// print finish messsage dialog
	public final static int ERRORMESSAGE_DIALOG = 3;				// error message dialog
	public int RETRYERRORMESSAGE_COUNT = 0;

	final Activity mActivity;
	final BCPControl mBcpControl;
	final PrintData mLabelData;
	/**
	 * 
	 * @param activity
	 * @param bcpControl
	 * @param labelData
	 */
	public PrintDialogDelegate(Activity activity , BCPControl bcpControl , PrintData labelData){
		mActivity = activity;
		mBcpControl = bcpControl;
		mLabelData = labelData;
	}

	public Dialog createDialog( int id ) {

        switch(id) {
    	
        case RETRYERRORMESSAGE_DIALOG:	// リトライ確認Dialog
        	return createRetryErrormessageDialog( );    		
    	    
        case PRINT_COMPLETEMESSAGE_DIALOG:
        	return  createPrintCompletemessageDialog( );    		
        	
        case ERRORMESSAGE_DIALOG:
        	return  createErrormessageDialog( );
        	
        default:
        	return  null;
        }
	}
	/**
	 * 
	 * @param id
	 * @param dialog
	 * @return
	 */
	public boolean PrepareDialog( int id, Dialog dialog){
		switch(id) {
		case RETRYERRORMESSAGE_DIALOG:	// リトライ確認Dialog
    	  ((AlertDialog)dialog).setMessage( getDialogMessage( mLabelData ) );
    	  return true;
		default:
			return false;
		}
		
	}
	
	/**
	 * エラーDialog生成処理
	 * @return Dialog
	 */
    private Dialog createErrormessageDialog() {
    
		AlertDialog.Builder bd = new AlertDialog.Builder(mActivity);
		bd.setTitle(R.string.error);
		bd.setMessage( mLabelData.getStatusMessage() );
		bd.setPositiveButton(R.string.msg_Ok, new OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// 
				mActivity.dismissDialog( ERRORMESSAGE_DIALOG );
				
			}}
		);
		return bd.create();
    }
	/**
	 * 印刷完了Dialog生成処理
	 * @return Dialog
	 */
	private Dialog createPrintCompletemessageDialog() {
		AlertDialog.Builder bd = new AlertDialog.Builder(mActivity);
		bd.setTitle(R.string.processComplete);
		bd.setMessage( mLabelData.getStatusMessage() );
		bd.setPositiveButton(R.string.msg_Ok, new OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// 
				mActivity.dismissDialog( PRINT_COMPLETEMESSAGE_DIALOG );
				
			}}
		);
		return bd.create();
	}
	/**
	 * リトライエラーDialog生成処理
	 * @return Dialog
	 */
	private Dialog createRetryErrormessageDialog() {
		AlertDialog.Builder bd = new AlertDialog.Builder(mActivity);
		bd.setTitle(R.string.error);
		bd.setIcon(android.R.drawable.ic_dialog_alert);
		bd.setMessage( getDialogMessage( mLabelData ) );
		
		bd.setPositiveButton(R.string.msg_Ok, new OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// 
				if(RETRYERRORMESSAGE_COUNT < 3 )
				{
					
					new RetryPrintExecuteTask( mActivity , mBcpControl ).execute( mLabelData );

					mActivity.dismissDialog( RETRYERRORMESSAGE_DIALOG );
					RETRYERRORMESSAGE_COUNT++;
				}
			}}
		);
		
		bd.setNegativeButton(R.string.msg_No, new OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// 
				mActivity.dismissDialog( RETRYERRORMESSAGE_DIALOG );
			} });
		
		return bd.create();
	}

	private String getDialogMessage( PrintData labelData){
		 long result = labelData.getResult();
	    StringBuilder sb = new StringBuilder();
    	// プリンタからの応答なし
    	if( result == 0x800A044CL ){
    		sb.insert( 0 , mActivity.getString(R.string.noResponseFromPrinter));
    		
    	} else if( result == 0x800A03EBL ){  // 送信タイムアウトエラー
    		sb.insert( 0 , mActivity.getString(R.string.writeFailed));
    		
    	} else if( result == 0x13L ) {		// 用紙が終了した
    		sb.insert( 0 , mActivity.getString(R.string.paperEnd) );
    	} else if( result == 0x09L ) {		// 用紙が終了した
    		sb.insert( 0 , mActivity.getString(R.string.issueEndAndpaperEnd));
    	} else	{
    		sb.insert( 0 , labelData.getStatusMessage() );
    	}
		return sb.toString();
		
	}
}
