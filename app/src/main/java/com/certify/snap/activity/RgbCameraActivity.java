package com.certify.snap.activity;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
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
import android.graphics.drawable.ColorDrawable;
import android.hardware.Camera;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.FaceFeature;
import com.arcsoft.face.LivenessInfo;
import com.arcsoft.face.enums.DetectFaceOrientPriority;
import com.arcsoft.face.enums.DetectMode;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.certify.snap.arcface.model.FacePreviewInfo;
import com.certify.snap.arcface.util.DrawHelper;
import com.certify.snap.arcface.util.camera.CameraHelper;
import com.certify.snap.arcface.util.camera.CameraListener;
import com.certify.snap.arcface.util.face.FaceHelper;
import com.certify.snap.arcface.util.face.FaceListener;
import com.certify.snap.arcface.util.face.LivenessType;
import com.certify.snap.arcface.util.face.RequestFeatureStatus;
import com.certify.snap.arcface.util.face.RequestLivenessStatus;
import com.certify.snap.arcface.widget.ShowFaceInfoAdapter;
import com.certify.snap.common.Application;
import com.certify.snap.common.ConfigUtil;
import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.M1CardUtils;
import com.certify.snap.common.Util;
import com.certify.snap.faceserver.FaceServer;
import com.certify.snap.model.GuestMembers;
import com.certify.snap.model.OfflineGuestMembers;
import com.certify.snap.model.OfflineVerifyMembers;
import com.certify.snap.model.RegisteredMembers;
import com.common.thermalimage.HotImageCallback;
import com.common.thermalimage.TemperatureBitmapData;
import com.common.thermalimage.TemperatureData;
import com.google.zxing.other.BeepManager;
import com.certify.snap.R;
import com.certify.snap.faceserver.CompareResult;
import com.certify.snap.view.MyGridLayoutManager;

import org.litepal.LitePal;

import java.io.File;
import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
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

public class RgbCameraActivity extends Activity implements ViewTreeObserver.OnGlobalLayoutListener {

    private Toast toast = null;
    private static final String TAG = "RgbCameraActivity";
    ImageView logo, loaddialog, scan, outerCircle, innerCircle, exit;
    private ObjectAnimator outerCircleAnimator, innerCircleAnimator;
    private ProcessHandler processHandler;
    private RelativeLayout relativeLayout;
    private long exitTime = 0;
    private int pressTimes = 0;
    private static final int GUEST_QR_CODE = 333;
    public static final int HIDE_VERIFY_UI = 334;
    private static final int CARD_ID_ERROR = 335;
    private static final int ENTER = 336;
    private static final int TIME_ERROR = 337;
    OfflineVerifyMembers offlineVerifyMembers;
    List<RegisteredMembers> registeredMemberslist;
    private RelativeLayout rlHeader;
    private boolean isIdentified;


    private View previewView;

    private TextView tv_display_time, tv_message, template_view;

    Timer tTimer, pTimer, imageTimer;

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

    private CameraHelper cameraHelper;
    private DrawHelper drawHelper;
    private Camera.Size previewSize;

    private Integer rgbCameraID = Camera.CameraInfo.CAMERA_FACING_BACK;

    private FaceEngine ftEngine;

    private FaceEngine frEngine;

    private FaceEngine flEngine;

    private int ftInitCode = -1;
    private int frInitCode = -1;
    private int flInitCode = -1;

    private FaceHelper faceHelper;
    private List<CompareResult> compareResultList;
    private ShowFaceInfoAdapter adapter;
    SharedPreferences sp;


    private static final String[] NEEDED_PERMISSIONS = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.READ_PHONE_STATE
    };

    private AlertDialog.Builder builder;

    private int relaytimenumber = 5;
    private AnimatorSet animatorSet;
    ImageView img_telpo, img_guest, temperature_image,img_temperature;
    TextView txt_guest;
    String message;
    private BeepManager mBeepManager,mBeepManager1,mBeepManager2, manormalBeep,malertBeep,mBeepSuccess;
    private WallpaperBroadcastReceiver wallpaperBroadcastReceiver;
    public static final String WALLPAPER_CHANGE = "com.telpo.telpo_face_system_wallpaper";

    private Bitmap temperatureBitmap = null;
    private int tempretrynum = 0;
    private int retrytemp = 0;
    private boolean isCalibrating = true;
    private boolean isTemperature = true;
    private boolean isSearch = true;
    private final Object obj = new Object();
    TemperatureListenter mTemperatureListenter;
    private float temperature = 0;

    public interface TemperatureListenter {
        void onTemperatureCall(boolean result, String temperature);
    }

    public void setonTemperatureListenter(TemperatureListenter temperatureListenter) {
        mTemperatureListenter = temperatureListenter;
    }
    private NfcAdapter mNfcAdapter;
    private Tag mTag;
    private PendingIntent mPendingIntent;
    private boolean isDetected = false;
    private SwipeCardThread mSwipeCardThread;
    private int DATA_BLOCK = 8;
    private final byte[] password1 = new byte[]{(byte) 0x80, (byte) 0x60,
            (byte) 0x30, (byte) 0x30, (byte) 0x70, (byte) 0x80};
    private AlertDialog nfcDialog;
    class WallpaperBroadcastReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null && intent.getAction().equals(WALLPAPER_CHANGE)) {
                showWallpaper();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_rgb);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Application.getInstance().addActivity(this);

        FaceServer.getInstance().init(this);//init FaceServer;
        initView();

        processHandler = new ProcessHandler(this);

        mNfcAdapter = M1CardUtils.isNfcAble(this);
        mPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

