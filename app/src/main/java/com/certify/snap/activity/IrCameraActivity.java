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
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Build;
import android.os.CountDownTimer;
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

import com.arcsoft.face.ActiveFileInfo;
import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.FaceFeature;
import com.arcsoft.face.FaceInfo;
import com.arcsoft.face.LivenessInfo;
import com.arcsoft.face.enums.DetectFaceOrientPriority;
import com.arcsoft.face.enums.DetectMode;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
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
import com.certify.snap.service.GuideService;
import com.common.thermalimage.HotImageCallback;
import com.common.thermalimage.TemperatureBitmapData;
import com.common.thermalimage.TemperatureData;
import com.google.zxing.other.BeepManager;
import com.certify.snap.faceserver.CompareResult;
import com.certify.snap.faceserver.FaceServer;
import com.certify.snap.view.MyGridLayoutManager;

import org.json.JSONObject;
import org.litepal.LitePal;

import java.io.File;
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

public class IrCameraActivity extends Activity implements ViewTreeObserver.OnGlobalLayoutListener, JSONObjectCallback, RecordTemperatureCallback {

    private Toast toast = null;
    private static final String TAG = "IrCameraActivity";
    ImageView logo, loaddialog, scan, outerCircle, innerCircle, exit;
    private ObjectAnimator outerCircleAnimator, innerCircleAnimator;
    private ProcessHandler processHandler;
    private RelativeLayout relativeLayout;
    private long exitTime = 0;//点击时间控制
    private int pressTimes = 0;//连续点击次数
    private FaceEngine faceEngine = new FaceEngine();

    private static final int GUEST_QR_CODE = 333;
    public static final int HIDE_VERIFY_UI = 334;
    private static final int CARD_ID_ERROR = 335;
    private static final int ENTER = 336;
    private static final int TIME_ERROR = 337;
    private CountDownTimer countDown;
    private int valuesTimer = 3;
    public SQLiteDatabase db;
    OfflineVerifyMembers offlineVerifyMembers;
    List<RegisteredMembers> registeredMemberslist;
    private boolean isIdentified = false;
    RelativeLayout rl_header;


    private View previewViewRgb;
    private View previewViewIr;

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

    private DualCameraHelper cameraHelper;
    private DualCameraHelper cameraHelperIr;
    private DrawHelper drawHelperRgb;
    private DrawHelper drawHelperIr;
    private Camera.Size previewSize;
    private Camera.Size previewSizeIr;


    private Integer cameraRgbId = Camera.CameraInfo.CAMERA_FACING_BACK;
    private Integer cameraIrId = Camera.CameraInfo.CAMERA_FACING_FRONT;

    private FaceEngine ftEngine;
    private FaceEngine frEngine;
    private FaceEngine flEngine;

    private int ftInitCode = -1;
    private int frInitCode = -1;
    private int flInitCode = -1;

    private FaceHelper faceHelperIr;
    private List<CompareResult> compareResultList;
    private ShowFaceInfoAdapter adapter;
    SharedPreferences sp;


    private static final String[] NEEDED_PERMISSIONS = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private AlertDialog.Builder builder;

    private int relaytimenumber = 5;
    ImageView img_guest, temperature_image, img_temperature, img_logo;
    TextView txt_guest;
    TextView tvDisplayingCount;
    String message;
    private BeepManager mBeepManager, manormalBeep, mBeepManager1, mBeepManager2, malertBeep, mBeepSuccess;
    private WallpaperBroadcastReceiver wallpaperBroadcastReceiver;
    public static final String WALLPAPER_CHANGE = "com.telpo.telpo_face_system_wallpaper";

    private volatile byte[] rgbData;
    private volatile byte[] irData;
    private AnimatorSet animatorSet;
    private boolean tackPickRgb = false;
    private boolean tackPickIr = false;
    private Bitmap irBitmap;
    private Bitmap rgbBitmap;
    private Bitmap temperatureBitmap = null;
    private int tempretrynum = 0;
    private int retrytemp = 0;
    private boolean isCalibrating = true;
    private boolean isTemperature = true;
    private boolean isSearch = true;
    private final Object obj = new Object();
    TemperatureListenter mTemperatureListenter;
    private float temperature = 0;
    RelativeLayout relative_main;
    TextView tv_thermal, tv_thermal_subtitle;


