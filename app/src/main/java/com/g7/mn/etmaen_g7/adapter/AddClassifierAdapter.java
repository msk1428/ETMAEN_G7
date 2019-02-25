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
import com.g7.mn.etmaen_g7.database.AddEntry;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AddClassifierAdapter extends RecyclerView.Adapter<AddClassifierAdapter.ClassifierViewHolder> {

  final private  ItemClickListener mItemClickListener;  //3
   private List <AddEntry> mImageEntries; // v. get all row in table
    private Context mContext;

    public AddClassifierAdapter(Context context, ItemClickListener listener){//4 consrtract object

        mContext=context;
        mItemClickListener=listener;
    }
//2 creat method
    @NonNull
    @Override
    public ClassifierViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) { //holder call page of list item and display
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.add_classification_item, viewGroup, false);

        return new ClassifierViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull ClassifierViewHolder holder, int position) {
        // Determine the values of the wanted data
        AddEntry imageEntry = mImageEntries.get(position);
        String name = imageEntry.getName();
        String phonenumber = imageEntry.getPhonenumber();
        String images = imageEntry.getImage();

        //Set values
        holder.name.setText(name);
        holder.particulars.setText("" + phonenumber);

        Glide.with(mContext)
                .load(images)
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() { // get number of row of table
        if (mImageEntries == null) {
            return 0;
        }
        return mImageEntries.size();
    }

    public List<AddEntry> getClassifier() {
        return mImageEntries;
    } //get row from table

    public void setTasks(List<AddEntry> imageEntries) { //set the v to table
        mImageEntries = imageEntries;
        notifyDataSetChanged();// notify the sys if have chane in DB such as add new row
    }

    public void removeItem(int position) { // if row deled it in DB the function know the adapter dosen't show
        mImageEntries.remove(position);
        notifyItemRemoved(position);
    }

    public void restoreItem(AddEntry item, int position) { //recancell in DB
        mImageEntries.add(position, item);
        // notify item added by position
        notifyItemInserted(position);
    }

    public interface ItemClickListener { // if press one of item open onther page ??
        void onItemClickListener(int itemId);
    }
    // Inner class for creating ViewHolders  1- on click
    public class ClassifierViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView name;
        TextView particulars;
        CircleImageView imageView;
        RelativeLayout viewForeground;

        ClassifierViewHolder(View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.name);
            particulars = itemView.findViewById(R.id.particulars);//phonenumber
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

