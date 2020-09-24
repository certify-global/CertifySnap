package com.certify.snap.printer.usb;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.AssetManager;
import android.widget.EditText;

import com.certify.snap.R;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;

import jp.co.toshibatec.bcp.library.BCPControl;

import static com.certify.snap.printer.usb.Defines.OPTION_CONTROLCODE_KEYNAME;
import static com.certify.snap.printer.usb.Defines.OPTION_GRAPHICTYPE_KEYNAME;
import static com.certify.snap.printer.usb.Defines.OPTION_LANGUAGE_KEYNAME;
import static com.certify.snap.printer.usb.Defines.OPTION_RECVTIME_KEYNAME;
import static com.certify.snap.printer.usb.Defines.PORTSETTING_FILE_PATH_KEYNAME;
import static com.certify.snap.printer.usb.Defines.PORTSETTING_IP_ADDRESS_KEYNAME;
import static com.certify.snap.printer.usb.Defines.PORTSETTING_PORT_MODE_KEYNAME;
import static com.certify.snap.printer.usb.Defines.PORTSETTING_PORT_NUMBER_KEYNAME;

public class util {

	public static final String bcpFolderPath = "/TOSHIBATEC/BCP_Print_for_Android";

	
	
	static final String sectionName = "bcpCommonData";
	public static int selected;
	public static BCPControl bcpControl = null;
	/**
	 * 
	 * @param ctxt
	 * @param key
	 * @param value
	 */
	public static void setPreferences( Context ctxt , String key , String value ) {
		ctxt.getSharedPreferences( sectionName , Context.MODE_PRIVATE).edit().putString( key, value ).commit();
	}
	/**
	 * 
	 * @param ctxt
	 * @param key
	 * @return
	 */
	public static String getPreferences( Context ctxt , String key ){
		return ctxt.getSharedPreferences( sectionName , Context.MODE_PRIVATE).getString( key , "");
	}	
	/**
	 * 
	 * @param conn
	 * @param className
	 * @return
	 */
    public static Intent getCallActivityIntent(Context conn , String className) {
        
    	String packageName = conn.getPackageName();
    	String callActivityName = packageName + "." + className;
    	
        Intent intentStartApp = new Intent();
        intentStartApp.setClassName( packageName, callActivityName);
        intentStartApp.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        // intentStartApp.putExtra("WorkData", WorkData);
        return intentStartApp;
    }
    /**
     * 
     * @param context
     * @param strMessage
     */
	public static void showAlertDialog(Context context, String strMessage){
		AlertDialog.Builder Alertbuilder = new AlertDialog.Builder(context);
		Alertbuilder.setMessage(strMessage);
		Alertbuilder.setCancelable(false);
		Alertbuilder.setPositiveButton(R.string.msg_Ok, null);
		AlertDialog alertDialog = Alertbuilder.create();
		alertDialog.show();
	}

	public static void comfirmDialog( Context context, String strMessage , OnClickListener okListener ) {
		AlertDialog.Builder Alertbuilder = new AlertDialog.Builder(context);
		Alertbuilder.setMessage(strMessage);
		Alertbuilder.setCancelable(false);
		Alertbuilder.setPositiveButton(R.string.msg_Ok, okListener );
		AlertDialog alertDialog = Alertbuilder.create();
		alertDialog.show();
		
	}
	/**
	 * 
	 * @param context  [in] 
	 * @param strMessage  [in] 
	 * @param okListener  [in] 
	 * @param cancelListener  [in] 
	 * @param cancelListener  [in] 
	 */
	public static void comfirmDialog( Context context, String strMessage , OnClickListener okListener , OnClickListener cancelListener ) {
		AlertDialog.Builder Alertbuilder = new AlertDialog.Builder(context);
		Alertbuilder.setMessage(strMessage);
		Alertbuilder.setCancelable(false);
		Alertbuilder.setPositiveButton(R.string.msg_Ok, okListener );
		Alertbuilder.setNegativeButton(R.string.msg_No, cancelListener );
		AlertDialog alertDialog = Alertbuilder.create();
		alertDialog.show();
		
	}
	/**
	 *  Rawリソースのファイル保存
	 * @param context
	 * @param resID
	 * @param folderName
	 * @param fileName
	 * @throws Exception
	 */
	public static void raw2file(Context context , int resID , String folderName , String fileName ) throws Exception {
        InputStream in = context.getResources().openRawResource(resID);
        in2file( context , in , folderName , fileName );
    }
	/**
	 * @param context
	 * @param inputFileName
	 * @param folderName
	 * @param outputFileName
	 * @throws Exception
	 */
	public static void asset2file(Context context , String inputFileName , String folderName , String outputFileName ) throws Exception {

		AssetManager as = context.getResources().getAssets();   
		InputStream is = as.open( inputFileName );  

		in2file( context , is , folderName , outputFileName );
    }
	
