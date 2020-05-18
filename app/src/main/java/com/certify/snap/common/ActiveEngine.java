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

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ActiveEngine {
    public static String TAG = "ActiveEngine -> ";

    public static void activeEngine(final Context context, final SharedPreferences sharedPreferences, final String deviceSno, final ActiveEngineCallback activeEngineCallback) {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                int activeCode = FaceEngine.activeOnline(context, readExcelFileFromAssets(context, deviceSno), Constants.APP_ID, Constants.SDK_KEY);
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
//                            Util.showToast(SettingActivity.this,getString(R.string.active_success));
//                            Util.writeBoolean(sp,"activate",true);
//                            show();
                        } else if (activeCode == ErrorInfo.MERR_ASF_ALREADY_ACTIVATED) {
//                            Util.showToast(SettingActivity.this,getString(R.string.already_activated));
                            Logger.debug("sp---true", "activate:" + true);
                            Util.writeBoolean(sharedPreferences, "activate", true);
                            if (activeEngineCallback != null)
                                activeEngineCallback.onActiveEngineCallback(true, "", null);
//                            show();
                        } else {
//                            Util.showToast(SettingActivity.this,getString(R.string.active_failed, activeCode));
                            Util.writeBoolean(sharedPreferences, "activate", false);
//                            hide();
                            if (activeEngineCallback != null)
                                activeEngineCallback.onActiveEngineCallback(false, "", null);
                        }


                        ActiveFileInfo activeFileInfo = new ActiveFileInfo();
                        int res = FaceEngine.getActiveFileInfo(context, activeFileInfo);
                        if (res == ErrorInfo.MOK) {
                            Logger.debug("activate---", activeFileInfo.toString());
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
        Logger.debug(TAG, "path : " + path);
        Logger.debug(TAG, "path2 : " + path2);
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
                        Logger.debug(TAG, "serialNumber" + serialNumber + " deviceSno " + deviceSno);
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
}
