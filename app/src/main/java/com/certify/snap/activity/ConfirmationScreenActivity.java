package com.certify.snap.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.certify.snap.R;
import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.Logger;
import com.certify.snap.common.Util;

public class ConfirmationScreenActivity extends Activity {
    Typeface rubiklight;
    TextView tv_title, tv_subtitle;
    private SharedPreferences sp;
    String value;
    private long delayMilli = 0;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            setContentView(R.layout.activity_confirmation_screen);
            value = getIntent().getStringExtra("tempVal");
            Log.d("eeeeeeeeee", value);
            rubiklight = Typeface.createFromAsset(getAssets(),
                    "rubiklight.ttf");
            sp = Util.getSharedPreferences(this);
            tv_title = findViewById(R.id.tv_title);
            tv_subtitle = findViewById(R.id.tv_subtitle);

            if (value.equals("high")) {
                if (sp.getString(GlobalParameters.Confirm_title_above, "Thank You").isEmpty())
                    tv_title.setText("Thank You");
                else
                    tv_title.setText(sp.getString(GlobalParameters.Confirm_title_above, "Thank You"));
                tv_subtitle.setText(sp.getString(GlobalParameters.Confirm_subtitle_above, ""));
            } else {
                if (sp.getString(GlobalParameters.Confirm_title_below, "Thank You").isEmpty())
                    tv_title.setText("Thank You");
                else
                    tv_title.setText(sp.getString(GlobalParameters.Confirm_title_below, "Thank You"));
                tv_subtitle.setText(sp.getString(GlobalParameters.Confirm_subtitle_below, ""));
            }
            tv_title.setTypeface(rubiklight);
            tv_subtitle.setTypeface(rubiklight);
            String longVal = sp.getString(GlobalParameters.DELAY_VALUE_CONFIRM, "3");
            if (longVal.equals("")) {
                delayMilli = 3;
            } else {
                delayMilli = Long.parseLong(longVal);
            }
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Util.switchRgbOrIrActivity(ConfirmationScreenActivity.this, true);
                    finish();
//                    Intent intent=new Intent(ConfirmationScreenActivity.this,IrCameraActivity.class);
//                    startActivity(intent);

                }
            }, delayMilli*1000);

        } catch (Exception e) {
            Logger.error(" onCreate(@Nullable Bundle savedInstanceState)", e.getMessage());
        }
    }
}
