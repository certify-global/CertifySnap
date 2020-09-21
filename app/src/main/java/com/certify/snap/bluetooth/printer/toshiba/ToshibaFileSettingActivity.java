package com.certify.snap.bluetooth.printer.toshiba;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
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
        if( filePath.length() == 0 ) {
            filePath = Environment.getExternalStorageDirectory().getPath() + "/PrintImageFile.txt";
        }
        ((EditText)this.findViewById(R.id.FileNameEditText)).setText( filePath );

    }
    @Override
    protected void onResume(){
        super.onResume();
    }
    /**
     *
     */
    public void onPause(){
        super.onPause();
    }
    /**
     *
     * @param event
     */
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction()==KeyEvent.ACTION_DOWN ) {
            if( event.getKeyCode() == KeyEvent.KEYCODE_BACK ) {
                confirmActivityFinish();
                return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }
    /**
     * [保存]ボタンが押されたときの処理
     * @param view
     */
    public void onClickButtonSave(View view){
        String fileName = ((EditText)this.findViewById(R.id.FileNameEditText)).getText().toString();
        if( fileName.length() == 0 ) {
            util.showAlertDialog(this , this.getString(R.string.filePathBlank));
            return;
        }
        util.setPreferences(this, PORTSETTING_FILE_PATH_KEYNAME, fileName );

        util.showAlertDialog(this , this.getString(R.string.saveFilePath));

    }
    /**
     * [前画面に戻る]ボタンが押されたときの処理
     * @param view
     */
    public void onClickButtonReturn(View view) {
        confirmActivityFinish();
    }
    /**
     *
     */
    protected void confirmActivityFinish() {
        util.comfirmDialog( this , this.getString(R.string.confirmBack),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // アプリケーションを終了させる
                        finish();
                    }
                }
                , new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // いいえを押した場合なので何もしないでDialogを閉じる

                    }
                } );

    }

}
