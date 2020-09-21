package com.certify.snap.bluetooth.printer.toshiba;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
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

import com.certify.snap.R;

import static com.certify.snap.bluetooth.printer.toshiba.Defines.PRINTER_LIST;
import static com.certify.snap.bluetooth.printer.toshiba.Defines.PRINTER_TYPE_KEYNAME;

public class ToshibaPrinterSettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_toshiba_printer_settings);
        final Context con = this.getApplicationContext();

        try {

            resizeReturnButton();

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_single_choice );
            String orginalPrinterType = util.getPreferences(this, PRINTER_TYPE_KEYNAME);
            int position = 0;
            int selectPosition = 0;
            for(int i=0; i<PRINTER_LIST.length; i++){
                adapter.add(PRINTER_LIST[i]);

                if (orginalPrinterType != null && orginalPrinterType.length() != 0
                        && PRINTER_LIST[i].compareTo(orginalPrinterType) == 0) {
                    selectPosition = position;
                }
                position += 1;
            }

            ListView listView = (ListView) findViewById(R.id.StartMenuButtonlist1);
            // 選択の方式の設定
            listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            // アダプターを設定します
            listView.setAdapter(adapter);

            listView.setSelector(new PaintDrawable(Color.BLUE));

            // 指定したアイテムがチェックされているかを設定
            listView.setItemChecked(selectPosition, true);

            /**
             *
             */
            AdapterView.OnItemClickListener clickListener = new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View arg1, int position, long id)  {
                    ListView listView = (ListView) parent;

                    selectMenu( position, listView );

                }
                /**
                 *
                 * @param position
                 * @param listView
                 */
                private void selectMenu(int position, ListView listView) {
                    // クリックされたアイテムを取得します
                    String item = (String) listView.getItemAtPosition(position);

                    util.setPreferences(con, PRINTER_TYPE_KEYNAME, item);

                    onClickButtonReturn(null);
                }
            };
            // リストビューのアイテムがクリックされた時に呼び出されるコールバックリスナーを登録します
            listView.setOnItemClickListener(clickListener);

        } catch( Exception e ) {

        }

    }
    /**
     * [前画面に戻る]ボタンを画面横サイズに合わせる処理
     */
    private void resizeReturnButton() {
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        Button btnReturn = (Button)this.findViewById(R.id.BttonReturn);
        Point size = new Point();
        display.getSize(size);
        btnReturn.setWidth(size.x);
    }
    @Override
    protected void onResume(){
        super.onResume();
    }
    public void onPause(){
        super.onPause();
    }
    /**
     *
     */
    public void  onClickButtonReturn( View view ) {
        this.confirmActivityFinish();
    }
    /**
     *
     * @param event
     */
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
        util.comfirmDialog( this ,this.getString(R.string.confirmBack),
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