package com.certify.snap.bluetooth.printer.toshiba;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

import com.certify.snap.R;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.co.toshibatec.bcp.library.BCPControl;
import jp.co.toshibatec.bcp.library.Constants;
import jp.co.toshibatec.bcp.library.GetResources;
import jp.co.toshibatec.bcp.library.LongRef;
import jp.co.toshibatec.bcp.library.NfcReadTag;

import static com.certify.snap.bluetooth.printer.toshiba.Defines.PORTSETTING_FILE_PATH_KEYNAME;
import static com.certify.snap.bluetooth.printer.toshiba.Defines.PORTSETTING_IP_ADDRESS_KEYNAME;
import static com.certify.snap.bluetooth.printer.toshiba.Defines.PORTSETTING_PORT_MODE_KEYNAME;
import static com.certify.snap.bluetooth.printer.toshiba.Defines.PORTSETTING_PORT_NUMBER_KEYNAME;

public class ToshibaMenuActivity extends AppCompatActivity implements
        BCPControl.LIBBcpControlCallBack {

    private final String pearingNameKey = "BluetoothPareName";
    private final String TAG = "StartMenuActivity";
    private final int REQUEST_ENABLE_BLUETOOTH = 1;

    private NfcAdapter mNfcAdapter;
    protected int selected;
    private BCPControl m_bcpControl = null;
    private NfcReadTag mNfcReadTag;
    static Handler mHandler = new Handler();
    boolean deviceNotFound;
    private ArrayList<String> availableDevices = new ArrayList<String>();
    private ProgressDialog progressDialog;

    List<String> interfaceListItems = null;
    private CharSequence[] choice;
    boolean wlanEnabled = false, btEnabled = false;
    WifiManager wifiManager = null;
    private BluetoothAdapter bluetoothAdapter = null;
    private String interfaceSelectedFromNfc;                        //Interface selected based on the data in the NFC tag.
    Button btnSelectedInterface;                                    //Output Destination/interface selected button.

    public interface AsyncResponse {
        void processFinish(String output);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        wifiManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_toshiba_menu);
        btnSelectedInterface = (Button) this.findViewById(R.id.BtnSelectPair);        //initialize output destination button.
        updateSelectOutputButton();

        copyIniFile();

        mNfcReadTag = new NfcReadTag(this);
        if (m_bcpControl == null) {
            m_bcpControl = new BCPControl(this);
            GetResources.setContext(this);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mNfcReadTag != null) {
            mNfcReadTag.enableForegroundDispatch();
        }
    }

    public void onPause() {
        super.onPause();
        if (mNfcReadTag != null)
            mNfcReadTag.disableForegroundDispatch();
    }


    public void onClickBtnSelectPrinterType(View view) {

        startActivity(new Intent(this, ToshibaPrinterSettingsActivity.class));
    }

    public void onClickBtnSelectPare(View view) {

        startActivity(new Intent(this, ToshibaPortSettingsActivity.class));
    }

    public void onClickBtnSelectLavel(View view) {

        if (false == checkOutputDevice(view.getContext())) {
            return;
        }

        startActivity(new Intent(this, ToshibaLabelActivity.class));

    }

    /**
     *
     */
    private void setWiFiEnabled() {
        WifiManager wifiManager = (WifiManager) this.getApplicationContext().getSystemService(WIFI_SERVICE);
        if (false == wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
    }

    public void onClickBtnCommunicate(View view) {
        try {

            if (false == checkOutputDevice(view.getContext())) {
                return;
            }
            startActivity(new Intent(this, CommunicationActivity.class));


        } catch (Throwable th) {

        }
    }


    public void onClickBtnOption(View view) {
        try {

            if (false == checkOutputDevice(view.getContext())) {
                return;
            }
            startActivity(new Intent(this, OptionActivity.class));


        } catch (Throwable th) {

        }
    }

    public void onClickButtonCancel(View view) {
        try {
            confirmationEndDialog(this);
        } catch (Throwable th) {

        }

    }

    /**
     * @param event
     */
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                confirmationEndDialog(this);
                return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    private void updateSelectOutputButton() {

        String portSettingMode = util.getPreferences(this,
                PORTSETTING_PORT_MODE_KEYNAME);
        Button btnSelectPair = (Button) this.findViewById(R.id.BtnSelectPair);

        if (portSettingMode.equals("Bluetooth")) {
            btnSelectPair.setText(R.string.msg_OpDestBluetooth);
        } else if (portSettingMode.equals("WLAN")) {
            btnSelectPair.setText(R.string.msg_OpDestWlan);
        } else if (portSettingMode.equals("FILE")) {
            btnSelectPair.setText(R.string.msg_OpDestFile);
        } else {
            btnSelectPair.setText(R.string.msg_OpDestSelection);
        }
    }

    /**
     * @param activity
     */
    private void confirmationEndDialog(Activity activity) {
        // 終了メッセージ
        AlertDialog.Builder Alertbuilder = new AlertDialog.Builder(activity);
        Alertbuilder.setMessage(R.string.alert_AppExit);
        Alertbuilder.setCancelable(false);
        Alertbuilder.setPositiveButton(R.string.msg_Ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface di, int witchBtn) {
                        System.exit(RESULT_OK);
                    }
                });
        Alertbuilder.setNegativeButton(R.string.msg_No,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface di, int witchBtn) {

                    }
                });
        Alertbuilder.show();

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 0:

                updateSelectOutputButton();
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * @return
     */
    private boolean checkBluetoothConnectivity() {

        BluetoothAdapter bAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bAdapter == null) {
            util.showAlertDialog(this,
                    getString(R.string.msg_IsSupportBluettoth));
            return false;
        }
        if (!bAdapter.isEnabled()) {
            startActivityForResult(new Intent(
                            BluetoothAdapter.ACTION_REQUEST_ENABLE),
                    REQUEST_ENABLE_BLUETOOTH);
            return false;
        }

        String pearingNameKey = "BluetoothPareName";
        String paringName = util.getPreferences(this, pearingNameKey);
        if (paringName == null || paringName.length() == 0) {
            util.showAlertDialog(this,
                    getString(R.string.msg_SelectDestination));
            return false;
        }
        String portName = util.getBluetoothAddress(paringName);

        String prifix = "Bluetooth:";
        String macAddress = portName.substring(prifix.length(),
                portName.length());

        BluetoothDevice device = null;
        try {
            device = bAdapter.getRemoteDevice(macAddress);

        } catch (Exception e) {
            util.showAlertDialog(this, e.getLocalizedMessage());
            return false;

        }
        return true;
    }

    /**
     * WLAN設定内容確認処理
     *
     * @return true: success / false: error
     */
    private boolean checkWLANConnectivity() {

        String ipAddress = util.getPreferences(this,
                PORTSETTING_IP_ADDRESS_KEYNAME);
        String portNumber = util.getPreferences(this,
                PORTSETTING_PORT_NUMBER_KEYNAME);
        if (ipAddress.length() == 0) {

            util.showAlertDialog(this, getString(R.string.msg_IPAddrnotSet));
            return false;
        }
        if (portNumber.length() == 0) {

            util.showAlertDialog(this, getString(R.string.msg_Portnotset));
            return false;
        }
        return true;
    }

    /**
     * @return
     */
    private boolean copyIniFile() {

        String myMemotyPath = Environment.getDataDirectory().getPath()
                + "/data/" + this.getPackageName();

        File newfile = new File(myMemotyPath);
        if (newfile.exists() == false) {
            if (newfile.mkdirs()) {

            }
        }
        try {

            util.asset2file(this, "ErrMsg0.ini", myMemotyPath, "ErrMsg0.ini");
            util.asset2file(this, "ErrMsg1.ini", myMemotyPath, "ErrMsg1.ini");
            util.asset2file(this, "PRTEP2G.ini", myMemotyPath, "PRTEP2G.ini");
            util.asset2file(this, "PRTEP2GQM.ini", myMemotyPath,
                    "PRTEP2GQM.ini");
            util.asset2file(this, "PRTEP4GQM.ini", myMemotyPath,
                    "PRTEP4GQM.ini");
            util.asset2file(this, "PRTEP4T.ini", myMemotyPath, "PRTEP4T.ini");
            util.asset2file(this, "PRTEV4TT.ini", myMemotyPath, "PRTEV4TT.ini");
            util.asset2file(this, "PRTEV4TG.ini", myMemotyPath, "PRTEV4TG.ini");
            util.asset2file(this, "PRTLV4TT.ini", myMemotyPath, "PRTLV4TT.ini");
            util.asset2file(this, "PRTLV4TG.ini", myMemotyPath, "PRTLV4TG.ini");
            util.asset2file(this, "PRTFP3DGQM.ini", myMemotyPath, "PRTFP3DGQM.ini");
            //ADD 03/12/2018
            util.asset2file(this, "PRTBA400TG.ini", myMemotyPath, "PRTBA400TG.ini");
            util.asset2file(this, "PRTBA400TT.ini", myMemotyPath, "PRTBA400TT.ini");
            util.asset2file(this, "PrtList.ini", myMemotyPath, "PrtList.ini");
            util.asset2file(this, "resource.xml", myMemotyPath, "resource.xml");
            util.asset2file(this, "PRTFP2DG.ini", myMemotyPath, "PRTFP2DG.ini");

            util.asset2file(this, "PRTFV4D.ini", myMemotyPath, "PRTFV4D.ini");

        } catch (Exception e) {

            util.showAlertDialog(this,
                    getString(R.string.msg_CopyPrinterConfigFile));
            return false;
        }

        return true;
    }

    private boolean checkOutputDevice(Context conn) {

        String portSettingPortMode = util.getPreferences(this,
                PORTSETTING_PORT_MODE_KEYNAME);

        if (portSettingPortMode.equals("Bluetooth")) {
            if (false == this.checkBluetoothConnectivity()) {
                return false;
            }
        } else if (portSettingPortMode.equals("WLAN")) {
            if (false == checkWLANConnectivity()) {
                return false;
            }

            setWiFiEnabled();

        } else if (portSettingPortMode.equals("FILE")) {
            String fileFullPath = util.getPreferences(this,
                    PORTSETTING_FILE_PATH_KEYNAME);
            if (fileFullPath.length() == 0) {
                String filePath = Environment.getExternalStorageDirectory()
                        .getPath() + "/PrintImageFile.txt";
                util.setPreferences(this, PORTSETTING_FILE_PATH_KEYNAME,
                        filePath);
            }
        } else {
            util.showAlertDialog(conn, getString(R.string.msg_OpDest));
            return false;
        }
        return true;

    }

    /**
     * To handle the NDEF action intent
     */
    @Override
    public void onNewIntent(final Intent intent) {
        if (mNfcReadTag != null) {
            if (intent.getAction().equals(NfcAdapter.ACTION_NDEF_DISCOVERED)) {
                progressDialog = new ProgressDialog(ToshibaMenuActivity.this);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.setCancelable(false);
                progressDialog.setMessage(this.getString(R.string.wait));
                progressDialog.show();
                LongRef result = new LongRef(0);

                if (m_bcpControl.NfcTagRead(result, this, intent)) {
                    if (m_bcpControl.getTagData().size() > 0) {
                        HashMap<String, String> hm = m_bcpControl.getTagData();
                        for (Map.Entry<String, String> s : hm.entrySet()) {
                            if (s.getKey().contains(Constants.NFC_SSID_PREFIX)) {
                                wlanEnabled = true;
                            }
                            if (s.getKey().contains(Constants.NFC_MAC_PREFIX)) {
                                wlanEnabled = true;
                            }
                            if (s.getKey().contains(Constants.NFC_BD_PREFIX)) {
                                btEnabled = true;
                            }
                        }
                        nfcConnectivity(intent, result);
                    } else {

                    }
                }
            }
        }
        super.onNewIntent(intent);
    }

    private void nfcConnectivity(Intent intent, LongRef result) {
        interfaceListItems = new ArrayList<String>();
        if (btEnabled)
            interfaceListItems.add("Bluetooth");
        if (wlanEnabled)
            interfaceListItems.add("Wifi");
        if (btEnabled == true && wlanEnabled == true) {
            choice = interfaceListItems.toArray(new CharSequence[interfaceListItems.size()]);
            initializeNfcInterfaceSpinner(intent);
        } else if (btEnabled == true && wlanEnabled == false) {
            interfaceSelectedFromNfc = getString(R.string.msg_OpDestBluetooth);        //selected interface is bluetooth.
            nfcCheckBluetooth(result, ToshibaMenuActivity.this);
        } else {
            interfaceSelectedFromNfc = getString(R.string.msg_OpDestWlan);                //selected interface is Wifi.
            nfcCheckWifi(result, ToshibaMenuActivity.this);
        }
        interfaceListItems = null;
        btEnabled = false;
        wlanEnabled = false;
    }

    @Override
    public void BcpControl_OnStatus(String printerStatus, long result) {

        progressDialog.dismiss();
        if (m_bcpControl.getTagData().get(Constants.NFC_BD_PREFIX) != null) {
            String bdaddr = m_bcpControl.getTagData().get(Constants.NFC_BD_PREFIX).toString();
            util.setPreferences(ToshibaMenuActivity.this, pearingNameKey, bdaddr);
        }
        m_bcpControl.getTagData().clear();
        Button btnSelectedInterface = (Button) this.findViewById(R.id.BtnSelectPair);
        switch (new Long(result).intValue()) {
            case 1000:
                util.setPreferences(this, PORTSETTING_IP_ADDRESS_KEYNAME, printerStatus);    //Set IP address
                util.setPreferences(this, PORTSETTING_PORT_NUMBER_KEYNAME, "9100");            // Set Port Number
                break;
            case 0:
                //Successful in pairing.
                util.showAlertDialog(this, getString(R.string.msg_success));
                btnSelectedInterface.setText(interfaceSelectedFromNfc);
                if (interfaceSelectedFromNfc.contains("Bluetooth")) {
                    saveConnectedBluetoothDeviceToPreferences();
                }
                break;
            case 0x800A0FA0:
                //Device doesn't support NFC
                util.showAlertDialog(this, getString(R.string.nfcNotSupported));
                break;
            case 0x800A0FA1:
                //Tag reading failed.
                util.showAlertDialog(this, getString(R.string.msg_TagReadTimeOut));
                break;
            case 0x800A0FA2:
                //It failed in the interface connection.
                util.showAlertDialog(this, getString(R.string.msg_FailedIninterfaceConn));
                break;
            case 0x800A0FA3:
                //Given SSID doesn't exist
                util.showAlertDialog(this, getString(R.string.deviceNotAvailable));
                break;
            case 0x800A0FA4:
                //Invalid SSID/MAC/BD Address
                util.showAlertDialog(this, getString(R.string.invalidSsidOrMacOrBDError));
                break;
            case 0x800A07D2:
                //Processing has been canceled.
                util.showAlertDialog(this, getString(R.string.processCancelError));
                break;
            default:
                break;
        }
    }

    // Broadcast receiver to receive Bluetooth Bonded State change.
    private final BroadcastReceiver mPairReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                final int state = intent
                        .getIntExtra(BluetoothDevice.EXTRA_BOND_STATE,
                                BluetoothDevice.ERROR);
                final int prevState = intent.getIntExtra(
                        BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE,
                        BluetoothDevice.ERROR);
                if (state == BluetoothDevice.BOND_BONDED
                        && prevState == BluetoothDevice.BOND_BONDING) {
                    progressDialog.dismiss();
                    util.showAlertDialog(ToshibaMenuActivity.this, getString(R.string.msg_success));
                    btnSelectedInterface.setText(interfaceSelectedFromNfc);
                    String bdAddress = m_bcpControl.getTagData().get(Constants.NFC_BD_PREFIX).toString();
                    util.setPreferences(ToshibaMenuActivity.this, pearingNameKey, bdAddress);
                    saveConnectedBluetoothDeviceToPreferences();
                    //m_bcpControl.tagData = null;

                } else if (state == BluetoothDevice.BOND_NONE) {
                    progressDialog.dismiss();
                    util.showAlertDialog(ToshibaMenuActivity.this, getString(R.string.msg_FailedIninterfaceConn));
                    //	m_bcpControl.tagData = null;
                }
            }
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter.STATE_OFF) {
                    progressDialog.dismiss();
                    util.showAlertDialog(ToshibaMenuActivity.this, getString(R.string.msg_FailedIninterfaceConn));
                    //m_bcpControl.tagData = null;
                }
                progressDialog.dismiss();
            }
        }

    };

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent
                        .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Add the name and address to an array adapter.
                Log.d(TAG,
                        "mReceiver" + device.getName() + "\n"
                                + device.getAddress());
                availableDevices.add(device.getAddress());
                for (int i = 0; i < availableDevices.size(); i++) {
                    Log.d(TAG, "BroadcastReceiver : " + m_bcpControl.getTagData().get(Constants.NFC_BD_PREFIX) + ":" + availableDevices.get(i).toString());
                    if (m_bcpControl.getTagData()
                            .get(Constants.NFC_BD_PREFIX).equals(
                                    availableDevices.get(i).toString()))
                        deviceNotFound = true;
                }
            }
        }
    };

    private final BroadcastReceiver bluetoothOffReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter.STATE_OFF) {

                }
            }
        }
    };

    protected void onStart() {
        super.onStart();
        IntentFilter mIntent = new IntentFilter(
                BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mPairReceiver, mIntent);

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);

        IntentFilter filterchange = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(bluetoothOffReceiver, filterchange);

    }

    ;

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mPairReceiver);
        unregisterReceiver(mReceiver);
        unregisterReceiver(bluetoothOffReceiver);
    }

    /**
     * Initialize interface selection spinner, if NFC Tag has SSID, MAC Address and BD address.
     *
     * @param ndefIntent
     */
    private void initializeNfcInterfaceSpinner(final Intent ndefIntent) {

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(getString(R.string.msg_InterfaceChoice));
        alert.setSingleChoiceItems(choice, 0,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selected = which;

                    }
                });
        alert.setPositiveButton(getString(R.string.dialogOk), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                LongRef result = new LongRef(0);
                Log.d(TAG, "initializeSpinner : onClick "
                        + which);

                if (choice[selected].equals(Constants.WIFI)) {
                    Log.d(TAG, "Selected : WiFi");
                    dialog.dismiss();
                    selected = 0;
                    nfcCheckWifi(result, ToshibaMenuActivity.this);
                    util.setPreferences(ToshibaMenuActivity.this, PORTSETTING_PORT_MODE_KEYNAME, "WLAN");
                    interfaceSelectedFromNfc = getString(R.string.msg_OpDestWlan);                                //selected interface is Wifi

                } else if (choice[selected]
                        .equals(Constants.BLUETOOTH)) {
                    Log.d(TAG, "Selected : Bluetooth");
                    dialog.dismiss();
                    selected = 0;
                    nfcCheckBluetooth(result, ToshibaMenuActivity.this);
                    util.setPreferences(ToshibaMenuActivity.this, PORTSETTING_PORT_MODE_KEYNAME, "Bluetooth");
                    interfaceSelectedFromNfc = getString(R.string.msg_OpDestBluetooth);                            //selected interface is Wifi
                }
            }

        }).setNegativeButton(getString(R.string.dialogCancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                progressDialog.dismiss();
                util.showAlertDialog(ToshibaMenuActivity.this, getString(R.string.msg_Cancel));
            }
        });
        alert.setCancelable(false);
        alert.show();
    }

    private void nfcCheckWifi(final LongRef result, Context con) {
        if (wifiManager != null) {
            if (!wifiManager.isWifiEnabled())                //If Wifi is not enabled.
            {
                // Confirmation Dialog to Turn On Wifi.
                util.comfirmDialog(this, getString(R.string.confirm_TurnOnWIFI), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        wifiManager.setWifiEnabled(true);
                        //Connect to the SSID read from the NFC Tag.
                        util.comfirmDialog(ToshibaMenuActivity.this, getString(R.string.msg_ConnectToSSID) + " " + m_bcpControl.getTagData().get(Constants.NFC_SSID_PREFIX),
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        m_bcpControl.CheckWifiConnection(result, ToshibaMenuActivity.this);
                                    }
                                });
                    }
                }, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        progressDialog.dismiss();
                        util.showAlertDialog(ToshibaMenuActivity.this, getString(R.string.msg_Cancel));
                    }
                });
            } else {
                //If Wifi is already enabled.
                //Connect to the SSID read from the NFC Tag.
                util.comfirmDialog(ToshibaMenuActivity.this, getString(R.string.msg_ConnectToSSID) + " " + m_bcpControl.getTagData().get(Constants.NFC_SSID_PREFIX),
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                m_bcpControl.CheckWifiConnection(result, ToshibaMenuActivity.this);
                            }
                        });
            }
        } else {
            util.showAlertDialog(this, getString(R.string.wifiError));
        }
    }

    private void nfcCheckBluetooth(final LongRef result, Context con) {
        if (bluetoothAdapter != null) {

            if (!bluetoothAdapter.isEnabled()) {
                //Confirmation dialog to Turn On Bluetooth
                util.comfirmDialog(this, getString(R.string.confirm_TurnOnBT), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        bluetoothAdapter.enable();
                        //Confirmation dialog to connect to the BD address read from the tag.
                        util.comfirmDialog(ToshibaMenuActivity.this, getString(R.string.msg_connBtDevice) + " " + m_bcpControl.getTagData().get(Constants.NFC_BD_PREFIX),
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        m_bcpControl.CheckBluetoothConnection(result, ToshibaMenuActivity.this);
                                    }
                                });

                    }
                }, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        progressDialog.dismiss();
                        util.showAlertDialog(ToshibaMenuActivity.this, getString(R.string.msg_Cancel));
                    }
                });
            } else {
                //Confirmation dialog to connect to the BD address read from the tag.
                util.comfirmDialog(ToshibaMenuActivity.this, getString(R.string.msg_connBtDevice) + " " + m_bcpControl.getTagData().get(Constants.NFC_BD_PREFIX),
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                m_bcpControl.CheckBluetoothConnection(result, ToshibaMenuActivity.this);
                            }
                        });
            }
        } else {
            util.showAlertDialog(this, getString(R.string.bluetoothError));
        }
    }

    //Save the Connected Bluetooth Device to preference.
    private void saveConnectedBluetoothDeviceToPreferences() {
        String pairedBluetoothDeviceName = util.getPreferences(this, pearingNameKey);
        BluetoothDevice connectedBluetoothDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(pairedBluetoothDeviceName);
        util.setPreferences(this, pearingNameKey, connectedBluetoothDevice.getName() + " (" + connectedBluetoothDevice.getAddress() + ")");
    }
}