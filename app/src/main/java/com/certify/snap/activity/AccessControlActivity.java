package com.certify.snap.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.certify.snap.R;
import com.certify.snap.common.Constants;
import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.Util;

public class AccessControlActivity extends Activity {
    private CheckBox mEnableADCheckBox;
    private CheckBox mEnableACCheckBox;
    private RadioGroup mBlockAccessRadioGroup;
    private RadioButton mBlockAccessYes;
    private RadioButton mBlockAccessNo;
    private EditText mRelayTimeEt;
    private RadioGroup mCardFormatRg;
    private RadioButton m26BitRb;
    private RadioButton m34BitRb;
    private TextView saveButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.access_control_layout);
        saveButton = findViewById(R.id.btn_save);
        mRelayTimeEt = findViewById(R.id.edit_relaytime);
        mEnableACCheckBox = findViewById(R.id.enable_ac_check_box);
        mBlockAccessRadioGroup = findViewById(R.id.block_access_rg);
        mBlockAccessYes = findViewById(R.id.block_access_yes);
        mBlockAccessNo = findViewById(R.id.block_access_no);
        mCardFormatRg = findViewById(R.id.card_format_rg);
        m26BitRb = findViewById(R.id.twentysix_bit_rb);
        m34BitRb = findViewById(R.id.thirtyfour_bit_rb);
        mEnableADCheckBox = findViewById(R.id.enable_ad_check_box);

        setDefaults();
        handleClickListeners();
    }

    private void setDefaults() {
        mEnableADCheckBox.setChecked(Util.getSharedPreferences(this).getBoolean(GlobalParameters.AutomaticDoorAccess, false));
        mEnableACCheckBox.setChecked(Util.getSharedPreferences(this).getBoolean(GlobalParameters.AccessControlEnable, false));
        if (Util.getSharedPreferences(this).getBoolean(GlobalParameters.BlockAccessHighTemp, true)) {
            mBlockAccessYes.setChecked(true);
            mBlockAccessNo.setChecked(false);
        } else {
            mBlockAccessNo.setChecked(true);
            mBlockAccessYes.setChecked(false);
        }
        mRelayTimeEt.setText(String.valueOf(Util.getSharedPreferences(this).getInt(GlobalParameters.RelayTime, Constants.DEFAULT_RELAY_TIME)));
        if (Util.getSharedPreferences(this).getInt(GlobalParameters.AccessControlCardFormat, Constants.DEFAULT_WEIGAN_CONTROLLER_FORMAT) == 26) {
            m26BitRb.setChecked(true);
            m34BitRb.setChecked(false);
        } else {
            m34BitRb.setChecked(true);
            m26BitRb.setChecked(false);
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

        mEnableACCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean value) {
                mEnableACCheckBox.setChecked(value);
            }
        });

        mBlockAccessRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                if (checkedId == R.id.block_access_yes) {
                    mBlockAccessYes.setChecked(true);
                    mBlockAccessNo.setChecked(false);
                } else if (checkedId == R.id.block_access_no) {
                    mBlockAccessNo.setChecked(true);
                    mBlockAccessYes.setChecked(false);
                }
            }
        });

        mCardFormatRg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                if (checkedId == R.id.twentysix_bit_rb) {
                    m26BitRb.setChecked(true);
                    m34BitRb.setChecked(false);
                } else if (checkedId == R.id.thirtyfour_bit_rb) {
                    m34BitRb.setChecked(true);
                    m26BitRb.setChecked(false);
                }
            }
        });

        mEnableADCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean value) {
                mEnableADCheckBox.setChecked(value);
            }
        });
    }

    private void saveSetting() {
        SharedPreferences sp = Util.getSharedPreferences(getApplicationContext());
        Util.writeBoolean(sp, GlobalParameters.AutomaticDoorAccess, mEnableADCheckBox.isChecked());
        Util.writeBoolean(sp, GlobalParameters.AccessControlEnable, mEnableACCheckBox.isChecked());
        Util.writeBoolean(sp, GlobalParameters.BlockAccessHighTemp, mBlockAccessYes.isChecked());
        Util.writeInt(sp, GlobalParameters.RelayTime, Integer.parseInt(mRelayTimeEt.getText().toString()));
        if (m26BitRb.isChecked()) {
            Util.writeInt(sp, GlobalParameters.AccessControlCardFormat, 26);
        } else if(m34BitRb.isChecked()) {
            Util.writeInt(sp, GlobalParameters.AccessControlCardFormat, 34);
        }
        finish();
        Toast.makeText(getApplicationContext(), getString(R.string.save_success), Toast.LENGTH_LONG).show();
    }

    public void onParamterback(View view) {
        startActivity(new Intent(this, SettingActivity.class));
        finish();
    }
}
