package com.certify.snap.async;

import android.content.Context;
import android.os.AsyncTask;

import com.certify.callback.ManageMemberCallback;
import com.certify.snap.common.Util;

import org.json.JSONObject;

public class AsyncJSONObjectManageMember extends AsyncTask<Void, Void, String> {
    private ManageMemberCallback myComponent;
    private JSONObject req;
    private String url;
    Context mcontext;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    public AsyncJSONObjectManageMember(JSONObject req, ManageMemberCallback myComponent, String url, Context context) {
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
        myComponent.onJSONObjectListenerManageMember(reportInfo, url,req);
    }
}
