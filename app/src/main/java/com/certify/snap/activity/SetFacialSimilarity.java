package com.certify.snap.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.certify.snap.R;
import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.Util;

public class SetFacialSimilarity extends AppCompatActivity {

    EditText editTextDialogUserInput;
    SharedPreferences sp;
    TextView btn_save;

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
