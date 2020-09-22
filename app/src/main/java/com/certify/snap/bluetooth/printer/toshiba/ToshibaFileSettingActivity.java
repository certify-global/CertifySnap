package com.certify.snap.bluetooth.printer.toshiba;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.EditText;

import com.certify.snap.R;

import static com.certify.snap.bluetooth.printer.toshiba.Defines.PORTSETTING_FILE_PATH_KEYNAME;

public class ToshibaFileSettingActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_toshiba_file_setting);
        String filePath = util.getPreferences(this, PORTSETTING_FILE_PATH_KEYNAME);
        if (filePath.length() == 0) {
            filePath = Environment.getExternalStorageDirectory().getPath() + "/PrintImageFile.txt";
        }
        ((EditText) this.findViewById(R.id.FileNameEditText)).setText(filePath);

    }

    public void onClickButtonSave(View view) {
        String fileName = ((EditText) this.findViewById(R.id.FileNameEditText)).getText().toString();
        if (fileName.length() == 0) {
            util.showAlertDialog(this, this.getString(R.string.filePathBlank));
            return;
        }
        util.setPreferences(this, PORTSETTING_FILE_PATH_KEYNAME, fileName);

        util.showAlertDialog(this, this.getString(R.string.saveFilePath));

    }


}
