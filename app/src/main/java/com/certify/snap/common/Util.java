package com.certify.snap.common;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.certify.pos.api.util.PosUtil;
import com.certify.callback.JSONObjectCallback;
import com.certify.callback.RecordTemperatureCallback;
import com.certify.snap.activity.IrCameraActivity;
import com.certify.snap.async.AsyncJSONObjectSender;
import com.certify.snap.async.AsyncRecordUserTemperature;
import com.example.a950jnisdk.SDKUtil;
import com.microsoft.appcenter.analytics.Analytics;

import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.UUID;

//工具类  目前有获取sharedPreferences 方法
public class Util {
    private static final String LOG = "Utils";

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

    //保存bitmap
    public static String saveBitmapFile(Bitmap bm, String fileName) throws IOException {//将Bitmap类型的图片转化成file类型，便于上传到服务器
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

    //获取带0的数值
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
     * 缩放Bitmap图片
     **/
    public static Bitmap zoomBitmap(Bitmap bitmap, int width, int height) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        Matrix matrix = new Matrix();
        float scaleWidth = ((float) width / w);
        float scaleHeight = ((float) height / h);
        matrix.postScale(scaleWidth, scaleHeight);// 利用矩阵进行缩放不会造成内存溢出
        Bitmap newbmp = Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);
        return newbmp;
    }

    /**
     * 以最省内存的方式读取本地资源的图片
     */
    public static Bitmap readBitMap(String path) {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inPreferredConfig = Bitmap.Config.RGB_565;
        opt.inPurgeable = true;
        opt.inInputShareable = true;
        // 获取资源图片
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

    // 如果是放大图片，filter决定是否平滑，如果是缩小图片，filter无影响
    public static Bitmap createScaleBitmap(Bitmap src, int dstWidth, int dstHeight) {
        Bitmap dst = Bitmap.createScaledBitmap(src, dstWidth, dstHeight, false);
        if (src != dst) { // 如果没有缩放，那么不回收
            src.recycle(); // 释放Bitmap的native像素数组
        }
        return dst;
    }

    // 从Resources中加载图片
    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options); // 读取图片长款
        options.inSampleSize = calculateInSampleSize(options, reqWidth,
                reqHeight); // 计算inSampleSize
        options.inJustDecodeBounds = false;
        Bitmap src = BitmapFactory.decodeResource(res, resId, options); // 载入一个稍大的缩略图
        return createScaleBitmap(src, reqWidth, reqHeight); // 进一步得到目标大小的缩略图
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

    //开关led
    public static void setLedPower(int status) {
        if (Build.MODEL.contains("950") || "TPS980Q".equals(Build.MODEL)) {
            new SDKUtil().camera_led(status);
        } else {
            PosUtil.setLedPower(status);
        }
    }

    //开关led2
    public static void enableLedPower(int status) {
        try {
            if (Build.MODEL.contains("950") || "TPS980Q".equals(Build.MODEL)) {
                new SDKUtil().camera_led(status);
            } else {
                PosUtil.setLedPower(status);
                if (status == 1)
                    ShellUtils.execCommand("echo 5 > /sys/class/backlight/led-brightness/brightness", false);
            }
        }catch (Exception e){
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
    public static float FahrenheitToCelcius(float celcius) {

        return ((celcius * 9) / 5) + 32;
    }

    public static void error(String classname, String message) {
        Analytics.trackEvent("Error:" + message);

    }

    public static String encodeToBase64(Bitmap image) {
        Bitmap immage = image;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        immage.compress(Bitmap.CompressFormat.PNG, 80, baos);
        byte[] b = baos.toByteArray();
        String imageEncoded = Base64.encodeToString(b, Base64.DEFAULT);
        return imageEncoded;
    }

    public static Bitmap decodeToBase64(String input) {
        byte[] decodedByte = Base64.decode(input, 0);
        return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
    }

    public static void getToken(JSONObjectCallback callback, Context context) {
        try {
            SharedPreferences sharedPreferences = Util.getSharedPreferences(context);

            JSONObject obj = new JSONObject();
            //  obj.put("DeviceSN", Util.getSerialNumber());

            new AsyncJSONObjectSender(obj, callback,sharedPreferences.getString(GlobalParameters.URL,EndPoints.prod_url)+ EndPoints.GenerateToken, context).execute();

        } catch (Exception e) {
            Logger.error(LOG + "getToken(JSONObjectCallback callback, Context context) ", e.getMessage());

        }
    }


    public static String getJSONObject(JSONObject req, String url, String header, Context context) {
        try {
            String responseTemp = Requestor.requestJson(url, req, Util.getSNCode(), context);
            if (responseTemp != null && !responseTemp.equals(""))
                return new String(responseTemp);
        } catch (Exception e) {

            Logger.error(LOG + "getJSONObject(JSONObject req, String url): req = " + req
                    + ", url = " + url, e.getMessage());
            return null;

        }
        return null;
    }


    public static String getJSONObjectTemp(JSONObject req, String url, String header, Context context) {
        try {
            String responseTemp = Requestor.postJson(url, req, Util.getSNCode(), context);
            if (responseTemp != null && !responseTemp.equals(""))
                return new String(responseTemp);
        } catch (Exception e) {

            Logger.error(LOG + "getJSONObject(JSONObject req, String url): req = " + req
                    + ", url = " + url, e.getMessage());
            return null;

        }
        return null;
    }

    public static boolean isConnectingToInternet(Context context) {
        try {
            ConnectivityManager cm = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
            if (cm != null)
                for (NetworkInfo ni : cm.getAllNetworkInfo()) {
                    switch (ni.getTypeName().trim().toUpperCase()) {
                        case "WIFI":
                        case "MOBILE":
                            if (ni.isConnected() && !(ni.getSubtypeName() != null && ni.getSubtypeName().trim().toUpperCase().equals("LTE") && ni.getExtraInfo() != null && ni.getExtraInfo().trim().toUpperCase().equals("IMS"))) {
                                // MyApplication.noInternet = 0;
                                return true;
                            }
                            break;
                    }
                }
        } catch (Exception e) {
            Logger.error(LOG + "isConnectingToInternet()", e.getMessage());
        }
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void recordUserTemperature(RecordTemperatureCallback callback, Context context, String temperature, Bitmap irBit, Bitmap rgbBit, Bitmap therbit) {
        try {
            SharedPreferences sp = Util.getSharedPreferences(context);
            JSONObject obj = new JSONObject();
            obj.put("certifyId", 0);
            obj.put("deviceId", Util.getSerialNumber());
            obj.put("temperature", temperature);
            obj.put("institutionId", sp.getString(GlobalParameters.INSTITUTION_ID, ""));
            obj.put("facilityId", 0);
            obj.put("locationId", 0);
            obj.put("deviceTime", Util.getMMDDYYYYDate());
            obj.put("irTemplate", irBit == null ? "" : Util.encodeToBase64(irBit));
            obj.put("rgbTemplate", rgbBit == null ? "" : Util.encodeToBase64(rgbBit));
            obj.put("thermalTemplate", therbit == null ? "" : Util.encodeToBase64(therbit));
            obj.put("deviceData", MobileDetails(context));

            new AsyncRecordUserTemperature(obj, callback,sp.getString(GlobalParameters.URL,EndPoints.prod_url)+EndPoints.RecordTemperature, context).execute();

        } catch (Exception e) {
            Logger.error(LOG + "getToken(JSONObjectCallback callback, Context context) ", e.getMessage());

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

            getNumberVersion((Activity) context);
            getDeviceUUid((Activity) context);


            JSONObject obj = new JSONObject();
            obj.put("deviceInfo", MobileDetails(context));


            new AsyncJSONObjectSender(obj, callback,sp.getString(GlobalParameters.URL,EndPoints.prod_url)+ EndPoints.ActivateApplication, context).execute();

        } catch (Exception e) {
            Logger.error(LOG + "getToken(JSONObjectCallback callback, Context context) ", e.getMessage());

        }
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    public static JSONObject MobileDetails(Context context) {
        JSONObject obj = new JSONObject();
        try {
            SharedPreferences sp = Util.getSharedPreferences(context);

            getNumberVersion((Activity) context);
            getDeviceUUid((Activity) context);

            obj.put("osVersion", "Android - " + Build.VERSION.RELEASE);
            obj.put("appVersion", sp.getString(GlobalParameters.MobileAppVersion, ""));
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
        return  obj;
    }
    public static Bitmap faceValidation(byte[] oldImage) {
//       ByteArrayOutputStream oldByte = new ByteArrayOutputStream();
//       ByteArrayInputStream arrayInputStream = new ByteArrayInputStream(oldImage);
//       Bitmap oldBitmap = BitmapFactory.decodeStream(arrayInputStream);
//       oldBitmap.compress(Bitmap.CompressFormat.JPEG, 100, oldByte);
        // String bbj= encodeToBase64(BitmapFactory.decodeByteArray(oldImage, 0, oldImage.length));
        return BitmapFactory.decodeByteArray(oldImage, 0, oldImage.length);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint({"MissingPermission", "HardwareIds"})
    public static void getDeviceUUid(Activity activity) {
        Logger.debug("uuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuu", "splash");
        SharedPreferences sp = Util.getSharedPreferences(activity);

        if (!sp.getString(GlobalParameters.UUID, "").isEmpty())
            return;
        String deviceUUid = null;
        try {
            try {
                if (!Util.PermissionRequest(activity, permission.phone))
                    //noinspection ConstantConditions
                    deviceUUid = ((TelephonyManager) activity.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
                Util.writeString(sp, GlobalParameters.IMEI, deviceUUid);


            } catch (Exception ignored) {
            }
            try {
                if (deviceUUid == null)
                    deviceUUid = Settings.Secure.getString(activity.getContentResolver(), Settings.Secure.ANDROID_ID);
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
    public static void getNumberVersion(Activity activity) {
        try {
            SharedPreferences sp = Util.getSharedPreferences(activity);

            TelephonyManager tMgr = (TelephonyManager) activity.getSystemService(Context.TELEPHONY_SERVICE);
            if (tMgr != null) {
                if (!Util.PermissionRequest(activity, permission.phone))
                    Util.writeString(sp, GlobalParameters.MOBILE_NUMBER, tMgr.getLine1Number());
            }
            PackageInfo packageInfo = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);
            Util.writeString(sp, GlobalParameters.MobileAppVersion, packageInfo.versionName);
        } catch (Exception e) {
            Logger.error(LOG + "getNumberVersion()", e.getMessage());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static boolean PermissionRequest(android.app.Activity context, String[] permissions) {
        try {
            if (permissions == null) return false;
            ArrayList<String> requestPermission = new ArrayList<>();
            for (String permission : permissions) {
                int permissionCheck = context.checkSelfPermission(permission);
                if (permissionCheck != PackageManager.PERMISSION_GRANTED)
                    requestPermission.add(permission);
            }
            if (requestPermission.size() <= 0) return false;
            context.requestPermissions(requestPermission.toArray(new String[0]), 1);
        } catch (Exception e) {
            Logger.error(LOG + "PermissionRequest(android.app.Activity context, String[] permissions", e.getMessage());
        }
        return true;
    }

    public static Bitmap convertYuvByteArrayToBitmap(byte[] data, Camera camera) {
        Camera.Parameters parameters = camera.getParameters();
        Camera.Size size = parameters.getPreviewSize();
        YuvImage image = new YuvImage(data, parameters.getPreviewFormat(), size.width, size.height, null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        image.compressToJpeg(new Rect(0, 0, size.width, size.height), 100, out);
        byte[] imageBytes = out.toByteArray();
//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inJustDecodeBounds = true;
//        options.inSampleSize = 3;
        Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

}
