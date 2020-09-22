package com.certify.snap.bluetooth.printer.toshiba;


public abstract class Defines {

	public static final String PORTSETTING_PORT_MODE_KEYNAME    = "PPMK";
	public static final String PORTSETTING_IP_ADDRESS_KEYNAME   = "PIAK";
	public static final String PORTSETTING_PORT_NUMBER_KEYNAME  = "PPNK";
	public static final String PORTSETTING_FILE_PATH_KEYNAME    = "PFPK";

	public static final String PRINTER_TYPE_KEYNAME             = "PRINTERTYPE";

	public static final String OPTION_CONTROLCODE_KEYNAME    = "OCOK";
	public static final String OPTION_RECVTIME_KEYNAME      = "OREK";
	public static final String OPTION_LANGUAGE_KEYNAME      = "OLAK";
	public static final String OPTION_GRAPHICTYPE_KEYNAME    = "OGRK";
	public static final String OPTION_PRINTMODE_KEYNAME    = "OGPM";

	public static final int    OPTION_RECVTIME_DEFALUTVALUE         = 15;   // 15mSec

	public static final int AsynchronousMode = 1;
	public static final int SynchronousMode = 2;

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
			"BA400T-G",
			"BA400T-T",
			"B-FP2D",
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
