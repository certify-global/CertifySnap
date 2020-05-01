package com.certify.snap.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import com.arcsoft.imageutil.ArcSoftImageFormat;
import com.arcsoft.imageutil.ArcSoftImageUtil;
import com.arcsoft.imageutil.ArcSoftImageUtilError;
import com.certify.snap.activity.InitializationActivity;
import com.google.gson.Gson;
import com.tamic.novate.Novate;
import com.tamic.novate.Throwable;
import com.tamic.novate.callback.RxFileCallBack;
import com.tamic.novate.callback.RxStringCallback;
import com.certify.snap.bean.ImageInfo;
import com.certify.snap.bean.MemberList;
import com.certify.snap.bean.MemberUpdate;
import com.certify.snap.bean.Members;
import com.certify.snap.common.Application;
import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.Util;
import com.certify.snap.faceserver.FaceServer;
import com.certify.snap.model.RegisteredFailedMembers;
import com.certify.snap.model.RegisteredMembers;

import org.litepal.LitePal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UpdateService extends Service {

    private static SharedPreferences sp;
    private static Gson gson;
    private ExecutorService fixedThreadPool;
    private ExecutorService singleThreadPool = Executors.newSingleThreadExecutor();
    private final Object obj = new Object();
    private boolean isUpdateManual = false;
    private boolean isDeleteManual = false;
    private List<ImageInfo> imageInfosList;
    private List<File> imagesList;
    private List<File> filesList;
    private boolean isContinue = true;
    private boolean isSuccess = false;
    private Long serverTime = (long) 0;
    private static UpdateDataListener mUpdateDataListener;
    private static DeleteDataListener mDeleteDataListener;
    private Novate mNovate;
    private List<Members> updateMembersList;
    private UpdateReceiver receiver;

    private String ROOT_PATH_STRING;
    private static final String UPDATE_IMAGE_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Telpo_face" + File.separator + "Update Image";

    public static final String UPDATE = "com.telpo.telpo_face_system_update";
    public static final String DELETE = "com.telpo.telpo_face_system_delete";
    public static final String TURN_ON_TIMING = "com.telpo.telpo_face_system_turn_on_timing";
    public static final String TURN_OFF_TIMING = "com.telpo.telpo_face_system_turn_off_timing";
    public static final String LIVE_UPDATE = "com.telpo.telpo_face_system_live_update";
    public static final String LIVE_DELETE = "com.telpo.telpo_face_system_live_delete";

    private Timer timer, liveUpdateTimer, liveDeleteTimer;
    private TimerTask timerTask, liveUpdateTimerTask, liveDeleteTImerTask;

    private int successDownloadImagesCount = 0;
    private int failedDownloadImagesCount = 0;
    private int totalUpdateCount;
    private int processSuccessCount;
    private int totalUpdateSuccessCount;
    private int updateProcessFailCount;
    private String updateId;
    private List<Members> updateList;
    private List<Members> registerList;

    @Override
    public void onCreate() {
//        if (BuildConfig.DEBUG) {
//            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
//                    .detectLeakedSqlLiteObjects()
//                    .detectLeakedClosableObjects()
//                    .penaltyLog()
//                    .penaltyDeath()
//                    .build());
//        }
        super.onCreate();
        Log.e("tag", "start update service");
        init();
    }

    private void init() {
        FaceServer.getInstance().init(this);//init FaceServer;
        sp = Util.getSharedPreferences(this);
        gson = new Gson();
        mNovate = Application.getInstance().getNovate();
        fixedThreadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1);
        imageInfosList = Collections.synchronizedList(new ArrayList<ImageInfo>());
        imagesList = Collections.synchronizedList(new ArrayList<File>());
        filesList = Collections.synchronizedList(new ArrayList<File>());
        updateList = Collections.synchronizedList(new ArrayList<Members>());
        registerList = Collections.synchronizedList(new ArrayList<Members>());

        receiver = new UpdateReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UPDATE);
        intentFilter.addAction(DELETE);
        intentFilter.addAction(TURN_ON_TIMING);
        intentFilter.addAction(TURN_OFF_TIMING);
        intentFilter.addAction(LIVE_UPDATE);
        intentFilter.addAction(LIVE_DELETE);
        registerReceiver(receiver, intentFilter);
        if (sp.getBoolean("isSelected", false)) {
            updateAuto();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Log.e("tag", "update service destroy");
        FaceServer.getInstance().unInit();
        cancelTimer();
        cancelTimerTask();
        if (fixedThreadPool != null && !fixedThreadPool.isShutdown()) {
            fixedThreadPool.shutdownNow();
        }
        if (singleThreadPool != null && !singleThreadPool.isShutdown()) {
            singleThreadPool.shutdownNow();
        }
        if (receiver != null) {
            unregisterReceiver(receiver);
        }
        removeUpdateDataListener();
        removeDeleteDataListener();
        super.onDestroy();
    }

    private void updateAuto() {
        if (timer == null) {
            timer = new Timer();
        }
        if (timerTask == null) {
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    update(isUpdateManual);
                    delete(isDeleteManual);
                }
            };
            timer.schedule(timerTask, 5000, 10 * 60 * 1000);
        }
        Log.e("tag", "timer start----");
    }

    private void liveUpdate(final String s) {
        if (s == null)
            return;
        if (s.equals(LIVE_UPDATE)) {
            Log.e("tag","liveUpdateTimer start");
                if (liveUpdateTimer == null) {
                    liveUpdateTimer = new Timer();
                }
                if (liveUpdateTimerTask == null) {
                    liveUpdateTimerTask = new TimerTask() {
                        @Override
                        public void run() {
                            update(isUpdateManual);
                        }
                    };
                    liveUpdateTimer.schedule(liveUpdateTimerTask, 0, 30*1000);
                }
        } else if (s.equals(LIVE_DELETE)) {
            Log.e("tag","liveDeleteTimer start");
                if (liveDeleteTimer == null) {
                    liveDeleteTimer = new Timer();
                }
                if (liveDeleteTImerTask == null) {
                    liveDeleteTImerTask = new TimerTask() {
                        @Override
                        public void run() {
                            delete(isUpdateManual);
                        }
                    };
                    liveDeleteTimer.schedule(liveDeleteTImerTask, 0, 30*1000);
                }
        }
    }

    private void cancelTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (liveUpdateTimer != null) {
            liveUpdateTimer.cancel();
            liveUpdateTimer = null;
        }
        if (liveDeleteTimer != null) {
            liveDeleteTimer.cancel();
            liveDeleteTimer = null;
        }
        Log.e("tag", "timer cancel---");
    }

    private void cancelTimerTask() {
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
        if (liveUpdateTimerTask != null) {
            liveUpdateTimerTask.cancel();
            liveUpdateTimerTask = null;
        }
        if (liveDeleteTImerTask != null) {
            liveDeleteTImerTask.cancel();
            liveDeleteTImerTask = null;
        }
    }

    private class UpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case UPDATE:
                        updateId = intent.getStringExtra("userId");
                        update(true);
                        break;
                    case DELETE:
                        delete(true);
                        break;
                    case TURN_ON_TIMING:
                        isUpdateManual = false;
                        isDeleteManual = false;
                        updateAuto();
                        break;
                    case TURN_OFF_TIMING:
                        cancelTimer();
                        cancelTimerTask();
                        break;
                    case LIVE_UPDATE:
                        liveUpdate(LIVE_UPDATE);
                        break;
                    case LIVE_DELETE:
                        liveUpdate(LIVE_DELETE);
                        break;
                }
            }
        }
    }

    private void update(boolean manual) {
        Log.e("tag", "start to update----");
        isUpdateManual = manual;
        if (sp.getBoolean("isInit", false)) {
            if (sp.getLong("timeStamp", 0) != 0) {
                String timeStamp = Long.toString(sp.getLong("timeStamp", 0));
                getUpdateList(timeStamp);
            }
        } else {
            Log.e("tag", "database is not init");
            if (isUpdateManual) {
                mUpdateDataListener.onUpdate(true, true, false, 0);
                isUpdateManual = false;
            }
        }
    }

    private void getUpdateList(String timeStamp) {
        if (timeStamp == null)
            return;
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("access_token", GlobalParameters.Access_token);
        parameters.put("sn", GlobalParameters.SN);
        parameters.put("updatetime", timeStamp);
        mNovate.rxPost(GlobalParameters.UpdateUserListURL, parameters, new RxStringCallback() {
            @Override
            public void onError(Object tag, Throwable e) {
                Log.e("onError---", "getupdate error:" + e.getMessage());
                if (isUpdateManual) {
                    mUpdateDataListener.onUpdate(true, false, false, 0);
                    isUpdateManual = false;
                }
            }

            @Override
            public void onCancel(Object tag, Throwable e) {

            }

            @Override
            public void onNext(Object tag, String response) {
                Log.e("onNext---", "getupdate response:" + response);
                try {
                    MemberUpdate memberUpdate = gson.fromJson(response, MemberUpdate.class);
                    updateMembersList = memberUpdate.getData();
                    serverTime = memberUpdate.getServertime();
                    if (updateMembersList != null && updateMembersList.size() > 0) {
                        downloadImage();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("tag", "no data to update");
                    if (isUpdateManual) {
                        mUpdateDataListener.onUpdate(false, true, false, 0);
                        isUpdateManual = false;
                    }
                }
            }
        });
    }

    private void downloadImage() {
        if (fixedThreadPool != null && !fixedThreadPool.isShutdown()) {
            fixedThreadPool.execute(new DownloadImageThread());
            Log.e("tag", "start to download image");
        }
    }

    class DownloadImageThread extends Thread {
        @Override
        public void run() {
            int updateCount = updateMembersList.size();
            Log.e("tag", "start to download");
            if (imagesList.size() == 0) {
                try {
                    Members member;
                    String imageUrl;
                    String fileName;
                    ImageInfo imageInfo;
                    synchronized (obj) {
                        for (int i = 0; i < updateCount; i++) {
                            member = updateMembersList.get(i);
                            imageUrl = member.getImage();
                            fileName = member.getName() + "-" + member.getUserId() + ".jpg";
                            imageInfo = new ImageInfo();
                            imageInfo.setImageUrl(imageUrl);
                            imageInfo.setFileName(fileName);
                            imageInfosList.add(imageInfo);
                            getImage(imageUrl, fileName);
                        }
                    }
                    while (imagesList.size() != updateCount) {
                        Thread.sleep(100);
                        if (successDownloadImagesCount + failedDownloadImagesCount == updateCount && failedDownloadImagesCount != 0) {
                            successDownloadImagesCount = 0;
                            failedDownloadImagesCount = 0;
                            if (isUpdateManual) {
                                mUpdateDataListener.onUpdate(true, false, false, 0);
                                isUpdateManual = false;
                            }
                            Log.e("tag", "network error----");
                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (isUpdateManual) {
                        mUpdateDataListener.onUpdate(true, false, false, 0);
                        isUpdateManual = false;
                    }
                }
            } else if (imagesList.size() > 0 && imagesList.size() < updateCount) {
                try {
                    int infosCount = imageInfosList.size();
                    if (infosCount != 0) {
                        Log.e("tag", "imageInfoList_size-----" + infosCount);
                        synchronized (obj) {
                            for (ImageInfo imageInfo : imageInfosList) {
                                getImage(imageInfo.getImageUrl(), imageInfo.getFileName());
                            }
                        }
                        while (imagesList.size() != updateCount) {
                            Thread.sleep(100);
                            if (successDownloadImagesCount + failedDownloadImagesCount == infosCount && failedDownloadImagesCount != 0) {
                                successDownloadImagesCount = 0;
                                failedDownloadImagesCount = 0;
                                if (isUpdateManual) {
                                    mUpdateDataListener.onUpdate(true, false, false, 0);
                                    isUpdateManual = false;
                                }
                                Log.e("tag", "network error----");
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (isUpdateManual) {
                        mUpdateDataListener.onUpdate(true, false, false, 0);
                        isUpdateManual = false;
                    }
                }
            } else if (imagesList.size() == updateCount) {
                Log.e("tag", "You have downloaded all the pictures" + "----" + updateMembersList.size());
                imagesList.clear();
                imageInfosList.clear();
                successDownloadImagesCount = 0;
                failedDownloadImagesCount = 0;
                doProcess();
            }
        }
    }

    private void getImage(final String url, final String fileName) {
        if (url != null && fileName != null) {
            mNovate.rxDownload(url, new RxFileCallBack(
                    UPDATE_IMAGE_DIR, fileName) {
                @Override
                public void onStart(Object tag) {
                    super.onStart(tag);
                }

                @Override
                public void onNext(Object tag, final File file) {
                    if (fixedThreadPool != null && !fixedThreadPool.isShutdown()) {
                        fixedThreadPool.execute(new Runnable() {
                            public void run() {
                                synchronized (obj) {
                                    if (imageInfosList.size() > 0) {
                                        ListIterator imageInfosListIterator = imageInfosList.listIterator();
                                        ImageInfo imageInfo;
                                        while (imageInfosListIterator.hasNext()) {
                                            imageInfo = (ImageInfo) imageInfosListIterator.next();
                                            if (imageInfo.getFileName().equals(fileName)) {
                                                imageInfosListIterator.remove();
                                                Log.e("tag", "remove---" + imageInfo.getFileName());
                                                break;
                                            }
                                        }
                                    }
                                    successDownloadImagesCount += 1;
                                    imagesList.add(file);
                                    Log.e("onnext---", "success" + "----" + fileName + "imagesList.size----" + imagesList.size());
                                }
                            }
                        });
                    }
                }

                @Override
                public void onProgress(Object tag, float progress, long downloaded, long total) {
                }

                @Override
                public void onProgress(Object tag, int progress, long speed, long transfered, long total) {
                    super.onProgress(tag, progress, speed, transfered, total);
                }

                @Override
                public void onError(Object tag, Throwable e) {
                    synchronized (obj) {
                        failedDownloadImagesCount += 1;
                        Log.e("onError---", e.toString() + "-" + e.getMessage());
                    }
                }

                @Override
                public void onCancel(Object tag, Throwable e) {

                }

                @Override
                public void onCompleted(Object tag) {
                    super.onCompleted(tag);
                    //Log.e("onCompleted---","success");
                }
            });
        } else {
            if (isUpdateManual) {
                mUpdateDataListener.onUpdate(true, true, false, 0);
                isUpdateManual = false;
            }
        }
    }

    private void doProcess() {
        File dir = new File(UPDATE_IMAGE_DIR);
        if (!dir.exists()) {
            Log.e("tag", "dir error");
            if (isUpdateManual) {
                mUpdateDataListener.onUpdate(true, true, false, 0);
                isUpdateManual = false;
            }
            return;
        }
        if (!dir.isDirectory()) {
            Log.e("tag", "dir error");
            if (isUpdateManual) {
                mUpdateDataListener.onUpdate(true, true, false, 0);
                isUpdateManual = false;
            }
            return;
        }
        final File[] imgFiles = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(FaceServer.IMG_SUFFIX);
            }
        });
        singleThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                totalUpdateCount = imgFiles.length;
                Log.e("tag", "imgFiles.length-----" + totalUpdateCount);
                processSuccessCount = 0;
                File registeredImageFile;
                for (int i = 0; i < totalUpdateCount; i++) {
                    final File jpgFile = imgFiles[i];
                    Bitmap bitmap = BitmapFactory.decodeFile(jpgFile.getAbsolutePath());
                    if (bitmap == null) {
                        Log.e("tag", "fail to get bitmap--");
                        File failedFile = new File(InitializationActivity.REGISTERED_FAILED_DIR + File.separator + jpgFile.getName());
                        if (!failedFile.getParentFile().exists()) {
                            if (failedFile.getParentFile().mkdirs()) {
                                Log.e("tag", "mkdirs");
                            }
                        }
                        moveFile(jpgFile, failedFile);
                        continue;
                    }
                    bitmap = ArcSoftImageUtil.getAlignedBitmap(bitmap, true);
                    if (bitmap == null) {
                        Log.e("tag", "fail to translate bitmap");
                        File failedFile = new File(InitializationActivity.REGISTERED_FAILED_DIR + File.separator + jpgFile.getName());
                        if (!failedFile.getParentFile().exists()) {
                            if (failedFile.getParentFile().mkdirs()) {
                                Log.e("tag", "mkdirs");
                            }
                        }
                        moveFile(jpgFile, failedFile);
                        continue;
                    }
                    byte[] bgr24 = ArcSoftImageUtil.createImageData(bitmap.getWidth(), bitmap.getHeight(), ArcSoftImageFormat.BGR24);
                    int transformCode = ArcSoftImageUtil.bitmapToImageData(bitmap, bgr24, ArcSoftImageFormat.BGR24);
                    if (transformCode != ArcSoftImageUtilError.CODE_SUCCESS) {
                        /*runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.dismiss();
                                tvNotificationRegisterResult.append("");
                            }
                        });
                        return;*/
                    }
                    boolean success = FaceServer.getInstance().registerBgr24(UpdateService.this, bgr24, bitmap.getWidth(), bitmap.getHeight(),
                            jpgFile.getName().substring(0, jpgFile.getName().lastIndexOf(".")));
                    if (!success) {
                        Log.e("tag", "fail to process bitmap");
                        File failedFile = new File(InitializationActivity.REGISTERED_FAILED_DIR + File.separator + jpgFile.getName());
                        if (!failedFile.getParentFile().exists()) {
                            if (failedFile.getParentFile().mkdirs()) {
                                Log.e("tag", "mkdirs");
                            }
                        }
                        moveFile(jpgFile, failedFile);
                    } else {
                        processSuccessCount++;
                        filesList.add(jpgFile);
                        deleteExistRegisteredImage(jpgFile.getName().substring(jpgFile.getName().lastIndexOf("-") + 1, jpgFile.getName().lastIndexOf(".")));
                        registeredImageFile = new File(InitializationActivity.REGISTERED_IMAGE_DIR + File.separator + jpgFile.getName());
                        moveFile(jpgFile, registeredImageFile);
                    }
                }
                isSuccess = (totalUpdateCount - processSuccessCount) <= 0;
                Log.e("tag", "success count----" + processSuccessCount);
                doUpdate();
            }
        });
    }

    private void doUpdate() {
        updateList.clear();
        registerList.clear();
        totalUpdateSuccessCount = 0;
        String id;
        List<RegisteredMembers> list;
        for (Members member : updateMembersList) {
            id = member.getUserId();
            list = LitePal.where("userId=?", id).find(RegisteredMembers.class);
            if (list != null && list.size() > 0) {
                // if(compareMember())....//如果待更新的信息与数据库中的一样，不执行重复更新
                if (updateId != null && updateId.equals(id)) {
                    //当情况为列表更新单项时，只更新对应的单项
                    updateList.clear();
                    registerList.clear();
                    updateList.add(member);
                    break;
                }
                updateList.add(member);
            } else {
                if (updateId != null && updateId.equals(id)) {
                    updateList.clear();
                    registerList.clear();
                    registerList.add(member);
                    break;
                }
                registerList.add(member);
            }
        }
        if (updateList.size() > 0) {
            Log.e("tag", "update member---");
            updateDatabase(updateList, true);
        }
        if (registerList.size() > 0) {
            Log.e("tag", "register member---");
            updateDatabase(registerList, false);
        }
        Log.e("tag", "total update success count---" + totalUpdateSuccessCount + "need to update count---" + (updateList.size() + registerList.size()));
        if (totalUpdateSuccessCount == (updateList.size() + registerList.size())) {
            filesList.clear();
            if (updateId == null) {
                sp.edit().putLong("timeStamp", serverTime).apply();
                //更新成功更新时间戳,同时取消timer
                if (liveUpdateTimer != null) {
                    liveUpdateTimer.cancel();
                    liveUpdateTimer = null;
                    Log.e("tag","liveUpdateTimer cancel");
                }
                if (liveUpdateTimerTask != null) {
                    liveUpdateTimerTask.cancel();
                    liveUpdateTimerTask = null;
                }
                Log.e("tag", "Batch update succeeded");
            }
            if (isUpdateManual) {
                if (updateId != null) {
                    updateId = null;
                    Log.e("tag", "single data update success");
                }
                mUpdateDataListener.onUpdate(true, true, true, updateProcessFailCount);
            }
        } else {
            if (isUpdateManual) {
                mUpdateDataListener.onUpdate(true, true, false, 0);
            }
        }
    }

    private void updateDatabase(List<Members> list, boolean isUpdate) {
        File imgsDir = new File(InitializationActivity.REGISTERED_IMAGE_DIR);
        if (!imgsDir.exists() || !imgsDir.isDirectory()) {
            Log.e("tag", "dir error");
            if (isUpdateManual) {
                mUpdateDataListener.onUpdate(true, true, false, 0);
                isUpdateManual = false;
            }
            return;
        }
        File featuresDir = new File(this.getFilesDir().getAbsolutePath() + File.separator + FaceServer.SAVE_FEATURE_DIR);
        if (!featuresDir.exists() || !featuresDir.isDirectory()) {
            Log.e("tag", "dir error");
            if (isUpdateManual) {
                mUpdateDataListener.onUpdate(true, true, false, 0);
                isUpdateManual = false;
            }
            return;
        }
        File failedDir = new File(InitializationActivity.REGISTERED_FAILED_DIR);
        int updateSuccessCount = 0;
        updateProcessFailCount = 0;
        try {
            if (!isUpdate) {
                if (list != null && list.size() > 0 && !isSuccess) {
                    File[] failedFiles = failedDir.listFiles();
                    String failedName, failedId, failedPathName;
                    RegisteredFailedMembers registeredFailedMembers;
                    List<RegisteredFailedMembers> registeredFailedMembersList;
                    boolean result;
                    for (File failedFile : failedFiles) {
                        failedPathName = failedFile.getName();
                        failedName = failedPathName.substring(0, failedPathName.lastIndexOf("-"));
                        failedId = failedPathName.substring(failedPathName.lastIndexOf("-") + 1, failedPathName.lastIndexOf("."));
                        for (Members member : list) {
                            if (member.getUserId().equals(failedId)) {
                                registeredFailedMembersList = LitePal.where("userId = ?", failedId).find(RegisteredFailedMembers.class);
                                if (registeredFailedMembersList.size() > 0) {
                                    //update
                                    registeredFailedMembers = registeredFailedMembersList.get(0);
                                    registeredFailedMembers.setImage(InitializationActivity.REGISTERED_FAILED_DIR + File.separator + failedPathName);
                                    registeredFailedMembers.setName(failedName);
                                   // registeredFailedMembers.setUserId(failedId);
                                    result = registeredFailedMembers.save();
                                    if (result) {
                                        updateProcessFailCount++;
                                        updateSuccessCount++;
                                    }
                                } else {
                                    //new
                                    registeredFailedMembers = new RegisteredFailedMembers();
                                    registeredFailedMembers.setName(member.getName());
                                   // registeredFailedMembers.setUserId(member.getUserId());
                                    registeredFailedMembers.setImage(InitializationActivity.REGISTERED_FAILED_DIR + File.separator + failedPathName);
                                    result = registeredFailedMembers.save();
                                    Log.e("tag", "registeredFailedMembers_name---" + member.getName() + "---id---" + member.getUserId());
                                    if (result) {
                                        updateProcessFailCount++;
                                        updateSuccessCount++;
                                    }
                                }
                                break;
                            }
                        }
                    }
                }
                if (list != null && list.size() > 0) {
                    RegisteredMembers registeredMembers;
                    File[] failedFiles;
                    boolean result;
                    List<RegisteredFailedMembers> registeredFailedMembersList;
                    if (ROOT_PATH_STRING == null) {
                        ROOT_PATH_STRING = this.getFilesDir().getAbsolutePath();
                    }
                    //new
                    for (File file : filesList) {
                        String pathName = file.getName();
                        final String userId = pathName.substring(pathName.lastIndexOf("-") + 1, pathName.lastIndexOf("."));
                        String image = InitializationActivity.REGISTERED_IMAGE_DIR + File.separator + pathName;
                        String feature = ROOT_PATH_STRING + File.separator + FaceServer.SAVE_FEATURE_DIR + File.separator + pathName.substring(0, pathName.lastIndexOf("."));
//                            Log.e("tag", "image_uri---" + image + "  feature_uri---" + feature);
                        for (Members member : list) {
                            if (member.getUserId().equals(userId)) {
                                registeredMembers = new RegisteredMembers();
                                registeredMembers.setName(member.getName());
                                //registeredMembers.setUserId(member.getUserId());
                                registeredMembers.setMobile(member.getMobile());
                                registeredMembers.setStatus(member.getStatus());
                                registeredMembers.setExpire_time(member.getExpire_time());
                                registeredMembers.setImage(image);
                                registeredMembers.setFeatures(feature);
                                result = registeredMembers.save();
                                if (result) {
                                    //如果失败列表中含有该条信息，则删除原来失败列表中该条人员信息
                                    registeredFailedMembersList = LitePal.where("userId = ?", userId).find(RegisteredFailedMembers.class);
                                    if (registeredFailedMembersList != null && registeredFailedMembersList.size() > 0) {
                                        int line = LitePal.deleteAll(RegisteredFailedMembers.class, "userId = ?", userId);
                                        if (line > 0) {
                                            failedFiles = failedDir.listFiles(new FilenameFilter() {
                                                @Override
                                                public boolean accept(File dir, String name) {
                                                    return name.contains(userId);
                                                }
                                            });
                                            if (failedFiles.length > 0) {
                                                for (File failedFile : failedFiles) {
                                                    if (failedFile.delete()) {
                                                        Log.e("tag", "delete exist failed file");
                                                    }
                                                }
                                                updateSuccessCount++;
                                                Log.e("tag", "update success");
                                            }
                                        }
                                    } else {
                                        updateSuccessCount++;
                                        Log.e("tag", "register success");
                                    }
                                }
                                break;
                            }
                        }
                    }
                    Log.e("tag", "registeredMembesr_size--" + LitePal.findAll(RegisteredMembers.class).size());
                }
            } else {
                if (list != null && list.size() > 0 && !isSuccess) {
                    File[] failedFiles = failedDir.listFiles();
                    File[] files;
                    RegisteredFailedMembers registeredFailedMembers;
                    List<RegisteredFailedMembers> registeredFailedMembersList;
                    List<RegisteredMembers> registeredMembersList;
                    boolean result;
                    for (File failedFile : failedFiles) {
                        String failedPathName = failedFile.getName();
                        String failedName = failedPathName.substring(0, failedPathName.lastIndexOf("-"));
                        final String failedId = failedPathName.substring(failedPathName.lastIndexOf("-") + 1, failedPathName.lastIndexOf("."));
                        for (Members member : list) {
                            if (member.getUserId().equals(failedId)) {
                                registeredFailedMembersList = LitePal.where("userId = ?", failedId).find(RegisteredFailedMembers.class);
                                if (registeredFailedMembersList.size() == 0) {
                                    registeredFailedMembers = new RegisteredFailedMembers();
                                    registeredFailedMembers.setName(failedName);
                                   // registeredFailedMembers.setUserId(failedId);
                                    registeredFailedMembers.setImage(InitializationActivity.REGISTERED_FAILED_DIR + File.separator + failedPathName);
                                    result = registeredFailedMembers.save();
                                    Log.e("tag", "registeredFailedMembers_name---" + member.getName() + "---id---" + member.getUserId());
                                    if (result) {
                                        //成功列表中含有该条信息，删除原来成功列表中该条人员信息
                                        registeredMembersList = LitePal.where("userId = ?", failedId).find(RegisteredMembers.class);
                                        if (registeredMembersList != null && registeredMembersList.size() > 0) {
                                            int line = LitePal.deleteAll(RegisteredMembers.class, "userId = ?", failedId);
                                            if (line > 0) {
                                                files = imgsDir.listFiles(new FilenameFilter() {
                                                    @Override
                                                    public boolean accept(File dir, String name) {
                                                        return name.contains(failedId);
                                                    }
                                                });
                                                if (files.length > 0) {
                                                    for (File file : files) {
                                                        Log.e("tag", file.getName());
                                                        if (file.delete()) {
                                                            Log.e("tag", "move to fail member");
                                                        }
                                                    }
                                                    updateProcessFailCount++;
                                                    updateSuccessCount++;
                                                }
                                            }
                                        }
                                    }
                                }
                                break;
                            }
                        }
                    }
                }
                if (list != null && list.size() > 0) {
                    Log.e("tag", "start to update data");
                    List<RegisteredMembers> registeredMembersList;
                    RegisteredMembers registeredMembers;
                    boolean result;
                    if (ROOT_PATH_STRING == null) {
                        ROOT_PATH_STRING = this.getFilesDir().getAbsolutePath();
                    }
                    for (File file : filesList) {
                        String pathName = file.getName();
                        String userId = pathName.substring(pathName.lastIndexOf("-") + 1, pathName.lastIndexOf("."));
                        String image = InitializationActivity.REGISTERED_IMAGE_DIR + File.separator + pathName;
                        String feature = ROOT_PATH_STRING + File.separator + FaceServer.SAVE_FEATURE_DIR + File.separator + pathName.substring(0, pathName.lastIndexOf("."));
                        registeredMembersList = LitePal.where("userId = ?", userId).find(RegisteredMembers.class);
                        if (registeredMembersList != null && registeredMembersList.size() > 0) {
                            //update
                            for (Members member : list) {
                                if (member.getUserId().equals(userId)) {
                                    registeredMembers = registeredMembersList.get(0);
                                    registeredMembers.setMobile(member.getMobile());
                                    registeredMembers.setStatus(member.getStatus());
                                    registeredMembers.setName(member.getName());
                                    registeredMembers.setExpire_time(member.getExpire_time());
                                    registeredMembers.setImage(image);
                                    registeredMembers.setFeatures(feature);
                                    result = registeredMembers.save();
                                    if (result) {
                                        updateSuccessCount++;
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            totalUpdateSuccessCount += updateSuccessCount;
            Log.e("tag", "updateSuccessCount---" + updateSuccessCount);
        } catch (Exception e) {
            e.printStackTrace();
            if (isUpdateManual) {
                mUpdateDataListener.onUpdate(true, true, false, 0);
                isUpdateManual = false;
            }
        }
    }

    private void delete(boolean manual) {
        isDeleteManual = manual;
        if (sp.getBoolean("isInit", false)) {
            getDeleteList();
        } else {
            Log.e("tag", "database is not init");
            if (isDeleteManual) {
                mDeleteDataListener.onDelete(true, true, false);
                isDeleteManual = false;
            }
        }
    }

    private void getDeleteList() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("access_token", GlobalParameters.Access_token);
        parameters.put("sn", GlobalParameters.SN);
        mNovate.rxPost(GlobalParameters.DeleteUserListURL, parameters, new RxStringCallback() {
            @Override
            public void onError(Object tag, Throwable e) {
                Log.e("onError---", "getdelete error:" + e.getMessage());
                if (isDeleteManual) {
                    mDeleteDataListener.onDelete(true, false, false);
                    isDeleteManual = false;
                }
            }

            @Override
            public void onCancel(Object tag, Throwable e) {

            }

            @Override
            public void onNext(Object tag, String response) {
                Log.e("onNext---", "getdelete response:" + response);
                try {
                    MemberList memberList = gson.fromJson(response, MemberList.class);
                    final List<Members> deleteMembersList = memberList.getData();
                    if (deleteMembersList.size() > 0 && fixedThreadPool != null && !fixedThreadPool.isShutdown()) {
                        fixedThreadPool.execute(new Runnable() {
                            @Override
                            public void run() {
                                doDelete(deleteMembersList);
                            }
                        });
                    } else {
//                        updateHandler.obtainMessage(SHOW_NO_DELETE_TOAST).sendToTarget();
                        Log.e("tag", "no data to delete");
                        if (isDeleteManual) {
                            mDeleteDataListener.onDelete(false, true, false);
                            isDeleteManual = false;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("getdelete exception:", e.getMessage());
                }
            }
        });
    }

    private void doDelete(final List<Members> deleteMembersList) {
        if (deleteMembersList != null) {
            boolean isDeleted = true;
            boolean isExist = false;
            boolean featureDeleteResult;
            boolean imgDeleteResult;
            List<RegisteredMembers> list;
            List<RegisteredFailedMembers> failedMembersList;
            String featurePath, imgPath, registeredImagePath, registeredFailedImagePath, name, id;
            File featureFile, imgFile, registeredImageFile, registeredFailedImageFile;
            if (ROOT_PATH_STRING == null) {
                ROOT_PATH_STRING = this.getFilesDir().getAbsolutePath();
            }
            for (Members member : deleteMembersList) {
                name = member.getName();
                id = member.getUserId();
                list = LitePal.where("userId = ?", id).find(RegisteredMembers.class);
                if (list != null && list.size() > 0) {
                    FaceServer.getInstance().deleteInfo(name + "-" + id);
                    isExist = true;
                    featurePath = ROOT_PATH_STRING + File.separator + FaceServer.SAVE_FEATURE_DIR + File.separator + name + "-" + id;
                    imgPath = ROOT_PATH_STRING + File.separator + FaceServer.SAVE_IMG_DIR + File.separator + name + "-" + id + ".jpg";
                    registeredImagePath = InitializationActivity.REGISTERED_IMAGE_DIR + File.separator + name + "-" + id + ".jpg";
                    int line = LitePal.deleteAll(RegisteredMembers.class, "userId = ?", id);
                    isDeleted &= (line > 0);
                    Log.e("tag", "line---" + line);
                    featureFile = new File(featurePath);
                    imgFile = new File(imgPath);
                    registeredImageFile = new File(registeredImagePath);
                    if (featureFile.exists() && featureFile.isFile()) {
                        featureDeleteResult = featureFile.delete();
                        if (featureDeleteResult) {
                            Log.e("tag", "feature delete success---" + featurePath);
                        }
                    }
                    if (imgFile.exists() && imgFile.isFile()) {
                        imgDeleteResult = imgFile.delete();
                        if (imgDeleteResult) {
                            Log.e("tag", "image delete success---" + featurePath);
                        }
                    }
                    if (registeredImageFile.exists() && registeredImageFile.isFile()) {
                        if (registeredImageFile.delete()) {
                            Log.e("tag", "registered image delete success---" + registeredImagePath);
                        }
                    }
                } else {
                    failedMembersList = LitePal.where("userId = ?", id).find(RegisteredFailedMembers.class);
                    if (failedMembersList != null && failedMembersList.size() > 0) {
                        isExist = true;
                        registeredFailedImagePath = InitializationActivity.REGISTERED_FAILED_DIR + File.separator + name + "-" + id + ".jpg";
                        int line = LitePal.deleteAll(RegisteredFailedMembers.class, "userId = ?", id);
                        isDeleted &= (line > 0);
                        registeredFailedImageFile = new File(registeredFailedImagePath);
                        if (registeredFailedImageFile.exists() && registeredFailedImageFile.isFile()) {
                            if (registeredFailedImageFile.delete()) {
                                Log.e("tag", "registered fail image delete success---" + registeredFailedImagePath);
                            }
                        }
                    }
                }
            }
            if (isExist) {
                if (isDeleted) {
                    Log.e("tag", "delete success");
                    if (isDeleteManual) {
                        mDeleteDataListener.onDelete(true, true, true);
                        isDeleteManual = false;
                    }
                    if (liveDeleteTimer != null) {
                        liveDeleteTimer.cancel();
                        liveDeleteTimer = null;
                        Log.e("tag","liveDeleteTimer cancel");
                    }
                    if (liveDeleteTImerTask != null) {
                        liveDeleteTImerTask.cancel();
                        liveDeleteTImerTask = null;
                    }
                } else {
                    Log.e("tag", "delete fail");
                    if (isDeleteManual) {
                        mDeleteDataListener.onDelete(true, true, false);
                        isDeleteManual = false;
                    }
                }
            } else {
                if (isDeleteManual) {
                    mDeleteDataListener.onDelete(false, true, false);
                    isDeleteManual = false;
                }
                Log.e("tag", "no data to delete");
            }
        }
    }

    private void deleteExistRegisteredImage(final String id) {
        File imageDir = new File(InitializationActivity.REGISTERED_IMAGE_DIR);
        if (!imageDir.exists()) {
            Log.e("tag", "dir error");
            if (isUpdateManual) {
                mUpdateDataListener.onUpdate(true, true, false, 0);
                isUpdateManual = false;
            }
            return;
        }
        if (!imageDir.isDirectory()) {
            Log.e("tag", "dir error");
            if (isUpdateManual) {
                mUpdateDataListener.onUpdate(true, true, false, 0);
                isUpdateManual = false;
            }
            return;
        }
        File[] imageFile = imageDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.contains(id);
            }
        });
        if (imageFile.length > 0) {
            for (File file : imageFile) {
                if (file.delete()) {
                    Log.e("tag", "delete exist registered image");
                }
            }
        }
    }

    private void moveFile(File source, File target) {
        try {
            FileInputStream in = new FileInputStream(source);
            FileOutputStream out = new FileOutputStream(target);
            byte[] buffer = new byte[1024];
            while (in.read(buffer) > 0) {
                out.write(buffer);
            }
            in.close();
            out.close();
            if (source.delete()) {
                Log.e("tag", "move done");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean compareMember(Members members, RegisteredMembers registeredMembers) {
        boolean result = members.getName().equals(registeredMembers.getName())
                && members.getExpire_time().equals(registeredMembers.getExpire_time())
                && members.getImage().equals(registeredMembers.getImage())
                && members.getMobile().equals(registeredMembers.getMobile())
                && members.getStatus().equals(registeredMembers.getStatus());
        return result;
    }

    public interface UpdateDataListener {
        void onUpdate(boolean exist, boolean network, boolean result, int processFailCount);
    }

    public static void setUpdateDataListener(UpdateDataListener updateDataListener) {
        if (updateDataListener != null) {
            mUpdateDataListener = updateDataListener;
        }
    }

    public static void removeUpdateDataListener() {
        if (mUpdateDataListener != null) {
            mUpdateDataListener = null;
        }
    }

    public interface DeleteDataListener {
        void onDelete(boolean exist, boolean network, boolean result);
    }

    public static void setDeleteDataListener(DeleteDataListener deleteDataListener) {
        if (deleteDataListener != null) {
            mDeleteDataListener = deleteDataListener;
        }
    }

    public void removeDeleteDataListener() {
        if (mDeleteDataListener != null) {
            mDeleteDataListener = null;
        }
    }

}
