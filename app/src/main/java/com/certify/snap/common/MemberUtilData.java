package com.certify.snap.common;

import android.graphics.Bitmap;
import android.util.Log;

import com.certify.snap.controller.DatabaseController;
import com.certify.snap.faceserver.FaceServer;
import com.certify.snap.model.RegisteredMembers;

import org.litepal.LitePal;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class MemberUtilData {
    private static String LOG = MemberUtilData.class.getSimpleName();

    public static String getMemberImagePath(String encodedImage, String fileName) {
        String imagePath = "";
        Bitmap bitmap = Util.decodeToBase64(encodedImage);
        if (bitmap != null) {
            try {
                imagePath = Util.saveBitmapFile(bitmap, fileName);
            } catch (IOException e) {
                Log.e(LOG, "Error in saving the bitmap in File");
            }
        }
        return imagePath;
    }

    public static String getImagePath(String encodedImage) {
        String imagePath = "";
        Bitmap bitmap = Util.decodeToBase64(encodedImage);
        if (bitmap != null) {
            try {
                imagePath = Util.saveBitmapFile(bitmap, "register.jpg");
                //imagePath = Util.saveAllImages(bitmap, name + ".jpg");
            } catch (IOException e) {
                Log.e(LOG, "Error in saving the bitmap in File");
            }
        }
        return imagePath;
    }
}
