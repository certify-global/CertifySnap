package com.certify.snap.common;


public class EndPoints {

    public enum Mode {Prod, Demo}
    public static final Mode deployment = Mode.Demo;
    public static String prod_url ="https://apidev.certify.me";//https://api.certify.me";//
    public static String dev_url = "https://onlinedev.certifyglobal.net/";


    //https://onlinedev.certifyglobal.net/CertifyXTAPI/GenerateToken
//    https://apidev.certify.me
//    https://apiqa.certify.me
//    https://apidemo.certify.me
//    https://api.certify.me

    private static String GetServerURL() {
        return (deployment == Mode.Demo ? dev_url : prod_url);
    }

    public static String domainUrl =  "/";
    public static final String  GenerateToken = domainUrl+"GenerateToken";
    public static final String  RecordTemperature = domainUrl+"RecordMemberTemperature";
    public static final String ActivateApplication = domainUrl + "ActivateApplication";




}