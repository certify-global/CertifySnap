package com.certify.snap.common;

import java.security.PublicKey;
import java.util.UUID;

//虹软 key
public class Constants {

//    public static final String APP_ID = "HYp6Tq6kBx3KCRDWnZ4N81epYf5UUEzptsgcnaj7EgG1";
//    public static final String SDK_KEY = "36WWRyCgeqZmjHJLHQUrmh1ve4VGQS13ct1z8tVcXenL";


    public static final String APP_ID = "GUhYjMeFLiFARWet58gTGotUpr189H6Ch7QFSSMgMUCZ";
    public static final String SDK_KEY = "4LxEGC47QhYUPdXZxemhKQSEiFh6p9rYUSitfEcAAFFF";
    public static final int port = 8080;

    /**
     * IR预览数据相对于RGB预览数据的横向偏移量，注意：是预览数据，一般的摄像头的预览数据都是 width > height
     */
    public static final int HORIZONTAL_OFFSET = 0;
    /**
     * IR预览数据相对于RGB预览数据的纵向偏移量，注意：是预览数据，一般的摄像头的预览数据都是 width > height
     */
    public static final int VERTICAL_OFFSET = 0;

    public static final int DEFAULT_RELAY_TIME = 5;

    public static final int DEFAULT_WEIGAN_CONTROLLER_FORMAT = 26;

    public static final int DEFAULT_SCAN_MODE = 1;

    public static final int FACE_MATCH_MAX_RETRY = 3;

    public static final int FACE_MIN_THRESHOLD_RETRY = 50;

    public static final int FACIAL_DETECT_THRESHOLD = 80;

    public static final int MEASURED_STATE_MASK = 0xff000000;

    public static final int PRO_MODEL_TEMPERATURE_MODULE_1 = 25;

    public static final int PRO_MODEL_TEMPERATURE_MODULE_2 = 27;

    public static final int PRO_SCANNER_INIT_TIME = 1260000;  //21 minutes

    public static final int PRO_SCANNER_INIT_INTERVAL = 60000;

    public static final int TEMPERATURE_MAX_RETRY = 5;

    public static final int DEFAULT_SCAN_TYPE = 1;

    public static final int DEFAULT_ACCESS_CONTROL_LOG_MODE = 0;

    public static final int DEFAULT_ACCESS_CONTROL_SCAN_MODE = 1;

    public static final int LANGUAGES_MAX_COUNT = 3;

    public static final int HEALTH_CHECK_INIT_TIME = 1440 * 1000; //24 minutes

    public static final int HEALTH_CHECK_INTERVAL =  1200 * 1000; //20 minutes

    public static final String UUID_VALUE = "2531dcd3-861a-41a1-8525-6495e6059ee1";
    public static final UUID ACCESS_CARD_UUID = UUID.fromString(UUID_VALUE);
    public static final UUID ACCESS_CARD_CHARACTERISTIC_UUID = UUID.fromString("1e67bdac-a5c8-4d78-8e6a-7e6424b1ac7a");
    public static final UUID ACCESS_CARD_CHARACTERISTIC_UUID_RESPONSE = UUID.fromString("065ba997-bbc1-4dfb-a114-11ea87f76504");
    //public static final UUID ACCESS_CARD_CHARACTERISTIC_UUID_RESPONSE = UUID.fromString("42f0e001-4960-4d2b-a6c7-0397951dc7d2");


}

