package com.certify.snap.common;

//全局变量
public class GlobalParameters {
    public static String BASEURL = "https://faceapi.telpocloud.com/";
    public static String SecretURL = "device/info";
    public static String AccessTokenURL = "Oauth2/access_token";
    public static String MemberListURL = "member/list";
    public static String MemberInfoURL = "member/info";
    public static String TotalMemberURL = "member/totalMember";
    public static String RegisterMemberURL = "member/regist";
    public static String UpdateMemberURL = "member/update";
    public static String DeleteMemberURL = "member/del";
    public static String UpdateOnlineURL = "device/updateOnline";
    public static String UpdateUserListURL = "member/syncUserUpdate";
    public static String DeleteUserListURL = "member/syncUserDelete";
    public static String OfflineVerify="detect/offlineVerify";
    public static String GuestlistURL = "member/guest";

    public static String SN = Util.getSNCode();
    public static String Client_id = "";
    public static String Client_secret = "";
    public static String Access_limit = "";
    public static String Device_password = "";
    public static String Wallpaper = "";

    //sp参数的key名
    public static final String CameraType = "cameratype";
    public static final String LivingType = "livingtype";
    public static final String Orientation = "orientation";
    public static final String LedType = "ledType";
    public static final String StatusBar = "statusbar";
    public static final String NavigationBar = "navigationbar";
    public static final String RelayTime = "relaytime";

    public static String Access_token = "";
    public static String channelID = "";

    public static boolean livenessDetect=true;

    public static final String ACTION_SHOW_NAVIGATIONBAR = "com.android.internal.policy.impl.showNavigationBar";
    public static final String ACTION_HIDE_NAVIGATIONBAR = "com.android.internal.policy.impl.hideNavigationBar";
    public static final String ACTION_OPEN_STATUSBAR = "com.android.systemui.statusbar.phone.statusopen";
    public static final String ACTION_CLOSE_STATUSBAR = "com.android.systemui.statusbar.phone.statusclose";
    public static final String FACE_TEMP = "facetemp";
    public static final String TEMP_ONLY = "temp";
    public static final String TEMP_TEST = "test";
    public static final String IMAGE_ICON = "image";
    public static final String ACCESS_TOKEN = "access_token";
    public static final String TOKEN_TYPE = "token_type";
    public static final String INSTITUTION_ID = "institutionID";
    public static final String DEVICE_PASSWORD = "device_password";
    public static final String MOBILE_NUMBER = "mobile_number";
    public static final String MobileAppVersion = "MobileAppVersion";
    public static final String UUID = "uuid";
    public static final String IMEI = "imei";
    public static final String ONLINE_MODE = "online_mode";
    public static final String URL = "url";
    public static final String Thermalscan_title = "Thermalscan_title";
    public static final String Thermalscan_subtitle = "Thermalscan_subtitle";
    public static final String CAPTURE_IMAGES_ABOVE = "c_m_above";
    public static final String CAPTURE_IMAGES_ALL = "c_m_all";
    public static final String CAPTURE_TEMPERATURE = "c_m_temp";
    public static final String CAPTURE_SOUND = "c_m_sound";
    public static final String DELAY_VALUE="delay_value";
    public static final String DELAY_VALUE_CONFIRM="delay_value_confirm";
    public static final String CONFIRM_SCREEN="confirm_screen";
    public static final String Confirm_title_below = "Confirm_title_below";
    public static final String Confirm_subtitle_below = "Confirm_subtitle_below";
    public static final String Confirm_title_above = "Confirm_title_above";
    public static final String Confirm_subtitle_above = "Confirm_subtitle_above";


}
