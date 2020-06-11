package com.certify.fcm;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.certify.snap.common.Logger;
import java.util.Random;

public class FirebaseBackgroundService extends BroadcastReceiver {
    private static final String TAG = "FirebaseBackground";

    @Override
    public void onReceive(Context context, Intent intent) {
        try {

        } catch (Exception e) {
            Logger.error(TAG + "onReceive(Context context, Intent intent)", e.getMessage());
        }
    }


    private static int generateRandomNumber(int max, int min) {
        Random random = new Random();
        return random.nextInt(max - min + 1) + min;
    }


}