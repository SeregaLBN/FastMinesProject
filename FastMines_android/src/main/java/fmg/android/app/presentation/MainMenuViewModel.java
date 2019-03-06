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
import fmg.common.LoggerSimple;
import fmg.common.geom.util.FigureHelper;

/** ViewModel for {@link fmg.android.app.MainActivity} */
public class MainMenuViewModel extends ViewModel {

    private final MosaicGroupDataSource mosaicGroupDS = new MosaicGroupDataSource();
    private final MosaicSkillDataSource mosaicSkillDS = new MosaicSkillDataSource();
    private final SplitViewPane splitViewPane = new SplitViewPane();

    public class SplitViewPane extends BaseObservable {

        private boolean open = true;
        private double currentStepAngle = open ? 360 : 0; // smooth angle

        @Bindable
        public SplitViewPane getSelf() { return this; }
        void fireEvent() {
//            LoggerSimple.put("> SplitViewPane::fireEvent");
            notifyPropertyChanged(BR.self);
        }

        public boolean isOpen() { return open; }
        public void   setOpen(boolean opened) {
            if (this.open == opened)
                return;
//            LoggerSimple.put("> SplitViewPane::setOpen: opened={0}", opened);
            this.open = opened;
            getSplitViewPane().fireEvent();
            SmoothHelper.runWidthSmoothTransition(this);
        }

        boolean isSmoothInProgress() {
            boolean b = (currentStepAngle > 0) &&  (currentStepAngle < 360);
//            LoggerSimple.put("> SplitViewPane::isSmoothInProgress: return {0}", b);
            return b;
        }

        boolean isSmoothIsFinished() {
            boolean b = isOpen()
                    ? (currentStepAngle >= 360)
                    : (currentStepAngle <= 0);
//            LoggerSimple.put("> SplitViewPane::isSmoothIsFinished: return {0}", b);
            return b;
        }

        public double getCurrentStepAngle() { return currentStepAngle; }
        public void setCurrentStepAngle(double currentStepAngle) {
            this.currentStepAngle = isOpen()
                    ? Math.min(360, currentStepAngle)
                    : Math.max(0  , currentStepAngle);
//            LoggerSimple.put("> SplitViewPane::setCurrentStepAngle: currentStepAngle={0}; this.currentStepAngle={1}", currentStepAngle, this.currentStepAngle);
            getSplitViewPane().fireEvent();
        }

        public double getPaneWidth() {
            double rad = FigureHelper.toRadian(currentStepAngle / 4);
            double koef = isOpen()
                    ? Math.sin(rad)
                    : 1 - Math.cos(rad);
            double val = MainMenuViewModel.this.getMosaicGroupDS().getImageSize().width
                    + koef * Cast.dpToPx(MainActivity.MenuTextWidthDp)
                    + 0; // vertical scrollbar width
//            LoggerSimple.put("> SplitViewPane::getPaneWidth: currStepAngle={0}; koef={1}; return {2}", currentStepAngle, koef, val);
            return val;
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
