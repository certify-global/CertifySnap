package com.certify.snap.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.certify.snap.R;
import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.Logger;
import com.certify.snap.common.Util;

public class QRViewSetting extends Activity {
   private static String  TAG ="GuideViewSetting";
    Typeface rubiklight;
    SharedPreferences sp;
    TextView btn_save,titles,qr_screen;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr);
        try {
            rubiklight = Typeface.createFromAsset(getAssets(),
                    "rubiklight.ttf");
            sp = Util.getSharedPreferences(this);
            RadioGroup radio_group_qr = findViewById(R.id.radio_group_qr);
            RadioButton rbguideyes = findViewById(R.id.radio_yes_qr);
            RadioButton rbguideno = findViewById(R.id.radio_no_qr);
            btn_save = findViewById(R.id.btn_exit);
            qr_screen = findViewById(R.id.qr_screen);
            titles = findViewById(R.id.titles);
            btn_save.setTypeface(rubiklight);
            titles.setTypeface(rubiklight);
            qr_screen.setTypeface(rubiklight);

            if (sp.getBoolean(GlobalParameters.QR_SCREEN, false))
                rbguideyes.setChecked(true);
            else rbguideno.setChecked(true);

            radio_group_qr.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    if (checkedId == R.id.radio_yes_qr)
                        Util.writeBoolean(sp, GlobalParameters.QR_SCREEN, true);
                    else Util.writeBoolean(sp, GlobalParameters.QR_SCREEN, false);
                }
            });


            btn_save.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(QRViewSetting.this,SettingActivity.class));
                    Util.showToast(QRViewSetting.this, getString(R.string.save_success));
                    finish();
                }
            });
        }catch (Exception e){
            Logger.error(TAG,e.toString());
        }
    }

    public void onParamterback(View view) {
        startActivity(new Intent(QRViewSetting.this,SettingActivity.class));
        finish();
    }
}
