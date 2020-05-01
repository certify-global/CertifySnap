package com.certify.snap.common;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import com.certify.snap.activity.GuideActivity;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author
 *
 */
public class MyCrashHandler implements UncaughtExceptionHandler {

	/** TAG */
	private static final String TAG = "CrashHandler";
	/**
	 * 报错大类 内含两个小类
	 */
	private Throwable throwable;
	/**
	 * 报错信息
	 */
	public static String error = null;
	/**
	 * localFileUrl
	 * 本地log文件的存放地址
	 */
	private static String localFileUrl = "";
	/** mDefaultHandler */
	private UncaughtExceptionHandler defaultHandler;

	/** instance */
	private static MyCrashHandler instance = new MyCrashHandler();

	/** infos */
	private Map<String, String> infos = new HashMap<String, String>();

	/** formatter */
	private DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	/** context*/
	private Application context;
	private MyCrashHandler() {}

	public static MyCrashHandler getInstance() {
		if (instance == null) {
			instance = new MyCrashHandler();
		}
		return instance;
	}

	/**
	 *
	 * @param ctx
	 * 初始化，此处最好在Application的OnCreate方法里来进行调用
	 */
	public void init(Application ctx) {
		this.context = ctx;
		defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(this);
	}

	/**
	 * uncaughtException
	 * 在这里处理为捕获的Exception
	 */
	@Override
	public void uncaughtException(Thread thread, Throwable throwable) {
		if(!handleException(throwable) && defaultHandler != null){
			defaultHandler.uncaughtException(thread, throwable);
		}else {
			restart();
		}
		handleException(throwable);
		defaultHandler.uncaughtException(thread, throwable);
	}
	private boolean handleException(Throwable ex) {
		if (ex == null) {
			return false;
		}
//		 // 使用 Toast 来显示异常信息
//        new Thread() {
//            @Override
//            public void run() {
//                Looper.prepare();
//                Toast.makeText(context, "很抱歉，程序出现异常，即将退出。", Toast.LENGTH_LONG).show();
//                Looper.loop();
//            }
//        }.start();
		collectDeviceInfo(context);
		throwable = ex;
		ex.printStackTrace();
		writeCrashInfoToFile(ex);

		Log.e(TAG, "restart--- "+ex.toString());// 重启程序
		android.os.Process.killProcess(android.os.Process.myPid());
		Intent intent = new Intent(context.getApplicationContext()   , GuideActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
				Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
//		restart();
		return true;
	}

	/**
	 *
	 * @param ctx
	 * 手机设备相关信息
	 */
	public void collectDeviceInfo(Context ctx) {
		try {
			PackageManager pm = ctx.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(),
					PackageManager.GET_ACTIVITIES);
			if (pi != null) {
				String versionName = pi.versionName == null ? "null"
						: pi.versionName;
				String versionCode = pi.versionCode + "";
				infos.put("versionName", versionName);
				infos.put("versionCode", versionCode);
				infos.put("crashTime", formatter.format(new Date()));
			}
		} catch (NameNotFoundException e) {
			Log.e(TAG, "an error occured when collect package info", e);
		}
		Field[] fields = Build.class.getDeclaredFields();
		for (Field field: fields) {
			try {
				field.setAccessible(true);
				infos.put(field.getName(), field.get(null).toString());
			} catch (Exception e) {
				Log.e(TAG, "an error occured when collect crash info", e);
			}
		}
	}

	/**
	 *
	 * @param ex
	 * 将崩溃写入文件系统
	 */
	private void writeCrashInfoToFile(Throwable ex) {
		StringBuffer sb = new StringBuffer();
		for (Map.Entry<String, String> entry: infos.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			sb.append(key + "=" + value + "\n");
		}

		Writer writer = new StringWriter();
		PrintWriter printWriter = new PrintWriter(writer);
		ex.printStackTrace(printWriter);
		Throwable cause = ex.getCause();
		while (cause != null) {
			cause.printStackTrace(printWriter);
			cause = cause.getCause();
		}
		printWriter.close();
		String result = writer.toString();
		sb.append(result);

		//这里把刚才异常堆栈信息写入SD卡的Log日志里面
		if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
		{
//			String sdcardPath = Environment.getExternalStorageDirectory().getPath();
			String sdcardPath = Environment.getExternalStorageDirectory() + File.separator + "Log";
			String filePath = sdcardPath + "/Telpoface/";
			Log.e("报错日志地址", "writeCrashInfoToFile: "+filePath);
			localFileUrl = writeLog(sb.toString(), filePath);
		}
	}

	/**
	 *
	 * @param log
	 * @param name
	 * @return 返回写入的文件路径
	 * 写入Log信息的方法，写入到SD卡里面
	 */
	private String writeLog(String log, String name)
	{
		Calendar c = Calendar.getInstance();
		CharSequence timestamp = new Date().toString().replace(" ", "");
		timestamp  = "crash-" ;
		String filename = name + timestamp + c.get(Calendar.YEAR)  +(c.get(Calendar.MONTH)+1) +"-"+ c.get(Calendar.DAY_OF_MONTH) +c.get(Calendar.HOUR_OF_DAY) + c.get(Calendar.MINUTE)  + ".log";

		File file = new File(filename);
		if(!file.getParentFile().exists()){
			file.getParentFile().mkdirs();
		}
		try
		{
			//			FileOutputStream stream = new FileOutputStream(new File(filename));
			//			OutputStreamWriter output = new OutputStreamWriter(stream);
			file.createNewFile();
			FileWriter fw=new FileWriter(file,true);
			BufferedWriter bw = new BufferedWriter(fw);
			//写入相关Log到文件
			bw.write(log);
			bw.newLine();
			bw.close();
			fw.close();
			return filename;
		}
		catch (IOException e)
		{
			Log.e(TAG, "an error occured while writing file...", e);
			e.printStackTrace();
			return null;
		}
	}

	private void judge(){
		if(throwable.getCause() instanceof StringIndexOutOfBoundsException){
			error = "字符串下标越界异常";
		}
		else if(throwable.getCause() instanceof NullPointerException){
			error = "空指针异常";
		}
		else if(throwable.getCause() instanceof IllegalArgumentException){
			error = "传递非法参数异常";
		}
		else if(throwable.getCause() instanceof ArrayStoreException){
			error = "向数组中存放与声明类型不兼容对象异常 ";
		}
		else if(throwable.getCause() instanceof IndexOutOfBoundsException){
			error = "下标越界异常";
		}
		else if(throwable.getCause() instanceof UnsupportedOperationException){
			error = "不支持的操作异常";
		}
		else if(throwable.getCause() instanceof RuntimeException){
			error = "运行时异常";
		}

	}

	private void restart(){
		try{
			Thread.sleep(10);
		}catch (InterruptedException e){
			Log.e(TAG, "error : ", e);
		}
//		 context.finishAllActivity();
//		 // 退出程序,注释下面的重启启动程序代码
//         android.os.Process.killProcess(android.os.Process.myPid());
//         System.exit(1);

//		judge();
//		Intent intent = new Intent(context.getApplicationContext(), IrCameraActivity.class);
//		intent.putExtra("error", error);
//		PendingIntent restartIntent = PendingIntent.getActivity(
//				context.getApplicationContext(), 0, intent,
//				Intent.FLAG_ACTIVITY_NEW_TASK);
////         //退出程序
//		AlarmManager mgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
//		mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 10,
//				restartIntent); // 1秒钟后重启应用


	}

}
