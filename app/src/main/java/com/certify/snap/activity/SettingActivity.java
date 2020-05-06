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
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.arcsoft.face.ActiveFileInfo;
import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.certify.callback.JSONObjectCallback;
import com.certify.snap.common.Application;
import com.certify.snap.common.Constants;
import com.certify.snap.common.EndPoints;
import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.Logger;
import com.certify.snap.common.Util;
import com.certify.snap.R;

import org.json.JSONObject;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class SettingActivity extends Activity implements JSONObjectCallback {

    private FaceEngine faceEngine = new FaceEngine();
    private SharedPreferences sp;
    private RelativeLayout activate, init, updatelist, management, register, parameter, led, card, record, setting_temperature, setting_upload, setting_access_password, setting_endpoint,
            thermal_check_setting, scan_setting,confirmation_setting;
    RadioGroup rg_temperature;
    RadioButton rb_temp, rb_temp_face;
    TextView access_pwd, upload_logo, setTemp, parameter_setting, activate_tv, endpoint, tv_version, tv_thermal_setting, tv_scan_setting,tv_confirmation_setting;
    Typeface rubiklight;
    private String userMail;
    private LinearLayout llSettings;
    private AlertDialog.Builder builder;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_setting);
        Util.getNumberVersion(SettingActivity.this);
        rubiklight = Typeface.createFromAsset(getAssets(),
                "rubiklight.ttf");
        sp = Util.getSharedPreferences(this);
        rg_temperature = findViewById(R.id.radio_group_work_flow);
        rb_temp = findViewById(R.id.radio_temp);
        rb_temp_face = findViewById(R.id.face_temp);
        String FlowType = sp.getString(GlobalParameters.TEMP_ONLY, "temp");

        rg_temperature.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.radio_camera_0:
                        Util.writeString(sp, GlobalParameters.TEMP_ONLY, "temp");
                        break;
                    case R.id.radio_camera_1:
                        Util.writeString(sp, GlobalParameters.FACE_TEMP, "facetemp");
                        break;

                }
            }
        });

        initView();
        Application.getInstance().addActivity(this);

        sp = Util.getSharedPreferences(this);
        if (sp.getBoolean("activate", false)) {
            Log.e("sp---true", "activate:" + sp.getBoolean("activate", false));
        } else {
            activeEngine(null);
            Log.e("sp---false", "activate:" + sp.getBoolean("activate", false));
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
        endpoint = findViewById(R.id.endpoint);
        tv_version = findViewById(R.id.tv_version);
        tv_thermal_setting = findViewById(R.id.tv_thermal_setting);
        tv_scan_setting = findViewById(R.id.tv_scan_setting);
        tv_confirmation_setting = findViewById(R.id.tv_confirmation_setting);
        access_pwd.setTypeface(rubiklight);
        setTemp.setTypeface(rubiklight);
        upload_logo.setTypeface(rubiklight);
        parameter_setting.setTypeface(rubiklight);
        activate_tv.setTypeface(rubiklight);
        endpoint.setTypeface(rubiklight);
        tv_version.setTypeface(rubiklight);
        tv_thermal_setting.setTypeface(rubiklight);
        tv_scan_setting.setTypeface(rubiklight);
        tv_confirmation_setting.setTypeface(rubiklight);
        tv_version.setText("Serial No: "+Util.getSNCode() +"  "+"Version: " + sp.getString(GlobalParameters.MobileAppVersion, ""));
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    public void activeEngine(final View view) {
        if (view != null) {
            view.setClickable(false);
        }
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                int activeCode = FaceEngine.activeOnline(SettingActivity.this, Constants.APP_ID, Constants.SDK_KEY);
                emitter.onNext(activeCode);
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Integer activeCode) {
                        if (activeCode == ErrorInfo.MOK) {
                            Util.writeBoolean(sp, "activate", true);
                        } else if (activeCode == ErrorInfo.MERR_ASF_ALREADY_ACTIVATED) {
                            Util.writeBoolean(sp, "activate", true);
                        } else {
                            Util.writeBoolean(sp, "activate", false);
                            //  hide();
                        }

                        if (view != null) {
                            view.setClickable(true);
                        }
                        ActiveFileInfo activeFileInfo = new ActiveFileInfo();
                        int res = FaceEngine.getActiveFileInfo(SettingActivity.this, activeFileInfo);
                        if (res == ErrorInfo.MOK) {
                            Log.e("activate---", activeFileInfo.toString());
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void onclick(View view) {
        boolean isopen = sp.getBoolean("activate", false);
        switch (view.getId()) {
            case R.id.setting_activate:
                //activeEngine(null);
                Util.activateApplication(SettingActivity.this, SettingActivity.this);
                break;
            case R.id.setting_init:
                if (isopen)
                    startActivity(new Intent(SettingActivity.this, InitializationActivity.class));
                break;
//            case R.id.setting_updatelist:
//                if(isopen) startActivity(new Intent(SettingActivity.this,UpdateActivity.class));
//                break;
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
                Intent tempIntent=new Intent(SettingActivity.this,TemperatureActivity.class);
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
        userInput.setText(sp.getString(GlobalParameters.URL, EndPoints.prod_url));


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
                                Util.writeString(sp, GlobalParameters.URL, url);

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
                            Util.writeString(sp, GlobalParameters.DEVICE_PASSWORD, acceptUserInput.getText().toString().trim());
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
            Util.writeString(sp, GlobalParameters.IMAGE_ICON, picturePath);
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
            JSONObject json1 = null;
            try {
                String formatedString = reportInfo.substring(1, reportInfo.length() - 1);
                json1 = new JSONObject(formatedString.replace("\\", ""));

            } catch (Exception e) {
                e.printStackTrace();
                json1 = new JSONObject(reportInfo/*.replace("\\", "")*/);
            }
            if (json1.getString("responseCode").equals("1")) {
                Util.writeString(sp, GlobalParameters.ONLINE_MODE, "true");
                Logger.toast(SettingActivity.this, "Device Activated");

            } else if (json1.getString("responseSubCode").equals("103")) {
                Util.writeString(sp, GlobalParameters.ONLINE_MODE, "true");
                Logger.toast(SettingActivity.this, "Already Activated");
            } else if (json1.getString("responseSubCode").equals("104")) {
                Logger.toast(SettingActivity.this, "Device Not Register");
            } else if (json1.getString("responseSubCode").equals("105")) {
                Logger.toast(SettingActivity.this, "Device Inactive");
            }

        } catch (Exception e) {
            Logger.error("onJSONObjectListenertemperature(String report, String status, JSONObject req)", e.getMessage());
        }
    }


}
