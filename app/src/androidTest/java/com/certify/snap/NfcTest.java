package com.certify.snap;

import android.content.Context;
import android.nfc.NfcAdapter;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Assert;
import org.junit.Test;

public class NfcTest {
    @Test
    public void testNfcEnabled() {
        Context context = ApplicationProvider.getApplicationContext();
        NfcAdapter adapter = NfcAdapter.getDefaultAdapter(context);
        System.out.println(adapter.isEnabled());
        Assert.assertTrue(adapter.isEnabled());
    }
}
