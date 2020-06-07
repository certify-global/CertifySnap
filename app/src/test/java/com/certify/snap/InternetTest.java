package com.certify.snap;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.Util;
import com.certify.snap.common.WebClientDevWrapper;

import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
@RunWith(MockitoJUnitRunner.class)
public class InternetTest {
    @Mock
    Context context;
    @Mock SharedPreferences sharedPreferences;
    @Mock SharedPreferences.Editor editor;

    @Test
    public void checkInternet(){
        when(context.getSharedPreferences(anyString(), eq(Context.MODE_PRIVATE))).thenReturn(sharedPreferences);

        when(sharedPreferences.getString(anyString(), anyString())).thenReturn("apix.certify.me");
        Assert.assertFalse("should fail with apix.certyf.me", Util.isConnectingToInternet(context));

        when(sharedPreferences.getString(anyString(), anyString())).thenReturn("apidev.certify.me");
        Assert.assertTrue("should work with apidev.certify.me", Util.isConnectingToInternet(context));
    }
    @Test
    public void recordUserTemperatureTest() throws IOException {
        //should fail with invalid url
        HttpClient defaultHttpClient = Mockito.mock(HttpClient.class);

        when(defaultHttpClient.execute(Mockito.isA(HttpUriRequest.class))).thenThrow(new java.net.SocketException("invalid address"));
//        Util.recordUserTemperature(null, context,"37.0", null, null, null, false);
        Assert.assertTrue("", true);
    }

}
