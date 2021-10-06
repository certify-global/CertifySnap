package com.certify.snap.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.certify.callback.FlowListCallback;
import com.certify.snap.R;
import com.certify.snap.api.response.TouchlessSettings;
import com.certify.snap.async.AsyncJSONObjectFlowList;
import com.certify.snap.common.AppSettings;
import com.certify.snap.common.EndPoints;
import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.Util;
import com.certify.snap.controller.DatabaseController;
import com.certify.snap.controller.DeviceSettingsController;
import com.certify.snap.controller.GestureController;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class QRCodeResultActivity extends AppCompatActivity {
    public static String TAG = "QRCodeResultActivity";
    private TextView familyName,QRDob,dateVaccination,firstName;
    private Button buttonDone;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode_result);
        initView();
        setClickListener();
    }

    private void setClickListener() {
        buttonDone.setOnClickListener(view -> finish());
    }

    private void initView() {
        familyName=findViewById(R.id.family_name);
        QRDob=findViewById(R.id.qr_dob);
        dateVaccination=findViewById(R.id.date_vaccination);
        buttonDone=findViewById(R.id.btn_done);
        firstName=findViewById(R.id.first_name);

    }


}
