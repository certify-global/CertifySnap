package com.certify.snap.activity;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.arcsoft.imageutil.ArcSoftImageFormat;
import com.arcsoft.imageutil.ArcSoftImageUtil;
import com.arcsoft.imageutil.ArcSoftImageUtilError;
import com.certify.callback.ManageMemberCallback;
import com.certify.callback.MemberIDCallback;
import com.certify.callback.MemberListCallback;
import com.certify.snap.R;
import com.certify.snap.adapter.MemberAdapter;
import com.certify.snap.adapter.MemberFailedAdapter;
import com.certify.snap.async.AsyncGetMemberData;
import com.certify.snap.async.AsyncJSONObjectManageMember;
import com.certify.snap.common.Application;
import com.certify.snap.common.EndPoints;
import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.Logger;
import com.certify.snap.common.M1CardUtils;
import com.certify.snap.common.MemberUtilData;
import com.certify.snap.common.Util;
import com.certify.snap.faceserver.FaceServer;
import com.certify.snap.model.RegisteredFailedMembers;
import com.certify.snap.model.RegisteredMembers;

import org.json.JSONArray;
import org.json.JSONObject;
import org.litepal.LitePal;
import org.litepal.crud.callback.FindMultiCallback;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.certify.snap.common.Util.getnumberString;

public class ManagementActivity extends AppCompatActivity implements ManageMemberCallback, MemberListCallback, MemberIDCallback {

    protected static final String LOG = "Management Activity ";
    private EditText msearch;
    private TextView mCountTv;
    private RecyclerView recyclerView, failed_recyclerView;
    private MemberAdapter memberAdapter;
    private MemberFailedAdapter memberfailedAdapter;
    private List<RegisteredMembers> datalist = new ArrayList<>();
    private List<RegisteredFailedMembers> faillist = new ArrayList<>();
    private Handler mhandler = new Handler();
    private String searchtext = "";
    private AlertDialog mUpdateDialog, mDeleteDialog;
    private String updateimagePath = "";
    private PopupWindow mpopupwindow, mpopupwindowUpdate;
    private Uri registerUri;
    private Uri imageUri;
    private String registerpath = "";
    private String model = Build.MODEL;
    private Uri updateUri;
    private ProgressDialog mprogressDialog, mdeleteprogressDialog, mloadingprogress;
    public SQLiteDatabase db;
    public final static int UPDATE = 1;
    public final static int TOAST = 2;
    public final static int REGISTER = 3;
    public final static int REGISTER_PHOTO = 1;
    private final static int PICK_IMAGE = 3;
    private final static int UPDATE_PICK_IMAGE = 4;
    public final static int UPDATE_PHOTO = 2;
    private String ROOT_PATH_STRING = "";
    private Boolean isUpdate = false;
    private Boolean isDeleted = false;

    private NfcAdapter mNfcAdapter; //Optimize
    private PendingIntent mPendingIntent;
    private RegisteredMembers updateMember = null;
    private EditText registerAccessid;
    private SharedPreferences sharedPreferences;
    private RelativeLayout relative_management;
    Snackbar snackbar;
    int listPosition;
    int count=0;

    private Runnable searchRun = new Runnable() {
        @Override
        public void run() {
            Log.e("searchRun---", "start Run: " + searchtext);
            searchData(searchtext);
            Util.hideSoftKeyboard(ManagementActivity.this);
        }
    };

    private String TAG = "management---";
    public String OFFLINE_FAILED_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "offline/failed/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_management);
        relative_management = findViewById(R.id.relative_management);
        mCountTv = findViewById(R.id.count_tv);

        Application.getInstance().addActivity(this);
        sharedPreferences = Util.getSharedPreferences(this);
        try {
            db = LitePal.getDatabase();
        } catch (Exception e) {
            e.printStackTrace();
        }
        ROOT_PATH_STRING = this.getFilesDir().getAbsolutePath();
        FaceServer.getInstance().init(this);//init FaceServer;

