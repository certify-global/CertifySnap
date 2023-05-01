package com.certify.snap.controller;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import com.certify.snap.common.AppSettings;
import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.Util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Timer;
import java.util.TimerTask;

public class SoundController {
    private static final String TAG = SoundController.class.getSimpleName();
    private static SoundController instance = null;
    private Context context;
    private boolean isNormalSoundEnable = false;
    private boolean isHighSoundEnable = false;
    private SoundPool soundPool;
    private boolean startTime = false;
    private Timer mTimer;

    public static SoundController getInstance() {
        if (instance == null) {
            instance = new SoundController();
        }
        return instance;
    }

    /**
     * Method that initializes the Sound parameters
     *
     * @param context context
     */
    public void init(Context context) {
        this.context = context;
        isNormalSoundEnable = Util.getSharedPreferences(context).getBoolean(GlobalParameters.TEMPERATURE_SOUND_NORMAL, false);
        isHighSoundEnable = Util.getSharedPreferences(context).getBoolean(GlobalParameters.TEMPERATURE_SOUND_HIGH, false);
        initSound();
    }

    /**
     * Method that initializes the Sound pool
     */
    private void initSound() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes attributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();

            soundPool = new SoundPool.Builder()
                    .setAudioAttributes(attributes)
                    .build();
        } else {
            soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        }
    }

    /**
     * Method that initiates playing of the normal temperature sound
     */
    public void playNormalTemperatureSound() {
        if (isNormalSoundEnable && AppSettings.getTimeAndAttendance() != 1) {
            Util.soundPool(context, "normal", soundPool);
        }
    }

    /**
     * Method that initiates playing of the high temperature sound
     */
    public void playHighTemperatureSound() {
        if (isHighSoundEnable) {
            Util.soundPool(context, "high", soundPool);
        }
    }

    /**
     * Method that initiates playing of the valid QrCode sound
     */
    public void playValidQrSound() {
        if (AppSettings.isQrSoundValid()) {
            Util.qrSoundPool(context, soundPool, true);
        }
    }

    /**
     * Method that initiates playing of the invalid QrCode sound
     */
    public void playInvalidQrSound() {
        if (startTime)
            return;
        startTime = true;
        startTimer();
        if (AppSettings.isQrSoundInvalid()) {
            Util.qrSoundPool(context, soundPool, false);
        }
    }

    /**
     * Method that initiates playing on granting access to door
     */
    public void playAccessGrantedSound() {
        if (soundPool == null) return;
        try {
            File file = new File(Environment.getExternalStorageDirectory() + "/Audio/MatchingSuccess.mp3");
            if (file.exists()) {
                soundPool.load(Environment.getExternalStorageDirectory() + "/Audio/MatchingSuccess.mp3", 1);
                soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
                    int lastStreamId = -1;

                    @Override
                    public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                        //soundPool.release();
                        lastStreamId = soundPool.play(sampleId, 1.0f, 1.0f, 0, 0, 1.0f);
                    }
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in playing the access sound" + e.getMessage());
        }
    }

    /**
     * Method that initiates playing of the valid QrCode sound
     */
    public void playAccessDeniedSound() {
        if (soundPool == null) return;
        try {
            File file = new File(Environment.getExternalStorageDirectory() + "/Audio/MatchingFailed.mp3");
            if (file.exists()) {
                soundPool.load(Environment.getExternalStorageDirectory() + "/Audio/MatchingFailed.mp3", 1);
                soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
                    int lastStreamId = -1;

                    @Override
                    public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                        //soundPool.release();
                        lastStreamId = soundPool.play(sampleId, 1.0f, 1.0f, 0, 0, 1.0f);
                    }
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in playing the access sound" + e.getMessage());
        }
    }

    /**
     * Method that saves the audio file in the External storage
     *
     * @param audioSoundFileData Audio file data
     * @param fileName           FileName
     */
    public void saveAudioFile(String audioSoundFileData, String fileName) {
        final byte[] imgBytesData = android.util.Base64.decode(audioSoundFileData,
                android.util.Base64.DEFAULT);
        final File file;
        try {
            String path = Environment.getExternalStorageDirectory() + "/Audio/";
            File dirFile = new File(path);
            if (!dirFile.exists()) {
                dirFile.mkdir();
            }
            file = new File(path + fileName);
            final FileOutputStream fileOutputStream;

            fileOutputStream = new FileOutputStream(file);
            final BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(
                    fileOutputStream);
            bufferedOutputStream.write(imgBytesData);
            bufferedOutputStream.close();
        } catch (Exception e) {
            Log.e(TAG, "Error in saving the audio file " + e.getMessage());
        }
    }

    public void deleteAudioFile(String fileName) {
        String dirPath = Environment.getExternalStorageDirectory() + "/Audio/";
        File dirFile = new File(dirPath);
        if (dirFile.exists()) {
            File file = new File(dirPath + fileName);
            if (file.exists()) {
                boolean result = file.delete();
                Log.i(TAG, String.valueOf(result));
            }
        }
    }

    public void onCheckInOutSound(String type) {
//        if ((!AppSettings.isTemperatureScanEnabled() &&
//                ((AppSettings.getSecondaryIdentifier() == CameraController.SecondaryIdentification.QR_CODE.getValue()) ||
//                        (AppSettings.getSecondaryIdentifier() == CameraController.SecondaryIdentification.QRCODE_OR_RFID.getValue())))) {
//            new Handler().postDelayed(() -> playCheckInOutSound(type), 2 * 1000);
//        } else {
        playCheckInOutSound(type);
        // }
    }

    public void playCheckInOutSound(String type) {
        if (soundPool == null) return;
        try {
            File file = new File(Environment.getExternalStorageDirectory() + "/Audio/" + type + ".mp3");
            if (file.exists()) {
                soundPool.load(Environment.getExternalStorageDirectory() + "/Audio/" + type + ".mp3", 1);
                soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
                    int lastStreamId = -1;

                    @Override
                    public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                        //soundPool.release();
                        lastStreamId = soundPool.play(sampleId, 1.0f, 1.0f, 0, 0, 1.0f);
                    }
                });
            } else {
                if (isNormalSoundEnable) {
                    Util.soundPool(context, "normal", soundPool);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "soundPoolCheckInOut error in " + e.getMessage());
        }

    }

    /**
     * Method that clears the Sound parameters
     */
    public void clearData() {
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }

    }

    private void startTimer() {
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            public void run() {
                startTime = false;
                this.cancel();
            }
        }, 2 * 1000);
    }
}
