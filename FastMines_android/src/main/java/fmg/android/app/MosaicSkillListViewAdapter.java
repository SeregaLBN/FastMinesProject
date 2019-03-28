package fmg.android.app;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;
import java.util.function.BiConsumer;

import fmg.android.app.databinding.MosaicSkillItemBinding;
import fmg.android.app.model.items.MosaicSkillDataItem;
import fmg.android.utils.Cast;
import fmg.common.Color;

public class MosaicSkillListViewAdapter extends RecyclerView.Adapter<MosaicSkillListViewAdapter.ViewHolder> {

    private final List<MosaicSkillDataItem> items;
    private final BiConsumer<View, Integer> onItemClick;

    public MosaicSkillListViewAdapter(List<MosaicSkillDataItem> items, BiConsumer<View, Integer> onItemClick) {
        this.items = items;
        this.onItemClick = onItemClick;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        LoggerSimple.put("  MosaicSkillListViewAdapter::onCreateViewHolder");

        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        MosaicSkillItemBinding binding = MosaicSkillItemBinding.inflate(layoutInflater, parent, false);
        return new ViewHolder(binding);
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
//        LoggerSimple.put("  MosaicSkillListViewAdapter::onBindViewHolder pos=" + position);

        holder.bind(items.get(position));
        if (!holder.binding.getRoot().hasOnClickListeners())
            holder.binding.getRoot().setOnClickListener(view -> {
                //LoggerSimple.put("  MosaicSkillListViewAdapter::onBindViewHolder:OnClickListener: layoutPos={0}, adapterPos={1}", holder.getLayoutPosition(), holder.getAdapterPosition());
                int pos = holder.getLayoutPosition(); // holder.getAdapterPosition();
                onItemClick.accept(view, pos);
            });

        Color clr = items.get(position).getEntity().getModel().getBackgroundColor();
        holder.itemView.setBackgroundColor(Cast.toColor(clr));
    }

    @Override
    public int getItemCount() { return items.size(); }


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

}
