package com.certify.snap.bluetooth.printer.toshiba;


public abstract class Defines {

	//プリファレンスキー名(PortSetting画面)
	public static final String PORTSETTING_PORT_MODE_KEYNAME    = "PPMK";
	public static final String PORTSETTING_MAC_ADDRESS_KEYNAME  = "PMAK";
	public static final String PORTSETTING_IP_ADDRESS_KEYNAME   = "PIAK";
	public static final String PORTSETTING_PORT_NUMBER_KEYNAME  = "PPNK";
	public static final String PORTSETTING_FILE_PATH_KEYNAME    = "PFPK";

	public static final String PRINTER_TYPE_KEYNAME             = "PRINTERTYPE";

	// 初期値(PortSetting画面)
	public static final String PRINTER_TYPE_DEFAULTVALUE             = "B-FP2D";
	public static final String PORTSETTING_PORT_DEFALUTVALUE         = "Bluetooth";   // 0:Bluetooth , 1:WLAN , 2:FILE
	public static final String PORTSETTING_MAC_ADDRESS_DEFALUTVALUE  = "";  //
	public static final String PORTSETTING_IP_ADDRESS_DEFALUTVALUE   = "";
	public static final String PORTSETTING_PORT_NUMBER_DEFALUTVALUE  = "8080";
	public static final String PORTSETTING_FILE_PATH_DEFALUTVALUE    = "";

	//プリファレンスキー名(Communicate画面)
	public static final String COMMUNI_OPENPORT_KEYNAME    = "COPK";
	// 初期値(PortSetting画面)
	public static final int   COMMUNI_OPENPORT_DEFALUTVALUE        = 0;    // 0:Send , 1:Issue


	//プリファレンスキー名(Option画面)
	public static final String OPTION_CONTROLCODE_KEYNAME    = "OCOK";
	public static final String OPTION_RECVTIME_KEYNAME      = "OREK";
	public static final String OPTION_LANGUAGE_KEYNAME      = "OLAK";
	public static final String OPTION_GRAPHICTYPE_KEYNAME    = "OGRK";
	public static final String OPTION_PRINTMODE_KEYNAME    = "OGPM";


	public static final String NFC_SSID_PREFIX    = "SSID://";				//SSID from the NFC Tag
	public static final String NFC_BD_PREFIX  = "BD://";					//Bluetooth address
	public static final String NFC_MAC_PREFIX   = "MAC://";					//MAC address

	//初期値(Option画面)
	public static final int    OPTION_PRINTMODE_DEFALUTVALUE        = 4;    // 4:TPCL
	public static final int    OPTION_CONTROLCODE_DEFALUTVALUE      = 1;    // 0:ESC,LF,NUL  1:{,|,}
	public static final int    OPTION_RECVTIME_DEFALUTVALUE         = 15;   // 15mSec
	public static final int    OPTION_LANGUAGE_DEFALUTVALUE         = 1;    // 0:Japanese  1:English
	public static final int    OPTION_GRAPHICTYPE_DEFALUTVALUE      = 1;    // 0:Nibble 1:Hex 2:Topix


	//プリファレンスキー名(Print画面)
	public static final String PRINT_LABELFORMAT_KEYNAME    = "PLFK";
	public static final String PRINT_PRINTMETHOD_KEYNAME    = "PPMK";

	/**
	 * IssueMode
	 */
	public static final int AsynchronousMode = 1;		// 送信完了復帰（非同期）
	public static final int SynchronousMode = 2;		// 発行完了復帰（同期）

	final public static int PRINTER_LIST_CNT = 12;
	final public static String[] PRINTER_LIST = {
			"B-EP2DL-G",
			"B-EP2DL-G-QM",
			"B-EP4DL-T",
			"B-EP4DL-G",
			"B-EV4-T",
			"B-EV4-G",
			"B-LV4T-T",
			"B-LV4T-G",
			"B-LP2D",
			"B-FP3D",
			//ADD 3-12-2018
			"BA400T-G",
			"BA400T-T",
			"B-FP2D",
			//Amir
			"B-FV4D"
	};

	final public static int PRINTER_NO[] = {
			11,
			13,
			12,
			14,
			18,
			19,
			20,
			21,
			27,
			29,
			//ADD 3-12-2018
			37,
			38,
			28,
			//Amir
			40
	};

	public static int getPrinterNo(String strPrinterType){
		if(strPrinterType == null)return 99;
		for(int i=0; i<PRINTER_LIST_CNT; i++){
			if(strPrinterType.equals(PRINTER_LIST[i])){
				return PRINTER_NO[i];
			}
		}
		return 99;
	}
}
