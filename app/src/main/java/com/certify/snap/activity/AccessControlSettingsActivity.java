package com.certify.snap.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.certify.snap.R;
import com.certify.snap.common.Constants;
import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.Util;

public class AccessControlSettingsActivity extends SettingsBaseActivity {
    private CheckBox mEnableRelayCb;
    private CheckBox mAllowAnonymousCb;
    private RadioGroup mModesRg;
    private RadioButton mNormalModeRb;
    private RadioButton mReverseModeRb;
    private CheckBox mStopRelayHighTemp;
    private EditText mRelayTimeEt;
    private CheckBox mEnableWeigand;
    private RadioGroup mCardFormatRg;
    private RadioButton m26BitRb;
    private RadioButton m34BitRb;
    private RadioButton m48BitRb;
    private TextView saveButton;
    private RadioGroup rg_logging;
    private RadioButton rb_logging_none;
    private RadioButton rb_logging_access_control;
    private RadioButton rb_logging_time;
    private RadioButton rb_logging_access_both;
    private RadioGroup rg_valid_access;
    private RadioButton rb_id_only;
    private RadioButton rb_face_only;
    private RadioButton rb_id_and_face;
    private RadioButton rb_id_or_face;
    private CheckBox mEnableWiegandPt;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_access_control_settings);
        mEnableRelayCb = findViewById(R.id.enable_relay_cb);
        mAllowAnonymousCb = findViewById(R.id.allow_anonymous_cb);
        mModesRg = findViewById(R.id.access_rg);
        mNormalModeRb = findViewById(R.id.normal_mode_rb);
        mReverseModeRb = findViewById(R.id.reverse_mode_rb);
        mStopRelayHighTemp = findViewById(R.id.stop_relay_cb);
        mRelayTimeEt = findViewById(R.id.edit_relaytime);
        mEnableWeigand = findViewById(R.id.enable_weigand_cb);
        mCardFormatRg = findViewById(R.id.card_format_rg);
        m26BitRb = findViewById(R.id.twentysix_bit_rb);
        m34BitRb = findViewById(R.id.thirtyfour_bit_rb);
        m48BitRb = findViewById(R.id.fourtyEight_bit_rb);
        saveButton = findViewById(R.id.btn_save);
        rg_logging = findViewById(R.id.rg_logging);
        rb_logging_access_control = findViewById(R.id.rb_logging_access_control);
        rb_logging_time = findViewById(R.id.rb_logging_time);
        rb_logging_access_both = findViewById(R.id.rb_logging_access_both);
        rb_logging_none = findViewById(R.id.rb_logging_none);
        mEnableWiegandPt = findViewById(R.id.enable_weigand_pt);
        rg_valid_access = findViewById(R.id.rg_valid_access);
        rb_id_only = findViewById(R.id.rb_id_only);
        rb_face_only = findViewById(R.id.rb_face_only);
        rb_id_and_face = findViewById(R.id.rb_id_and_face);
        rb_id_or_face = findViewById(R.id.rb_id_or_face);

        setDefaults();
        handleClickListeners();
    }

    private void setDefaults() {
        mEnableRelayCb.setChecked(Util.getSharedPreferences(this).getBoolean(GlobalParameters.EnableRelay, false));
        mAllowAnonymousCb.setChecked(Util.getSharedPreferences(this).getBoolean(GlobalParameters.AllowAnonymous, false));
        boolean value = Util.getSharedPreferences(this).getBoolean(GlobalParameters.RelayNormalMode, true);
        if (value) {
            mNormalModeRb.setChecked(true);
        } else {
            mReverseModeRb.setChecked(true);
        }
        mStopRelayHighTemp.setChecked(Util.getSharedPreferences(this).getBoolean(GlobalParameters.StopRelayOnHighTemp, false));
        mRelayTimeEt.setText(String.valueOf(Util.getSharedPreferences(this).getInt(GlobalParameters.RelayTime, Constants.DEFAULT_RELAY_TIME)));
        mEnableWeigand.setChecked(Util.getSharedPreferences(this).getBoolean(GlobalParameters.EnableWeigand, false));
        mEnableWiegandPt.setChecked(Util.getSharedPreferences(this).getBoolean(GlobalParameters.EnableWeigandPassThrough, false));
        if (Util.getSharedPreferences(this).getInt(GlobalParameters.WeiganFormatMessage, Constants.DEFAULT_WEIGAN_CONTROLLER_FORMAT) == 26) {
            m26BitRb.setChecked(true);
            m34BitRb.setChecked(false);
            m48BitRb.setChecked(false);
        } else if (Util.getSharedPreferences(this).getInt(GlobalParameters.WeiganFormatMessage, Constants.DEFAULT_WEIGAN_CONTROLLER_FORMAT) == 34) {
            m26BitRb.setChecked(false);
            m34BitRb.setChecked(true);
            m48BitRb.setChecked(false);
        } else {
            m26BitRb.setChecked(false);
            m34BitRb.setChecked(false);
            m48BitRb.setChecked(true);
        }
        if (Util.getSharedPreferences(this).getInt(GlobalParameters.AccessControlLogMode, Constants.DEFAULT_ACCESS_CONTROL_LOG_MODE) ==1) {
            rb_logging_access_control.setChecked(true);
            rb_logging_time.setChecked(false);
            rb_logging_access_both.setChecked(false);
            rb_logging_none.setChecked(false);
        } else if(Util.getSharedPreferences(this).getInt(GlobalParameters.AccessControlLogMode, Constants.DEFAULT_ACCESS_CONTROL_LOG_MODE) ==2) {
            rb_logging_access_control.setChecked(false);
            rb_logging_time.setChecked(true);
            rb_logging_access_both.setChecked(false);
            rb_logging_none.setChecked(false);
        } else if(Util.getSharedPreferences(this).getInt(GlobalParameters.AccessControlLogMode, Constants.DEFAULT_ACCESS_CONTROL_LOG_MODE) ==3) {
            rb_logging_access_control.setChecked(false);
            rb_logging_time.setChecked(false);
            rb_logging_access_both.setChecked(true);
            rb_logging_none.setChecked(false);
        }else{
            rb_logging_access_control.setChecked(false);
            rb_logging_time.setChecked(false);
            rb_logging_access_both.setChecked(false);
            rb_logging_none.setChecked(true);
        }

        if (Util.getSharedPreferences(this).getInt(GlobalParameters.AccessControlScanMode, Constants.DEFAULT_ACCESS_CONTROL_SCAN_MODE)==1) {
            rb_id_only.setChecked(true);
            rb_face_only.setChecked(false);
            rb_id_and_face.setChecked(false);
            rb_id_or_face.setChecked(false);
        } else if(Util.getSharedPreferences(this).getInt(GlobalParameters.AccessControlScanMode, Constants.DEFAULT_ACCESS_CONTROL_SCAN_MODE)==2) {
            rb_id_only.setChecked(false);
            rb_face_only.setChecked(true);
            rb_id_and_face.setChecked(false);
            rb_id_or_face.setChecked(false);
        } else if(Util.getSharedPreferences(this).getInt(GlobalParameters.AccessControlScanMode, Constants.DEFAULT_ACCESS_CONTROL_SCAN_MODE)==3) {
            rb_id_only.setChecked(false);
            rb_face_only.setChecked(false);
            rb_id_and_face.setChecked(true);
            rb_id_or_face.setChecked(false);
        }else{
            rb_id_only.setChecked(false);
            rb_face_only.setChecked(false);
            rb_id_and_face.setChecked(false);
            rb_id_or_face.setChecked(true);
        }
    }

    private void handleClickListeners() {
        saveButton.setOnClickListener(view -> {
            if (mRelayTimeEt.getText().toString().trim().isEmpty()) return;
            saveSetting();
        });

        mEnableRelayCb.setOnCheckedChangeListener((compoundButton, value) -> mEnableRelayCb.setChecked(value));

        mAllowAnonymousCb.setOnCheckedChangeListener((compoundButton, value) -> mAllowAnonymousCb.setChecked(value));

        mModesRg.setOnCheckedChangeListener((radioGroup, checkedId) -> {
            if (checkedId == R.id.normal_mode_rb) {
                mNormalModeRb.setChecked(true);
                mReverseModeRb.setChecked(false);
            } else if (checkedId == R.id.reverse_mode_rb) {
                mReverseModeRb.setChecked(true);
                mNormalModeRb.setChecked(false);
            }
        });

        mStopRelayHighTemp.setOnCheckedChangeListener((compoundButton, value) -> mStopRelayHighTemp.setChecked(value));

        mEnableWeigand.setOnCheckedChangeListener((compoundButton, value) -> mEnableWeigand.setChecked(value));

        mEnableWiegandPt.setOnCheckedChangeListener((compoundButton, value) -> mEnableWiegandPt.setChecked(value));

        mCardFormatRg.setOnCheckedChangeListener((radioGroup, checkedId) -> {
            if (checkedId == R.id.twentysix_bit_rb) {
                m26BitRb.setChecked(true);
                m34BitRb.setChecked(false);
                m48BitRb.setChecked(false);
            } else if (checkedId == R.id.thirtyfour_bit_rb) {
                m34BitRb.setChecked(true);
                m26BitRb.setChecked(false);
                m48BitRb.setChecked(false);
            }else if (checkedId == R.id.fourtyEight_bit_rb) {
                m34BitRb.setChecked(false);
                m26BitRb.setChecked(false);
                m48BitRb.setChecked(true);
            }
        });

        rg_logging.setOnCheckedChangeListener((radioGroup, checkedId) -> {
            if (checkedId == R.id.rb_logging_access_control) {
                rb_logging_access_control.setChecked(true);
                rb_logging_time.setChecked(false);
                rb_logging_access_both.setChecked(false);
                rb_logging_none.setChecked(false);
            } else if (checkedId == R.id.rb_logging_time) {
                rb_logging_access_control.setChecked(false);
                rb_logging_time.setChecked(true);
                rb_logging_access_both.setChecked(false);
                rb_logging_none.setChecked(false);
            }else if(checkedId==R.id.rb_logging_access_both){
                rb_logging_access_control.setChecked(false);
                rb_logging_time.setChecked(false);
                rb_logging_access_both.setChecked(true);
                rb_logging_none.setChecked(false);

            }else if(checkedId==R.id.rb_logging_none){
                rb_logging_access_control.setChecked(false);
                rb_logging_time.setChecked(false);
                rb_logging_access_both.setChecked(false);
                rb_logging_none.setChecked(true);
            }
        });
        rg_valid_access.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                if (checkedId == R.id.rb_id_only) {
                    rb_id_only.setChecked(true);
                    rb_face_only.setChecked(false);
                    rb_id_and_face.setChecked(false);
                    rb_id_or_face.setChecked(false);
                } else if (checkedId == R.id.rb_face_only) {
                    rb_id_only.setChecked(false);
                    rb_face_only.setChecked(true);
                    rb_id_and_face.setChecked(false);
                    rb_id_or_face.setChecked(false);
                }else if(checkedId==R.id.rb_id_and_face){
                    rb_id_only.setChecked(false);
                    rb_face_only.setChecked(false);
                    rb_id_and_face.setChecked(true);
                    rb_id_or_face.setChecked(false);
                }else if(checkedId==R.id.rb_id_or_face){
                    rb_id_only.setChecked(false);
                    rb_face_only.setChecked(false);
                    rb_id_and_face.setChecked(false);
                    rb_id_or_face.setChecked(true);
                }
            }
        });
    }

    private void saveSetting() {
        SharedPreferences sp = Util.getSharedPreferences(getApplicationContext());
        Util.writeBoolean(sp, GlobalParameters.EnableRelay, mEnableRelayCb.isChecked());
        Util.writeBoolean(sp, GlobalParameters.AllowAnonymous, mAllowAnonymousCb.isChecked());
        Util.writeBoolean(sp, GlobalParameters.RelayNormalMode, mNormalModeRb.isChecked());
        Util.writeBoolean(sp, GlobalParameters.RelayReverseMode, mReverseModeRb.isChecked());
        Util.writeBoolean(sp, GlobalParameters.StopRelayOnHighTemp, mStopRelayHighTemp.isChecked());
        Util.writeInt(sp, GlobalParameters.RelayTime, Integer.parseInt(mRelayTimeEt.getText().toString()));
        Util.writeBoolean(sp, GlobalParameters.EnableWeigand, mEnableWeigand.isChecked());
        Util.writeBoolean(sp, GlobalParameters.EnableWeigandPassThrough, mEnableWiegandPt.isChecked());
        if (m26BitRb.isChecked()) {
            Util.writeInt(sp, GlobalParameters.WeiganFormatMessage, 26);
        } else if(m34BitRb.isChecked()) {
            Util.writeInt(sp, GlobalParameters.WeiganFormatMessage, 34);
        }else if(m48BitRb.isChecked()){
            Util.writeInt(sp, GlobalParameters.WeiganFormatMessage, 48);
        }
        if (rb_logging_access_control.isChecked()) {
            Util.writeInt(sp, GlobalParameters.AccessControlLogMode, 1);
        } else if(rb_logging_time.isChecked()) {
            Util.writeInt(sp, GlobalParameters.AccessControlLogMode, 2);
        }else if(rb_logging_access_both.isChecked()) {
            Util.writeInt(sp, GlobalParameters.AccessControlLogMode, 3);
        }else if(rb_logging_none.isChecked()) {
            Util.writeInt(sp, GlobalParameters.AccessControlLogMode, 0);
        }

        if (rb_id_only.isChecked()) {
            Util.writeInt(sp, GlobalParameters.AccessControlScanMode, 1);
        } else if(rb_face_only.isChecked()) {
            Util.writeInt(sp, GlobalParameters.AccessControlScanMode, 2);
        }else if(rb_id_and_face.isChecked()) {
            Util.writeInt(sp, GlobalParameters.AccessControlScanMode, 3);
        }else if(rb_id_or_face.isChecked()) {
            Util.writeInt(sp, GlobalParameters.AccessControlScanMode, 4);
        }
        finish();
        Toast.makeText(getApplicationContext(), getString(R.string.save_success), Toast.LENGTH_LONG).show();
    }
}
