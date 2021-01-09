package com.x.jeotourist.scene.mainScene.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.x.jeotourist.R;
import com.x.jeotourist.data.local.entity.TourDataEntity;
import com.x.jeotourist.databinding.TourListItemBinding;

import java.util.ArrayList;

import timber.log.Timber;

public class TourListAdapter extends RecyclerView.Adapter<TourListAdapter.ViewHolder> {
    public interface ItemListener {
        public void onItemClickListener(TourDataEntity entity, int position);

        public void onItemDeleteClickListener(TourDataEntity entity, int position);
    }

    ItemListener itemListener;
    Activity activity;
    ArrayList<TourDataEntity> contents  = new ArrayList<TourDataEntity>();
    public TourListAdapter(Activity activity, ItemListener itemListener) {
        this.activity = activity;
        this.itemListener = itemListener;
    }

   public void setItems(ArrayList<TourDataEntity> contents) {
        Timber.v("select  %s", contents.size());
        this.contents.addAll(contents);
        notifyDataSetChanged();
    }

    public void addItem(TourDataEntity content) {
        this.contents.add(content);
        //notifyItemRangeInserted(itemCount, contents.size)
        notifyItemInserted(this.contents.size()-1);
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(DataBindingUtil.inflate(LayoutInflater.from(activity), R.layout.tour_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bindTo(holder, contents.get(position));
    }

    @Override
    public int getItemCount() {
        return contents.size();
    }




    public class ViewHolder extends RecyclerView.ViewHolder {
        TourListItemBinding listItemBinding;

        public ViewHolder(@NonNull TourListItemBinding binding) {
            super(binding.getRoot());
            listItemBinding = (TourListItemBinding) binding;
        }

        public void bindTo(ViewHolder holder, TourDataEntity content) {
            listItemBinding.setContentViewHolder(holder);
            listItemBinding.setTitle(content.getTitle());
            listItemBinding.setPosition(getAdapterPosition());
            listItemBinding.setIdTour(String.valueOf(content.getId()));
        }

        public void onItemClick(int position) {
            if (itemListener != null) {
                itemListener.onItemClickListener(contents.get(position), position);
            }


        }
    }
}