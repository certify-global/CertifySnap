package com.certify.snap.common;

import android.app.Activity;
import android.util.Log;

import com.common.thermalimage.ThermalImageUtil;
import com.tamic.novate.Novate;
//import com.tencent.bugly.crashreport.CrashReport;

import org.litepal.LitePal;

import java.util.LinkedList;
import java.util.List;

import okhttp3.logging.HttpLoggingInterceptor;

/**
 * Created by tsy on 2016/12/6.
 */

public class Application extends android.app.Application {

    private static Application mInstance;
    private Novate novate;
//    private MyOkHttp mMyOkHttp;
   // private DownloadMgr mDownloadMgr;
    public static boolean member=false;
    private List<Activity> activityList = new LinkedList();
    private ThermalImageUtil temperatureUtil;

    @Override
    public void onCreate() {
        super.onCreate();

        LitePal.initialize(this);

        mInstance = this;

        novate = new Novate.Builder(this).baseUrl(GlobalParameters.BASEURL).addLog(true)
                .connectTimeout(20).writeTimeout(15).build();

        //持久化存储cookie
//        ClearableCookieJar cookieJar =
//                new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(getApplicationContext()));

        //log拦截器
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        //自定义OkHttp
//        OkHttpClient okHttpClient = new OkHttpClient.Builder()
//                .connectTimeout(20000L, TimeUnit.MILLISECONDS)
//                .readTimeout(20000L, TimeUnit.MILLISECONDS)
//                //.writeTimeout(20000L, TimeUnit.MILLISECONDS)
//                .cookieJar(cookieJar)       //设置开启cookie
//                .addInterceptor(logging)            //设置开启log
//                .build();
//        mMyOkHttp = new MyOkHttp(okHttpClient);

//        MyCrashHandler.getInstance().init(this);
//        CrashReport.initCrashReport(getApplicationContext(), "467db9b3cc", false);

        BlockDetectByPrinter.start(false);//检测卡顿

        temperatureUtil = new ThermalImageUtil(this);
    }

    public static synchronized Application getInstance() {
        return mInstance;
    }

    public Novate getNovate() {
        return novate;
    }

//    public MyOkHttp getMyOkHttp() {
//        return mMyOkHttp;
//    }

    public ThermalImageUtil getTemperatureUtil(){
        return temperatureUtil;
    }

    // Activity
    public void addActivity(Activity activity) {
        activityList.add(activity);
    }

    // Activity finish
    public void exit() {
        for (Activity activity : activityList) {
            if(activity!=null && !activity.isFinishing()) {
                Log.e("exit---", activity.getLocalClassName());
                activity.finish();
            }
        }
        System.exit(0);
    }

}
