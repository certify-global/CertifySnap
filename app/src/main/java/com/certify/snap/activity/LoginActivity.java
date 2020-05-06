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

import com.certify.snap.R;
import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.Logger;
import com.certify.snap.common.Util;

public class LoginActivity extends Activity {

    EditText etPassword;
    SharedPreferences sp;
    Button btn_confirm;
    TextView textview_name;
    Typeface rubiklight;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_login);
            etPassword = findViewById(R.id.edittext_login);
             btn_confirm = findViewById(R.id.btn_login);
            textview_name = findViewById(R.id.textview_name);
             sp = Util.getSharedPreferences(LoginActivity.this);
            rubiklight = Typeface.createFromAsset(getAssets(),
                    "rubiklight.ttf");
            textview_name.setTypeface(rubiklight);
            btn_confirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String input = "1234567";//Util.getSNCode();     //input string
                    String lastsixDigits = "";     //substring containing last 4 characters

                    if (input.length() >6) {
                        lastsixDigits = input.substring(input.length() - 6);
                    } else {
                        lastsixDigits = input;
                    }
                    if (etPassword.getText().toString().equals(sp.getString(GlobalParameters.DEVICE_PASSWORD, lastsixDigits))) {
                        Intent intent = new Intent(LoginActivity.this, SettingActivity.class);
                        startActivity(intent);
                    } else {
                        Toast.makeText(LoginActivity.this, getString(R.string.toast_rgbir_pwderror), Toast.LENGTH_LONG).show();
                    }
                }
            });
        }catch (Exception e){
            Logger.error(" LoginActivity onCreate(@Nullable Bundle savedInstanceState)",e.getMessage());
        }
    }

    @Override
    public void onBackPressed() {
        Util.switchRgbOrIrActivity(LoginActivity.this, true);
    }
}
