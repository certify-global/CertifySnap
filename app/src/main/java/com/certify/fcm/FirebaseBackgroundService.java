package com.certify.fcm;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.Tag;

import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.Logger;
import com.certify.snap.common.Util;

import java.util.Random;

public class FirebaseBackgroundService extends BroadcastReceiver {
    private static final String TAG = "FirebaseBackground";
    SharedPreferences sharedPreferences;

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            if (intent.getExtras() != null) {
                Util.getSharedPreferences(context);

              String command= intent.getStringExtra(("Command") == null ? "" : intent.getStringExtra("Command"));
              String value= intent.getStringExtra(("Value1") == null ? "" : intent.getStringExtra("Value1"));
              Logger.debug(TAG,"push command");


                Util.writeString(sharedPreferences,GlobalParameters.Firebase_Command,command);
                Util.writeString(sharedPreferences,GlobalParameters.Firebase_Value,value);

               /* if(command.equals("SETTINGS")){

                }*/
            }

        } catch (Exception e) {
            Logger.error(TAG + "onReceive(Context context, Intent intent)", e.getMessage());
        }
    }


    private static int generateRandomNumber(int max, int min) {
        Random random = new Random();
        return random.nextInt(max - min + 1) + min;
    }


}