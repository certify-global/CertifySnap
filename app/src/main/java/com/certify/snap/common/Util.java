package com.certify.snap.common;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.YuvImage;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Camera;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.media.SoundPool;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.nfc.NfcAdapter;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Debug;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.certify.callback.JSONObjectCallback;
import com.certify.callback.MemberIDCallback;
import com.certify.callback.MemberListCallback;
import com.certify.callback.PushCallback;
import com.certify.callback.SettingCallback;
import com.certify.snap.BuildConfig;
import com.certify.snap.R;
import com.certify.snap.activity.AddDeviceActivity;
import com.certify.snap.activity.HomeActivity;
import com.certify.snap.activity.IrCameraActivity;
import com.certify.snap.activity.ProIrCameraActivity;
import com.certify.snap.activity.SettingsActivity;
import com.certify.snap.api.RetrofitInstance;
import com.certify.snap.api.request.DeviceInfo;
import com.certify.snap.api.response.AccessControlSettings;
import com.certify.snap.api.response.AudioVisualSettings;
import com.certify.snap.api.response.ConfirmationViewSettings;
import com.certify.snap.api.response.DeviceSettingsApi;
import com.certify.snap.api.response.DeviceSettingsData;
import com.certify.snap.api.response.GuideSettings;
import com.certify.snap.api.response.HomePageSettings;
import com.certify.snap.api.response.IdentificationSettings;
import com.certify.snap.api.response.PrinterSettings;
import com.certify.snap.api.response.ScanViewSettings;
import com.certify.snap.api.response.SettingsData;
import com.certify.snap.api.response.SettingsResponse;
import com.certify.snap.api.response.TouchlessSettings;
import com.certify.snap.async.AsyncDeviceLog;
import com.certify.snap.async.AsyncGetMemberData;
import com.certify.snap.async.AsyncJSONObjectGetMemberList;
import com.certify.snap.async.AsyncJSONObjectPush;
import com.certify.snap.async.AsyncJSONObjectSender;
import com.certify.snap.async.AsyncJSONObjectSetting;
import com.certify.snap.controller.AccessCardController;
import com.certify.snap.controller.ApplicationController;
import com.certify.snap.controller.CameraController;
import com.certify.snap.controller.DatabaseController;
import com.certify.snap.controller.DeviceSettingsController;
import com.certify.snap.controller.SoundController;
import com.certify.snap.model.AppStatusInfo;
import com.certify.snap.model.FaceParameters;
import com.certify.snap.model.MemberSyncDataModel;
import com.certify.snap.model.OfflineRecordTemperatureMembers;
import com.certify.snap.model.QrCodeData;
import com.certify.snap.model.RegisteredMembers;
import com.certify.snap.service.AccessTokenJobService;
import com.certify.snap.service.DeviceHealthService;
import com.certify.snap.service.MemberSyncService;
import com.common.pos.api.util.PosUtil;
import com.example.a950jnisdk.SDKUtil;
import com.google.gson.Gson;
import com.microsoft.appcenter.analytics.Analytics;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

//工具类  目前有获取sharedPreferences 方法
public class Util {
    private static final String LOG = Util.class.getSimpleName();
    private static final String TODO = "TODO";
    private static Long timeInMillis;
    private static ExecutorService taskExecutorService;
    private static String tokenRequestModule = ""; //Optimize
    private static AsyncJSONObjectSender asyncJSONObjectSender = null;

