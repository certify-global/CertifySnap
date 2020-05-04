package com.certify.snap.activity;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.certify.snap.R;
import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.Util;

public class ThermalSetting extends Activity {
    TextInputLayout  text_input_title,text_input_subtitle;
    EditText edittext_subtitle,edittext_title;
    SharedPreferences sp;
    Button btn_save;
    Typeface rubiklight;
    TextView tv_welcome,tv_thermal_view;
    String title="";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            setContentView(R.layout.activity_thermal_setting);
            rubiklight = Typeface.createFromAsset(getAssets(),
                    "rubiklight.ttf");
            text_input_title = findViewById(R.id.text_input_title);
            text_input_subtitle = findViewById(R.id.text_input_subtitle);
            edittext_title = findViewById(R.id.edittext_title);
            edittext_subtitle = findViewById(R.id.edittext_subtitle);
            btn_save = findViewById(R.id.btn_exit);
            tv_welcome = findViewById(R.id.tv_welcome);
            tv_thermal_view = findViewById(R.id.tv_thermal_view);
            tv_welcome.setTypeface(rubiklight);
            tv_thermal_view.setTypeface(rubiklight);
            sp = Util.getSharedPreferences(this);

            edittext_title.setText(sp.getString(GlobalParameters.Thermalscan_title,""));
            edittext_subtitle.setText(sp.getString(GlobalParameters.Thermalscan_subtitle,""));


            btn_save.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                    title = edittext_title.getText().toString();
                    if (title.equals("")) {
                        Util.writeString(sp, GlobalParameters.Thermalscan_title, "THERMAL SCAN");
                    } else {
                        Util.writeString(sp, GlobalParameters.Thermalscan_title, title);
                    }
                    Util.writeString(sp, GlobalParameters.Thermalscan_subtitle, edittext_subtitle.getText().toString());

                }
            });


        }catch(Exception e){
            e.printStackTrace();
        }



    }
}
