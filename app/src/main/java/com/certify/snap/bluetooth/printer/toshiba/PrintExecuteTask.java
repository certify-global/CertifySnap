package com.certify.snap.bluetooth.printer.toshiba;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;

import com.certify.snap.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

import jp.co.toshibatec.bcp.library.BCPControl;
import jp.co.toshibatec.bcp.library.LongRef;
import jp.co.toshibatec.bcp.library.StringRef;

import static com.certify.snap.bluetooth.printer.toshiba.Defines.PORTSETTING_FILE_PATH_KEYNAME;
import static com.certify.snap.bluetooth.printer.toshiba.Defines.PORTSETTING_PORT_MODE_KEYNAME;

public class PrintExecuteTask extends AsyncTask<PrintData, Void, String  >{

	// final String bcpFolderPath = "/TOSHIBATEC/BCP_Print_for_Android";
	
	BCPControl m_bcpObj = null;
	// PrintData  m_printData = null;
	Activity    mContext = null;
	ProgressDialog mProgressDlg = null;
	String mGetStatus = "";
	
	// 復帰モード
	final int AsynchronousMode = 1;	// 送信完了復帰（非同期）
	final int SynchronousMode = 2;		// 発行完了復帰（同期）
	
	
    // コンストラクタ
    public PrintExecuteTask(Activity conText , BCPControl bcpcontrol ) {
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
		// mProgressDlg.setMax(5);
		mProgressDlg.setCancelable(false);
		mProgressDlg.show();

    	
    }

    //Amir
	private Object _locker = new Object();
	private Object _sync_perm = new Object();
	private final String ACTION_USB_PERMISSION = "jp.co.toshibatec.bcp.sample000.USB_PERMISSION";

	private UsbManager m_usbManager = null;
	private UsbDevice m_usbDevice = null;
	private boolean m_isConnected = false;

