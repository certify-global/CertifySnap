package com.certify.snap.model;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.arcsoft.imageutil.ArcSoftImageFormat;
import com.arcsoft.imageutil.ArcSoftImageUtil;
import com.arcsoft.imageutil.ArcSoftImageUtilError;
import com.certify.snap.common.AppSettings;
import com.certify.snap.common.MemberUtilData;
import com.certify.snap.common.Util;
import com.certify.snap.controller.DatabaseController;
import com.certify.snap.faceserver.FaceServer;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MemberSyncDataModel {
    private static final String TAG = MemberSyncDataModel.class.getSimpleName();
    private static MemberSyncDataModel mInstance = null;
    private List<RegisteredMembers> membersList = new ArrayList<>();
    private List<RegisteredMembers> dbSyncErrorMemberList = new ArrayList<>();
    private HashMap<RegisteredMembers, Boolean> dbSyncErrorMap = new HashMap<>();  //Map to keep track for the successful addition from the errorList
    private boolean isSyncing = false;
    private Context context;
    private int NUM_OF_RECORDS = 0;
    private SyncDataCallBackListener listener = null;
    private DatabaseAddType dbAddType = DatabaseAddType.SCALE;
    private long index = 0;
    private int failedImageSyncCount = 0;

    public static final int SYNC_START = 1;
    public static final int SYNC_IN_PROGRESS = 2;
    public static final int SYNC_COMPLETED = 3;
    public static final int SYNC_PHOTO_FAILED = 4;

    public interface SyncDataCallBackListener {
        void onMemberAddedToDb(RegisteredMembers member);
    }

    public enum DatabaseAddType {
        SERIAL,
        SCALE
    }

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
     * Method for initialization with parameter
     * @param ctx context
     * @param type database add type Serial or Scale
     */
    public void init(Context ctx, DatabaseAddType type) {
        this.context = ctx;
        clear();
        dbAddType = type;
    }

    /**
     * Method that processes the Json response from the API and adds the data to the local data model
     * @param memberList Member Info
     */
    public void createMemberDataAndAdd(JSONArray memberList) {
        isSyncing = true;
        Observable
                .create((ObservableOnSubscribe<RegisteredMembers>) emitter -> {
                    RegisteredMembers member = new RegisteredMembers();
                    try {
                        for (int i = 0; i < memberList.length(); i++) {
                            JSONObject c = memberList.getJSONObject(i);
                            String certifyId = c.getString("id");
                            String memberId = c.getString("memberId");
                            String imagePath = MemberUtilData.getMemberImagePath(c.getString("faceTemplate"), certifyId);
                            member.setFirstname(c.getString("firstName"));
                            member.setLastname(c.getString("lastName"));
                            member.setAccessid(c.getString("accessId"));
                            member.setUniqueid(c.getString("id"));
                            member.setMemberid(memberId);
                            if (c.has("memberType")) {
                                member.setMemberType(c.getString("memberType"));
                            }
                            if (c.has("memberTypeName")) {
                                member.setMemberTypeName(c.getString("memberTypeName"));
                            }
                            if (c.has("groupId")) {
                                member.setGroupId(c.getString("groupId"));
                            }
                            if (c.has("networkId")) {
                                member.setNetworkId(c.getString("networkId"));
                            }
                            if (c.has("fromDate")) {
                                member.setAccessFromTime(c.getString("fromDate"));
                            }
                            if (c.has("toDate")) {
                                member.setAccessToTime(c.getString("toDate"));
                            }
                            member.setEmail(c.getString("email"));
                            member.setMobile(c.getString("phoneNumber"));
                            member.setImage(imagePath);
                            member.setStatus(String.valueOf(c.getBoolean("status")));
                            member.setDateTime(Util.currentDate());
                            index = index +1;
                            member.setPrimaryId(index);
                            emitter.onNext(member);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "SnapXT Exception while adding API response member to the model");
                    }
                })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<RegisteredMembers>() {
                    Disposable addMemberDisposable;

                    @Override
                    public void onSubscribe(Disposable d) {
                        addMemberDisposable = d;
                    }

                    @Override
                    public void onNext(RegisteredMembers member) {
                        synchronized (this) {
                            membersList.add(member);
                        }
                        Log.d(TAG, "SnapXT Add API response Member added " + membersList.size());

                        //Add records fetched from server, add it to the database
                        addToDatabase();
                        addMemberDisposable.dispose();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "Error in adding the member to data model from server");
                    }

                    @Override
                    public void onComplete() {
                        //do noop
                    }
                });
    }

    /**
     * Method that processes the Json response from the API and updates the data to the local data model
     * @param memberList Member Info
     */
    public void createMemberDataAndUpdate(JSONArray memberList) {
        isSyncing = true;
        Observable
                .create((ObservableOnSubscribe<RegisteredMembers>) emitter -> {
                    RegisteredMembers member = new RegisteredMembers();
                    try {
                        for (int i = 0; i < memberList.length(); i++) {
                            JSONObject c = memberList.getJSONObject(i);
                            String certifyId = c.getString("id");
                            String memberId = c.getString("memberId");
                            String imagePath = MemberUtilData.getMemberImagePath(c.getString("faceTemplate"), certifyId);
                            String groupId = "0";
                            member.setFirstname(c.getString("firstName"));
                            member.setLastname(c.getString("lastName"));
                            member.setAccessid(c.getString("accessId"));
                            member.setUniqueid(c.getString("id"));
                            member.setMemberid(memberId);
                            if (c.has("memberType")) {
                                member.setMemberType(c.getString("memberType"));
                            }
                            if (c.has("memberTypeName")) {
                                member.setMemberTypeName(c.getString("memberTypeName"));
                            }
                            if (c.has("networkId")) {
                                member.setNetworkId(c.getString("networkId"));
                            }
                            if (c.has("groupId")) {
                                groupId = c.getString("groupId");
                            }
                            if (c.has("fromDate")) {
                                member.setAccessFromTime(c.getString("fromDate"));
                            }
                            if (c.has("toDate")) {
                                member.setAccessToTime(c.getString("toDate"));
                            }
                            member.setEmail(c.getString("email"));
                            member.setMobile(c.getString("phoneNumber"));
                            member.setImage(imagePath);
                            member.setStatus(String.valueOf(c.getBoolean("status")));
                            member.setDateTime(Util.currentDate());
                            member.setGroupId(groupId);

                            List<RegisteredMembers> membersList = DatabaseController.getInstance().isUniqueIdExit(certifyId);
                            if (membersList != null && membersList.size() > 0) {
                                if (isMemberSyncGroupIdEnabled() &&
                                    !AppSettings.getMemberSyncGroupId().contains(groupId)) {
                                    deleteRecord(membersList.get(0).firstname, membersList.get(0).getPrimaryId());
                                    updateSyncCompletion();
                                    emitter.onNext(null);
                                } else {
                                    member.setPrimaryId(membersList.get(0).getPrimaryId());
                                    emitter.onNext(member);
                                }
                            } else {
                                if (isMemberSyncGroupIdEnabled() &&
                                    AppSettings.getMemberSyncGroupId().contains(groupId)) {
                                    index = index + 1;
                                    member.setPrimaryId(index);
                                    emitter.onNext(member);
                                } else {
                                    emitter.onNext(null);
                                }
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "SnapXT Exception while adding API response member to the model");
                    }
                })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<RegisteredMembers>() {
                    Disposable addMemberDisposable;

                    @Override
                    public void onSubscribe(Disposable d) {
                        addMemberDisposable = d;
                    }

                    @Override
                    public void onNext(RegisteredMembers member) {
                        if (member != null) {
                            membersList.add(member);
                            Log.d(TAG, "SnapXT Add API response Member added to List " + membersList.size());

                            //Add records fetched from server, add it to the database
                            addToDatabase();
                        }
                        addMemberDisposable.dispose();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "Error in adding the member to data model from server");
                    }

                    @Override
                    public void onComplete() {
                        //do noop
                    }
                });
    }

    /**
     * Method that initiates process of adding to the database
     * @param context context
     */
    private void addToDatabase(Context context) {
        Log.d(TAG, "SnapXT Add to database, number of records: " + membersList.size());
        doSendBroadcast(SYNC_IN_PROGRESS, 0, 0);
        new Thread(() -> {
                for (int i = 0; i < membersList.size(); i++) {
                    doSendBroadcast(SYNC_IN_PROGRESS, 0, 0);
                    RegisteredMembers member = membersList.get(i);
                    if (member.getStatus().equalsIgnoreCase("true") ||
                        member.getStatus().equalsIgnoreCase("1")) {
                        if (isMemberExistsInDb(member.getPrimaryId())) {
                            Log.d(TAG, "SnapXT Member already exist, delete and update " +i);
                            deleteRecord(member.firstname, member.getPrimaryId());
                            localRegister(member.getFirstname(), member.getLastname(), member.getMobile(),
                                    member.getMemberid(), member.getEmail(), member.getAccessid(), member.getUniqueid(),
                                    member.getImage(), "sync", context, member, member.getPrimaryId());
                        } else {
                            Log.d(TAG, "SnapXT New member update " +i);
                            localRegister(member.getFirstname(), member.getLastname(), member.getMobile(),
                                    member.getMemberid(), member.getEmail(), member.getAccessid(), member.getUniqueid(),
                                    member.getImage(), "sync", context, member, member.getPrimaryId());
                        }
                    }
                }
                if (dbSyncErrorMemberList.isEmpty()) {
                    updateSyncCompletion();
                }
                isSyncing = false;
        }).start();
    }

    /**
     * Method that initiates process of adding to the database
     * @param context context
     */
    private synchronized void addToDatabaseSerial(Context context) {
        Log.d(TAG, "SnapXT Add to database Serial, number of records: " + membersList.size());
        for (int i = 0; i < membersList.size(); i++) {
            RegisteredMembers member = membersList.get(i);
            if (member.getStatus().equalsIgnoreCase("true") ||
                    member.getStatus().equalsIgnoreCase("1")) {
                if (isMemberExistsInDb(member.getPrimaryId())) {
                    Log.d(TAG, "SnapXT Member already exist, delete and update " +i);
                    deleteRecord(member.firstname, member.getPrimaryId());
                    localRegister(member.getFirstname(), member.getLastname(), member.getMobile(),
                            member.getMemberid(), member.getEmail(), member.getAccessid(), member.getUniqueid(),
                            member.getImage(), "sync", context, member, member.getPrimaryId());
                    if (listener != null) {
                        listener.onMemberAddedToDb(member);
                    }
                    membersList.remove(member);
                } else {
                    Log.d(TAG, "SnapXT New member update " +i);
                    localRegister(member.getFirstname(), member.getLastname(), member.getMobile(),
                            member.getMemberid(), member.getEmail(), member.getAccessid(), member.getUniqueid(),
                            member.getImage(), "sync", context, member, member.getPrimaryId());
                    if (listener != null) {
                        listener.onMemberAddedToDb(member);
                    }
                    membersList.remove(member);
                }
            }
        }
        isSyncing = false;
    }

    /**
     * Method that checks if a member already exists in the database
     * @param primaryId Primary Id
     * @return true or false accordingly
     */
    private boolean isMemberExistsInDb(long primaryId) {
        boolean result = false;
        List<RegisteredMembers> list = DatabaseController.getInstance().findMember(primaryId);
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
     * @param memberId memberId
     * @param email email
     * @param accessid accessid
     * @param uniqueid uniqueis
     * @param imgpath imgpath
     * @param sync sync
     * @param context context
     * @param member member
     * @return true or false accordingly
     *
     */
    private boolean localRegister(String firstname, String lastname, String mobile, String memberId, String email, String accessid,
                                  String uniqueid, String imgpath, String sync, Context context, RegisteredMembers member, long primaryId) {
        boolean result = false;
        File imageFile = new File(imgpath);
        if (processImg(firstname + "-" + primaryId, imgpath, String.valueOf(primaryId),context) || !imageFile.exists()) {
            if (registerDatabase(firstname, lastname, mobile, memberId, email, accessid, uniqueid, context, member.getDateTime(), primaryId,
                                 member.memberType, member.memberTypeName, member.networkId, member.accessFromTime, member.accessToTime, member.groupId)) {
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
            /*if (!dbSyncErrorMemberList.contains(member)) {
                dbSyncErrorMemberList.add(member);
                Log.d(TAG, "SnapXT Error dbSyncErrorMemberList size " + dbSyncErrorMemberList.size());
            }*/
            failedImageSyncCount++;
            if (registerDatabase(firstname, lastname, mobile, memberId, email, accessid, uniqueid, context, member.getDateTime(), primaryId,
                    member.memberType, member.memberTypeName, member.networkId, member.accessFromTime, member.accessToTime, member.groupId)) {
                Log.d(TAG, "SnapXT Record successfully updated in db");
                result = true;
                updateDbSyncErrorMap(member);
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
    public boolean processImg(String name, String imgpath, String id,Context context) {
        if (imgpath.isEmpty()) return false;
        try {
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
            boolean success = FaceServer.getInstance().registerBgr24(context, bgr24, bitmap.getWidth(),
                    bitmap.getHeight(), name, id);
            return success;
        } catch (Exception e) {
            Log.e(TAG, "Error in processing the image");
        }
        return false;
    }

    /**
     * Method that add the record to the database
     * @param firstname firstname
     * @param lastname lastname
     * @param mobile mobile
     * @param memberId memberId
     * @param email email
     * @param accessid accessid
     * @param uniqueid uniqueid
     * @param context context
     * @param dateTime dateTime
     * @return true or false accordingly
     */
    public boolean registerDatabase(String firstname, String lastname, String mobile, String memberId, String email, String accessid, String uniqueid, Context context,
                                    String dateTime, long primaryId, String memberType, String memberTypeName,String networkId, String accessFromTime, String accessToTime,
                                    String groupId) {
        try {
            String username = firstname + "-" + primaryId;
            String ROOT_PATH_STRING = context.getFilesDir().getAbsolutePath();
            String image = ROOT_PATH_STRING + File.separator + FaceServer.SAVE_IMG_DIR + File.separator + username + FaceServer.IMG_SUFFIX;
            String feature = ROOT_PATH_STRING + File.separator + FaceServer.SAVE_FEATURE_DIR + File.separator + username;
            Log.e("tag", "image_uri---" + image + "  feature_uri---" + feature);

            RegisteredMembers registeredMembers = new RegisteredMembers();
            registeredMembers.setFirstname(firstname);
            registeredMembers.setLastname(lastname);
            registeredMembers.setMobile(mobile);
            registeredMembers.setStatus("1");
            registeredMembers.setMemberid(memberId);
            registeredMembers.setEmail(email);
            registeredMembers.setAccessid(accessid);
            registeredMembers.setUniqueid(uniqueid);
            //registeredMembers.setExpire_time(time);
            registeredMembers.setDateTime(dateTime);
            registeredMembers.setImage(image);
            registeredMembers.setFeatures(feature);
            registeredMembers.setPrimaryId(primaryId);
            registeredMembers.setMemberType(memberType);
            registeredMembers.setMemberTypeName(memberTypeName);
            registeredMembers.setNetworkId(networkId);
            registeredMembers.setAccessFromTime(accessFromTime);
            registeredMembers.setAccessToTime(accessToTime);
            registeredMembers.setGroupId(groupId);
            DatabaseController.getInstance().insertMemberToDB(registeredMembers);
            return true;
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
            Log.d(TAG, "SnapXT All members added to db");
            return;
        }
        dbSyncErrorMap.clear();
        doSendBroadcast(SYNC_IN_PROGRESS, 0, 0);
        new Thread(() -> {
                Log.d(TAG, "SnapXT Error Sync members to db, Records: " + dbSyncErrorMemberList.size());
                for (int i = 0; i < dbSyncErrorMemberList.size(); i++) {
                    doSendBroadcast(SYNC_IN_PROGRESS, 0, 0);
                    isSyncing = true;
                    RegisteredMembers member = dbSyncErrorMemberList.get(i);
                    if (isMemberExistsInDb(member.getPrimaryId())) {
                        Log.d(TAG, "SnapXT Error Member already exist, delete and update " +i);
                        deleteRecord(member.firstname, member.getPrimaryId());
                        localRegister(member.getFirstname(), member.getLastname(), member.getMobile(),
                                member.getMemberid(), member.getEmail(), member.getAccessid(), member.getUniqueid(),
                                member.getImage(), "sync", context, member, member.getPrimaryId());
                    } else {
                        Log.d(TAG, "SnapXT Error member update " +i);
                        localRegister(member.getFirstname(), member.getLastname(), member.getMobile(),
                                member.getMemberid(), member.getEmail(), member.getAccessid(), member.getUniqueid(),
                                member.getImage(), "sync", context, member, member.getPrimaryId());
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
    }

    /**
     * Method that updates the dbSyncErrorMemberList object model
     */
    private void updateDbSyncErrorList() {
        if (dbSyncErrorMemberList.isEmpty()) {
            Log.d(TAG , "SnapXT Error Sync completed successfully");
            updateSyncCompletion();
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

    private void doSendBroadcast(int actionCode, int memberCount, int count) {
        Intent event_snackbar = new Intent("EVENT_SNACKBAR");
        event_snackbar.putExtra("actionCode", actionCode);
        event_snackbar.putExtra("memberCount", memberCount);
        event_snackbar.putExtra("count", count);

        LocalBroadcastManager.getInstance(context).sendBroadcast(event_snackbar);
    }

    /**
     *
     * @param value
     */
    public void setNumOfRecords(int value) {
        this.NUM_OF_RECORDS = value;
    }

    private void addToDatabase() {
        switch (dbAddType) {
            case SCALE: {
                if (membersList.size() == NUM_OF_RECORDS) {
                    addToDatabase(context);
                }
            }
            break;

            case SERIAL: {
                addToDatabaseSerial(context);
            }
            break;
        }
    }

    /**
     * Method that set the callback listner
     * @param callBackListener callbackListener
     */
    public void setListener(SyncDataCallBackListener callBackListener) {
        this.listener = callBackListener;
    }

    private boolean deleteRecord(String name, long primaryId) {
        List<RegisteredMembers> list = DatabaseController.getInstance().findMember(primaryId);
        if (list != null && list.size() > 0) {
            FaceServer.getInstance().deleteInfo(name + "-" + primaryId);
            String featurePath = list.get(0).getFeatures();
            String imgPath = list.get(0).getImage();
            int line = DatabaseController.getInstance().deleteMember(primaryId);
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

    public boolean isSyncing() {
        return isSyncing;
    }

    private boolean isMemberSyncGroupIdEnabled() {
        return (!AppSettings.getMemberSyncGroupId().equals("0"));
    }

    private void updateSyncCompletion() {
        doSendBroadcast(SYNC_COMPLETED, 0, 0);
        if (failedImageSyncCount > 0) {
            doSendBroadcast(SYNC_PHOTO_FAILED, failedImageSyncCount, 0);
        }
    }

    /**
     * Method that clears the data model
     */
    public void clear() {
        Log.d(TAG, "SnapXT Clear Data model");
        membersList.clear();
        dbSyncErrorMemberList.clear();
        dbSyncErrorMap.clear();
        dbAddType = DatabaseAddType.SCALE;
        index = 0;
        failedImageSyncCount = 0;
    }
}
