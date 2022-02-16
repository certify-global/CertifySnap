package com.certify.snap.api;

import com.certify.snap.common.AppSettings;
import com.certify.snap.common.Logger;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitInstance {
    private static final String TAG = RetrofitInstance.class.getSimpleName();
    private static RetrofitInstance instance = null;
    private ApiInterface apiInterface = null;


    public void init () {
        createRetrofitInstance();
    }

    public static RetrofitInstance getInstance() {

        if (instance == null) {
            instance = new RetrofitInstance();
            instance.createRetrofitInstance();
        }
        return instance;
    }

    public void createRetrofitInstance() {
        OkHttpClient okHttpClient = createOkHttpClient();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(AppSettings.getEndpointUrl())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(okHttpClient)
                .build();
        apiInterface = retrofit.create(ApiInterface.class);
    }

    public ApiInterface getApiInterface() {
        if(apiInterface == null)
            Logger.error(TAG, "apiInterface is null");
        return apiInterface;
    }

    private OkHttpClient createOkHttpClient() {
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(chain -> {
            Request original = chain.request();
            Request request = original.newBuilder()
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + AppSettings.getAccessToken())
                    .method(original.method(), original.body())
                    .build();
            return chain.proceed(request);
        });
        return httpClient.build();
    }
}
