package com.certify.snap.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.certify.snap.R;
import com.certify.snap.model.SmartHealthCardData;
import com.certify.snap.qrverification.CertificateModel;

import org.w3c.dom.Text;

public class SmartHealthResultActivity extends AppCompatActivity {
    public static String TAG = "SmartHealthResultActivity";
    private boolean verification;
    private TextView userName, dob, date1, location1, lotnumber1, date2, location2, lotnumber2;
    private ImageView img_close;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smart_health);
        initView();
        updateUI();
        setClickListener();
    }

    private void updateUI() {
        SmartHealthCardData model = (SmartHealthCardData) getIntent().getSerializableExtra("smartHealthModel");
        verification = getIntent().getBooleanExtra("verification", false);
        if (verification) {
            userName.setText(model.getName());
            dob.setText(model.getDob());
            if (model.getDoseType() != null && model.getDoseType().equals("1")) {
                date1.setText(model.getDose1Date());
                location1.setText(model.getVaccinationLocation1());
                lotnumber1.setText(model.getDose1Lotnumber());
            }
            if (model.getDoseType1() != null && model.getDoseType1().equals("2")) {
                date2.setText(model.getDose2Date());
                location2.setText(model.getVaccinationLocation2());
                lotnumber2.setText(model.getDose2Lotnumber());
            }
        } else {

        }

    }

    private void setClickListener() {
        img_close.setOnClickListener(v -> finish());
    }

    private void initView() {
        userName = findViewById(R.id.textView2);
        dob = findViewById(R.id.tv_dob);
        date1 = findViewById(R.id.tv_vaccination_date);
        location1 = findViewById(R.id.vaccine_location);
        lotnumber1 = findViewById(R.id.tv_lot_number);
        date2 = findViewById(R.id.tv_vaccination_date1);
        location2 = findViewById(R.id.vaccine_location1);
        lotnumber2 = findViewById(R.id.tv_lot_number2);
        img_close = findViewById(R.id.img_close);
    }


}
