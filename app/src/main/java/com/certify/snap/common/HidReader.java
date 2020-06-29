package com.certify.snap.common;

import android.app.Activity;
import android.util.Log;

import com.telpo.tps550.api.serial.Serial;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicBoolean;

//card reader on serial port /dev/ttyS0.
public class HidReader {

    private static final String TAG = "HidReader"; 
    private Serial serial;
    private InputStream inputStream;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private OutputStream outputStream;

    private String serialPath = "/dev/ttyS0";
    private ReadThread readThread;
    private RfidScanCallback callback;

    public void start(RfidScanCallback callback){
        try{
            Log.v(TAG, "start open serial port: "+ serialPath);
            this.callback = callback;
            serial = new Serial(serialPath, 9600, 0);
            inputStream = serial.getInputStream();
            outputStream = serial.getOutputStream();
            readThread = new ReadThread();
            readThread.start();
        }catch(Exception e){
            Logger.warn(TAG, "start "+e.getMessage());
        }
    }

    public void setCallbackListener(RfidScanCallback callbackListener) {
        this.callback = callbackListener;
    }

    public void stop(){
        Log.v(TAG, "stop");
        try{
            callback = null;
            inputStream.reset();
            inputStream.close();
            if(readThread != null){
                readThread.cancel();
                //readThread.join();
            }
            if(serial != null) serial.close();
        }catch (Exception e){
            Logger.warn(TAG, "stop "+e.getMessage());
        }
    }
    private class ReadThread extends Thread {
        public void cancel(){
            running.set(false);
        }
        private void onReadCardData(String cardId) {
            Log.v(TAG, "HidReader cardData: " + cardId);
            if (callback != null) {
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        callback.onRfidScan(cardId);
                    }
                };
                if (callback instanceof Activity) {
                    Activity activity = (Activity) callback;
                    activity.runOnUiThread(runnable);
                } else {
                    new Thread(runnable).start();
                }
            }
        }
        @Override
        public void run() {
            super.run();
            running.set(true);
            while (running.get()) {
                sleep(10);
                if(inputStream != null){

                    int size = 0;
                    byte[] buffer = new byte[64];
                    try{
                        size = inputStream.available();
                        if(size > 0){
                            size = inputStream.read(buffer);
                            if(size > 0){
                                String cardData = new String(buffer, 0, size, "UTF-8");
                                onReadCardData(cardData);
                            }
                        }
                    }catch(Exception e){
                        Logger.warn(TAG, "HidReader "+e.getMessage());
                    }
                }
            }
        }

        private void sleep(int ms) {
            try {
                Thread.sleep(ms);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
public interface  RfidScanCallback{
        void onRfidScan(String cardId);
}
}
