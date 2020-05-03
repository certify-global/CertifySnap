package com.certify.snap.adapter;

import android.content.Context;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.certify.snap.R;
import com.certify.snap.model.RegisteredFailedMembers;

import java.io.File;
import java.util.List;

public class MemberFailedAdapter extends RecyclerView.Adapter<MemberFailedAdapter.MyViewHolder> {

    private Context mcontext;
    private List<RegisteredFailedMembers> mlist;

    public MemberFailedAdapter(Context context, List<RegisteredFailedMembers> list) {
        this.mcontext = context;
        this.mlist = list;
        Log.e("mlist-----", mlist.toString());
    }

    public void refresh(List<RegisteredFailedMembers> list) {
        if(list!=null) {
            this.mlist = list;
            notifyDataSetChanged();
        }
    }

    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public void setOnItemClickListener(OnItemClickListener OnItemClickListener) {
        this.mOnItemClickListener = OnItemClickListener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemview = LayoutInflater.from(mcontext).inflate(R.layout.member_failed_item, parent, false);
        return new MyViewHolder(itemview);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {

        try {
            holder.name.setText(mlist.get(position).getName());
            // holder.userid.setText("UserId: " + mlist.get(position).getUserId());
            //  String path = getImagebyName(mlist.get(position).getName());
            String path = mlist.get(position).getImage();
            Log.e("failed image path---", path);
            Glide.with(mcontext)
                    .load(path)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .error(R.mipmap.face_title)
                    .into(holder.image);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(holder.itemView, position);
                    }
                }
            });
        }catch (Exception e){
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
        TextView userid;

        public MyViewHolder(View view) {
            super(view);
            image = (ImageView) view.findViewById(R.id.member_failed_image);
            name = (TextView) view.findViewById(R.id.member_failed_name);
            userid = (TextView) view.findViewById(R.id.member_failed_userid);
        }
    }

    private String getImagebyName(String name) {
        String path = Environment.getExternalStorageDirectory().getPath() + "/Telpo_face/Failed/";
        String imagepath = "";
        File file = new File(path);
        if (file.exists()) {
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].getName().contains(name)) {
                    imagepath = files[i].getPath();
                    Log.e("path---", imagepath);
                    break;
                }
            }
            return imagepath;
        } else {
            Log.e("file---", "file not exists");
        }
        return imagepath;
    }

}
