package com.certify.snap.async;


import android.content.Context;
import android.os.AsyncTask;

import com.certify.callback.RecordTemperatureCallback;
import com.certify.snap.common.Util;

import org.json.JSONObject;

public class AsyncRecordUserTemperature extends AsyncTask<Void, Void, JSONObject> {
    private RecordTemperatureCallback myComponent;
    private JSONObject req;
    private String url;
    Context mcontext;


    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }

    public AsyncRecordUserTemperature(JSONObject req, RecordTemperatureCallback myComponent, String url, Context context) {
        this.req = req;
        this.myComponent = myComponent;
        this.url = url;
        this.mcontext=context;
    }

    @Override
    protected JSONObject doInBackground(Void... params) {
        return Util.getJSONObjectTemp(req, url,"",mcontext);
    }

    @Override
    protected void onPostExecute(JSONObject reportInfo) {
//        TODO:should be fire and forget?
        if (myComponent == null) return;
        myComponent.onJSONObjectListenerTemperature(reportInfo, url,req);
    }
}