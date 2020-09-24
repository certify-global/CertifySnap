/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package com.certify.snap.localserver;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;

import com.certify.snap.activity.ConnectivityStatusActivity;
import com.certify.snap.common.Constants;
import com.certify.snap.common.Logger;
import com.certify.snap.common.Util;
import com.certify.snap.model.AccessLogOfflineRecord;
import com.certify.snap.model.OfflineRecordTemperatureMembers;
import com.certify.snap.model.RegisteredMembers;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ConnectionClosedException;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.EndpointDetails;
import org.apache.hc.core5.http.ExceptionListener;
import org.apache.hc.core5.http.HttpConnection;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.MethodNotSupportedException;
import org.apache.hc.core5.http.Method;
import org.apache.hc.core5.http.impl.bootstrap.HttpServer;
import org.apache.hc.core5.http.impl.bootstrap.ServerBootstrap;
import org.apache.hc.core5.http.io.HttpRequestHandler;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.http.protocol.HttpCoreContext;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.util.TimeValue;
import org.json.JSONException;
import org.json.JSONObject;

import static android.content.Context.WIFI_SERVICE;

/**
 * Example of embedded HTTP/1.1 file server using classic I/O.
 */
public class LocalServer implements LocalServerController.LocalServerCallbackListener{
    private HttpServer server;
    private static String TAG = LocalServer.class.getSimpleName();;
    private Context mContext;
    public HttpContext httpContext;
    public ClassicHttpResponse httpResponse;
    DhcpInfo dhcpInfo;
    WifiManager wifi;

    public LocalServer(Context context){
        mContext = context;
        LocalServerController.getInstance().setListener(this);
    }

