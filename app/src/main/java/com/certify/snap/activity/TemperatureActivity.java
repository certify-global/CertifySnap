package com.certify.snap.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.certify.snap.R;
import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.Util;

public class TemperatureActivity extends Activity {
    EditText editTextDialogUserInput;
    SharedPreferences sp;
    TextView btn_save;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temperature);
        editTextDialogUserInput = findViewById(R.id.editTextDialogUserInput);
        btn_save = findViewById(R.id.btn_save);
        sp = Util.getSharedPreferences(this);
        RadioGroup radio_group_f_c = findViewById(R.id.radio_group_f_c);
        final RadioButton radio_f = findViewById(R.id.radio_f);
        RadioButton radio_c = findViewById(R.id.radio_c);

        editTextDialogUserInput.setText(sp.getString(GlobalParameters.TEMP_TEST, "100.4"));

        if (sp.getString(GlobalParameters.F_TO_C, "F").equals("F")) {
            radio_f.setChecked(true);
        } else radio_c.setChecked(true);

        radio_group_f_c.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radio_f) Util.writeString(sp, GlobalParameters.F_TO_C, "F");
                else Util.writeString(sp, GlobalParameters.F_TO_C, "C");

            }
        });

        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.showToast(TemperatureActivity.this, getString(R.string.save_success));
                Util.writeString(sp, GlobalParameters.TEMP_TEST, editTextDialogUserInput.getText().toString().trim());
                finish();
            }
        });


    }

    public void onParamterback(View view) {
        startActivity(new Intent(TemperatureActivity.this, SettingActivity.class));
        finish();
    }
}
