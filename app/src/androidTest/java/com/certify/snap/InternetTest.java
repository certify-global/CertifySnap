package com.certify.snap;

import android.content.Context;
import android.net.Uri;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.Util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class InternetTest {
    Context context;
    @Before
    public  void beforeTest(){
        context = InstrumentationRegistry.getTargetContext();

    }
    @Test
    public void checkInternetConnectionTest(){

        String url = "https://api.certify.me";
        Assert.assertFalse("should not connect to apix.certify.me", checkInternetWithHost(url));
        Assert.assertTrue("should succeed with apidev.certify.me", checkInternetWithHost("https://apidev.certify.me"));
    }
    private void setHost(String host){
        context.getSharedPreferences("settings", Context.MODE_PRIVATE).edit().putString(GlobalParameters.URL, host);
    }
    private boolean checkInternetWithHost(String host) {
        setHost(host);

        return Util.isConnectingToInternet(context);
    }
}
