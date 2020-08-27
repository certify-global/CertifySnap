package com.certify.snap.localserver;


import android.content.Context;

import com.certify.snap.model.AccessLogOfflineRecord;
import com.certify.snap.model.OfflineRecordTemperatureMembers;
import com.certify.snap.model.RegisteredMembers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LocalServer {

    private static final String TAG = LocalServer.class.getSimpleName();
    private static LocalServer mInstance = null;
    public int port = 8080;
    private HttpServer mHttpServer = null;
    private Context mContext;

    public static LocalServer getInstance() {
        if (mInstance == null) {
            mInstance = new LocalServer();
        }
        return mInstance;
    }

    public void startServer(Context context){
        try {
            this.mContext = context;
            InetSocketAddress inetSocketAddress = new InetSocketAddress(port);
            this.mHttpServer = HttpServer.create(inetSocketAddress, 0);
            ExecutorService es = Executors.newCachedThreadPool();
            mHttpServer.setExecutor(es);
            mHttpServer.createContext("/", rootHandler);
            mHttpServer.start();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void stopServer() {
        if (mHttpServer != null) {
            mHttpServer.stop(0);
        }

    }

    private HttpHandler rootHandler = new HttpHandler() {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            {

                String request = exchange.getRequestMethod();
                URI pingValue = exchange.getRequestURI();
                if (request != null) {
                    if (request.equals("GET")) {
                        String responseData = getResponseData(pingValue);
                        sendResponse(exchange, responseData);
                    } else if (request.equals("POST")) {
                        InputStream inputStream = exchange.getRequestBody();
                        if (inputStream != null){
                            String requestBody = streamToString(inputStream);
                            try {
                                JSONObject jsonBody = new JSONObject(requestBody);
                                String postData = postResponseData(pingValue, jsonBody);
                                if (postData != null && !postData.isEmpty())
                                    sendResponse(exchange, postData);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

            }
        }
    };


    private void sendResponse(HttpExchange httpExchange, String responseText){
        try {
            httpExchange.sendResponseHeaders(200, (long)responseText.length());
            OutputStream os = httpExchange.getResponseBody();
            os.write(responseText.getBytes());
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private final String streamToString(InputStream inputStream) {
        Scanner s = (new Scanner(inputStream)).useDelimiter("\\A");
        String var10000;
        if (s.hasNext()) {
            var10000 = s.next();
        } else {
            var10000 = "";
        }

        return var10000;
    }

    private String getResponseData(URI pingValue) {
        StringBuilder stringBuilderData = new StringBuilder();
        stringBuilderData.append("[\n");
        if (pingValue.getPath().equalsIgnoreCase("/Ping")){
            stringBuilderData.append(LocalServerController.getInstance().getDeviceHealthCheck(mContext));
        } else if (pingValue.getPath().equalsIgnoreCase("/GetTemperatureLogs")){
            if (LocalServerController.getInstance().getOfflineTempDataList().size() > 0){
                for (OfflineRecordTemperatureMembers list : LocalServerController.getInstance().getOfflineTempDataList()){
                    stringBuilderData.append(LocalServerController.getInstance().convertJsonData(list.getJsonObj()));
                }
            }
        } else if (pingValue.getPath().equalsIgnoreCase("/GetAccessLogs")){
            if (LocalServerController.getInstance().getAccessLogDataList().size() > 0){
                for (AccessLogOfflineRecord list : LocalServerController.getInstance().getAccessLogDataList()) {
                    stringBuilderData.append(LocalServerController.getInstance().convertJsonData(list.getJsonObj()));
                }
            }
        } else if (pingValue.getPath().equalsIgnoreCase("/GetMembers")) {
            if (LocalServerController.getInstance().getMemberDataList().size() > 0){
                for (RegisteredMembers list : LocalServerController.getInstance().getMemberDataList()) {
                    stringBuilderData.append(LocalServerController.getInstance().convertJsonMemberData(list));
                }
            }
        }
        stringBuilderData.append("]");
       return stringBuilderData.toString();
    }

    private String postResponseData(URI pingValue, JSONObject member) {
        if (pingValue.getPath().equalsIgnoreCase("/AddUpdateMember")){
            String updateMember = LocalServerController.getInstance().findUpdateMember(member);
            return updateMember;
        }

        return "";
    }
}
