package com.certify.snap.controller;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.util.Log;

import com.certify.snap.bluetooth.data.ReadPacket;
import com.certify.snap.bluetooth.data.WritePacket;
import com.certify.snap.service.PeripheralAdvertiseService;
import com.google.gson.Gson;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;

import static android.app.AlarmManager.ELAPSED_REALTIME;
import static android.os.SystemClock.elapsedRealtime;
import static com.certify.snap.common.Constants.ACCESS_CARD_CHARACTERISTIC_UUID;
import static com.certify.snap.common.Constants.ACCESS_CARD_UUID;

public class BlePeripheralController {
    public static final String TAG = BlePeripheralController.class.getSimpleName();
    private static BlePeripheralController instance = null;
    private Context context;

    private BluetoothLeScanner bluetoothLeScanner;
    private BluetoothGattServer mBleGattServer;
    private ServiceConnection mServiceConnection;
    private BluetoothManager mBluetoothManager;
    private BluetoothGattCharacteristic mCharacteristic;
    private BluetoothGattCharacteristic mSampleCharacteristic;
    private BroadcastReceiver mGattUpdateReceiver;
    private String currentDeviceAddress = "";

    private ArrayList<BluetoothDevice> deviceList = new ArrayList<>();
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mDeviceServices = new ArrayList<>();
    private BleCallbackListener listener = null;
    private Timer peripheralScanTimer;
    private int deviceMapIndex = 0;
    private static final long PERIPHERAL_ADVERTISE_TIME = 20000;

    public interface BleCallbackListener {
        void onUpdateStatus(String status);
    }

    public static BlePeripheralController getInstance() {
        if (instance == null) {
            instance = new BlePeripheralController();
        }
        return instance;
    }

    public void init(Context context) {
        this.context = context;
        mBluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        if (mBluetoothManager != null) {
            BluetoothAdapter bluetoothAdapter = mBluetoothManager.getAdapter();
            bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
            mBluetoothManager = (BluetoothManager)context.getSystemService(Context.BLUETOOTH_SERVICE);
        }
        deviceList.clear();
    }

    public void setBleListener(BleCallbackListener callbackListener) {
        this.listener = callbackListener;
    }

    public ArrayList<BluetoothDevice> getDeviceList() {
        return deviceList;
    }


    public void startAdvertising() {
        startPeripheralAdvertiseService(context);
    }

    public void startPeripheralAdvertiseService(Context context) {
        if (mBluetoothManager != null) {
            mBleGattServer = mBluetoothManager.openGattServer(context, mGattServerCallback);
            mBleGattServer.clearServices();
        }
        setBluetoothService();

        Log.d(TAG, "CertME Start Peripheral Advertise Service");
        // startPeripheralScanTimer();
        if (listener != null) {
            listener.onUpdateStatus("Advertising");
        }
        try {
            Intent restartServiceIntent = new Intent(context, PeripheralAdvertiseService.class);
            PendingIntent restartServicePendingIntent = PendingIntent.getService(
                    context, 1, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT);
            AlarmManager alarmService = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarmService.set(ELAPSED_REALTIME, elapsedRealtime() + 1000,
                    restartServicePendingIntent);
        } catch (Exception e) {
            Log.e(TAG, "startServiceD() " + e.getMessage());
        }
    }

    private void cancelPeripheralScanTimer() {
        if (peripheralScanTimer != null) {
            peripheralScanTimer.cancel();
        }
    }

    private void stopPeripheralService() {
        Log.d(TAG, "CertME Stop Peripheral Advertise Service");
        Intent intent = new Intent(context, PeripheralAdvertiseService.class);
        context.stopService(intent);
    }

    private void setBluetoothService() {
        // create the Service
        BluetoothGattService heartRateService = new BluetoothGattService(ACCESS_CARD_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY);

        mSampleCharacteristic = new BluetoothGattCharacteristic(ACCESS_CARD_CHARACTERISTIC_UUID, BluetoothGattCharacteristic.PROPERTY_NOTIFY | BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_WRITE, BluetoothGattCharacteristic.PERMISSION_READ | BluetoothGattCharacteristic.PERMISSION_WRITE);
        heartRateService.addCharacteristic(mSampleCharacteristic);

        // add the Service to the Server/Peripheral
        if (mBleGattServer != null) {
            byte[] data = getReadPacket();
            mSampleCharacteristic.setValue(data);
            boolean value = mBleGattServer.addService(heartRateService);
            Log.d(TAG, "Ble addService " + value);
        }
    }

