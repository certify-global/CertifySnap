package com.certify.snap.controller;

import android.content.Context;
import android.util.Log;

import com.certify.snap.activity.ProIrCameraActivity;
import com.certify.snap.common.UserExportedData;
import com.certify.snap.common.Util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TemperatureController {
    private static TemperatureController instance = null;
    private ConcurrentHashMap<Integer, UserExportedData> temperatureMap = new ConcurrentHashMap<>();
    private Context context;

    public static TemperatureController getInstance() {
        if (instance == null)
            instance = new TemperatureController();

        return instance;
    }

    public void init(Context context){
        this.context = context;
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
            Util.recordUserTemperature(null, context, data);
        }
        clearData();
    }

    private void clearData() {
        temperatureMap.clear();
    }
}
