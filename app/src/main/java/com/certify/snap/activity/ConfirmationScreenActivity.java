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
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.certify.snap.R;
import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.Logger;
import com.certify.snap.common.Util;
import com.certify.snap.faceserver.CompareResult;
import com.certify.snap.faceserver.FaceServer;

import java.io.File;

public class ConfirmationScreenActivity extends Activity {
    Typeface rubiklight;
    TextView tv_title, tv_subtitle, user_name;
    private SharedPreferences sp;
    String value;
    private long delayMilli = 0;
    String longVal ;
    ImageView user_img;
    CompareResult compareResultValues;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            setContentView(R.layout.activity_confirmation_screen);

            Intent intent = getIntent();
            value = getIntent().getStringExtra("tempVal");
            if (intent.getSerializableExtra("compareResult") != null)
                compareResultValues = (CompareResult) intent.getSerializableExtra("compareResult");

            rubiklight = Typeface.createFromAsset(getAssets(),
                    "rubiklight.ttf");
            sp = Util.getSharedPreferences(this);
            tv_title = findViewById(R.id.tv_title);
            tv_subtitle = findViewById(R.id.tv_subtitle);

            user_img = findViewById(R.id.iv_item_head_img);
            user_name = findViewById(R.id.tv_item_name);
            if(compareResultValues!= null && !sp.getBoolean(GlobalParameters.DISPLAY_IMAGE_CONFIRMATION,false)){
                compareResult();
            }

            if (value.equals("high")) {
                longVal = sp.getString(GlobalParameters.DELAY_VALUE_CONFIRM_ABOVE, "1");
                if (sp.getString(GlobalParameters.Confirm_title_above, "Please contact your supervisor before starting any work.").isEmpty())
                    tv_title.setText("Please contact your supervisor before starting any work.");
                else
                    tv_title.setText(sp.getString(GlobalParameters.Confirm_title_above, "Please contact your supervisor before starting any work."));
                tv_subtitle.setText(sp.getString(GlobalParameters.Confirm_subtitle_above, ""));
            } else {
                longVal = sp.getString(GlobalParameters.DELAY_VALUE_CONFIRM_BELOW, "1");
                if (sp.getString(GlobalParameters.Confirm_title_below, "Have a nice day!").isEmpty())
                    tv_title.setText("Have a nice day!");
                else
                    tv_title.setText(sp.getString(GlobalParameters.Confirm_title_below, "Have a nice day!"));
                tv_subtitle.setText(sp.getString(GlobalParameters.Confirm_subtitle_below, ""));
            }
            tv_title.setTypeface(rubiklight);
            tv_subtitle.setTypeface(rubiklight);

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

                }
            }, delayMilli*900);

            Log.d("delay milli seconds",""+delayMilli);

        } catch (Exception e) {
            Logger.error(" onCreate(@Nullable Bundle savedInstanceState)", e.getMessage());
        }
    }

    private void compareResult() {
        try {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    File imgFile = new File(FaceServer.ROOT_PATH + File.separator + FaceServer.SAVE_IMG_DIR + File.separator + compareResultValues.getUserName() + FaceServer.IMG_SUFFIX);
                    Glide.with(ConfirmationScreenActivity.this)
                            .load(imgFile)
                            .skipMemoryCache(true)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .into(user_img);
                    user_name.setText(compareResultValues.getMessage());
                }
            });

        }
        catch (Exception e){
            Logger.error(" compare result", e.getMessage());
        }

    }

}
