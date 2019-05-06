package fmg.android.app.presentation;

import android.arch.lifecycle.ViewModel;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import fmg.android.app.model.dataSource.MosaicDataSource;
import fmg.android.app.model.dataSource.MosaicGroupDataSource;
import fmg.android.app.model.dataSource.MosaicSkillDataSource;
import fmg.common.geom.SizeDouble;

/** ViewModel for {@link fmg.android.app.SelectMosaicFragment} */
public class MosaicsViewModel extends ViewModel {

    private final MosaicDataSource mosaicDS = new MosaicDataSource();
    private final PropertyChangeListener onMosaicDsPropertyChangedListener = this::onMosaicDsPropertyChanged;

    public MosaicsViewModel() {
        mosaicDS.addListener(onMosaicDsPropertyChangedListener);
    }

    public MosaicDataSource getMosaicDS() { return mosaicDS; }

    public SizeDouble getImageSize()         { return mosaicDS.getImageSize(); }
    public void       setImageSize(SizeDouble size) { mosaicDS.setImageSize(size); }

    private void onMosaicDsPropertyChanged(PropertyChangeEvent ev) {
        switch (ev.getPropertyName()) {
        case MosaicDataSource.PROPERTY_IMAGE_SIZE:
            // TODO ! notify parent container
            //notifier.firePropertyChanged<SizeDouble>(ev, nameof(this.ImageSize));
            break;
        }
    }

    @Override
    protected void onCleared() {
        mosaicDS.removeListener(onMosaicDsPropertyChangedListener);
        mosaicDS.close();
    }

}
