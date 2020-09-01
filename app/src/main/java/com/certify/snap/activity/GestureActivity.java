package com.certify.snap.activity;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.certify.snap.R;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class GestureActivity extends AppCompatActivity {

    private boolean runCheck = true;
    private TextView peopleHandTips;
    private EditText leftRange, rightRange;
    private TextView covidQuestionsText;
    private Button question_one_yes_button, question_one_no_button;

    public static final String COVID_QUESTION_ONE = "covid_question_one";
    public static final String COVID_QUESTION_TWO = "covid_question_two";
    public static final String COVID_QUESTION_THREE = "covid_question_three";
    private String nextQuestion = COVID_QUESTION_ONE;
    Boolean wait = true;
    private SeekBar mSeekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gesture);
        initView();
        new Thread(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                while (runCheck) {
                    Map<String, String> map = sendCMD(1);
                    if (map != null) {
                        final int left = Integer.valueOf(map.get("leftPower"));
                        final int right = Integer.valueOf(map.get("rightPower"));
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                // TODO Auto-generated method stub
                                peopleHandTips.setText("Left hand energy[" + left + "] Right hand energy[" + right + "]");
                            }
                        });

                        if (left >= 200) {
                            leftHandWave();
                        }
                        if (right >= 200) {
                            rightHandWave();
                        }

                        if (left <= leftRangeValue && right <= rightRangeValue) {
                            sendCMD(5);
                        } else if (left > leftRangeValue && right > rightRangeValue) {
                            sendCMD(6);
                        } else if (left > leftRangeValue && right <= rightRangeValue) {
                            sendCMD(4);
                        } else if (left <= leftRangeValue && right > rightRangeValue) {
                            sendCMD(3);
                        }
                    }
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        runCheck = false;
        System.exit(0);
    }

    void initView() {
        peopleHandTips = (TextView) findViewById(R.id.peopleHandTips);
        leftRange = (EditText) findViewById(R.id.leftRange);
        rightRange = (EditText) findViewById(R.id.rightRange);
        covidQuestionsText = findViewById(R.id.covid_questions_text);
        question_one_yes_button = findViewById(R.id.question_one_yes_button);
        question_one_no_button = findViewById(R.id.question_one_no_button);
        mSeekBar = findViewById(R.id.seekBar);
    }

    int leftRangeValue = 50;
    int rightRangeValue = 50;

    public void sureRange(View view) {
        String leftRangeString = leftRange.getText().toString();
        if (leftRangeString != null && !"".equals(leftRangeString)) {
            leftRangeValue = Integer.valueOf(leftRangeString);
        }
        String rightRangeString = rightRange.getText().toString();
        if (rightRangeString != null && !"".equals(rightRangeString)) {
            rightRangeValue = Integer.valueOf(rightRangeString);
        }
    }

    /**
     * 1: Left and right hand energy<br>
     * 2: Read version<br>
     * 3: When the left-hand induced energy value <= the left-hand threshold, the left-hand light is on.<br>
     * 4: Right-hand induction energy value <= right-hand threshold, right-hand light is on<br>
     * 5: Left hand sensing energy value <= left hand threshold value && right hand sensing energy value <= right hand threshold value, both lights are on<br>
     * 6: When left-hand sensing energy value> left-hand threshold && right-hand sensing energy value> right-hand threshold, both left-hand lights are off<br>
     */
    public Map<String, String> sendCMD(int type) {
        openReader(this);
        Map<String, String> map = null;
        if (type == 1) {// Left and right hand energy
            map = sendCommandUSB(new byte[]{(byte) 0xEE, (byte) 0xEE, (byte) 0xEE, 0x66, 0x02, 0x00, 0x11, 0x01, 0x14}, 1);
        } else if (type == 2) {//  Read version
            map = sendCommandUSB(new byte[]{(byte) 0xEE, (byte) 0xEE, (byte) 0xEE, 0x66, 0x02, 0x00, 0x11, 0x02, 0x15}, 2);
        } else if (type == 3) {// Left hand light is on
            map = sendCommandUSB(new byte[]{(byte) 0xEE, (byte) 0xEE, (byte) 0xEE, 0x66, 0x03, 0x00, 0x10, 0x01, 0x02, 0x16}, 3);
        } else if (type == 4) {// Right hand light is on
            map = sendCommandUSB(new byte[]{(byte) 0xEE, (byte) 0xEE, (byte) 0xEE, 0x66, 0x03, 0x00, 0x10, 0x01, 0x01, 0x15}, 3);
        } else if (type == 5) {// Both lights are on
            map = sendCommandUSB(new byte[]{(byte) 0xEE, (byte) 0xEE, (byte) 0xEE, 0x66, 0x03, 0x00, 0x10, 0x01, 0x00, 0x14}, 3);
        } else if (type == 6) {// Both lights are off
            map = sendCommandUSB(new byte[]{(byte) 0xEE, (byte) 0xEE, (byte) 0xEE, 0x66, 0x03, 0x00, 0x10, 0x01, 0x03, 0x17}, 3);
        }
        usbReader = null;
        mUsbManager = null;
        return map;
    }

    private Context mContext;
    private UsbDevice usbReader = null;
    private UsbManager mUsbManager = null;
    private static final String ACTION_USB_PERMISSION = "com.wch.multiport.USB_PERMISSION";

    private void openReader(Context context) {

        try {
            mContext = context;
            usbReader = null;
            mUsbManager = null;
            if (usbReader == null) {
                PendingIntent mPermissionIntent = PendingIntent.getBroadcast(mContext, 0, new Intent("com.android.example.USB_PERMISSION"), 0);
                mUsbManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
                HashMap<String, UsbDevice> deviceHashMap = mUsbManager.getDeviceList();
                Iterator<UsbDevice> iterator = deviceHashMap.values().iterator();

                while (iterator.hasNext()) {
                    UsbDevice usbDevice = iterator.next();
                    int pid = usbDevice.getProductId();
                    int vid = usbDevice.getVendorId();
                    if (
                            pid == 0x5790 && vid == 0x0483     //左右手
                    ) {
                        usbReader = usbDevice;
                        if (mUsbManager.hasPermission(usbDevice)) {
                            break;
                        } else {
                            mUsbManager.requestPermission(usbDevice, mPermissionIntent);
                        }
                    }
                }
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    private Map<String, String> sendCommandUSB(byte[] cmd, int type) {
        byte[] result = null;
        Map<String, String> resultData = null;
        try {
            UsbInterface usbInterface = usbReader.getInterface(0);// USBEndpoint为读写数据所需的节点
            UsbEndpoint inEndpoint = usbInterface.getEndpoint(0); // 读数据节点
            UsbEndpoint outEndpoint = usbInterface.getEndpoint(1);
            UsbDeviceConnection connection = null;
            connection = mUsbManager.openDevice(usbReader);
            if (connection == null) {
                return null;
            }
            connection.claimInterface(usbInterface, true);

            StringBuffer bufferCMD = new StringBuffer();
            for (int i = 0; i < cmd.length; i++) {
                bufferCMD.append(toHexString(new byte[]{cmd[i]}) + " ");
            }
            Log.d("tagg", "send[" + bufferCMD.toString() + "]");

            int out = connection.bulkTransfer(outEndpoint, cmd, cmd.length, 1500);

            byte[] byte2 = new byte[64];
            int ret;
            ret = connection.bulkTransfer(inEndpoint, byte2, byte2.length, 1500);
            Log.d("tagg", "receive ret[" + ret + "]");
            if (ret > 0) {
                result = Arrays.copyOfRange(byte2, 0, ret);
                resultData = new HashMap<String, String>();
                StringBuffer buffer = new StringBuffer();
                for (int i = 0; i < result.length; i++) {
                    buffer.append(toHexString(new byte[]{result[i]}) + " ");
                }
                Log.d("tagg", "receive[" + buffer.toString() + "]");
                if (type == 1 && ret == 13 && result[7] == 0x01) {
                    String rightString = toHexString(new byte[]{result[9], result[8]});
                    int rightPower = Integer.valueOf(rightString, 16);
                    String leftString = toHexString(new byte[]{result[11], result[10]});
                    int leftPower = Integer.valueOf(leftString, 16);
                    resultData.put("leftPower", "" + leftPower);
                    resultData.put("rightPower", "" + rightPower);
                } else if (type == 2 && ret == 11 && result[7] == 0x02) {
                    String versionString = toHexString(new byte[]{result[9], result[8]});
                    int version = Integer.valueOf(versionString);
                    resultData.put("version", "" + version);
                }
            } else {
                return null;
            }
            connection.close();
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        return resultData;
    }

    public static String toHexString(byte[] data) {
        if (data == null) {
            return "";
        }

        String string;
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < data.length; i++) {
            string = Integer.toHexString(data[i] & 0xFF);
            if (string.length() == 1) {
                stringBuilder.append("0");
            }

            stringBuilder.append(string.toUpperCase());
        }

        return stringBuilder.toString();
    }

    public static byte[] toBytes(String string) {
        int len;
        String str;
        String hexStr = "0123456789ABCDEF";

        String s = string.toUpperCase();

        len = s.length();
        if ((len % 2) == 1) {
            str = s + "0";
            len = (len + 1) >> 1;
        } else {
            str = s;
            len >>= 1;
        }

        byte[] bytes = new byte[len];
        byte high;
        byte low;

        for (int i = 0, j = 0; i < len; i++, j += 2) {
            high = (byte) (hexStr.indexOf(str.charAt(j)) << 4);
            low = (byte) hexStr.indexOf(str.charAt(j + 1));
            bytes[i] = (byte) (high | low);
        }

        return bytes;
    }


    private void leftHandWave() {
        Log.d("TAG", "Naga.......left wave ");
        if (wait) {
            changeQuestion(nextQuestion, true);
        }
    }

    private void rightHandWave() {
        Log.d("TAG", "Naga.......right wave");
        if (wait) {
            changeQuestion(nextQuestion, false);
        }
    }

    public void resetButton(View view) {
        changeQuestion(nextQuestion, true);
    }

    public void changeQuestion(final String key, final boolean value) {
        wait = false;
        startTimer();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (key.equals(COVID_QUESTION_ONE) && value) {
                    nextQuestion = COVID_QUESTION_TWO;
                    covidQuestionsText.setText("2. Have you travelled overseas in the last 14 days");
                    buttonReset();

                } else if (key.equals(COVID_QUESTION_ONE)) {
                    nextQuestion = COVID_QUESTION_TWO;
                    covidQuestionsText.setText("2. Have you travelled overseas in the last 14 days");
                    buttonReset();
                } else if (key.equals(COVID_QUESTION_TWO) && value) {
                    nextQuestion = COVID_QUESTION_THREE;
                    covidQuestionsText.setText("3. Have you been in contact with someone who has confirmed case of Covid-19?");
                    buttonReset();

                } else if (key.equals(COVID_QUESTION_TWO)) {
                    nextQuestion = COVID_QUESTION_THREE;
                    covidQuestionsText.setText("3. Have you been in contact with someone who has confirmed case of Covid-19?");
                    buttonReset();
                } else if (key.equals(COVID_QUESTION_THREE) && value) {
                    buttonReset();
                    //startActivity(new Intent(GestureActivity.this, IrCameraActivity.class));
                } else if (key.equals(COVID_QUESTION_THREE)) {
                    //startActivity(new Intent(GestureActivity.this, IrCameraActivity.class));
                    buttonReset();
                }
            }
        });
    }

    private void buttonReset() {
        mSeekBar.setProgress(mSeekBar.getProgress() + 1);
        question_one_yes_button.setBackgroundColor(getResources().getColor(R.color.gray));
        question_one_no_button.setBackgroundColor(getResources().getColor(R.color.gray));
    }

    private Timer mTimer;

    private void startTimer() {
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            public void run() {
                wait = true;
                this.cancel();
            }
        }, 2 * 1000);
    }

}