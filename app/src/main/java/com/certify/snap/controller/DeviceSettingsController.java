package com.certify.snap.controller;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.certify.callback.GetLanguagesCallback;
import com.certify.snap.api.response.LanguageData;
import com.certify.snap.api.response.LanguageListResponse;
import com.certify.snap.async.AsyncGetLanguages;
import com.certify.snap.common.EndPoints;
import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.Logger;
import com.certify.snap.common.Util;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

public class DeviceSettingsController implements GetLanguagesCallback {
    private static final String TAG = DeviceSettingsController.class.getSimpleName();
    private static DeviceSettingsController instance = null;
    private Context context;
    private SharedPreferences sharedPreferences;

    public static DeviceSettingsController getInstance() {
        if (instance == null) {
            instance = new DeviceSettingsController();
        }
        return instance;
    }

    public void init(Context context) {
        this.context = context;
        this.sharedPreferences = Util.getSharedPreferences(context);
    }

    public void getLanguages() {
        getLanguagesApi(0);
    }

    public void getLanguagesApi(int languageId) {
        SharedPreferences sharedPreferences = Util.getSharedPreferences(context);
        try {
            JSONObject obj = new JSONObject();
            obj.put("languageId", languageId);
            new AsyncGetLanguages(obj, this, sharedPreferences.getString(GlobalParameters.URL, EndPoints.prod_url) + EndPoints.GetLanguages, context).execute();
        } catch (Exception e) {
            Log.d(TAG, "getLanguagesApi" + e.getMessage());
        }
    }

    @Override
    public void onJSONObjectListenerGetLanguages(JSONObject report, String status, JSONObject req) {
        try {
            if (report == null) {
                Logger.error(TAG, "onJSONObjectListenerGetLanguages", "GetLanguages Log api failed");
                return;
            }
            Gson gson = new Gson();
            LanguageListResponse response = gson.fromJson(String.valueOf(report), LanguageListResponse.class);
            if (response.responseCode != null && response.responseCode.equals("1")) {
                List<LanguageData> languageList = response.languageList;
                if (languageList.size() > 0) {
                    DatabaseController.getInstance().deleteLanguagesFromDb();
                    for (int i = 0; i < languageList.size(); i++) {
                        DatabaseController.getInstance().insertLanguagesToDB(languageList.get(i));
                    }
                }
                Log.d(TAG, "Get Languages list updated");
            } else {
                //getQuestionsFromDb();
            }
        } catch (Exception e) {
            Log.d(TAG, "onJSONObjectListenerGetLanguages" + e.getMessage());
        }
    }

    public HashMap<String, String> getLanguageMapFromDb() {
        HashMap<String, String> languageMap = new HashMap<>();
        List<LanguageData> list = DatabaseController.getInstance().getLanguagesFromDb();
        if (list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                 LanguageData languageData = list.get(i);
                 languageMap.put(languageData.name, languageData.languageCode);
            }
        }
        return languageMap;
    }
}
