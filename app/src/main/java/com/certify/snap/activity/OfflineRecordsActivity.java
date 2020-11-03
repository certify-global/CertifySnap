package com.certify.snap.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
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
import com.certify.snap.controller.DatabaseController;
import com.certify.snap.model.OfflineRecordTemperatureMembers;
import com.certify.snap.model.OfflineVerifyMembers;
import com.certify.snap.R;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

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

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class OfflineRecordsActivity extends AppCompatActivity {

    protected static final String TAG = OfflineRecordsActivity.class.getSimpleName();
    private RecyclerView recyclerView;
    private RecordAdapter recordAdapter;
    private List<OfflineRecordTemperatureMembers> datalist = new ArrayList<>();
    private List<OfflineRecordTemperatureMembers> exportlist = new ArrayList<>();
    public SQLiteDatabase db;
    private ProgressDialog mprogressDialog;
    private AlertDialog mSelectDialog;
    private ExecutorService singleThreadPool = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_offline_record);

        Application.getInstance().addActivity(this);
        recyclerView = findViewById(R.id.recyclerview_record);

        initdata(true);
    }

    public void initdata(final boolean isNeedInit){
        try {
            Observable.create(new ObservableOnSubscribe<List<OfflineRecordTemperatureMembers>>() {
                @Override
                public void subscribe(ObservableEmitter<List<OfflineRecordTemperatureMembers>> emitter) throws Exception {
                    List<OfflineRecordTemperatureMembers> offlineRecordList = DatabaseController.getInstance().findAllOfflineRecord();
                    emitter.onNext(offlineRecordList);
                }
            }).subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<List<OfflineRecordTemperatureMembers>>() {
                        Disposable disposable;
                        @Override
                        public void onSubscribe(Disposable d) {
                            disposable = d;
                        }

                        @Override
                        public void onNext(List<OfflineRecordTemperatureMembers> list) {
                            datalist = list;
                            if (isNeedInit) {
                                initMember();
                            } else {
                                refreshMemberList(datalist);
                                recyclerView.scrollToPosition(0);
                            }
                            disposable.dispose();
                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.e(TAG, "Error in fetching the data model from database");
                        }

                        @Override
                        public void onComplete() {
                            disposable.dispose();
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void refreshMemberList(List<OfflineRecordTemperatureMembers> list) {
        Log.e("refreshRecordList---", "start");
        datalist = list;
        recordAdapter.refresh(datalist);
    }

    private void initMember() {
        recordAdapter = new RecordAdapter(OfflineRecordsActivity.this, datalist);
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
        if(i==1) value = Util.getSNCode(this);
        if(i==2) value = members.getName();
        if(i==3) value = members.getMobile();
        if(i==4) value = members.getTemperature()+" ℃";
        if(i==5) value = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(members.getVerify_time());
        return value;
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
                }else{
                    Toast.makeText(OfflineRecordsActivity.this,"Please input full date!",Toast.LENGTH_SHORT).show();
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
                startActivity(new Intent(OfflineRecordsActivity.this, SettingsActivity.class));
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