//        try {
//            LitePal.getDatabase();
//        }catch (Exception e){
//            e.printStackTrace();
//        }

        sp = Util.getSharedPreferences(this);
        relaytimenumber = sp.getInt(GlobalParameters.RelayTime, 5);
        Log.e("relaytimenumber---", relaytimenumber + "");
        GlobalParameters.livenessDetect = sp.getBoolean(GlobalParameters.LivingType, true);

        if (sp.getBoolean("wallpaper", false)) {
            showWallpaper();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null)
            mTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        startDetectCard();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mNfcAdapter != null) {
            mNfcAdapter.enableForegroundDispatch(this, mPendingIntent, null, null);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mNfcAdapter != null) {
            mNfcAdapter.disableForegroundDispatch(this);
        }
    }

    @Override
    public void onBackPressed() {
        Application.getInstance().exit();
    }

    private void initView() {
        previewView = findViewById(R.id.texture_preview);

        img_telpo = findViewById(R.id.img_telpo);

        relativeLayout = findViewById(R.id.rl_verify);
        outerCircle = findViewById(R.id.iv_verify_outer_circle);
        innerCircle = findViewById(R.id.iv_verify_inner_circle);

        previewView.getViewTreeObserver().addOnGlobalLayoutListener(this);

        tv_display_time = findViewById(R.id.tv_display_time);
        tv_message = findViewById(R.id.tv_message);
        rlHeader = findViewById(R.id.rl_header);
        tTimer = new Timer();
        tTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//yyyy-MM-dd HH:mm:ss
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

       // logo = findViewById(R.id.logo);
        rlHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                View view = LayoutInflater.from(RgbCameraActivity.this).inflate(R.layout.layout_login, null);
                final EditText etPassword = view.findViewById(R.id.et_password);

                builder = new AlertDialog.Builder(RgbCameraActivity.this).setView(view).setTitle(getString(R.string.login)).setIcon(R.drawable.logo_small)

                        .setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                if (etPassword.getText().toString().equals(sp.getString("device_password", "123456"))) {
                                    Intent loginIt = new Intent(RgbCameraActivity.this, SettingActivity.class);
                                    startActivity(loginIt);
                                    finish();
                                } else {
                                    Toast.makeText(RgbCameraActivity.this, getString(R.string.toast_rgbir_pwderror)
                                            , Toast.LENGTH_LONG).show();
                                }
                            }
                        }).setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });

                builder.create().show();

            }
        });
        logo.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                View view = LayoutInflater.from(RgbCameraActivity.this).inflate(R.layout.layout_login, null);
               // final EditText etUsername = view.findViewById(R.id.et_username);
                final EditText etPassword = view.findViewById(R.id.et_password);

                builder = new AlertDialog.Builder(RgbCameraActivity.this).setView(view).setTitle(getString(R.string.logout)).setIcon(R.drawable.logo_small)
                        .setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                if (etPassword.getText().toString().equals(sp.getString("device_password", "123456"))) {
                                    sendBroadcast(new Intent(GlobalParameters.ACTION_OPEN_STATUSBAR));
                                    sendBroadcast(new Intent(GlobalParameters.ACTION_SHOW_NAVIGATIONBAR));
                                    Application.getInstance().exit();
                                } else {
                                    Toast.makeText(RgbCameraActivity.this, getString(R.string.toast_rgbir_pwderror)
                                            , Toast.LENGTH_LONG).show();
                                }
                            }
                        }).setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });

                builder.create().show();

                return true;
            }
        });

        scan = findViewById(R.id.scan);
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
                        Toast.makeText(RgbCameraActivity.this, getString(R.string.toast_ocrnotinstall), Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        exit = findViewById(R.id.exit);
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendBroadcast(new Intent(GlobalParameters.ACTION_OPEN_STATUSBAR));
                sendBroadcast(new Intent(GlobalParameters.ACTION_SHOW_NAVIGATIONBAR));

                Application.getInstance().exit();
            }
        });

        template_view = findViewById(R.id.template_view);
        temperature_image = findViewById(R.id.temperature_image);
        img_temperature = findViewById(R.id.img_temperature);

    }

    private void showTip(final String msg,final boolean isplaysound) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv_message.setText(msg);
                if(isplaysound)  manormalBeep.playBeepSoundAndVibrate();
            }
        });
    }


    private void initEngine() {
        ftEngine = new FaceEngine();
        ftInitCode = ftEngine.init(this, DetectMode.ASF_DETECT_MODE_VIDEO, ConfigUtil.getFtOrient(this),
                16, MAX_DETECT_NUM, FaceEngine.ASF_FACE_DETECT);

        frEngine = new FaceEngine();
        frInitCode = frEngine.init(this, DetectMode.ASF_DETECT_MODE_IMAGE, DetectFaceOrientPriority.ASF_OP_0_ONLY,
                16, MAX_DETECT_NUM, FaceEngine.ASF_FACE_RECOGNITION);

        flEngine = new FaceEngine();
        flInitCode = flEngine.init(this, DetectMode.ASF_DETECT_MODE_IMAGE, DetectFaceOrientPriority.ASF_OP_0_ONLY,
                16, MAX_DETECT_NUM, FaceEngine.ASF_LIVENESS);

        Log.i(TAG, "initEngine:  init: " + ftInitCode);

        if (ftInitCode != ErrorInfo.MOK) {
            String error = getString(R.string.specific_engine_init_failed, "ftEngine", ftInitCode);
            Log.i(TAG, "initEngine: " + error);
            // Toast.makeText(this,error,Toast.LENGTH_SHORT).show();
        }
        if (frInitCode != ErrorInfo.MOK) {
            String error = getString(R.string.specific_engine_init_failed, "frEngine", frInitCode);
            Log.i(TAG, "initEngine: " + error);
            // Toast.makeText(this,error,Toast.LENGTH_SHORT).show();
        }
        if (flInitCode != ErrorInfo.MOK) {
            String error = getString(R.string.specific_engine_init_failed, "flEngine", flInitCode);
            Log.i(TAG, "initEngine: " + error);
            // Toast.makeText(this,error,Toast.LENGTH_SHORT).show();
        }
    }


    private void unInitEngine() {
        if (ftInitCode == ErrorInfo.MOK && ftEngine != null) {
            synchronized (ftEngine) {
                int ftUnInitCode = ftEngine.unInit();
                Log.i(TAG, "unInitEngine: " + ftUnInitCode);
            }
        }
        if (frInitCode == ErrorInfo.MOK && frEngine != null) {
            synchronized (frEngine) {
                int frUnInitCode = frEngine.unInit();
                Log.i(TAG, "unInitEngine: " + frUnInitCode);
            }
        }
        if (flInitCode == ErrorInfo.MOK && flEngine != null) {
            synchronized (flEngine) {
                int flUnInitCode = flEngine.unInit();
                Log.i(TAG, "unInitEngine: " + flUnInitCode);
            }
        }
    }


    @Override
    protected void onDestroy() {
        if (mSwipeCardThread != null) {
            mSwipeCardThread.interrupt();
            mSwipeCardThread = null;
//            Log.e("stop thread","success");
        }
        if(nfcDialog!=null&&nfcDialog.isShowing()) {
            nfcDialog.dismiss();
            nfcDialog = null;
        }
        processHandler.removeCallbacksAndMessages(null);
        if(tTimer!=null)
            tTimer.cancel();
        if(pTimer!=null)
            pTimer.cancel();
        if(imageTimer!=null)
            imageTimer.cancel();
        if (cameraHelper != null) {
            cameraHelper.release();
            cameraHelper = null;
        }

        FaceServer.getInstance().unInit();
        unInitEngine();
        if (getFeatureDelayedDisposables != null) {
            getFeatureDelayedDisposables.clear();
        }
        if (delayFaceTaskCompositeDisposable != null) {
            delayFaceTaskCompositeDisposable.clear();
        }
        if (faceHelper != null) {
            ConfigUtil.setTrackedFaceCount(this, faceHelper.getTrackedFaceCount());
            faceHelper.release();
            faceHelper = null;
        }
        // FaceServer.getInstance().unInit();
        stopAnimation();
        cancelImageTimer();
        super.onDestroy();
    }

    public void runTemperature() {
        isTemperature = false;
        isSearch = false;
        time1 = time2 = 0;
        time1 = System.currentTimeMillis();
        temperature = 0;
        synchronized (obj) {
            new Thread(new Runnable() {
                @Override
                public void run() {

                    try {
                        TemperatureData temperatureData = Application.getInstance().getTemperatureUtil()
                                .getDataAndBitmap(50, true, new HotImageCallback.Stub() {
                            @Override
                            public void onTemperatureFail(String e) throws RemoteException {
                                // TODO Auto-generated method stub
                                Log.e("tempfail---", "onTemperatureFail " + e);
                                retry(tempretrynum);
                            }

                            @Override
                            public void getTemperatureBimapData(final TemperatureBitmapData data) throws RemoteException {
                                // TODO Auto-generated method stub
                                temperatureBitmap = data.getBitmap();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (data.getBitmap() != null) {
                                            temperature_image.setVisibility(View.VISIBLE);
                                            temperature_image.setImageBitmap(data.getBitmap());
                                        }
                                    }
                                });
                            }
                        });

                        if (temperatureData != null) {
                            String text = "";
                            temperature = Util.FahrenheitToCelcius(temperatureData.getTemperature());
                            String tempString = String.format ("%,.1f", temperature);

                            Log.e("temperature str-",temperature+"");
                            if (temperatureData.isUnusualTem()) {
                                text = getString(R.string.temperature_anormaly) + tempString + getString(R.string.fahrenheit_symbol);
                                Log.e("temperatureBitmap",""+(temperatureBitmap==null));
                                mTemperatureListenter.onTemperatureCall(true, text);
                            } else {
                                text = getString(R.string.temperature_normal) + tempString + getString(R.string.fahrenheit_symbol);
                                mTemperatureListenter.onTemperatureCall(false, text);
                                Log.e("temperture---", "isUnusualTem-" + temperatureData.isUnusualTem() + "-" + text);
                            }
                        } else {
                            Log.e("temperatureData---", isTemperature + "-temperature data = null");
                        }

                    }catch(Exception e){
                        e.printStackTrace();
                        Log.e("测温结果catch-",e.toString());
                        retry(retrytemp);
                    }

                }
            }).start();

        }
    }

    private void retry(int retrynumber){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                img_temperature.setVisibility(View.GONE);
            }
        });
        if (retrynumber < 3) {
            Log.e("getDataAndBitmap---", retrytemp + "-" + tempretrynum );
            runTemperature();
            retrynumber++;
           // showTip(getString(R.string.temperature_retry), false);
        } else {
            showTip(getString(R.string.temperature_failresult), false);
            isSearch = true;
        }
    }

    long time1,time2;
    private void initCamera() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        final FaceListener faceListener = new FaceListener() {
            @Override
            public void onFail(Exception e) {
                Log.e(TAG, "imagefaillllllllllll: " + e.getMessage());
            }

            @Override
            public void onFaceFeatureInfoGet(@Nullable final FaceFeature faceFeature, final Integer requestId, final Integer errorCode) {
                if (faceFeature != null) {
//                    Log.i(TAG, "onPreview: fr end = " + System.currentTimeMillis() + " trackId = " + requestId);

                    isIdentified = false;
                    if (compareResultList != null) {
                        for (int i = compareResultList.size() - 1; i >= 0; i--) {
                            if (compareResultList.get(i).getTrackId() == requestId) {
                                isIdentified = true;
                                break;
                            }
                        }
                        if (!isIdentified) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    changeVerifyBackground(R.color.transparency, true);
                                    img_telpo.setVisibility(View.GONE);
                                    logo.setVisibility(View.GONE);
                                    showAnimation();

                                    setonTemperatureListenter(new TemperatureListenter() {
                                        @Override
                                        public void onTemperatureCall(final boolean result, final String temperature) {
                                            Log.e("TemperatureListenter---", "isnormal=" + result + "-" + temperature );
                                            isTemperature = true;
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    if(result) malertBeep.playBeepSoundAndVibrate();
                                                    img_temperature.setVisibility(View.VISIBLE);
                                                    img_temperature.setBackgroundResource(result?R.mipmap.temp_red:R.mipmap.temp_green);
                                                    tv_message.setTextColor(getResources().getColor(R.color.dark_text_color));
                                                   // tv_message.setTextColor(result? getResources().getColor(R.color.red) : getResources().getColor(R.color.green));
                                                    tv_message.setText(temperature);
                                                }
                                            });
                                        }
                                    });

                                    //Log.e("runTemperature---","isIdentified="+isIdentified);
                                    if (isCalibrating && isSearch) runTemperature();

                                    cancelImageTimer();
                                    imageTimer = new Timer();
                                    imageTimer.schedule(new TimerTask() {
                                        public void run() {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Log.e("imagetimer---","start");
                                                    isSearch = true;
                                                    img_telpo.setVisibility(View.VISIBLE);
                                                    logo.setVisibility(View.VISIBLE);
                                                    stopAnimation();
                                                    tv_message.setText("");
                                                    img_temperature.setVisibility(View.GONE);
                                                    Util.enableLedPower(0);
                                                }
                                            });

                                            this.cancel();
                                        }
                                    }, 3* 1000);//20
                                }
                            });
                        }
                    }

                    Integer liveness = livenessMap.get(requestId);
                    if (!GlobalParameters.livenessDetect) {
                        searchFace(faceFeature, requestId);
                    }
                    else if (liveness != null && liveness == LivenessInfo.ALIVE) {
                        Log.e("liveness---", "LivenessInfo.ALIVE---"+isTemperature);
                        searchFace(faceFeature, requestId);
                    }
                    else {
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

                                        }

                                        @Override
                                        public void onComplete() {
                                            getFeatureDelayedDisposables.remove(disposable);
                                        }
                                    });
                        }
                    }

                }
                else {
                    if (increaseAndGetValue(extractErrorRetryMap, requestId) > MAX_RETRY_TIME) {
                        extractErrorRetryMap.put(requestId, 0);

                        String msg;
                        if (errorCode != null && errorCode == ErrorInfo.MERR_FSDK_FACEFEATURE_LOW_CONFIDENCE_LEVEL) {
                            msg = getString(R.string.low_confidence_level);
                        } else {
                            msg = getString(R.string.ExtractCode) + errorCode;
                        }
                        faceHelper.setName(requestId, getString(R.string.recognize_failed_notice, msg));
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
                        faceHelper.setName(requestId, getString(R.string.recognize_failed_notice, "NOT_ALIVE"));
                        retryLivenessDetectDelayed(requestId);
                    }
                } else {
                    if (increaseAndGetValue(livenessErrorRetryMap, requestId) > MAX_RETRY_TIME) {
                        livenessErrorRetryMap.put(requestId, 0);
                        String msg;
                        if (errorCode != null && errorCode == ErrorInfo.MERR_FSDK_FACEFEATURE_LOW_CONFIDENCE_LEVEL) {
                            msg = getString(R.string.low_confidence_level);
                        } else {
                            msg = getString(R.string.ProcessCode) + errorCode;
                        }
                        faceHelper.setName(requestId, getString(R.string.recognize_failed_notice, msg));
                        retryLivenessDetectDelayed(requestId);
                    } else {
                        livenessMap.put(requestId, LivenessInfo.UNKNOWN);
                    }
                }
            }


        };


        CameraListener cameraListener = new CameraListener() {
            @Override
            public void onCameraOpened(Camera camera, int cameraId, int displayOrientation, boolean isMirror) {
                previewSize = camera.getParameters().getPreviewSize();
                drawHelper = new DrawHelper(previewSize.width, previewSize.height, previewView.getWidth(), previewView.getHeight(), displayOrientation,
                        cameraId, isMirror, false, false);
                if (faceHelper == null) {
                    faceHelper = new FaceHelper.Builder()
                            .ftEngine(ftEngine)
                            .frEngine(frEngine)
                            .flEngine(flEngine)
                            .frQueueSize(MAX_DETECT_NUM)
                            .flQueueSize(MAX_DETECT_NUM)
                            .previewSize(previewSize)
                            .faceListener(faceListener)
                            .trackedFaceCount(ConfigUtil.getTrackedFaceCount(RgbCameraActivity.this.getApplicationContext()))
                            .build();
                }
            }

            @Override
            public void onPreview(final byte[] nv21, Camera camera) {

                List<FacePreviewInfo> facePreviewInfoList = faceHelper.onPreviewFrame(nv21);
//                Log.e(TAG,"facePreviewInfoList size : "+ facePreviewInfoList.size());

                clearLeftFace(facePreviewInfoList);

                if (facePreviewInfoList != null && facePreviewInfoList.size() > 0 && previewSize != null) {
                    for (int i = 0; i < facePreviewInfoList.size(); i++) {
                        Integer status = requestFeatureStatusMap.get(facePreviewInfoList.get(i).getTrackId());

                        if (GlobalParameters.livenessDetect && (status == null || status != RequestFeatureStatus.SUCCEED)) {
                            Integer liveness = livenessMap.get(facePreviewInfoList.get(i).getTrackId());
                            if (liveness == null
                                    || (liveness != LivenessInfo.ALIVE && liveness != LivenessInfo.NOT_ALIVE && liveness != RequestLivenessStatus.ANALYZING)) {
                                livenessMap.put(facePreviewInfoList.get(i).getTrackId(), RequestLivenessStatus.ANALYZING);
                                faceHelper.requestFaceLiveness(nv21, facePreviewInfoList.get(i).getFaceInfo(), previewSize.width, previewSize.height,
                                        FaceEngine.CP_PAF_NV21, facePreviewInfoList.get(i).getTrackId(), LivenessType.RGB);
                            }
                        }

                        if (status == null
                                || status == RequestFeatureStatus.TO_RETRY) {
                            requestFeatureStatusMap.put(facePreviewInfoList.get(i).getTrackId(), RequestFeatureStatus.SEARCHING);
                            faceHelper.requestFaceFeature(nv21, facePreviewInfoList.get(i).getFaceInfo(), previewSize.width, previewSize.height, FaceEngine.CP_PAF_NV21, facePreviewInfoList.get(i).getTrackId());
//                            Log.i(TAG, "onPreview: fr start = " + System.currentTimeMillis() + " trackId = " + facePreviewInfoList.get(i).getTrackedFaceCount());
                        }
                    }
                }
            }

            @Override
            public void onCameraClosed() {
                Log.i(TAG, "onCameraClosed: ");
            }

            @Override
            public void onCameraError(Exception e) {
                Log.i(TAG, "onCameraError: " + e.getMessage());
            }

            @Override
            public void onCameraConfigurationChanged(int cameraID, int displayOrientation) {
                if (drawHelper != null) {
                    drawHelper.setCameraDisplayOrientation(displayOrientation);
                }
                Log.i(TAG, "onCameraConfigurationChanged: " + cameraID + "  " + displayOrientation);
            }
        };

        cameraHelper = new CameraHelper.Builder()
                .previewViewSize(new Point(previewView.getMeasuredWidth(), previewView.getMeasuredHeight()))
                .rotation(sp.getInt(GlobalParameters.Orientation, 0))
                .specificCameraId(rgbCameraID != null ? rgbCameraID : Camera.CameraInfo.CAMERA_FACING_FRONT)
                .isMirror(false)
                .previewOn(previewView)
                .cameraListener(cameraListener)
                .build();
        cameraHelper.init();
        cameraHelper.start();
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
                initEngine();
                initCamera();
                if (cameraHelper != null) {
                    cameraHelper.start();
                }
            } else {
                Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**

     *
     * @param facePreviewInfoList
     */
    private void clearLeftFace(List<FacePreviewInfo> facePreviewInfoList) {
        if (compareResultList != null) {
            for (int i = compareResultList.size() - 1; i >= 0; i--) {
                if (!requestFeatureStatusMap.containsKey(compareResultList.get(i).getTrackId())) {
                    Log.e("tag", "remove exist face");
                    compareResultList.remove(i);
                    adapter.notifyItemRemoved(i);
                    tv_message.setText("");
                    img_temperature.setVisibility(View.GONE);
                    stopAnimation();
                    img_telpo.setVisibility(View.VISIBLE);
                    logo.setVisibility(View.VISIBLE);
                    Util.enableLedPower(0);
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

    private void searchFace(final FaceFeature frFace, final Integer requestId) {
        try {
            Observable
                    .create(new ObservableOnSubscribe<CompareResult>() {
                        @Override
                        public void subscribe(ObservableEmitter<CompareResult> emitter) {
//                        Log.i(TAG, "subscribe: fr search start = " + System.currentTimeMillis() + " trackId = " + requestId);
                            CompareResult compareResult = FaceServer.getInstance().getTopOfFaceLib(frFace);
//                        Log.i(TAG, "subscribe: fr search end = " + System.currentTimeMillis() + " trackId = " + requestId);
                            emitter.onNext(compareResult);

                        }
                    })
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<CompareResult>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onNext(final CompareResult compareResult) {
                            if (compareResult == null || compareResult.getUserName() == null) {
                                requestFeatureStatusMap.put(requestId, RequestFeatureStatus.FAILED);
                                faceHelper.setName(requestId, getString(R.string.VISITOR) + requestId);
                                return;
                            }
//                        Log.i(TAG, "onNext: fr search get result  = " + System.currentTimeMillis() + " trackId = " + requestId + "  similar = " + compareResult.getSimilar());
                            if (compareResult.getSimilar() > SIMILAR_THRESHOLD) {
                                boolean isAdded = false;
                                if (compareResultList == null) {
                                    requestFeatureStatusMap.put(requestId, RequestFeatureStatus.FAILED);
                                    faceHelper.setName(requestId, getString(R.string.VISITOR) + requestId);
                                    return;
                                }
                                for (CompareResult compareResult1 : compareResultList) {
                                    if (compareResult1.getTrackId() == requestId) {
                                        isAdded = true;
                                        break;
                                    }
                                }
                                Log.e("onnext2---", "searchface---" + isTemperature + ",isAdd:" + isAdded);
                                if (!isAdded) {  //&& isTemperature
                                    if (compareResultList.size() >= MAX_DETECT_NUM) {
                                        compareResultList.remove(0);
                                        adapter.notifyItemRemoved(0);
                                    }
                                    isSearch = true;

                                    String[] split = compareResult.getUserName().split("-");
                                    String mobile = "";
                                    if (split != null) mobile = split[1];

                                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                    Date curDate = new Date(System.currentTimeMillis());
                                    String verify_time = formatter.format(curDate);
                                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
                                    String cpmpareTime = simpleDateFormat.format(curDate);

                                    Log.e("——人脸来", compareResult.getUserName() + "-" + requestId + "-" + mobile + "-" + verify_time);

                                    registeredMemberslist = LitePal.where("mobile = ?", mobile).find(RegisteredMembers.class);
                                    if (registeredMemberslist.size() > 0) {
                                        RegisteredMembers registeredMembers = registeredMemberslist.get(0);
                                        String expire_time = registeredMembers.getExpire_time();
                                        String status = registeredMembers.getStatus();
                                        String name = registeredMembers.getName();
                                        String image = registeredMembers.getImage();

                                        Log.e("yw——超时时间和状态", "expire_time:" + expire_time + " status:" + status + Util.isDateOneBigger(expire_time, verify_time));
                                        if (status.equals("1") && Util.isDateOneBigger(expire_time, verify_time)) {
                                            if ((!TextUtils.isEmpty(GlobalParameters.Access_limit) && compareAllLimitedTime(cpmpareTime, processLimitedTime(GlobalParameters.Access_limit)))
                                                    || TextUtils.isEmpty(GlobalParameters.Access_limit)) {
                                                message =name;

                                                addOfflineMember(name, mobile, image, new Date(), temperature);

                                                time2 = System.currentTimeMillis();
                                                Log.e("result---", "识别+测温时间=" + (time2 - time1));

                                                if (temperature > 0) {
                                                    if (temperature >=100) {
                                                        Log.e("temp---", temperature + "");
                                                      //  manormalBeep.playBeepSoundAndVibrate();
                                                        showResult(compareResult, requestId, message, false);
                                                    } else {
                                                      //  mBeepManager.playBeepSoundAndVibrate();
                                                        showResult(compareResult, requestId, message, true);
                                                    }
                                                }
                                            } else if (!TextUtils.isEmpty(GlobalParameters.Access_limit) && !compareAllLimitedTime(cpmpareTime, processLimitedTime(GlobalParameters.Access_limit))) {
                                                message = getString(R.string.text_notpasstime);
                                                showResult(compareResult, requestId, message, false);
                                            }
                                        } else if (!status.equals("1")) {
                                            message = getString(R.string.text_nopermission);
                                            showResult(compareResult, requestId, message, false);
                                        } else if (!Util.isDateOneBigger(expire_time, verify_time)) {
                                            message = getString(R.string.text_expiredtime);
                                            showResult(compareResult, requestId, message, false);
                                        }
                                    }
                                }
                                requestFeatureStatusMap.put(requestId, RequestFeatureStatus.SUCCEED);
                                faceHelper.setName(requestId, getString(R.string.recognize_success_notice, compareResult.getUserName()));
                                showResult(compareResult, requestId, message, false);
                                mBeepSuccess.playBeepSoundAndVibrate();


                                if (!isTemperature) {
                                    Log.e("retry----", "istemperature=" + isTemperature);
                                    faceHelper.setName(requestId, getString(R.string.recognize_failed_notice, "NOT_REGISTERED"));
                                    retryRecognizeDelayed(requestId);

                                }
                            } else {
                                faceHelper.setName(requestId, getString(R.string.recognize_failed_notice, "NOT_REGISTERED"));
                                retryRecognizeDelayed(requestId);
//                                Intent loginIt = new Intent(RgbCameraActivity.this, SettingActivity.class);
//                                loginIt.putExtra("register","register");
//                                startActivity(loginIt);
//                                finish();
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            faceHelper.setName(requestId, getString(R.string.recognize_failed_notice, "NOT_REGISTERED"));
                            retryRecognizeDelayed(requestId);
                        }

                        @Override
                        public void onComplete() {

                        }
                    });


        }catch (Exception e){
            e.printStackTrace();
        }
    }



    private void showResult(CompareResult compareResult, int requestId, String message, final boolean isdoor){
        sendMessageToStopAnimation(HIDE_VERIFY_UI);
        compareResult.setTrackId(requestId);
        compareResult.setMessage(message);
        compareResultList.add(compareResult);
        processHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(isdoor){
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
        }, 20*100);
    }

    private void addOfflineMember(String name,String mobile,String image,Date verify_time,float temperature){
        offlineVerifyMembers = new OfflineVerifyMembers();
        offlineVerifyMembers.setName(name);
        offlineVerifyMembers.setMobile(mobile);
        offlineVerifyMembers.setImagepath(image);
        offlineVerifyMembers.setVerify_time(verify_time);
        if(temperature >0) offlineVerifyMembers.setTemperature(""+temperature);
        offlineVerifyMembers.save();
    }


    @Override
    public void onGlobalLayout() {
        previewView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        if (!checkPermissions(NEEDED_PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, NEEDED_PERMISSIONS, ACTION_REQUEST_PERMISSIONS);
        } else {
            initEngine();
            initCamera();
        }
    }

    /**
     *
     * @param countMap map
     * @param key      key
     * @return
     */
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

    /**
     *
     * @param requestId ID
     */
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
                        if (GlobalParameters.livenessDetect) {
                            faceHelper.setName(requestId, Integer.toString(requestId));
                        }
                        livenessMap.put(requestId, LivenessInfo.UNKNOWN);
                        delayFaceTaskCompositeDisposable.remove(disposable);
                    }
                });
    }

    /**
     *
     * @param requestId ID
     */
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
                        faceHelper.setName(requestId, Integer.toString(requestId));
                        requestFeatureStatusMap.put(requestId, RequestFeatureStatus.TO_RETRY);
                        delayFaceTaskCompositeDisposable.remove(disposable);
                    }
                });
    }

    private void startDetectCard() {
        isDetected = true;
        mSwipeCardThread = new SwipeCardThread();
        mSwipeCardThread.start();
    }

    class SwipeCardThread extends Thread {
        @Override
        public void run() {
                try {
                    int resultCode = M1CardUtils.readBlockWithKeyA(mTag, DATA_BLOCK, password1);
                    if ( resultCode == 111) {
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
                            }, relaytimenumber*1000);//
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
    private void processResult(int what){
       processHandler.obtainMessage(what).sendToTarget();
        processHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(nfcDialog!=null&&nfcDialog.isShowing()) {
                    nfcDialog.dismiss();
                    nfcDialog = null;
                }
            }
        },1000);
    }

    private void restoreCameraAfterScan(Boolean isShow){
       initCamera();
       if(isShow) showGuestToast();
    }

    private void cancelImageTimer() {
        if (imageTimer != null)
            imageTimer.cancel();
    }

    private static class ProcessHandler extends Handler {
        WeakReference<RgbCameraActivity> activityWeakReference;

        private ProcessHandler(RgbCameraActivity activity) {
            activityWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            RgbCameraActivity rgbCameraActivity = activityWeakReference.get();
            if(rgbCameraActivity==null)
                return;
            switch (msg.what){
                case HIDE_VERIFY_UI:
                    rgbCameraActivity.stopAnimation();
                    rgbCameraActivity.changeVerifyBackground(R.color.transparency,false);
                    break;
                case CARD_ID_ERROR:
//                    rgbCameraActivity.mBeepManager2.playBeepSoundAndVibrate();
                    rgbCameraActivity.showNfcResult(false,false);
                    break;
                case ENTER:
//                    rgbCameraActivity.mBeepManager1.playBeepSoundAndVibrate();
                    rgbCameraActivity.showNfcResult(true,true);
                    break;
                case TIME_ERROR:
//                    rgbCameraActivity.mBeepManager2.playBeepSoundAndVibrate();
                    rgbCameraActivity.showNfcResult(true,false);
                    break;
            }
        }
    }

    private void showNfcResult(boolean isSuccess,boolean isLimitTime) {
        if(nfcDialog!=null && nfcDialog.isShowing())
            return;
        View view = View.inflate(this, R.layout.toast_guest, null);
        ImageView img_nfc = view.findViewById(R.id.img_guest);
        TextView txt_nfc = view.findViewById(R.id.txt_guest);
        img_nfc.setBackground(isSuccess ? (isLimitTime ? getDrawable(R.mipmap.scan_success): getDrawable(R.mipmap.scan_fail)) :getDrawable(R.mipmap.scan_fail));
        txt_nfc.setText(isSuccess ? (isLimitTime ? getString(R.string.welcome) : getString(R.string.text_notpasstime)):getString(R.string.Unrecognized));
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setView(view)
                .setCancelable(false);
        nfcDialog = builder.create();
        nfcDialog.show();
        Window window = nfcDialog.getWindow();
        if(window!=null){
            window.setLayout(getWindowManager().getDefaultDisplay().getWidth()/2, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawable(new ColorDrawable());
        }
    }

    private void sendMessageToStopAnimation(int what) {
        if (processHandler == null)
            return;

        boolean isSent = processHandler.sendEmptyMessage(what);

        if (!isSent) {
            new ProcessHandler(RgbCameraActivity.this).sendEmptyMessage(what);
        }
    }

    private void changeVerifyBackground(int id, boolean isVisible) {
        if (outerCircle == null || innerCircle == null)
            return;

        outerCircle.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        innerCircle.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        relativeLayout.setBackground(getDrawable(id));
//        outerCircle.setVisibility(View.VISIBLE);
//        innerCircle.setVisibility(View.VISIBLE);
        //template_view.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        img_temperature.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    private void showAnimation() {
        if (outerCircleAnimator == null && innerCircleAnimator == null) {
            outerCircle.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            outerCircleAnimator = ObjectAnimator.ofFloat(outerCircle, "rotation", 0.0f, 360.0f);
            outerCircleAnimator.setDuration(3000);
            outerCircleAnimator.setRepeatCount(Animation.INFINITE);
            outerCircleAnimator.setRepeatMode(ObjectAnimator.RESTART);
            outerCircleAnimator.setInterpolator(new LinearInterpolator());
            outerCircleAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationCancel(Animator animation) {
                    outerCircle.setLayerType(View.LAYER_TYPE_NONE, null);
                }
            });
            innerCircle.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            innerCircleAnimator = ObjectAnimator.ofFloat(innerCircle, "rotation", 0.0f, -360.0f);
            innerCircleAnimator.setDuration(2500);
            innerCircleAnimator.setRepeatCount(ValueAnimator.INFINITE);
            innerCircleAnimator.setRepeatMode(ValueAnimator.RESTART);
            innerCircleAnimator.setInterpolator(new LinearInterpolator());
            innerCircleAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationCancel(Animator animation) {
                    innerCircle.setLayerType(View.LAYER_TYPE_NONE, null);
                }
            });
            animatorSet = new AnimatorSet();
            animatorSet.play(outerCircleAnimator).with(innerCircleAnimator);
            animatorSet.start();

            tempretrynum = 0;
            retrytemp = 0;
            isTemperature = true;
            temperature_image.setVisibility(View.GONE);
            tv_message.setText("");

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Util.enableLedPower(1);
                }
            },500);
        }
    }

    private void stopAnimation() {
        if (animatorSet != null) {
            animatorSet.cancel();
            outerCircleAnimator = null;
            innerCircleAnimator = null;
            animatorSet = null;
        }
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
                        String compareTime = simpleDateFormat.format(curDate);
                        String verify_time = formatter.format(curDate);
                        String expire_time = guestMembers.get(0).getExpire_time();
                        String code = guestMembers.get(0).getQrcode();
                        if (code.equals(qrCode)) {
                            if (Util.isDateOneBigger(expire_time, verify_time)) {
                                if ((!TextUtils.isEmpty(GlobalParameters.Access_limit) && compareAllLimitedTime(compareTime, processLimitedTime(GlobalParameters.Access_limit)))
                                        || TextUtils.isEmpty(GlobalParameters.Access_limit)) {
                                    initCamera();
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
                                    Log.e("tag", "offlineGuestMembers userId----" + guestMembers.get(0).getUserId());
                                } else if (!TextUtils.isEmpty(GlobalParameters.Access_limit) && !compareAllLimitedTime(compareTime, processLimitedTime(GlobalParameters.Access_limit))) {
                                    restoreCameraAfterScan(true);
                                    if (txt_guest != null) {
                                        txt_guest.setText(R.string.text_notpasstime);
                                    }
                                }
                            } else {
                                restoreCameraAfterScan(true);
                                if (txt_guest != null) {
                                    txt_guest.setText(R.string.text_passcodeexpired);
                                }
                            }
                        } else restoreCameraAfterScan(true);
                    } else restoreCameraAfterScan(true);
                } else restoreCameraAfterScan(true);
            } else restoreCameraAfterScan(false);
        }
    }

    private void showWallpaper() {
        if (sp.getBoolean("wallpaper", false)) {
//            Glide.with(RgbCameraActivity.this)
//                   // .load(GuideService.WALLPAPER_DIR + File.separator + "wallpaper.png")
//                    .error(R.mipmap.telpo)
//                    .skipMemoryCache(true)
//                    .diskCacheStrategy(DiskCacheStrategy.NONE)
//                    .into(img_telpo);
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

    public String[] processLimitedTime(String data) {
        if (data.contains(";")) {
            return data.split(";");
        } else {
            return new String[]{data};
        }
    }

    public boolean compareAllLimitedTime(String compareTime, String[] limitedTimes) {
        boolean result = false;
        if (compareTime != null && limitedTimes != null) {
            for (String limitedTime : limitedTimes) {
                result |= compareLimitedTime(compareTime, limitedTime.split("-")[0], limitedTime.split("-")[1]);
            }
        }
        return result;
    }

    public boolean compareLimitedTime(String compareTime, String limitedStartTime, String limitedEndTime) {
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

}
