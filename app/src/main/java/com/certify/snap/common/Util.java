package com.certify.snap.common;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
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
import android.media.SoundPool;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Debug;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.certify.callback.MemberListCallback;
import com.certify.callback.SettingCallback;
import com.certify.callback.JSONObjectCallback;
import com.certify.callback.RecordTemperatureCallback;
import com.certify.snap.BuildConfig;
import com.certify.snap.R;
import com.certify.snap.activity.IrCameraActivity;
import com.certify.snap.activity.SettingActivity;
import com.certify.snap.async.AsyncJSONObjectGetMemberList;
import com.certify.snap.async.AsyncJSONObjectSender;
import com.certify.snap.async.AsyncJSONObjectSetting;
import com.certify.snap.async.AsyncRecordUserTemperature;
import com.certify.snap.model.RegisteredMembers;
import com.common.pos.api.util.PosUtil;
import com.example.a950jnisdk.SDKUtil;
import com.microsoft.appcenter.analytics.Analytics;

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
import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

//工具类  目前有获取sharedPreferences 方法
public class Util {
    private static final String LOG = Util.class.getSimpleName();
    private static String accessId = "";

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
        if (getSharedPreferences(context).getInt(GlobalParameters.CameraType, 0) == Camera.CameraInfo.CAMERA_FACING_BACK) {
            if (isActivity) context.startActivity(new Intent(context, IrCameraActivity.class));
            else
                context.startActivity(new Intent(context, IrCameraActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        } else {
            if (isActivity) context.startActivity(new Intent(context, IrCameraActivity.class));
            else
                context.startActivity(new Intent(context, IrCameraActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
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
    public static String getSNCode() {
        if (Build.VERSION.SDK_INT >= 23 && Build.VERSION.SDK_INT <= 28) {
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
        if (dt1.getTime() > dt2.getTime()) {
            isBigger = true;
        } else if (dt1.getTime() < dt2.getTime()) {
            isBigger = false;
        }
        return isBigger;
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
                if (status == 1)
                    ShellUtils.execCommand("echo 5 > /sys/class/backlight/led-brightness/brightness", false);
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

    public static String getUTCDate() {
        try {
            final SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            f.setTimeZone(TimeZone.getTimeZone("UTC"));
            return f.format(new Date());
        } catch (Exception e) {
            e.printStackTrace();
            // 如果throw java.text.ParseException或者NullPointerException，就说明格式不对

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

            JSONObject obj = new JSONObject();
            //  obj.put("DeviceSN", Util.getSerialNumber());

            new AsyncJSONObjectSender(obj, callback, sharedPreferences.getString(GlobalParameters.URL, EndPoints.prod_url) + EndPoints.GenerateToken, context).execute();

        } catch (Exception e) {
            Logger.error(LOG + "getToken(JSONObjectCallback callback, Context context) ", e.getMessage());

        }
    }

    public static void getSettings(SettingCallback callback, Context context) {
        try {
            SharedPreferences sharedPreferences = Util.getSharedPreferences(context);

            JSONObject obj = new JSONObject();
            obj.put("deviceSN", Util.getSNCode());//Util.getSNCode()
            obj.put("institutionId", sharedPreferences.getString(GlobalParameters.INSTITUTION_ID, ""));

            new AsyncJSONObjectSetting(obj, callback, sharedPreferences.getString(GlobalParameters.URL, EndPoints.prod_url) + EndPoints.DEVICESETTING, context).execute();

        } catch (Exception e) {
            Logger.error(LOG + "getSettings(JSONObjectCallback callback, Context context)", e.getMessage());

        }
    }


    public static String getJSONObject(JSONObject req, String url, String header, Context context) {
        try {
            String responseTemp = Requestor.requestJson(url, req, Util.getSNCode(), context, "");
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

    public static String getJSONObjectAddmember(JSONObject req, String url, String header, Context context) {
        try {
            String responseTemp = Requestor.requestJson(url, req, Util.getSNCode(), context, "device_sn");
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
    public static void recordUserTemperature(RecordTemperatureCallback callback, Context context,
                                             IrCameraActivity.UserExportedData data) {
        Log.v("Util", String.format("recordUserTemperature data: %s", data));
        try {
            if (data.temperature == null || data.temperature.isEmpty() || data.temperature.equals("")){
                Log.w(LOG, "recordUserTemperature temperature empty, abort send to server");
                return;
            }

            SharedPreferences sp = Util.getSharedPreferences(context);
            JSONObject obj = new JSONObject();
            obj.put("id", sp.getString(GlobalParameters.SNAP_ID, ""));
            obj.put("deviceId", Util.getSerialNumber());
            obj.put("temperature", data.temperature);
            obj.put("institutionId", sp.getString(GlobalParameters.INSTITUTION_ID, ""));
            obj.put("facilityId", 0);
            obj.put("locationId", 0);
            obj.put("deviceTime", Util.getMMDDYYYYDate());
            if (data.sendImages) {
                obj.put("irTemplate", data.ir == null ? "" : Util.encodeToBase64(data.ir));
                obj.put("rgbTemplate", data.rgb == null ? "" : Util.encodeToBase64(data.rgb));
                obj.put("thermalTemplate", data.thermal == null ? "" : Util.encodeToBase64(data.thermal));
            }
            obj.put("deviceData", MobileDetails(context));
            obj.put("temperatureFormat", sp.getString(GlobalParameters.F_TO_C, "F"));
            obj.put("exceedThreshold", data.exceedsThreshold);
            obj.put("qrCodeId", sp.getString(GlobalParameters.QRCODE_ID, ""));
            if (accessId.isEmpty()) {
                obj.put("accessId", accessId);
            }

            if (data.member == null) data.member = new RegisteredMembers();

            obj.put("accessId", data.member.getAccessid());
            obj.put("firstName", data.member.getFirstname());
            obj.put("lastName", data.member.getLastname());
            obj.put("memberId", data.member.getMemberid());
            obj.put("trqStatus", sp.getString(GlobalParameters.TRQ_STATUS, ""));//TODO: replace
            obj.put("maskStatus", data.maskStatus);
            obj.put("faceScore", data.faceScore);
            if(BuildConfig.DEBUG){
                Log.v(LOG,  "recordUserTemperature: "+obj.toString());
            }
            new AsyncRecordUserTemperature(obj, callback, sp.getString(GlobalParameters.URL, EndPoints.prod_url) + EndPoints.RecordTemperature, context).execute();

        } catch (Exception e) {
            Logger.error(LOG, "getToken(JSONObjectCallback callback, Context context) " + e.getMessage());
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
            Logger.error(LOG + "getBatteryLevel()", e.getMessage());
            return -1;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void activateApplication(JSONObjectCallback callback, Context context) {
        try {
            SharedPreferences sp = Util.getSharedPreferences(context);

            JSONObject obj = new JSONObject();
            obj.put("deviceInfo", MobileDetails(context));

            new AsyncJSONObjectSender(obj, callback, sp.getString(GlobalParameters.URL, EndPoints.prod_url) + EndPoints.ActivateApplication, context).execute();

        } catch (Exception e) {
            Logger.error(LOG, "getToken " + e.getMessage());
            //TODO:warn failed to activate
            Toast.makeText(context, "No Internet Connection", Toast.LENGTH_LONG);
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


        } catch (Exception e) {
            Logger.error(LOG + "getToken(JSONObjectCallback callback, Context context) ", e.getMessage());

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
            Logger.error("getLocalIpAddress()", ex.getMessage());
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
            Logger.error(LOG + "getNumberVersion()", e.getMessage());
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
        if (data == null || cameraParameters == null) return null;
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
            Logger.error(LOG + "KillApp()", e.getMessage());
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
            Logger.error(LOG + "getCurrentTime()", e.getMessage());
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
            SharedPreferences sharedPreferences = Util.getSharedPreferences(context);

            JSONObject obj = new JSONObject();
            obj.put("lastUpdateDateTime", Util.getUTCDate());
            obj.put("deviceSN", Util.getSNCode());
            obj.put("deviceInfo", MobileDetails(context));
            obj.put("institutionId", sharedPreferences.getString(GlobalParameters.INSTITUTION_ID, ""));

            new AsyncJSONObjectSender(obj, callback, sharedPreferences.getString(GlobalParameters.URL, EndPoints.prod_url) + EndPoints.DEVICEHEALTHCHECK, context).execute();

        } catch (Exception e) {
            Logger.error(LOG + "getToken(JSONObjectCallback callback, Context context) ", e.getMessage());

        }
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
//            if(reportInfo.getString("Message").equals("token expired"))
//                Util.getToken((JSONObjectCallback) context,context);
            if (reportInfo.getString("responseCode").equals("1")) {
                JSONObject responseData = reportInfo.getJSONObject("responseData");
                JSONObject jsonValue = responseData.getJSONObject("jsonValue");
                JSONObject jsonValueHome = jsonValue.getJSONObject("HomePageView");
                JSONObject jsonValueScan = jsonValue.getJSONObject("ScanView");
                JSONObject jsonValueConfirm = jsonValue.getJSONObject("ConfirmationView");
                JSONObject jsonValueGuide = jsonValue.getJSONObject("GuideMessages");
                JSONObject jsonValueIdentification = jsonValue.getJSONObject("IdentificationSettings");
                JSONObject jsonValueAccessControl = jsonValue.getJSONObject("AccessControl");
                //Homeview
                String settingVersion = responseData.getString("settingVersion");
                String deviceMasterCode = responseData.getString("deviceMasterCode");
                String homeLogo = jsonValueHome.getString("logo");
                String enableThermal = jsonValueHome.getString("enableThermalCheck");
                String homeLine1 = jsonValueHome.getString("line1");
                String homeLine2 = jsonValueHome.getString("line2");

                Util.writeString(sharedPreferences, GlobalParameters.settingVersion, settingVersion);
                Util.writeString(sharedPreferences, GlobalParameters.deviceMasterCode, deviceMasterCode);
                Util.writeString(sharedPreferences, GlobalParameters.IMAGE_ICON, homeLogo);
                Util.writeString(sharedPreferences, GlobalParameters.Thermalscan_title, homeLine1);
                Util.writeString(sharedPreferences, GlobalParameters.Thermalscan_subtitle, homeLine2);
                Log.d("mastercode", deviceMasterCode);

                //Scan View

                String displayTemperatureDetail = jsonValueScan.getString("displayTemperatureDetail");
                String captureUserImageAboveThreshold = jsonValueScan.getString("captureUserImageAboveThreshold");
                String captureAllUsersImage = jsonValueScan.getString("captureAllUsersImage");
                String enableSoundOnHighTemperature = jsonValueScan.getString("enableSoundOnHighTemperature");
                String viewDelay = jsonValueScan.getString("viewDelay");
                String tempval = jsonValueScan.getString("temperatureThreshold");
                String temperatureFormat = jsonValueScan.getString("temperatureFormat");
                String allowlowtemperaturescanning = jsonValueScan.getString("allowLowTemperatureScanning");
                String lowtemperatureThreshold = jsonValueScan.getString("lowTemperatureThreshold");
                String enableMaskDetection = "";
                if (jsonValueScan.has("enableMaskDetection")) {
                    enableMaskDetection = jsonValueScan.getString("enableMaskDetection");
                }

                Util.writeString(sharedPreferences, GlobalParameters.DELAY_VALUE, viewDelay);
                Util.writeBoolean(sharedPreferences, GlobalParameters.CAPTURE_IMAGES_ABOVE, captureUserImageAboveThreshold.equals("1"));
                Util.writeBoolean(sharedPreferences, GlobalParameters.CAPTURE_IMAGES_ALL, captureAllUsersImage.equals("1"));
                Util.writeBoolean(sharedPreferences, GlobalParameters.CAPTURE_SOUND, enableSoundOnHighTemperature.equals("1"));
                Util.writeBoolean(sharedPreferences, GlobalParameters.CAPTURE_TEMPERATURE, displayTemperatureDetail.equals("1"));
                Util.writeString(sharedPreferences, GlobalParameters.TEMP_TEST, tempval);
                Util.writeString(sharedPreferences, GlobalParameters.F_TO_C, temperatureFormat);
                Util.writeString(sharedPreferences, GlobalParameters.TEMP_TEST_LOW, lowtemperatureThreshold);
                Util.writeBoolean(sharedPreferences, GlobalParameters.ALLOW_ALL, allowlowtemperaturescanning.equals("1"));
                Util.writeBoolean(sharedPreferences, GlobalParameters.MASK_DETECT, enableMaskDetection.equals("1"));

                //ConfirmationView
                String enableConfirmationScreen = jsonValueConfirm.getString("enableConfirmationScreen");
                String normalViewLine1 = jsonValueConfirm.getString("normalViewLine1");
                String normalViewLine2 = jsonValueConfirm.getString("normalViewLine2");
                String aboveThresholdViewLine1 = jsonValueConfirm.getString("aboveThresholdViewLine1");
                String temperatureAboveThreshold2 = jsonValueConfirm.getString("temperatureAboveThreshold2");
                String confirmationviewDelay = jsonValueConfirm.getString("viewDelay");
                String enableConfirmationScreenAboveThreshold = jsonValueConfirm.getString("enableConfirmationScreenAboveThreshold");
                String viewDelayAboveThreshold = jsonValueConfirm.getString("viewDelayAboveThreshold");
//todo in api
                Util.writeBoolean(sharedPreferences, GlobalParameters.CONFIRM_SCREEN_BELOW, enableConfirmationScreen.equals("1"));
                Util.writeBoolean(sharedPreferences, GlobalParameters.CONFIRM_SCREEN_ABOVE, enableConfirmationScreenAboveThreshold.equals("1"));
                Util.writeString(sharedPreferences, GlobalParameters.DELAY_VALUE_CONFIRM_BELOW, confirmationviewDelay);
                Util.writeString(sharedPreferences, GlobalParameters.DELAY_VALUE_CONFIRM_ABOVE, viewDelayAboveThreshold);
                Util.writeString(sharedPreferences, GlobalParameters.Confirm_title_below, normalViewLine1);
                Util.writeString(sharedPreferences, GlobalParameters.Confirm_subtitle_below, normalViewLine2);
                Util.writeString(sharedPreferences, GlobalParameters.Confirm_title_above, aboveThresholdViewLine1);
                Util.writeString(sharedPreferences, GlobalParameters.Confirm_subtitle_above, temperatureAboveThreshold2);
                // Util.writeString(sharedPreferences, GlobalParameters.DELAY_VALUE_CONFIRM_BELOW, confirmationviewDelay);

                //GuideMessages
                String enableGuidMessages = jsonValueGuide.getString("enableGuideMessages");
                String message1 = jsonValueGuide.getString("message1");
                String message2 = jsonValueGuide.getString("message2");
                String message3 = jsonValueGuide.getString("message3");

                Util.writeBoolean(sharedPreferences, GlobalParameters.GUIDE_SCREEN, enableGuidMessages.equals("1"));
                Util.writeString(sharedPreferences, GlobalParameters.GUIDE_TEXT1, message1);
                Util.writeString(sharedPreferences, GlobalParameters.GUIDE_TEXT2, message2);
                Util.writeString(sharedPreferences, GlobalParameters.GUIDE_TEXT3, message3);

                //Identification setting
                String enableQRCodeScanner = jsonValueIdentification.getString("enableQRCodeScanner");
                String enableRFIDScanner = jsonValueIdentification.getString("enableRFIDScanner");
                String identificationTimeout = jsonValueIdentification.getString("identificationTimeout");
                String enableFacialRecognition = jsonValueIdentification.getString("enableFacialRecognition");
                String facialThreshold = jsonValueIdentification.getString("facialThreshold");
                String enableConfirmationNameAndImage = jsonValueIdentification.getString("enableConfirmationNameAndImage");

                Util.writeBoolean(sharedPreferences, GlobalParameters.QR_SCREEN, enableQRCodeScanner.equals("1"));
                Util.writeBoolean(sharedPreferences, GlobalParameters.RFID_ENABLE, enableRFIDScanner.equals("1"));
                Util.writeString(sharedPreferences, GlobalParameters.Timeout, identificationTimeout);
                Util.writeBoolean(sharedPreferences, GlobalParameters.FACIAL_DETECT, enableFacialRecognition.equals("1"));
                Util.writeString(sharedPreferences, GlobalParameters.FACIAL_THRESHOLD, facialThreshold);
                Util.writeBoolean(sharedPreferences, GlobalParameters.DISPLAY_IMAGE_CONFIRMATION, enableConfirmationNameAndImage.equals("1"));

                //access control setting
                String enableAutomaticDoors = jsonValueAccessControl.getString("enableAutomaticDoors");
                String blockAccessHighTemperature = jsonValueAccessControl.getString("blockAccessHighTemperature");
                int doorControlTimeWired = jsonValueAccessControl.getInt("doorControlTimeWired");
                String enableAccessControl = jsonValueAccessControl.getString("enableAccessControl");
                int accessControllerCardFormat = jsonValueAccessControl.getInt("accessControllerCardFormat");

                Util.writeBoolean(sharedPreferences, GlobalParameters.AutomaticDoorAccess, enableAutomaticDoors.equals("1"));
                Util.writeBoolean(sharedPreferences, GlobalParameters.AccessControlEnable, enableAccessControl.equals("1"));
                Util.writeBoolean(sharedPreferences, GlobalParameters.BlockAccessHighTemp, blockAccessHighTemperature.equals("1"));
                Util.writeInt(sharedPreferences, GlobalParameters.AccessControlCardFormat, accessControllerCardFormat);
                Util.writeInt(sharedPreferences, GlobalParameters.RelayTime, doorControlTimeWired);


            } else {
                Logger.toast(context, "Something went wrong please try again");

            }
        } catch (Exception e) {
            Logger.error("retrieveSetting(JSONObject reportInfo)", e.getMessage());
            Logger.toast(context, "Something went wrong please try again");
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static void getTokenActivate(String reportInfo, String status, Context context, String toast) {
        try {
            JSONObject json1 = null;
            SharedPreferences sharedPreferences = Util.getSharedPreferences(context);
            try {
                String formatedString = reportInfo.substring(1, reportInfo.length() - 1);
                json1 = new JSONObject(formatedString.replace("\\", ""));

            } catch (Exception e) {
                json1 = new JSONObject(reportInfo.replace("\\", ""));
            }

            if (status.contains("ActivateApplication")) {
                if (json1.getString("responseCode").equals("1")) {
                    Util.writeBoolean(sharedPreferences, GlobalParameters.ONLINE_MODE, true);
                    if (!toast.equals("guide")) {
                        Logger.toast(context, "Device Activated");
                    } else {
                        Util.switchRgbOrIrActivity(context, true);
                    }
                    Util.getToken((JSONObjectCallback) context, context);

                } else if (json1.getString("responseSubCode").equals("103")) {
                    Util.writeBoolean(sharedPreferences, GlobalParameters.ONLINE_MODE, true);
                    if (!toast.equals("guide")) {
                        Logger.toast(context, "Already Activated");
                    } else {
                        Util.switchRgbOrIrActivity(context, true);
                    }
                    Util.getToken((JSONObjectCallback) context, context);

                } else if (json1.getString("responseSubCode").equals("104")) {
                    Util.writeBoolean(sharedPreferences, GlobalParameters.ONLINE_MODE, false);
                    openDialogactivate(context, "This device SN: " + Util.getSNCode() + " " + context.getResources().getString(R.string.device_not_register), toast);
                } else if (json1.getString("responseSubCode").equals("105")) {
                    Util.writeBoolean(sharedPreferences, GlobalParameters.ONLINE_MODE, false);
                    openDialogactivate(context, "This device SN: " + Util.getSNCode() + " " + context.getResources().getString(R.string.device_inactive), toast);
                }
            } else {
                if (json1.isNull("access_token")) return;
                String access_token = json1.getString("access_token");
                String token_type = json1.getString("token_type");
                String institutionId = json1.getString("InstitutionID");
                Util.writeString(sharedPreferences, GlobalParameters.ACCESS_TOKEN, access_token);
                Util.writeString(sharedPreferences, GlobalParameters.TOKEN_TYPE, token_type);
                Util.writeString(sharedPreferences, GlobalParameters.INSTITUTION_ID, institutionId);
                Util.getSettings((SettingCallback) context, context);

//                ManageMemberHelper.loadMembers(access_token, Util.getSerialNumber(), context.getFilesDir().getAbsolutePath());
            }
        } catch (Exception e) {
            Util.switchRgbOrIrActivity(context, true);
            Logger.error("getTokenActivate(String reportInfo,String status,Context context)", e.getMessage());
        }
    }

    public static void getQRCode(JSONObject reportInfo, String status, Context context, String toast) {
        try {
            SharedPreferences sharedPreferences = Util.getSharedPreferences(context);
            String id = reportInfo.getJSONObject("responseData").getString("id");
            String firstName = reportInfo.getJSONObject("responseData").getString("firstName");
            String lastName = reportInfo.getJSONObject("responseData").getString("lastName");
            String trqStatus = reportInfo.getJSONObject("responseData").getString("trqStatus");
            String memberId = reportInfo.getJSONObject("responseData").getString("memberId");
            String qrAccessid = reportInfo.getJSONObject("responseData").getString("accessId");
            //TODO:
            Util.writeString(sharedPreferences, GlobalParameters.SNAP_ID, id);
            Util.writeString(sharedPreferences, GlobalParameters.FIRST_NAME, firstName);
            Util.writeString(sharedPreferences, GlobalParameters.LAST_NAME, lastName);
            Util.writeString(sharedPreferences, GlobalParameters.TRQ_STATUS, trqStatus);
            Util.writeString(sharedPreferences, GlobalParameters.MEMBER_ID, memberId);
            Util.writeString(sharedPreferences, GlobalParameters.ACCESS_ID, qrAccessid);


        } catch (Exception e) {
            Logger.error("getQRCode(JSONObject reportInfo, String status, Context context, String toast) ", e.getMessage());
        }
    }

    public static void soundPool(Context context, String tempVal, SoundPool soundPool) {
        if (soundPool == null) return;
        try {

            if (tempVal.equals("high")) {
                soundPool.load(context, R.raw.failed_last, 1);
            } else {
                soundPool.load(context, R.raw.thankyou_last, 1);
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
            Logger.error(" beepSound(Context context,String tempVal) ", e.getMessage());
        }


    }

    public static String getVersionBuild() {
        return String.format("v%s.%s", BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE);
    }

    public static void setAccessId(String id) {
        accessId = id;
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
                Intent intent = new Intent(context, SettingActivity.class);
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
            new AsyncJSONObjectGetMemberList(obj, callback, sharedPreferences.getString(GlobalParameters.URL, EndPoints.prod_url) + EndPoints.GetMemberList, context).execute();

        } catch (Exception e) {
            Logger.error(LOG + "getToken(JSONObjectCallback callback, Context context) ", e.getMessage());

        }
    }

    public static JSONObject getJSONObjectMemberList(JSONObject req, String url, String header, Context context,String device_sn) {
        try {
            String responseTemp = Requestor.requestJson(url, req, Util.getSNCode(), context,"device_sn");
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
    public static JSONObject getJSONObjectMemberData(JSONObject req, String url, String header, Context context,String device_sn) {
        try {
            String responseTemp = Requestor.requestJson(url, req, Util.getSNCode(), context,"device_sn");
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
}
