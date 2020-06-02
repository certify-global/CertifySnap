package com.certify.snap.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import com.certify.snap.common.Util;

public class ScanViewActivity extends Activity {

    private SharedPreferences sp;
    EditText et_screen_delay,editTextDialogUserInput_low;
    Typeface rubiklight;
    TextView tv_delay,tv_sound,tv_temp_all,tv_capture_image,tv_temp_details,tv_scan,btn_save,tv_reg;
    TextInputLayout text_input_low_temp;
    RadioGroup radio_group_mask;
    RadioButton radio_yes_mask;
    RadioButton radio_no_mask;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            rubiklight = Typeface.createFromAsset(getAssets(),
                    "rubiklight.ttf");
            setContentView(R.layout.activity_scan_view);
            sp = Util.getSharedPreferences(this);
            final RadioGroup rgCapture = findViewById(R.id.radio_group_capture);
           final RadioButton rbCaptureYes = findViewById(R.id.radio_yes_capture);
            RadioButton rbCaptureNo = findViewById(R.id.radio_no_capture);
            RadioGroup rgCaptureAll = findViewById(R.id.radio_group_capture_all);
            RadioButton rbCaptureAllYes = findViewById(R.id.radio_yes_capture_all);
            RadioButton rbCaptureAllNo = findViewById(R.id.radio_no_capture_all);
            final RadioGroup radio_group_tempe = findViewById(R.id.radio_group_tempe);
            final RadioButton radio_yes_temp = findViewById(R.id.radio_yes_temp);
            RadioButton radio_no_temp = findViewById(R.id.radio_no_temp);
            RadioGroup radio_group_sound = findViewById(R.id.radio_group_sound);
            RadioButton radio_yes_sound = findViewById(R.id.radio_yes_sound);
            RadioButton radio_no_sound = findViewById(R.id.radio_no_sound);
            RadioGroup radio_group_reg = findViewById(R.id.radio_group_reg);
            RadioButton radio_yes_reg = findViewById(R.id.radio_yes_reg);
            RadioButton radio_no_reg = findViewById(R.id.radio_no_reg);
            et_screen_delay = findViewById(R.id.et_screen_delay);
            text_input_low_temp = findViewById(R.id.text_input_low_temp);
            editTextDialogUserInput_low = findViewById(R.id.editTextDialogUserInput_low);
            radio_group_mask = findViewById(R.id.radio_group_mask);
            radio_yes_mask = findViewById(R.id.radio_yes_mask);
            radio_no_mask = findViewById(R.id.radio_no_mask);
            btn_save = findViewById(R.id.btn_exit);
            tv_delay = findViewById(R.id.tv_delay);
            tv_sound = findViewById(R.id.tv_sound);
            tv_temp_all = findViewById(R.id.tv_temp_all);
            tv_capture_image = findViewById(R.id.tv_capture_image);
            tv_reg = findViewById(R.id.tv_reg);
            tv_scan = findViewById(R.id.titles);
            tv_temp_details = findViewById(R.id.tv_temp_details);
            tv_delay.setTypeface(rubiklight);
            tv_sound.setTypeface(rubiklight);
            tv_temp_all.setTypeface(rubiklight);
            tv_capture_image.setTypeface(rubiklight);
            tv_temp_details.setTypeface(rubiklight);
            tv_scan.setTypeface(rubiklight);
            tv_reg.setTypeface(rubiklight);

            if(sp.getBoolean(GlobalParameters.CAPTURE_IMAGES_ABOVE,true))
                rbCaptureYes.setChecked(true);
            else rbCaptureNo.setChecked(true);
            if(sp.getBoolean(GlobalParameters.CAPTURE_IMAGES_ALL,false))
                rbCaptureAllYes.setChecked(true);
            else rbCaptureAllNo.setChecked(true);
            if(sp.getBoolean(GlobalParameters.CAPTURE_TEMPERATURE,true))
                radio_yes_temp.setChecked(true);
            else radio_no_temp.setChecked(true);
            if(sp.getBoolean(GlobalParameters.CAPTURE_SOUND,false))
                radio_yes_sound.setChecked(true);
            else radio_no_sound.setChecked(true);
            if(sp.getBoolean(GlobalParameters.ALLOW_ALL,false)) {
                radio_yes_reg.setChecked(true);
                text_input_low_temp.setVisibility(View.VISIBLE);
            }else{
                radio_no_reg.setChecked(true);
                text_input_low_temp.setVisibility(View.GONE);
            }

            if (sp.getBoolean(GlobalParameters.MASK_DETECT, false)) {
                radio_yes_mask.setChecked(true);
            } else {
                radio_no_mask.setChecked(true);
            }
            et_screen_delay.setText(sp.getString(GlobalParameters.DELAY_VALUE,"3"));
            editTextDialogUserInput_low.setText(sp.getString(GlobalParameters.TEMP_TEST_LOW, "93.2"));

            rgCapture.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    if(checkedId==R.id.radio_yes_capture)
                        Util.writeBoolean(sp, GlobalParameters.CAPTURE_IMAGES_ABOVE, true);
                    else Util.writeBoolean(sp, GlobalParameters.CAPTURE_IMAGES_ABOVE, false);
                }
            });
            rgCaptureAll.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    if(checkedId==R.id.radio_yes_capture_all) {
                        rbCaptureYes.setChecked(true);
                        Util.writeBoolean(sp, GlobalParameters.CAPTURE_IMAGES_ALL, true);
                    }
                    else Util.writeBoolean(sp, GlobalParameters.CAPTURE_IMAGES_ALL, false);

                }
            });
            radio_group_tempe.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    if(checkedId==R.id.radio_yes_temp)
                        Util.writeBoolean(sp, GlobalParameters.CAPTURE_TEMPERATURE, true);
                    else Util.writeBoolean(sp, GlobalParameters.CAPTURE_TEMPERATURE, false);
                }
            });
            radio_group_sound.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    if(checkedId==R.id.radio_yes_sound)
                        Util.writeBoolean(sp, GlobalParameters.CAPTURE_SOUND, true);
                    else Util.writeBoolean(sp, GlobalParameters.CAPTURE_SOUND, false);
                }
            });
            radio_group_reg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    if(checkedId==R.id.radio_yes_reg){
                        Util.writeBoolean(sp, GlobalParameters.ALLOW_ALL, true);
                        text_input_low_temp.setVisibility(View.VISIBLE);
                        }else{
                            Util.writeBoolean(sp, GlobalParameters.ALLOW_ALL, false);
                         text_input_low_temp.setVisibility(View.GONE);
                    }
                }
            });
            radio_group_mask.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    System.out.println("Test CheckId" + checkedId);
                    if (checkedId == R.id.radio_yes_mask) {
                        Util.writeBoolean(sp, GlobalParameters.MASK_DETECT, true);
                    } else {
                        Util.writeBoolean(sp, GlobalParameters.MASK_DETECT, false);
                    }
                }
            });
            btn_save.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Util.writeString(sp,GlobalParameters.DELAY_VALUE,et_screen_delay.getText().toString().trim());
                    Util.writeString(sp, GlobalParameters.TEMP_TEST_LOW, editTextDialogUserInput_low.getText().toString().trim());
                    Util.showToast(ScanViewActivity.this, getString(R.string.save_success));
                    finish();
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void onParamterback(View view) {
        startActivity(new Intent(ScanViewActivity.this,SettingActivity.class));
        finish();
    }
}
