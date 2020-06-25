package com.certify.snap.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import androidx.annotation.Nullable;

import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.certify.snap.R;
import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.Logger;
import com.certify.snap.common.Util;

public class GuideViewSetting extends SettingBaseActivity {
   private static String  TAG = GuideViewSetting.class.getSimpleName();
    Typeface rubiklight;
    SharedPreferences sp;
    EditText edittext_text1,edittext_text3,edittext_text2,edittext_text4;
    TextView btn_save,titles,guide_screen;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);
        try {
            rubiklight = Typeface.createFromAsset(getAssets(),
                    "rubiklight.ttf");
            sp = Util.getSharedPreferences(this);
            RadioGroup radio_group_guide = findViewById(R.id.radio_group_guide);
            RadioButton rbguideyes = findViewById(R.id.radio_yes_guide);
            RadioButton rbguideno = findViewById(R.id.radio_no_guide);
            edittext_text1 = findViewById(R.id.edittext_text1);
            edittext_text2 = findViewById(R.id.edittext_text2);
            edittext_text3 = findViewById(R.id.edittext_text3);
            edittext_text4 = findViewById(R.id.edittext_text4);
            btn_save = findViewById(R.id.btn_exit);
            guide_screen = findViewById(R.id.guide_screen);
            titles = findViewById(R.id.titles);
            btn_save.setTypeface(rubiklight);
            titles.setTypeface(rubiklight);
            guide_screen.setTypeface(rubiklight);

            if (sp.getBoolean(GlobalParameters.GUIDE_SCREEN, true))
                rbguideyes.setChecked(true);
            else rbguideno.setChecked(true);

            radio_group_guide.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    if (checkedId == R.id.radio_yes_guide)
                        Util.writeBoolean(sp, GlobalParameters.GUIDE_SCREEN, true);
                    else Util.writeBoolean(sp, GlobalParameters.GUIDE_SCREEN, false);
                }
            });

            edittext_text1.setText(sp.getString(GlobalParameters.GUIDE_TEXT1, getResources().getString(R.string.text_value1)));
            edittext_text2.setText(sp.getString(GlobalParameters.GUIDE_TEXT2, getResources().getString(R.string.text_value2)));
            edittext_text3.setText(sp.getString(GlobalParameters.GUIDE_TEXT3, getResources().getString(R.string.text_value3)));
            edittext_text4.setText(sp.getString(GlobalParameters.GUIDE_TEXT4, getResources().getString(R.string.text_value4)));

            btn_save.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!edittext_text1.getText().toString().isEmpty())
                        Util.writeString(sp, GlobalParameters.GUIDE_TEXT1, edittext_text1.getText().toString());
                    if (!edittext_text2.getText().toString().isEmpty())
                        Util.writeString(sp, GlobalParameters.GUIDE_TEXT2, edittext_text2.getText().toString());
                    if (!edittext_text3.getText().toString().isEmpty())
                        Util.writeString(sp, GlobalParameters.GUIDE_TEXT3, edittext_text3.getText().toString());
                    if (!edittext_text4.getText().toString().isEmpty())
                        Util.writeString(sp, GlobalParameters.GUIDE_TEXT4, edittext_text4.getText().toString());
                    startActivity(new Intent(GuideViewSetting.this,SettingActivity.class));
                    Util.showToast(GuideViewSetting.this, getString(R.string.save_success));
                    finish();
                }
            });
        }catch (Exception e){
            Log.e(TAG,e.toString());
        }
    }
}
