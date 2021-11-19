package com.certify.snap.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;

import androidx.annotation.RequiresApi;
import androidx.annotation.UiThread;

import com.certify.snap.common.AppSettings;
import com.certify.snap.common.License;
import com.certify.snap.controller.DatabaseController;
import com.google.android.material.snackbar.Snackbar;

import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
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

import com.arcsoft.face.FaceEngine;
import com.certify.callback.JSONObjectCallback;
import com.certify.callback.SettingCallback;
import com.certify.snap.R;
import com.certify.snap.common.Application;
import com.certify.snap.common.EndPoints;
import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.Logger;
import com.certify.snap.common.Util;
import com.certify.snap.service.DeviceHealthService;

import org.json.JSONObject;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.certify.snap.common.GlobalParameters.DEVICE_NAME;
import static com.certify.snap.common.GlobalParameters.ONLINE_MODE;

public class SettingsActivity extends SettingsBaseActivity implements JSONObjectCallback, SettingCallback {

    private static final String TAG = SettingsActivity.class.getSimpleName();
    private FaceEngine faceEngine = new FaceEngine();
    private SharedPreferences sharedPreferences;
    private RelativeLayout activate, init, updatelist, management, register, parameter, led, card, record, setting_temperature,
                    setting_upload, setting_access_password, setting_endpoint, thermal_check_setting, scan_setting,
                    confirmation_setting, guide_setting, qr_setting, audio_visual_setting, printer_settings_layout,touchless_interaction;
    RadioGroup rg_temperature;
    RadioButton rb_temp, rb_temp_face;
    TextView access_pwd, upload_logo, setTemp, parameter_setting, activate_tv, endpoint, tv_version, tv_thermal_setting,
            tv_scan_setting, tv_confirmation_setting, tv_serial_no, tv_guide_setting, tv_qr_setting, tv_member_management,
            tv_visual_settings, printer_setting_text,touchless_interaction_setting;
    Typeface rubiklight;
    private String userMail;
    private LinearLayout llSettings;
    private AlertDialog.Builder builder;
    ImageView img_sync;
    RelativeLayout relative_layout;
    Switch switch_activate;
    private RelativeLayout accessControl;
    private TextView accessControlTv, tvDeviceOnline, tvDeviceName, tvDeviceSettings, tvDeviceMode;
    private View temperature_setting_view,upload_logo_setting_view, home_view_setting_view, scan_view_setting_view,
            confirmation_screen_setting_view, guide_setting_view, member_management_setting_view, audio_visual_setting_view,
            identification_setting_view, access_control_setting_view, printer_setting_view;
    private TextView mConnectivityStatus, tvRecord;
    private View recordDivider;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_settings);
            Util.getNumberVersion(SettingsActivity.this);
            rubiklight = Typeface.createFromAsset(getAssets(),
                    "rubiklight.ttf");
            sharedPreferences = Util.getSharedPreferences(this);
            //rg_temperature = findViewById(R.id.radio_group_work_flow);
            rb_temp = findViewById(R.id.radio_temp);
            rb_temp_face = findViewById(R.id.face_temp);
            img_sync = findViewById(R.id.img_sync);
            relative_layout = findViewById(R.id.relative_layout);
            switch_activate = findViewById(R.id.switch_activate);
            String FlowType = sharedPreferences.getString(GlobalParameters.TEMP_ONLY, "temp");

       /*     rg_temperature.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
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
            });*/
            switch_activate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        Toast.makeText(getApplicationContext(), getString(R.string.online_msg), Toast.LENGTH_LONG).show();
                        // Util.writeBoolean(sharedPreferences, GlobalParameters.ONLINE_SWITCH, true);
                        Util.activateApplication(SettingsActivity.this, SettingsActivity.this);

                    } else {
                        // Util.writeBoolean(sharedPreferences, GlobalParameters.ONLINE_SWITCH, false);
                        Toast.makeText(getApplicationContext(), getString(R.string.offline_msg), Toast.LENGTH_LONG).show();
                        Util.writeBoolean(sharedPreferences, ONLINE_MODE, false);
                        stopHealthCheckService();
                    }
                }
            });

            initView();
            //initOnlineModeSetting();
            Util.setTokenRequestName("");
            Application.getInstance().addActivity(this);

            sharedPreferences = Util.getSharedPreferences(this);
            boolean activateStatus = sharedPreferences.getBoolean("activate", false);
            if (!activateStatus) License.activateLicense(this);
