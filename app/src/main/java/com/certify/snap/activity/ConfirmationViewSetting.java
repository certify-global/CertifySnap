package com.certify.snap.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.certify.snap.R;
import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.Logger;
import com.certify.snap.common.Util;


public class ConfirmationViewSetting extends Activity {
    Typeface rubiklight;
    TextView confirmation_screen,tv_confirm_above,tv_confirm_below;
    TextInputLayout text_input_title_below,text_input_subtitle_below,text_input_title_above,text_input_subtitle_above,text_input_delay;
    private SharedPreferences sp;
    EditText edittext_title_below,edittext_subtitle_below,edittext_title_above,edittext_subtitle_above,et_screen_delay;
    TextView btn_exit;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            setContentView(R.layout.activity_confirmation_setting);
            rubiklight = Typeface.createFromAsset(getAssets(),
                    "rubiklight.ttf");
            sp = Util.getSharedPreferences(this);
            RadioGroup rgCapture = findViewById(R.id.radio_group_tempe);
            RadioButton rbCaptureYes = findViewById(R.id.radio_yes_temp);
            RadioButton rbCaptureNo = findViewById(R.id.radio_no_temp);
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
            et_screen_delay = findViewById(R.id.et_screen_delay);
            text_input_delay = findViewById(R.id.text_input_delay);
            btn_exit = findViewById(R.id.btn_exit);
            confirmation_screen.setTypeface(rubiklight);
            tv_confirm_above.setTypeface(rubiklight);
            tv_confirm_below.setTypeface(rubiklight);
            if(sp.getBoolean(GlobalParameters.CONFIRM_SCREEN,true))
                rbCaptureYes.setChecked(true);
            else rbCaptureNo.setChecked(true);


             et_screen_delay.setText(sp.getString(GlobalParameters.DELAY_VALUE_CONFIRM,"3"));
             edittext_title_above.setText(sp.getString(GlobalParameters.Confirm_title_above,"Thank You"));
             edittext_title_below.setText(sp.getString(GlobalParameters.Confirm_title_below,"Thank You"));


            rgCapture.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    if(checkedId==R.id.radio_yes_temp)
                        Util.writeBoolean(sp, GlobalParameters.CONFIRM_SCREEN, true);
                    else Util.writeBoolean(sp, GlobalParameters.CONFIRM_SCREEN, false);
                }
            });


            edittext_subtitle_below.setText(sp.getString(GlobalParameters.Confirm_subtitle_below,""));
            edittext_subtitle_above.setText(sp.getString(GlobalParameters.Confirm_subtitle_above,""));
            et_screen_delay.setText(sp.getString(GlobalParameters.DELAY_VALUE_CONFIRM,"3"));
            btn_exit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!et_screen_delay.getText().toString().isEmpty())
                    Util.writeString(sp,GlobalParameters.DELAY_VALUE_CONFIRM,et_screen_delay.getText().toString().trim());
                    if(!edittext_title_below.getText().toString().isEmpty())
                    Util.writeString(sp, GlobalParameters.Confirm_title_below, edittext_title_below.getText().toString());
                    if(!edittext_subtitle_below.getText().toString().isEmpty())
                    Util.writeString(sp, GlobalParameters.Confirm_subtitle_below, edittext_subtitle_below.getText().toString());
                    if(!edittext_title_above.getText().toString().isEmpty())
                    Util.writeString(sp, GlobalParameters.Confirm_title_above, edittext_title_above.getText().toString());
                    if(!edittext_subtitle_above.getText().toString().isEmpty())
                    Util.writeString(sp, GlobalParameters.Confirm_subtitle_above, edittext_subtitle_above.getText().toString());
                    finish();
                }
            });

        }catch (Exception e){
            Logger.error(" onCreate(@Nullable Bundle savedInstanceState)",e.getMessage());
        }
    }
    public void onParamterback(View view) {
        startActivity(new Intent(ConfirmationViewSetting.this,SettingActivity.class));
        finish();
    }
}
