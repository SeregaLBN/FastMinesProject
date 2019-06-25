package fmg.android.app.recyclerView;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;
import java.util.function.BiConsumer;

import fmg.android.app.databinding.MosaicGroupItemBinding;
import fmg.android.app.model.items.MosaicGroupDataItem;
import fmg.android.utils.Cast;
import fmg.common.Color;

public class MosaicGroupListViewAdapter extends RecyclerView.Adapter<MosaicGroupListViewAdapter.ViewHolder> {

    private final List<MosaicGroupDataItem> items;
    private BiConsumer<View, Integer> onItemClick;

    public MosaicGroupListViewAdapter(List<MosaicGroupDataItem> items) {
        this.items = items;
    }

    public void setOnItemClick(BiConsumer<View, Integer> onItemClick) {
        this.onItemClick = onItemClick;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        Logger.info("  MosaicGroupListViewAdapter::onCreateViewHolder");

        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        MosaicGroupItemBinding binding = MosaicGroupItemBinding.inflate(layoutInflater, parent, false);
        return new ViewHolder(binding);
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
//        Logger.info("  MosaicGroupListViewAdapter::onBindViewHolder pos=" + position);

        holder.bind(items.get(position));
        if (!holder.binding.getRoot().hasOnClickListeners())
            holder.binding.getRoot().setOnClickListener(view -> {
                //Logger.info("  MosaicGroupListViewAdapter::onBindViewHolder:OnClickListener: layoutPos={0}, adapterPos={1}", holder.getLayoutPosition(), holder.getAdapterPosition());
                int pos = holder.getLayoutPosition(); // holder.getAdapterPosition();
                if (onItemClick != null)
                    onItemClick.accept(view, pos);
            });

        Color clr = items.get(position).getEntity().getModel().getBackgroundColor().brighter();
        holder.itemView.setBackgroundColor(Cast.toColor(clr));
    }

    @Override
    public int getItemCount() { return items.size(); }


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

}
