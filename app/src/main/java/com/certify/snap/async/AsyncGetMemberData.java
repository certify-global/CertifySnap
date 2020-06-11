package com.certify.snap.async;

import android.content.Context;
import android.os.AsyncTask;

import com.certify.callback.MemberIDCallback;
import com.certify.snap.common.Util;

import org.json.JSONObject;

public class AsyncGetMemberData extends AsyncTask<Void, Void, JSONObject> {
    private MemberIDCallback myComponent;
    private JSONObject req;
    private String url;
    Context mcontext;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    public AsyncGetMemberData(JSONObject req, MemberIDCallback myComponent, String url, Context context) {
        this.req = req;
        this.myComponent = myComponent;
        this.url = url;
        this.mcontext=context;
    }

    @Override
    protected JSONObject doInBackground(Void... params) {
        return Util.getJSONObjectMemberData(req, url,"",mcontext,"");
    }

    @Override
    protected void onPostExecute(final JSONObject reportInfo) {
        if (myComponent == null) return;
        myComponent.onJSONObjectListenerMemberID(reportInfo, url,req);
    }
}
