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

    private final String pearingNameKey = "BluetoothPareName";
    public static String selectedFileName;
    private EditText edittext;
    private String filePath;
    private static final int REQUEST_PATH = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_toshiba_label);

        try {

            final Context conn = this;

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);

            ListView listView = (ListView) findViewById(R.id.bluetoothlistview);
            // アダプターを設定します
            listView.setAdapter(adapter);

            listView.setSelector(new PaintDrawable(Color.BLUE));

            String strPrinterType = util.getPreferences(this, PRINTER_TYPE_KEYNAME);
            if (strPrinterType != null && strPrinterType.length() != 0) {
                if (PRINTER_LIST[0].compareTo(strPrinterType) == 0 || PRINTER_LIST[1].compareTo(strPrinterType) == 0 || PRINTER_LIST[8].compareTo(strPrinterType) == 0) {
                    adapter.add("SmpEP2G");
                } else if (PRINTER_LIST[2].compareTo(strPrinterType) == 0 || PRINTER_LIST[3].compareTo(strPrinterType) == 0) {
                    adapter.add("SmpEP4T");
                } else if (PRINTER_LIST[4].compareTo(strPrinterType) == 0) {
                    adapter.add("SmpEV4TT");
                } else if (PRINTER_LIST[5].compareTo(strPrinterType) == 0) {
                    adapter.add("SmpEV4TG");
                } else if (PRINTER_LIST[6].compareTo(strPrinterType) == 0) {
                    adapter.add("SmpLV4TT");
                } else if (PRINTER_LIST[7].compareTo(strPrinterType) == 0) {
                    adapter.add("SmpLV4TG");
                } else if (PRINTER_LIST[9].compareTo(strPrinterType) == 0) {
                    adapter.add("SmpFP3DGQM");
                } else if (PRINTER_LIST[10].compareTo(strPrinterType) == 0) {
                    adapter.add("SmpBA400TG_EN");
                } else if (PRINTER_LIST[11].compareTo(strPrinterType) == 0) {
                    adapter.add("SmpBA400TT_EN");
                    adapter.add("SmpBA400TT_JP");
                } else if (PRINTER_LIST[12].compareTo(strPrinterType) == 0) {
                    adapter.add("SmpFP2DG");
                    adapter.add("SmpFP2DGQM");
                } else if (PRINTER_LIST[13].compareTo(strPrinterType) == 0) {
                    adapter.add("SmpFV4D");
                }
            }

            final String myMemotyPath = Environment.getDataDirectory().getPath() + "/data/" + this.getPackageName();

            /**
             *
             */
            AdapterView.OnItemClickListener clickListener = new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View arg1, int position, long id) {


                    ListView listView = (ListView) parent;
                    // クリックされたアイテムを取得します
                    String selectItemName = (String) listView.getItemAtPosition(position);


                    if (selectItemName.compareTo("SmpEP2G") == 0) {
                        try {
                            util.asset2file(conn, "SmpEP2G.lfm", myMemotyPath, "tempLabel.lfm");


                        } catch (Exception e) {
                            e.printStackTrace();

                        }
                        startActivity(util.getCallActivityIntent(conn, "SmpEP2GActivity"));

                    } else if (selectItemName.compareTo("SmpEP4T") == 0) {
                        try {
                            util.asset2file(conn, "SmpEP4T.lfm", myMemotyPath, "tempLabel.lfm");

                        } catch (Exception e) {
                            e.printStackTrace();

                        }
                        startActivity(util.getCallActivityIntent(conn, "SmpEP4TActivity"));

                    } else if (selectItemName.compareTo("SmpEV4TT") == 0) {
                        try {
                            util.asset2file(conn, "SmpEV4TT.lfm", myMemotyPath, "tempLabel.lfm");

                        } catch (Exception e) {
                            e.printStackTrace();

                        }
                        startActivity(util.getCallActivityIntent(conn, "SmpEV4TActivity"));
                    } else if (selectItemName.compareTo("SmpLV4TT") == 0) {
                        try {
                            util.asset2file(conn, "SmpLV4TT.lfm", myMemotyPath, "tempLabel.lfm");

                        } catch (Exception e) {
                            e.printStackTrace();

                        }
                        startActivity(util.getCallActivityIntent(conn, "SmpEV4TActivity"));

                    } else if (selectItemName.compareTo("SmpEV4TG") == 0) {
                        try {
                            util.asset2file(conn, "SmpEV4TG.lfm", myMemotyPath, "tempLabel.lfm");

                        } catch (Exception e) {
                            e.printStackTrace();

                        }
                        startActivity(util.getCallActivityIntent(conn, "SmpEV4TActivity"));

                    } else if (selectItemName.compareTo("SmpLV4TG") == 0) {
                        try {
                            util.asset2file(conn, "SmpLV4TG.lfm", myMemotyPath, "tempLabel.lfm");

                        } catch (Exception e) {
                            e.printStackTrace();

                        }
                        startActivity(util.getCallActivityIntent(conn, "SmpEV4TActivity"));

                    } else if (selectItemName.compareTo("SmpFP3DGQM") == 0) {
                        try {
                            util.asset2file(conn, "SmpFP3DGQM.lfm", myMemotyPath, "tempLabel.lfm");

                        } catch (Exception e) {
                            e.printStackTrace();

                        }
                        startActivity(util.getCallActivityIntent(conn, "SmpFP3DActivity"));

                    } else if (selectItemName.compareTo("SmpFP3DBQM") == 0) {
                        try {
                            util.asset2file(conn, "SmpFP3DGBQM.lfm", myMemotyPath, "tempLabel.lfm");

                        } catch (Exception e) {
                            e.printStackTrace();

                        }
                        startActivity(util.getCallActivityIntent(conn, "SmpFP3DActivity"));

                    }
                    //add new 3/12/2018
                    else if (selectItemName.compareTo("SmpBA400TG_EN") == 0) {
                        try {
                            util.asset2file(conn, "SmpBA400TG_EN.lfm", myMemotyPath, "tempLabel.lfm");

                        } catch (Exception e) {
                            e.printStackTrace();

                        }
                        startActivity(util.getCallActivityIntent(conn, "SmpBA400TGActivity"));

                    } else if (selectItemName.compareTo("SmpBA400TT_EN") == 0) {
                        try {
                            util.asset2file(conn, "SmpBA400TT_EN.lfm", myMemotyPath, "tempLabel.lfm");

                        } catch (Exception e) {
                            e.printStackTrace();

                        }
                        startActivity(util.getCallActivityIntent(conn, "SmpBA400TTActivity"));

                    } else if (selectItemName.compareTo("SmpBA400TT_JP") == 0) {
                        try {
                            util.asset2file(conn, "SmpBA400TT_JP.lfm", myMemotyPath, "tempLabel.lfm");

                        } catch (Exception e) {
                            e.printStackTrace();

                        }
                        startActivity(util.getCallActivityIntent(conn, "SmpBA400TTActivity"));

                    } else if (selectItemName.compareTo("SmpFP2DG") == 0) {
                        try {
                            util.asset2file(conn, "SmpFP2DG.lfm", myMemotyPath, "tempLabel.lfm");

                        } catch (Exception e) {
                            e.printStackTrace();

                        }
                        startActivity(util.getCallActivityIntent(conn, "SmpFP2DActivity"));

                    } else if (selectItemName.compareTo("SmpFP2DGQM") == 0) {
                        try {
                            util.asset2file(conn, "SmpFP2DGQM.lfm", myMemotyPath, "tempLabel.lfm");

                        } catch (Exception e) {
                            e.printStackTrace();

                        }
                        startActivity(util.getCallActivityIntent(conn, "SmpFP2DActivity"));

                    } else if (selectItemName.compareTo("SmpFV4D") == 0) {
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
            /**
             *
             */
            AdapterView.OnItemSelectedListener selectListener = new AdapterView.OnItemSelectedListener() {

                @Override
                public void onItemSelected(AdapterView<?> parent, View arg1,
                                           int position, long id) {
                    ListView listView = (ListView) parent;
                    //
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