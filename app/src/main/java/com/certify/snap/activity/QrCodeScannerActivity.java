package com.certify.snap.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.certify.snap.R;
import com.certify.snap.qrverification.CertificateModel;
import com.certify.snap.qrverification.DiseaseType;
import com.certify.snap.qrverification.PersonModel;
import com.certify.snap.qrverification.VaccinationModel;
import com.certify.snap.qrverification.QRModelData;
import com.certify.snap.qrverification.VerificationFragment;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.BarcodeView;
import com.journeyapps.barcodescanner.DefaultDecoderFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import dgca.verifier.app.decoder.base45.DefaultBase45Service;
import dgca.verifier.app.decoder.cbor.DefaultCborService;
import dgca.verifier.app.decoder.cbor.GreenCertificateData;
import dgca.verifier.app.decoder.compression.DefaultCompressorService;
import dgca.verifier.app.decoder.cose.DefaultCoseService;
import dgca.verifier.app.decoder.cose.VerificationCryptoService;
import dgca.verifier.app.decoder.model.CertificateType;
import dgca.verifier.app.decoder.model.CoseData;
import dgca.verifier.app.decoder.model.GreenCertificate;
import dgca.verifier.app.decoder.model.Person;
import dgca.verifier.app.decoder.model.Vaccination;
import dgca.verifier.app.decoder.model.VerificationResult;
import dgca.verifier.app.decoder.prefixvalidation.DefaultPrefixValidationService;
import dgca.verifier.app.decoder.schema.DefaultSchemaValidator;
import dgca.verifier.app.decoder.services.X509;


import dagger.hilt.android.AndroidEntryPoint;
public class QrCodeScannerActivity extends AppCompatActivity {

