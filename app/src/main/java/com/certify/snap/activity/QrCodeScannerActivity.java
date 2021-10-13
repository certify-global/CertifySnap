package com.certify.snap.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.certify.snap.R;
import com.certify.snap.model.SmartHealthCardData;
import com.certify.snap.qrverification.CertificateModel;
import com.certify.snap.qrverification.DiseaseType;
import com.certify.snap.qrverification.JwtHelper;
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
import java.io.IOException;
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
                String qrText = result.getText();
//                String qrText = "shc:/5676290952432060346029243740446031222959532654603460292540772804336028702864716745222809286241223803127633772937404037614124294155414509364367694562294037054106322567213239452755415503563944035363327154065460573601064529295312707424284350386122127671683834312572613671033437362564734538002421394407702525072631242357365700113210522031626775766064074006067511100827066624302023664444751221414137593763702428277454403458096325310735255507293205663225526239566061205365573633547377111172317604421231337269453626395934075607642524092710114457093556063354312759216830550476675962043811376353353522684253413300704425342012336061614069666905651024054162381071210056506620063209722261697060245650100429717157282666502129385075337172270303457560583538524005036675320974211166280826390471001129702723572556390726277522355831755225081063204435243024404567071133523875665511756308335231652739113076610640296408622253594225577654607008694170636540775230714445093137120303771073080026502528446844366610312575093731055838406068315212556558653812707371703521752237273309350636775810076865551243062674414125502909440720326212224204502373296231262411414523701012266145051211336270592964437756076922067659743753360854547676685272561166575728436638327761347641052740287155087077081061755212032654574321594125296830665930046008003905572866043563696505767457454033716872435373225233055309575832014230085341293629725630375074601259594561336359591158565941384004692662365022523508312341041163767058072571415973314070597520580300353366275266726831351155644123411027082220\";";
                if(qrText.startsWith("shc:")){
                    smartHealthCard(qrText);
                }else if(qrText.startsWith("HC1:")){
                    parseQrText(qrText);
                }else{
                    Intent intent = new Intent(QrCodeScannerActivity.this, QRCodeResultActivity.class);
                    intent.putExtra("verification", false);
                    startActivity(intent);
                }
            }
        };
        Collection<BarcodeFormat> decodeFormats = new ArrayList<>();
        decodeFormats.add(BarcodeFormat.AZTEC);
        decodeFormats.add(BarcodeFormat.QR_CODE);
        barcodeScanner.setDecoderFactory(new DefaultDecoderFactory(decodeFormats));
        barcodeScanner.decodeContinuous(barcodeCallback);
    }


    private void smartHealthCard(String qrText) {
        SmartHealthCardData smartHealthCardData=new SmartHealthCardData();
        new Thread(() -> {
            try {
                JwtHelper j = JwtHelper.decode(qrText);
                JwtHelper.VerifiableCredential vc = j.payload.vc;

                smartHealthCardData.setName(vc.patient.name);
                smartHealthCardData.setDob(vc.patient.birthDate);
                if(vc.immunization.size() > 0){
                    smartHealthCardData.setDoseType("1");
                    smartHealthCardData.setDose1Date(vc.immunization.get(0).occurrenceDateTime);
                    smartHealthCardData.setDose1Lotnumber(vc.immunization.get(0).lotNumber);
                    smartHealthCardData.setVaccinationLocation1(vc.immunization.get(0).performer);
                    smartHealthCardData.setVacinationCode1(vc.immunization.get(0).vaccinationCode);
                }
                if(vc.immunization.size() > 1){
                    smartHealthCardData.setDoseType1("2");
                    smartHealthCardData.setDose2Date(vc.immunization.get(1).occurrenceDateTime);
                    smartHealthCardData.setDose2Lotnumber(vc.immunization.get(1).lotNumber);
                    smartHealthCardData.setVaccinationLocation2(vc.immunization.get(1).performer);
                    smartHealthCardData.setVacinationCode2(vc.immunization.get(1).vaccinationCode);
                }

            } catch (JwtHelper.DecodeException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            QrCodeScannerActivity.this.runOnUiThread(() -> {
                Intent intent = new Intent(this, SmartHealthResultActivity.class);
                intent.putExtra("verification", true);
                intent.putExtra("smartHealthModel", smartHealthCardData);
                startActivity(intent);

            });
        }).start();




    }

    private void parseQrText(String qrText) {

        VerificationResult verificationResult = new VerificationResult();
        String plainInput = new DefaultPrefixValidationService().decode(qrText, verificationResult);
        byte[] base45Decoded = new DefaultBase45Service().decode(plainInput, verificationResult);
        byte[] decompressed = new DefaultCompressorService().decode(base45Decoded, verificationResult);
        CoseData coseData = new DefaultCoseService().decode(decompressed, verificationResult);
            new DefaultSchemaValidator().validate(coseData.getCbor(), verificationResult);
            GreenCertificateData greenCertificateData = new DefaultCborService().decodeData(coseData.getCbor(), verificationResult);
            //TODO: validateCertData

            String kidBase64 = Base64.encodeToString(coseData.getKid(), Base64.DEFAULT);
            List<Certificate> certificateList = new ArrayList<>();
            byte[] decodedCertBytes = Base64.decode("MIICqjCCAlGgAwIBAgIJAIFaAnBKVQR5MAkGByqGSM49BAEweTELMAkGA1UEBhMCSUUxDzANBgNVBAgMBkR1YmxpbjEPMA0GA1UEBwwGRHVibGluMR0wGwYDVQQKDBREZXBhcnRtZW50IG9mIEhlYWx0aDEQMA4GA1UECwwHZUhlYWx0aDEXMBUGA1UEAwwOQ1NDQV9ER0NfSUVfMDEwHhcNMjEwNzA3MDkzMTIxWhcNMjMwNjI3MDkzMTIxWjBrMQswCQYDVQQGEwJJRTEdMBsGA1UEChMURGVwYXJ0bWVudCBvZiBIZWFsdGgxPTA7BgNVBAMTNHZhbGlkYXRlcXIuZ292LmllIDIwMjAxMzU0NTc5MTIzOTE4MTA1Mzk2MDA5OTk2OTQ1NTUwWTATBgcqhkjOPQIBBggqhkjOPQMBBwNCAARM8Tki2iWsbMtAs+pjE5P6itWZgm6wsOw9YvCsr5tskoEG1cGysPY/L+ixZiv0sXl985GnPGYFRCbOnsri4+Ijo4HQMIHNMA4GA1UdDwEB/wQEAwIHgDAdBgNVHQ4EFgQUT2vp7aj2JBxHrQ1vkpX4bZJoi10wHwYDVR0jBBgwFoAUXIe2JeQrVG0xki6dmWYLVblvTFAwSQYDVR0fBEIwQDA+oDygOoY4aHR0cHM6Ly9nZW4uZGlnaXRhbGNvdmlkY2VydGlmaWNhdGVzLmdvdi5pZS9hcGkvQ1NDQS5jcmwwMAYDVR0lBCkwJwYLKwYBBAGON49lAQEGCysGAQQBjjePZQECBgsrBgEEAY43j2UBAzAJBgcqhkjOPQQBA0gAMEUCIAEEtN5Jh3HP0OTeYkyq0o4eaL0yGZnAbDhUUa0+StGTAiEA5trl/PUTtSwMNUSpc1UAA/viDLCW3FXyfx9cJaheFsQ=", Base64.DEFAULT);
            InputStream inputStream = new ByteArrayInputStream(decodedCertBytes);
            GreenCertificate greenCertificate = null;
            if (greenCertificateData != null)
                greenCertificate = greenCertificateData.getGreenCertificate();

            boolean expired = true;
            try {
                X509Certificate x509Certificate = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(inputStream);
                certificateList.add(x509Certificate);
                CertificateType certificateType = CertificateType.UNKNOWN;
                if (greenCertificate != null)
                    certificateType = greenCertificate.getType();
                new VerificationCryptoService(
                        new X509()).validate(decompressed, x509Certificate, verificationResult, certificateType);
                if (verificationResult.getCoseVerified()) {
                    expired = x509Certificate.getNotAfter().before(new Date());
                }
            } catch (CertificateException e) {
                e.printStackTrace();
            }
            Log.d("QR", "expired: " + expired + ", verification: " + verificationResult);
            if (
                    !expired
                            && verificationResult.getCoseVerified()
                            && verificationResult.getBase45Decoded()
                            && verificationResult.getCborDecoded()
                            && verificationResult.getZlibDecoded()
                            && !verificationResult.getRulesValidationFailed()) {
                //success
                Log.d("QR", "success!!!!");
            }
            if (greenCertificate != null) {
                Person person = greenCertificate.getPerson();
                PersonModel personModel = new PersonModel(
                        person.getStandardisedFamilyName(),
                        person.getFamilyName(),
                        person.getStandardisedGivenName(),
                        person.getGivenName());
                List<VaccinationModel> vaccinationModels = new ArrayList<>();
                for (Vaccination v : Objects.requireNonNull(greenCertificate.getVaccinations())
                ) {
                    DiseaseType diseaseType = DiseaseType.UNDEFINED;
                    if (v.getDisease() != null && v.getDisease().toUpperCase(Locale.ROOT).equals("COVID-19")) {
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
                        greenCertificate.getDateOfBirth(), vaccinationModels, null, null);
                Intent intent = new Intent(this, QRCodeResultActivity.class);
                intent.putExtra("verification", true);
                intent.putExtra("certificateModel", certificateModel);
                startActivity(intent);

            }


    }

}
