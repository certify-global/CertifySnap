package com.certify.snap.bluetooth.printer.toshiba;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.certify.snap.R;

import jp.co.toshibatec.bcp.library.BCPControl;
import jp.co.toshibatec.bcp.library.LongRef;
import jp.co.toshibatec.bcp.library.StringRef;

import static com.certify.snap.bluetooth.printer.toshiba.Defines.PORTSETTING_FILE_PATH_KEYNAME;
import static com.certify.snap.bluetooth.printer.toshiba.Defines.PORTSETTING_IP_ADDRESS_KEYNAME;
import static com.certify.snap.bluetooth.printer.toshiba.Defines.PORTSETTING_PORT_MODE_KEYNAME;
import static com.certify.snap.bluetooth.printer.toshiba.Defines.PORTSETTING_PORT_NUMBER_KEYNAME;
import static com.certify.snap.bluetooth.printer.toshiba.Defines.PRINTER_TYPE_KEYNAME;


public class ConnectionDelegate {
	/**
	 * 
	 * @param activity
	 * @param bcpControl
	 * @param connectData
	 * @param issueMode
	 */
	public void openPort(Activity activity , BCPControl bcpControl , ConnectionData connectData , int issueMode ){
    	if( connectData.getIsOpen().get() == false ) {

//        	String systemPath = Environment.getExternalStorageDirectory().getPath() + util.bcpFolderPath;
        	String systemPath = Environment.getDataDirectory().getPath() + "/data/" + activity.getPackageName();
        	Log.i("openPort::systemPath ", systemPath );
        	bcpControl.setSystemPath(systemPath);

        	StringBuilder portSetting = getPortSetting(activity );
        	
        	connectData.setIssueMode( issueMode );
    		connectData.setPortSetting( portSetting.toString() );
    		
    		String printerType = util.getPreferences( activity , PRINTER_TYPE_KEYNAME );
    		int usePrinter = Defines.getPrinterNo(printerType);
    		bcpControl.setUsePrinter(usePrinter);
    		
    		ConnectExecuteTask connectTask = new ConnectExecuteTask( activity ,  bcpControl );
    		connectTask.execute( connectData );  
    		
    	}
		
	}
	/**
	 * 
	 * @param activity
	 * @param bcpControl
	 * @param connectData
	 */
	public void closePort(Activity activity , BCPControl bcpControl , ConnectionData connectData){
        if( connectData.getIsOpen().get() == true ) {
	    	LongRef Result = new LongRef( 0 );
	        if( false == bcpControl.ClosePort( Result ) ) {
	        	StringRef Message = new StringRef("");
	        	
	        	if( false == bcpControl.GetMessage(Result.getLongValue(), Message) ) {
		        	
		        	util.showAlertDialog(activity , String.format( R.string.msg_PortCloseErrorcode + "= %08x", Result.getLongValue() ) );
	        		
	        	} else {
	        		
		        	util.showAlertDialog(activity , Message.getStringValue() );
	        		
	        	}
	        	return;
	        } else {
	        	util.showAlertDialog(activity , activity.getString(R.string.msg_PortCloseSuccess) );
	        	
	        	connectData.getIsOpen().set( false );
	        }
        }
		
	}
	/**
	 * @param portSetting
	 */
	public static StringBuilder getPortSetting(Context con) {
		StringBuilder portSetting = new StringBuilder();
		String portMode = util.getPreferences( con , PORTSETTING_PORT_MODE_KEYNAME );
		Log.d("------------Port Mode:", portMode);
		if( portMode.equals( "Bluetooth" ) ) {
			String pearingNameKey = "BluetoothPareName"; 
			portSetting.insert(0, util.getPreferences(con, pearingNameKey) );
			portSetting.insert(0, util.getBluetoothAddress(portSetting.toString()) );
			
		} else if( portMode.equals( "WLAN"  ) ) {
			String ipAddress = util.getPreferences(con, PORTSETTING_IP_ADDRESS_KEYNAME );
			String portNumber = util.getPreferences(con, PORTSETTING_PORT_NUMBER_KEYNAME );

			portSetting.insert(0, "TCP://");
			portSetting.append( (ipAddress.length()==0)?"192.168.1.151": ipAddress);
			portSetting.append(":");
			portSetting.append( (portNumber.length()==0)?"65000": portNumber);
			
		} else if( portMode.equals( "FILE" ) ) {
			portSetting.insert(0, "FILE:");
			
			String fileFullPath = util.getPreferences(con, PORTSETTING_FILE_PATH_KEYNAME );
			
			if( fileFullPath.length() == 0 ) {
				portSetting.insert(0,"");		// portSettingの内容を空白にしてエラーとする。
			} else {
				portSetting.append( fileFullPath );
			}
			
		} else {
			portSetting.insert(0,"");
		}
		return portSetting;
	}
	
}
