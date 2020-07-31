package com.certify.snap.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.VersionInfo;
import com.certify.callback.JSONObjectCallback;
import com.certify.callback.SettingCallback;
import com.certify.snap.R;
import com.certify.snap.bluetooth.bleCommunication.BluetoothLeService;
import com.certify.snap.common.AppSettings;
import com.certify.snap.common.Application;
import com.certify.snap.common.SnapLocalServer;
import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.License;
import com.certify.snap.common.Logger;
import com.certify.snap.common.Util;
import com.certify.snap.controller.BLEController;
import com.certify.snap.controller.CameraController;
import com.certify.snap.faceserver.FaceServer;
import com.certify.snap.service.DeviceHealthService;
import com.certify.snap.service.MemberSyncService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.gson.Gson;
import com.microsoft.appcenter.AppCenter;
import com.tamic.novate.Novate;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class GuideActivity extends Activity implements SettingCallback, JSONObjectCallback {

    public static final String TAG = GuideActivity.class.getSimpleName();
    public static Activity mActivity;
    private ImageView imgPic;
    private Animation myAnimation;
    private FaceEngine faceEngine = new FaceEngine();
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
    private ImageView internetIndicatorImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.guide);
        try {
            Util.createAudioDirectory();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WindowManager.LayoutParams attributes = getWindow().getAttributes();
            attributes.systemUiVisibility = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            getWindow().setAttributes(attributes);
        }
        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                if (!task.isSuccessful()) {
                    Log.w(TAG, "getInstanceId failed", task.getException());
                    return;
                }

                // Get new Instance ID token
                String token = task.getResult().getToken();
                Util.writeString(sharedPreferences, GlobalParameters.Firebase_Token, token);
                Logger.debug(TAG, "firebase token", token);

            }
        });

        new ServerCall().execute();

        mActivity = this;
        Application.getInstance().addActivity(this);
        Util.setTokenRequestName("");
        sharedPreferences = Util.getSharedPreferences(this);
        TextView tvVersion = findViewById(R.id.tv_version_guide);
        tvVersion.setText(Util.getVersionBuild());
        internetIndicatorImage = findViewById(R.id.img_internet_indicator);
        try {
            onlineMode = sharedPreferences.getBoolean(GlobalParameters.ONLINE_MODE, true);
        } catch (Exception ex) {
            Logger.error(TAG, "onCreate()", "Error in reading Online mode setting from SharedPreferences" + ex.getMessage());
        }
        AppCenter.setEnabled(onlineMode);
        Logger.verbose(TAG, "onCreate() Online mode value is onCreate onlineMode: %b", onlineMode);

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

        if (!Util.isNetworkOff(GuideActivity.this) && sharedPreferences.getBoolean(GlobalParameters.Internet_Indicator, true)) {
            internetIndicatorImage.setVisibility(View.GONE);
        } else {
            internetIndicatorImage.setVisibility(View.VISIBLE);
        }
        AppSettings.getInstance().getSettingsFromSharedPref(GuideActivity.this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            FaceServer.getInstance().unInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            unbindService(BLEController.getInstance().mServiceConnection);
            BLEController.getInstance().mServiceConnection = null;
        } catch (Exception e) {
            Log.e(TAG, "BLE unbind Error");
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
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (!License.activateLicense(GuideActivity.this)) {
                    String message = getResources().getString(R.string.active_failed);
                    Log.e(TAG, message);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Util.openDialogactivate(GuideActivity.this, message, "");
                        }
                    });
                } else if (!onlineMode) {
                    //startActivity(new Intent(GuideActivity.this, IrCameraActivity.class));
                    Util.switchRgbOrIrActivity(GuideActivity.this, true);
                } else {
                    //TODO: This dialog is required when the connection fails to API server
                    //Util.openDialogactivate(this, getString(R.string.onlinemode_nointernet), "guide");

                    //If the network is off still launch the IRActivity and allow temperature scan in offline mode
                    if (Util.isNetworkOff(GuideActivity.this)) {
                        startBLEService();
                        new Handler(Looper.getMainLooper()).postDelayed(() -> Util.switchRgbOrIrActivity(GuideActivity.this, true), 1000);
                        return;
                    }
                    Util.activateApplication(GuideActivity.this, GuideActivity.this);
                  //  Util.switchRgbOrIrActivity(GuideActivity.this, true);

                }

            }
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    public void onJSONObjectListenerSetting(JSONObject reportInfo, String status, JSONObject req) {
        try {
            if (reportInfo == null) {
                onSettingsUpdated();
                return;
            }
            Util.retrieveSetting(reportInfo, GuideActivity.this);
            onSettingsUpdated();
        } catch (Exception e) {
            onSettingsUpdated();
            Logger.error(TAG, "onJSONObjectListenerSetting()", "Exception while processing API response callback" + e.getMessage());
        }

    }

    @Override
    public void onJSONObjectListener(String reportInfo, String status, JSONObject req) {
        try {
            if (reportInfo == null) {
                return;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Util.getTokenActivate(reportInfo, status, GuideActivity.this, "guide");
            }
            startHealthCheckService();
        } catch (Exception e) {
            Logger.error(TAG, "onJSONObjectListener()", "Exception occurred while processing API response callback with Token activate" + e.getMessage());
        }

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
        } catch (Exception e) {
            e.printStackTrace();
            Logger.error(TAG, "initHealthCheckService()", "Exception occurred in starting DeviceHealth Service" + e.getMessage());
        }
    }

    private void startMemberSyncService() {
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                if (!Util.isServiceRunning(MemberSyncService.class, GuideActivity.this) && (sharedPreferences.getBoolean(GlobalParameters.FACIAL_DETECT, true)
                        || sharedPreferences.getBoolean(GlobalParameters.RFID_ENABLE, false))) {
                    if (!sharedPreferences.getBoolean(GlobalParameters.MEMBER_SYNC_DO_NOT, false))
                        startService(new Intent(GuideActivity.this, MemberSyncService.class));
                    Application.StartService(GuideActivity.this);
                }
            }
        }, 100);
    }

    /**
     * Method that processes next steps after the App settings are updated in the SharedPref on app launch
     */
    private void onSettingsUpdated() {
        CameraController.getInstance().initDeviceMode();
        if (Util.getTokenRequestName().equalsIgnoreCase("guide")) {
            Util.switchRgbOrIrActivity(this, true);
            Util.setTokenRequestName("");
        }
        initNavigationBar();
        startMemberSyncService();
        startBLEService();
    }

    private void initNavigationBar() {
        if (sharedPreferences != null && sharedPreferences.getBoolean(GlobalParameters.NavigationBar, true)) {
            sendBroadcast(new Intent(GlobalParameters.ACTION_SHOW_NAVIGATIONBAR));
        } else {
            sendBroadcast(new Intent(GlobalParameters.ACTION_HIDE_NAVIGATIONBAR));
        }
    }

    private void startBLEService() {
        try {
            if (AppSettings.isBleLightNormalTemperature() || AppSettings.isBleLightHighTemperature()) {
                if (!Util.isServiceRunning(BluetoothLeService.class, GuideActivity.this)) {
                    Log.d(TAG, "startBLEService");
                    BLEController.getInstance().initServiceConnection();
                    // connection ble service
                    Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
                    bindService(gattServiceIntent, BLEController.getInstance().mServiceConnection, BIND_AUTO_CREATE);

                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "startBLEService: exception" + e.toString());
        }
    }

    private class ServerCall extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String[] params) {
            SnapLocalServer snapLocalServer = new SnapLocalServer();
            try {
                snapLocalServer.main(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String message) {
        }
    }
}
