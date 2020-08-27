package com.certify.snap.activity;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.certify.snap.R;
import com.certify.snap.bluetooth.bleCommunication.BluetoothGattAttributes;
import com.certify.snap.bluetooth.bleCommunication.BluetoothLeService;
import com.certify.snap.bluetooth.bleCommunication.BusProvider;
import com.certify.snap.bluetooth.bleCommunication.DeviceChangedEvent;
import com.certify.snap.bluetooth.data.DeviceInfoManager;
import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.Util;
import com.certify.snap.controller.BLEController;
import com.certify.snap.controller.PrinterController;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;

import static com.certify.snap.common.Constants.MEASURED_STATE_MASK;

public class AudioVisualActivity extends SettingBaseActivity {

    private SharedPreferences sp ;
    TextView tv_sound_high, tv_sound, btn_save, tv_light_low, tv_light_high, tv_ble_test, tv_ble_connect, tv_ble_status,
            title_audio_alert, title_visual_alert, tv_ble_connection, tv_qr_sound_valid, tv_qr_sound_invalid;
    Button tv_ble_connect_btn, light_on, light_off;
    Typeface rubiklight;
    LinearLayout visul_alert_layout;

    private final String TAG = AudioVisualActivity.this.getClass().getSimpleName();

    private boolean mConnected = false;

    private BluetoothLeService mBluetoothLeService;

    private static final int REQUEST_WRITE_STORAGE = 8000;
    private static final int RESULT_LOAD_IMAGE = 8001;

    private byte[] ledrgb = new byte[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_visual);
        ButterKnife.bind(this);
        BusProvider.getInstance().register(this);
        sp = Util.getSharedPreferences(this);

        initView();
        temperatureAudioCheck();
        visualCheck();
        qrAudioCheck();


        // request ble permission
        requestPermission();

