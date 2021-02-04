package com.certify.snap.activity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.certify.callback.JSONObjectCallback;
import com.certify.callback.SettingCallback;
import com.certify.snap.R;
import com.certify.snap.common.AppSettings;
import com.certify.snap.common.Application;
import com.certify.snap.common.Constants;
import com.certify.snap.common.EndPoints;
import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.Logger;
import com.certify.snap.common.ShellUtils;
import com.certify.snap.common.Util;
import com.certify.snap.controller.DatabaseController;
import com.certify.snap.controller.ApplicationController;
import com.certify.snap.controller.DeviceSettingsController;
import com.certify.snap.localserver.LocalServer;
import com.certify.snap.service.DeviceHealthService;
import com.certify.snap.service.MemberSyncService;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class DeviceSettingsActivity extends SettingsBaseActivity implements JSONObjectCallback, SettingCallback {
    private static final String TAG = DeviceSettingsActivity.class.getSimpleName();
    private EditText etEndUrl, etDeviceName, etPassword;
    private SharedPreferences sharedPreferences;
    private TextView btn_save, tvSettingsName, activateStatus, tv_device_activation_status, pro_settings;
    private RelativeLayout ll;
    private Switch switch_activate;
    private TextView tvDeviceManager, tvEnd, tvDeviceName, tvPass, tvSettingStr, tv_activate_tv_device, tvResetSnap, tv_reset_members, tv_clear_members,
            navigation_bar_textview, sync_online_members_textview, led_switch_textview, saveLogsTv, logOfflineData;
    private Button tvClearData, not_activate;
    private Typeface rubiklight;
    private String url_end;
    private String url;
    private Boolean isOnline;
    private LinearLayout pro_layout;
    private TextView tvProtocol, tvHostName;
    private View pro_settings_border;
    RadioGroup sync_member_radio_group, logOfflineDataRg;
    RadioButton sync_member_radio_yes, sync_member_radio_no, logOfflineDataYes, logOfflineDataNo;
    RadioGroup radio_group_local_server;
    RadioButton radio_yes_server, radio_no_server;
    TextView tvLocalServer, tvServerIp, tvLocaleSettings,additional_locale_settings;
    private boolean proSettingValueSp = false;
    private boolean proSettingValue = false;
    private SeekBar seekBar;
    private int ledLevel = 0;
    private boolean serverSettingValue = false;
    private ImageView ivClipBoard;
    private String serverAddress = "";
    private RelativeLayout addrRelativeLayout;
    private LinearLayout localServerLayout;
    private boolean deviceOnlineSwitch = false;
    private Button saveLogButton;
    private LinearLayout logOfflineDataLayout, captureLogsLayout;
    private Spinner spinnerLanguageSelector,additional_spinner_language_selector;
    private String currentlanguageCode = "";
    private Integer selectedLanguageId = 0;
    private String primaryLanguage = "";
    private String secondaryLanguage = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_device_settings);

            initView();
            proSettings();
            navigationBarSettings();
            syncMemberSettings();
            ledSwitchSettings();
            seekBarSettings();
            setDefaultLedBrightnessLevel();
            localServerSetting();
            captureLogSetting();
            logOfflineDataSetting();
            languageSetting();
            additionalLanguageSetting();

            tvProtocol = findViewById(R.id.tv_protocol);
            tvHostName = findViewById(R.id.tv_hostName);
            tvDeviceManager.setPaintFlags(tvDeviceManager.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            setUIData();
            copyLocalServerAddress();

            switch_activate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    deviceOnlineSwitch = true;
                    if (isChecked) {
                        isOnline = true;
                        localServerLayout.setVisibility(View.GONE);
                        logOfflineDataLayout.setVisibility(View.GONE);
                        captureLogsLayout.setVisibility(View.VISIBLE);
                        LocalServer localServer = new LocalServer(DeviceSettingsActivity.this);
                        localServer.stopServer();
                        Util.activateApplication(DeviceSettingsActivity.this, DeviceSettingsActivity.this);
                        activateStatus();

                    } else {
                        isOnline = false;
                        localServerLayout.setVisibility(View.VISIBLE);
                        logOfflineDataLayout.setVisibility(View.VISIBLE);
                        captureLogsLayout.setVisibility(View.GONE);
                        activateStatus();
                        // Util.writeBoolean(sharedPreferences, GlobalParameters.ONLINE_SWITCH, false);
                        Toast.makeText(getApplicationContext(), getString(R.string.offline_msg), Toast.LENGTH_LONG).show();
                        Util.writeBoolean(sharedPreferences, GlobalParameters.ONLINE_MODE, false);
                        Util.writeString(sharedPreferences, GlobalParameters.DEVICE_SETTINGS_NAME, "Local");
                        tvSettingsName.setText("Local");
                        stopHealthCheckService();

                    }
                }
            });


            etEndUrl.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    url = etEndUrl.getText().toString().trim();
                    if (url.endsWith("/"))
                        url = url.substring(0, url.length() - 1);
                    url = getString(R.string.protocol_text) + url + getString(R.string.hostname);
                    Util.writeString(sharedPreferences, GlobalParameters.URL, url);
                }
            });
            etPassword.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    try {
                        String strUserInput = etPassword.getText().toString().trim();
                        if (TextUtils.isEmpty(strUserInput)) {
                            etPassword.setError(getString(R.string.password_empty_msg));
                            return;
                        } else if (etPassword.getText().toString().length() < 6) {
                            etPassword.setError(getString(R.string.password_length_msg));
                        } else {
                            etPassword.setError(null);
                            Util.writeString(sharedPreferences, GlobalParameters.DEVICE_PASSWORD, etPassword.getText().toString().trim());
                        }
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage());

                    }
                }
            });
            btn_save.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (primaryLanguage.equals(secondaryLanguage)) {
                        Toast.makeText(DeviceSettingsActivity.this, getString(R.string.language_message), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    saveLedBrightnessSetting();
                    Util.writeString(sharedPreferences, GlobalParameters.DEVICE_NAME, etDeviceName.getText().toString().trim());
                    Util.writeBoolean(sharedPreferences, GlobalParameters.PRO_SETTINGS, proSettingValue);
                    Util.writeBoolean(sharedPreferences, GlobalParameters.LogOfflineData, logOfflineDataYes.isChecked());
                    AppSettings.setProSettings(proSettingValue);
                    if (!TextUtils.isEmpty(url_end) && !url_end.equals(getString(R.string.protocol_text) + etEndUrl.getText().toString().trim() + getString(R.string.hostname))) {
                        deleteAppData();
                        if (serverSettingValue)
                            Util.writeBoolean(sharedPreferences, GlobalParameters.LOCAL_SERVER_SETTINGS, true);
                        Util.writeString(sharedPreferences, GlobalParameters.URL, url);
                        ApplicationController.getInstance().setEndPointUrl(url);
                        restartApp();
                    } else {
                        String languageCode = sharedPreferences.getString(GlobalParameters.LANGUAGE_TYPE, "en");
                        //if (!currentlanguageCode.equals(languageCode)) {
                            DeviceSettingsController.getInstance().getSettingsFromDb(selectedLanguageId);
                        //}
                        if ((proSettingValueSp != proSettingValue) || (serverSettingValue
                                && deviceOnlineSwitch) || (!currentlanguageCode.equals(languageCode))) {
                            restartApp();
                            return;
                        }
                        finish();
                    }
                }
            });
            tvClearData.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteAppData();
                    restartApp();
                }
            });

            enableProDevice();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

    }

    private void initView() {
        etEndUrl = findViewById(R.id.et_end_url);
        etDeviceName = findViewById(R.id.et_device_name);
        etPassword = findViewById(R.id.et_device_password);
        btn_save = findViewById(R.id.btn_save_device);
        tvSettingsName = findViewById(R.id.tv_device_settings);
        switch_activate = findViewById(R.id.switch_activate_device);
        activateStatus = findViewById(R.id.activate_status);
        not_activate = findViewById(R.id.not_activate);
        tv_device_activation_status = findViewById(R.id.tv_device_activation_status);
        tvDeviceManager = findViewById(R.id.tv_device_manage);
        tv_activate_tv_device = findViewById(R.id.activate_tv_device);
        tvEnd = findViewById(R.id.tv_end_device);
        tvDeviceName = findViewById(R.id.tv_device_name_dev);
        tvPass = findViewById(R.id.tv_password_device);
        tvSettingStr = findViewById(R.id.tv_device_settings_str);
        sharedPreferences = Util.getSharedPreferences(this);
        tvClearData = findViewById(R.id.tv_clear_cache);
        tvResetSnap = findViewById(R.id.tv_reset_snap);
        pro_settings = findViewById(R.id.pro_settings);
        pro_layout = findViewById(R.id.pro_layout);
        pro_settings_border = findViewById(R.id.pro_settings_border);
        tv_reset_members = findViewById(R.id.tv_reset_members);
        tv_clear_members = findViewById(R.id.tv_clear_members);
        navigation_bar_textview = findViewById(R.id.navigation_bar_textview);
        sync_online_members_textview = findViewById(R.id.sync_online_members_textview);
        led_switch_textview = findViewById(R.id.led_switch_textview);
        seekBar = findViewById(R.id.seekbar);
        tvLocalServer = findViewById(R.id.local_server_tv);
        tvServerIp = findViewById(R.id.tv_server_ip);
        radio_group_local_server = findViewById(R.id.local_server_radio_group);
        radio_yes_server = findViewById(R.id.radio_yes_server_setting);
        radio_no_server = findViewById(R.id.radio_no_server_setting);
        ivClipBoard = findViewById(R.id.iv_clipBoard);
        addrRelativeLayout = findViewById(R.id.addr_relative_layout);
        localServerLayout = findViewById(R.id.local_server_parent_layout);
        saveLogsTv = findViewById(R.id.capture_logs);
        saveLogButton = findViewById(R.id.send_log_button);
        logOfflineDataLayout = findViewById(R.id.log_offline_data_parent_layout);
        logOfflineData = findViewById(R.id.log_offline_data);
        logOfflineDataRg = findViewById(R.id.log_offline_data_rg);
        logOfflineDataYes = findViewById(R.id.log_od_rb_yes);
        logOfflineDataNo = findViewById(R.id.log_od_rb_no);
        captureLogsLayout = findViewById(R.id.capture_logs_layout);
        spinnerLanguageSelector = findViewById(R.id.spinner_language_selector);
        additional_spinner_language_selector = findViewById(R.id.additional_spinner_language_selector);
        tvLocaleSettings = findViewById(R.id.locale_settings);
        additional_locale_settings = findViewById(R.id.additional_locale_settings);

        rubiklight = Typeface.createFromAsset(getAssets(),
                "rubiklight.ttf");
        activateStatus.setTypeface(rubiklight);
        tvDeviceManager.setTypeface(rubiklight);
        tv_activate_tv_device.setTypeface(rubiklight);
        tvDeviceName.setTypeface(rubiklight);
        tvPass.setTypeface(rubiklight);
        tvSettingStr.setTypeface(rubiklight);
        tvSettingsName.setTypeface(rubiklight);
        tvResetSnap.setTypeface(rubiklight);
        tvClearData.setTypeface(rubiklight);
        not_activate.setTypeface(rubiklight);
        tv_device_activation_status.setTypeface(rubiklight);
        tvEnd.setTypeface(rubiklight);
        pro_settings.setTypeface(rubiklight);
        tv_reset_members.setTypeface(rubiklight);
        tv_clear_members.setTypeface(rubiklight);
        navigation_bar_textview.setTypeface(rubiklight);
        sync_online_members_textview.setTypeface(rubiklight);
        led_switch_textview.setTypeface(rubiklight);
        tvLocalServer.setTypeface(rubiklight);
        tvServerIp.setTypeface(rubiklight);
        saveLogsTv.setTypeface(rubiklight);
        saveLogButton.setTypeface(rubiklight);
        logOfflineData.setTypeface(rubiklight);
        tvLocaleSettings.setTypeface(rubiklight);
        additional_locale_settings.setTypeface(rubiklight);

        if (!sharedPreferences.getBoolean(GlobalParameters.ONLINE_MODE, true)) {
            localServerLayout.setVisibility(View.VISIBLE);
            logOfflineDataLayout.setVisibility(View.VISIBLE);
            captureLogsLayout.setVisibility(View.GONE);
        }
    }

    private void proSettings() {
        RadioGroup radio_group_pro = findViewById(R.id.radio_group_pro_settings);
        RadioButton radio_yes_pro = findViewById(R.id.radio_yes_pro_settings);
        RadioButton radio_no_pro = findViewById(R.id.radio_no_pro_settings);

        if (sharedPreferences.getBoolean(GlobalParameters.PRO_SETTINGS, false)) {
            radio_yes_pro.setChecked(true);
            proSettingValueSp = true;
        } else {
            radio_no_pro.setChecked(true);
            proSettingValueSp = false;
        }

        radio_group_pro.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radio_yes_pro_settings) {
                    proSettingValue = true;
                } else {
                    proSettingValue = false;
                }
            }

        });

    }

    private void navigationBarSettings() {
        RadioGroup radio_group_navigationbar = findViewById(R.id.navigation_bar_radio_group);
        RadioButton radio_navigationbar_enable = findViewById(R.id.navigation_bar_radio_enable);
        RadioButton radio_navigationbar_disable = findViewById(R.id.navigation_bar_radio_disable);
        boolean navigationBar = sharedPreferences.getBoolean(GlobalParameters.NavigationBar, true);

        if (navigationBar) {
            radio_navigationbar_enable.setChecked(true);
            sendBroadcast(new Intent(GlobalParameters.ACTION_SHOW_NAVIGATIONBAR));
            sendBroadcast(new Intent(GlobalParameters.ACTION_OPEN_STATUSBAR));
        } else {
            radio_navigationbar_disable.setChecked(true);
            sendBroadcast(new Intent(GlobalParameters.ACTION_HIDE_NAVIGATIONBAR));
            sendBroadcast(new Intent(GlobalParameters.ACTION_CLOSE_STATUSBAR));
        }

        radio_group_navigationbar.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.navigation_bar_radio_enable:
                        Util.writeBoolean(sharedPreferences, GlobalParameters.NavigationBar, true);
                        sendBroadcast(new Intent(GlobalParameters.ACTION_SHOW_NAVIGATIONBAR));
                        sendBroadcast(new Intent(GlobalParameters.ACTION_OPEN_STATUSBAR));
                        break;
                    case R.id.navigation_bar_radio_disable:
                        Util.writeBoolean(sharedPreferences, GlobalParameters.NavigationBar, false);
                        sendBroadcast(new Intent(GlobalParameters.ACTION_HIDE_NAVIGATIONBAR));
                        sendBroadcast(new Intent(GlobalParameters.ACTION_CLOSE_STATUSBAR));
                        break;
                }
            }
        });

    }

    private void ledSwitchSettings() {
        RadioGroup led_radio_group = findViewById(R.id.radio_group_led);
        RadioButton radio_led_on = findViewById(R.id.radio_led_on);
        RadioButton radio_led_off = findViewById(R.id.radio_led_off);
        boolean ledSwitch = sharedPreferences.getBoolean(GlobalParameters.LedType, true);

        if (ledSwitch) {
            radio_led_on.setChecked(true);
        } else {
            radio_led_off.setChecked(true);
            Util.setLedPower(0);
        }

        led_radio_group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.radio_led_on:
                        Util.writeBoolean(sharedPreferences, GlobalParameters.LedType, true);
                        break;
                    case R.id.radio_led_off:
                        Util.writeBoolean(sharedPreferences, GlobalParameters.LedType, false);
                        Util.setLedPower(0);
                        break;
                }
            }
        });
    }

    private void syncMemberSettings() {

        sync_member_radio_group = findViewById(R.id.sync_online_members_radio_group);
        sync_member_radio_yes = findViewById(R.id.sync_online_members_radio_enable);
        sync_member_radio_no = findViewById(R.id.sync_online_members_radio_disable);

        if (sharedPreferences.getBoolean(GlobalParameters.SYNC_ONLINE_MEMBERS, false))
            sync_member_radio_yes.setChecked(true);
        else sync_member_radio_no.setChecked(true);

        sync_member_radio_group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.sync_online_members_radio_enable) {
                    Util.writeBoolean(sharedPreferences, GlobalParameters.SYNC_ONLINE_MEMBERS, true);
                } else {
                    Util.writeBoolean(sharedPreferences, GlobalParameters.SYNC_ONLINE_MEMBERS, false);
                }
            }

        });

    }

    private void setUIData() {
        try {
            if (ApplicationController.getInstance().getEndPointUrl().isEmpty()) {
                url_end = sharedPreferences.getString(GlobalParameters.URL, EndPoints.prod_url);
            } else {
                url_end = ApplicationController.getInstance().getEndPointUrl();
            }
            String removeHttps = url_end.replaceFirst("^(http[s]?://www\\.|http[s]?://|www\\.)", "");
            String endApiString = removeHttps.replace(getString(R.string.hostname), "");
            etEndUrl.setText(endApiString);
            if (endApiString != null && endApiString.length() > 0)
                etEndUrl.setSelection(endApiString.length());
            etDeviceName.setText(sharedPreferences.getString(GlobalParameters.DEVICE_NAME, ""));
            tvSettingsName.setText(sharedPreferences.getString(GlobalParameters.DEVICE_SETTINGS_NAME, "Local"));
            if (sharedPreferences.getBoolean(GlobalParameters.ONLINE_MODE, true)) {
                switch_activate.setChecked(true);
                activateStatus.setText(getString(R.string.activated));
                not_activate.setVisibility(View.GONE);
            } else {
                switch_activate.setChecked(false);
                activateStatus.setText(getString(R.string.not_activated_msg));
                not_activate.setText(getString(R.string.activate_msg));
                tvSettingsName.setText(getString(R.string.local));
            }
            syncMemberSettings();
            deviceAccessPassword();
        } catch (Exception e) {

        }
    }

    private void activateStatus() {
        activateStatus.setText(getString(R.string.not_activated_msg));
        not_activate.setText(getString(R.string.activate_msg));
        not_activate.setVisibility(View.VISIBLE);

        not_activate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.activateApplication(DeviceSettingsActivity.this, DeviceSettingsActivity.this);
            }
        });
    }

    private void deviceAccessPassword() {
        if (!sharedPreferences.getString(GlobalParameters.deviceSettingMasterCode, "").isEmpty()) {
            etPassword.setText(sharedPreferences.getString(GlobalParameters.deviceSettingMasterCode, ""));
        } else if (!sharedPreferences.getString(GlobalParameters.deviceMasterCode, "").isEmpty()) {
            etPassword.setText(sharedPreferences.getString(GlobalParameters.deviceMasterCode, ""));
        } else {
            String input = Util.getSNCode(this);
            String lastsixDigits = "";

            if (input.length() > 6) {
                lastsixDigits = input.substring(input.length() - 6);
            } else {
                lastsixDigits = input;
            }
            etPassword.setText(sharedPreferences.getString(GlobalParameters.DEVICE_PASSWORD, lastsixDigits));
            etPassword.setSelection(lastsixDigits.length());
        }
    }

    private void stopHealthCheckService() {
        Intent intent = new Intent(this, DeviceHealthService.class);
        stopService(intent);
    }

    @Override
    public void onJSONObjectListener(String reportInfo, String status, JSONObject req) {
        try {
            JSONObject json1 = null;
            if (reportInfo == null) {
                return;
            }
            try {
                String formatedString = reportInfo.substring(1, reportInfo.length() - 1);
                json1 = new JSONObject(formatedString.replace("\\", ""));

            } catch (Exception e) {
                json1 = new JSONObject(reportInfo.replace("\\", ""));
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                Util.getTokenActivate(reportInfo, status, this, "setting");
            startHealthCheckService();
            if (json1.isNull("responseSubCode")) return;
            if (json1.getString("responseSubCode").equals("104")) {
                switch_activate.setChecked(false);
            } else if (json1.getString("responseSubCode").equals("105")) {
                switch_activate.setChecked(false);
            } else if (json1.getString("responseCode").equals("1")) {
                switch_activate.setChecked(true);
                activateStatus.setText(getString(R.string.activated));
                not_activate.setVisibility(View.GONE);
            } else if (json1.getString("responseSubCode").equals("103")) {
                switch_activate.setChecked(true);
                activateStatus.setText(getString(R.string.activated));
                not_activate.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            Logger.error(TAG, "onJSONObjectListener(String report, String status, JSONObject req)", e.getMessage());
        }
    }

    private void startHealthCheckService() {
        try {
            if (Util.isConnectingToInternet(this) && !Util.isServiceRunning(DeviceHealthService.class, this) && sharedPreferences.getBoolean(GlobalParameters.ONLINE_MODE, false)) {
                startService(new Intent(this, DeviceHealthService.class));
                Application.StartService(this);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Logger.error(TAG, "Exception occurred in starting DeviceHealth Service" + e.getMessage());
        }
    }

    private void deleteAppData() {
        SharedPreferences sharedPreferences = Util.getSharedPreferences(this);
        if (sharedPreferences != null) {
            sharedPreferences.edit().clear().apply();
        }
        if (sharedPreferences != null) {
            Util.writeString(sharedPreferences, GlobalParameters.Firebase_Token, ApplicationController.getInstance().getFcmPushToken());
        }
    }

    private void stopMemberSyncService() {
        Intent intent = new Intent(this, MemberSyncService.class);
        stopService(intent);
    }

    private void restartApp() {
        initiateCloseApp();
    }

    private void closeApp() {
        Toast.makeText(DeviceSettingsActivity.this, getString(R.string.app_restart), Toast.LENGTH_SHORT).show();
        stopMemberSyncService();
        finishAffinity();
        Intent intent = new Intent(this, HomeActivity.class);
        int mPendingIntentId = 111111;
        PendingIntent mPendingIntent = PendingIntent.getActivity(this, mPendingIntentId, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis(), mPendingIntent);
    }

    @Override
    public void onJSONObjectListenerSetting(JSONObject reportInfo, String status, JSONObject req) {
        //no operation
        try {
            if (reportInfo == null) {
                return;
            }
            if (!reportInfo.isNull("Message")) {
                if (reportInfo.getString("Message").contains("token expired"))
                    Util.getToken(this, this);

            } else {
                if (reportInfo.isNull("responseCode")) return;
                if (reportInfo.getString("responseCode").equals("1")) {
                    Util.retrieveSetting(reportInfo, DeviceSettingsActivity.this);
                    setUIData();
                } else {
                    Logger.toast(this, "Something went wrong please try again");
                }
            }

        } catch (Exception e) {

        }
    }

    @Override
    public void onBackPressed() {
        if (isValidUrl()) {
            super.onBackPressed();
        }
    }

    @Override
    public void onParamterback(View view) {
        if (isValidUrl()) {
            super.onParamterback(view);
        }
    }

    private boolean isValidUrl() {
        if (etEndUrl.getText().toString().isEmpty()) {
            etEndUrl.setError(getString(R.string.input_valid_url));
            return false;
        }
        return true;
    }

    private void enableProDevice() {
        if (Util.isDeviceProModel()) {
            pro_layout.setVisibility(View.VISIBLE);
            pro_settings_border.setVisibility(View.VISIBLE);
        } else {
            pro_layout.setVisibility(View.GONE);
            pro_settings_border.setVisibility(View.GONE);
        }
    }

    public void clearDatabase(View view) {
        //LitePal.deleteDatabase("telpo_face");
        DatabaseController.getInstance().deleteAllMember();
        Toast.makeText(this, getString(R.string.all_members_cleared), Toast.LENGTH_LONG).show();
    }

    private void seekBarSettings() {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (Build.MODEL.contains("950") || "TPS980Q".equals(Build.MODEL)) {
                    progress /= 8;
                    Log.e("progress---", progress + "");
                    Util.setLedPower(progress);
                } else {
                    Process p = null;
                    ledLevel = progress;
                    //String cmd = "echo " + progress + " > /sys/class/backlight/rk28_bl_sub/brightness";
                    //String cmd = "echo " + progress + " > /sys/class/backlight/backlight_extend/brightness";//新
                    String cmd = "echo " + progress + " > /sys/class/backlight/led-brightness/brightness";//新
                    ShellUtils.execCommand(cmd, false);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    private void setDefaultLedBrightnessLevel() {
        ledLevel = sharedPreferences.getInt(GlobalParameters.LedBrightnessLevel, 30);
        seekBar.setProgress(ledLevel);
    }

    private void saveLedBrightnessSetting() {
        Util.writeInt(sharedPreferences, GlobalParameters.LedBrightnessLevel, ledLevel);
    }

    private void localServerSetting() {

        LocalServer localServer = new LocalServer(this);
        if (sharedPreferences.getBoolean(GlobalParameters.LOCAL_SERVER_SETTINGS, false)) {
            radio_yes_server.setChecked(true);
            addrRelativeLayout.setVisibility(View.VISIBLE);
            serverAddress = localServer.getIpAddress(this) + ":" + Constants.port;
            String text = String.format(getResources().getString(R.string.text_ip_address), serverAddress);
            tvServerIp.setText(text);
            serverSettingValue = true;
        } else {
            radio_no_server.setChecked(true);

        }

        radio_group_local_server.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radio_yes_server_setting) {
                    Util.writeBoolean(sharedPreferences, GlobalParameters.LOCAL_SERVER_SETTINGS, true);
                    radio_yes_server.setChecked(true);
                    addrRelativeLayout.setVisibility(View.VISIBLE);
                    serverAddress = localServer.getIpAddress(DeviceSettingsActivity.this) + ":" + Constants.port;
                    String text = String.format(getResources().getString(R.string.text_ip_address), serverAddress);
                    tvServerIp.setText(text);
                    serverSettingValue = true;
                } else {
                    Util.writeBoolean(sharedPreferences, GlobalParameters.LOCAL_SERVER_SETTINGS, false);
                    radio_no_server.setChecked(true);
                    serverSettingValue = false;
                    addrRelativeLayout.setVisibility(View.GONE);
                    tvServerIp.setText("");
                }
            }

        });

    }

    private void copyLocalServerAddress() {
        ivClipBoard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("text", serverAddress);
                clipboard.setPrimaryClip(clip);
                String message = String.format(getResources().getString(R.string.text_copied), serverAddress);
                Toast.makeText(DeviceSettingsActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void captureLogSetting() {
        saveLogButton.setOnClickListener(view -> {
            Util.sendDeviceLogs(DeviceSettingsActivity.this);
            Toast.makeText(DeviceSettingsActivity.this, getString(R.string.logs_sent), Toast.LENGTH_LONG).show();
        });
    }

    private void logOfflineDataSetting() {
        if (sharedPreferences.getBoolean(GlobalParameters.LogOfflineData, false)) {
            logOfflineDataYes.setChecked(true);
        } else {
            logOfflineDataNo.setChecked(true);
        }

        logOfflineDataRg.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.log_od_rb_yes) {
                logOfflineDataYes.setChecked(true);
            } else if (checkedId == R.id.log_od_rb_no) {
                logOfflineDataNo.setChecked(true);
            }
        });
    }

    private void initiateCloseApp() {
        ProgressDialog.show(this, "", getString(R.string.closing_app_msg));
        Observable
                .create((ObservableOnSubscribe<Boolean>) emitter -> {
                    ApplicationController.getInstance().releaseThermalUtil();
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    emitter.onNext(true);
                })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Boolean>() {
                    Disposable closeAppDisposable;

                    @Override
                    public void onSubscribe(Disposable d) {
                        closeAppDisposable = d;
                    }

                    @Override
                    public void onNext(Boolean value) {
                        closeApp();
                        closeAppDisposable.dispose();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "Error in fetching settings from the server");
                        closeApp();
                        closeAppDisposable.dispose();
                    }

                    @Override
                    public void onComplete() {
                        //do noop
                    }
                });
    }

    private void languageSetting() {
        HashMap<Integer, String> languageMap = DeviceSettingsController.getInstance().getLanguageMapFromDb();
        ArrayList<String> values = new ArrayList<>(languageMap.values());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, values);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLanguageSelector.setAdapter(adapter);

        spinnerLanguageSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                primaryLanguage = spinnerLanguageSelector.getSelectedItem().toString();
                if (primaryLanguage.equals(secondaryLanguage)) {
                    Toast.makeText(DeviceSettingsActivity.this, getString(R.string.language_message), Toast.LENGTH_SHORT).show();
                    return;
                }
                selectedLanguageId = (Integer) Util.getKeyOnValue(languageMap, primaryLanguage);
                if (selectedLanguageId != null) {
                    Util.writeString(sharedPreferences, GlobalParameters.LANGUAGE_TYPE,
                            DeviceSettingsController.getInstance().getLanguageOnId(selectedLanguageId));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        String languageCode = sharedPreferences.getString(GlobalParameters.LANGUAGE_TYPE, "en");
        currentlanguageCode = languageCode;
        String languageName = DeviceSettingsController.getInstance().getLanguageNameOnCode(languageCode);
        spinnerLanguageSelector.setSelection(((ArrayAdapter<String>)spinnerLanguageSelector.getAdapter()).getPosition(languageName));
    }

    private void additionalLanguageSetting() {
        HashMap<Integer, String> languageMap = DeviceSettingsController.getInstance().getLanguageMapFromDb();
        ArrayList<String> values = new ArrayList<>(languageMap.values());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, values);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        additional_spinner_language_selector.setAdapter(adapter);

        additional_spinner_language_selector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                secondaryLanguage = additional_spinner_language_selector.getSelectedItem().toString();
                if (secondaryLanguage.equals(primaryLanguage)) {
                    Toast.makeText(DeviceSettingsActivity.this, getString(R.string.language_message), Toast.LENGTH_SHORT).show();
                    return;
                }
                Integer selectedLanguageId = (Integer) Util.getKeyOnValue(languageMap, secondaryLanguage);
                if (selectedLanguageId != null) {
                    Util.writeString(sharedPreferences, GlobalParameters.LANGUAGE_TYPE_SECONDARY,
                            DeviceSettingsController.getInstance().getLanguageOnId(selectedLanguageId));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        String languageCode = sharedPreferences.getString(GlobalParameters.LANGUAGE_TYPE_SECONDARY, "es");
        currentlanguageCode = languageCode;
        String languageName = DeviceSettingsController.getInstance().getLanguageNameOnCode(languageCode);
        additional_spinner_language_selector.setSelection(((ArrayAdapter<String>)additional_spinner_language_selector.getAdapter()).getPosition(languageName));
    }
}