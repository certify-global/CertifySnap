package com.certify.snap.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.arcsoft.face.AgeInfo;
import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.FaceFeature;
import com.arcsoft.face.FaceInfo;
import com.arcsoft.face.FaceShelterInfo;
import com.arcsoft.face.GenderInfo;
import com.arcsoft.face.LivenessInfo;
import com.arcsoft.face.enums.DetectFaceOrientPriority;
import com.arcsoft.face.enums.DetectMode;
import com.certify.snap.R;
import com.certify.snap.arcface.model.DrawInfo;
import com.certify.snap.arcface.model.FacePreviewInfo;
import com.certify.snap.arcface.model.TemperatureRect;
import com.certify.snap.arcface.util.DrawHelper;
import com.certify.snap.arcface.util.camera.CameraListener;
import com.certify.snap.arcface.util.camera.DualCameraHelper;
import com.certify.snap.arcface.util.face.FaceHelperPro;
import com.certify.snap.arcface.util.face.FaceListener;
import com.certify.snap.arcface.util.face.LivenessType;
import com.certify.snap.arcface.util.face.RequestFeatureStatus;
import com.certify.snap.arcface.util.face.RequestLivenessStatus;
import com.certify.snap.arcface.widget.FaceRectView;
import com.certify.snap.adapter.ShowFaceInfoAdapter;
import com.certify.snap.common.AppSettings;
import com.certify.snap.common.Application;
import com.certify.snap.common.ConfigUtil;
import com.certify.snap.common.Constants;
import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.TemperatureStatus;
import com.certify.snap.common.UserExportedData;
import com.certify.snap.common.Util;
import com.certify.snap.controller.ApplicationController;
import com.certify.snap.controller.CameraController;
import com.certify.snap.controller.DatabaseController;
import com.certify.snap.controller.TemperatureController;
import com.certify.snap.faceserver.CompareResult;
import com.certify.snap.faceserver.FaceServer;
import com.certify.snap.model.RegisteredMembers;
import com.common.thermalimage.GuideDataCallBack;
import com.common.thermalimage.TemperatureBigData;
import com.common.thermalimage.ThermalImageUtil;

import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
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

import static com.certify.snap.common.GlobalParameters.livenessDetect;

public class ProIrCameraActivity extends BaseActivity implements ViewTreeObserver.OnGlobalLayoutListener {

    private static final String TAG = "ProIrCameraActivity";
    private ProcessHandler processHandler;

    public static final int HIDE_VERIFY_UI = 334;
    private static final int CARD_ID_ERROR = 335;
    private static final int ENTER = 336;
    private static final int TIME_ERROR = 337;

    private View previewViewRgb;
    private View previewViewIr;

    private FaceRectView faceRectView;

    Timer tTimer, pTimer;

    private static final int ACTION_REQUEST_PERMISSIONS = 0x001;


    private ConcurrentHashMap<Integer, Integer> temperatureStatusMap = new ConcurrentHashMap<>();

    private ConcurrentHashMap<Integer, Float> temperatureMap = new ConcurrentHashMap<>();

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

    private FaceHelperPro faceHelperProIr;
    private List<CompareResult> compareResultList;
    private ShowFaceInfoAdapter adapter;
    private SharedPreferences sharedPreferences;

    private static final String[] NEEDED_PERMISSIONS = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private AlertDialog.Builder builder;

    private int relaytimenumber = 5;
    ImageView img_temperature, img_guest, img_thermalImage, exit;
    TextView txt_guest, tv_mask;
    String message;

    private HashMap<Integer, Integer> spMap;
    public static final String WALLPAPER_CHANGE = "com.telpo.telpo_face_system_wallpaper";

    private volatile byte[] rgbData;
    private volatile byte[] irData;

