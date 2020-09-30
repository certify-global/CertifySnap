package com.certify.snap.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.certify.snap.R;
import com.common.thermalimage.CalibrationCallBack;
import com.common.thermalimage.GuideDataCallBack;
import com.common.thermalimage.TemperatureBigData;
import com.common.thermalimage.ThermalImageUtil;

import java.text.DecimalFormat;
import java.util.List;

public class TemperatureCalibrationGuideActivity extends SettingBaseActivity {


    ThermalImageUtil util;
    int[] moduleInfo;
    ImageView hotImage;
    TextView moduleType, showResult, imageTime, dataTime, showMessage;
    Button start;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_temperature_calibration_guide);


        moduleType = findViewById(R.id.moduleType);
        hotImage = findViewById(R.id.hotImage);
        showResult = findViewById(R.id.showResult);
        start = findViewById(R.id.start);
        imageTime = findViewById(R.id.imageTime);
        dataTime = findViewById(R.id.dataTime);
        showMessage = findViewById(R.id.display_message);
    }

    public void onParamterback(View view) {
        startActivity(new Intent(this, TemperatureActivity.class));
        finish();
    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
        new Thread(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                util = new ThermalImageUtil(TemperatureCalibrationGuideActivity.this);
                while (moduleInfo == null) {//Wait for the service connection to succeed
                    moduleInfo = util.getUsingModule();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        moduleType.setText("Model[" + moduleInfo[0] + "]");
                        start.performClick();
                    }
                });
            }
        }).start();
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
       /* if (util != null) {
            util.stopGetGuideData();
            util.release();
        }
        System.exit(0);*/
    }

    long lastBitmapTime;
    long lastDataTime;

    public void start(View view) {

        util.getGuideData(new GuideDataCallBack.Stub() {

            @Override
            public void callBackData(final TemperatureBigData data) throws RemoteException {
                // TODO Auto-generated method stub

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        List<float[]> info = data.getTemInfoList();
                        float envir = data.getEmvirTem();

                        for (int i = 0; i < info.size(); i++) {
                            float machineTemperature = info.get(i)[0] * (9f / 5) + 32;
                            float bodyTemperature = info.get(i)[3] * (9f / 5) + 32;
                            String fahrenheitText = new DecimalFormat("##.#").format(bodyTemperature);
                            //showResult.setText("Ambient temperature[" + envir + "]\n");
                            showResult.setText("Temperature"  + "[" + info.get(i)[3] +" ℃ /" + fahrenheitText + " °F"+"]\n");
                        }

                        long nowTime = System.currentTimeMillis();
                        dataTime.setText("dataTime[" + (nowTime - lastDataTime) + "ms]");
                        Log.d("tagg", "aadataTime[" + (nowTime - lastDataTime) + "]");
                        lastDataTime = nowTime;
                    }
                });
            }

            @Override
            public void callBackError(String s) throws RemoteException {

            }

            @Override
            public void callBackBitmap(final Bitmap bitmap) throws RemoteException {
                // TODO Auto-generated method stub

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        hotImage.setImageBitmap(bitmap);

                        long nowTime = System.currentTimeMillis();
                        imageTime.setText("imageTime[" + (nowTime - lastBitmapTime) + "ms]");
                        //Log.d("tadd", "bitmapTime["+(nowTime - lastBitmapTime)+"]");
                        lastBitmapTime = nowTime;

                    }
                });
            }
        });
        util.setGuideRect(new Rect[]{new Rect(0, 0, 400, 400)}, 0);
    }

    public void calibrate(View view){
        util.calibrationTem_DAT(new CalibrationCallBack.Stub() {
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
    }

    private void showMessage(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showMessage.setText(msg);
            }
        });
    }

   /* Handler handler = new Handler() {

        public void handleMessage(android.os.Message msg) {

            switch (msg.what) {
                case 1:
                    util.setGuideRect(new Rect[]{new Rect(60, 40, 300, 230)}, 0);
                    handler.sendEmptyMessageDelayed(1, 1000);
                    break;

                case 2:
                    util.setGuideRect(new Rect[]{new Rect(135, 95, 200, 155)}, 0);
                    handler.sendEmptyMessageDelayed(2, 1000);
                    break;

                case 3:
                    util.setGuideRect(new Rect[]{new Rect(120, 180, 180, 255)}, 0);
                    handler.sendEmptyMessageDelayed(3, 1000);
                    break;

                case 4:
                    util.setGuideRect(new Rect[]{new Rect(80, 25, 140, 70), new Rect(135, 95, 200, 155), new Rect(120, 180, 180, 255)}, 0);
                    handler.sendEmptyMessageDelayed(4, 1000);
                    break;

                default:
                    break;
            }

        }

        ;

    };*/

    private void removeMessage() {

       /* if (handler.hasMessages(1)) {
            handler.removeMessages(1);
        }
        if (handler.hasMessages(2)) {
            handler.removeMessages(2);
        }
        if (handler.hasMessages(3)) {
            handler.removeMessages(3);
        }
        if (handler.hasMessages(4)) {
            handler.removeMessages(4);
        }*/
    }

    public void rect1(View view) {
        //removeMessage();
        //handler.sendEmptyMessage(1);
    }

    public void rect2(View view) {
        //removeMessage();
        //handler.sendEmptyMessage(2);
    }

    public void rect3(View view) {
        //removeMessage();
        //handler.sendEmptyMessage(2);
    }

    public void allRect(View view) {
        //removeMessage();
        //handler.sendEmptyMessage(2);
    }

}