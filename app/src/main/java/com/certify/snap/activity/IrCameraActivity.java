package com.certify.snap.activity;

import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.arcsoft.face.ActiveFileInfo;
import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.FaceFeature;
import com.arcsoft.face.FaceInfo;
import com.arcsoft.face.LivenessInfo;
import com.arcsoft.face.enums.DetectFaceOrientPriority;
import com.arcsoft.face.enums.DetectMode;
import com.certify.callback.JSONObjectCallback;
import com.certify.callback.RecordTemperatureCallback;
import com.certify.snap.arcface.model.FacePreviewInfo;
import com.certify.snap.arcface.util.DrawHelper;
import com.certify.snap.arcface.util.camera.CameraListener;
import com.certify.snap.arcface.util.camera.DualCameraHelper;
import com.certify.snap.arcface.util.face.FaceHelper;
import com.certify.snap.arcface.util.face.FaceListener;
import com.certify.snap.arcface.util.face.LivenessType;
import com.certify.snap.arcface.util.face.RequestFeatureStatus;
import com.certify.snap.arcface.util.face.RequestLivenessStatus;
import com.certify.snap.arcface.widget.ProgressDialog;
import com.certify.snap.arcface.widget.ShowFaceInfoAdapter;
import com.certify.snap.common.Application;
import com.certify.snap.common.ConfigUtil;
import com.certify.snap.common.Constants;
import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.Logger;
import com.certify.snap.common.M1CardUtils;
import com.certify.snap.common.Util;
import com.certify.snap.model.GuestMembers;
import com.certify.snap.model.OfflineGuestMembers;
import com.certify.snap.model.OfflineVerifyMembers;
import com.certify.snap.model.RegisteredMembers;
import com.certify.snap.service.DeviceHealthService;
import com.common.thermalimage.HotImageCallback;
import com.common.thermalimage.TemperatureBitmapData;
import com.common.thermalimage.TemperatureData;
import com.google.zxing.other.BeepManager;
import com.certify.snap.faceserver.CompareResult;
import com.certify.snap.faceserver.FaceServer;
import com.certify.snap.view.MyGridLayoutManager;

import org.json.JSONObject;
import org.litepal.LitePal;

import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import com.certify.snap.R;

public class IrCameraActivity extends Activity implements ViewTreeObserver.OnGlobalLayoutListener {

    private static final String TAG = "IrCameraActivity";
    ImageView logo, loaddialog, scan, outerCircle, innerCircle, exit;
    private ObjectAnimator outerCircleAnimator, innerCircleAnimator;
    private ProcessHandler processHandler;
    private RelativeLayout relativeLayout;
    private long exitTime = 0;
    private int pressTimes = 0;
    private FaceEngine faceEngine;

    private static final int GUEST_QR_CODE = 333;
    public static final int HIDE_VERIFY_UI = 334;
    private static final int CARD_ID_ERROR = 335;
    private static final int ENTER = 336;
    private static final int TIME_ERROR = 337;
    OfflineVerifyMembers offlineVerifyMembers;
    List<RegisteredMembers> registeredMemberslist;
    private boolean isTemperatureIdentified = false;
    RelativeLayout rl_header;

    private View previewViewRgb;
    private View previewViewIr;
    private static boolean ConfirmationBoolean = false;

    private TextView tv_display_time, tv_message, template_view;

    Timer tTimer, pTimer, imageTimer, cameraTimer, lanchTimer;

    private static final float SIMILAR_THRESHOLD = 0.8F;

    private static final int ACTION_REQUEST_PERMISSIONS = 0x001;


    private ConcurrentHashMap<Integer, Integer> requestFeatureStatusMap = new ConcurrentHashMap<>();


    private ConcurrentHashMap<Integer, Integer> extractErrorRetryMap = new ConcurrentHashMap<>();


    private ConcurrentHashMap<Integer, Integer> livenessMap = new ConcurrentHashMap<>();


    private ConcurrentHashMap<Integer, Integer> livenessErrorRetryMap = new ConcurrentHashMap<>();

    private CompositeDisposable getFeatureDelayedDisposables = new CompositeDisposable();
    private CompositeDisposable delayFaceTaskCompositeDisposable = new CompositeDisposable();

    private static final int MAX_DETECT_NUM = 10;


    private static final int WAIT_LIVENESS_INTERVAL = 100;


    private static final long FAIL_RETRY_INTERVAL = 1000;


    private static final int MAX_RETRY_TIME = 3;

    private DualCameraHelper cameraHelper;
    private DualCameraHelper cameraHelperIr;
    private DrawHelper drawHelperRgb;
    private DrawHelper drawHelperIr;
    private Camera.Size previewSize;
    private Camera.Size previewSizeIr;


    private Integer cameraRgbId = Camera.CameraInfo.CAMERA_FACING_BACK;
    private Integer cameraIrId = Camera.CameraInfo.CAMERA_FACING_FRONT;


    private FaceHelper faceHelperIr;
    private List<CompareResult> compareResultList;
    private ShowFaceInfoAdapter adapter;
    private SharedPreferences sharedPreferences;
    protected static final String LOG = "IRCamera Activity - ";


    private static final String[] NEEDED_PERMISSIONS = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private AlertDialog.Builder builder;

    private int relaytimenumber = 5;
    ImageView img_guest, temperature_image, img_logo;
    TextView txt_guest;
    String message;
    //    private BeepManager mBeepManager, manormalBeep, mBeepManager1, mBeepManager2, malertBeep, mBeepSuccess;
    private WallpaperBroadcastReceiver wallpaperBroadcastReceiver;
    public static final String WALLPAPER_CHANGE = "com.telpo.telpo_face_system_wallpaper";

    private volatile byte[] irData;
    private AnimatorSet animatorSet;
    private boolean takePicRgb = false;
    private boolean takePicIr = false;
    private Bitmap irBitmap;
    private Bitmap rgbBitmap;
    private Bitmap temperatureBitmap = null;
    private int tempretrynum = 0;
    private int retrytemp = 0;
    private boolean isCalibrating = true;
    private boolean isTemperature = true;
    private boolean isSearch = true;
    private final Object obj = new Object();
    private float temperature = 0;
    private float lowTempValue = 0;
    RelativeLayout relative_main;
    TextView tv_thermal, tv_thermal_subtitle;
    private long delayMilli = 0;
    private int countTempError = 1;
    private boolean tempServiceClose = false;
    private TextView tvErrorMessage;
    private SoundPool soundPool;
    private FaceEngineHelper faceEngineHelper = new FaceEngineHelper();

    private NfcAdapter mNfcAdapter;
    private Tag mTag;
    private PendingIntent mPendingIntent;
    private SwipeCardThread mSwipeCardThread;
    private int DATA_BLOCK = 8;
    private final byte[] password1 = new byte[]{(byte) 0x80, (byte) 0x60,
            (byte) 0x30, (byte) 0x30, (byte) 0x70, (byte) 0x80};
    private AlertDialog nfcDialog;
    Typeface rubiklight;


