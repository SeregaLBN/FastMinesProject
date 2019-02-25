package fmg.android.app.presentation;

import android.arch.lifecycle.ViewModel;
import android.databinding.Bindable;
import android.databinding.BindingAdapter;
import android.databinding.ObservableField;
import android.view.View;
import android.view.ViewGroup;

import java.beans.PropertyChangeEvent;

import fmg.android.app.model.dataSource.MosaicGroupDataSource;
import fmg.android.app.model.dataSource.MosaicSkillDataSource;

/** ViewModel for {@link fmg.android.app.MainActivity} */
public class MainMenuViewModel extends ViewModel {

    private final MosaicGroupDataSource mosaicGroupDS = new MosaicGroupDataSource();
    private final MosaicSkillDataSource mosaicSkillDS = new MosaicSkillDataSource();
    private boolean isSplitViewPaneOpen;

    public MainMenuViewModel() {
//        ToggleSplitViewPaneCommand = new Command(() => IsSplitViewPaneOpen = !IsSplitViewPaneOpen);

        mosaicGroupDS.addListener(this::onMosaicGroupDsPropertyChanged);
    }

//    public ICommand ToggleSplitViewPaneCommand { get; private set; }
//
//    public bool IsSplitViewPaneOpen {
//        get { return _isSplitViewPaneOpen; }
//        set { SetProperty(ref _isSplitViewPaneOpen, value); }
//    }

    public MosaicGroupDataSource getMosaicGroupDS() { return mosaicGroupDS; }
    public MosaicSkillDataSource getMosaicSkillDS() { return mosaicSkillDS; }

    private void onMosaicGroupDsPropertyChanged(PropertyChangeEvent ev) {
        assert (ev.getSource() == mosaicGroupDS);

        switch (ev.getPropertyName()) {
        case MosaicGroupDataSource.PROPERTY_CURRENT_ITEM:
            //// auto-close split view pane
            //this.IsSplitViewPaneOpen = false;
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
