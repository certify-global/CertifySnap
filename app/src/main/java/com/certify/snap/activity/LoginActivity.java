package com.certify.snap.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.certify.snap.BuildConfig;
import com.certify.snap.R;
import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.Logger;
import com.certify.snap.common.Util;

public class LoginActivity extends Activity {

    EditText etPassword;
    SharedPreferences sp;
    Button btn_confirm;
    TextView textview_name,tv_version,tv_serial_no,tv_pwd_error;
    Typeface rubiklight;
    int count=10;
    TextInputLayout text_input_login;
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
            tv_serial_no.setText("Serial No: "+Util.getSNCode());
            btn_confirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(count<=10  && count>1 && !sp.getString(GlobalParameters.deviceMasterCode, "").equals("")) {
                        if(etPassword.getText().toString().isEmpty()){
                            text_input_login.setError("Password should not be empty");
                        }else if (etPassword.getText().toString().equals(sp.getString(GlobalParameters.deviceMasterCode, ""))) {
                            text_input_login.setError(null);
                            if(sp.getBoolean(GlobalParameters.Cloud_Activated,false)){
                                Util.openDialogSetting(LoginActivity.this);
                                finish();
                            }else {
                                Intent intent = new Intent(LoginActivity.this, SettingActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        }else{
                            count--;
                            text_input_login.setError(null);
                            tv_pwd_error.setVisibility(View.VISIBLE);
                            tv_pwd_error.setText("Invalid Password, Try Again");
                            //tv_pwd_error.setText("Invalid Password.Enter your access code "+""+count  +" times left");
                        }
                    }else{
                        String input =Util.getSNCode();     //input string
                        String lastsixDigits = "";

                        if (input.length() >6) {
                            lastsixDigits = input.substring(input.length() - 6);
                        } else {
                            lastsixDigits = input;
                        }
                        if(etPassword.getText().toString().isEmpty()){
                            text_input_login.setError("Password should not be empty");
                        }else if (etPassword.getText().toString().equals(sp.getString(GlobalParameters.DEVICE_PASSWORD, lastsixDigits))) {
                            text_input_login.setError(null);
                            if(sp.getBoolean(GlobalParameters.Cloud_Activated,false)){
                                Util.openDialogSetting(LoginActivity.this);
                                finish();
                            }else {
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
            Logger.error(" LoginActivity onCreate(@Nullable Bundle savedInstanceState)",e.getMessage());
        }
    }
    public void onParamterback(View view) {
       startActivity(new Intent(LoginActivity.this, IrCameraActivity.class));
        finish();
    }

    @Override
    public void onBackPressed() {

        startActivity(new Intent(LoginActivity.this, IrCameraActivity.class));
        finish();
    }
}
