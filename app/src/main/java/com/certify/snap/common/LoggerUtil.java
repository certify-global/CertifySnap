package com.certify.snap.common;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class LoggerUtil {
    private static LoggerUtil instance = null;
    private LogMessagesCallback listener = null;

    public interface LogMessagesCallback {
        void onLogMessagesToFile(String fileName);
    }

    public static LoggerUtil getInstance() {
        if (instance == null) {
            instance = new LoggerUtil();
        }
        return instance;
    }

    public void setListener(LogMessagesCallback callbackListener) {
        listener = callbackListener;
    }

    static void logMessagesToFile(Context context) {
        Date date = new Date();
        SimpleDateFormat df = new SimpleDateFormat("MM-dd-yyyy 'T'HH:mm", Locale.US);
        String dirPath = Environment.getExternalStorageDirectory() + File.separator + "CertifySnap" + File.separator + "Log" + File.separator;
        String fileName = dirPath + "CrashLog " + df.format(date) + ".log";
        File file = new File(fileName);
        file.mkdirs();
        Util.writeString(Util.getSharedPreferences(context), GlobalParameters.LogFilePath, fileName); //TODO1: Optimize

        //clears a file
        if (file.exists()) {
            file.delete();
        }

        try {
            String command = String.format("logcat -d -v -t threadtime *:*");
            Process process = Runtime.getRuntime().exec(command);

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder result = new StringBuilder();
            String currentLine = null;

            while ((currentLine = reader.readLine()) != null) {
                result.append(currentLine);
                result.append("\n");
            }

            FileWriter out = new FileWriter(file);
            out.write(result.toString());
            out.close();
        } catch (IOException e) {
            Logger.error("LoggerUtil", "logMessagesToFile()","Error in writing to File");
        }
    }

    public void logMessagesToFile(Context context, String filename) {
        Observable
                .create((ObservableOnSubscribe<String>) emitter -> {
                    Date date = new Date();
                    String dirPath = Environment.getExternalStorageDirectory() + File.separator + "CertifySnap" + File.separator + "Log" + File.separator;
                    String fileName = dirPath +  filename + ".log";
                    File dir = new File(dirPath);
                    File file = new File (fileName);
                    //Util.writeString(Util.getSharedPreferences(context), GlobalParameters.LogFilePath, fileName); //TODO1: Optimize

                    //clears a file
                    if (!dir.exists()) {
                        dir.mkdirs();
                    } else {
                        long fileSize = file.length()/(1024 * 1024);
                        if (fileSize > 10) {
                            file.delete();
                        }
                    }

                    try {
                        //String time = Util.getMMDDYYYYDate();
                        SimpleDateFormat format = new SimpleDateFormat("MM-dd HH:mm:ss.zzz", Locale.US);
                        Date curDate = new Date(System.currentTimeMillis());
                        String time = format.format(curDate);

                        String command = String.format("logcat -d -v -t %s", Util.getDateTimePastHour(time));
                        Process process = Runtime.getRuntime().exec(command);

                        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                        StringBuilder result = new StringBuilder();
                        String currentLine = null;

                        while ((currentLine = reader.readLine()) != null) {
                            if(currentLine.contains("skia") || currentLine.contains("expire")) continue;
                            result.append(currentLine);
                            result.append("\n");
                        }
                        Util.writeToFile(file, file.length(), result.toString().getBytes());
                    } catch (IOException e) {
                        Logger.error("LoggerUtil", "logMessagesToFile()","Error in writing to File");
                    }
                    emitter.onNext(fileName);
                })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    Disposable logMessagesDisposable;

                    @Override
                    public void onSubscribe(Disposable d) {
                        logMessagesDisposable = d;
                    }

                    @Override
                    public void onNext(String value) {
                        if (listener != null) {
                            listener.onLogMessagesToFile(value);
                        }
                        logMessagesDisposable.dispose();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("LoggerUtil", "Error in logging messages to the file");
                        logMessagesDisposable.dispose();
                    }

                    @Override
                    public void onComplete() {
                        //do noop
                    }
                });
    }
}
