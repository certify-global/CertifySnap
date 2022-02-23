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

    public static String SN = "";//Util.getSNCode();
    public static String Client_id = "";
    public static String Client_secret = "";
    public static String Access_limit = "";
    public static String Device_password = "";
    public static String Wallpaper = "";

    public static final String LICENSE_ACTIVATED = "activate";
    //sp参数的key名
    public static final String CameraType = "cameratype";
    public static final String LivingType = "livingtype";
    public static final String Orientation = "orientation";
    public static final String LedType = "ledType";
    public static final String StatusBar = "statusbar";
    public static final String NavigationBar = "navigationbar";
    public static final String RelayTime = "relaytime";
    public static final String LogFilePath = "logfilepath";
    public static final String EnableRelay = "enableRelay";
    public static final String AllowAnonymous = "allowAnonymous";
    public static final String RelayNormalMode = "relayNormalMode";
    public static final String RelayReverseMode = "relayReverseMode";
    public static final String StopRelayOnHighTemp = "stopRelayOnHighTemp";
    public static final String EnableWeigand = "enableWeigand";
    public static final String EnableWeigandPassThrough = "enableWeigandPassThrough";
    public static final String WeiganFormatMessage = "weiganFormatMessage";
    public static final String LedBrightnessLevel = "ledBrightnessLevel";
    public static final String ScanMode = "ScanMode";
    public static final String ScanProximity = "ScanProximity";
    public static final String PRAGMA_KEY = "pragmakey";
    public static final String ScanType = "ScanType";
    public static final String EnableTempScan = "EnableTempScan";
    public static final String LogOfflineData = "logOfflineData";
    public static final String TimeAttendanceOption = "TimeAttendanceOption";
    public static final String RETRY_SCAN = "RetryScan";
    public static final String anonymousFirstName = "anonymous_first_name";
    public static final String anonymousLastName = "anonymous_Last_name";
    public static final String anonymousVaccDate = "anonymous_vacc_date";
    public static final String anonymousVaccDate2 = "anonymous_vacc_date2";
    public static final String vaccineDocumentName = "vaccineDocumentName";
    public static final String anonymousId = "anonymous_id";

    public static String Access_token = "";
    public static String channelID = "";

    public static boolean livenessDetect=false;

    public static final String ACTION_SHOW_NAVIGATIONBAR = "com.android.internal.policy.impl.showNavigationBar";
    public static final String ACTION_HIDE_NAVIGATIONBAR = "com.android.internal.policy.impl.hideNavigationBar";
    public static final String ACTION_OPEN_STATUSBAR = "com.android.systemui.statusbar.phone.statusopen";
    public static final String ACTION_CLOSE_STATUSBAR = "com.android.systemui.statusbar.phone.statusclose";
    public static final String FACE_TEMP = "facetemp";
    public static final String TEMP_ONLY = "temp";
    public static final String TEMP_TEST = "test";
    public static final String DISPLAY_TEMP_THRESHOLD = "display_temp_threshold";
    public static final String IMAGE_ICON = "image";
    public static final String IMAGE_ICON_API = "image_api";
    public static final String ACCESS_TOKEN = "access_token";
    public static final String EXPIRE_TIME = "expire_time";
    public static final String TOKEN_TYPE = "token_type";
    public static final String INSTITUTION_ID = "institutionID";
    public static final String DEVICE_PASSWORD = "device_password";
    public static final String MOBILE_NUMBER = "mobile_number";
    public static final String UUID = "uuid";
    public static final String IMEI = "imei";
    public static final String ONLINE_MODE = "online_mode";
    public static final String ONLINE_SWITCH = "online_switch";
    public static final String FIRST_RUN = "first_run";
    public static final String URL = "url";
    public static final String DEVICE_NAME = "device_name";
    public static final String DEVICE_SETTINGS_NAME = "device_settings_name";
    public static final String PRO_SETTINGS = "pro_settings";
    public static final String Thermalscan_title = "Thermalscan_title";
    public static final String Thermalscan_subtitle = "Thermalscan_subtitle";
    public static final String CAPTURE_IMAGES_ABOVE = "c_m_above";
    public static final String CAPTURE_IMAGES_ALL = "c_m_all";
    public static final String CAPTURE_TEMPERATURE = "c_m_temp";
    public static final String TEMPERATURE_SOUND_NORMAL = "c_m_sound";
    public static final String TEMPERATURE_SOUND_HIGH = "c_sound_high";
    public static final String QR_SOUND_VALID = "qr_sound_valid";
    public static final String QR_SOUND_INVALID = "qr_sound_invalid";
    public static final String BLE_LIGHT_NORMAL = "BleLightNormal";
    public static final String BLE_LIGHT_HIGH = "BleLightHigh";
    public static final String BROTHER_BLUETOOTH_PRINTER = "BrotherBluetoothPrinter";
    public static final String TOSHIBA_USB_PRINTER = "ToshibaBluetoothPrinter";
    public static final String MASK_DETECT = "mask_detect";
    public static final String FACIAL_DETECT = "facial_detect";
    public static final String ALLOW_ALL = "allow_all";
    public static final String DELAY_VALUE="delay_value";
    public static final String DELAY_VALUE_CONFIRM_BELOW="delay_value_confirm";
    public static final String CONFIRM_SCREEN_BELOW="confirm_screen";
    public static final String CONFIRM_SCREEN_ABOVE="confirm_screen_above";
    public static final String SHOW_VACCINATION_INDICATOR="show_vaccination_indicator";
    public static final String SHOW_NON_VACCINATION_INDICATOR="show_non_vaccination_indicator";
    public static final String DELAY_VALUE_CONFIRM_ABOVE = "delay_value_confirm_above";
    public static final String Confirm_title_below = "Confirm_title_below";
    public static final String Confirm_subtitle_below = "Confirm_subtitle_below";
    public static final String Confirm_title_above = "Confirm_title_above";
    public static final String Confirm_subtitle_above = "Confirm_subtitle_above";
    public static final String F_TO_C = "f_to_c";
    public static final String settingVersion = "settingVersion";
    public static final String deviceMasterCode = "deviceMasterCode";
    public static final String deviceSettingMasterCode = "deviceSettingMasterCode";
    public static final String GUIDE_TEXT1 = "guide_text1";
    public static final String GUIDE_TEXT2 = "guide_text2";
    public static final String GUIDE_TEXT3 = "guide_text3";
    public static final String GUIDE_TEXT4 = "guide_text4";
    public static final String GUIDE_SCREEN = "guide_screen";
    public static final String TEMP_TEST_LOW = "temp_test_low";
    public static final String SNAP_ID = "snap_id";
    public static final String QR_SCREEN = "qr_screen";
    public static final String QRCODE_ID = "qrcode_id";
    public static final String QRCODE_Valid = "qrcode_valid";
    public static final String RFID_ENABLE = "rfid_enable";
    public static final String FIRST_NAME = "first_name";
    public static final String LAST_NAME = "last_name";
    public static final String MEMBER_ID = "member_id";
    public static final String TRQ_STATUS = "trq_status";
    public static final String Timeout = "timeout";
    public static final String ACCESS_ID = "access_id";
    public static final String Cloud_Activated = "cloud_activated";
    public static final String FACIAL_THRESHOLD = "facial_similarity_range";
    public static final String DISPLAY_IMAGE_CONFIRMATION = "display_image_confirmation";
    public static final String MASK_VALUE = "mask_value";
    public static final String FACE_SCORE = "face_score";
    public static final String ANONYMOUS_ENABLE = "anonymous_enable";
    public static final String HOME_TEXT_ONLY ="home_text_only";
    public static final String HOME_TEXT_ONLY_MESSAGE ="home_text_only_message";
    public static final String HOME_DISPLAY_TIME ="home_display_time";
    public static final String HOME_TEXT_ONLY_IS_ENABLE ="home_text_only_is_enable";
    public static final String HOME_TEXT_IS_ENABLE ="home_display_is_enable";
    public static final String Firebase_Command = "firebase_command";
    public static final String Firebase_Value = "firebase_value";
    public static final String COMPENSATION = "compensation";
    public static final String Firebase_Token = "firebase_token";
    public static final String Generate_Token_Command = "generate_token_command";
    public static final String SYNC_ONLINE_MEMBERS ="sync_online_members";
    public static final String Internet_Indicator ="internet_indicator";
    public static final String Admin_InstitutionID ="Admin_InstitutionID";
    public static final String Temp_ACCESS_TOKEN = "Temp_access_token";
    public static final String QR_BUTTON_TEXT = "QR_button_text";
    public static final String QR_SKIP_BUTTON_ENABLE_DISABLE = "QR_skip_button_enable";
    public static final String VISUAL_RECOGNITION = "VisualRecognition";
    public static final String HAND_GESTURE = "HandGesture";
    public static final String WAVE_QUESTIONS = "WaveQuestions";
    public static final String MASK_ENFORCEMENT = "maskEnforcement";
    public static final String PRO_DEVICE_BOOT_TIME = "ProDeviceBootTime";
    public static final String GESTURE_EXIT_NEGATIVE_OP = "gestureExitNegativeOp";
    public static final String GESTURE_EXIT_CONFIRM_TEXT = "gestureExitConfirmText";
    public static final String ENABLE_TOUCH_MODE = "enableTouchMode";
    public static final String TOUCH_HOME_PAGE_MSG = "touchHomePageMsg";
    public static final String MULTI_LANGUAGE_INSTRUCTIONS = "multilanguageInstructions";

    public static final String Temperature = "temperature";
    public static final String Temphint = "temphint";
    public static final String OpenDoor = "opendoor";
    public static final String Host = "host";
    public static final String Port = "port";
    public static final String Fromadd = "fromadd";
    public static final String Frompwd = "frompwd";
    public static final String type = "type";
    public static final String MaskMode = "maskMode";
    public static final String Centigrades = "centigrade";
    public static final String Fahrenheits = "fahrenheit";
    public static final String LOCAL_SERVER_SETTINGS = "local_server_settings";
    public static final String APP_LAUNCH_TIME = "app_launch_time";
    public static final String Touchless_setting_id = "touchless_setting_id";
    public static final String Touchless_wave_skip = "touchless_wave_skip";

    public static final String TEMPERATURE_THRESHOLD = "temperature_threshold";

    public static final String PRINT_ALL_SCAN = "printAllScan";
    public static final String PRINT_ACCESS_CARD_USERS = "printAccessCard";
    public static final String PRINT_QR_CODE_USERS = "printQRCode";
    public static final String PRINT_WAVE_USERS = "printWaveUsers";
    public static final String PRINT_HIGH_TEMPERATURE = "printHighTemperature";
    public static final String PRINT_QR_CODE_FOR_WAVE_INDICATOR = "printQrCodeForWaveIndicator";

    //print Label Options
    public static final String PRINT_LABEL_FACE = "printLabelFace";
    public static final String PRINT_LABEL_NAME = "printLabelName";
    public static final String PRINT_LABEL_UNIDENTIFIED_NAME = "printLabelUnidentifiedName";
    public static final String PRINT_LABEL_NORMAL_TEMPERATURE = "printLabelNormalTemperature";
    public static final String PRINT_LABEL_HIGH_TEMPERATURE = "printLabelHighTemperature";
    public static final String PRINT_LABEL_WAVE_ANSWERS = "printLabelWaveAnswers";
    public static final String PRINT_LABEL_WAVE_EDIT_NAME = "printLabelWaveEditName";
    public static final String PRINT_LABEL_WAVE_EDIT_QR_ANSWERS = "printLabelQRAnswers";
    public static final String PRINT_LABEL_EDIT_PASS_NAME = "printLabelPassName";
    public static final String PRINT_LABEL_WAVE_YES_ANSWER = "printLabelWaveYesAnswer";
    public static final String PRINT_LABEL_WAVE_NO_ANSWER = "printLabelWaveNoAnswer";

    public static final String ACKNOWLEDGEMENT_TEXT = "acknowledge_text";
    public static final String ACKNOWLEDGEMENT_SCREEN = "acknowledge_radio";
    public static final String RESULT_BAR = "result_bar";
    public static final String RESULT_BAR_NORMAL = "result_bar_normal";
    public static final String RESULT_BAR_HIGH = "result_bar_high";
    public static final String VALIDATE_DB = "validateDB";
    public static final String PROGRESS_BAR = "progress_bar";
    public static final String WAVE_INDICATOR = "wave_indicator";
    public static final String CLEAR_SHARED_PREF = "clearSharedPref";
    public static final String WAVE_IMAGE = "WaveImage";
    public static final String MASK_ENFORCE_INDICATOR = "mask_enforce_indicator";
    public static final String MEMBER_GROUP_SYNC = "member_group_sync";
    public static final String MEMBER_GROUP_ID = "member_group_id";
    public static final String AccessControlLogMode = "accessControlLogging";
    public static final String AccessControlScanMode = "accessControlScan";
    public static final String LANGUAGE_TYPE = "LanguageType";
    public static final String LANGUAGE_TYPE_SECONDARY = "LanguageTypeSecondary";
    public static final String LANGUAGE_ALLOW_MULTILINGUAL = "LanguageAllowMultilingual";
    public static final String ASK_QR_CODE_ALWAYS = "AskQrCode";
    public static final String QR_CODE_MEMBER_TYPE = "QrCodeAccessMemberType";
    public static final String FACE_QR_CODE = "FaceQrCode";
    public static final String PRIMARY_IDENTIFIER = "PrimaryIdentifier";
    public static final String SECONDARY_IDENTIFIER = "SecondaryIdentifier";
    public static final String SETTINGS_RETRIEVED = "SettingsRetrieved";
    public static final String DEBUG_MODE = "DebugMode";
    public static final String HEALTH_CHECK_OFFLINE = "HealthCheckOffline";
    public static final String MEMBER_DELTA_SYNC_ENABLED = "MemberDeltaSyncEnabled";
    public static final String ACCESSID_TRIM_ZEROES = "AccessIdTrimZeroes";
    public static final String MOBILE_ACCESS_CARD = "MobileAccessCard";

}
