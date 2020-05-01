package com.certify.snap.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.certify.snap.R;
import com.certify.snap.model.OfflineVerifyMembers;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * 作者    ljf
 * 时间    2019/6/13 0013 11:55
 * 文件    Telpo_Face_Demo_0610
 * 描述
 */
public class RecordAdapter extends RecyclerView.Adapter<RecordAdapter.MyViewHolder> {

    Context mcontext;
    List<OfflineVerifyMembers> mlist;

    public RecordAdapter(Context context, List<OfflineVerifyMembers> list) {
        this.mcontext = context;
        this.mlist = list;
    }

    public void refresh(List<OfflineVerifyMembers> list) {
        this.mlist = list;
        notifyDataSetChanged();
    }

    public OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public void setOnItemClickListener(OnItemClickListener OnItemClickListener) {
        this.mOnItemClickListener = OnItemClickListener;
    }

    public OnItemLongClickListener mOnItemLongClickListener;

    public interface OnItemLongClickListener {
        void onItemLongClick(View view, int position);
    }

    public void setOnItemLongClickListener(OnItemLongClickListener OnItemLongClickListener) {
        this.mOnItemLongClickListener = OnItemLongClickListener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemview = LayoutInflater.from(mcontext).inflate(R.layout.record_item, parent, false);
        return new MyViewHolder(itemview);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {

        try {
        holder.name.setText(mlist.get(position).getName());
        if(!TextUtils.isEmpty(mlist.get(position).getTemperature())){
            float temperature = Float.valueOf(mlist.get(position).getTemperature());
            if(temperature > 37.3) {
                holder.temperature.setTextColor( mcontext.getResources().getColor(R.color.red));
            }else {
                holder.temperature.setTextColor(mcontext.getResources().getColor(R.color.green));
            }
            holder.temperature.setText("Temperature: " + temperature+" ℃");
        }else{
            holder.temperature.setText("");
        }

        holder.mobile.setText("Mobile: "+mlist.get(position).getMobile());
        holder.time.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(mlist.get(position).getVerify_time()));
        String path = mlist.get(position).getImagepath();

            Glide.with(mcontext)
                    .load(path)
                    .skipMemoryCache(true) // 不使用内存缓存
                    .diskCacheStrategy(DiskCacheStrategy.NONE) // 不使用磁盘缓存
                    .error(R.mipmap.face_title)
                    .into(holder.image);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @Override
    public int getItemCount() {
        return mlist.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView name;
        TextView temperature;
        TextView time;
        TextView mobile;

        public MyViewHolder(View view) {
            super(view);
            image = (ImageView) view.findViewById(R.id.record_image);
            name = (TextView) view.findViewById(R.id.record_name);
            mobile = (TextView) view.findViewById(R.id.record_mobile);
            temperature = (TextView) view.findViewById(R.id.record_temp);
            time = (TextView) view.findViewById(R.id.record_time);
        }
    }

}
