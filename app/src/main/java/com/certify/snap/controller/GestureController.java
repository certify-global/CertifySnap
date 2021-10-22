package com.certify.snap.controller;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

import com.certify.callback.GestureAnswerCallback;
import com.certify.callback.GestureCallback;
import com.certify.snap.activity.IrCameraActivity;
import com.certify.snap.api.response.GestureQuestionsDb;
import com.certify.snap.api.response.LanguageData;
import com.certify.snap.api.response.QuestionData;
import com.certify.snap.api.response.QuestionListResponse;
import com.certify.snap.api.response.QuestionSurveyOptions;
import com.certify.snap.async.AsyncGestureAnswer;
import com.certify.snap.async.AsyncJSONObjectGesture;
import com.certify.snap.common.AppSettings;
import com.certify.snap.common.EndPoints;
import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.Logger;
import com.certify.snap.common.Util;
import com.certify.snap.gesture.GestureConfiguration;
import com.certify.snap.model.LogicWaveSkipDb;
import com.certify.snap.model.QuestionDataDb;
import com.certify.snap.model.TouchlessWaveSkip;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class GestureController implements GestureCallback, GestureAnswerCallback {
    private static final String TAG = GestureController.class.getSimpleName();
    private static GestureController instance = null;
    private Context mContext;
    private SharedPreferences sharedPreferences;
    private Timer mTimer;
    private boolean wait = false;
    private boolean runCheck = false;
    private boolean allQuestionAnswered = false;
    private GestureCallbackListener listener = null;
    private LinkedHashMap<QuestionData, String> questionAnswerMap = new LinkedHashMap<>();
    private QuestionData currentQuestionData = null;
    private GestureHomeCallBackListener gestureListener = null;
    private boolean isCallback = false;
    private Timer mQuestionsTimer;
    private GestureMECallbackListener gestureMEListener = null;
    private boolean isMECallback = false;
    private boolean isGestureDeviceConnected = false;
    private boolean isLanguageUpdated = false;

    //Hand Gesture
    private UsbDevice usbReader = null;
    private UsbManager mUsbManager = null;
    private static final String ACTION_USB_PERMISSION = "com.wch.multiport.USB_PERMISSION";
    private int index = 0;
    private int leftHandRangeVal = 200;
    private int rightHandRangeVal = 200;
    private boolean isQuestionnaireFailed = false;
    private Timer waveHandTimer = null;
    private LinkedHashMap<String, Boolean> waveHandProcessed = new LinkedHashMap<>();
    private static final String LEFT_HAND = "LeftHand";
    private static final String RIGHT_HAND = "RightHand";
    private static final String BOTH_HANDS = "BothHands";
    private int languageSelectionIndex = 0;
    private List<GestureQuestionsDb> gestureQuestionsDbList = null;

    //Voice Gesture
    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;
    private UsbDeviceConnection connection;

    public interface GestureCallbackListener {
        void onQuestionAnswered(String question);

        void onAllQuestionsAnswered();

        void onVoiceListeningStart();

        void onQuestionsReceived();

        void onQuestionsNotReceived();

        void onBothHandWave();

        void onFetchingQuestions();

        void onNegativeAnswer();

        void onWaveHandTimeout();

        void onWaveHandReset();
    }

    public interface GestureHomeCallBackListener {
        void onGestureDetected();

        void onLeftHandGesture();
    }

    public interface GestureMECallbackListener {
        void onGestureMEDetected();

        void onLeftHandWave();

        void onWaveHandTimeout();

        void onWaveHandReset();
    }

    public static GestureController getInstance() {
        if (instance == null) {
            instance = new GestureController();
        }
        return instance;
    }

    public void init(Context context) {
        this.mContext = context;
        index = 0;
        wait = true;
        isQuestionnaireFailed = false;
        sharedPreferences = Util.getSharedPreferences(mContext);
    }

    public void getQuestions() {
        List<QuestionData> questionDataList = new ArrayList<>(questionAnswerMap.keySet());
        if (questionDataList.isEmpty()) {
            Log.d(TAG, "Gesture Fetch questions");
            if (listener != null) {
                listener.onFetchingQuestions();
            }
            getQuestionsAPI(mContext);
            startGetQuestionsTimer();
            return;
        }
        Log.d(TAG, "Gesture Start flow");
        startGestureFlow();
    }

    public void initContext(Context context) {
        this.mContext = context;
        wait = false;
        sharedPreferences = Util.getSharedPreferences(mContext);
        isGestureDeviceConnected = Util.isGestureDeviceConnected(context);
    }

    public void initGestureRangeValues() {
        if (isGestureDeviceConnected) {
            GestureConfiguration.getInstance().initGestureRangeValue();
        }
    }

    private void startGestureFlow() {
        index = 0;
        resetQuestionAnswerMap();
        if (listener != null) {
            listener.onQuestionsReceived();
        }
    }

    public void setCallback(boolean callback) {
        isCallback = callback;
    }

    public boolean getGestureCallback() {
        return isCallback;
    }

    public void getGestureQuestions() {
        getQuestionsAPI(mContext);
        startGetQuestionsTimer();
    }

    public void getQuestionsAPI(Context context) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("settingId", sharedPreferences.getString(GlobalParameters.Touchless_setting_id, ""));
            obj.put("languageConversion", true);
            new AsyncJSONObjectGesture(obj, this, sharedPreferences.getString(GlobalParameters.URL, EndPoints.prod_url) + EndPoints.GetQuestions, context).execute();
        } catch (Exception e) {
            Log.d(TAG, "getQuestionSAPI" + e.getMessage());
        }
    }

    @Override
    public void onJSONObjectListenerGesture(JSONObject reportInfo, String status, JSONObject req) {
        try {
            if (reportInfo == null) {
                Logger.error(TAG, "onJSONObjectListenerGesture", "GetQuestions Log api failed");
                getQuestionsFromDb(DeviceSettingsController.getInstance().getLanguageToUpdate());
                if (listener != null) {
                    cancelQuestionsTimer();
                    listener.onQuestionsReceived();
                }
                return;
            }
            questionAnswerMap.clear();
            cancelQuestionsTimer();
            Gson gson = new Gson();
            QuestionListResponse response = gson.fromJson(String.valueOf(reportInfo), QuestionListResponse.class);
            if (response.responseCode != null && response.responseCode.equals("1")) {
                List<QuestionData> questionList = response.responseData.questionList;
                if (questionList.size() > 0) {
                    List<QuestionDataDb> questionDataDbList = new ArrayList<>();
                    GestureQuestionsDb gestureQuestionsDb = new GestureQuestionsDb();
                    QuestionData qData = null;
                    String languageCode = "";

                    DatabaseController.getInstance().deleteGestureQuestionsListFromDb();
                    clearQuestionAnswerMap();
                    for (int i = 0; i < questionList.size(); i++) {
                        QuestionData questionData = questionList.get(i);
                        if (questionData.languageCode != null) {
                            if (questionData.languageCode.equals(AppSettings.getLanguageType())) {
                                questionAnswerMap.put(questionData, "NA");
                            }
                            if (qData == null) {
                                qData = questionData;
                                languageCode = questionData.languageCode;
                            }
                            if (languageCode.equals(questionData.languageCode)) {
                                questionDataDbList.add(getDbQuestionData(questionData, i));
                                continue;
                            }
                            gestureQuestionsDb.primaryId = DeviceSettingsController.getInstance().getLanguageIdOnCode(languageCode);
                            gestureQuestionsDb.questionsDbList = questionDataDbList;
                            DatabaseController.getInstance().insertGestureQuestionList(gestureQuestionsDb);
                            questionDataDbList.clear();
                            questionDataDbList.add(getDbQuestionData(questionData, i));
                            qData = questionData;
                            languageCode = questionData.languageCode;
                        } else {
                            languageCode = "en";
                            questionDataDbList.add(getDbQuestionData(questionData, i));
                        }
                    }
                    if (!questionDataDbList.isEmpty()) {
                        gestureQuestionsDb.primaryId = DeviceSettingsController.getInstance().getLanguageIdOnCode(languageCode);
                        gestureQuestionsDb.questionsDbList = questionDataDbList;
                        DatabaseController.getInstance().insertGestureQuestionList(gestureQuestionsDb);
                        questionDataDbList.clear();
                    }
                    gestureQuestionsDbList = DatabaseController.getInstance().getGestureQuestionsListFromDb();
                    getQuestionsFromDb(DeviceSettingsController.getInstance().getLanguageToUpdate());
                }
                Log.d(TAG, "Gesture Questions list updated");
                DatabaseController.getInstance().deleteWaveLogicSkipListFromDb();
                Util.writeInt(sharedPreferences, GlobalParameters.Touchless_wave_skip, response.responseData.logicJsonAdvan.enableLogic);
                if (response.responseData.logicJsonAdvan.enableLogic == 1) {
                    List<TouchlessWaveSkip> waveSkipList = response.responseData.logicJsonAdvan.touchlessWaveSkips;
                    List<LogicWaveSkipDb> logicWaveSkipDbList = new ArrayList<>();
                    if (waveSkipList != null && waveSkipList.size() > 0) {
                        for (int i = 0; i < waveSkipList.size(); i++) {
                            TouchlessWaveSkip waveSkip = waveSkipList.get(i);
                            LogicWaveSkipDb temp = getDbWaveSkipData(waveSkip, i);
                            logicWaveSkipDbList.add(temp);
                        }
                        DatabaseController.getInstance().insertWaveSkipList(logicWaveSkipDbList);
                    }
                }

            } else {
                getQuestionsFromDb(DeviceSettingsController.getInstance().getLanguageToUpdate());
            }
            if (listener != null) {
                listener.onQuestionsReceived();
            }
        } catch (Exception e) {
            if (listener != null) {
                listener.onWaveHandTimeout();
            }
            Log.d(TAG, "onJSONObjectListenerGesture" + e.getMessage());
        }
    }

    public void setCallbackListener(GestureCallbackListener callbackListener) {
        this.listener = callbackListener;
    }

    public void setGestureHomeCallbackListener(GestureHomeCallBackListener callbackListener) {
        isCallback = false;
        this.gestureListener = callbackListener;
    }

    public void setGestureMECallbackListener(GestureMECallbackListener callbackListener) {
        wait = false;
        isMECallback = false;
        this.gestureMEListener = callbackListener;
    }

    public boolean isLanguageUpdated() {
        return isLanguageUpdated;
    }

    public void setLanguageUpdated(boolean languageUpdated) {
        isLanguageUpdated = languageUpdated;
    }

    /**
     * Method that initializes the voice
     *
     * @param context context
     */
    public void initVoice(Context context) {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);

        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 120000);
    }

    /**
     * Method that sets the Speech recognition listener
     */
    public void setSpeechRecognitionListener() {
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {
                Log.d(TAG, "Voice onReadyForSpeech");
            }

            @Override
            public void onBeginningOfSpeech() {
                Log.d(TAG, "Voice onBeginningOfSpeech");
            }

            @Override
            public void onRmsChanged(float v) {
                Log.d(TAG, "Voice onRmsChanged");
            }

            @Override
            public void onBufferReceived(byte[] bytes) {
                Log.d(TAG, "Voice onBufferReceived");
            }

            @Override
            public void onEndOfSpeech() {
                Log.d(TAG, "Voice onEndOfSpeech");

            }

            @Override
            public void onError(int i) {
                Log.d(TAG, "Voice onError");
                if (allQuestionAnswered) {
                    stopListening();
                    return;
                }
                startListening();
            }

            @Override
            public void onResults(Bundle bundle) {
                Log.d(TAG, "Voice onResults");
                ArrayList<String> data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (data != null && data.size() > 0) {
                    if (data.get(0).toLowerCase().equals("yes")
                            || data.get(0).toLowerCase().equals("no")) {
                        onQuestionAnswered(data.get(0).toLowerCase());
                    } else {
                        startListening();
                    }
                } else {
                    startListening();
                }
            }

            @Override
            public void onPartialResults(Bundle bundle) {
                Log.d(TAG, "Voice onPartialResults");
            }

            @Override
            public void onEvent(int i, Bundle bundle) {
                Log.d(TAG, "Voice onEvent");
            }
        });
    }

    /**
     * Method that initiates the voice listening
     */
    public void startListening() {
        new Handler().postDelayed(() -> {
            if (speechRecognizer != null) {
                speechRecognizer.startListening(speechRecognizerIntent);
                if (listener != null) {
                    listener.onVoiceListeningStart();
                }
            }
        }, 2000);
    }

    /**
     * Method that stops listening
     */
    public void stopListening() {
        if (speechRecognizer != null) {
            speechRecognizer.stopListening();
        }
    }

    public void setLeftHandRangeVal(int leftHandRangeVal) {
        this.leftHandRangeVal = leftHandRangeVal;
    }

    public void setRightHandRangeVal(int rightHandRangeVal) {
        this.rightHandRangeVal = rightHandRangeVal;
    }

    /**
     * Method that initializes the Hand Gesture
     */
    public void initHandGesture() {
        if (!Util.isGestureDeviceConnected(mContext) || runCheck) return;
        runCheck = true;
        new Thread(() -> {
            while (runCheck) {
                        Map<String, String> map = sendCMD(1);
                        if (map != null) {
                    try {
                        final int left = Integer.parseInt(Objects.requireNonNull(map.get("leftPower")));
                        final int right = Integer.parseInt(Objects.requireNonNull(map.get("rightPower")));
                    if (left > leftHandRangeVal && right > rightHandRangeVal) {
                        index = 0;
                        if (listener != null) {
                            listener.onBothHandWave();
                            Thread.sleep(1500);
                        }
                    } else if (left > leftHandRangeVal) {
                        leftHandWave();
                    } else if (right > rightHandRangeVal) {
                        rightHandWave();
                    } else {
                        cancelWaveHandTimer();
                        if (listener != null) {
                            listener.onWaveHandReset();
                        }
                        if (gestureMEListener != null) {
                            gestureMEListener.onWaveHandReset();
                        }
                        resetWaveHandProcessed();
                    }
                } catch (Exception e) {
                    Log.d(TAG, "Gesture Error in Gesture: " + e.getMessage());
                    if (e.getMessage() == null) {
                        restartGesture();
                    }
                }
            }
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Log.e(TAG, "Exception in initHandGesture Thread sleep" + e.getMessage());
            }
        }).start();
    }

    /**
     * 1: Left and right hand energy<br>
     * 2: Read version<br>
     * 3: When the left-hand induced energy value <= the left-hand threshold, the left-hand light is on.<br>
     * 4: Right-hand induction energy value <= right-hand threshold, right-hand light is on<br>
     * 5: Left hand sensing energy value <= left hand threshold value && right hand sensing energy value <= right hand threshold value, both lights are on<br>
     * 6: When left-hand sensing energy value> left-hand threshold && right-hand sensing energy value> right-hand threshold, both left-hand lights are off<br>
     */
    public Map<String, String> sendCMD(int type) {
        openReader();
        Map<String, String> map = null;
        if (type == 1) {// Left and right hand energy
            map = sendCommandUSB(new byte[]{(byte) 0xEE, (byte) 0xEE, (byte) 0xEE, 0x66, 0x02, 0x00, 0x11, 0x01, 0x14}, 1);
        } else if (type == 2) {//  Read version
            map = sendCommandUSB(new byte[]{(byte) 0xEE, (byte) 0xEE, (byte) 0xEE, 0x66, 0x02, 0x00, 0x11, 0x02, 0x15}, 2);
        } else if (type == 3) {// Left hand light is on
            map = sendCommandUSB(new byte[]{(byte) 0xEE, (byte) 0xEE, (byte) 0xEE, 0x66, 0x03, 0x00, 0x10, 0x01, 0x02, 0x16}, 3);
        } else if (type == 4) {// Right hand light is on
            map = sendCommandUSB(new byte[]{(byte) 0xEE, (byte) 0xEE, (byte) 0xEE, 0x66, 0x03, 0x00, 0x10, 0x01, 0x01, 0x15}, 3);
        } else if (type == 5) {// Both lights are on
            map = sendCommandUSB(new byte[]{(byte) 0xEE, (byte) 0xEE, (byte) 0xEE, 0x66, 0x03, 0x00, 0x10, 0x01, 0x00, 0x14}, 3);
        } else if (type == 6) {// Both lights are off
            map = sendCommandUSB(new byte[]{(byte) 0xEE, (byte) 0xEE, (byte) 0xEE, 0x66, 0x03, 0x00, 0x10, 0x01, 0x03, 0x17}, 3);
        }
        usbReader = null;
        mUsbManager = null;
        return map;
    }

    private void openReader() {
        try {
            usbReader = null;
            mUsbManager = null;
            PendingIntent mPermissionIntent = PendingIntent.getBroadcast(mContext, 0, new Intent("com.android.example.USB_PERMISSION"), 0);
            mUsbManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
            HashMap<String, UsbDevice> deviceHashMap = mUsbManager.getDeviceList();
            Iterator<UsbDevice> iterator = deviceHashMap.values().iterator();

            while (iterator.hasNext()) {
                UsbDevice usbDevice = iterator.next();
                int pid = usbDevice.getProductId();
                int vid = usbDevice.getVendorId();
                if (pid == 0x5790 && vid == 0x0483) {
                    usbReader = usbDevice;
                    if (mUsbManager.hasPermission(usbDevice)) {
                        break;
                    } else {
                        mUsbManager.requestPermission(usbDevice, mPermissionIntent);
                    }
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "Error in openReader " + e.getMessage());
        }
    }

    private Map<String, String> sendCommandUSB(byte[] cmd, int type) {
        byte[] result = null;
        Map<String, String> resultData = null;
        try {
            if(usbReader == null) return null;
            UsbInterface usbInterface = usbReader.getInterface(0);// USBEndpoint为读写数据所需的节点
            UsbEndpoint inEndpoint = usbInterface.getEndpoint(0); // 读数据节点
            UsbEndpoint outEndpoint = usbInterface.getEndpoint(1);
            connection = null;
            connection = mUsbManager.openDevice(usbReader);
            if (connection == null) {
                return null;
            }
            connection.claimInterface(usbInterface, true);

            StringBuffer bufferCMD = new StringBuffer();
            for (int i = 0; i < cmd.length; i++) {
                bufferCMD.append(toHexString(new byte[]{cmd[i]}) + " ");
            }
            int out = connection.bulkTransfer(outEndpoint, cmd, cmd.length, 1500);
            byte[] byte2 = new byte[64];
            int ret;
            ret = connection.bulkTransfer(inEndpoint, byte2, byte2.length, 1500);
            if (ret > 0) {
                result = Arrays.copyOfRange(byte2, 0, ret);
                resultData = new HashMap<String, String>();
                StringBuffer buffer = new StringBuffer();
                for (int i = 0; i < result.length; i++) {
                    buffer.append(toHexString(new byte[]{result[i]}) + " ");
                }
                if (type == 1 && ret == 13 && result[7] == 0x01) {
                    String rightString = toHexString(new byte[]{result[9], result[8]});
                    int rightPower = Integer.valueOf(rightString, 16);
                    String leftString = toHexString(new byte[]{result[11], result[10]});
                    int leftPower = Integer.valueOf(leftString, 16);
                    resultData.put("leftPower", "" + leftPower);
                    resultData.put("rightPower", "" + rightPower);
                } else if (type == 2 && ret == 11 && result[7] == 0x02) {
                    String versionString = toHexString(new byte[]{result[9], result[8]});
                    int version = Integer.valueOf(versionString);
                    resultData.put("version", "" + version);
                }
            } else {
                return null;
            }
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultData;
    }

    private void leftHandWave() {
        Log.d(TAG, "Left Hand wave");
        if (gestureListener != null && !isCallback && !waveHandProcessed.get(LEFT_HAND)) {
            isCallback = true;
            waveHandProcessed.put(LEFT_HAND, true);
            startWaveHandTimer();
            gestureListener.onLeftHandGesture();
            return;
        }
        if (gestureMEListener != null && !isMECallback && !waveHandProcessed.get(LEFT_HAND)) {
            Log.d(TAG, "Left Hand wave");
            isMECallback = true;
            waveHandProcessed.put(LEFT_HAND, true);
            startWaveHandTimer();
            gestureMEListener.onLeftHandWave();
            return;
        }
        if (wait && !waveHandProcessed.get(LEFT_HAND)) {
            waveHandProcessed.put(LEFT_HAND, true);
            startWaveHandTimer();
            updateOnWave("Y");
        }
    }

    private void rightHandWave() {
        if (gestureListener != null && !isCallback && !waveHandProcessed.get(RIGHT_HAND)) {
            Log.d(TAG, "Right Hand wave");
            isCallback = true;
            waveHandProcessed.put(RIGHT_HAND, true);
            startWaveHandTimer();
            new IrCameraActivity().waveType = "wave";
            gestureListener.onGestureDetected();
            return;
        }
        if (gestureMEListener != null && !isMECallback && !waveHandProcessed.get(RIGHT_HAND)) {
            Log.d(TAG, "Right Hand wave");
            isMECallback = true;
            waveHandProcessed.put(RIGHT_HAND, true);
            startWaveHandTimer();
            gestureMEListener.onGestureMEDetected();
            return;
        }
        if (wait && !waveHandProcessed.get(RIGHT_HAND)) {
            Log.d(TAG, "Right Hand wave update");
            waveHandProcessed.put(RIGHT_HAND, true);
            startWaveHandTimer();
            updateOnWave("N");
        }
    }

    public void updateOnWave(String answers) {
        onQuestionAnswered(answers);
    }

    public String getQuestion() {
        String question = "";
        List<QuestionData> questionDataList = new ArrayList<>(questionAnswerMap.keySet());
        if (questionDataList.size() > 0) {
            currentQuestionData = questionDataList.get(index);
            if (currentQuestionData != null) {
                question = currentQuestionData.questionName;
            }
        }
        return question;
    }

    private void onQuestionAnswered(String answer) {
        if (currentQuestionData == null) return;
        if (!isAnExpectedAnswer(answer)) {
            isQuestionnaireFailed = true;
            if (AppSettings.isGestureExitOnNegativeOp() && listener != null) {
                cancelWaveHandTimer();
                listener.onNegativeAnswer();
                sendAnswers(true);
                return;
            }
        }
        questionAnswerMap.put(currentQuestionData, answer);
        List<QuestionData> questionDataList = new ArrayList<>(questionAnswerMap.keySet());
        if (sharedPreferences.getInt(GlobalParameters.Touchless_wave_skip, 0) == 1) {
            LogicWaveSkipDb temp = DatabaseController.getInstance().getLogicWaveSkipDb(String.valueOf(currentQuestionData.id), answer.equalsIgnoreCase("N") ? "No" : "Yes");
            if (temp != null) {
                int oldIndex = index;
                if (temp.childQuestionId.equalsIgnoreCase("Confirmation")) {
                    index = questionDataList.size();
                } else if (temp.childQuestionId.startsWith("Termination")) {
                    cancelWaveHandTimer();
                    listener.onNegativeAnswer();
                    sendAnswers(true);
                    return;
                } else {
                    for (int i = 0; i < questionDataList.size(); i++) {
                        if (temp.childQuestionId.equals(String.valueOf(questionDataList.get(i).id))) {
                            index = i;
                            break;
                        }
                    }
                    if (oldIndex == index) index++;
                }
            } else {
                index++;
            }
        } else {
            index++;
        }
        if (index >= questionDataList.size()) {
            if (listener != null) {
                cancelWaveHandTimer();
                listener.onAllQuestionsAnswered();
                sendAnswers(false);
            }
            return;
        }
        if (listener != null) {
            listener.onQuestionAnswered(answer);
        }
    }

    public static String toHexString(byte[] data) {
        if (data == null) {
            return "";
        }
        String string;
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < data.length; i++) {
            string = Integer.toHexString(data[i] & 0xFF);
            if (string.length() == 1) {
                stringBuilder.append("0");
            }

            stringBuilder.append(string.toUpperCase());
        }

        return stringBuilder.toString();
    }

    public void setTimer() {
        wait = false;
        startTimer();
        if (AppSettings.isEnableVoice()) {
            startListening();
        }
    }

    private void startTimer() {
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            public void run() {
                wait = true;
                this.cancel();
            }
        }, 1 * 1000);
    }

    private QuestionSurveyOptions getQuestionOptionOnAnswer(String answer, QuestionData questionData) {
        QuestionSurveyOptions qSurveyOption = null;
        List<QuestionSurveyOptions> qSurveyOptionList = questionData.surveyOptions;
        if (qSurveyOptionList != null) {
            for (int i = 0; i < qSurveyOptionList.size(); i++) {
                QuestionSurveyOptions qOption = qSurveyOptionList.get(i);
                if (qOption.name.charAt(0) == answer.charAt(0)) {
                    qSurveyOption = qOption;
                    break;
                }
            }
        }
        return qSurveyOption;
    }

    public void sendAnswers(boolean negativeAnswer) {
        List<QuestionSurveyOptions> qSurveyOptionList = new ArrayList<>();
        List<QuestionData> questionDataList = new ArrayList<>();
        for (Map.Entry entry : questionAnswerMap.entrySet()) {
            String answer = (String) entry.getValue();
            QuestionData questionData = (QuestionData) entry.getKey();
            QuestionSurveyOptions qSurveyOption = getQuestionOptionOnAnswer(answer, questionData);
            if (qSurveyOption != null) {
                qSurveyOptionList.add(qSurveyOption);
                questionDataList.add(questionData);
            }
        }
        /*List<QuestionSurveyOptions> qSurveyList = new ArrayList<>();
        try {
            for (int i = 0; i < gestureQuestionsDbList.size(); i++) {
                List<QuestionDataDb> questionDataDbList = gestureQuestionsDbList.get(i).questionsDbList;
                for (int k = 0; k < questionDataDbList.size(); k++) {
                    QuestionDataDb questionDataDb = questionDataDbList.get(k);
                    Gson gson = new Gson();
                    Type listType = new TypeToken<ArrayList<QuestionSurveyOptions>>() {
                    }.getType();
                    List<QuestionSurveyOptions> surveyOptionList = gson.fromJson(questionDataDb.surveyOptions, listType);
                    for (int j = 0; j < surveyOptionList.size(); j++) {
                        QuestionSurveyOptions surveyOption = surveyOptionList.get(j);
                        QuestionSurveyOptions qAnswerSurveyOption = qSurveyOptionList.get(j);
                        if (surveyOption.name.equals(qAnswerSurveyOption.name)) {
                            qSurveyList.add(surveyOption);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "Gesture Exception " + e.getMessage());
        }*/
        //Call API
        sendAnswersAPI(qSurveyOptionList, negativeAnswer, questionDataList);

    }

    private void sendAnswersAPI(List<QuestionSurveyOptions> qSurveyOptionList, boolean answerNegative, List<QuestionData> questionDataList) {
        try {
            String uniqueID = UUID.randomUUID().toString();
            JSONObject obj = new JSONObject();
            JSONArray jsonArrayCustoms = new JSONArray();

            CameraController.getInstance().setQrCodeId(uniqueID);
            obj.put("VisitId", 0);
            obj.put("anonymousGuid", uniqueID);
            obj.put("settingId", sharedPreferences.getString(GlobalParameters.Touchless_setting_id, ""));
            if (!isQuestionnaireFailed) {
                obj.put("trqStatus", "0");
            } else {
                obj.put("trqStatus", "1");
            }
            for (int i = 0; i < qSurveyOptionList.size(); i++) {
                JSONObject jsonCustomFields = new JSONObject();
                jsonCustomFields.put("questionId", qSurveyOptionList.get(i).questionId);
                jsonCustomFields.put("optionId", qSurveyOptionList.get(i).optionId);
                jsonCustomFields.put("languageCode", DeviceSettingsController.getInstance().getLanguageToUpdate());
                if (questionDataList.get(i).questionParentId != null) {
                    jsonCustomFields.put("questionParentId", questionDataList.get(i).questionParentId);
                }
                jsonArrayCustoms.put(i, jsonCustomFields);
            }
            obj.put("QuestionOptions", jsonArrayCustoms);
            new AsyncGestureAnswer(obj, this, sharedPreferences.getString(GlobalParameters.URL, EndPoints.prod_url) + EndPoints.SaveAnswer, mContext).execute();

        } catch (Exception e) {
            Logger.error(TAG, "sendReqAddDevice " + e.getMessage());
        }
    }

    @Override
    public void onJSONObjectListenerGestureAnswer(JSONObject response, String status, JSONObject req) {
        if (response == null) {
            Logger.error(TAG, "Gesture Save Answers", "send answers Log api failed");
            return;
        }
        if (response.isNull("responseCode")) {
            return;
        }
        try {
            if (response.getString("responseCode").equals("1")) {
                Log.i(TAG, "Gesture Save Answers: SUCCESS");
            }
        } catch (JSONException e) {
            Log.e(TAG, "Gesture Save Answers: " + e.getMessage());
        }
    }

    public String getAnswers() {
        return questionAnswerMap.values().toString();
    }

    public int getIndex() {
        return index;
    }

    public HashMap<QuestionData, String> getQuestionAnswerMap() {
        return questionAnswerMap;
    }

    public int getQuestionsSize() {
        Log.d(TAG, "Gesture Questions size " + questionAnswerMap.size());
        return (questionAnswerMap.size());
    }

    private void startGetQuestionsTimer() {
        cancelQuestionsTimer();
        mQuestionsTimer = new Timer();
        mQuestionsTimer.schedule(new TimerTask() {
            public void run() {
                this.cancel();
                if (listener != null) {
                    cancelWaveHandTimer();
                    listener.onQuestionsNotReceived();
                }
            }
        }, 5 * 1000);
    }

    private void cancelQuestionsTimer() {
        if (mQuestionsTimer != null) {
            mQuestionsTimer.cancel();
            mQuestionsTimer = null;
        }
    }

    private void resetQuestionAnswerMap() {
        List<QuestionData> questionDataList = new ArrayList<>(questionAnswerMap.keySet());
        for (int i = 0; i < questionDataList.size(); i++) {
            QuestionData questionData = questionDataList.get(i);
            questionAnswerMap.put(questionData, "NA");
        }
    }

