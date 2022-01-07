package com.certify.snap.api;

import android.content.Context;
import android.util.Log;

import com.certify.snap.BuildConfig;
import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.Util;
import com.certify.snap.controller.ApplicationController;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitInstance {
    private static final String TAG = RetrofitInstance.class.getSimpleName();
    private static RetrofitInstance instance = null;
    private ApiInterface apiInterface = null;
    private Context context;

    public void init (Context context) {
        this.context = context;
        createRetrofitInstance();
    }

    public static RetrofitInstance getInstance() {
        if (instance == null) {
            instance = new RetrofitInstance();
        }
        return instance;
    }

    public void createRetrofitInstance() {
        OkHttpClient okHttpClient = createOkHttpClient();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Util.getSharedPreferences(context).getString(GlobalParameters.URL, BuildConfig.ENDPOINT_URL))
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(okHttpClient)
                .build();
        apiInterface = retrofit.create(ApiInterface.class);
    }

    public ApiInterface getApiInterface() {
        return apiInterface;
    }

    private OkHttpClient createOkHttpClient() {
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(chain -> {
            Request original = chain.request();
            Request request = original.newBuilder()
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + Util.getSharedPreferences(context).getString(GlobalParameters.ACCESS_TOKEN, ""))
                    .method(original.method(), original.body())
                    .build();
            return chain.proceed(request);
        });
        return httpClient.build();
    }
}
