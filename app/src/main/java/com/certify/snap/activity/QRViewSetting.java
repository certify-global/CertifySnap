package com.certify.snap.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.certify.snap.R;
import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.Logger;
import com.certify.snap.common.Util;

public class QRViewSetting extends Activity {
    private static String TAG = "GuideViewSetting";
    Typeface rubiklight;
    SharedPreferences sp;
    TextView btn_save, titles, qr_screen, tv_facial;
    private RadioGroup rfidRg;
    private RadioButton rfidYesRb;
    private RadioButton rfidNoRb;
    EditText editTextDialogTimeout;
    RadioGroup radio_group_facial;
    RadioButton radio_yes_facial;
    RadioButton radio_no_facial;
    RadioButton rbguideyes;
    RadioButton rbguideno;
    EditText editTextDialogUserInput;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr);
        try {
            rubiklight = Typeface.createFromAsset(getAssets(),
                    "rubiklight.ttf");
            sp = Util.getSharedPreferences(this);
            RadioGroup radio_group_qr = findViewById(R.id.radio_group_qr);
            rbguideyes = findViewById(R.id.radio_yes_qr);
            rbguideno = findViewById(R.id.radio_no_qr);
            editTextDialogTimeout = findViewById(R.id.editTextDialogTimeout);
            editTextDialogUserInput = findViewById(R.id.editTextDialogUserInput);
            btn_save = findViewById(R.id.btn_exit);
            qr_screen = findViewById(R.id.qr_screen);
            titles = findViewById(R.id.titles);
            btn_save.setTypeface(rubiklight);
            titles.setTypeface(rubiklight);
            qr_screen.setTypeface(rubiklight);
            TextView rfId = findViewById(R.id.rfid_tv);
            rfId.setTypeface(rubiklight);
            rfidRg = findViewById(R.id.radio_group_rfid);
            rfidYesRb = findViewById(R.id.radio_yes_rfid);
            rfidNoRb = findViewById(R.id.radio_no_rfid);
            radio_group_facial = findViewById(R.id.radio_group_facial);
            radio_yes_facial = findViewById(R.id.radio_yes_facial);
            radio_no_facial = findViewById(R.id.radio_no_facial);
            tv_facial = findViewById(R.id.tv_facial);
            tv_facial.setTypeface(rubiklight);



            editTextDialogTimeout.setText(sp.getString(GlobalParameters.Timeout, "5"));
            editTextDialogUserInput.setText(sp.getString(GlobalParameters.FACIAL_THRESHOLD, "70"));
            if (sp.getBoolean(GlobalParameters.QR_SCREEN, false))
                rbguideyes.setChecked(true);
            else rbguideno.setChecked(true);

            setRfidDefault();
            setRfidClickListener();

            radio_group_qr.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    if (checkedId == R.id.radio_yes_qr && (!sp.getBoolean(GlobalParameters.FACIAL_DETECT, false) == true))
                        Util.writeBoolean(sp, GlobalParameters.QR_SCREEN, true);
                    else
                        Util.writeBoolean(sp, GlobalParameters.QR_SCREEN, false);

                }
            });
            // Facial
            if (sp.getBoolean(GlobalParameters.FACIAL_DETECT, false)) {
                radio_yes_facial.setChecked(true);
            } else {
                radio_no_facial.setChecked(true);
            }
            radio_group_facial.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    System.out.println("Test CheckId" + checkedId);
                    if (checkedId == R.id.radio_yes_facial) {
                        radio_yes_facial.setChecked(true);
                        rbguideno.setChecked(true);
                        Util.writeBoolean(sp, GlobalParameters.FACIAL_DETECT, true);
                    } else {
                        Util.writeBoolean(sp, GlobalParameters.FACIAL_DETECT, false);
                    }
                }
            });


            btn_save.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    saveRfidSettings();
                    startActivity(new Intent(QRViewSetting.this, SettingActivity.class));
                    Util.showToast(QRViewSetting.this, getString(R.string.save_success));
                    Util.writeString(sp, GlobalParameters.Timeout, editTextDialogTimeout.getText().toString().trim());
                    Util.writeString(sp, GlobalParameters.FACIAL_THRESHOLD, editTextDialogUserInput.getText().toString().trim());
                    finish();
                }
            });
        } catch (Exception e) {
            Logger.error(TAG, e.toString());
        }
    }

    private void setRfidDefault() {
        if (sp.getBoolean(GlobalParameters.RFID_ENABLE, false)) {
            rfidYesRb.setChecked(true);
            rfidNoRb.setChecked(false);
        } else {
            rfidNoRb.setChecked(true);
            rfidYesRb.setChecked(false);
        }
    }

    private void setRfidClickListener() {
        rfidRg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int id) {
                if (id == R.id.radio_yes_rfid) {
                    rfidYesRb.setChecked(true);
                    rfidNoRb.setChecked(false);
                } else if (id == R.id.radio_no_rfid) {
                    rfidNoRb.setChecked(true);
                    rfidYesRb.setChecked(false);
                }
            }
        });
    }

    private void saveRfidSettings() {
        Util.writeBoolean(sp, GlobalParameters.RFID_ENABLE, rfidYesRb.isChecked());
    }

    public void onParamterback(View view) {
        startActivity(new Intent(QRViewSetting.this, SettingActivity.class));
        finish();
    }
}
