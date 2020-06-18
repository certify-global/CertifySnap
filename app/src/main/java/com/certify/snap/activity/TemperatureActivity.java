package com.certify.snap.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TemperatureActivity extends Activity {
    EditText editTextDialogUserInput,editTextcompensation;
    SharedPreferences sp;
    TextView btn_save;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temperature);
        editTextDialogUserInput = findViewById(R.id.editTextDialogUserInput);
        editTextcompensation = findViewById(R.id.editTextcompensation);
        btn_save = findViewById(R.id.btn_save);
        sp = Util.getSharedPreferences(this);
        RadioGroup radio_group_f_c = findViewById(R.id.radio_group_f_c);
        final RadioButton radio_f = findViewById(R.id.radio_f);
        RadioButton radio_c = findViewById(R.id.radio_c);

        editTextDialogUserInput.setText(sp.getString(GlobalParameters.TEMP_TEST, "100.4"));
        editTextcompensation.setText((String.valueOf(Util.getSharedPreferences(this).getFloat(GlobalParameters.COMPENSATION,0))));
        String regex="^-?[0-9]\\d*(\\.\\d+)?$";


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

        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.showToast(TemperatureActivity.this, getString(R.string.save_success));
                Util.writeString(sp, GlobalParameters.TEMP_TEST, editTextDialogUserInput.getText().toString().trim());
                Util.writeFloat(sp, GlobalParameters.COMPENSATION,Float.parseFloat(editTextcompensation.getText().toString().trim()));
                finish();
            }
        });


    }

    public void onParamterback(View view) {
        startActivity(new Intent(TemperatureActivity.this, SettingActivity.class));
        finish();
    }
}