    private static final String TAG = QrCodeScannerActivity.class.getSimpleName();
    private BarcodeView barcodeScanner;
    private FragmentManager fragmentManager=null;
    private AppCompatImageView cameraSquareImage;
    private boolean verification;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_code_reader);

        initView();
        barcodeScanner.getCameraSettings().setRequestedCameraId(0);
        initQrCodeScanner();
    }

    @Override
    protected void onResume() {
        super.onResume();
        barcodeScanner.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    private void initView() {
        barcodeScanner = findViewById(R.id.barcode_scanner);
        cameraSquareImage = findViewById(R.id.camera_square_image);
        fragmentManager=getSupportFragmentManager();
    }

    private void initQrCodeScanner() {
        BarcodeCallback barcodeCallback = new BarcodeCallback() {

            @Override
            public void barcodeResult(BarcodeResult result) {
                Log.d(TAG, "barcode result ");
                barcodeScanner.pause();
                verification=true;
                String qrText = result.getText();
                parseQrText(qrText);
            }
        };
        Collection<BarcodeFormat> decodeFormats = new ArrayList<>();
        decodeFormats.add(BarcodeFormat.AZTEC);
        decodeFormats.add(BarcodeFormat.QR_CODE);
        barcodeScanner.setDecoderFactory(new DefaultDecoderFactory(decodeFormats));
        barcodeScanner.decodeContinuous(barcodeCallback);
    }
    private void parseQrText(String qrText){

        VerificationResult verificationResult = new VerificationResult();
        String plainInput = new DefaultPrefixValidationService().decode(qrText, verificationResult);
        byte[] base45Decoded = new DefaultBase45Service().decode(plainInput, verificationResult);
        byte[] decompressed = new DefaultCompressorService().decode(base45Decoded, verificationResult);
        CoseData coseData = new DefaultCoseService().decode(decompressed, verificationResult);

        new DefaultSchemaValidator().validate(coseData.getCbor(), verificationResult);
        GreenCertificateData greenCertificateData = new DefaultCborService().decodeData(coseData.getCbor(), verificationResult);
        //TODO: validateCertData

        String kidBase64 = Base64.encodeToString(coseData.getKid(),Base64.DEFAULT );
        List<Certificate> certificateList = new ArrayList<>();
        byte[] decodedCertBytes = Base64.decode("MIICqjCCAlGgAwIBAgIJAIFaAnBKVQR5MAkGByqGSM49BAEweTELMAkGA1UEBhMCSUUxDzANBgNVBAgMBkR1YmxpbjEPMA0GA1UEBwwGRHVibGluMR0wGwYDVQQKDBREZXBhcnRtZW50IG9mIEhlYWx0aDEQMA4GA1UECwwHZUhlYWx0aDEXMBUGA1UEAwwOQ1NDQV9ER0NfSUVfMDEwHhcNMjEwNzA3MDkzMTIxWhcNMjMwNjI3MDkzMTIxWjBrMQswCQYDVQQGEwJJRTEdMBsGA1UEChMURGVwYXJ0bWVudCBvZiBIZWFsdGgxPTA7BgNVBAMTNHZhbGlkYXRlcXIuZ292LmllIDIwMjAxMzU0NTc5MTIzOTE4MTA1Mzk2MDA5OTk2OTQ1NTUwWTATBgcqhkjOPQIBBggqhkjOPQMBBwNCAARM8Tki2iWsbMtAs+pjE5P6itWZgm6wsOw9YvCsr5tskoEG1cGysPY/L+ixZiv0sXl985GnPGYFRCbOnsri4+Ijo4HQMIHNMA4GA1UdDwEB/wQEAwIHgDAdBgNVHQ4EFgQUT2vp7aj2JBxHrQ1vkpX4bZJoi10wHwYDVR0jBBgwFoAUXIe2JeQrVG0xki6dmWYLVblvTFAwSQYDVR0fBEIwQDA+oDygOoY4aHR0cHM6Ly9nZW4uZGlnaXRhbGNvdmlkY2VydGlmaWNhdGVzLmdvdi5pZS9hcGkvQ1NDQS5jcmwwMAYDVR0lBCkwJwYLKwYBBAGON49lAQEGCysGAQQBjjePZQECBgsrBgEEAY43j2UBAzAJBgcqhkjOPQQBA0gAMEUCIAEEtN5Jh3HP0OTeYkyq0o4eaL0yGZnAbDhUUa0+StGTAiEA5trl/PUTtSwMNUSpc1UAA/viDLCW3FXyfx9cJaheFsQ=", Base64.DEFAULT);
        InputStream inputStream = new ByteArrayInputStream(decodedCertBytes);
        GreenCertificate greenCertificate = null;
        if(greenCertificateData != null) greenCertificate = greenCertificateData.getGreenCertificate();

        boolean expired = true;
        try {
            X509Certificate x509Certificate = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(inputStream);
            certificateList.add(x509Certificate);
            CertificateType certificateType= CertificateType.UNKNOWN;
            if(greenCertificate != null)
                certificateType = greenCertificate.getType();
            new VerificationCryptoService(
                    new X509()).validate(decompressed, x509Certificate,verificationResult, certificateType);
            if(verificationResult.getCoseVerified()){
                expired = x509Certificate.getNotAfter().before(new Date());
            }
        } catch (CertificateException e) {
            e.printStackTrace();
        }
        Log.d("QR","expired: "+expired+", verification: "+verificationResult);
        if(
                !expired
                        && verificationResult.getCoseVerified()
                        && verificationResult.getBase45Decoded()
                        && verificationResult.getCborDecoded()
                        && verificationResult.getZlibDecoded()
                        && !verificationResult.getRulesValidationFailed()){
            //success
            Log.d("QR","success!!!!");
        }
        if(greenCertificate!= null){
            Person person = greenCertificate.getPerson();
            PersonModel personModel = new PersonModel(
                    person.getStandardisedFamilyName(),
                    person.getFamilyName(),
                    person.getStandardisedGivenName(),
                    person.getGivenName() );
            List<VaccinationModel> vaccinationModels = new ArrayList<>();
            for (Vaccination v: Objects.requireNonNull(greenCertificate.getVaccinations())
                 ) {
                DiseaseType diseaseType = DiseaseType.UNDEFINED;
                if(v.getDisease() != null && v.getDisease().toUpperCase(Locale.ROOT).equals("COVID-19")){
                    diseaseType = DiseaseType.COVID_19;
                }
                vaccinationModels.add(new VaccinationModel(
                        diseaseType, v.getVaccine(), v.getMedicinalProduct(),
                        v.getManufacturer(), v.getDoseNumber(), v.getTotalSeriesOfDoses(),
                        v.getDateOfVaccination(), v.getCountryOfVaccination(),
                        v.getCertificateIssuer(), v.getCertificateIdentifier()
                ));
            }
            CertificateModel certificateModel = new CertificateModel(personModel,
                    greenCertificate.getDateOfBirth(),vaccinationModels, null, null);
            Intent intent = new Intent(this, QRCodeResultActivity.class );
            intent.putExtra("certificateModel", certificateModel);
            startActivity(intent);

        }

    }

}
