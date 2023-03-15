package com.certify.snap.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.certify.snap.R;
import com.certify.snap.api.response.IdentificationSettings;
import com.certify.snap.common.AppSettings;
import com.certify.snap.common.Constants;
import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.Logger;
import com.certify.snap.common.Util;
import com.certify.snap.controller.CameraController;
import com.certify.snap.controller.DatabaseController;
import com.certify.snap.controller.DeviceSettingsController;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

public class IdentificationSettingsActivity extends SettingsBaseActivity {
    private static String TAG = IdentificationSettingsActivity.class.getSimpleName();
    Typeface rubiklight;
    SharedPreferences sp;
    TextView btn_save, titles, qr_screen, tv_facial, secondaryIdTv;
    private RadioGroup rfidRg;
    private RadioButton rfidYesRb;
    private RadioButton rfidNoRb;
    EditText editTextDialogTimeout;
    RadioGroup radio_group_primary, radio_group_display, radio_group_anonymous, secIdentityRg;
    RadioButton rAnonymousYesRb;
    RadioButton rAnonymousNoRb;
    RadioButton rbguideyes, radio_yes_display, radio_no_display;
    RadioButton rbguideno;
    RadioButton rbFaceRfidPrimary, rbQrCodeRfidPrimary, rbFacePrimary, rbQrCodePrimary, rbRfidPrimary, rbNonePrimary;
    RadioButton rbQrCodeRfidSecondary, rbFaceSecondary, rbQrCodeSecondary, rbRfidSecondary, rbNoneSecondary;
    EditText editTextDialogUserInput, editTextQRButton, editTextAcknowledge;
    TextView tv_display;
    TextView mAnonymousTv, qr_skip_button_enable_text, tv_acknowledge;
    TextInputLayout text_input_timeout, text_input_qr_button, text_input_acknowledge;
    private LinearLayout anonymous_qr_bar_code_layout, display_image_layout;
    private TextView scanMode;
    private RadioGroup scanModeRg, radio_group_acknowledge;
    private RadioButton scanModeRbEasy;
    private RadioButton scanModeRbFirm;
    private RadioButton radio_no_acknowledge;
    private RadioButton radio_yes_acknowledge;
    private boolean isHomeScreenViewEnabled;
    private LinearLayout parentLayout;
    private boolean isHomeScreenTextOnlyEnabled;
    private LinearLayout acknowledgmentLayout;
    private IdentificationSettings identificationSettings = null;
    private TextView offlineQrCode, vendorQrCode;
    private RadioGroup offlineQrCodeRg, vendorQrCodeRg;
    private RadioButton offlineQrCodeYes, offlineQrCodeNo, vendorQrCodeYes, vendorQrCodeNo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_identification_settings);
        try {
            rubiklight = Typeface.createFromAsset(getAssets(),
                    "rubiklight.ttf");
            sp = Util.getSharedPreferences(this);
            rbguideyes = findViewById(R.id.radio_yes_qr);
            rbguideno = findViewById(R.id.radio_no_qr);
            editTextDialogTimeout = findViewById(R.id.editTextDialogTimeout);
            editTextDialogUserInput = findViewById(R.id.editTextDialogUserInput);
            editTextQRButton = findViewById(R.id.editTextQRButton);
            btn_save = findViewById(R.id.btn_exit);
            qr_screen = findViewById(R.id.qr_screen);
            titles = findViewById(R.id.titles);
            scanMode = findViewById(R.id.tv_scan_mode);
            secondaryIdTv = findViewById(R.id.secondary_identification_tv);

            btn_save.setTypeface(rubiklight);
            titles.setTypeface(rubiklight);
            qr_screen.setTypeface(rubiklight);
            scanMode.setTypeface(rubiklight);
            secondaryIdTv.setTypeface(rubiklight);

            TextView rfId = findViewById(R.id.rfid_tv);
            rfId.setTypeface(rubiklight);
            rfidRg = findViewById(R.id.radio_group_rfid);
            rfidYesRb = findViewById(R.id.radio_yes_rfid);
            rfidNoRb = findViewById(R.id.radio_no_rfid);
            radio_group_primary = findViewById(R.id.radio_group_primary);
            rbFaceRfidPrimary = findViewById(R.id.face_id_rb_primary);
            rbQrCodeRfidPrimary = findViewById(R.id.qr_code_rfid_rb_primary);
            rbFacePrimary = findViewById(R.id.face_rb_primary);
            rbQrCodePrimary = findViewById(R.id.qrcode_rb_primary);
            rbRfidPrimary = findViewById(R.id.rfid_rb_primary);
            rbNonePrimary = findViewById(R.id.none_primary);

            secIdentityRg = findViewById(R.id.radio_group_secondary);
            rbQrCodeRfidSecondary = findViewById(R.id.qrcode_rfid_rb_secondary);
            rbFaceSecondary = findViewById(R.id.face_rb_secondary);
            rbQrCodeSecondary = findViewById(R.id.qr_code_rb_secondary);
            rbRfidSecondary = findViewById(R.id.rfid_rb_secondary);
            rbNoneSecondary = findViewById(R.id.none_secondary);

            tv_facial = findViewById(R.id.tv_facial);
            tv_display = findViewById(R.id.tv_display);
            radio_group_display = findViewById(R.id.radio_group_display);
            radio_yes_display = findViewById(R.id.radio_yes_display);
            radio_no_display = findViewById(R.id.radio_no_display);
            mAnonymousTv = findViewById(R.id.anonymous_tv);
            scanModeRg = findViewById(R.id.radio_group_scan_mode);
            scanModeRbEasy = findViewById(R.id.radio_scanmode_easy);
            scanModeRbFirm = findViewById(R.id.radio_scanmode_strict);
            tv_acknowledge = findViewById(R.id.tv_acknowledge);
            radio_yes_acknowledge = findViewById(R.id.radio_yes_acknowledge);
            radio_no_acknowledge = findViewById(R.id.radio_no_acknowledge);
            radio_group_acknowledge = findViewById(R.id.radio_group_acknowledge);
            acknowledgmentLayout = findViewById(R.id.acknowledgement_layout);
            mAnonymousTv.setTypeface(rubiklight);
            tv_facial.setTypeface(rubiklight);
            tv_display.setTypeface(rubiklight);
            tv_acknowledge.setTypeface(rubiklight);
            rAnonymousYesRb = findViewById(R.id.radio_yes_anonymous);
            rAnonymousNoRb = findViewById(R.id.radio_no_anonymous);
            radio_group_anonymous = findViewById(R.id.radio_group_anonymous);
            parentLayout = findViewById(R.id.parent_view_layout);
            text_input_timeout = findViewById(R.id.text_input_timeout);
            text_input_qr_button = findViewById(R.id.text_input_QR_button);
            anonymous_qr_bar_code_layout = findViewById(R.id.anonymous_qr_bar_code_layout);
            editTextAcknowledge = findViewById(R.id.editTextAcknowledge);
            text_input_acknowledge = findViewById(R.id.text_input_acknowledge);
            display_image_layout = findViewById(R.id.display_image_layout);
            qr_skip_button_enable_text = findViewById(R.id.qr_skip_button_enable_text);
            qr_skip_button_enable_text.setTypeface(rubiklight);
            offlineQrCode = findViewById(R.id.offline_qrcode_tv);
            offlineQrCode.setTypeface(rubiklight);
            offlineQrCodeRg = findViewById(R.id.offline_qr_code_rg);
            offlineQrCodeYes = findViewById(R.id.offline_qr_code_rb_yes);
            offlineQrCodeNo = findViewById(R.id.offline_qr_code_rb_no);
            vendorQrCode = findViewById(R.id.vendor_title_tv);
            vendorQrCode.setTypeface(rubiklight);
            vendorQrCodeRg = findViewById(R.id.vendor_qr_rg);
            vendorQrCodeYes = findViewById(R.id.vendor_qr_rb_yes);
            vendorQrCodeNo = findViewById(R.id.vendor_qr_rb_no);

            editTextDialogTimeout.setText(sp.getString(GlobalParameters.Timeout, "5"));
            editTextAcknowledge.setText(sp.getString(GlobalParameters.ACKNOWLEDGEMENT_TEXT, "All the acknowledge"));
            editTextDialogUserInput.setText(sp.getString(GlobalParameters.FACIAL_THRESHOLD, String.valueOf(Constants.FACIAL_DETECT_THRESHOLD)));
            editTextQRButton.setText(sp.getString(GlobalParameters.QR_BUTTON_TEXT, getString(R.string.qr_button_text)));

            getIdentificationSettingsFromDb();
            setAnonymousDefault();
            setAnonymousClickListener();
            setScanModeDefault();
            setScanModeClickListener();
            getHomeScreenEnabledStatus();
            setEditTextTimeOutListener();
            qrSkipCode();

            setPrimaryIdentifierListener();
            setSecondaryIdentifierListener();
            setOfflineQrCodeListener();
            setVendorQrCodeListener();
            visitorValidations();
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

            //Acknowledge
            if (sp.getBoolean(GlobalParameters.ACKNOWLEDGEMENT_SCREEN, false)) {
                radio_yes_acknowledge.setChecked(true);
            } else {
                radio_no_acknowledge.setChecked(true);
            }
            radio_group_acknowledge.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    System.out.println("Test radioack" + checkedId);
                    if (checkedId == R.id.radio_yes_acknowledge) {
                        radio_yes_acknowledge.setChecked(true);
                        Util.writeBoolean(sp, GlobalParameters.ACKNOWLEDGEMENT_SCREEN, true);
                    } else {
                        Util.writeBoolean(sp, GlobalParameters.ACKNOWLEDGEMENT_SCREEN, false);
                    }
                }
            });

            btn_save = findViewById(R.id.btn_exit);
            btn_save.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!isRfidCodeTimeOutInRange()) {
                        Toast.makeText(getApplicationContext(), getString(R.string.screen_timeout_msg), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    startActivity(new Intent(IdentificationSettingsActivity.this, SettingsActivity.class));
                    Util.showToast(IdentificationSettingsActivity.this, getString(R.string.save_success));
                    Util.writeString(sp, GlobalParameters.Timeout, editTextDialogTimeout.getText().toString().trim());
                    Util.writeString(sp, GlobalParameters.FACIAL_THRESHOLD, editTextDialogUserInput.getText().toString().trim());
                    Util.writeString(sp, GlobalParameters.QR_BUTTON_TEXT, editTextQRButton.getText().toString().trim());
                    Util.writeString(sp, GlobalParameters.ACKNOWLEDGEMENT_TEXT, editTextAcknowledge.getText().toString().trim());
                    saveScanModeSetting();

                    if (identificationSettings != null) {
                        identificationSettings.acknowledgementText = editTextAcknowledge.getText().toString().trim();
                        DatabaseController.getInstance().updateIdentificationSettings(identificationSettings);
                    }
                    finish();
                }
            });

            proIdentificationSettings();
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }

    private void setAnonymousClickListener() {
        radio_group_anonymous.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radio_yes_anonymous) {
                    if (!isHomeScreenViewEnabled) {
                        showSnackBarMessage(getString(R.string.enable_home_view_msg));
                        rAnonymousNoRb.setChecked(true);
                        return;
                    }
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

    private void setScanModeDefault() {
        if (sp.getInt(GlobalParameters.ScanMode, Constants.DEFAULT_SCAN_MODE) == 1) {
            scanModeRbEasy.setChecked(true);
            scanModeRbFirm.setChecked(false);
        } else {
            scanModeRbFirm.setChecked(true);
            scanModeRbEasy.setChecked(false);
        }
    }

    private void setScanModeClickListener() {
        scanModeRg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                if (checkedId == R.id.radio_scanmode_easy) {
                    scanModeRbEasy.setChecked(true);
                    scanModeRbFirm.setChecked(false);
                } else if (checkedId == R.id.radio_scanmode_strict) {
                    scanModeRbFirm.setChecked(true);
                    scanModeRbEasy.setChecked(false);
                }
            }
        });
    }

    private void saveScanModeSetting() {
        if (scanModeRbEasy.isChecked()) {
            Util.writeInt(sp, GlobalParameters.ScanMode, 1);
        } else if (scanModeRbFirm.isChecked()) {
            Util.writeInt(sp, GlobalParameters.ScanMode, 2);
        }
    }

    private void getHomeScreenEnabledStatus() {
        isHomeScreenViewEnabled = sp.getBoolean(GlobalParameters.HOME_TEXT_IS_ENABLE, true);
        isHomeScreenTextOnlyEnabled = sp.getBoolean(GlobalParameters.HOME_TEXT_ONLY_IS_ENABLE, false);
    }

    private void showSnackBarMessage(String message) {
        Snackbar.make(parentLayout, message, Snackbar.LENGTH_SHORT).show();
    }

    private void setEditTextTimeOutListener() {
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    if (charSequence.toString().isEmpty()) {
                        text_input_timeout.setError("");
                        return;
                    }
                    if (Integer.parseInt(charSequence.toString()) < 5) {
                        text_input_timeout.setError(getResources().getString(R.string.screen_timeout_msg));
                    }
                } catch (NumberFormatException ex) {
                    Log.d(TAG, ex.getMessage());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        };
        editTextDialogTimeout.addTextChangedListener(textWatcher);
    }

    private boolean isRfidCodeTimeOutInRange() {
        boolean result = false;
        if (editTextDialogTimeout.getText().toString().isEmpty()) return false;
        if (Integer.parseInt(editTextDialogTimeout.getText().toString()) >= 5) {
            result = true;
        }
        return result;
    }

    private void qrSkipCode() {
        RadioGroup qr_skip_button_radio_group = findViewById(R.id.qr_skip_button_radio_group);
        RadioButton qr_skip_button_radio_enable = findViewById(R.id.qr_skip_button_radio_yes);
        RadioButton qr_skip_button_radio_disable = findViewById(R.id.qr_skip_button_radio_no);

        if (sp.getBoolean(GlobalParameters.QR_SKIP_BUTTON_ENABLE_DISABLE, false))
            qr_skip_button_radio_enable.setChecked(true);
        else qr_skip_button_radio_disable.setChecked(true);

        qr_skip_button_radio_group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.qr_skip_button_radio_yes)
                    Util.writeBoolean(sp, GlobalParameters.QR_SKIP_BUTTON_ENABLE_DISABLE, true);
                else Util.writeBoolean(sp, GlobalParameters.QR_SKIP_BUTTON_ENABLE_DISABLE, false);
            }
        });
    }

    private void proIdentificationSettings() {
        if (Util.isDeviceProModel()) {
            if (AppSettings.isProSettings()) {
                Log.d(TAG, "proSettings: true");
                anonymous_qr_bar_code_layout.setVisibility(View.GONE);
                text_input_timeout.setVisibility(View.GONE);
            } else {
                Log.d(TAG, "proSettings: false");
                anonymous_qr_bar_code_layout.setVisibility(View.VISIBLE);
                text_input_timeout.setVisibility(View.VISIBLE);
            }
        } else {
            anonymous_qr_bar_code_layout.setVisibility(View.VISIBLE);
            text_input_timeout.setVisibility(View.VISIBLE);
        }
    }

    private void getIdentificationSettingsFromDb() {
        String languageType = AppSettings.getLanguageType();
        identificationSettings = DatabaseController.getInstance().getIdentificationSettingsId(
                DeviceSettingsController.getInstance().getLanguageIdOnCode(languageType));
    }

    private void setPrimaryIdentifier() {
        int value = Integer.parseInt(sp.getString(GlobalParameters.PRIMARY_IDENTIFIER, "1"));
        if (value == 1) {
            rbFaceRfidPrimary.setChecked(true);

            rbQrCodeRfidSecondary.setVisibility(View.GONE);
            rbFaceSecondary.setVisibility(View.GONE);
            rbRfidSecondary.setVisibility(View.GONE);
            rbQrCodeSecondary.setEnabled(true);
        } else if (value == 2) {
            rbQrCodeRfidPrimary.setChecked(true);

            rbQrCodeRfidSecondary.setVisibility(View.GONE);
            rbRfidSecondary.setVisibility(View.GONE);
            rbQrCodeSecondary.setVisibility(View.GONE);
            rbFaceSecondary.setEnabled(true);
        } else if (value == 3) {
            rbFacePrimary.setChecked(true);

            rbFaceSecondary.setVisibility(View.GONE);
            rbRfidSecondary.setEnabled(true);
            rbQrCodeSecondary.setEnabled(true);
            rbQrCodeRfidSecondary.setEnabled(true);
        } else if (value == 5) {
            rbQrCodePrimary.setChecked(true);

            rbRfidSecondary.setVisibility(View.GONE);
            rbQrCodeRfidSecondary.setVisibility(View.GONE);
            rbFaceSecondary.setEnabled(true);
            rbQrCodeSecondary.setEnabled(true);
        } else if (value == 4) {
            rbRfidPrimary.setChecked(true);

            rbQrCodeRfidSecondary.setVisibility(View.GONE);
            rbQrCodeSecondary.setVisibility(View.GONE);
            rbFaceSecondary.setEnabled(true);
            rbRfidSecondary.setEnabled(true);
        } else if (value == 6) {
            rbNonePrimary.setChecked(true);

            secondaryIdTv.setVisibility(View.GONE);
            secIdentityRg.setVisibility(View.GONE);
        }
    }

    private void setPrimaryIdentifierListener() {
        setPrimaryIdentifier();

        radio_group_primary.setOnCheckedChangeListener((radioGroup, id) -> {
            if (id != R.id.none_primary) {
                secondaryIdTv.setVisibility(View.VISIBLE);
                secIdentityRg.setVisibility(View.VISIBLE);
            }
            if (id == R.id.face_id_rb_primary) {
                rbFaceRfidPrimary.setChecked(true);
                Util.writeString(sp, GlobalParameters.PRIMARY_IDENTIFIER, "1");
                rbQrCodeRfidSecondary.setVisibility(View.GONE);
                rbFaceSecondary.setVisibility(View.GONE);
                rbRfidSecondary.setVisibility(View.GONE);
                rbQrCodeSecondary.setVisibility(View.VISIBLE);
                rbQrCodeSecondary.setEnabled(true);
            } else if (id == R.id.qr_code_rfid_rb_primary) {
                if (!isHomeScreenViewEnabled) {
                    showSnackBarMessage(getString(R.string.enable_home_view_msg));
                    setPrimaryIdentifier();
                    return;
                }
                rbQrCodeRfidPrimary.setChecked(true);
                Util.writeString(sp, GlobalParameters.PRIMARY_IDENTIFIER, "2");
                rbQrCodeRfidSecondary.setVisibility(View.GONE);
                rbRfidSecondary.setVisibility(View.GONE);
                rbQrCodeSecondary.setVisibility(View.GONE);
                rbFaceSecondary.setVisibility(View.VISIBLE);
                rbFaceSecondary.setEnabled(true);
            } else if (id == R.id.face_rb_primary) {
                rbFacePrimary.setChecked(true);
                Util.writeString(sp, GlobalParameters.PRIMARY_IDENTIFIER, "3");
                rbFaceSecondary.setVisibility(View.GONE);
                rbRfidSecondary.setVisibility(View.VISIBLE);
                rbRfidSecondary.setEnabled(true);
                rbQrCodeSecondary.setVisibility(View.VISIBLE);
                rbQrCodeSecondary.setEnabled(true);
                rbQrCodeRfidSecondary.setVisibility(View.VISIBLE);
                rbQrCodeRfidSecondary.setEnabled(true);
            } else if (id == R.id.qrcode_rb_primary) {
                if (!isHomeScreenViewEnabled) {
                    showSnackBarMessage(getString(R.string.enable_home_view_msg));
                    setPrimaryIdentifier();
                    return;
                }
                rbQrCodePrimary.setChecked(true);
                Util.writeString(sp, GlobalParameters.PRIMARY_IDENTIFIER, "5");
                rbQrCodeRfidSecondary.setVisibility(View.GONE);
                rbQrCodeSecondary.setVisibility(View.GONE);
                rbFaceSecondary.setVisibility(View.VISIBLE);
                rbFaceSecondary.setEnabled(true);
                rbRfidSecondary.setVisibility(View.VISIBLE);
                rbRfidSecondary.setEnabled(true);
            } else if (id == R.id.rfid_rb_primary) {
                rbRfidPrimary.setChecked(true);
                Util.writeString(sp, GlobalParameters.PRIMARY_IDENTIFIER, "4");
                rbRfidSecondary.setVisibility(View.GONE);
                rbQrCodeRfidSecondary.setVisibility(View.GONE);
                rbFaceSecondary.setVisibility(View.VISIBLE);
                rbFaceSecondary.setEnabled(true);
                rbQrCodeSecondary.setVisibility(View.VISIBLE);
                rbQrCodeSecondary.setEnabled(true);
            } else if (id == R.id.none_primary) {
                rbNonePrimary.setChecked(true);
                Util.writeString(sp, GlobalParameters.PRIMARY_IDENTIFIER, "6");
                CameraController.getInstance().setScanProcessState(CameraController.ScanProcessState.IDLE);

                secondaryIdTv.setVisibility(View.GONE);
                secIdentityRg.setVisibility(View.GONE);
            }
        });
    }

    private void setSecondaryIdentifier () {
        int value = Integer.parseInt(sp.getString(GlobalParameters.SECONDARY_IDENTIFIER, "0"));
        if (value == 0) {
            rbNoneSecondary.setChecked(true);
        } else {
            if (value == 1) {
                rbQrCodeRfidSecondary.setChecked(true);
            } else if (value == 2) {
                rbFaceSecondary.setChecked(true);
            } else if (value == 3) {
                rbRfidSecondary.setChecked(true);
            } else if (value == 4) {
                rbQrCodeSecondary.setChecked(true);
            } else if (value == 5) {
                rbNoneSecondary.setChecked(true);
            }
        }
    }

    private void setSecondaryIdentifierListener() {
        setSecondaryIdentifier();

        secIdentityRg.setOnCheckedChangeListener((radioGroup, id) -> {
            if (id == R.id.qrcode_rfid_rb_secondary) {
                rbQrCodeRfidSecondary.setChecked(true);
                Util.writeString(sp, GlobalParameters.SECONDARY_IDENTIFIER, "1");
            } else if (id == R.id.face_rb_secondary) {
                rbFaceSecondary.setChecked(true);
                Util.writeString(sp, GlobalParameters.SECONDARY_IDENTIFIER, "2");
            } else if (id == R.id.rfid_rb_secondary) {
                rbRfidSecondary.setChecked(true);
                Util.writeString(sp, GlobalParameters.SECONDARY_IDENTIFIER, "3");
            } else if (id == R.id.qr_code_rb_secondary) {
                if (!isHomeScreenViewEnabled) {
                    showSnackBarMessage(getString(R.string.enable_home_view_msg));
                    setSecondaryIdentifier();
                    return;
                }
                rbQrCodeSecondary.setChecked(true);
                Util.writeString(sp, GlobalParameters.SECONDARY_IDENTIFIER, "4");
            } else if (id == R.id.none_secondary) {
                rbNoneSecondary.setChecked(true);
                Util.writeString(sp, GlobalParameters.SECONDARY_IDENTIFIER, "5");
            }
        });
    }

    private void setOfflineQrCodeListener() {
        if (sp.getBoolean(GlobalParameters.OFFLINE_QR_CODE, false)) {
            offlineQrCodeYes.setChecked(true);
        } else {
            offlineQrCodeNo.setChecked(true);
        }

        offlineQrCodeRg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.offline_qr_code_rb_yes) {
                    if (!isHomeScreenViewEnabled) {
                        showSnackBarMessage(getString(R.string.enable_home_view_msg));
                        offlineQrCodeNo.setChecked(true);
                        return;
                    }
                    Util.writeBoolean(sp, GlobalParameters.OFFLINE_QR_CODE, true);
                } else {
                    Util.writeBoolean(sp, GlobalParameters.OFFLINE_QR_CODE, false);
                }
            }
        });
    }

    private void setVendorQrCodeListener() {
        if (sp.getBoolean(GlobalParameters.ENABLE_VENDOR_QR, false)) {
            vendorQrCodeYes.setChecked(true);
            vendorQrCodeNo.setChecked(false);
        } else {
            vendorQrCodeNo.setChecked(true);
            vendorQrCodeYes.setChecked(false);
        }

        vendorQrCodeRg.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.vendor_qr_rb_yes) {
                if (!isHomeScreenViewEnabled) {
                    showSnackBarMessage(getString(R.string.enable_home_view_msg));
                    vendorQrCodeNo.setChecked(true);
                    return;
                }
            }
            Util.writeBoolean(sp, GlobalParameters.ENABLE_VENDOR_QR, checkedId == R.id.vendor_qr_rb_yes);
        });
    }

    private void visitorValidations() {
        try {
            RadioGroup rgVisitor = findViewById(R.id.visitor_qr_rg);
            LinearLayout llVisitorMode = findViewById(R.id.visitor_mode_layout);
            LinearLayout llVisitorCheckOut = findViewById(R.id.visitor_check_out_layout);
            RadioGroup rgVisitorMode = findViewById(R.id.visitor_mode_qr_rg);
            RadioGroup rgVisitorCheck = findViewById(R.id.visitor_check_out_qr_rg);
            RadioButton visitorQrYes = findViewById(R.id.visitor_qr_rb_yes);
            RadioButton visitorQrNo = findViewById(R.id.visitor_qr_rb_no);
            RadioButton visitorModeAuto = findViewById(R.id.visitor_mode_qr_rb_auto);
            RadioButton visitorQrManual = findViewById(R.id.visitor_mode_qr_rb_manual_mode);
            RadioButton visitorCheckYes = findViewById(R.id.visitor_check_out_qr_rb_yes);
            RadioButton visitorCheckNo = findViewById(R.id.visitor_check_out_qr_rb_no);
            if (sp.getBoolean(GlobalParameters.ENABLE_VISITOR_QR, false)) {
                visitorQrYes.setChecked(true);
                llVisitorMode.setVisibility(View.VISIBLE);
            } else {
                visitorQrNo.setChecked(true);
                llVisitorMode.setVisibility(View.GONE);
                llVisitorCheckOut.setVisibility(View.GONE);
            }
            if (sp.getBoolean(GlobalParameters.ENABLE_VISITOR_MODE_MANUAL, false)) {
                visitorQrManual.setChecked(true);
                llVisitorCheckOut.setVisibility(View.VISIBLE);
            } else {
                visitorModeAuto.setChecked(true);
                llVisitorCheckOut.setVisibility(View.GONE);

            }
            if (sp.getBoolean(GlobalParameters.ENABLE_VISITOR_MODE_CHECK_OUT, false))
                visitorCheckYes.setChecked(true);
            else visitorCheckNo.setChecked(true);
            rgVisitor.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    if (checkedId == R.id.visitor_qr_rb_yes) {
                        if (!isHomeScreenViewEnabled) {
                            showSnackBarMessage(getString(R.string.enable_home_view_msg));
                            visitorQrNo.setChecked(true);
                            return;
                        }
                        Util.writeBoolean(sp, GlobalParameters.ENABLE_VISITOR_QR, true);
                        llVisitorMode.setVisibility(View.VISIBLE);
                        if (sp.getBoolean(GlobalParameters.ENABLE_VISITOR_MODE_MANUAL, false))
                            llVisitorCheckOut.setVisibility(View.VISIBLE);
                        else llVisitorCheckOut.setVisibility(View.GONE);

                    } else {
                        Util.writeBoolean(sp, GlobalParameters.ENABLE_VISITOR_QR, false);
                        llVisitorMode.setVisibility(View.GONE);
                        llVisitorCheckOut.setVisibility(View.GONE);
                    }
                }
            });
            rgVisitorMode.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    if (checkedId == R.id.visitor_mode_qr_rb_manual_mode) {
                        Util.writeBoolean(sp, GlobalParameters.ENABLE_VISITOR_MODE_MANUAL, true);
                        llVisitorCheckOut.setVisibility(View.VISIBLE);
                    } else {
                        Util.writeBoolean(sp, GlobalParameters.ENABLE_VISITOR_MODE_MANUAL, false);
                        llVisitorCheckOut.setVisibility(View.GONE);
                    }
                }
            });
            rgVisitorCheck.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    if (checkedId == R.id.visitor_check_out_qr_rb_yes) {
                        Util.writeBoolean(sp, GlobalParameters.ENABLE_VISITOR_MODE_CHECK_OUT, true);

                    } else {
                        Util.writeBoolean(sp, GlobalParameters.ENABLE_VISITOR_MODE_CHECK_OUT, false);

                    }
                }
            });
        } catch (Exception e) {
            Logger.error(TAG, e.getMessage());
        }
    }
}