    class WallpaperBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null && intent.getAction().equals(WALLPAPER_CHANGE)) {
                //    showWallpaper();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rubiklight = Typeface.createFromAsset(getAssets(),
                "rubiklight.ttf");

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_ir);
        sharedPreferences = Util.getSharedPreferences(this);
        img_logo = findViewById(R.id.img_logo);
        String path = sharedPreferences.getString(GlobalParameters.IMAGE_ICON, "");
        homeIcon(path);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Application.getInstance().addActivity(this);
        FaceServer.getInstance().init(this);//init FaceServer;
        try {

            processHandler = new ProcessHandler(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mNfcAdapter = M1CardUtils.isNfcAble(this);
        mPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        logo = findViewById(R.id.logo);
        rl_header = findViewById(R.id.rl_header);
        rl_header.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loginIt = new Intent(IrCameraActivity.this, LoginActivity.class);
                startActivity(loginIt);
                finish();
            }
        });

        scan = findViewById(R.id.scan);
        tv_thermal = findViewById(R.id.tv_thermal);
        tv_thermal_subtitle = findViewById(R.id.tv_thermal_subtitle);
        tv_thermal.setText(sharedPreferences.getString(GlobalParameters.Thermalscan_title, "THERMAL SCAN"));
        tv_thermal_subtitle.setText(sharedPreferences.getString(GlobalParameters.Thermalscan_subtitle, ""));
        tv_thermal.setTypeface(rubiklight);
        tv_thermal_subtitle.setTypeface(rubiklight);

        final PackageManager packageManager = getPackageManager();
        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClassName("com.telpo.tps550.api", "com.telpo.tps550.api.barcode.Capture");
                try {
                    if (intent.resolveActivityInfo(packageManager, PackageManager.MATCH_DEFAULT_ONLY) != null) {
                        startActivityForResult(intent, GUEST_QR_CODE);
                    } else
                        Toast.makeText(IrCameraActivity.this, getString(R.string.toast_ocrnotinstall), Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
//        try {
//            db = LitePal.getDatabase();
//        }catch (Exception e){
//            e.printStackTrace();
//        }

        initView();
        relaytimenumber = sharedPreferences.getInt(GlobalParameters.RelayTime, 5);
        GlobalParameters.livenessDetect = sharedPreferences.getBoolean(GlobalParameters.LivingType, true);

        if (sharedPreferences.getBoolean("wallpaper", false)) {
            //showWallpaper();
        }
        exit = findViewById(R.id.exit);
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendBroadcast(new Intent(GlobalParameters.ACTION_OPEN_STATUSBAR));
                sendBroadcast(new Intent(GlobalParameters.ACTION_SHOW_NAVIGATIONBAR));

//                GuideActivity.mActivity.finish();
//                finish();
                // System.exit(0);
                Application.getInstance().exit();
            }
        });

        template_view = findViewById(R.id.template_view);
        temperature_image = findViewById(R.id.temperature_image);
    }

    private void homeIcon(String path) {

        if (!path.isEmpty()) {
            Bitmap bitmap = Util.decodeToBase64(path);
            img_logo.setImageBitmap(bitmap);
        } else {
            img_logo.setBackgroundResource(R.drawable.final_logo);

        }

    }

    private void showTip(final String msg, final boolean isplaysound) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv_message.setText(msg);
