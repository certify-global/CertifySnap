package com.certify.snap.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.arcsoft.face.ActiveFileInfo;
import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.certify.callback.JSONObjectCallback;
import com.certify.callback.SettingCallback;
import com.certify.snap.BuildConfig;
import com.certify.snap.async.AsyncActiveEngine;
import com.certify.snap.common.Application;
import com.certify.snap.common.Constants;
import com.certify.snap.common.EndPoints;
import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.Logger;
import com.certify.snap.common.Util;
import com.certify.snap.R;
import com.certify.snap.service.DeviceHealthService;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class SettingActivity extends Activity implements JSONObjectCallback,SettingCallback {

    private FaceEngine faceEngine = new FaceEngine();
    private SharedPreferences sharedPreferences;
    private RelativeLayout activate, init, updatelist, management, register, parameter, led, card, record, setting_temperature, setting_upload, setting_access_password, setting_endpoint,
            thermal_check_setting, scan_setting, confirmation_setting, guide_setting,qr_setting;
    RadioGroup rg_temperature;
    RadioButton rb_temp, rb_temp_face;
    TextView access_pwd, upload_logo, setTemp, parameter_setting, activate_tv, endpoint, tv_version, tv_thermal_setting, tv_scan_setting, tv_confirmation_setting, tv_serial_no, tv_guide_setting,tv_qr_setting;
    Typeface rubiklight;
    private String userMail;
    private LinearLayout llSettings;
    private AlertDialog.Builder builder;
    ImageView img_sync;
    RelativeLayout relative_layout;
    Switch switch_activate;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            setContentView(R.layout.activity_setting);
            Util.getNumberVersion(SettingActivity.this);
            rubiklight = Typeface.createFromAsset(getAssets(),
                    "rubiklight.ttf");
            sharedPreferences = Util.getSharedPreferences(this);
            rg_temperature = findViewById(R.id.radio_group_work_flow);
            rb_temp = findViewById(R.id.radio_temp);
            rb_temp_face = findViewById(R.id.face_temp);
            img_sync = findViewById(R.id.img_sync);
            relative_layout = findViewById(R.id.relative_layout);
            switch_activate = findViewById(R.id.switch_activate);
            String FlowType = sharedPreferences.getString(GlobalParameters.TEMP_ONLY, "temp");

        rg_temperature.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.radio_camera_0:
                        Util.writeString(sharedPreferences, GlobalParameters.TEMP_ONLY, "temp");
                        break;
                    case R.id.radio_camera_1:
                        Util.writeString(sharedPreferences, GlobalParameters.FACE_TEMP, "facetemp");
                        break;

                    }
                }
            });
            switch_activate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        Toast.makeText(getApplicationContext(), getString(R.string.online_msg), Toast.LENGTH_LONG).show();
                        Util.writeBoolean(sharedPreferences, GlobalParameters.ONLINE_MODE, true);
                        Util.activateApplication(SettingActivity.this, SettingActivity.this);
                    } else {
                        Toast.makeText(getApplicationContext(), getString(R.string.offline_msg), Toast.LENGTH_LONG).show();
                        Util.writeBoolean(sharedPreferences, GlobalParameters.ONLINE_MODE, false);
                        stopHealthCheckService();
                    }
                }
            });

        initView();
        initOnlineModeSetting();
        Application.getInstance().addActivity(this);

        sharedPreferences = Util.getSharedPreferences(this);
        boolean activateStatus = sharedPreferences.getBoolean("activate", false);
        Logger.debug("sp---true", "activate:" + activateStatus);
        if (!activateStatus)
            new AsyncActiveEngine(SettingActivity.this, sharedPreferences,null,Util.getSNCode()).execute();
        img_sync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sharedPreferences.getBoolean(GlobalParameters.ONLINE_MODE,true)) {
                    Util.getSettings(SettingActivity.this, SettingActivity.this);
                    Snackbar snackbar = Snackbar
                            .make(relative_layout, R.string.snack_msg, Snackbar.LENGTH_LONG);
                    snackbar.show();
                }else{
                    Snackbar snackbar = Snackbar
                            .make(relative_layout, R.string.offline_msg, Snackbar.LENGTH_LONG);
                    snackbar.show();
                }


                }
            });
            if (sharedPreferences.getBoolean(GlobalParameters.ONLINE_MODE, true)) {
                switch_activate.setChecked(true);
            } else {
                switch_activate.setChecked(false);
            }
        }catch (Exception e){
            Logger.error("Setting  onCreate(Bundle savedInstanceState) ",e.getMessage());
        }

    }

    private void initView() {
        llSettings = findViewById(R.id.ll_settings);
        activate = findViewById(R.id.setting_activate);
        init = findViewById(R.id.setting_init);
        updatelist = findViewById(R.id.setting_updatelist);
        management = findViewById(R.id.setting_managment);
        register = findViewById(R.id.setting_register);
        parameter = findViewById(R.id.setting_parameter);
        led = findViewById(R.id.setting_led);
        thermal_check_setting = findViewById(R.id.thermal_check_setting);
        confirmation_setting = findViewById(R.id.confirmation_setting);
        card = findViewById(R.id.setting_activate_card);
        record = findViewById(R.id.setting_record);
        setting_temperature = findViewById(R.id.setting_temperature);
        setting_upload = findViewById(R.id.setting_upload);
        setting_access_password = findViewById(R.id.setting_access_password);
        setting_endpoint = findViewById(R.id.setting_endpoint);
        access_pwd = findViewById(R.id.access_pwd);
        setTemp = findViewById(R.id.setTemp);
        upload_logo = findViewById(R.id.upload_logo);
        parameter_setting = findViewById(R.id.parameter_setting);
        activate_tv = findViewById(R.id.activate_tv);
        guide_setting = findViewById(R.id.guide_setting);
        qr_setting = findViewById(R.id.qr_setting);
        endpoint = findViewById(R.id.endpoint);
        tv_version = findViewById(R.id.tv_version);
        tv_serial_no = findViewById(R.id.tv_serial_no);
        tv_thermal_setting = findViewById(R.id.tv_thermal_setting);
        tv_scan_setting = findViewById(R.id.tv_scan_setting);
        tv_confirmation_setting = findViewById(R.id.tv_confirmation_setting);
        tv_guide_setting = findViewById(R.id.tv_guide_setting);
        tv_qr_setting = findViewById(R.id.tv_qr_setting);
        access_pwd.setTypeface(rubiklight);
        setTemp.setTypeface(rubiklight);
        upload_logo.setTypeface(rubiklight);
        parameter_setting.setTypeface(rubiklight);
        activate_tv.setTypeface(rubiklight);
        endpoint.setTypeface(rubiklight);
        tv_version.setTypeface(rubiklight);
        tv_serial_no.setTypeface(rubiklight);
        tv_thermal_setting.setTypeface(rubiklight);
        tv_scan_setting.setTypeface(rubiklight);
        tv_confirmation_setting.setTypeface(rubiklight);
        tv_guide_setting.setTypeface(rubiklight);
        tv_qr_setting.setTypeface(rubiklight);
        tv_version.setText(Util.getVersionBuild());
        tv_serial_no.setText("Serial No: " + Util.getSNCode());
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void onclick(View view) {
        boolean isopen = sharedPreferences.getBoolean("activate", false);
        switch (view.getId()) {
            case R.id.setting_activate:
                if(Util.isConnectingToInternet(SettingActivity.this) && sharedPreferences.getBoolean(GlobalParameters.ONLINE_MODE,true)) {
                    Util.activateApplication(SettingActivity.this, SettingActivity.this);
                }else{
                    Snackbar snackbar = Snackbar
                            .make(relative_layout, R.string.offline_msg, Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
                break;
            case R.id.setting_init:
                if (isopen)
                    startActivity(new Intent(SettingActivity.this, InitializationActivity.class));
                break;
            case R.id.setting_managment:
                if (isopen)
                    startActivity(new Intent(SettingActivity.this, ManagementActivity.class));
                break;
            case R.id.setting_register:
                break;
            case R.id.setting_parameter:
                if (isopen)
                    startActivity(new Intent(SettingActivity.this, ParameterActivity.class));
                break;
            case R.id.setting_led:
                break;
            case R.id.setting_activate_card:
                if (isopen) startActivity(new Intent(SettingActivity.this, NFCCardActivity.class));
                break;
            case R.id.setting_record:
                if (isopen) startActivity(new Intent(SettingActivity.this, RecordActivity.class));
                break;
            case R.id.setting_temperature:
                //temperatureDialog();
                Intent tempIntent = new Intent(SettingActivity.this, TemperatureActivity.class);
                startActivity(tempIntent);
                break;
            case R.id.setting_upload:
                Intent i = new Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                startActivityForResult(i, 101);
                break;
            case R.id.setting_access_password:
                pwdDialog();
                break;
            case R.id.setting_endpoint:
                endpointDialog();
                break;
            case R.id.thermal_check_setting:
                Intent intent = new Intent(SettingActivity.this, ThermalSetting.class);
                startActivity(intent);
                break;
            case R.id.scan_setting:
                Intent intent1 = new Intent(SettingActivity.this, ScanViewActivity.class);
                startActivity(intent1);
                break;
            case R.id.confirmation_setting:
                Intent confirmationIntent = new Intent(SettingActivity.this, ConfirmationViewSetting.class);
                startActivity(confirmationIntent);
                break;
            case R.id.guide_setting:
                Intent guideIntent = new Intent(SettingActivity.this, GuideViewSetting.class);
                startActivity(guideIntent);
                break;
            case R.id.qr_setting:
                Intent qrintent = new Intent(SettingActivity.this, QRViewSetting.class);
                startActivity(qrintent);
                break;
            case R.id.btn_exit:
                Util.switchRgbOrIrActivity(SettingActivity.this, true);
                finish();
                break;
        }
    }

    private void endpointDialog() {
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.endpoint_url, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final EditText userInput = promptsView
                .findViewById(R.id.et_endpoint);
        userInput.setText(sharedPreferences.getString(GlobalParameters.URL, EndPoints.prod_url));


        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // get user input and set it to result
                                // edit text
                                String url = userInput.getText().toString().trim();
                                if (url.endsWith("/"))
                                    url = url.substring(0, url.length() - 1);
                                Util.writeString(sharedPreferences, GlobalParameters.URL, url);

                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    private void pwdDialog() {
        View view = getLayoutInflater().inflate(R.layout.access_password, null);

        final EditText acceptUserInput = view.findViewById(R.id.access_pwd);

        final AlertDialog alertDialog = new AlertDialog.Builder(SettingActivity.this)
                .setView(view)
                .setCancelable(false)
                .setPositiveButton("Ok", null)
                .setNegativeButton("Cancel", null)
                .create();

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {

                Button buttonPositive = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                buttonPositive.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        String strUserInput = acceptUserInput.getText().toString().trim();
                        if (TextUtils.isEmpty(strUserInput)) {
                            acceptUserInput.setError("Password should not be empty");
                            return;
                        } else if (acceptUserInput.getText().toString().length() < 6) {
                            acceptUserInput.setError("Password should be minimum six digits");
                        } else {
                            acceptUserInput.setText(acceptUserInput.getText());
                            Util.writeString(sharedPreferences, GlobalParameters.DEVICE_PASSWORD, acceptUserInput.getText().toString().trim());
                            alertDialog.dismiss();
                        }


                    }
                });

                Button buttonNegative = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                buttonNegative.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        alertDialog.dismiss();
                    }
                });

            }
        });
        alertDialog.show();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Util.switchRgbOrIrActivity(SettingActivity.this, true);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 101 && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
