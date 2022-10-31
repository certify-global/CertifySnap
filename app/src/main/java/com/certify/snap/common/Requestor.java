package com.certify.snap.common;

import android.content.Context;
import android.content.SharedPreferences;

import com.microsoft.appcenter.analytics.Analytics;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class Requestor {

    static final int TIME_OUT = 60000;
    static final int TIME_OUT_IMAGE = 600000;
    ;

    public static String requestJson(String urlStr, JSONObject reqPing, String SerialNo, Context context, String device_sn) {
        String responseStr = null;
        String[] endPoint = urlStr.split(".me/");
        SharedPreferences sp = Util.getSharedPreferences(context);
        try {
            if (EndPoints.deployment == EndPoints.Mode.Demo) {
                Logger.debug("urlStr", urlStr);
                Logger.debug("urlSreq", reqPing.toString());
            }
            URL url = new URL(urlStr);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-type", "application/json");
            if (device_sn.equals("device_sn"))
                conn.setRequestProperty("device_sn", SerialNo);
            else {
                conn.setRequestProperty("DeviceSN", SerialNo);//A040980P02800140
            }
            conn.setRequestProperty("Authorization", "bearer " + sp.getString(GlobalParameters.ACCESS_TOKEN, ""));
            conn.setDoInput(true);
            conn.setDoOutput(true);

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(reqPing.toString());
            writer.flush();
            writer.close();
            os.close();
            int responseCode = conn.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String response;
                StringBuilder stringBuilder = new StringBuilder();
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((response = br.readLine()) != null) {
                    stringBuilder.append(response);
                }
                responseStr = stringBuilder.toString();
            } else if (responseCode == HttpsURLConnection.HTTP_UNAUTHORIZED) {
                JSONObject objMessage = new JSONObject();
                objMessage.put("Message", "token expired");
                responseStr = objMessage.toString();

                Map<String, String> properties = new HashMap<>();
                for (Iterator<String> iter = reqPing.keys(); iter.hasNext(); ) {
                    String key = iter.next();
                    String value = reqPing.optString(key);
                    properties.put(key, value);
                }
                //properties.put("URL:", urlStr);
                //properties.put("Response:", responseStr);
                Analytics.trackEvent(endPoint[1], properties);
            } else {
                String response;
                StringBuilder stringBuilder = new StringBuilder();
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((response = br.readLine()) != null) {
                    stringBuilder.append(response);
                }
                responseStr = stringBuilder.toString();
            }
            if (EndPoints.deployment == EndPoints.Mode.Demo)
                Logger.debug("responseStr", responseStr);
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

    public static String postJson(String urlStr, JSONObject reqPing, Context context) {
        String responseStr = null;
        String[] endPoint = urlStr.split(".me/");
        SharedPreferences sp = Util.getSharedPreferences(context);
        try {
            if (AppSettings.isDebugModeEnabled()) {
                Logger.debug("urlStr", urlStr);
                Logger.debug("reqPing", reqPing.toString());
            }
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-type", "application/json");
            conn.setRequestProperty("Authorization", "bearer " + sp.getString(GlobalParameters.ACCESS_TOKEN, ""));
            conn.setDoInput(true);
            conn.setDoOutput(true);

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(reqPing.toString());
            writer.flush();
            writer.close();
            os.close();
            int responseCode = conn.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String response;
                StringBuilder stringBuilder = new StringBuilder();
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((response = br.readLine()) != null) {
                    stringBuilder.append(response);
                }
                responseStr = stringBuilder.toString();
            } else if (responseCode == HttpsURLConnection.HTTP_UNAUTHORIZED) {
                JSONObject objMessage = new JSONObject();
                objMessage.put("Message", "token expired");
                responseStr = objMessage.toString();

                Map<String, String> properties = new HashMap<>();
                for (Iterator<String> iter = reqPing.keys(); iter.hasNext(); ) {
                    String key = iter.next();
                    String value = reqPing.optString(key);
                    properties.put(key, value);
                }
                //properties.put("URL:", urlStr);
                //properties.put("Response:", responseStr);
                Analytics.trackEvent(endPoint[1], properties);
            } else {
                String response;
                StringBuilder stringBuilder = new StringBuilder();
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((response = br.readLine()) != null) {
                    stringBuilder.append(response);
                }
                responseStr = stringBuilder.toString();
            }
            if (AppSettings.isDebugModeEnabled())
                Logger.debug("responseStr", responseStr);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> properties = new HashMap<>();
            for (Iterator<String> iter = reqPing.keys(); iter.hasNext(); ) {
                String key = iter.next();
                String value = reqPing.optString(key);
                properties.put(key, value);
            }
            properties.put("Device Serial No:", Util.getSNCode(context));
            properties.put("URL:", urlStr);
            properties.put("Response:", responseStr);
            Analytics.trackEvent(endPoint[1], properties);
        }
        return responseStr;
    }

    public static String postJsonLogin(String urlStr, String reqPing, String header) {
        String responseStr = null;
        String[] endPoint = urlStr.split(".me/");
        try {
            if (EndPoints.deployment == EndPoints.Mode.Demo)
                Logger.debug("urlStr", urlStr);

            URL url = new URL(urlStr);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(reqPing.toString());
            writer.flush();
            writer.close();
            os.close();
            int responseCode = conn.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String response;
                StringBuilder stringBuilder = new StringBuilder();
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((response = br.readLine()) != null) {
                    stringBuilder.append(response);
                }
                responseStr = stringBuilder.toString();
            } else if (responseCode == HttpsURLConnection.HTTP_UNAUTHORIZED) {
                JSONObject objMessage = new JSONObject();
                objMessage.put("Message", "token expired");
                responseStr = objMessage.toString();
            } else {
                String response;
                StringBuilder stringBuilder = new StringBuilder();
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((response = br.readLine()) != null) {
                    stringBuilder.append(response);
                }
                responseStr = stringBuilder.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Logger.error("postJsonLogin", e.getMessage());
        }
        return responseStr;
    }

    public static String postJsonAdmin(String urlStr, JSONObject reqPing, Context context) {
        String responseStr = null;
        String[] endPoint = urlStr.split(".me/");
        SharedPreferences sp = Util.getSharedPreferences(context);
        try {
            if (EndPoints.deployment == EndPoints.Mode.Demo)
                Logger.debug("urlStr", urlStr);

            URL url = new URL(urlStr);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-type", "application/json");
            conn.setRequestProperty("Authorization", "bearer " + sp.getString(GlobalParameters.Temp_ACCESS_TOKEN, ""));
            conn.setDoInput(true);
            conn.setDoOutput(true);

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(reqPing.toString());
            writer.flush();
            writer.close();
            os.close();
            int responseCode = conn.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String response;
                StringBuilder stringBuilder = new StringBuilder();
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((response = br.readLine()) != null) {
                    stringBuilder.append(response);
                }
                responseStr = stringBuilder.toString();
            } else if (responseCode == HttpsURLConnection.HTTP_UNAUTHORIZED) {
                JSONObject objMessage = new JSONObject();
                objMessage.put("Message", "token expired");
                responseStr = objMessage.toString();

                Map<String, String> properties = new HashMap<>();
                for (Iterator<String> iter = reqPing.keys(); iter.hasNext(); ) {
                    String key = iter.next();
                    String value = reqPing.optString(key);
                    properties.put(key, value);
                }
                //properties.put("URL:", urlStr);
                //properties.put("Response:", responseStr);
                Analytics.trackEvent(endPoint[1], properties);
            } else {
                String response;
                StringBuilder stringBuilder = new StringBuilder();
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((response = br.readLine()) != null) {
                    stringBuilder.append(response);
                }
                responseStr = stringBuilder.toString();
            }

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> properties = new HashMap<>();
            for (Iterator<String> iter = reqPing.keys(); iter.hasNext(); ) {
                String key = iter.next();
                String value = reqPing.optString(key);
                properties.put(key, value);
            }
            properties.put("Device Serial No:", Util.getSNCode(context));
            properties.put("URL:", urlStr);
            properties.put("Response:", responseStr);
            Analytics.trackEvent(endPoint[1], properties);
        }
        return responseStr;
    }

    public static String postJsonLog(String urlStr, JSONObject reqPing, Context context) {
        String responseStr = null;
        String[] endPoint = urlStr.split(".me/");
        SharedPreferences sp = Util.getSharedPreferences(context);
        try {
            if (EndPoints.deployment == EndPoints.Mode.Demo)
                Logger.debug("urlStr", urlStr);


            URL url = new URL(urlStr);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-type", "application/json");
            conn.setRequestProperty("Authorization", "bearer " + sp.getString(GlobalParameters.ACCESS_TOKEN, ""));
            conn.setDoInput(true);
            conn.setDoOutput(true);

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(reqPing.toString());
            writer.flush();
            writer.close();
            os.close();
            int responseCode = conn.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String response;
                StringBuilder stringBuilder = new StringBuilder();
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((response = br.readLine()) != null) {
                    stringBuilder.append(response);
                }
                responseStr = stringBuilder.toString();
            } else if (responseCode == HttpsURLConnection.HTTP_UNAUTHORIZED) {
                JSONObject objMessage = new JSONObject();
                objMessage.put("Message", "token expired");
                responseStr = objMessage.toString();
            } else {
                String response;
                StringBuilder stringBuilder = new StringBuilder();
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((response = br.readLine()) != null) {
                    stringBuilder.append(response);
                }
                responseStr = stringBuilder.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> properties = new HashMap<>();
            for (Iterator<String> iter = reqPing.keys(); iter.hasNext(); ) {
                String key = iter.next();
                String value = reqPing.optString(key);
                properties.put(key, value);
            }
            properties.put("Device Serial No:", Util.getSNCode(context));
            properties.put("URL:", urlStr);
            properties.put("Response:", responseStr);
            Analytics.trackEvent(endPoint[1], properties);
        }
        return responseStr;
    }

}