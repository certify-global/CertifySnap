package com.certify.snap.async;

import android.content.Context;
import android.os.AsyncTask;

import com.certify.callback.GetLastCheckinTimeCallback;
import com.certify.snap.common.Util;

import org.json.JSONObject;

public class AsyncGetLastCheckinTime extends AsyncTask<Void, Void, JSONObject> {

    private GetLastCheckinTimeCallback myComponent;
    private JSONObject req;
    private String url;
    private Context mContext;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    public AsyncGetLastCheckinTime(JSONObject req, GetLastCheckinTimeCallback myComponent, String url, Context context) {
        this.req = req;
        this.myComponent = myComponent;
        this.url = url;
        this.mContext = context;
    }

    @Override
    protected JSONObject doInBackground(Void... params) {
        return Util.getJSONObjectLastCheckInTime(req, url,"", mContext);
    }

    @Override
    protected void onPostExecute(JSONObject reportInfo) {
        if (myComponent == null) return;
        myComponent.onJSONObjectListenerGetCheckInTime(reportInfo, url,req);
    }

}
