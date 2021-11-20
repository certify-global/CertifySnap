package com.certify.snap.activity;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.certify.snap.R;
import com.certify.snap.fragment.AccessLogOfflineFragment;
import com.certify.snap.fragment.TemperatureOfflineFragment;

public class OfflineRecordsActivity extends AppCompatActivity {
    private static final String TAG = OfflineRecordsActivity.class.getSimpleName();
    private ViewPager viewPager;
    private ImageView imageViewBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_offline_record);
        initView();
        setClickListener();
    }

    private void initView() {
        viewPager = findViewById(R.id.view_pager);
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(this, getSupportFragmentManager());
        viewPager.setAdapter(viewPagerAdapter);
    }

    private void setClickListener() {
        imageViewBack = findViewById(R.id.record_back);
        imageViewBack.setOnClickListener(view -> finish());
    }

    private static class ViewPagerAdapter extends FragmentPagerAdapter {
        private Context context;

        public ViewPagerAdapter(Context context, FragmentManager fm) {
            super(fm);
            this.context = context;
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return new TemperatureOfflineFragment();
            } else if (position == 1) {
                return new AccessLogOfflineFragment();
            }
            return null;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0) {
                return context.getString(R.string.temperature_records);
            } else if (position == 1) {
                return context.getString(R.string.access_log_records);
            }
            return super.getPageTitle(position);
        }
    }
}
