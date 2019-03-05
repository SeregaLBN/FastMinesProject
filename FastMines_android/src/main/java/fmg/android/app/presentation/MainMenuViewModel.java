package fmg.android.app.presentation;

import android.arch.lifecycle.ViewModel;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.BindingAdapter;
import android.databinding.ObservableField;
import android.view.View;
import android.view.ViewGroup;

import java.beans.PropertyChangeEvent;

import fmg.android.app.BR;
import fmg.android.app.model.dataSource.MosaicGroupDataSource;
import fmg.android.app.model.dataSource.MosaicSkillDataSource;
import fmg.common.geom.SizeDouble;

/** ViewModel for {@link fmg.android.app.MainActivity} */
public class MainMenuViewModel extends ViewModel {

    private final MosaicGroupDataSource mosaicGroupDS = new MosaicGroupDataSource();
    private final MosaicSkillDataSource mosaicSkillDS = new MosaicSkillDataSource();
    private final SplitViewPane splitViewPane = new SplitViewPane();
    private boolean isSplitViewPaneOpen = true;

    public class SplitViewPane extends BaseObservable {

        /** change open split pane event */ // live hack
        @Bindable
        public SplitViewPane getOpened() { return this; }

        void fireOpenedEvent() { notifyPropertyChanged(BR.opened); }

        public boolean isOpen() { return isSplitViewPaneOpen; }
        public SizeDouble getImageSize() { return MainMenuViewModel.this.getMosaicGroupDS().getImageSize(); }
    }


    public MainMenuViewModel() {
        mosaicGroupDS.addListener(this::onMosaicGroupDsPropertyChanged);
    }

    public boolean isSplitViewPaneOpen() { return isSplitViewPaneOpen; }
    public void   setSplitViewPaneOpen(boolean opened) {
        if (isSplitViewPaneOpen != opened) {
            isSplitViewPaneOpen = opened;
            getSplitViewPane().fireOpenedEvent();
        }
    }

    public MosaicGroupDataSource getMosaicGroupDS() { return mosaicGroupDS; }
    public MosaicSkillDataSource getMosaicSkillDS() { return mosaicSkillDS; }
    public SplitViewPane getSplitViewPane() { return splitViewPane; }

    private void onMosaicGroupDsPropertyChanged(PropertyChangeEvent ev) {
        assert (ev.getSource() == mosaicGroupDS);

        switch (ev.getPropertyName()) {
        case MosaicGroupDataSource.PROPERTY_IMAGE_SIZE:
            getSplitViewPane().fireOpenedEvent();
            break;
        case MosaicGroupDataSource.PROPERTY_CURRENT_ITEM:
            //// auto-close split view pane
            //splitViewPane.setOpen(false);
            break;
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        mosaicGroupDS.removeListener(this::onMosaicGroupDsPropertyChanged);
        mosaicGroupDS.close();
        mosaicSkillDS.close();
    }

}
