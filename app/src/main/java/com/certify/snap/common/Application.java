package com.certify.snap.common;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.SystemClock;
import android.util.Log;

import com.certify.snap.BuildConfig;
import com.certify.snap.activity.ConnectivityStatusActivity;
import com.certify.snap.bluetooth.data.SimplePreference;
import com.certify.snap.controller.ApplicationController;
import com.certify.snap.controller.DatabaseController;
import com.certify.snap.service.AlarmReceiver;
import com.certify.snap.service.DeviceHealthJobService;
import com.certify.snap.service.DeviceHealthService;
import com.certify.snap.service.LoggerService;
import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.crashes.AbstractCrashesListener;
import com.microsoft.appcenter.crashes.Crashes;
import com.microsoft.appcenter.crashes.ingestion.models.ErrorAttachmentLog;
import com.microsoft.appcenter.crashes.model.ErrorReport;
import com.tamic.novate.Novate;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import okhttp3.logging.HttpLoggingInterceptor;

/**
 * Created by tsy on 2016/12/6.
 */
public class Application extends android.app.Application {
    private static final String TAG = Application.class.getSimpleName();
    private static Application mInstance;
    public static int REQUEST_CODE_HEALTH = 111;
    private Novate novate;
    //    private MyOkHttp mMyOkHttp;
    // private DownloadMgr mDownloadMgr;
    public static boolean member = false;
    private static SimplePreference preference;
    private int deviceMode = 0;
    WifiManager wifi;
    SharedPreferences sharedPreferences;

    @Override
    public void onCreate() {
        super.onCreate();
        Logger.debug(TAG, "onCreate");
        sharedPreferences = Util.getSharedPreferences(this);
        if (sharedPreferences != null && !sharedPreferences.getBoolean(GlobalParameters.CLEAR_SHARED_PREF, false)) {
            ApplicationController.getInstance().clearSharedPrefData(this);
        }
        //validateDB();
        String password = getPragmaKey(this);
        DatabaseController.getInstance().init(this, password);
        preference = new SimplePreference(this);

        mInstance = this;

        novate = new Novate.Builder(this).baseUrl(GlobalParameters.BASEURL).addLog(true)
                .connectTimeout(20).writeTimeout(15).build();

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        if (BuildConfig.BUILD_TYPE != "debug") {
            initAppCenter();
        }

        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(this);

        ApplicationLifecycleHandler handler = new ApplicationLifecycleHandler();
        registerActivityLifecycleCallbacks(handler);
        registerComponentCallbacks(handler);
        scheduleJobHealth();
    }

    public static SimplePreference getPreference() {
        return preference;
    }

    public static synchronized Application getInstance() {
        return mInstance;
    }

    public Novate getNovate() {
        return novate;
    }


    // Activity
    public void addActivity(Activity activity) {
        // activityList.add(activity);
    }

    public static void StartService(Context context) {
        Logger.debug(TAG, "StartService - AlarmReceiver");
        Intent myIntent = new Intent(context, AlarmReceiver.class);
        PendingIntent restartServicePendingIntent = PendingIntent.getBroadcast(context, 0, myIntent, 0);
        // PendingIntent restartServicePendingIntent = PendingIntent.getService(context, 1, new Intent(this, BackgroundSyncService.class), PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmService = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 9);
        calendar.set(Calendar.MINUTE, 20);
        calendar.set(Calendar.SECOND, 0);

        if (alarmService != null)
            alarmService.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 24 * 60 * 60 * 1000, restartServicePendingIntent);

