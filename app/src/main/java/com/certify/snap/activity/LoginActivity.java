package com.certify.snap.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import androidx.annotation.Nullable;
import com.certify.snap.common.AppSettings;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.certify.snap.R;
import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.Logger;
import com.certify.snap.common.Util;

import java.util.Timer;
import java.util.TimerTask;

public class LoginActivity extends SettingBaseActivity {

    private static final String TAG = LoginActivity.class.getSimpleName();
    EditText etPassword;
    SharedPreferences sp;
    Button btn_confirm;
    TextView textview_name,tv_version,tv_serial_no,tv_pwd_error,text_input_login;
    Typeface rubiklight;
    int count=10;
    private Timer mLoginScreenTimer;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_login);
            etPassword = findViewById(R.id.edittext_login);
            btn_confirm = findViewById(R.id.btn_login);
            textview_name = findViewById(R.id.textview_name);
            tv_version = findViewById(R.id.tv_version);
            tv_serial_no = findViewById(R.id.tv_serial_no);
            text_input_login = findViewById(R.id.text_input_login);
            tv_pwd_error = findViewById(R.id.tv_pwd_error);
            sp = Util.getSharedPreferences(LoginActivity.this);
            rubiklight = Typeface.createFromAsset(getAssets(),
                    "rubiklight.ttf");
            textview_name.setTypeface(rubiklight);
            tv_pwd_error.setTypeface(rubiklight);
            tv_version.setText(Util.getVersionBuild());
            tv_serial_no.setText("Serial No: " + Util.getSNCode());
            btn_confirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cancelLoginScreenTimer();
                    if (count <= 10 && count > 1 && (!sp.getString(GlobalParameters.deviceMasterCode, "").equals("") || !sp.getString(GlobalParameters.deviceSettingMasterCode, "").equals(""))) {
                        if (etPassword.getText().toString().isEmpty()) {
                            text_input_login.setText("Password should not be empty");
                            return;
                        }
                        if (!sp.getString(GlobalParameters.deviceSettingMasterCode, "").isEmpty()) {
                            if (etPassword.getText().toString().equals(sp.getString(GlobalParameters.deviceSettingMasterCode, ""))) {
                                launchSettings();
                            } else {
                                validatePassword();
                            }
                            return;
                        }
                        if (etPassword.getText().toString().equals(sp.getString(GlobalParameters.deviceMasterCode, ""))) {
                            launchSettings();
                        } else {
                            validatePassword();
                        }
                    } else {
                        String input = Util.getSNCode();     //input string
                        String lastsixDigits = "";
                        if (input.length() > 6) {
                            lastsixDigits = input.substring(input.length() - 6);
                        } else {
                            lastsixDigits = input;
                        }
                        if (etPassword.getText().toString().isEmpty()) {
                            text_input_login.setText("Password should not be empty");
                        } else if (etPassword.getText().toString().equals(sp.getString(GlobalParameters.DEVICE_PASSWORD, lastsixDigits))) {
                            text_input_login.setError(null);
                            if (sp.getBoolean(GlobalParameters.ONLINE_MODE, false)) {
                                Util.openDialogSetting(LoginActivity.this);
                                // finish();
                            } else {
                                Intent intent = new Intent(LoginActivity.this, SettingActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        } else {
                            text_input_login.setError(null);
                            tv_pwd_error.setVisibility(View.VISIBLE);
                            tv_pwd_error.setText("Invalid Password, Try Again");
//
                        }
                    }
                }
            });
        }catch (Exception e){
            Log.e(TAG,e.getMessage());
        }
        startLoginScreenTimer();
        Logger.debug(TAG, "onCreate", "Init complete");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelLoginScreenTimer();
    }

    private  void validatePassword(){
        count--;
        text_input_login.setError(null);
        tv_pwd_error.setVisibility(View.VISIBLE);
        tv_pwd_error.setText("Invalid Password, Try Again");
    }

    public void onParamterback(View view) {
        launchHomeScreen();
        finish();
    }

    @Override
    public void onBackPressed() {
        //startActivity(new Intent(LoginActivity.this, IrCameraActivity.class));
        launchHomeScreen();
        finish();
    }

    private void launchSettings(){
        text_input_login.setError(null);
        if(sp.getBoolean(GlobalParameters.ONLINE_MODE,false)){
            Util.openDialogSetting(LoginActivity.this);
            // finish();
        }else {
            Intent intent = new Intent(LoginActivity.this, SettingActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void startLoginScreenTimer() {
       cancelLoginScreenTimer();
        mLoginScreenTimer = new Timer();
        mLoginScreenTimer.schedule(new TimerTask() {
            public void run() {
                launchHomeScreen();
                finish();
                this.cancel();
            }
        }, 10 * 1000);
    }

    private void cancelLoginScreenTimer() {
        if (mLoginScreenTimer != null)
            mLoginScreenTimer.cancel();
    }

    private void launchHomeScreen() {
        if (Util.isDeviceProModel()) {
            if (!AppSettings.isProSettings()) {
                startActivity(new Intent(this, IrCameraActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            } else {
                startActivity(new Intent(this, ProIrCameraActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }
        } else {
            Util.switchRgbOrIrActivity(LoginActivity.this, true);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        etPassword.getText().clear();
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        cancelLoginScreenTimer();
        Logger.debug(TAG, "onUserInteraction", "User action occurred");
    }

}
