package com.certify.snap.activity;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.TextView;
import com.certify.snap.R;
import com.certify.snap.common.Logger;
import com.certify.snap.common.Util;

public class RegisterSuccessActivity extends Activity {
    private static String TAG = "RegisterSuccessActivity -> ";
    TextView textview_name;
    Typeface rubiklight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_register_success);
            textview_name = findViewById(R.id.textview_name);
            rubiklight = Typeface.createFromAsset(getAssets(),
                    "rubiklight.ttf");
            textview_name.setTypeface(rubiklight);
            textview_name.setText("Your device Serial Number: "+  Util.getSNCode(RegisterSuccessActivity.this)+" has been activated.");

        } catch (Exception e) {
            Logger.error(TAG," onCreate(@Nullable Bundle savedInstanceState)", e.getMessage());
        }
    }


}