//                if (isplaysound) manormalBeep.playBeepSoundAndVibrate();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GUEST_QR_CODE) {
            if (resultCode == 0 && data != null) {
                String qrCode = data.getStringExtra("qrCode");
                if (qrCode != null) {
                    List<GuestMembers> guestMembers = LitePal.where("qrcode = ?", qrCode).find(GuestMembers.class);
                    if (guestMembers != null && guestMembers.size() > 0) {
                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Date curDate = new Date(System.currentTimeMillis());
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
                        String cpmpareTime = simpleDateFormat.format(curDate);
                        String verify_time = formatter.format(curDate);
                        String expire_time = guestMembers.get(0).getExpire_time();
                        String code = guestMembers.get(0).getQrcode();
                        if (code.equals(qrCode)) {
                            if (Util.isDateOneBigger(expire_time, verify_time)) {
                                if ((!TextUtils.isEmpty(GlobalParameters.Access_limit) && compareAllLimitedTime(cpmpareTime, processLimitedTime(GlobalParameters.Access_limit)))
                                        || TextUtils.isEmpty(GlobalParameters.Access_limit)) {
                                    initRgbCamera();
                                    showGuestToast();
                                    if (img_guest != null) {
                                        img_guest.setBackground(getDrawable(R.mipmap.scan_success));
                                    }
                                    if (txt_guest != null) {
                                        txt_guest.setText(getString(R.string.welcome) + guestMembers.get(0).getName());
                                    }
                                    Util.setRelayPower(1);
                                    pTimer = new Timer();
                                    pTimer.schedule(new TimerTask() {
                                        public void run() {
                                            Util.setRelayPower(0);
                                            this.cancel();
                                        }
                                    }, relaytimenumber * 1000);//
                                    OfflineGuestMembers offlineGuestMembers = new OfflineGuestMembers();
                                    offlineGuestMembers.setUserId(guestMembers.get(0).getUserId());
                                    offlineGuestMembers.setVerify_time(verify_time);
                                    offlineGuestMembers.save();
                                    Logger.debug("tag", "offlineGuestMembers userId----" + guestMembers.get(0).getUserId());
                                } else if (!TextUtils.isEmpty(GlobalParameters.Access_limit) && !compareAllLimitedTime(cpmpareTime, processLimitedTime(GlobalParameters.Access_limit))) {
                                    restoreCameraAfterScan(true);
                                    if (txt_guest != null)
                                        txt_guest.setText(R.string.text_notpasstime);
                                }
                            } else {
                                restoreCameraAfterScan(true);
                                if (txt_guest != null)
                                    txt_guest.setText(R.string.text_passcodeexpired);
                            }
                        } else restoreCameraAfterScan(true);
                    } else restoreCameraAfterScan(true);
                } else restoreCameraAfterScan(true);
            } else restoreCameraAfterScan(false);
        }
    }

    @Override
    public void onBackPressed() {
        //Application.getInstance().exit();
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(IrCameraActivity.this);
            builder.setMessage("Are you sure you want to exit?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {


                            finishAffinity();

                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initView() {


        previewViewRgb = findViewById(R.id.texture_preview);
        previewViewIr = findViewById(R.id.texture_preview_ir);

        relative_main = findViewById(R.id.relative_layout);

        relativeLayout = findViewById(R.id.rl_verify);
        outerCircle = findViewById(R.id.iv_verify_outer_circle);
        innerCircle = findViewById(R.id.iv_verify_inner_circle);
        tvErrorMessage = findViewById(R.id.tv_error_message);
        previewViewRgb.getViewTreeObserver().addOnGlobalLayoutListener(this);

        tv_display_time = findViewById(R.id.tv_display_time);
        TextView tvVersionIr = findViewById(R.id.tv_version_ir);
        tvVersionIr.setText(Util.getVersionBuild());
        tv_display_time.setTypeface(rubiklight);
        tv_message = findViewById(R.id.tv_message);
        tTimer = new Timer();
        tTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                SimpleDateFormat formatter = new SimpleDateFormat("MMMM dd, YYYY hh:mm:ss a", Locale.ENGLISH);//yyyy-MM-dd HH:mm:ss
                Date curDate = new Date(System.currentTimeMillis());
                final String str = formatter.format(curDate);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv_display_time.setText(str);
                    }
                });
            }
        }, 0, 1000);

        RecyclerView recyclerShowFaceInfo = findViewById(R.id.recycler_view_person);
        compareResultList = new ArrayList<>();
        adapter = new ShowFaceInfoAdapter(compareResultList, this);
        recyclerShowFaceInfo.setAdapter(adapter);
        recyclerShowFaceInfo.setLayoutManager(new MyGridLayoutManager(this, 1));
        recyclerShowFaceInfo.setItemAnimator(new DefaultItemAnimator());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes attributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();

            soundPool = new SoundPool.Builder()
                    .setAudioAttributes(attributes)
                    .build();
        } else {
            soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        }
    }

    private boolean checkPermissions(String[] neededPermissions) {
        if (neededPermissions == null || neededPermissions.length == 0) {
            return true;
        }
        boolean allGranted = true;
        for (String neededPermission : neededPermissions) {
            allGranted &= ContextCompat.checkSelfPermission(this, neededPermission) == PackageManager.PERMISSION_GRANTED;
        }
        return allGranted;
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null)
            mTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        //startDetectCard();
    }


    @Override
    protected void onResume() {
        Log.v(TAG, "onResume");
        super.onResume();

        //Logger.debug(TAG, "" + Util.logHeap(IrCameraActivity.this));
        String longVal = sharedPreferences.getString(GlobalParameters.DELAY_VALUE, "3");
        if (longVal.equals("")) {
            delayMilli = 3;
        } else {
            delayMilli = Long.parseLong(longVal);
        }
        Log.v(TAG, "onResume delayMilli:" + delayMilli);
        //   if (sharedPreferences.getBoolean(GlobalParameters.CONFIRM_SCREEN, true)) {
        if (ConfirmationBoolean) {
            isTemperatureIdentified = true;
            relative_main.setVisibility(View.VISIBLE);
            logo.setVisibility(View.VISIBLE);
            rl_header.setVisibility(View.VISIBLE);
            tv_message.setVisibility(View.GONE);
            temperature_image.setVisibility(View.GONE);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    ConfirmationBoolean = false;
                    clearLeftFace(null);
                    isTemperatureIdentified = false;
                }
            }, 3000);
            //  }
        }


        if (mNfcAdapter != null) {
            mNfcAdapter.enableForegroundDispatch(this, mPendingIntent, null, null);
        }
        try {
            if (cameraHelper != null) {
                cameraHelper.start();
            }
            if (cameraHelperIr != null) {
                cameraHelperIr.start();
            }
        } catch (RuntimeException e) {
            Util.error("Onresume", e.getMessage());
            Toast.makeText(this, e.getMessage() + getString(R.string.camera_error_notice), Toast.LENGTH_SHORT).show();
        }

        try {
            if (sharedPreferences.getBoolean(GlobalParameters.ONLINE_MODE, false))
                if (Util.isConnectingToInternet(this)) {
                    startService(new Intent(IrCameraActivity.this, DeviceHealthService.class));
                    Application.StartService(this);
                }
            // finish();
        } catch (Exception e) {
            e.printStackTrace();
            Logger.error("onresume", e.getMessage());
        }

    }

    @Override
    protected void onPause() {
        if (mNfcAdapter != null) {
            mNfcAdapter.disableForegroundDispatch(this);
        }
        if (cameraHelper != null) {
            cameraHelper.stop();
        }
        if (cameraHelperIr != null) {
            cameraHelperIr.stop();
        }
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (mSwipeCardThread != null) {
            mSwipeCardThread.interrupt();
            mSwipeCardThread = null;
//            Log.e("stop thread","success");
        }
        if (nfcDialog != null && nfcDialog.isShowing()) {
            nfcDialog.dismiss();
            nfcDialog = null;
        }
        processHandler.removeCallbacksAndMessages(null);
        if (tTimer != null)
            tTimer.cancel();
        if (pTimer != null)
            pTimer.cancel();
        if (imageTimer != null)
            imageTimer.cancel();
        if (cameraHelper != null) {
            cameraHelper.release();
            cameraHelper = null;
        }
        if (cameraHelperIr != null) {
            cameraHelperIr.release();
            cameraHelperIr = null;
        }

        faceEngineHelper.unInitEngine();

        if (getFeatureDelayedDisposables != null) {
            getFeatureDelayedDisposables.clear();
        }
        if (delayFaceTaskCompositeDisposable != null) {
            delayFaceTaskCompositeDisposable.clear();
        }

        if (faceHelperIr != null) {
            ConfigUtil.setTrackedFaceCount(this, faceHelperIr.getTrackedFaceCount());
            faceHelperIr.release();
            faceHelperIr = null;
        }

        FaceServer.getInstance().unInit();
        stopAnimation();
        cancelImageTimer();
        super.onDestroy();
    }

    long time1, time2;

    public void runTemperature() {
        isTemperature = false;
        isSearch = false;
        time1 = time2 = 0;
        time1 = System.currentTimeMillis();
        temperature = 0;
        synchronized (obj) {
            new Thread(new Runnable() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void run() {
                    try {

                        TemperatureData temperatureData = Application.getInstance().getTemperatureUtil()
                                .getDataAndBitmap(50, true, new HotImageCallbackImpl());
                        if (temperatureData == null) {
                            Log.w(TAG, "temperatureData is null");
                            return;
                        }
                        String text = "";
                        takePicIr = false;
                        takePicRgb = false;
                        String temperaturePreference = sharedPreferences.getString(GlobalParameters.F_TO_C, "F");
                        String temperatureFormat = temperaturePreference.equals("F")
                                ? getString(R.string.fahrenheit_symbol) : getString(R.string.centi);
                        String abnormalTemperature = getString(R.string.temperature_anormaly);
                        temperature = temperatureData.getTemperature();//centigrade
                        if (temperaturePreference.equals("F")) {
                            temperature = Util.FahrenheitToCelcius(temperatureData.getTemperature());
                        }
                        String tempString = String.format("%,.1f", temperature);
                        String thresholdTemperaturePreference = sharedPreferences.getString(GlobalParameters.TEMP_TEST, "99");
                        Float thresholdTemperature = Float.parseFloat(thresholdTemperaturePreference);
                        if (temperature > thresholdTemperature) {
                            text = getString(R.string.temperature_anormaly) + tempString + temperatureFormat;
                            TemperatureCallBackUISetup(true, text, tempString, false);
                            //  mTemperatureListener.onTemperatureCall(true, text);

                        } else {
                            text = getString(R.string.temperature_normal) + tempString + temperatureFormat;
                            TemperatureCallBackUISetup(false, text, tempString, false);
                            //   mTemperatureListener.onTemperatureCall(false, text);
//                                if (Util.isConnectingToInternet(IrCameraActivity.this) && (sharedPreferences.getString(GlobalParameters.ONLINE_MODE, "").equals("true"))) {
//                                    if (sharedPreferences.getBoolean(GlobalParameters.CAPTURE_IMAGES_ALL, false) || sharedPreferences.getBoolean(GlobalParameters.CAPTURE_IMAGES_ABOVE, true))
//                                        Util.recordUserTemperature(IrCameraActivity.this, IrCameraActivity.this, tempString, irBitmap, rgbBitmap, temperatureBitmap, false);
//                                    else
//                                        Util.recordUserTemperature(IrCameraActivity.this, IrCameraActivity.this, tempString, null, null, null, false);
//                                }
                        }
//                            rgbBitmap = null;
//                            irBitmap = null;
//                            temperatureBitmap = null;

                    } catch (Exception e) {
                        Logger.error(Util.getSNCode() + "getTemperatureBimapData(final TemperatureBitmapData data) throws RemoteException ", e.getMessage());
                        retry(retrytemp);
                    }

                }
            }).start();

        }
    }

    private void retry(int retryNumber) {
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                tv_message.setVisibility(View.GONE);
//
//            }
//        });
        if (retryNumber < 3) {
            Logger.error(Util.getSNCode() + "retry temperature---", retrytemp + "-" + tempretrynum);
            runTemperature();
            retryNumber++;
            // showTip(getString(R.string.temperature_retry), false);
        } else {
            showTip(getString(R.string.temperature_failresult), false);
            Logger.error(Util.getSNCode() + "temperature failed", retrytemp + "-" + tempretrynum);
            isSearch = true;
        }
    }


    private void initRgbCamera() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        final FaceListener faceListener = new FaceListener() {
            @Override
            public void onFail(Exception e) {
                Logger.error(Util.getSNCode() + "initRgbCamera() onFail(Exception e)", e.getMessage());

            }

            @Override
            public void onFaceFeatureInfoGet(@Nullable final FaceFeature faceFeature, final Integer requestId, final Integer errorCode) {
                Logger.debug(TAG, "initRgbCamera.faceListener.onFaceFeatureInfoGet");
                if (faceFeature != null) {
                    countTempError = 0;
                    Logger.debug(TAG, "onFaceFeatureInfoGet: fr end = " + System.currentTimeMillis() + " trackId = " + requestId + " isIdentified = " + isTemperatureIdentified + ",tempServiceColes " + tempServiceClose);
                    if (isTemperatureIdentified) return;
                    tempServiceClose = false;
                    takePicRgb = true;
                    takePicIr = true;
                    // isIdentified = false;
                    if (compareResultList != null) {
//                        for (int i = compareResultList.size() - 1; i >= 0; i--) {
//                            if (compareResultList.get(i).getTrackId() == requestId) {
//                                isIdentified = true;
//                                break;
//                            }
//                        }
                        if (!isTemperatureIdentified) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    changeVerifyBackground(R.color.transparency, true);
                                    relative_main.setVisibility(View.GONE);
                                    rl_header.setVisibility(View.GONE);
                                    logo.setVisibility(View.GONE);
                                    showAnimation();
                                    // Log.e("runTemperature---","isIdentified="+isIdentified);
                                    if (isCalibrating && isSearch) runTemperature();

                                    cancelImageTimer();
                                    imageTimer = new Timer();
                                    imageTimer.schedule(new TimerTask() {
                                        public void run() {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    isSearch = true;
                                                    Logger.debug(TAG + "runTemperature---", "isIdentified=" + isTemperatureIdentified);
                                                    //  tvDisplayingCount.setVisibility(View.GONE);
                                                    if (isTemperatureIdentified || !takePicRgb)
                                                        return;

                                                    stopAnimation();
                                                    tv_message.setText("");
                                                    tv_message.setVisibility(View.GONE);
                                                    tvErrorMessage.setVisibility(View.GONE);
                                                    temperature_image.setVisibility(View.GONE);
                                                    relative_main.setVisibility(View.VISIBLE);
                                                    logo.setVisibility(View.VISIBLE);
                                                    rl_header.setVisibility(View.VISIBLE);
                                                    tempServiceClose = true;
//                                                    Util.enableLedPower(0);
                                                }
                                            });

                                            this.cancel();
                                        }
                                    }, 10 * 1000);//wait 10 seconds for the temperature to be captured, go to home otherwise
                                }
                            });
                        }
                    }

                    Integer liveness = livenessMap.get(requestId);
                    if (!GlobalParameters.livenessDetect) {
                        //  searchFace(faceFeature, requestId);
                    } else if (liveness != null && liveness == LivenessInfo.ALIVE) {
                        Logger.debug("liveness---", "LivenessInfo.ALIVE---" + isTemperature);
                        //searchFace(faceFeature, requestId);
                    } else {

                        if (requestFeatureStatusMap.containsKey(requestId)) {
                            Observable.timer(WAIT_LIVENESS_INTERVAL, TimeUnit.MILLISECONDS)
                                    .subscribe(new Observer<Long>() {
                                        Disposable disposable;

                                        @Override
                                        public void onSubscribe(Disposable d) {
                                            disposable = d;
                                            getFeatureDelayedDisposables.add(disposable);
                                        }

                                        @Override
                                        public void onNext(Long aLong) {
                                            onFaceFeatureInfoGet(faceFeature, requestId, errorCode);
                                        }

                                        @Override
                                        public void onError(Throwable e) {
                                            Logger.error(Util.getSNCode() + "onFeatureInfo get", e.getMessage());

                                        }

                                        @Override
                                        public void onComplete() {
                                            getFeatureDelayedDisposables.remove(disposable);
                                        }
                                    });
                        }
                    }

                } else {
                    if (increaseAndGetValue(extractErrorRetryMap, requestId) > MAX_RETRY_TIME) {
                        extractErrorRetryMap.put(requestId, 0);
                        String msg;
                        // FaceInfo RGB
                        if (errorCode != null && errorCode == ErrorInfo.MERR_FSDK_FACEFEATURE_LOW_CONFIDENCE_LEVEL) {
                            msg = getString(R.string.low_confidence_level);
                        } else {
                            msg = getString(R.string.ExtractCode) + errorCode;
                        }
                        faceHelperIr.setName(requestId, getString(R.string.recognize_failed_notice, msg));
                        requestFeatureStatusMap.put(requestId, RequestFeatureStatus.FAILED);
                        retryRecognizeDelayed(requestId);
                    } else {
                        requestFeatureStatusMap.put(requestId, RequestFeatureStatus.TO_RETRY);
                    }
                }
            }

            @Override
            public void onFaceLivenessInfoGet(@Nullable LivenessInfo livenessInfo, final Integer requestId, Integer errorCode) {
                if (livenessInfo != null) {
                    int liveness = livenessInfo.getLiveness();
                    livenessMap.put(requestId, liveness);
                    if (liveness == LivenessInfo.NOT_ALIVE) {
                        faceHelperIr.setName(requestId, getString(R.string.recognize_failed_notice, "NOT_ALIVE"));
                        //  FAIL_RETRY_INTERVAL UNKNOWN
                        retryLivenessDetectDelayed(requestId);
                    }
                } else {
                    if (increaseAndGetValue(livenessErrorRetryMap, requestId) > MAX_RETRY_TIME) {
                        livenessErrorRetryMap.put(requestId, 0);
                        String msg;
                        // FaceInfo + IR
                        if (errorCode != null && errorCode == ErrorInfo.MERR_FSDK_FACEFEATURE_LOW_CONFIDENCE_LEVEL) {
                            msg = getString(R.string.low_confidence_level);
                        } else {
                            msg = getString(R.string.ProcessCode) + errorCode;
                        }
                        faceHelperIr.setName(requestId, getString(R.string.recognize_failed_notice, msg));
                        livenessMap.put(requestId, LivenessInfo.NOT_ALIVE);
                        retryLivenessDetectDelayed(requestId);
                    } else {
                        livenessMap.put(requestId, LivenessInfo.UNKNOWN);
                    }
                }
            }

        };
        CameraListener rgbCameraListener = new RgbCameraListener(faceListener);
        cameraHelper = new DualCameraHelper.Builder()
                .previewViewSize(new Point(previewViewRgb.getMeasuredWidth(), previewViewRgb.getMeasuredHeight()))
                .rotation(sharedPreferences.getInt(GlobalParameters.Orientation, 0))
                .specificCameraId(cameraRgbId != null ? cameraRgbId : Camera.CameraInfo.CAMERA_FACING_BACK)
                .previewOn(previewViewRgb)
                .cameraListener(rgbCameraListener)
                .isMirror(cameraRgbId != null && Camera.CameraInfo.CAMERA_FACING_FRONT == cameraRgbId)
                .build();
        cameraHelper.init();
        try {
            cameraHelper.start();
        } catch (RuntimeException e) {
            Toast.makeText(IrCameraActivity.this, e.getMessage() + getString(R.string.camera_error_notice), Toast.LENGTH_SHORT).show();
        }
    }

    private void initIrCamera() {
        CameraListener irCameraListener = new IrCameraListener();

        cameraHelperIr = new DualCameraHelper.Builder()
                .previewViewSize(new Point(previewViewIr.getMeasuredWidth(), previewViewIr.getMeasuredHeight()))
                .rotation(sharedPreferences.getInt(GlobalParameters.Orientation, 0))
                .specificCameraId(cameraIrId != null ? cameraIrId : Camera.CameraInfo.CAMERA_FACING_FRONT)
                .previewOn(previewViewIr)
                .cameraListener(irCameraListener)
                .isMirror(cameraIrId != null && Camera.CameraInfo.CAMERA_FACING_FRONT == cameraIrId)
//                .previewSize(new Point(1280, 960)) //Size，RGB of IR
//                .additionalRotation(270) //
                .build();
        cameraHelperIr.init();
        try {
            cameraHelperIr.start();
        } catch (RuntimeException e) {
            Toast.makeText(IrCameraActivity.this, e.getMessage() + getString(R.string.camera_error_notice), Toast.LENGTH_SHORT).show();
            Logger.error(Util.getSNCode() + "initIR camera", e.getMessage());
        }
    }


    private synchronized void processPreviewData(byte[] rgbData) {
        if (rgbData != null && irData != null) {
            final byte[] cloneNv21Rgb = rgbData.clone();
            List<FacePreviewInfo> facePreviewInfoList = faceHelperIr.onPreviewFrame(cloneNv21Rgb);
            clearLeftFace(facePreviewInfoList);
            if (facePreviewInfoList != null && facePreviewInfoList.size() > 0 && previewSize != null) {
                for (int i = 0; i < facePreviewInfoList.size(); i++) {
                    Integer status = requestFeatureStatusMap.get(facePreviewInfoList.get(i).getTrackId());
                    if (GlobalParameters.livenessDetect && (status == null || status != RequestFeatureStatus.SUCCEED)) {
                        Integer liveness = livenessMap.get(facePreviewInfoList.get(i).getTrackId());
                        if (liveness == null
                                || (liveness != LivenessInfo.ALIVE && liveness != LivenessInfo.NOT_ALIVE && liveness != RequestLivenessStatus.ANALYZING)) {
                            livenessMap.put(facePreviewInfoList.get(i).getTrackId(), RequestLivenessStatus.ANALYZING);
                            // IR
                            FaceInfo faceInfo = facePreviewInfoList.get(i).getFaceInfo().clone();
                            faceInfo.getRect().offset(Constants.HORIZONTAL_OFFSET, Constants.VERTICAL_OFFSET);
                            faceHelperIr.requestFaceLiveness(irData.clone(), faceInfo, previewSize.width, previewSize.height,
                                    FaceEngine.CP_PAF_NV21, facePreviewInfoList.get(i).getTrackId(), LivenessType.IR);
                        }
                    }


                    if (status == null
                            || status == RequestFeatureStatus.TO_RETRY) {
                        requestFeatureStatusMap.put(facePreviewInfoList.get(i).getTrackId(), RequestFeatureStatus.SEARCHING);
                        faceHelperIr.requestFaceFeature(cloneNv21Rgb, facePreviewInfoList.get(i).getFaceInfo(),
                                previewSize.width, previewSize.height, FaceEngine.CP_PAF_NV21,
                                facePreviewInfoList.get(i).getTrackId());
                    }
                }
            }
            irData = null;
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == ACTION_REQUEST_PERMISSIONS) {
            boolean isAllGranted = true;
            for (int grantResult : grantResults) {
                isAllGranted &= (grantResult == PackageManager.PERMISSION_GRANTED);
            }
            if (isAllGranted) {
                faceEngineHelper.initEngine(this);
                initRgbCamera();
                initIrCamera();
                if (cameraHelper != null) {
                    cameraHelper.start();
                }

            } else {
                Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
                Logger.error(Util.getSNCode() + "OnRequestPermissionResult", "permission denied");
            }
        }
    }


    private void clearLeftFace(List<FacePreviewInfo> facePreviewInfoList) {
        if (compareResultList != null) {
            for (int i = compareResultList.size() - 1; i >= 0; i--) {
                if (!requestFeatureStatusMap.containsKey(compareResultList.get(i).getTrackId())) {
                    Logger.debug(TAG, "remove exist face");
                    compareResultList.remove(i);
                    adapter.notifyItemRemoved(i);
                    tv_message.setText("");
                    tv_message.setVisibility(View.GONE);
                    //   tvDisplayingCount.setVisibility(View.GONE);
                    stopAnimation();

                    temperature_image.setVisibility(View.GONE);
                    tvErrorMessage.setVisibility(View.GONE);
                    logo.setVisibility(View.VISIBLE);
                    relative_main.setVisibility(View.VISIBLE);
                    rl_header.setVisibility(View.VISIBLE);
//                    Util.enableLedPower(0);
                }
            }
        }
        if (facePreviewInfoList == null || facePreviewInfoList.size() == 0) {
            requestFeatureStatusMap.clear();
            livenessMap.clear();
            livenessErrorRetryMap.clear();
            extractErrorRetryMap.clear();
            if (getFeatureDelayedDisposables != null) {
                getFeatureDelayedDisposables.clear();
            }
            return;
        }
        Enumeration<Integer> keys = requestFeatureStatusMap.keys();
        while (keys.hasMoreElements()) {
            int key = keys.nextElement();
            boolean contained = false;
            for (FacePreviewInfo facePreviewInfo : facePreviewInfoList) {
                if (facePreviewInfo.getTrackId() == key) {
                    contained = true;
                    break;
                }
            }
            if (!contained) {
                requestFeatureStatusMap.remove(key);
                livenessMap.remove(key);
                livenessErrorRetryMap.remove(key);
                extractErrorRetryMap.remove(key);
            }
        }


    }


    private void showResult(CompareResult compareResult, int requestId, String message, final boolean isdoor) {
        //When adding display personnel, save their trackId
        compareResult.setTrackId(requestId);
        compareResult.setMessage(message);
        compareResultList.add(compareResult);
        processHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isdoor) {
                    Util.setRelayPower(1);
                    pTimer = new Timer();
                    pTimer.schedule(new TimerTask() {
                        public void run() {
                            Util.setRelayPower(0);
                            this.cancel();
                        }
                    }, relaytimenumber * 1000);
                }
                sendMessageToStopAnimation(HIDE_VERIFY_UI);
                adapter.notifyItemInserted(compareResultList.size() - 1);
            }
        }, 100);
    }

    private void addOfflineMember(String name, String mobile, String image, Date verify_time, float temperature) {
        offlineVerifyMembers = new OfflineVerifyMembers();
        offlineVerifyMembers.setName(name);
        offlineVerifyMembers.setMobile(mobile);
        offlineVerifyMembers.setImagepath(image);
        offlineVerifyMembers.setVerify_time(verify_time);
        if (temperature > 0) offlineVerifyMembers.setTemperature("" + temperature);
        offlineVerifyMembers.save();
    }


    @Override
    public void onGlobalLayout() {
        previewViewRgb.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        if (!checkPermissions(NEEDED_PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, NEEDED_PERMISSIONS, ACTION_REQUEST_PERMISSIONS);
        } else {
            faceEngineHelper.initEngine(this);
            initRgbCamera();
            initIrCamera();
        }
    }


    public int increaseAndGetValue(Map<Integer, Integer> countMap, int key) {
        if (countMap == null) {
            return 0;
        }
        Integer value = countMap.get(key);
        if (value == null) {
            value = 0;
        }
        countMap.put(key, ++value);
        return value;
    }


    private void retryLivenessDetectDelayed(final Integer requestId) {
        Observable.timer(FAIL_RETRY_INTERVAL, TimeUnit.MILLISECONDS)
                .subscribe(new Observer<Long>() {
                    Disposable disposable;

                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable = d;
                        delayFaceTaskCompositeDisposable.add(disposable);
                    }

                    @Override
                    public void onNext(Long aLong) {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                        // UNKNOWN
                        if (GlobalParameters.livenessDetect) {
                            faceHelperIr.setName(requestId, Integer.toString(requestId));
                        }
                        livenessMap.put(requestId, LivenessInfo.UNKNOWN);
                        delayFaceTaskCompositeDisposable.remove(disposable);
                    }
                });
    }


    private void retryRecognizeDelayed(final Integer requestId) {
        requestFeatureStatusMap.put(requestId, RequestFeatureStatus.FAILED);
        Observable.timer(FAIL_RETRY_INTERVAL, TimeUnit.MILLISECONDS)
                .subscribe(new Observer<Long>() {
                    Disposable disposable;

                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable = d;
                        delayFaceTaskCompositeDisposable.add(disposable);
                    }

                    @Override
                    public void onNext(Long aLong) {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                        // FAILED
                        faceHelperIr.setName(requestId, Integer.toString(requestId));
                        requestFeatureStatusMap.put(requestId, RequestFeatureStatus.TO_RETRY);
                        delayFaceTaskCompositeDisposable.remove(disposable);
                    }
                });
    }

    private void startDetectCard() {
        mSwipeCardThread = new SwipeCardThread();
        mSwipeCardThread.start();
    }

    class SwipeCardThread extends Thread {
        @Override
        public void run() {
            try {
                int resultCode = M1CardUtils.readBlockWithKeyA(mTag, DATA_BLOCK, password1);
                if (resultCode == 111) {
                    Date curDate = new Date(System.currentTimeMillis());
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
                    String compareTime = simpleDateFormat.format(curDate);
                    if ((!TextUtils.isEmpty(GlobalParameters.Access_limit) && compareAllLimitedTime(compareTime, processLimitedTime(GlobalParameters.Access_limit)))
                            || TextUtils.isEmpty(GlobalParameters.Access_limit)) {
                        processResult(ENTER);
                        Util.setRelayPower(1);
                        pTimer = new Timer();
                        pTimer.schedule(new TimerTask() {
                            public void run() {
                                Util.setRelayPower(0);
                                this.cancel();
                            }
                        }, relaytimenumber * 1000);//
                    } else if (!TextUtils.isEmpty(GlobalParameters.Access_limit)
                            && !compareAllLimitedTime(compareTime, processLimitedTime(GlobalParameters.Access_limit))) {
                        processResult(TIME_ERROR);
                    }
                } else {
                    processResult(CARD_ID_ERROR);
                }
            } catch (Exception e) {
                e.printStackTrace();
                processResult(CARD_ID_ERROR);
            }
        }
    }

    private void processResult(int what) {
        processHandler.obtainMessage(what).sendToTarget();
        processHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (nfcDialog != null && nfcDialog.isShowing()) {
                    nfcDialog.dismiss();
                    nfcDialog = null;
                }
            }
        }, 1000);
    }


    private void restoreCameraAfterScan(Boolean isShow) {
        initRgbCamera();
        initIrCamera();
        if (isShow) showGuestToast();
    }

    private void cancelImageTimer() {
        if (imageTimer != null)
            imageTimer.cancel();
    }

    private static class ProcessHandler extends Handler {
        WeakReference<IrCameraActivity> activityWeakReference;

        private ProcessHandler(IrCameraActivity activity) {
            activityWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            IrCameraActivity irCameraActivity = activityWeakReference.get();
            if (irCameraActivity == null)
                return;
            switch (msg.what) {
                case HIDE_VERIFY_UI:
                    // irCameraActivity.stopAnimation();
                    //irCameraActivity.changeVerifyBackground(R.color.transparency, false);
                    break;
                case CARD_ID_ERROR:
//                    irCameraActivity.mBeepManager2.playBeepSoundAndVibrate();
                    irCameraActivity.showNfcResult(false, false);
                    break;
                case ENTER:
//                    irCameraActivity.mBeepManager1.playBeepSoundAndVibrate();
                    irCameraActivity.showNfcResult(true, true);
                    break;
                case TIME_ERROR:
//                    irCameraActivity.mBeepManager2.playBeepSoundAndVibrate();
                    irCameraActivity.showNfcResult(true, false);
                    break;
            }
        }
    }

    private void showNfcResult(boolean isSuccess, boolean isLimitTime) {
        if (nfcDialog != null && nfcDialog.isShowing())
            return;
        View view = View.inflate(this, R.layout.toast_guest, null);
        ImageView img_nfc = view.findViewById(R.id.img_guest);
        TextView txt_nfc = view.findViewById(R.id.txt_guest);
        img_nfc.setBackground(isSuccess ? (isLimitTime ? getDrawable(R.mipmap.scan_success) : getDrawable(R.mipmap.scan_fail)) : getDrawable(R.mipmap.scan_fail));
        txt_nfc.setText(isSuccess ? (isLimitTime ? getString(R.string.welcome) : getString(R.string.text_notpasstime)) : getString(R.string.Unrecognized));
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setView(view)
                .setCancelable(false);
        nfcDialog = builder.create();
        nfcDialog.show();
        Window window = nfcDialog.getWindow();
        if (window != null) {
            window.setLayout(getWindowManager().getDefaultDisplay().getWidth() / 2, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawable(new ColorDrawable());
        }
    }


    private void sendMessageToStopAnimation(int what) {
        if (processHandler == null)
            return;

        boolean isSent = processHandler.sendEmptyMessage(what);

        if (!isSent) {
            new ProcessHandler(IrCameraActivity.this).sendEmptyMessage(what);
        }
    }

    private void changeVerifyBackground(int id, boolean isVisible) {
        if (outerCircle == null || innerCircle == null)
            return;

        //outerCircle.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        //innerCircle.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        relativeLayout.setBackground(getDrawable(id));
    }

    private void showAnimation() {
        tempretrynum = 0;
        retrytemp = 0;
        isTemperature = true;
//        temperature_image.setVisibility(View.GONE);
//        tv_message.setText("");
//        tv_message.setVisibility(View.GONE);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //  Util.enableLedPower(1);
            }
        }, 500);
    }

    private void stopAnimation() {
//        if (animatorSet != null) {
//            animatorSet.cancel();
//            outerCircleAnimator = null;
//            innerCircleAnimator = null;
//            animatorSet = null;
//        }
    }

    private void showWallpaper() {
//        if (sp.getBoolean("wallpaper", false)) {
//            Glide.with(IrCameraActivity.this)
//                    .load(GuideService.WALLPAPER_DIR + File.separator + "wallpaper.png")
//                    .error(R.mipmap.telpo)
//                    .skipMemoryCache(true)
//                    .diskCacheStrategy(DiskCacheStrategy.NONE);
//            //.into(relativeLayout);
        //}
    }

    public String[] processLimitedTime(String data) {
        if (data.contains(";")) {
            return data.split(";");
        } else {
            return new String[]{data};
        }
    }

    public boolean compareAllLimitedTime(String compareTime, String[] limitedTimes) {
        if (compareTime == null || limitedTimes == null) {
            return false;
        }
        boolean result = false;
        for (String limitedTime : limitedTimes) {
            result |= compareLimitedTime(compareTime, limitedTime.split("-")[0], limitedTime.split("-")[1]);
        }
        return result;
    }

    public boolean compareLimitedTime(String compareTime, String limitedStartTime, String limitedEndTime) {
        if (compareTime == null || limitedStartTime == null || limitedEndTime == null) return false;
        boolean result = false;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
        try {
            Date date1 = simpleDateFormat.parse(compareTime);
            Date date2 = simpleDateFormat.parse(limitedStartTime);
            Date date3 = simpleDateFormat.parse(limitedEndTime);
            result = date1.getTime() >= date2.getTime() && date1.getTime() <= date3.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
        return result;
    }

    private void showGuestToast() {
        View view = View.inflate(this, R.layout.toast_guest, null);
        img_guest = view.findViewById(R.id.img_guest);
        txt_guest = view.findViewById(R.id.txt_guest);
        Toast guestToast = new Toast(this);
        guestToast.setView(view);
        guestToast.setDuration(Toast.LENGTH_SHORT);
        guestToast.setGravity(Gravity.CENTER, 0, 0);
        guestToast.show();
    }

    public void ShowLauncherView() {
        try {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Logger.debug(TAG, "ShowLauncherView  isTemperatureIdentified :" + isTemperatureIdentified);
                    isTemperatureIdentified = true;
                    requestFeatureStatusMap.put(0, RequestFeatureStatus.FAILED);
                    tv_message.setVisibility(View.GONE);
                    tvErrorMessage.setVisibility(View.GONE);
                    temperature_image.setVisibility(View.GONE);
                    relative_main.setVisibility(View.VISIBLE);
                    logo.setVisibility(View.VISIBLE);
                    rl_header.setVisibility(View.VISIBLE);

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                           // clearLeftFace(null);
                            isTemperatureIdentified = false;
                            Logger.debug(TAG, "ShowLauncherView  isTemperatureIdentified :" + isTemperatureIdentified);
                        }
                    }, delayMilli * 1000);
                }
            });
            Logger.debug(TAG, "ShowLauncherView  isTemperatureIdentified :" + isTemperatureIdentified);
        } catch (Exception e) {
            Logger.error(TAG, e.getMessage());
        }
    }

    private class IrCameraListener implements CameraListener {
        private Camera.Parameters cameraParameters;

        @Override
        public void onCameraOpened(Camera camera, int cameraId, int displayOrientation, boolean isMirror) {
            cameraParameters = camera.getParameters();
            previewSizeIr = camera.getParameters().getPreviewSize();
            drawHelperIr = new DrawHelper(previewSizeIr.width, previewSizeIr.height, previewViewIr.getWidth(), previewViewIr.getHeight(), displayOrientation,
                    cameraId, isMirror, false, false);
        }


        @Override
        public void onPreview(final byte[] nv21, final Camera camera) {
//                Logger.debug(TAG, "irCameraListener.onPreview");
            irData = nv21;
            if (takePicIr) {
                irBitmap = Util.convertYuvByteArrayToBitmap(nv21, cameraParameters);
                //  Log.d("irBitmap", "" + irBitmap.getByteCount() + "  byte = " + nv21.length);
                //  tackPickIr = false;
            }
        }

        @Override
        public void onCameraClosed() {
            Logger.debug(TAG, "onCameraClosed: ");
        }

        @Override
        public void onCameraError(Exception e) {
            Logger.debug(TAG, "onCameraError: " + e.getMessage());
        }

        @Override
        public void onCameraConfigurationChanged(int cameraID, int displayOrientation) {
            if (drawHelperIr != null) {
                drawHelperIr.setCameraDisplayOrientation(displayOrientation);
            }
            Logger.debug(TAG, "onCameraConfigurationChanged: " + cameraID + "  " + displayOrientation);
        }
    }

    private class RgbCameraListener implements CameraListener {
        private final FaceListener faceListener;

        public RgbCameraListener(FaceListener faceListener) {
            this.faceListener = faceListener;
        }

        @Override
        public void onCameraOpened(Camera camera, int cameraId, int displayOrientation, boolean isMirror) {
            previewSize = camera.getParameters().getPreviewSize();
            drawHelperRgb = new DrawHelper(previewSize.width, previewSize.height, previewViewRgb.getWidth(), previewViewRgb.getHeight(), displayOrientation,
                    cameraId, isMirror, false, false);
            if (faceHelperIr == null) {
                faceHelperIr = new FaceHelper.Builder()
                        .ftEngine(faceEngineHelper.getFtEngine())
                        .frEngine(faceEngineHelper.getFrEngine())
                        .flEngine(faceEngineHelper.getFlEngine())
                        .frQueueSize(MAX_DETECT_NUM)
                        .flQueueSize(MAX_DETECT_NUM)
                        .previewSize(previewSize)
                        .faceListener(faceListener)
                        .trackedFaceCount(ConfigUtil.getTrackedFaceCount(IrCameraActivity.this.getApplicationContext()))
                        .build();
            }
        }

        @Override
        public void onPreview(final byte[] nv21, final Camera camera) {
            processPreviewData(nv21);
            if (takePicRgb) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        rgbBitmap = Util.convertYuvByteArrayToBitmap(nv21, camera);
                    }
                });
            }
        }

        @Override
        public void onCameraClosed() {
            Logger.debug(TAG, "onCameraClosed: ");
        }

        @Override
        public void onCameraError(Exception e) {
            Logger.debug(TAG, "onCameraError: " + e.getMessage());
        }

        @Override
        public void onCameraConfigurationChanged(int cameraID, int displayOrientation) {
            if (drawHelperRgb != null) {
                drawHelperRgb.setCameraDisplayOrientation(displayOrientation);
            }
            Logger.debug(TAG, "onCameraConfigurationChanged: " + cameraID + "  " + displayOrientation);
        }
    }

    private void tempMessageUi(float temperatureInput) {
        try {
            tvErrorMessage.setVisibility(View.GONE);
            countTempError = 0;
            String text = "";
//            tackPickIr = false;
//            tackPickRgb = false;
            if (sharedPreferences.getString(GlobalParameters.F_TO_C, "F").equals("F")) {
                temperature = Util.FahrenheitToCelcius(temperatureInput);
            } else {
                temperature = temperatureInput;
            }
            String tempString = String.format("%,.1f", temperature);
            Logger.debug(TAG, "tempString =" + tempString);
            String testing_tempe = sharedPreferences.getString(GlobalParameters.TEMP_TEST, "99");
            Float tmpFloat = Float.parseFloat(testing_tempe);
            if (temperature > tmpFloat) {
                if (sharedPreferences.getString(GlobalParameters.F_TO_C, "F").equals("F")) {
                    text = getString(R.string.temperature_anormaly) + tempString + getString(R.string.fahrenheit_symbol);
                } else {
                    text = getString(R.string.temperature_anormaly) + tempString + getString(R.string.centi);
                }
//                mTemperatureListenter.onTemperatureCall(true, text);
                TemperatureCallBackUISetup(true, text, tempString, true);
//                if (Util.isConnectingToInternet(IrCameraActivity.this) && (sharedPreferences.getString(GlobalParameters.ONLINE_MODE, "").equals("true"))) {
//                    if (sharedPreferences.getBoolean(GlobalParameters.CAPTURE_IMAGES_ALL, false) || sharedPreferences.getBoolean(GlobalParameters.CAPTURE_IMAGES_ABOVE, true))
//                        Util.recordUserTemperature(null, IrCameraActivity.this, tempString, irBitmap, rgbBitmap, temperatureBitmap, true);
//                    else
//                        Util.recordUserTemperature(null, IrCameraActivity.this, tempString, null, null, null, true);
//                }
            } else {
                Logger.debug(TAG, "tempMessageUi = ");
                if (sharedPreferences.getString(GlobalParameters.F_TO_C, "F").equals("F")) {
                    text = getString(R.string.temperature_normal) + tempString + getString(R.string.fahrenheit_symbol);
                } else {
                    text = getString(R.string.temperature_normal) + tempString + getString(R.string.centi);
                }
//                mTemperatureListenter.onTemperatureCall(false, text);
                TemperatureCallBackUISetup(false, text, tempString, true);
                // removed Internet
//                if ((sharedPreferences.getString(GlobalParameters.ONLINE_MODE, "").equals("true"))) {
//                    if (sharedPreferences.getBoolean(GlobalParameters.CAPTURE_IMAGES_ALL, false))
//                        Util.recordUserTemperature(null, IrCameraActivity.this, tempString, irBitmap, rgbBitmap, temperatureBitmap, false);
//                    else
//                        Util.recordUserTemperature(null, IrCameraActivity.this, tempString, null, null, null, false);
//                }
            }
//            rgbBitmap = null;
//            irBitmap = null;
//            temperatureBitmap = null;
        } catch (Exception e) {

        }
    }

    private class HotImageCallbackImpl extends HotImageCallback.Stub {
        @Override
        public void onTemperatureFail(final String e) throws RemoteException {
            Log.e(TAG, String.format("onTemperatureFail(String) countTempError: %d, error: %s ", countTempError, e));
            if (e != null) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject obj = new JSONObject(e);
                            String error = obj.getString("err");
                            //   Toast.makeText(IrCameraActivity.this,obj.getString("err"),Toast.LENGTH_SHORT).show();
                            // if (obj.getString("err").equals("face out of range or for head too low")&&!isIdentified) {
                            float tmpr = 0f;
                            try {
                                tmpr = Float.parseFloat(obj.getString("temNoCorrect"));
                                Logger.debug(TAG, "tmpr = " + tmpr);
                            } catch (Exception ex) {
                                Logger.debug(TAG, "" + ex.getMessage());
                                tmpr = 0f;
                            }
                            countTempError++;
                            // if (obj.getString("err").equals("face out of range or for head too low")&&!isIdentified) {
                            if (sharedPreferences.getBoolean(GlobalParameters.ALLOW_ALL, false) && tmpr > 0 && error.contains("wrong tem , too cold")) {
//                                isTemperatureIdentified = true;
//                                if(countTempError > 10){
//                                    tvErrorMessage.setVisibility( View.GONE );
                                String lowTemperature = sharedPreferences.getString(GlobalParameters.TEMP_TEST_LOW, "93.2");
                                Float lowThresholdTemperature = Float.parseFloat(lowTemperature);
                                String temperaturePreference = sharedPreferences.getString(GlobalParameters.F_TO_C, "F");
                                if (temperaturePreference.equals("F")) {
                                    lowTempValue = Util.FahrenheitToCelcius(tmpr);
                                } else {
                                    lowTempValue = lowThresholdTemperature;
                                }
                                Log.d("low temp", "" + lowTempValue + "tempcelcius" + tmpr + "" + lowTemperature);
//
                                if (lowTempValue > lowThresholdTemperature && !isTemperatureIdentified) {
                                    tempMessageUi(tmpr);//TODO: add this method
                                } else {
                                    tvErrorMessage.setVisibility(tempServiceClose && isTemperatureIdentified ? View.GONE : View.VISIBLE);
                                    tvErrorMessage.setText(sharedPreferences.getString(GlobalParameters.GUIDE_TEXT2, getResources().getString(R.string.text_value2)));
                                }
                                return;
                            }

                            if (sharedPreferences.getBoolean(GlobalParameters.GUIDE_SCREEN, true)) {
                                tvErrorMessage.setVisibility(tempServiceClose ? View.GONE : View.VISIBLE);
                                if (error.contains("face out of range or for head too low"))
                                    tvErrorMessage.setText(sharedPreferences.getString(GlobalParameters.GUIDE_TEXT1, getResources().getString(R.string.text_value1)));
                                else if (error.contains("wrong tem , too cold"))
                                    tvErrorMessage.setText(sharedPreferences.getString(GlobalParameters.GUIDE_TEXT2, getResources().getString(R.string.text_value2)));
                                else if (error.contains("not enough validData , get tem fail"))
                                    tvErrorMessage.setText(sharedPreferences.getString(GlobalParameters.GUIDE_TEXT3, getResources().getString(R.string.text_value3)));
                                else
                                    tvErrorMessage.setVisibility(View.GONE);
                                //   }
                            }
                            if (error.contains("face out of range or for head too low") || error.contains("wrong tem , too cold") || error.contains("not enough validData , get tem fail"))
                                outerCircle.setBackgroundResource(R.drawable.border_shape_red);
//                            ++countTempError;
                            if (countTempError >= 10) {
//                                RestartAppOnTooManyErrors(error);
                            }
                        } catch (Exception ee) {

                        }
                    }

                    private void RestartAppOnTooManyErrors(String error) {
                        Log.v(TAG, "RestartAppOnTooManyErrors");
                        try {
                            // Toast.makeText(IrCameraActivity.this,""+countTempError,Toast.LENGTH_SHORT).show();
                            clearLeftFace(null);
                            if (error.equals("not enough validData , get tem fail , please get again")) {
                                Log.w(TAG, "onTemperatureFail  KillApp ->");
                                Util.KillApp();
                                // StopView();
                                Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.telpo.temperatureservice");
                                if (launchIntent != null) {
                                    startActivity(launchIntent);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
            retry(tempretrynum);
        }

        @Override
        public void getTemperatureBimapData(final TemperatureBitmapData data) throws RemoteException {
            temperatureBitmap = data.getBitmap();
            Logger.debug(TAG + " getTemperatureBimapData ->", "getTemperatureBimapData");

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (data.getBitmap() != null) {
                        temperature_image.setVisibility(tempServiceClose ? View.GONE : View.VISIBLE);
                        temperature_image.setImageBitmap(data.getBitmap());


                    }
                }
            });
        }


    }

    private class FaceEngineHelper {
        private FaceEngine ftEngine;
        private FaceEngine frEngine;
        private FaceEngine flEngine;

        private int ftInitCode = -1;
        private int frInitCode = -1;
        private int flInitCode = -1;

        public FaceEngine getFtEngine() {
            return ftEngine;
        }

        public FaceEngine getFlEngine() {
            return flEngine;
        }

        public FaceEngine getFrEngine() {
            return frEngine;
        }

        public void initEngine(Context context) {
            ftEngine = new FaceEngine();
            ftInitCode = ftEngine.init(context, DetectMode.ASF_DETECT_MODE_VIDEO, ConfigUtil.getFtOrient(context),
                    16, MAX_DETECT_NUM, FaceEngine.ASF_FACE_DETECT);

            frEngine = new FaceEngine();
            frInitCode = frEngine.init(context, DetectMode.ASF_DETECT_MODE_IMAGE, DetectFaceOrientPriority.ASF_OP_0_ONLY,
                    16, MAX_DETECT_NUM, FaceEngine.ASF_FACE_RECOGNITION);

            flEngine = new FaceEngine();
            flInitCode = flEngine.init(context, DetectMode.ASF_DETECT_MODE_IMAGE, DetectFaceOrientPriority.ASF_OP_0_ONLY,
                    16, MAX_DETECT_NUM, FaceEngine.ASF_IR_LIVENESS);

            Logger.debug(TAG, "initEngine:  init: " + ftInitCode);

            if (ftInitCode != ErrorInfo.MOK) {
                String error = getString(R.string.specific_engine_init_failed, "ftEngine", ftInitCode);
                Logger.debug(TAG, "initEngine: " + error);
                // Toast.makeText(this,error,Toast.LENGTH_SHORT).show();
            }
            if (frInitCode != ErrorInfo.MOK) {
                String error = getString(R.string.specific_engine_init_failed, "frEngine", ftInitCode);
                Logger.debug(TAG, "initEngine: " + error);
                // Toast.makeText(this,error,Toast.LENGTH_SHORT).show();
            }
            if (flInitCode != ErrorInfo.MOK) {
                String error = getString(R.string.specific_engine_init_failed, "flEngine", ftInitCode);
                Logger.debug(TAG, "initEngine: " + error);
                // Toast.makeText(this,error,Toast.LENGTH_SHORT).show();
            }
        }


        public void unInitEngine() {
            if (ftInitCode == ErrorInfo.MOK && ftEngine != null) {
                synchronized (ftEngine) {
                    int ftUnInitCode = ftEngine.unInit();
                    Logger.debug(TAG, "unInitEngine: " + ftUnInitCode);
                }
            }
            if (frInitCode == ErrorInfo.MOK && frEngine != null) {
                synchronized (frEngine) {
                    int frUnInitCode = frEngine.unInit();
                    Logger.debug(TAG, "unInitEngine: " + frUnInitCode);
                }
            }
            if (flInitCode == ErrorInfo.MOK && flEngine != null) {
                synchronized (flEngine) {
                    int flUnInitCode = flEngine.unInit();
                    Logger.debug(TAG, "unInitEngine: " + flUnInitCode);
                }
            }
        }
    }

    private void TemperatureCallBackUISetup(final boolean aboveThreshold, final String temperature, final String tempValue, final boolean lowTemp) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                isTemperatureIdentified = true;
                outerCircle.setBackgroundResource(R.drawable.border_shape);
                tvErrorMessage.setVisibility(View.GONE);
                //   temperature_image.setImageBitmap(rgbBitmap);
                if (tempServiceClose) {
                    relative_main.setVisibility(View.GONE);
                    logo.setVisibility(View.GONE);
                    rl_header.setVisibility(View.GONE);
                }
                // requestFeatureStatusMap.put(requestId, RequestFeatureStatus.FAILED);
                Logger.debug(TAG, "CAPTURE_TEMPERATURE : " + sharedPreferences.getBoolean(GlobalParameters.CAPTURE_TEMPERATURE, true));
                if (sharedPreferences.getBoolean(GlobalParameters.CAPTURE_TEMPERATURE, true)) {
                    tv_message.setVisibility(View.VISIBLE);
                } else {
                    tv_message.setVisibility(View.GONE);
                }

                tv_message.setTextColor(getResources().getColor(R.color.white));
                if (lowTemp)
                    tv_message.setBackgroundColor(getResources().getColor(R.color.bg_blue));
                else
                    tv_message.setBackgroundColor(aboveThreshold ? getResources().getColor(R.color.red) : getResources().getColor(R.color.bg_green));
                tv_message.setText(temperature);
                tv_message.setTypeface(rubiklight);
                if (sharedPreferences.getBoolean(GlobalParameters.CAPTURE_SOUND, false)) {
                    if (aboveThreshold) {
                        Util.soundPool(IrCameraActivity.this, "high", soundPool);
                    } else {
                        Util.soundPool(IrCameraActivity.this, "normal", soundPool);
                    }
                }
                if (lanchTimer != null)
                    lanchTimer.cancel();
                lanchTimer = new Timer();
                lanchTimer.schedule(new TimerTask() {
                    public void run() {
                        takePicRgb = true;
                        takePicIr = true;

                        //  requestFeatureStatusMap.put(requestId, RequestFeatureStatus.FAILED);
                        // temperature_image.setVisibility(View.GONE);
                        boolean confirmAboveScreen = sharedPreferences.getBoolean(GlobalParameters.CONFIRM_SCREEN_ABOVE, true) && aboveThreshold;
                        boolean confirmBelowScreen = sharedPreferences.getBoolean(GlobalParameters.CONFIRM_SCREEN_BELOW, true) && !aboveThreshold;
                        if (confirmAboveScreen || confirmBelowScreen) {
                            Intent intent = new Intent(IrCameraActivity.this, ConfirmationScreenActivity.class);
                            intent.putExtra("tempVal", aboveThreshold ? "high" : "");
                            startActivity(intent);
                            ConfirmationBoolean = true;
                            finish();
                        } else {
                            ShowLauncherView();
                        }
                        // }
                        //     clearLeftFace(null);
                    }
                }, delayMilli * 1000);
                //   if (Util.isConnectingToInternet(IrCameraActivity.this) && (sharedPreferences.getString(GlobalParameters.ONLINE_MODE, "").equals("true"))) {
