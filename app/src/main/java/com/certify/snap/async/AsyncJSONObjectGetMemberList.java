package com.certify.snap.async;

import android.content.Context;
import android.os.AsyncTask;

import com.certify.callback.ManageMemberCallback;
import com.certify.callback.MemberListCallback;
import com.certify.snap.common.Util;

import org.json.JSONObject;

public class AsyncJSONObjectGetMemberList extends AsyncTask<Void, Void, JSONObject> {
    private MemberListCallback myComponent;
    private JSONObject req;
    private String url;
    Context mcontext;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    public AsyncJSONObjectGetMemberList(JSONObject req, MemberListCallback myComponent, String url, Context context) {
        this.req = req;
        this.myComponent = myComponent;
        this.url = url;
        this.mcontext=context;
    }

    @Override
    protected JSONObject doInBackground(Void... params) {
        return Util.getJSONObjectMemberList(req, url,"",mcontext,"");
    }

    @Override
    protected void onPostExecute(JSONObject reportInfo) {
        if (myComponent == null) return;
        myComponent.onJSONObjectListenerMemberList(reportInfo, url,req);
    }
}
