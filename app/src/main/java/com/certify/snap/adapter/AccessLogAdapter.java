package com.certify.snap.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.certify.snap.R;
import com.certify.snap.model.AccessLogOfflineRecord;

import java.util.List;

public class AccessLogAdapter extends RecyclerView.Adapter<AccessLogAdapter.MyViewHolder> {

    private Context mcontext;
    private List<AccessLogOfflineRecord> mlist;

    public AccessLogAdapter(Context context, List<AccessLogOfflineRecord> list) {
        this.mcontext = context;
        this.mlist = list;
    }

    public void refresh(List<AccessLogOfflineRecord> list) {
        this.mlist = list;
        notifyDataSetChanged();
    }

    @Override
    public AccessLogAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mcontext).inflate(R.layout.access_log_item, parent, false);
        return new AccessLogAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final AccessLogAdapter.MyViewHolder holder, final int position) {

        try {
            holder.name.setText(mlist.get(position).getFirstName());
            if (mlist != null && mlist.get(position).getMemberId() != null) {
                String id = mlist.get(position).getMemberId();
                holder.mobile.setText("Id: " + id);
            }
            if (mlist.get(position).getDeviceTime() != null){
                holder.time.setText(mlist.get(position).getMemberId());
            }
            String path = mlist.get(position).getImagePath();

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
