package com.certify.snap.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
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
    private RadioButton rb_logging_yes;
    private RadioButton rb_logging_no;


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
        rb_logging_yes = findViewById(R.id.rb_logging_yes);
        rb_logging_no = findViewById(R.id.rb_logging_no);

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
        if (Util.getSharedPreferences(this).getBoolean(GlobalParameters.ACCESS_LOGGING,false)) {
            rb_logging_yes.setChecked(true);
            rb_logging_no.setChecked(false);
        } else {
            rb_logging_no.setChecked(true);
            rb_logging_yes.setChecked(false);
        }
    }

    private void handleClickListeners() {
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mRelayTimeEt.getText().toString().trim().isEmpty()) return;
                saveSetting();
            }
        });

        mEnableRelayCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean value) {
                mEnableRelayCb.setChecked(value);
            }
        });

        mAllowAnonymousCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean value) {
                mAllowAnonymousCb.setChecked(value);
            }
        });

        mModesRg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                if (checkedId == R.id.normal_mode_rb) {
                    mNormalModeRb.setChecked(true);
                    mReverseModeRb.setChecked(false);
                } else if (checkedId == R.id.reverse_mode_rb) {
                    mReverseModeRb.setChecked(true);
                    mNormalModeRb.setChecked(false);
                }
            }
        });

        mStopRelayHighTemp.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean value) {
                mStopRelayHighTemp.setChecked(value);
            }
        });

        mEnableWeigand.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean value) {
                mEnableWeigand.setChecked(value);
            }
        });

        mCardFormatRg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
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
            }
        });

        rg_logging.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                if (checkedId == R.id.rb_logging_yes) {
                    rb_logging_yes.setChecked(true);
                    rb_logging_no.setChecked(false);
                } else if (checkedId == R.id.rb_logging_no) {
                    rb_logging_no.setChecked(true);
                    rb_logging_yes.setChecked(false);
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
        if (m26BitRb.isChecked()) {
            Util.writeInt(sp, GlobalParameters.WeiganFormatMessage, 26);
        } else if(m34BitRb.isChecked()) {
            Util.writeInt(sp, GlobalParameters.WeiganFormatMessage, 34);
        }else if(m48BitRb.isChecked()){
            Util.writeInt(sp, GlobalParameters.WeiganFormatMessage, 48);
        }
        if (rb_logging_yes.isChecked()) {
            Util.writeBoolean(sp, GlobalParameters.ACCESS_LOGGING, true);
        } else if(rb_logging_no.isChecked()) {
            Util.writeBoolean(sp, GlobalParameters.ACCESS_LOGGING, false);
        }
        finish();
        Toast.makeText(getApplicationContext(), getString(R.string.save_success), Toast.LENGTH_LONG).show();
    }
}