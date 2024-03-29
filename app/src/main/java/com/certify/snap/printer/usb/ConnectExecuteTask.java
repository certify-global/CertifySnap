package com.certify.snap.printer.usb;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Button;

import com.certify.snap.R;

import jp.co.toshibatec.bcp.library.BCPControl;
import jp.co.toshibatec.bcp.library.LongRef;
import jp.co.toshibatec.bcp.library.StringRef;

import static com.certify.snap.printer.usb.Defines.AsynchronousMode;

/**
 *
 */

public class ConnectExecuteTask extends AsyncTask<ConnectionData, Void, String  >{

	BCPControl m_bcpObj = null;
	// ConnectionData  m_connectData = null;
	Activity    mContext = null;
	Button mPortOpenButton = null;

    public ConnectExecuteTask(Activity conText , BCPControl bcpcontrol ) {
    	mContext = conText;
    	m_bcpObj = bcpcontrol;
    	mPortOpenButton = null;
    }

    public ConnectExecuteTask(Activity conText , Button portOpenButton , BCPControl bcpcontrol ) {
    	mContext = conText;
    	m_bcpObj = bcpcontrol;
    	mPortOpenButton = portOpenButton;
    }


    /**
     * バックグラウンドで実行する処理 
     */
	@Override
	protected String doInBackground(ConnectionData... params) {
		
		ConnectionData connectData = params[0];
		String portSettingData = connectData.getPortSetting();
		
    	Log.i("ConnectExecuteTask", "onClickButtonOpen:portSetting  portSettingData = " + portSettingData );
    	m_bcpObj.setPortSetting( portSettingData );
    	// 出力先がFILEの場合は、IssueModeを非同期に設定する
    	if( portSettingData.substring(0, 4).equals("FILE") == true ) {
    		connectData.setIssueMode( AsynchronousMode );
    	}
    	
    	LongRef Result = new LongRef( 0 );

    	final boolean resultOpen = m_bcpObj.OpenPort( connectData.getIssueMode() , Result );

    	connectData.getIsOpen().set( resultOpen );
    	if( false ==  resultOpen) {

    		StringRef message = new StringRef("");
    		if( false == m_bcpObj.GetMessage( Result.getLongValue() , message ) ) {
    			
    			return mContext.getString(R.string.msg_OpenPorterror);
    		}
    		return message.getStringValue();
    		
    	}
		
        return mContext.getString(R.string.msg_success);
    }
	/**
	 * メインスレッドで実行する処理
     * @param result
	 */
    @Override  
    protected void onPostExecute(String result) {

    	if( mContext.getString(R.string.msg_success).equals(result) && null != mPortOpenButton ) {
        	mPortOpenButton.setText(R.string.msg_PortClose);
    	}

    	mContext = null;
    	m_bcpObj = null;
    	mPortOpenButton = null;
    	
    }
    

	
}
