package com.certify.snap.common;


import com.certify.snap.BuildConfig;

public class EndPoints {

    public enum Mode {Prod, Demo}
    public static final Mode deployment = Mode.Demo;
    public static String prod_url = BuildConfig.ENDPOINT_URL;
    public static String dev_url = "https://api.certify.me";


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
    public static final String DEVICEHEALTHCHECK = domainUrl + "DeviceHealthCheck";
    public static final String DEVICESETTING = domainUrl + "GetDeviceConfiguration";
    public static final String ValidateQRCode = domainUrl + "ValidateQRCode";
    public static final String ManageMember = domainUrl + "ManageMember";
    public static final String GetMemberList = domainUrl + "GetMemberList";
    public static final String GetMemberById = domainUrl + "GetMemberById";
    public static final String Token = domainUrl + "token";
    public static final String RegisterDevice = domainUrl + "RegisterDevice";
    public static final String PushCommandResponse = domainUrl + "PushCommandResponse";
    public static final String AccessLogs = domainUrl + "AccessLogs";
    public static final String GetQuestions = domainUrl + "GetWaveQuestionList"; //GetQuestionList
    public static final String GetWorkflowList = domainUrl + "GetWorkflowList";
    public static final String SaveAnswer = domainUrl + "SaveAnswer";
    public static final String DeviceLogs = domainUrl + "SaveDeviceLogs";
    public static final String GetLanguages = domainUrl + "GetLanguages";
    public static final String GetLastCheckinTime = domainUrl + "GetLastCheckinTime";

}