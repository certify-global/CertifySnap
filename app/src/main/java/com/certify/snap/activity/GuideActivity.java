package com.certify.snap.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.certify.callback.JSONObjectCallback;
import com.certify.callback.SettingCallback;
import com.certify.snap.R;
import com.certify.snap.bluetooth.bleCommunication.BluetoothLeService;
import com.certify.snap.common.AppSettings;
import com.certify.snap.common.Application;
import com.certify.snap.common.Constants;
import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.License;
import com.certify.snap.localserver.LocalServer;
import com.certify.snap.common.Logger;
import com.certify.snap.common.Util;
import com.certify.snap.controller.ApplicationController;
import com.certify.snap.controller.BLEController;
import com.certify.snap.controller.CameraController;
import com.certify.snap.faceserver.FaceServer;
import com.certify.snap.localserver.LocalServerController;
import com.certify.snap.localserver.LocalServerTask;
import com.certify.snap.model.AppStatusInfo;
import com.certify.snap.service.DeviceHealthService;
import com.certify.snap.service.MemberSyncService;
import com.certify.snap.service.ResetOfflineDataReceiver;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.microsoft.appcenter.AppCenter;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.Timer;
import java.util.TimerTask;

public class GuideActivity extends Activity implements SettingCallback, JSONObjectCallback {

