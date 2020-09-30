package com.certify.snap.controller;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.RemoteException;
import android.util.Log;

import com.certify.snap.R;
import com.certify.snap.arcface.model.FacePreviewInfo;
import com.certify.snap.arcface.model.TemperatureRect;
import com.certify.snap.arcface.util.DrawHelper;
import com.certify.snap.common.AppSettings;
import com.certify.snap.common.Application;
import com.certify.snap.common.UserExportedData;
import com.certify.snap.common.Util;
import com.certify.snap.model.AccessControlModel;
import com.certify.snap.model.MemberSyncDataModel;
import com.certify.snap.model.RegisteredMembers;
import com.common.thermalimage.GuideDataCallBack;
import com.common.thermalimage.HotImageCallback;
import com.common.thermalimage.TemperatureBigData;
import com.common.thermalimage.TemperatureBitmapData;
import com.common.thermalimage.TemperatureData;
import com.common.thermalimage.ThermalImageUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class TemperatureController {
    private static final String TAG = TemperatureController.class.getSimpleName();
    private static TemperatureController instance = null;
    private ConcurrentHashMap<Integer, UserExportedData> temperatureMap = new ConcurrentHashMap<>();
    private Context context;
    private ThermalImageUtil thermalImageUtil;
    private float temperature = 0;
    private TemperatureCallbackListener listener;
    private boolean isTemperatureInProcess = false;
    private String temperatureUnit = "";
    private UserExportedData temperatureRecordData = null;
    private HotImageCallbackImpl thermalImageCallback;
    private HashMap<Integer, String> trackIdMap = new HashMap<>();
    List<TemperatureRect> temperatureRectList = new ArrayList<>();
    List<TemperatureRect> originRectList = new ArrayList<>();
    List<Integer> distanceList = new ArrayList<>();
    private Rect tempRect;
    private boolean isTempAboveThreshold = false;
    private boolean isGuideInited = false;
    private Timer guideTempTimer = null;
    private float MIN_TEMPERATURE_THRESHOLD = 93.2f;
    private int mTemperatureRetry = 0;
    private String machineTemperature;
    private String ambientTemperature;

    public interface TemperatureCallbackListener {
        void onThermalImage(Bitmap bitmap);
        void onTemperatureRead(float temperature);
        void onTemperatureFail(GuideMessage errorCode);
        void onFaceNotInRangeOfThermal();
        void onThermalGuideReset();
        void onTemperatureLow(int retryCount, float temperature);
    }

    public enum GuideMessage {
        FACE_OUT_OF_RANGE ("face out of range or for head too low", 1),
        WRONG_TEMP_TOO_COLD("wrong tem , too cold", 2),
        NOT_ENOUGH_DATA ("not enough validData , get tem fail", 3),
        GENERIC_ERROR("generic error", 4);

        GuideMessage(final String name, final int value) {
            this.name = name;
            this.value = value;
        }

        private final String name;
        private final int value;

        public String getName() {
            return name;
        }

        public int getValue() {
            return value;
        }
    }

    public static TemperatureController getInstance() {
        if (instance == null)
            instance = new TemperatureController();

        return instance;
    }

    /**
     * Method that initializes the Temperature parameters
     * @param context Context
     */
    public void init(Context context) {
        this.context = context;
        thermalImageUtil = ApplicationController.getInstance().getTemperatureUtil();
        if (AppSettings.getfToC().equals("F")) {
            temperatureUnit = context.getString(R.string.fahrenheit_symbol);
            MIN_TEMPERATURE_THRESHOLD = 93.2f;
        } else {
            temperatureUnit = context.getString(R.string.centi);
            MIN_TEMPERATURE_THRESHOLD = 34;
        }
        tempRect = new Rect(160, 70, 220, 145);
    }

    /**
     * Method that set the callback Listener
     * @param callbackListener callbackListener
     */
    public void setTemperatureListener (TemperatureCallbackListener callbackListener) {
        this.listener = callbackListener;
    }

    /**
     * Method that gets the Temperature Listener
     * @return Temperature Listener
     */
    public TemperatureCallbackListener getTemperatureListener() {
        return this.listener;
    }

    /**
     * Method that gets the Temperature Map
     * @return Temperature Map
     */
    public ConcurrentHashMap<Integer, UserExportedData> getTemperatureMap() {
        return temperatureMap;
    }

    /**
     * Method that updates the Temperature map
     * @param trackId Request id
     * @param data User export data
     */
    public void updateTemperatureMap(int trackId, UserExportedData data) {
        boolean value = temperatureMap.containsKey(trackId);
        if (!value) {
            temperatureMap.clear();
            temperatureMap.put(trackId, data);
            sendTemperatureRecord(context);
        }
    }

    /**
     * Method that sends the Temperature record
     * @param context context
     */
    private void sendTemperatureRecord(Context context) {
        for (Map.Entry entry : temperatureMap.entrySet()) {
            UserExportedData data = (UserExportedData) entry.getValue();
            Util.recordUserTemperature(null, context, data, -1);
        }
    }

    /**
     * Method that sets the rect for the Guide temperature service
     * @param facePreviewInfoList Face preview list
     * @param drawHelperRgb Draw helper
     */
    public void setRect(final List<FacePreviewInfo> facePreviewInfoList, DrawHelper drawHelperRgb) {
        /*if (!isGuideInited) {
            Log.e(TAG, "Temp Guide is not inited");
            return;
        }*/
        new Thread(() -> {
            try {
                temperatureRectList.clear();
                originRectList.clear();
                distanceList.clear();
                int distance = 0;
                for (int i = 0; i < facePreviewInfoList.size(); i++) {
                    Rect rect = drawHelperRgb.adjustRect(facePreviewInfoList.get(i).getFaceInfo().getRect());
                    float fix = getDistance(facePreviewInfoList.get(i).getFaceInfo().getRect());
                    //Ignore the temperature read (resulting in low read if the face is not fully visible)
                    if (rect.right > 700 || rect.left < 80) {
                        final Rect[] rects = new Rect[temperatureRectList.size()];
                        int[] distances = new int[distanceList.size()];
                        thermalImageUtil.setGuideRect(rects, distances);
                        cancelGuideTempTimer();
                        if (listener != null) {
                            listener.onFaceNotInRangeOfThermal();
                        }
                        continue;
                    }
                    float horizontalOffset = (rect.left + rect.right) / 2.00f - 400;
                    float verticalOffset = (rect.top + rect.bottom) / 2.00f - 575;
                    Rect newRect;
                    if (fix > 100) {
                        newRect = new Rect(tempRect.left - 10, tempRect.top + 20, tempRect.right - 50, tempRect.bottom - 20);
                    } else if (fix > 60) {
                        newRect = new Rect(tempRect.left - 10, tempRect.top + 15, tempRect.right - 40, tempRect.bottom - 15);
                    } else if (fix > 30) {
                        newRect = new Rect(tempRect.left, tempRect.top + 10, tempRect.right - 20, tempRect.bottom - 10);
                    } else {
                        newRect = new Rect(tempRect);
                    }
                    int horizontalOffset2 = (int) (horizontalOffset / 580 * 270);
                    int verticalOffset2 = (int) (verticalOffset / 720 * 360);
                    newRect.left += verticalOffset2;
                    newRect.right += verticalOffset2;
                    newRect.top += horizontalOffset2;
                    newRect.bottom += horizontalOffset2;
                    if (newRect.left > 360 || newRect.right < 0 || newRect.top > 270 || newRect.bottom < 0) {
                        continue;
                    }
                    if (newRect.left < 0) {
                        newRect.left = 0;
                    }
                    if (newRect.right > 360) {
                        newRect.right = 360;
                    }
                    if (newRect.top < 0) {
                        newRect.top = 0;
                    }
                    if (newRect.bottom > 270) {
                        newRect.bottom = 270;
                    }
                    temperatureRectList.add(new TemperatureRect(facePreviewInfoList.get(i).getTrackId(), newRect, fix));
                    originRectList.add(new TemperatureRect(facePreviewInfoList.get(i).getTrackId(), rect, fix));
                    distanceList.add((int) fix);
                }
                if (temperatureRectList.size() == 0 || originRectList.size() == 0 || distanceList.size() == 0) {
                    return;
                }
                final Rect[] rects = new Rect[temperatureRectList.size()];
                for (int i = 0; i < temperatureRectList.size(); i++) {
                    rects[i] = temperatureRectList.get(i).getRect();
                }
                int[] distances = new int[distanceList.size()];
                for (int i = 0; i < distanceList.size(); i++) {
                    distances[i] = distanceList.get(i);
                }
                thermalImageUtil.setGuideRect(rects, distances);
            } catch (Exception e) {
                Log.e(TAG, "Exception in setGuideRect occurred" + e.getMessage());
            }
        }).start();
    }

    private float getDistance(Rect rect) {
        int h = rect.bottom - rect.top;
        int w = rect.right - rect.left;
        int v = h + w;
        return 80 * (310f / (float) v);
    }

    /**
     * Method that initiates the Guide Temperature service
     */
    public void startGuideTemperature() {
        Log.d(TAG, "Temp startGuideTemperature getGuideData");
        startGuideTemperatureTimer();
        thermalImageUtil.getGuideData(new GuideDataCallBack.Stub() {
            @Override
            public void callBackBitmap(final Bitmap bitmap) throws RemoteException {
                //update UI
                if (listener != null) {
                    listener.onThermalImage(bitmap);
                }
            }

            @Override
            public void callBackData(TemperatureBigData temperatureBigData) throws RemoteException {
                try {
                    Log.d(TAG, "Temp callBackData");
                    cancelGuideTempTimer();
                    final List<float[]> maxInRectInfo = temperatureBigData.getTemInfoList();//The length of the List is the number of rects,
                    // which records the temperature measurement information of the rect of each face
                    final float envirTem = temperatureBigData.getEmvirTem();//Ambient temperature
                    if (maxInRectInfo == null) {
                        Log.e(TAG, "Temp Error data");
                        return;
                    }
                    temperature = 0;
                    for (int i = 0; i < maxInRectInfo.size(); i++) {
                        float temperatureCelsius = maxInRectInfo.get(i)[3];
                        float machineTemperatureValue = maxInRectInfo.get(i)[0]* (9f / 5) + 32;
                        float ambientTemperatureValue = temperatureBigData.getEmvirTem() * (9f / 5) + 32;
                        machineTemperature = new DecimalFormat("##.#").format(machineTemperatureValue);
                        ambientTemperature = new DecimalFormat("##.#").format(ambientTemperatureValue);
                        if (AppSettings.getfToC().equals("F")) {
                            temperature = (float) Util.celsiusToFahrenheit(temperatureCelsius);
                        } else {
                            temperature = temperatureCelsius;
                        }
                        temperature += AppSettings.getTemperatureCompensation();
                        isGuideInited = false;
                        thermalImageUtil.stopGetGuideData();
                    }
                    if (listener != null) {
                        Log.d(TAG, "Temp measured " + temperature);
                        if ((temperature - AppSettings.getTemperatureCompensation())  >= MIN_TEMPERATURE_THRESHOLD) {
                            listener.onTemperatureRead(temperature);
                        } else {
                            mTemperatureRetry++;
                            listener.onTemperatureLow(mTemperatureRetry, temperature);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void callBackError(String s) throws RemoteException {

            }
        });
        isGuideInited = true;
    }

    /**
     * Method that initiates the Temperature measure
     * @param requestId Request id
     */
    public void startTemperatureMeasure(int requestId) {
        Log.d(TAG, "Temp startTemperatureMeasure " + trackIdMap.isEmpty());
        if (thermalImageUtil == null || isTemperatureInProcess) return;
        isTemperatureInProcess = true;
        temperature = 0;
        if (trackIdMap.containsKey(requestId)) {
            Log.d(TAG, "Track Id already exist");
            return;
        }
        trackIdMap.put(requestId, "Measure Temperature");
        if (Util.isDeviceProModel()) {
            startGuideTemperature();
            return;
        }
        startTemperature(requestId);
    }

    /**
     * Method that starts reading the Temperature
     * @param requestId Request Id
     */
    private void startTemperature(int requestId) {
        if (thermalImageUtil == null) return;
        Observable
                .create((ObservableOnSubscribe<Float>) emitter -> {
                    thermalImageCallback = new HotImageCallbackImpl();
                    temperature = 0;
                    TemperatureData temperatureData = thermalImageUtil.getDataAndBitmap(50, true, thermalImageCallback);
                    if (temperatureData != null) {
                        temperature = temperatureData.getTemperature();
                        if (AppSettings.getfToC().equals("F")) {
                            temperature = (float) Util.celsiusToFahrenheit(temperature);
                        } else {
                            temperature = temperatureData.getTemperature();
                        }
                    }
                    emitter.onNext(temperature);
                })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Float>() {
                    Disposable tempDisposable;

                    @Override
                    public void onSubscribe(Disposable d) {
                        tempDisposable = d;
                    }

                    @Override
                    public void onNext(Float temperature) {
                        if (listener != null) {
                            if ((temperature - AppSettings.getTemperatureCompensation()) >= MIN_TEMPERATURE_THRESHOLD) {
                                listener.onTemperatureRead(temperature);
                            } else {
                                mTemperatureRetry++;
                                listener.onTemperatureLow(mTemperatureRetry, temperature);
                            }
                        }
                        tempDisposable.dispose();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "Error in getting the temperature");
                        tempDisposable.dispose();
                    }

                    @Override
                    public void onComplete() {
                        //do noop
                    }
                });
    }

    /**
     * Method that gets the Temperature
     * @return temperature value
     */
    public float getTemperature() {
        return temperature;
    }

    /**
     * Method that gets the Temperature Unit
     * @return temperature unit value
     */
    public String getTemperatureUnit() {
        return temperatureUnit;
    }

    /**
     * Method that sets the temperature process flag
     * @param temperatureInProcess
     */
    public void setTemperatureInProcess(boolean temperatureInProcess) {
        isTemperatureInProcess = temperatureInProcess;
    }

    /**
     * Method that checks if the given temperature is above threshold
     * @param temperature temperature input parameter
     * @return true or false accordingly
     */
    public boolean isTemperatureAboveThreshold(float temperature) {
        boolean result = false;
        String tempThresholdSetting = AppSettings.getTemperatureThreshold();
        if (!tempThresholdSetting.isEmpty() && (temperature > Float.parseFloat(tempThresholdSetting))) {
            result = true;
        }
        isTempAboveThreshold = result;
        return result;
    }

    /**
     * Method that gets the temperature record data
     * @return Temperature record data
     */
    public UserExportedData getTemperatureRecordData() {
        return temperatureRecordData;
    }

    /**
     * Method that sets the Temperature record data
     * @param temperatureRecordData input data
     */
    public void setTemperatureRecordData(UserExportedData temperatureRecordData) {
        this.temperatureRecordData = temperatureRecordData;
    }

    /**
     * Method that returns the Temperature retry value
     * @return value
     */
    public int getTemperatureRetry() {
        return mTemperatureRetry;
    }

    /**
     * Method that sets the Temperature retry value
     */
    public void setTemperatureRetry(int value) {
        mTemperatureRetry = value;
    }

    /**
     * Class that handles the Temperature reading callbacks
     */
    private static class HotImageCallbackImpl extends HotImageCallback.Stub {
        TemperatureCallbackListener callbackListener = TemperatureController.getInstance().getTemperatureListener();

        @Override
        public void getTemperatureBimapData(TemperatureBitmapData temperatureBitmapData) throws RemoteException {
            if (callbackListener != null) {
                callbackListener.onThermalImage(temperatureBitmapData.getBitmap());
            }
        }

        @Override
        public void onTemperatureFail(String reason) throws RemoteException {
            JSONObject obj = null;
            try {
                if (reason != null) {
                    obj = new JSONObject(reason);
                    String errorCode = obj.getString("err");

                    if (errorCode != null) {
                        float temNoCorrect = Float.parseFloat(obj.getString("temNoCorrect"));
                        Log.e(TAG, "SnapXT Temperature Failed Tem no correct: " + temNoCorrect);
                        updateAllowLowOnTemperatureFail(errorCode, temNoCorrect);

                        Log.e(TAG, "SnapXT Temperature Failed Reason Error Code: " + errorCode);
                        if (TemperatureController.getInstance().getTrackIdMap().isEmpty()) return;
                        updateGuideMsgOnTemperatureFail(errorCode);
                    }
                }
            } catch (JSONException e) {
                Log.e(TAG, "Error in fetching the error value on Temperature Fail");
            }
        }

        /**
         * Method that checks and handles the Guide message settings
         * @param errorCode error value
         */
        private void updateGuideMsgOnTemperatureFail(String errorCode) {
            GuideMessage value = GuideMessage.GENERIC_ERROR;
            if (AppSettings.isGuideScreen()) {
                Log.e(TAG, "SnapXT Temperature updateGuideMsgOnTemperatureFail " + errorCode);
                if (errorCode.contains("face out of range or for head too low") ||
                        errorCode.contains("face out of range or forhead too low")) {
                    value = GuideMessage.FACE_OUT_OF_RANGE;
                } else if (errorCode.contains("wrong tem , too cold")) {
                    value = GuideMessage.WRONG_TEMP_TOO_COLD;
                } else if (errorCode.contains("not enough validData , get tem fail")) {
                    value = GuideMessage.NOT_ENOUGH_DATA;
                }
            }
            if (callbackListener != null) {
                callbackListener.onTemperatureFail(value);
            }
        }

        /**
         * Method that checks and handles the low temperature value reading
         * @param errorCode error value
         * @param tempNoCorrect temperature value read
         */
        private void updateAllowLowOnTemperatureFail(String errorCode, float tempNoCorrect) {
            if (AppSettings.isAllowLow()) {
                Log.e(TAG, "SnapXT Temperature updateAllowLowOnTemperatureFail " + tempNoCorrect);
                if (errorCode.contains("wrong tem , too cold") && tempNoCorrect > 0) {
                    float lowTemperatureSetting = Float.parseFloat(AppSettings.getTempTestLow());
                    float lowTemperature = tempNoCorrect;
                    if (AppSettings.getfToC().equals("F")) {
                        lowTemperature = (float) Util.celsiusToFahrenheit(tempNoCorrect);
                    }
                    if (lowTemperature > lowTemperatureSetting) {
                        if (callbackListener != null) {
                            callbackListener.onTemperatureRead(lowTemperature);
                        }
                        return;
                    }
                    if (callbackListener != null) {
                        TemperatureController.getInstance().mTemperatureRetry++;
                        callbackListener.onTemperatureLow(TemperatureController.getInstance().mTemperatureRetry, lowTemperature);
                    }
                }
            }
        }
    }

    /**
     * Method that updates the corresponding Controllers for further processing on reading normal temperature
     * @param membersList Members list
     */
    public void updateControllersOnNormalTempRead(List<RegisteredMembers> membersList) {
        Log.d(TAG, "updateControllers on Normal Temperature");
        CameraController.getInstance().setScanState(CameraController.ScanState.COMPLETE);
        CameraController.getInstance().setFaceVisible(false);
        SoundController.getInstance().playNormalTemperatureSound();
        AccessCardController.getInstance().processUnlockDoor(membersList);
        BLEController.getInstance().setLightOnNormalTemperature();
        PrinterController.getInstance().printOnNormalTemperature();
        MemberSyncDataModel.getInstance().syncDbErrorList(context);
        AccessCardController.getInstance().accessCardLog(context,
                AccessControlModel.getInstance().getRfidScanMatchedMember(), temperature,
                TemperatureController.getInstance().getTemperatureRecordData());
    }

    /**
     * Method that updates the corresponding Controllers for further processing on reading high temperature
     * @param membersList Members list
     */
    public void updateControllersOnHighTempRead(List<RegisteredMembers> membersList) {
        Log.d(TAG, "updateControllers on High Temperature");
        CameraController.getInstance().setScanState(CameraController.ScanState.COMPLETE);
        CameraController.getInstance().setFaceVisible(false);
        SoundController.getInstance().playHighTemperatureSound();
        AccessCardController.getInstance().processUnlockDoorHigh(membersList);
        BLEController.getInstance().setLightOnHighTemperature();
        PrinterController.getInstance().printOnHighTemperature();
        MemberSyncDataModel.getInstance().syncDbErrorList(context);
        AccessCardController.getInstance().accessCardLog(context,
                AccessControlModel.getInstance().getRfidScanMatchedMember(), temperature,
                TemperatureController.getInstance().getTemperatureRecordData());
    }

    /**
     * Method that returns the value if the temperature read is above threshold
     * @return true or false accordingly
     */
    public boolean isTempAboveThreshold() {
        return isTempAboveThreshold;
    }

    /**
     * Method that clears the Temperature map
     */
    public void clearTemperatureMap() {
        temperatureMap.clear();
    }

    /**
     * Method that returns the trackId Map for Temperature
     * @return Map data
     */
    public HashMap<Integer, String> getTrackIdMap() {
        return trackIdMap;
    }

    /**
     * Method that starts the Guide Temperature timer
     */
    private void startGuideTemperatureTimer() {
        guideTempTimer = new Timer();
        guideTempTimer.schedule(new TimerTask() {
            public void run() {
                this.cancel();
                resetGuideThermal();
            }
        }, 5 * 1000);
    }

    /**
     * Method that cancels the Guide Temperature timer
     */
    private void cancelGuideTempTimer() {
        if (guideTempTimer != null) {
            Log.d(TAG, "Temp Cancel Guide Temperature timer");
            guideTempTimer.cancel();
        }
    }

    /**
     * Method that resets the Guide Thermal
     */
    private void resetGuideThermal() {
        if (thermalImageUtil != null) {
            Log.e(TAG, "Reset Guide ThemalUtil");
            thermalImageUtil.stopGetGuideData();
            ApplicationController.getInstance().releaseThermalUtil();
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            ApplicationController.getInstance().initThermalUtil(context);
            if (listener != null) {
                listener.onThermalGuideReset();
            }
        }
    }

    public String getMachineTemperature() {
        return machineTemperature;
    }

    public String getAmbientTemperature() {
        return ambientTemperature;
    }

    /**
     * Method that clears the Temperature data parameters
     */
    public void clearData() {
        isTemperatureInProcess = false;
        listener = null;
        if (Util.isDeviceProModel()) {
            isGuideInited = false;
            if (thermalImageUtil != null) {
                thermalImageUtil.stopGetGuideData();
            }
            cancelGuideTempTimer();
        }
        temperatureRecordData = null;
        thermalImageCallback = null;
        temperature = 0;
        trackIdMap.clear();
        temperatureRectList.clear();
        originRectList.clear();
        distanceList.clear();
        isTempAboveThreshold = false;
    }
}
