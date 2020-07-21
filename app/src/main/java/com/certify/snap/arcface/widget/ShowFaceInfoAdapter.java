package com.certify.snap.arcface.widget;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.certify.snap.R;
import com.certify.snap.common.AppSettings;
import com.certify.snap.faceserver.CompareResult;
import com.certify.snap.faceserver.FaceServer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ShowFaceInfoAdapter extends RecyclerView.Adapter<ShowFaceInfoAdapter.CompareResultHolder> {
    private List<CompareResult> compareResultList;
    private LayoutInflater inflater;
    private Context mContext;

    public ShowFaceInfoAdapter(List<CompareResult> compareResultList, Context context) {
        inflater = LayoutInflater.from(context);
        this.compareResultList = compareResultList;
        mContext = context;
    }

    @NonNull
    @Override
    public CompareResultHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = inflater.inflate(R.layout.item_head, parent, false);
        CompareResultHolder compareResultHolder = new CompareResultHolder(itemView);
        compareResultHolder.tv_name = itemView.findViewById(R.id.tv_item_name);
        compareResultHolder.tv_temperature = itemView.findViewById(R.id.tv_item_temperature);
        compareResultHolder.imageView = itemView.findViewById(R.id.iv_item_head_img);
        compareResultHolder.tv_last_name = itemView.findViewById(R.id.tv_item_last_name);
        compareResultHolder.item_layout = itemView.findViewById(R.id.item_layout);
        return compareResultHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull CompareResultHolder holder, int position) {
        if (compareResultList == null) {
            return;
        }
        File imgFile = new File(FaceServer.ROOT_PATH + File.separator + FaceServer.SAVE_IMG_DIR + File.separator + compareResultList.get(position).getUserName() + FaceServer.IMG_SUFFIX);
        Glide.with(mContext)
                .load(imgFile)
                .skipMemoryCache(true) // 不使用内存缓存
                .diskCacheStrategy(DiskCacheStrategy.NONE) // 不使用磁盘缓存
                .into(holder.imageView);
        holder.tv_name.setText(compareResultList.get(position).getMessage());

        String temperatureUnit = AppSettings.getfToC();
        if (temperatureUnit.equals("F")) {
            holder.tv_temperature.setText(compareResultList.get(position).getTemperature() +mContext.getString(R.string.Fahrenheit_temp));
        } else {
            holder.tv_temperature.setText(compareResultList.get(position).getTemperature() +mContext.getString(R.string.centigrade));
        }

        holder.tv_last_name.setText(compareResultList.get(position).getLastName());

        if (!(Float.parseFloat(compareResultList.get(position).getTemperature()) > Float.parseFloat(AppSettings.getTemperatureThreshold()))) {
            holder.item_layout.setBackgroundColor(mContext.getColor(R.color.green));
        } else {
            holder.item_layout.setBackgroundColor(mContext.getColor(R.color.red));
        }
    }

    @Override
    public int getItemCount() {
        return compareResultList == null ? 0 : compareResultList.size();
    }

    class CompareResultHolder extends RecyclerView.ViewHolder {

        TextView tv_name,tv_temperature, tv_last_name;
        ImageView imageView;
        LinearLayout item_layout;

        CompareResultHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
