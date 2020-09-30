package com.certify.snap.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.certify.callback.FlowListCallback;
import com.certify.snap.R;
import com.certify.snap.async.AsyncJSONObjectFlowList;
import com.certify.snap.common.EndPoints;
import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.Util;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class GestureActivity extends SettingBaseActivity implements FlowListCallback {
    public static String TAG = "GestureActivity";
    TextView enableWave, waveOptions, enableWaveQuestions, enableMask, enableVoiceRecognition;
    RadioGroup radioGroupWave, radioGroupQuestions, radioGroupMask, radioGroupVoice;
    Spinner spinnerQuestionSelector;
    RadioButton radioYesWave, radioNoWave, radioYesWaveQuestions, radioNoWaveQuestions, radioYesMask, radioNoMask,
            radioYesVoice, radioNoVoice;
    Typeface rubikLight;
    private SharedPreferences sharedPreferences;
    private HashMap<String, String> flowHashmap = new HashMap<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_touchless);
        initView();
        waveCheck();
        waveQuestionCheck();
        maskEnforcementCheck();
        voiceRecognitionCheck();
    }

    private void initView() {
        enableWave = findViewById(R.id.enable_wave);
        waveOptions = findViewById(R.id.wave_options);
        enableWaveQuestions = findViewById(R.id.enable_wave_questions);
        enableMask = findViewById(R.id.enable_mask_enforcement);
        enableVoiceRecognition = findViewById(R.id.enable_voice_recognition);
        spinnerQuestionSelector = findViewById(R.id.spinner_question_selector);

        radioGroupWave = findViewById(R.id.radio_group_wave);
        radioYesWave = findViewById(R.id.radio_yes_wave);
        radioNoWave = findViewById(R.id.radio_no_wave);

        radioGroupQuestions = findViewById(R.id.radio_group_enable_questions);
        radioYesWaveQuestions = findViewById(R.id.radio_yes_wave_questions);
        radioNoWaveQuestions = findViewById(R.id.radio_no_wave_questions);

        radioGroupMask = findViewById(R.id.radio_group_mask_enforcement);
        radioYesMask = findViewById(R.id.radio_yes_mask_enforcement);
        radioNoMask = findViewById(R.id.radio_no_mask_enforcement);

        radioGroupVoice = findViewById(R.id.radio_group_voice_recognition);
        radioYesVoice = findViewById(R.id.radio_yes_voice_recognition);
        radioNoVoice = findViewById(R.id.radio_no_voice_recognition);

        rubikLight = Typeface.createFromAsset(getAssets(),
                "rubiklight.ttf");
        enableWave.setTypeface(rubikLight);
        waveOptions.setTypeface(rubikLight);
        enableWaveQuestions.setTypeface(rubikLight);
        enableVoiceRecognition.setTypeface(rubikLight);
        enableMask.setTypeface(rubikLight);
        sharedPreferences = Util.getSharedPreferences(this);
        getFlowListAPI();
    }

    private void waveCheck(){
        if(sharedPreferences.getBoolean(GlobalParameters.HAND_GESTURE,false))
            radioYesWave.setChecked(true);
        else radioNoWave.setChecked(true);

        radioGroupWave.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId==R.id.radio_yes_wave)
                    Util.writeBoolean(sharedPreferences, GlobalParameters.HAND_GESTURE, true);
                else Util.writeBoolean(sharedPreferences, GlobalParameters.HAND_GESTURE, false);
            }
        });
    }

    private void waveQuestionCheck(){
        if(sharedPreferences.getBoolean(GlobalParameters.WAVE_QUESTIONS,false))
            radioYesWaveQuestions.setChecked(true);
        else radioNoWaveQuestions.setChecked(true);

        radioGroupQuestions.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId==R.id.radio_yes_wave_questions)
                    Util.writeBoolean(sharedPreferences, GlobalParameters.WAVE_QUESTIONS, true);
                else Util.writeBoolean(sharedPreferences, GlobalParameters.WAVE_QUESTIONS, false);
            }
        });
    }

    private void maskEnforcementCheck(){

        if(sharedPreferences.getBoolean(GlobalParameters.MASK_ENFORCEMENT,false))
            radioYesMask.setChecked(true);
        else radioNoMask.setChecked(true);

        radioGroupMask.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId==R.id.radio_yes_mask_enforcement)
                    Util.writeBoolean(sharedPreferences, GlobalParameters.MASK_ENFORCEMENT, true);
                else Util.writeBoolean(sharedPreferences, GlobalParameters.MASK_ENFORCEMENT, false);
            }
        });
    }

    private void voiceRecognitionCheck(){

        if(sharedPreferences.getBoolean(GlobalParameters.VISUAL_RECOGNITION,false))
            radioYesVoice.setChecked(true);
        else radioNoVoice.setChecked(true);

        radioGroupVoice.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId==R.id.radio_yes_voice_recognition)
                    Util.writeBoolean(sharedPreferences, GlobalParameters.VISUAL_RECOGNITION, true);
                else Util.writeBoolean(sharedPreferences, GlobalParameters.VISUAL_RECOGNITION, false);
            }
        });
    }


    private void setValues() {

        ArrayList<String> values = new ArrayList<>(flowHashmap.keySet());
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,values);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerQuestionSelector.setAdapter(adapter);

        spinnerQuestionSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String name = spinnerQuestionSelector.getSelectedItem().toString();
                String settingID = flowHashmap.get(name);

                Util.writeString(sharedPreferences,GlobalParameters.Touchless_setting_id,settingID);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
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
        startActivity(new Intent(this, SettingActivity.class));
        finish();
    }


    @Override
    public void onJSONObjectListenerFlowList(JSONObject report, String status, JSONObject req) {
        try {
            if (report == null) return;
            flowHashmap.clear();

            if (report.getInt("responseCode") == 1) {
                JSONArray jsonArray = report.getJSONArray("responseData");
                 flowHashmap.put("Choose Questionnaire","header");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String id = jsonObject.getString("id");
                    String settingName = jsonObject.getString("settingName");
                    int statusVal = jsonObject.getInt("status");
                    if (statusVal == 1) {
                        flowHashmap.put(settingName, id);
                        Log.d("CertifyXT settingID",id);
                        setValues();

                    }
                }
            }


        } catch (Exception e) {
            Log.d(TAG, "onJSONObjectListenerFlowList" + e.getMessage());
        }

    }
}
