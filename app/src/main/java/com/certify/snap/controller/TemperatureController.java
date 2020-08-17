package com.certify.snap.controller;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.RemoteException;
import android.util.Log;

import com.certify.snap.R;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    public interface TemperatureCallbackListener {
        void onThermalImage(Bitmap bitmap);
        void onTemperatureRead(float temperature);
        void onTemperatureFail(GuideMessage errorCode);
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
        thermalImageUtil = Application.getInstance().getTemperatureUtil();
        if (AppSettings.getfToC().equals("F")) {
            temperatureUnit = context.getString(R.string.fahrenheit_symbol);
        } else {
            temperatureUnit = context.getString(R.string.centi);
        }
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
        clearTemperatureMap();
    }

    /**
     * Method that initiates the Guide Temperature service
     */
    public void startGuideTemperature() {
        Log.d(TAG, "Temp startGuideTemperature getGuideData");
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
                    final List<float[]> maxInRectInfo = temperatureBigData.getTemInfoList();//The length of the List is the number of rects,
                    // which records the temperature measurement information of the rect of each face
                    final float envirTem = temperatureBigData.getEmvirTem();//Ambient temperature
                    if (maxInRectInfo == null) {
                        Log.e(TAG, "Temp Error data");
                        return;
                    }
                    float temp;
                    for (int i = 0; i < maxInRectInfo.size(); i++) {
                        float temperatureCelsius = maxInRectInfo.get(i)[3];
                        if (AppSettings.getfToC().equals("F")) {
                            temperature = (float) Util.celsiusToFahrenheit(temperatureCelsius);
                        } else {
                            temperature = temperatureCelsius;
                        }
                        thermalImageUtil.stopGetGuideData();
                        if (listener != null) {
                            Log.d(TAG, "Temp measured " + temperature);
                            listener.onTemperatureRead(temperature);
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
        if (Util.isDeviceProModel()) {
            startGuideTemperature();
            return;
        }
        if (trackIdMap.containsKey(requestId)) {
            Log.d(TAG, "Track Id already exist");
            return;
        }
        trackIdMap.put(requestId, "Measure Temperature");
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
                    TemperatureData temperatureData = thermalImageUtil.getDataAndBitmap(50, true, thermalImageCallback);
                    if (temperatureData != null) {
                        if (AppSettings.getfToC().equals("F")) {
                            temperature = (float) Util.celsiusToFahrenheit(temperatureData.getTemperature());
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
                        if (listener != null && temperature != 0) {
                            listener.onTemperatureRead(temperature);
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
                    Log.e(TAG, "SnapXT Temperature Failed Reason Error Code: " + errorCode);
                    updateGuideMsgOnTemperatureFail(errorCode);

                    float temNoCorrect = Float.parseFloat(obj.getString("temNoCorrect"));
                    Log.e(TAG, "SnapXT Temperature Failed Tem no correct: " + temNoCorrect);
                    updateAllowLowOnTemperatureFail(errorCode, temNoCorrect);
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
                        callbackListener.onTemperatureFail(GuideMessage.WRONG_TEMP_TOO_COLD);
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
                AccessControlModel.getInstance().getRfidScanMatchedMember(), temperature);
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
        MemberSyncDataModel.getInstance().syncDbErrorList(context);
        AccessCardController.getInstance().accessCardLog(context,
                AccessControlModel.getInstance().getRfidScanMatchedMember(), temperature);
    }

    /**
     * Method that clears the Temperature map
     */
    private void clearTemperatureMap() {
        temperatureMap.clear();
    }

    /**
     * Method that clears the Temperature data parameters
     */
    public void clearData() {
        isTemperatureInProcess = false;
        listener = null;
        if (Util.isDeviceProModel()) {
            thermalImageUtil.stopGetGuideData();
        }
        //thermalImageUtil.reset();
        temperatureRecordData = null;
        thermalImageCallback = null;
        temperature = 0;
        trackIdMap.clear();
    }
}
