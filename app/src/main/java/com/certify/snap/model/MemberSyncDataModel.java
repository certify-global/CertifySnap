package com.certify.snap.model;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.arcsoft.imageutil.ArcSoftImageFormat;
import com.arcsoft.imageutil.ArcSoftImageUtil;
import com.arcsoft.imageutil.ArcSoftImageUtilError;
import com.certify.snap.common.MemberUtilData;
import com.certify.snap.faceserver.FaceServer;

import org.json.JSONArray;
import org.json.JSONObject;
import org.litepal.LitePal;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MemberSyncDataModel {
    private static final String TAG = MemberSyncDataModel.class.getSimpleName();
    private static MemberSyncDataModel mInstance = null;
    private List<RegisteredMembers> membersList = new ArrayList<>();
    private List<RegisteredMembers> dbSyncErrorMemberList = new ArrayList<>();
    private HashMap<RegisteredMembers, Boolean> dbSyncErrorMap = new HashMap<>();  //Map to keep track for the successful addition from the errorList
    private boolean isSyncing = false;
    private Context context;
    private static final String SYNCING_MESSAGE = "Syncing...";
    private static final String SYNCING_COMPLETED = "Sync completed";

    public static MemberSyncDataModel getInstance() {
        if (mInstance == null) {
            mInstance = new MemberSyncDataModel();
        }
        return mInstance;
    }

    /**
     * Method for initialization
     */
    public void init(Context ctx) {
        this.context = ctx;
        clear();
    }

    /**
     * Method that processes the Json response from the API and adds the data to the local data model
     * @param memberList Member Info
     */
    public void createMemberDataAndAdd(JSONArray memberList) {
        new Thread(() -> {
                RegisteredMembers member = new RegisteredMembers();
                isSyncing = true;
                try {
                    for (int i = 0; i < memberList.length(); i++) {
                        JSONObject c = memberList.getJSONObject(i);
                        String certifyId = c.getString("id");
                        String memberId = c.getString("memberId").replaceAll("[-+.^:,]", "");
                        if (memberId.isEmpty()) {
                            memberId = certifyId;
                        }
                        String imagePath = MemberUtilData.getMemberImagePath(c.getString("faceTemplate"), certifyId);
                        member.setFirstname(c.getString("firstName"));
                        member.setLastname(c.getString("lastName"));
                        member.setAccessid(c.getString("accessId"));
                        member.setUniqueid(c.getString("id"));
                        member.setMemberid(memberId);
                        member.setEmail(c.getString("email"));
                        member.setMobile(c.getString("phoneNumber"));
                        member.setImage(imagePath);
                        member.setStatus(String.valueOf(c.getBoolean("status")));
                        membersList.add(member);
                        Log.d(TAG, "SnapXT Add API response Member added " + membersList.size());
                    }
                } catch (Exception e) {
                    Log.e(TAG, "SnapXT Exception while adding API response member to the model");
                }
        }).start();
    }

    /**
     * Method that initiates process of adding to the database
     * @param context context
     */
    public void addToDatabase(Context context) {
        Log.d(TAG, "SnapXT Add to database, number of records: " + membersList.size());
        doSendBroadcast(SYNCING_MESSAGE, 0, 0);
        new Thread(() -> {
                for (int i = 0; i < membersList.size(); i++) {
                    doSendBroadcast(SYNCING_MESSAGE, 0, 0);
                    RegisteredMembers member = membersList.get(i);
                    if (member.getStatus().equalsIgnoreCase("true") ||
                        member.getStatus().equalsIgnoreCase("1")) {
                        if (isMemberExistsInDb(member.getUniqueid())) {
                            Log.d(TAG, "SnapXT Member already exist, delete and update " +i);
                            MemberUtilData.deleteDatabaseCertifyId(member.firstname, member.getUniqueid());
                            localRegister(member.getFirstname(), member.getLastname(), member.getMobile(),
                                    member.getMemberid(), member.getEmail(), member.getAccessid(), member.getUniqueid(),
                                    member.getImage(), "sync", context, member);
                        } else {
                            Log.d(TAG, "SnapXT New member update " +i);
                            localRegister(member.getFirstname(), member.getLastname(), member.getMobile(),
                                    member.getMemberid(), member.getEmail(), member.getAccessid(), member.getUniqueid(),
                                    member.getImage(), "sync", context, member);
                        }
                    }
                }
                if (dbSyncErrorMemberList.isEmpty()) {
                    doSendBroadcast(SYNCING_COMPLETED, 0, 0);
                }
                isSyncing = false;
        }).start();
    }

    /**
     * Method that checks if a member already exists in the database
     * @param uniqueID UniqueId
     * @return true or false accordingly
     */
    private boolean isMemberExistsInDb(String uniqueID) {
        boolean result = false;
        List<RegisteredMembers> list = LitePal.where("uniqueid = ?", uniqueID).find(RegisteredMembers.class);
        if (list != null && list.size() > 0) {
            result = true;
        }
        return result;
    }

    /**
     * Method that handles adding one record to the database
     * TODO: Can opitmize not send the individual porams
     * @param firstname firstname
     * @param lastname lastname
     * @param mobile mobile
     * @param id id
     * @param email email
     * @param accessid accessid
     * @param uniqueid uniqueis
     * @param imgpath imgpath
     * @param sync sync
     * @param context context
     * @param member member
     * @return true or false accordingly
     */
    private boolean localRegister(String firstname, String lastname, String mobile, String id, String email, String accessid,
                                  String uniqueid, String imgpath, String sync, Context context, RegisteredMembers member) {
        boolean result = false;
        File imageFile = new File(imgpath);
        if (processImg(firstname + "-" + id, imgpath, id,context) || !imageFile.exists()) {
            if (registerDatabase(firstname, lastname, mobile, id, email, accessid, uniqueid, context)) {
                Log.d(TAG, "SnapXT Record successfully updated in db");
                result = true;
                updateDbSyncErrorMap(member);
            } else {
                Log.d(TAG, "SnapXT Record failed to update in db");
                if (!dbSyncErrorMemberList.contains(member)) {
                    dbSyncErrorMemberList.add(member);
                    Log.d(TAG, "SnapXT Error dbSyncErrorMemberList size " + dbSyncErrorMemberList.size());
                }
            }
        } else {
            Log.d(TAG, "SnapXT Fail to process the image");
            if (!dbSyncErrorMemberList.contains(member)) {
                dbSyncErrorMemberList.add(member);
                Log.d(TAG, "SnapXT Error dbSyncErrorMemberList size " + dbSyncErrorMemberList.size());
            }
        }
        return result;
    }

    /**
     * Method that processes the image
     * @param name name
     * @param imgpath  path
     * @param id id
     * @param context context
     * @return true or false accordingly
     */
    private boolean processImg(String name, String imgpath, String id,Context context) {
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

    /**
     * Method that add the record to the database
     * @param firstname firstname
     * @param lastname lastname
     * @param mobile mobile
     * @param id id
     * @param email email
     * @param accessid accessid
     * @param uniqueid uniqueid
     * @param context context
     * @return true or false accordingly
     */
    private boolean registerDatabase(String firstname, String lastname, String mobile, String id, String email, String accessid, String uniqueid, Context context) {
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
            //registeredMembers.setExpire_time(time);
            registeredMembers.setImage(image);
            registeredMembers.setFeatures(feature);
            boolean result = registeredMembers.save();
            return result;
        } catch (Exception e) {
            Log.e(TAG, "SnapXT Exception while saving to Database");
        }
        return false;
    }

    /**
     * Method that initiates adding the error records to the database
     * @param context context
     */
    public void syncDbErrorList(Context context) {
        if (isSyncing) return;
        if (dbSyncErrorMemberList.isEmpty()) {
            Log.d(TAG, "SnapXT Error All members added to db");
            doSendBroadcast(SYNCING_COMPLETED, 0, 0);
            return;
        }
        dbSyncErrorMap.clear();
        doSendBroadcast(SYNCING_MESSAGE, 0, 0);
        new Thread(() -> {
                Log.d(TAG, "SnapXT Error Sync members to db, Records: " + dbSyncErrorMemberList.size());
                for (int i = 0; i < dbSyncErrorMemberList.size(); i++) {
                    doSendBroadcast(SYNCING_MESSAGE, 0, 0);
                    isSyncing = true;
                    RegisteredMembers member = dbSyncErrorMemberList.get(i);
                    if (isMemberExistsInDb(member.getUniqueid())) {
                        Log.d(TAG, "SnapXT Error Member already exist, delete and update " +i);
                        MemberUtilData.deleteDatabaseCertifyId(member.firstname, member.getUniqueid());
                        localRegister(member.getFirstname(), member.getLastname(), member.getMobile(),
                                member.getMemberid(), member.getEmail(), member.getAccessid(), member.getUniqueid(),
                                member.getImage(), "sync", context, member);
                    } else {
                        Log.d(TAG, "SnapXT Error member update " +i);
                        localRegister(member.getFirstname(), member.getLastname(), member.getMobile(),
                                member.getMemberid(), member.getEmail(), member.getAccessid(), member.getUniqueid(),
                                member.getImage(), "sync", context, member);
                    }
                }
                updateDbSyncErrorList();
                isSyncing = false;
        }).start();
    }

    /**
     * Method that updates the Error map for the error database records sync
     * @param member RegisterMember
     */
    private void updateDbSyncErrorMap(RegisteredMembers member) {
        if (dbSyncErrorMemberList.isEmpty()) return;
        dbSyncErrorMap.put(member, true);
    }

    /**
     * Method that updates the dbSyncErrorMemberList object model
     */
    private void updateDbSyncErrorList() {
        if (dbSyncErrorMemberList.isEmpty()) {
            Log.d(TAG , "SnapXT Error Sync completed successfully");
            clear();
            return;
        }
        for (Map.Entry<RegisteredMembers, Boolean> entry : dbSyncErrorMap.entrySet()) {
             RegisteredMembers uniqueId = entry.getKey();
             if (uniqueId != null) {
                 dbSyncErrorMemberList.remove(uniqueId);
             }
        }
    }

    private void doSendBroadcast(String message, int memberCount, int count) {
        Intent event_snackbar = new Intent("EVENT_SNACKBAR");
        if (!TextUtils.isEmpty(message))
            event_snackbar.putExtra("message",message);
        event_snackbar.putExtra("memberCount",memberCount);
        event_snackbar.putExtra("count",count);

        LocalBroadcastManager.getInstance(context).sendBroadcast(event_snackbar);
    }

    /**
     * Method that clears the data model
     */
    private void clear() {
        Log.d(TAG, "SnapXT Clear Data model");
        membersList.clear();
        dbSyncErrorMemberList.clear();
        dbSyncErrorMap.clear();
    }
}
