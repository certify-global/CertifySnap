package com.certify.snap.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.certify.snap.R;
import com.certify.snap.common.Util;
import com.certify.snap.model.RegisteredMembers;

import java.io.InputStream;
import java.util.List;


public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.MyViewHolder> {

    Context mcontext;
    List<RegisteredMembers> mlist;

    public MemberAdapter(Context context, List<RegisteredMembers> list) {
        this.mcontext = context;
        this.mlist = list;
    }

    public void refresh(List<RegisteredMembers> list) {
        if(list!=null) {
            this.mlist = list;
            notifyDataSetChanged();
        }
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
    public int getItemViewType(int position) {
        return position;
    }
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemview = LayoutInflater.from(mcontext).inflate(R.layout.member_item, parent, false);
        return new MyViewHolder(itemview);
    }
    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        try {
            holder.name.setText(mlist.get(position).getFirstname()+" " + mlist.get(position).getLastname());
            holder.lastname.setText(mlist.get(position).getLastname());
            holder.mobile.setText("Mobile: " + mlist.get(position).getMobile());
            if(!mlist.get(position).getMemberid().isEmpty()) {
                holder.id.setText("Id: " + mlist.get(position).getMemberid());
            }else{
                holder.id.setText("Id: "+mlist.get(position).getAccessid());
            }
            holder.email.setText("Email: " + mlist.get(position).getEmail());
            //holder.time.setText("ExpireTime: " + mlist.get(position).getExpire_time());
            String path = mlist.get(position).getImage();
            InputStream inputStream;

            Bitmap bitmap = BitmapFactory.decodeFile(path);

             if(bitmap!=null){
                 holder.image.setImageBitmap(bitmap);
             }else{
                 //holder.image.setBackgroundResource(R.drawable.face_title);
                 holder.image.setImageResource(R.drawable.face_title);
            }
/*
            Glide.with(mcontext).asBitmap()
                    .load(bitmap)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .error(R.mipmap.face_title)
                    .into(holder.image);*/
//            inputStream = new FileInputStream(new File(path));
//            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
//            if(bitmap!=null) {
//                Bitmap bitmap2 = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
//                Canvas canvas = new Canvas(bitmap2);
//                Paint paint = new Paint();
//                canvas.drawBitmap(bitmap, new Matrix(), paint);
//                if(bitmap != null && !bitmap.isRecycled()){
//                    bitmap.recycle();
//                }
//                if (bitmap2 != null) {
//                    Bitmap newbitmap = Util.zoomBitmap(bitmap2, bitmap2.getWidth() / 2, bitmap2.getHeight() / 2);
//                    bitmap2.recycle();
//                    holder.image.setImageBitmap(newbitmap);
//                }
//            }else{
//                holder.image.setImageBitmap(BitmapFactory.decodeResource(mcontext.getResources(),R.mipmap.face_title));
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(holder.itemView, position);
                }
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mOnItemLongClickListener != null) {
                    mOnItemLongClickListener.onItemLongClick(holder.itemView, position);
                }
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return mlist.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView name;
        TextView mobile;
        TextView id;
        TextView email;
        TextView lastname;
        //TextView time;

        public MyViewHolder(View view) {
            super(view);
            image = view.findViewById(R.id.member_image);
            name = view.findViewById(R.id.member_first_name);
            lastname = view.findViewById(R.id.member_last_name);
            id = view.findViewById(R.id.member_id);
            mobile = view.findViewById(R.id.member_mobile);
            email = view.findViewById(R.id.member_email);

            //time = view.findViewById(R.id.member_time);
        }
    }

}
