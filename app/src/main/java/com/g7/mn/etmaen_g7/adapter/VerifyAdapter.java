package com.g7.mn.etmaen_g7.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.g7.mn.etmaen_g7.R;
import com.g7.mn.etmaen_g7.database.VerifiedEntry;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class VerifyAdapter extends RecyclerView.Adapter<VerifyAdapter.ClassifierViewHolder>{

    // 3 Member variable to handle item clicks
    final private ItemClickListener mItemClickListener;
    private List<VerifiedEntry> mImageEntries;
    private Context mContext;

    public VerifyAdapter(Context context, ItemClickListener listener) {//4 obj
        mContext = context;
        mItemClickListener = listener;
    }

//2 creat method
    @NonNull
    @Override //5
    public ClassifierViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {// call item page
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.verify_face_items, parent, false);

        return new ClassifierViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ClassifierViewHolder holder, int position) {
        // Determine the values of the wanted data
        VerifiedEntry imageEntry = mImageEntries.get(position);
        String name = imageEntry.getName();
        String address = imageEntry.getAddress();
        String phonenumber = imageEntry.getPhonenumber();
        String images = imageEntry.getImage();

        //Set values
        holder.name.setText(name);
        holder.address.setText(address);

        Glide.with(mContext)
                .load(images)
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        if (mImageEntries == null) {
            return 0;
        }
        return mImageEntries.size();

    }

    public List<VerifiedEntry> getClassifier() {
        return mImageEntries;
    } //get
    public void setTasks(List<VerifiedEntry> imageEntries) {//set
        mImageEntries = imageEntries;
        notifyDataSetChanged();
    }

    public interface ItemClickListener { //3
        void onItemClickListener(int itemId);
    }
    // 1-Inner class for creating ViewHolders -->pattern enables you to access each list item view without the need for the look up,
    public class ClassifierViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView name;
        TextView address;
        CircleImageView imageView;
        public RelativeLayout viewForeground;

        public ClassifierViewHolder(View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.name);
            address = itemView.findViewById(R.id.address);
            imageView = itemView.findViewById(R.id.image);
            viewForeground = itemView.findViewById(R.id.view_foreground);
            itemView.setOnClickListener(this);
            imageView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int elementId = mImageEntries.get(getAdapterPosition()).getId();
            mItemClickListener.onItemClickListener(elementId);
        }
    }
}
