package com.certify.snap.common;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.arcsoft.imageutil.ArcSoftImageFormat;
import com.arcsoft.imageutil.ArcSoftImageUtil;
import com.arcsoft.imageutil.ArcSoftImageUtilError;
import com.certify.snap.R;
import com.certify.snap.activity.ManagementActivity;
import com.certify.snap.faceserver.FaceServer;
import com.certify.snap.model.RegisteredMembers;

import org.json.JSONArray;
import org.json.JSONObject;
import org.litepal.LitePal;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class MemberUtilData {
    public static String LOG ="";
    public static void MemberData(JSONArray memberList,Context context){
        try{
            for (int i = 0; i < memberList.length(); i++) {
                JSONObject c = memberList.getJSONObject(i);

                String certifyId = c.getString("id");
                String memberId = c.getString("memberId");
                if (memberId.isEmpty()) {
                    memberId = certifyId;
                }
                String accessId = c.getString("accessId");
                String firstName = c.getString("firstName");
                String lastName = c.getString("lastName");
                String email = c.getString("email");
                String phoneNumber = c.getString("phoneNumber");
                String faceTemplate = c.getString("faceTemplate");
                Boolean statusVal = c.getBoolean("status");
                String accountId = c.getString("accountId");
                String memberType = c.getString("memberType");

                String imagePath = MemberUtilData.getMemberImagePath(faceTemplate);


                if (statusVal) {
                    // Thread.sleep(200);
                    MemberUtilData.deleteDatabaseCertifyId(firstName, certifyId);
                    localRegister(firstName, lastName, phoneNumber, memberId, email, accessId, certifyId, imagePath, "sync",context);

                }
            }
        }catch (Exception e){
            Log.e(LOG,e.getMessage());
        }
    }
    public static String getMemberImagePath(String encodedImage) {
        String imagePath = "";
        Bitmap bitmap = Util.decodeToBase64(encodedImage);
        if (bitmap != null) {
            try {
                imagePath = Util.saveBitmapFile(bitmap, "register.jpg");
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
    public static boolean processImg(String name, String imgpath, String id,Context context) {
        if (imgpath.isEmpty()) return false;
        Bitmap bitmap = BitmapFactory.decodeFile(imgpath);
        bitmap = ArcSoftImageUtil.getAlignedBitmap(bitmap, true);
        if (bitmap == null) {
            Log.e("tag", "fail to translate bitmap");
           // showResult(getString(R.string.toast_translateBitmapfail));
            return false;
        }
        byte[] bgr24 = ArcSoftImageUtil.createImageData(bitmap.getWidth(), bitmap.getHeight(), ArcSoftImageFormat.BGR24);
        int transformCode = ArcSoftImageUtil.bitmapToImageData(bitmap, bgr24, ArcSoftImageFormat.BGR24);
        if (transformCode != ArcSoftImageUtilError.CODE_SUCCESS) {
        }
        boolean success = FaceServer.getInstance().registerBgr24( context, bgr24, bitmap.getWidth(),
                bitmap.getHeight(), name, id);
        return success;
    }
    public static void localRegister(String firstname, String lastname, String mobile, String id, String email, String accessid, String uniqueid, String imgpath, String sync,Context context) {
        String data = "";
        File imageFile = new File(imgpath);
        if (processImg(firstname + "-" + id, imgpath, id,context) || !imageFile.exists()) {
           registerDatabase(firstname, lastname, mobile, id, email, accessid, uniqueid, context);
                //if (!sync.equals("sync"))
                  //  showResult(getString(R.string.Register_success));
             //   handler.obtainMessage(REGISTER).sendToTarget();
               // refresh();
//                File file = new File(registerpath);
//                if (file.exists()) {
//                    file.delete();
//                    registerpath = "";
//                }
           // } else {
//                Log.e("tag", "Register failed");
//                if (!sync.equals("sync"))
//                    showResult(getString(R.string.register_failed));
           // }
        } else {
            Log.e("tag", "fail to process bitmap");
//            if (!sync.equals("sync"))
//                showResult(getString(R.string.register_face_not_recognized));
        }
    }
    public static boolean registerDatabase(String firstname, String lastname, String mobile, String id, String email, String accessid, String uniqueid, Context context) {
        try {
            String username = firstname + "-" + id;
            String ROOT_PATH_STRING = context.getFilesDir().getAbsolutePath();
            String image = ROOT_PATH_STRING + File.separator + FaceServer.SAVE_IMG_DIR + File.separator + username + FaceServer.IMG_SUFFIX;
            String feature = ROOT_PATH_STRING + File.separator + FaceServer.SAVE_FEATURE_DIR + File.separator + username;
            Log.e("tag", "image_uri---" + image + "  feature_uri---" + feature);

            RegisteredMembers registeredMembers = new RegisteredMembers();
            registeredMembers.setFirstname(firstname);
            registeredMembers.setLastname(lastname);
            registeredMembers.setMobile(mobile);
            registeredMembers.setStatus("1");
            registeredMembers.setMemberid(id);
            registeredMembers.setEmail(email);
            registeredMembers.setAccessid(accessid);
            registeredMembers.setUniqueid(uniqueid);
//          registeredMembers.setExpire_time(time);
            registeredMembers.setImage(image);
            registeredMembers.setFeatures(feature);
            boolean result = registeredMembers.save();
            return result;
        }catch (Exception e){
            Logger.debug("boolean registerDatabase(String firstname, String lastname, String mobile, String id, String email, String accessid, String uniqueid) {",e.getMessage());
        }
        return false;
    }
    public static String getImagePath(String name, String encodedImage) {
        String imagePath = "";
        Bitmap bitmap = Util.decodeToBase64(encodedImage);
        if (bitmap != null) {
            try {
                imagePath = Util.saveBitmapFile(bitmap, "register.jpg");
                imagePath = Util.saveAllImages(bitmap, name + ".jpg");
            } catch (IOException e) {
                Log.e(LOG, "Error in saving the bitmap in File");
            }
        }
        return imagePath;
    }
}
