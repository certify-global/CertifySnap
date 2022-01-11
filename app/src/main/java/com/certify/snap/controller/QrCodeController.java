package com.certify.snap.controller;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import com.certify.callback.GetLastCheckinTimeCallback;
import com.certify.snap.activity.QRCodeResultActivity;
import com.certify.snap.activity.SmartHealthResultActivity;
import com.certify.snap.async.AsyncGetLastCheckinTime;
import com.certify.snap.common.AppSettings;
import com.certify.snap.common.EndPoints;
import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.Logger;
import com.certify.snap.common.UserExportedData;
import com.certify.snap.common.Util;
import com.certify.snap.model.SmartHealthCardData;
import com.certify.snap.qrverification.CertificateModel;
import com.certify.snap.qrverification.DiseaseType;
import com.certify.snap.qrverification.JwtHelper;
import com.certify.snap.qrverification.PersonModel;
import com.certify.snap.qrverification.VaccinationModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
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

public class QrCodeController implements GetLastCheckinTimeCallback {
    private final String TAG = QrCodeController.class.getSimpleName();
    private static QrCodeController mInstance = null;
    private final String QR_FORMAT_ON_DATE = "/OD";
    private final String QR_FORMAT_BEFORE_DATE = "/OC";
    private final String QR_FORMAT_GENERIC = "/A:";
    private final String QR_DATE_FORMAT = "yyyyMMdd";
    private boolean isQrCodeMemberMatch = false;
    private UserExportedData data = null;
    private QrCodeListener listener = null;
    private boolean memberCheckedIn = false;
    private Context mContext = null;

    public interface QrCodeListener {
        void onGetLastCheckInTime(boolean checkedIn);
    }

    public static QrCodeController getInstance() {
        if (mInstance == null) {
            mInstance = new QrCodeController();
        }
        return mInstance;
    }

    public void init(Context context) {
        this.mContext = context;
    }

    public void setListener(QrCodeListener callbackListener) {
        listener = callbackListener;
    }

    /**
     * Method that checks for the Qr code format
     *
     * @param qrCode Qr code input
     * @return true or false accordingly
     */
    public boolean isQrCodeDated(String qrCode) {
        boolean result = false;
        if (qrCode.contains(QR_FORMAT_ON_DATE) ||
                qrCode.contains(QR_FORMAT_BEFORE_DATE) ||
                qrCode.contains(QR_FORMAT_GENERIC)) {
            result = true;
        }
        return result;
    }

    /**
     * Method that validates if the Qr code data is in range (today or expired)
     *
     * @param qrCode qrCode Qr code input
     * @return true or false accordingly
     */
    public boolean validateDatedQrCode(String qrCode) {
        boolean result = false;
        if (qrCode.contains(QR_FORMAT_ON_DATE)) {
            String strSplit[] = qrCode.split("/");
            int size = strSplit.length;
            String strFormatDate = strSplit[size - 1];
            String strDate = strFormatDate.substring(2);
            if (!strDate.isEmpty() && strDate.equals(Util.currentDate(QR_DATE_FORMAT))) {
                result = true;
            }
        } else if (qrCode.contains(QR_FORMAT_BEFORE_DATE)) {
            String strSplit[] = qrCode.split("/");
            int size = strSplit.length;
            String strFormatDate = strSplit[size - 1];
            String strDate = strFormatDate.substring(2);
            String currentDate = Util.currentDate(QR_DATE_FORMAT);
            if (!strDate.isEmpty() && currentDate != null) {
                if (Util.isDateBigger(strDate, currentDate, QR_DATE_FORMAT)) {
                    result = true;
                }
            }
        } else if (qrCode.contains(QR_FORMAT_GENERIC)) {
            result = true;
        }
        return result;
    }

    public void setQrCodeMemberMatch(boolean value) {
        isQrCodeMemberMatch = value;
    }

    /*public boolean isFaceSearchedOnQrCode() {
        return (AppSettings.isQrCodeEnabled() && AppSettings.isScanOnQrEnabled() && isQrCodeMemberMatch);
    }*/

    public UserExportedData getData() {
        return data;
    }

    public void setData(UserExportedData data) {
        this.data = data;
    }

    public void getLastCheckInTime(Context context, String certifyId) {
        SharedPreferences sharedPreferences = Util.getSharedPreferences(context);
        try {
            JSONObject obj = new JSONObject();
            obj.put("institutionId", sharedPreferences.getString(GlobalParameters.INSTITUTION_ID, ""));
            obj.put("certifyId", certifyId);
            new AsyncGetLastCheckinTime(obj, this, sharedPreferences.getString(GlobalParameters.URL, EndPoints.prod_url) + EndPoints.GetLastCheckinTime, context).execute();
        } catch (Exception e) {
            Log.d(TAG, "getLanguagesApi" + e.getMessage());
        }
    }

