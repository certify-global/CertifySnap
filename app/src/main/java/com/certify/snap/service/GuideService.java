package com.certify.snap.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.tamic.novate.Novate;
import com.tamic.novate.Throwable;
import com.tamic.novate.callback.ResponseCallback;
import com.tamic.novate.callback.RxFileCallBack;
import com.tamic.novate.callback.RxStringCallback;
import com.certify.snap.activity.HomeActivity;
import com.certify.snap.bean.AccessToken;
import com.certify.snap.bean.Guest;
import com.certify.snap.bean.GuestList;
import com.certify.snap.bean.Normal;
import com.certify.snap.bean.Secret;
import com.certify.snap.common.Application;
import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.ResponseBody;
import rx.Subscription;


/**
 * 作者    ljf
 * 时间    2019/8/22 0022 21:47
 * 文件    Telpo_Face_system
 * 描述
 */
public class GuideService extends Service {

    public static final String TAG = "GuideService";
    private Timer onlinetimer, tokentimer, offlineverifytimer, updateGuestListTimer, deleteExpiredGuestDataTimer, liveGuestTimer, liveTimer, wallpaperTimer;
    private MyBinder mBinder = new MyBinder();
    private Gson gson = new Gson();
    private Novate mnovate;
    private SharedPreferences sp;
    private int TryToken = 3;
    private int TryOnline = 3;
    private int TryUpdateGuest = 3;
    private final Object obj = new Object();
    private Long serverTime;
    private List<Guest> guestList;
    private SQLiteDatabase db;
    private boolean updateGuestSuccessResult = false;
    private boolean updateGuestFailResult = true;
    private static SecretListener mSecretListener;
    public static final String WALLPAPER_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Telpo_face" + File.separator + "Wallpaper";
    public static final String GET_SECRET = "com.telpo.telpo_face_system_get_secret";
    public static final String LIVE_UPDATE_GUEST = "com.telpo.telpo_face_system_live_update_guest";
    public static final String LIVE_UPDATE_PASSWORD = "com.telpo.telpo_face_system_live_update_password";
    public static final String LIVE_UPDATE_WALLPAPER = "com.telpo.telpo_face_system_live_update_wallpaper";
    public static final String LIVE_UPDATE_LIMIT_TIME = "com.telpo.telpo_face_system_live_update_limit_time";
    private GetSecretBroadcastReceiver getSecretBroadcastReceiver;
    private boolean isFirst = true;
    private boolean isWallPaper = false;

