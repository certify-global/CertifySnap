package com.certify.snap.adapter;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.certify.snap.R;
import com.certify.snap.model.OfflineRecordTemperatureMembers;

import java.util.List;


public class TemperatureRecordAdapter extends RecyclerView.Adapter<TemperatureRecordAdapter.MyViewHolder> {

    Context mcontext;
    List<OfflineRecordTemperatureMembers> mlist;

    public TemperatureRecordAdapter(Context context, List<OfflineRecordTemperatureMembers> list) {
        this.mcontext = context;
        this.mlist = list;
    }

    public void refresh(List<OfflineRecordTemperatureMembers> list) {
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
            holder.name.setText(mlist.get(position).getFirstName());
            if(!TextUtils.isEmpty(mlist.get(position).getTemperature())){
                float temperature = Float.valueOf(mlist.get(position).getTemperature());
                if(temperature > 100.4) {
                    holder.temperature.setTextColor( mcontext.getResources().getColor(R.color.colorRed));
                }else {
                    holder.temperature.setTextColor(mcontext.getResources().getColor(R.color.green));
                }
                holder.temperature.setText("Temperature: " + temperature+" °F");
            }else{
                holder.temperature.setText("");
            }
            if (mlist != null && mlist.get(position).getMemberId() != null) {
                String id = mlist.get(position).getMemberId();
                holder.mobile.setText("Id: " + id);
            }
            if (mlist.get(position).getDeviceTime() != null){
                holder.time.setText(mlist.get(position).deviceTime);
            }
            String path = mlist.get(position).getImagepath();

            Glide.with(mcontext)
                    .load(path)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
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
