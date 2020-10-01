package com.certify.snap.async;


import android.content.Context;
import android.os.AsyncTask;

import com.certify.callback.GestureAnswerCallback;
import com.certify.callback.GestureCallback;
import com.certify.snap.common.Util;

import org.json.JSONObject;

public class AsyncGestureAnswer extends AsyncTask<Void, Void, JSONObject> {
    private GestureAnswerCallback myComponent;
    private JSONObject req;
    private String url;
    Context mcontext;


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    public AsyncGestureAnswer(JSONObject req, GestureAnswerCallback myComponent, String url, Context context) {
        this.req = req;
        this.myComponent = myComponent;
        this.url = url;
        this.mcontext=context;
    }

    @Override
    protected JSONObject doInBackground(Void... params) {
        return Util.getJSONObjectGesture(req, url,"",mcontext);
    }

    @Override
    protected void onPostExecute(JSONObject reportInfo) {
        if (myComponent == null) return;
        myComponent.onJSONObjectListenerGestureAnswer(reportInfo, url,req);
    }
}