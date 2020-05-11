package com.certify.snap.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.arcsoft.face.BuildConfig;
import com.certify.snap.R;
import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.Logger;
import com.certify.snap.common.Util;

public class LoginActivity extends Activity {

    EditText etPassword;
    SharedPreferences sp;
    Button btn_confirm;
    TextView textview_name,tv_version,tv_serial_no;
    Typeface rubiklight;
    int count=0;
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
             sp = Util.getSharedPreferences(LoginActivity.this);
            rubiklight = Typeface.createFromAsset(getAssets(),
                    "rubiklight.ttf");
            textview_name.setTypeface(rubiklight);

            tv_version.setText("Version: " + BuildConfig.VERSION_NAME);
            tv_serial_no.setText("Serial No: "+Util.getSNCode());
            btn_confirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(count<10) {
                        if (etPassword.getText().toString().equals(sp.getString(GlobalParameters.deviceMasterCode, ""))) {
                            Intent intent = new Intent(LoginActivity.this, SettingActivity.class);
                            startActivity(intent);
                            finish();
                        }else{
                            count++;
                            Toast.makeText(LoginActivity.this, getString(R.string.toast_rgbir_pwderror)+""+count, Toast.LENGTH_LONG).show();
                        }
                    }else {
                        String input ="1234567";//Util.getSNCode();     //input string
                        String lastsixDigits = "";

                        if (input.length() >6) {
                            lastsixDigits = input.substring(input.length() - 6);
                        } else {
                            lastsixDigits = input;
                        }
                        if (etPassword.getText().toString().equals(sp.getString(GlobalParameters.DEVICE_PASSWORD, lastsixDigits))) {
                            Intent intent = new Intent(LoginActivity.this, SettingActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(LoginActivity.this, getString(R.string.toast_rgbir_pwderror), Toast.LENGTH_LONG).show();
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
