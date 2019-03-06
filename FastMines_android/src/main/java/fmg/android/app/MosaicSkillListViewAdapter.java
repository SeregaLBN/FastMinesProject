package fmg.android.app;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.beans.PropertyChangeEvent;

import fmg.android.app.databinding.MosaicSkillItemBinding;
import fmg.android.app.model.dataSource.MosaicSkillDataSource;
import fmg.android.app.model.items.MosaicSkillDataItem;
import fmg.android.utils.Cast;
import fmg.common.Color;

public class MosaicSkillListViewAdapter extends RecyclerView.Adapter<MosaicSkillListViewAdapter.ViewHolder> implements AutoCloseable {

    private final MosaicSkillDataSource mosaicSkillDS;
    private final OnItemClickListener listener;

    public MosaicSkillListViewAdapter(MosaicSkillDataSource mosaicSkillDS, OnItemClickListener listener) {
        this.mosaicSkillDS = mosaicSkillDS;
        this.listener = listener;

        mosaicSkillDS.addListener(this::onMosaicSkillDsPropertyChanged);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        LoggerSimple.put("  MenuMosaicSkillListViewAdapter::onCreateViewHolder");

        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        MosaicSkillItemBinding binding = MosaicSkillItemBinding.inflate(layoutInflater, parent, false);
        return new ViewHolder(binding);
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
//        LoggerSimple.put("  MenuMosaicSkillListViewAdapter::onBindViewHolder pos=" + position);

        holder.bind(mosaicSkillDS.getDataSource().get(position));
        if (!holder.binding.getRoot().hasOnClickListeners())
            holder.binding.getRoot().setOnClickListener(view -> onClickItem(view, holder.getLayoutPosition(), holder.getAdapterPosition()));

        // Here I am just highlighting the background
        Color clr = mosaicSkillDS.getDataSource().get(position).getEntity().getModel().getBackgroundColor();
        holder.itemView.setBackgroundColor(Cast.toColor(clr));
    }

    private void onClickItem(View view, int layoutPos, int adapterPos) {
//        LoggerSimple.put("  MenuMosaicSkillListViewAdapter::OnClickListener: layoutPos={0}, adapterPos={1}", layoutPos, adapterPos);

        listener.onItemClick(view, layoutPos);
        mosaicSkillDS.setCurrentItemPos(adapterPos);
    }

    public void onMosaicSkillDsPropertyChanged(PropertyChangeEvent ev) {
        switch (ev.getPropertyName()) {
        case MosaicSkillDataSource.PROPERTY_CURRENT_ITEM_POS:
//            LoggerSimple.put("  MenuMosaicSkillListViewAdapter::onMosaicSkillDsPropertyChanged: ev=" + ev);
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
    public int getItemCount() { return mosaicSkillDS.getDataSource().size(); }

    @FunctionalInterface
    interface OnItemClickListener {
        void onItemClick(View view, int position);
    }


    static class ViewHolder extends RecyclerView.ViewHolder {

        final MosaicSkillItemBinding binding;

        ViewHolder(MosaicSkillItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(MosaicSkillDataItem mosaicSkillItem) {
            binding.setMosaicSkillItem(mosaicSkillItem);
            binding.executePendingBindings();
        }
    }

    @Override
    public void close() {
        mosaicSkillDS.removeListener(this::onMosaicSkillDsPropertyChanged);
    }

}
