package com.certify.snap.controller;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.RemoteException;
import android.util.Log;

import com.certify.snap.R;
import com.certify.snap.common.AppSettings;
import com.certify.snap.common.Application;
import com.certify.snap.common.UserExportedData;
import com.certify.snap.common.Util;
import com.common.thermalimage.GuideDataCallBack;
import com.common.thermalimage.TemperatureBigData;
import com.common.thermalimage.ThermalImageUtil;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TemperatureController {
    private static final String TAG = TemperatureController.class.getSimpleName();
    private static TemperatureController instance = null;
    private ConcurrentHashMap<Integer, UserExportedData> temperatureMap = new ConcurrentHashMap<>();
    private Context context;
    private ThermalImageUtil thermalImageUtil;
    private float temperature = 0;
    private TemperatureCallbackListener listener;
    private boolean isTemperatureInProcess = false;
    private String temperatureUnit = "";
    private UserExportedData temperatureRecordData = null;

    public interface TemperatureCallbackListener {
        void onThermalImage(Bitmap bitmap);
        void onTemperatureRead(float temperature);
    }

    public static TemperatureController getInstance() {
        if (instance == null)
            instance = new TemperatureController();

        return instance;
    }

    public void init(Context context) {
        this.context = context;
        thermalImageUtil = Application.getInstance().getTemperatureUtil();
        if (AppSettings.getfToC().equals("F")) {
            temperatureUnit = context.getString(R.string.fahrenheit_symbol);
        } else {
            temperatureUnit = context.getString(R.string.centi);
        }
    }

    public void setTemperatureListener (TemperatureCallbackListener callbackListener) {
        this.listener = callbackListener;
    }

    public ConcurrentHashMap<Integer, UserExportedData> getTemperatureMap() {
        return temperatureMap;
    }

    public void updateTemperatureMap(int trackId, UserExportedData data) {
        boolean value = temperatureMap.containsKey(trackId);
        if (!value) {
            temperatureMap.put(trackId, data);
            sendTemperatureRecord(context);
        }
    }

    private void sendTemperatureRecord(Context context) {
        for (Map.Entry entry : temperatureMap.entrySet()) {
            UserExportedData data = (UserExportedData) entry.getValue();
            Util.recordUserTemperature(null, context, data, -1);
        }
        clearTemperatureMap();
    }

    public void startTemperatureMeasure() {
        Log.d(TAG, "Temp startTemperatureMeasure");
        if (thermalImageUtil == null || isTemperatureInProcess) return;
        isTemperatureInProcess = true;
        Log.d(TAG, "Temp startTemperatureMeasure getGuideData");
        thermalImageUtil.getGuideData(new GuideDataCallBack.Stub() {
            @Override
            public void callBackBitmap(final Bitmap bitmap) throws RemoteException {
                //update UI
                if (listener != null) {
                    listener.onThermalImage(bitmap);
                }
            }

            @Override
            public void callBackData(TemperatureBigData temperatureBigData) throws RemoteException {
                try {
                    Log.d(TAG, "Temp callBackData");
                    final List<float[]> maxInRectInfo = temperatureBigData.getTemInfoList();//The length of the List is the number of rects,
                    // which records the temperature measurement information of the rect of each face
                    final float envirTem = temperatureBigData.getEmvirTem();//Ambient temperature
                    if (maxInRectInfo == null) {
                        Log.e(TAG, "Temp Error data");
                        return;
                    }
                    float temp;
                    for (int i = 0; i < maxInRectInfo.size(); i++) {
                        temperature = maxInRectInfo.get(i)[3];
                        Log.d(TAG, "Temp measured " + temperature);
                        float tempFarhenheit = (float) Util.celsiusToFahrenheit(temperature);
                        thermalImageUtil.stopGetGuideData();
                        if (listener != null) {
                            listener.onTemperatureRead(tempFarhenheit);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void callBackError(String s) throws RemoteException {

            }
        });
    }

    public String getTemperatureUnit() {
        return temperatureUnit;
    }

    public boolean isTemperatureAboveThreshold(float temperature) {
        boolean result = false;
        if (temperature > Float.parseFloat(AppSettings.getTemperatureThreshold())) {
            result = true;
        }
        return result;
    }

    public UserExportedData getTemperatureRecordData() {
        return temperatureRecordData;
    }

    public void setTemperatureRecordData(UserExportedData temperatureRecordData) {
        this.temperatureRecordData = temperatureRecordData;
    }

    private void clearTemperatureMap() {
        temperatureMap.clear();
    }

    public void clearData() {
        isTemperatureInProcess = false;
        listener = null;
        thermalImageUtil.stopGetGuideData();
        temperatureRecordData = null;
    }
}
