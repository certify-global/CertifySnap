package com.certify.snap.controller;

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
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.util.Log;

import com.certify.snap.common.Constants;
import com.certify.snap.service.BluetoothLeService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.Context.BIND_AUTO_CREATE;
import static com.certify.snap.common.Constants.CHARACTERISTIC_UUID;

public class BleController1 {
    private static final String TAG = BleController1.class.getSimpleName();
    private static BleController1 instance = null;
    private Context context;

    private BluetoothLeScanner bluetoothLeScanner;
    private BluetoothGattServer mBleGattServer;
    private BluetoothLeService mBluetoothLeService;
    private ServiceConnection mServiceConnection;
    private BluetoothManager mBluetoothManager;
    private BluetoothGattCharacteristic mCharacteristic;
    private BluetoothGattCharacteristic mSampleCharacteristic;
    private BroadcastReceiver mGattUpdateReceiver;
    private String currentDeviceAddress = "";

    private ArrayList<BluetoothDevice> deviceList = new ArrayList<>();
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mDeviceServices = new ArrayList<>();
    private BleCallbackListener listener = null;
    private Timer centralScanTimer;
    private Timer peripheralScanTimer;
    private String deviceSID = "";
    private int deviceMapIndex = 0;
    public static final long CENTRAL_SCAN_TIME = 30000;
    private static final long PERIPHERAL_ADVERTISE_TIME = 20000;

    public interface BleCallbackListener {
        void onScanResultsUpdate();
        void onScanFailed(int errorCode);
        void onStartLeScan();
        void onUpdateStatus(String status);
    }

    public static BleController1 getInstance() {
        if (instance == null) {
            instance = new BleController1();
        }
        return instance;
    }

    public void init(Context context) {
        this.context = context;
        //deviceSID = Util.getSharedPreferences(context).getString(GlobalParameters.KEY_SID, "");
        mBluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        if (mBluetoothManager != null) {
            BluetoothAdapter bluetoothAdapter = mBluetoothManager.getAdapter();
            bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
            mBluetoothManager = (BluetoothManager)context.getSystemService(Context.BLUETOOTH_SERVICE);
        }
        deviceList.clear();
        registerGattReceiver();
    }

    public void setBleListener(BleCallbackListener callbackListener) {
        this.listener = callbackListener;
    }

    public ServiceConnection getServiceConnection() {
        return mServiceConnection;
    }

    public BroadcastReceiver getGattUpdateReceiver() {
        return mGattUpdateReceiver;
    }

    public ArrayList<BluetoothDevice> getDeviceList() {
        return deviceList;
    }

    public void startLeScan(final boolean enable) {
        if (enable) {
            Log.d(TAG, "CertME Start Ble Scan");
            bluetoothLeScanner.startScan(buildScanFilters(), buildScanSettings(), mScanCallback);
            startCentralScanTimer();
        } else {
            bluetoothLeScanner.stopScan(mScanCallback);
        }
    }

