package com.certify.snap.controller;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import com.certify.snap.common.AppSettings;
import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.Util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class SoundController {
    private static final String TAG = SoundController.class.getSimpleName();
    private static SoundController instance = null;
    private Context context;
    private boolean isNormalSoundEnable = false;
    private boolean isHighSoundEnable = false;
    private SoundPool soundPool;

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
        if (isNormalSoundEnable) {
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
        if (AppSettings.isQrSoundInvalid()) {
            Util.qrSoundPool(context, soundPool, false);
        }
    }

    /**
     * Method that saves the audio file in the External storage
     * @param audioSoundFileData Audio file data
     * @param fileName FileName
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
        if (dirFile.exists()){
            File file = new File(dirPath + fileName);
            if (file.exists()) {
                boolean result = file.delete();
                Log.i(TAG, String.valueOf(result));
            }
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
    }
}