    @Override
    public void onJSONObjectListener(String reportInfo, String status, JSONObject req) {
        try {
            if (reportInfo == null) {
                return;
            }
            JSONObject json1 = null;
            try {
                String formatedString = reportInfo.substring(1, reportInfo.length() - 1);
                json1 = new JSONObject(formatedString.replace("\\", ""));

            } catch (Exception e) {
                e.printStackTrace();
                json1 = new JSONObject(reportInfo/*.replace("\\", "")*/);
            }
            String access_token = json1.getString("access_token");
            String token_type = json1.getString("token_type");
            String institutionId = json1.getString("InstitutionID");
            Util.writeString(sp, GlobalParameters.ACCESS_TOKEN, access_token);
            Util.writeString(sp, GlobalParameters.TOKEN_TYPE, token_type);
            Util.writeString(sp, GlobalParameters.INSTITUTION_ID, institutionId);
        } catch (Exception e) {
            e.printStackTrace();
            Logger.error(TAG, e.getMessage());

        }
    }

    @Override
    public void onJSONObjectListenertemperature(String reportInfo, String status, JSONObject req) {
        try {

            if (reportInfo == null) {
                return;
            }
            Log.d("JSL", "reportInfo = " + reportInfo + " status " + " ,json  " + req.toString());
            JSONObject json1 = null;
            try {
                String formatedString = reportInfo.substring(1, reportInfo.length() - 1);
                //      json1 = new JSONObject(formatedString.replace("\\", ""));

            } catch (Exception e) {
                e.printStackTrace();
                json1 = new JSONObject(reportInfo/*.replace("\\", "")*/);
            }
//            if (json1.getInt("responseCode") == 1) {
//
//            }

        } catch (Exception e) {
            Logger.error("onJSONObjectListenertemperature(String report, String status, JSONObject req)", e.getMessage());
        }

    }

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
    private int DATA_BLOCK = 8;//第二扇区第1快
    private final byte[] password1 = new byte[]{(byte) 0x80, (byte) 0x60,
            (byte) 0x30, (byte) 0x30, (byte) 0x70, (byte) 0x80};
    private AlertDialog nfcDialog;


    class WallpaperBroadcastReceiver extends BroadcastReceiver {
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
        setContentView(R.layout.activity_ir);
        sp = Util.getSharedPreferences(this);
        img_logo = findViewById(R.id.img_logo);
        String path = sp.getString(GlobalParameters.IMAGE_ICON, "");
        if (path.equals("")) {
            img_logo.setBackgroundResource(R.drawable.final_logo);
        } else {
            Bitmap bitmap = Util.readBitMap(path);
            Drawable d = new BitmapDrawable(getResources(), bitmap);
            img_logo.setBackground(d);
        }
        if (sp.getString(GlobalParameters.ONLINE_MODE, "").equals("true"))
            if (Util.isConnectingToInternet(this)) {
                Util.getToken(IrCameraActivity.this, IrCameraActivity.this);
            } else {
                Logger.toast(this, getResources().getString(R.string.network_error));
            }

