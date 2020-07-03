package com.certify.snap.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Environment;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.certify.snap.adapter.RecordAdapter;
import com.certify.snap.common.Application;
import com.certify.snap.common.Util;
import com.certify.snap.model.OfflineVerifyMembers;
import com.certify.snap.R;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.litepal.LitePal;
import org.litepal.crud.callback.FindMultiCallback;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RecordActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecordAdapter recordAdapter;
    private List<OfflineVerifyMembers> datalist = new ArrayList<>();
    private List<OfflineVerifyMembers> exportlist = new ArrayList<>();
    public SQLiteDatabase db;
    private ProgressDialog mprogressDialog;
    private AlertDialog mSelectDialog;
    private ExecutorService singleThreadPool = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_record);

        Application.getInstance().addActivity(this);
        try {
            db = LitePal.getDatabase();
        }catch (Exception e){
            e.printStackTrace();
        }
        recyclerView = findViewById(R.id.recyclerview_record);

        initdata(true);
    }

    public void initdata(final boolean isNeedInit){
        try {
            if (db != null) {
                LitePal.findAllAsync(OfflineVerifyMembers.class).listen(new FindMultiCallback<OfflineVerifyMembers>() {
                    @Override
                    public void onFinish(List<OfflineVerifyMembers> list) {
                        datalist = list;
                        if (isNeedInit) {
                            initMember();
                        } else {
                            refreshMemberList(datalist);
                            recyclerView.scrollToPosition(0);
                        }
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void refreshMemberList(List<OfflineVerifyMembers> list) {
        Log.e("refreshRecordList---", "start");
        datalist = list;
        recordAdapter.refresh(datalist);
    }

    private void initMember() {
        recordAdapter = new RecordAdapter(RecordActivity.this, datalist);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(recordAdapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recordAdapter.notifyDataSetChanged();
    }

    private void refresh() {
        initdata(false);
    }

    private CellStyle getcellstyle(SXSSFWorkbook sxssfWorkbook){
        CellStyle cellStyle = sxssfWorkbook.createCellStyle();
        cellStyle.setAlignment((short)2);
        cellStyle.setVerticalAlignment((short)1);

        Font font0 = sxssfWorkbook.createFont();
        font0.setFontHeightInPoints((short) 14);
        font0.setBold(true);
        cellStyle.setFont(font0);
        return cellStyle;
    }

    private CellStyle getcellstyle1(SXSSFWorkbook sxssfWorkbook){
        CellStyle cellStyle = sxssfWorkbook.createCellStyle();
        cellStyle.setAlignment((short)2);
        cellStyle.setVerticalAlignment((short)1);

        Font font0 = sxssfWorkbook.createFont();
        font0.setFontHeightInPoints((short) 14);
        cellStyle.setFont(font0);
        return cellStyle;
    }

    private String selectValue(int i){
        String value = "";
        if(i==0) value = "Device Model";
        if(i==1) value = "Device SN";
        if(i==2) value = "Name";
        if(i==3) value = "Mobile";
        if(i==4) value = "Temperature";
        if(i==5) value = "Verify Time";
        return value;
    }

    private String selectValue(int i,OfflineVerifyMembers members){
        String value = "";
        if(i==0) value = Build.MODEL;
        if(i==1) value = Util.getSNCode();
        if(i==2) value = members.getName();
        if(i==3) value = members.getMobile();
        if(i==4) value = members.getTemperature()+" ℃";
        if(i==5) value = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(members.getVerify_time());
        return value;
    }

    private void exportXlsx(List<OfflineVerifyMembers> list,String name) {
        long startTime = System.currentTimeMillis();
        String path = Environment.getExternalStorageDirectory() + "/offline/record/";
        File file = new File(path);
        if (!file.exists()) file.mkdirs();
        String filePath = path + name + ".xlsx";
        SXSSFWorkbook sxssfWorkbook = null;
        BufferedOutputStream outputStream = null;
        try {
            sxssfWorkbook = new SXSSFWorkbook(getXSSFWorkbook(filePath), 100);
            SXSSFSheet sheet = (SXSSFSheet) sxssfWorkbook.getSheetAt(0);
            for(int z=0;z<6;z++){
                sheet.setColumnWidth(z,25*256);
            }
            for (int i = 0; i < list.size()+1; i++) {
                SXSSFRow row = (SXSSFRow) sheet.createRow(i);
                for(int j=0;j<6;j++) {
                    if (i == 0) {
                        Cell cell = row.createCell(j);
                        cell.setCellValue(selectValue(j));
                        cell.setCellStyle(getcellstyle(sxssfWorkbook));
                    } else if(i>=1){
                        Cell cell = row.createCell(j);
                        cell.setCellValue(selectValue(j,list.get(i-1)));
                        cell.setCellStyle(getcellstyle1(sxssfWorkbook));
                    }
                }

            }
            outputStream = new BufferedOutputStream(new FileOutputStream(filePath));
            sxssfWorkbook.write(outputStream);
            outputStream.flush();
            sxssfWorkbook.dispose();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        long endTime = System.currentTimeMillis();
        Log.e("time---", "write time= " + (endTime - startTime) + "ms");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                DismissProgressDialog(mprogressDialog);
                Toast.makeText(RecordActivity.this,"Export finish! File path : /sdcard/offline/record/",Toast.LENGTH_SHORT).show();
            }
        });

    }

    /**
     * 先创建一个XSSFWorkbook对象
     *
     * @param filePath
     * @return
     */
    public static XSSFWorkbook getXSSFWorkbook(String filePath) {
        XSSFWorkbook workbook = null;
        BufferedOutputStream outputStream = null;
        try {
            File fileXlsxPath = new File(filePath);
            outputStream = new BufferedOutputStream(new FileOutputStream(fileXlsxPath));
            workbook = new XSSFWorkbook();
            workbook.createSheet("Record Sheet");
            workbook.write(outputStream);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return workbook;
    }

    private long stringToDate(String strTime, String formatType) throws Exception {
        SimpleDateFormat formatter = new SimpleDateFormat(formatType);
        Date date = null;
        date = formatter.parse(strTime);
        return date.getTime();
    }

    private void export(String starttime,String endtime){
        try {
            mprogressDialog = ProgressDialog.show(RecordActivity.this, "Export", "Export file! Please wait...");
            Log.e("export---",stringToDate(starttime,"yyyy-MM")+"-"+
                    stringToDate(endtime,"yyyy-MM")+"-"+System.currentTimeMillis());

            final List<OfflineVerifyMembers> resultlist = LitePal.where(
                    "verify_time >="+ stringToDate(starttime,"yyyy-MM")
                            +" and verify_time <="+ stringToDate(endtime,"yyyy-MM")+"")
                    .order("verify_time asc").find(OfflineVerifyMembers.class);
            if (resultlist != null && resultlist.size()>0) {
                Log.e("search result---",resultlist.size()+"-"+resultlist.get(0).toString());
                final String date = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
                singleThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        exportXlsx(resultlist,"Record-"+date);
                    }
                });
            }else{
                Toast.makeText(RecordActivity.this,"Data is null!",Toast.LENGTH_SHORT).show();
                DismissProgressDialog(mprogressDialog);
            }

        } catch (Exception e) {
            e.printStackTrace();
            DismissProgressDialog(mprogressDialog);
        }
    }

    private void DismissProgressDialog(ProgressDialog progress) {
        if (progress != null && progress.isShowing())
            progress.dismiss();
        if (mSelectDialog != null && mSelectDialog.isShowing()) {
            mSelectDialog.dismiss();
        }
    }

    private void showSelectDialog(){
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_record_select, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view).setCancelable(false);
        if (mSelectDialog == null) mSelectDialog = builder.create();

        final EditText mstartyear = view.findViewById(R.id.record_select_startyear);
        final EditText mstarmonth = view.findViewById(R.id.record_select_startmonth);
        final EditText mendyear = view.findViewById(R.id.record_select_endyear);
        final EditText mendmonth = view.findViewById(R.id.record_select_endmonth);

        Button mconfirm =  view.findViewById(R.id.record_select_confirm);
        mconfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String startyearstr = mstartyear.getText().toString();
                String startmonthstr = mstarmonth.getText().toString();
                String endyearstr = mendyear.getText().toString();
                String endmonthstr = mendmonth.getText().toString();

                if(!TextUtils.isEmpty(startyearstr) && !TextUtils.isEmpty(startmonthstr)
                        && !TextUtils.isEmpty(endyearstr) && !TextUtils.isEmpty(endmonthstr)){
                    String starttime = startyearstr +"-"+ startmonthstr;
                    String endtime = endyearstr +"-"+ endmonthstr;
                    Log.e("exportdate---",starttime+"-"+endtime);
                    Log.e("isvaliddate---", Util.isValidDate(starttime,"yyyy-MM")+"-"+ Util.isValidDate(endtime,"yyyy-MM"));
                    if(Util.isValidDate(starttime,"yyyy-MM") && Util.isValidDate(endtime,"yyyy-MM")){
                        export(starttime,endtime);
                    }
                }else{
                    Toast.makeText(RecordActivity.this,"Please input full date!",Toast.LENGTH_SHORT).show();
                }

            }
        });

        Button mcancel =  view.findViewById(R.id.record_select_cancel);
        mcancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSelectDialog != null && mSelectDialog.isShowing()) {
                    mSelectDialog.dismiss();
                    mSelectDialog = null;
                }
            }
        });

        if (mSelectDialog != null && !mSelectDialog.isShowing()) {
            mSelectDialog.show();
        }
    }

    public void ontemperature(View view) {
        switch (view.getId()){
            case R.id.refresh:
                if (recordAdapter != null) {
                    refresh();
                }
                break;
            case R.id.record_back:
                startActivity(new Intent(RecordActivity.this, SettingActivity.class));
                finish();
                break;
            case R.id.record_export:
                showSelectDialog();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (singleThreadPool != null && !singleThreadPool.isShutdown()) {
            singleThreadPool.shutdownNow();
        }
        DismissProgressDialog(mprogressDialog);
    }
}
