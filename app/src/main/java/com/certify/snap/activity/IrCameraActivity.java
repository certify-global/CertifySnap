package com.certify.snap.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.hardware.Camera;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.arcsoft.face.AgeInfo;
import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.Face3DAngle;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.FaceFeature;
import com.arcsoft.face.FaceInfo;
import com.arcsoft.face.FaceShelterInfo;
import com.arcsoft.face.GenderInfo;
import com.arcsoft.face.LivenessInfo;
import com.arcsoft.face.LivenessParam;
import com.arcsoft.face.MaskInfo;
import com.arcsoft.face.enums.DetectFaceOrientPriority;
import com.arcsoft.face.enums.DetectMode;
import com.arcsoft.face.enums.DetectModel;
import com.arcsoft.imageutil.ArcSoftImageFormat;
import com.arcsoft.imageutil.ArcSoftImageUtil;
import com.arcsoft.imageutil.ArcSoftImageUtilError;
import com.certify.callback.BarcodeSendData;
import com.certify.callback.JSONObjectCallback;
import com.certify.callback.PrintStatusCallback;
import com.certify.callback.QRCodeCallback;
import com.certify.snap.BuildConfig;
import com.certify.snap.R;
import com.certify.snap.api.response.MemberData;
import com.certify.snap.arcface.model.DrawInfo;
import com.certify.snap.arcface.model.FacePreviewInfo;
import com.certify.snap.arcface.util.DrawHelper;
import com.certify.snap.arcface.util.camera.CameraListener;
import com.certify.snap.arcface.util.camera.DualCameraHelper;
import com.certify.snap.arcface.util.face.FaceHelper;
import com.certify.snap.arcface.util.face.FaceListener;
import com.certify.snap.arcface.util.face.LivenessType;
import com.certify.snap.arcface.util.face.RequestFeatureStatus;
import com.certify.snap.arcface.util.face.RequestLivenessStatus;
import com.certify.snap.arcface.widget.FaceRectView;
import com.certify.snap.async.AsyncJSONObjectQRCode;
import com.certify.snap.bluetooth.bleCommunication.BluetoothLeService;
import com.certify.snap.bluetooth.data.WritePacket;
import com.certify.snap.common.AppSettings;
import com.certify.snap.common.Application;
import com.certify.snap.common.ConfigUtil;
import com.certify.snap.common.Constants;
import com.certify.snap.common.EndPoints;
import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.Logger;
import com.certify.snap.common.M1CardUtils;
import com.certify.snap.common.UserExportedData;
import com.certify.snap.common.Util;
import com.certify.snap.controller.AccessCardController;
import com.certify.snap.controller.ApplicationController;
import com.certify.snap.controller.BLEController;
import com.certify.snap.controller.BlePeripheralController;
import com.certify.snap.controller.CameraController;
import com.certify.snap.controller.DatabaseController;
import com.certify.snap.controller.DeviceSettingsController;
import com.certify.snap.controller.GestureController;
import com.certify.snap.controller.PrinterController;
import com.certify.snap.controller.QrCodeController;
import com.certify.snap.controller.SoundController;
import com.certify.snap.controller.TemperatureController;
import com.certify.snap.faceserver.CompareResult;
import com.certify.snap.faceserver.FaceServer;
import com.certify.snap.fragment.AcknowledgementFragment;
import com.certify.snap.fragment.ConfirmationScreenFragment;
import com.certify.snap.fragment.GestureFragment;
import com.certify.snap.fragment.MaskEnforceFragment;
import com.certify.snap.fragment.SecondaryIdentificationFragment;
import com.certify.snap.fragment.TouchModeFragment;
import com.certify.snap.model.AccessControlModel;
import com.certify.snap.model.FaceParameters;
import com.certify.snap.model.GuestMembers;
import com.certify.snap.model.MemberSyncDataModel;
import com.certify.snap.model.OfflineGuestMembers;
import com.certify.snap.model.QrCodeData;
import com.certify.snap.model.RegisteredMembers;
import com.certify.snap.printer.usb.PrintExecuteTask;
import com.certify.snap.printer.usb.util;
import com.certify.snap.qrscan.BarcodeScannerProcessor;
import com.certify.snap.qrscan.CameraSource;
import com.certify.snap.qrscan.CameraSourcePreview;
import com.certify.snap.qrscan.GraphicOverlay;
import com.certify.snap.service.HIDService;
import com.certify.snap.service.OfflineRecordSyncService;

import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
import me.grantland.widget.AutofitTextView;