        // connection ble service
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // connect ble device
        if  (mBluetoothLeService != null)
            mBluetoothLeService.connect(DeviceInfoManager.getInstance().getDeviceAddress());

    }
    private void initView(){

        tv_ble_connect_btn = findViewById(R.id.tv_ble_connection_btn);
        light_on = findViewById(R.id.light_On);
        light_off = findViewById(R.id.light_off);

        tv_sound_high = findViewById(R.id.tv_sound_high);
        tv_sound = findViewById(R.id.tv_sound_low);
        btn_save = findViewById(R.id.btn_exit);
        tv_light_low = findViewById(R.id.tv_light_low);
        tv_light_high = findViewById(R.id.tv_light_high);
        tv_ble_test = findViewById(R.id.tv_ble_test);
        tv_ble_connect = findViewById(R.id.tv_ble_connect);
        tv_ble_status = findViewById(R.id.tv_ble_status);
        title_audio_alert = findViewById(R.id.title_audio_alert);
        title_visual_alert = findViewById(R.id.title_visual_alert);
        tv_ble_connection = findViewById(R.id.tv_ble_connection);
        visul_alert_layout = findViewById(R.id.visul_alert_layout);
        tv_qr_sound_valid = findViewById(R.id.tv_qr_sound_valid);
        tv_qr_sound_invalid = findViewById(R.id.tv_qr_sound_invalid);


        rubiklight = Typeface.createFromAsset(getAssets(),
                "rubiklight.ttf");
        tv_sound.setTypeface(rubiklight);
        tv_sound_high.setTypeface(rubiklight);
        btn_save.setTypeface(rubiklight);
        tv_ble_connect_btn.setTypeface(rubiklight);
        tv_light_low.setTypeface(rubiklight);
        tv_light_high.setTypeface(rubiklight);
        tv_ble_test.setTypeface(rubiklight);
        light_on.setTypeface(rubiklight);
        light_off.setTypeface(rubiklight);
        tv_ble_connect.setTypeface(rubiklight);
        tv_ble_status.setTypeface(rubiklight);
        title_audio_alert.setTypeface(rubiklight);
        title_visual_alert.setTypeface(rubiklight);
        tv_ble_connection.setTypeface(rubiklight);
        tv_qr_sound_valid.setTypeface(rubiklight);
        tv_qr_sound_invalid.setTypeface(rubiklight);

        String text = "<a style='text-decoration:underline' href='http://www.sample.com'>Connect</a>";
        if (Build.VERSION.SDK_INT >= 24) {
            tv_ble_connection.setText(Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY));
        } else {
            tv_ble_connection.setText(Html.fromHtml(text));
        }
    }

    private void temperatureAudioCheck(){
        RadioGroup radio_group_sound = findViewById(R.id.radio_group_sound);
        RadioButton radio_yes_sound = findViewById(R.id.radio_yes_sound);
        RadioButton radio_no_sound = findViewById(R.id.radio_no_sound);
        RadioGroup radio_group_sound_high = findViewById(R.id.radio_group_sound_high);
        RadioButton radio_yes_sound_high = findViewById(R.id.radio_yes_sound_high);
        RadioButton radio_no_sound_high = findViewById(R.id.radio_no_sound_high);

        if(sp.getBoolean(GlobalParameters.TEMPERATURE_SOUND_NORMAL,false))
            radio_yes_sound.setChecked(true);
        else radio_no_sound.setChecked(true);
        if(sp.getBoolean(GlobalParameters.TEMPERATURE_SOUND_HIGH,false))
            radio_yes_sound_high.setChecked(true);
        else radio_no_sound_high.setChecked(true);

        radio_group_sound.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId==R.id.radio_yes_sound)
                    Util.writeBoolean(sp, GlobalParameters.TEMPERATURE_SOUND_NORMAL, true);
                else Util.writeBoolean(sp, GlobalParameters.TEMPERATURE_SOUND_NORMAL, false);
            }
        });
        radio_group_sound_high.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId==R.id.radio_yes_sound_high)
                    Util.writeBoolean(sp, GlobalParameters.TEMPERATURE_SOUND_HIGH, true);
                else Util.writeBoolean(sp, GlobalParameters.TEMPERATURE_SOUND_HIGH, false);
            }
        });
    }

    private void qrAudioCheck(){
        RadioGroup qr_valid_radio_group_sound = findViewById(R.id.qr_radio_group_sound_valid);
        RadioButton qr_radio_yes_sound_valid = findViewById(R.id.qr_radio_yes_sound_valid);
        RadioButton qr_radio_no_sound_valid = findViewById(R.id.qr_radio_no_sound_valid);
        RadioGroup qr_radio_group_sound_invalid = findViewById(R.id.qr_radio_group_sound_invalid);
        RadioButton qr_radio_yes_sound_invalid = findViewById(R.id.qr_radio_yes_sound_invalid);
        RadioButton qr_radio_no_sound_invalid = findViewById(R.id.qr_radio_no_sound_invalid);

        if(sp.getBoolean(GlobalParameters.QR_SOUND_VALID,false))
            qr_radio_yes_sound_valid.setChecked(true);
        else qr_radio_no_sound_valid.setChecked(true);
        if(sp.getBoolean(GlobalParameters.QR_SOUND_INVALID,false))
            qr_radio_yes_sound_invalid.setChecked(true);
        else qr_radio_no_sound_invalid.setChecked(true);

        qr_valid_radio_group_sound.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId==R.id.qr_radio_yes_sound_valid)
                    Util.writeBoolean(sp, GlobalParameters.QR_SOUND_VALID, true);
                else Util.writeBoolean(sp, GlobalParameters.QR_SOUND_VALID, false);
            }
        });
        qr_radio_group_sound_invalid.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId==R.id.qr_radio_yes_sound_invalid)
                    Util.writeBoolean(sp, GlobalParameters.QR_SOUND_INVALID, true);
                else Util.writeBoolean(sp, GlobalParameters.QR_SOUND_INVALID, false);
            }
        });
    }

    private void visualCheck(){
        RadioGroup radio_group_light = findViewById(R.id.radio_group_light);
        RadioButton radio_yes_light = findViewById(R.id.radio_yes_light);
        RadioButton radio_no_light = findViewById(R.id.radio_no_light);
        RadioGroup radio_group_light_high = findViewById(R.id.radio_group_light_high);
        RadioButton radio_yes_light_high = findViewById(R.id.radio_yes_light_high);
        RadioButton radio_no_light_high = findViewById(R.id.radio_no_light_high);

        if(sp.getBoolean(GlobalParameters.BLE_LIGHT_NORMAL,false))
            radio_yes_light.setChecked(true);
        else radio_no_light.setChecked(true);
        if(sp.getBoolean(GlobalParameters.BLE_LIGHT_HIGH,false))
            radio_yes_light_high.setChecked(true);
        else radio_no_light_high.setChecked(true);

        radio_group_light.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId==R.id.radio_yes_light)
                    Util.writeBoolean(sp, GlobalParameters.BLE_LIGHT_NORMAL, true);
                else Util.writeBoolean(sp, GlobalParameters.BLE_LIGHT_NORMAL, false);
            }
        });
        radio_group_light_high.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId==R.id.radio_yes_light_high)
                    Util.writeBoolean(sp, GlobalParameters.BLE_LIGHT_HIGH, true);
                else Util.writeBoolean(sp, GlobalParameters.BLE_LIGHT_HIGH, false);
            }
        });
    }

    public void saveAudioSettings(View view){
        Util.showToast(AudioVisualActivity.this, getString(R.string.save_success));
        finish();
    }

    /**
     * bluetooth service connection
     */
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Toast.makeText(getBaseContext(), R.string.ble_not_find, Toast.LENGTH_SHORT).show();
                finish();
            }
            mBluetoothLeService.connect(DeviceInfoManager.getInstance().getDeviceAddress());
            BLEController.getInstance().setBluetoothLeService(mBluetoothLeService);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    /**
     * receive connection state
     */
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final Intent mIntent = intent;
            final String action = intent.getAction();

            // connected
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                Log.e(TAG, "BroadcastReceiver : Connected!");
                Toast.makeText(getBaseContext(), R.string.ble_connect_success, Toast.LENGTH_SHORT).show();
                mConnected = true;
                uiConnect();
            }
            // disconnected
            else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                Log.e(TAG, "BroadcastReceiver : Disconnected!");
                Toast.makeText(getBaseContext(), R.string.ble_disconnected, Toast.LENGTH_SHORT).show();
                mConnected = false;
                uiDisConnect();
            }
            // found GATT service
            else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                Log.e(TAG, "BroadcastReceiver : Found GATT!");
            }
        }
    };

    private void uiConnect(){
        //tv_ble_connect_btn.setText("CLEAR");
        String text = "<a style='text-decoration:underline' href='http://www.sample.com'>Clear</a>";
        if (Build.VERSION.SDK_INT >= 24) {
            tv_ble_connection.setText(Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY));
        } else {
            tv_ble_connection.setText(Html.fromHtml(text));
        }
        String deviceInfo = DeviceInfoManager.getInstance().getDeviceName() + " " + DeviceInfoManager.getInstance().getDeviceAddress();
        tv_ble_status.setText(deviceInfo);
        tv_ble_status.setTextColor(getResources().getColor(R.color.green));
        light_on.setEnabled(true);
        light_off.setEnabled(true);
        light_on.setBackgroundColor(getResources().getColor(R.color.bg_blue));
        light_off.setBackgroundColor(getResources().getColor(R.color.bg_blue));
    }

    private void uiDisConnect(){
        //tv_ble_connect_btn.setText("CONNECT");
        tv_ble_status.setText("NONE");
        tv_ble_status.setTextColor(getResources().getColor(R.color.red));
        String text = "<a style='text-decoration:underline' href='http://www.sample.com'>Connect</a>";
        if (Build.VERSION.SDK_INT >= 24) {
            tv_ble_connection.setText(Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY));
        } else {
            tv_ble_connection.setText(Html.fromHtml(text));
        }
        light_on.setEnabled(false);
        light_off.setEnabled(false);
        light_on.setBackgroundColor(getResources().getColor(R.color.gray));
        light_off.setBackgroundColor(getResources().getColor(R.color.gray));
    }

    /**
     * get broadcast intent-filter
     * @return
     */
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        return intentFilter;
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
      /*  BusProvider.getInstance().unregister(this);
        try {
            if (mConnected) {
                unbindService(mServiceConnection);
                mBluetoothLeService = null;
            }
        }
        catch (Exception e){
            Log.e(TAG, "BLE unbind Error");
        }*/
    }

    /**
     * if selected a new ble device, connect a new one
     * @param event
     */
    @Subscribe
    public void deviceChanged(DeviceChangedEvent event) {
        if (mBluetoothLeService != null) {
            mBluetoothLeService.disconnect();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mBluetoothLeService.connect(DeviceInfoManager.getInstance().getDeviceAddress());
                }
            }, 500);
        } else
            mBluetoothLeService.connect(DeviceInfoManager.getInstance().getDeviceAddress());
    }

    /**
     * send rgb byte array to ble device
     * @param rgb
     * @return
     */
    private boolean controlLed(byte[] rgb) {
        // get bluetoothGattCharacteristic
        BluetoothGattCharacteristic characteristic = mBluetoothLeService.getGattCharacteristic(BluetoothGattAttributes.LED_CHARACTERISTIC);

        if (characteristic != null) {
            // check connection
            if (!mConnected) {
                Toast.makeText(this, R.string.ble_not_connected, Toast.LENGTH_SHORT).show();
                return false;
            }
            // send characteristic data
            mBluetoothLeService.sendDataCharacteristic(characteristic,rgb );
            return true;
        }
        Log.e(TAG, "Not founded characteristic");
        return false;
    }

    public void selectBleDevice(View v) {
        if (!mConnected) {
            Intent intent = new Intent(this, SelectDeviceActivity.class);
            startActivity(intent);
        } else {
            mBluetoothLeService.disconnect();
        }
    }

    /**
     * requestPermissions
     */
    private void requestPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, R.string.permission_request, Toast.LENGTH_LONG);
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.BLUETOOTH,
                            Manifest.permission.BLUETOOTH_ADMIN,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.RECORD_AUDIO,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.INTERNET},
                    REQUEST_WRITE_STORAGE);
        }
    }

    /**
     * onActivityResult, request ble system enable
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // user chose not to enable bluetooth.
        if (requestCode == REQUEST_WRITE_STORAGE && resultCode == Activity.RESULT_CANCELED) {
            Toast.makeText(this, R.string.ble_canceled, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // user choose a picture from gallery
        else if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };
            Cursor cursor = getContentResolver().query(selectedImage,filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            Bitmap bitmap = BitmapFactory.decodeFile(picturePath);
            Drawable drawable = new BitmapDrawable(getResources(), bitmap);
        }
    }

    public void bleLightOn(View view){
        if(mConnected) {
            int val = lightOn();
            byte[] rgb = {6, 1, intToByte(Color.red(val)), intToByte(Color.green(val)), intToByte(Color.blue(val))};
            controlLed(rgb);
            for(int i=0; i<3; i++)
                ledrgb[i] = rgb[i+1];
        }
    }

    public void bleLightOff(View view){
        if(mConnected) {
            //Off the light
            byte[] rgb = {6, 1, intToByte(Color.red(MEASURED_STATE_MASK)), intToByte(Color.green(MEASURED_STATE_MASK)), intToByte(Color.blue(MEASURED_STATE_MASK))};

            controlLed(rgb);

            for(int i=0; i<3; i++)
                ledrgb[i] = rgb[i+1];
        }
    }

    public static byte intToByte(int i) {
        return (byte) (i & 255);
    }

    private List<byte[]> colorDataList = new ArrayList<>();

    public void setColor(byte[] bArr) {
        int i = -1;
        byte[] bArr1 = {6, 1, intToByte(Color.red(i)), intToByte(Color.green(i)), intToByte(Color.blue(i))};
        this.colorDataList.clear();
        this.colorDataList.add(bArr1);
    }

    public void data(){
        byte[] bArr = new byte[(this.colorDataList.size() * 5)];
        int i = 0;
        for (byte[] bArr2 : this.colorDataList) {
            System.arraycopy(bArr2, 0, bArr, i, bArr2.length);
            i += bArr2.length;
        }
    }

    public int setColorLight(int i, float f) {
        int i2;
        int i3;
        int i4;
        if (((double) f) <= 0.0d) {
            int red = (int) (((float) Color.red(i)) - (((float) (255 - Color.red(i))) * f));
            i4 = (int) (((float) Color.green(i)) - (((float) (255 - Color.green(i))) * f));
            i3 = (int) (((float) Color.blue(i)) - (f * ((float) (255 - Color.blue(i)))));
            i2 = red;
        } else {
            float f2 = 1.0f - f;
            i2 = (int) (((float) Color.red(i)) * f2);
            i4 = (int) (((float) Color.green(i)) * f2);
            i3 = (int) (f2 * ((float) Color.blue(i)));
        }
        return Color.rgb(i2, i4, i3);
    }

    public static int getColor() {
        return mColor;
    }

    public static void setColor(int i) {
        mColor = -1;
    }

    public static float getPerLight() {
        return mPer;
    }

    public static void setPerLight(float f) {
        mPer = f;
    }

    public int lightOn(){
        int i = 0;
        float value = 5/ 255.0f;
        return setColorLight(-1,value);
    }

    public static int mColor = -1;
    private static float mPer;
}