//            SharedPreferences myPrefrence = getPreferences(MODE_PRIVATE);
//            SharedPreferences.Editor editor = myPrefrence.edit();
//            editor.putString("imagePreferance", picturePath);
//            editor.commit();
            Util.writeString(sharedPreferences, GlobalParameters.IMAGE_ICON, Util.encodeImagePath(picturePath));
//            bitmap = BitmapFactory.decodeFile(picturePath);
//            Drawable d = new BitmapDrawable(getResources(),bitmap);
//            RelativeLayout bg = (RelativeLayout) findViewById(R.id.abc);
//            bg.setBackground(d);
        }
    }

    @Override
    public void onJSONObjectListener(String reportInfo, String status, JSONObject req) {
        try {
            if (reportInfo == null) {
                return;
            }
            Util.getTokenActivate(reportInfo,status,SettingActivity.this,"setting");
            startHealthCheckService();
        } catch (Exception e) {
            Logger.error("onJSONObjectListenertemperature(String report, String status, JSONObject req)", e.getMessage());
        }
    }

    @Override
    public void onJSONObjectListenerSetting(JSONObject reportInfo, String status, JSONObject req) {
        try {
            if (reportInfo == null) {
                return;
            }
            if (!reportInfo.isNull("Message")) {
                if (reportInfo.getString("Message").contains("token expired"))
                    Util.getToken(this, this);

            }else{
                if (reportInfo.isNull("responseCode")) return;
                if (reportInfo.getString("responseCode").equals("1")) {
                    Util.retrieveSetting(reportInfo,SettingActivity.this);
                } else {
                    Logger.toast(this, "Something went wrong please try again");
                }
            }

        } catch (Exception e) {
            Logger.error("onJSONObjectListenertemperature(String report, String status, JSONObject req)", e.getMessage());
        }
    }

    private void initOnlineModeSetting() {
        switch_activate.setChecked(sharedPreferences.getBoolean(GlobalParameters.ONLINE_MODE, true));
    }

    /**
     * Method that initiates the HealthCheck service if not started
     */
    private void startHealthCheckService() {
        try {
            if (Util.isConnectingToInternet(this) && !Util.isServiceRunning(DeviceHealthService.class, this)) {
                startService(new Intent(this, DeviceHealthService.class));
                Application.StartService(this);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Logger.error("SettingActivity", "initHealthCheckService()", "Exception occurred in starting DeviceHealth Service" + e.getMessage());
        }
    }

    /**
     * Method that stop the HealthCheck service
     * //TODO1: Create BaseActivity for the common code
     */
    private void stopHealthCheckService() {
        Intent intent = new Intent(this, DeviceHealthService.class);
        stopService(intent);
    }
}