public class IrCameraActivity extends BaseActivity implements ViewTreeObserver.OnGlobalLayoutListener, BarcodeSendData,
        JSONObjectCallback, QRCodeCallback, TemperatureController.TemperatureCallbackListener, PrinterController.PrinterCallbackListener,
        PrintStatusCallback, GestureController.GestureHomeCallBackListener, AccessCardController.AccessCallbackListener,
        QrCodeController.QrCodeListener, ApplicationController.ApplicationCallbackListener, BlePeripheralController.BleCallbackListener {

    private static final String TAG = IrCameraActivity.class.getSimpleName();
    ImageView outerCircle, innerCircle;
    private Button logo, button_checkIn, button_checkOut;
    private RelativeLayout relativeLayout;

    private CompareResult compareResult;
    private static final int GUEST_QR_CODE = 333;
    List<RegisteredMembers> registeredMemberslist = null;
    private boolean isFaceIdentified;
    private RelativeLayout rl_header;
    private LinearLayout time_attendance_layout;

    private View previewViewRgb;
    private View previewViewIr;

    private TextView tv_display_time, tv_message, tvVersionIr, mask_message, tv_sync, tvDisplayTimeOnly, tvVersionOnly, tvTime, tvDate, tvWaveMessage;
    private Button btWaveStart;
    Timer tTimer, pTimer, imageTimer, lanchTimer;

    private static final int ACTION_REQUEST_PERMISSIONS = 0x001;


    private ConcurrentHashMap<Integer, Integer> requestFeatureStatusMap = new ConcurrentHashMap<>();


    private ConcurrentHashMap<Integer, Integer> extractErrorRetryMap = new ConcurrentHashMap<>();


    private ConcurrentHashMap<Integer, Integer> livenessMap = new ConcurrentHashMap<>();


    private ConcurrentHashMap<Integer, Integer> livenessErrorRetryMap = new ConcurrentHashMap<>();

    private CompositeDisposable getFeatureDelayedDisposables = new CompositeDisposable();
    private CompositeDisposable delayFaceTaskCompositeDisposable = new CompositeDisposable();
    private CompositeDisposable alignedFacesDisposable = new CompositeDisposable();
    private CompositeDisposable searchFaceDisposable = new CompositeDisposable();
    private CompositeDisposable maskDetectDisposable = new CompositeDisposable();

    private static final int MAX_DETECT_NUM = 10;

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
    private SharedPreferences sharedPreferences;
    private Toast faceThermalToast = null;

    private static final String[] NEEDED_PERMISSIONS = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private int relaytimenumber = 5;
    ImageView img_guest, temperature_image, img_logo;
    TextView txt_guest;
    private Button retryButton;

    private volatile byte[] irData;
    private Bitmap irBitmap;
    private Bitmap rgbBitmap;
    private Bitmap temperatureBitmap = null;
    RelativeLayout relative_main;
    TextView tv_thermal, tv_thermal_subtitle;
    private long delayMilli = 0;
    private String delayMilliTimeOut = "";
    private TextView tvErrorMessage, tv_scan, tvFaceMessage;
    private FaceEngineHelper faceEngineHelper;

    private NfcAdapter mNfcAdapter;
    private Tag mTag;
    private PendingIntent mPendingIntent;
    private boolean rfIdEnable = false;
    private String mNfcIdString = "";
    private Toast mToastbar;
    private Toast toastmSnackbar;
    private Float temperature;

    private AlertDialog nfcDialog;
    Typeface rubiklight;
    private CameraSource cameraSource = null;
    private CameraSourcePreview preview;
    private GraphicOverlay graphicOverlay;
    private static final String BARCODE_DETECTION = "Barcode Detection";
    FrameLayout frameLayout;
    ImageView img_qr;
    View imageqr;
    RelativeLayout qr_main;
    private boolean qrCodeEnable = false;
    private String institutionId = "";
    private boolean ledSettingEnabled = false;
    private int processMask = FaceEngine.ASF_MASK_DETECT | FaceEngine.ASF_FACE_SHELTER | FaceEngine.ASF_AGE
            | FaceEngine.ASF_FACE3DANGLE | FaceEngine.ASF_GENDER | FaceEngine.ASF_LIVENESS;
    private Bitmap maskDetectBitmap;
    private int maskStatus = -2;
    private boolean maskEnabled = false;
    private boolean faceDetectEnabled = false;
    private boolean isProDevice = false;
    private BroadcastReceiver mMessageReceiver = null;
    private AutofitTextView tvOnlyText;
    int memberCount;
    int totalCount;
    private RelativeLayout snack_layout, cameraPreviewLayout;
    private int scanMode = 0;
    private boolean isHomeViewEnabled;
    private List<FaceInfo> searchFaceInfoList = new ArrayList<>();
    private int mFaceMatchRetry = 0;
    private Timer previewIdleTimer;
    private boolean isNfcFDispatchEnabled = false;
    private boolean isNavigationBarOn = true;
    private boolean isActivityResumed = false;
    private boolean isReadyToScan = true;
    private BroadcastReceiver hidReceiver;
    private ProgressDialog progressDialog;
    private UserExportedData userData;
    private Button qrSkipButton;
    private FaceRectView faceRectView;
    private Face3DAngle face3DAngle;
    private int orientationValue = 0;
    private Timer mQRTimer = null;
    private boolean isLowTempRead;
    private int MIN_TEMP_DISPLAY_THRESHOLD = 50;
    private Fragment acknowledgementFragment;
    private Fragment gestureFragment;
    private Fragment maskEnforceFragment;
    private Fragment secondaryScreenFragment;
    private Fragment touchModeFragment;
    private boolean qrCodeReceived = false;
    private boolean resumedFromGesture = false;
    private boolean isRecordNotSent = false;
    private boolean isFaceNotDetectedTimer = false;
    private Timer previewTimer;

    private void instanceStart() {
        try {
            faceEngineHelper = new FaceEngineHelper();
        } catch (Exception e) {
            Logger.error(TAG, "instanceStart()", "Exception occurred in instantiating FaceEngineHelper:" + e.getMessage());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (isDestroyed()) return;
                memberCount = intent.getIntExtra("memberCount", 0);
                totalCount = intent.getIntExtra("count", 0);
                int actionCode = intent.getIntExtra("actionCode", 0);
                showSnackbar(actionCode);
            }
        };
        rubiklight = Typeface.createFromAsset(getAssets(),
                "rubiklight.ttf");
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (Util.isDeviceF10()) {
            setContentView(R.layout.activity_f10_ir);
        } else {
            setContentView(R.layout.activity_ir);
        }

        instanceStart();
        sharedPreferences = Util.getSharedPreferences(this);
        img_logo = findViewById(R.id.img_logo);
        String path = sharedPreferences.getString(GlobalParameters.IMAGE_ICON, "");
        homeIcon(path);
        getAppSettings();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Application.getInstance().addActivity(this);
        initTemperature();
        FaceServer.getInstance().init(this);//init FaceServer;
        CameraController.getInstance().init(this);
        CameraController.getInstance().startProDeviceInitTimer(this);
        ApplicationController.getInstance().setListener(this);
        QrCodeController.getInstance().init(this);
        //initAccessControl();
        initNfc();
        initGesture();
        SoundController.getInstance().init(this);
        BlePeripheralController.getInstance().setBleListener(this);

        logo = findViewById(R.id.loginLogo);
        rl_header = findViewById(R.id.rl_header);
        logo.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (progressDialog != null && progressDialog.isShowing()) return false;
                CameraController.getInstance().setAppExitTriggered(true);
                Logger.debug(TAG, "onLongClick", "Launch Login activity");
                progressDialog = ProgressDialog.show(IrCameraActivity.this, "", getString(R.string.launch_settings_msg));
                if ((CameraController.getInstance().getScanState() == CameraController.ScanState.FACIAL_SCAN) &&
                        !Util.isDeviceF10()) {
                    return false;
                }
                Intent loginIt = new Intent(IrCameraActivity.this, LoginActivity.class);
                startActivity(loginIt);
                finish();
                return true;
            }
        });

        tv_thermal = findViewById(R.id.tv_thermal);
        tv_thermal_subtitle = findViewById(R.id.tv_thermal_subtitle);
        tv_thermal.setText(sharedPreferences.getString(GlobalParameters.Thermalscan_title, getString(R.string.thermal_scan)));
        tv_thermal_subtitle.setText(sharedPreferences.getString(GlobalParameters.Thermalscan_subtitle, ""));
        tv_thermal.setTypeface(rubiklight);
        tv_thermal_subtitle.setTypeface(rubiklight);

        initView();
        setClickListener();
        resetImageLogo();

        String onlyTextMes = sharedPreferences.getString(GlobalParameters.HOME_TEXT_ONLY_MESSAGE, "");
        tvOnlyText.setText(onlyTextMes);
        tvOnlyText.setTextSize(CameraController.getInstance().getOnlyTextSize(onlyTextMes.length()));

        relaytimenumber = sharedPreferences.getInt(GlobalParameters.RelayTime, 5);

        //template_view = findViewById(R.id.template_view);
        temperature_image = findViewById(R.id.temperature_image);

        initHidReceiver();
        initRecordUserTempService();
        initBluetoothPrinter();
        startBLEService();
        if (AppSettings.isEnableTouchMode() && !Util.isDeviceF10()) {
            btWaveStart.setVisibility(View.VISIBLE);
            tvWaveMessage.setVisibility(View.VISIBLE);
            btWaveStart.setText(getResources().getString(R.string.start));
            if (AppSettings.isMultiLingualEnabled() && DatabaseController.getInstance().getLanguagesFromDb().size() > 1) {
                tvWaveMessage.setText(AppSettings.getMultiLanguageHomeMsg());
            } else {
                tvWaveMessage.setText(AppSettings.getTouchHomePageMsg());

            }
        } else {
            btWaveStart.setVisibility(View.GONE);
            tvWaveMessage.setVisibility(View.GONE);
        }
        btWaveStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GestureController.getInstance().setAnswerType(GestureController.AnswerType.Touch);
                if (AppSettings.isMultiLingualEnabled() && DatabaseController.getInstance().getLanguagesFromDb().size() > 1) {
                    launchTouchFragment();
                } else {
                    GestureController.getInstance().setCallback(true);
                    onGestureDetected();
                }
            }
        });
    }

    private void setClickListener() {
        retryButton.setOnClickListener(view -> {
            TemperatureController.getInstance().setTemperatureListener(null);
            retryButton.setVisibility(View.GONE);
            resetHomePage();
        });
    }

    private void initQRCode() {
        if (!isHomeViewEnabled) return;
        try {
            QrCodeController.getInstance().resetQrCodeData(this);
            qr_main.setVisibility(View.VISIBLE);
            if (sharedPreferences.getBoolean(GlobalParameters.ANONYMOUS_ENABLE, false)) {
                tv_scan.setText(R.string.tv_qr_bar_scan);
            } else {
                tv_scan.setText(R.string.tv_qr_scan);
            }
            tv_scan.setBackgroundColor(getResources().getColor(R.color.colorWhite));
            tv_scan.setTextColor(getResources().getColor(R.color.black));
            tv_scan.setTypeface(rubiklight);
            Animation animation =
                    AnimationUtils.loadAnimation(getApplicationContext(), R.anim.qr_line_anim);
            if (preview == null) {
                Log.d(TAG, "Preview is null");
            }
            preview.getDrawingCache(true);
            createCameraSource(BARCODE_DETECTION);
            if (qrCodeEnable) {
                //Move the logo to the top
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) img_logo.getLayoutParams();
                params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                params.addRule(RelativeLayout.CENTER_IN_PARENT);
                params.setMargins(0, (int) Util.convertDpToPixel(20, this), 0, 0);
                img_logo.setLayoutParams(params);
                frameLayout.setVisibility(View.VISIBLE);
                //qrSkipButton.setVisibility(View.VISIBLE);
                imageqr.startAnimation(animation);
                qrSkipButton.setText(sharedPreferences.getString(GlobalParameters.QR_BUTTON_TEXT, getString(R.string.qr_button_text)));
            } else {
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) img_logo.getLayoutParams();
                params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                params.addRule(RelativeLayout.CENTER_IN_PARENT);
                params.setMargins(0, 230, 0, 0);
                img_logo.setLayoutParams(params);
                frameLayout.setVisibility(View.GONE);
                //qrSkipButton.setVisibility(View.GONE);

            }
        } catch (Exception e) {
            Logger.debug("initQRCode()", e.getMessage());
        }

    }

    private void homeIcon(String path) {
        try {
            if (!path.isEmpty()) {
                Bitmap bitmap = Util.decodeToBase64(path);
                img_logo.setImageBitmap(bitmap);
            } else {
                img_logo.setBackgroundResource(R.drawable.final_logo);
            }

        } catch (Exception ex) {
            Log.e(TAG, String.format("homeIcone path: %s, message: %s", path, ex.getMessage()));
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GUEST_QR_CODE) {
            if (resultCode == 0 && data != null) {
                String qrCode = data.getStringExtra("qrCode");
                if (qrCode != null) {
                    //List<GuestMembers> guestMembers = LitePal.where("qrcode = ?", qrCode).find(GuestMembers.class);
                    List<GuestMembers> guestMembers = null;
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
                                    //offlineGuestMembers.save();
                                    Logger.verbose(TAG, "offlineGuestMembers userId----", guestMembers.get(0).getUserId());
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
            builder.setMessage(getString(R.string.app_exit_msg))
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.button_ok), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            finishAffinity();
                            stopHealthCheckService();
                            //stopBLEService();
                            //stopMemberSyncService();
                            stopHidService();
                        }
                    })
                    .setNegativeButton(getString(R.string.living_type_0), new DialogInterface.OnClickListener() {
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
        tvOnlyText = findViewById(R.id.tv_only_text);
        snack_layout = findViewById(R.id.snack_layout);

        relativeLayout = findViewById(R.id.rl_verify);
        outerCircle = findViewById(R.id.iv_verify_outer_circle);
        innerCircle = findViewById(R.id.iv_verify_inner_circle);
        tvErrorMessage = findViewById(R.id.tv_error_message);
        tvFaceMessage = findViewById(R.id.tv_close_message);
        previewViewRgb.getViewTreeObserver().addOnGlobalLayoutListener(this);
        mask_message = findViewById(R.id.mask_message);
        mask_message.setTypeface(rubiklight);
        tv_sync = findViewById(R.id.tv_sync);
        graphicOverlay = findViewById(R.id.fireFaceOverlay);

        tv_display_time = findViewById(R.id.tv_display_time);
        tvDisplayTimeOnly = findViewById(R.id.tv_display_time_only);
        btWaveStart = findViewById(R.id.bt_wave_start);
        tvVersionOnly = findViewById(R.id.tv_version_ir_only);
        tvVersionIr = findViewById(R.id.tv_version_ir);
        tvVersionIr.setText(Util.getVersionBuild());
        tvVersionOnly.setText(Util.getVersionBuild());
        frameLayout = findViewById(R.id.barcode_scanner);
        preview = findViewById(R.id.firePreview);
        imageqr = findViewById(R.id.imageView);
        tv_scan = findViewById(R.id.tv_scan);
        img_qr = findViewById(R.id.img_qr);
        qr_main = findViewById(R.id.qr_main);
        tv_display_time.setTypeface(rubiklight);
        tvOnlyText.setTypeface(rubiklight);
        tvDisplayTimeOnly.setTypeface(rubiklight);
        btWaveStart.setTypeface(rubiklight);
        tvDisplayTimeOnly.setTypeface(rubiklight);
        tvWaveMessage = findViewById(R.id.tv_message_wave);
        tvWaveMessage.setTypeface(rubiklight);
        tvVersionOnly.setTypeface(rubiklight);
        tv_message = findViewById(R.id.tv_message);
        internetIndicatorImg = findViewById(R.id.img_internet_indicator);
        qrSkipButton = findViewById(R.id.qr_skip_button);
        faceRectView = findViewById(R.id.face_rect_view);
        tvFaceMessage.setTypeface(rubiklight);
        time_attendance_layout = findViewById(R.id.time_attendance_linear_layout);
        button_checkIn = findViewById(R.id.btn_check_in);
        button_checkOut = findViewById(R.id.btn_check_out);
        tvTime = findViewById(R.id.tv_time);
        tvDate = findViewById(R.id.tv_date);
        retryButton = findViewById(R.id.button_retry);
        cameraPreviewLayout = findViewById(R.id.camera_preview_layout);

        tTimer = new Timer();
        tTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                SimpleDateFormat formatter = new SimpleDateFormat("MMMM dd, yyyy hh:mm:ss a", Locale.getDefault()); //yyyy-MM-dd HH:mm:ss
                String currentTime = new SimpleDateFormat("HH:mm aaa", Locale.getDefault()).format(new Date());
                Date curDate = new Date(System.currentTimeMillis());
                final String str = formatter.format(curDate);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (tv_display_time != null)//todo null value
                            tv_display_time.setText(str);
                        if (tvDisplayTimeOnly != null)//todo null value
                            tvDisplayTimeOnly.setText(str);
                        if (tvTime != null && time_attendance_layout.getVisibility() == View.VISIBLE)
                            tvTime.setText(currentTime);
                    }
                });
            }
        }, 0, 1000);

        compareResultList = new ArrayList<>();
        /*RecyclerView recyclerShowFaceInfo = findViewById(R.id.recycler_view_person);
        adapter = new ShowFaceInfoAdapter(compareResultList, this);
        recyclerShowFaceInfo.setAdapter(adapter);
        recyclerShowFaceInfo.setLayoutManager(new MyGridLayoutManager(this, 1));
        recyclerShowFaceInfo.setItemAnimator(new DefaultItemAnimator());*/
        if (isProDevice) {
            faceRectView.setVisibility(View.VISIBLE);
            outerCircle.setVisibility(View.GONE);
        } else if (Util.isDeviceF10()) {
            outerCircle.setVisibility(View.GONE);
        } else {
            outerCircle.setVisibility(View.VISIBLE);
            faceRectView.setVisibility(View.GONE);
        }
        HomeTextOnlyText();
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
        if (intent != null) {
            if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction()) || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction()) ||
                    NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
                mTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                if (mTag != null) {
                    byte[] ID = new byte[20];
                    ID = mTag.getId();
                    String UID = Util.bytesToHexString(ID);
                    if (UID == null) {
                        Toast.makeText(getApplicationContext(), getString(R.string.rfid_card_error), Toast.LENGTH_LONG).show();
                        return;
                    }
                    mNfcIdString = Util.bytearray2Str(Util.hexStringToBytes(UID.substring(2)), 0, 4, 10);
                    onRfidScan(mNfcIdString);
                    return;
                }
                showSnackBarMessage(getString(R.string.rfid_card_error));
            }
        }
    }

    @Override
    protected void onResume() {
        Log.v(TAG, "onResume");
        super.onResume();
        isActivityResumed = true;
        resumeCameraScan();
        if (mMessageReceiver != null) {
            LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("EVENT_SNACKBAR"));
        }
        if (hidReceiver != null) {
            LocalBroadcastManager.getInstance(this).registerReceiver(hidReceiver, new IntentFilter(HIDService.HID_BROADCAST_ACTION));
        } else {
            initHidReceiver();
            LocalBroadcastManager.getInstance(this).registerReceiver(hidReceiver, new IntentFilter(HIDService.HID_BROADCAST_ACTION));
        }
        if (AppSettings.getSecondaryIdentifier() == CameraController.SecondaryIdentification.QR_CODE.getValue()) {
            disableNfc();
        } else {
            enableNfc();
        }
        enableHidReader();
        String longVal = sharedPreferences.getString(GlobalParameters.DELAY_VALUE, "1");
        if (longVal.equals("")) {
            delayMilli = 1;
        } else {
            delayMilli = Long.parseLong(longVal);
        }
        if (preview != null) {
            try {
                preview.start(cameraSource, graphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Exception is camera preview");
            }
        }
        DisplayTimeAttendance();
        if (!sharedPreferences.getBoolean(GlobalParameters.HOME_TEXT_ONLY_IS_ENABLE, false) && !sharedPreferences.getBoolean(GlobalParameters.HOME_TEXT_ONLY_IS_ENABLE, false)) {
            clearLeftFace(null);
        }
        if (ApplicationController.getInstance().isDeviceBoot()) {
            showPrintMsgDialog();
            ApplicationController.getInstance().setDeviceBoot(false);
        }
        updateGestureOnLanguageChange();
    }

    @Override
    protected void onPause() {
        Log.v(TAG, "onPause");
        super.onPause();
        isActivityResumed = false;
        if (preview != null) {
            preview.stop();
        }
        disableNfc();
        if (cameraHelper != null) {
            cameraHelper.stop();
        }
        if (cameraHelperIr != null) {
            cameraHelperIr.stop();
        }
        if (hidReceiver != null) {
            hidReceiver.clearAbortBroadcast();
            LocalBroadcastManager.getInstance(this).unregisterReceiver(hidReceiver);
        }
        if (mMessageReceiver != null) {
            mMessageReceiver.clearAbortBroadcast();
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        ApplicationController.getInstance().setDeviceBoot(false);
    }

    @Override
    protected void onDestroy() {
        Log.v(TAG, "onDestroy");
        super.onDestroy();
        isActivityResumed = false;
        if (nfcDialog != null && nfcDialog.isShowing()) {
            nfcDialog.dismiss();
            nfcDialog = null;
        }
        if (tTimer != null) {
            tTimer.cancel();
            tTimer = null;
        }
        if (pTimer != null) {
            pTimer.cancel();
            pTimer = null;
        }
        if (imageTimer != null) {
            imageTimer.cancel();
            imageTimer = null;
        }
        if (cameraHelper != null) {
            cameraHelper.release();
            cameraHelper = null;
        }
        cancelPreviewTimer();
        if (cameraHelperIr != null) {
            cameraHelperIr.release();
            cameraHelperIr = null;
        }
        if (hidReceiver != null) {
            hidReceiver.clearAbortBroadcast();
            LocalBroadcastManager.getInstance(this).unregisterReceiver(hidReceiver);
            hidReceiver = null;
        }
        if (mMessageReceiver != null) {
            mMessageReceiver.clearAbortBroadcast();
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
            mMessageReceiver = null;
        }
        clearDisposables();
        if (faceEngineHelper != null) {
            try {
                faceEngineHelper.unInitEngine();
            } catch (Exception e) {
                Log.e(TAG, "Exception when releasing Face Engine");
            }
        }

        if (faceHelperIr != null) {
            ConfigUtil.setTrackedFaceCount(this, faceHelperIr.getTrackedFaceCount());
            faceHelperIr.release();
            faceHelperIr = null;
        }

        //FaceServer.getInstance().unInit();
        temperatureBitmap = null;
        clearQrCodePreview();
        resetMaskStatus();
        compareResult = null;
        searchFaceInfoList.clear();
        cancelPreviewIdleTimer();
        if (mNfcAdapter != null && isNfcFDispatchEnabled) {
            mNfcAdapter.disableForegroundDispatch(this);
            isNfcFDispatchEnabled = false;
        }
        Util.enableLedPower(0);
        TemperatureController.getInstance().clearData();
        SoundController.getInstance().clearData();
        clearData();
        CameraController.getInstance().setScanCloseProximityEnabled(false);
        if (BLEController.getInstance().mServiceConnection != null) {
            try {
                unbindService(BLEController.getInstance().mServiceConnection);
                BLEController.getInstance().mServiceConnection = null;
            } catch (Exception e) {
                Log.e(TAG, "BLE unbind Error");
            }
        }
        PrinterController.getInstance().clearData();
        GestureController.getInstance().cancelWaveHandTimer();
    }

    public void runTemperature(int requestId, final UserExportedData data) {
        if (!CameraController.getInstance().isFaceVisible()) return;
        if (CameraController.getInstance().isAppExitTriggered()) {
            if (handler != null) {
                handler.obtainMessage(CameraController.IMAGE_PROCESS_COMPLETE).sendToTarget();
            }
            return;
        }
        if (!isScanWithMaskEnforced()) return;
        if (!CameraController.getInstance().isPrimarySecondaryMemberMatch()) {
            runOnUiThread(() -> {
                Toast.makeText(IrCameraActivity.this, getString(R.string.auth_failed), Toast.LENGTH_LONG).show();
                new Handler().postDelayed(() -> {
                    disableLedPower();
                    resetToHomePage();
                }, 1000);
            });
            return;
        }
        if (Util.isDeviceF10()) {
            launchConfirmationFragment(String.valueOf(false));
            if (isHomeViewEnabled) {
                pauseCameraScan();
            } else {
                isReadyToScan = false;
            }
            resetHomeScreen();
            disableLedPower();
            CameraController.getInstance().updateControllers(registeredMemberslist, data);
            TemperatureController.getInstance().recordUserTemperature(data, -1);
            return;
        }
        if (!AppSettings.isTemperatureScanEnabled()) {
            CameraController.getInstance().setUserExportedData(data);
            onTemperatureScanDisabled();
            return;
        }
        Log.d(TAG, "runTemperature");
        CameraController.getInstance().setScanState(CameraController.ScanState.THERMAL_SCAN);
        TemperatureController.getInstance().setTemperatureRecordData(data);
        TemperatureController.getInstance().setTemperatureListener(this);
        TemperatureController.getInstance().startTemperatureMeasure(requestId);
    }

    private void initRgbCamera() {
        final Activity that = this;
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        final FaceListener faceListener = new FaceListener() {
            @Override
            public void onFail(Exception e) {
                Logger.error(TAG, "initRgbCamera.FaceListener.onFail()", "Exception in FaceListener callback" + e.getMessage());

            }

            @Override
            public void onFaceFeatureInfoGet(@Nullable final FaceFeature faceFeature, final Integer requestId, final Integer errorCode) {
                if ((that != null && that.isDestroyed())) return;
                //TODO: clone
                final Bitmap rgbBitmapClone = rgbBitmap == null ? null : rgbBitmap.copy(rgbBitmap.getConfig(), false);
                final Bitmap irBitmapClone = irBitmap == null ? null : irBitmap.copy(irBitmap.getConfig(), false);
                Log.v(TAG, String.format("onFaceFeatureInfoGet irBitmapClone: %s, rgbBitmapClone: %s", irBitmapClone, rgbBitmapClone));
                if (!AppSettings.isTemperatureScanEnabled()) {
                    if (!faceDetectEnabled && !rfIdEnable && !AppSettings.isPrintLabelFace()) {
                        return;
                    }
                }
                if (faceFeature != null) {
                    isFaceIdentified = false;
                    Logger.verbose(TAG, "initRgbCamera.FaceListener.onFaceFeatureInfoGet()", " compareResultList= " + compareResult + " trackId = " + requestId + " isIdentified = " + ",tempServiceColes ");

                    // isIdentified = false;
                    if (compareResultList != null) {
                        for (int i = compareResultList.size() - 1; i >= 0; i--) {
                            if (compareResultList.get(i).getTrackId() == requestId) {
                                isFaceIdentified = true;
                                break;
                            }
                        }
                    }

                    if (CameraController.getInstance().isScanCloseProximityEnabled()) {
                        if (isProDevice) {
                            runOnUiThread(() -> {
                                if (!AppSettings.isMaskEnforced()) {
                                    changeVerifyBackground(R.color.colorTransparency, true);
                                    relative_main.setVisibility(View.GONE);
                                    startCameraPreviewTimer();
                                }
                            });
                        }
                        if (!AppSettings.isLivenessDetect()) {
                            checkFaceClosenessAndSearch(faceFeature, requestId, rgbBitmapClone, irBitmapClone);
                        } else if (!faceDetectEnabled) {
                            checkFaceClosenessAndSearch(faceFeature, requestId, rgbBitmapClone, irBitmapClone);
                        }
                    } else if (!isFaceIdentified) {
                        if (!Util.isOfflineMode(IrCameraActivity.this)) {
                            showCameraPreview(faceFeature, requestId, rgbBitmapClone, irBitmapClone);
                            if (isFindTemperature() && !Util.isOfflineMode(IrCameraActivity.this)) {
                                runTemperature(requestId, new UserExportedData(rgbBitmap, irBitmap, new RegisteredMembers(), 0));
                            }
                        }
                    }

                    Integer liveness = livenessMap.get(requestId);
                    initiateFaceSearch(faceFeature, requestId, liveness, rgbBitmapClone, irBitmapClone);
                    if (!AppSettings.isLivenessDetect()) {
                        /*if(sharedPreferences.getBoolean(GlobalParameters.FACIAL_DETECT,false)){
                            Logger.debug(TAG, " Facial Score ---  not liveness Defect ");
                            searchFace(faceFeature, requestId);
                        }*/
                    } else if (liveness != null && liveness == LivenessInfo.ALIVE) {
                        /*Logger.debug(TAG, "initRgbCamera.FaceListener.onFaceFeatureInfoGet()", "Liveness info Alive, isTemperature " + isTemperature);
                        if(sharedPreferences.getBoolean(GlobalParameters.FACIAL_DETECT,false)) {
                            Logger.debug(TAG, " Facial Score ---  check facial defect");
                            searchFace(faceFeature, requestId);
                        }*/
                    } else {

//                        if (requestFeatureStatusMap.containsKey(requestId)) {
//                            Observable.timer(WAIT_LIVENESS_INTERVAL, TimeUnit.MILLISECONDS)
//                                    .subscribe(new Observer<Long>() {
//                                        Disposable disposable;
//
//                                        @Override
//                                        public void onSubscribe(Disposable d) {
//                                            disposable = d;
//                                            getFeatureDelayedDisposables.add(disposable);
//                                        }
//
//                                        @Override
//                                        public void onNext(Long aLong) {
//                                            onFaceFeatureInfoGet(faceFeature, requestId, errorCode);
//                                        }
//
//                                        @Override
//                                        public void onError(Throwable e) {
//                                            Logger.error(TAG, "initRgbCamera.FaceListener.onFaceFeatureInfoGet()", "Wait Liveness Interval observable error" + e.getMessage());
//                                        }
//
//                                        @Override
//                                        public void onComplete() {
//                                            getFeatureDelayedDisposables.remove(disposable);
//                                        }
//                                    });
//                        }
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
                        //faceHelperIr.setName(requestId, getString(R.string.recognize_failed_notice, msg));
                        requestFeatureStatusMap.put(requestId, RequestFeatureStatus.FAILED);
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
                        //faceHelperIr.setName(requestId, getString(R.string.recognize_failed_notice, "NOT_ALIVE"));
                        //  FAIL_RETRY_INTERVAL UNKNOWN
                        retryLivenessDetectDelayed(requestId);
                    } else if (liveness == LivenessInfo.ALIVE) {
                        requestFeatureStatusMap.put(requestId, RequestFeatureStatus.TO_RETRY);
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
                        //faceHelperIr.setName(requestId, getString(R.string.recognize_failed_notice, msg));
                        livenessMap.put(requestId, LivenessInfo.NOT_ALIVE);
                        retryLivenessDetectDelayed(requestId);
                    } else {
                        livenessMap.put(requestId, LivenessInfo.UNKNOWN);
                    }
                }
            }

        };
        CameraListener rgbCameraListener = new RgbCameraListener(faceListener);
        int previewHeight = previewViewRgb.getMeasuredHeight();
        if (!isNavigationBarOn) {
            previewHeight = CameraController.getInstance().CAMERA_PREVIEW_HEIGHT;
        }
        if (Build.VERSION.SDK_INT >= 26) {
            orientationValue = 270;
            previewHeight = CameraController.getInstance().CAMERA_PREVIEW_HEIGHT;
        }
        if (Util.isDeviceF10()) {
            previewHeight = previewViewRgb.getMeasuredHeight();
        }
        cameraHelper = new DualCameraHelper.Builder()
                .previewViewSize(new Point(previewViewRgb.getMeasuredWidth(), previewHeight))
                .rotation(Util.getOrientation(sharedPreferences))
                .specificCameraId(cameraRgbId != null ? cameraRgbId : Camera.CameraInfo.CAMERA_FACING_BACK)
                .previewOn(previewViewRgb)
                .cameraListener(rgbCameraListener)
                .isMirror(cameraRgbId != null && Camera.CameraInfo.CAMERA_FACING_FRONT == cameraRgbId)
                .build();
        cameraHelper.init();
        try {
            cameraHelper.start();
        } catch (RuntimeException e) {
            Logger.error(TAG, "Error in opening Rgb camera");
        }
    }

    private void initIrCamera() {
        CameraListener irCameraListener = new IrCameraListener();

        int previewHeight = previewViewIr.getMeasuredHeight();
        if (!isNavigationBarOn) {
            previewHeight = CameraController.getInstance().CAMERA_PREVIEW_HEIGHT;
        }
        if (Build.VERSION.SDK_INT >= 26) {
            orientationValue = 270;
            previewHeight = CameraController.getInstance().CAMERA_PREVIEW_HEIGHT;
        }
        if (Util.isDeviceF10()) {
            previewHeight = previewViewIr.getMeasuredHeight();
        }
        cameraHelperIr = new DualCameraHelper.Builder()
                .previewViewSize(new Point(previewViewIr.getMeasuredWidth(), previewHeight))
                .rotation(sharedPreferences.getInt(GlobalParameters.Orientation, orientationValue))
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
            Logger.debug(TAG, "initIrCamera()", "Exception in IrCamera start" + e.getMessage());
        }
    }


    private synchronized void processPreviewData(byte[] rgbData) {
        if (!isHomeViewEnabled && !isReadyToScan) return;
        if (rgbData != null && irData != null) {
            byte[] cloneNv21Rgb;
            if (scanMode == 1) {
                cloneNv21Rgb = rgbData.clone();
            } else {
                cloneNv21Rgb = irData.clone();
            }
            List<FacePreviewInfo> facePreviewInfoList = faceHelperIr.onPreviewFrame(cloneNv21Rgb);
            if (isProDevice) {
                List<FacePreviewInfo> faceList = new ArrayList<>();
                if (facePreviewInfoList.size() > 0) {
                    faceList.add(facePreviewInfoList.get(0));
                    TemperatureController.getInstance().setRect(faceList, drawHelperRgb);
                }
                if (faceRectView != null) {
                    faceRectView.clearFaceInfo();
                }
                if (drawHelperRgb != null) {
                    drawPreviewInfo(facePreviewInfoList);
                }
            }
            clearLeftFace(facePreviewInfoList);
            if (facePreviewInfoList != null && facePreviewInfoList.size() > 0 && previewSize != null) {
                for (int i = 0; i < facePreviewInfoList.size(); i++) {
                    Integer status = requestFeatureStatusMap.get(facePreviewInfoList.get(i).getTrackId());
                    if (AppSettings.isLivenessDetect() && (status == null || status != RequestFeatureStatus.SUCCEED)) {
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
                        if (CameraController.getInstance().getScanState() == CameraController.ScanState.THERMAL_SCAN) {
                            Log.d(TAG, "Scan state is Thermal scan");
                            return;
                        }
                        CameraController.getInstance().setFaceVisible(true);
                        requestFeatureStatusMap.put(facePreviewInfoList.get(i).getTrackId(), RequestFeatureStatus.SEARCHING);
                        //TemperatureController.getInstance().addTrackId(facePreviewInfoList.get(i).getTrackId());
                        faceHelperIr.requestFaceFeature(cloneNv21Rgb, facePreviewInfoList.get(i).getFaceInfo(),
                                previewSize.width, previewSize.height, FaceEngine.CP_PAF_NV21,
                                facePreviewInfoList.get(i).getTrackId());
                    }
                }
            } else {
                CameraController.getInstance().setFaceVisible(false);
            }
            irData = null;
        }

    }

    private void initPreviewTimer() {
        if (!isFaceNotDetectedTimer) {
            cancelImageTimer();
            isFaceNotDetectedTimer = true;
            previewTimer = new Timer();
            previewTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(() -> {
                        if ((relative_main.getVisibility() != View.VISIBLE) || Util.isDeviceF10()) {
                            cancelImageTimer();
                            retryButton.setVisibility(View.VISIBLE);
                            tv_thermal.setVisibility(View.GONE);
                            Toast.makeText(IrCameraActivity.this, getString(R.string.face_not_detected), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }, 15 * 1000);
        }
    }

    private void cancelPreviewTimer() {
        if (previewTimer != null) {
            previewTimer.cancel();
            previewTimer = null;
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
                //  startMemberSyncService();
            } else {
                Toast.makeText(getApplicationContext(), R.string.permission_denied, Toast.LENGTH_SHORT).show();
                Logger.error(TAG, "onRequestPermissionsResult()", "Permission denied");
            }
        }
    }


    private void clearLeftFace(List<FacePreviewInfo> facePreviewInfoList) {
        if (compareResultList != null) {
            for (int i = compareResultList.size() - 1; i >= 0; i--) {
                if (!requestFeatureStatusMap.containsKey(compareResultList.get(i).getTrackId()) && !faceDetectEnabled) {
                    Logger.debug(TAG, "clearLeftFace()", "CompareResultList failed, Remove and exit face. TrackId = " + compareResultList.get(i).getTrackId());
                    compareResultList.remove(i);
                    //adapter.notifyItemRemoved(i);
                    tv_message.setText("");
                    tv_message.setVisibility(View.GONE);
                    //   tvDisplayingCount.setVisibility(View.GONE);

                    temperature_image.setVisibility(View.GONE);
                    tvErrorMessage.setVisibility(View.GONE);
                    tvFaceMessage.setVisibility(View.GONE);
                    homeDisplayView();
                    mask_message.setText("");
                    mask_message.setVisibility(View.GONE);
                    disableLedPower();
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


    private void showResult(CompareResult compareResult, int requestId, String name, String id, String facescore, final boolean isdoor) {
        //When adding display personnel, save their trackId
        compareResult.setTrackId(requestId);
        compareResult.setMessage(name);
        compareResult.setMemberId(id);
        compareResult.setFacialScore(facescore);
        compareResultList.add(compareResult);
        this.compareResult = compareResult;
    }

    @Override
    public void onGlobalLayout() {
        previewViewRgb.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        if (!checkPermissions(NEEDED_PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, NEEDED_PERMISSIONS, ACTION_REQUEST_PERMISSIONS);
        } else {
            if (AppSettings.getTimeAndAttendance() == 0) {
                faceEngineHelper.initEngine(this);
                initScan();
            }
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
                        if (AppSettings.isLivenessDetect()) {
                            //faceHelperIr.setName(requestId, Integer.toString(requestId));
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
                        //faceHelperIr.setName(requestId, Integer.toString(requestId));
                        requestFeatureStatusMap.put(requestId, RequestFeatureStatus.TO_RETRY);
                        delayFaceTaskCompositeDisposable.remove(disposable);
                    }
                });
    }

    private void restoreCameraAfterScan(Boolean isShow) {
        initRgbCamera();
        initIrCamera();
        if (isShow) showGuestToast();
    }

    private void cancelImageTimer() {
        if (imageTimer != null) {
            imageTimer.cancel();
            imageTimer = null;
        }

    }

    private void showSnackbar(int actionCode) {
        tv_sync.setTypeface(rubiklight);
        if (actionCode == MemberSyncDataModel.SYNC_START) {
            tv_sync.setText(totalCount++ + " " + getString(R.string.out_of) + " " + memberCount);
        } else if (actionCode == MemberSyncDataModel.SYNC_COMPLETED) {
            tv_sync.setText(getString(R.string.sync_completed));
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    tv_sync.setText("");
                }
            }, 2 * 1000);
        } else if (actionCode == MemberSyncDataModel.SYNC_PHOTO_FAILED) {
            String message = String.format(getString(R.string.image_sync_failed_msg), memberCount);
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        } else if (actionCode == MemberSyncDataModel.SYNC_GROUP_ID_NOT_EXIST) {
            //  Toast.makeText(this, getString(R.string.sync_failed), Toast.LENGTH_LONG).show();
        } else {
            tv_sync.setText(getString(R.string.syncing));
        }
    }

    private void changeVerifyBackground(int id, boolean isVisible) {
        if (outerCircle == null || innerCircle == null || relativeLayout == null)
            return;

        //outerCircle.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        //innerCircle.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        relativeLayout.setBackground(getDrawable(id));
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
        if (isDestroyed() || tv_message == null) return;
        try {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Logger.verbose(TAG, "ShowLauncherView()", "Display Home page start");
                    if (!isHomeViewEnabled) {
                        cancelPreviewTimer();
                        final Activity that = IrCameraActivity.this;
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (tv_message != null) tv_message.setVisibility(View.GONE);
                                if (tvErrorMessage != null) tvErrorMessage.setVisibility(View.GONE);
                                tvFaceMessage.setVisibility(View.GONE);
                                if (temperature_image != null)
                                    temperature_image.setVisibility(View.GONE);
                                if (mask_message != null) mask_message.setVisibility(View.GONE);
                                clearData();
                            }
                        }, 1 * 1000);
                        return;
                    }
                    requestFeatureStatusMap.put(0, RequestFeatureStatus.FAILED);

                    TemperatureController.getInstance().setTemperatureListener(null);
                    resetToHomePage();
                }
            });
        } catch (Exception e) {
            Logger.debug(TAG, "ShowLauncherView()", "Exception in launching Home page" + e.getMessage());
        }
    }

    @Override
    public void onAccessGranted() {
        SoundController.getInstance().playAccessGrantedSound();
        runOnUiThread(() -> {
            if ((AppSettings.getTimeAndAttendance() == 1)) {
                if (ApplicationController.getInstance().getTimeAttendance() == 1)
                    Toast.makeText(IrCameraActivity.this, getString(R.string.checked_in), Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(IrCameraActivity.this, getString(R.string.checked_out), Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(IrCameraActivity.this, getString(R.string.access_control_granted), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onAccessDenied() {
        SoundController.getInstance().playAccessDeniedSound();
        runOnUiThread(() -> {
            Toast.makeText(IrCameraActivity.this, getString(R.string.access_control_denied), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onCheckInOutStatus() {
        runOnUiThread(() -> {
            RegisteredMembers registeredMember = AccessCardController.getInstance().getCheckedInOutMember();
            if (registeredMember != null) {
                String dateTimeCheckInOut = "";
                if (ApplicationController.getInstance().getTimeAttendance() == 1) {
                    if (registeredMember.dateTimeCheckInOut.isEmpty())
                        dateTimeCheckInOut = Util.getMMDDYYYYDate();
                } else if (ApplicationController.getInstance().getTimeAttendance() == 2) {
                    dateTimeCheckInOut = "";
                }
                registeredMember.dateTimeCheckInOut = dateTimeCheckInOut;
                DatabaseController.getInstance().updateMember(registeredMember);
                AccessCardController.getInstance().setCheckedInOutMember(null);
                ApplicationController.getInstance().setTimeAttendance(0);
            }
        });
    }

    @Override
    public void onMemberDetailsReceived(MemberData memberData) {
        runOnUiThread(() -> {
            if (memberData != null) {
                onRfidScan(memberData.accessId);
            }
        });
    }

    @Override
    public void onGetLastCheckInTime(boolean checkedIn) {
        runOnUiThread(() -> {
            if (checkedIn) {
                QrCodeController.getInstance().setMemberCheckedIn(true);
                isReadyToScan = true;
                setCameraPreview();
                new Handler().postDelayed(() -> runTemperature(CameraController.getInstance().getRequestId(),
                        CameraController.getInstance().getData()), 1000);
            } else {
                enableQrCodeScan();
                isReadyToScan = false;
            }
        });
    }

    @Override
    public void onBleUpdateStatus(String status) {
        runOnUiThread(() -> {
            Log.d(TAG, "Ble Update Status " + status);
        });
    }

    @Override
    public void onBleDataReceived(WritePacket writePacket) {
        runOnUiThread(() -> {
            if (AppSettings.getPrimaryIdentifier() == CameraController.PrimaryIdentification.RFID.getValue() ||
                    AppSettings.getPrimaryIdentifier() == CameraController.PrimaryIdentification.QRCODE_OR_RFID.getValue() ||
                    AppSettings.getSecondaryIdentifier() == CameraController.SecondaryIdentification.RFID.getValue() ||
                    AppSettings.getSecondaryIdentifier() == CameraController.SecondaryIdentification.QRCODE_OR_RFID.getValue()) {
                if (writePacket.getGuid() != null && !writePacket.getGuid().isEmpty()) {
                    List<RegisteredMembers> registeredMembers = DatabaseController.getInstance().findMemberByGuid(writePacket.getGuid());
                    if (registeredMembers != null && registeredMembers.size() > 0) {
                        RegisteredMembers member = registeredMembers.get(0);
                        onRfidScan(member.accessid);
                        return;
                    }
                    //Make API call and get AccessId
                    AccessCardController.getInstance().getMemberDetailsByGuid(writePacket.getGuid());
                }
            }
        });
    }

    private class IrCameraListener implements CameraListener {
        private Camera.Parameters cameraParameters;

        @Override
        public void onCameraOpened(Camera camera, int cameraId, int displayOrientation, boolean isMirror) {
            cameraParameters = camera.getParameters();
            previewSizeIr = camera.getParameters().getPreviewSize();
            boolean mirrorHorizontal = false;
            if (isProDevice) {
                mirrorHorizontal = true;
            }
            drawHelperIr = new DrawHelper(previewSizeIr.width, previewSizeIr.height, previewViewIr.getWidth(), previewViewIr.getHeight(), displayOrientation,
                    cameraId, isMirror, mirrorHorizontal, false);
        }


        @Override
        public void onPreview(final byte[] nv21, final Camera camera) {
            irData = nv21;
            irBitmap = Util.convertYuvByteArrayToBitmap(nv21, cameraParameters);
        }

        @Override
        public void onCameraClosed() {
            Logger.verbose(TAG, "IrCameraListener.onCameraClosed()", "onCameraClosed");
        }

        @Override
        public void onCameraError(Exception e) {
            Logger.debug(TAG, "IrCameraListener.onCameraError()", "Error occurred, exception = " + e.getMessage());
        }

        @Override
        public void onCameraConfigurationChanged(int cameraID, int displayOrientation) {
            if (drawHelperIr != null) {
                drawHelperIr.setCameraDisplayOrientation(displayOrientation);
            }
            Logger.verbose(TAG, "IrCameraListener.onCameraConfigurationChanged()-CameraId:", cameraID + "DisplayOrientation:" + displayOrientation);
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
            boolean mirrorHorizontal = false;
            if (isProDevice) {
                mirrorHorizontal = true;
            }
            drawHelperRgb = new DrawHelper(previewSize.width, previewSize.height, previewViewRgb.getWidth(), previewViewRgb.getHeight(), displayOrientation,
                    cameraId, isMirror, mirrorHorizontal, false);
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
            if (nv21 == null || camera == null) return;
            if ((rfIdEnable || qrCodeEnable || AppSettings.isEnableHandGesture()) && !isReadyToScan) {
                return;
            }
            processPreviewData(nv21);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    rgbBitmap = Util.convertYuvByteArrayToBitmap(nv21, camera);
                }
            });
        }

        @Override
        public void onCameraClosed() {
            Logger.verbose(TAG, "RgbCameraListener.onCameraClosed()", "onCameraClosed");
        }

        @Override
        public void onCameraError(Exception e) {
            Logger.debug(TAG, "onCameraError: " + e.getMessage());
            Logger.debug(TAG, "RgbCameraListener.onCameraError()", "Error occurred, exception = " + e.getMessage());
        }

        @Override
        public void onCameraConfigurationChanged(int cameraID, int displayOrientation) {
            if (drawHelperRgb != null) {
                drawHelperRgb.setCameraDisplayOrientation(displayOrientation);
            }
            Logger.verbose(TAG, "RgbCameraListener.onCameraConfigurationChanged()-CameraId:", cameraID + "DisplayOrientation:" + displayOrientation);
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
            frInitCode = frEngine.init(context, DetectMode.ASF_DETECT_MODE_IMAGE, DetectFaceOrientPriority.ASF_OP_ALL_OUT,
                    16, MAX_DETECT_NUM, FaceEngine.ASF_FACE_RECOGNITION | FaceEngine.ASF_FACE_DETECT | processMask);

            flEngine = new FaceEngine();
            if (AppSettings.isLivenessDetect()) {
                LivenessParam livenessParam = new LivenessParam(0.5f, 0.7f);
                flEngine.setLivenessParam(livenessParam);
            }
            flInitCode = flEngine.init(context, DetectMode.ASF_DETECT_MODE_IMAGE, DetectFaceOrientPriority.ASF_OP_ALL_OUT,
                    16, MAX_DETECT_NUM, FaceEngine.ASF_IR_LIVENESS | FaceEngine.ASF_FACE_DETECT | processMask);

            Logger.verbose(TAG, "FaceEngineHelper.initEngine()-Face EngineHelper init with code: ", flInitCode);

            if (ftInitCode != ErrorInfo.MOK) {
                String error = getString(R.string.specific_engine_init_failed, "ftEngine", ftInitCode);
                Logger.verbose(TAG, "FaceEngineHelper.initEngine()-Face Detect init code is not Error MOK, Error: ", error);
                // Toast.makeText(this,error,Toast.LENGTH_SHORT).show();
            }
            if (frInitCode != ErrorInfo.MOK) {
                String error = getString(R.string.specific_engine_init_failed, "frEngine", ftInitCode);
                Logger.verbose(TAG, "FaceEngineHelper.initEngine()-Face Recognition init code is not Error MOK, Error: ", error);
                // Toast.makeText(this,error,Toast.LENGTH_SHORT).show();
            }
            if (flInitCode != ErrorInfo.MOK) {
                String error = getString(R.string.specific_engine_init_failed, "flEngine", ftInitCode);
                Logger.verbose(TAG, "FaceEngineHelper.initEngine()-Face IrLiveness init code is not Error MOK, Error: ", error);
                // Toast.makeText(this,error,Toast.LENGTH_SHORT).show();
            }
        }


        public void unInitEngine() {
            if (ftInitCode == ErrorInfo.MOK && ftEngine != null) {
                synchronized (ftEngine) {
                    int ftUnInitCode = ftEngine.unInit();
                    Logger.verbose(TAG, "FaceEngineHelper.unInitEngine()-Face detection UnInitEngine with code:", ftUnInitCode);
                }
            }
            if (frInitCode == ErrorInfo.MOK && frEngine != null) {
                synchronized (frEngine) {
                    int frUnInitCode = frEngine.unInit();
                    Logger.verbose(TAG, "FaceEngineHelper.unInitEngine()-Face recognition UnInitEngine with code:", frUnInitCode);
                }
            }
            if (flInitCode == ErrorInfo.MOK && flEngine != null) {
                synchronized (flEngine) {
                    int flUnInitCode = flEngine.unInit();
                    Logger.verbose(TAG, "FaceEngineHelper.unInitEngine()-Face IrLiveness UnInitEngine with code:", flUnInitCode);
                }
            }
        }
    }

    private void TemperatureCallBackUISetup(final boolean aboveThreshold, final String temperature, final String tempValue,
                                            final boolean lowTemp, final UserExportedData data) {
        if (isDestroyed()) return;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                cancelImageTimer();
                dismissSnackBar();
                if (outerCircle != null) {
                    outerCircle.setBackgroundResource(R.drawable.border_shape);
                }
                if (tvErrorMessage != null) {
                    tvErrorMessage.setVisibility(View.GONE);
                }
                retryButton.setVisibility(View.GONE);
                tvFaceMessage.setVisibility(View.GONE);
                // requestFeatureStatusMap.put(requestId, RequestFeatureStatus.FAILED);
                if (AppSettings.isTemperatureResultBar()) {
                    tv_message.setVisibility(View.VISIBLE);
                    tv_message.setTextColor(getResources().getColor(R.color.colorWhite));
                    tv_message.setBackgroundColor(aboveThreshold ? getResources().getColor(R.color.colorRed) : getResources().getColor(R.color.colorBgGreen));
                    tv_message.setText(temperature);
                    tv_message.setTypeface(rubiklight);
                } else {
                    tv_message.setVisibility(View.GONE);
                    if (maskEnabled) {
                        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, mask_message.getLayoutParams().height);
                        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                        mask_message.setLayoutParams(params);
                    }
                }

                showMaskStatus();
                boolean sendAboveThreshold = sharedPreferences.getBoolean(GlobalParameters.CAPTURE_IMAGES_ABOVE, true) && aboveThreshold;
                if (data != null) {
                    data.exceedsThreshold = aboveThreshold;
                    data.temperature = tempValue;
                    data.sendImages = sharedPreferences.getBoolean(GlobalParameters.CAPTURE_IMAGES_ALL, false) || sendAboveThreshold;
                    data.thermal = temperatureBitmap;
                    if (maskEnabled) {
                        data.maskStatus = String.valueOf(maskStatus);
                    } else {
                        data.maskStatus = String.valueOf(-2);
                    }
                    data.triggerType = CameraController.getInstance().getTriggerType();
                    data.machineTemperature = String.valueOf(TemperatureController.getInstance().getMachineTemperature());
                    data.ambientTemperature = String.valueOf(TemperatureController.getInstance().getAmbientTemperature());
                    userData = data;
                    int syncStatus;
                    if (Util.isOfflineMode(IrCameraActivity.this)) {
                        syncStatus = 1;
                    } else {
                        syncStatus = -1;
                    }
                    if (isProDevice) {
                        TemperatureController.getInstance().recordUserTemperature(data, syncStatus);
                    } else {
                        if (data.thermal != null) {
                            isRecordNotSent = false;
                            TemperatureController.getInstance().recordUserTemperature(data, syncStatus);
                        } else {
                            isRecordNotSent = true;
                        }
                    }
                }

                if (PrinterController.getInstance().isPrintScan(aboveThreshold)) {
                    return;
                }
                if (lanchTimer != null) {
                    lanchTimer.cancel();
                    lanchTimer = null;
                }
                lanchTimer = new Timer();
                lanchTimer.schedule(new TimerTask() {
                    public void run() {
                        disableLedPower();
                        boolean confirmAboveScreen = sharedPreferences.getBoolean(GlobalParameters.CONFIRM_SCREEN_ABOVE, true) && aboveThreshold;
                        boolean confirmBelowScreen = sharedPreferences.getBoolean(GlobalParameters.CONFIRM_SCREEN_BELOW, true) && !aboveThreshold;
                        if (confirmAboveScreen || confirmBelowScreen) {
                            runOnUiThread(() -> {
                                if (isDestroyed()) return;
//                                if (userData != null && userData.member.firstname != null) {
//                                    if ((AppSettings.getTimeAndAttendance() == 1) && Util.isAlreadyChecked(userData.member.getDateTimeCheckInOut())) {
//                                        isAlreadyCheckEdOut();
//                                        return;
//                                    }
//
//                                }
                                launchConfirmationFragment(String.valueOf(aboveThreshold));
                                if (isHomeViewEnabled) {
                                    pauseCameraScan();
                                } else {
                                    isReadyToScan = false;
                                }
                                resetHomeScreen();
                            });
                            compareResultList.clear();
                            if (data != null) {
                                data.compareResult = null;  //Make the compare result null to avoid update again
                            }
                        } else {
                            ShowLauncherView();
                        }
                    }
                }, delayMilli * 1000);
            }
        });
    }

    //Optimize this can move to Utils
    public void enableLedPower() {
        if (ledSettingEnabled) {
            Util.enableLedPower(1);
        }
    }

    //Optimize this can move to Utils
    public void disableLedPower() {
        if (ledSettingEnabled) {
            Util.enableLedPower(0);
        }
    }

    /**
     * Method that stop the HealthCheck service
     */
//    private void stopHealthCheckService() {
//        Intent intent = new Intent(this, DeviceHealthService.class);
//        stopService(intent);
//    }
    @Override
    public void onJSONObjectListener(String reportInfo, String status, JSONObject req) {
        try {
            if (reportInfo == null) {
                return;
            }
            if (reportInfo.contains("Authorization has been denied for this request"))
                Util.getToken(this, this);

        } catch (Exception e) {
            Logger.error(TAG, "onJSONObjectListener(JSONObject reportInfo, String status, JSONObject req)", e.getMessage());
        }

    }

    /*@Override
    public void onJSONObjectListenerTemperature(JSONObject reportInfo, String status, JSONObject req) {
        try {
            if (reportInfo == null) {
                Util.recordUserTemperature(IrCameraActivity.this, IrCameraActivity.this, userData, 1);
                return;
            }
            if (reportInfo.has("responseCode")) {
                if (!reportInfo.getString("responseCode").equals("1")) {
                    Util.recordUserTemperature(IrCameraActivity.this, IrCameraActivity.this, userData, 1);
                }
            } else {
                Util.recordUserTemperature(IrCameraActivity.this, IrCameraActivity.this, userData, 1);
            }
            if (reportInfo.isNull("Message")) return;
            if (reportInfo.getString("Message").contains("token expired"))
                Util.getToken(this, this);

        } catch (Exception e) {
            Logger.error(TAG, "onJSONObjectListenerTemperature(JSONObject reportInfo, String status, JSONObject req)", e.getMessage());
        }
    }*/

    private void createCameraSource(String model) {
        // If there's no existing cameraSource, create one.
        if (cameraSource == null) {
            cameraSource = new CameraSource(this, graphicOverlay);
        }
        if (Util.isDeviceF10()) {
            cameraSource.setFacing(CameraSource.CAMERA_FACING_BACK);
        } else {
            cameraSource.setFacing(CameraSource.CAMERA_FACING_FRONT);
        }
        try {
            switch (model) {
                case BARCODE_DETECTION:
                    Log.i(TAG, "Using Custom Image Classifier Processor");
                    cameraSource.setMachineLearningFrameProcessor(new BarcodeScannerProcessor(this, (BarcodeSendData) this));
                    break;
                default:
                    Log.e(TAG, "Unknown model: " + model);
            }
        } catch (Exception e) {
            Log.e(TAG, "can not create camera source: " + model);
        }
    }

    private void startCameraSource() {
        if (cameraSource != null) {
            try {
                if (preview == null) {
                    Log.d(TAG, "resume: Preview is null");
                }
                preview.start(cameraSource, graphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source " + e.getMessage());
                cameraSource.release();
                cameraSource = null;
            }
        }
    }

    @Override
    public void onBarcodeData(String guid) {
        try {
            if (isTopFragmentGesture() ||
                    CameraController.getInstance().getTriggerType().equals(CameraController.triggerValue.WAVE.toString()))
                return;
            if (!qrCodeReceived) {
                qrCodeReceived = true;
                CameraController.getInstance().updateTriggerType(CameraController.triggerValue.CODEID.toString());
                preview.stop();
                frameLayout.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                tv_scan.setBackgroundColor(getResources().getColor(R.color.colorOrange));
                tv_scan.setTextColor(getResources().getColor(R.color.black));
                qr_main.setBackgroundColor(getResources().getColor(R.color.colorTransparency));
                if (guid.startsWith("shc:")) {
                    QrCodeController.getInstance().smartHealthCard(guid, this);
                    clearQrCodePreview();
                    qrCodeReceived = false;
                    setCameraPreview();
                    return;
                } else if (guid.startsWith("HC1:")) {
                    QrCodeController.getInstance().parseQrText(guid, this);
                    clearQrCodePreview();
                    qrCodeReceived = false;
                    setCameraPreview();
                    return;
                } else if (QrCodeController.getInstance().isQrCodeDated(guid)) {
                    tv_scan.setText(R.string.tv_qr_validating);
                    if (QrCodeController.getInstance().validateDatedQrCode(guid)) {
                        CameraController.getInstance().setQrCodeId(guid);
                        Util.writeString(sharedPreferences, GlobalParameters.ACCESS_ID, guid);
                        clearQrCodePreview();
                        setCameraPreview();
                        SoundController.getInstance().playValidQrSound();
                    } else {
                        resetInvalidQrCode();
                        SoundController.getInstance().playInvalidQrSound();
                    }
                    qrCodeReceived = false;
                    return;
                } else if ((Util.isNumeric(guid) || !Util.isQRCodeWithPrefix(guid)) && AppSettings.isAnonymousQREnable()) {
                    tv_scan.setText(R.string.tv_bar_validating);
                    CameraController.getInstance().setQrCodeId(guid);
                    Util.writeString(sharedPreferences, GlobalParameters.ACCESS_ID, guid);
                    /*if ((CameraController.getInstance().getScanProcessState()
                            == CameraController.ScanProcessState.FIRST_SCAN) ||
                            (CameraController.getInstance().getScanProcessState() ==
                                    CameraController.ScanProcessState.FIRST_SCAN_COMPLETE)) {
                        initSecondaryScan();
                    } else {
                        clearQrCodePreview();
                        setCameraPreview();
                    }*/
                    CameraController.ScanProcessState state = CameraController.getInstance().getScanProcessState();
                    if (state == CameraController.ScanProcessState.SECOND_SCAN) {
                        CameraController.getInstance().updateScanState(CameraController.ScanProcessState.SECOND_SCAN_COMPLETE);
                    }
                    clearQrCodePreview();
                    if (AppSettings.getSecondaryIdentifier() == CameraController.SecondaryIdentification.RFID.getValue()) {
                        resetQrCode();
                        initSecondaryScan();
                    } else {
                        setCameraPreview();
                    }
                    SoundController.getInstance().playValidQrSound();
                    qrCodeReceived = false;
                    return;
                }
                tv_scan.setText(R.string.tv_qr_validating);
                img_qr.setVisibility(View.VISIBLE);
                img_qr.setBackgroundResource(R.drawable.qrimage);

                if (isInstitutionIdEmpty()) {
                    qrCodeReceived = false;
                    return;
                }

                //Make API call
                Util.writeString(sharedPreferences, GlobalParameters.QRCODE_ID, guid);
                CameraController.getInstance().setQrCodeId(guid);
                if (sharedPreferences.getBoolean(GlobalParameters.ONLINE_MODE, true)) {
                    startQRTimer(guid);
                    JSONObject obj = new JSONObject();
                    obj.put("qrCodeID", guid);
                    obj.put("institutionId", sharedPreferences.getString(GlobalParameters.INSTITUTION_ID, ""));
                    new AsyncJSONObjectQRCode(obj, this, sharedPreferences.getString(GlobalParameters.URL, EndPoints.prod_url) + EndPoints.ValidateQRCode, this).execute();
                }
            }
        } catch (Exception e) {
            qrCodeReceived = false;
            cancelQRTimer();
            Log.e(TAG + "onBarCodeData", e.getMessage());
        }
    }

    @Override
    public void onJSONObjectListenerQRCode(JSONObject reportInfo, String status, JSONObject req) {
        qrCodeReceived = false;
        cancelQRTimer();
        try {
            if (reportInfo == null) {
                resetInvalidQrCode();
                Logger.debug(TAG, reportInfo.toString());
                return;
            }
            if (reportInfo.has("responseCode") && reportInfo.getString("responseCode").equals("1")) {
                runOnUiThread(() -> {
                    if (isReadyToScan) return;
                    if ((AppSettings.getSecondaryIdentifier() == CameraController.SecondaryIdentification.QR_CODE.getValue())
                            && !AppSettings.isAskQrCodeAlwaysEnabled()) {
                        RegisteredMembers registeredMember = registeredMemberslist.get(0);
                        if (registeredMember != null) {
                            registeredMember.isMemberAccessed = true;
                            DatabaseController.getInstance().updateMember(registeredMember);
                        }
                    }
                    CameraController.ScanProcessState state = CameraController.getInstance().getScanProcessState();
                    if (state == CameraController.ScanProcessState.SECOND_SCAN) {
                        CameraController.getInstance().updateScanState(CameraController.ScanProcessState.SECOND_SCAN_COMPLETE);
                    }
                    Util.getQRCode(reportInfo, status, IrCameraActivity.this, "QRCode");
                    preview.stop();
                    clearQrCodePreview();
                    if (AppSettings.getSecondaryIdentifier() == CameraController.SecondaryIdentification.RFID.getValue()) {
                        resetQrCode();
                        initSecondaryScan();
                    } else {
                        setCameraPreview();
                    }
                    SoundController.getInstance().playValidQrSound();
                });
                return;
            }
            resetInvalidQrCode();
            SoundController.getInstance().playInvalidQrSound();
        } catch (Exception e) {
            Logger.error("QRCode Response processing error", e.getMessage());
            resetInvalidQrCode();
        }
    }

    private void getAppSettings() {
        isProDevice = Util.isDeviceProModel();
        AppSettings.getTextSettings(this);
        rfIdEnable = AppSettings.isRfidEnabled();
        qrCodeEnable = AppSettings.isQrCodeEnabled();
        institutionId = sharedPreferences.getString(GlobalParameters.INSTITUTION_ID, "");
        delayMilliTimeOut = sharedPreferences.getString(GlobalParameters.Timeout, "5");
        ledSettingEnabled = sharedPreferences.getBoolean(GlobalParameters.LedType, true);
        maskEnabled = sharedPreferences.getBoolean(GlobalParameters.MASK_DETECT, false);
        faceDetectEnabled = AppSettings.isFacialDetect();
        scanMode = sharedPreferences.getInt(GlobalParameters.ScanMode, Constants.DEFAULT_SCAN_MODE);
        isHomeViewEnabled = sharedPreferences.getBoolean(GlobalParameters.HOME_TEXT_IS_ENABLE, true) ||
                sharedPreferences.getBoolean(GlobalParameters.HOME_TEXT_ONLY_IS_ENABLE, false);
        isNavigationBarOn = sharedPreferences.getBoolean(GlobalParameters.NavigationBar, true);
        if (isProDevice) {
            CameraController.getInstance().setScanCloseProximityEnabled(true);
            Util.writeBoolean(sharedPreferences, GlobalParameters.ScanProximity, true);
        } else {
            CameraController.getInstance().setScanCloseProximityEnabled(sharedPreferences.getBoolean(GlobalParameters.ScanProximity, false));
        }
        getAccessControlSettings();
        getAudioVisualSettings();
    }

    /**
     * Method that fetches settings from the SharedPref
     */
    private void getAccessControlSettings() {
        AccessCardController.getInstance().setEnableRelay(Util.getSharedPreferences(this).getBoolean(GlobalParameters.EnableRelay, false));
        AccessCardController.getInstance().setAllowAnonymous(Util.getSharedPreferences(this).getBoolean(GlobalParameters.AllowAnonymous, false));
        AccessCardController.getInstance().setNormalRelayMode(Util.getSharedPreferences(this).getBoolean(GlobalParameters.RelayNormalMode, false));
        AccessCardController.getInstance().setReverseRelayMode(Util.getSharedPreferences(this).getBoolean(GlobalParameters.RelayNormalMode, false));
        AccessCardController.getInstance().setStopRelayOnHighTemp(Util.getSharedPreferences(this).getBoolean(GlobalParameters.StopRelayOnHighTemp, false));
        AccessCardController.getInstance().setEnableWeigan(Util.getSharedPreferences(this).getBoolean(GlobalParameters.EnableWeigand, false));
        AccessCardController.getInstance().setRelayTime(Util.getSharedPreferences(this).getInt(GlobalParameters.RelayTime, Constants.DEFAULT_RELAY_TIME));
        AccessCardController.getInstance().setWeiganControllerFormat(Util.getSharedPreferences(this).getInt(GlobalParameters.WeiganFormatMessage, Constants.DEFAULT_WEIGAN_CONTROLLER_FORMAT));
        AccessCardController.getInstance().setEnableWiegandPt(Util.getSharedPreferences(this).getBoolean(GlobalParameters.EnableWeigandPassThrough, false));
    }

    private void getAudioVisualSettings() {
        BLEController.getInstance().setHighTempLightEnabled(Util.getSharedPreferences(this).getBoolean(GlobalParameters.BLE_LIGHT_HIGH, false));
        BLEController.getInstance().setNormalTempLightEnabled(Util.getSharedPreferences(this).getBoolean(GlobalParameters.BLE_LIGHT_NORMAL, false));
    }

    /**
     * Method that initializes the access control & Nfc related members
     */
    private void initAccessControl() {
        AccessCardController.getInstance().init(this);
        AccessCardController.getInstance().setCallbackListener(this);
        /*if (!faceDetectEnabled) {
            isReadyToScan = false;
        }*/
        AccessCardController.getInstance().lockStandAloneDoor();  //by default lock the door when the Home page is displayed
        enableNfc();
    }

    private void initNfc() {
        mNfcAdapter = M1CardUtils.isNfcAble(this);
        mPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
    }

    private void enableNfc() {
        if (rfIdEnable) {
            if (mNfcAdapter != null && mNfcAdapter.isEnabled()
                    && isActivityResumed) {
                isNfcFDispatchEnabled = true;
                mNfcAdapter.enableForegroundDispatch(this, mPendingIntent, null, null);
            }
        }
    }

    private void disableNfc() {
        if (mNfcAdapter != null && mNfcAdapter.isEnabled() &&
                isNfcFDispatchEnabled) {
            mNfcAdapter.disableForegroundDispatch(this);
            isNfcFDispatchEnabled = false;
        }
    }

    private void enableHidReader() {
        if (rfIdEnable) {
            if (mNfcAdapter != null && !mNfcAdapter.isEnabled()) {
                startHidService();
            }
        }
    }

    private void setCameraPreview() {
        if (!AppSettings.isTemperatureScanEnabled() && !AppSettings.isFacialDetect() &&
                !AppSettings.isPrintLabelFace()) {
            onTemperatureScanDisabled();
            return;
        }
        long delay = 1000;
        qr_main.setVisibility(View.GONE);
        if (AppSettings.getSecondaryIdentifier() == CameraController.SecondaryIdentification.QRCODE_OR_RFID.getValue() ||
                (AppSettings.getSecondaryIdentifier() == CameraController.SecondaryIdentification.QR_CODE.getValue()) ||
                (AppSettings.getSecondaryIdentifier() == CameraController.SecondaryIdentification.FACE.getValue())) {
            resetCameraView();
            if (rfIdEnable) {
                delay = 2 * 1000; //With both Qr and Rfid enabled, taking time to init Camera on Non-HID devices
            }
        } else {
            resetCameraView();
        }
        CameraController.getInstance().setCameraOnRfid(true);
        enableLedPower();
        isReadyToScan = true;
        new Handler().postDelayed(() -> {
            if (!Util.isDeviceF10()) {
                if (outerCircle != null)
                    outerCircle.setBackgroundResource(R.drawable.border_shape);
                /*if (logo != null) {
                    logo.setVisibility(View.GONE);
                }*/
                if (relative_main != null) {
                    relative_main.setVisibility(View.GONE);
                }
            }
            time_attendance_layout.setVisibility(View.GONE);
            changeVerifyBackground(R.color.colorTransparency, true);
            disableNfc();
        }, delay);
        setCameraPreviewTimer();
    }

    private void setCameraPreviewTimer() {
        if (imageTimer != null) {
            imageTimer.cancel();
            imageTimer = null;
        }
        imageTimer = new Timer();
        imageTimer.schedule(new TimerTask() {
            public void run() {
                disableLedPower();
                runOnUiThread(() -> resetToHomePage());
                this.cancel();
            }
        }, Long.parseLong(delayMilliTimeOut) * 1000); //wait 10 seconds for the temperature to be captured, go to home otherwise

    }

    private void setCameraPreviewTimer(int timeInSeconds) {
        if (imageTimer != null) {
            imageTimer.cancel();
            imageTimer = null;
        }
        imageTimer = new Timer();
        imageTimer.schedule(new TimerTask() {
            public void run() {
                disableLedPower();
                runOnUiThread(() -> resetToHomePage());
                this.cancel();
            }
        }, timeInSeconds * 1000); //wait in seconds for the temperature to be captured, go to home otherwise
    }

    private void clearQrCodePreview() {
        try {
            if (graphicOverlay != null) {
                graphicOverlay.clear();
            }
            if (preview != null) {
                preview.stop();
                preview.release();
            }
            if (cameraSource != null) {
                cameraSource.stop();
                cameraSource.release();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void processImageAndGetMaskStatus(Bitmap maskDetectBitmap) {
        Observable
                .create((ObservableOnSubscribe<Integer>) emitter -> {
                    Bitmap bitmap = ArcSoftImageUtil.getAlignedBitmap(maskDetectBitmap, true);
                    if (bitmap == null) {
                        Logger.debug(TAG, "Bitmap is null");
                        emitter.onNext(-2);
                        return;
                    }
                    int width = bitmap.getWidth();
                    int height = bitmap.getHeight();
                    byte[] bgr24 = ArcSoftImageUtil.createImageData(bitmap.getWidth(), bitmap.getHeight(), ArcSoftImageFormat.BGR24);

                    int transformCode = ArcSoftImageUtil.bitmapToImageData(bitmap, bgr24, ArcSoftImageFormat.BGR24);
                    if (transformCode != ArcSoftImageUtilError.CODE_SUCCESS) {
                        Log.d(TAG, "Mask Value --- transform failed, code is " + transformCode);
                        emitter.onNext(-2);
                        return;
                    }
                    List<FaceInfo> faceInfoList = new ArrayList<>();
                    int result = faceEngineHelper.getFrEngine().detectFaces(bgr24, width, height, FaceEngine.CP_PAF_BGR24, DetectModel.RGB, faceInfoList);
                    Log.d(TAG, "Mask Result = " + result);

                    if (result != ErrorInfo.MOK) {
                        emitter.onNext(-2);
                        return;
                    }

                    int faceProcessCode = faceEngineHelper.getFrEngine().process(bgr24, width, height, FaceEngine.CP_PAF_BGR24, faceInfoList, processMask);
                    // Need to work on condition
                    if (faceProcessCode == ErrorInfo.MOK) {
                        Log.d(TAG, "Mask Value --- faceProcessCode is success, code is " + faceProcessCode);

                        List<MaskInfo> maskInfoList = new ArrayList<>();
                        faceEngineHelper.getFrEngine().getMask(maskInfoList);
                        if (maskInfoList.size() > 0) {
                            emitter.onNext(maskInfoList.get(0).getMask());
                        }
                    }
                })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() {
                    Disposable maskDisposable;

                    @Override
                    public void onSubscribe(Disposable d) {
                        maskDisposable = d;
                        maskDetectDisposable.add(d);
                    }

                    @Override
                    public void onNext(Integer maskStat) {
                        Log.d(TAG, "Call Mask Status " + maskStat);
                        maskStatus = maskStat;
                        //maskDisposable.dispose();
                        maskDetectDisposable.remove(maskDisposable);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "Error in getting the mask status");
                        maskDetectDisposable.remove(maskDisposable);
                    }

                    @Override
                    public void onComplete() {
                        //do noop
                    }
                });
    }

    private void showMaskStatus() {
        if (!maskEnabled) return;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (maskStatus) {
                    case 0: {
                        mask_message.setTextColor(getResources().getColor(R.color.colorRed));
                        mask_message.setText(getString(R.string.no_mask));
                        mask_message.setVisibility(View.VISIBLE);
                        mask_message.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                    }
                    break;
                    case 1: {
                        mask_message.setTextColor(getResources().getColor(R.color.green));
                        mask_message.setText(getString(R.string.mask_detected));
                        mask_message.setVisibility(View.VISIBLE);
                        mask_message.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                    }
                    break;
                    case -1: {
                        mask_message.setTextColor(getResources().getColor(R.color.colorDarkOrange));
                        mask_message.setText(getString(R.string.mask_not_detected));
                        mask_message.setVisibility(View.VISIBLE);
                        mask_message.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                    }
                    break;
                }
            }
        });
    }

    String faceSimilarScore;
    private static DecimalFormat df = new DecimalFormat("0.00");

    private void searchFace(final FaceFeature frFace, final Integer requestId, final Bitmap rgb, final Bitmap ir) {
        Log.d(TAG, String.format("Snap searchFace requestId: %s", requestId));
        if (!isScanWithMaskEnforced()) return;
        if (((CameraController.getInstance().getScanProcessState() == CameraController.ScanProcessState.IDLE) &&
                !GestureController.getInstance().isGestureEnabledAndDeviceConnected()) ||
                ((AppSettings.getSecondaryIdentifier() == CameraController.SecondaryIdentification.NONE.getValue()) &&
                        !AppSettings.isPrimaryIdentifierFace())) {
            runTemperature(requestId, new UserExportedData(rgb, ir, new RegisteredMembers(), (int) 0));
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
                        searchFaceDisposable.add(d);
                    }

                    @Override
                    public void onNext(final CompareResult compareResult) {
                        Log.v(TAG, String.format("searchFace requestId: %s, compareResult : %s", requestId, compareResult));
                        if (compareResult == null || compareResult.getUserName() == null) {
                            requestFeatureStatusMap.put(requestId, RequestFeatureStatus.FAILED);
                            //faceHelperIr.setName(requestId, getString(R.string.VISITOR) + requestId);
                            return;
                        }
                        if (BuildConfig.DEBUG) {
                            long timestamp = System.currentTimeMillis();
                            if (ir != null && rgb != null) {
                                try {
                                    ir.compress(Bitmap.CompressFormat.JPEG, 85, new FileOutputStream(Environment.getExternalStorageDirectory() + "/" + timestamp + compareResult.getMemberId() + "-ir.jpg"));
                                    rgb.compress(Bitmap.CompressFormat.JPEG, 85, new FileOutputStream(Environment.getExternalStorageDirectory() + "/" + timestamp + compareResult.getMemberId() + "-rgb.jpg"));
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        float similarValue = compareResult.getSimilar() * 100;
                        String formattedSimilarityScore = df.format(compareResult.getSimilar() * 100);

                        String thresholdFacialPreference = sharedPreferences.getString(GlobalParameters.FACIAL_THRESHOLD, String.valueOf(Constants.FACIAL_DETECT_THRESHOLD));
                        int thresholdvalue = Integer.parseInt(thresholdFacialPreference);

                        if (similarValue > thresholdvalue) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    tvErrorMessage.setVisibility(View.GONE);
                                    tvFaceMessage.setVisibility(View.GONE);
                                }
                            });
                            Log.d(TAG, "Snap Compare result Match Similarity value " + similarValue);
                            boolean isAdded = false;
                            if (compareResultList == null) {
                                requestFeatureStatusMap.put(requestId, RequestFeatureStatus.FAILED);
                                //faceHelperIr.setName(requestId, getString(R.string.VISITOR) + requestId);
                                return;
                            }
                            for (CompareResult compareResult1 : compareResultList) {
                                if (compareResult1.getTrackId() == requestId) {
                                    isAdded = true;
                                    break;
                                }
                            }
                            Log.e("onnext2---", "searchface--- isAdd:" + isAdded);
                            if (!isAdded) {
                                Log.d(TAG, "Snap Compare result isAdded, Add it " + isAdded);

                                if (compareResultList.size() >= MAX_DETECT_NUM) {
                                    compareResultList.remove(0);
                                    //adapter.notifyItemRemoved(0);
                                }

                                String[] split = compareResult.getUserName().split("-");
                                String id = "";
                                if (split != null && split.length > 1) id = split[split.length - 1];

                                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                Date curDate = new Date(System.currentTimeMillis());
                                String verify_time = formatter.format(curDate);
                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
                                String cpmpareTime = simpleDateFormat.format(curDate);

                                //registeredMemberslist = LitePal.where("memberid = ?", split[1]).find(RegisteredMembers.class);
                                registeredMemberslist = DatabaseController.getInstance().findMember(Long.parseLong(split[split.length - 1]));

                                if (registeredMemberslist.size() > 0) {
                                    Log.d(TAG, "Snap Matched Database, Run temperature");
                                    RegisteredMembers registeredMembers = registeredMemberslist.get(0);
                                    if (validateCheckInCheckOut(registeredMembers)) {
                                        searchFaceDisposable.remove(searchMemberDisposable);
                                        return;
                                    }
                                    if (CameraController.getInstance().getTriggerType().equals(CameraController.triggerValue.CAMERA.toString())) {
                                        CameraController.getInstance().updateTriggerType(CameraController.triggerValue.FACE.toString());
                                    }

                                    CameraController.getInstance().updateScanProcessState(registeredMembers);

                                    if (checkSecondaryIdentifier()) {
                                        UserExportedData data = new UserExportedData(rgb, ir, registeredMemberslist.get(0), (int) similarValue);
                                        data.compareResult = compareResult;
                                        data.faceScore = (int) similarValue;
                                        CameraController.getInstance().setData(data);
                                        CameraController.getInstance().setRequestId(requestId);
                                        showResult(compareResult, requestId, registeredMembers.firstname, registeredMembers.memberid, formattedSimilarityScore, false);
                                        CameraController.getInstance().setCompareResult(compareResult);
                                        initSecondaryScan();
                                        return;
                                    }
                                    //CameraController.getInstance().updateScanState(CameraController.ScanProcessState.SECOND_SCAN);
                                    //showCameraPreview(frFace, requestId, rgbBitmap, irBitmap);

                                    CameraController.getInstance().setFaceVisible(true);

                                    UserExportedData data = new UserExportedData(rgb, ir, registeredMemberslist.get(0), (int) similarValue);
                                    data.compareResult = compareResult;
                                    data.faceScore = (int) similarValue;
                                    CameraController.getInstance().setCompareResult(compareResult);

                                    String status = registeredMembers.getStatus();
                                    String name = registeredMembers.getFirstname();
                                    String memberId = registeredMembers.getMemberid();
                                    String image = registeredMembers.getImage();
                                    AccessCardController.getInstance().setAccessIdDb(registeredMembers.getAccessid());
                                    if ((!TextUtils.isEmpty(GlobalParameters.Access_limit) && compareAllLimitedTime(cpmpareTime, processLimitedTime(GlobalParameters.Access_limit)))
                                            || TextUtils.isEmpty(GlobalParameters.Access_limit)) {
                                        Log.d(TAG, "Snap Matched Database match Status 1 member id is " + memberId);
                                        memberId = getString(R.string.id) + memberId;
                                        showResult(compareResult, requestId, name, memberId, formattedSimilarityScore, false);
                                    }
                                    runTemperature(requestId, data);   //TODO1: Optimize
                                } else {
                                    //showCameraPreview(frFace, requestId, rgbBitmap, irBitmap);
                                    displayCameraPreview(frFace, requestId, rgbBitmap, irBitmap);
                                    Log.e(TAG, "Snap Compare result database no match " + isAdded);
                                    if (AppSettings.getAccessControlScanMode() == AccessCardController.AccessControlScanMode.FACE_ONLY.getValue()) {
                                        AccessCardController.getInstance().setAccessFaceNotMatch(true);
                                    }
                                    if (Util.isOfflineMode(IrCameraActivity.this)) {
                                        runTemperature(requestId, new UserExportedData(rgb, ir, new RegisteredMembers(), (int) 0));
                                    }
                                }
                            } else {
                                Log.d(TAG, "Snap Compare result, isAdded condition failed " + isAdded);
                            }
                            requestFeatureStatusMap.put(requestId, RequestFeatureStatus.SUCCEED);
                            //faceHelperIr.setName(requestId, getString(R.string.recognize_success_notice, compareResult.getUserName()));
                        } else {
                            Log.d(TAG, "Snap Compare result Match not meeting threshold " + similarValue);
                            displayCameraPreview(frFace, requestId, rgbBitmap, irBitmap);

                            if (similarValue < Constants.FACE_MIN_THRESHOLD_RETRY) {
                                runTemperature(requestId, new UserExportedData(rgb, ir, new RegisteredMembers(), (int) similarValue));
                                return;
                            }
                            if (mFaceMatchRetry == Constants.FACE_MATCH_MAX_RETRY) {
                                CameraController.getInstance().setFaceNotMatchedOnRetry(true);
                                runTemperature(requestId, new UserExportedData(rgb, ir, new RegisteredMembers(), (int) similarValue));
                                mFaceMatchRetry = 0;
                                return;
                            }
                            mFaceMatchRetry++;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showGuideMessage(getString(R.string.analyzing_face));
                                    tvFaceMessage.setVisibility(View.GONE);
                                }
                            });
                            //faceHelperIr.setName(requestId, getString(R.string.recognize_failed_notice, "NOT_REGISTERED"));
                            retryRecognizeDelayed(requestId);
                        }
                        //searchMemberDisposable.dispose();
                        searchFaceDisposable.remove(searchMemberDisposable);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "Snap Compare result Error ");
                        runTemperature(requestId, new UserExportedData(rgb, ir, new RegisteredMembers(), (int) 0)); // Register member photo is not there, Still find temperature
                        /*if (faceHelperIr != null) {
                            faceHelperIr.setName(requestId, getString(R.string.recognize_failed_notice, "NOT_REGISTERED"));
                        }*/
                        //retryRecognizeDelayed(requestId);
                        searchFaceDisposable.remove(searchMemberDisposable);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void resetMaskStatus() {
        maskDetectBitmap = null;
        maskStatus = -2;
    }

    private boolean isFindTemperature() {
        return !faceDetectEnabled;
    }

    public void onRfidScan(String cardId) {
        Log.v(TAG, "onRfidScan cardId: " + cardId);
        if (cardId.isEmpty() || isTopFragmentGesture())
            return;
        if ((AppSettings.getTimeAndAttendance() == 1) && (time_attendance_layout.getVisibility() == View.VISIBLE)) {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.please_click_check_in_out), Toast.LENGTH_LONG).show();
            return;
        }

        if (isTopFragmentSecondaryScreen()) {
            closeFragment(secondaryScreenFragment);
        }
        isReadyToScan = false;
        CameraController.getInstance().updateTriggerType(CameraController.triggerValue.ACCESSID.toString());
        AccessCardController accessCardController = AccessCardController.getInstance();
        accessCardController.setAccessCardId(cardId);
        if (AccessControlModel.getInstance().isMemberMatch(cardId)) {
            RegisteredMembers matchedMember = AccessControlModel.getInstance().getRfidScanMatchedMember();
            if (accessCardController.isAccessTimeExpired(matchedMember)) {
                onRfidNoMemberMatch(cardId);
                return;
            }
            if (validateCheckInCheckOut(matchedMember)) {
                return;
            }

//                if ((AppSettings.getTimeAndAttendance() == 1) && Util.isAlreadyChecked(matchedMember.getDateTimeCheckInOut())) {
//                    if (ApplicationController.getInstance().getTimeAttendance() == 1)
//                        Toast.makeText(this, getResources().getString(R.string.already_check_in), Toast.LENGTH_LONG).show();
//                    if (ApplicationController.getInstance().getTimeAttendance() == 2)
//                        Toast.makeText(this, getResources().getString(R.string.already_check_out), Toast.LENGTH_LONG).show();
//                    if (isHomeViewEnabled) {
//                        pauseCameraScan();
//                    } else {
//                        isReadyToScan = false;
//                    }
//                    resetHomeScreen();
//                    return;
//                }

            registeredMemberslist = new ArrayList<>();
            registeredMemberslist.add(matchedMember);
            //launch the fragment
            if (AppSettings.isAcknowledgementScreen()) {
                if (accessCardController.getTapCount() == 0) {
                    CameraController.getInstance().updateScanProcessState(matchedMember);
                    accessCardController.setTapCount(1);
                    launchAcknowledgementFragment();
                    setCameraPreviewTimer(15);
                    return;
                }
            } else {
                CameraController.getInstance().updateScanProcessState(matchedMember);
                if (checkSecondaryIdentifier()) {
                    disableNfc();
                    initSecondaryScan();
                    return;
                }
            }
            if (AppSettings.isAcknowledgementScreen()) {
                if (checkSecondaryIdentifier()) {
                    initSecondaryScan();
                    new Handler().postDelayed(() -> {
                        closeFragment(acknowledgementFragment);
                        accessCardController.setTapCount(0);
                    }, 1000);
                    return;
                }
                new Handler().postDelayed(() -> {
                    closeFragment(acknowledgementFragment);
                    accessCardController.setTapCount(0);
                }, 2000);
            }
            if ((CameraController.getInstance().getScanProcessState()
                    == CameraController.ScanProcessState.FIRST_SCAN) ||
                    (CameraController.getInstance().getScanProcessState()
                            == CameraController.ScanProcessState.FIRST_SCAN_COMPLETE)) {
                CameraController.getInstance().setScanProcessState(CameraController.ScanProcessState.SECOND_SCAN);
            }
            if (AppSettings.isTemperatureScanEnabled()) {
                enableLedPower();
                showSnackBarMessage(getString(R.string.access_granted));
                setCameraPreview();
            } else {
                UserExportedData userExportedData = new UserExportedData(rgbBitmap, irBitmap, registeredMemberslist.get(0), 0);
                CameraController.getInstance().setUserExportedData(userExportedData);
                onTemperatureScanDisabled();
            }
            return;
        } else if (accessCardController.isDoMemberMatch()) onRfidOnlyEnabled(cardId);
        else onRfidNoMemberMatch(cardId);
    }

    private void showSnackBarMessage(String message) {
        if (mToastbar != null) {
            mToastbar.cancel();
        }
        mToastbar = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG);
        mToastbar.show();
    }

    private void dismissSnackBar() {
        if (mToastbar != null) {
            mToastbar.cancel();
            mToastbar = null;
        }

        if (toastmSnackbar != null) {
            toastmSnackbar.cancel();
            toastmSnackbar = null;
        }
    }

    private void initiateFaceSearch(FaceFeature faceFeature, int requestId, Integer liveness, Bitmap rgb, Bitmap ir) {
        Log.v(TAG, String.format("initiateFaceSearch faceDetectEnabled: %s", faceDetectEnabled));
        if (faceDetectEnabled || Util.isOfflineMode(IrCameraActivity.this)) {
            if (AppSettings.isLivenessDetect()) {
                if (liveness != null && liveness == LivenessInfo.ALIVE) {
                    Log.d(TAG, "Liveness Face is Alive initiate Search");
                    startFaceSearch(faceFeature, requestId, rgb, ir);
                }
                return;
            }
            startFaceSearch(faceFeature, requestId, rgb, ir);
        }
    }

    public void HomeTextOnlyText() {
        try {
            if (sharedPreferences.getBoolean(GlobalParameters.HOME_TEXT_ONLY_IS_ENABLE, false)) {
                //logo.setVisibility(View.GONE);
                tv_thermal.setVisibility(View.GONE);
                tv_thermal_subtitle.setVisibility(View.GONE);
                img_logo.setVisibility(View.GONE);
                tvOnlyText.setVisibility(View.VISIBLE);
                tvDisplayTimeOnly.setVisibility(View.VISIBLE);
                tvVersionOnly.setVisibility(View.VISIBLE);
                tv_display_time.setVisibility(View.GONE);
                tvVersionIr.setVisibility(View.GONE);
            } else if (sharedPreferences.getBoolean(GlobalParameters.HOME_TEXT_IS_ENABLE, true)) {
                //logo.setVisibility(View.VISIBLE);
                String text = tv_thermal.getText().toString();
                if (qrCodeEnable && AppSettings.isEnableHandGesture()) {
                    tv_thermal.setTextSize(22);
                    setLayoutMargins();
                }
                if ((text.length() > 100)) {
                    tv_thermal.setTextSize(22);
                }
                tv_thermal.setVisibility(View.VISIBLE);
                tv_thermal_subtitle.setVisibility(View.VISIBLE);
                tvOnlyText.setVisibility(View.GONE);
                img_logo.setVisibility(View.VISIBLE);
                tvDisplayTimeOnly.setVisibility(View.GONE);
                tvVersionOnly.setVisibility(View.GONE);
                tvVersionIr.setVisibility(View.VISIBLE);
                tv_display_time.setVisibility(View.VISIBLE);
            } else {
                new Handler().postDelayed(() -> {
                    //logo.setVisibility(View.GONE);
                    relative_main.setVisibility(View.GONE);
                    changeVerifyBackground(R.color.colorTransparency, true);
                }, 150);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void homeDisplayView() {
        time_attendance_layout.setVisibility(View.GONE);
        if (!sharedPreferences.getBoolean(GlobalParameters.HOME_TEXT_IS_ENABLE, true) && !sharedPreferences.getBoolean(GlobalParameters.HOME_TEXT_ONLY_IS_ENABLE, false)) {
            relative_main.setVisibility(View.GONE);
            // rl_header.setVisibility(View.GONE);
        } else {
            temperature_image.setVisibility(View.GONE);
            mask_message.setVisibility(View.GONE);
            relative_main.setVisibility(View.VISIBLE);
            HomeTextOnlyText();
            rl_header.setVisibility(View.VISIBLE);
        }
        if (isProDevice) {
            long scannerRemainingTime = CameraController.getInstance().getScannerRemainingTime();
            if (scannerRemainingTime > 0) {
                new Handler().postDelayed(() -> Toast.makeText(IrCameraActivity.this, String.format(getString(R.string.scanner_remaining_time_msg), scannerRemainingTime), Toast.LENGTH_SHORT).show(), 100);
            }
        }

        /*if (AppSettings.isScanOnQrEnabled()) {
            frameLayout.setVisibility(View.GONE);
            qr_main.setVisibility(View.GONE);
        }*/
    }

    /**
     * TODO1: Optimize with process image
     *
     * @param faceEngine faceEngine
     * @param rgbBitmap  bitmap
     * @param requestId  request id
     */
    public void detectAlignedFaces(FaceEngine faceEngine, Bitmap rgbBitmap, int requestId) {
        Observable
                .create((ObservableOnSubscribe<List<FaceInfo>>) emitter -> {
                    Bitmap mAlignedBitmap = ArcSoftImageUtil.getAlignedBitmap(rgbBitmap, true);
                    if (mAlignedBitmap == null) {
                        Logger.debug(TAG, "Face Bitmap is null");
                        emitter.onNext(searchFaceInfoList);
                        return;
                    }
                    byte[] mBgr24 = ArcSoftImageUtil.createImageData(mAlignedBitmap.getWidth(), mAlignedBitmap.getHeight(), ArcSoftImageFormat.BGR24);
                    int transformCode = ArcSoftImageUtil.bitmapToImageData(mAlignedBitmap, mBgr24, ArcSoftImageFormat.BGR24);
                    if (transformCode != ArcSoftImageUtilError.CODE_SUCCESS) {
                        emitter.onNext(searchFaceInfoList);
                        return;
                    }
                    int detectFacesResult = faceEngine.detectFaces(mBgr24, mAlignedBitmap.getWidth(), mAlignedBitmap.getHeight(), FaceEngine.CP_PAF_BGR24, DetectModel.RGB, searchFaceInfoList);
                    if (detectFacesResult != ErrorInfo.MOK) {
                        emitter.onNext(searchFaceInfoList);
                        return;
                    }

                    int faceProcessCode = faceEngineHelper.getFrEngine().process(mBgr24, mAlignedBitmap.getWidth(), mAlignedBitmap.getHeight(), FaceEngine.CP_PAF_BGR24, searchFaceInfoList, processMask);
                    // Need to work on condition
                    if (faceProcessCode == ErrorInfo.MOK) {
                        FaceParameters faceParameters = CameraController.getInstance().getFaceParameters();

                        if (faceParameters != null) {
                            List<MaskInfo> maskInfoList = new ArrayList<>();
                            faceEngineHelper.getFrEngine().getMask(maskInfoList);
                            if (maskInfoList.size() > 0) {
                                faceParameters.maskStatus = maskInfoList.get(0).getMask();
                                maskStatus = faceParameters.maskStatus;
                            }

                            List<FaceShelterInfo> shelterInfoList = new ArrayList<>();
                            faceEngineHelper.getFrEngine().getFaceShelter(shelterInfoList);
                            if (shelterInfoList.size() > 0) {
                                faceParameters.faceShelter = faceParameters.getFaceShelter(shelterInfoList.get(0));
                            }

                            List<Face3DAngle> face3DAngles = new ArrayList<>();
                            faceEngineHelper.getFrEngine().getFace3DAngle(face3DAngles);
                            if (face3DAngles.size() > 0) {
                                faceParameters.face3DAngle = faceParameters.getFace3DAngle(face3DAngles.get(0));
                                face3DAngle = face3DAngles.get(0);
                            }

                            List<AgeInfo> ageInfos = new ArrayList<>();
                            faceEngineHelper.getFrEngine().getAge(ageInfos);
                            if (ageInfos.size() > 0) {
                                faceParameters.age = ageInfos.get(0).getAge();
                            }

                            List<GenderInfo> genderInfos = new ArrayList<>();
                            faceEngineHelper.getFrEngine().getGender(genderInfos);
                            if (genderInfos.size() > 0) {
                                faceParameters.gender = faceParameters.getGender(genderInfos.get(0));
                            }

                            List<LivenessInfo> livenessInfos = new ArrayList<>();
                            faceEngineHelper.getFrEngine().getLiveness(livenessInfos);
                            if (livenessInfos.size() > 0) {
                                faceParameters.liveness = faceParameters.getFaceLiveness(livenessInfos.get(0));
                            }
                        }
                    }
                    emitter.onNext(searchFaceInfoList);
                })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<FaceInfo>>() {
                    Disposable faceDisposable;

                    @Override
                    public void onSubscribe(Disposable d) {
                        faceDisposable = d;
                        alignedFacesDisposable.add(d);
                    }

                    @Override
                    public void onNext(List<FaceInfo> resultList) {
                        Log.d(TAG, "SearchFaceInfoList = " + resultList.size());
                        searchFaceInfoList.addAll(resultList);
                        //faceDisposable.dispose();
                        alignedFacesDisposable.remove(faceDisposable);
                        checkFaceCloseness(searchFaceInfoList, requestId);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "Error in getting the face properties");
                        alignedFacesDisposable.remove(faceDisposable);
                    }

                    @Override
                    public void onComplete() {
                        //do noop
                    }
                });
    }

    private void checkFaceClosenessAndSearch(FaceFeature faceFeature, int requestId, Bitmap rgb, Bitmap ir) {
        if (!AppSettings.isTemperatureScanEnabled() && !Util.isDeviceF10()) {
            runOnUiThread(() -> {
                if (tvErrorMessage != null) {
                    showGuideMessage(getString(R.string.face_center));
                }
            });
        }
        if (searchFaceInfoList.isEmpty()) {
            setPreviewIdleTimer();
            detectAlignedFaces(faceEngineHelper.getFrEngine(), rgb, requestId);
        } else {
            runOnUiThread(() -> {
                if (tvErrorMessage != null) {
                    tvErrorMessage.setVisibility(View.GONE);
                }
                tvFaceMessage.setVisibility(View.GONE);
            });
            if (faceDetectEnabled || Util.isOfflineMode(IrCameraActivity.this)) {
                if (CameraController.getInstance().isScanCloseProximityEnabled() &&
                        !isFaceIdentified) {
                    Log.d(TAG, "FaceRecognition");
                    showCameraPreview(faceFeature, requestId, rgb, ir);
                } else if (Util.isOfflineMode(IrCameraActivity.this)) {
                    showCameraPreview(faceFeature, requestId, rgb, ir);
                }
                if (CameraController.getInstance().getScanProcessState()
                        == CameraController.ScanProcessState.SECOND_SCAN_COMPLETE) {
                    UserExportedData data = CameraController.getInstance().getData();
                    int faceScore = 0;
                    if (data != null) {
                        faceScore = data.faceScore;
                    }
                    if (registeredMemberslist != null) {
                        runTemperature(requestId, new UserExportedData(rgb, ir, registeredMemberslist.get(0), faceScore));
                    } else {
                        runTemperature(requestId, new UserExportedData(rgb, ir, new RegisteredMembers(), faceScore));
                    }
                    return;
                }
                searchFace(faceFeature, requestId, rgb, ir);
                return;
            }
            showCameraPreview(faceFeature, requestId, rgb, ir);
            runTemperature(requestId, new UserExportedData(rgb, ir, new RegisteredMembers(), 0));
        }
    }

    private void checkFaceCloseness(List<FaceInfo> searchFaceList, int requestId) {
        if (searchFaceList.size() > 0 && !isFaceClose(searchFaceList.get(0))) {
            if (isProDevice) {
                showGuideMessage(sharedPreferences.getString(GlobalParameters.GUIDE_TEXT4, getString(R.string.step_closer)));
            } else {
                runOnUiThread(() -> {
                    if (Util.isDeviceF10()) {
                        tvVersionIr.setVisibility(View.GONE);
                        tvVersionOnly.setVisibility(View.GONE);
                    }
                    tvFaceMessage.setVisibility(View.VISIBLE);
                    tvFaceMessage.setText(sharedPreferences.getString(GlobalParameters.GUIDE_TEXT4, getString(R.string.step_closer)));
                });
            }
            searchFaceInfoList.clear();
            face3DAngle = null;
            requestFeatureStatusMap.put(requestId, RequestFeatureStatus.TO_RETRY);
            return;
        }
        if (isProDevice) {
            if (searchFaceList.size() > 0 && !isFaceAngleCentered(face3DAngle)) {
                runOnUiThread(() -> {
                    if (isProDevice) {
                        showGuideMessage(getString(R.string.face_center));
                    } else {
                        tvFaceMessage.setVisibility(View.VISIBLE);
                        tvFaceMessage.setText(getString(R.string.face_center));
                    }
                });
                searchFaceInfoList.clear();
                face3DAngle = null;
                requestFeatureStatusMap.put(requestId, RequestFeatureStatus.TO_RETRY);
                return;
            }
        }
        Log.d(TAG, "Face is close, Initiate search");
        requestFeatureStatusMap.put(requestId, RequestFeatureStatus.TO_RETRY);
    }

    public boolean isFaceClose(FaceInfo faceInfo) {
        boolean result = false;
        if (faceInfo != null) {
            Rect rect = faceInfo.getRect();
            Log.d(TAG, "SnapXT Face Rect values" + "(" + rect.width() + " " + rect.height() + " )");
            Log.d(TAG, "SnapXT Face Orient" + faceInfo.getOrient());
            if (rect.width() > 45) {
                result = true;
                Log.d(TAG, "SnapXT Face is close");
            } else {
                Log.d(TAG, "SnapXT Face is not close");
            }
        }
        return result;
    }

    /**
     * Method that checks for the Face angle, returns true if face is straight (90 degrees)
     *
     * @param face3DAngle face3D info
     * @return true or false accordingly
     */
    private boolean isFaceAngleCentered(Face3DAngle face3DAngle) {
        boolean result = false;
        //In Pro-devices with mask on, face angle is not meeting the below criteria, so skip the face angle check
        //For quick scan, skip the face angle check
        if ((isProDevice && maskStatus == 1) || (AppSettings.getScanType() == 1)) return true;

        if (face3DAngle != null) {
            float yaw = face3DAngle.getYaw();
            //With mask, the yaw value is ranging from 30 - 50 with the face straight up
            if ((yaw > -10 && yaw < 10) ||
                    ((maskStatus == 1) && (yaw > -10 && yaw < 50))) {
                result = true;
            }
        }
        return result;
    }

    private void showCameraPreview(FaceFeature faceFeature, int requestId, Bitmap rgbBitmap, Bitmap irBitmap) {
        if ((!AppSettings.isTemperatureScanEnabled() && !AppSettings.isFacialDetect()) ||
                (GestureController.getInstance().isGestureWithMaskEnforceEnabled())) {
            return;
        }
        if ((CameraController.getInstance().getScanProcessState()
                != CameraController.ScanProcessState.SECOND_SCAN) &&
                (CameraController.getInstance().getScanProcessState() != CameraController.ScanProcessState.IDLE) &&
                (AppSettings.getSecondaryIdentifier() != CameraController.SecondaryIdentification.NONE.getValue())) {
            return;
        }
        displayCameraPreview(faceFeature, requestId, rgbBitmap, irBitmap);
    }

    public void displayCameraPreview(FaceFeature faceFeature, int requestId, Bitmap rgbBitmap, Bitmap irBitmap) {
        if (!Util.isDeviceF10()) {
            checkDeviceMode();
        }
        cancelPreviewIdleTimer();
        enableLedPower();
        if (maskEnabled && !faceDetectEnabled) {
            if (maskDetectBitmap == null && rgbBitmap != null) {
                maskDetectBitmap = rgbBitmap.copy(rgbBitmap.getConfig(), false);
                processImageAndGetMaskStatus(maskDetectBitmap);
            }
        }

        runOnUiThread(() -> {
            if (rl_header == null) return;
            CameraController.getInstance().setScanState(CameraController.ScanState.FACIAL_SCAN);

            if (!Util.isDeviceF10()) {
                changeVerifyBackground(R.color.colorTransparency, true);
                relative_main.setVisibility(View.GONE);
            }
            // rl_header.setVisibility(View.GONE);
            //logo.setVisibility(View.GONE);
            //setCameraPreviewTimer();
            if (AppSettings.isRetryScanEnabled()) {
                if (!isFaceIdentified) {
                    initPreviewTimer();
                }
            }
        });
    }

    private void startFaceSearch(FaceFeature faceFeature, int requestId, Bitmap rgbBitmap, Bitmap irBitmap) {
        if (CameraController.getInstance().isScanCloseProximityEnabled()
                && !AppSettings.isLivenessDetect()) {
            //searchFace(faceFeature, requestId, rgbBitmap, irBitmap);
            return;
        }
        checkFaceClosenessAndSearch(faceFeature, requestId, rgbBitmap, irBitmap);
    }

    private void clearData() {
        CameraController.getInstance().clearData();
        AccessCardController.getInstance().clearData();
        searchFaceInfoList.clear();
        compareResultList.clear();
        resetMaskStatus();
        mFaceMatchRetry = 0;
        temperature = 0f;
        CameraController.getInstance().setTriggerType(CameraController.triggerValue.CAMERA.toString());
        clearLeftFace(null);
        TemperatureController.getInstance().setTemperatureRetry(0);
        TemperatureController.getInstance().clearData();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        face3DAngle = null;
        faceThermalToast = null;
        isRecordNotSent = false;
        temperatureBitmap = null;
        qrCodeReceived = false;
        registeredMemberslist = null;

        if (isDisconnected) {
            runOnUiThread(() -> Toast.makeText(getBaseContext(), getString(R.string.connect_light_device), Toast.LENGTH_SHORT).show());
            BLEController.getInstance().connectToDevice();
        }
        GestureController.getInstance().clearData();
        PrinterController.getInstance().setPrinting(false);
        //GestureController.getInstance().setLanguageUpdated(false);
        QrCodeController.getInstance().clearData();
        //   ApplicationController.getInstance().setTimeAttendance(0);
    }

    private void setPreviewIdleTimer() {
        cancelPreviewIdleTimer();
        previewIdleTimer = new Timer();
        previewIdleTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    if (tvErrorMessage != null) {
                        tvErrorMessage.setVisibility(View.GONE);
                    }
                    tvFaceMessage.setVisibility(View.GONE);
                    if (Util.isDeviceF10()) {
                        if (sharedPreferences.getBoolean(GlobalParameters.HOME_TEXT_IS_ENABLE, true)) {
                            tvVersionIr.setVisibility(View.VISIBLE);
                        } else if (sharedPreferences.getBoolean(GlobalParameters.HOME_TEXT_ONLY_IS_ENABLE, true)) {
                            tvVersionOnly.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
        }, 10 * 1000);
    }

    private void cancelPreviewIdleTimer() {
        if (previewIdleTimer != null) {
            previewIdleTimer.cancel();
            previewIdleTimer = null;
        }
    }

    private void launchConfirmationFragment(String value) {
        String argVal = value;
        if (value.equals("true")) {
            argVal = "high";
        }

        /*if (AppSettings.getTimeAndAttendance() == 1) {
            if (registeredMemberslist != null && registeredMemberslist.size() > 0 && Util.isAlreadyChecked(registeredMemberslist.get(0).getDateTimeCheckInOut())) {
                isAlreadyCheckEdOut();
                return;
            } else if (registeredMemberslist != null && registeredMemberslist.size() > 0 && AccessCardController.getInstance().getCheckInResponseCode() == AccessCardController.AccessCheckInOutStatus.RESPONSE_CODE_SUCCESS.getValue())
                Toast.makeText(IrCameraActivity.this, getString(R.string.access_control_granted), Toast.LENGTH_SHORT).show();
            if (Util.isOfflineMode(getApplicationContext())) onCheckInOutStatus(false);
        }*/

        Fragment confirmationScreenFragment = new ConfirmationScreenFragment();
        Bundle bundle = new Bundle();
        bundle.putString("tempVal", argVal);
        confirmationScreenFragment.setArguments(bundle);
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.add(R.id.dynamic_fragment_frame_layout, confirmationScreenFragment, "ConfirmationScreenFragment");
        transaction.addToBackStack("ConfirmationScreenFragment");
        transaction.commitAllowingStateLoss();
    }

    private void resetHomeScreen() {
        cancelPreviewTimer();
        if (tv_message != null) tv_message.setVisibility(View.GONE);
        if (tvErrorMessage != null) tvErrorMessage.setVisibility(View.GONE);
        tvFaceMessage.setVisibility(View.GONE);
        if (temperature_image != null) temperature_image.setVisibility(View.GONE);
        if (mask_message != null) mask_message.setVisibility(View.GONE);
        if (outerCircle != null && !Util.isDeviceF10()) {
            outerCircle.setBackgroundResource(R.drawable.border_shape);
        }
        retryButton.setVisibility(View.GONE);
        cancelImageTimer();
        DisplayTimeAttendance();
    }

    public void resumeScan() {
        if ((AppSettings.isEnableHandGesture() && Util.isGestureDeviceConnected(this)) || AppSettings.isEnableTouchMode()) {
            GestureController.getInstance().setLanguageUpdated(false);
            GestureController.getInstance().setCallback(false);
            if (AppSettings.isMultiLingualEnabled()) {
                resetGesture();
                return;
            }
        }
        runOnUiThread(() -> {
            img_qr.setVisibility(View.GONE);
            frameLayout.setVisibility(View.GONE);
            clearQrCodePreview();
            if (temperature_image != null) {
                temperature_image.setVisibility(View.GONE);
            }
            if (tvErrorMessage != null) {
                tvErrorMessage.setVisibility(View.GONE);
            }
            tvFaceMessage.setVisibility(View.GONE);
            DisplayTimeAttendance();
        });
        clearData();
        if (AppSettings.getTimeAndAttendance() == 0) {
            if (Util.isDeviceF10()) {
                resumeCameraScan();
                CameraController.getInstance().setScanProcessState(CameraController.ScanProcessState.FIRST_SCAN);
            } else {
                resumeCameraScan();
                initScan();
            }
        }
        if (AppSettings.isEnableHandGesture()) {
            if (Util.isGestureDeviceConnected(this)) {
                if (AppSettings.isMaskEnforced()) {
                    resetMaskEnforceStatus();
                } else {
                    isReadyToScan = false;
                }
                GestureController.getInstance().setLanguageUpdated(false);
            }
            resetGesture();
        }
        if (AppSettings.isEnableTouchMode()) {
            if (AppSettings.isMaskEnforced()) {
                resetMaskEnforceStatus();
            } else {
                isReadyToScan = false;
            }
            GestureController.getInstance().setLanguageUpdated(false);

        }
        resetRfid();
        if (!Util.isDeviceF10() && !qrCodeEnable) {
            resetImageLogo();
        }
        if (!isHomeViewEnabled) isReadyToScan = true;
    }

    private void resetRfid() {
        if (rfIdEnable) {
            if (isTopFragmentSecondaryScreen()) {
                closeFragment(secondaryScreenFragment);
            }
            enableNfc();
            CameraController.getInstance().setCameraOnRfid(false);
        }
    }

    private void clearQrCode() {
        if (qrCodeEnable) {
            frameLayout.setVisibility(View.GONE);
            img_qr.setVisibility(View.GONE);
            resetImageLogo();
            clearQrCodePreview();
        }
    }

    private void pauseCameraScan() {
        Log.d(TAG, "Pause camera scan");
        if (cameraHelper != null) {
            cameraHelper.stop();
        }
        if (cameraHelperIr != null) {
            cameraHelperIr.stop();
        }
    }

    private void resumeCameraScan() {
        Log.d(TAG, "resume camera scan");
        if (cameraHelper != null && cameraHelper.isStopped()) {
            cameraHelper.start();
        }
        if (cameraHelperIr != null && cameraHelperIr.isStopped()) {
            cameraHelperIr.start();
        }
    }

    private void resetCameraView() {
        if (cameraHelper != null) {
            cameraHelper.release();
        }
        if (cameraHelperIr != null) {
            cameraHelperIr.release();
        }
        new Handler().post(() -> {
            initRgbCamera();
            initIrCamera();
        });
    }

    private void resetQrCode() {
        if (qrCodeEnable) {
            runOnUiThread(() -> {
                img_qr.setVisibility(View.GONE);
                clearQrCodePreview();
                initQRCode();
                startCameraSource();
            });
        }
    }

    private void resetInvalidQrCode() {
        preview.stop();
        startCameraSource();
        Toast snackbar = Toast
                .makeText(getApplicationContext(), R.string.invalid_qr, Toast.LENGTH_LONG);
        snackbar.show();
        img_qr.setBackgroundResource(R.drawable.invalid_qr);
        imageqr.setBackgroundColor(getResources().getColor(R.color.colorRed));
        img_qr.setVisibility(View.GONE);
        tv_scan.setText(R.string.tv_qr_scan);
        tv_scan.setBackgroundColor(getResources().getColor(R.color.colorWhite));
        tv_scan.setTextColor(getResources().getColor(R.color.black));
        imageqr.setBackgroundColor(getResources().getColor(R.color.colorWhite));
        Util.writeString(sharedPreferences, GlobalParameters.QRCODE_ID, "");

        AccessCardController.getInstance().sendAccessLogInvalid(this, new RegisteredMembers(), 0,
                new UserExportedData(rgbBitmap, irBitmap, new RegisteredMembers(), (int) 0));

    }

    private void startHidService() {
        if (!Util.isServiceRunning(HIDService.class, this)) {
            Intent msgIntent = new Intent(this, HIDService.class);
            startService(msgIntent);
        }
    }

    private void stopBLEService() {
        Intent intent = new Intent(this, BluetoothLeService.class);
        stopService(intent);
    }

    private void initHidReceiver() {
        hidReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent != null && intent.getAction().equals(HIDService.HID_BROADCAST_ACTION)) {
                    if (AppSettings.getSecondaryIdentifier() == CameraController.SecondaryIdentification.QR_CODE.getValue()) {
                        return;
                    }
                    String data = intent.getStringExtra(HIDService.HID_DATA);
                    runOnUiThread(() -> {
                        if (!data.equals(HIDService.HID_RESTART_SERVICE)) {
                            if (rfIdEnable) {
                                if (!isReadyToScan
                                        || !CameraController.getInstance().isCameraOnRfid()) {
                                    Log.d(TAG, "HID Card Id UI " + data);
                                    onRfidScan(data);
                                }
                            }
                            return;
                        }
                        HIDService.readTerminal = false;
                        new Handler().postDelayed(() -> {
                            Log.d(TAG, "HID Restarting Service");
                            enableHidReader();
                        }, 200);
                    });
                }
            }
        };
    }

    private void stopHidService() {
        HIDService.readTerminal = false;
    }

    private void clearDisposables() {
        if (getFeatureDelayedDisposables != null) {
            getFeatureDelayedDisposables.clear();
        }
        if (delayFaceTaskCompositeDisposable != null) {
            delayFaceTaskCompositeDisposable.clear();
        }
        if (alignedFacesDisposable != null) {
            alignedFacesDisposable.clear();
        }
        if (searchFaceDisposable != null) {
            searchFaceDisposable.clear();
        }
        if (maskDetectDisposable != null) {
            maskDetectDisposable.clear();
        }
    }

    private void initRecordUserTempService() {
        if (!Util.isOfflineMode(IrCameraActivity.this) && !Util.isServiceRunning(OfflineRecordSyncService.class, this)) {
            Log.d(TAG, "Offline service ");
            startService(new Intent(IrCameraActivity.this, OfflineRecordSyncService.class));
        }
    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            if (message.what == CameraController.IMAGE_PROCESS_COMPLETE) {
                if (CameraController.getInstance().isAppExitTriggered()) {
                    Log.d(TAG, "App exit triggered, Launch Login");
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                        Intent loginIt = new Intent(IrCameraActivity.this, LoginActivity.class);
                        startActivity(loginIt);
                        finish();
                    }
                }
                CameraController.getInstance().setScanState(CameraController.ScanState.IDLE);
                CameraController.getInstance().setAppExitTriggered(false);
            }
            return false;
        }
    });

    public void skipQr(View view) {
        preview.stop();
        clearQrCodePreview();
        setCameraPreview();
    }

    /**
     * Method that initiates the temperature controller init
     */
    private void initTemperature() {
        TemperatureController.getInstance().init(this);
        //TemperatureController.getInstance().setTemperatureListener(this);
        if (AppSettings.getfToC().equals("F")) {
            MIN_TEMP_DISPLAY_THRESHOLD = 50;
        } else {
            MIN_TEMP_DISPLAY_THRESHOLD = 10;
        }
    }

    @Override
    public void onThermalImage(Bitmap bitmap) {
        runOnUiThread(() -> {
            if (bitmap != null) {
                temperatureBitmap = bitmap;
                temperature_image.setVisibility(View.VISIBLE);
                temperature_image.setImageBitmap(bitmap);
                if (!isProDevice && userData != null && isRecordNotSent) {
                    userData.thermal = temperatureBitmap;
                    isRecordNotSent = false;
                    int syncStatus;
                    if (Util.isOfflineMode(IrCameraActivity.this)) {
                        syncStatus = 1;
                    } else {
                        syncStatus = -1;
                    }
                    TemperatureController.getInstance().recordUserTemperature(userData, syncStatus);
                }
            }
        });
    }

    @Override
    public void onTemperatureRead(float temperature) {
        TemperatureController.getInstance().setTemperatureRetry(0);
        this.temperature = temperature;
        boolean aboveThreshold = TemperatureController.getInstance().isTemperatureAboveThreshold(temperature);
        if (PrinterController.getInstance().isPrintScan(aboveThreshold)) {
            updatePrintOnTemperatureRead(temperature);
            return;
        }
        String tempString = String.valueOf(temperature);
        String text = "";
        if (isLowTempRead || !AppSettings.isCaptureTemperature()) {
            if (!AppSettings.isCaptureTemperature()) {
                text = AppSettings.getTempResultBarNormal();
            } else if (temperature < TemperatureController.MIN_TEMPERATURE_THRESHOLD) {
                float tempValue = TemperatureController.MIN_TEMPERATURE_THRESHOLD - 0.1f;
                String tempRead = String.format("%.1f", tempValue);
                if (TemperatureController.getInstance().isTemperatureAboveThreshold(tempValue)) {
                    text = AppSettings.getTempResultBarHigh() + ": " + tempRead + TemperatureController.getInstance().getTemperatureUnit();
                    TemperatureCallBackUISetup(true, text, tempRead, false, TemperatureController.getInstance().getTemperatureRecordData());
                    TemperatureController.getInstance().updateControllersOnHighTempRead(registeredMemberslist);
                    TemperatureController.getInstance().clearData();
                    isLowTempRead = false;
                    return;
                }
                text = AppSettings.getTempResultBarNormal() + ": " + tempRead + TemperatureController.getInstance().getTemperatureUnit();
            } else {
                text = AppSettings.getTempResultBarNormal() + ": " + tempString + TemperatureController.getInstance().getTemperatureUnit();
            }
        } else {
            text = AppSettings.getTempResultBarNormal() + ": " + tempString + TemperatureController.getInstance().getTemperatureUnit();
        }
        isLowTempRead = false;
        if (TemperatureController.getInstance().isTemperatureAboveThreshold(temperature)) {
            if (AppSettings.isCaptureTemperature()) {
                text = AppSettings.getTempResultBarHigh() + ": " + tempString + TemperatureController.getInstance().getTemperatureUnit();
            } else {
                text = AppSettings.getTempResultBarHigh();
            }
            TemperatureCallBackUISetup(true, text, tempString, false, TemperatureController.getInstance().getTemperatureRecordData());
            TemperatureController.getInstance().updateControllersOnHighTempRead(registeredMemberslist);
            TemperatureController.getInstance().clearData();
            return;
        }
        TemperatureCallBackUISetup(false, text, tempString, false, TemperatureController.getInstance().getTemperatureRecordData());
        TemperatureController.getInstance().updateControllersOnNormalTempRead(registeredMemberslist);
        TemperatureController.getInstance().clearData();
    }

    @Override
    public void onTemperatureFail(TemperatureController.GuideMessage errorCode) {
        runOnUiThread(() -> {
            switch (errorCode.getValue()) {
                case 1: {
                    tvErrorMessage.setVisibility(View.VISIBLE);
                    tvErrorMessage.setText(sharedPreferences.getString(GlobalParameters.GUIDE_TEXT1, getResources().getString(R.string.text_value1)));
                    outerCircle.setBackgroundResource(R.drawable.border_shape_red);
                }
                break;

                case 2: {
                    tvErrorMessage.setVisibility(View.VISIBLE);
                    tvErrorMessage.setText(sharedPreferences.getString(GlobalParameters.GUIDE_TEXT2, getResources().getString(R.string.text_value2)));
                    outerCircle.setBackgroundResource(R.drawable.border_shape_red);
                }
                break;

                case 3: {
                    tvErrorMessage.setVisibility(View.VISIBLE);
                    tvErrorMessage.setText(sharedPreferences.getString(GlobalParameters.GUIDE_TEXT3, getResources().getString(R.string.text_value3)));
                    outerCircle.setBackgroundResource(R.drawable.border_shape_red);
                }
                break;

                /*default: {
                    tvErrorMessage.setVisibility(View.GONE);
                }*/
            }
        });
        TemperatureController.getInstance().clearData();
        CameraController.getInstance().setScanState(CameraController.ScanState.IDLE);
        TemperatureController.getInstance().setTemperatureListener(this);
        clearLeftFace(null);
    }

    @Override
    public void onFaceNotInRangeOfThermal() {
        runOnUiThread(() -> {
            if (faceThermalToast != null) return;
            faceThermalToast = Toast.makeText(IrCameraActivity.this, getString(R.string.move_center_msg), Toast.LENGTH_SHORT);
            faceThermalToast.show();
        });
    }

    @Override
    public void onThermalGuideReset() {
        Log.d(TAG, "onThermalGuideReset");
        TemperatureController.getInstance().clearData();
        CameraController.getInstance().setScanState(CameraController.ScanState.IDLE);
        TemperatureController.getInstance().setTemperatureListener(this);
        clearLeftFace(null);
    }

    @Override
    public void onTemperatureLow(int retryCount, float temperature) {
        this.temperature = temperature;
        if (TemperatureController.getInstance().getTemperatureRetry() == Constants.TEMPERATURE_MAX_RETRY) {
            TemperatureController.getInstance().setTemperatureRetry(0);
            if (temperature < MIN_TEMP_DISPLAY_THRESHOLD && (AppSettings.getScanType() != 1)) {
                Log.d(TAG, "onTemperatureLow Temperature less than display threshold" + temperature);
                return;
            }
            Log.d(TAG, "onTemperatureLow " + temperature);
            cancelImageTimer();
            isLowTempRead = true;
            onTemperatureRead(temperature);
            return;
        }
        runOnUiThread(() -> {
            tvErrorMessage.setVisibility(View.VISIBLE);
            if (isProDevice) {
                tvErrorMessage.setText(sharedPreferences.getString(GlobalParameters.GUIDE_TEXT4, getResources().getString(R.string.step_closer)));
            } else {
                tvErrorMessage.setText(sharedPreferences.getString(GlobalParameters.GUIDE_TEXT2, getResources().getString(R.string.text_value2)));
                outerCircle.setBackgroundResource(R.drawable.border_shape_red);
            }
        });
        startCameraPreviewTimer();
        TemperatureController.getInstance().clearData();
        CameraController.getInstance().setScanState(CameraController.ScanState.IDLE);
        TemperatureController.getInstance().setTemperatureListener(this);
        clearLeftFace(null);
    }

    @Override
    public void onFaceDistanceNotInRange() {
        runOnUiThread(() -> tvErrorMessage.setText(getResources().getString(R.string.step_closer)));
    }

    private void initBluetoothPrinter() {
        // initialization for printing
        if (AppSettings.isEnablePrinter() || AppSettings.isPrintUsbEnabled()) {
            PrinterController.getInstance().init(this, this);
            PrinterController.getInstance().setPrinterListener(this);
        }
    }

    @Override
    public void onBluetoothDisabled() {
        /*final Intent enableBtIntent = new Intent(
                BluetoothAdapter.ACTION_REQUEST_ENABLE);
        enableBtIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(enableBtIntent);*/
    }

    @Override
    public void onPrintComplete() {
        if (!AppSettings.isTemperatureScanEnabled()) {
            updateOnTemperatureScanDisabled();
            return;
        }
        onTemperatureUpdate();
        TemperatureController.getInstance().clearData();
    }

    @Override
    public void onPrintError() {
        if (!AppSettings.isTemperatureScanEnabled()) {
            updateOnTemperatureScanDisabled();
            return;
        }
        onTemperatureUpdate();
        TemperatureController.getInstance().clearData();
    }

    @Override
    public void onPrintUsbCommand() {
        runOnUiThread(() -> new PrintExecuteTask(this,
                PrinterController.getInstance().getUsbPrintControl(), this)
                .execute(PrinterController.getInstance().getPrintData()));
    }

    @Override
    public void onPrintUsbSuccess(String status, long resultCode) {
        runOnUiThread(() -> {
            String strMessage = String.format(getString(R.string.statusReception) + " %s : %08x ", status, resultCode);
            util.showAlertDialog(IrCameraActivity.this, strMessage);
            onPrintComplete();
        });

    }

    @Override
    public void onPrintStatus(String status, int code) {
        Log.d(TAG, "Print status " + status);
        PrinterController.getInstance().setPrinting(false);
        runOnUiThread(this::onPrintComplete);
    }

    private void updatePrinterParameters(boolean highTemperature) {
        Bitmap bitmap = null;
        String name = "";
        String thermalText = "";

        PrinterController.getInstance().checkPrintFileExists();
        if (AppSettings.isPrintLabelFace() && rgbBitmap != null) {
            bitmap = Bitmap.createScaledBitmap(rgbBitmap, 320, 320, false);
            PrinterController.getInstance().updateImageForPrint(bitmap);
        }
        if (AppSettings.isPrintLabelUnidentifiedName()) {
            name = AppSettings.getEditTextNameLabel();
        }
        RegisteredMembers member = null;
        UserExportedData data = null;
        if (AppSettings.isTemperatureScanEnabled()) {
            data = TemperatureController.getInstance().getTemperatureRecordData();
        } else {
            data = CameraController.getInstance().getUserExportedData();
        }
        if (data != null) {
            member = data.member;
        }
        String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
        String date = new SimpleDateFormat("MM/dd/yy", Locale.getDefault()).format(new Date());
        //String dateTime = date;
        String triggerType = CameraController.getInstance().getTriggerType();
        if (triggerType.equals(CameraController.triggerValue.CODEID.toString())) {
            if (AppSettings.isEnablePrinter()) {
                date = new SimpleDateFormat("MMM dd yyyy", Locale.getDefault()).format(new Date());
            }
            QrCodeData qrCodeData = CameraController.getInstance().getQrCodeData();
            if ((AppSettings.isPrintQrCodeUsers() || AppSettings.isPrintAllScan()) && qrCodeData != null) {
                if (AppSettings.isPrintLabelFace() && member != null) {
                    bitmap = BitmapFactory.decodeFile(member.image);
                    if (bitmap == null) {
                        bitmap = rgbBitmap;
                    }
                }
                if (AppSettings.isPrintLabelName()) {
                    name = qrCodeData.getFirstName();
                }
            }
            if (AppSettings.isPrintLabelQRAnswers()) {
                thermalText = AppSettings.getEditTextPrintQRAnswers();
            }
            thermalText = thermalText + " " + getTemperatureValue(highTemperature);
        } else if (triggerType.equals(CameraController.triggerValue.ACCESSID.toString())) {
            if ((AppSettings.isPrintAccessCardUsers() || AppSettings.isPrintAllScan()) && AccessControlModel.getInstance().getRfidScanMatchedMember() != null) {
                if (AppSettings.isPrintLabelFace()) {
                    bitmap = BitmapFactory.decodeFile(AccessControlModel.getInstance().getRfidScanMatchedMember().image);
                    if (bitmap == null) {
                        bitmap = rgbBitmap;
                    }
                }
                if (AppSettings.isPrintLabelName()) {
                    name = AccessControlModel.getInstance().getRfidScanMatchedMember().firstname;
                }
                if (AppSettings.isPrintLabelQRAnswers()) {
                    thermalText = AppSettings.getEditTextPrintQRAnswers();
                }
            }
        } else if (triggerType.equals(CameraController.triggerValue.WAVE.toString())) {
            if ((AppSettings.isPrintWaveUsers() || AppSettings.isPrintAllScan())) {
                if (AppSettings.isPrintLabelFace() && member != null) {
                    bitmap = BitmapFactory.decodeFile(member.image);
                    if (bitmap == null) {
                        bitmap = rgbBitmap;
                    }
                }
                if (AppSettings.isEnablePrinter()) {
                    date = new SimpleDateFormat("MMM dd yyyy", Locale.getDefault()).format(new Date());
                }
                String answers = GestureController.getInstance().getAnswers();
                answers = answers.replace(",", "");
                answers = answers.replace("[", "");
                answers = answers.replace("]", "");

                if (AppSettings.isPrintLabelWaveAnswers()) {
                    String answerYes = AppSettings.getEditTextPrintWaveYes();
                    String answerNo = AppSettings.getEditTextPrintWaveNo();
                    answers = answers.replace("Y", answerYes);
                    answers = answers.replace("N", answerNo);
                    String tempValueStr = getTemperatureValue(highTemperature);
                    thermalText = answers + " " + tempValueStr;
                }
            }
        } else {
            if (AppSettings.isPrintAllScan()) {
                if (AppSettings.isFacialDetect() && member != null) {
                    if (AppSettings.isPrintLabelFace()) {
                        bitmap = BitmapFactory.decodeFile(member.image);
                    }
                    if (member.firstname != null) {
                        if (AppSettings.isPrintLabelName()) {
                            name = member.firstname;
                        }
                    }
                } else {
                    name = "";
                    if (AppSettings.isPrintLabelUnidentifiedName()) {
                        name = AppSettings.getEditTextNameLabel();
                    }
                }
                if (AppSettings.isPrintLabelQRAnswers()) {
                    thermalText = AppSettings.getEditTextPrintQRAnswers();
                }
                if (bitmap == null && AppSettings.isPrintLabelFace()) {
                    bitmap = rgbBitmap;
                }
            }
        }
        PrinterController.getInstance().setPrintData(name, date, thermalText, currentTime, highTemperature);
        convertUIToImage(bitmap, name, date, thermalText, currentTime, highTemperature);
    }

    private void convertUIToImage(Bitmap bitmap, String name, String dateTime, String thermalText, String scanTime, boolean highTemperature) {
        View view = getLayoutInflater().inflate(R.layout.print_layout, null);
        LinearLayout linearLayout = view.findViewById(R.id.screen);
        TextView expireDate = view.findViewById(R.id.expire_date);
        TextView userName = view.findViewById(R.id.user_name);
        ImageView userImage = view.findViewById(R.id.user_image);
        TextView tempPass = view.findViewById(R.id.temp_Pass);
        TextView scanTimeText = view.findViewById(R.id.scan_time);
        TextView thermalDisplayText = view.findViewById(R.id.thermal_scan_text);
        scanTimeText.setText(scanTime);
        userName.setText(name);
        thermalDisplayText.setText(thermalText);
        if (highTemperature) {
            tempPass.setText("");
            tempPass.setBackgroundColor(getColor(R.color.colorWhite));
        } else {
            String passText = AppSettings.getEditTextPrintPassName();
            String triggerType = CameraController.getInstance().getTriggerType();
            if (triggerType.equals(CameraController.triggerValue.WAVE.toString()) &&
                    GestureController.getInstance().isQuestionnaireFailed()) {
                tempPass.setText("");
                tempPass.setBackgroundColor(getColor(R.color.colorWhite));
            } else {
                tempPass.setText(passText);
            }
        }
        if (bitmap != null) {
            userImage.setImageBitmap(bitmap);
        }
        expireDate.setText(dateTime);
        linearLayout.setDrawingCacheEnabled(true);
        linearLayout.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        linearLayout.layout(0, 0, linearLayout.getMeasuredWidth(), linearLayout.getMeasuredHeight());
        linearLayout.buildDrawingCache(true);
        PrinterController.getInstance().setPrintImage(linearLayout.getDrawingCache());
    }

    /**
     * Method that draws the rect for the face
     *
     * @param facePreviewInfoList face info list
     */
    private void drawPreviewInfo(List<FacePreviewInfo> facePreviewInfoList) {
        runOnUiThread(() -> {
            List<DrawInfo> drawInfoList = new ArrayList<>();
            if (!facePreviewInfoList.isEmpty()) {
                int trackId = facePreviewInfoList.get(0).getTrackId();
                Integer liveness = livenessMap.get(trackId);
                drawInfoList.add(new DrawInfo(drawHelperRgb.adjustRect(facePreviewInfoList.get(0).getFaceInfo().getRect()),
                        GenderInfo.UNKNOWN, AgeInfo.UNKNOWN_AGE,
                        liveness == null ? LivenessInfo.UNKNOWN : liveness, Color.WHITE, ""));

                drawHelperRgb.drawPreviewInfo(faceRectView, drawInfoList);
            }
        });
    }

    private void updatePrintOnTemperatureRead(float temperature) {
        String tempString = String.valueOf(temperature);
        String text = "";
        if (isLowTempRead || !AppSettings.isCaptureTemperature()) {
            if (!AppSettings.isCaptureTemperature()) {
                text = AppSettings.getTempResultBarNormal();
            } else if (temperature < TemperatureController.MIN_TEMPERATURE_THRESHOLD) {
                float tempValue = TemperatureController.MIN_TEMPERATURE_THRESHOLD - 0.1f;
                String tempRead = String.format("%.1f", tempValue);
                if (TemperatureController.getInstance().isTemperatureAboveThreshold(tempValue)) {
                    text = AppSettings.getTempResultBarHigh() + ": " + tempRead + TemperatureController.getInstance().getTemperatureUnit();
                    TemperatureCallBackUISetup(true, text, tempRead, false, TemperatureController.getInstance().getTemperatureRecordData());
                    updatePrinterParameters(true);
                    TemperatureController.getInstance().updateControllersOnHighTempRead(registeredMemberslist);
                    TemperatureController.getInstance().clearData();
                    isLowTempRead = false;
                    return;
                }
                text = AppSettings.getTempResultBarNormal() + ": " + tempRead + TemperatureController.getInstance().getTemperatureUnit();
            } else {
                text = AppSettings.getTempResultBarNormal() + ": " + tempString + TemperatureController.getInstance().getTemperatureUnit();
            }
        } else {
            text = AppSettings.getTempResultBarNormal() + ": " + tempString + TemperatureController.getInstance().getTemperatureUnit();
        }
        isLowTempRead = false;
        if (TemperatureController.getInstance().isTemperatureAboveThreshold(temperature)) {
            if (AppSettings.isCaptureTemperature()) {
                text = AppSettings.getTempResultBarHigh() + ": " + tempString + TemperatureController.getInstance().getTemperatureUnit();
            } else {
                text = AppSettings.getTempResultBarHigh();
            }
            TemperatureCallBackUISetup(true, text, tempString, false, TemperatureController.getInstance().getTemperatureRecordData());
            updatePrinterParameters(true);
            TemperatureController.getInstance().updateControllersOnHighTempRead(registeredMemberslist);
            TemperatureController.getInstance().clearData();
            return;
        }
        TemperatureCallBackUISetup(false, text, tempString, false, TemperatureController.getInstance().getTemperatureRecordData());
        updatePrinterParameters(false);
        TemperatureController.getInstance().updateControllersOnNormalTempRead(registeredMemberslist);
        TemperatureController.getInstance().clearData();
    }

    /**
     * Method that updates the UI for Normal temperature
     */
    private void onTemperatureUpdate() {
        disableLedPower();
        runOnUiThread(() -> {
            boolean confirmAboveScreen = sharedPreferences.getBoolean(GlobalParameters.CONFIRM_SCREEN_ABOVE, true);
            boolean confirmBelowScreen = sharedPreferences.getBoolean(GlobalParameters.CONFIRM_SCREEN_BELOW, true);
            if (TemperatureController.getInstance().isTemperatureAboveThreshold(temperature)) {
                updateUIOnPrint(confirmAboveScreen, true);
                return;
            }
            updateUIOnPrint(confirmBelowScreen, false);
        });
    }

    private void launchGestureFragment() {
        if (isDestroyed() || isFinishing() || !isActivityResumed) return;
        try {
            gestureFragment = new GestureFragment();
            Bundle bundle = new Bundle();
            bundle.putString("maskStatus", String.valueOf(maskStatus));
            gestureFragment.setArguments(bundle);
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.add(R.id.dynamic_fragment_frame_layout, gestureFragment, "GestureFragment");
            transaction.addToBackStack("GestureFragment");
            transaction.commitAllowingStateLoss();
        } catch (Exception e) {
            Log.e(TAG, "Error in launching Gesture fragment");
        }
    }

    private void launchTouchFragment() {
        if (isDestroyed() || isFinishing() || !isActivityResumed) return;
        try {
            touchModeFragment = new TouchModeFragment();
            Bundle bundle = new Bundle();
            bundle.putString("maskStatus", String.valueOf(maskStatus));
            touchModeFragment.setArguments(bundle);
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.add(R.id.dynamic_fragment_frame_layout, touchModeFragment, "launchTouch");
            transaction.addToBackStack("launchTouch");
            transaction.commitAllowingStateLoss();
        } catch (Exception e) {
            Log.e(TAG, "Error in launching Touch fragment");
        }
    }

    public void resumeFromGesture() {
        resumedFromGesture = true;
        runOnUiThread(() -> {
            int delay = 2 * 1000;
            if (isProDevice) {
                delay = 1500;
            }
            if (AppSettings.isTemperatureScanEnabled() || AppSettings.isFacialDetect() ||
                    AppSettings.isPrintLabelFace()) {
                CameraController.getInstance().setScanState(CameraController.ScanState.FACIAL_SCAN);
                setCameraPreview();
                new Handler().postDelayed(this::closeGestureFragment, delay);
            } else {
                onTemperatureScanDisabled();
            }
        });
    }

    private void showPrintMsgDialog() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPreferences != null) {
            String printDevice = sharedPreferences.getString("printer", "NONE");
            if (printDevice != null && !printDevice.equalsIgnoreCase("NONE")) {
                showAlertDialog("", getString(R.string.pair_printer_message), getString(R.string.button_ok), "");
            }
        }
    }

    private void showAlertDialog(String title, String message, String positiveButton, String negativeButton) {
        AlertDialog.Builder builder = new AlertDialog.Builder(IrCameraActivity.this);
        builder.setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton(positiveButton, (dialog, id) -> {
                    dialog.dismiss();
                });
        AlertDialog dialog = builder.create();
        dialog.show();
        new Handler().postDelayed(() -> {
            if (isDestroyed() || isFinishing()) return;
            if (dialog != null) {
                dialog.dismiss();
            }
        }, 6000);
    }

    private void startBLEService() {
        try {
            if (AppSettings.isBleLightNormalTemperature() || AppSettings.isBleLightHighTemperature()) {
                if (!Util.isServiceRunning(BluetoothLeService.class, IrCameraActivity.this)) {
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

    private boolean isInstitutionIdEmpty() {
        boolean result = false;
        if (institutionId.isEmpty()) {
            result = true;
            Logger.error(TAG, "onBarcodeData()", "Error! InsitutionId is empty");
            Toast toastbar = Toast
                    .makeText(getApplicationContext(), R.string.device_not_register, Toast.LENGTH_LONG);
            toastbar.show();
            resetInvalidQrCode();
        }
        return result;
    }

    private void startQRTimer(String guid) {
        cancelQRTimer();
        mQRTimer = new Timer();
        mQRTimer.schedule(new TimerTask() {
            public void run() {
                runOnUiThread(() -> {
                    qrCodeReceived = false;
                    Toast.makeText(IrCameraActivity.this, getString(R.string.qr_validation_msg), Toast.LENGTH_SHORT).show();
                    CameraController.getInstance().setQrCodeId(guid);
                    Util.writeString(sharedPreferences, GlobalParameters.ACCESS_ID, guid);
                    clearQrCodePreview();
                    if ((CameraController.getInstance().getScanProcessState()
                            == CameraController.ScanProcessState.FIRST_SCAN) ||
                            (CameraController.getInstance().getScanProcessState() ==
                                    CameraController.ScanProcessState.FIRST_SCAN_COMPLETE)) {
                        if ((AppSettings.getSecondaryIdentifier() == CameraController.SecondaryIdentification.FACE.getValue()) ||
                                (AppSettings.getSecondaryIdentifier() == CameraController.SecondaryIdentification.NONE.getValue())) {
                            setCameraPreview();
                        } else {
                            initSecondaryScan();
                        }
                    } else {
                        setCameraPreview();
                    }
                });
                this.cancel();
            }
        }, 5 * 1000);
    }

    private void cancelQRTimer() {
        if (mQRTimer != null) {
            mQRTimer.cancel();
            mQRTimer = null;
        }
    }

    private void startCameraPreviewTimer() {
        if (imageTimer != null) {
            imageTimer.cancel();
            imageTimer = null;
        }
        imageTimer = new Timer();
        imageTimer.schedule(new TimerTask() {
            public void run() {
                TemperatureController.getInstance().setTemperatureListener(null);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        resetHomePage();
                    }
                });
                this.cancel();
            }
        }, 10 * 1000);//wait 10 seconds for the temperature to be captured, go to home otherwise
    }

    private void initGesture() {
        if (AppSettings.isEnableHandGesture()) {
            if (Util.isGestureDeviceConnected(this)) {
                if (AppSettings.isMaskEnforced()) {
                    isReadyToScan = true;
                } else {
                    isReadyToScan = false;
                }
                CameraController.getInstance().setScanState(CameraController.ScanState.GESTURE_SCAN);
                GestureController.getInstance().initContext(this);
                GestureController.getInstance().setGestureHomeCallbackListener(this);
                return;
            }
            if (GestureController.getInstance().getAnswerType() != GestureController.AnswerType.Touch)
                runOnUiThread(() -> Toast.makeText(IrCameraActivity.this, getString(R.string.connect_gesture_device), Toast.LENGTH_LONG).show());
            return;
        }
        if (AppSettings.isEnableTouchMode()) {
            if (AppSettings.isMaskEnforced()) {
                isReadyToScan = true;
            } else {
                isReadyToScan = false;
            }
        }
        GestureController.getInstance().checkGestureStatus();
    }

    public void resetGestureScreen() {
        resetGesture();
        if (AppSettings.getTimeAndAttendance() == 1) {
            DisplayTimeAttendance();
        }
    }

    public void resetGesture() {
        GestureController.getInstance().setGestureHomeCallbackListener(this);
        CameraController.getInstance().setScanState(CameraController.ScanState.GESTURE_SCAN);
        if ((AppSettings.isEnableHandGesture() && Util.isGestureDeviceConnected(this)) || AppSettings.isEnableTouchMode()) {
            GestureController.getInstance().setLanguageUpdated(false);
            if (AppSettings.isMultiLingualEnabled()) {
                GestureController.getInstance().setLanguageSelectionIndex(0);
                DeviceSettingsController.getInstance().setLanguageToUpdate(AppSettings.getLanguageType());
                DeviceSettingsController.getInstance().getSettingsFromDb(
                        DeviceSettingsController.getInstance().getLanguageIdOnCode(AppSettings.getLanguageType()));
                GestureController.getInstance().getQuestionsFromDb(AppSettings.getLanguageType());
                runOnUiThread(this::recreate);
            }
        }
    }

    public void onGestureNegativeAnswer() {
        resumedFromGesture = true;
        runOnUiThread(() -> {
            int delay = 500;
            new Handler().postDelayed(() -> closeFragment(gestureFragment), delay);
            boolean confirmAboveScreen = sharedPreferences.getBoolean(GlobalParameters.CONFIRM_SCREEN_ABOVE, true);
            boolean confirmBelowScreen = sharedPreferences.getBoolean(GlobalParameters.CONFIRM_SCREEN_BELOW, true);
            if (confirmAboveScreen || confirmBelowScreen) {
                launchConfirmationFragment("gestureExit");
                if (isHomeViewEnabled) {
                    pauseCameraScan();
                } else {
                    isReadyToScan = false;
                }
                resetHomeScreen();
            } else {
                ShowLauncherView();
            }
        });
    }

    @Override
    public void onGestureDetected() {
        if (!AppSettings.isEnableQuestions()) {
            Toast.makeText(this, getString(R.string.gesture_questions_not_available), Toast.LENGTH_LONG).show();
            return;
        }
        GestureController.getInstance().setLanguageSelectionIndex(0);
        runOnUiThread(() -> {
            if (time_attendance_layout.getVisibility() == View.VISIBLE) {
                GestureController.getInstance().setCallback(false);
                return;
            }
            if ((relative_main.getVisibility() == View.GONE) ||
                    (AccessCardController.getInstance().getTapCount() != 0) ||
                    (CameraController.getInstance().getTriggerType().equals(CameraController.triggerValue.CODEID.toString())))
                return;

            resumedFromGesture = false;
            GestureController.getInstance().clearData();
            CameraController.getInstance().setTriggerType(CameraController.triggerValue.WAVE.toString());
            if (AppSettings.isEnableHandGesture() && GestureController.getInstance().getAnswerType() == GestureController.AnswerType.Wave)
                Toast.makeText(this, getString(R.string.gesture_launch_msg), Toast.LENGTH_SHORT).show();
            if (AppSettings.isMaskEnforced()) {
                isReadyToScan = false;
                clearLeftFace(null);

                if (maskStatus == 1) {
                    new Handler().postDelayed(this::launchGestureFragment, 1000);
                } else {
                    new Handler().postDelayed(this::launchMaskEnforceFragment, 1000);
                }
                return;
            }
            new Handler().postDelayed(this::launchGestureFragment, 1000);
        });
    }

    @Override
    public void onLeftHandGesture() {
        runOnUiThread(() -> {
            if (GestureController.getInstance().getAnswerType() == GestureController.AnswerType.Touch)
                return;
            if ((relative_main.getVisibility() == View.GONE) ||
                    (AccessCardController.getInstance().getTapCount() != 0) ||
                    (CameraController.getInstance().getTriggerType().equals(CameraController.triggerValue.CODEID.toString()))) {
                GestureController.getInstance().setLanguageSelectionIndex(0);
                GestureController.getInstance().cancelWaveHandTimer();
                return;
            }

            if (AppSettings.isMultiLingualEnabled() &&
                    !GestureController.getInstance().isLanguageUpdated()) {
                if (GestureController.getInstance().updateNextLanguage()) {
                    String msg = String.format(getString(R.string.update_language_msg),
                            GestureController.getInstance().getUpdatingLanguageName());
                    Toast.makeText(IrCameraActivity.this, msg, Toast.LENGTH_LONG).show();
                    DeviceSettingsController.getInstance().getSettingsFromDb(DeviceSettingsController.getInstance().
                            getLanguageIdOnCode(DeviceSettingsController.getInstance().getLanguageToUpdate()));
                    GestureController.getInstance().getQuestionsFromDb(DeviceSettingsController.getInstance().getLanguageToUpdate());
                    new Handler().postDelayed(this::recreate, 500);
                }
                return;
            }
            GestureController.getInstance().setCallback(false);
        });
    }

    private void launchAcknowledgementFragment() {
        acknowledgementFragment = new AcknowledgementFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.add(R.id.dynamic_fragment_frame_layout, acknowledgementFragment, "AcknowledgementFragment");
        transaction.addToBackStack("AcknowledgementFragment");
        transaction.commitAllowingStateLoss();
    }

    private void closeFragment(Fragment fragment) {
        if (fragment != null) {
            if (isDestroyed() || isFinishing()) return;
            try {
                getFragmentManager().beginTransaction().remove(fragment).commitAllowingStateLoss();
            } catch (Exception e) {
                Log.d(TAG, "Error in closing Acknowledgement fragment");
            }
        }
    }

    private void resetAcknowledgementScreen() {
        if (AppSettings.isAcknowledgementScreen()) {
            closeFragment(acknowledgementFragment);
            AccessCardController.getInstance().setTapCount(0);
        }
    }

    private void onTemperatureScanDisabled() {
        if (PrinterController.getInstance().isPrintScan(false) &&
                CameraController.getInstance().isMemberIdentified(registeredMemberslist)) {
            cancelImageTimer();
            updatePrinterParameters(false);
            PrinterController.getInstance().printOnNormalTemperature();
            return;
        }
        updateOnTemperatureScanDisabled();
    }

    private void onRfidOnlyEnabled(String cardId) {
        AccessCardController accessCardController = AccessCardController.getInstance();
        accessCardController.setAccessCardId(cardId);
        //launch the fragment
        if (AppSettings.isAcknowledgementScreen()) {
            if (accessCardController.getTapCount() == 0) {
                CameraController.getInstance().updateScanProcessState(null);
                accessCardController.setTapCount(1);
                launchAcknowledgementFragment();
                setCameraPreviewTimer(15);
                return;
            }
        } else {
            CameraController.getInstance().updateScanProcessState(null);
            if (checkSecondaryIdentifier()) {
                initSecondaryScan();
                return;
            }
        }
        if (AppSettings.isAcknowledgementScreen()) {
            if (checkSecondaryIdentifier()) {
                initSecondaryScan();
                new Handler().postDelayed(() -> {
                    closeFragment(acknowledgementFragment);
                    accessCardController.setTapCount(0);
                }, 1000);
                return;
            }
        }
        if ((CameraController.getInstance().getScanProcessState()
                == CameraController.ScanProcessState.FIRST_SCAN) ||
                (CameraController.getInstance().getScanProcessState()
                        == CameraController.ScanProcessState.FIRST_SCAN_COMPLETE)) {
            CameraController.getInstance().setScanProcessState(CameraController.ScanProcessState.SECOND_SCAN);
        }
        if (AppSettings.isTemperatureScanEnabled()) {
            enableLedPower();
            showSnackBarMessage(getString(R.string.access_granted));
            setCameraPreview();
        } else {
            onTemperatureScanDisabled();
            return;
        }
        new Handler().postDelayed(() -> {
            closeFragment(acknowledgementFragment);
            accessCardController.setTapCount(0);
        }, 2000);
    }

    private void onRfidNoMemberMatch(String cardId) {
        if (AccessCardController.getInstance().isEnableWiegandPt() ||
                AccessCardController.getInstance().isAllowAnonymous()) {
            onRfidOnlyEnabled(cardId);
            return;
        }
        showSnackBarMessage(getString(R.string.access_denied));
        //Access Log api call
        RegisteredMembers member = new RegisteredMembers();
        member.setAccessid(cardId);
        AccessCardController.getInstance().sendAccessLogInvalid(this, member, 0,
                new UserExportedData(rgbBitmap, irBitmap, new RegisteredMembers(), (int) 0));

        resetToHomePage();
    }

    private void launchMaskEnforceFragment() {
        if (isDestroyed() || isFinishing() || !isActivityResumed) return;
        try {
            maskEnforceFragment = new MaskEnforceFragment();
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.add(R.id.dynamic_fragment_frame_layout, maskEnforceFragment, "MaskEnforceFragment");
            transaction.addToBackStack("MaskEnforceFragment");
            transaction.commitAllowingStateLoss();
        } catch (Exception e) {
            Log.e(TAG, "Error in launching mask enforcement fragment");
        }
    }

    private void closeMaskEnforceFragment() {
        if (maskEnforceFragment != null) {
            if (isDestroyed() || isFinishing() || !isActivityResumed) return;
            try {
                getFragmentManager().beginTransaction().remove(maskEnforceFragment).commitAllowingStateLoss();
            } catch (Exception e) {
                Log.e(TAG, "Error in closing mask enforcement fragment");
            }
        }
    }

    public void resumeFromMaskEnforcement() {
        runOnUiThread(() -> {
            new Handler().postDelayed(() -> {
                launchGestureFragment();
                closeMaskEnforceFragment();
            }, 300);
        });
    }

    private void resetMaskEnforceStatus() {
        resumedFromGesture = false;
        isReadyToScan = true;
    }

    public void resetMaskEnforcementGesture() {
        resetMaskEnforceStatus();
        resetGesture();
    }

    public void resetTouchModeGesture() {
        touchModeGesture();
    }

    private void touchModeGesture() {
        GestureController.getInstance().setAnswerType(GestureController.AnswerType.Touch);
        GestureController.getInstance().setCallback(true);
        onGestureDetected();
    }

    private void updateUIOnPrint(boolean displayCScreen, boolean aboveThreshold) {
        if (displayCScreen) {
            if (isDestroyed()) return;
            launchConfirmationFragment(String.valueOf(aboveThreshold));
            if (isHomeViewEnabled) {
                pauseCameraScan();
            } else {
                isReadyToScan = false;
            }
            resetHomeScreen();
            compareResultList.clear();
        } else {
            ShowLauncherView();
        }
    }

    private boolean isScanWithMaskEnforced() {
        boolean result = true;
        String triggerType = CameraController.getInstance().getTriggerType();
        if ((GestureController.getInstance().isGestureEnabledAndDeviceConnected()
                && AppSettings.isMaskEnforced() && !resumedFromGesture
                && triggerType.equals(CameraController.triggerValue.CAMERA.toString()))) {
            result = false;
        }
        return result;
    }

    private boolean isTopFragmentGesture() {
        boolean result = false;
        Fragment gFragment = getFragmentManager().findFragmentByTag("GestureFragment");
        Fragment mFragment = getFragmentManager().findFragmentByTag("MaskEnforceFragment");
        if ((gFragment != null && gFragment.isVisible()) ||
                mFragment != null && mFragment.isVisible()) {
            result = true;
        }
        return result;
    }

    private boolean isTopFragmentSecondaryScreen() {
        boolean result = false;
        Fragment fragment = getFragmentManager().findFragmentByTag("SecondaryIdentificationFragment");
        if ((fragment != null && fragment.isVisible())) {
            result = true;
        }
        return result;
    }

    private String getTemperatureValue(boolean aboveThreshold) {
        int tempValue = 0;
        String tempValueStr = "";
        if (aboveThreshold) {
            if (AppSettings.isPrintLabelHighTemperature()) {
                tempValue = (int) (TemperatureController.getInstance().getTemperature() * 10);
                tempValueStr = String.format("%4s", tempValue).replace(' ', '0');
            }
        } else {
            if (AppSettings.isPrintLabelNormalTemperature()) {
                tempValue = (int) (TemperatureController.getInstance().getTemperature() * 10);
                tempValueStr = String.format("%4s", tempValue).replace(' ', '0');
            }
        }
        return tempValueStr;
    }

    private void onGestureLanguageUpdate(String languageType) {
        runOnUiThread(() -> {
            Toast.makeText(IrCameraActivity.this, getString(R.string.gesture_launch_msg), Toast.LENGTH_SHORT).show();
            GestureController.getInstance().onGestureLanguageChange(languageType);
            recreate();
        });
    }

    private void updateGestureOnLanguageChange() {
        if ((AppSettings.isEnableHandGesture() && Util.isGestureDeviceConnected(this)) || AppSettings.isEnableTouchMode()) {
            if ((relative_main.getVisibility() == View.GONE) ||
                    (AccessCardController.getInstance().getTapCount() != 0) ||
                    (CameraController.getInstance().getTriggerType().equals(CameraController.triggerValue.CODEID.toString()))) {
                GestureController.getInstance().cancelWaveHandTimer();
                return;
            }
            if (GestureController.getInstance().isLanguageUpdated()) {
                GestureController.getInstance().setCallback(true);
                GestureController.getInstance().setAnswerType(GestureController.AnswerType.Wave);
                onGestureDetected();
            } else {
                if (GestureController.getInstance().getLanguageSelectionIndex() != 0) {
                    String msg = String.format(getString(R.string.updated_language_msg),
                            GestureController.getInstance().getUpdatingLanguageName());
                    if (!GestureController.getInstance().getUpdatingLanguageName().isEmpty()) {
                        Toast.makeText(IrCameraActivity.this, msg, Toast.LENGTH_LONG).show();
                        GestureController.getInstance().setCallback(false);
                    }
                }
            }
        }
    }

    private void resetImageLogo() {
        runOnUiThread(() -> {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) img_logo.getLayoutParams();
            params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            params.addRule(RelativeLayout.CENTER_IN_PARENT);
            params.setMargins(0, 230, 0, 0);
            img_logo.setLayoutParams(params);
        });
    }

    private void setLayoutMargins() {
        RelativeLayout.LayoutParams tvSubTitleParams = (RelativeLayout.LayoutParams) tv_thermal_subtitle.getLayoutParams();
        tvSubTitleParams.bottomMargin = (int) Util.convertPixelsToDp(10, this);
        tv_thermal_subtitle.setMaxLines(2);
        tv_thermal_subtitle.setEllipsize(TextUtils.TruncateAt.END);
        String text = tv_thermal.getText().toString();
        if (text.length() > 100) {
            tv_thermal_subtitle.setTextSize(22);
        }
    }

    private void initScan() {
        if (AppSettings.getTimeAndAttendance() == 0) {
            if ((GestureController.getInstance().isGestureEnabledAndDeviceConnected() && AppSettings.isFacialDetect()) || AppSettings.isEnableTouchMode()) {
                CameraController.getInstance().setScanProcessState(CameraController.ScanProcessState.FIRST_SCAN);
                return;
            }
        }
        int primaryIdentifier = AppSettings.getPrimaryIdentifier();
        if (primaryIdentifier != CameraController.PrimaryIdentification.NONE.getValue()) {
            if (primaryIdentifier == CameraController.PrimaryIdentification.FACE_OR_RFID.getValue()) {
                enableRfidScan();
                enableFaceScan();
                isReadyToScan = true;
            } else if (primaryIdentifier == CameraController.PrimaryIdentification.QRCODE_OR_RFID.getValue()) {
                enableRfidScan();
                enableQrCodeScan();
                isReadyToScan = false;
            } else if (primaryIdentifier == CameraController.PrimaryIdentification.FACE.getValue()) {
                enableFaceScan();
                isReadyToScan = true;
            } else if (primaryIdentifier == CameraController.PrimaryIdentification.RFID.getValue()) {
                enableRfidScan();
                isReadyToScan = false;
            } else if (primaryIdentifier == CameraController.PrimaryIdentification.QR_CODE.getValue()) {
                enableQrCodeScan();
                isReadyToScan = false;
            }
            CameraController.getInstance().setScanProcessState(CameraController.ScanProcessState.FIRST_SCAN);
        } else {
            enableFaceScan();
            isReadyToScan = true;
        }
    }

    private void initSecondaryScan() {
        if (AppSettings.getSecondaryIdentifier() == CameraController.SecondaryIdentification.QRCODE_OR_RFID.getValue()) {
            enableQrCodeScan();
            enableRfidScan();
            isReadyToScan = false;
        } else if (AppSettings.getSecondaryIdentifier() == CameraController.SecondaryIdentification.FACE.getValue()) {
            isReadyToScan = true;
        } else if (AppSettings.getSecondaryIdentifier() == CameraController.SecondaryIdentification.RFID.getValue()) {
            displaySecondaryIdentificationScreen();
            enableRfidScan();
            isReadyToScan = false;
        } else if (AppSettings.getSecondaryIdentifier() == CameraController.SecondaryIdentification.QR_CODE.getValue()) {
            closeCameraPreview();
            if (AppSettings.isAskQrCodeAlwaysEnabled() || AppSettings.isAnonymousQREnable()) {
                enableQrCodeScan();
                isReadyToScan = false;
            } else {
                if (registeredMemberslist != null) {
                    RegisteredMembers registeredMember = registeredMemberslist.get(0);
                    if (registeredMember != null) {
                        isReadyToScan = false;
                        if (MemberSyncDataModel.getInstance().isMemberTypeExists(registeredMemberslist)) {
                            enableQrCodeScan();
                        } else {
                            QrCodeController.getInstance().setListener(this);
                            QrCodeController.getInstance().getLastCheckInTime(this, registeredMember.uniqueid);
                        }
                    }
                } else {
                    isReadyToScan = false;
                    enableQrCodeScan();
                }
            }
        }
        CameraController.getInstance().setScanProcessState(CameraController.ScanProcessState.SECOND_SCAN);
        setSecondaryIdentificationTimer();
    }

    private void enableFaceScan() {
        initRgbCamera();
        initIrCamera();
        AccessCardController.getInstance().setCallbackListener(this);
    }

    private void disableFaceScan() {
        isReadyToScan = false;
    }

    private void enableRfidScan() {
        initAccessControl();
    }

    private void disableRfidScan() {
        isReadyToScan = true;
    }

    private void enableQrCodeScan() {
        clearLeftFace(null);
        resetQrCode();
    }

    private void disableQrCodeScan() {
        isReadyToScan = false;
    }

    private boolean checkSecondaryIdentifier() {
        CameraController.ScanProcessState scanProcessState =
                CameraController.getInstance().getScanProcessState();
        if ((scanProcessState == CameraController.ScanProcessState.SECOND_SCAN) ||
                (scanProcessState == CameraController.ScanProcessState.SECOND_SCAN_COMPLETE) ||
                GestureController.getInstance().isGestureEnabledAndDeviceConnected()) {
            return false;
        }
        boolean result = false;

        if (AppSettings.getPrimaryIdentifier() == CameraController.PrimaryIdentification.FACE_OR_RFID.getValue()) {
            if (AppSettings.getSecondaryIdentifier() == CameraController.SecondaryIdentification.QR_CODE.getValue()) {
                result = true;
            }
        } else if (AppSettings.getPrimaryIdentifier() == CameraController.PrimaryIdentification.QRCODE_OR_RFID.getValue()) {
            if (AppSettings.getSecondaryIdentifier() == CameraController.SecondaryIdentification.FACE.getValue()) {
                result = false;
                isReadyToScan = true;
            }
        } else if (AppSettings.getPrimaryIdentifier() == CameraController.PrimaryIdentification.FACE.getValue()) {
            if ((AppSettings.getSecondaryIdentifier() != CameraController.SecondaryIdentification.FACE.getValue()) &&
                    (AppSettings.getSecondaryIdentifier() != CameraController.SecondaryIdentification.NONE.getValue())) {
                result = true;
            }
        } else if (AppSettings.getPrimaryIdentifier() == CameraController.PrimaryIdentification.RFID.getValue()) {
            if (AppSettings.getSecondaryIdentifier() == CameraController.SecondaryIdentification.QR_CODE.getValue()) {
                result = true;
            }
        } else if (AppSettings.getPrimaryIdentifier() == CameraController.PrimaryIdentification.QR_CODE.getValue()) {
            if (AppSettings.getSecondaryIdentifier() == CameraController.SecondaryIdentification.RFID.getValue()) {
                result = true;
            }
        }

        if (result) {
            CameraController.getInstance().setScanProcessState(CameraController.ScanProcessState.SECOND_SCAN);
        }
        return result;
    }

    private void displaySecondaryIdentificationScreen() {
        secondaryScreenFragment = new SecondaryIdentificationFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.add(R.id.dynamic_fragment_frame_layout, secondaryScreenFragment, "SecondaryIdentificationFragment");
        transaction.addToBackStack("SecondaryIdentificationFragment");
        transaction.commitAllowingStateLoss();
    }

    private void setSecondaryIdentificationTimer() {
        if (imageTimer != null) {
            imageTimer.cancel();
            imageTimer = null;
        }
        imageTimer = new Timer();
        imageTimer.schedule(new TimerTask() {
            public void run() {
                disableLedPower();
                resetToHomePage();
            }
        }, 10 * 1000); //wait 10 seconds for the secondary scan, go to home otherwise
    }

    private void resetHomePage() {
        if (CameraController.getInstance().isAppExitTriggered()) {
            if (handler != null) {
                handler.obtainMessage(CameraController.IMAGE_PROCESS_COMPLETE).sendToTarget();
            }
            return;
        }

        Logger.debug(TAG, "showCameraPreview", "ImageTimer execute, isFaceIdentified:" + isFaceIdentified);

        isFaceNotDetectedTimer = false;
        tv_message.setText("");
        tv_message.setVisibility(View.GONE);
        tvErrorMessage.setVisibility(View.GONE);
        tvFaceMessage.setVisibility(View.GONE);
        temperature_image.setVisibility(View.GONE);
        homeDisplayView();
        mask_message.setText("");
        mask_message.setVisibility(View.GONE);
        disableLedPower();
        resetToHomePage();
    }

    private void resetToHomePage() {
        runOnUiThread(() -> {
            resetAcknowledgementScreen();
            clearData();
            resetHomeScreen();
            resetRfid();
            clearQrCode();
            resetGesture();
            initScan();
            DisplayTimeAttendance();
        });
    }

    private void updateOnTemperatureScanDisabled() {
        disableLedPower();
        TemperatureController.getInstance().updateControllersOnTempScanDisabled(registeredMemberslist);
        runOnUiThread(() -> {
            closeGestureFragment();
            closeFragment(acknowledgementFragment);
            launchConfirmationFragment(String.valueOf(false));
            if (isHomeViewEnabled) {
                pauseCameraScan();
            }
            resetHomeScreen();
        });
    }

    private void closeCameraPreview() {
        disableLedPower();
        relative_main.setVisibility(View.VISIBLE);
    }

    private void closeGestureFragment() {
        if (gestureFragment != null) {
            if (isDestroyed() || isFinishing() || !isActivityResumed) return;
            try {
                getFragmentManager().beginTransaction().remove(gestureFragment).commitAllowingStateLoss();
            } catch (Exception e) {
                Log.d(TAG, "Error in closing Gesture fragment");
            }
        }
    }

    private void DisplayTimeAttendance() {
        try {
            //updateHealthCheckUI();
            if (AppSettings.getTimeAndAttendance() == 1) {
                relative_main.setVisibility(View.GONE);
                time_attendance_layout.setVisibility(View.VISIBLE);
                String currentTime = new SimpleDateFormat("HH:mm aaa", Locale.getDefault()).format(new Date());
                tvTime.setText(currentTime);
                String currentDate = new SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()).format(new Date());
                tvDate.setText(currentDate);
                if (isHomeViewEnabled) {
                    pauseCameraScan();
                } else {
                    isReadyToScan = false;
                }
            } else if (AppSettings.getTimeAndAttendance() == 0) {
                if (Util.isDeviceF10()) {
                    updateUI();
                } else {
                    homeDisplayView();
                }
            } else {
                new Handler().postDelayed(() -> {
                    //logo.setVisibility(View.GONE);
                    relative_main.setVisibility(View.GONE);
                    changeVerifyBackground(R.color.colorTransparency, true);
                }, 150);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onCheckInClick(View view) {
        ApplicationController.getInstance().setTimeAttendance(1);
        AccessCardController.getInstance().setCheckInResponseCode(-1);
        time_attendance_layout.setVisibility(View.GONE);
        homeDisplayView();
        faceEngineHelper.initEngine(this);
        if (GestureController.getInstance().isGestureEnabledAndDeviceConnected() || AppSettings.isEnableTouchMode()) {
            int primaryIdentifier = AppSettings.getPrimaryIdentifier();
            if ((primaryIdentifier == CameraController.PrimaryIdentification.QR_CODE.getValue()) ||
                    (primaryIdentifier == CameraController.PrimaryIdentification.QRCODE_OR_RFID.getValue())) {
                initScan();
            }
        } else {
            initScan();
        }
        GestureController.getInstance().setCallback(false);
    }

    public void onCheckOutClick(View view) {
        GestureController.getInstance().setCallback(true);
        ApplicationController.getInstance().setTimeAttendance(2);
        time_attendance_layout.setVisibility(View.GONE);
        homeDisplayView();
        faceEngineHelper.initEngine(this);
        int primaryIdentifier = AppSettings.getPrimaryIdentifier();
        if ((primaryIdentifier == CameraController.PrimaryIdentification.QR_CODE.getValue())) {
            clearQrCode();
            enableFaceScan();
            isReadyToScan = true;
            CameraController.getInstance().setScanProcessState(CameraController.ScanProcessState.FIRST_SCAN);
        } else if (primaryIdentifier == CameraController.PrimaryIdentification.QRCODE_OR_RFID.getValue()) {
            clearQrCode();
            enableRfidScan();
            enableFaceScan();
            isReadyToScan = true;
            CameraController.getInstance().setScanProcessState(CameraController.ScanProcessState.FIRST_SCAN);
        } else {
            initScan();
        }
    }

    private void setCameraLayout() {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) img_logo.getLayoutParams();
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        params.setMargins(0, (int) Util.convertDpToPixel(20, this), 0, 0);
    }

    private void updateHealthCheckUI() {
        String appRestartTime = ApplicationController.getInstance().getAppRestartTime();
        if (sharedPreferences.getBoolean(GlobalParameters.HEALTH_CHECK_OFFLINE, false) && !appRestartTime.isEmpty()) {
            if (internetIndicatorImg != null) {
                internetIndicatorImg.setVisibility(View.VISIBLE);
            }
            tvErrorMessage.setVisibility(View.VISIBLE);
            tvErrorMessage.setTextSize(22);
            tvVersionIr.setVisibility(View.GONE);
            tvVersionOnly.setVisibility(View.GONE);
            tvErrorMessage.setText(String.format(getString(R.string.health_check_failed_msg), appRestartTime));
        }
    }

    @Override
    public void onHealthCheckNoResponse(int min, int sec) {
        runOnUiThread(() -> {
            if (internetIndicatorImg != null) {
                internetIndicatorImg.setVisibility(View.VISIBLE);
            }
            tvErrorMessage.setVisibility(View.VISIBLE);
            tvErrorMessage.setTextSize(22);
            tvVersionIr.setVisibility(View.GONE);
            tvVersionOnly.setVisibility(View.GONE);
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MINUTE, min);
            calendar.add(Calendar.SECOND, sec);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm:ss a", Locale.getDefault());
            String appRestartTime = simpleDateFormat.format(calendar.getTime());
            ApplicationController.getInstance().setAppRestartTime(appRestartTime);
            tvErrorMessage.setText(String.format(getString(R.string.health_check_failed_msg), appRestartTime));
            Util.writeBoolean(sharedPreferences, GlobalParameters.HEALTH_CHECK_OFFLINE, true);
        });
    }

    @Override
    public void onHealthCheckTimeout() {
        Util.writeBoolean(sharedPreferences, GlobalParameters.HEALTH_CHECK_OFFLINE, false);
        stopServices();
        restartApplication();
    }

    @Override
    public void onHealthCheckTimerCancelled() {
        runOnUiThread(() -> {
            tvErrorMessage.setVisibility(View.GONE);
            tvErrorMessage.setTextSize(28);
        });
    }

    private void showGuideMessage(String message) {
        tvErrorMessage.setVisibility(View.VISIBLE);
        tvErrorMessage.setTextSize(28);
        tvErrorMessage.setText(message);
        if (Util.isDeviceF10()) {
            tvVersionIr.setVisibility(View.GONE);
            tvVersionOnly.setVisibility(View.GONE);
        }
    }

    private void isAlreadyCheckEdOut() {
        if (AccessCardController.getInstance().getCheckInResponseCode() == AccessCardController.AccessCheckInOutStatus.RESPONSE_CODE_FAILED.getValue())
            Toast.makeText(IrCameraActivity.this, getResources().getString(R.string.please_try_again), Toast.LENGTH_LONG).show();
        else if (ApplicationController.getInstance().getTimeAttendance() == 1)
            Toast.makeText(IrCameraActivity.this, getResources().getString(R.string.already_check_in), Toast.LENGTH_LONG).show();
        else if (ApplicationController.getInstance().getTimeAttendance() == 2)
            Toast.makeText(IrCameraActivity.this, getResources().getString(R.string.already_check_out), Toast.LENGTH_LONG).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isHomeViewEnabled) {
                    pauseCameraScan();
                } else {
                    isReadyToScan = false;
                }
                resetHomeScreen();
            }
        }, 3000);
    }

    private boolean validateCheckInCheckOut(RegisteredMembers matchedMember) {
        if (AccessCardController.getInstance().isCheckedIn(matchedMember)) {
            runOnUiThread(() -> {
                Toast.makeText(IrCameraActivity.this, getResources().getString(R.string.already_check_in), Toast.LENGTH_LONG).show();
                disableLedPower();
                resetToHomePage();
            });
            return true;
        }
        if (AccessCardController.getInstance().isCheckedOut(matchedMember)) {
            runOnUiThread(() -> {
                Toast.makeText(IrCameraActivity.this, getResources().getString(R.string.already_check_out), Toast.LENGTH_LONG).show();
                disableLedPower();
                resetToHomePage();
            });
            return true;
        }
        AccessCardController.getInstance().setCheckedInOutMember(matchedMember);
        return false;
    }

    private void updateUI() {
        if (rfIdEnable) {
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) tv_thermal.getLayoutParams();
            layoutParams.bottomMargin = (int) Util.convertDpToPixel(200, this);
        } else if (qrCodeEnable) {
            setCameraLayout();
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) tv_thermal_subtitle.getLayoutParams();
            layoutParams.bottomMargin = (int) Util.convertDpToPixel(100, this);
            RelativeLayout.LayoutParams tvThermalParams = (RelativeLayout.LayoutParams) tv_thermal.getLayoutParams();
            tvThermalParams.bottomMargin = (int) Util.convertDpToPixel(210, this);
        } else {
            setCameraLayout();
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) tv_thermal_subtitle.getLayoutParams();
            layoutParams.bottomMargin = (int) Util.convertDpToPixel(90, this);
        }
        CameraController.getInstance().setScanState(CameraController.ScanState.FACIAL_SCAN);
        changeVerifyBackground(R.color.colorTransparency, true);
        relative_main.setVisibility(View.VISIBLE);
        homeIcon(sharedPreferences.getString(GlobalParameters.IMAGE_ICON, ""));
        if (sharedPreferences.getBoolean(GlobalParameters.HOME_TEXT_IS_ENABLE, true)) {
            tvVersionIr.setVisibility(View.VISIBLE);
        } else if (sharedPreferences.getBoolean(GlobalParameters.HOME_TEXT_ONLY_IS_ENABLE, true)) {
            tvVersionOnly.setVisibility(View.VISIBLE);
        }
    }
}