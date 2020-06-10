package com.certify.snap.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.certify.snap.common.Application;
import com.certify.snap.common.ConfigUtil;
import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.ShellUtils;
import com.certify.snap.common.Util;
import com.certify.snap.R;
import static com.arcsoft.face.enums.DetectFaceOrientPriority.ASF_OP_0_ONLY;
import static com.arcsoft.face.enums.DetectFaceOrientPriority.ASF_OP_180_ONLY;
import static com.arcsoft.face.enums.DetectFaceOrientPriority.ASF_OP_270_ONLY;
import static com.arcsoft.face.enums.DetectFaceOrientPriority.ASF_OP_90_ONLY;
import static com.arcsoft.face.enums.DetectFaceOrientPriority.ASF_OP_ALL_OUT;

public class ParameterActivity extends AppCompatActivity {

    private EditText relaytime;
    RadioGroup radioGroupCamera,radioGroupLiving,radioGroupOrientation,radioGroupFtOrient;
    RadioButton rgbCamera, irCamera;
    RadioButton rbLiving0,rbLiving1;
    RadioButton rbOrientation0,rbOrientation90,rbOrientation180,rbOrientation270;
    RadioButton rbOrient0,rbOrient90,rbOrient180,rbOrient270,rbOrientAll;
    RadioGroup radio_group_led,radio_group_statusbar,radio_group_navigationbar;
    RadioButton radio_led_on,radio_led_off,radio_led_lamp;
    RadioButton radio_statusbar_show,radio_statusbar_hide,radio_navigationbar_show,radio_navigationbar_hide;
    private SeekBar seekBar;
    SharedPreferences sp;
    TextView title_save;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            setContentView(R.layout.activity_parameter);

