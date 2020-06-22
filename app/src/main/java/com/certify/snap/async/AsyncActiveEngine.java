package com.certify.snap.async;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.certify.callback.ActiveEngineCallback;
import com.certify.callback.SettingCallback;
import com.certify.snap.activity.SettingActivity;
import com.certify.snap.common.ActiveEngine;
import com.certify.snap.common.Constants;
import com.certify.snap.common.Logger;
import com.certify.snap.common.Util;

import static com.certify.snap.common.ActiveEngine.activeEngineOffline;

public class AsyncActiveEngine extends AsyncTask<Void, Void, Boolean> {
    private static final String TAG = AsyncActiveEngine.class.getSimpleName();
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
        boolean activated = ActiveEngine.activeEngineOffline(context);

        if(!activated){
            String serialNumber = Util.getSNCode();
            String activationKey = ActiveEngine.getDeviceList().get(serialNumber);
            Logger.verbose(TAG,
                    "doInBackground activeEngineOffline failed, serialNumber: %s, activationKey: %s",
                            serialNumber + activationKey);
            int activationResult = FaceEngine.activeOnline(context, activationKey, Constants.APP_ID, Constants.SDK_KEY);
            activated = activationResult == ErrorInfo.MOK || activationResult == ErrorInfo.MERR_ASF_ALREADY_ACTIVATED;
            Logger.verbose(TAG, "doInBackground FaceEngine.activeOnline activationResult: %d", activationResult);
        }
        Util.writeBoolean(sharedPreferences, "activate", activated);
        return activated;
    }

    @Override
    protected void onPostExecute(Boolean activate) {
        if (activeEngineCallback != null) {
            activeEngineCallback.onActiveEngineCallback(activate, null, null);
        }

    }
}