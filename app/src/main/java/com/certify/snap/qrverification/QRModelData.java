package com.certify.snap.qrverification;

import java.io.Serializable;

public class QRModelData implements Serializable {
    private String firstName;
    private String certificateType;
    private String familyName;
    private String givenName;
    private String dob;
    private String targetDisease;
    private String dateOfVaccinaton;
    private String issuerCountry;
    private boolean verification;

    public boolean isVerification() {
        return verification;
    }

    public void setVerification(boolean verification) {
        this.verification = verification;
    }


    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getCertificateType() {
        return certificateType;
    }

    public void setCertificateType(String certificateType) {
        this.certificateType = certificateType;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getTargetDisease() {
        return targetDisease;
    }

    public void setTargetDisease(String targetDisease) {
        this.targetDisease = targetDisease;
    }

    public String getDateOfVaccinaton() {
        return dateOfVaccinaton;
    }

    public void setDateOfVaccinaton(String dateOfVaccinaton) {
        this.dateOfVaccinaton = dateOfVaccinaton;
    }

    public String getIssuerCountry() {
        return issuerCountry;
    }

    public void setIssuerCountry(String issuerCountry) {
        this.issuerCountry = issuerCountry;
    }




}
