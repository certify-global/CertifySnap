package com.certify.snap.async;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.certify.callback.ActiveEngineCallback;
import com.certify.callback.SettingCallback;
import com.certify.snap.common.ActiveEngine;
import com.certify.snap.common.Util;

import static com.certify.snap.common.ActiveEngine.activeEngineOffline;

public class AsyncActiveEngine extends AsyncTask<Void, Void, Boolean> {
    private Context context;
    private SharedPreferences sharedPreferences;
    private ActiveEngineCallback activeEngineCallback;
    private String deviceSno;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    public AsyncActiveEngine(Context context, SharedPreferences sharedPreferences, ActiveEngineCallback activeEngineCallback,String deviceSno) {
        this.context = context;
        this.sharedPreferences = sharedPreferences;
        this.activeEngineCallback = activeEngineCallback;
        this.deviceSno = deviceSno;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        //  ActiveEngine.activeEngine(context,sharedPreferences);
        boolean activate = ActiveEngine.activeEngineOffline(context);
        if (!activate)
            ActiveEngine.activeEngine(context, sharedPreferences, deviceSno, activeEngineCallback);
        Util.writeBoolean(sharedPreferences, "activate", activate);
        return activate;
    }

    @Override
    protected void onPostExecute(Boolean activate) {
        if (activeEngineCallback != null) {
            activeEngineCallback.onActiveEngineCallback(activate, "", null);
        }

    }
}