package com.certify.snap.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.certify.callback.FlowListCallback;
import com.certify.snap.R;
import com.certify.snap.api.response.TouchlessSettings;
import com.certify.snap.async.AsyncJSONObjectFlowList;
import com.certify.snap.common.AppSettings;
import com.certify.snap.common.EndPoints;
import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.Util;
import com.certify.snap.controller.DatabaseController;
import com.certify.snap.controller.DeviceSettingsController;
import com.certify.snap.controller.GestureController;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class TouchlessSettingsActivity extends SettingsBaseActivity implements FlowListCallback {
    public static String TAG = "GestureActivity";
    TextView enableWave, waveOptions, enableWaveQuestions, enableMask, enableVoiceRecognition, enable_progress_bar, btn_save, tvWaveImage, gestureExitTv;
    RadioGroup radioGroupWave, radioGroupQuestions, radioGroupMask, radioGroupVoice, radio_group_progress, radioGroupWaveImage, gestureExitRg;
    Spinner spinnerQuestionSelector;
    RadioButton radioYesWave, radioNoWave, radioYesWaveQuestions, radioNoWaveQuestions, radioYesMask, radioNoMask,
            radioYesVoice, radioNoVoice, radio_yes_progress, radio_no_progress, radioYesWaveImage, radioNoWaveImage, gestureExitRbYes, gestureExitRbNo;
    Typeface rubikLight;
    private SharedPreferences sharedPreferences;
    private HashMap<String, String> flowHashmap = new HashMap<>();
    private HashMap<String, String> waveSkipHashmap = new HashMap<>();
    private EditText editTextWaveFooter, editTextMaskEnforce, editGestureExitMsg;
    private String gestureWorkFlow = "";
    private TextInputLayout maskEditLayout, gestureExitLayout;
    private TouchlessSettings touchlessSettingsDb;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_touchless_settings);
        initView();
        waveCheck();
        waveQuestionCheck();
        maskEnforcementCheck();
        voiceRecognitionCheck();
        progressbarCheck();
        waveImageCheck();
        gestureExitCheck();

        getTouchlessSettingsFromDb();
    }

    private void initView() {
        enableWave = findViewById(R.id.enable_wave);
        waveOptions = findViewById(R.id.wave_options);
        enableWaveQuestions = findViewById(R.id.enable_wave_questions);
        enableMask = findViewById(R.id.enable_mask_enforcement);
        enableVoiceRecognition = findViewById(R.id.enable_voice_recognition);
        spinnerQuestionSelector = findViewById(R.id.spinner_question_selector);
        gestureExitTv = findViewById(R.id.gesture_exit_op);
        editGestureExitMsg = findViewById(R.id.gesture_exit_text);
        maskEditLayout = findViewById(R.id.text_input_mask_enforce);
        gestureExitLayout = findViewById(R.id.gesture_exit_text_input);

        radioGroupWave = findViewById(R.id.radio_group_wave);
        radioYesWave = findViewById(R.id.radio_yes_wave);
        radioNoWave = findViewById(R.id.radio_no_wave);

        radioGroupQuestions = findViewById(R.id.radio_group_enable_questions);
        radioYesWaveQuestions = findViewById(R.id.radio_yes_wave_questions);
        radioNoWaveQuestions = findViewById(R.id.radio_no_wave_questions);

        radioGroupMask = findViewById(R.id.radio_group_mask_enforcement);
        radioYesMask = findViewById(R.id.radio_yes_mask_enforcement);
        radioNoMask = findViewById(R.id.radio_no_mask_enforcement);

        gestureExitRg = findViewById(R.id.gesture_exit_rg);
        gestureExitRbYes = findViewById(R.id.gesture_exit_rb_yes);
        gestureExitRbNo = findViewById(R.id.gesture_exit_rb_no);

        radioGroupVoice = findViewById(R.id.radio_group_voice_recognition);
        radioYesVoice = findViewById(R.id.radio_yes_voice_recognition);
        radioNoVoice = findViewById(R.id.radio_no_voice_recognition);
        enable_progress_bar = findViewById(R.id.enable_progress_bar);
        radio_group_progress = findViewById(R.id.radio_group_progress);
        radio_yes_progress = findViewById(R.id.radio_yes_progress);
        radio_no_progress = findViewById(R.id.radio_no_progress);
        btn_save = findViewById(R.id.btn_exit);
        editTextWaveFooter = findViewById(R.id.editTextWaveFooter);
        editTextMaskEnforce = findViewById(R.id.editTextMaskEnforce);
        radioYesWaveImage = findViewById(R.id.radio_yes_wave_image);
        radioNoWaveImage = findViewById(R.id.radio_no_wave_image);
        tvWaveImage = findViewById(R.id.tv_wave_image);
        radioGroupWaveImage = findViewById(R.id.radio_group_wave_image);

        rubikLight = Typeface.createFromAsset(getAssets(),
                "rubiklight.ttf");
        enableWave.setTypeface(rubikLight);
        waveOptions.setTypeface(rubikLight);
        enableWaveQuestions.setTypeface(rubikLight);
        enableVoiceRecognition.setTypeface(rubikLight);
        enableMask.setTypeface(rubikLight);
        enable_progress_bar.setTypeface(rubikLight);
        tvWaveImage.setTypeface(rubikLight);
        gestureExitTv.setTypeface(rubikLight);

        sharedPreferences = Util.getSharedPreferences(this);
        gestureWorkFlow = AppSettings.getGestureWorkFlow();
        getFlowListAPI();

        editTextWaveFooter.setText(sharedPreferences.getString(GlobalParameters.WAVE_INDICATOR, getResources().getString(R.string.bottom_text)));
        editTextMaskEnforce.setText(sharedPreferences.getString(GlobalParameters.MASK_ENFORCE_INDICATOR, getResources().getString(R.string.wear_a_mask)));
        editGestureExitMsg.setText(sharedPreferences.getString(GlobalParameters.GESTURE_EXIT_CONFIRM_TEXT, getString(R.string.gesture_exit_default)));

        btn_save.setOnClickListener(v -> {
            if (!gestureWorkFlow.isEmpty() && !gestureWorkFlow.equals(sharedPreferences.getString(GlobalParameters.Touchless_setting_id, ""))) {
                GestureController.getInstance().clearQuestionAnswerMap();
            }
            Util.writeString(sharedPreferences, GlobalParameters.WAVE_INDICATOR, editTextWaveFooter.getText().toString());
            Util.writeString(sharedPreferences, GlobalParameters.MASK_ENFORCE_INDICATOR, editTextMaskEnforce.getText().toString());
            Util.writeString(sharedPreferences, GlobalParameters.GESTURE_EXIT_CONFIRM_TEXT, editGestureExitMsg.getText().toString());

            if (touchlessSettingsDb != null) {
                touchlessSettingsDb.waveIndicatorInstructions = editTextWaveFooter.getText().toString();
                touchlessSettingsDb.maskEnforceText = editTextMaskEnforce.getText().toString();
                touchlessSettingsDb.messageForNegativeOutcome = editGestureExitMsg.getText().toString();
            }

            finish();
        });
    }

    private void waveCheck() {
        if (sharedPreferences.getBoolean(GlobalParameters.HAND_GESTURE, false))
            radioYesWave.setChecked(true);
        else radioNoWave.setChecked(true);

        radioGroupWave.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radio_yes_wave)
                Util.writeBoolean(sharedPreferences, GlobalParameters.HAND_GESTURE, true);
            else Util.writeBoolean(sharedPreferences, GlobalParameters.HAND_GESTURE, false);
        });
    }

    private void waveQuestionCheck() {
        if (sharedPreferences.getBoolean(GlobalParameters.WAVE_QUESTIONS, false))
            radioYesWaveQuestions.setChecked(true);
        else radioNoWaveQuestions.setChecked(true);

        radioGroupQuestions.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radio_yes_wave_questions)
                Util.writeBoolean(sharedPreferences, GlobalParameters.WAVE_QUESTIONS, true);
            else Util.writeBoolean(sharedPreferences, GlobalParameters.WAVE_QUESTIONS, false);
        });
    }

    private void maskEnforcementCheck() {
        if (sharedPreferences.getBoolean(GlobalParameters.MASK_ENFORCEMENT, false)) {
            radioYesMask.setChecked(true);
            maskEditLayout.setVisibility(View.VISIBLE);
        } else {
            radioNoMask.setChecked(true);
            maskEditLayout.setVisibility(View.GONE);
        }

        radioGroupMask.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radio_yes_mask_enforcement) {
                Util.writeBoolean(sharedPreferences, GlobalParameters.MASK_ENFORCEMENT, true);
                maskEditLayout.setVisibility(View.VISIBLE);
            } else {
                Util.writeBoolean(sharedPreferences, GlobalParameters.MASK_ENFORCEMENT, false);
                maskEditLayout.setVisibility(View.GONE);
            }
        });
    }

    private void voiceRecognitionCheck() {

        if (sharedPreferences.getBoolean(GlobalParameters.VISUAL_RECOGNITION, false))
            radioYesVoice.setChecked(true);
        else radioNoVoice.setChecked(true);

        radioGroupVoice.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radio_yes_voice_recognition)
                Util.writeBoolean(sharedPreferences, GlobalParameters.VISUAL_RECOGNITION, true);
            else Util.writeBoolean(sharedPreferences, GlobalParameters.VISUAL_RECOGNITION, false);
        });
    }

    private void progressbarCheck() {
        if (sharedPreferences.getBoolean(GlobalParameters.PROGRESS_BAR, false))
            radio_yes_progress.setChecked(true);
        else radio_no_progress.setChecked(true);

        radio_group_progress.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radio_yes_progress)
                Util.writeBoolean(sharedPreferences, GlobalParameters.PROGRESS_BAR, true);
            else Util.writeBoolean(sharedPreferences, GlobalParameters.PROGRESS_BAR, false);
        });
    }

    private void waveImageCheck() {
        if (sharedPreferences.getBoolean(GlobalParameters.WAVE_IMAGE, false))
            radioYesWaveImage.setChecked(true);
        else radioNoWaveImage.setChecked(true);

        radioGroupWaveImage.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radio_yes_wave_image)
                    Util.writeBoolean(sharedPreferences, GlobalParameters.WAVE_IMAGE, true);
                else Util.writeBoolean(sharedPreferences, GlobalParameters.WAVE_IMAGE, false);
            }
        });
    }

    private void gestureExitCheck() {
        if (sharedPreferences.getBoolean(GlobalParameters.GESTURE_EXIT_NEGATIVE_OP, false)) {
            gestureExitRbYes.setChecked(true);
            gestureExitLayout.setVisibility(View.VISIBLE);
        } else {
            gestureExitRbNo.setChecked(true);
            gestureExitLayout.setVisibility(View.GONE);
        }

        gestureExitRg.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.gesture_exit_rb_yes) {
                Util.writeBoolean(sharedPreferences, GlobalParameters.GESTURE_EXIT_NEGATIVE_OP, true);
                gestureExitLayout.setVisibility(View.VISIBLE);
            } else {
                Util.writeBoolean(sharedPreferences, GlobalParameters.GESTURE_EXIT_NEGATIVE_OP, false);
                gestureExitLayout.setVisibility(View.GONE);
            }
        });
    }


    private void setValues() {
        ArrayList<String> values = new ArrayList<>(flowHashmap.keySet());
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, values);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerQuestionSelector.setAdapter(adapter);

        spinnerQuestionSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String name = spinnerQuestionSelector.getSelectedItem().toString();
                String settingID = flowHashmap.get(name);

                Util.writeString(sharedPreferences, GlobalParameters.Touchless_setting_id, settingID);
                if (waveSkipHashmap.get(settingID) == null)
                    Util.writeString(sharedPreferences, GlobalParameters.Touchless_wave_skip, "0");
                else
                    Util.writeString(sharedPreferences, GlobalParameters.Touchless_wave_skip, waveSkipHashmap.get(settingID));

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        String selectedID = sharedPreferences.getString(GlobalParameters.Touchless_setting_id, "");
        String value = Util.getKeyFromValue(flowHashmap, selectedID);
        spinnerQuestionSelector.setSelection(((ArrayAdapter<String>) spinnerQuestionSelector.getAdapter()).getPosition(value));
        Log.d("CertifyXT gesture", value);

    }

    public void getFlowListAPI() {
        try {
            JSONObject obj = new JSONObject();
            new AsyncJSONObjectFlowList(obj, this, sharedPreferences.getString(GlobalParameters.URL, EndPoints.prod_url) + EndPoints.GetWorkflowList, this).execute();

        } catch (Exception e) {
            Log.d(TAG, "getFlowListAPI" + e.getMessage());
        }
    }

    public void onParamterback(View view) {
        startActivity(new Intent(this, SettingsActivity.class));
        finish();
    }


    @Override
    public void onJSONObjectListenerFlowList(JSONObject report, String status, JSONObject req) {
        try {
            if (report == null) return;
            flowHashmap.clear();
            waveSkipHashmap.clear();
            if (report.getInt("responseCode") == 1) {
                JSONArray jsonArray = report.getJSONArray("responseData");
                flowHashmap.put("Choose Questionnaire", "header");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String id = jsonObject.getString("id");
                    String settingName = jsonObject.getString("settingName");
                    int statusVal = jsonObject.getInt("status");
                    int enableLogic = jsonObject.isNull("enableLogic") ? 0 : jsonObject.getInt("enableLogic");
                    if (statusVal == 1) {
                        flowHashmap.put(settingName, id);
                        Log.d("CertifyXT settingID", id);

                    }
                    if (enableLogic == 1) {
                        JSONArray logicJsonObject = jsonObject.isNull("logicJsonObject") ? new JSONArray() : jsonObject.getJSONArray("logicJsonObject");
                        waveSkipHashmap.put(id, logicJsonObject.toString());
                    }
                }
                setValues();

            }


        } catch (Exception e) {
            Log.d(TAG, "onJSONObjectListenerFlowList" + e.getMessage());
        }

    }

    private void getTouchlessSettingsFromDb() {
        String languageType = AppSettings.getLanguageType();
        touchlessSettingsDb = DatabaseController.getInstance().getTouchlessSettingsOnId(
                DeviceSettingsController.getInstance().getLanguageIdOnCode(languageType));

    }
}
