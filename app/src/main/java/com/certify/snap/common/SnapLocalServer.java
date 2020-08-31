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

package com.certify.snap.common;

import android.content.Context;
import com.certify.snap.localserver.LocalServerController;
import com.certify.snap.model.AccessLogOfflineRecord;
import com.certify.snap.model.OfflineRecordTemperatureMembers;
import com.certify.snap.model.RegisteredMembers;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.URISyntaxException;
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

/**
 * Example of embedded HTTP/1.1 file server using classic I/O.
 */
public class SnapLocalServer {
    private static HttpServer server;
    private static String TAG = SnapLocalServer.class.getSimpleName();;
    private static Context mContext;

    public static void main(final String[] args, Context context) throws Exception {
        int port = 8080;
        mContext = context;

        final SocketConfig socketConfig = SocketConfig.custom()
                .setSoTimeout(80, TimeUnit.SECONDS)
                .setTcpNoDelay(true)
                .build();
//TODO make port configurable, listen on assigned interface ip address and loopback address
        server = ServerBootstrap.bootstrap()
                .setListenerPort(port)
                .setLocalAddress(InetAddress.getByName("127.0.0.1"))
                .setCanonicalHostName("127.0.0.1")
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
        Logger.debug(TAG, "Listening on port " + port);

        server.awaitTermination(TimeValue.MAX_VALUE);

    }

    public static void startServer() {
        try {
            server.start();
        } catch (Exception e) {
            Logger.error(TAG, e.getMessage());
        }

    }

    public static void stopServer() {
        try {
            server.stop();
        } catch (Exception e) {
            Logger.error(TAG, e.getMessage());
        }

    }

    public static class HttpFileHandler implements HttpRequestHandler {

        public HttpFileHandler() {
            super();
        }

        @Override
        public void handle(
                final ClassicHttpRequest request,
                final ClassicHttpResponse response,
                final HttpContext context) throws HttpException, IOException {

            final String method = request.getMethod();
            try {
                String pingValue = request.getUri().getPath();

                if (!Method.GET.isSame(method) && !Method.HEAD.isSame(method) && !Method.POST.isSame(method)) {
                    throw new MethodNotSupportedException(method + " method not supported");
                } else {
                    if (Method.GET.isSame(method)){
                        String responseData = getResponseData(pingValue);
                        final HttpCoreContext coreContext = HttpCoreContext.adapt(context);
                        final EndpointDetails endpoint = coreContext.getEndpointDetails();
                        response.setCode(HttpStatus.SC_OK);
                        StringEntity stringEntity = new StringEntity(responseData, ContentType.APPLICATION_JSON);
                        response.setEntity(stringEntity);
                        Logger.debug(TAG, response.toString());
                    }
                }
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }

    }

    private static String getResponseData(String pingValue) {
        StringBuilder stringBuilderData = new StringBuilder();
        stringBuilderData.append("[\n");
        if (pingValue.equalsIgnoreCase("/Ping")) {
            stringBuilderData.append(LocalServerController.getInstance().getDeviceHealthCheck(mContext));
        } else if (pingValue.equalsIgnoreCase("/GetTemperatureLogs")) {
            if (LocalServerController.getInstance().getOfflineTempDataList().size() > 0) {
                for (OfflineRecordTemperatureMembers list : LocalServerController.getInstance().getOfflineTempDataList()) {
                    stringBuilderData.append(list.toString());
                }
            }
        } else if (pingValue.equalsIgnoreCase("/GetAccessLogs")) {
            if (LocalServerController.getInstance().getAccessLogDataList().size() > 0) {
                for (AccessLogOfflineRecord list : LocalServerController.getInstance().getAccessLogDataList()) {
                    stringBuilderData.append(list.toString());
                }
            }
        } else if (pingValue.equalsIgnoreCase("/GetMembers")) {
            if (LocalServerController.getInstance().getMemberDataList().size() > 0) {
                for (RegisteredMembers list : LocalServerController.getInstance().getMemberDataList()) {
                    stringBuilderData.append(LocalServerController.getInstance().convertJsonMemberData(list));
                }
            }
        }
        stringBuilderData.append("\n]");
        return stringBuilderData.toString();
    }

}