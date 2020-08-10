package com.certify.snap.activity;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
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

import com.certify.snap.api.response.MemberListData;
import com.certify.snap.api.response.MemberListResponse;
import com.certify.snap.async.AsyncTaskExecutorService;
import com.certify.snap.controller.DatabaseController;
import com.certify.snap.model.MemberSyncDataModel;
import com.certify.snap.service.HIDService;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AlertDialog;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
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
import com.certify.snap.common.Util;
import com.certify.snap.faceserver.FaceServer;
import com.certify.snap.model.RegisteredFailedMembers;
import com.certify.snap.model.RegisteredMembers;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.LitePal;
import org.litepal.crud.callback.FindMultiCallback;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.certify.snap.common.Util.getnumberString;

public class ManagementActivity extends SettingBaseActivity implements ManageMemberCallback,
        MemberListCallback, MemberIDCallback, MemberSyncDataModel.SyncDataCallBackListener {

    protected static final String TAG = ManagementActivity.class.getSimpleName();
    private EditText msearch;
    private TextView mCountTv;
    private RecyclerView recyclerView, failed_recyclerView;
    private MemberAdapter memberAdapter;
    private MemberFailedAdapter memberfailedAdapter;
    private List<RegisteredMembers> datalist = new ArrayList<>();
    private List<RegisteredFailedMembers> faillist = new ArrayList<>();
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
    int listPosition;
    int count=0;
    private BroadcastReceiver hidReceiver;
    private int activeMemberCount = 0;
    private int totalMemberCount = 0;
    private ExecutorService taskExecutorService;
    public String OFFLINE_FAILED_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "offline/failed/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_management);
        relative_management = findViewById(R.id.relative_management);
        mCountTv = findViewById(R.id.count_tv);

        Application.getInstance().addActivity(this);
        sharedPreferences = Util.getSharedPreferences(this);
        initHidReceiver();

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
                if (memberAdapter != null) {
                    memberAdapter.getFilter().filter(s.toString());
                }
            }
        });

        initData(true);
        initNfc();
        initMemberSync();
    }

    @Override
    protected void onResume() {
        super.onResume();
        enableNfc();
        LocalBroadcastManager.getInstance(this).registerReceiver(hidReceiver, new IntentFilter(HIDService.HID_BROADCAST_ACTION));
    }

    @Override
    protected void onPause() {
        super.onPause();
        disableNfc();
        if (hidReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(hidReceiver);
        }
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
        onRfidScan(id);
        if (popupEnrollBtn != null)
            popupEnrollBtn.setVisibility(View.GONE);
    }

    public synchronized void onmemberclick(View v) {
        switch (v.getId()) {
            case R.id.refresh:
                if (memberAdapter != null || memberfailedAdapter != null) {
                    //refresh();
                    if(sharedPreferences.getBoolean(GlobalParameters.ONLINE_MODE, true)&&!sharedPreferences.getBoolean(GlobalParameters.MEMBER_SYNC_DO_NOT,false)) {

                        count = 0;
                        testCount = 1;
                        activeMemberCount = totalMemberCount = 0;
                        datalist.clear();
                        mloadingprogress = ProgressDialog.show(ManagementActivity.this, "Loading", "Loading please wait...");
                        Util.getmemberList(this, this);
                    }
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
            Observable.create(new ObservableOnSubscribe<List<RegisteredMembers>>() {
                @Override
                public void subscribe(ObservableEmitter<List<RegisteredMembers>> emitter) throws Exception {
                    List<RegisteredMembers> membersList = DatabaseController.getInstance().findAll();
                    emitter.onNext(membersList);
                }
            }).subscribeOn(Schedulers.computation())
              .observeOn(AndroidSchedulers.mainThread())
              .subscribe(new Observer<List<RegisteredMembers>>() {
                  Disposable disposable;
                  @Override
                  public void onSubscribe(Disposable d) {
                      disposable = d;
                  }

                  @Override
                  public void onNext(List<RegisteredMembers> list) {
                      if (list != null) {
                          datalist = list;
                          if (isNeedInit) {
                              initMember();
                          } else {
                              refreshMemberList(list);
                          }
                      }
                      disposable.dispose();
                  }

                  @Override
                  public void onError(Throwable e) {
                      Log.e(TAG, "Error in adding the member to data model from database");
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

    private void initMember() {
        mCountTv.setText(String.valueOf(datalist.size()));
        if (memberAdapter == null) {
            memberAdapter = new MemberAdapter(ManagementActivity.this, datalist);
        }
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

    private void refreshMemberList(List<RegisteredMembers> memberlist) {
        Log.e("refreshMemberList---", "start-" + memberlist.toString());
        datalist = memberlist;
        memberAdapter.refresh(datalist);
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

                        if (!idstr.equals(updateMember.getMemberid()) && DatabaseController.getInstance().isMemberExist(idstr)) {
                            Toast.makeText(ManagementActivity.this, getString(R.string.toast_manage_member_exist), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (!accessstr.equals(null) && !accessstr.equals(updateMember.getAccessid()) &&
                                DatabaseController.getInstance().isAccessIdExist(accessstr)) {
                            Toast.makeText(ManagementActivity.this, getString(R.string.toast_manage_access_exist), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        mprogressDialog = ProgressDialog.show(ManagementActivity.this, "Update", "Update! Please wait...");
//                        if(isValidDate(timestr,"yyyy-MM-dd HH:mm:ss")) {
//                            mprogressDialog = ProgressDialog.show(ManagementActivity.this, "Update", "Update! Please wait...");
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
                                Logger.error(TAG + "AsyncJSONObjectMemberManage", e.getMessage());
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
                                    Logger.error(TAG + "AsyncJSONObjectMemberManage", e.getMessage());
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
                    if (DatabaseController.getInstance().isMemberExist(memberidstr)) {
                        Toast.makeText(ManagementActivity.this, getString(R.string.toast_manage_member_exist), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (DatabaseController.getInstance().isAccessIdExist(accessstr) && !accessstr.equals(null)) {
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
                            Logger.error(TAG + "AsyncJSONObjectMemberManage", e.getMessage());
                        }
                    } else {
                        localRegister(firstnamestr, lastnamestr, mobilestr, memberidstr, emailstr, accessstr, uniquestr, registerpath, "", Util.currentDate());
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

    private void localRegister(String firstname, String lastname, String mobile, String id, String email, String accessid, String uniqueid, String imgpath, String sync, String dateTime) {
        String data = "";
        Log.d(TAG, "Snap Member id : " + id);
        File imageFile = new File(imgpath);
        if (MemberSyncDataModel.getInstance().processImg(firstname + "-" + id, imgpath, id, this) || !imageFile.exists()) {
            if (MemberSyncDataModel.getInstance().registerDatabase(firstname, lastname, mobile, id, email, accessid, uniqueid, this, dateTime)) {
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
        List<RegisteredMembers> list = DatabaseController.getInstance().findMember(oldId);
        if (list != null && list.size() > 0) {

            DismissProgressDialog(mprogressDialog);
            File file = new File(imagePath);
            String filepath = Environment.getExternalStorageDirectory() + "/pic/update.jpg";
            if (file.exists() && filepath.equalsIgnoreCase(imagePath)) {
                if (MemberSyncDataModel.getInstance().processImg(fistname + "-" + id, imagePath, oldId, this)) {
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
                    Members.setDateTime(Util.currentDate());
                    //Members.save();
                    DatabaseController.getInstance().insertMemberToDB(Members);

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
                //Members.save();

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
        List<RegisteredMembers> list = DatabaseController.getInstance().findMember(id);
        if (list != null && list.size() > 0) {
            FaceServer.getInstance().deleteInfo(name + "-" + id);
            String featurePath = list.get(0).getFeatures();
            String imgPath = list.get(0).getImage();
            int line = DatabaseController.getInstance().deleteMember(id);
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

    private void initNfc() {
        Log.v(TAG, "initNfc");
        mNfcAdapter = M1CardUtils.isNfcAble(this);
        mPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
    }

    private void enableNfc() {
        Log.v(TAG, "enableNfc");
        if (mNfcAdapter != null && mNfcAdapter.isEnabled()) {
            mNfcAdapter.enableForegroundDispatch(this, mPendingIntent, null, null);
        }
    }

    private void disableNfc() {
        Log.v(TAG, "disableNfc");
        if (mNfcAdapter != null && mNfcAdapter.isEnabled()) {
            mNfcAdapter.disableForegroundDispatch(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FaceServer.getInstance().unInit();
        MemberSyncDataModel.getInstance().setListener(null);
        MemberSyncDataModel.getInstance().clear();
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
                        localRegister(firstnamestr, lastnamestr, mobilestr, memberidstr, emailstr, accessstr, uniquestr, registerpath, "", Util.currentDate());
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

    @Override
    public void onJSONObjectListenerMemberList(JSONObject reportInfo, String status, JSONObject req) {
        if (reportInfo != null) {
            Gson gson = new Gson();
            MemberListResponse response = gson.fromJson(String.valueOf(reportInfo), MemberListResponse.class);
            if (response.responseCode != null && response.responseCode.equals("1")) {
                List<MemberListData> memberList = response.memberList;
                Log.d(TAG, "MemberList Size " + memberList.size());
                MemberSyncDataModel.getInstance().setNumOfRecords(memberList.size());
                for (int i = 0; i < memberList.size(); i++) {
                    if (memberList.get(i).status) {
                        activeMemberCount++;
                        getMemberID(memberList.get(i).id);
                    }
                }
                totalMemberCount = activeMemberCount;
                MemberSyncDataModel.getInstance().setNumOfRecords(activeMemberCount);
            } else {
                //DismissProgressDialog(mloadingprogress);
            }
            Log.e(TAG, "MemberList response = " + response.responseCode);
        }
        Log.e(TAG, "MemberList null response");
    }

    private void getMemberID(String certifyId) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("id", certifyId);
            if (taskExecutorService != null) {
                new AsyncGetMemberData(obj, this, sharedPreferences.getString(GlobalParameters.URL,
                        EndPoints.prod_url) + EndPoints.GetMemberById, this).executeOnExecutor(taskExecutorService);
            } else {
                new AsyncGetMemberData(obj, this, sharedPreferences.getString(GlobalParameters.URL,
                        EndPoints.prod_url) + EndPoints.GetMemberById, this).execute();
            }
        } catch (Exception e) {
            Logger.error(TAG, " getMemberID()",e.getMessage());
        }
    }

    private int testCount=0;
    public void onJSONObjectListenerMemberID(final JSONObject reportInfo, String status, JSONObject req) {
        if (reportInfo != null) {
            try {
                if (reportInfo.isNull("responseCode")) {
                    return;
                }
                if (reportInfo.getString("responseCode").equals("1")) {
                    JSONArray memberList = reportInfo.getJSONArray("responseData");
                    if (memberList != null) {
                        MemberSyncDataModel.getInstance().createMemberDataAndAdd(memberList);
                    }
                }
            } catch (JSONException e) {
                DismissProgressDialog(mloadingprogress);
                Log.e(TAG, "Get Member Id resposne, Error is fetching the data");
            }
        }
    }

    public void onRfidScan(String cardId) {
        if(updateMember != null) updateMember.setAccessid(cardId);
        runOnUiThread(()->{
            if (updateMember != null && maccessid != null) {
                maccessid.setText(cardId);
            } else {
                if (registerAccessid != null) {
                    registerAccessid.setText(cardId);
                }
            }
        });
    }

    private void enableHidReader() {
        if (sharedPreferences.getBoolean(GlobalParameters.RFID_ENABLE, false)) {
            if (mNfcAdapter != null && !mNfcAdapter.isEnabled()) {
                startHidService();
            }
        }
    }

    private void startHidService() {
        Intent msgIntent = new Intent(this, HIDService.class);
        startService(msgIntent);
    }

    private void initHidReceiver() {
        hidReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent != null && intent.getAction().equals(HIDService.HID_BROADCAST_ACTION)) {
                    String data = intent.getStringExtra(HIDService.HID_DATA);
                    runOnUiThread(() -> {
                        if (!data.equals(HIDService.HID_RESTART_SERVICE)) {
                            Log.d(TAG, "HID Card Id UI " + data);
                            onRfidScan(data);
                            return;
                        }
                        HIDService.readTerminal = false;
                        new Handler().postDelayed(() -> {
                            Log.d(TAG, "HID Restarting Service");
                            enableHidReader();
                        }, 200);
                    });
                }
            }
        };
    }

    private void initMemberSync() {
        MemberSyncDataModel.getInstance().init(this, MemberSyncDataModel.DatabaseAddType.SERIAL);
        MemberSyncDataModel.getInstance().setListener(this);
        AsyncTaskExecutorService executorService = new AsyncTaskExecutorService();
        taskExecutorService = executorService.getExecutorService();
    }

    @Override
    public void onMemberAddedToDb(RegisteredMembers addedMember) {
        runOnUiThread(() -> {
            DismissProgressDialog(mloadingprogress);
            if (testCount == totalMemberCount) {
                mCountTv.setText(String.valueOf(datalist.size() + 1));
            } else {
                mCountTv.setText(testCount++ + " / " + totalMemberCount);
            }
            datalist.add(addedMember);
            refreshMemberList(datalist);
        });
    }
}