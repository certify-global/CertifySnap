package com.certify.snap.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.certify.snap.R;
import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.Logger;
import com.certify.snap.common.Util;
import com.google.android.material.textfield.TextInputLayout;

public class AdminLoginActivity extends Activity {

    EditText etPassword,etEmail;
    SharedPreferences sp;
    Button btn_confirm;
    TextView textview_name,tv_version,tv_serial_no,tv_note;
    Typeface rubiklight;
    TextInputLayout text_input_login,text_input_email;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_login_admin);
            etPassword = findViewById(R.id.edittext_login);
            etEmail = findViewById(R.id.edittext_email);
            btn_confirm = findViewById(R.id.btn_login);
            textview_name = findViewById(R.id.textview_name);
            tv_note = findViewById(R.id.tv_note);
            tv_version = findViewById(R.id.tv_version);
            tv_serial_no = findViewById(R.id.tv_serial_no);
            text_input_login = findViewById(R.id.text_input_login);
            text_input_email = findViewById(R.id.text_input_email);
            sp = Util.getSharedPreferences(AdminLoginActivity.this);
            rubiklight = Typeface.createFromAsset(getAssets(),
                    "rubiklight.ttf");
            textview_name.setTypeface(rubiklight);
            tv_note.setTypeface(rubiklight);
            tv_version.setText(Util.getVersionBuild());
            tv_serial_no.setText("Serial No: " + Util.getSNCode());
            btn_confirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (etEmail.getText().toString().isEmpty()) {
                        text_input_email.setError("Email should not be empty");
                    }
                   else if (etPassword.getText().toString().isEmpty()) {
                        text_input_login.setError("Password should not be empty");
                    }else{

                    }
                }
            });
        } catch (Exception e) {
            Logger.error(" LoginActivity onCreate(@Nullable Bundle savedInstanceState)", e.getMessage());
        }
    }


    public void onParamterback(View view) {
       startActivity(new Intent(AdminLoginActivity.this, IrCameraActivity.class));
        finish();
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(AdminLoginActivity.this, IrCameraActivity.class));
        finish();
    }

}