    @Override
    public void onJSONObjectListenerGetCheckInTime(JSONObject report, String status, JSONObject req) {
        if (report == null) {
            Logger.error(TAG, "onJSONObjectListenerGetCheckInTime", "Get Last Check-in time failed");
            if (listener != null) {
                listener.onGetLastCheckInTime(false);
            }
            return;
        }
        try {
            if (report.getString("responseCode").equals("1")) {
                if (listener != null) {
                    listener.onGetLastCheckInTime(true);
                }
            } else {
                if (listener != null) {
                    listener.onGetLastCheckInTime(false);
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error in get last check in response " + e.getMessage());
        }
    }

    public boolean isMemberCheckedIn() {
        return memberCheckedIn;
    }

    public void setMemberCheckedIn(boolean memberCheckedIn) {
        this.memberCheckedIn = memberCheckedIn;
    }

    public boolean isOnlyQrCodeEnabled() {
        boolean result = false;
        String triggerType = CameraController.getInstance().getTriggerType();
        if (((AppSettings.getPrimaryIdentifier()
                == CameraController.PrimaryIdentification.QR_CODE.getValue()) ||
                triggerType.equals(CameraController.triggerValue.CODEID.toString())) &&
                (AppSettings.getSecondaryIdentifier() == CameraController.SecondaryIdentification.NONE.getValue())) {
            result = true;
        }
        return result;
    }

    public void clearData() {
        this.data = null;
        isQrCodeMemberMatch = false;
        memberCheckedIn = false;
        resetQrCodeData(mContext);
    }

    public void smartHealthCard(String qrText, Context context) {
        SmartHealthCardData smartHealthCardData = new SmartHealthCardData();
        new Thread(() -> {
            try {
                JwtHelper j = JwtHelper.decode(qrText);
                JwtHelper.VerifiableCredential vc = j.payload.vc;

                smartHealthCardData.setName(vc.patient.name);
                smartHealthCardData.setDob(vc.patient.birthDate);
                if (vc.immunization.size() > 0) {
                    smartHealthCardData.setDoseType("1");
                    smartHealthCardData.setDose1Date(vc.immunization.get(0).occurrenceDateTime);
                    smartHealthCardData.setDose1Lotnumber(vc.immunization.get(0).lotNumber);
                    smartHealthCardData.setVaccinationLocation1(vc.immunization.get(0).performer);
                    smartHealthCardData.setVacinationCode1(vc.immunization.get(0).vaccinationCode);
                }
                if (vc.immunization.size() > 1) {
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
            Util.writeString(Util.getSharedPreferences(context), GlobalParameters.anonymousFirstName, smartHealthCardData.getName());
            Util.writeString(Util.getSharedPreferences(context), GlobalParameters.anonymousLastName, "");
            Util.writeString(Util.getSharedPreferences(context), GlobalParameters.anonymousVaccDate, smartHealthCardData.getDose1Date());
            Util.writeString(Util.getSharedPreferences(context), GlobalParameters.anonymousVaccDate2, smartHealthCardData.getDose2Date());
            Util.writeString(Util.getSharedPreferences(context), GlobalParameters.vaccineDocumentName, smartHealthCardData.getDoseType());

//            Intent intent = new Intent(context, SmartHealthResultActivity.class);
//            intent.putExtra("verification", true);
//            intent.putExtra("smartHealthModel", smartHealthCardData);
//            context.startActivity(intent);

        }).start();
    }

    public void parseQrText(String qrText, Context context) {

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
            Util.writeString(Util.getSharedPreferences(context), GlobalParameters.anonymousFirstName, certificateModel.getPerson().getGivenName());
            Util.writeString(Util.getSharedPreferences(context), GlobalParameters.anonymousLastName, certificateModel.getPerson().getFamilyName());
            Util.writeString(Util.getSharedPreferences(context), GlobalParameters.anonymousVaccDate, certificateModel.getVaccinations().get(0).getDateOfVaccination());
            Util.writeString(Util.getSharedPreferences(context), GlobalParameters.anonymousVaccDate2, "");
            Util.writeString(Util.getSharedPreferences(context), GlobalParameters.vaccineDocumentName, certificateModel.getVaccinations().get(0).getManufacturer());

//            Intent intent = new Intent(context, QRCodeResultActivity.class);
//            intent.putExtra("verification", true);
//            intent.putExtra("certificateModel", certificateModel);
//            context.startActivity(intent);

        }
    }

    public void resetQrCodeData(Context context) {
        SharedPreferences sharedPreferences = Util.getSharedPreferences(context);
        Util.writeString(sharedPreferences, GlobalParameters.anonymousFirstName, "");
        Util.writeString(sharedPreferences, GlobalParameters.anonymousLastName, "");
        Util.writeString(sharedPreferences, GlobalParameters.anonymousVaccDate, "");
        Util.writeString(sharedPreferences, GlobalParameters.anonymousVaccDate2, "");
        Util.writeString(sharedPreferences, GlobalParameters.vaccineDocumentName, "");
    }

}
