package com.certify.snap.async;


import android.content.Context;
import android.os.AsyncTask;

import com.certify.callback.JSONObjectCallback;
import com.certify.callback.JSONObjectCallbackLogin;
import com.certify.snap.common.Util;

import org.json.JSONObject;

public class AsyncJSONObjectLogin extends AsyncTask<Void, Void, JSONObject> {
    private JSONObjectCallbackLogin myComponent;
    private String req;
    private String url;
    Context mcontext;


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    public AsyncJSONObjectLogin(String req, JSONObjectCallbackLogin myComponent, String url, Context context) {
        this.req = req;
        this.myComponent = myComponent;
        this.url = url;
        this.mcontext=context;
    }

    @Override
    protected JSONObject doInBackground(Void... params) {
        return Util.getJSONObjectLogin(req, url,"",mcontext);
    }

    @Override
    protected void onPostExecute(JSONObject reportInfo) {
        if (myComponent == null) return;
        myComponent.onJSONObjectListenerLogin(reportInfo, url,req);
    }
}