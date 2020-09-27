package com.certify.snap.async;


import android.content.Context;
import android.os.AsyncTask;

import com.certify.callback.FlowListCallback;
import com.certify.callback.GestureCallback;
import com.certify.snap.common.Util;

import org.json.JSONObject;

public class AsyncJSONObjectFlowList extends AsyncTask<Void, Void, JSONObject> {
    private FlowListCallback myComponent;
    private JSONObject req;
    private String url;
    Context mcontext;


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    public AsyncJSONObjectFlowList(JSONObject req, FlowListCallback myComponent, String url, Context context) {
        this.req = req;
        this.myComponent = myComponent;
        this.url = url;
        this.mcontext=context;
    }

    @Override
    protected JSONObject doInBackground(Void... params) {
        return Util.getJSONObjectFlowList(req, url,"",mcontext);
    }

    @Override
    protected void onPostExecute(JSONObject reportInfo) {
        if (myComponent == null) return;
        myComponent.onJSONObjectListenerFlowList(reportInfo, url,req);
    }
}