        //保持亮屏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Application.getInstance().addActivity(this);
        FaceServer.getInstance().init(this);//init FaceServer;
        try {
            mBeepManager = new BeepManager(this, R.raw.welcome);
            mBeepManager1 = new BeepManager(this, R.raw.beep);
//            mBeepManager2 = new BeepManager(this, R.raw.error);
            manormalBeep = new BeepManager(this, R.raw.anormaly);
            malertBeep = new BeepManager(this, R.raw.alert);
            mBeepSuccess = new BeepManager(this, R.raw.success);
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

                View view = LayoutInflater.from(IrCameraActivity.this).inflate(R.layout.layout_login, null);
                final EditText etPassword = view.findViewById(R.id.et_password);


                builder = new AlertDialog.Builder(IrCameraActivity.this).setView(view).setTitle(getString(R.string.login)).setIcon(R.drawable.logo_small)
                        .setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String input = Util.getSNCode();     //input string
                                String lastsixDigits = "";     //substring containing last 4 characters

                                if (input.length() > 6) {
                                    lastsixDigits = input.substring(input.length() - 6);
                                } else {
                                    lastsixDigits = input;
                                }


                                if (etPassword.getText().toString().equals(sp.getString(GlobalParameters.DEVICE_PASSWORD, lastsixDigits))) {
                                    Intent loginIt = new Intent(IrCameraActivity.this, SettingActivity.class);
                                    startActivity(loginIt);
                                    finish();
                                } else {
                                    Toast.makeText(IrCameraActivity.this, getString(R.string.toast_rgbir_pwderror), Toast.LENGTH_LONG).show();
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

        scan = findViewById(R.id.scan);
        Typeface rubiklight = Typeface.createFromAsset(getAssets(),
                "rubiklight.ttf");
        tv_thermal = findViewById(R.id.tv_thermal);
        tv_thermal_subtitle = findViewById(R.id.tv_thermal_subtitle);
        tv_thermal.setText(sp.getString(GlobalParameters.Thermalscan_title, "THERMAL SCAN"));
        tv_thermal_subtitle.setText(sp.getString(GlobalParameters.Thermalscan_subtitle, ""));
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
        relaytimenumber = sp.getInt(GlobalParameters.RelayTime, 5);
        Log.e("relaytimenumber---", relaytimenumber + "");
        GlobalParameters.livenessDetect = sp.getBoolean(GlobalParameters.LivingType, true);

        if (sp.getBoolean("wallpaper", false)) {
            showWallpaper();
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
        img_temperature = findViewById(R.id.img_temperature);
        if (sp.getBoolean("activate", false)) {
            Log.e("sp---true", "activate:" + sp.getBoolean("activate", false));
        } else {
            new AsyncTime().execute();
            Log.e("sp---false", "activate:" + sp.getBoolean("activate", false));
        }
    }

    private void showTip(final String msg, final boolean isplaysound) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv_message.setText(msg);
                if (isplaysound) manormalBeep.playBeepSoundAndVibrate();
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
                                    Log.e("tag", "offlineGuestMembers userId----" + guestMembers.get(0).getUserId());
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
        Application.getInstance().exit();
    }

    private void initView() {


        previewViewRgb = findViewById(R.id.texture_preview);
        previewViewIr = findViewById(R.id.texture_preview_ir);

        relative_main = findViewById(R.id.relative_layout);

        relativeLayout = findViewById(R.id.rl_verify);
        outerCircle = findViewById(R.id.iv_verify_outer_circle);
        innerCircle = findViewById(R.id.iv_verify_inner_circle);

        previewViewRgb.getViewTreeObserver().addOnGlobalLayoutListener(this);

        Typeface rubiklight = Typeface.createFromAsset(getAssets(),
                "rubiklight.ttf");
        tv_display_time = findViewById(R.id.tv_display_time);
        tv_display_time.setTypeface(rubiklight);
        tv_message = findViewById(R.id.tv_message);
        tvDisplayingCount = findViewById(R.id.tv_displing_count);
        tTimer = new Timer();
        tTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                SimpleDateFormat formatter = new SimpleDateFormat("MMMM dd, YYYY hh:mm:ss a", Locale.ENGLISH);//yyyy-MM-dd HH:mm:ss
                Date curDate = new Date(System.currentTimeMillis());//获取当前时间
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


    private void initEngine() {
        ftEngine = new FaceEngine();
        ftInitCode = ftEngine.init(this, DetectMode.ASF_DETECT_MODE_VIDEO, ConfigUtil.getFtOrient(this),
                16, MAX_DETECT_NUM, FaceEngine.ASF_FACE_DETECT);

        frEngine = new FaceEngine();
        frInitCode = frEngine.init(this, DetectMode.ASF_DETECT_MODE_IMAGE, DetectFaceOrientPriority.ASF_OP_0_ONLY,
                16, MAX_DETECT_NUM, FaceEngine.ASF_FACE_RECOGNITION);

        flEngine = new FaceEngine();
        flInitCode = flEngine.init(this, DetectMode.ASF_DETECT_MODE_IMAGE, DetectFaceOrientPriority.ASF_OP_0_ONLY,
                16, MAX_DETECT_NUM, FaceEngine.ASF_IR_LIVENESS);

        Log.i(TAG, "initEngine:  init: " + ftInitCode);

        if (ftInitCode != ErrorInfo.MOK) {
            String error = getString(R.string.specific_engine_init_failed, "ftEngine", ftInitCode);
            Log.i(TAG, "initEngine: " + error);
            // Toast.makeText(this,error,Toast.LENGTH_SHORT).show();
        }
        if (frInitCode != ErrorInfo.MOK) {
            String error = getString(R.string.specific_engine_init_failed, "frEngine", ftInitCode);
            Log.i(TAG, "initEngine: " + error);
            // Toast.makeText(this,error,Toast.LENGTH_SHORT).show();
        }
        if (flInitCode != ErrorInfo.MOK) {
            String error = getString(R.string.specific_engine_init_failed, "flEngine", ftInitCode);
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

        unInitEngine();

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
                                .getDataAndBitmap(50, true, new HotImageCallback.Stub() {
                                    @Override
                                    public void onTemperatureFail(String e) throws RemoteException {
                                        Logger.error(Util.getSNCode() + "onTemperatureFail(String e) throws RemoteException", e);
                                        retry(tempretrynum);
                                    }

                                    @Override
                                    public void getTemperatureBimapData(final TemperatureBitmapData data) throws RemoteException {
                                        temperatureBitmap = data.getBitmap();

                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (data.getBitmap() != null) {
                                                    //  tvDisplayingCount.setVisibility(View.GONE);
                                                    temperature_image.setVisibility(View.VISIBLE);
                                                    //  rl_header.setVisibility(View.VISIBLE);
                                                    temperature_image.setImageBitmap(data.getBitmap());
                                                    //                        //temperature_image.setImageBitmap(da);
                                                }
                                            }
                                        });
                                    }

                                });
                        if (temperatureData != null) {
                            String text = "";
                            tackPickIr = true;
                            tackPickRgb = true;
                            temperature = Util.FahrenheitToCelcius(temperatureData.getTemperature());
                            String tempString = String.format("%,.1f", temperature);


                            Log.e("temperature str-", temperature + "");
                            String testing_tempe = sp.getString(GlobalParameters.TEMP_TEST, "99");
                            Float tmpFloat = Float.parseFloat(testing_tempe);
                            Log.e("temperature str-", temperature + " tmpFloat " + tmpFloat);
                            if (temperature > tmpFloat) {
                                text = getString(R.string.temperature_anormaly) + tempString + getString(R.string.centigrade);
                                Log.e("temperatureBitmap", "" + (temperatureBitmap == null));
                                mTemperatureListenter.onTemperatureCall(true, text);
                                if (Util.isConnectingToInternet(IrCameraActivity.this) && (sp.getString(GlobalParameters.ONLINE_MODE, "").equals("true"))) {
                                    if (sp.getBoolean(GlobalParameters.CAPTURE_IMAGES_ALL, false) || sp.getBoolean(GlobalParameters.CAPTURE_IMAGES_ABOVE, true))
                                        Util.recordUserTemperature(IrCameraActivity.this, IrCameraActivity.this, tempString, irBitmap, rgbBitmap, temperatureBitmap);
                                    else
                                        Util.recordUserTemperature(IrCameraActivity.this, IrCameraActivity.this, tempString, null, null, null);
                                }
                            } else {
                                text = getString(R.string.temperature_normal) + tempString + getString(R.string.centigrade);
                                mTemperatureListenter.onTemperatureCall(false, text);
                                Log.d("temperture---", "isUnusualTem-" + temperatureData.isUnusualTem() + "-" + text);
                                if (Util.isConnectingToInternet(IrCameraActivity.this) && (sp.getString(GlobalParameters.ONLINE_MODE, "").equals("true"))) {
                                    if (sp.getBoolean(GlobalParameters.CAPTURE_IMAGES_ALL, false))
                                        Util.recordUserTemperature(IrCameraActivity.this, IrCameraActivity.this, tempString, irBitmap, rgbBitmap, temperatureBitmap);
                                    else
                                        Util.recordUserTemperature(IrCameraActivity.this, IrCameraActivity.this, tempString, null, null, null);
                                }
                            }

                        } else {
                            Logger.error(Util.getSNCode() + "temperatureData", "temperature data null");
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        Logger.error(Util.getSNCode() + "getTemperatureBimapData(final TemperatureBitmapData data) throws RemoteException ", e.getMessage());
                        retry(retrytemp);
                    }

                }
            }).start();

        }
    }

    private void retry(int retrynumber) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                img_temperature.setVisibility(View.GONE);
                tv_message.setVisibility(View.GONE);
                tvDisplayingCount.setVisibility(View.GONE);

            }
        });
        if (retrynumber < 3) {
            Logger.error(Util.getSNCode() + "retry temperature---", retrytemp + "-" + tempretrynum);
            runTemperature();
            retrynumber++;
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

            //请求FR的回调
            @Override
            public void onFaceFeatureInfoGet(@Nullable final FaceFeature faceFeature, final Integer requestId, final Integer errorCode) {
                //FR成功
                if (faceFeature != null) {
                    Log.i(TAG, "onPreview: fr end = " + System.currentTimeMillis() + " trackId = " + requestId + " isIdentified = " + isIdentified);
                    if (isIdentified) return;
                    // isIdentified = false;
                    if (compareResultList != null) {
//                        for (int i = compareResultList.size() - 1; i >= 0; i--) {
//                            if (compareResultList.get(i).getTrackId() == requestId) {
//                                isIdentified = true;
//                                break;
//                            }
//                        }
                        if (!isIdentified) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    changeVerifyBackground(R.color.transparency, true);
                                    relative_main.setVisibility(View.GONE);
                                    rl_header.setVisibility(View.GONE);
                                    logo.setVisibility(View.GONE);
                                    showAnimation();

                                    setonTemperatureListenter(new TemperatureListenter() {
                                        @Override
                                        public void onTemperatureCall(final boolean result, final String temperature) {
                                            Log.e("TemperatureListenter---", "isnormal=" + result + "-" + temperature);
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    sendMessageToStopAnimation(HIDE_VERIFY_UI);
                                                    isIdentified = true;
                                                    tvDisplayingCount.setVisibility(View.GONE);//ch
                                                    //   setTimerCount();
                                                    //if(ir!=null)
                                                    tackPickRgb = false;
                                                    tackPickIr = false;
                                                    //temperature_image.setImageBitmap(rgbBitmap);
                                                    //temperature_image.setVisibility(View.VISIBLE); // if (result)
                                                    // //.playBeepSoundAndVibrate();
                                                    requestFeatureStatusMap.put(requestId, RequestFeatureStatus.FAILED);
                                                    //  img_temperature.setImageResource(result ? R.drawable.stop : R.drawable.r);
                                                    tv_message.setVisibility(View.VISIBLE);
                                                    tv_message.setTextColor(getResources().getColor(R.color.white));
                                                    tv_message.setBackgroundColor(result ? getResources().getColor(R.color.red) : getResources().getColor(R.color.green));
                                                    tv_message.setText(temperature);
                                                    img_temperature.setVisibility(View.GONE);
                                                    new Handler().postDelayed(new Runnable() {
                                                        @Override
                                                        public void run() {

                                                            tvDisplayingCount.setVisibility(View.GONE);
                                                            img_temperature.setVisibility(View.GONE);
                                                            requestFeatureStatusMap.put(requestId, RequestFeatureStatus.FAILED);
                                                            isIdentified = false;
                                                            if (countDown != null)
                                                                countDown.cancel();
                                                            valuesTimer = 3;
                                                            tackPickIr = true;
                                                            tackPickRgb = true;
                                                            irBitmap = null;
                                                            rgbBitmap = null;
                                                            new Handler().postDelayed(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    clearLeftFace(null);
                                                                }
                                                            }, 3000);
                                                            //     clearLeftFace(null);
                                                        }
                                                    }, 3000);
                                                    //requestFeatureStatusMap.put(requestId, RequestFeatureStatus.FAILED);
                                                    //  faceHelper.setName(requestId, getString(R.string.VISITOR) + requestId);
                                                }
                                            });
                                        }
                                    });

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
                                                    //  tvDisplayingCount.setVisibility(View.GONE);


                                                    stopAnimation();
                                                    tv_message.setText("");
                                                    tv_message.setVisibility(View.GONE);
                                                    img_temperature.setVisibility(View.GONE);
                                                    relative_main.setVisibility(View.VISIBLE);
                                                    logo.setVisibility(View.VISIBLE);
                                                    rl_header.setVisibility(View.VISIBLE);
                                                    temperature_image.setVisibility(View.GONE);
