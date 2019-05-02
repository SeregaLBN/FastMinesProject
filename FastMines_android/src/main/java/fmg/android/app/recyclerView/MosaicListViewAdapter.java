package fmg.android.app.recyclerView;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import fmg.android.app.databinding.MosaicItemBinding;
import fmg.android.app.model.items.MosaicDataItem;

public class MosaicListViewAdapter extends RecyclerView.Adapter<MosaicListViewAdapter.ViewHolder> {

    private final List<MosaicDataItem> items;
    private final BiConsumer<View, Integer> onItemClick;
    private final BiConsumer<View, Integer> onItemLongClick;

    public MosaicListViewAdapter(List<MosaicDataItem> items, BiConsumer<View, Integer> onItemClick, BiConsumer<View, Integer> onItemLongClick) {
        this.items = new ArrayList<>(items); // copy!
        this.onItemClick = onItemClick;
        this.onItemLongClick = onItemLongClick;
    }

    public void updateItems(List<MosaicDataItem> items) {
        this.items.clear();
        this.items.addAll(items); // copy!
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        LoggerSimple.put("  MosaicListViewAdapter::onCreateViewHolder");

        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        MosaicItemBinding binding = MosaicItemBinding.inflate(layoutInflater, parent, false);
        return new ViewHolder(binding);
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
//        LoggerSimple.put("  MosaicListViewAdapter::onBindViewHolder pos=" + position);

        holder.bind(items.get(position));
        if (!holder.binding.getRoot().hasOnClickListeners()) {
            View root = holder.binding.getRoot();
            root.setOnClickListener(view -> {
                //LoggerSimple.put("  MosaicListViewAdapter::OnClickListener: layoutPos={0}, adapterPos={1}", holder.getLayoutPosition(), holder.getAdapterPosition());
                int pos = holder.getLayoutPosition(); // holder.getAdapterPosition();
                onItemClick.accept(view, pos);
            });
            if (onItemLongClick != null)
                root.setOnLongClickListener(view -> {
                    int pos = holder.getLayoutPosition();
                    onItemLongClick.accept(view, pos);
                    return false;
                });
        }

//        Color clr = items.get(position).getEntity().getModel().getBackgroundColor();
//        holder.itemView.setBackgroundColor(Cast.toColor(clr));
    }

    @Override
    public int getItemCount() { return items.size(); }


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

}
