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
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
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
import com.certify.snap.localserver.LocalServer;
import com.certify.snap.service.DeviceHealthService;
import com.certify.snap.service.MemberSyncService;

import org.json.JSONObject;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class DeviceSettingsActivity extends SettingBaseActivity implements JSONObjectCallback, SettingCallback {
    private static final String TAG = DeviceSettingsActivity.class.getSimpleName();
    private EditText etEndUrl, etDeviceName, etPassword;
    private SharedPreferences sharedPreferences;
    private TextView btn_save, tvSettingsName, activateStatus, tv_device_activation_status, pro_settings;
    private RelativeLayout ll;
    private Switch switch_activate;
    private TextView tvDeviceManager, tvEnd, tvDeviceName, tvPass, tvSettingStr, tv_activate_tv_device, tvResetSnap, tv_reset_members, tv_clear_members,
                navigation_bar_textview, sync_online_members_textview, led_switch_textview;
    private Button tvClearData, not_activate;
    private Typeface rubiklight;
    private String url_end;
    private String url;
    private Boolean isOnline;
    private LinearLayout pro_layout;
    private TextView tvProtocol, tvHostName;
    private View pro_settings_border;
    RadioGroup sync_member_radio_group;
    RadioButton sync_member_radio_yes, sync_member_radio_no;
    RadioGroup radio_group_local_server;
    RadioButton radio_yes_server, radio_no_server;
    TextView tvLocalServer, tvServerIp;
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
                        LocalServer localServer = new LocalServer(DeviceSettingsActivity.this);
                        localServer.stopServer();
                        Util.activateApplication(DeviceSettingsActivity.this, DeviceSettingsActivity.this);
                        activateStatus();

                    } else {
                        isOnline = false;
                        localServerLayout.setVisibility(View.VISIBLE);
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
                            etPassword.setError("Password should not be empty");
                            return;
                        } else if (etPassword.getText().toString().length() < 6) {
                            etPassword.setError("Password should be minimum six digits");
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
                    saveLedBrightnessSetting();
                    Util.writeString(sharedPreferences, GlobalParameters.DEVICE_NAME, etDeviceName.getText().toString().trim());
                    Util.writeBoolean(sharedPreferences, GlobalParameters.PRO_SETTINGS, proSettingValue);
                    AppSettings.setProSettings(proSettingValue);
                    if (!TextUtils.isEmpty(url_end) && !url_end.equals(getString(R.string.protocol_text) + etEndUrl.getText().toString().trim() + getString(R.string.hostname))) {
                        deleteAppData();
                        if (serverSettingValue)
                            Util.writeBoolean(sharedPreferences, GlobalParameters.LOCAL_SERVER_SETTINGS, true);
                        Util.writeString(sharedPreferences, GlobalParameters.URL, url);
                        ApplicationController.getInstance().setEndPointUrl(url);
                        restartApp();
                    } else {
                        if ((proSettingValueSp != proSettingValue) || (serverSettingValue
                            && deviceOnlineSwitch)) {
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

    private void initView(){
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
        pro_settings_border= findViewById(R.id.pro_settings_border);
        tv_reset_members = findViewById(R.id.tv_reset_members);
        tv_clear_members = findViewById(R.id.tv_clear_members);
        navigation_bar_textview = findViewById(R.id.navigation_bar_textview);
        sync_online_members_textview = findViewById(R.id.sync_online_members_textview);
        led_switch_textview = findViewById(R.id.led_switch_textview);
        seekBar=findViewById(R.id.seekbar);
        tvLocalServer = findViewById(R.id.local_server_tv);
        tvServerIp = findViewById(R.id.tv_server_ip);
        radio_group_local_server = findViewById(R.id.local_server_radio_group);
        radio_yes_server = findViewById(R.id.radio_yes_server_setting);
        radio_no_server = findViewById(R.id.radio_no_server_setting);
        ivClipBoard = findViewById(R.id.iv_clipBoard);
        addrRelativeLayout = findViewById(R.id.addr_relative_layout);
        localServerLayout = findViewById(R.id.local_server_parent_layout);
        saveLogButton = findViewById(R.id.send_log_button);

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

        if (!sharedPreferences.getBoolean(GlobalParameters.ONLINE_MODE, true)) {
            localServerLayout.setVisibility(View.VISIBLE);
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
        RadioGroup  radio_group_navigationbar = findViewById(R.id.navigation_bar_radio_group);
        RadioButton radio_navigationbar_enable = findViewById(R.id.navigation_bar_radio_enable);
        RadioButton radio_navigationbar_disable = findViewById(R.id.navigation_bar_radio_disable);
        boolean navigationBar = sharedPreferences.getBoolean(GlobalParameters.NavigationBar,true);

        if (navigationBar){
            radio_navigationbar_enable.setChecked(true);
            sendBroadcast(new Intent(GlobalParameters.ACTION_SHOW_NAVIGATIONBAR));
            sendBroadcast(new Intent(GlobalParameters.ACTION_OPEN_STATUSBAR));
        }else {
            radio_navigationbar_disable.setChecked(true);
            sendBroadcast(new Intent(GlobalParameters.ACTION_HIDE_NAVIGATIONBAR));
            sendBroadcast(new Intent(GlobalParameters.ACTION_CLOSE_STATUSBAR));
        }

        radio_group_navigationbar.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.navigation_bar_radio_enable:
                        Util.writeBoolean(sharedPreferences,GlobalParameters.NavigationBar,true);
                        sendBroadcast(new Intent(GlobalParameters.ACTION_SHOW_NAVIGATIONBAR));
                        sendBroadcast(new Intent(GlobalParameters.ACTION_OPEN_STATUSBAR));
                        break;
                    case R.id.navigation_bar_radio_disable:
                        Util.writeBoolean(sharedPreferences,GlobalParameters.NavigationBar,false);
                        sendBroadcast(new Intent(GlobalParameters.ACTION_HIDE_NAVIGATIONBAR));
                        sendBroadcast(new Intent(GlobalParameters.ACTION_CLOSE_STATUSBAR));
                        break;
                }
            }
        });

    }

    private void ledSwitchSettings() {
        RadioGroup  led_radio_group = findViewById(R.id.radio_group_led);
        RadioButton radio_led_on = findViewById(R.id.radio_led_on);
        RadioButton radio_led_off = findViewById(R.id.radio_led_off);
        boolean ledSwitch = sharedPreferences.getBoolean(GlobalParameters.LedType,true);

        if (ledSwitch){
            radio_led_on.setChecked(true);
        }else {
            radio_led_off.setChecked(true);
            Util.setLedPower(0);
        }

        led_radio_group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.radio_led_on:
                        Util.writeBoolean(sharedPreferences,GlobalParameters.LedType,true);
                        break;
                    case R.id.radio_led_off:
                        Util.writeBoolean(sharedPreferences,GlobalParameters.LedType,false);
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
                activateStatus.setText("Activated");
                not_activate.setVisibility(View.GONE);
            } else {
                switch_activate.setChecked(false);
                activateStatus.setText("Not Activated");
                not_activate.setText("Activate");
                tvSettingsName.setText("Local");
            }
            syncMemberSettings();
            deviceAccessPassword();
        } catch (Exception e) {

        }
    }

    private void activateStatus() {
        activateStatus.setText("Not Activated");
        not_activate.setText("Activate");
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
            String input = Util.getSNCode();
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
                activateStatus.setText("Activated");
                not_activate.setVisibility(View.GONE);
            } else if (json1.getString("responseSubCode").equals("103")) {
                switch_activate.setChecked(true);
                activateStatus.setText("Activated");
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
        //LitePal.deleteDatabase("telpo_face");
    }

    private void stopMemberSyncService() {
        Intent intent = new Intent(this, MemberSyncService.class);
        stopService(intent);
    }

    private void restartApp() {
        initiateCloseApp();
    }

    private void closeApp() {
        Toast.makeText(DeviceSettingsActivity.this, "App will restart", Toast.LENGTH_SHORT).show();
        stopMemberSyncService();
        finishAffinity();
        Intent intent = new Intent(this, GuideActivity.class);
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
            etEndUrl.setError("Please input valid url");
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
        Toast.makeText(this, "All Members Cleared", Toast.LENGTH_LONG).show();
    }

    private void seekBarSettings(){
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(Build.MODEL.contains("950")||"TPS980Q".equals(Build.MODEL)){
                    progress /= 8;
                    Log.e("progress---",progress+"");
                    Util.setLedPower(progress);
                }else {
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
            serverAddress = localServer.getIpAddress(this) +":"+Constants.port;
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
                    serverAddress = localServer.getIpAddress(DeviceSettingsActivity.this) +":"+Constants.port;
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
            Toast.makeText(DeviceSettingsActivity.this, "Logs sent", Toast.LENGTH_LONG).show();
        });
    }

    private void initiateCloseApp() {
        ProgressDialog.show(this, "", "Closing and Restarting App, Please wait...");
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
}