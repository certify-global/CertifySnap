package com.certify.snap.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.arcsoft.imageutil.ArcSoftImageFormat;
import com.arcsoft.imageutil.ArcSoftImageUtil;
import com.arcsoft.imageutil.ArcSoftImageUtilError;
import com.certify.snap.database.Database;
import com.certify.snap.database.DatabaseStore;
import com.certify.snap.model.OfflineVerifyMembers;
import com.certify.snap.bean.ImageInfo;
import com.certify.snap.bean.Members;
import com.certify.snap.bean.Pages;
import com.certify.snap.common.Application;
import com.certify.snap.common.Util;
import com.certify.snap.faceserver.FaceServer;
import com.certify.snap.model.RegisteredFailedMembers;
import com.certify.snap.model.RegisteredMembers;

import org.litepal.LitePal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.certify.snap.R;

public class InitializationActivity extends Activity {

    public SQLiteDatabase db;
    private int progress = 0;
    private int maxProgress = 100;
    private List<Members> membersList;
    private List<Pages> pagesList;
    private List<ImageInfo> imageInfosList;
    private List<File> imagesList;
    private List<File> filesList;
    private final Object obj = new Object();
    private AlertDialog alertDialog;

    private ExecutorService singleThreadPool = Executors.newSingleThreadExecutor();

    public static final int SHOW_NETWORK_ERROR = 0;

    private ListIterator pagesListIterator, imageInfosListIterator;
    private static AlertDialog waitingDialog;
    private Animation animation;

    public static final String OFFLINE_REGISTER_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "offline/register/";
    public static final String OFFLINE_FAILED_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "offline/failed/";

    public static final String ROOT_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "offline/register/";
    public static final String REGISTERED_IMAGE_DIR = ROOT_DIR + File.separator + "Registered Image";
    public static final String REGISTERED_FAILED_DIR = ROOT_DIR + File.separator + "Failed";
    private SharedPreferences sp;
    private TextView txt_wait_result_title;
    private TextView txt_wait_result;
    private Button btn_wait_dialog_ok;
    private ImageView img_wait, img_auto;
    private TextView txt_wait;

