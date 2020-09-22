package com.certify.snap.bluetooth.printer.toshiba;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.PaintDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.certify.snap.R;

import jp.co.toshibatec.bcp.library.BCPControl;
import jp.co.toshibatec.bcp.library.LongRef;

import static com.certify.snap.bluetooth.printer.toshiba.Defines.PORTSETTING_PORT_MODE_KEYNAME;
import static com.certify.snap.bluetooth.printer.toshiba.Defines.PRINTER_LIST;
import static com.certify.snap.bluetooth.printer.toshiba.Defines.PRINTER_TYPE_KEYNAME;

public class ToshibaPrinterSettingsActivity extends AppCompatActivity {

    private BCPControl m_bcpControl = null;
    private ConnectionData mConnectData = new ConnectionData();
    private String mIssueMode = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_toshiba_printer_settings);
        final Context context = this.getApplicationContext();

        try {
            resizeReturnButton();
            printerList(context);
            portList(context);

        } catch( Exception e ) {
            Log.d("TAG", "onCreate: ");
        }

    }

    private void printerList(Context context){
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_single_choice );
        String orginalPrinterType = util.getPreferences(this, PRINTER_TYPE_KEYNAME);
        int position = 0;
        int selectPosition = 13;
        for(int i=0; i<PRINTER_LIST.length; i++){
            adapter.add(PRINTER_LIST[i]);

            if (orginalPrinterType != null && orginalPrinterType.length() != 0
                    && PRINTER_LIST[i].compareTo(orginalPrinterType) == 0) {
                selectPosition = position;
            }
            position += 1;
        }
        ListView listView = (ListView) findViewById(R.id.StartMenuButtonlist1);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setAdapter(adapter);
        listView.setSelector(new PaintDrawable(Color.BLUE));
        listView.setItemChecked(selectPosition, true);
        AdapterView.OnItemClickListener clickListener = new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View arg1, int position, long id)  {
                ListView listView = (ListView) parent;
                selectMenu( position, listView );
            }
            private void selectMenu(int position, ListView listView) {
                String item = (String) listView.getItemAtPosition(position);
                util.setPreferences(context, PRINTER_TYPE_KEYNAME, item);
                onClickButtonReturn(null);
            }
        };
        listView.setOnItemClickListener(clickListener);
    }

    private void portList(Context context){
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_single_choice);
        adapter.add("FILE");
        ListView listView = (ListView) findViewById(R.id.port_menu_list);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setAdapter(adapter);

        listView.setSelector(new PaintDrawable(Color.BLUE));

        AdapterView.OnItemClickListener clickListener = new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View arg1, int position, long id) {
                ListView listView = (ListView) parent;
                selectMenu(position, listView);
            }

            private void selectMenu(int position, ListView listView) {
                String item = (String) listView.getItemAtPosition(position);

                if (item.equals("FILE")) {
                    util.setPreferences(context, PORTSETTING_PORT_MODE_KEYNAME, item);
                    callFILE();
                }
            }
        };
        listView.setOnItemClickListener(clickListener);

        String item = util.getPreferences(context, PORTSETTING_PORT_MODE_KEYNAME);
        if (item.length() == 0) {
            listView.setItemChecked(0, true);
        } else {
            listView.setItemChecked(2, true);
        }

    }

    private void resizeReturnButton() {
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        Button btnReturn = (Button)this.findViewById(R.id.BttonReturn);
        Point size = new Point();
        display.getSize(size);
        btnReturn.setWidth(size.x);
    }

    public void  onClickButtonReturn( View view ) {
        //this.confirmActivityFinish();
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction()==KeyEvent.ACTION_DOWN ) {
            if( event.getKeyCode() == KeyEvent.KEYCODE_BACK ) {
                this.confirmActivityFinish();

                return true;
            }
        }
        return super.dispatchKeyEvent(event);
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
                        // いいえを押した場合なので何もしないでDialogを閉じる
                    }
                });
    }

    private void callFILE() {
        String className = "FileSettingActivity";
        startActivity(new Intent(this, ToshibaFileSettingActivity.class));
    }

    public void testPrint(View view){
        final String myMemotyPath = Environment.getDataDirectory().getPath() + "/data/" + this.getPackageName();
        try {
            util.asset2file(this, "SmpFV4D.lfm", myMemotyPath, "tempLabel.lfm");
        } catch (Exception e) {
            e.printStackTrace();
        }
        startActivity(new Intent(ToshibaPrinterSettingsActivity.this, ToshibaSmpLabelActivity.class));
    }
}