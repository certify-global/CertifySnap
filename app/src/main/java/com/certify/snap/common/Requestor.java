package com.certify.snap.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;


import com.microsoft.appcenter.analytics.Analytics;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Requestor {

    static final int TIME_OUT = 60000;
    static final int TIME_OUT_IMAGE = 600000;

    ;

    public static String requestJson(String urlStr, JSONObject reqPing, String SerialNo, Context context) {
        String responseStr = null;
        String[] endPoint = urlStr.split(".me/");
        SharedPreferences  sp = Util.getSharedPreferences(context);
        try {
            if (EndPoints.deployment == EndPoints.Mode.Demo)
                Logger.debug("urlStr", urlStr);
            HttpPost httpost = new HttpPost(urlStr);
            httpost.addHeader("Content-type", "application/json");
            httpost.setHeader("DeviceSN", SerialNo);
           // if(!sp.getString(GlobalParameters.ACCESS_TOKEN,"").equals(""))
            httpost.setHeader("Authorization","bearer "+ sp.getString(GlobalParameters.ACCESS_TOKEN,""));
            DefaultHttpClient httpclient1 = (DefaultHttpClient) WebClientDevWrapper
                    .getNewHttpClient();
            httpost.setEntity(new StringEntity(reqPing.toString(), "UTF-8"));
            HttpResponse responseHttp = httpclient1.execute(httpost);
            Log.d("responseHttp",""+ responseHttp.getStatusLine().getStatusCode());

            responseStr = EntityUtils
                    .toString(responseHttp.getEntity());
            if (EndPoints.deployment == EndPoints.Mode.Demo)
                Logger.debug("responseStr ", responseStr);

        } catch (ClientProtocolException e) {
            e.printStackTrace();
            Map<String, String> properties = new HashMap<>();
            for (Iterator<String> iter = reqPing.keys(); iter.hasNext(); ) {
                String key = iter.next();
                String value = reqPing.optString(key);
                properties.put(key, value);
            }
            properties.put("Device Serial No:", Util.getSNCode());
            properties.put("URL:", urlStr);
            properties.put("Response:", responseStr);
            Analytics.trackEvent(endPoint[1], properties);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> properties = new HashMap<>();
            for (Iterator<String> iter = reqPing.keys(); iter.hasNext(); ) {
                String key = iter.next();
                String value = reqPing.optString(key);
                properties.put(key, value);
            }
            properties.put("URL:", urlStr);
            properties.put("Response:", responseStr);
            Analytics.trackEvent(endPoint[1], properties);
        }
        return responseStr;
    }

    public static String getRequest(String urlStr, String token, Context context, String terminal) {
        String[] endPoint = urlStr.split(".com/");

        try {

            if (EndPoints.deployment == EndPoints.Mode.Demo)
                Logger.debug("urlStr", urlStr);

            BufferedReader in;
            String data;
            HttpClient client = new DefaultHttpClient();
            URI website = new URI(urlStr);
            HttpGet request = new HttpGet();
            request.setURI(website);
            // request.setHeader("Authorization","Bearer "+token);

            request.setHeader("Content-Type", "application/x-www-form-urlencoded");
            HttpResponse response = client.execute(request);
            response.getStatusLine().getStatusCode();
            in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            StringBuffer sb = new StringBuffer("");
            String l = "";
            String nl = System.getProperty("line.separator");
            while ((l = in.readLine()) != null) {
                sb.append(l + nl);
            }
            in.close();
            data = sb.toString();
            if (EndPoints.deployment == EndPoints.Mode.Demo)
                Logger.debug("data", data);
            return data;
        } catch (Exception e) {
            Logger.error("getRequest(String urlStr)", e.getMessage());
            Map<String, String> properties = new HashMap<>();
            properties.put("URL:", urlStr);
            Analytics.trackEvent(endPoint[1], properties);
        }
        return "";
    }
    public static String postJson(String urlStr, JSONObject reqPing, String SerialNo, Context context) {
        String responseStr = null;
        String[] endPoint = urlStr.split(".me/");
        SharedPreferences  sp = Util.getSharedPreferences(context);
        try {
            if (EndPoints.deployment == EndPoints.Mode.Demo)
                Logger.debug("urlStr", urlStr);
            HttpPost httpost = new HttpPost(urlStr);
            httpost.addHeader("Content-type", "application/json");
            httpost.setHeader("Authorization","bearer "+ sp.getString(GlobalParameters.ACCESS_TOKEN,""));
            DefaultHttpClient httpclient1 = (DefaultHttpClient) WebClientDevWrapper
                    .getNewHttpClient();
            httpost.setEntity(new StringEntity(reqPing.toString(), "UTF-8"));
            HttpResponse responseHttp = httpclient1.execute(httpost);
            responseStr = EntityUtils
                    .toString(responseHttp.getEntity());


        } catch (ClientProtocolException e) {
            e.printStackTrace();
            Map<String, String> properties = new HashMap<>();
            for (Iterator<String> iter = reqPing.keys(); iter.hasNext(); ) {
                String key = iter.next();
                String value = reqPing.optString(key);
                properties.put(key, value);
            }
            properties.put("URL:", urlStr);
            properties.put("Response:", responseStr);
            Analytics.trackEvent(endPoint[1], properties);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> properties = new HashMap<>();
            for (Iterator<String> iter = reqPing.keys(); iter.hasNext(); ) {
                String key = iter.next();
                String value = reqPing.optString(key);
                properties.put(key, value);
            }
            properties.put("Device Serial No:", Util.getSNCode());
            properties.put("URL:", urlStr);
            properties.put("Response:", responseStr);
            Analytics.trackEvent(endPoint[1], properties);
        }
        return responseStr;
    }
}