    class GetSecretBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null) {
                switch (intent.getAction()) {
                    case GET_SECRET:
                        getSecret();
                        break;
                    case LIVE_UPDATE_GUEST:
                        liveUpdate(LIVE_UPDATE_GUEST);
                        break;
                    case LIVE_UPDATE_PASSWORD:
                        liveUpdate(LIVE_UPDATE_PASSWORD);
                    case LIVE_UPDATE_WALLPAPER:
                        liveUpdate(LIVE_UPDATE_WALLPAPER);
                    case LIVE_UPDATE_LIMIT_TIME:
                        liveUpdate(LIVE_UPDATE_LIMIT_TIME);
                        break;
                }
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mnovate = Application.getInstance().getNovate();
        sp = Util.getSharedPreferences(this);
        /*try {
            db = LitePal.getDatabase();
        }catch (Exception e){
            e.printStackTrace();
        }*/
        Log.d(TAG, "onCreate() executed");
        getSecretBroadcastReceiver = new GetSecretBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(GET_SECRET);
        intentFilter.addAction(LIVE_UPDATE_GUEST);
        intentFilter.addAction(LIVE_UPDATE_PASSWORD);
        intentFilter.addAction(LIVE_UPDATE_WALLPAPER);
        intentFilter.addAction(LIVE_UPDATE_LIMIT_TIME);
        registerReceiver(getSecretBroadcastReceiver, intentFilter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand() executed");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy() executed");
        if (getSecretBroadcastReceiver != null) {
            unregisterReceiver(getSecretBroadcastReceiver);
        }
        if (onlinetimer != null) onlinetimer.cancel();
        if (tokentimer != null) tokentimer.cancel();
        if (offlineverifytimer != null) offlineverifytimer.cancel();
        if (updateGuestListTimer != null) updateGuestListTimer.cancel();
        if (deleteExpiredGuestDataTimer != null) deleteExpiredGuestDataTimer.cancel();
        if (liveGuestTimer != null) liveGuestTimer.cancel();
        if (liveTimer != null) liveTimer.cancel();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class MyBinder extends Binder {
        public void getDeviceInfo(){
            getSecret();
        }
    }

    public void startTokenTimer() {
        Log.e("TAG", "startTokenTimer() executed");
        // 执行获取token
        if (tokentimer == null) {
            tokentimer = new Timer();
            tokentimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    getAccessToken();
                }
            }, 0, 30 * 60 * 1000);
        }
    }

    public void startOnlineTimer() {
        Log.e("TAG", "startOnlineTimer() executed");
        // 执行上报在线
        if (onlinetimer == null) {
            onlinetimer = new Timer();
            onlinetimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    TryOnline = 3;
                    uploadOnline();
                }
            }, 10 * 1000, 10 * 60 * 1000);
        }
    }


    public void startOfflineverifyTimer() {
        Log.e("yw_startOff", "startOfflineverifyTimer() executed");
        // 执行上报在线
        if (offlineverifytimer == null) {
            offlineverifytimer = new Timer();
            offlineverifytimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    uploadOfflineverify();
                }
            }, 100 * 1000, 15 * 60 * 1000);
        }
    }

    public void startUpdateGuestRecordTimer() {
        Log.e("tag", "startUpdateGuestRecordTimer() executed");
        if (updateGuestListTimer == null) {
            updateGuestListTimer = new Timer();
            updateGuestListTimer.schedule(new TimerTask() {
                @Override
                public void run() {
//                    updateGuestList();
                    uploadGuestRecord();
                }
            }, 20 * 1000, 10 * 60 * 1000);
        }
    }

    public void startDeleteExpiredGuestDataTimer() {
        Log.e("tag", "startDeleteExpiredGuestDataTimer() executed");
        if (deleteExpiredGuestDataTimer == null) {
            deleteExpiredGuestDataTimer = new Timer();
            deleteExpiredGuestDataTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    deleteExpiredGuestData();
                }
            }, 30 * 1000, 7 * 24 * 60 * 60 * 1000);
        }
    }

    public void liveUpdate(String s) {
        if (s == null)
            return;
        if (s.equals(LIVE_UPDATE_GUEST)) {
            Log.e("tag","liveGuestTimer start");
            if (liveGuestTimer == null) {
                liveGuestTimer = new Timer();
                liveGuestTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        updateGuestList();
                    }
                }, 0, 30 * 1000);
            }
        } else if (s.equals(LIVE_UPDATE_PASSWORD) || s.equals(LIVE_UPDATE_WALLPAPER) || s.equals(LIVE_UPDATE_LIMIT_TIME)) {
            Log.e("tag","liveTimer start");
            if (s.equals(LIVE_UPDATE_WALLPAPER)) isWallPaper = true;
            if (liveTimer == null) {
                liveTimer = new Timer();
                liveTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        getSecret();
                    }
                }, 0, 30 * 1000);
            }
        }
    }

    //上报在线状态
    private void uploadOnline() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("access_token", GlobalParameters.Access_token);
        parameters.put("sn", GlobalParameters.SN);
        if (!TextUtils.isEmpty(GlobalParameters.channelID))
            parameters.put("push_id", GlobalParameters.channelID);
        mnovate.rxPost(GlobalParameters.UpdateOnlineURL, parameters, new RxStringCallback() {
            @Override
            public void onError(Object tag, Throwable e) {
                Log.e("onError---", "uploadonline error:" + e.getMessage() + "-" + TryOnline);
                if (TryOnline > 0) {
                    uploadOnline();
                    TryOnline--;
                }
            }

            @Override
            public void onCancel(Object tag, Throwable e) {

            }

            @Override
            public void onNext(Object tag, String response) {
                Log.e("onNext---", "uploadonline success:" + response);
            }
        });
        Subscription s= mnovate.rxPost(GlobalParameters.UpdateOnlineURL, parameters, new ResponseCallback<Subscription, ResponseBody>() {
            @Override
            public Subscription onHandleResponse(ResponseBody response) throws Exception {
                return null;
            }

            @Override
            public void onError(Object tag, Throwable e) {

            }

            @Override
            public void onCancel(Object tag, Throwable e) {

            }

            @Override
            public void onNext(Object tag, Call call, Subscription response) {

            }
        });
        s.unsubscribe();
    }

    //上报考勤记录
    private void uploadOfflineverify() {

        /*LitePal.findAllAsync(OfflineVerifyMembers.class).listen(new FindMultiCallback<OfflineVerifyMembers>() {
            @Override
            public void onFinish(List<OfflineVerifyMembers> list) {
                final List<OfflineVerifyMembers> mlist = list;
                if (mlist != null && mlist.size() > 0) {
                    Log.e("yw_开始上报", "数量" + mlist.size());
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            synchronized (obj) {
                                for (int j = 0; j < mlist.size(); j++) {
                                    Map<String, Object> parameters = new HashMap<>();
                                    parameters.put("access_token", GlobalParameters.Access_token);
                                    parameters.put("groupId", GlobalParameters.Client_id);
                                    parameters.put("sn", GlobalParameters.SN);
                                    //parameters.put("userId", mlist.get(j).getUserId());
                                    parameters.put("verify_time", mlist.get(j).getVerify_time());
                                    // Log.e("yw", parameters.toString());

                                    dopostoffline(parameters);

                                    try {
                                        Thread.sleep(1000);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    //    Log.e("yw12", parameters.toString());

                                }
                            }
                        }
                    }).start();

                }
            }
        });*/


    }

    private void dopostoffline(final Map params) {


        // Log.e("yw11", params.toString());

     /*   mMyOkhttp.post()
                .url(GlobalParameters.BASEURL + GlobalParameters.OfflineVerify)
                .params(params)
                .tag(this)
                .enqueue(new JsonResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, JSONObject response) {
                        //  Log.e("yw_offlineVerify",statusCode+"   "+response.toString());
                        try {
                            int code = response.getInt("code");
                            if (code == 1) {
                                // Log.e("yw---", "delete verify_time " + params.get("verify_time").toString());
                                LitePal.deleteAll(OfflineVerifyMembers.class, "verify_time = ? ", params.get("verify_time").toString());


                            } else if (code == 0) {

                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }


                    @Override
                    public void onFailure(int statusCode, String error_msg) {
                        // Log.d(TAG, "doPost onFailure:" + error_msg);
                        // Log.e("yw_offlineVerify__fail",statusCode+"   "+error_msg);


                    }
                });*/

    }

    //更新访客数据
    private void updateGuestList() {
        String guestTimeStamp = Long.toString(sp.getLong("guestTimeStamp", 0));
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("access_token", GlobalParameters.Access_token);
        parameters.put("sn", GlobalParameters.SN);
        parameters.put("updatetime", guestTimeStamp);
        mnovate.rxPost(GlobalParameters.GuestlistURL, parameters, new RxStringCallback() {
            @Override
            public void onNext(Object tag, String response) {
                Log.e("onNext---", "getguest respon:" + response);
                GuestList updateList = gson.fromJson(response, GuestList.class);
                serverTime = updateList.getServertime();
                guestList = updateList.getData();
                if (guestList != null && guestList.size() != 0) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            for (int i = 0; i < guestList.size(); i++) {
                                final Guest guest = guestList.get(i);
                                final String id = guest.getUserId();
                                /*LitePal.where("userId = ?", id).findAsync(GuestMembers.class).listen(new FindMultiCallback<GuestMembers>() {
                                    @Override
                                    public void onFinish(List<GuestMembers> list) {
                                        if (list != null && list.size() != 0) {
                                            GuestMembers updateGuestMember = list.get(0);
                                            updateGuestMember.setName(guest.getName());
                                            updateGuestMember.setMobile(guest.getMobile());
                                            updateGuestMember.setExpire_time(guest.getExpire_time());
                                            updateGuestMember.setQrcode(guest.getQrcode());
                                           *//* if (telpoDatabaseStore.insertGuestMembers(updateGuestMember)) {
                                                updateGuestSuccessResult = true;
                                                Log.e("tag", "update guest info success");
                                            } else {
                                                updateGuestFailResult = false;
                                                Log.e("tag", "update guest info fail");
                                            }*//*
                                        } else {
                                            GuestMembers guestMembers = new GuestMembers();
                                            guestMembers.setUserId(id);
                                            guestMembers.setName(guest.getName());
                                            guestMembers.setMobile(guest.getMobile());
                                            guestMembers.setExpire_time(guest.getExpire_time());
                                            guestMembers.setQrcode(guest.getQrcode());
                                          *//*  if (telpoDatabaseStore.insertGuestMembers(guestMembers)) {
                                                updateGuestSuccessResult = true;
                                                Log.e("tag", "save guest info success");
                                            } else {
                                                updateGuestFailResult = false;
                                                Log.e("tag", "save guest info fail");
                                            }*//*
                                        }
                                    }
                                });*/
                            }
                            if (updateGuestSuccessResult & updateGuestFailResult) {
                                sp.edit().putLong("guestTimeStamp", serverTime).apply();
//                                sp.edit().putBoolean("liveUpdateGuest", true).apply();
                                if (liveGuestTimer != null) {
                                    liveGuestTimer.cancel();
                                    liveGuestTimer = null;
                                }
                            }
                        }
                    }).start();
                }
            }

            @Override
            public void onError(Object tag, Throwable e) {
                Log.e("tag", "update guest list fail");
//                if (TryUpdateGuest > 0) {
//                    updateGuestList();
//                    TryUpdateGuest--;
//                }
            }

            @Override
            public void onCancel(Object tag, Throwable e) {

            }
        });
    }

    //上报访客信息
    private void uploadGuestRecord() {
        /*LitePal.findAllAsync(OfflineGuestMembers.class).listen(new FindMultiCallback<OfflineGuestMembers>() {
            @Override
            public void onFinish(final List<OfflineGuestMembers> list) {
                if (list != null && list.size() > 0) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Log.e("tag", "start to upload guest record");
                            Map<String, Object> parameters;
                            for (OfflineGuestMembers offlineGuestMembers : list) {
                                parameters = new HashMap<>();
                                parameters.put("access_token", GlobalParameters.Access_token);
                                parameters.put("groupId", GlobalParameters.Client_id);
                                parameters.put("sn", GlobalParameters.SN);
                                parameters.put("userId", offlineGuestMembers.getUserId());
                                parameters.put("verify_time", offlineGuestMembers.getVerify_time());
                                parameters.put("guest", 1);
                                uploadGuest(parameters);
                            }
                        }
                    }).start();
                }
            }
        });*/
    }

    private void uploadGuest(final Map<String, Object> parameters) {
        mnovate.rxPost(GlobalParameters.OfflineVerify, parameters, new RxStringCallback() {
            @Override
            public void onNext(Object tag, String response) {
                Normal reponseData = gson.fromJson(response, Normal.class);
                int code = reponseData.getCode();
                if (code == 1) {
                    Log.e("tag", "upload guest record success");
                    try {
                        //LitePal.deleteAll(OfflineGuestMembers.class, "verify_time = ?", parameters.get("verify_time").toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onError(Object tag, Throwable e) {

            }

            @Override
            public void onCancel(Object tag, Throwable e) {

            }
        });
    }

    private void deleteExpiredGuestData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.e("tag", "start to delete expired guest data");
                /*List<GuestMembers> guestMembersList = LitePal.findAll(GuestMembers.class);
                if (guestMembersList != null && guestMembersList.size() > 0) {
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date curDate = new Date(System.currentTimeMillis());
                    String currentTime = formatter.format(curDate);
                    ListIterator listIterator = guestMembersList.listIterator();
                    String expire_time;
                    GuestMembers guestMembers;
                    while (listIterator.hasNext()) {
                        guestMembers = (GuestMembers) listIterator.next();
                        expire_time = guestMembers.getExpire_time();
                        if (!Util.isDateOneBigger(expire_time, currentTime)) {
                            //LitePal.deleteAll(GuestMembers.class, "userId = ?", guestMembers.getUserId());
                        }
                    }
                }*/
            }
        }).start();
    }

    //获取Clientid等信息
    private void getSecret() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("sn", GlobalParameters.SN);
        mnovate.rxPost(GlobalParameters.SecretURL, parameters, new RxStringCallback() {
            @Override
            public void onError(Object tag, Throwable e) {
                Log.e("onError---", "getsecret error:" + e.getMessage());
                mSecretListener.onFail();
            }

            @Override
            public void onCancel(Object tag, Throwable e) {

            }

            @Override
            public void onNext(Object tag, String response) {
                Log.e("onNext---", "getsecret respon:" + response);
                try {
                    Secret secret = gson.fromJson(response, Secret.class);
                    if (secret != null) {
                        if (liveTimer != null) {
                            liveTimer.cancel();
                            liveTimer = null;
                        }
                        GlobalParameters.Client_id = secret.getData().getClient_id();
                        GlobalParameters.Client_secret = secret.getData().getClient_secret();
                        GlobalParameters.Access_limit = secret.getData().getAccess_limit();
                        Log.e("tag", "access time---" + GlobalParameters.Access_limit);
                        if (!TextUtils.isEmpty(secret.getData().getDevice_password())) {
                            sp.edit().putString("device_password", secret.getData().getDevice_password()).apply();
                            Log.e("tag", "device_password----" + secret.getData().getDevice_password());
                        }
                        final String wallpaperUrl = secret.getData().getWallpaper();
                        //获取的壁纸url不为空且没更换过壁纸就下载壁纸更换，
                        if (!TextUtils.isEmpty(wallpaperUrl)&&!sp.getBoolean("wallpaper",false)) {
                            final String fileName = wallpaperUrl.substring(wallpaperUrl.lastIndexOf("/") + 1);
//                            sp.edit().putString("wallpaper_url", wallpaperUrl).apply();
                            if(wallpaperTimer==null){
                                wallpaperTimer = new Timer();
                                wallpaperTimer.schedule(new TimerTask() {
                                    @Override
                                    public void run() {
                                        downloadWallpaper(wallpaperUrl, fileName);
                                    }
                                },0,30*1000);
                            }
                        }
                        //是壁纸推送再去下载新壁纸
                       if(!TextUtils.isEmpty(wallpaperUrl)&&isWallPaper){
                           final String fileName = wallpaperUrl.substring(wallpaperUrl.lastIndexOf("/") + 1);
//                            sp.edit().putString("wallpaper_url", wallpaperUrl).apply();
                           if(wallpaperTimer==null){
                               wallpaperTimer = new Timer();
                               wallpaperTimer.schedule(new TimerTask() {
                                   @Override
                                   public void run() {
                                       downloadWallpaper(wallpaperUrl, fileName);
                                   }
                               },0,30*1000);
                           }
                       }
                        Log.e("onNext---", GlobalParameters.Client_id + "-" + GlobalParameters.Client_secret);
                        if (!TextUtils.isEmpty(GlobalParameters.Client_id) && !TextUtils.isEmpty(GlobalParameters.Client_secret) && isFirst) {
                            //finish();
                            startTokenTimer();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("getsecret exception---", e.getMessage());
                    mSecretListener.onFail();
                }
            }
        });
    }

    //获取token
    private void getAccessToken() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("client_id", GlobalParameters.Client_id);
        parameters.put("client_secret", GlobalParameters.Client_secret);
        parameters.put("grant_type", "client_credentials");
        mnovate.rxPost(GlobalParameters.AccessTokenURL, parameters, new RxStringCallback() {
            @Override
            public void onError(Object tag, Throwable e) {
                Log.e("onError---", "gettoken error:" + e.getMessage() + "-" + TryToken);
                if (TryToken > 0) {
                    getAccessToken();
                    TryToken--;
                } else mSecretListener.onFail();
            }

            @Override
            public void onCancel(Object tag, Throwable e) {

            }

            @Override
            public void onNext(Object tag, String response) {
                Log.e("onNext---", "gettoken respon:" + response);
                try {
                    AccessToken accessToken = gson.fromJson(response, AccessToken.class);
                    if (accessToken != null) {
                        isFirst = false;
                        GlobalParameters.Access_token = accessToken.getAccess_token();
                        Log.e("accesstoken---", GlobalParameters.Access_token);
                        startOnlineTimer();
                        startOfflineverifyTimer();
                        startUpdateGuestRecordTimer();
                        startDeleteExpiredGuestDataTimer();
                        if (Util.isActivityTop(HomeActivity.class, GuideService.this)) {
                            Log.e("isactivitytop---", "guide是否是栈顶=" + Util.isActivityTop(HomeActivity.class, GuideService.this));
                            Util.switchRgbOrIrActivity(GuideService.this, false);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("gettoken exception---", e.getMessage());
                }
            }
        });
    }

    private void downloadWallpaper(String url, String fileName) {
        if (url != null && fileName != null) {
            Log.e("tag", "wallpaper---" + fileName);
            mnovate.rxDownload(url, new RxFileCallBack(WALLPAPER_DIR, fileName) {
                @Override
                public void onNext(Object tag, File file) {
                    final File source = file;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            File wallpaperFile = new File(WALLPAPER_DIR, "wallpaper.png");
                            moveFile(source, wallpaperFile);
                        }
                    }).start();
                    isWallPaper = false;
                    if(wallpaperTimer!=null){
                        wallpaperTimer.cancel();
                        wallpaperTimer = null;
                    }
                }

                @Override
                public void onProgress(Object tag, float progress, long downloaded, long total) {

                }

                @Override
                public void onError(Object tag, Throwable e) {

                }

                @Override
                public void onCancel(Object tag, Throwable e) {

                }

            });
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
                sp.edit().putBoolean("wallpaper", true).apply();
               // sendBroadcast(new Intent(IrCameraActivity.WALLPAPER_CHANGE));
                Log.e("tag", "source wallpaper image delete");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public interface SecretListener {
        void onFail();
    }

    public static void setSecretListener(SecretListener secretListener) {
        if (secretListener != null) {
            mSecretListener = secretListener;
        }
    }

}
