package com.certify.snap.bluetooth.printer.toshiba;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.PaintDrawable;
import android.os.Bundle;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.certify.snap.R;

import jp.co.toshibatec.bcp.library.BCPControl;
import jp.co.toshibatec.bcp.library.LongRef;

import static com.certify.snap.bluetooth.printer.toshiba.Defines.PORTSETTING_PORT_MODE_KEYNAME;
import static com.certify.snap.bluetooth.printer.toshiba.Defines.PRINTER_LIST;
import static com.certify.snap.bluetooth.printer.toshiba.Defines.PRINTER_TYPE_KEYNAME;

public class ToshibaPortSettingsActivity extends AppCompatActivity {


    private static final String OPTION_PRINTMODE_KEYNAME = "OGPM";
    private BCPControl m_bcpControl = null;
    private ConnectionData mConnectData = new ConnectionData();

    private String mIssueMode = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_toshiba_port_settings);
        final Context con = this.getApplicationContext();

        try {

            resizeReturnButton();

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_single_choice);
            String strPrinterType = util.getPreferences(this, PRINTER_TYPE_KEYNAME);
            if (strPrinterType != null && strPrinterType.length() != 0 &&
                    (PRINTER_LIST[4].compareTo(strPrinterType) == 0 || PRINTER_LIST[5].compareTo(strPrinterType) == 0)) {
                //adapter.add("WLAN");
                adapter.add("FILE");
            } else {
               /* adapter.add("Bluetooth");
                adapter.add("WLAN");*/
                adapter.add("FILE");
            }

            ListView listView = (ListView) findViewById(R.id.StartMenuButtonlist1);
            listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            listView.setAdapter(adapter);

            listView.setSelector(new PaintDrawable(Color.BLUE));

            /**
             *
             */
            AdapterView.OnItemClickListener clickListener = new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View arg1, int position, long id) {
                    ListView listView = (ListView) parent;


                    selectMenu(position, listView);


                }

                /**
                 *
                 * @param position
                 * @param listView
                 */
                private void selectMenu(int position, ListView listView) {
                    String item = (String) listView.getItemAtPosition(position);

                    if (item.equals("Bluetooth")) {
                        util.setPreferences(con, PORTSETTING_PORT_MODE_KEYNAME, item);
                        callBluetooth();
                    } else if (item.equals("WLAN")) {
                        util.setPreferences(con, PORTSETTING_PORT_MODE_KEYNAME, item);
                        callWLAN();
                    } else if (item.equals("FILE")) {
                        util.setPreferences(con, PORTSETTING_PORT_MODE_KEYNAME, item);
                        callFILE();
                    } else {

                    }
                }
            };
            listView.setOnItemClickListener(clickListener);

            String item = util.getPreferences(con, PORTSETTING_PORT_MODE_KEYNAME);
            if (item.length() == 0) {
                listView.setItemChecked(0, true);

            } else {
                if (item.equals("Bluetooth")) {
                    listView.setItemChecked(0, true);

                } else if (item.equals("WLAN")) {
                    listView.setItemChecked(1, true);
                } else if (item.equals("FILE")) {
                    listView.setItemChecked(2, true);
                } else {
                    listView.setItemChecked(0, true);
                }
            }

        } catch (Exception e) {

        }

    }

    private void resizeReturnButton() {
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        Button btnReturn = (Button) this.findViewById(R.id.BttonReturn);
        Point size = new Point();
        display.getSize(size);
        btnReturn.setWidth(size.x);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void onPause() {
        super.onPause();
    }

    /**
     *
     */
    public void onClickButtonReturn(View view) {
        this.confirmActivityFinish();
    }


    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                this.confirmActivityFinish();

                return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }


    private void callBluetooth() {
        //
        String className = "BluetoothListActivity";
        startActivity(util.getCallActivityIntent(this, className));

    }

    private void callWLAN() {
        String className = "WLANSettingActivity";
        startActivity(util.getCallActivityIntent(this, className));

    }

    private void callFILE() {
        String className = "FileSettingActivity";
        startActivity(new Intent(this, ToshibaFileSettingActivity.class));

    }

    protected void confirmActivityFinish() {
        util.comfirmDialog(this, this.getString(R.string.confirmBack),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }
                , new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
    }

    public void onClickButtonPortOpen(View view) {
        try {
            if (mConnectData.getIsOpen().get() == true) {
                LongRef Result = new LongRef(0);
                if (false == m_bcpControl.ClosePort(Result)) {
                    util.showAlertDialog(this, String.format(R.string.msg_PortCloseError + "= %08x", Result.getLongValue()));
                } else {
                    mConnectData.getIsOpen().set(false);
                    ((Button) this.findViewById(R.id.BttonPortOpen)).setText(R.string.msg_PortOpen);
                }
            } else {
                portOpen();
            }

        } catch (Exception ex) {

        }

    }

    private void portOpen() {
        String portSetting = "";

        String portMode = util.getPreferences(this, PORTSETTING_PORT_MODE_KEYNAME);

        portSetting = util.getPortSetting(this);
        if (portSetting.length() == 0) {
            return;
        }
        if (mIssueMode.equalsIgnoreCase("Send")) {
            mConnectData.setIssueMode(1);
        } else {
            mConnectData.setIssueMode(2);
        }
        mConnectData.setPortSetting(portSetting);

        String printerType = util.getPreferences(this, PRINTER_TYPE_KEYNAME);
        int usePrinter = Defines.getPrinterNo(printerType);
        m_bcpControl.setUsePrinter(usePrinter);

        ConnectExecuteTask task = new ConnectExecuteTask(this, ((Button) this.findViewById(R.id.BttonPortOpen)), m_bcpControl);
        task.execute(mConnectData);


    }
}
