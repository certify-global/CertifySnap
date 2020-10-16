package com.certify.snap.common;

import android.content.Context;
import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LoggerUtil {

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
            String command = String.format("logcat -d -v threadtime *:*");
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

    public static void logMessagesToFile(Context context, String filename) {
        Date date = new Date();
        SimpleDateFormat df = new SimpleDateFormat("MM-dd-yyyy 'T'HH:mm", Locale.US);
        String dirPath = Environment.getExternalStorageDirectory() + File.separator + "CertifySnap" + File.separator + "Log" + File.separator;
        String fileName = dirPath +  filename + " " + df.format(date) + ".log";
        File file = new File(fileName);
        file.mkdirs();
        //Util.writeString(Util.getSharedPreferences(context), GlobalParameters.LogFilePath, fileName); //TODO1: Optimize

        //clears a file
        if (file.exists()) {
            file.delete();
        }

        try {
            String command = String.format("logcat -d -v threadtime *:*");
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
}
