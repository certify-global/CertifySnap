package com.certify.snap.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.certify.callback.JSONObjectCallbackLogin;
import com.certify.snap.R;
import com.certify.snap.async.AsyncJSONObjectLogin;
import com.certify.snap.common.EndPoints;
import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.Logger;
import com.certify.snap.common.Util;
import com.google.android.material.textfield.TextInputLayout;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static android.webkit.ConsoleMessage.MessageLevel.LOG;

public class AdminLoginActivity extends SettingsBaseActivity implements JSONObjectCallbackLogin {
    private static String TAG = "AdminLoginActivity -> ";
    EditText etPassword,etEmail;
    SharedPreferences sharedPreferences;
    Button btn_confirm;
    TextView textview_name,tv_version,tv_serial_no,tv_note;
    Typeface rubiklight;
    TextInputLayout text_input_login,text_input_email;
    private ProgressDialog mprogressDialog;

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
            sharedPreferences = Util.getSharedPreferences(AdminLoginActivity.this);
            rubiklight = Typeface.createFromAsset(getAssets(),
                    "rubiklight.ttf");
            textview_name.setTypeface(rubiklight);
            tv_note.setTypeface(rubiklight);
            tv_version.setText(Util.getVersionBuild());
            tv_serial_no.setText("Serial No: " + Util.getSNCode(this));

            btn_confirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (etEmail.getText().toString().isEmpty()) {
                        text_input_email.setError("Email should not be empty");
                        text_input_login.setError(null);
                    }else if(!Util.isValidEmail(etEmail.getText().toString())){
                        text_input_email.setError("Invalid Email");
                    }else if (etPassword.getText().toString().isEmpty()) {
                        text_input_login.setError("Password should not be empty");
                        text_input_email.setError(null);
                    }else{
                        sendReq(etEmail.getText().toString(),etPassword.getText().toString());
                    }
                }
            });
        } catch (Exception e) {
            Logger.error(" LoginActivity onCreate(@Nullable Bundle savedInstanceState)", e.getMessage());
        }
    }

    private void sendReq(String userMail,String password) {
        try{
            mprogressDialog = ProgressDialog.show(AdminLoginActivity.this, "Loading", "Loading! Please wait...");
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("username", userMail));
            params.add(new BasicNameValuePair("password", password));
            params.add(new BasicNameValuePair("grant_type", "password"));
            params.add(new BasicNameValuePair("linkExpiryMinutes", "60"));
            params.add(new BasicNameValuePair("loginPrevilageType", "LoginCare"));
            params.add(new BasicNameValuePair("loginType", "CCARE"));
            String objParam = params.toString().replace(",", "&").replace("[", "").replace("]", "");
            new AsyncJSONObjectLogin(objParam, AdminLoginActivity.this, sharedPreferences.getString(GlobalParameters.URL, EndPoints.prod_url) + EndPoints.Token, this).execute();
        }catch (Exception e){
            Logger.error(TAG,"sendReq","Login api exception"+e.getMessage());
        }
    }


    public void onParamterback(View view) {
       startActivity(new Intent(AdminLoginActivity.this, AddDeviceActivity.class));
        finish();
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(AdminLoginActivity.this, AddDeviceActivity.class));
        finish();
    }

    @Override
    public void onJSONObjectListenerLogin(JSONObject report, String status, String req) {
        try {
            if (report != null) {
                mprogressDialog.dismiss();
                if (report.getString("responseCode").equals("1")) {
                    String institutionID=report.getString("InstitutionID");
                    String token=report.getString("access_token");
                    Util.writeString(sharedPreferences,GlobalParameters.Admin_InstitutionID,institutionID);
                    Util.writeString(sharedPreferences,GlobalParameters.Temp_ACCESS_TOKEN,token);
                    startActivity(new Intent(AdminLoginActivity.this,RegisterDeviceActivity.class));
                } else {
                    Logger.toast(this,"Invalid Credentials");
                }
            }
        } catch (Exception e) {
            Logger.error(LOG + "onJSONObjectListenerLogin","Login api response error"+e.getMessage());
            Logger.toast(this,"Invalid Credentials");
            mprogressDialog.dismiss();


        }
    }
}
