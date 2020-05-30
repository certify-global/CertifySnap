package com.certify.snap;

import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.test.platform.app.InstrumentationRegistry;

import com.certify.snap.common.Util;
import com.common.thermalimage.ThermalImageUtil;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.pluginapi.Sdk;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@Config(sdk = Build.VERSION_CODES.N_MR1, manifest = Config.NONE)
public class ApiTest {
    @Mock Context context;
    @Mock
    ThermalImageUtil thermalImageUtil;
    @Mock
    Intent temperatureServiceIntent;

    @Test
    public void testRecordUserTemperature() throws IOException {
        HttpClient defaultHttpClient = Mockito.mock(HttpClient.class);

        when(defaultHttpClient.execute(Mockito.isA(HttpUriRequest.class))).thenThrow(new java.net.SocketException("invalid address"));
        Util.recordUserTemperature(null, context, "37.0", null, null, null, false);
        Assert.assertTrue();

    }
}
