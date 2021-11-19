package com.certify.snap.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.certify.snap.R;
import com.certify.snap.activity.OfflineRecordsActivity;
import com.certify.snap.adapter.AccessLogAdapter;
import com.certify.snap.controller.DatabaseController;
import com.certify.snap.model.AccessLogOfflineRecord;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class AccessLogOfflineFragment extends Fragment {

    protected static final String TAG = OfflineRecordsActivity.class.getSimpleName();
    private RecyclerView recyclerView;
    private AccessLogAdapter recordAdapter;
    private List<AccessLogOfflineRecord> dataList = new ArrayList<>();
    private View view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_offline_record, container, false);
        initRecylerView();
        initData();
        return view;
    }

    public void initData() {
        try {
            Observable.create((ObservableOnSubscribe<List<AccessLogOfflineRecord>>) emitter -> {
                List<AccessLogOfflineRecord> offlineRecordList = DatabaseController.getInstance().findAllOfflineAccessLogRecord();
                emitter.onNext(offlineRecordList);
            }).subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<List<AccessLogOfflineRecord>>() {
                        Disposable disposable;
                        @Override
                        public void onSubscribe(Disposable d) {
                            disposable = d;
                        }

                        @Override
                        public void onNext(List<AccessLogOfflineRecord> list) {
                            refreshOfflineRecords(list);
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

    private void refreshOfflineRecords(List<AccessLogOfflineRecord> list) {
        dataList.addAll(list);
        recordAdapter.refresh(dataList);
        recyclerView.scrollToPosition(0);
    }

    private void initRecylerView() {
        recyclerView = view.findViewById(R.id.recyclerview_record);
        recordAdapter = new AccessLogAdapter(this.getContext(), dataList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        recyclerView.setAdapter(recordAdapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recordAdapter.notifyDataSetChanged();
    }

}
