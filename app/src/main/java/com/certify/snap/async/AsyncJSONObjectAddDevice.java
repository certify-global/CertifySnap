package com.certify.snap.async;


import android.content.Context;
import android.os.AsyncTask;

import com.certify.callback.AddDeviceCallback;
import com.certify.callback.QRCodeCallback;
import com.certify.snap.common.Util;

import org.json.JSONObject;

public class AsyncJSONObjectAddDevice extends AsyncTask<Void, Void, JSONObject> {
    private AddDeviceCallback myComponent;
    private JSONObject req;
    private String url;
    Context mcontext;


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    public AsyncJSONObjectAddDevice(JSONObject req, AddDeviceCallback myComponent, String url, Context context) {
        this.req = req;
        this.myComponent = myComponent;
        this.url = url;
        this.mcontext=context;
    }

    @Override
    protected JSONObject doInBackground(Void... params) {
        return Util.getJSONObjectAddDevice(req, url,"",mcontext);
    }

    @Override
    protected void onPostExecute(JSONObject reportInfo) {
        if (myComponent == null) return;
        myComponent.onJSONObjectListenerAddDevice(reportInfo, url,req);
    }
}