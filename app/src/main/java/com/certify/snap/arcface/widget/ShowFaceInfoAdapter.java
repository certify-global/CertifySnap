package com.certify.snap.arcface.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;


import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.certify.snap.faceserver.CompareResult;
import com.certify.snap.faceserver.FaceServer;
import com.certify.snap.R;
import java.io.File;
import java.util.List;

public class ShowFaceInfoAdapter extends RecyclerView.Adapter<ShowFaceInfoAdapter.CompareResultHolder> {
    private List<CompareResult> compareResultList;
    private LayoutInflater inflater;
    private Context mContext;
    private Animation animation;

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
        compareResultHolder.textView = itemView.findViewById(R.id.tv_item_name);
        compareResultHolder.imageView = itemView.findViewById(R.id.iv_item_head_img);
//        compareResultHolder.img_ring = itemView.findViewById(R.id.iv_ring);
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
        holder.textView.setText(compareResultList.get(position).getMessage());
//        animation = AnimationUtils.loadAnimation(mContext,R.anim.dialog_initialize_wait_rotate);
//        animation.setDuration(1500);
//        LinearInterpolator linearInterpolator = new LinearInterpolator();
//        animation.setInterpolator(linearInterpolator);
//        holder.img_ring.startAnimation(animation);
    }

    @Override
    public int getItemCount() {
        return compareResultList == null ? 0 : compareResultList.size();
    }

    class CompareResultHolder extends RecyclerView.ViewHolder {

        TextView textView;
        ImageView imageView;
        ImageView img_ring;

        CompareResultHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    public void stopAnimation(){
        if(animation!=null){
            animation.cancel();
        }
    }
}