    /**
     * @param context
     * @param in
     * @param folderName
     * @param fileName
     * @throws FileNotFoundException
     * @throws Exception
     */
    private static void in2file(Context context, InputStream in , String folderName , String fileName) throws FileNotFoundException, Exception {
        int size;
        byte[] w = new byte[1024];
        FileOutputStream fis = null;
        try {

        	
        	String fileFullPath = folderName + "/" + fileName;
        	
        	fis = new FileOutputStream( fileFullPath );
            while (true) {
                size=in.read(w);
                if (size<=0) break;
                fis.write(w , 0 , size);
            };
            fis.close();
            in.close();
		} catch (FileNotFoundException e) {
            try {
                if (in !=null) in.close();
                if (fis!=null) fis.close();
            } catch (Exception e2) {
            }
            throw e;
        } catch (Exception e) {
            try {
                if (in !=null) in.close();
                if (fis!=null) fis.close();
            } catch (Exception e2) {
            }
            throw e;
        }
    }
	
    /**
     * 
     * @param activityObj [in] activity instance
     * @param editControlID [in] edit text control id
     * @param compareData [in] compare data
     * @param keyName [in] preference save keyName
     */
    public static void saveLabelData( Activity activityObj , int editControlID , String compareData, String keyName) {
		
		String targetData = (( EditText )activityObj.findViewById( editControlID )).getText().toString();
		
		if( targetData.compareTo(compareData )  == 0 ) {
        	util.setPreferences( activityObj.getApplicationContext(), keyName , "" );
        } else {
        	util.setPreferences( activityObj.getApplicationContext(), keyName , targetData );
        }
	}
	
    /**
     * 
     * @param activityObj [in] activity instance
     * @param editControlID [in] resource id
     * @param defaultData [in] default value
     * @return
     */
    public static String getLavelDataForEditText( Activity activityObj , int editControlID , String defaultData ) {
		
		String targetData = (( EditText )activityObj.findViewById( editControlID )).getText().toString();
		if( targetData == null || targetData.length() == 0 ) {
			targetData = defaultData;	
		}
		return targetData;
    }
    
    
    
    
    /**
     * 
     * @param paringName
     * @return
     */
	public static String getBluetoothAddress(String paringName) {
		String mPortSetting;
		int firstPosition = paringName.lastIndexOf("(");
    	int endPosition = paringName.lastIndexOf(")");
    	mPortSetting = "Bluetooth:" + paringName.substring( firstPosition+1, endPosition);
		return mPortSetting;
	}

