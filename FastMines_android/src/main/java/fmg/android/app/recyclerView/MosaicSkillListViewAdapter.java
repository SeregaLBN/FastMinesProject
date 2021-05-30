package fmg.android.app.recyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.function.BiConsumer;

import fmg.android.app.databinding.MosaicSkillItemBinding;
import fmg.android.app.model.items.MosaicSkillDataItem;
import fmg.android.utils.Cast;
import fmg.common.Color;

public class MosaicSkillListViewAdapter extends RecyclerView.Adapter<MosaicSkillListViewAdapter.ViewHolder> {

    private final List<MosaicSkillDataItem> items;
    private BiConsumer<View, Integer> onItemClick;

    public MosaicSkillListViewAdapter(List<MosaicSkillDataItem> items) {
        this.items = items;
    }

    public void setOnItemClick(BiConsumer<View, Integer> onItemClick) {
        this.onItemClick = onItemClick;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        Logger.info("  MosaicSkillListViewAdapter::onCreateViewHolder");

        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        MosaicSkillItemBinding binding = MosaicSkillItemBinding.inflate(layoutInflater, parent, false);
        return new ViewHolder(binding);
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
//        Logger.info("  MosaicSkillListViewAdapter::onBindViewHolder pos=" + position);

        holder.bind(items.get(position));
        if (!holder.binding.getRoot().hasOnClickListeners())
            holder.binding.getRoot().setOnClickListener(view -> {
                //Logger.info("  MosaicSkillListViewAdapter::onBindViewHolder:OnClickListener: layoutPos={0}, adapterPos={1}", holder.getLayoutPosition(), holder.getAdapterPosition());
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
