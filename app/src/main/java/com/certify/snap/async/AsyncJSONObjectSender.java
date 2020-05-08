package com.certify.snap.async;


import android.content.Context;
import android.os.AsyncTask;

import com.certify.snap.common.Util;
import com.certify.callback.JSONObjectCallback;

import org.json.JSONObject;

public class AsyncJSONObjectSender extends AsyncTask<Void, Void, String> {
    private JSONObjectCallback myComponent;
    private JSONObject req;
    private String url;
    Context mcontext;


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    public AsyncJSONObjectSender(JSONObject req, JSONObjectCallback myComponent, String url,Context context) {
        this.req = req;
        this.myComponent = myComponent;
        this.url = url;
        this.mcontext=context;
    }

    @Override
    protected String doInBackground(Void... params) {
        return Util.getJSONObject(req, url,"",mcontext);
    }

    @Override
    protected void onPostExecute(String reportInfo) {
        if (myComponent == null) return;
        myComponent.onJSONObjectListener(reportInfo, url,req);
    }
}