        recyclerView = findViewById(R.id.recyclerview);
        failed_recyclerView = findViewById(R.id.failed_recyclerview);
        msearch = findViewById(R.id.edit_search);
        msearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().equals("\n")){
                    msearch.setSingleLine(true);
                } else {
                    msearch.setSingleLine(false);
                }

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (searchRun != null) {
                    mhandler.removeCallbacks(searchRun);
                }
                searchtext = s.toString();
                if (!TextUtils.isEmpty(searchtext)) mhandler.postDelayed(searchRun, 1000);

                if (TextUtils.isEmpty(searchtext) && searchtext != null) {
                    refresh();
                   Util.hideSoftKeyboard(ManagementActivity.this);
                }
            }
        });

        initData(true);
        initNfc();

    }

    @Override
    protected void onResume() {
        super.onResume();
        enableNfc();
    }

    @Override
    protected void onPause() {
        super.onPause();
        disableNfc();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        byte[] ID = new byte[20];
        ID = tag.getId();
        String UID = Util.bytesToHexString(ID);
        if (UID == null) return;
        String id = bytearray2Str(hexStringToBytes(UID.substring(2)), 0, 4, 10);
        if (updateMember != null) {
            updateMember.setAccessid(id);
            //Update UI
            maccessid.setText(id);
            return;
        }
        registerAccessid.setText(id);
        popupEnrollBtn.setVisibility(View.GONE);
    }

    public synchronized void onmemberclick(View v) {
        switch (v.getId()) {
            case R.id.refresh:
                if (memberAdapter != null || memberfailedAdapter != null) {
                    //refresh();
                    Util.getmemberList(this, this);
                    count=0;
                    testCount = 1;
                    mloadingprogress = ProgressDialog.show(ManagementActivity.this, "Loading", "Loading please wait...");

                }
                break;
            case R.id.register:
                resetUpdateMember();
                showRegisterPopupwindow();
                Log.e("register---", "showRegisterPopupwindow");
                break;
            case R.id.edit_clear:
                msearch.setText("");
                refresh();
                Util.hideSoftKeyboard(this);
                break;
            case R.id.member_back:
                //startActivity(new Intent(ManagementActivity.this, SettingActivity.class));
                finish();
                break;
        }
    }

    private void initData(final boolean isNeedInit) {
        try {
            if (db != null) {
                LitePal.findAllAsync(RegisteredMembers.class).listen(new FindMultiCallback<RegisteredMembers>() {
                    @Override
                    public void onFinish(List<RegisteredMembers> list) {
                        Log.e("NagaTest list---", list.size() + "");
                        if (list != null) {
                            datalist = list;
//                            if(list.size() == 34){
//                                Toast.makeText(ManagementActivity.this, getString(R.string.records_sync_completed), Toast.LENGTH_LONG).show();
//
//                            }
                            if (isNeedInit) {
                                initMember();
                            } else {
                                refreshMemberList(list);
                                //Util.showToast(ManagementActivity.this, getString(R.string.records_sync_completed));
                               // recyclerView.scrollToPosition(0);
                            }

                        }

                    }
                });

             /*   List<RegisteredFailedMembers> list = getFailedList();
                if (list != null) {
                    Log.e("faillist---", list.size() + "-" + list.toString());
                    faillist = list;
                    if (isNeedInit) {
                        initFailedMember();
                    } else {
                        refresMemberFailList(list);
                        failed_recyclerView.scrollToPosition(0);
                    }
                }*/

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initMember() {
        if(totalMemberCount==totalMemberLastcount){
            mCountTv.setText(String.valueOf(datalist.size()));
        }else{
            mCountTv.setText(testCount++ + " / " + totalMemberCount);
        }
        if(memberAdapter == null)
            memberAdapter = new MemberAdapter(ManagementActivity.this, datalist);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(memberAdapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        memberAdapter.notifyDataSetChanged();
        memberAdapter.setOnItemClickListener(new MemberAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                showUpdateDialog(datalist.get(position));
                listPosition = position;
            }
        });
        memberAdapter.setOnItemLongClickListener(new MemberAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(View view, int position) {
                showDeleteDialog(datalist.get(position));
            }
        });
    }

    private void initFailedMember() {
        memberfailedAdapter = new MemberFailedAdapter(ManagementActivity.this, faillist);
        failed_recyclerView.setNestedScrollingEnabled(false);
        failed_recyclerView.setHasFixedSize(true);
        failed_recyclerView.setLayoutManager(new LinearLayoutManager(this));
        failed_recyclerView.setAdapter(memberfailedAdapter);
        failed_recyclerView.setItemAnimator(new DefaultItemAnimator());
        memberfailedAdapter.notifyDataSetChanged();
        memberfailedAdapter.setOnItemClickListener(new MemberFailedAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Util.showToast(ManagementActivity.this, getString(R.string.toast_manage_photoerror));
            }
        });
    }

    private void searchData(String searchstr) {
        try {
            List<RegisteredMembers> resultlist = LitePal.where("firstname like ? or memberid like ?", searchstr + "%", searchstr + "%")
                    .order("firstname asc").find(RegisteredMembers.class);
            if (resultlist != null && resultlist.size() > 0) {
                Log.e("search result----", resultlist.toString());
                refreshMemberList(resultlist);
            }

            List<RegisteredFailedMembers> memberfaillist = getFailedList();
            if (memberfaillist != null && memberfaillist.size() > 0) {
                Log.e("search fail result----", memberfaillist.toString());
                refresMemberFailList(memberfaillist);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void refreshMemberList(List<RegisteredMembers> memberlist) {
        Log.e("refreshMemberList---", "start-" + memberlist.toString());
        datalist = memberlist;
        memberAdapter.refresh(datalist);
    }

    private void refresMemberFailList(List<RegisteredFailedMembers> failedlist) {
        Log.e("refresMemberFailList---", "start---");
        faillist = failedlist;
        memberfailedAdapter.refresh(faillist);
    }

    private List<RegisteredFailedMembers> getFailedList() {
        List<RegisteredFailedMembers> list = new ArrayList<>();
        File faildir = new File(OFFLINE_FAILED_DIR);
        if (!faildir.exists()) faildir.mkdirs();

        File[] filelist = faildir.listFiles();
        if (filelist != null && filelist.length > 0) {
            Log.e(TAG, "fail file length >0");
            for (File file : filelist) {
                RegisteredFailedMembers fail = new RegisteredFailedMembers();
                fail.setName(file.getName());
                fail.setImage(file.getAbsolutePath());
                list.add(fail);
            }
        }
        return list;
    }

    ImageView mfaceimg;

    //TextView mtimetext;
    EditText mfirstname;
    EditText mlasttname;
    EditText mmobile;
    EditText mmemberid;
    EditText mmemberemail;
    EditText maccessid;
    EditText muniqueid;
    //EditText mtime;
    Button mupdate;
    ImageView medit;
    LinearLayout textbody;
    LinearLayout editbody;
    private Button enrollBtn;
    private Button popupEnrollBtn;
    ImageView mregisterfaceimg = null;
    Button mtakephoto;
    Button musephoto;
    Button mupdatetakephoto;
    Button mupdateusephoto;

    TextInputLayout text_input_access_id, text_input_member_id;

    private void showUpdateDialog(final RegisteredMembers member) {
        try {
            updateMember = member;
            updateimagePath = "";
            View view = LayoutInflater.from(this).inflate(R.layout.dialog_update, null);
            mpopupwindowUpdate = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            mpopupwindowUpdate.setOutsideTouchable(false);
            mpopupwindowUpdate.setFocusable(true);
            mpopupwindowUpdate.setAnimationStyle(R.style.animTranslate);

            final TextView muserid = view.findViewById(R.id.dialog_edit_userid);
            //mtimetext = view.findViewById(R.id.dialog_text_time);
            mfirstname = view.findViewById(R.id.dialog_edit_first_name);
            mlasttname = view.findViewById(R.id.dialog_edit_last_name);
            mmobile = view.findViewById(R.id.dialog_edit_mobile);
            mmemberid = view.findViewById(R.id.dialog_edit_member_id);
            mmemberemail = view.findViewById(R.id.dialog_edit_email);
            maccessid = view.findViewById(R.id.dialog_edit_accessid);
            muniqueid = view.findViewById(R.id.dialog_edit_uniqueid);
            text_input_access_id = view.findViewById(R.id.text_input_access_id);
            text_input_member_id = view.findViewById(R.id.text_input_member_id);


            //mtime = view.findViewById(R.id.dialog_edit_time);
            textbody = view.findViewById(R.id.linear_body);
            editbody = view.findViewById(R.id.linear_edit_body);

            maccessid.setText(member.getAccessid());
            mupdatetakephoto = view.findViewById(R.id.dialog_take_photo);
            mupdatetakephoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    takefacephoto();
                }
            });
            mupdateusephoto = view.findViewById(R.id.dialog_use_photo);
            mupdateusephoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    gallaryPhoto("update.jpg", updateimagePath, UPDATE_PICK_IMAGE);
                }
            });
            //mtimetext.setText(member.getExpire_time());

            mfaceimg = view.findViewById(R.id.dialog_faceimg);
            Bitmap bitmap = BitmapFactory.decodeFile(member.getImage());
            if (bitmap != null) {
                mfaceimg.setImageBitmap(bitmap);
            }


