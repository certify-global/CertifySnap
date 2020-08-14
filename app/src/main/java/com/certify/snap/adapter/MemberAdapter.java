package com.certify.snap.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.certify.snap.R;
import com.certify.snap.model.RegisteredMembers;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.MyViewHolder> implements Filterable {

    private Context mcontext;
    private List<RegisteredMembers> mlist;
    private List<RegisteredMembers> membersList;

    public MemberAdapter(Context context, List<RegisteredMembers> list) {
        this.mcontext = context;
        this.mlist = list;
        this.membersList = list;
    }

    public void refresh(List<RegisteredMembers> list) {
        if(list!=null) {
            this.membersList = this.mlist = list;
            notifyDataSetChanged();
        }
    }

    public OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(RegisteredMembers member);
    }

    public void setOnItemClickListener(OnItemClickListener OnItemClickListener) {
        this.mOnItemClickListener = OnItemClickListener;
    }

    public OnItemLongClickListener mOnItemLongClickListener;

    public interface OnItemLongClickListener {
        void onItemLongClick(RegisteredMembers member);
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
            holder.id.setText("Id: " + mlist.get(position).getMemberid());
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
        } catch (Exception e) {
            e.printStackTrace();
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(mlist.get(position));
                }
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mOnItemLongClickListener != null) {
                    mOnItemLongClickListener.onItemLongClick(mlist.get(position));
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

    @Override
    public Filter getFilter() {
        mlist = membersList;
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    mlist = membersList;
                } else {
                    List<RegisteredMembers> filterList = new ArrayList<>();
                    for (RegisteredMembers member : mlist) {
                        if (member.getFirstname().toLowerCase().contains(charString.toLowerCase()) ||
                            member.getLastname().toLowerCase().contains(charString.toLowerCase())) {
                            filterList.add(member);
                        }
                    }
                    mlist = filterList;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = mlist;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                mlist = (List<RegisteredMembers>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }
}
