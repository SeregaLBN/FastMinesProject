package fmg.android.app.presentation;

import android.arch.lifecycle.ViewModel;
import android.databinding.BaseObservable;
import android.databinding.Bindable;

import java.beans.PropertyChangeEvent;

import fmg.android.app.BR;
import fmg.android.app.MainActivity;
import fmg.android.app.model.dataSource.MosaicGroupDataSource;
import fmg.android.app.model.dataSource.MosaicSkillDataSource;
import fmg.android.utils.Cast;

/** ViewModel for {@link fmg.android.app.MainActivity} */
public class MainMenuViewModel extends ViewModel {

    private final MosaicGroupDataSource mosaicGroupDS = new MosaicGroupDataSource();
    private final MosaicSkillDataSource mosaicSkillDS = new MosaicSkillDataSource();
    private final SplitViewPane splitViewPane = new SplitViewPane();

    public class SplitViewPane extends BaseObservable {

        class SmoothContext extends SmoothHelper.Context {

            @Override
            public void setForward(boolean forward) {
                super.setForward(forward);
                SplitViewPane.this.fireEvent();
            }

            @Override
            public void setCurrentStepAngle(double currentStepAngle) {
                super.setCurrentStepAngle(currentStepAngle);
                SplitViewPane.this.fireEvent();
            }
        }

        private SmoothContext context = new SmoothContext();

        @Bindable
        public SplitViewPane getSelf() { return this; }
        void fireEvent() {
            notifyPropertyChanged(BR.self);
        }

        public boolean isOpen() {
            return context.isForward();
        }
        public void   setOpen(boolean opened) {
            context.setForward(opened);
            SmoothHelper.runSmoothTransition(context, 350, 50);
        }

        public double getPaneWidth() {
            double coef = context.getSmoothCoefficient();
            return MainMenuViewModel.this.getMosaicGroupDS().getImageSize().width
                    + coef * Cast.dpToPx(MainActivity.MenuTextWidthDp)
                    + 0; // vertical scrollbar width
        }

    }


    public MainMenuViewModel() {
        mosaicGroupDS.addListener(this::onMosaicGroupDsPropertyChanged);
    }

    public MosaicGroupDataSource getMosaicGroupDS() { return mosaicGroupDS; }
    public MosaicSkillDataSource getMosaicSkillDS() { return mosaicSkillDS; }
    public SplitViewPane getSplitViewPane() { return splitViewPane; }

    private void onMosaicGroupDsPropertyChanged(PropertyChangeEvent ev) {
        assert (ev.getSource() == mosaicGroupDS);

        switch (ev.getPropertyName()) {
        case MosaicGroupDataSource.PROPERTY_IMAGE_SIZE:
            getSplitViewPane().fireEvent();
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
