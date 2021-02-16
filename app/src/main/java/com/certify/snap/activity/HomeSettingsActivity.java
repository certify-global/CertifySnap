package com.certify.snap.activity;

import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.certify.snap.api.response.HomePageSettings;
import com.certify.snap.common.AppSettings;
import com.certify.snap.controller.DatabaseController;
import com.certify.snap.controller.DeviceSettingsController;
import com.google.android.material.textfield.TextInputLayout;

import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import com.certify.snap.R;
import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.Util;

public class HomeSettingsActivity extends SettingsBaseActivity {
    TextInputLayout text_input_title, text_input_subtitle;
    EditText edittext_subtitle, edittext_title, etDisplayTime, etOnlyText;
    SharedPreferences sp;
    TextView btn_save, tv_time_home;
    Typeface rubiklight;
    TextView tv_welcome, titles;
    CheckBox cbHomeText, cbTextOnly;
    private HomePageSettings homePageSettingsDb;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            setContentView(R.layout.activity_home_settings);
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

            edittext_title.setText(sp.getString(GlobalParameters.Thermalscan_title, getString(R.string.thermal_scan)));
            edittext_subtitle.setText(sp.getString(GlobalParameters.Thermalscan_subtitle, ""));
            etDisplayTime.setText("" + sp.getInt(GlobalParameters.HOME_DISPLAY_TIME, 2));
            etOnlyText.setText(sp.getString(GlobalParameters.HOME_TEXT_ONLY_MESSAGE, ""));
            cbHomeText.setChecked(sp.getBoolean(GlobalParameters.HOME_TEXT_IS_ENABLE, true));
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
                    Util.showToast(HomeSettingsActivity.this, getString(R.string.save_success));

                    homePageSettingsDb.line1 = edittext_title.getText().toString();
                    homePageSettingsDb.line2 = edittext_subtitle.getText().toString();
                    homePageSettingsDb.homeText = etOnlyText.getText().toString();
                    DeviceSettingsController.getInstance().updateHomeViewSettingsInDb(homePageSettingsDb);
                    finish();
                }
            });

            cbHomeText.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Util.writeBoolean(sp, GlobalParameters.HOME_TEXT_IS_ENABLE, isChecked);
                    if (isChecked) {
                        cbTextOnly.setChecked(false);
                    }
                    if(!isChecked)setIdentificationOptions();
                }
            });
            cbTextOnly.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Util.writeBoolean(sp, GlobalParameters.HOME_TEXT_ONLY_IS_ENABLE, isChecked);
                    if (isChecked) {
                        cbHomeText.setChecked(false);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void  setIdentificationOptions() {
        Util.writeBoolean(sp, GlobalParameters.QR_SCREEN, false);
        Util.writeBoolean(sp, GlobalParameters.ANONYMOUS_ENABLE, false);
        Util.writeBoolean(sp, GlobalParameters.RFID_ENABLE, false);
    }

    private void getHomePageSettingsFromDb() {
        String languageType = AppSettings.getLanguageType();
        homePageSettingsDb = DatabaseController.getInstance().getHomePageSettingsOnId(
                DeviceSettingsController.getInstance().getLanguageIdOnCode(languageType));

    }
}
