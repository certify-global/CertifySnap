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
import com.certify.snap.common.Util;

public class GuideViewSetting extends Activity {
    Typeface rubiklight;
    SharedPreferences sp;
    EditText edittext_text1,edittext_text3,edittext_text2;
    TextView btn_save,titles;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);
        rubiklight = Typeface.createFromAsset(getAssets(),
                "rubiklight.ttf");
        sp = Util.getSharedPreferences(this);
        RadioGroup radio_group_guide = findViewById(R.id.radio_group_guide);
        RadioButton rbguideyes = findViewById(R.id.radio_yes_guide);
        RadioButton rbguideno = findViewById(R.id.radio_no_guide);
        edittext_text1 = findViewById(R.id.edittext_text1);
        edittext_text2 = findViewById(R.id.edittext_text2);
        edittext_text3 = findViewById(R.id.edittext_text3);
        btn_save = findViewById(R.id.btn_exit);
        titles = findViewById(R.id.titles);
        btn_save.setTypeface(rubiklight);
        titles.setTypeface(rubiklight);

        if(sp.getBoolean(GlobalParameters.GUIDE_SCREEN,true))
            rbguideyes.setChecked(true);
        else rbguideno.setChecked(true);

        radio_group_guide.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId==R.id.radio_yes_guide)
                    Util.writeBoolean(sp, GlobalParameters.GUIDE_SCREEN, true);
                else Util.writeBoolean(sp, GlobalParameters.GUIDE_SCREEN, false);
            }
        });

        edittext_text1.setText(sp.getString(GlobalParameters.GUIDE_TEXT1,"Please center your face to the screen."));
        edittext_text2.setText(sp.getString(GlobalParameters.GUIDE_TEXT2,"Come Closer and Center your face"));
        edittext_text3.setText(sp.getString(GlobalParameters.GUIDE_TEXT3,"Please wait, preparing to scan"));

        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!edittext_text1.getText().toString().isEmpty())
                    Util.writeString(sp,GlobalParameters.GUIDE_TEXT1,edittext_text1.getText().toString());
                if(!edittext_text2.getText().toString().isEmpty())
                    Util.writeString(sp, GlobalParameters.GUIDE_TEXT2, edittext_text2.getText().toString());
                if(!edittext_text3.getText().toString().isEmpty())
                    Util.writeString(sp, GlobalParameters.GUIDE_TEXT3, edittext_text3.getText().toString());
            }
        });

    }

    public void onParamterback(View view) {
        startActivity(new Intent(GuideViewSetting.this,SettingActivity.class));
        finish();
    }
}
