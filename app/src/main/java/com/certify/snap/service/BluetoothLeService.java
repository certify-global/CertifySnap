package com.certify.snap.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.certify.snap.common.Constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class BluetoothLeService extends Service {

    private final static String TAG = BluetoothLeService.class.getSimpleName();

    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<>();

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    public final static String EXTRA_DATA = "EXTRA_DATA";
    private Context mContext;

    private int mConnectionState = STATE_DISCONNECTED;
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public final static String ACTION_DATA_AVAILABLE = "ACTION_DATA_AVAILABLE";
    public final static String ACTION_GATT_CONNECTED = "ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_GATT_BLUETOOTH_DATA = "ACTION_GATT_CONNECTED_BLUETOOTH_DATA";

    /**
     * @Callback
     * Gatt Connection State Change CallBack & Send Broadcast (Updated State)
     */
    //region
    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

        // listen connection state changed (connected & disconnected)
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.e(TAG, "onConnectionStateChanged");

            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) { // STATE : connected
                mConnectionState = STATE_CONNECTED;
                Log.d(TAG, "Connected to device, Requesting for MTU");

                //disconnect();
                gatt.requestMtu(510);     //Device has limit of 20 bytes, Request MTU to read larger data from remote device

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) { // STATE : disconnected
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;

                broadcastUpdate(intentAction); // broadcast Gatt connection state : disconnected

                Log.e(TAG, "Disconnected from GATT server.");
            }
        }

        // listen Gatt attribute service discovered
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.d(TAG, "Ble services discovered ");
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Ble services discovered Success");
                //getGattServices(getSupportedGattServices());
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED); // broadcast Gatt connection state : discovered
            } else {
                Log.e(TAG, "Ble onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.d(TAG, "onCharacteristicRead ");
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "onCharacteristicRead Success");
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            } else {
                Log.d(TAG, "onCharacteristicRead GATT_FAILURE");
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);

            Log.d(TAG, "CertME onMTU request Status " + status);
            String intentAction = ACTION_GATT_CONNECTED;
            broadcastUpdate(intentAction); // broadcast Gatt connection state : connected

            Log.d(TAG, "Attempting to start service discovery:" + mBluetoothGatt.discoverServices());
        }
    };

    /**
     * @ServiceBinder
     * Service Local Binding & Initialize & Connect
     */
    //region
    private IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        close();
        return super.onUnbind(intent);
    }

    // initialize bluetooth manager
    public boolean initialize(Context context) {
        Log.e(TAG, "initialize");
        mContext = context;

        mBluetoothDeviceAddress = "";
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }
        return true;
    }

    // try connect bluetooth
    public boolean connect(final String address) {
        if (mConnectionState == STATE_CONNECTING) {
            Log.d(TAG, "Device connecting ");
            return false;
        }
        if (mConnectionState == STATE_CONNECTED) {
            //Log.d(TAG, "Device connected ");
            //disconnect();
            return false;
        }

        // check bluetooth adapter & ble address
        if (mBluetoothAdapter == null || address == null) {
            Log.e(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // check ble address & ble Gatt
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress) && mBluetoothGatt != null) {
            Log.e(TAG, "Ble Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                Log.d(TAG, "Ble Connecting ");
                mConnectionState = STATE_CONNECTING;
                return true;
            } else {
                Log.d(TAG, "Ble Connecting false");
                return false;
            }
        }

        // get ble remote device
        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);

        // check device validate
        if (device == null) {
            Log.e(TAG, "Ble Device not found.  Unable to connect.");
            return false;
        }

        // try connect to Gatt server
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        Log.e(TAG, "Ble Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;

        return true;
    }

    // try disconnect bluetooth
    public void disconnect() {
        Log.e(TAG, "disconnect");

        // check bluetooth adapter & Gatt server
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.e(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    // close Gatt socket
    public void close() {
        Log.e(TAG, "close");

        try {
            if (mBluetoothGatt == null)
                return;

            disconnect();
            mBluetoothGatt.close();
            mBluetoothGatt = null;
        }
        catch (Exception e){
        }
    }
    //endregion

    /**
     * get supported Gatt services
     * @return
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null)
            return null;
        return mBluetoothGatt.getServices();
    }

    /**
     * get Gatt services and save as arrayList
     * @param gattServices
     */
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> getGattServices(List<BluetoothGattService> gattServices) {
        // check Gatt service
        if (gattServices == null) return null;

        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData = new ArrayList<>();
        mGattCharacteristics = new ArrayList<>();

        // loops through available GATT services
        /*for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<>();
            String uuid = gattService.getUuid().toString();
            currentServiceData.put("LIST_NAME", BluetoothGattAttributes.lookup(uuid, getString(R.string.unknown_service)));
            currentServiceData.put("LIST_UUID", uuid);
            gattServiceData.add(currentServiceData);
            ArrayList<HashMap<String, String>> gattCharacteristicGroupData = new ArrayList<>();
            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas = new ArrayList<>();

            // loops through available characteristics
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<>();
                uuid = gattCharacteristic.getUuid().toString();
                currentCharaData.put("LIST_NAME", BluetoothGattAttributes.lookup(uuid, getString(R.string.unknown_characteristic)));
                currentCharaData.put("LIST_UUID", uuid);
                gattCharacteristicGroupData.add(currentCharaData);
                Log.e(TAG, "LIST_NAME : " + BluetoothGattAttributes.lookup(uuid, getString(R.string.unknown_characteristic)) + ", LIST_UUID : " + uuid);
            }

            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }*/
        return mGattCharacteristics;
    }

    /**
     * return a Gatt characteristic
     * @param CharName
     * @return
     */
    public BluetoothGattCharacteristic getGattCharacteristic(String CharName) {

        BluetoothGattCharacteristic characteristic = null;
        for (int i = 0; i < mGattCharacteristics.size(); i++) {
            for (int k = 0; k < mGattCharacteristics.get(i).size(); k++) {
                if (UUID.fromString(CharName).equals(mGattCharacteristics.get(i).get(k).getUuid())) {
                    characteristic = mGattCharacteristics.get(i).get(k);
                    break;
                }
            }
        }
        return characteristic;
    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {

        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }

        mBluetoothGatt.readCharacteristic(characteristic);
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled If true, enable notification.  False otherwise.
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enabled) {

        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.d(TAG, "CertME SetCharacteristicNotification Error, Adapter or Gatt not initialized");
            return;
        }

        Log.d(TAG, "CertME Set CharacteristicNotification");
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);

        // This is specific to Heart Rate Measurement.
        /*
        if (HEART_RATE_MEASUREMENT_UUID.toString().equals(characteristic.getUuid().toString())) {
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIGURATION_UUID);
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
        }
        */
    }

    /*// send characteristic data
    public void sendDataCharacteristic(BluetoothGattCharacteristic characteristic, byte[] value) {
        final BluetoothGattCharacteristic mBluetoothGattCharacteristic = characteristic;
        if(UUID.fromString(BluetoothGattAttributes.LED_CHARACTERISTIC).equals(characteristic.getUuid())) {
            mBluetoothGattCharacteristic.setValue(value);
            mBluetoothGatt.writeCharacteristic(mBluetoothGattCharacteristic);
        }
    }*/

    private void broadcastUpdate(final String action) {
        Log.e(TAG, "broadcastUpdate");

        Intent intent = new Intent(action);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        //sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic) {
        Intent intent = new Intent(action);
        Log.i(TAG,"characteristic.getUuid()  "+characteristic.getUuid() +" , CHARACTERISTIC_UUID = "+ Constants.CHARACTERISTIC_UUID );

        if (Constants.CHARACTERISTIC_UUID.equals(characteristic.getUuid())) {
            int flag = characteristic.getProperties();
            int format = -1;
            Log.d(TAG, "Value: " + Arrays.toString(characteristic.getValue()));
            if ((flag & 0x01) != 0) {
                format = BluetoothGattCharacteristic.FORMAT_UINT16;
                Log.d(TAG, "data format UINT16.");
            } else {
                format = BluetoothGattCharacteristic.FORMAT_UINT8;
                Log.d(TAG, "data format UINT8.");
            }
            intent.putExtra(EXTRA_DATA, characteristic.getValue());
        }
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

}