//        mtime.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                if (hasFocus) {
//                    showUpdateDatePicker(mtime);
//                } else {
//                    Log.e("hasfocus-----", "false");
//                }
//            }
//        });


            mfirstname.setText(member.getFirstname());
            mlasttname.setText(member.getLastname());
            mmobile.setText(member.getMobile());
            mmemberid.setText(member.getMemberid());
            mmemberemail.setText(member.getEmail());
            maccessid.setText(member.getAccessid());
            muniqueid.setText(member.getUniqueid());

            ImageView mexit = view.findViewById(R.id.dialog_exit);
            mexit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mpopupwindowUpdate != null) {
                        mpopupwindowUpdate.dismiss();
                    }
                }
            });
            mupdate = view.findViewById(R.id.btn_update);
            mupdate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String firstnamestr = mfirstname.getText().toString();
                    String lastnamestr = mlasttname.getText().toString();
                    String mobilestr = mmobile.getText().toString();
                    String idstr = mmemberid.getText().toString();
                    String emailstr = mmemberemail.getText().toString();
                    String accessstr = maccessid.getText().toString();
                    String uniquestr = muniqueid.getText().toString();

                    //String timestr = mtime.getText().toString();
                    Log.e("updateinfo---", firstnamestr + "-" + lastnamestr + "-" + mobilestr + "-" + idstr + "-" + emailstr + accessstr + "-" + uniquestr);
                    if (!TextUtils.isEmpty(idstr) || !TextUtils.isEmpty(accessstr)
                            && !TextUtils.isEmpty(lastnamestr) && !TextUtils.isEmpty(emailstr)) {
                        if ("".equalsIgnoreCase(updateimagePath)) {
                            updateimagePath = member.getImage();
                            Log.e("updateimgpath---", updateimagePath);
                        }

                        if (!idstr.equals(updateMember.getMemberid()) && isMemberExist(idstr)) {
                            Toast.makeText(ManagementActivity.this, getString(R.string.toast_manage_member_exist), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (!accessstr.equals(null) && !accessstr.equals(updateMember.getAccessid()) && isAccessIdExist(accessstr)) {
                            Toast.makeText(ManagementActivity.this, getString(R.string.toast_manage_access_exist), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        mprogressDialog = ProgressDialog.show(ManagementActivity.this, "Update", "Update! pls wait...");
//                        if(isValidDate(timestr,"yyyy-MM-dd HH:mm:ss")) {
//                            mprogressDialog = ProgressDialog.show(ManagementActivity.this, "Update", "Update! pls wait...");
//                            localUpdate(member.getMobile(),namestr,mobilestr,timestr,updateimagePath);
//                        }else{
//                            Util.showToast(ManagementActivity.this, getString(R.string.toast_manage_dateerror));
//                        }
                        if (sharedPreferences.getBoolean(GlobalParameters.ONLINE_MODE, true)) {
                            try {
                                Bitmap bitmap1 = BitmapFactory.decodeFile(updateimagePath);
                                isUpdate = true;
                                JSONObject obj = new JSONObject();
                                obj.put("id", uniquestr);
                                obj.put("firstName", firstnamestr);
                                obj.put("lastname", lastnamestr);
                                obj.put("email", emailstr);
                                obj.put("phoneNumber", mobilestr);
                                obj.put("memberId", idstr);
                                obj.put("accessId", accessstr);
                                obj.put("faceTemplate", bitmap1 == null ? "" : Util.encodeToBase64(bitmap1));
                                obj.put("status", true);
                                obj.put("memberType", 1);
                                new AsyncJSONObjectManageMember(obj, ManagementActivity.this, sharedPreferences.getString(GlobalParameters.URL, EndPoints.prod_url) + EndPoints.ManageMember, ManagementActivity.this).execute();
                            } catch (Exception e) {
                                Logger.error(LOG + "AsyncJSONObjectMemberManage", e.getMessage());
                            }
                        } else {
                            localUpdate(member.getMemberid(), firstnamestr, lastnamestr, mobilestr, idstr, emailstr, accessstr, uniquestr, updateimagePath);
                        }
                    } else if (TextUtils.isEmpty(idstr)) {
                        text_input_member_id.setError("Member Id should not be empty");
                    } else if (TextUtils.isEmpty(accessstr)) {
                        text_input_access_id.setError("Access Id should not be empty");
                    }
                }
            });

            enrollBtn = view.findViewById(R.id.btn_enroll);
            enrollBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(getApplicationContext(), "Please scan the card to enroll", Toast.LENGTH_LONG).show();
                }
            });

            View parent = LayoutInflater.from(ManagementActivity.this).inflate(R.layout.activity_management, null);
            if (mpopupwindowUpdate != null) {
                mpopupwindowUpdate.showAtLocation(parent, Gravity.CENTER, 0, 0);
            }
        } catch (Exception e) {
            Logger.error(TAG, "showUpdateDialog(final RegisteredMembers member)", e.getMessage());
        }
    }

    private void setViewVisible(View v1) {
        v1.setVisibility(View.VISIBLE);
    }

    private void setViewclick(View view, boolean status) {
        view.setEnabled(status);
    }

    public int year, month, day, hour, minute;
    public StringBuffer date = new StringBuffer();
    public StringBuffer time = new StringBuffer();

    private void initDateTime() {
        Calendar calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH) + 1;
        day = calendar.get(Calendar.DAY_OF_MONTH);
        hour = calendar.get(Calendar.HOUR);
        minute = calendar.get(Calendar.MINUTE);
    }

    private void showUpdateDatePicker(final EditText editText) {
        try {
            initDateTime();
            Log.e("showUpdateDatePicker---", "---");
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Log.e("onclick----", date.toString());
                    if (date.length() > 0) { //清除上次记录的日期
                        date.delete(0, date.length());
                    }

                    String datestr = date.append(year).append("-").append(getnumberString(month))
                            .append("-").append(getnumberString(day)).append(" ").toString();
                    showUpdateTimePicker(editText, datestr);
                    dialog.dismiss();
                }
            });
            builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            AlertDialog dialog = builder.create();
            View dialogView = View.inflate(this, R.layout.dialog_date, null);
            DatePicker datePicker = dialogView.findViewById(R.id.datePicker);
            dialog.setTitle(getString(R.string.SetDate));
            dialog.setView(dialogView);
            dialog.show();
            datePicker.init(year, month - 1, day, new DatePicker.OnDateChangedListener() {
                @Override
                public void onDateChanged(DatePicker view, int years, int monthOfYears, int dayOfMonths) {
                    Log.e("datachange---", years + "-" + monthOfYears + "-" + dayOfMonths);
                    year = years;
                    month = monthOfYears + 1;
                    day = dayOfMonths;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showUpdateTimePicker(final EditText editText, final String datestr) {
        AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
        builder2.setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (time.length() > 0) {
                    time.delete(0, time.length());
                }
                String timestr = time.append(getnumberString(hour)).append(":")
                        .append(getnumberString(minute)).append(":").append("00").toString();
                String datetime = datestr + timestr;
                editText.setText(datetime);
                dialog.dismiss();
            }
        });
        builder2.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog2 = builder2.create();
        View dialogView2 = View.inflate(this, R.layout.dialog_time, null);
        TimePicker timePicker = dialogView2.findViewById(R.id.timePicker);
        timePicker.setCurrentHour(hour);
        timePicker.setCurrentMinute(minute);
        timePicker.setIs24HourView(true);
        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minutes) {
                hour = hourOfDay;
                minute = minutes;
            }
        });
        dialog2.setTitle(getString(R.string.SetTime));
        dialog2.setView(dialogView2);
        dialog2.show();
    }

    private void showDeleteDialog(final RegisteredMembers members) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_delete, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view).setCancelable(false);
        mDeleteDialog = builder.create();

        Button confirm = view.findViewById(R.id.delete_confirm);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sharedPreferences.getBoolean(GlobalParameters.ONLINE_MODE, true)) {
                    if (members.getUniqueid() != null) {

                                mdeleteprogressDialog = ProgressDialog.show(ManagementActivity.this, getString(R.string.delete), getString(R.string.delete_wait));
                                try {
                                    isDeleted = true;
                                    JSONObject obj = new JSONObject();
                                    obj.put("id", members.getUniqueid());
                                    obj.put("firstName", members.getFirstname());
                                    obj.put("lastname", members.getLastname());
                                    obj.put("email", members.getEmail());
                                    obj.put("phoneNumber", members.getMobile());
                                    obj.put("memberId", members.getMemberid());
                                    obj.put("accessId", members.getAccessid());
                                    obj.put("faceTemplate", Util.encodeImagePath(members.getImage()));
                                    obj.put("status", false);
                                    obj.put("memberType", 1);
                                    new AsyncJSONObjectManageMember(obj, ManagementActivity.this, sharedPreferences.getString(GlobalParameters.URL, EndPoints.prod_url) + EndPoints.ManageMember, ManagementActivity.this).execute();
                                } catch (Exception e) {
                                    Logger.error(LOG + "AsyncJSONObjectMemberManage", e.getMessage());
                                }
                                }

                } else {
                    localDelete(members);
                }
            }
        });
        Button cancel = view.findViewById(R.id.delete_cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDeleteDialog.dismiss();
            }
        });

        if (mDeleteDialog != null && !mDeleteDialog.isShowing()) {
            mDeleteDialog.show();
        }
    }


    @SuppressLint("ResourceAsColor")
    private void showRegisterPopupwindow() {
        View view = LayoutInflater.from(this).inflate(R.layout.popup_register, null);
        mpopupwindow = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mpopupwindow.setOutsideTouchable(false);
        mpopupwindow.setFocusable(true);
        mpopupwindow.setAnimationStyle(R.style.animTranslate);
        mpopupwindow.setBackgroundDrawable(new ColorDrawable(R.color.backwhite));

        registerpath = "";
        final EditText mfirstname = view.findViewById(R.id.popup_first_name);
        final EditText mlastname = view.findViewById(R.id.popup_last_name);
        final EditText mmobile = view.findViewById(R.id.popup_mobile);
        final EditText mmemberid = view.findViewById(R.id.popup_member_id);
        final EditText memail = view.findViewById(R.id.popup_email);
        registerAccessid = view.findViewById(R.id.popup_access_id);
        final EditText muniqueid = view.findViewById(R.id.popup_unique_id);
        final TextInputLayout text_input_member_id = view.findViewById(R.id.text_input_member_id);
        final TextInputLayout text_input_access_id = view.findViewById(R.id.text_input_access_id);
        //final EditText mregistertime = view.findViewById(R.id.popup_time);
//        mregistertime.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                if (hasFocus) {
//                    showUpdateDatePicker(mregistertime);
//                } else {
//                    Log.e("hasfocus---", "false");
//                }
//            }
//        });

        mregisterfaceimg = view.findViewById(R.id.popup_faceimg);
        musephoto = view.findViewById(R.id.popup_use_photo);
        mtakephoto = view.findViewById(R.id.popup_take_photo);
        popupEnrollBtn = view.findViewById(R.id.popup_btn_enroll);

/*        mregisterfaceimg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takephoto();
            }
        });*/

        mtakephoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takephoto();
            }
        });

        musephoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gallaryPhoto("register.jpg", registerpath, PICK_IMAGE);
            }
        });

        ImageView mexit = view.findViewById(R.id.popup_exit);
        mexit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mpopupwindow != null && mpopupwindow.isShowing()) {
                    mpopupwindow.dismiss();
                    Log.e("mexit2---", "click---");
                }
            }
        });

        popupEnrollBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "Please scan the card to enroll", Toast.LENGTH_LONG).show();
            }
        });

        Button mregister = view.findViewById(R.id.btn_register);
        mregister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String firstnamestr = mfirstname.getText().toString();
                String lastnamestr = mlastname.getText().toString();
                String mobilestr = mmobile.getText().toString();
                String memberidstr = mmemberid.getText().toString();
                String emailstr = memail.getText().toString();
                String accessstr = registerAccessid.getText().toString();
                String uniquestr = muniqueid.getText().toString();
                isUpdate = false;

                //String timestr = mregistertime.getText().toString();

                Log.e("info---", firstnamestr + "-" + lastnamestr + "-" + mobilestr + "-" + memberidstr + "-" + emailstr + accessstr + "-" + uniquestr);
                if (!TextUtils.isEmpty(memberidstr)) {
                    if (isMemberExist(memberidstr)) {
                        Toast.makeText(ManagementActivity.this, getString(R.string.toast_manage_member_exist), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (isAccessIdExist(accessstr) && !accessstr.equals(null)) {
                        Toast.makeText(ManagementActivity.this, getString(R.string.toast_manage_access_exist), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    mprogressDialog = ProgressDialog.show(ManagementActivity.this, getString(R.string.Register), getString(R.string.register_wait));
//                        if(isValidDate(timestr,"yyyy-MM-dd HH:mm:ss")) {
//                            mprogressDialog = ProgressDialog.show(ManagementActivity.this, getString(R.string.Register), getString(R.string.register_wait));
//                            localRegister(namestr, mobilestr, timestr, registerpath);
//                        }else{
//                            Toast.makeText(ManagementActivity.this, getString(R.string.toast_manage_dateerror), Toast.LENGTH_SHORT).show();
//                        }
                    if (sharedPreferences.getBoolean(GlobalParameters.ONLINE_MODE, true)) {
                        try {
                            JSONObject obj = new JSONObject();
                            obj.put("id", "");
                            obj.put("firstName", firstnamestr);
                            obj.put("lastname", lastnamestr);
                            obj.put("email", emailstr);
                            obj.put("phoneNumber", mobilestr);
                            obj.put("memberId", memberidstr);
                            obj.put("accessId", accessstr);
                            obj.put("faceTemplate", Util.encodeImagePath(registerpath));
                            obj.put("status", true);
                            obj.put("memberType", 1);
                            new AsyncJSONObjectManageMember(obj, ManagementActivity.this, sharedPreferences.getString(GlobalParameters.URL, EndPoints.prod_url) + EndPoints.ManageMember, ManagementActivity.this).execute();
                        } catch (Exception e) {
                            Logger.error(LOG + "AsyncJSONObjectMemberManage", e.getMessage());
                        }
                    } else {
                        localRegister(firstnamestr, lastnamestr, mobilestr, memberidstr, emailstr, accessstr, uniquestr, registerpath, "");
                    }

                } else if (TextUtils.isEmpty(memberidstr) || TextUtils.isEmpty(accessstr)) {
                    text_input_member_id.setError("Member Id should not be empty");
                } else if (TextUtils.isEmpty(accessstr)) {
                    text_input_access_id.setError("Access Id should not be empty");
                }
            }
        });

        View parent = LayoutInflater.from(ManagementActivity.this).inflate(R.layout.activity_management, null);
        if (mpopupwindow != null && !mpopupwindow.isShowing()) {
            mpopupwindow.showAtLocation(parent, Gravity.CENTER, 0, 0);
        }
    }

    private void showResult(String data) {
        DismissProgressDialog(mprogressDialog);
        Util.showToast(ManagementActivity.this, data);
    }

    private boolean processImg(String name, String imgpath, String id) {
        if (imgpath.isEmpty()) return false;
        Bitmap bitmap = BitmapFactory.decodeFile(imgpath);
        bitmap = ArcSoftImageUtil.getAlignedBitmap(bitmap, true);
        if (bitmap == null) {
            Log.e("tag", "fail to translate bitmap");
            showResult(getString(R.string.toast_translateBitmapfail));
            return false;
        }
        byte[] bgr24 = ArcSoftImageUtil.createImageData(bitmap.getWidth(), bitmap.getHeight(), ArcSoftImageFormat.BGR24);
        int transformCode = ArcSoftImageUtil.bitmapToImageData(bitmap, bgr24, ArcSoftImageFormat.BGR24);
        if (transformCode != ArcSoftImageUtilError.CODE_SUCCESS) {
        }
        boolean success = FaceServer.getInstance().registerBgr24(ManagementActivity.this, bgr24, bitmap.getWidth(),
                bitmap.getHeight(), name, id);
        return success;
    }

    public boolean registerDatabase(String firstname, String lastname, String mobile, String id, String email, String accessid, String uniqueid) {
        try {
            String username = firstname + "-" + id;
            String image = ROOT_PATH_STRING + File.separator + FaceServer.SAVE_IMG_DIR + File.separator + username + FaceServer.IMG_SUFFIX;
            String feature = ROOT_PATH_STRING + File.separator + FaceServer.SAVE_FEATURE_DIR + File.separator + username;
            Log.e("tag", "image_uri---" + image + "  feature_uri---" + feature);

        RegisteredMembers registeredMembers = new RegisteredMembers();
        registeredMembers.setFirstname(firstname);
        registeredMembers.setLastname(lastname);
        registeredMembers.setMobile(mobile);
        registeredMembers.setStatus("1");
        registeredMembers.setMemberid(id);
        registeredMembers.setEmail(email);
        registeredMembers.setAccessid(accessid);
        registeredMembers.setUniqueid(uniqueid);
//      registeredMembers.setExpire_time(time);
        registeredMembers.setImage(image);
        registeredMembers.setFeatures(feature);
        boolean result = registeredMembers.save();
        return result;
        }catch (Exception e){
            Logger.debug("boolean registerDatabase(String firstname, String lastname, String mobile, String id, String email, String accessid, String uniqueid) {",e.getMessage());
        }
        return false;
    }


    private boolean isMemberExist(String memberId) {
        List<RegisteredMembers> membersList = LitePal.where("memberid = ?", memberId).find(RegisteredMembers.class);
        if (membersList != null && membersList.size() > 0) {
            return true;
        }
        return false;
    }

    private boolean isAccessIdExist(String accessId) {
        List<RegisteredMembers> membersList = LitePal.where("accessid = ?", accessId).find(RegisteredMembers.class);
        if (membersList == null && membersList.size() > 0) {
            return true;
        }
        return false;
    }

    private boolean isCertifyIdExist(String uniqueID) {
        List<RegisteredMembers> membersList = LitePal.where("uniqueid = ?", uniqueID).find(RegisteredMembers.class);
        if (membersList != null && membersList.size() > 0) {
            return true;
        }
        return false;
    }

    private void localRegister(String firstname, String lastname, String mobile, String id, String email, String accessid, String uniqueid, String imgpath, String sync) {
        String data = "";
        Log.d(TAG, "Snap Member id : " + id);
        File imageFile = new File(imgpath);
        if (processImg(firstname + "-" + id, imgpath, id) || !imageFile.exists()) {
            if (registerDatabase(firstname, lastname, mobile, id, email, accessid, uniqueid)) {
                if (!sync.equals("sync"))
                    showResult(getString(R.string.Register_success));
                handler.obtainMessage(REGISTER).sendToTarget();
                refresh();
                File file = new File(registerpath);
                if (file.exists()) {
                    file.delete();
                    registerpath = "";
                }
            } else {
                Log.e("tag", "Register failed");
                if (!sync.equals("sync"))
                    showResult(getString(R.string.register_failed));
            }
        } else {
            Log.e("tag", "fail to process bitmap");
            if (!sync.equals("sync"))
                showResult(getString(R.string.register_face_not_recognized));
        }
    }

    public void localUpdate(String oldId, String fistname, String lastname, String mobile, String id, String email, String accessid, String uniqueid, String imagePath) {
        String data = "";
        List<RegisteredMembers> list = LitePal.where("memberid = ?", oldId).find(RegisteredMembers.class);
        if (list != null && list.size() > 0) {

            DismissProgressDialog(mprogressDialog);
            File file = new File(imagePath);
            String filepath = Environment.getExternalStorageDirectory() + "/pic/update.jpg";
            if (file.exists() && filepath.equalsIgnoreCase(imagePath)) {
                if (processImg(fistname + "-" + id, imagePath, oldId)) {
                    RegisteredMembers Members = list.get(0);
                    Members.setFirstname(fistname);
                    Members.setLastname(lastname);
                    Members.setMobile(mobile);
                    Members.setMemberid(id);
                    Members.setEmail(email);
                    Members.setAccessid(accessid);
                    Members.setUniqueid(uniqueid);
                    //Members.setExpire_time(time);
                    Members.setStatus(Members.getStatus());
                    Members.setImage(Members.getImage());
                    Members.setFeatures(Members.getFeatures());
                    Members.save();

                    file.delete();
                    updateimagePath = "";
                    dismissUpdateDialog();
                    data = getString(R.string.Update_success);
                } else {
                    data = getString(R.string.update_face_not_recognized);
                }
            } else {
                RegisteredMembers Members = list.get(0);

                String newimage = "";
                String newfeature = "";
                //if(!oldmobile.equals(mobile)){
                String oldimage = Members.getImage();
                String oldfeature = Members.getFeatures();
                newimage = ROOT_PATH_STRING + File.separator + FaceServer.SAVE_IMG_DIR + File.separator + fistname + "-" + id + FaceServer.IMG_SUFFIX;
                newfeature = ROOT_PATH_STRING + File.separator + FaceServer.SAVE_FEATURE_DIR + File.separator + fistname + "-" + id;
                renameFile(oldimage, newimage);
                renameFile(oldfeature, newfeature);
//                }else{
//                    newimage = Members.getImage();
//                    newfeature = Members.getFeatures();
//                }
                Members.setFirstname(fistname);
                Members.setLastname(lastname);
                Members.setMobile(mobile);
                Members.setEmail(email);
                Members.setMemberid(id);
                Members.setAccessid(accessid);
                Members.setUniqueid(uniqueid);
                //Members.setExpire_time(time);
                Members.setStatus(Members.getStatus());
                Members.setImage(newimage);
                Members.setFeatures(newfeature);
                Members.save();

                dismissUpdateDialog();
                data = getString(R.string.Update_success);
            }
            handler.obtainMessage(TOAST, data).sendToTarget();

        }
    }

    /**
     * @param oldPath
     * @param newPath
     */
    public static void renameFile(String oldPath, String newPath) {
        File oleFile = new File(oldPath);
        File newFile = new File(newPath);
        oleFile.renameTo(newFile);
    }

    private void dismissUpdateDialog() {
        if (mUpdateDialog != null && mUpdateDialog.isShowing()) {
            mUpdateDialog.dismiss();
            mUpdateDialog = null;
        }
        refresh();
        handler.obtainMessage(UPDATE).sendToTarget();
    }

    public boolean deleteDatabase(String name, String id) {
        List<RegisteredMembers> list = LitePal.where("memberid = ?", id).find(RegisteredMembers.class);
        if (list != null && list.size() > 0) {
            FaceServer.getInstance().deleteInfo(name + "-" + id);
            String featurePath = list.get(0).getFeatures();
            String imgPath = list.get(0).getImage();
            int line = LitePal.deleteAll(RegisteredMembers.class, "memberid = ?", id);
            Log.e("tag", "line---" + line);
            File featureFile = new File(featurePath);
            File imgFile = new File(imgPath);
            if (featureFile.exists() && featureFile.isFile()) {
                boolean featureDeleteResult = featureFile.delete();
                if (featureDeleteResult) {
                    FaceServer.getInstance().deleteInfo(featureFile.getName());
                    Log.e("tag", "feature delete success---" + featurePath);
                }
            }
            if (imgFile.exists() && imgFile.isFile()) {
                boolean imgDeleteResult = imgFile.delete();
                if (imgDeleteResult) {
                    Log.e("tag", "image delete success---" + featurePath);
                }
            }
            return line > 0;
        }
        return false;
    }

    private void localDelete(RegisteredMembers members) {
        String data = "";
        DismissProgressDialog(mdeleteprogressDialog);
        if (deleteDatabase(members.getFirstname(), members.getMemberid())) {
            DismissDialog(mDeleteDialog);
            data = getString(R.string.Delete_success);
            refresh();
        } else {
            data = getString(R.string.Delete_failed);
        }
        handler.obtainMessage(TOAST, data).sendToTarget();
    }

    private void takephoto() {
        File outputImage = new File(getExternalCacheDir(), "register.jpg");
        if (outputImage.exists()) outputImage.delete();
        try {
            outputImage.createNewFile();
            Log.e("registerpath----", outputImage.getPath() + "-" + registerpath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (Build.VERSION.SDK_INT >= 24) {
            registerUri = FileProvider.getUriForFile(ManagementActivity.this, "com.certify.snap.fileprovider", outputImage);
        } else {
            registerUri = Uri.fromFile(outputImage);
        }
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, registerUri);
        startActivityForResult(intent, REGISTER_PHOTO);
    }

    private void gallaryPhoto(String imageName, String imagePath, int resultCode) {
        File outputImage = new File(getExternalCacheDir(), imageName);
        if (outputImage.exists()) outputImage.delete();
        try {
            outputImage.createNewFile();
            Log.e("gallaypath----", outputImage.getPath() + "-" + imagePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Intent gallery = new Intent();
        gallery.setType("image/*");
        gallery.setAction(Intent.ACTION_GET_CONTENT);

        startActivityForResult(Intent.createChooser(gallery, "Sellect Picture"), resultCode);
    }

    private void takefacephoto() {
        File outputImage = new File(getExternalCacheDir(), "update.jpg");
        if (outputImage.exists()) outputImage.delete();
        try {
            outputImage.createNewFile();
            Log.e("updatepath----", outputImage.getPath() + "\n" + updateimagePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (Build.VERSION.SDK_INT >= 24) {
            updateUri = FileProvider.getUriForFile(ManagementActivity.this, "com.certify.snap.fileprovider", outputImage);
        } else {
            updateUri = Uri.fromFile(outputImage);
        }
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, updateUri);
        startActivityForResult(intent, UPDATE_PHOTO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REGISTER_PHOTO:
                if (resultCode == RESULT_OK) {
                    try {
                        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(registerUri));
                        if (model.contains("950") || "TPS980Q".equals(Build.MODEL))
                            bitmap = Util.rotateToDegrees(bitmap, 90);
                        registerpath = Util.saveBitmapFile(bitmap, "register.jpg");
                        mregisterfaceimg.setImageBitmap(bitmap);
                        Log.e("onactivityresult---", "set register bitmap-" + registerpath);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            case UPDATE_PHOTO:
                if (resultCode == RESULT_OK) {
                    try {
                        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(updateUri));
                        if (model.contains("950") || "TPS980Q".equals(Build.MODEL))
                            bitmap = Util.rotateToDegrees(bitmap, 90);
                        updateimagePath = Util.saveBitmapFile(bitmap, "update.jpg");
                        mfaceimg.setImageBitmap(bitmap);
                        Log.e("onactivityresult----", "set update bitmap");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            case PICK_IMAGE:
                if (resultCode == RESULT_OK && data != null) {
                    imageUri = data.getData();

                    try {
                        Bitmap bitmap1;
                        bitmap1 = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                        registerpath = Util.saveBitmapFile(bitmap1, "register.jpg");
                        mregisterfaceimg.setImageBitmap(bitmap1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case UPDATE_PICK_IMAGE:
                if (resultCode == RESULT_OK && data != null) {
                    imageUri = data.getData();

                    try {
                        Bitmap bitmap1;
                        bitmap1 = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                        updateimagePath = Util.saveBitmapFile(bitmap1, "update.jpg");
                        mfaceimg.setImageBitmap(bitmap1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }


    private void DismissDialog(AlertDialog dialog) {
        if (dialog != null && dialog.isShowing())
            dialog.dismiss();
    }

    private void DismissProgressDialog(ProgressDialog progress) {
        if (progress != null && progress.isShowing())
            progress.dismiss();
    }

    private void refresh() {
        initData(false);
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String info = (String) msg.obj;
            switch (msg.what) {
                case UPDATE:
                    setViewclick(mfaceimg, false);
                    break;
                case TOAST:
                    Util.showToast(ManagementActivity.this, info);
                    break;
                case REGISTER:
                    if (mpopupwindow != null && mpopupwindow.isShowing()) {
                        mpopupwindow.dismiss();
                        Log.e("register success---", "mpopupwindow close---");
                    }
                    break;
            }
        }
    };

    private boolean ProcessPic(File picfile) {
        Bitmap bitmap = BitmapFactory.decodeFile(picfile.getAbsolutePath());
        if (bitmap == null) {
            Log.e("tag", "fail to get bitmap--");
            return false;
        }
        bitmap = ArcSoftImageUtil.getAlignedBitmap(bitmap, true);
        if (bitmap == null) {
            Log.e("tag", "fail to translate bitmap");
            return false;
        }
        byte[] bgr24 = ArcSoftImageUtil.createImageData(bitmap.getWidth(), bitmap.getHeight(), ArcSoftImageFormat.BGR24);
        int transformCode = ArcSoftImageUtil.bitmapToImageData(bitmap, bgr24, ArcSoftImageFormat.BGR24);
        if (transformCode != ArcSoftImageUtilError.CODE_SUCCESS) {
        }
        boolean success = FaceServer.getInstance().registerBgr24Test(ManagementActivity.this, bgr24, bitmap.getWidth(), bitmap.getHeight());
        return success;
    }

    private void initNfc() {
        mNfcAdapter = M1CardUtils.isNfcAble(this);
        mPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
    }

    private void enableNfc() {
        if (mNfcAdapter != null) {
            mNfcAdapter.enableForegroundDispatch(this, mPendingIntent, null, null);
        }
    }

    private void disableNfc() {
        if (mNfcAdapter != null) {
            mNfcAdapter.disableForegroundDispatch(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FaceServer.getInstance().unInit();
    }

    private static String bytearray2Str(byte[] data, int start, int length, int targetLength) {
        long number = 0;
        if (data.length < start + length) {
            return "";
        }
        for (int i = 1; i <= length; i++) {
            number *= 0x100;
            number += (data[start + length - i] & 0xFF);
        }
        return String.format("%0" + targetLength + "d", number);
    }

    private static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    @Override
    public void onJSONObjectListenerManageMember(String reportInfo, String status, JSONObject responseData) {
        if (reportInfo != null || responseData != null) {
            try {
                JSONObject json = new JSONObject(reportInfo);
                if (json.getInt("responseCode") == 1) {
                    String firstnamestr = responseData.getString("firstName");
                    String lastnamestr = responseData.getString("lastname");
                    String emailstr = responseData.getString("email");
                    String mobilestr = responseData.getString("phoneNumber");
                    String memberidstr = responseData.getString("memberId");
                    String accessstr = responseData.getString("accessId");
                    String uniquestr = json.getString("id");
                    String image = responseData.getString("faceTemplate");
                    String statusStr = responseData.getString("status");
                    //mprogressDialog = ProgressDialog.show(ManagementActivity.this, getString(R.string.Register), getString(R.string.register_wait));
                    if (isUpdate) {
                        localUpdate(datalist.get(listPosition).getMemberid(), firstnamestr, lastnamestr, mobilestr, memberidstr, emailstr, accessstr, uniquestr, updateimagePath);
                    } else if (isDeleted) {
                        RegisteredMembers members = new RegisteredMembers();
                        members.setFirstname(firstnamestr);
                        members.setLastname(lastnamestr);
                        members.setEmail(emailstr);
                        members.setMobile(mobilestr);
                        members.setMemberid(memberidstr);
                        members.setAccessid(accessstr);
                        members.setUniqueid(uniquestr);
                        members.setImage(image);
                        members.setStatus(statusStr);

                        localDelete(members);
                        isDeleted = false;
                    } else {
                        localRegister(firstnamestr, lastnamestr, mobilestr, memberidstr, emailstr, accessstr, uniquestr, registerpath, "");
                    }
//                        if(isValidDate(timestr,"yyyy-MM-dd HH:mm:ss")) {
//                            mprogressDialog = ProgressDialog.show(ManagementActivity.this, getString(R.string.Register), getString(R.string.register_wait));
//                            localRegister(namestr, mobilestr, timestr, registerpath);
//                        }else{
//                            Toast.makeText(ManagementActivity.this, getString(R.string.toast_manage_dateerror), Toast.LENGTH_SHORT).show();
//                        }
                } else {
                    if (isDeleted) {
                        deletionFailed();
                    } else {
                        if (isUpdate){
                            showResult(getString(R.string.update_failed));
                            isUpdate = false;
                        }else {
                            showResult(getString(R.string.register_failed));
                        }
                    }
                }

            } catch (Exception e) {
                if (isDeleted){
                    deletionFailed();
                } else {
                    DismissProgressDialog(mprogressDialog);
                }
            }
        } else {
            deletionFailed();
        }

    }

    private void deletionFailed() {
        if (isDeleted) {
            showResult(getString(R.string.deletion_failed));
            DismissProgressDialog(mdeleteprogressDialog);
            isDeleted = false;
        }
    }

    private void resetUpdateMember() {
        updateMember = null;
    }

    private int totalMemberCount,totalMemberLastcount;
    @Override
    public void onJSONObjectListenerMemberList(JSONObject reportInfo, String status, JSONObject req) {
        try {
            if (reportInfo == null) {
                return;
            }
            if (!reportInfo.isNull("Message")) {
//                if (reportInfo.getString("Message").contains("token expired"))
//                    Util.getToken(ManagementActivity.this, this);
            } else {
                if (reportInfo.isNull("responseCode")) return;
                if (reportInfo.getString("responseCode").equals("1")) {
                    JSONArray memberList = reportInfo.getJSONArray("responseData");
                    totalMemberCount = memberList.length();
                    totalMemberLastcount = memberList.length()-1;
                   // System.out.println("NagaTest onJSONObjectListenerMemberList memberList.size: " + memberList.length() );
                    for (int i = 0; i < memberList.length(); i++) {
                        JSONObject c = memberList.getJSONObject(i);

                        String certifyId = c.getString("id");
                        String memberId = c.getString("memberId");
                        String accessId = c.getString("accessId");

                       // System.out.println("NagaTest onJSONObjectListenerMemberList memberList: " + i + "/" + memberList.length());
                        JSONObject obj = new JSONObject();
                        obj.put("id", certifyId);

                        //Intent intent = new Intent(this, MemberDataService.class);
                        //intent.putExtra()
                        //startService(intent);
                        new AsyncGetMemberData(obj, this, sharedPreferences.getString(GlobalParameters.URL,
                                EndPoints.prod_url) + EndPoints.GetMemberById, this).execute();

                        //Toast.makeText(this, "Loading: "+count++ +" out of "+memberList.length(), Toast.LENGTH_SHORT).show();
                        //updateRecordMsg(i, memberList.length());

                    }

                } else {
                    Logger.toast(this, "Something went wrong please try again");
                }
            }

        } catch (Exception e) {
            Logger.error("onJSONObjectListenerSetting(String report, String status, JSONObject req)", e.getMessage());
        }

    }

    private int testCount=1;
    public void onJSONObjectListenerMemberID(final JSONObject reportInfo, String status, JSONObject req) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (reportInfo == null) {
                        return;
                    }
                    if (!reportInfo.isNull("Message")) {
//                if (reportInfo.getString("Message").contains("token expired"))
//                    Util.getToken(ManagementActivity.this, this);
                    } else {
                        if (reportInfo.isNull("responseCode")) return;
                        if (reportInfo.getString("responseCode").equals("1")) {
                            JSONArray memberList = reportInfo.getJSONArray("responseData");
                            for (int i = 0; i < memberList.length(); i++) {
                                JSONObject c = memberList.getJSONObject(i);

                                String certifyId = c.getString("id");
                                String memberId = c.getString("memberId").replaceAll("[-+.^:,]","");
                                if (memberId.isEmpty()) {
                                    memberId = certifyId;
                                }
                                String accessId = c.getString("accessId");
                                String firstName = c.getString("firstName");
                                String lastName = c.getString("lastName");
                                String email = c.getString("email");
                                String phoneNumber = c.getString("phoneNumber");
                                String faceTemplate = c.getString("faceTemplate");
                                Boolean statusVal = c.getBoolean("status");
                                String accountId = c.getString("accountId");
                                String memberType = c.getString("memberType");

                                String imagePath = MemberUtilData.getImagePath(faceTemplate);


                                if (statusVal){
                                   // Thread.sleep(200);

                                    if (isCertifyIdExist(certifyId)) {
                                        MemberUtilData.deleteDatabaseCertifyId(firstName, certifyId);
                                        localRegister(firstName, lastName, phoneNumber, memberId, email, accessId, certifyId, imagePath, "sync");

                                    } else {
                                        MemberUtilData.deleteDatabaseCertifyId(firstName, certifyId);
                                        localRegister(firstName, lastName, phoneNumber, memberId, email, accessId, certifyId, imagePath, "sync");
                                    }
                                    initData(true);
                                }else {
                                    totalMemberCount--;
                                    totalMemberLastcount--;

                                }
                            }
                            DismissProgressDialog(mloadingprogress);
                           // initData(true);
                        } else {
                            DismissProgressDialog(mloadingprogress);
                            Logger.toast(ManagementActivity.this, "Something went wrong please try again");
                        }
                    }
                } catch (Exception e) {
                    DismissProgressDialog(mloadingprogress);
                    Logger.error("onJSONObjectListenerSetting(String report, String status, JSONObject req)", e.getMessage());
                    //initData(true);
                }
            }
        });
    }
}