package com.certify.snap.activity;

import android.app.AlarmManager;
import android.app.PendingIntent;
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
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.certify.callback.JSONObjectCallback;
import com.certify.callback.SettingCallback;
import com.certify.snap.R;
import com.certify.snap.common.AppSettings;
import com.certify.snap.common.Application;
import com.certify.snap.common.EndPoints;
import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.Logger;
import com.certify.snap.common.Util;
import com.certify.snap.controller.ApplicationController;
import com.certify.snap.service.DeviceHealthService;
import com.certify.snap.service.MemberSyncService;

import org.json.JSONObject;
import org.litepal.LitePal;

public class DeviceSettingsActivity extends SettingBaseActivity implements JSONObjectCallback, SettingCallback {
    private static final String TAG = DeviceSettingsActivity.class.getSimpleName();
    private EditText etEndUrl, etDeviceName, etPassword;
    private SharedPreferences sharedPreferences;
    private TextView btn_save, tvSettingsName, activateStatus, tv_device_activation_status, pro_settings;
    private RelativeLayout ll;
    private Switch switch_activate;
    private TextView tvDeviceManager, tvEnd, tvDeviceName, tvPass, tvSettingStr, tv_activate_tv_device, tvResetSnap, tv_reset_members, tv_clear_members, navigation_bar_textview, sync_online_members_textview;
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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_device_settings);
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

            proSettings();
            navigationBarSettings();
            syncMemberSettings();

            tvProtocol = findViewById(R.id.tv_protocol);
            tvHostName = findViewById(R.id.tv_hostName);
            tvDeviceManager.setPaintFlags(tvDeviceManager.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            setUIData();

            switch_activate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        isOnline = true;
                        Util.activateApplication(DeviceSettingsActivity.this, DeviceSettingsActivity.this);
                        activateStatus();

                    } else {
                        isOnline = false;
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
                    Util.writeString(sharedPreferences, GlobalParameters.DEVICE_NAME, etDeviceName.getText().toString().trim());
                    if (!TextUtils.isEmpty(url_end) && !url_end.equals(getString(R.string.protocol_text) + etEndUrl.getText().toString().trim() + getString(R.string.hostname))) {
                        Toast.makeText(DeviceSettingsActivity.this, "App will restart", Toast.LENGTH_SHORT).show();
                        deleteAppData();
                        Util.writeString(sharedPreferences, GlobalParameters.URL, url);
                        ApplicationController.getInstance().setEndPointUrl(url);
                        restartApp();
                    } else {
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

    private void proSettings() {
        RadioGroup radio_group_pro = findViewById(R.id.radio_group_pro_settings);
        RadioButton radio_yes_pro = findViewById(R.id.radio_yes_pro_settings);
        RadioButton radio_no_pro = findViewById(R.id.radio_no_pro_settings);

        if (sharedPreferences.getBoolean(GlobalParameters.PRO_SETTINGS, true))
            radio_yes_pro.setChecked(true);
        else radio_no_pro.setChecked(true);

        radio_group_pro.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radio_yes_pro_settings) {
                    Util.writeBoolean(sharedPreferences, GlobalParameters.PRO_SETTINGS, true);
                    AppSettings.setProSettings(true);
                } else {
                    Util.writeBoolean(sharedPreferences, GlobalParameters.PRO_SETTINGS, false);
                    AppSettings.setProSettings(false);
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

    private void syncMemberSettings() {

        sync_member_radio_group = findViewById(R.id.sync_online_members_radio_group);
        sync_member_radio_yes = findViewById(R.id.sync_online_members_radio_enable);
        sync_member_radio_no = findViewById(R.id.sync_online_members_radio_disable);

        if (sharedPreferences.getBoolean(GlobalParameters.MEMBER_SYNC_DO_NOT, false))
            sync_member_radio_yes.setChecked(true);
        else sync_member_radio_no.setChecked(true);

        sync_member_radio_group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.sync_online_members_radio_enable) {
                    Util.writeBoolean(sharedPreferences, GlobalParameters.MEMBER_SYNC_DO_NOT, true);
                } else {
                    Util.writeBoolean(sharedPreferences, GlobalParameters.MEMBER_SYNC_DO_NOT, false);
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
        LitePal.deleteDatabase("telpo_face");
    }

    private void stopMemberSyncService() {
        Intent intent = new Intent(this, MemberSyncService.class);
        stopService(intent);
    }

    private void restartApp() {
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
        LitePal.deleteDatabase("telpo_face");
        Toast.makeText(this, "All Members Cleared", Toast.LENGTH_LONG).show();
    }

}