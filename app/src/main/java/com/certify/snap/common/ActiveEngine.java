package com.certify.snap.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;

import com.arcsoft.face.ActiveFileInfo;
import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.certify.callback.ActiveEngineCallback;
import com.certify.snap.activity.IrCameraActivity;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ActiveEngine {
    public static String TAG = "ActiveEngine -> ";

    public static void activeEngine(final Context context, final SharedPreferences sharedPreferences, final String activityKey, final ActiveEngineCallback activeEngineCallback) {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                int activeCode = FaceEngine.activeOnline(context, activityKey, Constants.APP_ID, Constants.SDK_KEY);
                emitter.onNext(activeCode);
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Integer activeCode) {
                        if (activeCode == ErrorInfo.MOK) {
                            if (activeEngineCallback != null)
                                activeEngineCallback.onActiveEngineCallback(true, "online", null);
//                            Util.showToast(SettingActivity.this,getString(R.string.active_success));
//                            Util.writeBoolean(sp,"activate",true);
//                            show();
                        } else if (activeCode == ErrorInfo.MERR_ASF_ALREADY_ACTIVATED) {
//                            Util.showToast(SettingActivity.this,getString(R.string.already_activated));
                            Logger.verbose(TAG, "activate:", true);
                            Util.writeBoolean(sharedPreferences, "activate", true);
                            if (activeEngineCallback != null)
                                activeEngineCallback.onActiveEngineCallback(true, "online", null);
//                            show();
                        } else {
//                            Util.showToast(SettingActivity.this,getString(R.string.active_failed, activeCode));
                            Util.writeBoolean(sharedPreferences, "activate", false);
                            Logger.verbose(TAG, "activate:", false);
//                            hide();
                            if (activeEngineCallback != null)
                                activeEngineCallback.onActiveEngineCallback(false, "online", null);
                        }


                        ActiveFileInfo activeFileInfo = new ActiveFileInfo();
                        int res = FaceEngine.getActiveFileInfo(context, activeFileInfo);
                        if (res == ErrorInfo.MOK) {
                            if (activeEngineCallback != null)
                                activeEngineCallback.onActiveEngineCallback(true, "online", null);

                            Logger.debug(TAG, activeFileInfo.toString());
                        } else {
                            if (activeEngineCallback != null)
                                activeEngineCallback.onActiveEngineCallback(false, "online", null);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.error(Util.getSNCode() + "onerror engine not activated", e.getMessage());

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public static boolean activeEngineOffline(Context context) {
        boolean result = false;
        String path = Environment.getExternalStorageDirectory() + "/active_result.dat";
        String path1 = Environment.getExternalStorageDirectory() + "/ArcFacePro32.dat";
        String path2 = context.getApplicationContext().getFilesDir() + "/ArcFacePro32.dat";
        Logger.verbose(TAG, "path : ", path);
        Logger.verbose(TAG, "path2 : ", path2);
        File file = new File(path);
        if (file.exists()) {
            int activeCode = FaceEngine.activeOffline(context,
                    path);
            if (activeCode == ErrorInfo.MOK) {
                result = true;
                Log.e("active_result", "true  1");
            } else if (activeCode == ErrorInfo.MERR_ASF_ALREADY_ACTIVATED) {
                result = true;
                Log.e("active_result", "true  2");
            } else {
                result = false;
                Log.e("active_result", "false  1");
            }
        } else {
            File file1 = new File(path1);
            if (file1.exists()) {
                copyFile(path1, path2);
                File file2 = new File(path2);
                if (file2.exists()) {
                    result = true;
                }
            } else {
                Log.e("active_result", "false  no .dat file");
            }
        }

        return result;
    }

    public static boolean copyFile(String filePath, String destPath) {
        File originFile = new File(filePath);

        if (!originFile.exists()) {
            Log.e("yw_lisence", "lisence not exist");
            return false;
        }
        File destFile = new File(destPath);
        BufferedInputStream reader = null;
        BufferedOutputStream writer = null;
        try {
            if (!destFile.exists()) {
                destFile.createNewFile();
            }
            reader = new BufferedInputStream(new FileInputStream(originFile));
            writer = new BufferedOutputStream(new FileOutputStream(destFile));
            byte[] buffer = new byte[1024];
            int length;
            while ((length = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, length);
            }
        } catch (Exception exception) {
            return false;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                }
            }
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                }
            }
        }
        return true;
    }

    public static String readExcelFileFromAssets(Context context, String deviceSno) {
        String activityKey = "";
        // HashMap<String,String> devicesList = new HashMap<>();
        try {
            InputStream myInput;
            // initialize asset manager
            AssetManager assetManager = context.getAssets();
            //  open excel sheet
            myInput = assetManager.open("devices_activation_key.xls");
            // Create a POI File System object
            POIFSFileSystem myFileSystem = new POIFSFileSystem(myInput);
            // Create a workbook using the File System
            HSSFWorkbook myWorkBook = new HSSFWorkbook(myFileSystem);
            // Get the first sheet from workbook
            HSSFSheet mySheet = myWorkBook.getSheetAt(0);
            // We now need something to iterate through the cells.
            Iterator<Row> rowIter = mySheet.rowIterator();
            int rowno = 0;
            while (rowIter.hasNext()) {
                HSSFRow myRow = (HSSFRow) rowIter.next();
                if (rowno != 0) {
                    Iterator<Cell> cellIter = myRow.cellIterator();
                    int colno = 0;
                    String serialNumber = "", ActivationCode = "";
                    while (cellIter.hasNext()) {
                        HSSFCell myCell = (HSSFCell) cellIter.next();
                        if (colno == 0) {
                            serialNumber = myCell.toString();
                        } else if (colno == 1) {
                            ActivationCode = myCell.toString();
                        }
                        colno++;

                    }
                    if (serialNumber.equals(deviceSno)) {
                        Logger.verbose(TAG, "serialNumber", serialNumber + " deviceSno " + deviceSno);
                        return ActivationCode;

                    }
                }
                rowno++;
            }
        } catch (Exception e) {
            Log.e(TAG, "error " + e.toString());
        }
        return activityKey;
    }

    public static Map<String, String> getDeviceList() {
        Logger.debug(TAG, "getDeviceList");
        Map map = new HashMap<String, String>();
        map.put("A040980P02800144", "0858-112B-P3GN-CT4Z");
        map.put("A040980P02800148", "0858-112B-P3HP-UHDH");
        map.put("A040980P02800133", "0858-112B-P3JG-NNXV");
        map.put("A040980P02800137", "0858-112B-P3KT-LTCP");
        map.put("A040980P02800138", "0858-112B-P3L5-GATW");
        map.put("A040980P02800134", "0858-112B-P3MT-MPJW");
        map.put("A040980P02800139", "0858-112B-P3NZ-T213");
        map.put("A040980P02800135", "0858-112B-P3PW-1L92");
        map.put("A040980P02800140", "0858-112B-P3RN-4BR7");
        map.put("A040980P02800136", "0858-112B-P3Q4-V2Y9");
        map.put("A040980P02800153", "0858-112K-A1MD-1AZN");
        map.put("A040980P02800149", "0858-112K-A1NT-7B6H");
        map.put("A040980P02800154", "0858-112K-A1P1-VDAL");
        map.put("A040980P02800124", "0858-112K-A1QQ-R43R");
        map.put("A040980P02800155", "0858-112K-A1RJ-DJUE");
        map.put("A040980P02800214", "0858-112K-A2JB-VJXK");
        map.put("A040980P02800156", "0858-112K-A1TL-ZLGU");
        map.put("A040980P02800150", "0858-112K-A1UQ-N3F4");
        map.put("A040980P02800183", "0858-112K-A24Y-K5QW");
        map.put("A040980P02800184", "0858-112K-A25B-7F2J");
        map.put("A040980P02800185", "0858-112K-A26P-J373");
        map.put("A040980P02800186", "0858-112K-A27P-U68H");
        map.put("A040980P02800187", "0858-112K-A283-4K2E");
        map.put("A040980P02800188", "0858-112K-A29Z-LMUP");
        map.put("A040980P02800253", "0858-112K-A2AU-6V8R");
        map.put("A040980P02800254", "0858-112K-A2BB-5BWD");
        map.put("A040980P02800255", "0858-112K-A2CN-311H");
        map.put("A040980P02800256", "0858-112K-A2DW-5E6D");
        map.put("A040980P02800257", "0858-112K-A2EL-D4GD");
        map.put("A040980P02800258", "0858-112K-A2F6-DCD5");
        map.put("A040980P02800152", "0858-112K-A1VU-EELG");
        map.put("A040980P02800259", "0858-112K-A2GN-H2UX");
        map.put("A040980P02800260", "0858-112K-A2HZ-JRV2");
        map.put("A040980P02800215", "0858-112K-A23Q-9NHM");
        map.put("A040980P02800151", "0858-112K-A1W6-44BY");
        map.put("A040980P02800216", "0858-112K-A22M-MJW5");
        map.put("A040980P02800219", "0858-112K-A21E-RCTQ");
        map.put("A040980P02800218", "0858-112K-A1Z4-QJRG");
        map.put("A040980P02800217", "0858-112K-A1Y2-2XFR");
        map.put("A040980P02800213", "0858-112K-A1X9-MUZZ");
        map.put("A040980P03100037", "0858-112N-645Q-5PNT");
        map.put("A040980P03100038", "0858-112N-646U-ZVGQ");
        map.put("A040980P03100039", "0858-112N-647E-9R35");
        map.put("A040980P03100040", "0858-112N-648C-E3CV");
        map.put("A040980P03100041", "0858-112N-649N-PYXX");
        map.put("A040980P03100042", "0858-112N-64A3-CJRF");
        map.put("A040980P03100043", "0858-112N-64BL-GATX");
        map.put("A040980P03100044", "0858-112N-64CB-PJHV");
        map.put("A040980P03100045", "0858-112N-64DZ-JAPB");
        map.put("A040980P03100046", "0858-112N-64ED-ACQL");
        map.put("A040980P03100047", "0858-112N-64FP-GCN3");
        map.put("A040980P03100048", "0858-112N-64GH-PT1X");
        map.put("A040980P03100049", "0858-112N-64HN-4T8Q");
        map.put("A040980P03100050", "0858-112N-64JD-TQXT");
        map.put("A040980P03100051", "0858-112N-64K9-VDVC");
        map.put("A040980P03100052", "0858-112N-64LN-TJ23");
        map.put("A040980P03100053", "0858-112N-64MP-7YJF");
        map.put("A040980P03100054", "0858-112N-64NU-PCPN");
        map.put("A040980P03100055", "0858-112N-64PG-88EG");
        map.put("A040980P03100056", "0858-112N-64Q6-UVMM");
        map.put("A040980P03100057", "0858-112N-64RM-JHUP");
        map.put("A040980P03100058", "0858-112N-64TV-PVL8");
        map.put("A040980P03100059", "0858-112N-64UA-ZPPX");
        map.put("A040980P03100060", "0858-112N-64VG-LCZP");
        map.put("A040980P03100061", "0858-112N-64WU-BWV7");
        map.put("A040980P03100062", "0858-112N-64X9-U8ZM");
        map.put("A040980P03100063", "0858-112N-64YL-BTDG");
        map.put("A040980P03100064", "0858-112N-64ZJ-WV8X");
        map.put("A040980P03100065", "0858-112N-6514-DG7M");
        map.put("A040980P03100066", "0858-112N-6524-URAG");
        map.put("A040980P03100067", "0858-112N-653K-5M51");
        map.put("A040980P03100068", "0858-112N-654U-L2TU");
        map.put("A040980P03100069", "0858-112N-655L-D1XH");
        map.put("A040980P03100070", "0858-112N-656T-8P3E");
        map.put("A040980P03100071", "0858-112N-657M-QJN8");
        map.put("A040980P03100072", "0858-112N-6589-BK4Y");
        map.put("A040980P03100073", "0858-112N-659K-FWVG");
        map.put("A040980P03100074", "0858-112N-65AU-JLXM");
        map.put("A040980P03100075", "0858-112N-65BU-VUAK");
        map.put("A040980P03100076", "0858-112N-65CJ-89A5");
        map.put("A040980P03100077", "0858-112N-65DE-ED79");
        map.put("A040980P03100078", "0858-112N-65EM-39D8");
        map.put("A040980P03100079", "0858-112N-65FJ-ERVG");
        map.put("A040980P03100080", "0858-112N-65G8-FCPN");
        map.put("A040980P03100081", "0858-112N-65HZ-DW3A");
        map.put("A040980P03100082", "0858-112N-65JT-L3UA");
        map.put("A040980P03100083", "0858-112N-65K7-UMJ8");
        map.put("A040980P03100084", "0858-112N-65LW-XU9Y");
        map.put("A040980P03100085", "0858-112N-65MB-KTME");
        map.put("A040980P03100086", "0858-112N-65NF-Y1M6");
        map.put("A040980P03100087", "0858-112N-65PP-KPHM");
        map.put("A040980P03100088", "0858-112N-65QL-63GH");
        map.put("A040980P03100089", "0858-112N-65R5-YTY2");
        map.put("A040980P03100090", "0858-112N-65TJ-BC1G");
        map.put("A040980P03100091", "0858-112N-65UD-QRLR");
        map.put("A040980P03100092", "0858-112N-65VH-CHLA");
        map.put("A040980P03100093", "0858-112N-65WY-Y3GT");
        map.put("A040980P03100094", "0858-112N-65X5-ZAXP");
        map.put("A040980P03100095", "0858-112N-65YX-EWH5");
        map.put("A040980P03100096", "0858-112N-65ZL-859N");
        map.put("A040980P03100097", "0858-112N-6612-QKC7");
        map.put("A040980P03100098", "0858-112N-662N-XZ14");
        map.put("A040980P03100099", "0858-112N-663F-87GX");
        map.put("A040980P03100100", "0858-112N-664Z-W7G5");
        map.put("A040980P03100101", "0858-112N-665G-H1WH");
        map.put("A040980P03100102", "0858-112N-666M-F3ZT");
        map.put("A040980P03100103", "0858-112N-667B-8DAX");
        map.put("A040980P03100105", "0858-112N-668Q-XFKD");
        map.put("A040980P03100106", "0858-112N-669M-95C5");
        map.put("A040980P03100107", "0858-112N-66AM-QBAR");
        map.put("A040980P03100108", "0858-112N-66BH-DRAR");
        map.put("A040980P03100109", "0858-112N-66CV-5AUY");
        map.put("A040980P03100110", "0858-112N-66DE-6X6J");
        map.put("A040980P03100104", "0858-112N-66ER-J3GR");
        map.put("A040980P03100111", "0858-112N-66FN-TBZP");
        map.put("A040980P03100112", "0858-112N-66G5-4LVN");
        map.put("A040980P03100113", "0858-112N-66H7-8LHJ");
        map.put("A040980P03100114", "0858-112N-66J8-U85F");
        map.put("A040980P03100115", "0858-112N-66KB-U8WB");
        map.put("A040980P03100116", "0858-112N-66L4-6TAF");
        map.put("A040980P03100117", "0858-112N-66M2-HKMW");
        map.put("A040980P03100118", "0858-112N-66NH-6XE6");
        map.put("A040980P03100119", "0858-112N-66PG-MTJM");
        map.put("A040980P03100120", "0858-112N-66QT-2TZU");
        map.put("A040980P03100010", "0858-112N-6724-GX13");
        map.put("A040980P03100011", "0858-112N-6737-G65K");
        map.put("A040980P03100012", "0858-112N-6748-Q3H6");
        map.put("A040980P03100017", "0858-112N-679F-HTVD");
        map.put("A040980P03100018", "0858-112N-67AW-3XE4");
        map.put("A040980P03100019", "0858-112N-67BJ-A74Z");
        map.put("A040980P03100020", "0858-112N-67CD-P8BH");
        map.put("A040980P03100021", "0858-112N-67D5-P9FF");
        map.put("A040980P03100022", "0858-112N-67EV-71GG");
        map.put("A040980P03100023", "0858-112N-67FY-TAHH");
        map.put("A040980P03100024", "0858-112N-67G3-8JA5");
        map.put("A040980P03100025", "0858-112N-67H9-UKJ8");
        map.put("A040980P03100026", "0858-112N-67JF-D1QM");
        map.put("A040980P03100027", "0858-112N-67K6-PWC7");
        map.put("A040980P03100028", "0858-112N-67LP-EPXV");
        map.put("A040980P03100029", "0858-112N-67MF-FBLZ");
        map.put("A040980P03100030", "0858-112N-67N9-N2BG");
        map.put("A040980P03100031", "0858-112N-67PR-YNXH");
        map.put("A040980P03100032", "0858-112N-67QH-3FDN");
        map.put("A040980P03100033", "0858-112N-67RH-JFWN");
        map.put("A040980P03100034", "0858-112N-67T5-7EPR");
        map.put("A040980P03100035", "0858-112N-67UW-VXTT");
        map.put("A040980P03100036", "0858-112N-67V1-HTLA");
        map.put("A050980P03100563", "0858-112P-Z1C2-28RL");
        map.put("A050980P03100564", "0858-112P-Z1BM-UUZR");
        map.put("A050980P03100562", "0858-112P-Z1D5-LBH5");
        map.put("A050980P03100600", "0858-112P-Z4D1-E26A");
        map.put("A050980P03100561", "0858-112P-Z1EN-D3D4");
        map.put("A050980P03100599", "0858-112P-Z4EW-PCXJ");
        map.put("A050980P03100598", "0858-112P-Z4FY-EAM1");
        map.put("A050980P03100589", "0858-112P-Z4KB-37JZ");
        map.put("A050980P03100597", "0858-112P-Z4NX-H3XC");
        map.put("A050980P03100596", "0858-112P-Z4PB-KJM2");
        map.put("A050980P03100593", "0858-112P-Z4Q7-RXA6");
        map.put("A050980P03100592", "0858-112P-Z4RN-QQ38");
        map.put("A050980P03100591", "0858-112P-Z21X-HLAJ");
        map.put("A050980P03100590", "0858-112P-Z222-7Y3H");
        map.put("A050980P03100559", "0858-112P-Z25A-5KGU");
        map.put("A050980P03100560", "0858-112P-Z26Y-9UGW");
        map.put("A050980P03100528", "0858-112P-Z27A-B6AG");
        map.put("A050980P03100558", "0858-112P-Z292-4CTR");
        map.put("A050980P03100557", "0858-112P-Z2A2-AFW6");
        map.put("A050980P03100595", "0858-112P-Z2H5-EC8V");
        map.put("A050980P03100594", "0858-112P-Z2J5-7KVH");
        map.put("A050980P03100567", "0858-112P-Z4XR-8G4R");
        map.put("A050980P03100568", "0858-112P-Z4WX-DUAK");
        map.put("A050980P03100610", "0858-112P-Z4ZJ-9KD1");
        map.put("A050980P03100545", "0858-112P-Z52D-FU77");
        map.put("A050980P03100607", "0858-112P-Z56X-VVKN");
        map.put("A050980P03100613", "0858-112P-Z57Q-LTPB");
        map.put("A050980P03100612", "0858-112P-Z58Q-HLN2");
        map.put("A050980P03100542", "0858-112P-Z5H1-BXDF");
        map.put("A050980P03100511", "0858-112P-Z5NN-N4U8");
        map.put("A050980P03100575", "0858-112P-Z5QD-DJ6H");
        map.put("A050980P03100603", "0858-112P-Z5RH-P4ET");
        map.put("A050980P03100605", "0858-112P-Z5TL-7815");
        map.put("A050980P03100566", "0858-112P-Z5UK-BW2Y");
        map.put("A050980P03100573", "0858-112P-Z61V-G7YU");
        map.put("A050980P03100574", "0858-112P-Z625-N3LV");
        map.put("A050980P03100625", "0858-112P-Z2N8-WDV8");
        map.put("A050980P03100620", "0858-112P-Z2PJ-JR2T");
        map.put("A050980P03100579", "0858-112P-Z64P-F44T");
        map.put("A050980P03100578", "0858-112P-Z66D-FFLD");
        map.put("A050980P03100628", "0858-112P-Z678-ZQ5L");
        map.put("A050980P03100627", "0858-112P-Z68Y-2N5C");
        map.put("A050980P03100631", "0858-112P-Z6CN-EEY5");
        map.put("A050980P03100632", "0858-112P-Z6BD-D8E3");
        map.put("A050980P03100655", "0858-112P-Z6FP-AYRG");
        map.put("A050980P03100616", "0858-112P-Z6RH-EDWA");
        map.put("A050980P03100622", "0858-112P-Z6UE-85VT");
        map.put("A050980P03100621", "0858-112P-Z6V5-HJM1");
        map.put("A050980P03100623", "0858-112P-Z6WA-VUEL");
        map.put("A050980P03100676", "0858-112P-Z744-PAEZ");
        map.put("A050980P03100688", "0858-112P-Z327-JQPF");
        map.put("A050980P03100626", "0858-112P-Z355-JFP7");
        map.put("A050980P03100656", "0858-112P-Z7P1-NM7K");
        map.put("A050980P03100624", "0858-112P-Z7R3-DYP8");
        map.put("A050980P03100602", "0858-112P-Z3HH-JH5F");
        map.put("A050980P03100565", "0858-112P-Z3GW-2W1J");
        map.put("A050980P03100577", "0858-112P-Z3LV-MV6C");
        map.put("A050980P03100668", "0858-112P-Z3Q2-ZM3P");
        map.put("A050980P03100669", "0858-112P-Z417-WL1Z");
        map.put("A050980P03100689", "0858-112P-Z9D9-6QFE");
        return map;
    }
}