//    public void getWaveSkip() {
//        try {
//            touchlessWaveSkipList.clear();
    //    String skipValues = sharedPreferences.getString(GlobalParameters.Touchless_wave_skip, "0");
//            if (skipValues != null && !skipValues.equals("0")) {
//                JSONArray jsonArraySkipValues = new JSONArray(skipValues);
//                Gson gson = new Gson();
//                for (int i = 0; i < jsonArraySkipValues.length(); i++) {
//                    TouchlessWaveSkip touchlessWaveSkip = gson.fromJson(String.valueOf(jsonArraySkipValues.getJSONObject(i)), TouchlessWaveSkip.class);
//                    touchlessWaveSkipList.add(touchlessWaveSkip);
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

//    private String TouchlessWaveLogic(String id, String answer) {
//        for (int i = 0; i < touchlessWaveSkipList.size(); i++) {
//            if (id.equals(touchlessWaveSkipList.get(i).parentQuestionId) && answer.equalsIgnoreCase(touchlessWaveSkipList.get(i).expectedOutcomeName.substring(0, 1))) {
//                return touchlessWaveSkipList.get(i).childQuestionId;
//            }
//        }
//        return "-1";
//    }

    public void clearQuestionAnswerMap() {
        questionAnswerMap.clear();
    }

    public void checkGestureStatus() {
        if (runCheck) {
            if (connection != null) {
                connection.close();
            }
            clearData();
        }
    }

    private void restartGesture() {
        Log.d(TAG, "Restart gesture ");
        runCheck = false;
        if (connection != null) {
            connection.close();
        }
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                initHandGesture();
            }
        }, 2000);
    }

    public boolean isGestureEnabledAndDeviceConnected() {
        boolean result = false;
        if (AppSettings.isEnableHandGesture() && isGestureDeviceConnected) {
            result = true;
        }
        return result;
    }

    private boolean isAnExpectedAnswer(String answer) {
        boolean result = true;
        if (currentQuestionData != null && currentQuestionData.expectedOutcome != null) {
            if (currentQuestionData.expectedOutcome.equalsIgnoreCase("None")) {
                //do noop
            } else if (!answer.equalsIgnoreCase(currentQuestionData.expectedOutcome.substring(0, 1))) {
                result = false;
            }
        }
        return result;
    }

    public boolean isQuestionnaireFailed() {
        return isQuestionnaireFailed;
    }

    private QuestionDataDb getDbQuestionData(QuestionData questionData, int index) {
        index = index + 1;
        QuestionDataDb questionDataDb = new QuestionDataDb();
        questionDataDb.primaryId = index;
        questionDataDb.id = questionData.id;
        questionDataDb.institutionId = questionData.institutionId;
        questionDataDb.questionName = questionData.questionName;
        questionDataDb.settingId = questionData.settingId;
        questionDataDb.title = questionData.title;
        questionDataDb.userId = questionData.userId;
        questionDataDb.expectedOutcome = questionData.expectedOutcome;
        questionDataDb.surveyQuestionaryDetails = questionData.surveyQuestionaryDetails;
        questionDataDb.languageCode = questionData.languageCode;
        Gson gson = new Gson();
        questionDataDb.surveyOptions = gson.toJson(questionData.surveyOptions);
        questionDataDb.questionParentId = questionData.questionParentId;
        return questionDataDb;
    }

    private LogicWaveSkipDb getDbWaveSkipData(TouchlessWaveSkip touchlessWaveSkip, int index) {
        index = index + 1;
        LogicWaveSkipDb logicWaveSkipDb = new LogicWaveSkipDb();
        logicWaveSkipDb.primaryId = index;
        logicWaveSkipDb.childQuestionId = touchlessWaveSkip.childQuestion;
        logicWaveSkipDb.parentQuestionId = touchlessWaveSkip.parentQuestion;
        logicWaveSkipDb.expectedOutcomeName = touchlessWaveSkip.expectedOutcomeName;
        // logicWaveSkipDb.expectedOutcome = touchlessWaveSkip.expecte;
        return logicWaveSkipDb;
    }

    public void initLanguageDb() {
        gestureQuestionsDbList = DatabaseController.getInstance().getGestureQuestionsListFromDb();
    }

    public void getQuestionsFromDb(String languageCode) {
        int languageId = DeviceSettingsController.getInstance().getLanguageIdOnCode(languageCode);
        List<QuestionDataDb> questionDataDbList = null;
        if (gestureQuestionsDbList != null) {
            if (gestureQuestionsDbList.size() > 0) {
                for (int j = 0; j < gestureQuestionsDbList.size(); j++) {
                    if (languageId == gestureQuestionsDbList.get(j).primaryId) {
                        questionDataDbList = gestureQuestionsDbList.get(j).questionsDbList;
                        break;
                    }
                }
                questionAnswerMap.clear();
                if (questionDataDbList != null) {
                    for (int i = 0; i < questionDataDbList.size(); i++) {
                        QuestionDataDb questionDataDb = questionDataDbList.get(i);
                        QuestionData questionData = new QuestionData();
                        questionData.id = questionDataDb.id;
                        questionData.institutionId = questionDataDb.institutionId;
                        questionData.questionName = questionDataDb.questionName;
                        questionData.title = questionDataDb.title;
                        questionData.settingId = questionDataDb.settingId;
                        questionData.userId = questionDataDb.userId;
                        questionData.expectedOutcome = questionDataDb.expectedOutcome;
                        questionData.surveyQuestionaryDetails = questionDataDb.surveyQuestionaryDetails;
                        Gson gson = new Gson();
                        Type listType = new TypeToken<ArrayList<QuestionSurveyOptions>>() {
                        }.getType();
                        questionData.surveyOptions = gson.fromJson(questionDataDb.surveyOptions, listType);
                        questionData.questionParentId = questionDataDb.questionParentId;
                        questionAnswerMap.put(questionData, "NA");
                    }
                }
            }
        }
    }

    private void resetWaveHandProcessed() {
        waveHandProcessed.clear();
        waveHandProcessed.put(LEFT_HAND, false);
        waveHandProcessed.put(RIGHT_HAND, false);
        waveHandProcessed.put(BOTH_HANDS, false);
    }

    public void startWaveHandTimer() {
        if (waveHandTimer != null) {
            waveHandTimer.cancel();
            waveHandTimer = null;
        }
        waveHandTimer = new Timer();
        waveHandTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                waveHandTimer.cancel();
                if (listener != null) {
                    listener.onWaveHandTimeout();
                }
                if (gestureMEListener != null) {
                    gestureMEListener.onWaveHandTimeout();
                }
                startWaveTimer();
            }
        }, 7 * 1000);
    }

    public void cancelWaveHandTimer() {
        if (waveHandTimer != null) {
            waveHandTimer.cancel();
            waveHandTimer = null;
        }
    }

    private void startWaveTimer() {
        startWaveHandTimer();
    }

    public boolean isGestureWithMaskEnforceEnabled() {
        return (AppSettings.isEnableHandGesture() && AppSettings.isMaskEnforced() &&
                GestureController.getInstance().isGestureEnabledAndDeviceConnected());
    }

    public void onGestureLanguageChange(String languageType) {
        DeviceSettingsController.getInstance().setLanguageToUpdate(languageType);
        setLanguageUpdated(true);
        languageSelectionIndex = 0;
        getQuestionsFromDb(languageType);
        DeviceSettingsController.getInstance().getSettingsFromDb(DeviceSettingsController.getInstance().getLanguageIdOnCode(languageType));
        clearQuestionAnswerMap();
    }

    public boolean updateNextLanguage() {
        List<LanguageData> languageDataList = DeviceSettingsController.getInstance().getLanguageDataList();
        if (languageDataList != null && !languageDataList.isEmpty()) {
            if (languageDataList.size() == 1) {
                isLanguageUpdated = true;
                return false;
            }
            if (languageDataList.size() == 2) {
                if (languageSelectionIndex >= 2) {
                    languageSelectionIndex = 0;
                }
                languageSelectionIndex++;
                DeviceSettingsController.getInstance().setLanguageToUpdate(languageDataList.get(languageSelectionIndex).languageCode);
                languageSelectionIndex++;
                isLanguageUpdated = true;
                return true;
            }
            if (languageSelectionIndex >= languageDataList.size()) {
                languageSelectionIndex = 0;
                DeviceSettingsController.getInstance().setLanguageToUpdate(AppSettings.getLanguageType());
                return true;
            }
            String currentLanguage = AppSettings.getLanguageType();
            if (currentLanguage.equals(languageDataList.get(languageSelectionIndex).languageCode)) {
                languageSelectionIndex++;
            }
            if (languageSelectionIndex < languageDataList.size()) {
                DeviceSettingsController.getInstance().setLanguageToUpdate(languageDataList.get(languageSelectionIndex).languageCode);
                languageSelectionIndex++;
            }
            return true;
        }
        return false;
    }

    public String getUpdatingLanguageName() {
        if (languageSelectionIndex == 0)
            return DeviceSettingsController.getInstance().getLanguageNameFromCode(AppSettings.getLanguageType());
        List<LanguageData> languageDataList = DeviceSettingsController.getInstance().getLanguageDataList();
        return languageDataList.get(languageSelectionIndex - 1).name;
    }

    public int getLanguageSelectionIndex() {
        return languageSelectionIndex;
    }

    public void setLanguageSelectionIndex(int value) {
        languageSelectionIndex = value;
    }

    public void clearData() {
        if (speechRecognizer != null) {
            speechRecognizer.stopListening();
            speechRecognizer.destroy();
        }
        cancelWaveHandTimer();
        listener = null;
        index = 0;
        currentQuestionData = null;
        gestureListener = null;
    }
}
