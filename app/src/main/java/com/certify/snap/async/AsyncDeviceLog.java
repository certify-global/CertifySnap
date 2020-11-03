package com.certify.snap.async;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.certify.callback.MemberIDCallback;
import com.certify.snap.common.Util;

import org.json.JSONObject;

public class AsyncDeviceLog extends AsyncTask<Void, Void, JSONObject> {
    private JSONObject req;
    private String url;
    Context mcontext;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    public AsyncDeviceLog(JSONObject req, MemberIDCallback myComponent, String url, Context context) {
        this.req = req;
        this.url = url;
        this.mcontext=context;
    }

    @Override
    protected JSONObject doInBackground(Void... params) {
        return Util.getJSONObjectDeviceLog(req, url,"", mcontext);
    }

    @Override
    protected void onPostExecute(final JSONObject reportInfo) {
        Log.d("AsyncDeviceLog", " reportInfo");
    }
}
