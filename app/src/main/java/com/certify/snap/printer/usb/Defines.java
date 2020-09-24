package com.certify.snap.printer.usb;


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

	final public static int PRINTER_LIST_CNT = 1;
	final public static String[] PRINTER_LIST = {
			"B-FV4D"
	};

	final public static int PRINTER_NO[] = {
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
