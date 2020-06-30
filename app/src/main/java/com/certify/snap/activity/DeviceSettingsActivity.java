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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.certify.callback.JSONObjectCallback;
import com.certify.callback.SettingCallback;
import com.certify.snap.R;
import com.certify.snap.common.Application;
import com.certify.snap.common.EndPoints;
import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.Logger;
import com.certify.snap.common.Util;
import com.certify.snap.service.DeviceHealthService;
import com.certify.snap.service.MemberSyncService;

import org.json.JSONObject;
import org.litepal.LitePal;

public class DeviceSettingsActivity extends SettingBaseActivity implements JSONObjectCallback, SettingCallback {
    private static final String TAG = DeviceSettingsActivity.class.getSimpleName();
    private EditText etEndUrl, etDeviceName, etPassword;
    private SharedPreferences sharedPreferences;
    private TextView btn_save, tvSettingsName, activateStatus, not_activate, tv_device_activation_status;
    private RelativeLayout ll;
    private Switch switch_activate;
    private TextView tvDeviceManager, tvEnd, tvDeviceName, tvPass, tvSettingStr, tv_activate_tv_device, tvResetSnap;
    private Button tvClearData;
    private CheckBox cbDoSyc;
    private Typeface rubiklight;
    private String url_end;
    private String url;
    private Boolean isOnline;

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
            cbDoSyc = findViewById(R.id.cb_enable_do_not_sync);
            sharedPreferences = Util.getSharedPreferences(this);
            tvClearData = findViewById(R.id.tv_clear_cache);
            tvResetSnap = findViewById(R.id.tv_reset_snap);
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
            cbDoSyc.setTypeface(rubiklight);
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
                    if (!TextUtils.isEmpty(url_end) && !url_end.equals(etEndUrl.getText().toString().trim())) {
                        Toast.makeText(DeviceSettingsActivity.this, "App will restart", Toast.LENGTH_SHORT).show();
                        deleteAppData();
                        Util.writeString(sharedPreferences, GlobalParameters.URL, url);
                        restartApp();
                    } else {
                        finish();
                    }
                }
            });
            cbDoSyc.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Util.writeBoolean(sharedPreferences, GlobalParameters.MEMBER_SYNC_DO_NOT, isChecked);
                }
            });
            tvClearData.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteAppData();
                    restartApp();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

    }
private void setUIData(){
        try{
            url_end = sharedPreferences.getString(GlobalParameters.URL, EndPoints.prod_url);
            etEndUrl.setText(url_end);
            if (url_end != null && url_end.length() > 0)
                etEndUrl.setSelection(url_end.length());
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
            cbDoSyc.setChecked(sharedPreferences.getBoolean(GlobalParameters.MEMBER_SYNC_DO_NOT, false));
            deviceAccessPassword();
        }catch (Exception e){

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
}