package com.certify.snap.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
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
    private static String TAG = "QRViewSetting";
    Typeface rubiklight;
    SharedPreferences sp;
    TextView btn_save, titles, qr_screen, tv_facial;
    private RadioGroup rfidRg;
    private RadioButton rfidYesRb;
    private RadioButton rfidNoRb;
    EditText editTextDialogTimeout;
    RadioGroup radio_group_facial,radio_group_display, radio_group_anonymous;
    RadioButton radio_yes_facial, rAnonymousYesRb;
    RadioButton radio_no_facial, rAnonymousNoRb;
    RadioButton rbguideyes,radio_yes_display,radio_no_display;
    RadioButton rbguideno;
    EditText editTextDialogUserInput;
    TextView tv_display;
    TextView mAnonymousTv;


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
            tv_display = findViewById(R.id.tv_display);
            radio_group_display = findViewById(R.id.radio_group_display);
            radio_yes_display = findViewById(R.id.radio_yes_display);
            radio_no_display = findViewById(R.id.radio_no_display);
            mAnonymousTv = findViewById(R.id.anonymous_tv);
            mAnonymousTv.setTypeface(rubiklight);
            tv_facial.setTypeface(rubiklight);
            tv_display.setTypeface(rubiklight);
            rAnonymousYesRb = findViewById(R.id.radio_yes_anonymous);
            rAnonymousNoRb = findViewById(R.id.radio_no_anonymous);



            editTextDialogTimeout.setText(sp.getString(GlobalParameters.Timeout, "5"));
            editTextDialogUserInput.setText(sp.getString(GlobalParameters.FACIAL_THRESHOLD, "70"));
            if (sp.getBoolean(GlobalParameters.QR_SCREEN, false))
                rbguideyes.setChecked(true);
            else rbguideno.setChecked(true);

            setRfidDefault();
            setRfidClickListener();
            setAnonymousDefault();
            setAnonymousClickListener();

            radio_group_qr.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    if (checkedId == R.id.radio_yes_qr)
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
                        Util.writeBoolean(sp, GlobalParameters.FACIAL_DETECT, true);
                    } else {
                        Util.writeBoolean(sp, GlobalParameters.FACIAL_DETECT, false);
                    }
                }
            });

            if (sp.getBoolean(GlobalParameters.DISPLAY_IMAGE_CONFIRMATION, false)) {
                radio_yes_display.setChecked(true);
            } else {
                radio_no_display.setChecked(true);
            }

            radio_group_display.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    if (checkedId == R.id.radio_yes_display) {
                        Util.writeBoolean(sp, GlobalParameters.DISPLAY_IMAGE_CONFIRMATION, true);
                    } else {
                        Util.writeBoolean(sp, GlobalParameters.DISPLAY_IMAGE_CONFIRMATION, false);
                    }
                }
            });
            radio_group_facial.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    System.out.println("Test CheckId" + checkedId);
                    if (checkedId == R.id.radio_yes_facial) {
                        radio_yes_facial.setChecked(true);
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

    private void setAnonymousClickListener() {
        radio_group_anonymous.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                System.out.println("Test CheckId" + checkedId);
                if (checkedId == R.id.radio_yes_anonymous) {
                    rAnonymousYesRb.setChecked(true);
                    Util.writeBoolean(sp, GlobalParameters.ANONYMOUS_ENABLE, true);
                } else {
                    Util.writeBoolean(sp, GlobalParameters.ANONYMOUS_ENABLE, false);
                }
            }
        });
    }

    private void setAnonymousDefault() {
        if (sp.getBoolean(GlobalParameters.ANONYMOUS_ENABLE, false)) {
            rAnonymousYesRb.setChecked(true);
            rAnonymousNoRb.setChecked(false);
        } else {
            rAnonymousNoRb.setChecked(true);
            rAnonymousYesRb.setChecked(false);
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
