package fmg.android.app.presentation;

import androidx.lifecycle.ViewModel;

import java.util.function.Consumer;

import fmg.android.app.model.dataSource.MosaicDataSource;
import fmg.common.geom.SizeDouble;

/** ViewModel for {@link fmg.android.app.SelectMosaicFragment} */
public class MosaicDsViewModel extends ViewModel {

    private final MosaicDataSource mosaicDS = new MosaicDataSource();
    private Consumer<String> changedMosaicDSCallback;

    public MosaicDsViewModel() {
        mosaicDS.setListener(this::onMosaicDsPropertyChanged);
    }

    public MosaicDataSource getMosaicDS() { return mosaicDS; }

    public SizeDouble getImageSize()         { return mosaicDS.getImageSize(); }
    public void       setImageSize(SizeDouble size) { mosaicDS.setImageSize(size); }

    private void onMosaicDsPropertyChanged(String propertyName) {
        switch (propertyName) {
        case MosaicDataSource.PROPERTY_IMAGE_SIZE:
            // TODO ! notify parent container
            //notifier.firePropertyChanged<SizeDouble>(ev, nameof(this.ImageSize));
            break;
        }
        if (changedMosaicDSCallback != null)
            changedMosaicDSCallback.accept(propertyName);
    }

    @Override
    protected void onCleared() {
        mosaicDS.setListener(null);
        mosaicDS.close();
        changedMosaicDSCallback = null;
    }

    public void setMosaicDSListener(Consumer<String> callback) {
        if (callback == null) {
            // unset
            changedMosaicDSCallback = null;
        } else {
            // set
            if (changedMosaicDSCallback != null)
                throw new IllegalArgumentException("The callback is already set");
            changedMosaicDSCallback = callback;
        }
    }

}
