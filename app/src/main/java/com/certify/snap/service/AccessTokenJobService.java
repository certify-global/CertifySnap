package com.certify.snap.service;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.SharedPreferences;

import com.certify.callback.JSONObjectCallback;
import com.certify.snap.common.Constants;
import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.Logger;
import com.certify.snap.common.Util;

import org.json.JSONObject;

public class AccessTokenJobService extends JobService implements JSONObjectCallback {


    @Override
    public boolean onStartJob(JobParameters params) {
        Util.getToken(this, this);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

    @Override
    public void onJSONObjectListener(String reportInfo, String status, JSONObject req) {
        try {
            if (reportInfo.equals(Constants.TIME_OUT_RESPONSE)) {
                Util.switchRgbOrIrActivity(this, true);
                return;
            }
            JSONObject json1 = null;
            SharedPreferences sharedPreferences = Util.getSharedPreferences(this);
            try {
                String formatedString = reportInfo.substring(1, reportInfo.length() - 1);
                json1 = new JSONObject(formatedString.replace("\\", ""));

            } catch (Exception e) {
                json1 = new JSONObject(reportInfo.replace("\\", ""));
            }

            if (json1.isNull("access_token")) return;
            String access_token = json1.getString("access_token");
            String token_type = json1.getString("token_type");
            String institutionId = json1.getString("InstitutionID");
            String expire_time = json1.getString(".expires");
            String command = json1.isNull("command") ? "" : json1.getString("command");
            if (command.equals("REACTIVATE")) {
                // Util.activateApplication();
            }

            Util.writeString(sharedPreferences, GlobalParameters.ACCESS_TOKEN, access_token);
            Util.writeString(sharedPreferences, GlobalParameters.EXPIRE_TIME, expire_time);
            Util.writeString(sharedPreferences, GlobalParameters.TOKEN_TYPE, token_type);
            Util.writeString(sharedPreferences, GlobalParameters.INSTITUTION_ID, institutionId);
            Util.writeString(sharedPreferences, GlobalParameters.Generate_Token_Command, command);

        } catch (Exception e) {
            Util.switchRgbOrIrActivity(this, true);
            Logger.error("getTokenActivate(String reportInfo,String status,Context context)", e.getMessage());
        }
    }
}
