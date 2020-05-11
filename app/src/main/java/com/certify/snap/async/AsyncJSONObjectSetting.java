package com.certify.snap.async;


import android.content.Context;
import android.os.AsyncTask;

import com.certify.callback.JSONObjectCallback;
import com.certify.callback.SettingCallback;
import com.certify.snap.common.Util;

import org.json.JSONObject;

public class AsyncJSONObjectSetting extends AsyncTask<Void, Void, JSONObject> {
    private SettingCallback myComponent;
    private JSONObject req;
    private String url;
    Context mcontext;


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    public AsyncJSONObjectSetting(JSONObject req, SettingCallback myComponent, String url, Context context) {
        this.req = req;
        this.myComponent = myComponent;
        this.url = url;
        this.mcontext=context;
    }

    @Override
    protected JSONObject doInBackground(Void... params) {
        return Util.getJSONObjectSetting(req, url,"",mcontext);
    }

    @Override
    protected void onPostExecute(JSONObject reportInfo) {
        if (myComponent == null) return;
        myComponent.onJSONObjectListenerSetting(reportInfo, url,req);
    }
}