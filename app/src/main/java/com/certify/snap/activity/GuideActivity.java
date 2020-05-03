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
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import com.arcsoft.face.ActiveFileInfo;
import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.VersionInfo;
import com.certify.snap.common.Application;
import com.certify.snap.common.Constants;
import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.Util;
import com.certify.snap.service.GuideService;
import com.google.gson.Gson;
import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.crashes.Crashes;
import com.romainpiel.titanic.library.Titanic;
import com.romainpiel.titanic.library.TitanicTextView;
import com.tamic.novate.Novate;
import com.certify.snap.faceserver.FaceServer;


import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import com.certify.snap.R;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class GuideActivity extends Activity {

    public static final String TAG  = "GuideActivity";
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
    private GuideService.MyBinder myBinder;
    private SharedPreferences sp;

    boolean libraryExists = true;
    // Demo
    private static final String[] LIBRARIES = new String[]{
            "libarcsoft_face_engine.so",
            "libarcsoft_face.so",
            "libarcsoft_image_util.so",
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCenter.start(getApplication(), "bb348a98-dbeb-407f-862d-3337632c4e0e",
                Analytics.class, Crashes.class);
        AppCenter.start(getApplication(), "bb348a98-dbeb-407f-862d-3337632c4e0e",
                Analytics.class, Crashes.class);
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
//        sp = Util.getSharedPreferences(this);
//        if(sp.getBoolean("activate",false)) {
//            Log.e("sp---true","activate:"+sp.getBoolean("activate",false));
//        }else{
//            activeEngine(null);
//            Log.e("sp---false","activate:"+sp.getBoolean("activate",false));
//        }
        if(!isInstalled(GuideActivity.this,"com.telpo.temperatureservice")){
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

        boolean navigationBar = Util.getSharedPreferences(GuideActivity.this).getBoolean(GlobalParameters.NavigationBar,true);
        boolean statusBar = Util.getSharedPreferences(GuideActivity.this).getBoolean(GlobalParameters.StatusBar,true);

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

    private void checkStatus(){
        checkPermission();
        libraryExists = checkSoFile(LIBRARIES);
        ApplicationInfo applicationInfo = getApplicationInfo();
        Log.e(TAG, "onCreate: " + applicationInfo.nativeLibraryDir);
        if (!libraryExists) {
            Toast.makeText(this,getString(R.string.library_not_found),Toast.LENGTH_SHORT).show();
            finish();
        }else {
            VersionInfo versionInfo = new VersionInfo();
            int code = FaceEngine.getVersion(versionInfo);
            Log.e(TAG, "onCreate: getVersion, code is: " + code + ", versionInfo is: " + versionInfo);
        }
    }

    /**
     *
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
     *
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
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //FaceServer.getInstance().init(GuideActivity.this);
                Util.switchRgbOrIrActivity(GuideActivity.this,true);
            }
        },1000);

    }
    public void activeEngine(final View view) {
        if (view != null) {
            view.setClickable(false);
        }
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                int activeCode = faceEngine.activeOnline(GuideActivity.this, Constants.APP_ID, Constants.SDK_KEY);
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
                          //  Util.showToast(SettingActivity.this,getString(R.string.active_success));
                            Util.writeBoolean(sp,"activate",true);
                         //   show();
                        } else if (activeCode == ErrorInfo.MERR_ASF_ALREADY_ACTIVATED) {
                          //  Util.showToast(SettingActivity.this,getString(R.string.already_activated));
                            Util.writeBoolean(sp,"activate",true);
                           // show();
                        } else {
                          //  Util.showToast(SettingActivity.this,getString(R.string.active_failed, activeCode));
                            Util.writeBoolean(sp,"activate",false);
                         //   hide();
                        }

                        if (view != null) {
                            view.setClickable(true);
                        }
                        ActiveFileInfo activeFileInfo = new ActiveFileInfo();
                        int res = faceEngine.getActiveFileInfo(GuideActivity.this, activeFileInfo);
                        if (res == ErrorInfo.MOK) {
                            Log.e("activate---", activeFileInfo.toString());
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }
}
