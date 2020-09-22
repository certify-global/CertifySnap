package com.certify.snap.bluetooth.printer.toshiba;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.PaintDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.certify.snap.R;

import static com.certify.snap.bluetooth.printer.toshiba.Defines.PRINTER_LIST;
import static com.certify.snap.bluetooth.printer.toshiba.Defines.PRINTER_TYPE_KEYNAME;

public class ToshibaLabelActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_toshiba_label);

        try {
            final Context conn = this;
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
            ListView listView = findViewById(R.id.bluetoothlistview);
            listView.setAdapter(adapter);
            listView.setSelector(new PaintDrawable(Color.BLUE));

            String strPrinterType = util.getPreferences(this, PRINTER_TYPE_KEYNAME);
            if (strPrinterType != null && strPrinterType.length() != 0) {
                    adapter.add("SmpFV4D");
            }

            final String myMemotyPath = Environment.getDataDirectory().getPath() + "/data/" + this.getPackageName();

            AdapterView.OnItemClickListener clickListener = new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View arg1, int position, long id) {
                    ListView listView = (ListView) parent;
                    String selectItemName = (String) listView.getItemAtPosition(position);
                    if (selectItemName.compareTo("SmpFV4D") == 0) {
                        try {
                            util.asset2file(conn, "SmpFV4D.lfm", myMemotyPath, "tempLabel.lfm");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        startActivity(new Intent(ToshibaLabelActivity.this, ToshibaSmpLabelActivity.class));
                    } else {
                        util.showAlertDialog(ToshibaLabelActivity.this, conn.getString(R.string.selectLabelError));
                        return;
                    }
                    finish();
                }

            };
            AdapterView.OnItemSelectedListener selectListener = new AdapterView.OnItemSelectedListener() {

                @Override
                public void onItemSelected(AdapterView<?> parent, View arg1,
                                           int position, long id) {
                    ListView listView = (ListView) parent;
                    String lfmFileName = (String) listView
                            .getItemAtPosition(position);

                    startActivity(util.getCallActivityIntent(conn,
                            "DebugMenuActivity"));

                    finish();

                }

                @Override
                public void onNothingSelected(AdapterView<?> arg0) {
                    // TODO Auto-generated method stub
                }

            };

            listView.setOnItemClickListener(clickListener);

            listView.setOnItemSelectedListener(selectListener);

        } catch (Throwable th) {
        }
    }
}