package com.certify.snap.activity;

import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.certify.snap.R;
import com.certify.snap.common.Application;
import com.certify.snap.common.M1CardUtils;
import com.google.zxing.other.BeepManager;

import java.io.IOException;

public class NFCCardActivity extends AppCompatActivity {

    private NfcAdapter mNfcAdapter;
    private Tag mTag;
    private PendingIntent mPendingIntent;
    private ProgressDialog dialog;
    private BeepManager mBeepManager;
    private BeepManager mBeepManager1;
    private int relaytimenumber = 5;
    private int DATA_BLOCK = 8;//第二扇区第1快
    private final int PASSWORD_BLOCK = 11;//第2扇区第4块

    private final byte[] password = new byte[]{(byte) 0xff, (byte) 0xff,
            (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff};

    private final byte[] password1 = new byte[]{(byte) 0x80, (byte) 0x60,
            (byte) 0x30, (byte) 0x30, (byte) 0x70, (byte) 0x80};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_nfccard);

        Application.getInstance().addActivity(this);

        mBeepManager = new BeepManager(this, R.raw.beep);
        mBeepManager1 = new BeepManager(this, R.raw.error);
        mNfcAdapter = M1CardUtils.isNfcAble(this);
        mPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

    }

    public void onNFCclick(View view) {
        switch (view.getId()){
            case R.id.nfc_activatecard:
                new InitTask().execute();
                break;
            case R.id.nfc_back:
                startActivity(new Intent(NFCCardActivity.this,SettingActivity.class));
                finish();
                break;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null)
            mTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mNfcAdapter != null) {
            mNfcAdapter.enableForegroundDispatch(this, mPendingIntent, null, null);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mNfcAdapter != null) {
            mNfcAdapter.disableForegroundDispatch(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dialog != null && dialog.isShowing())
            dialog.dismiss();
    }

    private class InitTask extends AsyncTask<Void, Void, Integer> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(NFCCardActivity.this);
            dialog.setTitle("Active Card");
            dialog.setMessage("Progressing ...");
            dialog.setCancelable(false);
            dialog.show();

        }

        @Override
        protected Integer doInBackground(Void... params) {
            int amount = -1;
            try {
                //初始化卡内数据为111
                M1CardUtils.writeBlockWithKeyA(mTag, DATA_BLOCK, 111, password);
                //修改密码
                M1CardUtils.changeKeyA(mTag, PASSWORD_BLOCK, password1, password);
                amount = 0;
                Log.e("tag","init card success");
            } catch (IOException e) {
                e.printStackTrace();
                return catchError(e);
            }
            return amount;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            Log.e("tag","result :"+ result);
            if (result == -1 || result == -2 || result == -3)
                processError(result);
            else {
                if (dialog != null && dialog.isShowing())
                    dialog.dismiss();
                Toast.makeText(NFCCardActivity.this,"Activate card success",Toast.LENGTH_LONG).show();
            }

        }
    }

    private int catchError(Exception e) {
        if (e == null || e.getMessage() == null)
            return -1;
        else if (e.getMessage().contains(M1CardUtils.CARD_TYPE_ERROR)) {
            return -2;
        } else if (e.getMessage().contains(M1CardUtils.AUTHENTICATE_ERROR)) {
            return -3;
        } else return -1;
    }

    private void processError(int error) {
        if (error == -1) {
            if (dialog != null && dialog.isShowing())
                dialog.dismiss();
//            mBeepManager1.playBeepSoundAndVibrate();
            Toast.makeText(NFCCardActivity.this,"Card error",Toast.LENGTH_LONG).show();
        } else if (error == -2) {
            if (dialog != null && dialog.isShowing())
                dialog.dismiss();
//            mBeepManager1.playBeepSoundAndVibrate();
            Toast.makeText(NFCCardActivity.this,M1CardUtils.CARD_TYPE_ERROR,Toast.LENGTH_LONG).show();
        } else if (error == -3) {
            if (dialog != null && dialog.isShowing())
                dialog.dismiss();
//            mBeepManager1.playBeepSoundAndVibrate();
            Toast.makeText(NFCCardActivity.this,M1CardUtils.AUTHENTICATE_ERROR,Toast.LENGTH_LONG).show();
        }
    }


}
