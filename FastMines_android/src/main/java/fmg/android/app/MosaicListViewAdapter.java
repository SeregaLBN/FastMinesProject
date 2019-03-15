package fmg.android.app;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.beans.PropertyChangeEvent;

import fmg.android.app.databinding.MosaicItemBinding;
import fmg.android.app.model.dataSource.MosaicDataSource;
import fmg.android.app.model.items.MosaicDataItem;
import fmg.android.utils.Cast;
import fmg.common.Color;

public class MosaicListViewAdapter extends RecyclerView.Adapter<MosaicListViewAdapter.ViewHolder> implements AutoCloseable {

    private final MosaicDataSource mosaicDS;
    private final OnItemClickListener listener;

    public MosaicListViewAdapter(MosaicDataSource mosaicDS, OnItemClickListener listener) {
        this.mosaicDS = mosaicDS;
        this.listener = listener;

        mosaicDS.addListener(this::onMosaicDsPropertyChanged);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        LoggerSimple.put("  MenuMosaicListViewAdapter::onCreateViewHolder");

        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        MosaicItemBinding binding = MosaicItemBinding.inflate(layoutInflater, parent, false);
        return new ViewHolder(binding);
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
//        LoggerSimple.put("  MenuMosaicListViewAdapter::onBindViewHolder pos=" + position);

        holder.bind(mosaicDS.getDataSource().get(position));
        if (!holder.binding.getRoot().hasOnClickListeners())
            holder.binding.getRoot().setOnClickListener(view -> onClickItem(view, holder.getLayoutPosition(), holder.getAdapterPosition()));

//        Color clr = mosaicDS.getDataSource().get(position).getEntity().getModel().getBackgroundColor();
//        holder.itemView.setBackgroundColor(Cast.toColor(clr));
    }

    private void onClickItem(View view, int layoutPos, int adapterPos) {
//        LoggerSimple.put("  MenuMosaicListViewAdapter::OnClickListener: layoutPos={0}, adapterPos={1}", layoutPos, adapterPos);

        mosaicDS.setCurrentItemPos(adapterPos); // change current item before call listener
        listener.onItemClick(view, layoutPos);
    }

    public void onMosaicDsPropertyChanged(PropertyChangeEvent ev) {
        switch (ev.getPropertyName()) {
        case MosaicDataSource.PROPERTY_CURRENT_ITEM_POS:
//            LoggerSimple.put("  MenuMosaicListViewAdapter::onMosaicDsPropertyChanged: ev=" + ev);
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
    public int getItemCount() { return mosaicDS.getDataSource().size(); }

    @FunctionalInterface
    interface OnItemClickListener {
        void onItemClick(View view, int position);
    }


    static class ViewHolder extends RecyclerView.ViewHolder {

        final MosaicItemBinding binding;

        ViewHolder(MosaicItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(MosaicDataItem mosaicItem) {
            binding.setMosaicItem(mosaicItem);
            binding.executePendingBindings();
        }
    }

    @Override
    public void close() {
        mosaicDS.removeListener(this::onMosaicDsPropertyChanged);
    }

}
