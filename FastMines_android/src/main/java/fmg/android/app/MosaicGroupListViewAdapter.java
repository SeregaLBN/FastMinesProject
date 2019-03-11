package fmg.android.app;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.beans.PropertyChangeEvent;

import fmg.android.app.databinding.MosaicGroupItemBinding;
import fmg.android.app.model.dataSource.MosaicGroupDataSource;
import fmg.android.app.model.items.MosaicGroupDataItem;
import fmg.android.utils.Cast;
import fmg.common.Color;
import fmg.common.LoggerSimple;

public class MosaicGroupListViewAdapter extends RecyclerView.Adapter<MosaicGroupListViewAdapter.ViewHolder> implements AutoCloseable {

    private final MosaicGroupDataSource mosaicGroupDS;
    private final OnItemClickListener listener;

    public MosaicGroupListViewAdapter(MosaicGroupDataSource mosaicGroupDS, OnItemClickListener listener) {
        this.mosaicGroupDS = mosaicGroupDS;
        this.listener = listener;

        mosaicGroupDS.addListener(this::onMosaicGroupDsPropertyChanged);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        LoggerSimple.put("  MenuMosaicGroupListViewAdapter::onCreateViewHolder");

        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        MosaicGroupItemBinding binding = MosaicGroupItemBinding.inflate(layoutInflater, parent, false);
        return new ViewHolder(binding);
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
//        LoggerSimple.put("  MenuMosaicGroupListViewAdapter::onBindViewHolder pos=" + position);

        holder.bind(mosaicGroupDS.getDataSource().get(position));
        if (!holder.binding.getRoot().hasOnClickListeners())
            holder.binding.getRoot().setOnClickListener(view -> onClickItem(view, holder.getLayoutPosition(), holder.getAdapterPosition()));

        Color clr = mosaicGroupDS.getDataSource().get(position).getEntity().getModel().getBackgroundColor();
        holder.itemView.setBackgroundColor(Cast.toColor(clr));
    }

    private void onClickItem(View view, int layoutPos, int adapterPos) {
//        LoggerSimple.put("  MenuMosaicGroupListViewAdapter::OnClickListener: layoutPos={0}, adapterPos={1}", layoutPos, adapterPos);

        listener.onItemClick(view, layoutPos);
        mosaicGroupDS.setCurrentItemPos(adapterPos);
    }

    public void onMosaicGroupDsPropertyChanged(PropertyChangeEvent ev) {
        switch (ev.getPropertyName()) {
        case MosaicGroupDataSource.PROPERTY_CURRENT_ITEM_POS:
//            LoggerSimple.put("  MenuMosaicGroupListViewAdapter::onMosaicGroupDsPropertyChanged: ev=" + ev);
            int oldPos = (Integer)ev.getOldValue();
            int newPos = (Integer)ev.getNewValue();

//            // Below line is just like a safety check, because sometimes holder could be null,
//            // in that case, getAdapterPosition() will return RecyclerView.NO_POSITION
//            if (newPos == RecyclerView.NO_POSITION)
//                return;

            // Updating old as well as new positions
            notifyItemChanged(oldPos);
            notifyItemChanged(newPos);

            break;
        }
    }

    @Override
    public int getItemCount() { return mosaicGroupDS.getDataSource().size(); }

    @FunctionalInterface
    interface OnItemClickListener {
        void onItemClick(View view, int position);
    }


    static class ViewHolder extends RecyclerView.ViewHolder {

        final MosaicGroupItemBinding binding;

        ViewHolder(MosaicGroupItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(MosaicGroupDataItem mosaicGroupItem) {
            binding.setMosaicGroupItem(mosaicGroupItem);
            binding.executePendingBindings();
        }
    }

    @Override
    public void close() {
        mosaicGroupDS.removeListener(this::onMosaicGroupDsPropertyChanged);
    }

}
