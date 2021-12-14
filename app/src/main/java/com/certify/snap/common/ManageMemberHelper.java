package com.certify.snap.common;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;

import androidx.annotation.RequiresApi;
import android.util.Log;

import com.arcsoft.imageutil.ArcSoftImageFormat;
import com.arcsoft.imageutil.ArcSoftImageUtil;
import com.arcsoft.imageutil.ArcSoftImageUtilError;
import com.certify.snap.controller.DatabaseController;
import com.certify.snap.faceserver.FaceServer;
import com.certify.snap.model.RegisteredMembers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.HeaderMap;
import retrofit2.http.POST;

public class ManageMemberHelper {
    private SnapService snapService;
    private Map<String, String> headers;
    private static String ROOT_PATH_STRING = "";
    public ManageMemberHelper() {
    }

    public ManageMemberHelper(String accessToken, String serialNumber) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(EndPoints.prod_url)
                .addConverterFactory(JacksonConverterFactory.create())
                .build();
        snapService = retrofit.create(SnapService.class);
        headers = new HashMap<>();
        headers.put("Authorization", "bearer " + accessToken);
        headers.put("device_sn", serialNumber);

    }

    private static final String LOG = "ManageMemberHelper";

    public GetMemberListResponse getMemberList() throws IOException {
        try {
            Response<GetMemberListResponse> r = snapService.getMemberList(headers).execute();
            Log.d(LOG, r.toString());
            GetMemberListResponse memberList = r.body();
            return memberList;
        } catch (Exception e) {
//            Logger.error(LOG + "getToken(JSONObjectCallback callback, Context context) ", e.getMessage());
            throw e;

        }
//        return null;
    }

    public GetMemberByIdResponse GetMemberById(long memberId) throws IOException {
        Response<GetMemberByIdResponse> r = snapService.getMemberById(headers,
                new GetMemberByIdRequest(memberId)).execute();
        Log.d(LOG, r.toString());
        GetMemberByIdResponse member = r.body();
        return member;
    }

    //save
    private static String getImagePath(String encodedImage) {
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

    private static boolean processImg(String name, String imgpath, String id) {
        if (imgpath.isEmpty()) return false;
        Bitmap bitmap = BitmapFactory.decodeFile(imgpath);
        bitmap = ArcSoftImageUtil.getAlignedBitmap(bitmap, true);
        if (bitmap == null) {
            Log.e("tag", "fail to translate bitmap");
//            showResult(getString(R.string.toast_translateBitmapfail));
            return false;
        }
        byte[] bgr24 = ArcSoftImageUtil.createImageData(bitmap.getWidth(), bitmap.getHeight(), ArcSoftImageFormat.BGR24);
        int transformCode = ArcSoftImageUtil.bitmapToImageData(bitmap, bgr24, ArcSoftImageFormat.BGR24);
        if (transformCode != ArcSoftImageUtilError.CODE_SUCCESS) {
        }
        boolean success = FaceServer.getInstance().registerBgr24(Application.getInstance(), bgr24, bitmap.getWidth(),
                bitmap.getHeight(), name, id);
        return success;
    }
    private static boolean registerDatabase(MemberResponse member) {
        try {
            String username = member.firstName + "-" + member.id;
            String image = ROOT_PATH_STRING + File.separator + FaceServer.SAVE_IMG_DIR + File.separator + username + FaceServer.IMG_SUFFIX;
            String feature = ROOT_PATH_STRING + File.separator + FaceServer.SAVE_FEATURE_DIR + File.separator + username;
            Log.e("tag", "image_uri---" + image + "  feature_uri---" + feature);

            RegisteredMembers registeredMembers = new RegisteredMembers();
            registeredMembers.setFirstname(member.firstName);
            registeredMembers.setLastname(member.lastName);
            registeredMembers.setMobile(member.phoneNumber);
            registeredMembers.setStatus("1");
            registeredMembers.setMemberid(member.memberId);
            registeredMembers.setEmail(member.email);
            registeredMembers.setAccessid(member.accessId);
            registeredMembers.setUniqueid(String.valueOf(member.id));
//      registeredMembers.setExpire_time(time);
            registeredMembers.setImage(image);
            registeredMembers.setFeatures(feature);
//            boolean result = registeredMembers.save();
            return true;
        }catch (Exception e){
            Logger.debug("boolean registerDatabase(String firstname, String lastname, String mobile, String id, String email, String accessid, String uniqueid) {",e.getMessage());
        }
        return false;
    }

    private static void localRegister(MemberResponse member, String imgpath, String sync) {
        String data = "";
        File imageFile = new File(imgpath);
        if (processImg(member.firstName + "-" + member.id, imgpath, String.valueOf(member.id)) || !imageFile.exists()) {
            if (registerDatabase(member)) {

                    File file = new File(imgpath);
                if (file.exists()) {
                    file.delete();
//                    registerpath = "";
                }
            } else {
                Log.e("tag", "Register failed");
            }
        } else {
            Log.e("tag", "fail to process bitmap");
        }
    }
    private static boolean isCertifyIdExist(String uniqueID) {
//        List<RegisteredMembers> membersList = LitePal.where("uniqueid = ?", uniqueID).find(RegisteredMembers.class);
        List<RegisteredMembers> membersList = DatabaseController.getInstance().isUniqueIdExist(uniqueID);
        if (membersList != null && membersList.size() > 0) {
            return true;
        }
        return false;
    }
    public static boolean deleteDatabaseCertifyId(String name, String certifyId) {
        //List<RegisteredMembers> list = LitePal.where("uniqueid = ?", certifyId).find(RegisteredMembers.class);
        List<RegisteredMembers> list = DatabaseController.getInstance().isUniqueIdExist(certifyId);
        if (list != null && list.size() > 0) {
            FaceServer.getInstance().deleteInfo(name + "-" + certifyId);
            String featurePath = list.get(0).getFeatures();
            String imgPath = list.get(0).getImage();
            //int line = LitePal.deleteAll(RegisteredMembers.class, "uniqueid = ?", certifyId);
            int line = DatabaseController.getInstance().deleteMemberByCertifyId(certifyId);
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
    @RequiresApi(api = Build.VERSION_CODES.N)
    public static void loadMembers(String accessToken, String serialNumber, String path){
Log.v(LOG, "loadMembers");
        try {
            ROOT_PATH_STRING = path;
            ManageMemberHelper.GetMemberListResponse memberListResponse = new ManageMemberHelper(accessToken, serialNumber).getMemberList();
            if(memberListResponse == null) memberListResponse = new ManageMemberHelper.GetMemberListResponse();
            if(memberListResponse.responseCode != 1){
                Log.e(LOG, String.format("loadMembers failed: ", memberListResponse.responseSubCode, memberListResponse.responseMessage));
                return;
            }
            List<ManageMemberHelper.MemberResponse> memberList = memberListResponse.responseData;
            for(ManageMemberHelper.MemberResponse member : memberList){
                String imagePath = getImagePath(member.faceTemplate);
                Log.v(LOG, "loadMembers member: "+member);
                if (member.status)
                    if (isCertifyIdExist(String.valueOf(member.id))) {
                        deleteDatabaseCertifyId(member.firstName,String.valueOf(member.id));
                        localRegister(member, imagePath, "sync");
                    } else {
                        deleteDatabaseCertifyId(member.firstName,String.valueOf(member.id));
                        localRegister(member, imagePath, "sync");
                    }
            }

        } catch (IOException e) {
            Log.e(LOG, "loadMembers:"+e.getMessage());
            e.printStackTrace();
        }
    }

    public interface SnapService {
        @POST("GetMemberList")
        Call<GetMemberListResponse> getMemberList(@HeaderMap Map<String, String> headers);

        @POST("GetMemberById")
        Call<GetMemberByIdResponse> getMemberById(@HeaderMap Map<String, String> headers, @Body GetMemberByIdRequest getMemberByIdRequest);
    }

    public static class SnapResponse {
        public int responseCode;
        public int responseSubCode;
        public String responseMessage;
    }

    public static class MemberResponse {
        public long id;
        public String firstName;
        public String lastName;
        public String email;
        public String phoneNumber;
        public String memberId;
        public String accessId;
        public String faceTemplate;
        public boolean status;
        public int accountId;
        public int memberType;

        @Override
        public String toString() {
            return "MemberResponse{" +
                    "id=" + id +
                    ", firstName='" + firstName + '\'' +
                    ", lastName='" + lastName + '\'' +
                    ", email='" + email + '\'' +
                    ", phoneNumber='" + phoneNumber + '\'' +
                    ", memberId='" + memberId + '\'' +
                    ", accessId='" + accessId + '\'' +
                    ", status=" + status +
                    ", accountId=" + accountId +
                    ", memberType=" + memberType +
                    '}';
        }
    }
    public  class GetMemberByIdRequest{
        public long id;
        public GetMemberByIdRequest(long id){
            this.id = id;
        }
    }
    public static class GetMemberListResponse extends SnapResponse {
        public List<MemberResponse> responseData = new ArrayList<>();
    }

    public static class GetMemberByIdResponse extends SnapResponse {
        public List<MemberResponse> responseData = new ArrayList<>();
    }
}
