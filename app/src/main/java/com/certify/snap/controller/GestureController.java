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
import com.certify.snap.model.QuestionDataDb;
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
    }

    public interface GestureMECallbackListener {
        void onGestureMEDetected();
        void onLeftHandWave();
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

    public void getGestureQuestions(){
        getQuestionsAPI(mContext);
        startGetQuestionsTimer();
    }

    public void getQuestionsAPI(Context context) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("settingId", sharedPreferences.getString(GlobalParameters.Touchless_setting_id, ""));
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
                getQuestionsFromDb();
                if (listener != null) {
                    listener.onQuestionsReceived();
                }
                return;
            }
            questionAnswerMap.clear();
            cancelQuestionsTimer();
            Gson gson = new Gson();
            QuestionListResponse response = gson.fromJson(String.valueOf(reportInfo), QuestionListResponse.class);
            if (response.responseCode != null && response.responseCode.equals("1")) {
                List<QuestionData> questionList = response.questionList;
                if (questionList.size() > 0) {
                    clearQuestionAnswerMap();
                    DatabaseController.getInstance().deleteQuestionsFromDb();
                    for (int i = 0; i < questionList.size(); i++) {
                        QuestionData questionData = questionList.get(i);
                        questionAnswerMap.put(questionData, "NA");
                        DatabaseController.getInstance().insertQuestionsToDB(getDbQuestionData(questionData, i));
                    }
                }
                Log.d(TAG, "Gesture Questions list updated");
            } else {
                getQuestionsFromDb();
            }
            if (listener != null) {
                listener.onQuestionsReceived();
            }
        } catch (Exception e) {
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
        if (gestureMEListener != null && !isMECallback) {
            Log.d(TAG, "Right Hand wave");
            isMECallback = true;
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
        if (gestureListener != null && !isCallback) {
            Log.d(TAG, "Right Hand wave");
            isCallback = true;
            gestureListener.onGestureDetected();
            return;
        }
        if (gestureMEListener != null && !isMECallback) {
            Log.d(TAG, "Right Hand wave");
            isMECallback = true;
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

    private void updateOnWave(String answers) {
        onQuestionAnswered(answers);
    }

    public String getQuestion() {
        String question = "";
        List<QuestionData> questionDataList = new ArrayList<>(questionAnswerMap.keySet());
        currentQuestionData = questionDataList.get(index);
        if (currentQuestionData != null) {
            question = currentQuestionData.questionName;
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
        index++;
        List<QuestionData> questionDataList = new ArrayList<>(questionAnswerMap.keySet());
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
        for (Map.Entry entry : questionAnswerMap.entrySet()) {
            String answer = (String) entry.getValue();
            QuestionSurveyOptions qSurveyOption = getQuestionOptionOnAnswer(answer, (QuestionData) entry.getKey());
            if (qSurveyOption != null) {
                qSurveyOptionList.add(qSurveyOption);
            }
        }
        //Call API
        sendAnswersAPI(qSurveyOptionList, negativeAnswer);

    }

    private void sendAnswersAPI(List<QuestionSurveyOptions> qSurveyOptionList, boolean answerNegative) {
        try {
            String uniqueID = UUID.randomUUID().toString();
            JSONObject obj = new JSONObject();
            JSONArray jsonArrayCustoms = new JSONArray();

            CameraController.getInstance().setQrCodeId(uniqueID);
            obj.put("VisitId", 0);
            obj.put("anonymousGuid", uniqueID);
            obj.put("settingId", sharedPreferences.getString(GlobalParameters.Touchless_setting_id,""));

            for(int i=0;i<qSurveyOptionList.size();i++) {
                JSONObject jsonCustomFields = new JSONObject();
                jsonCustomFields.put("questionId", qSurveyOptionList.get(i).questionId);
                jsonCustomFields.put("optionId", qSurveyOptionList.get(i).optionId);
                jsonArrayCustoms.put(i, jsonCustomFields);
            }
            obj.put("QuestionOptions",jsonArrayCustoms);


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
            Log.e(TAG, "Gesture Save Answers: "+ e.getMessage() );
        }
    }

    public String getAnswers(){
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
        if (currentQuestionData != null && currentQuestionData.expectedOutcome != null){
            if (!answer.equalsIgnoreCase(currentQuestionData.expectedOutcome.substring(0, 1))) {
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
        Gson gson = new Gson();
        questionDataDb.surveyOptions = gson.toJson(questionData.surveyOptions);
        return questionDataDb;
    }

    public void getQuestionsFromDb () {
        List<QuestionDataDb> questionDataDbList = DatabaseController.getInstance().getQuestionsFromDb();
        if (questionDataDbList.size() > 0) {
            questionAnswerMap.clear();
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
                questionAnswerMap.put(questionData, "NA");
            }
        }
    }

    private void resetWaveHandProcessed() {
        waveHandProcessed.clear();
        waveHandProcessed.put(LEFT_HAND, false);
        waveHandProcessed.put(RIGHT_HAND, false);
        waveHandProcessed.put(BOTH_HANDS, false);
    }

    private void startWaveHandTimer() {
        cancelWaveHandTimer();
        waveHandTimer = new Timer();
        waveHandTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                waveHandTimer.cancel();
                if (listener != null) {
                    listener.onWaveHandTimeout();
                }
                startWaveTimer();
            }
        }, 7 * 1000);
    }

    private void cancelWaveHandTimer() {
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

    public void clearData() {
        if (speechRecognizer != null) {
            speechRecognizer.stopListening();
            speechRecognizer.destroy();
        }
        listener = null;
        index = 0;
        currentQuestionData = null;
        gestureListener = null;
    }
}
