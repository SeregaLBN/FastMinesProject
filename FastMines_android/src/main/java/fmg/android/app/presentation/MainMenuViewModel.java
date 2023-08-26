package fmg.android.app.presentation;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.lifecycle.ViewModel;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.function.Consumer;

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
    private Consumer<String> changedMosaicGroupDSCallback;
    private Consumer<String> changedMosaicSkillDSCallback;

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
                    + Cast.dpToPx((float) (getMenuGroupPaddingInDip().dip * 2)) // left and right padding
                    + 0; // vertical scrollbar width
        }

    }


    public MainMenuViewModel() {
        mosaicGroupDS.setListener(this::onMosaicGroupDsPropertyChanged);
        mosaicSkillDS.setListener(this::onMosaicSkillDsPropertyChanged);
    }

    public MosaicGroupDataSource getMosaicGroupDS() { return mosaicGroupDS; }
    public MosaicSkillDataSource getMosaicSkillDS() { return mosaicSkillDS; }
    public SplitViewPane getSplitViewPane() { return splitViewPane; }


    public Converters.DipWrapper getMenuGroupPaddingInDip() {
        // see ./res/drawable/menu_group_border.xml
        //  <shape><stroke android:width=???
        return new Converters.DipWrapper(1);
    }

    private void onMosaicGroupDsPropertyChanged(String propertyName) {
        switch (propertyName) {
            case MosaicGroupDataSource.PROPERTY_IMAGE_SIZE:
                getSplitViewPane().fireEvent();
                break;
            case MosaicGroupDataSource.PROPERTY_CURRENT_ITEM:
                //// auto-close split view pane
                //splitViewPane.setOpen(false);
                break;
        }
        if (changedMosaicGroupDSCallback != null)
            changedMosaicGroupDSCallback.accept(propertyName);
    }

    private void onMosaicSkillDsPropertyChanged(String propertyName) {
        if (changedMosaicSkillDSCallback != null)
            changedMosaicSkillDSCallback.accept(propertyName);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        mosaicGroupDS.setListener(null);
        mosaicGroupDS.close();
        mosaicSkillDS.close();
    }

    public void setMosaicGroupDSListener(Consumer<String> callback) {
        if (callback == null) {
            // unset
            changedMosaicGroupDSCallback = null;
        } else {
            // set
            if (changedMosaicGroupDSCallback != null)
                throw new IllegalArgumentException("The callback is already set");
            changedMosaicGroupDSCallback = callback;
        }
    }

    public void setMosaicSkillDSListener(Consumer<String> callback) {
        if (callback == null) {
            // unset
            changedMosaicSkillDSCallback = null;
        } else {
            // set
            if (changedMosaicSkillDSCallback != null)
                throw new IllegalArgumentException("The callback is already set");
            changedMosaicSkillDSCallback = callback;
        }
    }
}
