package com.certify.snap.common;

import android.content.Context;
import android.os.Environment;

import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.enums.DetectMode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class License {
    private static  final String TAG = "License";
    static boolean checkLicense(Context context){
        FaceEngine faceEngine = null;

        try{
            int activationCode = new FaceEngine().init(context, DetectMode.ASF_DETECT_MODE_VIDEO, ConfigUtil.getFtOrient(context),
                    16, 10, FaceEngine.ASF_FACE_DETECT);
            Logger.warn(TAG, "checkLicense activationCode: "+activationCode);
            return (activationCode == ErrorInfo.MOK || activationCode == ErrorInfo.MERR_ASF_ALREADY_ACTIVATED);
        }catch (Exception ex){

        } finally {
            if(faceEngine != null) faceEngine.unInit();
        }
        return false;
    }
    public  static boolean activateLicense(Context context){
        boolean activated = License.checkLicense(context);
        if(!activated){
            copyLicense(context);
            activated = License.checkLicense(context);
        }
        if(!activated){
            activated = ActiveEngine.activeEngineOffline(context);

            if(!activated){
                String serialNumber = Util.getSNCode();
                String activationKey = ActiveEngine.getDeviceList().get(serialNumber);
                Logger.debug(TAG,
                        String.format("checkLicense activeEngineOffline failed, serialNumber: %s, activationKey: %s",
                                serialNumber, activationKey));
                int activationResult = FaceEngine.activeOnline(context, activationKey, Constants.APP_ID, Constants.SDK_KEY);
                activated = activationResult == ErrorInfo.MOK || activationResult == ErrorInfo.MERR_ASF_ALREADY_ACTIVATED;
                Logger.debug(TAG, String.format("doInBackground FaceEngine.activeOnline activationResult: %d", activationResult));
            }
            if(activated) copyLicense(context);
            Util.writeBoolean(Util.getSharedPreferences(context), "activate", activated);

        }
        return activated;
    }
    public static void copyLicense(Context context){
        String licenseFileName = "ArcFacePro32.dat";

        File externalStorageLicense = new File(Environment.getExternalStorageDirectory(), licenseFileName);
        File applicationLicense = new File(context.getFilesDir(), licenseFileName);
        boolean shouldCopyToApplication = !externalStorageLicense.exists() && applicationLicense.exists();
        boolean shouldCopyToExternalStorage = externalStorageLicense.exists() && !applicationLicense.exists();
        Logger.debug(TAG, String.format("copyLicense shouldCopy: %b, folders externalStorage: %s, application: %s",
                shouldCopyToApplication, externalStorageLicense.getPath(), applicationLicense.getPath()));

        if(shouldCopyToApplication){
            copyFiles(applicationLicense, externalStorageLicense);
        }else if(shouldCopyToExternalStorage){
            copyFiles(externalStorageLicense, applicationLicense);
        }

    }
    private static void copyFiles(File in, File out){
        if(in == null || !in.exists()) return;
        try(
                FileInputStream fin = new FileInputStream(in);
                FileOutputStream fout = new FileOutputStream(out);
        ) {
            byte[] buffer = new byte[(int) in.length()];
            fin.read(buffer);
            fout.write(buffer);
            fout.flush();
        } catch (Exception e) {
            Logger.error(TAG, String.format("copyLicense failed: %s", e.getMessage()));
        }

    }

}
