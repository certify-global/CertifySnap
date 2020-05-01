package com.certify.snap.common;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.other.BeepManager;
import com.certify.snap.R;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class NfcHelper {

    private int relaytimenumber = 5;
    private int DATA_BLOCK = 8;//第二扇区第1快
    private final int PASSWORD_BLOCK = 11;//第2扇区第4块

    private final byte[] password = new byte[]{(byte) 0xff, (byte) 0xff,
            (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff};

    private final byte[] password1 = new byte[]{(byte) 0x80, (byte) 0x60,
            (byte) 0x30, (byte) 0x30, (byte) 0x70, (byte) 0x80};
    private ProgressDialog dialog;

    private BeepManager mBeepManager;
    private BeepManager mBeepManager1;

    private boolean isDetected = false;
    private SwipeCardThread mSwipeCardThread;
    SharedPreferences sp;
    private Timer pTimer;
    static ImageView img_guest;
    static TextView txt_guest;
    private MyHandler myHandler;
    private Activity mActivity;
    private Context mContext;
    private Tag mTag;
    private static final int CARD_ERROR = 0;
    private static final int CARD_TYPE_ERROR = 1;
    private static final int CARD_KEY_AUTHENTICATE_ERROR = 2;
    private static final int CARD_ID_ERROR = 3;
    private static final int ENTER = 4;
    private static final int TIME_ERROR = 5;
    public NfcHelper(Activity activity , Tag tag){
        this.mActivity = activity;
        this.mContext = activity;
        this.mTag = tag;
        myHandler = new MyHandler(mActivity);
        sp = Util.getSharedPreferences(mActivity);
        relaytimenumber = sp.getInt(GlobalParameters.RelayTime,5);
        mBeepManager = new BeepManager(mActivity, R.raw.beep);
        mBeepManager1 = new BeepManager(mActivity, R.raw.error);
    }

    public void initCard() {
        new InitTask(mContext).execute();
    }

    public void close(){
        isDetected = false;
        if (mSwipeCardThread != null) {
            mSwipeCardThread.interrupt();
            mSwipeCardThread = null;
//            Log.e("stop thread","success");
        }
        if(pTimer!=null)
            pTimer.cancel();
    }

    private class InitTask extends AsyncTask<Void, Void, Integer> {
        Context mContext;
        public InitTask(Context context){
            mContext = context;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(mContext);
            dialog.setTitle("Active Card");
            dialog.setMessage("Progressing ...");
            dialog.setCancelable(false);
            dialog.show();

        }

        @Override
        protected Integer doInBackground(Void... params) {
            int amount = -1;
            try {
                //初始化卡内数据为111
                M1CardUtils.writeBlockWithKeyA(mTag, DATA_BLOCK, 111, password);
                //修改密码
                M1CardUtils.changeKeyA(mTag, PASSWORD_BLOCK, password1, password);
                amount = 0;
                Log.e("tag","init card success");
            } catch (IOException e) {
                e.printStackTrace();
                return catchError(e);
            }
            return amount;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            if (result == -1 || result == -2 || result == -3)
                processError(result);
            else {
                if (dialog != null && dialog.isShowing())
                    dialog.dismiss();
                mBeepManager.playBeepSoundAndVibrate();
            }

        }
    }

    private int catchError(Exception e) {
        if (e == null || e.getMessage() == null)
            return -1;
        else if (e.getMessage().contains(M1CardUtils.CARD_TYPE_ERROR)) {
            return -2;
        } else if (e.getMessage().contains(M1CardUtils.AUTHENTICATE_ERROR)) {
            return -3;
        }
        return -1;
    }

    private void processError(int error) {
        if (error == -1) {
            if (dialog != null && dialog.isShowing())
                dialog.dismiss();
            mBeepManager1.playBeepSoundAndVibrate();
            myHandler.obtainMessage(CARD_ERROR).sendToTarget();
        } else if (error == -2) {
            if (dialog != null && dialog.isShowing())
                dialog.dismiss();
            mBeepManager1.playBeepSoundAndVibrate();
            myHandler.obtainMessage(CARD_TYPE_ERROR).sendToTarget();
        } else if (error == -3) {
            if (dialog != null && dialog.isShowing())
                dialog.dismiss();
            mBeepManager1.playBeepSoundAndVibrate();
            myHandler.obtainMessage(CARD_KEY_AUTHENTICATE_ERROR).sendToTarget();
        }
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

    private void showDialog(){

    }

    private static class MyHandler extends Handler {
        WeakReference<Activity> activityWeakReference;
        private MyHandler(Activity activity){
            activityWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            Activity activity = activityWeakReference.get();
            switch (msg.what){
                case CARD_ERROR:
                    Toast.makeText(activity,"Card error",Toast.LENGTH_LONG).show();
                    break;
                case CARD_TYPE_ERROR:
                    Toast.makeText(activity,M1CardUtils.CARD_TYPE_ERROR,Toast.LENGTH_LONG).show();
                    break;
                case CARD_KEY_AUTHENTICATE_ERROR:
                    Toast.makeText(activity,M1CardUtils.AUTHENTICATE_ERROR,Toast.LENGTH_LONG).show();
                    break;
                case CARD_ID_ERROR:
                    showResult(activity,false,false);
                    break;
                case ENTER:
                    showResult(activity,true,true);
                    break;
                case TIME_ERROR:
                    showResult(activity,true,false);
                    break;
            }
        }
    }

    private void processResult(int what){
        myHandler.obtainMessage(what).sendToTarget();
        myHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                isDetected = true;
            }
        },2000);
    }

    //获取所有限制时间段的数组
    protected String[] processLimitedTime(String data) {
        if (data.contains(";")) {
            return data.split(";");
        } else {
            return new String[]{data};
        }
    }

    //比较所有限制时间段
    protected boolean compareAllLimitedTime(String compareTime, String[] limitedTimes) {
        boolean result = false;
        if (compareTime != null && limitedTimes != null) {
            for (String limitedTime : limitedTimes) {
                result |= compareLimitedTime(compareTime, limitedTime.split("-")[0], limitedTime.split("-")[1]);
            }
        }
        return result;
    }

    //比较具体一个时间段的方法
    protected boolean compareLimitedTime(String compareTime, String limitedStartTime, String limitedEndTime) {
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

    private static void showResult(Context context,boolean isSuccess,boolean isLimitTime) {
        View view = View.inflate(context, R.layout.toast_guest, null);
        img_guest = view.findViewById(R.id.img_guest);
        txt_guest = view.findViewById(R.id.txt_guest);
        img_guest.setBackground(isSuccess ? (isLimitTime ? context.getDrawable(R.mipmap.scan_success): context.getDrawable(R.mipmap.scan_fail)) :context.getDrawable(R.mipmap.scan_fail));
        txt_guest.setText(isSuccess ? (isLimitTime ? "Welcome" : "Non-passage time,Please contact the administrator!"):"Unrecognized");
        Toast guestToast = new Toast(context);
        guestToast.setView(view);
        guestToast.setDuration(Toast.LENGTH_SHORT);
        guestToast.setGravity(Gravity.CENTER, 0, 0);
        guestToast.show();
    }
}
