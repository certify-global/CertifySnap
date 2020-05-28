package com.certify.snap.activity;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.certify.callback.JSONObjectCallback;
import com.certify.callback.QRCodeCallback;
import com.certify.snap.R;
import com.certify.snap.async.AsyncJSONObjectQRCode;
import com.certify.snap.common.EndPoints;
import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.Logger;
import com.certify.snap.common.Util;

import org.json.JSONObject;

public class QRCodeActivity extends Activity implements QRCodeCallback, JSONObjectCallback {
    Typeface rubiklight;
    private SharedPreferences sharedPreferences;
    View image;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            setContentView(R.layout.activity_qr_screen);
            sharedPreferences = Util.getSharedPreferences(this);
            image=findViewById(R.id.qr_imageView);
            Animation animation =
                    AnimationUtils.loadAnimation(getApplicationContext(), R.anim.qr_line_anim);
            image.startAnimation(animation);
            rubiklight = Typeface.createFromAsset(getAssets(),
                    "rubiklight.ttf");
            if (Util.isConnectingToInternet(QRCodeActivity.this) && (sharedPreferences.getBoolean(GlobalParameters.ONLINE_MODE, true))) {
                sendQR();
            }


        } catch (Exception e) {
            Logger.error(" onCreate(@Nullable Bundle savedInstanceState)", e.getMessage());
        }
    }

    private void sendQR() {
        try {
            JSONObject obj = new JSONObject();
            obj.put("qrCodeID", sharedPreferences.getString(GlobalParameters.QRCODE_ID, ""));

            new AsyncJSONObjectQRCode(obj, this, sharedPreferences.getString(GlobalParameters.URL, EndPoints.prod_url) + EndPoints.ValidateQRCode, this).execute();
        } catch (Exception e) {
            Logger.error("sendQR() ", e.getMessage());
        }
    }


    @Override
    public void onJSONObjectListenerQRCode(JSONObject reportInfo, String status, JSONObject req) {
        try {
            if (reportInfo == null) {
                return;
            }

            if (!reportInfo.isNull("Message")) {
                if (reportInfo.getString("Message").contains("token expired"))
                    Util.getToken(this, this);
                  Util.switchRgbOrIrActivity(this,true);


            } else {
                if (reportInfo.isNull("responseCode")) return;
                if (reportInfo.getString("responseCode").equals("1")) {
//                    Util.getQRCode(reportInfo, status,QRCodeActivity.this,"QRCode");
                    SharedPreferences sharedPreferences = Util.getSharedPreferences(this);
                    String snapID = reportInfo.getJSONObject("responseData").getString("snapID");
                    Util.writeString(sharedPreferences, GlobalParameters.SNAP_ID, snapID);
                    Util.writeBoolean(sharedPreferences, GlobalParameters.QRCODE_Valid, true);
                    Util.switchRgbOrIrActivity(this,true);
                } else {
                    Logger.toast(this, "Invalid QRCode");
                    Util.writeBoolean(sharedPreferences, GlobalParameters.QRCODE_Valid, false);
                    Util.switchRgbOrIrActivity(this,true);


                }
            }


        } catch (Exception e) {
            Logger.error(" onJSONObjectListenerQRCode(JSONObject reportInfo, String status, JSONObject req)", e.getMessage());
        }
    }

    @Override
    public void onJSONObjectListener(String report, String status, JSONObject req) {

    }
}
