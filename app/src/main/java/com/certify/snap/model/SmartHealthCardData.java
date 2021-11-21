package com.certify.snap.model;

import java.io.Serializable;

public class SmartHealthCardData implements Serializable {
    private String name;
    private String dob;
    private String doseType;
    private String doseType1;
    private String statusdose1;
    private String vacinationCode1;
    private String vaccinationLocation1;
    private String dose1Date;
    private String dose1Lotnumber;
    private String statusdose2;
    private String vacinationCode2;
    private String vaccinationLocation2;
    private String dose2Date;
    private String dose2Lotnumber;


    public String getDoseType1() {
        return doseType1;
    }

    public void setDoseType1(String doseType1) {
        this.doseType1 = doseType1;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getDoseType() {
        return doseType;
    }

    public void setDoseType(String doseType) {
        this.doseType = doseType;
    }

    public String getStatusdose1() {
        return statusdose1;
    }

    public void setStatusdose1(String statusdose1) {
        this.statusdose1 = statusdose1;
    }

    public String getVacinationCode1() {
        return vacinationCode1;
    }

    public void setVacinationCode1(String vacinationCode1) {
        this.vacinationCode1 = vacinationCode1;
    }

    public String getVaccinationLocation1() {
        return vaccinationLocation1;
    }

    public void setVaccinationLocation1(String vaccinationLocation1) {
        this.vaccinationLocation1 = vaccinationLocation1;
    }

    public String getDose1Date() {
        return dose1Date;
    }

    public void setDose1Date(String dose1Date) {
        this.dose1Date = dose1Date;
    }

    public String getDose1Lotnumber() {
        return dose1Lotnumber;
    }

    public void setDose1Lotnumber(String dose1Lotnumber) {
        this.dose1Lotnumber = dose1Lotnumber;
    }

    public String getStatusdose2() {
        return statusdose2;
    }

    public void setStatusdose2(String statusdose2) {
        this.statusdose2 = statusdose2;
    }

    public String getVacinationCode2() {
        return vacinationCode2;
    }

    public void setVacinationCode2(String vacinationCode2) {
        this.vacinationCode2 = vacinationCode2;
    }

    public String getVaccinationLocation2() {
        return vaccinationLocation2;
    }

    public void setVaccinationLocation2(String vaccinationLocation2) {
        this.vaccinationLocation2 = vaccinationLocation2;
    }

    public String getDose2Date() {
        return dose2Date;
    }

    public void setDose2Date(String dose2Date) {
        this.dose2Date = dose2Date;
    }

    public String getDose2Lotnumber() {
        return dose2Lotnumber;
    }

    public void setDose2Lotnumber(String dose2Lotnumber) {
        this.dose2Lotnumber = dose2Lotnumber;
    }

}
