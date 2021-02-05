package com.certify.snap.activity;

import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import androidx.annotation.Nullable;

import com.certify.snap.api.response.ConfirmationViewSettings;
import com.certify.snap.common.AppSettings;
import com.certify.snap.controller.DatabaseController;
import com.certify.snap.controller.DeviceSettingsController;
import com.google.android.material.textfield.TextInputLayout;

import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.certify.snap.R;
import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.Util;


public class ConfirmationSettingsActivity extends SettingsBaseActivity {
    private static final String TAG = ConfirmationSettingsActivity.class.getSimpleName();
    Typeface rubiklight;
    TextView confirmation_screen, tv_confirm_above, tv_confirm_below,confirmation_above;
    TextInputLayout text_input_title_below, text_input_subtitle_below, text_input_title_above, text_input_subtitle_above, text_input_delay;
    private SharedPreferences sp;
    EditText edittext_title_below, edittext_subtitle_below, edittext_title_above, edittext_subtitle_above, et_screen_delay_above, et_screen_below;
    TextView btn_exit;
    private ConfirmationViewSettings confirmationSettingsDb;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_confirmation_settings);
            getConfirmationSettingsFromDb();
            rubiklight = Typeface.createFromAsset(getAssets(),
                    "rubiklight.ttf");
            sp = Util.getSharedPreferences(this);
            RadioGroup rgCapture = findViewById(R.id.radio_group_tempe);
            RadioButton rbCaptureYes = findViewById(R.id.radio_yes_temp);
            RadioButton rbCaptureNo = findViewById(R.id.radio_no_temp);
            RadioGroup rgCaptureAbove = findViewById(R.id.radio_group_temp_above);
            RadioButton rbCaptureYesAbove = findViewById(R.id.radio_yes_temp_above);
            RadioButton rbCaptureNoAbove = findViewById(R.id.radio_no_temp_above);
            confirmation_screen = findViewById(R.id.confirmation_screen);
            tv_confirm_above = findViewById(R.id.tv_confirm_above);
            tv_confirm_below = findViewById(R.id.tv_confirm_below);
            text_input_title_below = findViewById(R.id.text_input_title_below);
            text_input_subtitle_below = findViewById(R.id.text_input_subtitle_below);
            text_input_title_above = findViewById(R.id.text_input_title_above);
            text_input_subtitle_above = findViewById(R.id.text_input_subtitle_above);
            edittext_title_below = findViewById(R.id.edittext_title_below);
            edittext_subtitle_below = findViewById(R.id.edittext_subtitle_below);
            edittext_title_above = findViewById(R.id.edittext_title_above);
            edittext_subtitle_above = findViewById(R.id.edittext_subtitle_above);
            et_screen_delay_above = findViewById(R.id.et_screen_delay);
            text_input_delay = findViewById(R.id.text_input_delay);
            et_screen_below = findViewById(R.id.et_screen_below);
            btn_exit = findViewById(R.id.btn_exit);
            confirmation_above = findViewById(R.id.confirmation_above);
            confirmation_screen.setTypeface(rubiklight);
            tv_confirm_above.setTypeface(rubiklight);
            tv_confirm_below.setTypeface(rubiklight);
            confirmation_above.setTypeface(rubiklight);
            if (sp.getBoolean(GlobalParameters.CONFIRM_SCREEN_ABOVE, true))
                rbCaptureYesAbove.setChecked(true);
            else rbCaptureNoAbove.setChecked(true);
            if (sp.getBoolean(GlobalParameters.CONFIRM_SCREEN_BELOW, true))
                rbCaptureYes.setChecked(true);
            else rbCaptureNo.setChecked(true);

            et_screen_delay_above.setText(sp.getString(GlobalParameters.DELAY_VALUE_CONFIRM_ABOVE, "1"));
            et_screen_below.setText(sp.getString(GlobalParameters.DELAY_VALUE_CONFIRM_BELOW, "1"));
            edittext_title_above.setText(sp.getString(GlobalParameters.Confirm_title_above, getString(R.string.contact_supervisor_msg)));
            edittext_title_below.setText(sp.getString(GlobalParameters.Confirm_title_below, getString(R.string.nice_day_msg)));
            rgCapture.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    if (checkedId == R.id.radio_yes_temp)
                        Util.writeBoolean(sp, GlobalParameters.CONFIRM_SCREEN_BELOW, true);
                    else Util.writeBoolean(sp, GlobalParameters.CONFIRM_SCREEN_BELOW, false);
                }
            });
            rgCaptureAbove.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    if (checkedId == R.id.radio_yes_temp_above)
                        Util.writeBoolean(sp, GlobalParameters.CONFIRM_SCREEN_ABOVE, true);
                    else Util.writeBoolean(sp, GlobalParameters.CONFIRM_SCREEN_ABOVE, false);
                }
            });

            edittext_subtitle_below.setText(sp.getString(GlobalParameters.Confirm_subtitle_below, ""));
            edittext_subtitle_above.setText(sp.getString(GlobalParameters.Confirm_subtitle_above, ""));
            btn_exit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!et_screen_below.getText().toString().isEmpty())
                        Util.writeString(sp, GlobalParameters.DELAY_VALUE_CONFIRM_BELOW, et_screen_below.getText().toString().trim());
                    if (!et_screen_delay_above.getText().toString().isEmpty())
                        Util.writeString(sp, GlobalParameters.DELAY_VALUE_CONFIRM_ABOVE, et_screen_delay_above.getText().toString().trim());
                    if (!edittext_title_below.getText().toString().isEmpty())
                        Util.writeString(sp, GlobalParameters.Confirm_title_below, edittext_title_below.getText().toString());
                    if (!edittext_subtitle_below.getText().toString().isEmpty())
                        Util.writeString(sp, GlobalParameters.Confirm_subtitle_below, edittext_subtitle_below.getText().toString());
                    if (!edittext_title_above.getText().toString().isEmpty())
                        Util.writeString(sp, GlobalParameters.Confirm_title_above, edittext_title_above.getText().toString());
                    if (!edittext_subtitle_above.getText().toString().isEmpty())
                        Util.writeString(sp, GlobalParameters.Confirm_subtitle_above, edittext_subtitle_above.getText().toString());

                    confirmationSettingsDb.normalViewLine1 = edittext_title_below.getText().toString();
                    confirmationSettingsDb.normalViewLine2 = edittext_subtitle_below.getText().toString();
                    confirmationSettingsDb.aboveThresholdViewLine1 = edittext_title_above.getText().toString();
                    confirmationSettingsDb.temperatureAboveThreshold2 = edittext_subtitle_above.getText().toString();
                    DeviceSettingsController.getInstance().updateConfirmationSettingsInDb(confirmationSettingsDb);

                    Util.showToast(ConfirmationSettingsActivity.this, getString(R.string.save_success));
                    finish();
                }
            });

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private void getConfirmationSettingsFromDb() {
        String languageType = AppSettings.getLanguageType();
        confirmationSettingsDb = DatabaseController.getInstance().getConfirmationSettingOnId(
                DeviceSettingsController.getInstance().getLanguageIdOnCode(languageType));

    }
}
