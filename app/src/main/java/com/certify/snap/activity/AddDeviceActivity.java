package com.certify.snap.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.certify.callback.JSONObjectCallback;
import com.certify.callback.SettingCallback;
import com.certify.snap.R;
import com.certify.snap.common.Constants;
import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.Logger;
import com.certify.snap.common.Util;
import com.certify.snap.service.DeviceHealthService;
import com.certify.snap.service.MemberSyncService;

import org.json.JSONObject;

public class AddDeviceActivity extends SettingBaseActivity implements JSONObjectCallback, SettingCallback {
    private static String LOG = "AddDeviceActivity -> ";
    SharedPreferences sharedPreferences;
    TextView tv_header, tv_header1, tv_header2, tv_skip,textview_name;
    Button btn_offline, btn_activation_again, btn_add_device;
    private Typeface rubiklight;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_device);
            rubiklight = Typeface.createFromAsset(getAssets(),
                    "rubiklight.ttf");
            sharedPreferences = Util.getSharedPreferences(this);
            tv_header = findViewById(R.id.tv_header);
            tv_header1 = findViewById(R.id.tv_header1);
            tv_header2 = findViewById(R.id.tv_header2);
            tv_skip = findViewById(R.id.tv_skip);
            textview_name = findViewById(R.id.textview_name);
            btn_offline = findViewById(R.id.btn_offline);
            btn_activation_again = findViewById(R.id.btn_activation_again);
            btn_add_device = findViewById(R.id.btn_add_device);
            tv_header.setTypeface(rubiklight);
            tv_header1.setTypeface(rubiklight);
            tv_header2.setTypeface(rubiklight);
            tv_skip.setTypeface(rubiklight);
            textview_name.setTypeface(rubiklight);

            tv_header2.setText("If you have already added the device on the portal SL NO: " + Util.getSNCode());


            btn_activation_again.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Util.activateApplication(AddDeviceActivity.this, AddDeviceActivity.this);

                }
            });
            tv_skip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                    Util.switchRgbOrIrActivity(AddDeviceActivity.this, true);
                }
            });

            btn_add_device.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(AddDeviceActivity.this, AdminLoginActivity.class));
                }
            });
            btn_offline.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Util.writeBoolean(sharedPreferences, GlobalParameters.ONLINE_MODE, false);
                    Util.writeString(sharedPreferences, GlobalParameters.DEVICE_SETTINGS_NAME, "Local");
                    stopHealthCheckService();
                    Util.switchRgbOrIrActivity(AddDeviceActivity.this, true);

                }
            });
        } catch (Exception e) {
            Logger.error(LOG + "oncreate", e.getMessage());
        }
    }

    private void stopHealthCheckService() {
        Intent intent = new Intent(this, DeviceHealthService.class);
        stopService(intent);
    }

    @Override
    public void onJSONObjectListener(String reportInfo, String status, JSONObject req) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                Util.getTokenActivate(reportInfo, status, this, "deviceactivity");

        } catch (Exception e) {
            Logger.error(LOG + "onJSONObjectListener(String report, String status, JSONObject req)", e.getMessage());
        }
    }

    @Override
    public void onJSONObjectListenerSetting(JSONObject reportInfo, String status, JSONObject req) {
        try {
            if (reportInfo == null) {
                onSettingsUpdated();
                return;
            }
            Util.retrieveSetting(reportInfo, AddDeviceActivity.this);
            onSettingsUpdated();
        } catch (Exception e) {
            onSettingsUpdated();
            Logger.error(LOG, "onJSONObjectListenerSetting()", "Exception while processing API response callback" + e.getMessage());
        }

    }

    private void onSettingsUpdated() {
        if (Util.getTokenRequestName().equalsIgnoreCase("deviceactivity")) {
              Util.switchRgbOrIrActivity(this, true);
              Util.setTokenRequestName("");
        }
        startMemberSyncService();
    }

    private void startMemberSyncService() {
        try {
            if ((sharedPreferences.getBoolean(GlobalParameters.FACIAL_DETECT, true) || sharedPreferences.getBoolean(GlobalParameters.RFID_ENABLE, false))
                    && sharedPreferences.getBoolean(GlobalParameters.SYNC_ONLINE_MEMBERS, false))
                startService(new Intent(this, MemberSyncService.class));
        } catch (Exception e) {
            Logger.error(LOG +"startMemberSyncService", e.getMessage());

        }
    }
}
