package fmg.android.app.model.dataSource;

import android.graphics.Bitmap;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import java.util.List;
import java.util.function.Consumer;

import fmg.android.app.BR;
import fmg.android.app.model.items.BaseDataItem;
import fmg.common.Color;
import fmg.common.geom.SizeDouble;
import fmg.common.ui.UiInvoker;
import fmg.core.img.IImageModel2;
import fmg.core.img.IImageView2;
import fmg.core.img.ImageController2;
import fmg.core.mosaic.MosaicModel2;

/** Base container for image items */
public abstract class BaseDataSource<THeader extends BaseDataItem<THeaderId, THeaderModel, THeaderView, THeaderCtrlr>,
                                     THeaderId,
                                     THeaderModel extends IImageModel2,
                                     THeaderView  extends IImageView2<Bitmap>,
                                     THeaderCtrlr extends ImageController2<Bitmap, THeaderView, THeaderModel>,

                                     TItem   extends BaseDataItem<TItemId, TItemModel, TItemView, TItemCtrlr>,
                                     TItemId,
                                     TItemModel extends IImageModel2,
                                     TItemView  extends IImageView2<Bitmap>,
                                     TItemCtrlr extends ImageController2<Bitmap, TItemView, TItemModel>>
    extends BaseObservable
    implements AutoCloseable
{
    public static Color DefaultBkColor = MosaicModel2.DefaultBkColor;

    public static final String PROPERTY_DATA_SOURCE      = "DataSource";
    public static final String PROPERTY_HEADER           = "Header";
    public static final String PROPERTY_IMAGE_SIZE       = "ImageSize";
    public static final String PROPERTY_CURRENT_ITEM     = "CurrentItem";
    public static final String PROPERTY_CURRENT_ITEM_POS = "CurrentItemPos";

    /** Images that describes this data source */
    protected THeader header;
    /** Data source - images that describes the elements */
    protected List<TItem> dataSource; // TODO??? MutableLiveData<...>
    
    /** Current item index in {@link #dataSource} */
    protected int currentItemPos = NOT_SELECTED_POS;
    
    private boolean disposed;

    public static final int NOT_SELECTED_POS = -1; // RecyclerView.NO_POSITION;

    private Consumer<String> changedCallback;

    protected BaseDataSource() {
    }

    /** the top item that this data source describes */
    @Bindable
    public abstract THeader getHeader();

    /** <summary> list of items */
    @Bindable
    public abstract List<TItem> getDataSource();

    /** Selected element */
    @Bindable
    public TItem getCurrentItem() {
        int pos = getCurrentItemPos();
        if (pos < 0)
            return null;
        return getDataSource().get(pos);
    }
    public void setCurrentItem(TItem activeItem) {
        setCurrentItemPos(getDataSource().indexOf(activeItem));
    }

    /** Selected index of element */
    @Bindable
    public int getCurrentItemPos() { return currentItemPos; }
    public void setCurrentItemPos(int pos) {
        if ((pos < 0) || (pos >= getDataSource().size())) {
            if (pos != NOT_SELECTED_POS)
                throw new IllegalArgumentException("Illegal index of pos=" + pos);
        }
        if (pos == currentItemPos)
            return;

        firePropertyChanged(PROPERTY_CURRENT_ITEM_POS);
    }

    @Bindable
    public SizeDouble getImageSize() {
        return getDataSource().stream().map(x -> x.getSize()).findAny().orElseGet(() -> new SizeDouble(-123, -456));
    }
    public void setImageSize(SizeDouble size) {
        SizeDouble old = getImageSize();
        getDataSource().forEach(mi -> mi.setSize(size));

        if (!old.equals(size))
            firePropertyChanged(PROPERTY_IMAGE_SIZE);
    }

    /** for one selected - start animate; for all other - stop animate */
    protected abstract void onCurrentItemChanged();

    protected void firePropertyChanged(String propertyName) {
        if (disposed)
            return;

        if (changedCallback != null) {

            switch (propertyName) {
            case PROPERTY_CURRENT_ITEM_POS:
                onCurrentItemChanged();
                changedCallback.accept(PROPERTY_CURRENT_ITEM);
                break;
            }
        }

        // refire as async event
        UiInvoker.Deferred.accept(() -> firePropertyChangedAsync(propertyName));
    }

    protected void firePropertyChangedAsync(String propertyName) {
        if (disposed)
            return;

        // refire as android data binding event
        switch (propertyName) {
        case PROPERTY_DATA_SOURCE     : notifyPropertyChanged(BR.dataSource    ); break;
        case PROPERTY_HEADER          : notifyPropertyChanged(BR.header        ); break;
        case PROPERTY_IMAGE_SIZE      : notifyPropertyChanged(BR.imageSize     ); break;
        case PROPERTY_CURRENT_ITEM    : notifyPropertyChanged(BR.currentItem   ); break;
        case PROPERTY_CURRENT_ITEM_POS: notifyPropertyChanged(BR.currentItemPos); break;
        }
    }

    protected boolean isDisposed() { return disposed; }

    public void setListener(Consumer<String> callback) {
        if (callback == null) {
            // unset
            changedCallback = null;
        } else {
            // set
            if (changedCallback != null)
                throw new IllegalArgumentException("The callback is already set");
            changedCallback = callback;
        }
    }

    @Override
    public void close() {
        changedCallback = null;
        disposed = true;
        if (header != null)
            header.close();
        if (dataSource != null) {
            dataSource.forEach(TItem::close);
            dataSource.clear();
        }
    }

}
