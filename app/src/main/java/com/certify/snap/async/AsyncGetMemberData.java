package com.certify.snap.async;

import android.content.Context;
import android.os.AsyncTask;

import com.certify.callback.MemberIDCallback;
import com.certify.callback.MemberListCallback;
import com.certify.snap.common.Util;

import org.json.JSONObject;

import static com.microsoft.appcenter.utils.HandlerUtils.runOnUiThread;

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
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return Util.getJSONObjectMemberData(req, url,"",mcontext,"");
    }

    @Override
    protected void onPostExecute(final JSONObject reportInfo) {
        if (myComponent == null) return;
        /*try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                myComponent.onJSONObjectListenerMemberID(reportInfo, url,req);
            }
        });
        /*new Thread(new Runnable() {
            @Override
            public void run() {
                myComponent.onJSONObjectListenerMemberID(reportInfo, url,req);
            }
        }).start();*/
    }
}
