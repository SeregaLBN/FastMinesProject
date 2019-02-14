package fmg.android.app.model.dataSource;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.graphics.Bitmap;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import fmg.android.app.BR;
import fmg.android.app.model.items.BaseDataItem;
import fmg.common.geom.SizeDouble;
import fmg.common.notyfier.INotifyPropertyChanged;
import fmg.common.notyfier.NotifyPropertyChanged;
import fmg.core.img.IAnimatedModel;
import fmg.core.img.IImageView;
import fmg.core.img.ImageController;

public abstract class BaseDataSource<THeader extends BaseDataItem<THeaderId, THeaderModel, THeaderView, THeaderCtrlr>,
                                     THeaderId,
                                     THeaderModel extends IAnimatedModel,
                                     THeaderView  extends IImageView<Bitmap, THeaderModel>,
                                     THeaderCtrlr extends ImageController<Bitmap, THeaderView, THeaderModel>,
                                     
                                     TItem   extends BaseDataItem<TItemId, TItemModel, TItemView, TItemCtrlr>,
                                     TItemId,
                                     TItemModel extends IAnimatedModel,
                                     TItemView  extends IImageView<Bitmap, TItemModel>,
                                     TItemCtrlr extends ImageController<Bitmap, TItemView, TItemModel>>
    extends BaseObservable
    implements INotifyPropertyChanged, AutoCloseable
{

    public static final String PROPERTY_DATA_SOURCE      = "DataSource";
    public static final String PROPERTY_HEADER           = "Header";
    public static final String PROPERTY_IMAGE_SIZE       = "ImageSize";
    public static final String PROPERTY_CURRENT_ITEM     = "CurrentItem";
    public static final String PROPERTY_CURRENT_ITEM_POS = "CurrentItemPos";

    /** Images that describes this data source */
    protected THeader header;
    /** Data source - images that describes the elements */
    protected List<TItem> dataSource;
    /** Current item index in {@link #dataSource} */
    protected int currentItemPos = 0;

    protected final NotifyPropertyChanged notifier/*Sync*/ = new NotifyPropertyChanged(this, false);
    private   final NotifyPropertyChanged notifierAsync    = new NotifyPropertyChanged(this, true);

    protected BaseDataSource() {
        notifier     .addListener(this::onPropertyChanged);
        notifierAsync.addListener(this::onAsyncPropertyChanged);
    }

    @Bindable
    public abstract THeader getHeader();

    @Bindable
    public abstract List<TItem> getDataSource();

    /** Selected element */
    @Bindable
    public TItem getCurrentItem() {
        return getDataSource().get(getCurrentItemPos());
    }
    public void setCurrentItem(TItem activeItem) {
        setCurrentItemPos(getDataSource().indexOf(activeItem));
    }

    /** Selected index of element */
    @Bindable
    public int getCurrentItemPos() { return currentItemPos; }
    public void setCurrentItemPos(int pos) {
        if ((pos < 0) || (pos >= getDataSource().size()))
            throw new IllegalArgumentException();
        if (pos == currentItemPos)
            return;
        notifier.setProperty(this.currentItemPos, pos, PROPERTY_CURRENT_ITEM_POS);
    }

    @Bindable
    public SizeDouble getImageSize() {
        return getDataSource().get(0).getSize();
    }
    public void setImageSize(SizeDouble size) {
        SizeDouble old = getImageSize();
        getDataSource().forEach(mi -> mi.setSize(size));

        //notifier.setProperty(old, size, PROPERTY_IMAGE_SIZE);
        if (!old.equals(size))
            notifier.firePropertyChanged(old, size, PROPERTY_IMAGE_SIZE);
    }

    /** for one selected - start animate; for all other - stop animate */
    protected  abstract void onCurrentItemChanged();

    protected void onPropertyChanged(PropertyChangeEvent ev) {
        // refire as async event
        notifierAsync.firePropertyChanged(ev.getOldValue(), ev.getNewValue(), ev.getPropertyName());

        switch (ev.getPropertyName()) {
        case PROPERTY_CURRENT_ITEM_POS:
            onCurrentItemChanged();
            notifier.firePropertyChanged(PROPERTY_CURRENT_ITEM);
            break;
        }
    }

    protected void onAsyncPropertyChanged(PropertyChangeEvent ev) {
        // refire as android data binding event
        switch (ev.getPropertyName()) {
        case PROPERTY_DATA_SOURCE     : notifyPropertyChanged(BR.dataSource    ); break;
        case PROPERTY_HEADER          : notifyPropertyChanged(BR.header        ); break;
        case PROPERTY_IMAGE_SIZE      : notifyPropertyChanged(BR.imageSize     ); break;
        case PROPERTY_CURRENT_ITEM    : notifyPropertyChanged(BR.currentItem   ); break;
        case PROPERTY_CURRENT_ITEM_POS: notifyPropertyChanged(BR.currentItemPos); break;
        }
    }

    @Override
    public void close() {
        if (header != null)
            header.close();
        if (dataSource != null) {
            dataSource.forEach(TItem::close);
            dataSource.clear();
        }
        notifier     .removeListener(this::onPropertyChanged);
        notifierAsync.removeListener(this::onAsyncPropertyChanged);
        notifier.close();
        notifierAsync.close();
    }


    @Override
    public void addListener(PropertyChangeListener listener) {
        notifierAsync.addListener(listener);
    }
    @Override
    public void removeListener(PropertyChangeListener listener) {
        notifierAsync.removeListener(listener);
    }

}