//                                                    Util.enableLedPower(0);
                                                }
                                            });

                                            this.cancel();
                                        }
                                    }, 3 * 1000);//20秒
                                }
                            });
                        }
                    }

                    Integer liveness = livenessMap.get(requestId);
                    if (!GlobalParameters.livenessDetect) {
                        //  searchFace(faceFeature, requestId);
                    } else if (liveness != null && liveness == LivenessInfo.ALIVE) {
                        Log.e("liveness---", "LivenessInfo.ALIVE---" + isTemperature);
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
        CameraListener rgbCameraListener = new CameraListener() {
            @Override
            public void onCameraOpened(Camera camera, int cameraId, int displayOrientation, boolean isMirror) {
                previewSize = camera.getParameters().getPreviewSize();
                drawHelperRgb = new DrawHelper(previewSize.width, previewSize.height, previewViewRgb.getWidth(), previewViewRgb.getHeight(), displayOrientation,
                        cameraId, isMirror, false, false);
                if (faceHelperIr == null) {
                    faceHelperIr = new FaceHelper.Builder()
                            .ftEngine(ftEngine)
                            .frEngine(frEngine)
                            .flEngine(flEngine)
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
                rgbData = nv21;
                processPreviewData();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (tackPickRgb && rgbBitmap == null) {
                            rgbBitmap = Util.convertYuvByteArrayToBitmap(nv21, camera);
                            //                   Log.d("ddddddddddddddddddddddd", "" + rgbBitmap.getByteCount() + "  byte = " + nv21.length);
                            tackPickRgb = false;
                        }
                    }
                });
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
                if (drawHelperRgb != null) {
                    drawHelperRgb.setCameraDisplayOrientation(displayOrientation);
                }
                Log.i(TAG, "onCameraConfigurationChanged: " + cameraID + "  " + displayOrientation);
            }
        };
        cameraHelper = new DualCameraHelper.Builder()
                .previewViewSize(new Point(previewViewRgb.getMeasuredWidth(), previewViewRgb.getMeasuredHeight()))
                .rotation(sp.getInt(GlobalParameters.Orientation, 0))
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
        CameraListener irCameraListener = new CameraListener() {
            @Override
            public void onCameraOpened(Camera camera, int cameraId, int displayOrientation, boolean isMirror) {
                previewSizeIr = camera.getParameters().getPreviewSize();
                drawHelperIr = new DrawHelper(previewSizeIr.width, previewSizeIr.height, previewViewIr.getWidth(), previewViewIr.getHeight(), displayOrientation,
                        cameraId, isMirror, false, false);
            }


            @Override
            public void onPreview(final byte[] nv21, final Camera camera) {

                irData = nv21;
                if (tackPickIr && irBitmap == null) {
                    irBitmap = Util.convertYuvByteArrayToBitmap(nv21, camera);
                    //  Log.d("irBitmap", "" + irBitmap.getByteCount() + "  byte = " + nv21.length);
                    tackPickIr = false;
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
                if (drawHelperIr != null) {
                    drawHelperIr.setCameraDisplayOrientation(displayOrientation);
                }
                Log.i(TAG, "onCameraConfigurationChanged: " + cameraID + "  " + displayOrientation);
            }
        };

        cameraHelperIr = new DualCameraHelper.Builder()
                .previewViewSize(new Point(previewViewIr.getMeasuredWidth(), previewViewIr.getMeasuredHeight()))
                .rotation(sp.getInt(GlobalParameters.Orientation, 0))
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


    private synchronized void processPreviewData() {
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
            //  rgbData = null;
            //   irData = null;
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
                initEngine();
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
                    Log.e("tag", "remove exist face");
                    compareResultList.remove(i);
                    adapter.notifyItemRemoved(i);
                    tv_message.setText("");
                    tv_message.setVisibility(View.GONE);
                    img_temperature.setVisibility(View.GONE);
                    //   tvDisplayingCount.setVisibility(View.GONE);
                    stopAnimation();
                    relative_main.setVisibility(View.VISIBLE);
                    rl_header.setVisibility(View.VISIBLE);
                    temperature_image.setVisibility(View.GONE);

                    logo.setVisibility(View.VISIBLE);
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


    private void searchFace(final FaceFeature frFace, final Integer requestId) {
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
                            faceHelperIr.setName(requestId, getString(R.string.VISITOR) + requestId);
                            return;
                        }

//                        Log.i(TAG, "onNext: fr search get result  = " + System.currentTimeMillis() + " trackId = " + requestId + "  similar = " + compareResult.getSimilar());
                        if (compareResult.getSimilar() > SIMILAR_THRESHOLD) {
                            boolean isAdded = false;
                            if (compareResultList == null) {
                                requestFeatureStatusMap.put(requestId, RequestFeatureStatus.FAILED);
                                faceHelperIr.setName(requestId, getString(R.string.VISITOR) + requestId);
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
                                // MAX_DETECT_NUM
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

                                Log.e("yw——1", compareResult.getUserName() + "  " + verify_time);

                                registeredMemberslist = LitePal.where("mobile = ?", split[1]).find(RegisteredMembers.class);
                                if (registeredMemberslist.size() > 0) {
                                    RegisteredMembers registeredMembers = registeredMemberslist.get(0);
                                    String expire_time = registeredMembers.getExpire_time();
                                    String status = registeredMembers.getStatus();
                                    String name = registeredMembers.getName();
                                    String image = registeredMembers.getImage();

                                    Log.e("yw—", "expire_time:" + expire_time + " status:" + status + Util.isDateOneBigger(expire_time, verify_time));
                                    if (status.equals("1") && Util.isDateOneBigger(expire_time, verify_time)) {
                                        if ((!TextUtils.isEmpty(GlobalParameters.Access_limit) && compareAllLimitedTime(cpmpareTime, processLimitedTime(GlobalParameters.Access_limit)))
                                                || TextUtils.isEmpty(GlobalParameters.Access_limit)) {
                                            message = name;

                                            addOfflineMember(name, mobile, image, new Date(), temperature);

                                            time2 = System.currentTimeMillis();
                                            Log.e("result---", "=" + (time2 - time1));
                                            String testing_tempe = sp.getString(GlobalParameters.TEMP_TEST, "99");
                                            Float tmpFloat = Float.parseFloat(testing_tempe);
                                            if (testing_tempe.length() > 1)
                                                if (temperature > 0) {
                                                    Log.d("tmpFloat", "" + tmpFloat);
                                                    if (temperature > tmpFloat) {
                                                        Log.e("temp---", temperature + "");
                                                        // manormalBeep.playBeepSoundAndVibrate();
                                                        showResult(compareResult, requestId, message, false);
                                                    } else {
                                                        // mBeepManager.playBeepSoundAndVibrate();
                                                        showResult(compareResult, requestId, message, true);
                                                    }
                                                }
                                        } else if (!TextUtils.isEmpty(GlobalParameters.Access_limit) && !compareAllLimitedTime(cpmpareTime, processLimitedTime(GlobalParameters.Access_limit))) {
                                            //Unqualified conditions Unrestricted time
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
                            faceHelperIr.setName(requestId, getString(R.string.recognize_success_notice, compareResult.getUserName()));
                            showResult(compareResult, requestId, message, false);
                            mBeepSuccess.playBeepSoundAndVibrate();


                            if (!isTemperature) {
                                Log.e("retry----", "istemperature=" + isTemperature);
                                faceHelperIr.setName(requestId, getString(R.string.recognize_failed_notice, "NOT_REGISTERED"));
                                retryRecognizeDelayed(requestId);
                            }
                        } else {
                            faceHelperIr.setName(requestId, getString(R.string.recognize_failed_notice, "NOT_REGISTERED"));
                            retryRecognizeDelayed(requestId);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        faceHelperIr.setName(requestId, getString(R.string.recognize_failed_notice, "NOT_REGISTERED"));
                        retryRecognizeDelayed(requestId);
                        Logger.error(Util.getSNCode() + "searchFace(final FaceFeature frFace, final Integer requestId)", e.getMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                });
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
            initEngine();
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
                    irCameraActivity.stopAnimation();
                    irCameraActivity.changeVerifyBackground(R.color.transparency, false);
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

    private void setTimerCount() {
        countDown = new CountDownTimer(60000, 1000) { //Set Timer for 60 seconds

            public void onTick(long millisUntilFinished) {
                //Implement a logic that tells you "oi! 60 seconds then reuse this!"
                tvDisplayingCount.setText("" + valuesTimer);
                if (valuesTimer == 1) return;
                valuesTimer = --valuesTimer;

            }

            @Override
            public void onFinish() {

                // counterTV.setVisibility(View.INVISIBLE);
                tv_display_time.setVisibility(View.GONE);
                // enableVerifyButton(true);

            }
        }
                .start();
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

        outerCircle.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        innerCircle.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        relativeLayout.setBackground(getDrawable(id));
//        outerCircle.setVisibility(View.VISIBLE);
//        innerCircle.setVisibility(View.VISIBLE);
        // template_view.setVisibility(isVisible?View.VISIBLE:View.GONE);
        //     img_temperature.setVisibility(isVisible ? View.VISIBLE : View.GONE);
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
            tv_message.setVisibility(View.GONE);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    //  Util.enableLedPower(1);
                }
            }, 500);
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

    private void showWallpaper() {
        if (sp.getBoolean("wallpaper", false)) {
            Glide.with(IrCameraActivity.this)
                    .load(GuideService.WALLPAPER_DIR + File.separator + "wallpaper.png")
                    .error(R.mipmap.telpo)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE);
            //.into(relativeLayout);
        }
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

    public void activeEngine() {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                int activeCode = faceEngine.activeOnline(IrCameraActivity.this, Constants.APP_ID, Constants.SDK_KEY);
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
                            Util.writeBoolean(sp, "activate", true);
//                            show();
                        } else {
//                            Util.showToast(SettingActivity.this,getString(R.string.active_failed, activeCode));
                            Util.writeBoolean(sp, "activate", false);
//                            hide();
                        }


                        ActiveFileInfo activeFileInfo = new ActiveFileInfo();
                        int res = faceEngine.getActiveFileInfo(IrCameraActivity.this, activeFileInfo);
                        if (res == ErrorInfo.MOK) {
                            Log.e("activate---", activeFileInfo.toString());
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

    public class AsyncTime extends AsyncTask<Void, Void, String> {
        private ProgressDialog progress;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress = new ProgressDialog(IrCameraActivity.this);
            progress.setMessage("Application Initializing...");
            progress.show();

        }

        @Override
        protected String doInBackground(Void... params) {
            activeEngine();
            return "";
        }

        @Override
        protected void onPostExecute(String reportInfo) {
            progress.dismiss();

        }
    }
}