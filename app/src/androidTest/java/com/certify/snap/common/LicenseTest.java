package com.certify.snap.common;

import android.content.Context;
import android.os.Environment;

import androidx.test.platform.app.InstrumentationRegistry;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

public class LicenseTest {
    private static final String TAG = "LicenseTest";
    Context context;
    File externalStorageLicense;
    File applicationLicense;
    File testFolder;
    private static String arcFaceLicenseFileName = "ArcFacePro32.dat";
    private static final String activeResultLicenseFileName = "active_result.dat";

    @Before
    public void beforeTest() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        testFolder = new File(Environment.getExternalStorageDirectory(), "test");
        externalStorageLicense = new File(Environment.getExternalStorageDirectory(), arcFaceLicenseFileName);
        applicationLicense = new File(context.getFilesDir(), arcFaceLicenseFileName);
    }

    @Test
    public void testActivateWithArcFacePro32OnExternalStore() {
        if (applicationLicense.exists()) applicationLicense.delete();
        if (!externalStorageLicense.exists()) {
            File licenseOnTestFolder = new File(testFolder, arcFaceLicenseFileName);
            if (!licenseOnTestFolder.exists()) {
                Assert.fail("copy license to /sdcard/test folder to run this test");
            }
            Assert.assertFalse(License.checkLicense(context));
            License.copyFiles(licenseOnTestFolder, externalStorageLicense);
            boolean activated = License.activateWithArcFaceFile(context);
            Assert.assertTrue(activated);
        }
    }

    @Test
    public void testActivateWithActiveResultOnExternalStorage() {
        if (applicationLicense.exists()) applicationLicense.delete();
        if (externalStorageLicense.exists()) externalStorageLicense.delete();
        File testActiveResultFile = new File(testFolder, activeResultLicenseFileName);

        if (!testActiveResultFile.exists()) Assert.fail("copy active_result.dat to /sdcard/test");
        License.copyFiles(testActiveResultFile, new File(Environment.getExternalStorageDirectory(),
                activeResultLicenseFileName));
        boolean activated = License.activateWithActiveResultFile(context);
        Assert.assertTrue(activated);
    }

    @Test
    public void testActivateWithActiveResultFromServer() {
        if (applicationLicense.exists()) applicationLicense.delete();
        if (externalStorageLicense.exists()) externalStorageLicense.delete();
        EndPoints.prod_url = "http://192.168.0.101:8080";//TODO: use env var

        boolean activated = License.activateWithActiveResultFile(context);
        Assert.assertTrue(activated);
    }
}
