package com.certify.snap.localserver;

import android.os.AsyncTask;

public class LocalServerTask extends AsyncTask<String, Void, String> {

    LocalServer mLocalServer;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    public LocalServerTask(LocalServer localServer) {
        this.mLocalServer = localServer;
    }
    @Override
    protected String doInBackground(String[] params) {

        try {
            mLocalServer.init();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(String message) {
    }
}
