package com.certify.snap.activity;

import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.brother.ptouch.sdk.NetPrinter;
import com.brother.ptouch.sdk.Printer;
import com.brother.ptouch.sdk.PrinterInfo;
import com.certify.snap.R;
import com.certify.snap.common.IpAddressValidator;
import com.certify.snap.common.Util;
import com.certify.snap.printer.Common;
import com.certify.snap.view.PrinterMsgDialog;

import java.util.ArrayList;

public class PrinterWifiSettingsActivity extends ListActivity {

    // information
    private final PrinterMsgDialog msgDialog = new PrinterMsgDialog(this);
    private String modelName; // the print model name.
    private NetPrinter[] mNetPrinter; // array of storing Printer informations.
    private ArrayList<String> mItems = null; // List of storing the printer's
    private SearchThread searchPrinter;
    private EditText printerIp, printerMac;
    private TextView printerIpError, printerMacError;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get the modelName
        final Bundle extras = getIntent().getExtras();
        modelName = extras.getString(Common.MODEL_NAME);
        setContentView(R.layout.activity_printer_wifi_settings);

        printerIp = findViewById(R.id.edittext_printer_ip);
        printerMac = findViewById(R.id.edittext_printer_mac);
        printerIpError = findViewById(R.id.ip_input_error);
        printerMacError = findViewById(R.id.mac_input_error);

        setValues();

        Button btnRefresh = (Button) findViewById(R.id.btnRefresh);
        btnRefresh.setOnClickListener(view -> {
            Util.hideSoftKeyboard(this);
            setDialog();
            // launch printer searching thread
            searchPrinter = new SearchThread();
            searchPrinter.start();
        });

        Button btPrinterSettings = (Button) findViewById(R.id.btPrinterSettings);
        btPrinterSettings.setOnClickListener(view -> {
            settingsButtonOnClick();
        });

        Button addPrinter = (Button) findViewById(R.id.printer_add);
        addPrinter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (printerIp.getText().toString().isEmpty() || !IpAddressValidator.isValid(printerIp.getText().toString())) {
                    printerIpError.setText("Please input a valid Ip address");
                    return;
                }
                if (printerMac.getText().toString().isEmpty()) {
                    printerMacError.setText("Please input a valid Mac address");
                    return;
                }
                updatePrinterSettings();
            }
        });

        // show searching dialog
        //setDialog();

        // launch printer searching thread
       /* searchPrinter = new SearchThread();
        searchPrinter.start();*/

        this.setTitle(R.string.netPrinterListTitle_label);
    }

    @Override
    protected void onStart() {
        super.onStart();
        this.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    /**
     * Called when [Settings] button is tapped
     */
    private void settingsButtonOnClick() {
        Intent wifiSettings = new Intent(
                android.provider.Settings.ACTION_WIFI_SETTINGS);
        startActivityForResult(wifiSettings, Common.ACTION_WIFI_SETTINGS);
    }

    /**
     * Called when [Refresh] button is tapped
     */
    private void refreshButtonOnClick() {
        setDialog();
        searchPrinter = new SearchThread();
        searchPrinter.start();
    }

    /**
     * Called when the Settings activity exits
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == Common.ACTION_WIFI_SETTINGS) {
            setDialog();
            searchPrinter = new SearchThread();
            searchPrinter.start();
        }
    }

    /**
     * This method will be called when an item in the list is selected.
     *
     * @return
     */
    @Override
    protected void onListItemClick(ListView listView, View view, int position,
                                   long id) {

        final String item = (String) getListAdapter().getItem(position);
        if (!item.equalsIgnoreCase(getString(R.string.no_network_device))) {
            // send the selected printer info. to Settings Activity and close
            // the current Activity
            final Intent settings = new Intent(this, PrinterWifiBTSettingsActivity.class);
            settings.putExtra("ipAddress", mNetPrinter[position].ipAddress);
            settings.putExtra("macAddress", mNetPrinter[position].macAddress);
            settings.putExtra("localName", "");
            settings.putExtra("printer", mNetPrinter[position].modelName);
            setResult(RESULT_OK, settings);
        }
        finish();
    }

    /**
     * search the net printer and adds the searched printer information into the
     * printerList
     */
    private boolean netPrinterList(int times) {

        boolean searchEnd = false;

        try {
            // clear the item list
            if (mItems != null) {
                mItems.clear();
            }

            // get net printers of the particular model
            mItems = new ArrayList<String>();
            Printer myPrinter = new Printer();
            PrinterInfo info = myPrinter.getPrinterInfo();
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            info.enabledTethering = Boolean.parseBoolean(sharedPreferences
                    .getString("enabledTethering", "false"));
            myPrinter.setPrinterInfo(info);

            mNetPrinter = myPrinter.getNetPrinters(modelName);
            final int netPrinterCount = mNetPrinter.length;

            // when find printers,set the printers' information to the list.
            if (netPrinterCount > 0) {
                searchEnd = true;

                String dispBuff[] = new String[netPrinterCount];
                for (int i = 0; i < netPrinterCount; i++) {
                    dispBuff[i] = mNetPrinter[i].modelName + "\n\n"
                            + mNetPrinter[i].ipAddress + "\n"
                            + mNetPrinter[i].macAddress + "\n"
                            + mNetPrinter[i].serNo + "\n"
                            + mNetPrinter[i].nodeName;
                    mItems.add(dispBuff[i]);
                }
            } else if (netPrinterCount == 0
                    && times == (Common.SEARCH_TIMES - 1)) { // when no printer
                // is found
                String dispBuff[] = new String[1];
                dispBuff[0] = getString(R.string.no_network_device);
                mItems.add(dispBuff[0]);
                searchEnd = true;
            }

            if (searchEnd) {
                // list the result of searching for net printer
                this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        final ArrayAdapter<String> fileList = new ArrayAdapter<String>(
                                PrinterWifiSettingsActivity.this,
                                android.R.layout.test_list_item, mItems);
                        PrinterWifiSettingsActivity.this.setListAdapter(fileList);
                    }
                });
            }
        } catch (Exception e) {
        }

        return searchEnd;
    }

    /**
     * make a dialog, which shows the message during searching
     */
    private void setDialog() {
        msgDialog.showMsgNoButton(
                getString(R.string.netPrinterListTitle_label),
                getString(R.string.search_printer));
    }

    /**
     * printer searching thread
     */
    private class SearchThread extends Thread {

        /* search for the printer for 10 times until printer has been found. */
        @Override
        public void run() {

            for (int i = 0; i < Common.SEARCH_TIMES; i++) {
                // search for net printer.
                if (netPrinterList(i)) {
                    msgDialog.close();
                    break;
                }
            }
            msgDialog.close();
        }
    }

    private void updatePrinterSettings() {
        final Intent settings = new Intent(this, PrinterWifiBTSettingsActivity.class);
        settings.putExtra("ipAddress", printerIp.getText().toString());
        settings.putExtra("printer", "Brother QL-820NWB");
        settings.putExtra("macAddress", printerMac.getText().toString());
        setResult(RESULT_OK, settings);
        finish();
    }

    private void setValues() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String ipAddress = sharedPreferences.getString("address", "");
        String macAddress = sharedPreferences.getString("macAddress", "");
        printerIp.setText(ipAddress);
        printerMac.setText(macAddress);
    }

    public void onParamterback(View view) {
        startActivity(new Intent(this, PrinterWifiBTSettingsActivity.class));
        finish();
    }
}