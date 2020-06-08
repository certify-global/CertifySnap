package com.certify.snap.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.VersionInfo;
import com.certify.callback.ActiveEngineCallback;
import com.certify.callback.JSONObjectCallback;
import com.certify.callback.SettingCallback;
import com.certify.snap.R;
import com.certify.snap.BuildConfig;
import com.certify.snap.async.AsyncActiveEngine;
import com.certify.snap.common.ActiveEngine;
import com.certify.snap.common.Application;
import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.License;
import com.certify.snap.common.Logger;
import com.certify.snap.common.Util;
import com.certify.snap.faceserver.FaceServer;
import com.certify.snap.service.DeviceHealthService;
import com.certify.snap.service.MemberSyncService;
import com.google.gson.Gson;
import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.crashes.Crashes;
import com.romainpiel.titanic.library.Titanic;
import com.romainpiel.titanic.library.TitanicTextView;
import com.tamic.novate.Novate;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GuideActivity extends Activity implements SettingCallback, JSONObjectCallback, ActiveEngineCallback {

    public static final String TAG = GuideActivity.class.getSimpleName();
    public static Activity mActivity;
    private ImageView imgPic;
    private Animation myAnimation;
    private FaceEngine faceEngine = new FaceEngine();
    private Titanic titanic;
    private TitanicTextView TitanicTextView;
    private Novate mnovate;
    HashMap<String, String> map = new HashMap<String, String>();
    Gson gson = new Gson();
    private boolean isRunService = false;
    private SharedPreferences sharedPreferences;
    private boolean onlineMode = true;
    boolean libraryExists = true;
    // Demo
    private static final String[] LIBRARIES = new String[]{
            "libarcsoft_face_engine.so",
            "libarcsoft_face.so",
            "libarcsoft_image_util.so",
    };

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.guide);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WindowManager.LayoutParams attributes = getWindow().getAttributes();
            attributes.systemUiVisibility = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            getWindow().setAttributes(attributes);
        }
        mActivity = this;
        Application.getInstance().addActivity(this);
        sharedPreferences = Util.getSharedPreferences(this);
        TextView tvVersion = findViewById(R.id.tv_version_guide);
        tvVersion.setText(Util.getVersionBuild());
        try {
            onlineMode = sharedPreferences.getBoolean(GlobalParameters.ONLINE_MODE, true);
        } catch (Exception ex) {
            Logger.error(TAG, "onCreate()", "Error in reading Online mode setting from SharedPreferences" + ex.getMessage());
        }
        AppCenter.setEnabled(onlineMode);
        Logger.debug(TAG, "onCreate()", "Online mode value is " + String.format("onCreate onlineMode: %b", onlineMode));

        //  Util.activateApplication(this, this);

        if (onlineMode) {
            Util.activateApplication(this, this);
        }
        if (!isInstalled(GuideActivity.this, "com.telpo.temperatureservice")) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //  Util.showToast(GuideActivity.this,getString(R.string.toast_tempservice_notinstall));
                }
            });

        }

        myAnimation = AnimationUtils.loadAnimation(this, R.anim.alpha);
        imgPic = findViewById(R.id.img_telpo);
        imgPic.startAnimation(myAnimation);
        checkStatus();
        boolean navigationBar = Util.getSharedPreferences(GuideActivity.this).getBoolean(GlobalParameters.NavigationBar, true);
        boolean statusBar = Util.getSharedPreferences(GuideActivity.this).getBoolean(GlobalParameters.StatusBar, true);

        sendBroadcast(new Intent(navigationBar ? GlobalParameters.ACTION_SHOW_NAVIGATIONBAR : GlobalParameters.ACTION_HIDE_NAVIGATIONBAR));
        sendBroadcast(new Intent(statusBar ? GlobalParameters.ACTION_OPEN_STATUSBAR : GlobalParameters.ACTION_CLOSE_STATUSBAR));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (titanic != null) titanic.cancel();

        try {
            FaceServer.getInstance().unInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isInstalled(Context context, String packageName) {
        final PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);
        for (int i = 0; i < pinfo.size(); i++) {
            if (pinfo.get(i).packageName.equalsIgnoreCase(packageName))
                return true;
        }
        return false;
    }

    private void checkStatus() {
        checkPermission();
        libraryExists = checkSoFile(LIBRARIES);
        ApplicationInfo applicationInfo = getApplicationInfo();
        Log.e(TAG, "onCreate: " + applicationInfo.nativeLibraryDir);
        if (!libraryExists) {
//            Toast.makeText(this,getString(R.string.library_not_found),Toast.LENGTH_SHORT).show();
//            finish();
        } else {
            VersionInfo versionInfo = new VersionInfo();
            int code = FaceEngine.getVersion(versionInfo);
            Log.e(TAG, "onCreate: getVersion, code is: " + code + ", versionInfo is: " + versionInfo);
        }
    }

    /**
     * @param libraries
     * @return
     */
    private boolean checkSoFile(String[] libraries) {
        File dir = new File(getApplicationInfo().nativeLibraryDir);
        File[] files = dir.listFiles();
        if (files == null || files.length == 0) {
            return false;
        }
        List<String> libraryNameList = new ArrayList<>();
        for (File file : files) {
            libraryNameList.add(file.getName());
        }
        boolean exists = true;
        for (String library : libraries) {
            exists &= libraryNameList.contains(library);
        }
        return exists;
    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE}, 1000);
            } else {
                start();
            }
        }
    }

    /**
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1000) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                start();
            } else {
                Util.showToast(GuideActivity.this, getString(R.string.toast_guide_permission));
                finish();
            }
        }
    }

    private void start() {
        if (!License.activateLicense(this)) {
            String message = getResources().getString(R.string.active_failed);
            Logger.error(TAG, message);
            //TODO: alternate license activation
            Util.openDialogactivate(this, message, "");
        }else if (!onlineMode) {
            startActivity(new Intent(this, IrCameraActivity.class));

        } else {
            Util.openDialogactivate(this, getString(R.string.onlinemode_nointernet), "guide");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    public void onJSONObjectListenerSetting(JSONObject reportInfo, String status, JSONObject req) {
        try {
            if (reportInfo == null) {
                return;
            }
            Util.retrieveSetting(reportInfo, GuideActivity.this);

        } catch (Exception e) {
            Logger.error(TAG, "onJSONObjectListenerSetting()", "Exception while processing API response callback" + e.getMessage());
        }

    }

    @Override
    public void onJSONObjectListener(String reportInfo, String status, JSONObject req) {
        try {
            if (reportInfo == null) {
                return;
            }
            Util.getTokenActivate(reportInfo, status, GuideActivity.this, "guide");
            startHealthCheckService();
        } catch (Exception e) {
            Logger.error(TAG, "onJSONObjectListener()", "Exception occurred while processing API response callback with Token activate" + e.getMessage());
        }

    }

    @Override
    public void onActiveEngineCallback(Boolean activeStatus, String status, JSONObject req) {
        Logger.debug(TAG, "onActiveEngineCallback()", "Active status:" + activeStatus);
        if (activeStatus) {
            License.copyLicense(getApplicationContext());
            Util.switchRgbOrIrActivity(GuideActivity.this, true);
        } else if ("Offline".equals(status)) {
            String activityKey = ActiveEngine.readExcelFileFromAssets(GuideActivity.this, Util.getSNCode());
            ActiveEngine.activeEngine(GuideActivity.this, sharedPreferences, activityKey, GuideActivity.this);
        } else
            Toast.makeText(GuideActivity.this, getResources().getString(R.string.active_failed), Toast.LENGTH_LONG).show();
    }

    /**
     * Method that initiates the HealthCheck service if not started
     */
    private void startHealthCheckService() {
        try {
            if (sharedPreferences.getBoolean(GlobalParameters.ONLINE_MODE, true))
                if (!Util.isServiceRunning(DeviceHealthService.class, this)) {
                    startService(new Intent(this, DeviceHealthService.class));
                    Application.StartService(this);
                }
            if (!Util.isServiceRunning(MemberSyncService.class, this)) {
                startService(new Intent(this, MemberSyncService.class));
                Application.StartService(this);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Logger.error(TAG, "initHealthCheckService()", "Exception occurred in starting DeviceHealth Service" + e.getMessage());
        }
    }
}
