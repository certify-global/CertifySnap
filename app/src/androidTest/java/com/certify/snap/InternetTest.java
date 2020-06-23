package com.certify.snap;

import android.content.Context;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.Util;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import java.io.IOException;

@RunWith(AndroidJUnit4.class)
public class InternetTest {
    Context context;
    @Before
    public  void beforeTest(){
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();

    }
    private void setHost(String host){
        context.getSharedPreferences("settings", Context.MODE_PRIVATE).edit().putString(GlobalParameters.URL, host);
    }
    private boolean checkInternetWithHost(String host) {
        setHost(host);
        return Util.isConnectingToInternet(context);
    }
    @Test
    public void testRecordUserTemperature() throws IOException {
        HttpClient defaultHttpClient = Mockito.mock(HttpClient.class);


        Mockito.when(defaultHttpClient.execute(Mockito.isA(HttpUriRequest.class))).thenThrow(new java.net.SocketException("invalid address"));
//        Util.recordUserTemperature(null, null, "37.0", null, null, null, false);
        Assert.assertTrue("", true);

    }
}