    private String TAG = "batch import---";
    private String ROOT_PATH_STRING = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_initialization);
        init();
        FaceServer.getInstance().init(this);//init FaceServer;
        Application.getInstance().addActivity(this);
    }

    public void init() {
        pagesList = Collections.synchronizedList(new ArrayList<Pages>());//Arraylist is not Thread safe,should use Collections.synchronizedList(List<T>) to create;
        membersList = Collections.synchronizedList(new ArrayList<Members>());
        imageInfosList = Collections.synchronizedList(new ArrayList<ImageInfo>());
        imagesList = Collections.synchronizedList(new ArrayList<File>());
        filesList = Collections.synchronizedList(new ArrayList<File>());

        sp = Util.getSharedPreferences(this);

        ROOT_PATH_STRING = this.getFilesDir().getAbsolutePath();
        try {
            db = LitePal.getDatabase();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public void initialize(View v) {
        showWaitingDialog();
        singleThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                batchimport();
            }
        });
    }

    private void batchimport(){
        try {
            File registerdirFile = new File(OFFLINE_REGISTER_DIR);
            if (!registerdirFile.exists()) registerdirFile.mkdirs();

            File faildirFile = new File(OFFLINE_FAILED_DIR);
            if (!faildirFile.exists()) faildirFile.mkdirs();

            File[] jpgfilelist = registerdirFile.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(FaceServer.IMG_SUFFIX);
                }
            });
            if (jpgfilelist == null && jpgfilelist.length == 0) {
                Log.e(TAG,"file length is not enough");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Util.showToast(InitializationActivity.this, getString(R.string.toast_filenotexist));
                    }
                });
                return;
            }

            long start = System.currentTimeMillis();
            int successNum = 0;
            int totalNum = jpgfilelist.length;
            progressBar.setMax(totalNum);

            for (File file : jpgfilelist) {
                String filename = file.getName();
                String[] result = filename.substring(0,filename.lastIndexOf(".")).split("_");
                Log.e(TAG,"filename="+filename);
                if(result!=null && result.length ==3){
                    String name = result[0];
                    String mobile = result[1];
                    String validtime = formatStringDate(result[2]);
                    String jpgpath = file.getAbsolutePath();
                    Log.e(TAG,"result[]=" +name+"-"+mobile+"-"+validtime+"-"+jpgpath);
                    if(!TextUtils.isEmpty(validtime)) {
                        if (processImg(name + "-" + mobile, jpgpath, mobile)) {
                            Log.e(TAG, "processImg success");
                            boolean issuccess = writeRegisterDatabase(name, mobile, validtime);
                            if(issuccess){
                                progressBar.setProgress(successNum++);
                                file.delete();
                            }else{
                                Log.e(TAG, "writeToFaillDir---");
                                writeToFaillDir(file, file.getName(),name+"-"+mobile);
                            }
                        } else {
                            Log.e(TAG, "processImg fail");
                            moveFile(file, file.getName());
                        }
                    }else{
                        Log.e(TAG, "validtime format fail");
                        moveFile(file, file.getName());
                    }
                }else{
                    Log.e(TAG,"result[] = null");
                    moveFile(file, file.getName());
                }
            }

            long end = System.currentTimeMillis();
            Log.e(TAG,"total = "+totalNum+",success = "+successNum+",time = "+ (end-start)+" ms");
            showResult(totalNum,successNum);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private boolean processImg(String name,String imgpath,String id){
        Bitmap bitmap = BitmapFactory.decodeFile(imgpath);
        bitmap = ArcSoftImageUtil.getAlignedBitmap(bitmap, true);
        if (bitmap == null) {
            Log.e("tag", "fail to translate bitmap");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Util.showToast(InitializationActivity.this,getString(R.string.toast_batch_translateBitmapfail));
                }
            });

            return false;
        }
        byte[] bgr24 = ArcSoftImageUtil.createImageData(bitmap.getWidth(), bitmap.getHeight(), ArcSoftImageFormat.BGR24);
        int transformCode = ArcSoftImageUtil.bitmapToImageData(bitmap, bgr24, ArcSoftImageFormat.BGR24);
        if (transformCode != ArcSoftImageUtilError.CODE_SUCCESS) { }
        boolean success = FaceServer.getInstance().registerBgr24(InitializationActivity.this, bgr24, bitmap.getWidth(),
                bitmap.getHeight(), name,id);
        return success;
    }

    public boolean writeRegisterDatabase(String name,String mobile,String time) {
        String username = name +"-"+mobile;
        String image =  ROOT_PATH_STRING + File.separator + FaceServer.SAVE_IMG_DIR + File.separator + username+FaceServer.IMG_SUFFIX;
        String feature = ROOT_PATH_STRING + File.separator + FaceServer.SAVE_FEATURE_DIR + File.separator + username;
        Log.e("tag", "image_uri---" + image + "  feature_uri---" + feature);

        RegisteredMembers registeredMembers;
        List<RegisteredMembers> list = LitePal.where("mobile = ?", mobile).find(RegisteredMembers.class);
        if (list != null && list.size() > 0) {
            registeredMembers = list.get(0);
        }else{
            registeredMembers = new RegisteredMembers();
        }
        registeredMembers.setFirstname(name);
        registeredMembers.setMobile(mobile);
        registeredMembers.setStatus("1");
        //registeredMembers.setExpire_time(time);
        registeredMembers.setImage(image);
        registeredMembers.setFeatures(feature);
//        boolean result = registeredMembers.save();
        return true;
    }

    private void writeToFaillDir(File source, String failpath,String name){
        moveFile(source,failpath);
        FaceServer.getInstance().deleteImgFeature(InitializationActivity.this,name);
    }

    private void moveFile(File source, String failname) {
        try {
            File failedFile = new File(OFFLINE_FAILED_DIR + failname);
            FileInputStream in = new FileInputStream(source);
            FileOutputStream out = new FileOutputStream(failedFile);
            byte[] buffer = new byte[1024];
            while (in.read(buffer) > 0) {
                out.write(buffer);
            }
            in.close();
            out.close();
            if (source.delete()) {
                Log.e(TAG, "move failfile done");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String formatStringDate(String time){
        try{
            if(TextUtils.isEmpty(time)) return "";

            String[] result = time.split("-");
            if(result!=null && result.length ==6){
                String formattime = result[0]+"-"+ result[1]+"-"+ result[2]+" "+ result[3]+":"+ result[4]+":"+ result[5];
                if(Util.isValidDate(formattime,"yyyy-MM-dd HH:mm:ss")) return formattime;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return "";
    }

    private ProgressBar progressBar;
    private void showWaitingDialog() {
        if(waitingDialog!=null&&waitingDialog.isShowing())
            return;
        Log.e(TAG,"showWaitingDialog-----");

        View view = View.inflate(this, R.layout.dialog_initialize_wait, null);
        img_wait = view.findViewById(R.id.img_initialize_wait);
        txt_wait = view.findViewById(R.id.txt_initialize_wait_title);
        txt_wait_result_title = view.findViewById(R.id.txt_initialize_result_dialog_title);
        txt_wait_result = view.findViewById(R.id.txt_initialize_result_dialog);
        progressBar = view.findViewById(R.id.progress_download);
        btn_wait_dialog_ok = view.findViewById(R.id.btn_wait_dialog_ok);
        progressBar.setMax(maxProgress);
        progressBar.setProgress(progress);
        img_wait.setVisibility(View.VISIBLE);
        txt_wait.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        btn_wait_dialog_ok.setVisibility(View.GONE);
        txt_wait_result_title.setVisibility(View.GONE);
        txt_wait_result.setVisibility(View.GONE);
        animation = AnimationUtils.loadAnimation(this, R.anim.dialog_initialize_wait_rotate);
        animation.setDuration(3000);
        LinearInterpolator linearInterpolator = new LinearInterpolator();
        animation.setInterpolator(linearInterpolator);
        img_wait.setAnimation(animation);
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setView(view)
                .setCancelable(false);
        waitingDialog = builder.create();
        img_wait.startAnimation(animation);
        Window window = waitingDialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable());//show rounded dialog
        }
        waitingDialog.show();
    }

    private void showResult(final int totalnum, final int successnum ){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txt_wait_result_title.setText(R.string.txt_initialize_import_data_wait_dialog_title);
                txt_wait_result.setText(R.string.txt_initialize_import_data_wait_dialog_result);
                txt_wait_result.append("\n"+ getString(R.string.toast_init_batchtotal) + totalnum + "\n"+ getString(R.string.toast_init_batchsuccess) + successnum
                        + "\n"+ getString(R.string.toast_init_batchfailed) + (totalnum - successnum) + "\n"+getString(R.string.toast_init_batchresult) + "\n");
                img_wait.setVisibility(View.GONE);
                txt_wait.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
                txt_wait_result_title.setVisibility(View.VISIBLE);
                txt_wait_result.setVisibility(View.VISIBLE);
                btn_wait_dialog_ok.setVisibility(View.VISIBLE);
            }
        });
    }

    private void dismissWaitingDialog() {
        Log.e(TAG,"dismissWaitingDialog-------");
        if (animation != null) {
            animation.cancel();
        }
        if (waitingDialog != null && waitingDialog.isShowing()) {
            waitingDialog.dismiss();
            waitingDialog = null;
        }
    }

    public void dismissWaitingDialog(View v) {
        dismissWaitingDialog();
    }

    public void clear(View v) {
        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(R.string.notification)
                .setMessage(R.string.confirm_delete)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            deleteDirFile(OFFLINE_REGISTER_DIR);
                            deleteDirFile(OFFLINE_FAILED_DIR);

                            LitePal.deleteAll(RegisteredMembers.class);
                            LitePal.deleteAll(RegisteredFailedMembers.class);
                            LitePal.deleteAll(OfflineVerifyMembers.class);

                            int deleteCount = FaceServer.getInstance().clearAllFaces(InitializationActivity.this);
                            Util.showToast(InitializationActivity.this, deleteCount + getString(R.string.toast_init_deleteinfo));
                            FaceServer.getInstance().deleteAllInfo();
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .create();
        dialog.show();
    }

    private void deleteDirFile(String path){
        File Dir = new File(path);
        if (Dir.exists() && Dir.isDirectory()) {
            File[] files = Dir.listFiles();
            if (files != null && files.length>0) {
                for (File file : files) {
                    if (file.delete()) {
                        Log.e("tag", path+" delete exist failed picture");
                    }
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (singleThreadPool != null && !singleThreadPool.isShutdown()) {
            singleThreadPool.shutdownNow();
        }
        FaceServer.getInstance().unInit();
        dismissWaitingDialog();
    }

    public void onDataManagementback(View view) {
        startActivity(new Intent(InitializationActivity.this, SettingActivity.class));
        finish();
    }

}
