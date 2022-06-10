package com.certify.snap.activity;

import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.certify.snap.R;
import com.certify.snap.api.response.ScanViewSettings;
import com.certify.snap.common.AppSettings;
import com.certify.snap.common.Constants;
import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.Util;
import com.certify.snap.controller.DatabaseController;
import com.certify.snap.controller.DeviceSettingsController;
import com.google.android.material.textfield.TextInputLayout;

public class ScanSettingsActivity extends SettingsBaseActivity {

    private SharedPreferences sp;
    EditText et_screen_delay,editTextDialogUserInput_low,et_normal,et_high;
    Typeface rubiklight;
    TextView tv_delay,tv_temp_all,tv_capture_image,tv_temp_details,tv_scan,btn_save,tv_reg,tv_mask,
            tv_temp_result_bar,tv_temp_text,tv_temp_text_normal,tv_temp_text_high;
    TextInputLayout text_input_low_temp;
    RadioGroup radio_group_mask,radio_group_bar;
    RadioButton radio_yes_mask,radio_yes_bar,radio_no_bar;
    RadioButton radio_no_mask;
    private TextView scanProximityView;
    private RadioGroup scanProximityRg;
    private RadioButton scanProximityYes;
    private RadioButton scanProximityNo;
    private TextView scanType, retryScan;
    private RadioGroup scanTypeRg, retryScanRg;
    private RadioButton scanTypeQuick, retryScanRbYes, retryScanRbNo;
    private RadioButton scanTypeStandard;
    private TextView enableTempScan;
    private RadioGroup tempScanRg;
    private RadioButton tempScanYes;
    private RadioButton tempScanNo;
    private LinearLayout tempScanSettingsLayout;
    private TextView enableLivenessTv;
    private RadioGroup livenessRg;
    private RadioButton livenessYes, livenessNo;
    private ScanViewSettings scanViewSettings = null;
    private TextView lowTempImageTv;
    private RadioGroup lowTempImageRg;
    private RadioButton lowTempImageRbYes, lowTempImageRbNo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_scan_settings);
            sp = Util.getSharedPreferences(this);

            initView();
            getScanSettingsFromDb();

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
            et_normal.setText(sp.getString(GlobalParameters.RESULT_BAR_NORMAL, getString(R.string.temperature_normal_msg)));
            et_high.setText(sp.getString(GlobalParameters.RESULT_BAR_HIGH, getString(R.string.temperature_high_msg)));



            setDefaultScanProximity();
            setScanProximityClickListener();
            setScanTypeDefault();
            setScanTypeClickListener();
            setDefaultAllowTempScan();
            setTempScanClickListener();
            setDefaultLiveness();
            setLivenessClickListener();
            setRetryScanSettings();
            setCaptureLowTempImages();

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

                    if (scanViewSettings != null) {
                        scanViewSettings.temperatureNormal = et_normal.getText().toString().trim();
                        scanViewSettings.temperatureHigh = et_high.getText().toString().trim();
                        DeviceSettingsController.getInstance().updateScanViewSettingsInDb(scanViewSettings);
                    }

                    Util.showToast(ScanSettingsActivity.this, getString(R.string.save_success));
                    saveScanProximity();
                    saveScanType();
                    saveTempScan();
                    saveLiveness();
                    saveRetryScan();
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
        enableTempScan = findViewById(R.id.temp_scan);
        tempScanRg = findViewById(R.id.temp_scan_rg);
        tempScanYes = findViewById(R.id.temp_scan_yes);
        tempScanNo = findViewById(R.id.temp_scan_no);
        tempScanSettingsLayout = findViewById(R.id.temp_scan_settings_layout);
        enableLivenessTv = findViewById(R.id.liveness_tv);
        livenessRg = findViewById(R.id.liveness_rg);
        livenessYes = findViewById(R.id.liveness_yes_rb);
        livenessNo = findViewById(R.id.liveness_no_rb);
        retryScanRg = findViewById(R.id.retry_scan_rg);
        retryScanRbYes = findViewById(R.id.retry_scan_yes);
        retryScanRbNo = findViewById(R.id.retry_scan_no);
        retryScan = findViewById(R.id.retry_scan_tv);

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
        tv_temp_result_bar.setTypeface(rubiklight);
        tv_temp_text.setTypeface(rubiklight);
        tv_temp_text_normal.setTypeface(rubiklight);
        tv_temp_text_high.setTypeface(rubiklight);
        scanType.setTypeface(rubiklight);
        enableTempScan.setTypeface(rubiklight);
        enableLivenessTv.setTypeface(rubiklight);
        retryScan.setTypeface(rubiklight);
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
            if (checkedId == R.id.scan_type_quick) {
                scanTypeQuick.setChecked(true);
                scanTypeStandard.setChecked(false);
            } else if (checkedId == R.id.scan_type_standard) {
                scanTypeStandard.setChecked(true);
                scanTypeQuick.setChecked(false);
            }
        });
    }

    private void saveScanType() {
        if (scanTypeQuick.isChecked()) {
            Util.writeInt(sp, GlobalParameters.ScanType, 1);
        } else if(scanTypeStandard.isChecked()) {
            Util.writeInt(sp, GlobalParameters.ScanType, 0);
        }
    }

    private void setDefaultAllowTempScan() {
        if (sp.getBoolean(GlobalParameters.EnableTempScan, true)) {
            tempScanYes.setChecked(true);
            tempScanNo.setChecked(false);
            tempScanSettingsLayout.setVisibility(View.VISIBLE);
        } else {
            tempScanNo.setChecked(true);
            tempScanYes.setChecked(false);
            tempScanSettingsLayout.setVisibility(View.GONE);
        }
    }

    private void setTempScanClickListener() {
        tempScanRg.setOnCheckedChangeListener((radioGroup, checkedId) -> {
            if (checkedId == R.id.temp_scan_yes) {
                tempScanYes.setChecked(true);
                tempScanNo.setChecked(false);
                tempScanSettingsLayout.setVisibility(View.VISIBLE);
            } else if (checkedId == R.id.temp_scan_no) {
                tempScanNo.setChecked(true);
                tempScanYes.setChecked(false);
                tempScanSettingsLayout.setVisibility(View.GONE);
            }
        });
    }

    private void saveTempScan() {
        if (tempScanYes.isChecked()) {
            Util.writeBoolean(sp, GlobalParameters.EnableTempScan, true);
        } else if(tempScanNo.isChecked()) {
            Util.writeBoolean(sp, GlobalParameters.EnableTempScan, false);
        }
    }

    private void setDefaultLiveness() {
        if (sp.getBoolean(GlobalParameters.LivingType, false)) {
            livenessYes.setChecked(true);
            livenessNo.setChecked(false);
        } else {
            livenessNo.setChecked(true);
            livenessYes.setChecked(false);
        }
    }

    private void setLivenessClickListener() {
        livenessRg.setOnCheckedChangeListener((radioGroup, checkedId) -> {
            if (checkedId == R.id.liveness_yes_rb) {
                livenessYes.setChecked(true);
                livenessNo.setChecked(false);
            } else if (checkedId == R.id.liveness_no_rb) {
                livenessNo.setChecked(true);
                livenessYes.setChecked(false);
            }
        });
    }

    private void saveLiveness() {
        if (livenessYes.isChecked()) {
            Util.writeBoolean(sp, GlobalParameters.LivingType, true);
        } else if(livenessNo.isChecked()) {
            Util.writeBoolean(sp, GlobalParameters.LivingType, false);
        }
    }

    private void getScanSettingsFromDb() {
        String languageType = AppSettings.getLanguageType();
        scanViewSettings = DatabaseController.getInstance().getScanViewSettingsOnId(
                DeviceSettingsController.getInstance().getLanguageIdOnCode(languageType));

    }

    private void setRetryScanSettings() {
        if (sp.getBoolean(GlobalParameters.RETRY_SCAN, false)) {
            retryScanRbYes.setChecked(true);
        } else {
            retryScanRbNo.setChecked(true);
        }

        retryScanRg.setOnCheckedChangeListener((radioGroup, id) -> {
            if (id == R.id.retry_scan_yes) {
                retryScanRbYes.setChecked(true);
            } else if (id == R.id.retry_scan_no) {
                retryScanRbNo.setChecked(true);
            }
        });
    }

    private void saveRetryScan() {
        Util.writeBoolean(sp, GlobalParameters.RETRY_SCAN, retryScanRbYes.isChecked());
    }

    private void setCaptureLowTempImages() {
        lowTempImageTv = findViewById(R.id.low_threshold_tv);
        lowTempImageRg = findViewById(R.id.low_threshold_images_rg);
        lowTempImageRbYes = findViewById(R.id.low_threshold_images_rb_y);
        lowTempImageRbNo = findViewById(R.id.low_threshold_images_rb_n);
        lowTempImageTv.setTypeface(rubiklight);

        if (sp.getBoolean(GlobalParameters.CAPTURE_LOW_TEMP_IMAGES, false)) {
            lowTempImageRbYes.setChecked(true);
            lowTempImageRbNo.setChecked(false);
        } else {
            lowTempImageRbNo.setChecked(true);
            lowTempImageRbYes.setChecked(false);
        }

        lowTempImageRg.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.low_threshold_images_rb_y) {
                lowTempImageRbYes.setChecked(true);
                lowTempImageRbNo.setChecked(false);
                Util.writeBoolean(sp, GlobalParameters.CAPTURE_LOW_TEMP_IMAGES, true);
            } else if (checkedId == R.id.low_threshold_images_rb_n) {
                lowTempImageRbNo.setChecked(true);
                lowTempImageRbYes.setChecked(false);
                Util.writeBoolean(sp, GlobalParameters.CAPTURE_LOW_TEMP_IMAGES, false);
            }
        });
    }
}