    public static void SetPropaty(Context con , BCPControl bcpControlObject)
    {
        int receiveTimeValue = 0;
    	String receiveTimeString = util.getPreferences(con, OPTION_RECVTIME_KEYNAME  );
    	if( receiveTimeString.length() == 0 ) {
    		receiveTimeValue = 15;
    	} else {
    		receiveTimeValue = Integer.parseInt( receiveTimeString );
    	}
        bcpControlObject.setRecvTimeout( receiveTimeValue );

        String controlCode = util.getPreferences(con, OPTION_CONTROLCODE_KEYNAME );
        int controlCodeValue = 0;
        if( controlCode.equals("ESC,LF,NUL") ) {
        	controlCodeValue = 0;
        } else if( controlCode.equals("{,|,}") ) {
        	controlCodeValue = 1;
        } else {
        	controlCodeValue = 0;        	
        }
        bcpControlObject.setControlCode(controlCodeValue);
        /** GraphicTypeの設定 */
        String graphicValue = util.getPreferences(con, OPTION_GRAPHICTYPE_KEYNAME );
        int graphicTypeValue = 2;
        if( graphicValue.equals("Nibble") ) {
        	graphicTypeValue = 0;
        } else if( graphicValue.equals("Hex") ) {
        	graphicTypeValue = 1;
        } else if( graphicValue.equals("Topix") ) {
        	graphicTypeValue = 2;
        } else {
        	graphicTypeValue = 1;
        }
        bcpControlObject.setGraphicType(graphicTypeValue);
        /** Language の設定 */
        String languageValue = util.getPreferences(con, OPTION_LANGUAGE_KEYNAME );
        int languageCodeValue = 1;
        if( languageValue.equals("English") ) {
        	languageCodeValue = 1;
        } else if( languageValue.equals("Japanese") ) {
        	languageCodeValue = 0;
        } else {
        	languageCodeValue = 1;
        }
        bcpControlObject.setLanguage(languageCodeValue);
    }

	public static String getPortSetting(Context conn ) {
		
		String portSetting = "";
		String currentPortMode = util.getPreferences(conn, PORTSETTING_PORT_MODE_KEYNAME );
    	
    	if( currentPortMode.equals("Bluetooth") ) {

    		StringBuilder sb = new StringBuilder();
    		
			String pearingNameKey = "BluetoothPareName";
			sb.insert(0, util.getPreferences(conn, pearingNameKey) );
			if( sb.toString().length() == 0 ) {
    			util.showAlertDialog(conn, conn.getString(R.string.bdAddrNotSet));
    			return "";
			}
			sb.insert(0, util.getBluetoothAddress(sb.toString()) );
			portSetting = sb.toString();
			if( portSetting.toString().length() == 0 ) {
    			util.showAlertDialog(conn, conn.getString(R.string.bdAddrNotSet));
    			return "";
			}
    		
    	} else if( currentPortMode.equals("WLAN") ) {
    		
    		String ipAddress = util.getPreferences(conn, PORTSETTING_IP_ADDRESS_KEYNAME );
    		String portNumber = util.getPreferences(conn, PORTSETTING_PORT_NUMBER_KEYNAME );

    		if( ipAddress.length()==0 || portNumber.length() == 0 ) {
    			util.showAlertDialog(conn, conn.getString(R.string.ipOrPortNoNotSet) );
    			return "";
    		}
    		portSetting = "TCP://";
    		portSetting += ipAddress;
    		portSetting += ":";
    		portSetting += portNumber;
    		
    	} else if( currentPortMode.equals("FILE") ) {
    		
    		String fileNamePath = util.getPreferences(conn, PORTSETTING_FILE_PATH_KEYNAME );
    		if( fileNamePath.length() == 0 ) {
    			util.showAlertDialog(conn, conn.getString(R.string.fileNameNotSet) );
    			return "";
    		}
    		portSetting = "FILE:";
    		portSetting += fileNamePath;
    		
    	} else {
    		util.showAlertDialog(conn, conn.getString(R.string.outputMethodNotSelected));
			return "";
    	}
		return portSetting;
	}
    //

	public static String trimControlChars(String str)
    {
    	StringBuffer buf = new StringBuffer();

    	char chr_tmp;
    	int code;
    	for (int i=0;i<str.length();i++)
    	{
    		chr_tmp=str.charAt(i);
    		code=(int)chr_tmp;

    		if(code > 0x1f && code != 0x7f){
    			buf.append(chr_tmp);
    		}
    	}

    	return buf.toString();
    }
    
    public static boolean checkIP(String ipAddress) {

		if (ipAddress != null && (ipAddress.length() > 0)) {
			// IPアドレス正規表現
			String regex = "^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])\\."
					+ "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
					+ "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
					+ "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])$";
			// IPアドレス有効かをチェック
			if (ipAddress.matches(regex)) {
				// 正常の場合
				return true;
			} else {
				// 異常
				return false;
			}
		}
		// 空白の場合
		return true;
	}
}
