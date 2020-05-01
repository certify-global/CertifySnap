package com.certify.snap.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.certify.snap.R;
import com.certify.snap.model.RegisteredMembers;

import java.io.InputStream;
import java.util.List;

/**
 * 作者    ljf
 * 时间    2019/6/13 0013 11:55
 * 文件    Telpo_Face_Demo_0610
 * 描述
 */
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
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemview = LayoutInflater.from(mcontext).inflate(R.layout.member_item, parent, false);
        return new MyViewHolder(itemview);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {

        try {
        holder.name.setText(mlist.get(position).getName());
        holder.mobile.setText("Mobile: " + mlist.get(position).getMobile());
        holder.time.setText("ExpireTime: " + mlist.get(position).getExpire_time());
        String path = mlist.get(position).getImage();
        InputStream inputStream;

            Glide.with(mcontext)
                    .load(path)
                    .skipMemoryCache(true) // 不使用内存缓存
                    .diskCacheStrategy(DiskCacheStrategy.NONE) // 不使用磁盘缓存
                    .error(R.mipmap.face_title)
                    .into(holder.image);
//            inputStream = new FileInputStream(new File(path));
//            // 创建一个原图的拷贝, 把拷贝的图片 放在iv
//            // 原图对应的bitmap 注意:这个图片是只读的 不可以被修改.
//            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
//            // 创建一个可以被修改的位图资源,宽高信息 和 图片的类型 跟原图是一模一样
//            // 下面的代码创建的图片 是一个空白的图片
//            if(bitmap!=null) {
//                Bitmap bitmap2 = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
//                // 拿着可以被修改的图片创建一个画布.
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
        TextView time;

        public MyViewHolder(View view) {
            super(view);
            image = (ImageView) view.findViewById(R.id.member_image);
            name = (TextView) view.findViewById(R.id.member_name);
            mobile = (TextView) view.findViewById(R.id.member_mobile);
            time = (TextView) view.findViewById(R.id.member_time);
        }
    }

}
