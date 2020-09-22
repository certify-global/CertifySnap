package com.certify.snap.bluetooth.printer.toshiba;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;

import com.certify.snap.R;

import java.io.File;

import jp.co.toshibatec.bcp.library.BCPControl;
import jp.co.toshibatec.bcp.library.Constants;
import jp.co.toshibatec.bcp.library.GetResources;

import static com.certify.snap.bluetooth.printer.toshiba.Defines.PORTSETTING_IP_ADDRESS_KEYNAME;
import static com.certify.snap.bluetooth.printer.toshiba.Defines.PORTSETTING_PORT_NUMBER_KEYNAME;

public class ToshibaMenuActivity extends AppCompatActivity  {

    private final String pearingNameKey = "BluetoothPareName";
    protected int selected;
    private BCPControl m_bcpControl = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_toshiba_menu);

        copyIniFile();

    }

    public void onClickBtnSelectPrinterType(View view) {
        startActivity(new Intent(this, ToshibaPrinterSettingsActivity.class));
    }

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

}