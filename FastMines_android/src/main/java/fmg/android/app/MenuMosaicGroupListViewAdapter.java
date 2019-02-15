package fmg.android.app;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import fmg.android.app.databinding.MosaicGroupItemBinding;
import fmg.android.app.model.items.MosaicGroupDataItem;
import fmg.common.LoggerSimple;

public class MenuMosaicGroupListViewAdapter extends RecyclerView.Adapter<MenuMosaicGroupListViewAdapter.ViewHolder> {

    private final List<MosaicGroupDataItem> items;
    private final OnItemClickListener listener;
    int selected_position = -1; // You have to set this globally in the Adapter class

    public MenuMosaicGroupListViewAdapter(List<MosaicGroupDataItem> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        MosaicGroupItemBinding binding = MosaicGroupItemBinding.inflate(layoutInflater, parent, false);
        return new ViewHolder(binding);
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(items.get(position), listener);

        // Here I am just highlighting the background
        LoggerSimple.put("  onBindViewHolder pos=" + position);
        holder.itemView.setBackgroundColor(selected_position == position ? Color.GREEN : Color.RED);
    }

    @Override
    public int getItemCount() { return items.size(); }

    @FunctionalInterface
    interface OnItemClickListener {
        void onItemClick(View view, int position);
    }


    class ViewHolder extends RecyclerView.ViewHolder {

        private final MosaicGroupItemBinding binding;

        ViewHolder(MosaicGroupItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(MosaicGroupDataItem mosaicGroupItem, OnItemClickListener listener) {
            binding.setMosaicGroupItem(mosaicGroupItem);
            if (listener != null)
                binding.getRoot().setOnClickListener(view -> {
                    listener.onItemClick(view, getLayoutPosition());

                    // Below line is just like a safety check, because sometimes holder could be null,
                    // in that case, getAdapterPosition() will return RecyclerView.NO_POSITION
                    if (getAdapterPosition() == RecyclerView.NO_POSITION)
                        return;

                    // Updating old as well as new positions
                    LoggerSimple.put("  OnClickListener oldPos=" + selected_position);
                    notifyItemChanged(selected_position);
                    selected_position = getAdapterPosition();
                    notifyItemChanged(selected_position);
                    LoggerSimple.put("  OnClickListener newPos=" + selected_position);

                });

            binding.executePendingBindings();
        }
    }

}