    private BluetoothGattServerCallback mGattServerCallback = new BluetoothGattServerCallback() {

        public void onConnectionStateChange(BluetoothDevice device, final int status, int newState) {
            super.onConnectionStateChange(device, status, newState);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "CertME BluetoothGatt Server success");
                if (newState == BluetoothGatt.STATE_CONNECTED) {
                    Log.d(TAG, "CertME BluetoothGatt Server STATE_CONNECTED");
                } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                    Log.d(TAG, "CertME BluetoothGatt Server STATE_DISCONNECTED");
                }
            }
        }

        @Override
        public void onNotificationSent(BluetoothDevice device, int status) {
            super.onNotificationSent(device, status);
            Log.v(TAG, "Notification sent. Status: " + status);
        }

        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic);

            if (mBleGattServer == null) {
                Log.e(TAG, "Gatt server " + "null");
                return;
            }
            byte[] fullValue = characteristic.getValue();

            //check
            if (offset > fullValue.length) {
                mBleGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, new byte[]{0});
                return;
            }

            int size = fullValue.length - offset;
            byte[] response = new byte[size];

            for (int i = offset; i < fullValue.length; i++) {
                response[i - offset] = fullValue[i];
            }
            Log.d(TAG, "CertME serviceCharacteristic.getUuid()  " + characteristic.getUuid() + " , ACCESS_CARD_CHARACTERISTIC_UUID = " + ACCESS_CARD_CHARACTERISTIC_UUID);

            if (ACCESS_CARD_CHARACTERISTIC_UUID.equals(characteristic.getUuid())) {
                Log.d(TAG, "Ble Send GATT success response");
                mBleGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, response);
                return;
            }
            mBleGattServer.sendResponse(device, requestId,
                    BluetoothGatt.GATT_FAILURE,
                    0,
                    null);
        }

        @Override
        public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value);

            String byteStr = new String(value, StandardCharsets.UTF_8);
            Gson gson = new Gson();
            WritePacket packet = gson.fromJson(byteStr, WritePacket.class);
            Log.d(TAG, "Ble Write packet data " + packet.getAccessId());

            /*mSampleCharacteristic.setValue(value);

            if (responseNeeded) {
                mBleGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, value);
            }*/
        }

        @Override
        public void onDescriptorReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattDescriptor descriptor) {
            super.onDescriptorReadRequest(device, requestId, offset, descriptor);

            if (mBleGattServer == null) {
                return;
            }
            Log.d(TAG, "Device tried to read descriptor: " + descriptor.getUuid());
            mBleGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, descriptor.getValue());
        }

        @Override
        public void onDescriptorWriteRequest(BluetoothDevice device, int requestId,
                                             BluetoothGattDescriptor descriptor, boolean preparedWrite, boolean responseNeeded,
                                             int offset,
                                             byte[] value) {
            super.onDescriptorWriteRequest(device, requestId, descriptor, preparedWrite, responseNeeded, offset, value);
            Log.v(TAG, "Descriptor Write Request " + descriptor.getUuid() + " " + Arrays.toString(value));
        }
    };

    private byte[] getReadPacket() {
        ReadPacket packet = new ReadPacket();
        packet.setData("AccessId");
        packet.setDeviceModel(Build.MODEL);
        Gson gson = new Gson();
        String jsonData = gson.toJson(packet);
        return jsonData.getBytes(StandardCharsets.UTF_8);
    }

    private void unbindService() {
        if (context != null && mServiceConnection != null) {
            Log.d(TAG, "CertME Unbind BluetoothLe Service");
            try {
                context.unbindService(mServiceConnection);
                mServiceConnection = null;
            } catch (Exception e) {
                Log.e(TAG, "Error in unbinding the service connection");
            }
        }
    }

    public void clearLeScan() {
        unbindService();
        deviceList.clear();
        deviceMapIndex = 0;
    }

    public void clearData() {
        cancelPeripheralScanTimer();
        deviceList.clear();
        mDeviceServices.clear();
        if (mBleGattServer != null) {
            mBleGattServer.close();
            mBleGattServer = null;
        }
        currentDeviceAddress = "";
        listener = null;
    }
}
