package com.certify.snap.activity;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.view.View;
import android.view.WindowManager;

import com.certify.snap.R;
import com.certify.snap.common.ContextUtils;
import com.certify.snap.printer.Common;
import com.certify.snap.printer.PrinterModelInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class PrinterWifiBTSettingsActivity extends PreferenceActivity implements
        Preference.OnPreferenceChangeListener {

    private SharedPreferences sharedPreferences;

    @Override
    protected void attachBaseContext(Context newBase) {
        Locale localeToSwitchTo;
        if (HomeActivity.mSelectLanguage) {
            localeToSwitchTo = new Locale("es");
        } else {
            localeToSwitchTo = new Locale("en");
        }
        ContextWrapper localeUpdatedContext = ContextUtils.updateLocale(newBase, localeToSwitchTo);
        super.attachBaseContext(localeUpdatedContext);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        addPreferencesFromResource(R.xml.activity_printer_wifibt_settings);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // initialize the printerModel ListPreference
        ListPreference printerModelPreference = (ListPreference) getPreferenceScreen()
                .findPreference("printerModel");
        printerModelPreference.setEntryValues(PrinterModelInfo.getModelNames());
        printerModelPreference.setEntries(PrinterModelInfo.getModelNames());


        // initialize the printer_settings
        setPreferenceValue("printerModel");
        String printerModel = sharedPreferences.getString("printerModel", "QL_820NWB");

        // set paper size & port information
        printerModelChange(printerModel);

        setPreferenceValue("port");
        setEditValue("address");
        setEditValue("macAddress");
        setPreferenceValue("paperSize");
        setPreferenceValue("orientation");
        setEditValue("numberOfCopies");
        setPreferenceValue("printMode");

        setPreferenceValue("printQuality");

        setEditValue("scaleValue");

        // initialize the custom paper size's printer_settings
        File newdir = new File(Common.CUSTOM_PAPER_FOLDER);
        if (!newdir.exists()) {
            newdir.mkdir();
        }
        File[] files = new File(Common.CUSTOM_PAPER_FOLDER).listFiles();
        List<String> entriesList = new ArrayList<String>(files.length);
        List<String> entryValuesList = new ArrayList<String>(files.length);

        for (File file : files) {
            String filename = file.getName();
            String extention = filename.substring(
                    filename.lastIndexOf(".", filename.length()) + 1,
                    filename.length());
            if (extention.equalsIgnoreCase("bin")) {
                entriesList.add(filename);
                entryValuesList.add(filename);
            }
        }
        String[] entries = entriesList.toArray(new String[entriesList.size()]);
        String[] entryValues = entryValuesList.toArray(new String[entriesList.size()]);
        Arrays.sort(entries);
        Arrays.sort(entryValues);

        ListPreference customSettingPreference = (ListPreference) getPreferenceScreen()
                .findPreference("customSetting");
        //customSettingPreference.setEntries(entries);
        //customSettingPreference.setEntryValues(entryValues);


        //setPreferenceValue("dashLine");
        setPreferenceValue("autoCut");
        setPreferenceValue("specialType");
        setPreferenceValue("halfCut");
        //setPreferenceValue("trimTapeAfterData");

        // initialization for printer
        PreferenceScreen printerPreference = (PreferenceScreen) getPreferenceScreen()
                .findPreference("printer");

        String printer = sharedPreferences.getString("printer", "");
        if (!printer.equals("")) {
            printerPreference.setSummary(printer);
        }

        printerPreference
                .setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        String printerModel = sharedPreferences.getString(
                                "printerModel", "");
                        setPrinterList(printerModel);
                        return true;
                    }
                });


        // set the BackgroundForPreferenceScreens to light
        setBackgroundForPreferenceScreens("prefIpMacAddress");
        setBackgroundForPreferenceScreens("prefCutSettings");

        //setBackgroundForPreferenceScreens("halfToningSetting");
        setBackgroundForPreferenceScreens("scaleModelSetting");

        //setSavePathPreference();

        setEditValue("processTimeout");
        setEditValue("sendTimeout");
        setEditValue("receiveTimeout");
        setEditValue("connectionTimeout");
        setEditValue("closeWaitTime");

        //setPreferenceValue("softFocusing");
        //setPreferenceValue("enabledTethering");
        //setPreferenceValue("rawMode");
        //setPreferenceValue("useLegacyHalftoneEngine");
        setWorkPathPreference();

    }

    private void setSavePathPreference() {
        PreferenceScreen savePrnPathPreference = (PreferenceScreen) getPreferenceScreen()
                .findPreference("savePrnPath");
        String savePrnPath = sharedPreferences.getString("savePrnPath", "");
        if (!savePrnPath.equals("")) {
            savePrnPathPreference.setSummary(savePrnPath);
        }

        savePrnPathPreference
                .setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {

                        setSavePath();
                        return true;
                    }
                });
    }

    /**
     * Called when [printer] is tapped
     */
    private void setSavePath() {

     /*   Intent savePath = new Intent(this, SaveFileActivity.class);
        startActivityForResult(savePath, Common.SAVE_PATH);*/
    }

    private void setWorkPathPreference() {

        ListPreference printerValuePreference = (ListPreference) getPreferenceScreen()
                .findPreference("workPath");

        String internalFolder = getFilesDir().getAbsolutePath();
        String externalFolder = getExternalFilesDir(null).getAbsolutePath();

        String[] dirValues = {
                internalFolder,
                externalFolder
        };
        printerValuePreference.setEntryValues(dirValues);

        String savedWorkPath = sharedPreferences.getString("workPath", "");
        if (savedWorkPath.isEmpty()) {
            savedWorkPath = internalFolder;
        }

        int selectedIndex = Arrays.asList(dirValues).indexOf(savedWorkPath);
        if (selectedIndex < 0) {
            selectedIndex = 0;
        }
        printerValuePreference.setValueIndex(selectedIndex);
    }

    /**
     * Called when a Preference has been changed by the user. This is called
     * before the state of the Preference is about to be updated and before the
     * state is persisted.
     */
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {

        if (newValue != null) {
            if (preference.getKey().equals("printerModel")) {
                String printerModel = sharedPreferences.getString(
                        "printerModel", "");
                if (printerModel.equalsIgnoreCase(newValue.toString())) {
                    return true;
                }

                // initialize if printer model is changed
                printerModelChange(newValue.toString());
                ListPreference paperSizePreference = (ListPreference) getPreferenceScreen()
                        .findPreference("paperSize");
                paperSizePreference.setValue(paperSizePreference
                        .getEntryValues()[0].toString());
                paperSizePreference.setSummary(paperSizePreference
                        .getEntryValues()[0].toString());

                ListPreference portPreference = (ListPreference) getPreferenceScreen()
                        .findPreference("port");
                portPreference.setValue(portPreference.getEntryValues()[0]
                        .toString());
                portPreference.setSummary(portPreference.getEntryValues()[0]
                        .toString());

                setChangedData();
            }

            if (preference.getKey().equals("port")) {
                setChangedData();
            }

            preference.setSummary((CharSequence) newValue);

            return true;
        }

        return false;

    }

    /**
     * Called when the searching printers activity you launched exits.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (Common.PRINTER_SEARCH == requestCode) {
            EditTextPreference addressPreference = (EditTextPreference) getPreferenceScreen()
                    .findPreference("address");
            EditTextPreference macAddressPreference = (EditTextPreference) getPreferenceScreen()
                    .findPreference("macAddress");
            PreferenceScreen printerPreference = (PreferenceScreen) getPreferenceScreen()
                    .findPreference("printer");

            if (resultCode == RESULT_OK) {
                // IP address
                String ipAddress = data.getStringExtra("ipAddress");
                addressPreference.setText(ipAddress);
                if (ipAddress.equalsIgnoreCase("")) {
                    ipAddress = getString(R.string.address_value);
                }
                addressPreference.setSummary(ipAddress);

                // MAC address
                String macAddress = data.getStringExtra("macAddress");
                macAddressPreference.setText(macAddress);
                macAddressPreference.setSummary(macAddress);

                // Printer name
                printerPreference.setSummary(data.getStringExtra("printer"));

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("printer", data.getStringExtra("printer"));
                editor.putString("localName", data.getStringExtra("localName"));
                editor.apply();
            }
        } else if (Common.SAVE_PATH == requestCode) {
            if (resultCode == RESULT_OK) {
                PreferenceScreen saveFilePreference = (PreferenceScreen) getPreferenceScreen()
                        .findPreference("savePrnPath");

                saveFilePreference.setSummary(data
                        .getStringExtra("savePrnPath"));
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("savePrnPath",
                        data.getStringExtra("savePrnPath"));
                editor.apply();
            }
        }
    }

    /**
     * set data of a particular ListPreference
     */
    private void setPreferenceValue(String value) {
        String data = sharedPreferences.getString(value, "");

        ListPreference printerValuePreference = (ListPreference) getPreferenceScreen()
                .findPreference(value);
        printerValuePreference.setOnPreferenceChangeListener(this);
        if (!data.equals("")) {
            printerValuePreference.setSummary(data);
        }
    }

    /**
     * set data of a particular EditTextPreference
     */
    private void setEditValue(String value) {
        String name = sharedPreferences.getString(value, "");
        EditTextPreference printerValuePreference = (EditTextPreference) getPreferenceScreen()
                .findPreference(value);
        printerValuePreference.setOnPreferenceChangeListener(this);

        if (!name.equals("")) {
            printerValuePreference.setSummary(name);
        }
    }

    /**
     * Called when [printer] is tapped
     */
    private void setPrinterList(String printModel) {
        String port = sharedPreferences.getString("port", "");

        // call the Activity_NetPrinterList when port is NET
        if (port.equalsIgnoreCase("NET")) {
            Intent printerList = new Intent(this, PrinterWifiSettingsActivity.class);
            String printTempModel = printModel.replaceAll("_", "-");
            printerList.putExtra("modelName", printTempModel);
            startActivityForResult(printerList, Common.PRINTER_SEARCH);
        } else // call the Activity_BluetoothPrinterList when port is Bluetooth
        {
            Intent printerList = new Intent(this,
                    PrinterBTSettingsActivity.class);
            startActivityForResult(printerList, Common.PRINTER_SEARCH);
        }
    }


    /**
     * set paper size & port information with changing printer model
     */
    private void printerModelChange(String printerModel) {

        // paper size
        ListPreference paperSizePreference = (ListPreference) getPreferenceScreen()
                .findPreference("paperSize");
        // port
        ListPreference portPreference = (ListPreference) getPreferenceScreen()
                .findPreference("port");
        if (!printerModel.equals("")) {

            String[] entryPort;
            String[] entryPaperSize;
            entryPort = PrinterModelInfo.getPortOrPaperSizeInfo(printerModel, Common.SETTINGS_PORT);
            entryPaperSize = PrinterModelInfo.getPortOrPaperSizeInfo(printerModel, Common.SETTINGS_PAPERSIZE);

            portPreference.setEntryValues(entryPort);
            portPreference.setEntries(entryPort);

            paperSizePreference.setEntryValues(entryPaperSize);
            paperSizePreference.setEntries(entryPaperSize);

        }
    }

    /**
     * initialize the address & macAddress information with changing printer
     * model or port
     */
    private void setChangedData() {
        EditTextPreference addressPreference = (EditTextPreference) getPreferenceScreen()
                .findPreference("address");
        EditTextPreference macAddressPreference = (EditTextPreference) getPreferenceScreen()
                .findPreference("macAddress");
        PreferenceScreen printerPreference = (PreferenceScreen) getPreferenceScreen()
                .findPreference("printer");

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("address", "");
        editor.putString("macAddress", "");
        editor.putString("printer", getString(R.string.printer_text));
        editor.apply();

        addressPreference.setText("");
        macAddressPreference.setText("");
        printerPreference.setSummary(getString(R.string.printer_text));
        macAddressPreference.setSummary(getString(R.string.mac_address_value));
        addressPreference.setSummary(getString(R.string.address_value));
    }

    /**
     * set the BackgroundForPreferenceScreens to light it is black when at OS
     * 2.1/2.2
     */
    private void setBackgroundForPreferenceScreens(String key) {
        PreferenceScreen preferenceScreen = (PreferenceScreen) getPreferenceScreen()
                .findPreference(key);

        preferenceScreen
                .setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        PreferenceScreen pref = (PreferenceScreen) preference;
                        pref.getDialog()
                                .getWindow()
                                .setBackgroundDrawableResource(
                                        android.R.drawable.screen_background_light);
                        return false;
                    }
                });
    }

    public void onParamterback(View view) {
        startActivity(new Intent(this, SettingsActivity.class));
        finish();
    }
}