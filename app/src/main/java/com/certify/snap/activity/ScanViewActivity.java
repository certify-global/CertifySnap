package com.certify.snap.activity;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.certify.snap.R;
import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.Util;

public class ScanViewActivity extends AppCompatActivity {

    private SharedPreferences sp;
    EditText et_screen_delay;
    Button btn_save;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            setContentView(R.layout.activity_scan_view);
            sp = Util.getSharedPreferences(this);
            RadioGroup rgCapture = findViewById(R.id.radio_group_capture);
            RadioButton rbCaptureYes = findViewById(R.id.radio_yes_capture);
            RadioButton rbCaptureNo = findViewById(R.id.radio_no_capture);
            RadioGroup rgCaptureAll = findViewById(R.id.radio_group_capture_all);
            RadioButton rbCaptureAllYes = findViewById(R.id.radio_yes_capture_all);
            RadioButton rbCaptureAllNo = findViewById(R.id.radio_no_capture_all);
            RadioGroup radio_group_tempe = findViewById(R.id.radio_group_tempe);
            RadioButton radio_yes_temp = findViewById(R.id.radio_yes_temp);
            RadioButton radio_no_temp = findViewById(R.id.radio_no_temp);
            RadioGroup radio_group_sound = findViewById(R.id.radio_group_sound);
            RadioButton radio_yes_sound = findViewById(R.id.radio_yes_sound);
            RadioButton radio_no_sound = findViewById(R.id.radio_no_sound);
            et_screen_delay = findViewById(R.id.et_screen_delay);
            btn_save = findViewById(R.id.btn_exit);

            if(sp.getBoolean(GlobalParameters.CAPTURE_IMAGES_ABOVE,true))
                rbCaptureYes.setChecked(true);
            else rbCaptureNo.setChecked(true);
            if(sp.getBoolean(GlobalParameters.CAPTURE_IMAGES_ALL,false))
                rbCaptureAllYes.setChecked(true);
            else rbCaptureAllNo.setChecked(true);
            if(sp.getBoolean(GlobalParameters.CAPTURE_TEMPERATURE,true))
                radio_yes_temp.setChecked(true);
            else radio_no_temp.setChecked(true);
            if(sp.getBoolean(GlobalParameters.CAPTURE_SOUND,true))
                radio_yes_sound.setChecked(true);
            else radio_no_sound.setChecked(true);

            rgCapture.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    if(checkedId==R.id.radio_yes_capture)
                        Util.writeBoolean(sp, GlobalParameters.CAPTURE_IMAGES_ABOVE, true);
                    else Util.writeBoolean(sp, GlobalParameters.CAPTURE_IMAGES_ABOVE, false);
                }
            });
            rgCaptureAll.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    if(checkedId==R.id.radio_yes_capture_all)
                        Util.writeBoolean(sp, GlobalParameters.CAPTURE_IMAGES_ALL, true);
                    else Util.writeBoolean(sp, GlobalParameters.CAPTURE_IMAGES_ALL, false);

                }
            });
            radio_group_tempe.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    if(checkedId==R.id.radio_yes_temp)
                        Util.writeBoolean(sp, GlobalParameters.CAPTURE_TEMPERATURE, true);
                    else Util.writeBoolean(sp, GlobalParameters.CAPTURE_TEMPERATURE, false);
                }
            });
            radio_group_sound.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    if(checkedId==R.id.radio_yes_sound)
                        Util.writeBoolean(sp, GlobalParameters.CAPTURE_SOUND, true);
                    else Util.writeBoolean(sp, GlobalParameters.CAPTURE_SOUND, false);
                }
            });


            btn_save.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Util.writeString(sp,GlobalParameters.DELAY_VALUE,et_screen_delay.getText().toString().trim());
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
