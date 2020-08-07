package com.certify.snap.async;


import android.content.Context;
import android.os.AsyncTask;

import com.certify.callback.PushCallback;
import com.certify.callback.SettingCallback;
import com.certify.snap.common.Util;

import org.json.JSONObject;

public class AsyncJSONObjectPush extends AsyncTask<Void, Void, JSONObject> {
    private PushCallback myComponent;
    private JSONObject req;
    private String url;
    Context mcontext;


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    public AsyncJSONObjectPush(JSONObject req, PushCallback myComponent, String url, Context context) {
        this.req = req;
        this.myComponent = myComponent;
        this.url = url;
        this.mcontext=context;
    }

    @Override
    protected JSONObject doInBackground(Void... params) {
        return Util.getJSONObjectPush(req, url,"",mcontext);
    }

    @Override
    protected void onPostExecute(JSONObject reportInfo) {
        if (myComponent == null) return;
        myComponent.onJSONObjectListenerPush(reportInfo, url,req);
    }
}