    /**
     * Ble Scan result callback
     */
    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            List<ScanResult> scanResultList = new ArrayList<>();
            scanResultList.add(result);
            processResult(scanResultList);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            processResult(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            if (listener != null) {
                listener.onScanFailed(errorCode);
            }
        }
    };

    public void stopBleScan() {
        if (bluetoothLeScanner != null) {
            Log.d(TAG, "CertME Stop Ble Scan");
            bluetoothLeScanner.stopScan(mScanCallback);
        }
    }

    private void startCentralScanTimer() {
        /*centralScanTimer = new Timer();
        centralScanTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                clearLeScan();
                startPeripheralAdvertiseService(context);
            }
        }, CENTRAL_SCAN_TIME);*/
        if (listener != null) {
            listener.onStartLeScan();
        }
    }

    private void cancelCentralScanTimer() {
        if (centralScanTimer != null) {
            centralScanTimer.cancel();
        }
    }

    private void processResult(List<ScanResult> scanResultList) {
        if (scanResultList != null && scanResultList.size() > 0) {
            for (ScanResult scanResult : scanResultList) {
                if (isAdvertiseUuidPresent(scanResult)) {
                    processScanResult(scanResult);
                    BluetoothDevice device = scanResult.getDevice();
                    Log.d(TAG, "CertME Bluetooth device address " + scanResult.getDevice().getAddress());
                    if (!deviceList.contains(device)) {
                        Log.d(TAG, "CertME device address " + device.getAddress());
                        deviceList.add(scanResult.getDevice());
                        mBluetoothLeService.connect(device.getAddress());
                    }
                }
            }
            Log.d(TAG, "CertME Devices list " + deviceList.size());
        }
    }

    private boolean isAdvertiseUuidPresent(ScanResult scanResult) {
        boolean result = false;
        if (scanResult.getScanRecord() != null) {
            List<ParcelUuid> serviceUuidList = scanResult.getScanRecord().getServiceUuids();
            if (serviceUuidList != null && serviceUuidList.size() > 0) {
                for (int i = 0; i < serviceUuidList.size(); i++) {
                    ParcelUuid serviceUuid = serviceUuidList.get(i);
                    ParcelUuid advertiseUuid = ParcelUuid.fromString(Constants.UUID_VALUE);
                    if (serviceUuid.equals(advertiseUuid)) {
                        result = true;
                        break;
                    }
                }
            }
        }
        return result;
    }

    private void processScanResult(ScanResult scanResult) {
        BluetoothDevice device = scanResult.getDevice();
    }

    public void initServiceConnection() {
        mServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder service) {
                Log.d(TAG, "CertME onServiceConnected");
                mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
                if (!mBluetoothLeService.initialize()) {
                    Log.e(TAG, "Unable to initialize Bluetooth");
                }
                //initiateDeviceConnection();
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                Log.d(TAG, "CertME onServiceDisconnected");
                mBluetoothLeService = null;
            }
        };
    }

    private void initiateDeviceConnection() {
        /*if (!bleDeviceMap.isEmpty()) {
            List<String> keyList = new ArrayList<>(bleDeviceMap.keySet());
            if (deviceMapIndex >= keyList.size()) {
                if (listener != null) {
                    listener.onUpdateStatus("Scanning");
                }
                return;
            }
            currentDeviceAddress = keyList.get(deviceMapIndex);
            Log.d(TAG, "CertME initiate Connection " + currentDeviceAddress);
            currentConnectedBeacon = bleDeviceMap.get(currentDeviceAddress);
            mBluetoothLeService.connect(currentDeviceAddress);
        }*/
    }

    public void registerGattReceiver() {
        mGattUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action == null) {
                    return;
                }

                switch (intent.getAction()) {
                    case BluetoothLeService.ACTION_GATT_CONNECTED: {
                        Log.d(TAG, "CertME Device connected");
                        if (listener != null) {
                            listener.onUpdateStatus("Connected");
                        }
                    }
                    break;

                    case BluetoothLeService.ACTION_GATT_DISCONNECTED:
                        Log.d(TAG, "CertME Device disconnected");
                        break;

                    case BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED:
                        Log.d(TAG, "CertME Device services discovered");
                        setGattServices(mBluetoothLeService.getSupportedGattServices());
                        registerCharacteristic();
                        break;

                    case BluetoothLeService.ACTION_DATA_AVAILABLE:
                        /*String contactDeviceUId = intent.getStringExtra(CentralService.EXTRA_DATA);
                        Log.d(TAG, "CertME ACTION_DATA_AVAILABLE " + "  " + contactDeviceUId);

                        if (contactDeviceUId != null && contactDeviceUId.contains(",")) {
                            String[] data = contactDeviceUId.split(",");
                            Util.sendBeaconData(context, deviceSID, data[0], currentConnectedBeacon);
                            if (listener != null) {
                                listener.onUpdateStatus("Data complete");
                            }
                            initiateNextConnection();
                        }*/
                        /*byte[] byteData = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                        if (byteData != null) {
                            Log.d(TAG, "CertME ACTION_DATA_AVAILABLE ");
                            String byteStr = new String(byteData, StandardCharsets.UTF_8);
                            Gson gson = new Gson();
                            WritePacket packet = gson.fromJson(byteStr, WritePacket.class);
                            Util.sendBleDeviceData(context, deviceSID, packet.getId(),packet.getModel(), currentConnectedBeacon);
                            if (listener != null) {
                                listener.onUpdateStatus("Data complete");
                            }
                            initiateNextConnection();
                        }*/
                        break;
                }
            }
        };
    }

    private void setGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) {
            return;
        }
        mDeviceServices.clear();

        // Loops through available GATT Services from the connected device
        for (BluetoothGattService gattService : gattServices) {
            ArrayList<BluetoothGattCharacteristic> characteristic = new ArrayList<>();
            characteristic.addAll(gattService.getCharacteristics()); // each GATT Service can have multiple characteristic
            mDeviceServices.add(characteristic);
        }
    }

    private void registerCharacteristic() {
        BluetoothGattCharacteristic characteristic = null;
        if (mDeviceServices != null) {
            /* iterate all the Services the connected device offer.
            a Service is a collection of Characteristic.
             */
            for (ArrayList<BluetoothGattCharacteristic> service : mDeviceServices) {
                // iterate all the Characteristic of the Service
                for (BluetoothGattCharacteristic serviceCharacteristic : service) {
                    Log.i(TAG,"CertME serviceCharacteristic.getUuid()  "+serviceCharacteristic.getUuid() +" , LOCATION_CHARACTERISTIC_UUID = "+CHARACTERISTIC_UUID );
                    if (serviceCharacteristic.getUuid().equals(CHARACTERISTIC_UUID)) {
                        characteristic = serviceCharacteristic;
                        mCharacteristic = characteristic;
                    }
                }
            }

            if (characteristic != null) {
                Log.d(TAG, "CertME Read Characteristics");
                mBluetoothLeService.readCharacteristic(characteristic);
                mBluetoothLeService.setCharacteristicNotification(characteristic, true);
            }
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
            Log.d(TAG, "CertME serviceCharacteristic.getUuid()  " + characteristic.getUuid() + " , CHARACTERISTIC_UUID = " + CHARACTERISTIC_UUID);

            if (CHARACTERISTIC_UUID.equals(characteristic.getUuid())) {
                Log.d(TAG, "CertME Send GATT success response");
                mBleGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, response);
                mBleGattServer.close();
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
            mSampleCharacteristic.setValue(value);

            if (responseNeeded) {
                mBleGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, value);
            }
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

    private void initiateNextConnection() {
        deviceMapIndex++;
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                disconnectDevice();
                initiateDeviceConnection();
            }
        }, 2000);
    }

    private void disconnectDevice() {
        if (mBluetoothLeService != null) {
            mBluetoothLeService.disconnect();
        }
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

    public static List<ScanFilter> buildScanFilters() {
        List<ScanFilter> scanFilters = new ArrayList<>();

        ScanFilter.Builder builder = new ScanFilter.Builder();
        builder.setServiceUuid(ParcelUuid.fromString(Constants.UUID_VALUE.toString()));
        builder.setServiceUuid(ParcelUuid.fromString(Constants.UUID_VALUE.toLowerCase()));
        scanFilters.add(builder.build());

        return scanFilters;
    }

    /**
     * Return a {@link ScanSettings} object set to use low power (to preserve battery life).
     */
    public static ScanSettings buildScanSettings() {
        ScanSettings.Builder builder = new ScanSettings.Builder();
        builder.setScanMode(ScanSettings.SCAN_MODE_LOW_POWER);
        builder.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);
        return builder.build();
    }

    public void stopScan() {
        cancelCentralScanTimer();
        stopBleScan();
        clearLeScan();
    }

    public void clearLeScan() {
        stopBleScan();
        disconnectDevice();
        unbindService();
        deviceList.clear();
        deviceMapIndex = 0;
    }

    public void clearData() {
        cancelCentralScanTimer();
        deviceList.clear();
        mDeviceServices.clear();
        if (mBleGattServer != null) {
            mBleGattServer.close();
            mBleGattServer = null;
        }
        disconnectDevice();
        currentDeviceAddress = "";
        listener = null;
    }
}
