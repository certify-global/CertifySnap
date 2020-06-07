package com.certify.snap;

import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.test.platform.app.InstrumentationRegistry;

import com.certify.snap.common.ManageMemberHelper;
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
    @Mock
    Context context;
    @Mock
    ThermalImageUtil thermalImageUtil;
    @Mock
    Intent temperatureServiceIntent;

    @Test
    public void testRecordUserTemperature() throws IOException {
        HttpClient defaultHttpClient = Mockito.mock(HttpClient.class);

        when(defaultHttpClient.execute(Mockito.isA(HttpUriRequest.class))).thenThrow(new java.net.SocketException("invalid address"));
//        Util.recordUserTemperature(null, context, "37.0", null, null, null, false);
        //Assert.assertTrue();

    }

    @Test
    public void testGetMemberList() throws IOException {
        String accessToken = "Ka_cQJO5h-fRgnueW1WU-0qibUAyeyMPIR8AgporfWl4jX2CArhUKMrSgMAGB5g_jJdhrxesUglpPK2iRg4QP1SqHuHixccHrZvA_pI4AvHaAfOl_DO4DCNIpC4toCvfvHXO2rbto9TYxtZRmlo5SlsG8rJ6YotBMmvRooKj19ID9bf4rlO35AFFZrJV4U50OB4isN4fhD8Fxbg0aOpdet1y31b9GkiDXaloRmX2OqXQIXPP1AnOxHRUV15-w4cw9jvt8w";
        ManageMemberHelper.GetMemberListResponse memberList = new ManageMemberHelper(accessToken, "A040980P02800254").getMemberList();
//        System.out.println(memberList);
        Assert.assertTrue(memberList.responseCode == 1);

    }
    @Test
    public void testGetMemberById() throws IOException {
        String accessToken = "Ka_cQJO5h-fRgnueW1WU-0qibUAyeyMPIR8AgporfWl4jX2CArhUKMrSgMAGB5g_jJdhrxesUglpPK2iRg4QP1SqHuHixccHrZvA_pI4AvHaAfOl_DO4DCNIpC4toCvfvHXO2rbto9TYxtZRmlo5SlsG8rJ6YotBMmvRooKj19ID9bf4rlO35AFFZrJV4U50OB4isN4fhD8Fxbg0aOpdet1y31b9GkiDXaloRmX2OqXQIXPP1AnOxHRUV15-w4cw9jvt8w";
        ManageMemberHelper.GetMemberByIdResponse memberResp = new ManageMemberHelper(accessToken, "A040980P02800254").GetMemberById(11972635805197L);
        Assert.assertTrue(memberResp.responseCode == 1);
        Assert.assertTrue(memberResp.responseData.size() == 1);
    }
    @Test
    public  void testLoadMembers() throws IOException{
        String accessToken = "Ka_cQJO5h-fRgnueW1WU-0qibUAyeyMPIR8AgporfWl4jX2CArhUKMrSgMAGB5g_jJdhrxesUglpPK2iRg4QP1SqHuHixccHrZvA_pI4AvHaAfOl_DO4DCNIpC4toCvfvHXO2rbto9TYxtZRmlo5SlsG8rJ6YotBMmvRooKj19ID9bf4rlO35AFFZrJV4U50OB4isN4fhD8Fxbg0aOpdet1y31b9GkiDXaloRmX2OqXQIXPP1AnOxHRUV15-w4cw9jvt8w";
        String deviceSerialNumber = "A040980P02800254";
        ManageMemberHelper.loadMembers(accessToken, deviceSerialNumber, "/sdcard");
    }
}