    private boolean mask;
    private boolean isSaveFile = false;
    private String mailName, mailWorkNum;
    private AlertDialog dialog;
    private EditText edit_rect_left, edit_rect_top, edit_rect_right, edit_rect_bottom;
    private TextView tv_measure_area, tv_current_rect, tv_default_rect;
    Map<String, String> distanceInfo = null;
    byte lastCheckSum = 0x00;
    ThermalImageUtil util = ApplicationController.getInstance().getTemperatureUtil();
    private boolean isTemperature = false;
    private Rect tempRect;
    private Tag mTag;
    private PendingIntent mPendingIntent;
    private AlertDialog nfcDialog;
    public String WALLPAPER_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + "/offline/wallpaper/";
    private boolean isMeasured = false;
    List<RegisteredMembers> registeredMemberslist;
    RelativeLayout rl_header;
    Button logo;
    private float temperature = 0;
    private List<FacePreviewInfo> facePreviewInfoList;
    private Bitmap irBitmap;
    private Bitmap rgbBitmap;
    private Bitmap thermalBitmap;
    private Timer guideTempTimer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_pro_ir_camera);
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WindowManager.LayoutParams attributes = getWindow().getAttributes();
            attributes.systemUiVisibility = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            getWindow().setAttributes(attributes);
        }*/

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Application.getInstance().addActivity(this);
        FaceServer.getInstance().init(this);//init FaceServer;
        TemperatureController.getInstance().init(this);

        sharedPreferences = Util.getSharedPreferences(this);

        initView();
        new InitTemperatureThread().start();

        spMap = new HashMap<Integer, Integer>();

        processHandler = new ProcessHandler(ProIrCameraActivity.this);

        mPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        relaytimenumber = sharedPreferences.getInt(GlobalParameters.RelayTime, 5);
        livenessDetect = sharedPreferences.getBoolean(GlobalParameters.LivingType, true);
        mask = sharedPreferences.getBoolean(GlobalParameters.MaskMode, false);
        tempRect = new Rect(sharedPreferences.getInt("rect_left", 24), sharedPreferences.getInt("rect_top", 30),
                sharedPreferences.getInt("rect_right", 28), sharedPreferences.getInt("rect_bottom", 40));

        logo = findViewById(R.id.loginLogo);
        rl_header = findViewById(R.id.rl_header);
        logo.setOnLongClickListener(v -> {
            Intent loginIt = new Intent(ProIrCameraActivity.this, LoginActivity.class);
            startActivity(loginIt);
            finish();
            return true;
        });

        img_temperature = findViewById(R.id.img_temperature);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //new InitTemperatureThread().start();
    }

    @Override
    public void onBackPressed() {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(ProIrCameraActivity.this);
            builder.setMessage("Are you sure you want to exit?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", (dialog, id) -> finishAffinity())
                    .setNegativeButton("No", (dialog, id) -> dialog.cancel());
            AlertDialog alert = builder.create();
            alert.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initView() {
        previewViewRgb = findViewById(R.id.texture_preview);
        previewViewIr = findViewById(R.id.texture_preview_ir);
        faceRectView = findViewById(R.id.single_camera_face_rect_view);

        img_thermalImage = findViewById(R.id.iv_thermalImage);
        tv_measure_area = findViewById(R.id.tv_measure_area);

        previewViewRgb.getViewTreeObserver().addOnGlobalLayoutListener(this);

        tv_mask = findViewById(R.id.mask);

        tTimer = new Timer();

        RecyclerView recyclerShowFaceInfo = findViewById(R.id.recycler_view_person);
        compareResultList = new ArrayList<>();
        adapter = new ShowFaceInfoAdapter(compareResultList, this);
        LinearLayoutManager layoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true);
        recyclerShowFaceInfo.setAdapter(adapter);
        recyclerShowFaceInfo.setItemAnimator(new DefaultItemAnimator());
        recyclerShowFaceInfo.setLayoutManager(layoutManager);
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
                16, MAX_DETECT_NUM,
                FaceEngine.ASF_FACE_DETECT | FaceEngine.ASF_FACE_SHELTER | FaceEngine.ASF_MASK_DETECT | FaceEngine.ASF_FACELANDMARK);
        int code = ftEngine.setFaceShelterParam(0.8f);
        Log.i(TAG, "initEngine:  setFaceShelterParam   " + code);

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
        /*if (intent != null)
            mTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        new Thread(new Runnable() {
            @Override
            public void run() {
                startDetectCard();
            }
        }).start();*/
    }


    @Override
    protected void onResume() {
        super.onResume();
        try {
            if (cameraHelper != null) {
                cameraHelper.start();
            }
            if (cameraHelperIr != null) {
                cameraHelperIr.start();
            }
        } catch (RuntimeException e) {
            Toast.makeText(this, e.getMessage() + getString(R.string.camera_error_notice), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
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
        super.onDestroy();
        if (nfcDialog != null && nfcDialog.isShowing()) {
            nfcDialog.dismiss();
            nfcDialog = null;
        }
        processHandler.removeCallbacksAndMessages(null);
        if (tTimer != null)
            tTimer.cancel();
        if (pTimer != null)
            pTimer.cancel();
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

        if (faceHelperProIr != null) {
            ConfigUtil.setTrackedFaceCount(this, faceHelperProIr.getTrackedFaceCount());
            faceHelperProIr.release();
            faceHelperProIr = null;
        }

        FaceServer.getInstance().unInit();
        if (util!=null) {
            util.stopGetGuideData();
        }
        TemperatureController.getInstance().clearTemperatureMap();
    }

    private void  openled() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Util.enableLedPower(1);
            }
        }, 500);
    }

    private static final int FACE_SHELTER_CACHE_SIZE = 3;
    private Map<Integer, Integer> faceShelterCacheMap = new ConcurrentHashMap();
    private Map<Integer, Integer> faceShelterProcessCountMap = new ConcurrentHashMap();

    /**
     * @param trackId
     * @param shelter
     * @return
     */
    private int isSheltered(int trackId, int shelter) {
        int processCount = increaseAndGetValue(faceShelterProcessCountMap, trackId);

        if (shelter != FaceShelterInfo.SHELTERED) {
            shelter = FaceShelterInfo.NOT_SHELTERED;
        }
        int requiredContinueSheltered = 0b111;
        Integer shelterCache = faceShelterCacheMap.get(trackId);
        if (shelterCache == null) {
            shelterCache = 0;
        }
        shelterCache <<= 1;
        shelterCache |= shelter;
        shelterCache &= requiredContinueSheltered;


        boolean sheltered = processCount > 3 && (shelterCache == requiredContinueSheltered);

        faceShelterCacheMap.put(trackId, shelterCache);

        int ret = processCount > 3 ? (sheltered ? FaceShelterInfo.SHELTERED : FaceShelterInfo.NOT_SHELTERED) : FaceShelterInfo.UNKNOWN;
        Log.i(TAG, "isSheltered: " + (processCount > 3));

        faceShelterCacheMap.put(trackId, shelterCache);
        Log.i(TAG, "isSheltered: ret = " + ret + " , shelter = " + shelter);
        return ret;
    }

    private void showMaskTip(final String msg, final int colorid) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (tv_mask != null) {
                    tv_mask.setTextColor(colorid);
                    tv_mask.setText(msg);
                }
            }
        });
    }

    private void initRgbCamera() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        final FaceListener faceListener = new FaceListener() {
            @Override
            public void onFail(Exception e) {
                Log.e(TAG, "onFail: " + e.getMessage());
            }

            @Override
            public void onFaceFeatureInfoGet(@Nullable final FaceFeature faceFeature, final Integer requestId, final Integer errorCode) {

                Bitmap rgbBitmapClone = null, irBitmapClone = null;
                if(rgbBitmap != null) {
                    rgbBitmapClone = rgbBitmap.copy(rgbBitmap.getConfig(), false);
                }
                if(irBitmap!=null) {
                    irBitmapClone = irBitmap.copy(irBitmap.getConfig(), false);
                }

                if (faceFeature != null) {
                    Integer liveness = livenessMap.get(requestId);
                    if (!livenessDetect || liveness != null) {
                        if (AppSettings.isFacialDetect())
                            searchFace(faceFeature, requestId, rgbBitmapClone, irBitmapClone);
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
                        if (errorCode != null && errorCode == ErrorInfo.MERR_FSDK_FACEFEATURE_LOW_CONFIDENCE_LEVEL) {
                            msg = getString(R.string.low_confidence_level);
                        } else {
                            msg = getString(R.string.ExtractCode) + errorCode;
                        }
//                        faceHelperProIr.setName(requestId, getString(R.string.recognize_failed_notice, msg));
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
                        if (faceHelperProIr != null) {
//                            faceHelperProIr.setName(requestId, getString(R.string.recognize_failed_notice, "NOT_ALIVE"));
                            retryLivenessDetectDelayed(requestId);
                        }
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
                        if (faceHelperProIr != null) {
//                            faceHelperProIr.setName(requestId, getString(R.string.recognize_failed_notice, msg));
                        }
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
                        cameraId, isMirror, true, false);
                if (faceHelperProIr == null) {
                    faceHelperProIr = new FaceHelperPro.Builder()
                            .ftEngine(ftEngine)
                            .frEngine(frEngine)
                            .flEngine(flEngine)
                            .frQueueSize(MAX_DETECT_NUM)
                            .flQueueSize(MAX_DETECT_NUM)
                            .previewSize(previewSize)
                            .faceListener(faceListener)
                            .trackedFaceCount(ConfigUtil.getTrackedFaceCount(ProIrCameraActivity.this.getApplicationContext()))
                            .build();
                }
            }

            @Override
            public void onPreview(final byte[] nv21, Camera camera) {
                /*if(isSaveFile){
                    Util.nv21ToFile(nv21,camera,"people.jpg");
                    isSaveFile = false;
                    Log.e("issavefile---",isSaveFile+"-save guest jpg");
                }*/
                rgbData = nv21;
                rgbBitmap = Util.convertYuvByteArrayToBitmap(nv21, camera);
                processPreviewData();
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

        int previewHeight = previewViewRgb.getMeasuredHeight();
        if (!AppSettings.isIsNavigationBarOn()) {
            previewHeight = CameraController.getInstance().CAMERA_PREVIEW_HEIGHT;
        }
        cameraHelper = new DualCameraHelper.Builder()
                .previewViewSize(new Point(previewViewRgb.getMeasuredWidth(), previewHeight))
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
            Toast.makeText(ProIrCameraActivity.this, e.getMessage() + getString(R.string.camera_error_notice), Toast.LENGTH_SHORT).show();
        }
    }

    private void initIrCamera() {
        CameraListener irCameraListener = new CameraListener() {
            private Camera.Parameters cameraParameters;
            @Override
            public void onCameraOpened(Camera camera, int cameraId, int displayOrientation, boolean isMirror) {
                cameraParameters = camera.getParameters();
                previewSizeIr = camera.getParameters().getPreviewSize();
                drawHelperIr = new DrawHelper(previewSizeIr.width, previewSizeIr.height, previewViewIr.getWidth(), previewViewIr.getHeight(), displayOrientation,
                        cameraId, isMirror, false, false);
            }


            @Override
            public void onPreview(final byte[] nv21, Camera camera) {
                irData = nv21;
                irBitmap = Util.convertYuvByteArrayToBitmap(nv21, cameraParameters);
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

        int previewHeight = previewViewRgb.getMeasuredHeight();
        if (!AppSettings.isIsNavigationBarOn()) {
            previewHeight = CameraController.getInstance().CAMERA_PREVIEW_HEIGHT;
        }
        cameraHelperIr = new DualCameraHelper.Builder()
                .previewViewSize(new Point(previewViewIr.getMeasuredWidth(), previewHeight))
                .rotation(sharedPreferences.getInt(GlobalParameters.Orientation, 0))
                .specificCameraId(cameraIrId != null ? cameraIrId : Camera.CameraInfo.CAMERA_FACING_FRONT)
                .previewOn(previewViewIr)
                .cameraListener(irCameraListener)
                .isMirror(cameraIrId != null && Camera.CameraInfo.CAMERA_FACING_FRONT == cameraIrId)
//                .previewSize(new Point(1280, 960))
//                .additionalRotation(270)
                .build();
        cameraHelperIr.init();
        try {
            cameraHelperIr.start();
        } catch (RuntimeException e) {
            Toast.makeText(ProIrCameraActivity.this, e.getMessage() + getString(R.string.camera_error_notice), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 处理预览数据
     */
    private synchronized void processPreviewData() {
        if (rgbData != null && irData != null) {
            final byte[] cloneNv21Rgb = rgbData.clone();

            if (faceRectView != null) {
                faceRectView.clearFaceInfo();
            }

            List<FacePreviewInfo> facePreviewInfoList = faceHelperProIr.onPreviewFrame(cloneNv21Rgb);
            this.facePreviewInfoList = facePreviewInfoList;

            if(facePreviewInfoList != null && facePreviewInfoList.size() > 0 && module!=0 && !isTemperature){
                if(module == 25){
                    getTemperature(facePreviewInfoList, new UserExportedData(rgbBitmap, irBitmap, new RegisteredMembers(), 0));
                }else if(module == 27){
                    setRect(facePreviewInfoList);
                }
                startGuideTemperatureTimer();
            }
            if (facePreviewInfoList != null && faceRectView != null && drawHelperRgb != null) {
                drawPreviewInfo(facePreviewInfoList);
            }

            clearLeftFace(facePreviewInfoList);

            if (facePreviewInfoList != null && facePreviewInfoList.size() > 0 && previewSize != null) {
//                openled();
                for (int i = 0; i < facePreviewInfoList.size(); i++) {
                    // 注意：这里虽然使用的是IR画面活体检测，RGB画面特征提取，但是考虑到成像接近，所以只用了RGB画面的图像质量检测
                    int trackId = facePreviewInfoList.get(i).getTrackId();
                    Integer status = requestFeatureStatusMap.get(trackId);
                    if (status != null && status == RequestFeatureStatus.SUCCEED) {
                        continue;
                    }
                    if (module == 25) {
                        Rect rect = drawHelperRgb.adjustRect(facePreviewInfoList.get(i).getFaceInfo().getRect());
                        if(rect.top < 300 || rect.bottom> 850){
                            continue;
                        }
                    }
                    if (mask) {
                        int mask = facePreviewInfoList.get(i).getMask();
                        int faceShelter = isSheltered(facePreviewInfoList.get(i).getTrackId(), facePreviewInfoList.get(i).getFaceShelter());// ? FaceShelterInfo.SHELTERED : FaceShelterInfo.NOT_SHELTERED;

                        int combined = (mask << 1) | faceShelter;
//                            boolean canAnalyze = false;
                        switch (combined) {
                            case 0:
                            case 1:
//                                faceHelperIr.setMask(trackId, 1);
//                                showMaskTip(getString(R.string.mask_no),getResources().getColor(R.color.red));
                                break;
                            case 0b10:
//                                faceHelperIr.setMask(trackId, 0b10);
//                                showMaskTip(getString(R.string.mask_error),getResources().getColor(R.color.red));
                            case 0b11:
//                                    canAnalyze = true;
//                                faceHelperIr.setMask(trackId, 0b11);
//                                showMaskTip(getString(R.string.mask_detect),getResources().getColor(R.color.skyblue));
                                break;
                            default:
//                                showMaskTip("",getResources().getColor(R.color.skyblue));
                                break;
                        }
                            /*if (!canAnalyze) {
                                continue;
                            }*/
                    }

                    if (livenessDetect) {
                        Integer liveness = livenessMap.get(facePreviewInfoList.get(i).getTrackId());
                        if (liveness == null
                                || (liveness != LivenessInfo.ALIVE && liveness != LivenessInfo.NOT_ALIVE && liveness != RequestLivenessStatus.ANALYZING)) {
                            livenessMap.put(facePreviewInfoList.get(i).getTrackId(), RequestLivenessStatus.ANALYZING);
                            // IR数据偏移
                            FaceInfo faceInfo = facePreviewInfoList.get(i).getFaceInfo().clone();
                            faceInfo.getRect().offset(Constants.HORIZONTAL_OFFSET, Constants.VERTICAL_OFFSET);
                            faceHelperProIr.requestFaceLiveness(irData.clone(), faceInfo, previewSize.width, previewSize.height,
                                    FaceEngine.CP_PAF_NV21, facePreviewInfoList.get(i).getTrackId(), LivenessType.IR);
                        }
                    }
                    if (status == null || status == RequestFeatureStatus.TO_RETRY) {
                        requestFeatureStatusMap.put(facePreviewInfoList.get(i).getTrackId(), RequestFeatureStatus.SEARCHING);
                        faceHelperProIr.requestFaceFeature(cloneNv21Rgb, facePreviewInfoList.get(i).getFaceInfo(), facePreviewInfoList.get(i).getMask(),
                                previewSize.width, previewSize.height, FaceEngine.CP_PAF_NV21,
                                facePreviewInfoList.get(i).getTrackId());
                    }
                }
            }
            rgbData = null;
            irData = null;
        }

    }

    private int module;
    private class InitTemperatureThread extends Thread{
        @Override
        public void run() {
            //确保测温服务正常连接
            while (util.getUsingModule() == null) {
                try {
                    sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            try {
                module = util.getUsingModule()[0];
                Log.e(TAG,"temperature module : " + module);
                if (module == 25) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(tv_measure_area!=null){
                                tv_measure_area.setVisibility(View.VISIBLE);
                            }
                            if (img_thermalImage != null) {
                                ViewGroup.LayoutParams params = img_thermalImage.getLayoutParams();
                                params.width = 400;
                                params.height = 320;
                                img_thermalImage.setLayoutParams(params);
                            }
                        }
                    });
                    tempRect = new Rect(sharedPreferences.getInt("rect_left", 23), sharedPreferences.getInt("rect_top", 30),
                            sharedPreferences.getInt("rect_right", 33), sharedPreferences.getInt("rect_bottom", 40));
                }else if(module == 27){
                    tempRect = new Rect(sharedPreferences.getInt("rect_left", 140), sharedPreferences.getInt("rect_top", 105),
                            sharedPreferences.getInt("rect_right", 200), sharedPreferences.getInt("rect_bottom", 165));
                    UserExportedData data = new UserExportedData(rgbBitmap, irBitmap, new RegisteredMembers(), 0);
                    startTempMeasure(data);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    long lastBitmapTime;
    long lastDataTime;
    private Map<Integer, Integer> temperatureProcessCountMap = new ConcurrentHashMap();
    List<DrawInfo> drawInfos = new ArrayList<>();
    boolean hasFever;

    private void startTempMeasure(UserExportedData data) {
        util.getGuideData(new GuideDataCallBack.Stub() {
            @Override
            public void callBackBitmap(final Bitmap bitmap) throws RemoteException {
                long nowTime = System.currentTimeMillis();
                Log.d(TAG, "bitmapTime["+(nowTime - lastBitmapTime)+"]");
                lastBitmapTime = nowTime;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (bitmap != null && img_thermalImage != null) {
                            thermalBitmap = bitmap;
                            img_thermalImage.setImageBitmap(bitmap);
                        } else {
                            img_thermalImage.setImageBitmap(null);
                        }
                    }
                });
            }

            @Override
            public void callBackData(TemperatureBigData temperatureBigData) throws RemoteException {
                try {
                    cancelGuideTempTimer();
                    long nowTime = System.currentTimeMillis();
                    long costTime = nowTime - lastDataTime;
                    Log.d(TAG, "aadataTime["+ costTime +"]");
                    lastDataTime = nowTime;
                    if(costTime > 300) {
                        isTemperature = false;
                        Log.d(TAG, "out of time");
                        return;
                    }
                    if(isTemperature) {
                        synchronized (this) {
                            final List<float[]> maxInRectInfo = temperatureBigData.getTemInfoList();//The length of the List is the number of rects,
                            // which records the temperature measurement information of the rect of each face
                            final float envirTem = temperatureBigData.getEmvirTem();//Ambient temperature
                            if (maxInRectInfo == null || temperatureRectList == null || temperatureRectList.size() <= 0 || maxInRectInfo.size() != temperatureRectList.size()) {
                                isTemperature = false;
                                Log.d(TAG, "error data");
                                return;
                            }
                            drawInfos.clear();
                            float temp = 0;
                            temperature = 0;
                            boolean isFever;
                            hasFever = false;
                            for (int i = 0; i < maxInRectInfo.size(); i++) {
                                int trackId = temperatureRectList.get(i).getTrackId();
                                temperature = maxInRectInfo.get(i)[3];
                                if (temperature <= 10) continue;
                                String temperatureUnit = AppSettings.getfToC();
                                if (temperatureUnit.equals("F")) {
                                    temperature = (float) Util.celsiusToFahrenheit(temperature);
                                }
                                if (!AppSettings.isFacialDetect()) {
                                    data.temperature = String.valueOf(temperature);
                                    data.sendImages = AppSettings.isCaptureImagesAll() || AppSettings.isCaptureImagesAboveThreshold();
                                    data.rgb = rgbBitmap;
                                    data.ir = irBitmap;
                                    data.thermal = thermalBitmap;
                                    data.triggerType = CameraController.triggerValue.MULTIUSER.toString();
                                    TemperatureController.getInstance().updateTemperatureMap(trackId, data);
                                }
                                faceHelperProIr.setName(trackId, String.valueOf(temperature));
                                isFever = Util.isUsualTemperature(ProIrCameraActivity.this, temp);
                                drawInfos.add(new DrawInfo(originRectList.get(i).getRect(),
                                        GenderInfo.UNKNOWN, AgeInfo.UNKNOWN_AGE,
                                        livenessMap.get(trackId) == null ? LivenessInfo.UNKNOWN : livenessMap.get(trackId), isFever ? Color.RED : Color.GREEN,
                                        faceHelperProIr.getName(trackId) == null ? "" : faceHelperProIr.getName(trackId)));
                                int processCount = increaseAndGetValue(temperatureProcessCountMap, trackId);
                                if (processCount == 3) {
                                    if (temperatureMap.get(trackId) == null) {
                                        temperatureMap.put(trackId, temperature);
                                        temperatureStatusMap.put(trackId, TemperatureStatus.SUCCEED);
                                    } else if (temperature > temperatureMap.get(trackId)) {
                                        temperatureMap.put(trackId, temperature);
                                    }
                                    hasFever |= isFever;
                                }
                                Log.e(TAG, "trackId : " + trackId + " Body temperature : " + temperatureMap.get(trackId) + "-" + temperatureStatusMap.get(trackId));
                            }
                            if (hasFever) {
                                /*drawHelperRgb.drawPreviewInfo(faceRectView, drawInfos);
                                saveBitmapFile(captureView(faceRectView));
                                soundPool.stop(spMap.get(3));
                                playSounds(3, 0);*/
                            }
                            isTemperature = false;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    isTemperature = false;
                }
            }

            @Override
            public void callBackError(String s) throws RemoteException {

            }
        });
    }

    private float getDistance(Rect rect) {
        int h = rect.bottom - rect.top;
        int w = rect.right - rect.left;
        int v = h + w;
        return 80 * (310f / (float) v);
    }

    List<TemperatureRect> temperatureRectList = new ArrayList<>();
    List<TemperatureRect> originRectList = new ArrayList<>();
    List<Integer> distanceList = new ArrayList<>();
    private void setRect(final List<FacePreviewInfo> facePreviewInfoList) {
        isTemperature = true;
        temperatureRectList.clear();
        originRectList.clear();
        distanceList.clear();
        long startTime = System.currentTimeMillis();
        int distance = 0;
        for (int i = 0; i < facePreviewInfoList.size(); i++) {
            Rect rect = drawHelperRgb.adjustRect(facePreviewInfoList.get(i).getFaceInfo().getRect());
            float fix = getDistance(rect);
            //Ignore the temperature read (resulting in low read if the face is not fully visible)
            if (rect.right > 700 || rect.left < 80) {
                final Rect[] rects = new Rect[temperatureRectList.size()];
                int[] distances = new int[distanceList.size()];
                util.setGuideRect(rects, distances);
                cancelGuideTempTimer();
                continue;
            }
            float horizontalOffset = (rect.left + rect.right) / 2.00f - 400;
            float verticalOffset = (rect.top + rect.bottom) / 2.00f - 575;
            Rect newRect;
            if(fix > 100) {
                newRect = new Rect(tempRect.left - 10, tempRect.top + 20, tempRect.right - 50, tempRect.bottom - 20);
            }else if (fix > 60) {
                newRect = new Rect(tempRect.left - 10, tempRect.top + 15, tempRect.right - 40, tempRect.bottom - 15);
            } else if (fix > 30) {
                newRect = new Rect(tempRect.left, tempRect.top + 10, tempRect.right - 20, tempRect.bottom - 10);
            } else {
                newRect = new Rect(tempRect);
            }
            int horizontalOffset2 = (int) (horizontalOffset / 580 * 270);
            int verticalOffset2 = (int) (verticalOffset / 720 * 360);
            newRect.left += verticalOffset2;
            newRect.right += verticalOffset2;
            newRect.top += horizontalOffset2;
            newRect.bottom += horizontalOffset2;
            if (newRect.left > 360 || newRect.right < 0 || newRect.top > 270 || newRect.bottom < 0) {
                continue;
            }
            if (newRect.left < 0) {
                newRect.left = 0;
            }
            if (newRect.right > 360) {
                newRect.right = 360;
            }
            if (newRect.top < 0) {
                newRect.top = 0;
            }
            if (newRect.bottom > 270) {
                newRect.bottom = 270;
            }
//            Log.e(TAG,"set rect :" + newRect.toString());
            temperatureRectList.add(new TemperatureRect(facePreviewInfoList.get(i).getTrackId(), newRect , fix));
            originRectList.add(new TemperatureRect(facePreviewInfoList.get(i).getTrackId(), rect , fix));
            distanceList.add((int)fix);
        }
        if (temperatureRectList.size() == 0 || originRectList.size() == 0 || distanceList.size() == 0) {
            isTemperature = false;
            return;
        }
        final Rect[] rects = new Rect[temperatureRectList.size()];
        for (int i = 0; i < temperatureRectList.size(); i++) {
            rects[i] = temperatureRectList.get(i).getRect();
        }
        int[] distances = new int[distanceList.size()];
        for(int i = 0; i < distanceList.size(); i++){
            distances[i] = distanceList.get(i);
        }
        util.setGuideRect(rects, distances);
        Log.e(TAG,"distance :" + distance);
    }


    private void getTemperature(final List<FacePreviewInfo> facePreviewInfoList, UserExportedData data) {
        isTemperature = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    long startTime = System.currentTimeMillis();
                    distanceInfo = util.getDistanceInfo();
                    final int distance_tp53 = distanceInfo != null ? Integer.valueOf(distanceInfo.get("distance")) : 0;//get distance ,unit cm
//                    Log.e(TAG, "distance: " + distance_tp53);
                    temperatureRectList = new ArrayList<>();
                    temperatureRectList.clear();
                    for (int i = 0; i < facePreviewInfoList.size(); i++) {
                        Rect rect = drawHelperRgb.adjustRect(facePreviewInfoList.get(i).getFaceInfo().getRect());
                        //For the detection range of the RGB camera and the temperature module is different,
                        // it is necessary to filter out the face frame outside the detection range of the temperature module
                        Log.e(TAG,"rect width diff:" + (rect.right - rect.left));
                        if (rect.top > 300 && rect.bottom < 850 && (rect.right - rect.left) > 80 && (rect.right - rect.left) < 160) {
                            //Adjust the size of rect according to the distance ,and adjust the rect according to the offset of the face rect
                            //It is just a simple reference method of forehead positioning, which can be modified or selected according to the actual demand scenario.
                            float horizontalOffset = (rect.left + rect.right) / 2.00f - 400;
                            float verticalOffset = (rect.top + rect.bottom) / 2.00f - 575;
                            Rect newRect = new Rect(tempRect);
                            int horizontalOffset2 = (int)(horizontalOffset / 310 * 40);
                            int verticalOffset2 = (int)(verticalOffset / 220 * 32);
                            newRect.left += verticalOffset2;
                            newRect.right += verticalOffset2;
                            newRect.top += horizontalOffset2;
                            newRect.bottom += horizontalOffset2;
                            if(newRect.left < 0 || newRect.top < 0){
                                continue;
                            }
                            Log.e(TAG,newRect.toString());
                            temperatureRectList.add(new TemperatureRect(facePreviewInfoList.get(i).getTrackId(), newRect, 0));
                        }else {
                            faceHelperProIr.setName(facePreviewInfoList.get(i).getTrackId(),"");
                        }
                    }
                    if (temperatureRectList.size() == 0) {
                        isTemperature = false;
                        return;
                    }
                    final Rect[] rects = new Rect[temperatureRectList.size()];
                    for (int i = 0; i < temperatureRectList.size(); i++) {
                        rects[i] = temperatureRectList.get(i).getRect();
                    }
                    Log.e(TAG, "temperatureRectList size: " + temperatureRectList.size());
                    TemperatureBigData temperatureBigData = util.TP53getData(rects, distance_tp53);//Get one frame of imaging data
                    Log.e(TAG, "get temperature cost time : " + (System.currentTimeMillis() - startTime));
                    if (temperatureBigData != null) {
                        Log.e(TAG, "get temperatureBigData");
                        final byte[] originData = temperatureBigData.getOriginData();//One frame of raw data, needed to generate thermal imaging
                        if (originData != null && originData[10250] != lastCheckSum) {//Compare checksums, do not process duplicate frames
                            lastCheckSum = originData[10250];
                            final List<float[]> maxInRectInfo = temperatureBigData.getTemInfoList();//The length of the List is the number of rects,
                            // which records the temperature measurement information of the rect of each face
                            final float envirTem = temperatureBigData.getEmvirTem();//Ambient temperature
                            final Bitmap bitmap = util.TP53createBitmap(rects, maxInRectInfo, originData, R.drawable.image4v);
                            thermalBitmap = bitmap;
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    if (bitmap != null) {
                                        img_thermalImage.setImageBitmap(bitmap);
                                    } else {
                                        img_thermalImage.setImageBitmap(null);
                                    }
                                    if (maxInRectInfo != null) {
                                        for (int i = 0; i < maxInRectInfo.size(); i++) {
                                            int trackId = temperatureRectList.get(i).getTrackId();
                                            temperature = maxInRectInfo.get(i)[3];

                                            String temperatureUnit = AppSettings.getfToC();
                                            if (temperatureUnit.equals("F")) {
                                                temperature = (float) Util.celsiusToFahrenheit(temperature);
                                            }
                                            faceHelperProIr.setName(trackId, String.valueOf(temperature));

                                            if (temperatureMap.get(trackId) == null) {
                                                temperatureMap.put(trackId, temperature);
                                                temperatureStatusMap.put(trackId, TemperatureStatus.SUCCEED);
                                                if (!AppSettings.isFacialDetect()) {
                                                    data.temperature = String.valueOf(temperature);
                                                    data.sendImages = AppSettings.isCaptureImagesAll() || AppSettings.isCaptureImagesAboveThreshold();
                                                    data.rgb = rgbBitmap;
                                                    data.ir = irBitmap;
                                                    data.thermal = bitmap;
                                                    data.triggerType = CameraController.triggerValue.MULTIUSER.toString();
                                                    TemperatureController.getInstance().updateTemperatureMap(trackId, data);
                                                }
                                            } else if (temperature > temperatureMap.get(trackId)) {
                                                temperatureMap.put(trackId, temperature);
                                            }
                                            Log.e(TAG, "trackId : " + trackId + " Body temperature : " + temperatureMap.get(trackId) + "-" + temperatureStatusMap.get(trackId));
                                        }
                                    }
                                    isTemperature = false;
                                }
                            });
                        } else {
                            isTemperature = false;
                        }
                    } else {
                        isTemperature = false;
                        Log.e(TAG, "get temperatureBigData failed");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    isTemperature = false;
                    Log.e(TAG, "get temperature error");

                }
            }
        }).start();

    }

    public void adjustRect(View view){
        if(tempRect == null){
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View contentView = View.inflate(this,R.layout.dialog_adjust_temp_rect,null);
        tv_default_rect = contentView.findViewById(R.id.tv_default_rect);
        if(module == 25){
            tv_default_rect.setText("Default rect : " + "Rect(21,32-27,38)");
        }else if(module == 27){
            tv_default_rect.setText("Default rect : " + "Rect(120,115-160,155)");
        }
        tv_current_rect = contentView.findViewById(R.id.tv_current_rect);
        tv_current_rect.setText("Current rect : " + tempRect.toString());
        edit_rect_left = contentView.findViewById(R.id.edit_rect_left);
        edit_rect_top = contentView.findViewById(R.id.edit_rect_top);
        edit_rect_right = contentView.findViewById(R.id.edit_rect_right);
        edit_rect_bottom = contentView.findViewById(R.id.edit_rect_bottom);
        edit_rect_left.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        edit_rect_top.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        edit_rect_right.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        edit_rect_bottom.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        builder.setView(contentView);
        builder.setCancelable(false);
        if(dialog == null){
            dialog = builder.create();
            dialog.show();
            dialog.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
            dialog.getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
                @Override
                public void onSystemUiVisibilityChange(int visibility) {
                    int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                            View.SYSTEM_UI_FLAG_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
                    dialog.getWindow().getDecorView().setSystemUiVisibility(uiOptions);
                }
            });
        }
    }

    public void confirmAdjustRect(View view) {
        if (tv_current_rect == null || edit_rect_left == null || edit_rect_top == null || edit_rect_right == null || edit_rect_bottom == null) {
            return;
        }
        String rect_left = edit_rect_left.getText().toString().trim();
        String rect_top = edit_rect_top.getText().toString().trim();
        String rect_right = edit_rect_right.getText().toString().trim();
        String rect_bottom = edit_rect_bottom.getText().toString().trim();
        if (TextUtils.isEmpty(rect_left) || TextUtils.isEmpty(rect_top) || TextUtils.isEmpty(rect_right) || TextUtils.isEmpty(rect_bottom)) {
            Toast.makeText(this, "Please enter complete information", Toast.LENGTH_SHORT).show();
            return;
        }
        if (tempRect != null) {
            int left = Integer.parseInt(rect_left);
            int top = Integer.parseInt(rect_top);
            int right = Integer.parseInt(rect_right);
            int bottom = Integer.parseInt(rect_bottom);
            tempRect.left = left;
            tempRect.top = top;
            tempRect.right = right;
            tempRect.bottom = bottom;
            sharedPreferences.edit().putInt("rect_left", left).apply();
            sharedPreferences.edit().putInt("rect_top", top).apply();
            sharedPreferences.edit().putInt("rect_right", right).apply();
            sharedPreferences.edit().putInt("rect_bottom", bottom).apply();
            Toast.makeText(this, "Adjust success", Toast.LENGTH_SHORT).show();
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
                dialog = null;
            }
        }

    }

    public void cancelAdjustRect(View view) {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
            dialog = null;
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
                /*if (cameraHelper != null) {
                    cameraHelper.start();
                }*/
            } else {
                Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
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
                }
            }
        }
        if (facePreviewInfoList == null || facePreviewInfoList.size() == 0) {
            temperatureMap.clear();
            temperatureStatusMap.clear();
            requestFeatureStatusMap.clear();
            livenessMap.clear();
            livenessErrorRetryMap.clear();
            extractErrorRetryMap.clear();
            faceShelterProcessCountMap.clear();
            faceShelterCacheMap.clear();
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
                temperatureMap.remove(key);
                temperatureStatusMap.remove(key);
                requestFeatureStatusMap.remove(key);
                livenessMap.remove(key);
                livenessErrorRetryMap.remove(key);
                extractErrorRetryMap.remove(key);
                faceShelterProcessCountMap.remove(key);
                faceShelterCacheMap.remove(key);
            }
        }


    }

    private static final float SIMILAR_THRESHOLD = 0.7F;
    private static DecimalFormat df = new DecimalFormat("0.00");

    private void searchFace(final FaceFeature frFace, final Integer requestId, final Bitmap rgb, final Bitmap ir) {
        if (faceHelperProIr == null) {
            return;
        }

        Observable
                .create(new ObservableOnSubscribe<CompareResult>() {
                    @Override
                    public void subscribe(ObservableEmitter<CompareResult> emitter) {
                        CompareResult compareResult = FaceServer.getInstance().getTopOfFaceLib(frFace);
                        emitter.onNext(compareResult);
                    }
                })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<CompareResult>() {
                    Disposable searchMemberDisposable;

                    @Override
                    public void onSubscribe(Disposable d) {
                        searchMemberDisposable = d;
                    }

                    @Override
                    public void onNext(final CompareResult compareResult) {
                        Log.d(TAG, "Naga........onNext: ");
                        if (compareResult == null || compareResult.getUserName() == null) {
                            requestFeatureStatusMap.put(requestId, RequestFeatureStatus.FAILED);
                            return;
                        }
                        Integer temperatureStatus = temperatureStatusMap.get(requestId);
                        if (temperatureStatus == null || temperatureStatus != TemperatureStatus.SUCCEED) {
                            requestFeatureStatusMap.put(requestId, RequestFeatureStatus.TO_RETRY);
                            return;
                        }
                        float similarValue = compareResult.getSimilar() * 100;
                        if (compareResult.getSimilar() > SIMILAR_THRESHOLD) {
                            boolean isAdded = false;
                            if (compareResultList == null) {
                                requestFeatureStatusMap.put(requestId, RequestFeatureStatus.FAILED);
                                return;
                            }
                            for (CompareResult compareResult1 : compareResultList) {
                                if (compareResult1.getTrackId() == requestId) {
                                    isAdded = true;
                                    break;
                                }
                            }
                            Log.e("onnext2---", "searchface---" + isTemperature + ",isAdd:" + isAdded);
                            if (!isAdded) {
                                if (compareResultList.size() >= MAX_DETECT_NUM) {
                                    compareResultList.remove(0);
                                    adapter.notifyItemRemoved(0);
                                }

                                String[] split = compareResult.getUserName().split("-");
                                String id = "";
                                if (split != null && split.length > 1) id = split[1];

                                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                Date curDate = new Date(System.currentTimeMillis());
                                String verify_time = formatter.format(curDate);
                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
                                String cpmpareTime = simpleDateFormat.format(curDate);

                                float temperature = temperatureMap.get(requestId) == null ? 0 : temperatureMap.get(requestId);
                                String temperatureResult = String.valueOf(temperature);

                                compareResult.setTemperature(temperatureResult);
                                registeredMemberslist = DatabaseController.getInstance().findMember(Long.parseLong(split[1]));
                                if (registeredMemberslist.size() > 0) {
                                    UserExportedData data = new UserExportedData(rgb, ir, registeredMemberslist.get(0), (int) similarValue);
                                    //getTemperature(facePreviewInfoList, data);
                                    RegisteredMembers registeredMembers = registeredMemberslist.get(0);
                                    String status = registeredMembers.getStatus();
                                    String name = registeredMembers.getFirstname();
                                    String lastname = registeredMembers.getLastname();
                                    String image = registeredMembers.getImage();

                                    if (status.equals("1")) {
                                        if ((!TextUtils.isEmpty(GlobalParameters.Access_limit) && compareAllLimitedTime(cpmpareTime, processLimitedTime(GlobalParameters.Access_limit)))
                                                || TextUtils.isEmpty(GlobalParameters.Access_limit)) {
                                            message = name;

                                            mailName = name;

                                            if (temperature > 0) {
                                                if (Util.isUsualTemperature(ProIrCameraActivity.this, temperature)) {
                                                    Log.e("temp---", temperature + "");
                                                    showResult(compareResult, requestId, message, lastname, false);
                                                } else {
                                                    showResult(compareResult, requestId, message, lastname,true);
                                                }
                                                data.member = registeredMembers;
                                                updateExportData(data, temperature);
                                                TemperatureController.getInstance().updateTemperatureMap(requestId, data);
                                            }

                                        } else if (!TextUtils.isEmpty(GlobalParameters.Access_limit) && !compareAllLimitedTime(cpmpareTime, processLimitedTime(GlobalParameters.Access_limit))) {
                                            message = getString(R.string.text_notpasstime);
                                            showResult(compareResult, requestId, message, lastname,false);
                                        }
                                    } else if (!status.equals("1")) {
                                        message = getString(R.string.text_nopermission);
                                        showResult(compareResult, requestId, message, lastname,false);
                                    }
                                } else {
                                    if (temperature > 0) {
                                        UserExportedData data = new UserExportedData(rgb, ir, new RegisteredMembers(), (int) similarValue);
                                        updateExportData(data, temperature);
                                        TemperatureController.getInstance().updateTemperatureMap(requestId, data);
                                    }
                                }
                            }
                            if (faceHelperProIr != null) {
                                requestFeatureStatusMap.put(requestId, RequestFeatureStatus.SUCCEED);
                            }

                            if (!isTemperature) {
                                Log.e("retry----", "istemperature=" + isTemperature);
                                if (faceHelperProIr != null) {
                                    retryRecognizeDelayed(requestId);
                                }
                            }
                        } else {
                            if (faceHelperProIr != null) {
                                //retryRecognizeDelayed(requestId);
                                if (temperature > 0) {
                                    UserExportedData data = new UserExportedData(rgb, ir, new RegisteredMembers(), (int) similarValue);
                                    updateExportData(data, temperature);
                                    TemperatureController.getInstance().updateTemperatureMap(requestId, data);
                                }
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (faceHelperProIr != null) {
                            retryRecognizeDelayed(requestId);
                        }
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void showResult(CompareResult compareResult, int requestId, String message, String lastName, final boolean isdoor) {
        compareResult.setTrackId(requestId);
        compareResult.setMessage(message);
        compareResult.setLastName(lastName);
        compareResultList.add(compareResult);
        processHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                sendMessageToStopAnimation(HIDE_VERIFY_UI);
                adapter.notifyItemInserted(compareResultList.size());
            }
        }, 100);
    }

    private void drawPreviewInfo(List<FacePreviewInfo> facePreviewInfoList) {
        List<DrawInfo> drawInfoList = new ArrayList<>();
//        List<PointF[]> drawLandmarkInfo = new ArrayList<>();
        for (int i = 0; i < facePreviewInfoList.size(); i++) {
            int trackId = facePreviewInfoList.get(i).getTrackId();
            String name = faceHelperProIr.getName(trackId);
            Integer liveness = livenessMap.get(trackId);
            Integer recognizeStatus = requestFeatureStatusMap.get(trackId);

            //int sheltered = isSheltered(trackId, facePreviewInfoList.get(i).getFaceShelter());
//            int color = RecognizeColor.COLOR_UNKNOWN;
            int color = Color.YELLOW;
//            color = sheltered == FaceShelterInfo.NOT_SHELTERED || facePreviewInfoList.get(i).getMask() == MaskInfo.NOT_WORN ? Color.RED : Color.YELLOW;
            if (temperatureMap.get(trackId) != null) {
                color = Color.GREEN;
                if (TemperatureController.getInstance().isTemperatureAboveThreshold(temperatureMap.get(trackId))) {
                    color = Color.RED;
                }
            }
            drawInfoList.add(new DrawInfo(drawHelperRgb.adjustRect(facePreviewInfoList.get(i).getFaceInfo().getRect()),
                    GenderInfo.UNKNOWN, AgeInfo.UNKNOWN_AGE,
                    liveness == null ? LivenessInfo.UNKNOWN : liveness, color,
                    name == null ? "" : name));
//            drawLandmarkInfo.add(drawHelper.adjustPoint(facePreviewInfoList.get(i).getLandmarkInfo().getLandmarks()));
        }
        drawHelperRgb.drawPreviewInfo(faceRectView, drawInfoList);
//        drawHelper.drawLandmarkInfo(faceLandmarkView, drawLandmarkInfo);
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
                        if (livenessDetect) {
//                            faceHelperProIr.setName(requestId, Integer.toString(requestId));
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
//                        faceHelperProIr.setName(requestId, Integer.toString(requestId));
                        requestFeatureStatusMap.put(requestId, RequestFeatureStatus.TO_RETRY);
                        delayFaceTaskCompositeDisposable.remove(disposable);
                    }
                });
    }

    private void startDetectCard() {
        try {
            String cardid = parseCardId(mTag);
            if (cardid != null) {
//                if (LitePal.isExist(RegisteredMembers.class, "card = ?", cardid)) {
                    Date curDate = new Date(System.currentTimeMillis());
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
                    String compareTime = simpleDateFormat.format(curDate);
                    if ((!TextUtils.isEmpty(GlobalParameters.Access_limit) && compareAllLimitedTime(compareTime, processLimitedTime(GlobalParameters.Access_limit)))
                            || TextUtils.isEmpty(GlobalParameters.Access_limit)) {
                        processResult(ENTER, cardid);
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
                        processResult(TIME_ERROR, cardid);
                    }
//                }
               /* else {
                    processResult(CARD_ID_ERROR, null);
                }*/
            } else {
                processResult(CARD_ID_ERROR, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            processResult(CARD_ID_ERROR, null);
        }
    }

    private void processResult(int what, String cardid) {
        Bundle data = new Bundle();
        data.putString("cardid", cardid);
        Message message = processHandler.obtainMessage(what);
        message.setData(data);
        message.sendToTarget();
    }

    private static class ProcessHandler extends Handler {
        WeakReference<ProIrCameraActivity> activityWeakReference;

        private ProcessHandler(ProIrCameraActivity activity) {
            activityWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            ProIrCameraActivity irCameraActivity = activityWeakReference.get();
            if (irCameraActivity == null)
                return;
            switch (msg.what) {
                case HIDE_VERIFY_UI:
                    irCameraActivity.changeVerifyBackground(false);
                    break;
                case CARD_ID_ERROR:
//                    rgbCameraActivity.mBeepManager2.playBeepSoundAndVibrate();
//                    irCameraActivity.showNfcResult(false, false, msg.getData().getString("cardid"));
                    break;
                case ENTER:
//                    rgbCameraActivity.mBeepManager1.playBeepSoundAndVibrate();
                    //irCameraActivity.showNfcResult(true, true, msg.getData().getString("cardid"));
                    break;
                case TIME_ERROR:
//                    rgbCameraActivity.mBeepManager2.playBeepSoundAndVibrate();
                    break;
            }
        }
    }

    private void sendMessageToStopAnimation(int what) {
        if (processHandler == null)
            return;

        boolean isSent = processHandler.sendEmptyMessage(what);

        if (!isSent) {
            new ProcessHandler(ProIrCameraActivity.this).sendEmptyMessage(what);
        }
    }

    private void changeVerifyBackground(boolean isVisible) {
        img_temperature.setVisibility(isVisible ? View.VISIBLE : View.GONE);
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

    private String parseCardId(Tag tag) {
        if (tag == null) {
            return null;
        }
        return Util.toHexString(tag.getId());
    }

    private void updateExportData(UserExportedData data, float temperature) {
        data.temperature = String.valueOf(temperature);
        data.sendImages = AppSettings.isCaptureImagesAll() || AppSettings.isCaptureImagesAboveThreshold();
        data.rgb = rgbBitmap;
        data.ir = irBitmap;
        data.thermal = thermalBitmap;
        data.triggerType = CameraController.triggerValue.MULTIUSER.toString();
    }

    /**
     * Method that starts the Guide Temperature timer
     */
    public void startGuideTemperatureTimer() {
        if (guideTempTimer != null) return;
        guideTempTimer = new Timer();
        guideTempTimer.schedule(new TimerTask() {
            public void run() {
                cancelGuideTempTimer();
                resetGuideThermal();
            }
        }, 20 * 1000);
    }

    /**
     * Method that cancels the Guide Temperature timer
     */
    public void cancelGuideTempTimer() {
        if (guideTempTimer != null) {
            Log.d(TAG, "Temp Cancel Guide Temperature timer");
            guideTempTimer.cancel();
            guideTempTimer = null;
        }
    }

    /**
     * Method that resets the Guide Thermal
     */
    private void resetGuideThermal() {
        module = 0;
        new Thread(() -> {
            util.stopGetGuideData();
            TemperatureController.getInstance().clearTemperatureMap();
            ApplicationController.getInstance().releaseThermalUtil();
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            ApplicationController.getInstance().initThermalUtil(ProIrCameraActivity.this);
            util = ApplicationController.getInstance().getTemperatureUtil();
            isTemperature = false;
            new InitTemperatureThread().start();
        }).start();
    }

}