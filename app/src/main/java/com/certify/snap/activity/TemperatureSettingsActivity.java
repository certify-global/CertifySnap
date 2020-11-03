package com.certify.snap.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import androidx.annotation.Nullable;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.certify.snap.R;
import com.certify.snap.common.Constants;
import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.Util;
import com.certify.snap.controller.CameraController;

public class TemperatureSettingsActivity extends SettingsBaseActivity {
    EditText editTextDialogUserInput,editTextcompensation, editTextTemperatureThreshold;
    SharedPreferences sp;
    TextView btn_save, temperatureThresholdText;
    Typeface rubiklight;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temperature);
        editTextDialogUserInput = findViewById(R.id.editTextDialogUserInput);
        editTextcompensation = findViewById(R.id.editTextcompensation);
        editTextTemperatureThreshold = findViewById(R.id.edit_text_temperature_threshold);
        btn_save = findViewById(R.id.btn_save);
        temperatureThresholdText = findViewById(R.id.temperature_threshold_text);
        rubiklight = Typeface.createFromAsset(getAssets(),
                "rubiklight.ttf");
        temperatureThresholdText.setTypeface(rubiklight);

        sp = Util.getSharedPreferences(this);
        RadioGroup radio_group_f_c = findViewById(R.id.radio_group_f_c);
        final RadioButton radio_f = findViewById(R.id.radio_f);
        RadioButton radio_c = findViewById(R.id.radio_c);

        editTextDialogUserInput.setText(sp.getString(GlobalParameters.TEMP_TEST, "100.4"));
        editTextcompensation.setText((String.valueOf(Util.getSharedPreferences(this).getFloat(GlobalParameters.COMPENSATION,0))));
        editTextTemperatureThreshold.setText((String.valueOf(Util.getSharedPreferences(this).getFloat(GlobalParameters.DISPLAY_TEMP_THRESHOLD, (float) 94.9))));
        String regex="^-?[0-9]\\d*(\\.\\d+)?$";

        checkTemperatureThreshold();
        editTextcompensation.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                editTextcompensation.setError(null);
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(editTextcompensation.getText().toString().matches(regex)){
                    editTextcompensation.setError(null);
                    btn_save.setEnabled(true);
                }else{
                    editTextcompensation.setError("Please enter a valid number.See below example");
                    btn_save.setEnabled(false);
                }
            }
        });

        if (sp.getString(GlobalParameters.F_TO_C, "F").equals("F")) {
            radio_f.setChecked(true);
        } else radio_c.setChecked(true);

        radio_group_f_c.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radio_f) Util.writeString(sp, GlobalParameters.F_TO_C, "F");
                else Util.writeString(sp, GlobalParameters.F_TO_C, "C");

            }
        });

        editTextTemperatureThreshold.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                editTextTemperatureThreshold.setError(null);
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(editTextTemperatureThreshold.getText().toString().matches(regex)){
                    editTextTemperatureThreshold.setError(null);
                    btn_save.setEnabled(true);
                }else{
                    editTextTemperatureThreshold.setError("Please enter a valid number.See below example");
                    btn_save.setEnabled(false);
                }
            }
        });

        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.showToast(TemperatureSettingsActivity.this, getString(R.string.save_success));
                Util.writeString(sp, GlobalParameters.TEMP_TEST, editTextDialogUserInput.getText().toString().trim());
                Util.writeFloat(sp, GlobalParameters.COMPENSATION,Float.parseFloat(editTextcompensation.getText().toString().trim()));
                Util.writeFloat(sp, GlobalParameters.DISPLAY_TEMP_THRESHOLD,Float.parseFloat(editTextTemperatureThreshold.getText().toString().trim()));
                finish();
            }
        });

    }

    private void checkTemperatureThreshold(){
        RadioGroup radio_group_temp_threshold = findViewById(R.id.temperature_threshold_radio_group);
        RadioButton radio_yes_temp_threshold = findViewById(R.id.temperature_threshold_radio_yes);
        RadioButton radio_no_temp_threshold = findViewById(R.id.temperature_threshold_radio_no);

        if(sp.getBoolean(GlobalParameters.TEMPERATURE_THRESHOLD,false))
            radio_yes_temp_threshold.setChecked(true);
        else radio_no_temp_threshold.setChecked(true);

        radio_group_temp_threshold.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId==R.id.temperature_threshold_radio_yes)
                    Util.writeBoolean(sp, GlobalParameters.TEMPERATURE_THRESHOLD, true);
                else Util.writeBoolean(sp, GlobalParameters.TEMPERATURE_THRESHOLD, false);
            }
        });
    }

    public void temperatureCalibration(View view){
        int mode = CameraController.getInstance().getDeviceMode();
        if(mode == Constants.PRO_MODEL_TEMPERATURE_MODULE_1 || mode == Constants.PRO_MODEL_TEMPERATURE_MODULE_2){
            Intent tempIntent = new Intent(this, TemperatureCalibrationGuideActivity.class);
            startActivity(tempIntent);
        }else{
            Intent tempIntent = new Intent(this, TemperatureCalibrationActivity.class);
            startActivity(tempIntent);
        }
    }
}
