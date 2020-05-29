package com.certify.snap;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.Util;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class InternetTest {
    @Test
    public void checkInternetConnectionTest(){
        Context context = InstrumentationRegistry.getTargetContext();
        Assert.assertFalse("should not connect to apix.certify.me", checkInternetWithHost(context, "apix.certify.me"));
        Assert.assertTrue("should succeed with apidev.certify.me", checkInternetWithHost(context, "apidev.certify.me"));
    }

    private boolean checkInternetWithHost(Context context, String host) {
        context.getSharedPreferences("settings", Context.MODE_PRIVATE).edit().putString(GlobalParameters.URL, host);

        return Util.isConnectingToInternet(context);
    }
}
