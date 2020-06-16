package com.certify.snap.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.google.android.material.textfield.TextInputLayout;

import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import com.certify.snap.R;
import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.Util;

public class ThermalSetting extends Activity {
    TextInputLayout text_input_title, text_input_subtitle;
    EditText edittext_subtitle, edittext_title, etDisplayTime, etOnlyText;
    SharedPreferences sp;
    TextView btn_save, tv_time_home;
    Typeface rubiklight;
    TextView tv_welcome, titles;
    CheckBox cbHomeText, cbTextOnly;
    String deffat ="if your answer to all 3covid questions is no please tap your badge  at the bottom of the device for a thermal Scan.and report toif your answer to all 3covid questions is no please tap your badge  at the bottom of the device for a thermal Scan.and report to" +
            "if your answer to all 3covid questions is no please tap your badge  at the bottom of the device for a thermal Scan.and report toif your answer to all 3covid questions is no please tap your badge  at the bottom of the device for ";//+
//               "if your answer to all 3covid questions is no please tap your badge  at the bottom of the device for a thermal Scan.and report toif your answer to all 3covid questions is no please tap your badge  at the bottom of the device for "+
//            "if your answer to all 3covid questions is no please tap your badge  at the bottom of the device for a thermal Scan.and report toif your answer to all 3covid questions is no please tap your badge  at the bottom of the device for ";
    String title = "";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            setContentView(R.layout.activity_thermal_setting);
            rubiklight = Typeface.createFromAsset(getAssets(),
                    "rubiklight.ttf");
            text_input_title = findViewById(R.id.text_input_title);
            text_input_subtitle = findViewById(R.id.text_input_subtitle);
            edittext_title = findViewById(R.id.edittext_title);
            edittext_subtitle = findViewById(R.id.edittext_subtitle);
            etDisplayTime = findViewById(R.id.et_display_home_time);
            etOnlyText = findViewById(R.id.et_text_only);
            tv_time_home = findViewById(R.id.tv_time_home);
            cbHomeText = findViewById(R.id.cb_enable_home);
            cbTextOnly = findViewById(R.id.cb_enable_text_only);
            btn_save = findViewById(R.id.btn_exit);
            tv_welcome = findViewById(R.id.tv_welcome);
            titles = findViewById(R.id.titles);
            tv_welcome.setTypeface(rubiklight);
            titles.setTypeface(rubiklight);
            btn_save.setTypeface(rubiklight);
            tv_time_home.setTypeface(rubiklight);
            cbTextOnly.setTypeface(rubiklight);
            cbHomeText.setTypeface(rubiklight);
            sp = Util.getSharedPreferences(this);

            edittext_title.setText(sp.getString(GlobalParameters.Thermalscan_title, "THERMAL SCAN"));
            edittext_subtitle.setText(sp.getString(GlobalParameters.Thermalscan_subtitle, ""));
            etDisplayTime.setText("" + sp.getInt(GlobalParameters.HOME_DISPLAY_TIME, 2));
            etOnlyText.setText(sp.getString(GlobalParameters.HOME_TEXT_ONLY_MESSAGE, ""));
            etOnlyText.setText(deffat+deffat+deffat);
            cbHomeText.setChecked(sp.getBoolean(GlobalParameters.HOME_TEXT_IS_ENABLE, false));
            cbTextOnly.setChecked(sp.getBoolean(GlobalParameters.HOME_TEXT_ONLY_IS_ENABLE, false));

            btn_save.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!edittext_title.getText().toString().isEmpty())
                        Util.writeString(sp, GlobalParameters.Thermalscan_title, edittext_title.getText().toString());
                    if (!edittext_subtitle.getText().toString().isEmpty())
                        Util.writeString(sp, GlobalParameters.Thermalscan_subtitle, edittext_subtitle.getText().toString());
                    if (!etDisplayTime.getText().toString().isEmpty())
                        Util.writeInt(sp, GlobalParameters.HOME_DISPLAY_TIME, Integer.parseInt(etDisplayTime.getText().toString()));
                    if (!etOnlyText.getText().toString().isEmpty())
                        Util.writeString(sp, GlobalParameters.HOME_TEXT_ONLY_MESSAGE, etOnlyText.getText().toString());
                    Util.showToast(ThermalSetting.this, getString(R.string.save_success));
                    finish();
                }
            });

            cbHomeText.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Util.writeBoolean(sp, GlobalParameters.HOME_TEXT_IS_ENABLE, isChecked);
                }
            });
            cbTextOnly.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Util.writeBoolean(sp, GlobalParameters.HOME_TEXT_ONLY_IS_ENABLE, isChecked);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void onParamterback(View view) {
        startActivity(new Intent(ThermalSetting.this, SettingActivity.class));
        finish();
    }
}
