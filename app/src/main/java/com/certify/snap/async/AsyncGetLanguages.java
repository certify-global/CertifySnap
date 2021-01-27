package com.certify.snap.async;

import android.content.Context;
import android.os.AsyncTask;

import com.certify.callback.GetLanguagesCallback;
import com.certify.snap.common.Util;

import org.json.JSONObject;

public class AsyncGetLanguages extends AsyncTask<Void, Void, JSONObject> {
    private GetLanguagesCallback myComponent;
    private JSONObject req;
    private String url;
    private Context mContext;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    public AsyncGetLanguages(JSONObject req, GetLanguagesCallback myComponent, String url, Context context) {
        this.req = req;
        this.myComponent = myComponent;
        this.url = url;
        this.mContext = context;
    }

    @Override
    protected JSONObject doInBackground(Void... params) {
        return Util.getJSONObjectLanguages(req, url,"", mContext);
    }

    @Override
    protected void onPostExecute(JSONObject reportInfo) {
        if (myComponent == null) return;
        myComponent.onJSONObjectListenerGetLanguages(reportInfo, url,req);
    }
}