        Calendar calendar2 = Calendar.getInstance();
        calendar2.set(Calendar.HOUR_OF_DAY, 15);
        calendar2.set(Calendar.MINUTE, 0);
        calendar2.set(Calendar.SECOND, 0);
        if (alarmService != null)
            alarmService.setRepeating(AlarmManager.RTC_WAKEUP, calendar2.getTimeInMillis(), 24 * 60 * 60 * 1000, restartServicePendingIntent);
    }

    public void runDeviceService() {
        Logger.debug(TAG, "runDeviceService");
        Intent myIntent = new Intent(getApplicationContext(), DeviceHealthService.class);
        startService(myIntent);
        PendingIntent restartServicePendingIntent = PendingIntent.getService(getApplicationContext(), REQUEST_CODE_HEALTH, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        if (alarmService == null) {
            Logger.error(TAG, "AlarmManager not available");
        }
        alarmService.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), 10 * 60 * 1000, restartServicePendingIntent);

    }

    public void scheduleJobHealth() {
        try {
            int timeDelay = 19 * 60 * 1000;
            ComponentName componentName = new ComponentName(getApplicationContext(), DeviceHealthJobService.class);
            @SuppressLint("MissingPermission") JobInfo jobInfo = new JobInfo.Builder(11, componentName)
                    .setPersisted(true)
                    .setPeriodic(20 * 60 * 1000, timeDelay)
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY).build();
            JobScheduler jobScheduler = (JobScheduler) getApplicationContext().getSystemService(Context.JOB_SCHEDULER_SERVICE);
            jobScheduler.schedule(jobInfo);
        } catch (Exception e) {
            Logger.error(TAG, "scheduleJobHealth" + e.getMessage());
        }

    }

    public void runLoggerService(Context context) {
        Logger.debug(TAG, "runLoggerService");
        Intent myIntent = new Intent(context, LoggerService.class);
        startService(myIntent);
        PendingIntent restartServicePendingIntent = PendingIntent.getService(context, 0, myIntent, 0);
        AlarmManager alarmService = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmService == null) {
            Logger.error(TAG, "AlarmManager not available runLoggerService");
        }
        alarmService.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), 64 * 60 * 1000, restartServicePendingIntent);

    }

    private void initAppCenter() {
        setAppCenterCrashListener(); //Listener should be set before calling AppCenter start
        AppCenter.start(this, "bb348a98-dbeb-407f-862d-3337632c4e0e",
                Analytics.class, Crashes.class);
        AppCenter.setUserId(Util.getSerialNumber());
        Crashes.setEnabled(true);
    }

    private void setAppCenterCrashListener() {
        AbstractCrashesListener crashesListener = new AbstractCrashesListener() {
            @Override
            public boolean shouldProcess(ErrorReport report) {
                Log.i(TAG, "Should process");
                return true;
            }

            @Override
            public Iterable<ErrorAttachmentLog> getErrorAttachments(ErrorReport report) {
                Log.d(TAG, "Initiate crash report sending");
                SharedPreferences sp = Util.getSharedPreferences(getApplicationContext());
                byte[] binaryData = Util.getBytesFromFile(sp.getString(GlobalParameters.LogFilePath, ""));
                ErrorAttachmentLog binaryLog = ErrorAttachmentLog.attachmentWithBinary(binaryData, "Crashlog.log", "text/plain");
                return Collections.singletonList(binaryLog);
            }

            @Override
            public void onSendingFailed(ErrorReport report, Exception e) {
                Log.e(TAG, "Crash report sending failed");
            }

            @Override
            public void onSendingSucceeded(ErrorReport report) {
                Log.d(TAG, "Success: Crash report sent");
            }
        };
        Crashes.setListener(crashesListener);
    }

    public String getPragmaKey(Context context) {
        wifi = (WifiManager) context.getSystemService(WIFI_SERVICE);
        String macAddress = ConnectivityStatusActivity.getMacAddress("p2p0");
        String deviceSerialNo = Util.getSNCode(this);
        return getSha256Hash(deviceSerialNo + macAddress);
    }

    private String getSha256Hash(String password) {
        try {
            MessageDigest digest = null;
            try {
                digest = MessageDigest.getInstance("SHA-256");
            } catch (NoSuchAlgorithmException e1) {
                e1.printStackTrace();
            }
            digest.reset();
            return bin2hex(digest.digest(password.getBytes()));
        } catch (Exception ignored) {
            return null;
        }
    }

    private String bin2hex(byte[] data) {
        StringBuilder hex = new StringBuilder(data.length * 2);
        for (byte b : data)
            hex.append(String.format("%02x", b & 0xFF));
        return hex.toString();
    }


}