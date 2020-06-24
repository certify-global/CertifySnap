package com.certify.snap.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.Nullable;

import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.certify.snap.R;
import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.Logger;
import com.certify.snap.common.Util;
import com.certify.snap.controller.CameraController;
import com.certify.snap.faceserver.CompareResult;
import com.certify.snap.faceserver.FaceServer;
import com.certify.snap.model.AccessControlModel;
import com.certify.snap.model.RegisteredMembers;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

public class ConfirmationScreenActivity extends Activity {
    Typeface rubiklight;
    TextView tv_title, tv_subtitle, user_name, face_score;
    private SharedPreferences sp;
    String value ="";
    private long delayMilli = 0;

    String longVal;
    ImageView user_img;
    CompareResult compareResultValues;

    private String confirm_title,
            confirm_subtitle ;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            setContentView(R.layout.activity_confirmation_screen);
            Intent intent = getIntent();
            value = getIntent().getStringExtra("tempVal");
            compareResultValues = CameraController.getInstance().getCompareResult();

            rubiklight = Typeface.createFromAsset(getAssets(),
                    "rubiklight.ttf");
            sp = Util.getSharedPreferences(this);
            tv_title = findViewById(R.id.tv_title);
            tv_subtitle = findViewById(R.id.tv_subtitle);

            user_img = findViewById(R.id.iv_item_head_img);
            user_name = findViewById(R.id.tv_item_name);
            face_score = findViewById(R.id.facial_score);

            if (compareResultValues != null && sp.getBoolean(GlobalParameters.DISPLAY_IMAGE_CONFIRMATION, false)) {
                user_img.setVisibility(View.VISIBLE);
                compareResult();
            } else if (CameraController.getInstance().isFaceNotMatchedOnRetry()) {
                showSnackBarMessage("Potential face match didn't happen. Please retry");
            } else {
                onAccessCardMatch();
                //user_img.setVisibility(View.GONE);
            }
            if (value.equals("high")) {
                confirm_title = sp.getString(GlobalParameters.Confirm_title_above, getResources().getString(R.string.confirmation_text_above));
                confirm_subtitle = sp.getString(GlobalParameters.Confirm_subtitle_above, "");
                longVal = sp.getString(GlobalParameters.DELAY_VALUE_CONFIRM_ABOVE, "1");
                tv_title.setText(confirm_title);
                tv_subtitle.setText(confirm_subtitle);
            } else {
                confirm_title = sp.getString(GlobalParameters.Confirm_title_below, getResources().getString(R.string.confirm_title_below));
                confirm_subtitle = sp.getString(GlobalParameters.Confirm_subtitle_below, "");
                longVal = sp.getString(GlobalParameters.DELAY_VALUE_CONFIRM_BELOW, "1");
                tv_title.setText(confirm_title);
                tv_subtitle.setText(confirm_subtitle);
            }

            tv_title.setTypeface(rubiklight);
            tv_subtitle.setTypeface(rubiklight);
            tv_title.setTextSize(titleSize(confirm_title.length()));
            tv_subtitle.setTextSize(titleSizeSub(confirm_subtitle.length()));
            Log.d("ConfirmationScreen", "Deep confirmation setting time value " + longVal);
            if (longVal.equals("")) {
                delayMilli = 3;
            } else {
                delayMilli = Long.parseLong(longVal);
            }
            Log.d("ConfirmationScreen", "Deep confirmation setting time value " + delayMilli);
            setHomeScreenTimer();

            Log.d("delay milli seconds", "" + delayMilli);

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
                    face_score.setText(compareResultValues.getFacialScore());
                }
            });
            CameraController.getInstance().setCompareResult(null);
        } catch (Exception e) {
            Logger.error(" compare result", e.getMessage());
            CameraController.getInstance().setCompareResult(null);
        }

    }

    private void onAccessCardMatch() {
        RegisteredMembers matchedMember = AccessControlModel.getInstance().getRfidScanMatchedMember();
        if (matchedMember != null) {
            Glide.with(ConfirmationScreenActivity.this)
                    .load(matchedMember.getImage())
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(user_img);
            user_name.setText(matchedMember.getFirstname());
        }else {
            user_img.setVisibility(View.GONE);
            user_name.setVisibility(View.GONE);
            face_score.setVisibility(View.GONE);
        }
    }

    private float titleSize(int titleLength) {
        if (titleLength > 500)
            return 28;
        else if (titleLength > 150)
            return 34;
        else return 40;
    }
    private float titleSizeSub(int titleLength) {
        if (titleLength > 500)
            return 22;
        else if (titleLength > 150)
            return 28;
        else return 32;
    }

    private void showSnackBarMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void setHomeScreenTimer() {
        Timer time = new Timer();
        time.schedule(new TimerTask() {
            @Override
            public void run() {
                startActivity(new Intent(getApplicationContext(), IrCameraActivity.class));
                finish();
            }
        }, delayMilli * 400);
    }
}