    public void init() throws Exception {

        final SocketConfig socketConfig = SocketConfig.custom()
                .setSoTimeout(80, TimeUnit.SECONDS)
                .setTcpNoDelay(true)
                .build();
//TODO make port configurable, listen on assigned interface ip address and loopback address
        server = ServerBootstrap.bootstrap()
                .setListenerPort(Constants.port)
                .setLocalAddress(InetAddress.getByName(getIpAddress(mContext)))
                .setCanonicalHostName(getIpAddress(mContext))
                .setSocketConfig(socketConfig)
                //.setSslContext(sslContext)
                .setExceptionListener(new ExceptionListener() {

                    @Override
                    public void onError(final Exception ex) {
                        ex.printStackTrace();
                        Logger.debug(TAG, ex.getMessage());

                    }

                    @Override
                    public void onError(final HttpConnection conn, final Exception ex) {
                        if (ex instanceof SocketTimeoutException) {
                            Logger.debug(TAG, ex.getMessage());
                        } else if (ex instanceof ConnectionClosedException) {
                            Logger.debug(TAG, ex.getMessage());
                        } else {
                            ex.printStackTrace();
                        }
                    }

                })
                .register("*", new HttpFileHandler())
                .create();
        startServer();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                server.close(CloseMode.GRACEFUL);

            }
        });
        Logger.debug(TAG, "Listening on port " + Constants.port);

        server.awaitTermination(TimeValue.MAX_VALUE);

    }

    public void startServer() {
        try {
            server.start();
            Logger.verbose(TAG, "Start the server " , Constants.port);
        } catch (Exception e) {
            Logger.error(TAG, e.getMessage());
        }

    }

    public void stopServer() {
        try {
            server.stop();
            Logger.verbose(TAG, "Stop the server " , Constants.port);
        } catch (Exception e) {
            Logger.error(TAG, e.getMessage());
        }

    }

    public class HttpFileHandler implements HttpRequestHandler {

        public HttpFileHandler() {
            super();
        }

        @Override
        public void handle(
                final ClassicHttpRequest request,
                final ClassicHttpResponse response,
                final HttpContext context) throws HttpException, IOException {

            final String method = request.getMethod();
            httpContext = context;
            httpResponse = response;
            try {
                String pingValue = request.getUri().getPath();

                if (!Method.GET.isSame(method) && !Method.HEAD.isSame(method) && !Method.POST.isSame(method)) {
                    throw new MethodNotSupportedException(method + " method not supported");
                } else {
                    String responseData = "";
                    if (Method.GET.isSame(method)){
                        responseData = getResponseData(pingValue);
                    } else if (Method.POST.isSame(method)){
                        InputStream inputStream = request.getEntity().getContent();
                        if (inputStream != null) {
                            String requestBody = streamToString(inputStream);
                            try {
                                JSONObject jsonBody = new JSONObject(requestBody);
                                responseData = postResponseData(pingValue, jsonBody);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    sendResponseData(httpContext, responseData);
                }
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }

    }

    private static final String streamToString(InputStream inputStream) {
        Scanner s = (new Scanner(inputStream)).useDelimiter("\\A");
        String str;
        if (s.hasNext()) {
            str = s.next();
        } else {
            str = "";
        }

        return str;
    }

    private String getResponseData(String pingValue) {
        StringBuilder stringBuilderData = new StringBuilder();
        stringBuilderData.append("[\n");
        if (pingValue.equalsIgnoreCase("/Ping")) {
            stringBuilderData.append(LocalServerController.getInstance().getDeviceHealthCheck(mContext));
        } else if (pingValue.equalsIgnoreCase("/GetTemperatureLogs")) {
            LocalServerController.getInstance().findLastTenOfflineTempRecord();
            if (LocalServerController.getInstance().getOfflineTempDataList().size() > 0) {
                for (OfflineRecordTemperatureMembers list : LocalServerController.getInstance().getOfflineTempDataList()) {
                    stringBuilderData.append(list.toString());
                }
            }
        } else if (pingValue.equalsIgnoreCase("/GetAccessLogs")) {
            LocalServerController.getInstance().findLastTenOfflineAccessLogRecord();
            if (LocalServerController.getInstance().getAccessLogDataList().size() > 0) {
                for (AccessLogOfflineRecord list : LocalServerController.getInstance().getAccessLogDataList()) {
                    stringBuilderData.append(list.toString());
                }
            }
        } else if (pingValue.equalsIgnoreCase("/GetMembers")) {
            LocalServerController.getInstance().findAllMembers();
            if (LocalServerController.getInstance().getMemberDataList().size() > 0) {
                for (RegisteredMembers list : LocalServerController.getInstance().getMemberDataList()) {
                    stringBuilderData.append(LocalServerController.getInstance().convertJsonMemberData(list));
                }
            }
        }
        stringBuilderData.append("\n]");
        return stringBuilderData.toString();
    }

    private String postResponseData(String pingValue, JSONObject member) {
        if (pingValue.equalsIgnoreCase("/AddUpdateMember")) {
            String updateMember = LocalServerController.getInstance().findUpdateMember(member);
            return updateMember;
        } else if (pingValue.equalsIgnoreCase("/DeleteMember")) {
            String deleteMember = LocalServerController.getInstance().deleteMember(member);
            return deleteMember;
        }

        return "";
    }

    @Override
    public void onGetMemberRequest(List<RegisteredMembers> list) {
        StringBuilder stringBuilderData = new StringBuilder();
        stringBuilderData.append("[\n");
        if (LocalServerController.getInstance().getMemberDataList().size() > 0) {
            for (RegisteredMembers registeredMember : LocalServerController.getInstance().getMemberDataList()) {
                stringBuilderData.append(LocalServerController.getInstance().convertJsonMemberData(registeredMember));
            }
        }
        stringBuilderData.append("\n]");
        sendResponseData(httpContext,stringBuilderData.toString());
    }

    public void sendResponseData(HttpContext context, String responseData){
        final HttpCoreContext coreContext = HttpCoreContext.adapt(context);
        final EndpointDetails endpoint = coreContext.getEndpointDetails();
        httpResponse.setCode(HttpStatus.SC_OK);
        StringEntity stringEntity = new StringEntity(responseData, ContentType.APPLICATION_JSON);
        httpResponse.setEntity(stringEntity);
                    //Logger.debug(TAG, response.toString());
    }

    public void processGetRequest(String requestName){
        if (requestName.equalsIgnoreCase("/GetMembers")){
            LocalServerController.getInstance().findAllMembers();
        }
    }

    @SuppressWarnings("deprecation")
    public String getIpAddress(Context context){
        String ipAddress = "";
        if (Util.isConnectedWifi(context)){
            wifi= (WifiManager) context.getApplicationContext().getSystemService(WIFI_SERVICE);
            dhcpInfo=wifi.getDhcpInfo();
            ipAddress = String.valueOf(Formatter.formatIpAddress(dhcpInfo.ipAddress));
        } else if (Util.isConnectedEthernet(context) || Util.isConnectedMobile(context)){
            ipAddress = ConnectivityStatusActivity.getIPAddress(true);
        } else {
            ipAddress = "127.0.0.1";
        }
        return ipAddress;
    }

}