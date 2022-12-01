package com.certify.snap.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.certify.snap.R;
import com.certify.snap.qrverification.CertificateModel;

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