	final BroadcastReceiver USB_BCAST_RECEIVER = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			try {
				// Step1: Check Permission
				if (!Objects.requireNonNull(intent.getAction()).equals(ACTION_USB_PERMISSION)) {
					throw new Exception("Unknown Intent Action:" + intent.getAction());
				}

				// Step2: Get "Request Permission" device information
				m_usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
				if (!intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
					throw new Exception("Usb Connection Permission Denied");
				}

			} catch (Exception err) {
			}
			synchronized (_sync_perm) {
				_sync_perm.notify();
			}
		}
	};

	protected boolean connectToUSBPrinter(int vID, int pID) {

		m_usbManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
		HashMap<String, UsbDevice> deviceList = m_usbManager.getDeviceList();

		Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();

		// Iterate all the available devices and find ours.
		while (deviceIterator.hasNext()) {
			UsbDevice device = deviceIterator.next();
			if ((device.getProductId() == pID || pID ==0xFFFF) && device.getVendorId() == vID) {
				m_usbDevice = device;
			}
		}

		if (m_usbDevice == null) { // device not found
			return false;
		}

		// we already have permission
		if(m_usbManager.hasPermission(m_usbDevice)) {
			return true;
		}

		HandlerThread thPerm = new HandlerThread("Common Lib TEC USB Permission Request");
		thPerm.start();

		Looper thLoop = thPerm.getLooper();
		Handler hCast = new Handler(thLoop);
		PendingIntent mPermissionIntent = PendingIntent.getBroadcast(mContext, 0, new Intent(ACTION_USB_PERMISSION), 0);
		IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
		filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
		// Receiver if user approve the USB permission
		// Step1: Check Permission
		// Step2: Get "Request Permission" device information

		// Create and intent and request a permission.
		mContext.registerReceiver(USB_BCAST_RECEIVER, filter, null, hCast);
		m_usbManager.requestPermission(m_usbDevice, mPermissionIntent);

		int timeout = 30000;
		synchronized (_sync_perm) {
			try {
				if (timeout == -1) {
					// wait infinitely
					_sync_perm.wait();
				} else {
					_sync_perm.wait(timeout);
				}
			} catch (InterruptedException e) {
				// Problem happen
			}
		}
		mContext.unregisterReceiver(USB_BCAST_RECEIVER);
		thLoop.quit();

		if(!m_usbManager.hasPermission(m_usbDevice)) {
			//access denied
			return false;
		}

		return true;
	}

	private boolean readThread(){
		new Thread(new Runnable() {
			@Override
			public void run() {

				long lastReadTime = System.currentTimeMillis();
				while (m_isConnected) {
					try{Thread.sleep(500);} catch (InterruptedException ex){};

					UsbInterface readIntf = null;//m_usbDevice.getInterface(0);
					UsbEndpoint readEp = null;//readIntf.getEndpoint(0);
					// find interface
					for (int k=0; k < m_usbDevice.getInterfaceCount();k++){
						UsbInterface intf = m_usbDevice.getInterface(k);
						if (intf.getInterfaceClass() == 7){
							readIntf = intf;
						}
					}

					if (readIntf == null){
						try{Thread.sleep(1000);} catch (InterruptedException ex){};
						//ShowLog("No Printer class interface found (in reading)!");
						continue;
					}

					// find read endpoint
					for (int k=0; k < readIntf.getEndpointCount();k++){
						UsbEndpoint ep = readIntf.getEndpoint(k);
						if (ep.getDirection() == UsbConstants.USB_DIR_IN){
							readEp = ep;
							//ShowLog("Read Endpoint: " + readEp.toString());
						}
					}
					if (readEp == null){
						try{Thread.sleep(1000);} catch (InterruptedException ex){};
						continue;
					}

					synchronized (_locker) {
						// open device
						UsbDeviceConnection readConnection = null;
						try {
							readConnection = m_usbManager.openDevice(m_usbDevice);
							if (readConnection == null){
								try{Thread.sleep(2000);} catch (InterruptedException ex){};
								continue;
							}
						} catch (SecurityException e) {
							try{Thread.sleep(2000);} catch (InterruptedException ex){};
							continue;
						}

						// claim device
						readConnection.claimInterface(readIntf, true);

						// Read the data as a bulk transfer with the size = MaxPacketSize
						int packetSize = readEp.getMaxPacketSize();
						byte[] buffer = new byte[packetSize];
						int nRead = readConnection.bulkTransfer(readEp, buffer, packetSize, 50);
						if (nRead > 0) {

						}

						// Release the interface lock.
						readConnection.releaseInterface(readIntf);
						readConnection.close();
					}
				}
			}
		}).start();

		return true;
	}

	private String readFile(){
		// read file
		String content = null;

		String fileNamePath = util.getPreferences(mContext, PORTSETTING_FILE_PATH_KEYNAME );
		File fl = new File(fileNamePath);
		try {
			FileInputStream fin = new FileInputStream(fl);
			BufferedReader reader = new BufferedReader(new InputStreamReader(fin));
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line).append("\n");
			}
			reader.close();
			content = sb.toString();
			//Make sure you close all streams.
			fin.close();
		} catch (FileNotFoundException fex){
			// file not found!

		}
		catch (IOException iex){
			// error reading!
		}

		return content;

	}
	private boolean printUSB(){
		if ( connectToUSBPrinter(0x08A6, 0xFFFF) == false){
			return false;
		}

		m_isConnected = true;
		readThread();

		// write
		do {
			final String content = readFile();
			if (content == null){
				break;
			}

			try {
				// Lock that is common for read/write methods.
				synchronized (_locker) {
					UsbInterface writeIntf = null;// m_usbDevice.getInterface(0);
					UsbEndpoint writeEp = null;//writeIntf.getEndpoint(1);

					// find interface
					for (int k = 0; k < m_usbDevice.getInterfaceCount(); k++) {
						UsbInterface intf = m_usbDevice.getInterface(k);
						if (intf.getInterfaceClass() == 7) {
							writeIntf = intf;

						}
					}

					if (writeIntf == null) {
						writeIntf = m_usbDevice.getInterface(0);
					}

					if (writeIntf == null) {
						// error
						break;
					}

					// find write endpoint
					for (int k = 0; k < writeIntf.getEndpointCount(); k++) {

						UsbEndpoint ep = writeIntf.getEndpoint(k);
						if (ep.getDirection() == UsbConstants.USB_DIR_OUT) {
							writeEp = ep;
						}
					}

					// open device
					UsbDeviceConnection writeConnection = m_usbManager.openDevice(m_usbDevice);
					if (writeConnection == null) {
						//error
						break;
					}

					// Lock the usb interface.
					if (!writeConnection.claimInterface(writeIntf, true)) {
						//error
						break;
					}

					int idx = 0;
					int size = content.length();

					while (size > 0 && writeEp != null) {
						int epSize = writeEp.getMaxPacketSize();
						int sendSize = Math.min(size, epSize);

						// Write the data as a bulk transfer with defined data length.
						int sentSize = writeConnection.bulkTransfer(writeEp, content.getBytes(), idx, sendSize, 0);
						if (sentSize != -1) {
							if (sentSize == 0) break;
						} else {
							break;
						}

						size -= sentSize;
						idx += sentSize;
					}

					// Release the usb interface.
					writeConnection.releaseInterface(writeIntf);
					writeConnection.close();
				}

			} catch (NullPointerException e) {
				break;
			}
		} while (false);

		m_isConnected = false;
		return true;
	}

	// バックグラウンドで実行する処理
    @Override  
    protected String doInBackground(PrintData... urls) {

    	 PrintData printData = urls[0];
    	 Log.d("PrintExecuteTask","----------------doInBackground");
    	long ret = 0;

		printData.setResult( 0 );
		printData.setStatusMessage("");

		//Amir - delete file by closing the port and reopening
		String currentPortMode = util.getPreferences(mContext, PORTSETTING_PORT_MODE_KEYNAME );
		if( currentPortMode.equals("FILE")) {

			LongRef Result = new LongRef(0);
			m_bcpObj.ClosePort(Result);

			final boolean resultOpen = m_bcpObj.OpenPort(1/*printData.getIssueMode()*/, Result);
			if (false == resultOpen) {

				StringRef message = new StringRef("");
				if (false == m_bcpObj.GetMessage(Result.getLongValue(), message)) {

					return mContext.getString(R.string.msg_OpenPorterror);
				}
				return message.getStringValue();
			}
		}

		String lfmFileFullPath = printData.getLfmFileFullPath();
		mGetStatus = "";
		// load lfm file
		Log.d("PrintExecuteTask","-----------------loadLfmFile start");
		if( 0 != (ret = loadLfmFile( lfmFileFullPath )) ) {
			Log.d("PrintExecuteTask","-----------------loadLfmFile Error");
			return String.format( "loadLfmFile Error = %08x %s" , ret, lfmFileFullPath );
		}
		// 
		// 復帰モードが発行完了モードの場合は、
		Log.d("PrintExecuteTask","-----------------changePosition start");
		if( printData.getCurrentIssueMode() ==  SynchronousMode ) {
			// GetStatusメソッドを呼び出してプリンタから受信した文字列と実行結果を取得します。
			if( 0 != ( ret = changePosition() ) ){
				Log.d("PrintExecuteTask","-----------------changePosition Error");
				return String.format( "changePosition Error = %08x " , ret );
			}
		}
		
		// change issue mode
		Log.d("PrintExecuteTask","-----------------changeIssueMode start");
		if( 0 != (ret = changeIssueMode( )) ) {
			Log.d("","-----------------changeIssueMode Error");
			return String.format( "changeIssueMode Error = %08x " , ret );
		}
		// set object
		Log.d("PrintExecuteTask","-----------------setObjectDataEx start");
		if( 0 != (ret = setObjectDataEx(printData)) ) {
			Log.d("PrintExecuteTask","-----------------setObjectDataEx Error");
			return String.format( "setObjectDataEx Error = %08x " , ret );
		}
		int printCount = printData.getPrintCount();
		
		StringRef printerStatus = new StringRef("");
		// print
		if( 0 != (ret = executePrint( printCount , printerStatus )) ) {
			
			//
			printData.setResult( ret );
			
			StringRef message = new StringRef("");
			// プリンタからステータスを受信しました。
			if( ret == 0x800A044EL ) {
				String errCode = printerStatus.getStringValue().substring(0, 2);
				m_bcpObj.GetMessage( errCode , message );  
			} else {
				if( false == m_bcpObj.GetMessage( ret , message ) ) {
					message.setStringValue( String.format( "executePrint Error = %08x " , ret ) );
				}
			}
			printData.setStatusMessage( message.getStringValue() );
			
			// リトライ可能エラー有無の確認
			if( m_bcpObj.IsIssueRetryError() ) {
				return mContext.getString(R.string.msg_RetryError);
			} else {
				return mContext.getString(R.string.error);
			}
			
		} else {
			printData.setResult( ret );
		}
		// 復帰モードが発行完了モードの場合は、
		if( printData.getCurrentIssueMode() ==  SynchronousMode ) {
			// GetStatusメソッドを呼び出してプリンタから受信した文字列と実行結果を取得します。
			mGetStatus = this.getPrinterStatus();
			
		}
		//Amir - if file selected, try to print to USB printer
		if( currentPortMode.equals("FILE")){
			printUSB();
		}

		printData.setStatusMessage( mContext.getString(R.string.msg_success));
        return  mContext.getString(R.string.msg_success);
    }
  
    // メインスレッドで実行する処理
    @Override  
    protected void onPostExecute(String result) {
    	mProgressDlg.dismiss();
    //	util.showAlertDialog(mContext, result);
   	
    	if( result.equals(mContext.getString(R.string.msg_success)) ) {
    		mContext.showDialog( PrintDialogDelegate.PRINT_COMPLETEMESSAGE_DIALOG );
    	
    	} else if( result.equals(mContext.getString(R.string.msg_RetryError)) ) {
    		Log.d("RetryError","");
    		mContext.showDialog( PrintDialogDelegate.RETRYERRORMESSAGE_DIALOG );
    	} else {
    		Log.d("else RetryError","");
    		mContext.showDialog( PrintDialogDelegate.ERRORMESSAGE_DIALOG );
    	}
    	m_bcpObj = null;
    	mContext = null;
    }
    
    /**--------------------------------------------------------------------------
     * 
     --------------------------------------------------------------------------*/
    private long changePosition() {
    
    	LongRef result = new LongRef( 0 );
    	int mode = 0;			// 変更対象モード(0:フィード量 1:カット剥離位置 )
    	int adjust = -15;		// 微調整値(0.1mm単位)-1.5mm
    	
    	if( false == this.m_bcpObj.ChangePosition(mode, adjust, result ) ) {
    		return result.getLongValue();
    	}
    	return 0;
    }
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
    		
        	return String.format( R.string.msg_GetStatus + "= %s :" + R.string.msg_result + " = %08x ", printerStatus.getStringValue(), result.getLongValue() );
    	}
    }
    /**
     * 
     * @param printCount [in] 印刷枚数
     * @return
     */
    private long executePrint( int printCount , StringRef printerStatus ) {
    	long ret = 0;
    	LongRef result = new LongRef( 0 );
    	// 印刷実行
		int cutInterval = 10;	// 10msec
		if( false == m_bcpObj.Issue(printCount, cutInterval, printerStatus, result) ) {
			ret = result.getLongValue();
		}
    	return ret;
    }
    
    /**
     * 
     * @return
     */
    private long setObjectDataEx( PrintData printdata ) {
    	long ret = 0;
    	LongRef result = new LongRef( 0 );
    	//すべてのキー値を取得   
    	Set keySet = printdata.getObjectDataList().keySet();                          
		Iterator keyIte = keySet.iterator();   
		//ループ。反復子iteratorによる　キー　取得   
		while( keyIte.hasNext() ) {                         
			String key = (String)keyIte.next();   
			String value = (String)printdata.getObjectDataList().get( key );        //キーよりvalueを取得   
			if( false == m_bcpObj.SetObjectDataEx( key , value , result ) ) {
				ret = result.getLongValue();
				break;
			}
			
			
		}
    	
    	return ret;
    }

    
    /**
     * 発行条件の変更
     * @return
     * 
     * 2012/08/06 TIS Asai  引数typeの値 を　1:反射センサーから0:センサーなしに修正
     * 
     */
    private long changeIssueMode( ) {
		int flag = 0;	// 0 :センサー種別
		int type = 0;	// 1 : 反射センサー , 0 : センサーなし
    	
    	LongRef result = new LongRef( 0 );
    	m_bcpObj.ChangeIssueMode( flag, type, result );
		return new Long( result.getLongValue() );
    	
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
    /**
     * 
     * @param lfmFileFullPath
     * @return
     */
    private long loadLfmFile( String lfmFileFullPath ) {
    	
    	LongRef result = new LongRef( 0 );
    	if( false == m_bcpObj.LoadLfmFile(lfmFileFullPath, result ) ) {
    		return result.getLongValue();
    	}
    	
    	
    	return 0;
    }
        
}
