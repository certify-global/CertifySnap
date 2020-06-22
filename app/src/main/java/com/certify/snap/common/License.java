package com.certify.snap.common;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.enums.DetectMode;
import com.google.gson.Gson;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URL;

public class License {
    private static final String TAG = "License";

    static boolean checkLicense(Context context) {
        FaceEngine faceEngine = null;

        try {
            faceEngine = new FaceEngine();
            int activationCode = faceEngine.init(context, DetectMode.ASF_DETECT_MODE_VIDEO, ConfigUtil.getFtOrient(context),
                    16, 10, FaceEngine.ASF_FACE_DETECT);
            Log.v(TAG, "checkLicense activationCode: " + activationCode);
            return (activationCode == ErrorInfo.MOK || activationCode == ErrorInfo.MERR_ASF_ALREADY_ACTIVATED);
        } catch (Exception ex) {

        } finally {
            if (faceEngine != null) faceEngine.unInit();
        }
        return false;
    }

    //check license:
    //1. if license file exists
    //2. copying from ArcFacePro32.dat from /sdcard to <application folder>
    //3. activating with FaceEngine.activeOffline()
    //3.1 with local active_result.dat on /sdcard
    //3.2 with remote active_result.dat from server
    //TODO: fetching remote license could block.
    public static boolean activateLicense(Context context) {
        boolean activated = License.checkLicense(context);
        if (!activated) {
            activated = activateWithArcFaceFile(context);
        }
        if (!activated) {
            activated = activateWithActiveResultFile(context);
        }

        if (activated) {
            //copy license(ArcFacePro32.dat) back to external storage(/sdcard)
            String licenseFileName = "ArcFacePro32.dat";
            File applicationLicense = new File(context.getFilesDir(), licenseFileName);

            if (applicationLicense.exists()) {
                File externalStorageLicense = new File(Environment.getExternalStorageDirectory(), licenseFileName);
                copyFiles(applicationLicense, externalStorageLicense);
            }
        }
        Logger.debug(TAG, "activateLicense status: " + (activated ? "activated" : "NOT activated"));
        Util.writeBoolean(Util.getSharedPreferences(context), GlobalParameters.LICENSE_ACTIVATED, activated);
        return activated;
    }

    static boolean isServerAvailable() {
        try {
            URL url = URI.create(EndPoints.prod_url).toURL();
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(url.getHost(), url.getPort()), 500);
                return true;
            }

        } catch (Exception e) {
        }
        return false;
    }

    static boolean activateWithActiveResultFile(Context context) {
        boolean activated = false;
        File activeResultPath = new File(Environment.getExternalStorageDirectory(), "/active_result.dat");
        boolean onlineMode = true;
        try {
            onlineMode = Util.getSharedPreferences(context)
                    .getBoolean(GlobalParameters.ONLINE_MODE, true);
        } catch (Exception ex) {
        }
        //check if /sdcard/active_result.dat exists, fetch from server if not
        if (!activeResultPath.exists() && onlineMode) {
            writeRemoteLicenseToExternalStorage(activeResultPath);
        }
        if (activeResultPath.exists()) {
            activated = activateOffline(context, activeResultPath.getAbsolutePath());
        }
        return activated;
    }

    private static void writeRemoteLicenseToExternalStorage(File activeResultPath) {
        String url = Util.getSharedPreferences(Application.getInstance().getApplicationContext())
                .getString(GlobalParameters.URL, EndPoints.prod_url);
        String activeResultDat = getLicenseRemote(url, Util.getSNCode());
        if (activeResultDat != null && !activeResultDat.isEmpty()) {
            writeToFile(activeResultPath, activeResultDat);
        }
    }

    static boolean activateWithArcFaceFile(Context context) {
        boolean activated = false;
        String licenseFileName = "ArcFacePro32.dat";
        File externalStorageLicense = new File(Environment.getExternalStorageDirectory(), licenseFileName);
        if (externalStorageLicense.exists()) {
            File applicationLicense = new File(context.getFilesDir(), licenseFileName);
            copyFiles(externalStorageLicense, applicationLicense);
            activated = License.checkLicense(context);
        }
        return activated;
    }

    private static void writeToFile(File activeResultPath, String activeResultDat) {
        try (OutputStream out = new FileOutputStream(activeResultPath)) {
            out.write(activeResultDat.getBytes());
        } catch (Exception e) {
            Log.w(TAG, "writeToFile " + e.getMessage());
        }
    }

    private static boolean activateOffline(Context context, String activeResultPath) {

        int activeCode = FaceEngine.activeOffline(context, activeResultPath);
        return (activeCode == ErrorInfo.MOK || activeCode == ErrorInfo.MERR_ASF_ALREADY_ACTIVATED);
    }

    public static String getLicenseRemote(String url, String deviceSN) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url + "/GetFaceLicenceInfo?deviceSN=" + deviceSN)
                .build();
        try (Response resp = client.newCall(request).execute()) {
            if (!resp.isSuccessful()) return "";

            String body = resp.body().string();
            Log.v(TAG, "getLicenseRemote " + body);
            LicenseResponse licenseResponse = new Gson().fromJson(body, LicenseResponse.class);
            if (licenseResponse.responseCode != 1 || licenseResponse.responseSubCode != 0) {
                return "";
            }

            return licenseResponse.responseData.faceLicenceInfo;
        } catch (Exception ex) {
            Log.w(TAG, "getLicenseRemote " + ex.getMessage());
            //TODO: return error / log
        }
        return "";
    }

    public static class LicenseResponse extends ManageMemberHelper.SnapResponse {
        public LicenseData responseData;
    }

    public static class LicenseData {
        public String deviceSN;
        public String activationKey;
        public String faceLicenceInfo;
    }

    //copy license from external storage(/sdcard) to application folder
    public static void copyLicense(Context context) {
        String licenseFileName = "ArcFacePro32.dat";

        File externalStorageLicense = new File(Environment.getExternalStorageDirectory(), licenseFileName);
        File applicationLicense = new File(context.getFilesDir(), licenseFileName);
        boolean shouldCopyToApplication = externalStorageLicense.exists() && !applicationLicense.exists();
        Logger.debug(TAG, String.format("copyLicense shouldCopyToApplication: %b, folders externalStorage: %s, application: %s",
                shouldCopyToApplication, externalStorageLicense.getPath(), applicationLicense.getPath()));

        if (shouldCopyToApplication) {
            copyFiles(externalStorageLicense, applicationLicense);
        }

    }

    public static void copyFiles(File in, File out) {
        if (in == null || !in.exists()) return;
        try (
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
