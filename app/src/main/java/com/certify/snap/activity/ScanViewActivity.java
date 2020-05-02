package com.certify.snap.activity;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.certify.snap.R;
import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.Util;

public class ScanViewActivity extends AppCompatActivity {

    private SharedPreferences sp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_scan_view);
            sp = Util.getSharedPreferences(this);
            RadioGroup rgCapture = findViewById(R.id.radio_group_capture);
            RadioButton rbCaptureYes = findViewById(R.id.radio_yes_capture);
            RadioButton rbCaptureNo = findViewById(R.id.radio_no_capture);
            RadioGroup rgCaptureAll = findViewById(R.id.radio_group_capture_all);
            RadioButton rbCaptureAllYes = findViewById(R.id.radio_yes_capture_all);
            RadioButton rbCaptureAllNo = findViewById(R.id.radio_no_capture_all);
            if(sp.getBoolean(GlobalParameters.CAPTURE_IMAGES_ABOVE,true))
                rbCaptureYes.setChecked(true);
            else rbCaptureNo.setChecked(true);
            if(sp.getBoolean(GlobalParameters.CAPTURE_IMAGES_ALL,true))
                rbCaptureAllYes.setChecked(true);
            else rbCaptureAllNo.setChecked(true);

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
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