//                                    if (sharedPreferences.getBoolean(GlobalParameters.CAPTURE_IMAGES_ALL, false) || sharedPreferences.getBoolean(GlobalParameters.CAPTURE_IMAGES_ABOVE, true))
//                                        Util.recordUserTemperature(IrCameraActivity.this, IrCameraActivity.this, tempString, irBitmap, rgbBitmap, temperatureBitmap, false);
//                                    else
//                                        Util.recordUserTemperature(IrCameraActivity.this, IrCameraActivity.this, tempString, null, null, null, false);
//                                }
                if (Util.isConnectingToInternet(IrCameraActivity.this) && (sharedPreferences.getString(GlobalParameters.ONLINE_MODE, "").equals("true"))) {
                    boolean sendAboveThreshold = sharedPreferences.getBoolean(GlobalParameters.CAPTURE_IMAGES_ABOVE, true) && aboveThreshold;
                    if (sharedPreferences.getBoolean(GlobalParameters.CAPTURE_IMAGES_ALL, false) || sendAboveThreshold)
                        Util.recordUserTemperature(null, IrCameraActivity.this, tempValue, irBitmap, rgbBitmap, temperatureBitmap, aboveThreshold);
                    else
                        Util.recordUserTemperature(null, IrCameraActivity.this, tempValue, null, null, null, aboveThreshold);
                }
                //requestFeatureStatusMap.put(requestId, RequestFeatureStatus.FAILED);
                //  faceHelper.setName(requestId, getString(R.string.VISITOR) + requestId);
            }
        });

    }
}