            Application.getInstance().addActivity(this);
            sp = Util.getSharedPreferences(this);
            initView();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void initView(){
        radioGroupCamera = findViewById(R.id.radio_group_camera_type);
        rgbCamera = findViewById(R.id.radio_camera_0);
        irCamera = findViewById(R.id.radio_camera_1);
        title_save = findViewById(R.id.title_save);
        int cameratype = sp.getInt(GlobalParameters.CameraType, Camera.CameraInfo.CAMERA_FACING_BACK);
        switch (cameratype) {
            case Camera.CameraInfo.CAMERA_FACING_BACK:
                rgbCamera.setChecked(true);
                break;
            case Camera.CameraInfo.CAMERA_FACING_FRONT:
                irCamera.setChecked(true);
                break;
            default:
                rgbCamera.setChecked(true);
                break;
        }
        radioGroupCamera.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.radio_camera_0:
                        Util.writeInt(sp, GlobalParameters.CameraType,Camera.CameraInfo.CAMERA_FACING_BACK);
                        break;
                    case R.id.radio_camera_1:
                        Util.writeInt(sp, GlobalParameters.CameraType,Camera.CameraInfo.CAMERA_FACING_FRONT);
                        break;
                    default:
                        Util.writeInt(sp, GlobalParameters.CameraType,Camera.CameraInfo.CAMERA_FACING_BACK);
                        break;
                }
            }
        });

        title_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.showToast(ParameterActivity.this, getString(R.string.save_success));
                finish();
            }
        });

        radioGroupLiving = findViewById(R.id.radio_group_living_type);
        rbLiving0 = findViewById(R.id.radio_living_0);
        rbLiving1 = findViewById(R.id.radio_living_1);
        boolean living = sp.getBoolean(GlobalParameters.LivingType,false);

        if (living){
            rbLiving1.setChecked(true);
        }else {
            rbLiving0.setChecked(true);
        }

        radioGroupLiving.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.radio_living_0:
                        Util.writeBoolean(sp, GlobalParameters.LivingType,false);
                        break;
                    case R.id.radio_living_1:
                        Util.writeBoolean(sp, GlobalParameters.LivingType,true);
                        break;
                    default:
                        Util.writeBoolean(sp, GlobalParameters.LivingType,true);
                        break;
                }
            }
        });

        radioGroupOrientation = findViewById(R.id.radio_group_orientation);
        rbOrientation0 = findViewById(R.id.rb_orientation_0);
        rbOrientation90 = findViewById(R.id.rb_orientation_90);
        rbOrientation180 = findViewById(R.id.rb_orientation_180);
        rbOrientation270 = findViewById(R.id.rb_orientation_270);
        int orient = sp.getInt(GlobalParameters.Orientation,0);
        switch (orient) {
            case 0:
                rbOrientation0.setChecked(true);
                break;
            case 90:
                rbOrientation90.setChecked(true);
                break;
            case 180:
                rbOrientation180.setChecked(true);
                break;
            case 270:
                rbOrientation270.setChecked(true);
                break;
            default:
                rbOrientation0.setChecked(true);
                break;
        }
        radioGroupOrientation.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rb_orientation_0:
                        Util.writeInt(sp, GlobalParameters.Orientation,0);
                        break;
                    case R.id.rb_orientation_90:
                        Util.writeInt(sp, GlobalParameters.Orientation,90);
                        break;
                    case R.id.rb_orientation_180:
                        Util.writeInt(sp, GlobalParameters.Orientation,180);
                        break;
                    case R.id.rb_orientation_270:
                        Util.writeInt(sp, GlobalParameters.Orientation,270);
                        break;
                    default:
                        Util.writeInt(sp, GlobalParameters.Orientation,0);
                        break;
                }
            }
        });


         radioGroupFtOrient = findViewById(R.id.radio_group_ft_orient);
         rbOrient0 = findViewById(R.id.rb_orient_0);
         rbOrient90 = findViewById(R.id.rb_orient_90);
         rbOrient180 = findViewById(R.id.rb_orient_180);
         rbOrient270 = findViewById(R.id.rb_orient_270);
         rbOrientAll = findViewById(R.id.rb_orient_all);
         switch (ConfigUtil.getFtOrient(this)) {
             case ASF_OP_0_ONLY:
                rbOrient0.setChecked(true);
                break;
            case ASF_OP_90_ONLY:
                rbOrient90.setChecked(true);
                break;
            case ASF_OP_180_ONLY:
                rbOrient180.setChecked(true);
                break;
            case ASF_OP_270_ONLY:
                rbOrient270.setChecked(true);
                break;
             case ASF_OP_ALL_OUT:
                rbOrientAll.setChecked(true);
                break;
            default:
                rbOrientAll.setChecked(true);
                break;
        }
        radioGroupFtOrient.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rb_orient_0:
                        ConfigUtil.setFtOrient(ParameterActivity.this, ASF_OP_0_ONLY);
                        break;
                    case R.id.rb_orient_90:
                        ConfigUtil.setFtOrient(ParameterActivity.this, ASF_OP_90_ONLY);
                        break;
                    case R.id.rb_orient_180:
                        ConfigUtil.setFtOrient(ParameterActivity.this, ASF_OP_180_ONLY);
                        break;
                    case R.id.rb_orient_270:
                        ConfigUtil.setFtOrient(ParameterActivity.this, ASF_OP_270_ONLY);
                        break;
                    case R.id.rb_orient_all:
                        ConfigUtil.setFtOrient(ParameterActivity.this, ASF_OP_ALL_OUT);
                        break;
                    default:
                        ConfigUtil.setFtOrient(ParameterActivity.this, ASF_OP_90_ONLY);
                        break;
                }
            }
        });
        radio_group_led=findViewById(R.id.radio_group_led);
        radio_led_on=findViewById(R.id.radio_led_on);
        radio_led_off=findViewById(R.id.radio_led_off);
        radio_led_lamp=findViewById(R.id.radio_led_lamp);
        seekBar=findViewById(R.id.seekbar);

        int led = sp.getInt(GlobalParameters.LedType,0);
        switch (led) {
            case 0:
                radio_led_on.setChecked(true);
                break;
            case 1:
                radio_led_off.setChecked(true);
               // PosUtil.setLedPower(0);
                Util.setLedPower(0);
                break;
            case 2:
                radio_led_lamp.setChecked(true);
                //PosUtil.setLedPower(0);
 //               Util.setLedPower(0);
                break;

            default:
                radio_led_off.setChecked(true);
                //PosUtil.setLedPower(0);
                Util.setLedPower(0);
                break;
        }
        radio_group_led.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.radio_led_on:
                        Util.writeInt(sp, GlobalParameters.LedType,0);
                        break;
                    case R.id.radio_led_off:
                        Util.writeInt(sp, GlobalParameters.LedType,1);
                        //PosUtil.setLedPower(0);
                        Util.setLedPower(0);
                        break;
                    case R.id.radio_led_lamp:
                        Util.writeInt(sp, GlobalParameters.LedType,2);

                    default:
                        Util.writeInt(sp, GlobalParameters.LedType,1);
                       // PosUtil.setLedPower(0);
                        Util.setLedPower(0);
                        break;
                }
            }
        });

        radio_group_statusbar = findViewById(R.id.radio_group_statusbar);
        radio_statusbar_show = findViewById(R.id.radio_status_show);
        radio_statusbar_hide = findViewById(R.id.radio_status_hide);
        boolean statusbar = sp.getBoolean(GlobalParameters.StatusBar,true);

        if (statusbar){
            radio_statusbar_show.setChecked(true);
            sendBroadcast(new Intent(GlobalParameters.ACTION_OPEN_STATUSBAR));
        }else {
            radio_statusbar_hide.setChecked(true);
            sendBroadcast(new Intent(GlobalParameters.ACTION_CLOSE_STATUSBAR));
        }

        radio_group_statusbar.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.radio_status_show:
                        Util.writeBoolean(sp,GlobalParameters.StatusBar,true);
                        sendBroadcast(new Intent(GlobalParameters.ACTION_OPEN_STATUSBAR));
                        break;
                    case R.id.radio_status_hide:
                        Util.writeBoolean(sp,GlobalParameters.StatusBar,false);
                        sendBroadcast(new Intent(GlobalParameters.ACTION_CLOSE_STATUSBAR));
                        break;
                }
            }
        });
        radio_group_navigationbar = findViewById(R.id.radio_group_navigationbar);
        radio_navigationbar_show = findViewById(R.id.radio_navigation_show);
        radio_navigationbar_hide = findViewById(R.id.radio_navigation_hide);
        boolean navigationbar = sp.getBoolean(GlobalParameters.NavigationBar,true);

        if (navigationbar){
            radio_navigationbar_show.setChecked(true);
            sendBroadcast(new Intent(GlobalParameters.ACTION_SHOW_NAVIGATIONBAR));
        }else {
            radio_navigationbar_hide.setChecked(true);
            sendBroadcast(new Intent(GlobalParameters.ACTION_HIDE_NAVIGATIONBAR));
        }

        radio_group_navigationbar.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.radio_navigation_show:
                        Util.writeBoolean(sp,GlobalParameters.NavigationBar,true);
                        sendBroadcast(new Intent(GlobalParameters.ACTION_SHOW_NAVIGATIONBAR));
                        break;
                    case R.id.radio_navigation_hide:
                        Util.writeBoolean(sp,GlobalParameters.NavigationBar,false);
                        sendBroadcast(new Intent(GlobalParameters.ACTION_HIDE_NAVIGATIONBAR));
                        break;
                }
            }
        });
        relaytime = findViewById(R.id.edit_relaytime);
        int relaytimeresult = sp.getInt(GlobalParameters.RelayTime,5);
        if(relaytimeresult > 0){
            relaytime.setText(""+ relaytimeresult);
        }
        relaytime.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString().trim();
                if(!TextUtils.isEmpty(text)) {
                    try {
                        int time = Integer.parseInt(text);
                        Util.writeInt(sp, GlobalParameters.RelayTime, time);
                        Log.e("relaytime---", text + "-" + time);
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(Build.MODEL.contains("950")||"TPS980Q".equals(Build.MODEL)){
                    progress /= 8;
                    Log.e("progress---",progress+"");
                    Util.setLedPower(progress);
                }else {
                    Process p = null;
                    //String cmd = "echo " + progress + " > /sys/class/backlight/rk28_bl_sub/brightness";
                    //String cmd = "echo " + progress + " > /sys/class/backlight/backlight_extend/brightness";//新
                    String cmd = "echo " + progress + " > /sys/class/backlight/led-brightness/brightness";//新
                    ShellUtils.execCommand(cmd, false);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

    }

    public void onParamterback(View view) {
        startActivity(new Intent(ParameterActivity.this,SettingActivity.class));
        finish();
    }
}
