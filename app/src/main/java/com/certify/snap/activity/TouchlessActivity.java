package com.certify.snap.activity;

import android.app.Activity;
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
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.certify.callback.FlowListCallback;
import com.certify.snap.R;
import com.certify.snap.async.AsyncJSONObjectFlowList;
import com.certify.snap.async.AsyncJSONObjectGesture;
import com.certify.snap.common.EndPoints;
import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.Util;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class TouchlessActivity extends Activity implements FlowListCallback {
    public static String TAG = "TouchlessActivity";
    TextView enable_wave, wave_options, enable_wave_questions, enable_mask;
    RadioGroup radio_group_enable_wave, radio_group_enable_questions, radio_group_mask;
    Spinner spinner_question_selector;
    RadioButton radio_yes_wave, radio_no_wave, radio_yes_wave_questions, radio_no_wave_questions, radio_yes_wave_mask, radio_no_wave_mask;
    Typeface rubiklight;
    private SharedPreferences sharedPreferences;
    private HashMap<String, String> flowHashmap = new HashMap<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_touchless);
        initView();
    }

    private void initView() {
        enable_wave = findViewById(R.id.enable_wave);
        wave_options = findViewById(R.id.wave_options);
        enable_wave_questions = findViewById(R.id.enable_wave_questions);
        enable_mask = findViewById(R.id.enable_mask);
        radio_group_enable_wave = findViewById(R.id.radio_group_enable_wave);
        radio_group_enable_questions = findViewById(R.id.radio_group_enable_questions);
        radio_group_mask = findViewById(R.id.radio_group_mask);
        spinner_question_selector = findViewById(R.id.spinner_question_selector);
        radio_yes_wave = findViewById(R.id.radio_yes_wave);
        radio_no_wave = findViewById(R.id.radio_no_wave);
        radio_yes_wave_questions = findViewById(R.id.radio_yes_wave_questions);
        radio_no_wave_questions = findViewById(R.id.radio_no_wave_questions);
        radio_yes_wave_mask = findViewById(R.id.radio_yes_wave_mask);
        radio_no_wave_mask = findViewById(R.id.radio_no_wave_mask);

        rubiklight = Typeface.createFromAsset(getAssets(),
                "rubiklight.ttf");
        enable_wave.setTypeface(rubiklight);
        wave_options.setTypeface(rubiklight);
        enable_wave_questions.setTypeface(rubiklight);
        sharedPreferences = Util.getSharedPreferences(this);
        getFlowListAPI();
    }

    private void setValues() {

        ArrayList<String> values = new ArrayList<>(flowHashmap.keySet());
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,values);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_question_selector.setAdapter(adapter);

        spinner_question_selector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String name = spinner_question_selector.getSelectedItem().toString();
                String settingID = flowHashmap.get(name);
                //Toast.makeText(TouchlessActivity.this, "key"+settingID + "value"+name, Toast.LENGTH_SHORT).show();
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
