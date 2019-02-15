package fmg.android.app;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import fmg.android.app.databinding.MosaicGroupItemBinding;
import fmg.android.app.model.items.MosaicGroupDataItem;

public class MenuMosaicGroupListViewAdapter extends RecyclerView.Adapter<MenuMosaicGroupListViewAdapter.ViewHolder> {

    private final List<MosaicGroupDataItem> items;
    private final OnItemClickListener listener;

    public MenuMosaicGroupListViewAdapter(List<MosaicGroupDataItem> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent == null ? null : parent.getContext());
        MosaicGroupItemBinding binding = MosaicGroupItemBinding.inflate(layoutInflater, parent, false);
        return new ViewHolder(binding);
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(items.get(position), listener);
    }

    @Override
    public int getItemCount() { return items.size(); }

    @FunctionalInterface
    interface OnItemClickListener {
        void onItemClick(View view, int position);
    }


    static class ViewHolder extends RecyclerView.ViewHolder {

        private final MosaicGroupItemBinding binding;

        ViewHolder(MosaicGroupItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(MosaicGroupDataItem mosaicGroupItem, OnItemClickListener listener) {
            binding.setMosaicGroupItem(mosaicGroupItem);
            if (listener != null)
                binding.getRoot().setOnClickListener(view -> listener.onItemClick(view, getLayoutPosition()) );

            binding.executePendingBindings();
        }
    }

}