//                new AsyncActiveEngine(SettingActivity.this, sharedPreferences, null, Util.getSNCode()).execute();
            img_sync.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (sharedPreferences.getBoolean(ONLINE_MODE, true)) {
                        Util.getSettings(SettingsActivity.this, SettingsActivity.this);
                        Snackbar snackbar = Snackbar
                                .make(relative_layout, R.string.snack_msg, Snackbar.LENGTH_LONG);
                        snackbar.show();
                    } else {
                        Snackbar snackbar = Snackbar
                                .make(relative_layout, R.string.offline_msg, Snackbar.LENGTH_LONG);
                        snackbar.show();
                    }


                }
            });
            mConnectivityStatus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(SettingsActivity.this, ConnectivityStatusActivity.class));
                }
            });
            if (sharedPreferences.getBoolean(ONLINE_MODE, true)) {
                switch_activate.setChecked(true);
            } else {
                switch_activate.setChecked(false);
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
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
        tvRecord = findViewById(R.id.tv_record);
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
        tv_member_management = findViewById(R.id.member_management_setting);
        tv_visual_settings = findViewById(R.id.visual_setting);
        tv_guide_setting = findViewById(R.id.tv_guide_setting);
        tv_qr_setting = findViewById(R.id.tv_qr_setting);
        accessControl = findViewById(R.id.access_control);
        accessControlTv = findViewById(R.id.access_control_tv);
        tvDeviceOnline = findViewById(R.id.tv_online);
        tvDeviceName = findViewById(R.id.tv_device_name);
        tvDeviceSettings = findViewById(R.id.tv_device_setting);
        tvDeviceMode = findViewById(R.id.tv_device_model);
        mConnectivityStatus = findViewById(R.id.tv_connectivity_status);
        temperature_setting_view = findViewById(R.id.temperature_setting_view);
        upload_logo_setting_view = findViewById(R.id.upload_logo_setting_view);
        home_view_setting_view = findViewById(R.id.home_view_setting_view);
        scan_view_setting_view = findViewById(R.id.scan_view_setting_view);
        confirmation_screen_setting_view = findViewById(R.id.confirmation_screen_setting_view);
        guide_setting_view = findViewById(R.id.guide_setting_view);
        member_management_setting_view = findViewById(R.id.member_management_setting_view);
        audio_visual_setting_view = findViewById(R.id.audio_visual_setting_view);
        identification_setting_view = findViewById(R.id.identification_setting_view);
        access_control_setting_view = findViewById(R.id.access_control_setting_view);
        audio_visual_setting = findViewById(R.id.audio_visual_setting);
        printer_settings_layout = findViewById(R.id.printer_settings_layout);
        printer_setting_view = findViewById(R.id.printer_setting_view);
        printer_setting_text = findViewById(R.id.printer_setting_text);

        recordDivider = findViewById(R.id.record_divider);
        touchless_interaction = findViewById(R.id.touchless_interaction);
        touchless_interaction_setting = findViewById(R.id.touchless_interaction_setting);

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
        tv_member_management.setTypeface(rubiklight);
        tv_visual_settings.setTypeface(rubiklight);
        tv_guide_setting.setTypeface(rubiklight);
        tv_qr_setting.setTypeface(rubiklight);
        tvDeviceOnline.setTypeface(rubiklight);
        tvDeviceName.setTypeface(rubiklight);
        tvDeviceSettings.setTypeface(rubiklight);
        tvDeviceMode.setTypeface(rubiklight);
        tvRecord.setTypeface(rubiklight);
        touchless_interaction_setting.setTypeface(rubiklight);
        printer_setting_text.setTypeface(rubiklight);
        tv_version.setText(Util.getVersionBuild());
        tv_serial_no.setText(getString(R.string.serial_num) + Util.getSNCode(this));
        accessControlTv.setTypeface(rubiklight);

        String text = "<a style='text-decoration:underline' href='http://www.sample.com'>"+getString(R.string.connectivity_status)+"</a>";
        mConnectivityStatus.setTypeface(rubiklight);
        if (Build.VERSION.SDK_INT >= 24) {
            mConnectivityStatus.setText(Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY));
        } else {
            mConnectivityStatus.setText(Html.fromHtml(text));
        }
        setData();
    }

    @Override
    protected void onStart() {
        super.onStart();
        proSettings();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Util.isNetworkOff(SettingsActivity.this)) {
            record.setVisibility(View.VISIBLE);
            recordDivider.setVisibility(View.VISIBLE);
        }else {
            record.setVisibility(View.GONE);
            recordDivider.setVisibility(View.GONE);
        }
        setData();
    }

    public void setData() {
        try {
            if (tvDeviceOnline == null) return;
            tvDeviceOnline.setText(String.format("%s: %s", getResources().getString(R.string.online_device_activation_status), sharedPreferences.getBoolean(ONLINE_MODE, true) ? getString(R.string.activated) : getString(R.string.not_activated_msg)));
            tvDeviceName.setText(String.format("%s: %s", getResources().getString(R.string.device_name), sharedPreferences.getString(DEVICE_NAME, "New Name")));
            tvDeviceSettings.setText(String.format("%s: %s", getResources().getString(R.string.device_settings), sharedPreferences.getString(GlobalParameters.DEVICE_SETTINGS_NAME, "Local")));
            tvDeviceMode.setText(String.format("%s: %s", getResources().getString(R.string.device_mode), sharedPreferences.getBoolean(GlobalParameters.ONLINE_MODE, true) ? getString(R.string.online) : getString(R.string.offline)));

        } catch (Exception e) {

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void onclick(View view) {
        boolean isopen = sharedPreferences.getBoolean("activate", false);
        switch (view.getId()) {
            case R.id.setting_activate:
                if (Util.isConnectingToInternet(SettingsActivity.this) && sharedPreferences.getBoolean(ONLINE_MODE, true)) {
                    Util.activateApplication(SettingsActivity.this, SettingsActivity.this);
                } else {
                    Snackbar snackbar = Snackbar
                            .make(relative_layout, R.string.offline_msg, Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
                break;
            case R.id.rl_device_setting:
                startActivity(new Intent(SettingsActivity.this, DeviceSettingsActivity.class));
                break;
            case R.id.setting_managment:
                if (isopen)
                    startActivity(new Intent(SettingsActivity.this, MemberManagementActivity.class));
                break;
            case R.id.setting_led:
                break;
            case R.id.setting_record:
                if (isopen) startActivity(new Intent(SettingsActivity.this, OfflineRecordsActivity.class));
                break;
            case R.id.setting_temperature:
                //temperatureDialog();
                Intent tempIntent = new Intent(SettingsActivity.this, TemperatureSettingsActivity.class);
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
                Intent intent = new Intent(SettingsActivity.this, HomeSettingsActivity.class);
                startActivity(intent);
                break;
            case R.id.scan_setting:
                Intent intent1 = new Intent(SettingsActivity.this, ScanSettingsActivity.class);
                startActivity(intent1);
                break;
            case R.id.confirmation_setting:
                Intent confirmationIntent = new Intent(SettingsActivity.this, ConfirmationSettingsActivity.class);
                startActivity(confirmationIntent);
                break;
            case R.id.guide_setting:
                Intent guideIntent = new Intent(SettingsActivity.this, GuideSettingsActivity.class);
                startActivity(guideIntent);
                break;
            case R.id.qr_setting:
                Intent qrintent = new Intent(SettingsActivity.this, IdentificationSettingsActivity.class);
                startActivity(qrintent);
                break;
            case R.id.access_control:
                Intent acIntent = new Intent(SettingsActivity.this, AccessControlSettingsActivity.class);
                startActivity(acIntent);
                break;
            case R.id.management_setting:
                Intent memberIntent = new Intent(SettingsActivity.this, MemberManagementActivity.class);
                startActivity(memberIntent);
                break;
            case R.id.audio_visual_setting:
                Intent visualIntent = new Intent(SettingsActivity.this, AudioVisualSettingsActivity.class);
                startActivity(visualIntent);
                break;
            case R.id.printer_settings_layout:
                Intent printerIntent = new Intent(SettingsActivity.this, PrinterSettingsActivity.class);
                startActivity(printerIntent);
                break;
            case R.id.touchless_interaction:
                Intent touchlessIntent = new Intent(SettingsActivity.this, TouchlessSettingsActivity.class);
                startActivity(touchlessIntent);
                break;
            case R.id.btn_exit:
                initiateLaunchHomeScreen();
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
                .setPositiveButton(getString(R.string.button_ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // get user input and set it to result
                                // edit text
                                if (!sharedPreferences.getString(GlobalParameters.URL, EndPoints.prod_url).equals(userInput.getText().toString().trim())) {
                                    //LitePal.deleteAll(RegisteredMembers.class);
                                    DatabaseController.getInstance().deleteAllMember();
                                    Util.clearAllSharedPreferences(sharedPreferences);
                                    Util.activateApplication(SettingsActivity.this, SettingsActivity.this);
                                }
                                String url = userInput.getText().toString().trim();
                                if (url.endsWith("/"))
                                    url = url.substring(0, url.length() - 1);
                                Util.writeString(sharedPreferences, GlobalParameters.URL, url);

                            }
                        })
                .setNegativeButton(getString(R.string.cancel),
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

        final AlertDialog alertDialog = new AlertDialog.Builder(SettingsActivity.this)
                .setView(view)
                .setCancelable(false)
                .setPositiveButton(getString(R.string.button_ok), null)
                .setNegativeButton(getString(R.string.cancel), null)
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
                            acceptUserInput.setError(getString(R.string.password_empty_msg));
                            return;
                        } else if (acceptUserInput.getText().toString().length() < 6) {
                            acceptUserInput.setError(getString(R.string.password_length_msg));
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
        //super.onBackPressed();
        initiateLaunchHomeScreen();
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

    @RequiresApi(api = Build.VERSION_CODES.N)
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

            Util.getTokenActivate(reportInfo, status, SettingsActivity.this, "setting");
            startHealthCheckService();
            if (json1.isNull("responseSubCode")) return;
            if (json1.getString("responseSubCode").equals("104")) {
                switch_activate.setChecked(false);
            } else if (json1.getString("responseSubCode").equals("105")) {
                switch_activate.setChecked(false);
            } else if (json1.has("responseCode") && json1.getString("responseCode").equals("1")) {
                switch_activate.setChecked(true);
            } else if (json1.getString("responseSubCode").equals("103")) {
                switch_activate.setChecked(true);
            }


        } catch (Exception e) {
            Logger.error(TAG,"onJSONObjectListener(String report, String status, JSONObject req)", e.getMessage());
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

            } else {
                if (reportInfo.isNull("responseCode")) return;
                if (reportInfo.getString("responseCode").equals("1")) {
                    Util.retrieveSetting(reportInfo, SettingsActivity.this);
                } else {
                    Logger.toast(this, "Something went wrong please try again");
                }
            }

        } catch (Exception e) {
            Logger.error(TAG,"onJSONObjectListenerSetting(String report, String status, JSONObject req)", e.getMessage());
        }
    }

    private void initOnlineModeSetting() {
        switch_activate.setChecked(sharedPreferences.getBoolean(ONLINE_MODE, true));
    }

    /**
     * Method that initiates the HealthCheck service if not started
     */
    private void startHealthCheckService() {
        try {
            if (Util.isConnectingToInternet(this) && !Util.isServiceRunning(DeviceHealthService.class, this) && sharedPreferences.getBoolean(ONLINE_MODE, false)) {
                startService(new Intent(this, DeviceHealthService.class));
                Application.StartService(this);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Logger.error(TAG, "initHealthCheckService()", "Exception occurred in starting DeviceHealth Service" + e.getMessage());
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

    private void proSettings(){
        if (Util.isDeviceProModel()) {
            Log.d(TAG, "proSettings: true");
            if (AppSettings.isProSettings()) {
                setting_upload.setVisibility(View.GONE);
                thermal_check_setting.setVisibility(View.GONE);
                confirmation_setting.setVisibility(View.GONE);
                accessControl.setVisibility(View.GONE);
                guide_setting.setVisibility(View.GONE);
                audio_visual_setting.setVisibility(View.GONE);
                printer_settings_layout.setVisibility(View.GONE);

                upload_logo_setting_view.setVisibility(View.GONE);
                home_view_setting_view.setVisibility(View.GONE);
                confirmation_screen_setting_view.setVisibility(View.GONE);
                access_control_setting_view.setVisibility(View.GONE);
                guide_setting_view.setVisibility(View.GONE);
                audio_visual_setting_view.setVisibility(View.GONE);
                printer_setting_view.setVisibility(View.GONE);
            } else {
                setting_upload.setVisibility(View.VISIBLE);
                thermal_check_setting.setVisibility(View.VISIBLE);
                confirmation_setting.setVisibility(View.VISIBLE);
                accessControl.setVisibility(View.VISIBLE);
                guide_setting.setVisibility(View.VISIBLE);
                audio_visual_setting.setVisibility(View.VISIBLE);
                printer_settings_layout.setVisibility(View.VISIBLE);

                upload_logo_setting_view.setVisibility(View.VISIBLE);
                home_view_setting_view.setVisibility(View.VISIBLE);
                confirmation_screen_setting_view.setVisibility(View.VISIBLE);
                access_control_setting_view.setVisibility(View.VISIBLE);
                guide_setting_view.setVisibility(View.VISIBLE);
                audio_visual_setting_view.setVisibility(View.VISIBLE);
                printer_setting_view.setVisibility(View.VISIBLE);
            }
        }
        else {
            Log.d(TAG, "proSettings: false");
            setting_upload.setVisibility(View.VISIBLE);
            thermal_check_setting.setVisibility(View.VISIBLE);
            confirmation_setting.setVisibility(View.VISIBLE);
            accessControl.setVisibility(View.VISIBLE);
            guide_setting.setVisibility(View.VISIBLE);
            audio_visual_setting.setVisibility(View.VISIBLE);
            printer_settings_layout.setVisibility(View.VISIBLE);

            upload_logo_setting_view.setVisibility(View.VISIBLE);
            home_view_setting_view.setVisibility(View.VISIBLE);
            confirmation_screen_setting_view.setVisibility(View.VISIBLE);
            access_control_setting_view.setVisibility(View.VISIBLE);
            guide_setting_view.setVisibility(View.VISIBLE);
            audio_visual_setting_view.setVisibility(View.VISIBLE);
            printer_setting_view.setVisibility(View.VISIBLE);
        }
    }

    private void initiateLaunchHomeScreen() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ProgressDialog.show(SettingsActivity.this, "", getString(R.string.launch_home_screen));
            }
        });
        Observable
                .create((ObservableOnSubscribe<Boolean>) emitter -> {
                    AppSettings.getInstance().getSettingsFromSharedPref(SettingsActivity.this);
                    emitter.onNext(true);
                })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Boolean>() {
                    Disposable settingDisposable;

                    @Override
                    public void onSubscribe(Disposable d) {
                        settingDisposable = d;
                    }

                    @Override
                    public void onNext(Boolean value) {
                        launchHomeScreen();
                        finish();
                        settingDisposable.dispose();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "Error in fetching settings from the server");
                        launchHomeScreen();
                        finish();
                    }

                    @Override
                    public void onComplete() {
                        //do noop
                    }
                });
    }

    private void launchHomeScreen() {
        if (Util.isDeviceProModel()) {
            if (!AppSettings.isProSettings()) {
                startActivity(new Intent(this, IrCameraActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            } else {
                startActivity(new Intent(this, ProIrCameraActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }
        } else {
            Util.switchRgbOrIrActivity(SettingsActivity.this, true);
        }
    }
}