    public static final class permission {
        public static final String[] camera = new String[]{android.Manifest.permission.CAMERA};
        static final String[] phone = new String[]{android.Manifest.permission.READ_PHONE_STATE};
        public static final String[] storage = new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
        public static final String[] all = new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.READ_PHONE_STATE, android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION};
    }

    public static SharedPreferences getSharedPreferences(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        return sharedPreferences;
    }

    public static void clearAllSharedPreferences(SharedPreferences sp) {
        SharedPreferences.Editor edit = sp.edit();
        edit.clear().commit();
    }

    //sp写入string
    public static void writeString(SharedPreferences sp, String key, String value) {
        SharedPreferences.Editor edit = sp.edit();
        edit.putString(key, value);
        edit.commit();
    }

    //sp写入int
    public static void writeInt(SharedPreferences sp, String key, int value) {
        SharedPreferences.Editor edit = sp.edit();
        edit.putInt(key, value);
        edit.commit();
    }

    public static void writeFloat(SharedPreferences sp, String key, Float value) {
        SharedPreferences.Editor edit = sp.edit();
        edit.putFloat(key, value);
        edit.commit();
    }

    //sp写入boolean
    public static void writeBoolean(SharedPreferences sp, String key, Boolean value) {
        SharedPreferences.Editor edit = sp.edit();
        edit.putBoolean(key, value);
        edit.commit();
    }

    //sp写入int
    public static void writeBoolean(SharedPreferences sp, String key, boolean value) {
        SharedPreferences.Editor edit = sp.edit();
        edit.putBoolean(key, value);
        edit.commit();
    }

    public static void switchRgbOrIrActivity(Context context, Boolean isActivity) {
        if (context == null)
            return;
        Class<?> activity = IrCameraActivity.class;
        if (isDeviceProModel() && getSharedPreferences(context).getBoolean(GlobalParameters.PRO_SETTINGS, false)) {
            activity = ProIrCameraActivity.class;
        }
        if (getSharedPreferences(context).getInt(GlobalParameters.CameraType, 0) == Camera.CameraInfo.CAMERA_FACING_BACK) {
            if (isActivity) context.startActivity(new Intent(context, activity));
            else
                context.startActivity(new Intent(context, activity).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        } else {
            if (isActivity) context.startActivity(new Intent(context, IrCameraActivity.class));
            else
                context.startActivity(new Intent(context, activity).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }

//        if(isActivity) context.startActivity(new Intent(context, RgbCameraActivity.class));
//        else context.startActivity(new Intent(context, RgbCameraActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        //  context.startActivity(new Intent(context, RgbCameraActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    // 获取sn号
    public static String getSerialNumber() {
        String serial = null;
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class);
            serial = (String) get.invoke(c, "ro.serialno");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return serial;
    }

    /**
     * 获取SN号
     *
     * @return
     */
    @SuppressLint("MissingPermission")
    public static String getSNCode(Context context) {
        if (Build.VERSION.SDK_INT >= 26) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                Log.d(LOG, "No permissions getSNCode: ");
                return TODO;
            }
            Log.d(LOG, "permissions getSNCode: ");
            return android.os.Build.getSerial();
        } else if (Build.VERSION.SDK_INT >= 23 && Build.VERSION.SDK_INT <= 25) {
            return android.os.Build.SERIAL;
        } else {
            return getSerialNumber();
        }
    }

    private static Toast toast = null;

    public static void showToast(Context context, String s) {
        if (toast == null) {
            toast = Toast.makeText(context, s, Toast.LENGTH_SHORT);
            toast.show();
        } else {
            toast.setText(s);
            toast.show();
        }
    }

    /**
     * 图片旋转
     *
     * @param tmpBitmap
     * @param degrees
     * @return
     */
    public static Bitmap rotateToDegrees(Bitmap tmpBitmap, float degrees) {
        Matrix matrix = new Matrix();
        matrix.reset();
        matrix.setRotate(degrees);
        return tmpBitmap =
                Bitmap.createBitmap(tmpBitmap, 0, 0, tmpBitmap.getWidth(), tmpBitmap.getHeight(), matrix,
                        true);
    }

    //bitmap
    public static String saveBitmapFile(Bitmap bm, String fileName) throws IOException {//Bitmap
        String path = Environment.getExternalStorageDirectory() + "/pic/";
        File dirFile = new File(path);
        if (!dirFile.exists()) {
            dirFile.mkdir();
        }
        File myCaptureFile = new File(path + fileName);
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(myCaptureFile));
            bm.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return myCaptureFile.getPath();

    }

    public static void saveBitmapImage(Bitmap bm, String fileName) throws IOException {

        String path = Environment.getExternalStorageDirectory() + "/CertifySnap/Pic/";
        File dirFile = new File(path);
        if (!dirFile.exists()) {
            dirFile.mkdir();
        }
        File myCaptureFile = new File(path + fileName);
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(myCaptureFile));
            bm.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        myCaptureFile.getPath();

    }

    public static String saveAllImages(Bitmap bm, String fileName) throws IOException {//Bitmap
        String path = Environment.getExternalStorageDirectory() + "/certifysnap/registeredface/";
        File dirFile = new File(path);
        if (!dirFile.exists()) {
            dirFile.mkdir();
        }
        File myCaptureFile = new File(path + fileName);
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(myCaptureFile));
            bm.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return myCaptureFile.getPath();

    }

    public static String getnumberString(int number) {
        String numberstr = "";
        try {
            if (number >= 0 && number < 10) {
                numberstr = "0" + number;
            } else {
                numberstr = number + "";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return numberstr;
    }

    /**
     * Bitmap
     **/
    public static Bitmap zoomBitmap(Bitmap bitmap, int width, int height) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        Matrix matrix = new Matrix();
        float scaleWidth = ((float) width / w);
        float scaleHeight = ((float) height / h);
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap newbmp = Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);
        return newbmp;
    }

    /**
     *
     */
    public static Bitmap readBitMap(String path) {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inPreferredConfig = Bitmap.Config.RGB_565;
        opt.inPurgeable = true;
        opt.inInputShareable = true;
        return BitmapFactory.decodeFile(path, opt);
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    //filter
    public static Bitmap createScaleBitmap(Bitmap src, int dstWidth, int dstHeight) {
        Bitmap dst = Bitmap.createScaledBitmap(src, dstWidth, dstHeight, false);
        if (src != dst) {
            src.recycle(); // Bitmap native
        }
        return dst;
    }

    //Resources
    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options); //
        options.inSampleSize = calculateInSampleSize(options, reqWidth,
                reqHeight); // inSampleSize
        options.inJustDecodeBounds = false;
        Bitmap src = BitmapFactory.decodeResource(res, resId, options);
        return createScaleBitmap(src, reqWidth, reqHeight);
    }

    // 从sd卡上加载图片
    public static Bitmap decodeSampledBitmapFromSD(String pathName, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pathName, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        Bitmap src = BitmapFactory.decodeFile(pathName, options);
        return createScaleBitmap(src, reqWidth, reqHeight);
    }


    public static boolean isDateOneBigger(String str1, String str2) {
        boolean isBigger = false;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date dt1 = null;
        Date dt2 = null;
        try {
            dt1 = sdf.parse(str1);
            dt2 = sdf.parse(str2);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        timeInMillis = dt1.getTime() - 60 * 60 * 1000;
        if (dt1.getTime() > dt2.getTime()) {
            isBigger = true;
        } else if (dt1.getTime() < dt2.getTime()) {
            isBigger = false;
        }
        return isBigger;
    }

    public static long getTokenDateTimeDifference(String str1, String str2) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date dt1 = null;
        Date dt2 = null;
        try {
            dt1 = sdf.parse(str1);
            dt2 = sdf.parse(str2);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return (dt1.getTime() - dt2.getTime());
    }


    /**
     * 隐藏软键盘(只适用于Activity，不适用于Fragment)
     */
    public static void hideSoftKeyboard(Activity activity) {
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    //开关relay
    public static void setRelayPower(int status) {
        if (Build.MODEL.contains("950") || "TPS980Q".equals(Build.MODEL)) {
            new SDKUtil().relay_power(status);
        } else {
            PosUtil.setRelayPower(status);
        }
    }

    //led TODOMe
    public static void setLedPower(int status) {
        if (Build.MODEL.contains("950") || "TPS980Q".equals(Build.MODEL)) {
            new SDKUtil().camera_led(status);
        } else {
            PosUtil.setLedPower(status);
        }
    }

    //led2
    public static void enableLedPower(int status) {
        try {
            if (Build.MODEL.contains("950") || "TPS980Q".equals(Build.MODEL)) {
                new SDKUtil().camera_led(status);
            } else {
                PosUtil.setLedPower(status);

                //Below code is not required as the Led brightness level is controlled through the Parameter settings
                /*if (status == 1)
                    ShellUtils.execCommand("echo " + progress + " > /sys/class/backlight/led-brightness/brightness", false);*/
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断某activity是否处于栈顶
     *
     * @return true在栈顶 false不在栈顶
     */
    public static boolean isActivityTop(Class cls, Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        String name = manager.getRunningTasks(1).get(0).topActivity.getClassName();
        return name.equals(cls.getName());
    }

    /**
     * 检查时间格式是否正确
     *
     * @param str    时间格式字符
     */
    public static boolean isValidDate(String str, String pattern) {
        boolean convertSuccess = true;
//        String pattern = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        try {
            // 设置lenient为false. 否则SimpleDateFormat会比较宽松地验证日期，比如2007/02/29会被接受，并转换成2007/03/01
            if (str.length() != pattern.length()) return false;

            format.setLenient(false);
            format.parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
            // 如果throw java.text.ParseException或者NullPointerException，就说明格式不对
            convertSuccess = false;
        }
        return convertSuccess;
    }

    public static String getMMDDYYYYDate() {
        try {
            SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
            Date curDate = new Date(System.currentTimeMillis());//获取当前时间
            return format.format(curDate);
        } catch (Exception e) {
            e.printStackTrace();
            // 如果throw java.text.ParseException或者NullPointerException，就说明格式不对

        }
        return "";
    }

    public static String getUTCDate(String str) {
        try {
            final SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            if (str.isEmpty()) {
                f.setTimeZone(TimeZone.getTimeZone("UTC"));
                return f.format(new Date());
            } else {
                Date localTime = new Date(str);
                f.setTimeZone(TimeZone.getTimeZone("UTC"));
                return f.format(localTime);
            }
        } catch (Exception e) {
            e.printStackTrace();
            // 如果throw java.text.ParseException或者NullPointerException，就说明格式不对

        }
        return "";
    }

    public static String getDate(String str) {
        try {
            final SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            if (str.isEmpty()) {
                return f.format(new Date());
            } else {
                Date localTime = new Date(str);
                return f.format(localTime);
            }
        } catch (Exception e) {
            Log.e(LOG, "Error in formatting the date " + e.getMessage());
        }
        return "";
    }

    public static float FahrenheitToCelcius(float celcius) {

        return ((celcius * 9) / 5) + 32;
    }

    public static void error(String classname, String message) {
        Analytics.trackEvent("Error:" + message);

    }

    public static String encodeImagePath(String path) {
        File imagefile = new File(path);
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(imagefile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return "";//appcenter:285232885u
        }
        Bitmap bm = BitmapFactory.decodeStream(fis);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] b = baos.toByteArray();
        String encImage = Base64.encodeToString(b, Base64.DEFAULT);
        //Base64.de
        return encImage;

    }

    public static String encodeToBase64(Bitmap image) {
        Bitmap immage = image;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        immage.compress(Bitmap.CompressFormat.PNG, 80, baos);
        byte[] b = baos.toByteArray();
        String imageEncoded = Base64.encodeToString(b, Base64.DEFAULT);
        //   Log.d("imageEncoded",""+imageEncoded.length());
        return imageEncoded;
    }

    public static Bitmap decodeToBase64(String input) {
        try {
            byte[] decodedByte = Base64.decode(input, 0);
            return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
        } catch (Exception e) {
            Logger.debug("Bitmap decodeToBase64(String input) ", e.getMessage());
        }
        return null;
    }

    public static void getToken(JSONObjectCallback callback, Context context) {
        try {
            SharedPreferences sharedPreferences = Util.getSharedPreferences(context);

            Log.d(LOG, "Get Token");
            JSONObject obj = new JSONObject();
            //  obj.put("DeviceSN", Util.getSerialNumber());
            new AsyncJSONObjectSender(obj, callback, sharedPreferences.getString(GlobalParameters.URL, EndPoints.prod_url) + EndPoints.GenerateToken, context).execute();

            /*String expire_time = sharedPreferences.getString(GlobalParameters.EXPIRE_TIME, "");
            if (!expire_time.isEmpty() && expire_time != null) {
                String expireTime = getUTCDate(expire_time);
                String currentTime = currentDate();
                if (isDateOneBigger(expireTime, currentTime)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        scheduleJobAccessToken(context);
                    }
                }
            }*/

        } catch (Exception e) {
            Logger.error(LOG + "getToken(JSONObjectCallback callback, Context context) ", e.getMessage());

        }
    }

    public static void scheduleJobAccessToken(Context context) {
        ComponentName componentName = new ComponentName(context, AccessTokenJobService.class);
        JobInfo jobInfo = new JobInfo.Builder(1, componentName)
                .setPeriodic(timeInMillis, 5 * 60 * 1000).setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
                .setPersisted(true).build();
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(context.JOB_SCHEDULER_SERVICE);
        jobScheduler.schedule(jobInfo);
    }

    public static void getSettings(SettingCallback callback, Context context) {
        try {
            SharedPreferences sharedPreferences = Util.getSharedPreferences(context);

            JSONObject obj = new JSONObject();
            obj.put("deviceSN", Util.getSNCode(context));//Util.getSNCode()
            obj.put("institutionId", sharedPreferences.getString(GlobalParameters.INSTITUTION_ID, ""));

            new AsyncJSONObjectSetting(obj, callback, sharedPreferences.getString(GlobalParameters.URL, EndPoints.prod_url) + EndPoints.DEVICESETTING, context).execute();

        } catch (Exception e) {
            Util.switchRgbOrIrActivity(context, true);
            Logger.error(LOG + "getSettings(JSONObjectCallback callback, Context context)", e.getMessage());

        }
    }


    public static String getJSONObject(JSONObject req, String url, String header, Context context) {
        try {
            String responseTemp = Requestor.requestJson(url, req, Util.getSNCode(context), context, "");
            if (responseTemp != null && !responseTemp.equals("")) {
                return new String(responseTemp);
            }
        } catch (Exception e) {

            Logger.error(LOG + "getJSONObject(JSONObject req, String url): req = " + req
                    + ", url = " + url, e.getMessage());
            return null;

        }
        return null;
    }

    public static JSONObject getJSONObjectLogin(String req, String url, String header, Context context) {
        try {
            String responseTemp = Requestor.postJsonLogin(url, req, "");
            if (responseTemp != null && !responseTemp.equals("")) {
                return new JSONObject(responseTemp);
            }
        } catch (Exception e) {
            Logger.error(LOG + "getJSONObjectLogin", e.getMessage());
            return null;
        }
        return null;
    }

    public static String getJSONObjectAddmember(JSONObject req, String url, String header, Context context) {
        try {
            String responseTemp = Requestor.requestJson(url, req, Util.getSNCode(context), context, "device_sn");
            if (responseTemp != null && !responseTemp.equals("")) {
                return new String(responseTemp);
            }
        } catch (Exception e) {

            Logger.error(LOG + "getJSONObject(JSONObject req, String url): req = " + req
                    + ", url = " + url, e.getMessage());
            return null;

        }
        return null;
    }


    public static JSONObject getJSONObjectSetting(JSONObject req, String url, String header, Context context) {
        try {
            String responseTemp = Requestor.postJson(url, req, context);
            if (responseTemp != null && !responseTemp.equals("")) {
                return new JSONObject(responseTemp);
            }
        } catch (Exception e) {
            Logger.error(LOG + "getJSONObject(JSONObject req, String url): req = " + req
                    + ", url = " + url, e.getMessage());
            return null;

        }
        return null;
    }

    public static JSONObject getJSONObjectQRCode(JSONObject req, String url, String header, Context context) {
        try {
            String responseTemp = Requestor.postJson(url, req, context);
            if (responseTemp != null && !responseTemp.equals("")) {
                return new JSONObject(responseTemp);
            }
        } catch (Exception e) {
            Logger.error(LOG + "getJSONObject(JSONObject req, String url): req = " + req
                    + ", url = " + url, e.getMessage());
            return null;

        }
        return null;
    }

    public static JSONObject getJSONObjectAddDevice(JSONObject req, String url, String header, Context context) {
        try {
            String responseTemp = Requestor.postJsonAdmin(url, req, context);
            if (responseTemp != null && !responseTemp.equals("")) {
                return new JSONObject(responseTemp);
            }
        } catch (Exception e) {
            Logger.error(LOG + "getJSONObject(JSONObject req, String url): req = " + req
                    + ", url = " + url, e.getMessage());
            return null;

        }
        return null;
    }

    public static JSONObject getJSONObjectTemp(JSONObject req, String url, String header, Context context) {
        try {
            String responseTemp = Requestor.postJson(url, req, context);
            if (responseTemp != null && !responseTemp.equals("")) {
                return new JSONObject(responseTemp);
            }
        } catch (Exception e) {

            Logger.error(LOG + "getJSONObject(JSONObject req, String url): req = " + req
                    + ", url = " + url, e.getMessage());
            return null;

        }
        return null;
    }

    public static boolean isConnectingToInternet(Context context) {
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private static void saveOfflineTempRecord(JSONObject obj, Context context, UserExportedData data, int offlineSyncStatus) {
        if (AppSettings.isLogOfflineDataEnabled()) {
            try {
                OfflineRecordTemperatureMembers offlineRecordTemperatureMembers = new OfflineRecordTemperatureMembers();
                offlineRecordTemperatureMembers.setTemperature(obj.getString("temperature"));
                offlineRecordTemperatureMembers.setJsonObj(obj.toString());
                offlineRecordTemperatureMembers.setDeviceTime(obj.getString("deviceTime"));
                offlineRecordTemperatureMembers.setUtcTime(obj.getString("utcTime"));
                offlineRecordTemperatureMembers.setImagepath(data.member.getImage());
                offlineRecordTemperatureMembers.setPrimaryid(OfflineRecordTemperatureMembers.lastPrimaryId());
                offlineRecordTemperatureMembers.setOfflineSync(offlineSyncStatus);
                if (data.member.getFirstname() != null) {
                    offlineRecordTemperatureMembers.setMemberId(data.member.getMemberid());
                    offlineRecordTemperatureMembers.setFirstName(data.member.getFirstname());
                    offlineRecordTemperatureMembers.setLastName(data.member.getLastname());
                } else {
                    offlineRecordTemperatureMembers.setFirstName("Anonymous");
                    offlineRecordTemperatureMembers.setLastName("");
                }
                //offlineRecordTemperatureMembers.save();
                DatabaseController.getInstance().insertOfflineMemberIntoDB(offlineRecordTemperatureMembers);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static String FaceParameters(Context context, UserExportedData data) {
        String value = "";
        SharedPreferences sp = Util.getSharedPreferences(context);
        if (AppSettings.isFacialDetect()) {
            String thresholdFacialPreference = sp.getString(GlobalParameters.FACIAL_THRESHOLD, String.valueOf(Constants.FACIAL_DETECT_THRESHOLD));
            int thresholdvalue = Integer.parseInt(thresholdFacialPreference);
            value = "thresholdValue:" + thresholdvalue + ", " +
                    "faceScore:" + data.faceScore;
            FaceParameters faceParameters = CameraController.getInstance().getFaceParameters();
            if (faceParameters != null) {
                value = value + ", " + "age:" + faceParameters.age + ", " +
                        "gender:" + faceParameters.gender + ", " +
                        "maskStatus:" + faceParameters.maskStatus + ", " +
                        "faceShelter:" + faceParameters.faceShelter + ", " +
                        "face3DAngle:" + faceParameters.face3DAngle + ", " +
                        "liveness:" + faceParameters.liveness;
            }
        }
        return value;
    }

    private static void updateFaceMemberValues(JSONObject obj, UserExportedData data, Context context) {
        try {
            if (data.member == null) data.member = new RegisteredMembers();
            obj.put("id", data.member.getUniqueid());
            if (getSharedPreferences(context).getString(GlobalParameters.anonymousFirstName, "").isEmpty()) {
                obj.put("firstName", data.member.getFirstname());
                obj.put("lastName", data.member.getLastname());
            } else {
                obj.put("firstName", getSharedPreferences(context).getString(GlobalParameters.anonymousFirstName, ""));
                obj.put("lastName", getSharedPreferences(context).getString(GlobalParameters.anonymousLastName, ""));
                obj.put("vaccineDocumentName", String.format("%s_%s", getSharedPreferences(context).getString(GlobalParameters.anonymousFirstName, "").replace(" ", ""), Util.getUTCDateMMDDYYYYHHMMSS()));
                obj.put("vaccinationStatus", "1");
            }
            obj.put("memberId", data.member.getMemberid());
            obj.put("memberTypeId", data.member.getMemberType());
            obj.put("memberTypeName", data.member.getMemberTypeName());
            obj.put("trqStatus", ""); //Send this empty if not Qr
            obj.put("networkId", data.member.getNetworkId());
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public static int getBatteryLevel(Context context) {
        try {
            IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = context.registerReceiver(null, intentFilter);
            int level = 1;
            if (batteryStatus != null)
                level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            return level;
        } catch (Exception e) {
            Log.e(LOG + "getBatteryLevel()", e.getMessage());
            return -1;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void activateApplication(JSONObjectCallback callback, Context context) {
        try {
            SharedPreferences sp = Util.getSharedPreferences(context);

            JSONObject obj = new JSONObject();
            obj.put("pushAuthToken", sp.getString(GlobalParameters.Firebase_Token, ""));
            obj.put("deviceInfo", MobileDetails(context));

            new AsyncJSONObjectSender(obj, callback, sp.getString(GlobalParameters.URL, EndPoints.prod_url) + EndPoints.ActivateApplication, context).execute();

        } catch (Exception e) {
            Logger.error(LOG, "getToken " + e.getMessage());
            //TODO:warn failed to activate
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static JSONObject MobileDetails(Context context) {
        JSONObject obj = new JSONObject();
        try {
            SharedPreferences sp = Util.getSharedPreferences(context);

            getNumberVersion(context);
            getDeviceUUid(context);

            obj.put("osVersion", "Android - " + Build.VERSION.RELEASE);
            obj.put("appVersion", getVersionBuild());
            obj.put("mobileIp", Util.getLocalIpAddress());
            obj.put("mobileNumber", sp.getString(GlobalParameters.MOBILE_NUMBER, "+1"));
            obj.put("uniqueDeviceId", sp.getString(GlobalParameters.UUID, ""));
            obj.put("IMEINumber", sp.getString(GlobalParameters.IMEI, ""));
            obj.put("deviceModel", Build.MODEL);
            obj.put("deviceSN", Util.getSerialNumber());
            obj.put("batteryStatus", getBatteryLevel(context));
            obj.put("networkStatus", isConnectingToInternet(context));
            obj.put("appState", getAppState());

        } catch (Exception e) {
            Log.e(LOG + "MobileDetailsData ", e.getMessage());

        }
        return obj;
    }

    public static Bitmap faceValidation(byte[] oldImage) {
        return BitmapFactory.decodeByteArray(oldImage, 0, oldImage.length);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint({"MissingPermission", "HardwareIds"})
    public static void getDeviceUUid(Context context) {
        SharedPreferences sp = Util.getSharedPreferences(context);

        if (!sp.getString(GlobalParameters.UUID, "").isEmpty())
            return;
        String deviceUUid = null;
        try {
            try {
                if (!Util.PermissionRequest(context, permission.phone))
                    //noinspection ConstantConditions
                    deviceUUid = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
                Util.writeString(sp, GlobalParameters.IMEI, deviceUUid);


            } catch (Exception ignored) {
            }
            try {
                if (deviceUUid == null)
                    deviceUUid = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            } catch (Exception ignored) {
                deviceUUid = UUID.randomUUID().toString();
                Util.writeString(sp, GlobalParameters.UUID, deviceUUid);
            }

        } catch (Exception e) {
            deviceUUid = UUID.randomUUID().toString();
            Util.writeString(sp, GlobalParameters.UUID, deviceUUid);
        }
        Util.writeString(sp, GlobalParameters.UUID, deviceUUid);
    }


    public static String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
            Log.e("getLocalIpAddress()", ex.getMessage());
        }
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint("MissingPermission")
    public static void getNumberVersion(Context context) {
        try {
            SharedPreferences sp = Util.getSharedPreferences(context);

            TelephonyManager tMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (tMgr != null) {
                if (!Util.PermissionRequest(context, permission.phone))
                    Util.writeString(sp, GlobalParameters.MOBILE_NUMBER, tMgr.getLine1Number());
            }
        } catch (Exception e) {
            Log.e(LOG + "getNumberVersion()", e.getMessage());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static boolean PermissionRequest(Context context, String[] permissions) {
        try {
            if (permissions == null) return false;
            ArrayList<String> requestPermission = new ArrayList<>();
            for (String permission : permissions) {
                int permissionCheck = context.checkSelfPermission(permission);
                if (permissionCheck != PackageManager.PERMISSION_GRANTED)
                    requestPermission.add(permission);
            }
            if (requestPermission.size() <= 0) return false;
            // context.requestPermissions(requestPermission.toArray(new String[0]), 1);
        } catch (Exception e) {
            Logger.error(LOG, "PermissionRequest(android.app.Activity context, String[] permissions" + e.getMessage());
        }
        return true;
    }

    public static Bitmap convertYuvByteArrayToBitmap(byte[] data, Camera camera) {
        try {
            Camera.Parameters parameters = camera.getParameters();
            return convertYuvByteArrayToBitmap(data, parameters);
        } catch (Exception e) {
            Logger.error(LOG, "convertYuvByteArrayToBitmap", "Conversion from byte to bitmap failed");
            return null;
        }
    }

    public static Bitmap convertYuvByteArrayToBitmap(byte[] data, Camera.Parameters cameraParameters) {
        if (data == null || cameraParameters == null) {
            Log.w(LOG, String.format("convertYuvByteArrayToBitmap data: %s, cameraParameters: %s", data == null, cameraParameters == null));
            return null;
        }
        Camera.Size size = cameraParameters.getPreviewSize();
        YuvImage image = new YuvImage(data, cameraParameters.getPreviewFormat(), size.width, size.height, null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        image.compressToJpeg(new Rect(0, 0, size.width, size.height), 80, out);
        byte[] imageBytes = out.toByteArray();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        options.inSampleSize = 3;
        Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length, options);
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    public static void KillApp() {
        try {
            Process suProcess = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(suProcess.getOutputStream());

            os.writeBytes("adb shell" + "\n");

            os.flush();

            os.writeBytes("am force-stop com.telpo.temperatureservice" + "\n");

            os.flush();

//            int pid = getPid(com.XXX);
//            android.os.Process.killProcess(android.os.Process.myPid());

        } catch (Exception e) {
            Log.e(LOG + "KillApp()", e.getMessage());
        }
    }


    public static boolean isServiceRunning(Class<?> serviceClass, Context context) {
        try {
            ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (serviceClass.getName().equals(service.service.getClassName())) {
                    return true;
                }
            }
        } catch (Exception e) {
            Logger.error(LOG + "isServiceRunning(Class<?> serviceClass)", e.getMessage());
        }
        return false;
    }

    public static long getCurrentTimeLong() {
        try {
            Calendar serverTime = new GregorianCalendar(TimeZone.getTimeZone(getCurrentTimezoneOffset()));
            serverTime.setTimeInMillis(System.currentTimeMillis());
            return serverTime.getTimeInMillis() / 1000 * 1000;
        } catch (Exception e) {
            Log.e(LOG + "getCurrentTime()", e.getMessage());
            return 0;
        }
    }

    public static String getCurrentTimezoneOffset() {
        TimeZone tz = TimeZone.getDefault();
        Calendar cal = GregorianCalendar.getInstance(tz);
        int offsetInMillis = tz.getOffset(cal.getTimeInMillis());
        String offset = String.format(Locale.ENGLISH, "%02d:%02d", Math.abs(offsetInMillis / 3600000), Math.abs((offsetInMillis / 60000) % 60));
        offset = "GMT" + (offsetInMillis >= 0 ? "+" : "-") + offset;

        return offset;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void getDeviceHealthCheck(JSONObjectCallback callback, Context context) {
        try {
            SharedPreferences sharedPreferences = getSharedPreferences(context);

            JSONObject obj = new JSONObject();
            obj.put("lastUpdateDateTime", getUTCDate(""));
            obj.put("deviceSN", getSNCode(context));
            obj.put("deviceInfo", MobileDetails(context));
            obj.put("institutionId", sharedPreferences.getString(GlobalParameters.INSTITUTION_ID, ""));
            obj.put("appState", getAppState());
            obj.put("appUpTime", getAppUpTime(context));
            obj.put("deviceUpTime", getDeviceUpTime());

            Log.d(LOG, "Health check time " + getMMDDYYYYDate());

            if (!Util.isOfflineMode(context))
                new AsyncJSONObjectSender(obj, callback, sharedPreferences.getString(GlobalParameters.URL, EndPoints.prod_url) + EndPoints.DEVICEHEALTHCHECK, context).execute();
            sendOfflineServiceBroadcast(context);
        } catch (Exception e) {
            Logger.error(LOG + "getDeviceHealthCheck Error ", e.getMessage());

        }
    }

    public static String getAppState() {
        String appState = "Foreground";
        if (ApplicationLifecycleHandler.isInBackground)
            appState = "Background";
        return appState;
    }

    public static boolean activeEngineOffline(Context context) {
        boolean result = false;
        String path = Environment.getExternalStorageDirectory() + "/active_result.dat";
        String path1 = Environment.getExternalStorageDirectory() + "/ArcFacePro32.dat";
        String path2 = context.getApplicationContext().getFilesDir() + "/ArcFacePro32.dat";
        File file = new File(path);
        if (file.exists()) {
//            int activeCode = FaceEngine.activeOffline(context,
//                    path);
//            if (activeCode == ErrorInfo.MOK) {
//                result=true;
//                Log.e("active_result","true  1");
//            } else if (activeCode == ErrorInfo.MERR_ASF_ALREADY_ACTIVATED) {
//                result=true;
//                Log.e("active_result","true  2");
//            } else {
//                result=false;
//                Log.e("active_result","false  1");
//            }
        } else {
            File file1 = new File(path1);
            if (file1.exists()) {
                copyFile(path1, path2);
                File file2 = new File(path2);
                if (file2.exists()) {
                    result = true;
                }
            } else {
                Log.e("active_result", "false  no .dat file");
            }
        }

        return result;
    }

    public static boolean copyFile(String filePath, String destPath) {
        File originFile = new File(filePath);

        if (!originFile.exists()) {
            Log.e("yw_lisence", "lisence not exist");
            return false;
        }
        File destFile = new File(destPath);
        BufferedInputStream reader = null;
        BufferedOutputStream writer = null;
        try {
            if (!destFile.exists()) {
                destFile.createNewFile();
            }
            reader = new BufferedInputStream(new FileInputStream(originFile));
            writer = new BufferedOutputStream(new FileOutputStream(destFile));
            byte[] buffer = new byte[1024];
            int length;
            while ((length = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, length);
            }
        } catch (Exception exception) {
            return false;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                }
            }
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                }
            }
        }
        return true;
    }

    public static String logHeap() {
        Double allocated = new Double(Debug.getNativeHeapAllocatedSize()) / new Double((1048576));
        Double available = new Double(Debug.getNativeHeapSize()) / 1048576.0;
        Double free = new Double(Debug.getNativeHeapFreeSize()) / 1048576.0;
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        df.setMinimumFractionDigits(2);
        return String.format("heap native: allocated %s MB of %s MB(%s MB free)", df.format(allocated), df.format(available), df.format(free));
        // Log.d("tag", "debug.heap native: allocated " + df.format(allocated) + "MB of " + df.format(available) + "MB (" + df.format(free) + "MB free)");
        //Log.d("tag", "debug.memory: allocated: " + df.format(new Double(Runtime.getRuntime().totalMemory()/1048576)) + "MB of " + df.format(new Double(Runtime.getRuntime().maxMemory()/1048576))+ "MB (" + df.format(new Double(Runtime.getRuntime().freeMemory()/1048576)) +"MB free)");
    }

    public static void retrieveSetting(JSONObject reportInfo, Context context) {
        SharedPreferences sharedPreferences = Util.getSharedPreferences(context);
        try {
            if (reportInfo.has("responseCode") && reportInfo.getString("responseCode") != null && reportInfo.getString("responseCode").equals("1")) {
                Gson gson = new Gson();
                SettingsResponse settingsResponse = gson.fromJson(String.valueOf(reportInfo), SettingsResponse.class);

                SettingsData settingsData = settingsResponse.settingsData;
                if (settingsData != null) {
                    DeviceSettingsController.getInstance().clearLanguageSettings();
                    Util.writeString(sharedPreferences, GlobalParameters.DEVICE_NAME, settingsData.deviceName);
                    Util.writeString(sharedPreferences, GlobalParameters.DEVICE_SETTINGS_NAME, settingsData.settingName);
                    Util.writeString(sharedPreferences, GlobalParameters.settingVersion, settingsData.settingVersion);
                    Util.writeString(sharedPreferences, GlobalParameters.deviceMasterCode, settingsData.deviceMasterCode);

                    DeviceSettingsApi deviceSettingsApi = settingsData.deviceSettingsApi;

                    //Device Settings
                    DeviceSettingsData deviceSettings = deviceSettingsApi.deviceSettingsData;
                    if (deviceSettings != null) {
                        if (deviceSettings.doNotSyncMembers.equals("1")) {
                            Util.writeBoolean(sharedPreferences, GlobalParameters.SYNC_ONLINE_MEMBERS, true);
                        } else {
                            Util.writeBoolean(sharedPreferences, GlobalParameters.SYNC_ONLINE_MEMBERS, false);
                        }
                        Util.writeBoolean(sharedPreferences, GlobalParameters.MEMBER_GROUP_SYNC, deviceSettings.syncMemberGroup.equals("1"));
                        if (deviceSettings.groupId.isEmpty()) {
                            deviceSettings.groupId = "0";
                        }
                        if (AppSettings.isMemberGroupSyncEnabled() != deviceSettings.syncMemberGroup.equals("1")) {
                            Util.writeBoolean(sharedPreferences, GlobalParameters.MEMBER_DELTA_SYNC_ENABLED, false);
                        } else {
                            if (deviceSettings.syncMemberGroup.equals("1")) {
                                if (!AppSettings.getMemberSyncGroupId().equals(deviceSettings.groupId)) {
                                    Util.writeBoolean(sharedPreferences, GlobalParameters.MEMBER_DELTA_SYNC_ENABLED, false);
                                }
                            }
                        }
                        Util.writeString(sharedPreferences, GlobalParameters.MEMBER_GROUP_ID, deviceSettings.groupId);
                        Util.writeString(sharedPreferences, GlobalParameters.deviceSettingMasterCode, deviceSettings.deviceMasterCode);
                        Util.writeBoolean(sharedPreferences, GlobalParameters.NavigationBar, deviceSettings.navigationBar.equals("1"));
                        Util.writeBoolean(sharedPreferences, GlobalParameters.PRO_SETTINGS, false);
                        Util.writeBoolean(sharedPreferences, GlobalParameters.LANGUAGE_ALLOW_MULTILINGUAL, deviceSettings.allowMultilingual.equals("1"));
                        Util.writeBoolean(sharedPreferences, GlobalParameters.DEBUG_MODE, deviceSettings.debugMode.equals("1"));
                    }

                    //HomeView settings
                    HomePageSettings homePageSettings = deviceSettingsApi.homePageView;
                    if (homePageSettings != null) {
                        Util.writeString(sharedPreferences, GlobalParameters.IMAGE_ICON, homePageSettings.logo);
                        Util.writeString(sharedPreferences, GlobalParameters.Thermalscan_title, homePageSettings.line1);
                        Util.writeString(sharedPreferences, GlobalParameters.Thermalscan_subtitle, homePageSettings.line2);
                        Util.writeBoolean(sharedPreferences, GlobalParameters.HOME_TEXT_IS_ENABLE, homePageSettings.enableHomeScreen.equals("1"));
                        Util.writeInt(sharedPreferences, GlobalParameters.HOME_DISPLAY_TIME, Integer.parseInt(homePageSettings.viewIntervalDelay));
                        Util.writeBoolean(sharedPreferences, GlobalParameters.HOME_TEXT_ONLY_IS_ENABLE, homePageSettings.enableTextOnly.equals("1"));
                        Util.writeString(sharedPreferences, GlobalParameters.HOME_TEXT_ONLY_MESSAGE, homePageSettings.homeText);
                    }

                    //ScanView settings
                    ScanViewSettings scanViewSettings = deviceSettingsApi.scanView;
                    if (scanViewSettings != null) {
                        Util.writeString(sharedPreferences, GlobalParameters.DELAY_VALUE, scanViewSettings.viewDelay);
                        Util.writeBoolean(sharedPreferences, GlobalParameters.CAPTURE_IMAGES_ABOVE, scanViewSettings.captureUserImageAboveThreshold.equals("1"));
                        Util.writeBoolean(sharedPreferences, GlobalParameters.CAPTURE_IMAGES_ALL, scanViewSettings.captureAllUsersImage.equals("1"));
                        Util.writeBoolean(sharedPreferences, GlobalParameters.TEMPERATURE_SOUND_NORMAL, scanViewSettings.enableSoundOnNormalTemperature.equals("1"));
                        Util.writeBoolean(sharedPreferences, GlobalParameters.TEMPERATURE_SOUND_HIGH, scanViewSettings.enableSoundOnHighTemperature.equals("1"));
                        Util.writeBoolean(sharedPreferences, GlobalParameters.CAPTURE_TEMPERATURE, scanViewSettings.displayTemperatureDetail.equals("1"));
                        Util.writeString(sharedPreferences, GlobalParameters.TEMP_TEST, scanViewSettings.temperatureThreshold);
                        Util.writeString(sharedPreferences, GlobalParameters.F_TO_C, scanViewSettings.temperatureFormat);
                        Util.writeString(sharedPreferences, GlobalParameters.TEMP_TEST_LOW, scanViewSettings.lowTemperatureThreshold);
                        Util.writeBoolean(sharedPreferences, GlobalParameters.ALLOW_ALL, scanViewSettings.allowLowTemperatureScanning.equals("1"));
                        Util.writeBoolean(sharedPreferences, GlobalParameters.MASK_DETECT, scanViewSettings.enableMaskDetection.equals("1"));
                        Util.writeFloat(sharedPreferences, GlobalParameters.COMPENSATION, Float.parseFloat(scanViewSettings.temperatureCompensation));
                        Util.writeBoolean(sharedPreferences, GlobalParameters.ScanProximity, scanViewSettings.closeProximityScan.equals("1"));
                        Util.writeBoolean(sharedPreferences, GlobalParameters.RESULT_BAR, scanViewSettings.displayResultBar.equals("1"));
                        Util.writeInt(sharedPreferences, GlobalParameters.ScanType, Integer.parseInt(scanViewSettings.scanType));
                        Util.writeBoolean(sharedPreferences, GlobalParameters.EnableTempScan, scanViewSettings.enableTemperatureScan.equals("1"));
                        Util.writeBoolean(sharedPreferences, GlobalParameters.LivingType, scanViewSettings.enableLiveness.equals("1"));
                        Util.writeString(sharedPreferences, GlobalParameters.RESULT_BAR_NORMAL, scanViewSettings.temperatureNormal);
                        Util.writeString(sharedPreferences, GlobalParameters.RESULT_BAR_HIGH, scanViewSettings.temperatureHigh);
                        Util.writeBoolean(sharedPreferences, GlobalParameters.RETRY_SCAN, scanViewSettings.retryOptionFaceScan.equals("1"));

                        if (scanViewSettings.audioForNormalTemperature != null && !scanViewSettings.audioForNormalTemperature.isEmpty()) {
                            SoundController.getInstance().saveAudioFile(scanViewSettings.audioForNormalTemperature, "Normal.mp3");
                        } else {
                            SoundController.getInstance().deleteAudioFile("Normal.mp3");
                        }
                        if (scanViewSettings.audioForHighTemperature != null && !scanViewSettings.audioForHighTemperature.isEmpty()) {
                            SoundController.getInstance().saveAudioFile(scanViewSettings.audioForHighTemperature, "High.mp3");
                        } else {
                            SoundController.getInstance().deleteAudioFile("High.mp3");
                        }
                    }

                    //ConfirmationView settings
                    ConfirmationViewSettings confirmationSettings = deviceSettingsApi.confirmationView;
                    if (confirmationSettings != null) {
                        Util.writeBoolean(sharedPreferences, GlobalParameters.CONFIRM_SCREEN_BELOW, confirmationSettings.enableConfirmationScreen.equals("1"));
                        Util.writeBoolean(sharedPreferences, GlobalParameters.CONFIRM_SCREEN_ABOVE, confirmationSettings.enableConfirmationScreenAboveThreshold.equals("1"));
                        Util.writeString(sharedPreferences, GlobalParameters.DELAY_VALUE_CONFIRM_BELOW, confirmationSettings.viewDelay);
                        Util.writeString(sharedPreferences, GlobalParameters.DELAY_VALUE_CONFIRM_ABOVE, confirmationSettings.viewDelayAboveThreshold);
                        Util.writeString(sharedPreferences, GlobalParameters.Confirm_title_below, confirmationSettings.normalViewLine1);
                        Util.writeString(sharedPreferences, GlobalParameters.Confirm_subtitle_below, confirmationSettings.normalViewLine2);
                        Util.writeString(sharedPreferences, GlobalParameters.Confirm_title_above, confirmationSettings.aboveThresholdViewLine1);
                        Util.writeString(sharedPreferences, GlobalParameters.Confirm_subtitle_above, confirmationSettings.temperatureAboveThreshold2);
                        Util.writeBoolean(sharedPreferences, GlobalParameters.SHOW_VACCINATION_INDICATOR, confirmationSettings.showVaccinationIndicator.equals("1"));
                        Util.writeBoolean(sharedPreferences, GlobalParameters.SHOW_NON_VACCINATION_INDICATOR, confirmationSettings.showNonVaccinationIndicator.equals("1"));
                    }

                    //GuideView Settings
                    GuideSettings guideSettings = deviceSettingsApi.guideMessages;
                    if (guideSettings != null) {
                        Util.writeBoolean(sharedPreferences, GlobalParameters.GUIDE_SCREEN, guideSettings.enableGuideMessages.equals("1"));
                        Util.writeString(sharedPreferences, GlobalParameters.GUIDE_TEXT1, guideSettings.message1);
                        Util.writeString(sharedPreferences, GlobalParameters.GUIDE_TEXT2, guideSettings.message2);
                        Util.writeString(sharedPreferences, GlobalParameters.GUIDE_TEXT3, guideSettings.message3);
                    }

                    //Identification Settings
                    IdentificationSettings identificationSettings = deviceSettingsApi.identificationSettings;
                    if (identificationSettings != null) {
                        if (deviceSettingsApi.settingVersion.isEmpty()) {
                            if ((identificationSettings.enableQRCodeScanner.equals("1") &&
                                    identificationSettings.enableRFIDScanner.equals("1"))) {
                                Util.writeString(sharedPreferences, GlobalParameters.PRIMARY_IDENTIFIER, String.valueOf(CameraController.PrimaryIdentification.QRCODE_OR_RFID.getValue()));
                                if (identificationSettings.enableFacialRecognition.equals("1")) {
                                    Util.writeString(sharedPreferences, GlobalParameters.SECONDARY_IDENTIFIER, String.valueOf(CameraController.SecondaryIdentification.FACE.getValue()));
                                } else {
                                    Util.writeString(sharedPreferences, GlobalParameters.SECONDARY_IDENTIFIER, String.valueOf(CameraController.SecondaryIdentification.NONE.getValue()));
                                }
                            } else if ((identificationSettings.enableQRCodeScanner.equals("1") &&
                                    identificationSettings.enableFacialRecognition.equals("1"))) {
                                Util.writeString(sharedPreferences, GlobalParameters.PRIMARY_IDENTIFIER, String.valueOf(CameraController.PrimaryIdentification.QRCODE_OR_RFID.getValue()));
                                Util.writeString(sharedPreferences, GlobalParameters.SECONDARY_IDENTIFIER, String.valueOf(CameraController.SecondaryIdentification.FACE.getValue()));
                            } else if ((identificationSettings.enableRFIDScanner.equals("1") &&
                                    identificationSettings.enableFacialRecognition.equals("1"))) {
                                Util.writeString(sharedPreferences, GlobalParameters.PRIMARY_IDENTIFIER, String.valueOf(CameraController.PrimaryIdentification.FACE_OR_RFID.getValue()));
                                Util.writeString(sharedPreferences, GlobalParameters.SECONDARY_IDENTIFIER, String.valueOf(CameraController.SecondaryIdentification.NONE.getValue()));
                            } else if (identificationSettings.enableQRCodeScanner.equals("1")) {
                                Util.writeString(sharedPreferences, GlobalParameters.PRIMARY_IDENTIFIER, String.valueOf(CameraController.PrimaryIdentification.QR_CODE.getValue()));
                                Util.writeString(sharedPreferences, GlobalParameters.SECONDARY_IDENTIFIER, String.valueOf(CameraController.SecondaryIdentification.NONE.getValue()));
                            } else if ((identificationSettings.enableRFIDScanner.equals("1"))) {
                                Util.writeString(sharedPreferences, GlobalParameters.PRIMARY_IDENTIFIER, String.valueOf(CameraController.PrimaryIdentification.RFID.getValue()));
                                Util.writeString(sharedPreferences, GlobalParameters.SECONDARY_IDENTIFIER, String.valueOf(CameraController.SecondaryIdentification.NONE.getValue()));
                            } else if ((identificationSettings.enableFacialRecognition.equals("1"))) {
                                Util.writeString(sharedPreferences, GlobalParameters.PRIMARY_IDENTIFIER, String.valueOf(CameraController.PrimaryIdentification.FACE.getValue()));
                                Util.writeString(sharedPreferences, GlobalParameters.SECONDARY_IDENTIFIER, String.valueOf(CameraController.SecondaryIdentification.NONE.getValue()));
                            }
                        } else {
                            Util.writeString(sharedPreferences, GlobalParameters.PRIMARY_IDENTIFIER, identificationSettings.enablePrimary);
                            Util.writeString(sharedPreferences, GlobalParameters.SECONDARY_IDENTIFIER, identificationSettings.enabledSecondary);
                        }
                        //Util.writeBoolean(sharedPreferences, GlobalParameters.QR_SCREEN, identificationSettings.enableQRCodeScanner.equals("1"));
                        //Util.writeBoolean(sharedPreferences, GlobalParameters.RFID_ENABLE, identificationSettings.enableRFIDScanner.equals("1"));
                        Util.writeString(sharedPreferences, GlobalParameters.Timeout, identificationSettings.identificationTimeout);
                        //Util.writeBoolean(sharedPreferences, GlobalParameters.FACIAL_DETECT, identificationSettings.enableFacialRecognition.equals("1"));
                        Util.writeString(sharedPreferences, GlobalParameters.FACIAL_THRESHOLD, identificationSettings.facialThreshold);
                        Util.writeBoolean(sharedPreferences, GlobalParameters.DISPLAY_IMAGE_CONFIRMATION, identificationSettings.enableConfirmationNameAndImage.equals("1"));
                        Util.writeBoolean(sharedPreferences, GlobalParameters.ANONYMOUS_ENABLE, identificationSettings.enableAnonymousQRCode.equals("1"));
                        Util.writeInt(sharedPreferences, GlobalParameters.ScanMode, Integer.parseInt(identificationSettings.cameraScanMode));
                        Util.writeBoolean(sharedPreferences, GlobalParameters.ACKNOWLEDGEMENT_SCREEN, identificationSettings.enableAcknowledgementScreen.equals("1"));
                        Util.writeString(sharedPreferences, GlobalParameters.ACKNOWLEDGEMENT_TEXT, identificationSettings.acknowledgementText);
                        Util.writeBoolean(sharedPreferences, GlobalParameters.ASK_QR_CODE_ALWAYS, identificationSettings.enableQRCode.equals("1"));
                        Util.writeString(sharedPreferences, GlobalParameters.QR_CODE_MEMBER_TYPE, identificationSettings.memberTypeId);
                        Util.writeBoolean(sharedPreferences, GlobalParameters.FACE_QR_CODE, identificationSettings.enableFaceIdentification.equals("1"));
                    }

                    //AccessControl Settings
                    AccessControlSettings accessControlSettings = deviceSettingsApi.accessControl;
                    if (accessControlSettings != null) {
                        Util.writeBoolean(sharedPreferences, GlobalParameters.EnableRelay, accessControlSettings.enableAutomaticDoors.equals("1"));
                        Util.writeBoolean(sharedPreferences, GlobalParameters.AllowAnonymous, accessControlSettings.allowAnonymous.equals("1"));
                        Util.writeBoolean(sharedPreferences, GlobalParameters.RelayNormalMode, accessControlSettings.relayMode.equals("1"));
                        Util.writeBoolean(sharedPreferences, GlobalParameters.StopRelayOnHighTemp, accessControlSettings.blockAccessOnHighTemperature.equals("1"));
                        Util.writeInt(sharedPreferences, GlobalParameters.RelayTime, accessControlSettings.doorControlTimeWired);
                        Util.writeBoolean(sharedPreferences, GlobalParameters.EnableWeigand, accessControlSettings.enableAccessControl.equals("1"));
                        Util.writeInt(sharedPreferences, GlobalParameters.WeiganFormatMessage, accessControlSettings.accessControllerCardFormat);
                        Util.writeBoolean(sharedPreferences, GlobalParameters.EnableWeigandPassThrough, accessControlSettings.enableWeigandPassThrough.equals("1"));
                        Util.writeInt(sharedPreferences, GlobalParameters.AccessControlLogMode, accessControlSettings.loggingMode);
                        Util.writeInt(sharedPreferences, GlobalParameters.AccessControlScanMode, accessControlSettings.validAccessOption);
                        Util.writeInt(sharedPreferences, GlobalParameters.TimeAttendanceOption, accessControlSettings.attendanceMode);
                        if (accessControlSettings.truncateZeroes != null) {
                            Util.writeBoolean(sharedPreferences, GlobalParameters.ACCESSID_TRIM_ZEROES, accessControlSettings.truncateZeroes.equals("1"));
                        }
                        Util.writeBoolean(sharedPreferences, GlobalParameters.MOBILE_ACCESS_CARD, accessControlSettings.enableMobileAccessCard.equals("1"));
                    }

                    //Audio-Visual settings
                    AudioVisualSettings audioVisualSettings = deviceSettingsApi.audioVisualAlerts;
                    if (audioVisualSettings != null) {
                        Util.writeBoolean(sharedPreferences, GlobalParameters.QR_SOUND_VALID, audioVisualSettings.enableSoundForValidQRCode.equals("1"));
                        Util.writeBoolean(sharedPreferences, GlobalParameters.QR_SOUND_INVALID, audioVisualSettings.enableSoundForInvalidQRCode.equals("1"));
                        Util.writeBoolean(sharedPreferences, GlobalParameters.BLE_LIGHT_NORMAL, audioVisualSettings.enableLightOnNormalTemperature.equals("1"));
                        Util.writeBoolean(sharedPreferences, GlobalParameters.BLE_LIGHT_HIGH, audioVisualSettings.enableLightOnHighTemperature.equals("1"));

                        if (audioVisualSettings.audioForValidQRCode != null && !audioVisualSettings.audioForValidQRCode.isEmpty()) {
                            SoundController.getInstance().saveAudioFile(audioVisualSettings.audioForValidQRCode, "Valid.mp3");
                        } else {
                            SoundController.getInstance().deleteAudioFile("Valid.mp3");
                        }
                        if (audioVisualSettings.audioForInvalidQRCode != null && !audioVisualSettings.audioForInvalidQRCode.isEmpty()) {
                            SoundController.getInstance().saveAudioFile(audioVisualSettings.audioForInvalidQRCode, "Invalid.mp3");
                        } else {
                            SoundController.getInstance().deleteAudioFile("Invalid.mp3");
                        }
                    }

                    //Printer settings
                    PrinterSettings printerSettings = deviceSettingsApi.printerSettings;
                    if (printerSettings != null) {
                        Util.writeBoolean(sharedPreferences, GlobalParameters.BROTHER_BLUETOOTH_PRINTER, printerSettings.enableWBPrint.equals("1"));
                        Util.writeBoolean(sharedPreferences, GlobalParameters.TOSHIBA_USB_PRINTER, printerSettings.enableUSBPrint.equals("1"));
                        Util.writeBoolean(sharedPreferences, GlobalParameters.PRINT_ALL_SCAN, printerSettings.printAllScan.equals("1"));
                        Util.writeBoolean(sharedPreferences, GlobalParameters.PRINT_ACCESS_CARD_USERS, printerSettings.printAccessCard.equals("1"));
                        Util.writeBoolean(sharedPreferences, GlobalParameters.PRINT_QR_CODE_USERS, printerSettings.printQRCode.equals("1"));
                        Util.writeBoolean(sharedPreferences, GlobalParameters.PRINT_WAVE_USERS, printerSettings.printWaveUsers.equals("1"));
                        Util.writeBoolean(sharedPreferences, GlobalParameters.PRINT_HIGH_TEMPERATURE, printerSettings.printHighTempScans.equals("1"));
                        Util.writeBoolean(sharedPreferences, GlobalParameters.PRINT_LABEL_FACE, printerSettings.printFace.equals("1"));
                        Util.writeBoolean(sharedPreferences, GlobalParameters.PRINT_LABEL_NAME, printerSettings.printName.equals("1"));
                        Util.writeBoolean(sharedPreferences, GlobalParameters.PRINT_LABEL_UNIDENTIFIED_NAME, printerSettings.unidentifiedPrintText.equals("1"));
                        Util.writeString(sharedPreferences, GlobalParameters.PRINT_LABEL_WAVE_EDIT_NAME, printerSettings.unidentifiedPrintTextValue);
                        Util.writeBoolean(sharedPreferences, GlobalParameters.PRINT_LABEL_NORMAL_TEMPERATURE, printerSettings.printNormalTemperature.equals("1"));
                        Util.writeBoolean(sharedPreferences, GlobalParameters.PRINT_LABEL_HIGH_TEMPERATURE, printerSettings.printHighTemperature.equals("1"));
                        Util.writeBoolean(sharedPreferences, GlobalParameters.PRINT_LABEL_WAVE_ANSWERS, printerSettings.printWaveAnswers.equals("1"));
                        Util.writeString(sharedPreferences, GlobalParameters.PRINT_LABEL_WAVE_YES_ANSWER, printerSettings.printWaveAnswerYes);
                        Util.writeString(sharedPreferences, GlobalParameters.PRINT_LABEL_WAVE_NO_ANSWER, printerSettings.printWaveAnswerNo);
                        Util.writeBoolean(sharedPreferences, GlobalParameters.PRINT_QR_CODE_FOR_WAVE_INDICATOR, printerSettings.printIndicatorForQR.equals("1"));
                        Util.writeString(sharedPreferences, GlobalParameters.PRINT_LABEL_WAVE_EDIT_QR_ANSWERS, printerSettings.defaultBottomBarText);
                        Util.writeString(sharedPreferences, GlobalParameters.PRINT_LABEL_EDIT_PASS_NAME, printerSettings.defaultResultPrint);
                    }

                    //Touchless Interaction settings
                    TouchlessSettings touchlessSettings = deviceSettingsApi.touchlessInteraction;
                    if (touchlessSettings != null) {
                        Util.writeBoolean(sharedPreferences, GlobalParameters.HAND_GESTURE, touchlessSettings.enableWave.equals("1"));
                        Util.writeBoolean(sharedPreferences, GlobalParameters.WAVE_QUESTIONS, touchlessSettings.enableQuestionAndAnswer.equals("1"));
                        Util.writeString(sharedPreferences, GlobalParameters.Touchless_setting_id, touchlessSettings.settingId);
                        Util.writeBoolean(sharedPreferences, GlobalParameters.MASK_ENFORCEMENT, touchlessSettings.enableMaskEnforcement.equals("1"));
                        Util.writeBoolean(sharedPreferences, GlobalParameters.VISUAL_RECOGNITION, touchlessSettings.enableVoice.equals("1"));
                        Util.writeBoolean(sharedPreferences, GlobalParameters.PROGRESS_BAR, touchlessSettings.showWaveProgress.equals("1"));
                        Util.writeString(sharedPreferences, GlobalParameters.WAVE_INDICATOR, touchlessSettings.waveIndicatorInstructions);
                        Util.writeBoolean(sharedPreferences, GlobalParameters.WAVE_IMAGE, touchlessSettings.showWaveImage.equals("1"));
                        Util.writeString(sharedPreferences, GlobalParameters.MASK_ENFORCE_INDICATOR, touchlessSettings.maskEnforceText);
                        Util.writeBoolean(sharedPreferences, GlobalParameters.GESTURE_EXIT_NEGATIVE_OP, touchlessSettings.exitOnNegativeOutcome.equals("1"));
                        Util.writeString(sharedPreferences, GlobalParameters.GESTURE_EXIT_CONFIRM_TEXT, touchlessSettings.messageForNegativeOutcome);
                        Util.writeBoolean(sharedPreferences, GlobalParameters.ENABLE_TOUCH_MODE, touchlessSettings.enableTouchMode.equals("1"));
                        Util.writeString(sharedPreferences, GlobalParameters.TOUCH_HOME_PAGE_MSG, touchlessSettings.waveHomePage);
                        Util.writeString(sharedPreferences, GlobalParameters.MULTI_LANGUAGE_INSTRUCTIONS, touchlessSettings.multilanguageInstructions);

                    }

                    /*//Add settings to DB
                    if (deviceSettings != null) {
                        DeviceSettingsController.getInstance().handleAddUpdateLanguageApi(Integer.parseInt(deviceSettings.primaryLanguageId), deviceSettingsApi);
                    }*/
                    DeviceSettingsController.getInstance().handleAdditionalLanguagesApi(deviceSettingsApi.settings);
                    if (deviceSettings != null) {
                        DeviceSettingsController.getInstance().addLanguageDataToDb(Integer.parseInt(deviceSettings.primaryLanguageId));
                        String languageType = DeviceSettingsController.getInstance().getLanguageOnId(Integer.parseInt(deviceSettings.primaryLanguageId));
                        Util.writeString(sharedPreferences, GlobalParameters.LANGUAGE_TYPE, languageType);
                        DeviceSettingsController.getInstance().setLanguageToUpdate(languageType);
                        DeviceSettingsController.getInstance().getSettingsFromDb(Integer.parseInt(deviceSettings.primaryLanguageId));
                    }
                }
            } else {
                Log.e(LOG, "Setting retrieval Something went wrong please try again");
            }
        } catch (Exception e) {
            Logger.error(LOG + "retrieveSetting failed", e.getMessage());
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static void getTokenActivate(String reportInfo, String status, Context context, String toast) {
        try {
            tokenRequestModule = toast;
            JSONObject json1 = null;
            SharedPreferences sharedPreferences = Util.getSharedPreferences(context);
            try {
                String formatedString = reportInfo.substring(1, reportInfo.length() - 1);
                json1 = new JSONObject(formatedString.replace("\\", ""));

            } catch (Exception e) {
                json1 = new JSONObject(reportInfo.replace("\\", ""));
            }

            if (status.contains("ActivateApplication")) {
                if (json1.has("responseCode") && json1.getString("responseCode").equals("1")) {
                    Util.writeBoolean(sharedPreferences, GlobalParameters.ONLINE_MODE, true);
                    Util.getToken((JSONObjectCallback) context, context);

                } else if (json1.getString("responseSubCode").equals("103")) {
                    Util.writeBoolean(sharedPreferences, GlobalParameters.ONLINE_MODE, true);
                    Util.getToken((JSONObjectCallback) context, context);

                } else if (json1.getString("responseSubCode").equals("104")) {
                    //Util.writeBoolean(sharedPreferences, GlobalParameters.ONLINE_MODE, false);
                    // openDialogactivate(context, "This device SN: " + Util.getSNCode() + " " + context.getResources().getString(R.string.device_not_register), toast);
                    context.startActivity(new Intent(context, AddDeviceActivity.class));

                } else if (json1.getString("responseSubCode").equals("105")) {
                    Util.writeBoolean(sharedPreferences, GlobalParameters.ONLINE_MODE, false);
                    openDialogactivate(context, "This device SN: " + Util.getSNCode(context) + " " + context.getResources().getString(R.string.device_inactive), toast);
                }
            } else {
                if (json1.isNull("access_token")) {
                    Util.switchRgbOrIrActivity(context, true);
                    return;
                }
                if (json1.has("Message") && json1.getString("Message").equals("Authorization has been denied for this request")) {
                    Util.switchRgbOrIrActivity(context, true);
                    return;
                }
                String access_token = json1.getString("access_token");
                String token_type = json1.getString("token_type");
                String institutionId = json1.getString("InstitutionID");
                String expire_time = json1.getString(".expires");
                String command = json1.isNull("command") ? "" : json1.getString("command");

                String institutionIdOld = sharedPreferences.getString(GlobalParameters.INSTITUTION_ID, "");

                if (institutionIdOld != null && !institutionId.isEmpty()
                        && !institutionIdOld.equals(institutionId)) {
                    DatabaseController.getInstance().clearAll();
                }
                Log.d(LOG, "Get token success");
                Util.writeString(sharedPreferences, GlobalParameters.ACCESS_TOKEN, access_token);
                Util.writeString(sharedPreferences, GlobalParameters.EXPIRE_TIME, expire_time);
                Util.writeString(sharedPreferences, GlobalParameters.TOKEN_TYPE, token_type);
                Util.writeString(sharedPreferences, GlobalParameters.INSTITUTION_ID, institutionId);
                Util.writeString(sharedPreferences, GlobalParameters.Generate_Token_Command, command);
                RetrofitInstance.getInstance().createRetrofitInstance();

                Util.getSettings((SettingCallback) context, context);
            }
        } catch (Exception e) {
            Util.switchRgbOrIrActivity(context, true);
            Logger.error(LOG, "getTokenActivate(String reportInfo,String status,Context context)", e.getMessage());
        }
    }

    public static void getQRCode(JSONObject reportInfo, String status, Context context, String toast) {
        try {
            JSONObject responseData = reportInfo.getJSONObject("responseData");
            String id = responseData.getString("id") == null ? "" : responseData.getString("id");
            String firstName = responseData.getString("firstName") == null ? "" : responseData.getString("firstName");
            String lastName = responseData.getString("lastName") == null ? "" : responseData.getString("lastName");
            String trqStatus = responseData.getString("trqStatus") == null ? "" : responseData.getString("trqStatus");
            String memberId = responseData.getString("memberId") == null ? "" : responseData.getString("memberId");
            int memberTypeId = responseData.isNull("memberTypeId") ? 0 : responseData.getInt("memberTypeId");
            String memberTypeName = responseData.getString("memberTypeName") == null ? "" : responseData.getString("memberTypeName");
            String qrAccessid = responseData.getString("accessId") == null ? "" : responseData.getString("accessId");

            QrCodeData qrCodeData = new QrCodeData();
            qrCodeData.setUniqueId(id);
            qrCodeData.setFirstName(firstName);
            qrCodeData.setLastName(lastName);
            qrCodeData.setTrqStatus(trqStatus);
            qrCodeData.setMemberId(memberId);
            qrCodeData.setAccessId(qrAccessid);
            qrCodeData.setMemberTypeId(memberTypeId);
            qrCodeData.setMemberTypeName(memberTypeName);
            CameraController.getInstance().setQrCodeData(qrCodeData);
        } catch (Exception e) {
            Logger.error("getQRCode(JSONObject reportInfo, String status, Context context, String toast) ", e.getMessage());
        }
    }

    public static void soundPool(Context context, String tempVal, SoundPool soundPool) {
        if (soundPool == null) return;
        try {

            if (tempVal.equals("high")) {
                File file = new File(Environment.getExternalStorageDirectory() + "/Audio/High.mp3");
                if (file.exists()) {
                    soundPool.load(Environment.getExternalStorageDirectory() + "/Audio/High.mp3", 1);
                } else {
                    soundPool.load(context, R.raw.failed_last, 1);
                }
            } else {
                File file = new File(Environment.getExternalStorageDirectory() + "/Audio/Normal.mp3");
                if (file.exists()) {
                    soundPool.load(Environment.getExternalStorageDirectory() + "/Audio/Normal.mp3", 1);
                } else {
                    soundPool.load(context, R.raw.thankyou_last, 1);
                }
            }
            soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
                int lastStreamId = -1;

                @Override
                public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                    //soundPool.release();
                    lastStreamId = soundPool.play(sampleId, 1.0f, 1.0f, 0, 0, 1.0f);
                }
            });
        } catch (Exception e) {
            Log.e(LOG, e.getMessage());
        }


    }

    public static String getVersionBuild() {
        return String.format("v%s.%s", BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE);
    }

    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("0x");
        if (src == null || src.length <= 0) {
            return null;
        }
        char[] buffer = new char[2];
        for (int i = 0; i < src.length; i++) {
            buffer[0] = Character.forDigit((src[i] >>> 4) & 0x0F, 16);
            buffer[1] = Character.forDigit(src[i] & 0x0F, 16);
            stringBuilder.append(buffer);
        }
        return stringBuilder.toString();
    }

    public static String bytearray2Str(byte[] data, int start, int length, int targetLength) {
        long number = 0;
        if (data.length < start + length) {
            return "";
        }
        for (int i = 1; i <= length; i++) {
            number *= 0x100;
            number += (data[start + length - i] & 0xFF);
        }
        return String.format("%0" + targetLength + "d", number);
    }

    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    public static byte[] getBytesFromFile(String filePath) {
        File file = new File(filePath);
        int size = (int) file.length();
        byte[] bytes = new byte[size];
        try {
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
            buf.read(bytes, 0, bytes.length);
            buf.close();
        } catch (IOException e) {
            Logger.error(LOG, "getBytesFromFile()", "Reading bytes from file failed");
        }
        return bytes;
    }

    public static void openDialogSetting(final Context context) {
        Typeface rubiklight = Typeface.createFromAsset(context.getAssets(),
                "rubiklight.ttf");
        final Dialog d = new Dialog(context);
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //  d.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        d.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        d.setCancelable(false);
        d.setContentView(R.layout.activate_dialog);
        TextView tv_setting_message = d.findViewById(R.id.tv_setting_message);
        TextView tv_note_message = d.findViewById(R.id.tv_note_message);
        TextView btn_continue = d.findViewById(R.id.btn_continue);
        tv_setting_message.setTypeface(rubiklight);
        tv_note_message.setTypeface(rubiklight);
        btn_continue.setTypeface(rubiklight);


        btn_continue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.dismiss();
                Intent intent = new Intent(context, SettingsActivity.class);
                context.startActivity(intent);
            }
        });
        d.show();
    }

    public static void openDialogactivate(final Context context, String message, final String toast) {
        Typeface rubiklight = Typeface.createFromAsset(context.getAssets(),
                "rubiklight.ttf");
        final Dialog d = new Dialog(context);
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //  d.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        d.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        d.setCancelable(false);
        d.setContentView(R.layout.activate_dialog_activate);
        TextView tv_setting_message = d.findViewById(R.id.tv_setting_message);
        TextView btn_continue = d.findViewById(R.id.btn_continue);
        tv_setting_message.setText(message);
        tv_setting_message.setTypeface(rubiklight);
        btn_continue.setTypeface(rubiklight);


        btn_continue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (toast.equals("guide")) {
                    d.dismiss();
                    Util.switchRgbOrIrActivity(context, true);
                } else {
                    d.dismiss();
                }
            }
        });
        d.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void getmemberList(MemberListCallback callback, Context context) {
        try {
            SharedPreferences sharedPreferences = Util.getSharedPreferences(context);

            JSONObject obj = new JSONObject();
            if (AppSettings.isMemberGroupSyncEnabled()) {
                obj.put("groupId", AppSettings.getMemberSyncGroupId());
            } else {
                obj.put("groupId", "0");
            }
            if (sharedPreferences.getBoolean(GlobalParameters.MEMBER_DELTA_SYNC_ENABLED, true)) {
                RegisteredMembers registeredMember = DatabaseController.getInstance().getLastMemberSyncDateTime();
                if (registeredMember != null) {
                    obj.put("fromDate", registeredMember.dateTime);
                }
            }
            new AsyncJSONObjectGetMemberList(obj, callback, sharedPreferences.getString(GlobalParameters.URL,
                    EndPoints.prod_url) + EndPoints.GetMemberList, context).execute();

        } catch (Exception e) {
            Logger.error(LOG + "getToken(JSONObjectCallback callback, Context context) ", e.getMessage());

        }
    }

    public static void sendDeviceLogs(Context context) {
        if (Util.isOfflineMode(context)) return;
        SharedPreferences sharedPreferences = Util.getSharedPreferences(context);
        JSONObject obj = new JSONObject();
        try {
            obj.put("deviceSN", Util.getSNCode(context));
            String filePath = LoggerUtil.logMessagesToFile(context, "AppLog");
            String encodedData = Base64.encodeToString(Util.getBytesFromFile(filePath), Base64.NO_WRAP);
            obj.put("deviceLog", encodedData);
            obj.put("deviceData", MobileDetails(context));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d(LOG, "Logger API call");
        new AsyncDeviceLog(obj, null, sharedPreferences.getString(GlobalParameters.URL,
                EndPoints.prod_url) + EndPoints.DeviceLogs, context).execute();
    }

    public static JSONObject getJSONObjectMemberList(JSONObject req, String url, String header, Context context, String device_sn) {
        try {
            String responseTemp = Requestor.requestJson(url, req, Util.getSNCode(context), context, "device_sn");
            if (responseTemp != null && !responseTemp.equals("")) {
                return new JSONObject(responseTemp);
            }
        } catch (Exception e) {
            Logger.error(LOG + "getJSONObject(JSONObject req, String url): req = " + req
                    + ", url = " + url, e.getMessage());
            return null;
        }
        return null;
    }

    public static JSONObject getJSONObjectMemberData(JSONObject req, String url, String header, Context context, String device_sn) {
        try {
            String responseTemp = Requestor.requestJson(url, req, Util.getSNCode(context), context, "device_sn");
            if (responseTemp != null && !responseTemp.equals("")) {
                return new JSONObject(responseTemp);
            }
        } catch (Exception e) {
            Logger.error(LOG + "getJSONObject(JSONObject req, String url): req = " + req
                    + ", url = " + url, e.getMessage());
            return null;
        }
        return null;
    }

    public static boolean isNumeric(String str) {
        if (str.isEmpty()) {
            return false;
        } else {
            return str != null && str.matches("[+-]?\\d*(\\.\\d+)?");
        }
    }

    public static String currentDate() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date curDate = new Date(System.currentTimeMillis());
        String currentDate = formatter.format(curDate);
        return currentDate;
    }

    /**
     * Method that checks if there is a network connected
     *
     * @param context context
     * @return true or false accordingly
     */
    public static boolean isNetworkOff(Context context) {
        boolean result = false;
        ConnectivityManager connMgr =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Network[] networks = connMgr.getAllNetworks();
        if (networks != null && networks.length == 0) {
            result = true;
        }
        return result;
    }

    /**
     * Get the network info
     *
     * @param context
     * @return
     */
    public static NetworkInfo getNetworkInfo(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo();
    }

    /**
     * Check if there is any connectivity to a Wifi network
     *
     * @param context
     * @return
     */
    public static boolean isConnectedWifi(Context context) {
        NetworkInfo info = Util.getNetworkInfo(context);
        return (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_WIFI);
    }

    /**
     * Check if there is any connectivity to a mobile network
     *
     * @param context
     * @return
     */
    public static boolean isConnectedMobile(Context context) {
        NetworkInfo info = Util.getNetworkInfo(context);
        return (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_MOBILE);
    }

    /**
     * Check if there is any connectivity to a ethernet network
     *
     * @param context
     * @return
     */
    public static boolean isConnectedEthernet(Context context) {
        NetworkInfo info = Util.getNetworkInfo(context);
        return (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_ETHERNET);
    }

    public static void getMemberID(Context context, String certifyId) {
        try {
            MemberSyncDataModel.getInstance().clear();
            MemberSyncDataModel.getInstance().setNumOfRecords(1);
            doSendBroadcast(context, "start", 1, 1);
            SharedPreferences sharedPreferences = Util.getSharedPreferences(context);
            JSONObject obj = new JSONObject();
            obj.put("id", certifyId);
            if (taskExecutorService != null) {
                new AsyncGetMemberData(obj, (MemberIDCallback) context, sharedPreferences.getString(GlobalParameters.URL,
                        EndPoints.prod_url) + EndPoints.GetMemberById, context).executeOnExecutor(taskExecutorService);
            } else {
                new AsyncGetMemberData(obj, (MemberIDCallback) context, sharedPreferences.getString(GlobalParameters.URL,
                        EndPoints.prod_url) + EndPoints.GetMemberById, context).execute();
            }
        } catch (Exception e) {
            Logger.error(" getMemberID()", e.getMessage());
        }
    }

    private static void doSendBroadcast(Context context, String message, int memberCount, int count) {
        Intent event_snackbar = new Intent("EVENT_SNACKBAR");

        if (!TextUtils.isEmpty(message))
            event_snackbar.putExtra("message", message);
        event_snackbar.putExtra("memberCount", memberCount);
        event_snackbar.putExtra("count", count);

        LocalBroadcastManager.getInstance(context).sendBroadcast(event_snackbar);
    }

    //bitmap
    public static void createAudioDirectory() throws IOException {//Bitmap
        String path = Environment.getExternalStorageDirectory() + "/Audio/";
        File dirFile = new File(path);
        if (!dirFile.exists()) {
            dirFile.mkdir();


        }
    }

    public static String getTokenRequestName() {
        return tokenRequestModule;
    }

    public static void setTokenRequestName(String value) {
        tokenRequestModule = value;
    }

    public final static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    public static boolean isUsualTemperature(Context context, float temperature) {
        boolean isUnusual;
        SharedPreferences sp = getSharedPreferences(context);
        if (sp.getInt(GlobalParameters.Temperature, 0) == 0) {
            float centigrade = sp.getFloat(GlobalParameters.Centigrades, (float) 37.3);
            isUnusual = (temperature > centigrade) ? true : false;
//            Log.e("centigrade---",centigrade+"-"+isUnusual);
        } else {
            float fahrenheit = sp.getFloat(GlobalParameters.Fahrenheits, (float) 99.14);
            isUnusual = (celsiusToFahrenheit(temperature) > fahrenheit) ? true : false;
//            Log.e("fahrenheit---",fahrenheit+"-"+isUnusual);
        }
        return isUnusual;
    }

    public static double celsiusToFahrenheit(float temperature) {
        BigDecimal b = new BigDecimal(temperature * 1.8 + 32);
        return b.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public static String toHexString(byte[] data) {
        if (data == null) {
            return "";
        } else {
            StringBuilder stringBuilder = new StringBuilder();

            for (int i = 0; i < data.length; ++i) {
                String string = Integer.toHexString(data[i] & 255);
                if (string.length() == 1) {
                    stringBuilder.append("0");
                }

                stringBuilder.append(string.toUpperCase());
            }

            return stringBuilder.toString();
        }
    }

    public static boolean isDeviceProModel() {
        int mode = CameraController.getInstance().getDeviceMode();
        return (mode == Constants.PRO_MODEL_TEMPERATURE_MODULE_1 || mode == Constants.PRO_MODEL_TEMPERATURE_MODULE_2);
    }

    public static boolean isOfflineMode(Context context) {
        return isNetworkOff(context);
    }

    public static void qrSoundPool(Context context, SoundPool soundPool, Boolean validQRCode) {
        if (soundPool == null) return;
        try {

            if (validQRCode) {
                File file = new File(Environment.getExternalStorageDirectory() + "/Audio/Valid.mp3");
                if (file.exists()) {
                    soundPool.load(Environment.getExternalStorageDirectory() + "/Audio/Valid.mp3", 1);
                } else {
                    soundPool.load(context, R.raw.valid, 1);
                }
            } else {
                File file = new File(Environment.getExternalStorageDirectory() + "/Audio/Invalid.mp3");
                if (file.exists()) {
                    soundPool.load(Environment.getExternalStorageDirectory() + "/Audio/Invalid.mp3", 1);
                } else {
                    soundPool.load(context, R.raw.invalid, 1);
                }
            }
            soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
                int lastStreamId = -1;

                @Override
                public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                    //soundPool.release();
                    lastStreamId = soundPool.play(sampleId, 1.0f, 1.0f, 0, 0, 1.0f);
                }
            });
        } catch (Exception e) {
            Log.e(LOG, e.getMessage());
        }
    }

    public static boolean isQRCodeWithPrefix(String code) {
        if (code != null) {
            if (code.isEmpty()) {
                return false;
            }
            return code.toLowerCase().startsWith("tr");
        }
        return false;
    }

    public static void deleteAppData(Context context) {
        SharedPreferences sharedPreferences = Util.getSharedPreferences(context);
        if (sharedPreferences != null) {
            sharedPreferences.edit().clear().apply();
        }
        DatabaseController.getInstance().deleteAllMember();
        // Saving the token, after clearing the sharedPreference
        if (sharedPreferences != null) {
            Util.writeString(sharedPreferences, GlobalParameters.Firebase_Token, ApplicationController.getInstance().getFcmPushToken());
        }
    }

    public static void stopMemberSyncService(Context context) {
        Intent intent = new Intent(context, MemberSyncService.class);
        context.stopService(intent);
    }

    public static void restartApp(Context context) {
        stopMemberSyncService(context);
        Intent intent = new Intent(context, HomeActivity.class);
        int mPendingIntentId = 111111;
        PendingIntent mPendingIntent = PendingIntent.getActivity(context, mPendingIntentId, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis(), mPendingIntent);
    }


    public static void getPushresponse(PushCallback callback, Context context, String guid, String uniqueID, String response_msg, String eventType) {
        try {
            SharedPreferences sharedPreferences = Util.getSharedPreferences(context);

            JSONObject obj = new JSONObject();
            obj.put("commandGuid", guid);
            obj.put("deviceUUID", uniqueID);
            obj.put("eventType", eventType);
            obj.put("response", response_msg + " push success");
            if (guid == null) {
                obj.put("APPSTARTED", AppStatusInfo.getInstance().getAppStarted());
                obj.put("APPCLOSED", AppStatusInfo.getInstance().getAppClosed());
                obj.put("LOGINSUCCESS", AppStatusInfo.getInstance().getLoginSuccess());
                obj.put("LOGINFAILED", AppStatusInfo.getInstance().getLoginFailed());
                obj.put("DEVICESETTINGS", AppStatusInfo.getInstance().getDeviceSettings());
            }

            new AsyncJSONObjectPush(obj, callback, sharedPreferences.getString(GlobalParameters.URL, EndPoints.prod_url) + EndPoints.PushCommandResponse, context).execute();

        } catch (Exception e) {
            Util.switchRgbOrIrActivity(context, true);
            Logger.error(LOG + "getPushresponse", e.getMessage());

        }
    }


    public static JSONObject getJSONObjectPush(JSONObject req, String url, String header, Context context) {
        try {
            String responseTemp = Requestor.postJson(url, req, context);
            if (responseTemp != null && !responseTemp.equals("")) {
                return new JSONObject(responseTemp);
            }
        } catch (Exception e) {
            Logger.error(LOG + "getJSONObjectPush " + req
                    + ", url = " + url, e.getMessage());
            return null;

        }
        return null;
    }

    public static String getUniqueIMEIId(Context context) {
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return "";
            }
            String imei = telephonyManager.getDeviceId();
            if (imei != null && !imei.isEmpty()) {
                return imei;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static JSONObject getJSONObjectAccessLog(JSONObject req, String url, String header, Context context) {
        try {
            String responseTemp = Requestor.postJson(url, req, context);
            if (responseTemp != null && !responseTemp.equals("")) {
                return new JSONObject(responseTemp);
            }
        } catch (Exception e) {
            Logger.error(LOG + "getJSONObjectAccessLog " + req
                    + ", url = " + url, e.getMessage());
            return null;

        }
        return null;
    }

    public static JSONObject getJSONObjectDeviceLog(JSONObject req, String url, String header, Context context) {
        try {
            String responseTemp = Requestor.postJsonLog(url, req, context);
            if (responseTemp != null && !responseTemp.equals("")) {
                return new JSONObject(responseTemp);
            }
        } catch (Exception e) {
            Logger.error(LOG + "getJSONObjectAccessLog " + req
                    + ", url = " + url, e.getMessage());
            return null;

        }
        return null;
    }

    public static String getAppUpTime(Context context) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        String appLaunchTime = sharedPreferences.getString(GlobalParameters.APP_LAUNCH_TIME, "");
        Date appLaunchDateTime = new Date(Long.parseLong(appLaunchTime));
        Date currentDateTime = new Date(System.currentTimeMillis());
        long differenceInTime = currentDateTime.getTime() - appLaunchDateTime.getTime();
        long seconds = TimeUnit.MILLISECONDS.toSeconds(differenceInTime) % 60;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(differenceInTime) % 60;
        long hours = TimeUnit.MILLISECONDS.toHours(differenceInTime) % 24;
        long days = TimeUnit.MILLISECONDS.toDays(differenceInTime) % 365;
        long totalHours = hours + days * 24;
        return String.format(Locale.getDefault(), "%d:%02d:%02d", totalHours, minutes, seconds);
    }

    public static boolean isAlreadyChecked(String oldDateTime) {
        try {
            if (AccessCardController.getInstance().getCheckInResponseCode() == -1)
                return false;
            if (AccessCardController.getInstance().getCheckInResponseCode() == AccessCardController.AccessCheckInOutStatus.RESPONSE_CODE_FAILED.getValue() || AccessCardController.getInstance().getCheckInResponseCode() == AccessCardController.AccessCheckInOutStatus.RESPONSE_CODE_ALREADY.getValue())
                return true;
            if (ApplicationController.getInstance().getTimeAttendance() == 2 && (oldDateTime.isEmpty()))
                return true;
            else if (ApplicationController.getInstance().getTimeAttendance() == 2 && !oldDateTime.isEmpty())
                return false;
            if (ApplicationController.getInstance().getTimeAttendance() == 1 && oldDateTime.isEmpty())
                return false;
            else if (ApplicationController.getInstance().getTimeAttendance() == 1 && !oldDateTime.isEmpty()) {
                SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
                Date appLaunchDateTime = format.parse(oldDateTime);
                Date currentDateTime = new Date(System.currentTimeMillis());
                long differenceInTime = currentDateTime.getTime() - appLaunchDateTime.getTime();
                long hours = TimeUnit.MILLISECONDS.toHours(differenceInTime) % 24;
                long days = TimeUnit.MILLISECONDS.toDays(differenceInTime) % 365;
                long totalHours = hours + days * 24;
                return totalHours < 24;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean differenceInTimeTwoDates(String lastCheckInTime) {
        try {
            if (lastCheckInTime.isEmpty()) return false;
            SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
            Date appLaunchDateTime = format.parse(lastCheckInTime);
            Date currentDateTime = new Date(System.currentTimeMillis());
            long differenceInTime = currentDateTime.getTime() - appLaunchDateTime.getTime();
            long hours = TimeUnit.MILLISECONDS.toHours(differenceInTime) % 24;
            long days = TimeUnit.MILLISECONDS.toDays(differenceInTime) % 365;
            long totalHours = hours + days * 24;
            return totalHours < 24;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String getDeviceUpTime() {
        long uptimeMillis = SystemClock.elapsedRealtime();
        String deviceUptime = String.format(Locale.getDefault(),
                "%d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(uptimeMillis),
                TimeUnit.MILLISECONDS.toMinutes(uptimeMillis)
                        - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS
                        .toHours(uptimeMillis)),
                TimeUnit.MILLISECONDS.toSeconds(uptimeMillis)
                        - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS
                        .toMinutes(uptimeMillis)));
        return deviceUptime;
    }

    public static JSONObject getJSONObjectGesture(JSONObject req, String url, String header, Context context) {
        try {
            String responseTemp = Requestor.postJson(url, req, context);
            if (responseTemp != null && !responseTemp.equals("")) {
                return new JSONObject(responseTemp);
            }
        } catch (Exception e) {
            Logger.error(LOG + "getJSONObject(JSONObject req, String url): req = " + req
                    + ", url = " + url, e.getMessage());
            return null;

        }
        return null;
    }

    public static JSONObject getJSONObjectFlowList(JSONObject req, String url, String header, Context context) {
        try {
            String responseTemp = Requestor.postJson(url, req, context);
            if (responseTemp != null && !responseTemp.equals("")) {
                return new JSONObject(responseTemp);
            }
        } catch (Exception e) {
            Logger.error(LOG + "getJSONObject(JSONObject req, String url): req = " + req
                    + ", url = " + url, e.getMessage());
            return null;

        }
        return null;
    }

    public static String getKeyFromValue(Map hm, Object value) {
        for (Object o : hm.keySet()) {
            if (hm.get(o).equals(value)) {
                return String.valueOf(o);
            }
        }
        return null;
    }

    public static Object getKeyOnValue(Map hm, Object value) {
        for (Object o : hm.keySet()) {
            if (hm.get(o).equals(value)) {
                return o;
            }
        }
        return null;
    }

    public static boolean isGestureDeviceConnected(Context context) {
        boolean result = false;
        UsbManager mUsbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceHashMap = mUsbManager.getDeviceList();
        Iterator<UsbDevice> iterator = deviceHashMap.values().iterator();
        while (iterator.hasNext()) {
            UsbDevice usbDevice = iterator.next();
            int pid = usbDevice.getProductId();
            int vid = usbDevice.getVendorId();
            if (pid == 0x5790 && vid == 0x0483) {
                if (mUsbManager.hasPermission(usbDevice)) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    public static boolean isInstitutionIdValid(Context context) {
        boolean result = false;
        SharedPreferences sp = Util.getSharedPreferences(context);
        String institutionId = sp.getString(GlobalParameters.INSTITUTION_ID, "");
        if (institutionId != null && !institutionId.isEmpty()) {
            long institutionIdVal = Long.parseLong(institutionId);
            if (institutionIdVal > 0) {
                result = true;
            } else {
                Log.e(LOG, "Institution Id is 0");
            }
        }
        return result;
    }

    public static String currentDate(String format) {
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        Date curDate = new Date(System.currentTimeMillis());
        String currentDate = formatter.format(curDate);
        return currentDate;
    }

    public static boolean isDateBigger(String expiryDate, String inputDate, String format) {
        boolean result = false;
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        Date dt1 = null;
        Date dt2 = null;
        try {
            dt1 = sdf.parse(expiryDate);
            dt2 = sdf.parse(inputDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (dt1.getTime() >= dt2.getTime()) {
            result = true;
        }
        return result;
    }

    public static JSONObject getJSONObjectLanguages(JSONObject req, String url, String header, Context context) {
        try {
            String responseTemp = Requestor.postJson(url, req, context);
            if (responseTemp != null && !responseTemp.equals("")) {
                return new JSONObject(responseTemp);
            }
        } catch (Exception e) {
            Logger.error(LOG + "getJSONObject(JSONObject req, String url): req = " + req
                    + ", url = " + url, e.getMessage());
            return null;
        }
        return null;
    }

    public static JSONObject getJSONObjectLastCheckInTime(JSONObject req, String url, String header, Context context) {
        try {
            String responseTemp = Requestor.postJson(url, req, context);
            if (responseTemp != null && !responseTemp.equals("")) {
                return new JSONObject(responseTemp);
            }
        } catch (Exception e) {
            Logger.error(LOG + "getJSONObject(JSONObject req, String url): req = " + req
                    + ", url = " + url, e.getMessage());
            return null;

        }
        return null;
    }

    public static String getDateTimeZone() {
        Calendar mCalendar = new GregorianCalendar();
        TimeZone mTimeZone = mCalendar.getTimeZone();
        int mGMTOffset = mTimeZone.getRawOffset();
        long timeZoneValue = TimeUnit.HOURS.convert(mGMTOffset, TimeUnit.MILLISECONDS);
        timeZoneValue = timeZoneValue + (mTimeZone.getDSTSavings() / (3600 * 1000));
        String timeZoneStr = String.valueOf(timeZoneValue);
        if (!timeZoneStr.contains("-")) {
            timeZoneStr = "+" + timeZoneStr;
        }
        return (timeZoneStr + ":00");
    }

    public static float convertDpToPixel(float dp, Context context) {
        return dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    public static float convertPixelsToDp(float px, Context context) {
        return px / ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    public static String getInternalModel() {
        String value = "";
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class);
            value = (String) (get.invoke(c, "ro.internal.model"));
            if (value == null || "".equals(value)) {
                value = (String) (get.invoke(c, "ro.product.model"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
        return value;
    }

    public static int getOrientation(SharedPreferences sp) {
        if ("TPS980Q".equals(getInternalModel()) || getInternalModel().contains("TPS950")
                || getInternalModel().contains("F10") || getInternalModel().contains("970")
                || getInternalModel().contains("F8")) {
            if ("F801".equals(getInternalModel()))
                return sp.getInt(GlobalParameters.Orientation, 0);
            return sp.getInt(GlobalParameters.Orientation, 270);
        } else {
            return sp.getInt(GlobalParameters.Orientation, 0);
        }
    }

    public static boolean isDeviceF10() {
        return getInternalModel().equals("F10");
    }

    public static void writeToFile(File file, long offset, byte[] content) throws IOException {
        RandomAccessFile accessFile = new RandomAccessFile(file, "rw");
        FileChannel sourceChannel = accessFile.getChannel();
        sourceChannel.truncate(offset);
        accessFile.seek(offset);
        accessFile.write(content);
        sourceChannel.close();
        accessFile.close();
    }

    //TODO: Optimize
    public static String getDateTimePastHour(String currentTime) {
        String splitStr[] = currentTime.split(":");
        String hrStr = splitStr[0];
        String splitStr1[] = hrStr.split(" ");
        int hour = Integer.parseInt(splitStr1[1]);
        if (hour != 0) {
            hour--;
        }
        splitStr1[1] = String.valueOf(hour);
        String dateTimeHour = splitStr1[0] + " " + splitStr1[1];
        return dateTimeHour + ":" + splitStr[1] + ":" + splitStr[2];
    }

    public static boolean isInternetConnected() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process mIpAddrProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int processValue = mIpAddrProcess.waitFor();
            if (processValue == 0) {
                return true;
            }
        } catch (Exception e) {
            Log.e(LOG, "Error is executing ping command");
        }
        return false;
    }

    private static void checkForInternetConnection(Context context) {
        Observable
                .create((ObservableOnSubscribe<Boolean>) emitter -> {
                    Boolean value = isInternetConnected();
                    emitter.onNext(value);
                })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Boolean>() {
                    Disposable connectionDisposable;

                    @Override
                    public void onSubscribe(Disposable d) {
                        connectionDisposable = d;
                    }

                    @Override
                    public void onNext(Boolean value) {
                        if (!value) {
                            writeBoolean(getSharedPreferences(context), GlobalParameters.Internet_Indicator, true);
                            Log.d(LOG, "Offline internet " + Util.getSharedPreferences(context).getBoolean(GlobalParameters.Internet_Indicator, false));
                            Intent intent = new Intent();
                            intent.setAction(DeviceHealthService.HEALTH_CHECK_OFFLINE_ACTION);
                            LocalBroadcastManager.getInstance(context.getApplicationContext()).sendBroadcast(intent);
                        } else {
                            writeBoolean(getSharedPreferences(context), GlobalParameters.Internet_Indicator, false);
                            Log.d(LOG, "Online internet " + Util.getSharedPreferences(context).getBoolean(GlobalParameters.Internet_Indicator, false));
                            Intent intent = new Intent();
                            intent.setAction(DeviceHealthService.HEALTH_CHECK_ONLINE_ACTION);
                            LocalBroadcastManager.getInstance(context.getApplicationContext()).sendBroadcast(intent);
                        }
                        connectionDisposable.dispose();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(LOG, "Error in checking connection");
                    }

                    @Override
                    public void onComplete() {
                        //do noop
                    }
                });
    }

    /**
     * Method that truncates the leading zero's
     *
     * @param accessId accessId value
     * @return accessId
     */
    public static String validateAccessId(String accessId) {
        String accessIdStr = accessId;
        if (accessId != null && !accessId.isEmpty()) {
            char[] accessIdArray = accessId.toCharArray();
            for (int i = 0; i < accessIdArray.length; i++) {
                if (accessIdStr.startsWith("0")) {
                    accessIdStr = accessIdStr.replaceFirst("0", "");
                } else {
                    break;
                }
            }

        }
        return accessIdStr;
    }

    public static void sendOfflineServiceBroadcast(Context context) {
        Intent intent = new Intent();
        intent.setAction(DeviceHealthService.HEALTH_CHECK_ONLINE_ACTION);
        LocalBroadcastManager.getInstance(context.getApplicationContext()).sendBroadcast(intent);
    }

    public static DeviceInfo getDeviceInfo(Context context) {
        getNumberVersion(context);
        getDeviceUUid(context);

        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.appVersion = Util.getVersionBuild();
        deviceInfo.osVersion = "Android - " + Build.VERSION.RELEASE;
        deviceInfo.mobileIp = getLocalIpAddress();
        deviceInfo.mobileNumber = getSharedPreferences(context).getString(GlobalParameters.MOBILE_NUMBER, "+1");
        deviceInfo.uniqueDeviceId = getSharedPreferences(context).getString(GlobalParameters.UUID, "");
        deviceInfo.imeiNumber = getSharedPreferences(context).getString(GlobalParameters.IMEI, "");
        deviceInfo.deviceModel = Build.MODEL;
        deviceInfo.deviceSerialNumber = getSerialNumber();
        deviceInfo.batteryStatus = getBatteryLevel(context);
        deviceInfo.networkStatus = isConnectedEthernet(context);
        deviceInfo.appState = getAppState();
        return deviceInfo;
    }

    public static String getUTCDateMMDDYYYYHHMMSS() {
        try {
            final SimpleDateFormat f = new SimpleDateFormat("MMddyyyy_HHmmss");
            f.setTimeZone(TimeZone.getTimeZone("UTC"));
            return f.format(new Date());

        } catch (Exception e) {
            e.printStackTrace();
            // 如果throw java.text.ParseException或者NullPointerException，就说明格式不对

        }
        return "";
    }

    public static boolean changeNfcEnabled(Context context, boolean enabled) {
        // Turn NFC on/off
        final boolean desiredState = enabled;
        NfcAdapter mNfcAdapter = NfcAdapter.getDefaultAdapter(context);

        if (mNfcAdapter == null) {
            // NFC is not supported
            return false;
        }

        new Thread("toggleNFC") {
            public void run() {
                Log.d(LOG, "Setting NFC enabled state to: " + desiredState);
                boolean success = false;
                Class<?> NfcManagerClass;
                Method setNfcEnabled, setNfcDisabled;
                boolean Nfc;
                if (desiredState) {
                    try {
                        NfcManagerClass = Class.forName(mNfcAdapter.getClass().getName());
                        setNfcEnabled = NfcManagerClass.getDeclaredMethod("enable");
                        setNfcEnabled.setAccessible(true);
                        Nfc = (Boolean) setNfcEnabled.invoke(mNfcAdapter);
                        success = mNfcAdapter.isEnabled();
                    } catch (ClassNotFoundException e) {
                    } catch (NoSuchMethodException e) {
                    } catch (IllegalArgumentException e) {
                    } catch (IllegalAccessException e) {
                    } catch (InvocationTargetException e) {
                    }
                } else {
                    try {
                        NfcManagerClass = Class.forName(mNfcAdapter.getClass().getName());
                        setNfcDisabled = NfcManagerClass.getDeclaredMethod("disable");
                        setNfcDisabled.setAccessible(true);
                        Nfc = (Boolean) setNfcDisabled.invoke(mNfcAdapter);
                        success = Nfc;
                    } catch (ClassNotFoundException e) {
                    } catch (NoSuchMethodException e) {
                    } catch (IllegalArgumentException e) {
                    } catch (IllegalAccessException e) {
                    } catch (InvocationTargetException e) {
                    }
                }
                if (success) {
                    Log.d(LOG, "Successfully changed NFC enabled state to " + desiredState);
                } else {
                    Log.w(LOG, "Error setting NFC enabled state to " + desiredState);
                }
            }
        }.start();
        return false;
    }//end method
}