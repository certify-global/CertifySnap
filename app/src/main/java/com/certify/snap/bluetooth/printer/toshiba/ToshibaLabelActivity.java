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
    }

    public void printScreen(View view) {
        final String myMemotyPath = Environment.getDataDirectory().getPath() + "/data/" + this.getPackageName();
        try {
            util.asset2file(this, "SmpFV4D.lfm", myMemotyPath, "tempLabel.lfm");
        } catch (Exception e) {
            e.printStackTrace();
        }
        startActivity(new Intent(ToshibaLabelActivity.this, ToshibaSmpLabelActivity.class));
    }
}