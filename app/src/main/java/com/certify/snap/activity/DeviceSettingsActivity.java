package com.certify.snap.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.certify.callback.JSONObjectCallback;
import com.certify.snap.R;
import com.certify.snap.common.Application;
import com.certify.snap.common.EndPoints;
import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.Logger;
import com.certify.snap.common.Util;
import com.certify.snap.service.DeviceHealthService;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONObject;

public class DeviceSettingsActivity extends Activity implements JSONObjectCallback {
    private static String LOG = "DeviceSettingsActivity -> ";
    private EditText etEndUrl, etDeviceName, etPassword;
    private SharedPreferences sharedPreferences;
    private TextView btn_save;
    private RelativeLayout ll;
    private TextInputLayout tiy_endpoint, tiy_password;
    private Switch switch_activate;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_device_settings);
            etEndUrl = findViewById(R.id.et_end_url);
            etDeviceName = findViewById(R.id.et_device_name);
            etPassword = findViewById(R.id.et_device_password);
            btn_save = findViewById(R.id.btn_save_device);
            switch_activate = findViewById(R.id.switch_activate_device);
            tiy_endpoint = findViewById(R.id.tiy_endpoint);
            tiy_password = findViewById(R.id.tiy_password);
            sharedPreferences = Util.getSharedPreferences(this);
            String url_end = sharedPreferences.getString(GlobalParameters.URL, EndPoints.prod_url);
            etEndUrl.setText(url_end);
            if (url_end != null && url_end.length() > 0)
                etEndUrl.setSelection(url_end.length());
            etDeviceName.setText(sharedPreferences.getString(GlobalParameters.DEVICE_NAME, ""));
            String input = Util.getSNCode();     //input string
            String lastsixDigits = "";

            if (input.length() > 6) {
                lastsixDigits = input.substring(input.length() - 6);
            } else {
                lastsixDigits = input;
            }
            etPassword.setText(sharedPreferences.getString(GlobalParameters.DEVICE_PASSWORD, lastsixDigits));
            etPassword.setSelection(lastsixDigits.length());
            switch_activate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        Toast.makeText(getApplicationContext(), getString(R.string.online_msg), Toast.LENGTH_LONG).show();
                        // Util.writeBoolean(sharedPreferences, GlobalParameters.ONLINE_SWITCH, true);
                        Util.activateApplication(DeviceSettingsActivity.this, DeviceSettingsActivity.this);

                    } else {
                        // Util.writeBoolean(sharedPreferences, GlobalParameters.ONLINE_SWITCH, false);
                        Toast.makeText(getApplicationContext(), getString(R.string.offline_msg), Toast.LENGTH_LONG).show();
                        Util.writeBoolean(sharedPreferences, GlobalParameters.ONLINE_MODE, false);
                        stopHealthCheckService();
                    }
                }
            });
            if (sharedPreferences.getBoolean(GlobalParameters.ONLINE_MODE, true)) {
                switch_activate.setChecked(true);
            } else {
                switch_activate.setChecked(false);
            }
            etEndUrl.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    String url = etEndUrl.getText().toString().trim();
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
                            tiy_password.setError("Password should not be empty");
                            return;
                        } else if (etPassword.getText().toString().length() < 6) {
                            tiy_password.setError("Password should be minimum six digits");
                        } else {
                            tiy_password.setError(null);
                            Util.writeString(sharedPreferences, GlobalParameters.DEVICE_PASSWORD, etPassword.getText().toString().trim());
                        }
                    } catch (Exception e) {
                        Logger.error(LOG + "etPassword --> afterTextChanged", e.getMessage());

                    }
                }
            });
            btn_save.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Util.writeString(sharedPreferences, GlobalParameters.DEVICE_NAME, etDeviceName.getText().toString().trim());
                    finish();
                }
            });
        } catch (Exception e) {
            Logger.error(LOG + "onCreate(@Nullable Bundle savedInstanceState)", e.getMessage());
        }

    }

    private void stopHealthCheckService() {
        Intent intent = new Intent(this, DeviceHealthService.class);
        stopService(intent);
    }

    public void onParamterback(View view) {
        startActivity(new Intent(DeviceSettingsActivity.this, SettingActivity.class));
        finish();
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
            Util.getTokenActivate(reportInfo, status, DeviceSettingsActivity.this, "setting");
            startHealthCheckService();
            if (json1.isNull("responseSubCode")) return;
            if (json1.getString("responseSubCode").equals("104")) {
                switch_activate.setChecked(false);
            } else if (json1.getString("responseSubCode").equals("105")) {
                switch_activate.setChecked(false);
            } else if (json1.getString("responseCode").equals("1")) {
                switch_activate.setChecked(true);
            } else if (json1.getString("responseSubCode").equals("103")) {
                switch_activate.setChecked(true);
            }
        } catch (Exception e) {
            Logger.error(LOG + "onJSONObjectListener(String report, String status, JSONObject req)", e.getMessage());
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
            Logger.error(LOG + "startHealthCheckService()", "Exception occurred in starting DeviceHealth Service" + e.getMessage());
        }
    }
}
