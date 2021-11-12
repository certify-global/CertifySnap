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
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
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
import com.certify.snap.qrverification.CertificateModel;
import com.certify.snap.qrverification.QRModelData;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class QRCodeResultActivity extends AppCompatActivity {
    public static String TAG = "QRCodeResultActivity";
    private TextView familyName, QRDob, dateVaccination, firstName, given_name, issuer_country, certificate_identifier, medicinal_product, manufacturer;
    private Button buttonDone, buttonRetry;
    private LinearLayout linearLayout_success;
    private RelativeLayout relativeLayout_failure;
    private boolean verification;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode_result);
        initView();
        updateUI();
        setClickListener();
    }

    private void updateUI() {
        CertificateModel model = getIntent().getParcelableExtra("certificateModel");
        verification=getIntent().getBooleanExtra("verification",false);
        if(verification) {
            linearLayout_success.setVisibility(View.VISIBLE);
            relativeLayout_failure.setVisibility(View.GONE);
            familyName.setText(model.getPerson().getFamilyName());
            firstName.setText(model.getFullName());
            QRDob.setText(model.getDateOfBirth());
            dateVaccination.setText(model.getVaccinations().get(0).getDateOfVaccination());
            given_name.setText(model.getPerson().getGivenName());
            issuer_country.setText(model.getVaccinations().get(0).getCertificateIssuer());
            certificate_identifier.setText(model.getVaccinations().get(0).getCertificateIdentifier());
            medicinal_product.setText(model.getVaccinations().get(0).getMedicinalProduct());
            manufacturer.setText(model.getVaccinations().get(0).getManufacturer());
        }else{
            linearLayout_success.setVisibility(View.GONE);
            relativeLayout_failure.setVisibility(View.VISIBLE);
        }

    }

    private void setClickListener() {
        buttonDone.setOnClickListener(v -> finish());
        buttonRetry.setOnClickListener(v -> finish());
    }

    private void initView() {
        familyName = findViewById(R.id.family_name);
        QRDob = findViewById(R.id.qr_dob);
        dateVaccination = findViewById(R.id.date_vaccination);
        buttonDone = findViewById(R.id.btn_done);
        firstName = findViewById(R.id.first_name);
        given_name = findViewById(R.id.given_name);
        issuer_country = findViewById(R.id.issuer_country);
        buttonRetry = findViewById(R.id.btn_retry);
        certificate_identifier = findViewById(R.id.certificate_identifier);
        medicinal_product = findViewById(R.id.medicinal_product);
        manufacturer = findViewById(R.id.manufacturer);
        linearLayout_success = findViewById(R.id.linearLayout_success);
        relativeLayout_failure = findViewById(R.id.relativeLayout_failure);

    }


}
