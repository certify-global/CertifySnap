package com.certify.snap.localserver;


import com.certify.snap.model.OfflineRecordTemperatureMembers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LocalServer {

    private static final String TAG = LocalServer.class.getSimpleName();
    private static LocalServer mInstance = null;
    public int port = 8080;
    private HttpServer mHttpServer = null;

    public static LocalServer getInstance() {
        if (mInstance == null) {
            mInstance = new LocalServer();
        }
        return mInstance;
    }

    public void startServer(){
        try {
            InetSocketAddress inetSocketAddress = new InetSocketAddress(port);
            this.mHttpServer = HttpServer.create(inetSocketAddress, 0);
            ExecutorService es = Executors.newCachedThreadPool();
            mHttpServer.setExecutor(es);
            mHttpServer.createContext("/", rootHandler);
            mHttpServer.createContext("/index", messageHandler);
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
                StringBuilder stringBuilderData = new StringBuilder();
                stringBuilderData.append("[\n");
                if (LocalServerController.getInstance().getDataList().size() > 0){
                    for (OfflineRecordTemperatureMembers list : LocalServerController.getInstance().getDataList()){
                        stringBuilderData.append(LocalServerController.getInstance().convertJsonData(list));
                    }
                    stringBuilderData.append("]");
                }
                if (request != null) {
                    if (request.equals("GET")) {
//                        sendResponse(exchange, "Welcome to my server Shailendra");
                        sendResponse(exchange, stringBuilderData.toString());
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


    private final HttpHandler messageHandler = (HttpHandler)(new HttpHandler() {
        public final void handle(HttpExchange httpExchange) {
            String request = httpExchange.getRequestMethod();
            if (request != null) {
                switch(request.hashCode()) {
                    case 70454:
                        if (request.equals("GET")) {
                            sendResponse(httpExchange, "Would be all messages stringified json");
                        }
                        break;
                    case 2461856:
                        if (request.equals("POST")) {
                            InputStream inputStream = httpExchange.getRequestBody();
                            if (inputStream != null){
                                String requestBody = streamToString(inputStream);
                                try {
                                    JSONObject jsonBody = new JSONObject(requestBody);
                                    String jsonString = jsonBody.toString();
                                    if (jsonString != null)
                                        sendResponse(httpExchange, jsonString);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }

                        }
                }
            }

        }
    });

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
}
