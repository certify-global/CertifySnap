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

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;

import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ConnectionClosedException;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.EndpointDetails;
import org.apache.hc.core5.http.ExceptionListener;
import org.apache.hc.core5.http.HttpConnection;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.MethodNotSupportedException;
import org.apache.hc.core5.http.Method;
import org.apache.hc.core5.http.impl.bootstrap.HttpServer;
import org.apache.hc.core5.http.impl.bootstrap.ServerBootstrap;
import org.apache.hc.core5.http.io.HttpRequestHandler;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.FileEntity;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.http.protocol.HttpCoreContext;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.util.TimeValue;

/**
 * Example of embedded HTTP/1.1 file server using classic I/O.
 */
public class ClassicFileServerExample {

    public static void main(final String[] args) throws Exception {
       /* if (args.length < 1) {
           Logger.debug("ClassicFileServerExample","Please specify document root directory");
            System.exit(1);
        }*/
        // Document root directory
       // final String docRoot = args[0];
        int port = 8080;

/*
        SSLContext sslContext = null;
        if (port == 8443) {
            // Initialize SSL context
            final URL url = ClassicFileServerExample.class.getResource("/my.keystore");
            if (url == null) {
              Logger.debug("ClassicFileServerExample","Keystore not found");
                System.exit(1);
            }
            sslContext = SSLContexts.custom()
                    .loadKeyMaterial(url, "secret".toCharArray(), "secret".toCharArray())
                    .build();
        }*/
        final SocketConfig socketConfig = SocketConfig.custom()
                .setSoTimeout(80, TimeUnit.SECONDS)
                .setTcpNoDelay(true)
                .build();

        final HttpServer server = ServerBootstrap.bootstrap()
                .setListenerPort(port)
                .setLocalAddress(InetAddress.getLoopbackAddress())
                .setSocketConfig(socketConfig)
                //.setSslContext(sslContext)
                .setExceptionListener(new ExceptionListener() {

                    @Override
                    public void onError(final Exception ex) {
                        ex.printStackTrace();
                        Logger.debug("ClassiconError",ex.getMessage());

                    }

                    @Override
                    public void onError(final HttpConnection conn, final Exception ex) {
                        if (ex instanceof SocketTimeoutException) {
                            Logger.debug("ClassicSocketTimeoutException",ex.getMessage());
                        } else if (ex instanceof ConnectionClosedException) {
                           Logger.debug("ClassicConnectionClosedException",ex.getMessage());
                        } else {
                            ex.printStackTrace();
                        }
                    }

                })
              //  .register("*", new HttpFileHandler(docRoot))
                .create();

        server.start();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                server.close(CloseMode.GRACEFUL);
                Logger.debug("ClassicFileServerExample","server close");

            }
        });
       Logger.debug("ClassicFileServerExample","Listening on port " + port);

        server.awaitTermination(TimeValue.MAX_VALUE);

    }

    public static class HttpFileHandler implements HttpRequestHandler  {

        private final String docRoot;

        public HttpFileHandler(final String docRoot) {
            super();
            this.docRoot = docRoot;
        }

        @Override
        public void handle(
                final ClassicHttpRequest request,
                final ClassicHttpResponse response,
                final HttpContext context) throws HttpException, IOException {

            final String method = request.getMethod();
            if (!Method.GET.isSame(method) && !Method.HEAD.isSame(method) && !Method.POST.isSame(method)) {
                throw new MethodNotSupportedException(method + " method not supported");


            } else {
                final HttpCoreContext coreContext = HttpCoreContext.adapt(context);
                final EndpointDetails endpoint = coreContext.getEndpointDetails();
                response.setCode(HttpStatus.SC_OK);
                StringEntity stringEntity=new StringEntity("hello",ContentType.DEFAULT_TEXT);
                response.setEntity(stringEntity);
               Logger.debug("ClassicFileServerExampleresponse",response.toString());
            }
        }

    }

}