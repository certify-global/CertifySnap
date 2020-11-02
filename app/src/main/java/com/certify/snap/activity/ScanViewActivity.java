package com.certify.snap.activity;

import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.certify.snap.R;
import com.certify.snap.common.Constants;
import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.Util;
import com.google.android.material.textfield.TextInputLayout;

public class ScanViewActivity extends SettingBaseActivity {

    private SharedPreferences sp;
    EditText et_screen_delay,editTextDialogUserInput_low,et_normal,et_high;
    Typeface rubiklight;
    TextView tv_delay,tv_temp_all,tv_capture_image,tv_temp_details,tv_scan,btn_save,tv_reg,tv_mask,
            voiceRecognitionTextView ,  handGestureTextView,tv_temp_result_bar,tv_temp_text,tv_temp_text_normal,tv_temp_text_high;
    TextInputLayout text_input_low_temp;
    RadioGroup radio_group_mask,radio_group_bar;
    RadioButton radio_yes_mask,radio_yes_bar,radio_no_bar;
    RadioButton radio_no_mask;
    private TextView scanProximityView;
    private RadioGroup scanProximityRg;
    private RadioButton scanProximityYes;
    private RadioButton scanProximityNo;
    private TextView scanType;
    private RadioGroup scanTypeRg;
    private RadioButton scanTypeQuick;
    private RadioButton scanTypeStandard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_scan_view);
            sp = Util.getSharedPreferences(this);

            initView();
            //voiceRecognitionCheck();
            //handGestureCheck();

            final RadioGroup rgCapture = findViewById(R.id.radio_group_capture);
            final RadioButton rbCaptureYes = findViewById(R.id.radio_yes_capture);
            RadioButton rbCaptureNo = findViewById(R.id.radio_no_capture);
            RadioGroup rgCaptureAll = findViewById(R.id.radio_group_capture_all);
            RadioButton rbCaptureAllYes = findViewById(R.id.radio_yes_capture_all);
            RadioButton rbCaptureAllNo = findViewById(R.id.radio_no_capture_all);
            final RadioGroup radio_group_tempe = findViewById(R.id.radio_group_tempe);
            final RadioButton radio_yes_temp = findViewById(R.id.radio_yes_temp);
            RadioButton radio_no_temp = findViewById(R.id.radio_no_temp);
            RadioGroup radio_group_reg = findViewById(R.id.radio_group_reg);
            RadioButton radio_yes_reg = findViewById(R.id.radio_yes_reg);
            RadioButton radio_no_reg = findViewById(R.id.radio_no_reg);

            if(sp.getBoolean(GlobalParameters.CAPTURE_IMAGES_ABOVE,true))
                rbCaptureYes.setChecked(true);
            else rbCaptureNo.setChecked(true);
            if(sp.getBoolean(GlobalParameters.CAPTURE_IMAGES_ALL,false))
                rbCaptureAllYes.setChecked(true);
            else rbCaptureAllNo.setChecked(true);
            if(sp.getBoolean(GlobalParameters.CAPTURE_TEMPERATURE,true)) {
                radio_yes_temp.setChecked(true);
            }else {
                radio_no_temp.setChecked(true);
            }

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
            if (sp.getBoolean(GlobalParameters.RESULT_BAR, false)) {
                radio_yes_bar.setChecked(true);
                et_high.setEnabled(true);
                et_normal.setEnabled(true);
            } else {
                radio_no_bar.setChecked(true);
                et_high.setEnabled(false);
                et_normal.setEnabled(false);
            }
            et_screen_delay.setText(sp.getString(GlobalParameters.DELAY_VALUE,"3"));
            editTextDialogUserInput_low.setText(sp.getString(GlobalParameters.TEMP_TEST_LOW, "93.2"));
            et_normal.setText(sp.getString(GlobalParameters.RESULT_BAR_NORMAL, "Temperature Normal"));
            et_high.setText(sp.getString(GlobalParameters.RESULT_BAR_HIGH, "Temperature High"));



            setDefaultScanProximity();
            setScanProximityClickListener();
            setScanTypeDefault();
            setScanTypeClickListener();

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
                    if(checkedId==R.id.radio_yes_temp) {
                        Util.writeBoolean(sp, GlobalParameters.CAPTURE_TEMPERATURE, true);
                        setRadioValues();
                    }else {
                        Util.writeBoolean(sp, GlobalParameters.CAPTURE_TEMPERATURE, false);
                        setRadioValues();
                    }
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

            radio_group_bar.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    System.out.println("Test CheckId" + checkedId);
                    if (checkedId == R.id.radio_yes_bar) {
                        Util.writeBoolean(sp, GlobalParameters.RESULT_BAR, true);
                        setRadioValues();
                    } else {
                        Util.writeBoolean(sp, GlobalParameters.RESULT_BAR, false);
                        setRadioValues();
                    }
                }
            });
            btn_save.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Util.writeString(sp,GlobalParameters.DELAY_VALUE,et_screen_delay.getText().toString().trim());
                    Util.writeString(sp, GlobalParameters.TEMP_TEST_LOW, editTextDialogUserInput_low.getText().toString().trim());
                    Util.writeString(sp, GlobalParameters.RESULT_BAR_NORMAL, et_normal.getText().toString().trim());
                    Util.writeString(sp, GlobalParameters.RESULT_BAR_HIGH, et_high.getText().toString().trim());
                    Util.showToast(ScanViewActivity.this, getString(R.string.save_success));
                    saveScanProximity();
                    finish();
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void setRadioValues() {
        if (sp.getBoolean(GlobalParameters.RESULT_BAR, false)) {
            et_high.setEnabled(true);
            et_normal.setEnabled(true);
        } else {
            et_high.setEnabled(false);
            et_normal.setEnabled(false);
        }
    }

    private void initView(){
        et_screen_delay = findViewById(R.id.et_screen_delay);
        text_input_low_temp = findViewById(R.id.text_input_low_temp);
        editTextDialogUserInput_low = findViewById(R.id.editTextDialogUserInput_low);
        radio_group_mask = findViewById(R.id.radio_group_mask);
        radio_yes_mask = findViewById(R.id.radio_yes_mask);
        radio_no_mask = findViewById(R.id.radio_no_mask);
        btn_save = findViewById(R.id.btn_exit);
        tv_delay = findViewById(R.id.tv_delay);
        tv_temp_all = findViewById(R.id.tv_temp_all);
        tv_capture_image = findViewById(R.id.tv_capture_image);
        tv_reg = findViewById(R.id.tv_reg);
        tv_scan = findViewById(R.id.titles);
        tv_temp_details = findViewById(R.id.tv_temp_details);
        tv_mask = findViewById(R.id.tv_mask);
        scanProximityView = findViewById(R.id.tv_scan_proximity);
        scanProximityRg = findViewById(R.id.scan_proximity_rg);
        scanProximityYes = findViewById(R.id.radio_yes_scan_proximity);
        scanProximityNo = findViewById(R.id.radio_no_scan_proximity);
        voiceRecognitionTextView = findViewById(R.id.voice_recognition_textView);
        handGestureTextView = findViewById(R.id.hand_gesture_text_view);
        tv_temp_result_bar = findViewById(R.id.tv_temp_result_bar);
        tv_temp_text = findViewById(R.id.tv_temp_text);
        tv_temp_text_normal = findViewById(R.id.tv_temp_text_normal);
        tv_temp_text_high = findViewById(R.id.tv_temp_text_high);
        radio_yes_bar = findViewById(R.id.radio_yes_bar);
        radio_no_bar = findViewById(R.id.radio_no_bar);
        radio_group_bar = findViewById(R.id.radio_group_bar);
        et_normal = findViewById(R.id.et_normal);
        et_high = findViewById(R.id.et_high);
        scanType = findViewById(R.id.scan_type);
        scanTypeRg = findViewById(R.id.scan_type_rg);
        scanTypeQuick = findViewById(R.id.scan_type_quick);
        scanTypeStandard = findViewById(R.id.scan_type_standard);

        rubiklight = Typeface.createFromAsset(getAssets(),
                "rubiklight.ttf");

        tv_delay.setTypeface(rubiklight);
        tv_temp_all.setTypeface(rubiklight);
        tv_capture_image.setTypeface(rubiklight);
        tv_temp_details.setTypeface(rubiklight);
        tv_scan.setTypeface(rubiklight);
        tv_reg.setTypeface(rubiklight);
        tv_mask.setTypeface(rubiklight);
        scanProximityView.setTypeface(rubiklight);
        voiceRecognitionTextView.setTypeface(rubiklight);
        handGestureTextView.setTypeface(rubiklight);
        tv_temp_result_bar.setTypeface(rubiklight);
        tv_temp_text.setTypeface(rubiklight);
        tv_temp_text_normal.setTypeface(rubiklight);
        tv_temp_text_high.setTypeface(rubiklight);
        scanType.setTypeface(rubiklight);

    }

    private void voiceRecognitionCheck(){
        RadioGroup radio_group_light = findViewById(R.id.radio_group_voice_recognition);
        RadioButton radio_yes_light = findViewById(R.id.radio_yes_voice_recognition);
        RadioButton radio_no_light = findViewById(R.id.radio_no_voice_recognition);

        if(sp.getBoolean(GlobalParameters.VISUAL_RECOGNITION,false))
            radio_yes_light.setChecked(true);
        else radio_no_light.setChecked(true);

        radio_group_light.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId==R.id.radio_yes_voice_recognition)
                    Util.writeBoolean(sp, GlobalParameters.VISUAL_RECOGNITION, true);
                else Util.writeBoolean(sp, GlobalParameters.VISUAL_RECOGNITION, false);
            }
        });
    }

    private void handGestureCheck(){
        RadioGroup radio_group_light = findViewById(R.id.radio_group_hand_gesture);
        RadioButton radio_yes_light = findViewById(R.id.radio_yes_hand_gesture);
        RadioButton radio_no_light = findViewById(R.id.radio_no_hand_gesture);

        if(sp.getBoolean(GlobalParameters.HAND_GESTURE,false))
            radio_yes_light.setChecked(true);
        else radio_no_light.setChecked(true);

        radio_group_light.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId==R.id.radio_yes_hand_gesture)
                    Util.writeBoolean(sp, GlobalParameters.HAND_GESTURE, true);
                else Util.writeBoolean(sp, GlobalParameters.HAND_GESTURE, false);
            }
        });
    }

    private void setDefaultScanProximity() {
        if (sp.getBoolean(GlobalParameters.ScanProximity, false)) {
            scanProximityYes.setChecked(true);
            scanProximityNo.setChecked(false);
        } else {
            scanProximityNo.setChecked(true);
            scanProximityYes.setChecked(false);
        }
    }

    private void setScanProximityClickListener() {
        scanProximityRg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                if (checkedId == R.id.radio_scanmode_easy) {
                    scanProximityYes.setChecked(true);
                    scanProximityNo.setChecked(false);
                } else if (checkedId == R.id.radio_scanmode_strict) {
                    scanProximityNo.setChecked(true);
                    scanProximityYes.setChecked(false);
                }
            }
        });
    }

    private void saveScanProximity() {
        if (scanProximityYes.isChecked()) {
            Util.writeBoolean(sp, GlobalParameters.ScanProximity, true);
        } else if(scanProximityNo.isChecked()) {
            Util.writeBoolean(sp, GlobalParameters.ScanProximity, false);
        }
    }

    private void setScanTypeDefault() {
        if (sp.getInt(GlobalParameters.ScanType, Constants.DEFAULT_SCAN_TYPE) == 1) {
            scanTypeQuick.setChecked(true);
            scanTypeStandard.setChecked(false);
        } else {
            scanTypeStandard.setChecked(true);
            scanTypeQuick.setChecked(false);
        }
    }

    private void setScanTypeClickListener() {
        scanTypeRg.setOnCheckedChangeListener((radioGroup, checkedId) -> {
            if (checkedId == R.id.radio_scanmode_easy) {
                scanTypeQuick.setChecked(true);
                scanTypeStandard.setChecked(false);
            } else if (checkedId == R.id.radio_scanmode_strict) {
                scanTypeStandard.setChecked(true);
                scanTypeQuick.setChecked(false);
            }
        });
    }
}
