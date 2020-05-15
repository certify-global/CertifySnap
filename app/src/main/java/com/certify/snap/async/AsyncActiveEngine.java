package com.certify.snap.async;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.certify.snap.common.ActiveEngine;

public class AsyncActiveEngine extends AsyncTask<Void, Void, String> {
       Context context;
       SharedPreferences sharedPreferences;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
    public AsyncActiveEngine(Context context, SharedPreferences sharedPreferences) {
        this.context = context;
        this.sharedPreferences = sharedPreferences;
    }
        @Override
        protected String doInBackground(Void... params) {
            ActiveEngine.activeEngine(context,sharedPreferences);
            return "";
        }

        @Override
        protected void onPostExecute(String reportInfo) {


        }
    }