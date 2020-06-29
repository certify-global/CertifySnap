package com.certify.snap.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.certify.snap.R;
import com.common.thermalimage.CalibrationCallBack;
import com.common.thermalimage.HotImageCallback;
import com.common.thermalimage.TemperatureBitmapData;
import com.common.thermalimage.TemperatureData;
import com.common.thermalimage.ThermalImageUtil;

import java.text.DecimalFormat;

public class TemperatureCalibrationActivity extends SettingBaseActivity {
    ThermalImageUtil temperatureUtil;
    TextView showMessage;
    ImageView image;
    EditText distance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_temperature_calibration);

        temperatureUtil = new ThermalImageUtil(this);
        showMessage = findViewById(R.id.display_message);
        image = findViewById(R.id.image);

        distance = findViewById(R.id.distance);
    }

    public void onParamterback(View view) {
        startActivity(new Intent(this, TemperatureActivity.class));
        finish();
    }

    private void showMessage(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showMessage.setText(msg);
            }
        });
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.get_temperature:
                float distance = 50;
                if (this.distance.length() != 0) {
                    distance = Float.valueOf(this.distance.getText().toString());
                }
                final float distances = distance;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        TemperatureData temperatureData = temperatureUtil.getDataAndBitmap(distances, true, new HotImageCallback.Stub() {
                            @Override
                            public void onTemperatureFail(String e) {
                                showMessage("Failed to get temperature:  " + e);
                            }

                            @Override
                            public void getTemperatureBimapData(final TemperatureBitmapData data) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        image.setImageBitmap(data.getBitmap());
                                    }
                                });
                            }

                        });
                        if (temperatureData != null) {
                            String text = "";
                            if (temperatureData.isUnusualTem()) {
                                text = "Temperature abnormaly!";
                            } else {
                                text = "Temperature normal";
                            }
                            float celsius = temperatureData.getTemperature();
                            float fahrenheit = celsius * (9f / 5) + 32;

                            String fahrenheitText = new DecimalFormat("##.#").format(fahrenheit);
                            showMessage(text + "\nTemperature: " + celsius + " ℃ / " + fahrenheitText + " °F");
                        }
                    }
                }).start();

                break;

            case R.id.check_calibration:
                temperatureUtil.calibrationTem_DAT(new CalibrationCallBack.Stub() {
                    @Override
                    public void onCalibrating() {
                        showMessage("Calibrating...");
                    }

                    @Override
                    public void onSuccess() {
                        showMessage("Calibration success!");
                    }

                    @Override
                    public void onFail(final String errmsg) {
                        showMessage("Calibration failed! " + errmsg);
                    }
                });
                break;
        }

    }
}