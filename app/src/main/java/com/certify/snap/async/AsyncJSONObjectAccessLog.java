package com.certify.snap.async;


import android.content.Context;
import android.os.AsyncTask;

import com.certify.callback.AccessCallback;
import com.certify.callback.PushCallback;
import com.certify.snap.common.Util;

import org.json.JSONObject;

public class AsyncJSONObjectAccessLog extends AsyncTask<Void, Void, JSONObject> {
    private AccessCallback myComponent;
    private JSONObject req;
    private String url;
    Context mcontext;


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    public AsyncJSONObjectAccessLog(JSONObject req, AccessCallback myComponent, String url, Context context) {
        this.req = req;
        this.myComponent = myComponent;
        this.url = url;
        this.mcontext=context;
    }

    @Override
    protected JSONObject doInBackground(Void... params) {
        return Util.getJSONObjectAccessLog(req, url,"",mcontext);
    }

    @Override
    protected void onPostExecute(JSONObject reportInfo) {
        if (myComponent == null) return;
        myComponent.onJSONObjectListenerAccess(reportInfo, url,req);
    }
}