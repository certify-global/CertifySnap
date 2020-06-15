package com.certify.snap.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.certify.snap.R;
import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.Util;

public class SetFacialSimilarity extends AppCompatActivity {

    EditText editTextDialogUserInput;
    SharedPreferences sp;
    TextView btn_save;
    RadioGroup radio_group_mask;
    RadioButton radio_yes_mask;
    RadioButton radio_no_mask;
    RadioGroup radio_group_facial;
    RadioButton radio_yes_facial;
    RadioButton radio_no_facial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_set_facial_similarity);
        editTextDialogUserInput = findViewById(R.id.editTextDialogUserInput);
        btn_save = findViewById(R.id.btn_save);
        sp = Util.getSharedPreferences(this);

        editTextDialogUserInput.setText(sp.getString(GlobalParameters.FACIAL_THRESHOLD, "70"));

        radio_group_mask = findViewById(R.id.radio_group_mask);
        radio_yes_mask = findViewById(R.id.radio_yes_mask);
        radio_no_mask = findViewById(R.id.radio_no_mask);
        radio_group_facial = findViewById(R.id.radio_group_facial);
        radio_yes_facial = findViewById(R.id.radio_yes_facial);
        radio_no_facial = findViewById(R.id.radio_no_facial);

        // Mask
        if(sp.getBoolean(GlobalParameters.MASK_DETECT,false)){
            radio_yes_mask.setChecked(true);
        }
        else {
            radio_no_mask.setChecked(true);
        }
        radio_group_mask.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                System.out.println("Test CheckId"+checkedId);
                if(checkedId==R.id.radio_yes_mask){
                    radio_yes_mask.setChecked(true);
                    Util.writeBoolean(sp, GlobalParameters.MASK_DETECT, true);
                }
                else{
                    Util.writeBoolean(sp, GlobalParameters.MASK_DETECT, false);
                }
            }
        });


        // Facial
        if(sp.getBoolean(GlobalParameters.FACIAL_DETECT,false)){
            radio_yes_facial.setChecked(true);
        }
        else {
            radio_no_facial.setChecked(true);
        }
        radio_group_facial.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                System.out.println("Test CheckId"+checkedId);
                if(checkedId==R.id.radio_yes_facial){
                    radio_yes_facial.setChecked(true);
                    Util.writeBoolean(sp, GlobalParameters.FACIAL_DETECT, true);
                }
                else{
                    Util.writeBoolean(sp, GlobalParameters.FACIAL_DETECT, false);
                }
            }
        });


        // Save
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.showToast(SetFacialSimilarity.this, getString(R.string.save_success));
                Util.writeString(sp, GlobalParameters.FACIAL_THRESHOLD, editTextDialogUserInput.getText().toString().trim());
                finish();
            }
        });
    }

    public void onParamterback(View view) {
        startActivity(new Intent(SetFacialSimilarity.this, SettingActivity.class));
        finish();
    }
}
