package com.certify.snap.common;

import android.graphics.Bitmap;
import android.util.Log;

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

    public static boolean deleteDatabaseCertifyId(String name, String certifyId) {
        List<RegisteredMembers> list = LitePal.where("uniqueid = ?", certifyId).find(RegisteredMembers.class);
        if (list != null && list.size() > 0) {
            FaceServer.getInstance().deleteInfo(name + "-" + certifyId);
            String featurePath = list.get(0).getFeatures();
            String imgPath = list.get(0).getImage();
            int line = LitePal.deleteAll(RegisteredMembers.class, "uniqueid = ?", certifyId);
            Log.e("tag", "line---" + line);
            File featureFile = new File(featurePath);
            File imgFile = new File(imgPath);
            if (featureFile.exists() && featureFile.isFile()) {
                boolean featureDeleteResult = featureFile.delete();
                if (featureDeleteResult) {
                    FaceServer.getInstance().deleteInfo(featureFile.getName());
                    Log.e("feature delete", "feature delete success---" + featurePath);
                }
            }
            if (imgFile.exists() && imgFile.isFile()) {
                boolean imgDeleteResult = imgFile.delete();
                if (imgDeleteResult) {
                    Log.e("image delete ", "image delete success---" + featurePath);
                }
            }
            return line > 0;
        }
        return false;
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
