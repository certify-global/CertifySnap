package com.certify.snap.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.certify.callback.AddDeviceCallback;
import com.certify.snap.R;
import com.certify.snap.async.AsyncJSONObjectAddDevice;
import com.certify.snap.common.EndPoints;
import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.Logger;
import com.certify.snap.common.Util;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONObject;


public class RegisterDeviceActivity extends SettingsBaseActivity implements AddDeviceCallback {
    private static String TAG = "RegisterDeviceActivity -> ";

    EditText edittext_device_name,edittext_sno,edittext_imei;
    SharedPreferences sharedPreferences;
    Button btn_close,btn_save;
    TextView textview_name,tv_version,tv_serial_no, tv_status, text_serial_number, text_imei_number;
    Typeface rubiklight;
    TextInputLayout text_input_device_name,text_input_sno;
    RadioGroup rg_status;
    RadioButton rb_active,rb_inactive;
    Boolean rbstatus=true;
    private ProgressDialog mprogressDialog;
    private String imeiNumber = "";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_register_device);
            tv_version = findViewById(R.id.tv_version);
            tv_serial_no = findViewById(R.id.tv_serial_no);
            textview_name = findViewById(R.id.textview_name);
            tv_status = findViewById(R.id.tv_status);
            edittext_device_name = findViewById(R.id.edittext_device_name);
            edittext_sno = findViewById(R.id.edittext_sno);
            edittext_imei = findViewById(R.id.edittext_imei);
            text_input_device_name = findViewById(R.id.text_input_device_name);
            text_input_sno = findViewById(R.id.text_input_sno);
            btn_close = findViewById(R.id.btn_close);
            btn_save = findViewById(R.id.btn_save);
            rg_status = findViewById(R.id.radio_group_status);
            rb_active = findViewById(R.id.radio_active);
            rb_inactive = findViewById(R.id.radio_inactive);
            text_serial_number = findViewById(R.id.text_serial_number);
            text_imei_number = findViewById(R.id.text_imei_number);

            sharedPreferences = Util.getSharedPreferences(RegisterDeviceActivity.this);
            rubiklight = Typeface.createFromAsset(getAssets(),
                    "rubiklight.ttf");
            textview_name.setTypeface(rubiklight);
            text_serial_number.setTypeface(rubiklight);
            text_imei_number.setTypeface(rubiklight);

            tv_version.setText(Util.getVersionBuild());
            tv_serial_no.setText("Serial No: " + Util.getSNCode(this));
            text_serial_number.setText("SERIAL NUMBER: " + Util.getSNCode(this));
            imeiNumber = Util.getUniqueIMEIId(this);
            text_imei_number.setText("IMEI NUMBER: " + imeiNumber);
            rb_active.setChecked(true);
            edittext_sno.setText(Util.getSNCode(this));
            rg_status.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    if (checkedId == R.id.radio_active) {
                        rbstatus=true;
                    }
                    else
                        rbstatus=false;
                }
            });

            btn_save.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (edittext_device_name.getText().toString().isEmpty()) {
                        text_input_device_name.setError("Device Name should not be empty");
                    }else{
                        sendReqAddDevice(edittext_device_name.getText().toString(), Util.getSNCode(RegisterDeviceActivity.this), imeiNumber,rbstatus);
                    }

                }
            });
            btn_close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(RegisterDeviceActivity.this,AddDeviceActivity.class));
                }
            });

        } catch (Exception e) {
            Logger.error(" LoginActivity onCreate(@Nullable Bundle savedInstanceState)", e.getMessage());
        }
    }

    private void sendReqAddDevice(String deviceName,String sno,String imei,Boolean status) {
        try {
            mprogressDialog = ProgressDialog.show(RegisterDeviceActivity.this, "Loading", "Loading! Please wait...");
            JSONObject obj = new JSONObject();
            obj.put("institutionId", sharedPreferences.getString(GlobalParameters.Admin_InstitutionID, ""));
            obj.put("deviceID", 0);
            obj.put("deviceName",deviceName);
            obj.put("serialNumber",sno);
            obj.put("IMEINumber",imei);
            obj.put("status",status);
            obj.put("userId", 0);
            obj.put("srcInProcess", "Device");
            obj.put("settingsId", 1);
            obj.put("facilityId", 0);
            obj.put("locationId", 0);

            new AsyncJSONObjectAddDevice(obj, RegisterDeviceActivity.this, sharedPreferences.getString(GlobalParameters.URL, EndPoints.prod_url) + EndPoints.RegisterDevice, this).execute();

        } catch (Exception e) {
            Logger.error(TAG, "sendReqAddDevice " + e.getMessage());
        }
    }


    public void onParamterback(View view) {
       startActivity(new Intent(RegisterDeviceActivity.this, AddDeviceActivity.class));
        finish();
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(RegisterDeviceActivity.this, AddDeviceActivity.class));
        finish();
    }

    @Override
    public void onJSONObjectListenerAddDevice(JSONObject report, String status, JSONObject req) {
        try {
            if (report != null) {
                mprogressDialog.dismiss();
                if (report.getString("responseCode").equals("1")) {
                   Logger.toast(this,"Device Registered Successfully");
                   startActivity(new Intent(RegisterDeviceActivity.this,RegisterSuccessActivity.class));
                    Util.writeBoolean(sharedPreferences, GlobalParameters.ONLINE_MODE, true);

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                          //  Util.switchRgbOrIrActivity(RegisterDeviceActivity.this,true);
                            startActivity(new Intent(RegisterDeviceActivity.this,GuideActivity.class));

                        }
                    }, 3 * 1000);
                } else {
                    String responseMessage=report.getString("responseMessage");
                    Logger.toast(this,responseMessage);
                }
            }
        } catch (Exception e) {
            Logger.error(TAG + "onJSONObjectListenerAddDevice","Login api response error"+e.getMessage());
            mprogressDialog.dismiss();

        }
    }
    }

