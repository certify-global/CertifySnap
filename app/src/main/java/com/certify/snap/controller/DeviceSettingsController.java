package com.certify.snap.controller;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.certify.callback.GetLanguagesCallback;
import com.certify.snap.api.response.LanguageData;
import com.certify.snap.api.response.LanguageListResponse;
import com.certify.snap.async.AsyncGetLanguages;
import com.certify.snap.common.Constants;
import com.certify.snap.common.EndPoints;
import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.Logger;
import com.certify.snap.common.Util;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class DeviceSettingsController implements GetLanguagesCallback {
    private static final String TAG = DeviceSettingsController.class.getSimpleName();
    private static DeviceSettingsController instance = null;
    private Context context;
    private GetLanguagesListener listener;

    public interface GetLanguagesListener {
        void onGetLanguages();
    }

    public static DeviceSettingsController getInstance() {
        if (instance == null) {
            instance = new DeviceSettingsController();
        }
        return instance;
    }

    public void init(Context context, GetLanguagesListener callbackListener) {
        this.context = context;
        this.listener = callbackListener;
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
                if (listener != null) {
                    listener.onGetLanguages();
                }
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
                        if (listener != null) {
                            listener.onGetLanguages();
                        }
                    }
                }
                Log.d(TAG, "Get Languages list updated");
                return;
            }
            if (listener != null) {
                listener.onGetLanguages();
            }
        } catch (Exception e) {
            Log.d(TAG, "onJSONObjectListenerGetLanguages" + e.getMessage());
        }
    }

    public HashMap<String, String> getLanguageMapFromDb() {
        LinkedHashMap<String, String> languageMap = new LinkedHashMap<>();
        List<LanguageData> list = DatabaseController.getInstance().getLanguagesFromDb();
        if (list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                 LanguageData languageData = list.get(i);
                 languageMap.put(languageData.name, languageData.languageCode);
            }
        }
        return languageMap;
    }

    public String getLanguageOnId(int languageId) {
        String language = "en";
        LanguageData languageData = DatabaseController.getInstance().getLanguageOnId(languageId);
        if (languageData != null) {
            language = languageData.languageCode;
        }
        return language;
    }

    public boolean isLanguagesInDBEmpty() {
        return DatabaseController.getInstance().getLanguagesFromDb().isEmpty();
    }

    public void addOfflineLanguages() {
        for (int i = 1; i <= Constants.LANGUAGES_MAX_COUNT; i++) {
            LanguageData languageData = getOfflineLanguageData(i);
            DatabaseController.getInstance().insertLanguagesToDB(languageData);
        }
    }

    private LanguageData getOfflineLanguageData(int value) {
        LanguageData languageData = new LanguageData();
        switch (value) {
            case 1: {
                languageData.languageId = value;
                languageData.name = "English";
                languageData.languageCode = "en";
                languageData.fileCode = "";
            }
            break;

            case 2: {
                languageData.languageId = value;
                languageData.name = "Spanish";
                languageData.languageCode = "es";
                languageData.fileCode = "es-rES";
            }
            break;

            case 3: {
                languageData.languageId = value;
                languageData.name = "German";
                languageData.languageCode = "de";
                languageData.fileCode = "de-rDE";
            }
            break;

            case 4: {
                languageData.languageId = value;
                languageData.name = "French";
                languageData.languageCode = "fr";
                languageData.fileCode = "fr-rFR";
            }
            break;

            case 5: {
                languageData.languageId = value;
                languageData.name = "Italian";
                languageData.languageCode = "it";
                languageData.fileCode = "it-rIT";
            }
            break;

            case 6: {
                languageData.languageId = value;
                languageData.name = "Hindi";
                languageData.languageCode = "hi";
                languageData.fileCode = "hi-rIN";
            }
            break;

            case 7: {
                languageData.languageId = value;
                languageData.name = "Tamil";
                languageData.languageCode = "ta";
                languageData.fileCode = "ta-rIN";
            }
            break;

            case 8: {
                languageData.languageId = value;
                languageData.name = "Telugu";
                languageData.languageCode = "te";
                languageData.fileCode = "te-rIN";
            }
            break;
        }
        return languageData;
    }

}