    public static final String TAG = GuideActivity.class.getSimpleName();
    public static Activity mActivity;
    private ImageView imgPic;
    private Animation myAnimation;
    private SharedPreferences sharedPreferences;
    private boolean onlineMode = true;
    private Timer mActivationTimer;
    private CountDownTimer startUpCountDownTimer;
    private long remainingTime = 20;
    private ImageView internetIndicatorImage;
    public LocalServer localServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.guide);

        mActivity = this;
        ApplicationController.getInstance().initThermalUtil(this);
        Application.getInstance().addActivity(this);
        Util.setTokenRequestName("");
        sharedPreferences = Util.getSharedPreferences(this);
        TextView tvVersion = findViewById(R.id.tv_version_guide);
        tvVersion.setText(Util.getVersionBuild());

        Util.enableLedPower(0);

        Intent intent = getIntent();
        String value = intent.getStringExtra("DEVICE_BOOT");

        //Delay of 1 second for the Thermal module to get initialized
        new Handler().postDelayed(() -> {
            CameraController.getInstance().initDeviceMode();
            if (value != null && value.equals("BootCompleted")) {
                ApplicationController.getInstance().setDeviceBoot(true);
                if (Util.isDeviceProModel() && ApplicationController.getInstance().isProDeviceStartScannerTimer(sharedPreferences)) {
                    startProDeviceInitTimer();
                    return;
                }
            }
            initApp();
        }, 1000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        updateAppStatusInfo("APPCLOSED", AppStatusInfo.APP_CLOSED);
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
        cancelActivationTimer();
        if (startUpCountDownTimer != null) {
            startUpCountDownTimer.cancel();
        }
        ApplicationController.getInstance().releaseThermalUtil();
        if (localServer != null){
            localServer.stopServer();
        }
        ApplicationController.getInstance().setDeviceBoot(false);
    }

    private void checkStatus() {
        checkPermission();
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
            startLocalServer();
            Util.switchRgbOrIrActivity(GuideActivity.this, true);
        } else {
            //TODO: This dialog is required when the connection fails to API server
            //Util.openDialogactivate(this, getString(R.string.onlinemode_nointernet), "guide");

            //If the network is off still launch the IRActivity and allow temperature scan in offline mode
            if (Util.isNetworkOff(GuideActivity.this)) {
                startBLEService();
                new Handler(Looper.getMainLooper()).postDelayed(() -> Util.switchRgbOrIrActivity(GuideActivity.this, true), 2 * 1000);
                return;
            }
            Util.activateApplication(GuideActivity.this, GuideActivity.this);
            startActivationTimer();
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
            cancelActivationTimer();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Util.getTokenActivate(reportInfo, status, GuideActivity.this, "guide");
            }
            startHealthCheckService();
        } catch (Exception e) {
            Util.switchRgbOrIrActivity(GuideActivity.this, true);
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
                    if (sharedPreferences.getBoolean(GlobalParameters.SYNC_ONLINE_MEMBERS, false))
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
        AppSettings.getInstance().getSettingsFromSharedPref(GuideActivity.this);
        if (Util.getTokenRequestName().equalsIgnoreCase("guide")) {
            Util.switchRgbOrIrActivity(this, true);
            Util.setTokenRequestName("");
        }
        initNavigationBar();
        startMemberSyncService();
        startBLEService();
        updateAppStatusInfo("DEVICESETTINGS", AppStatusInfo.DEVICE_SETTINGS);
    }

    private void initNavigationBar() {
        if (sharedPreferences != null && sharedPreferences.getBoolean(GlobalParameters.NavigationBar, true)) {
            sendBroadcast(new Intent(GlobalParameters.ACTION_SHOW_NAVIGATIONBAR));
            sendBroadcast(new Intent(GlobalParameters.ACTION_OPEN_STATUSBAR));
        } else {
            sendBroadcast(new Intent(GlobalParameters.ACTION_HIDE_NAVIGATIONBAR));
            sendBroadcast(new Intent(GlobalParameters.ACTION_CLOSE_STATUSBAR));
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

    private void startActivationTimer() {
        cancelActivationTimer();
        mActivationTimer = new Timer();
        mActivationTimer.schedule(new TimerTask() {
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(GuideActivity.this, "Activate Application Request Error!", Toast.LENGTH_SHORT).show();
                        Util.switchRgbOrIrActivity(GuideActivity.this, true);
                    }
                });
                this.cancel();
            }
        }, 10 * 1000);
    }

    private void cancelActivationTimer() {
       if (mActivationTimer != null) {
           mActivationTimer.cancel();
       }
    }

    private void initAppStatusInfo(){
        AppStatusInfo.getInstance().clear();
        updateAppStatusInfo("APPSTARTED", AppStatusInfo.APP_STARTED);
    }

    private void updateAppStatusInfo(String key, String message) {
        if (message.equals(AppStatusInfo.APP_STARTED))
            AppStatusInfo.getInstance().setAppStarted(message);
        else if (message.equals(AppStatusInfo.APP_CLOSED))
            AppStatusInfo.getInstance().setAppClosed(message);
        else if(message.equals(AppStatusInfo.DEVICE_SETTINGS))
            AppStatusInfo.getInstance().setDeviceSettings(message);
        Logger.debug(TAG, key, message);
    }

    private void initApp() {
        runOnUiThread(() -> {
            initAppStatusInfo();
            try {
                Util.createAudioDirectory();
            } catch (IOException e) {
                e.printStackTrace();
            }
            AppSettings.getInstance().getSettingsFromSharedPref(GuideActivity.this);
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
                    Util.writeString(sharedPreferences,GlobalParameters.Firebase_Token,token);
                    Logger.verbose(TAG,"firebase token",token);

                }
            });
            internetIndicatorImage = findViewById(R.id.img_internet_indicator);
            try {
                onlineMode = sharedPreferences.getBoolean(GlobalParameters.ONLINE_MODE, true);
            } catch (Exception ex) {
                Logger.error(TAG, "onCreate()", "Error in reading Online mode setting from SharedPreferences" + ex.getMessage());
            }
            AppCenter.setEnabled(onlineMode);
            Logger.verbose(TAG, "onCreate() Online mode value is onCreate onlineMode: %b", onlineMode);

            myAnimation = AnimationUtils.loadAnimation(GuideActivity.this, R.anim.alpha);
            imgPic = findViewById(R.id.img_telpo);
            imgPic.startAnimation(myAnimation);
            checkStatus();
            boolean navigationBar = Util.getSharedPreferences(GuideActivity.this).getBoolean(GlobalParameters.NavigationBar, true);
            boolean statusBar = Util.getSharedPreferences(GuideActivity.this).getBoolean(GlobalParameters.StatusBar, true);

            sendBroadcast(new Intent(navigationBar ? GlobalParameters.ACTION_SHOW_NAVIGATIONBAR : GlobalParameters.ACTION_HIDE_NAVIGATIONBAR));
            sendBroadcast(new Intent(statusBar ? GlobalParameters.ACTION_OPEN_STATUSBAR : GlobalParameters.ACTION_CLOSE_STATUSBAR));

            if (!Util.isNetworkOff(GuideActivity.this) && sharedPreferences.getBoolean(GlobalParameters.Internet_Indicator, true)){
                internetIndicatorImage.setVisibility(View.GONE);
            } else {
                internetIndicatorImage.setVisibility(View.VISIBLE);
            }

            ResetOfflineDataReceiver br = new ResetOfflineDataReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Intent.ACTION_DATE_CHANGED);
            intentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
            this.registerReceiver(br, intentFilter);
        });
        Util.writeString(sharedPreferences, GlobalParameters.APP_LAUNCH_TIME, String.valueOf(System.currentTimeMillis()));
    }

    private void startProDeviceInitTimer() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setButton("OK", (dialog, which) -> {
            startUpCountDownTimer.cancel();
            progressDialog.dismiss();
            CameraController.getInstance().setScannerRemainingTime(remainingTime);
            initApp();
        });
        startUpCountDownTimer = new CountDownTimer(Constants.PRO_SCANNER_INIT_TIME, Constants.PRO_SCANNER_INIT_INTERVAL) {
            @Override
            public void onTick(long remTime) {
                remainingTime = ((remTime/1000)/60);
                progressDialog.setMessage(String.format(getString(R.string.scanner_time_msg), remainingTime));
            }

            @Override
            public void onFinish() {
                startUpCountDownTimer.cancel();
                progressDialog.dismiss();
                ApplicationController.getInstance().setProDeviceBootTime(sharedPreferences, Util.currentDate());
                initApp();
            }
        };
        startUpCountDownTimer.start();
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void startLocalServer() {
        if (sharedPreferences.getBoolean(GlobalParameters.LOCAL_SERVER_SETTINGS, false)
            && !sharedPreferences.getBoolean(GlobalParameters.ONLINE_MODE, true)) {
            localServer = new LocalServer(this);
            new LocalServerTask(localServer).execute();
        }
    }
}
