package fmg.android.app.presentation;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.lifecycle.ViewModel;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.SimpleDateFormat;

import fmg.android.app.BR;
import fmg.android.mosaic.MosaicViewController;
import fmg.android.utils.Timer;
import fmg.common.Logger;
import fmg.common.ui.ITimer;
import fmg.core.mosaic.MosaicController;
import fmg.core.types.EGameStatus;

/** ViewModel for {@link fmg.android.app.MosaicActivity} */
public class MosaicViewModel extends ViewModel {

    private static final SimpleDateFormat timeFormatHms = new SimpleDateFormat("HH:mm:ss");
    private static final SimpleDateFormat timeFormatMs  = new SimpleDateFormat("m:ss");

    private final MosaicViewController mosaicController;
    private final TopPanel topPanel;
    private final transient PropertyChangeListener onMosaicControllerPropertyChangedListener = this::onMosaicControllerPropertyChanged;
    private long time;

    public MosaicViewModel() {
        mosaicController = new MosaicViewController(null);
        topPanel = new TopPanel();

        mosaicController.addListener(onMosaicControllerPropertyChangedListener);
    }

    public MosaicViewController getMosaicController() {
        return mosaicController;
    }
    public TopPanel getTopPanel() {
        return topPanel;
    }

    @Override
    protected void onCleared() {
        Logger.info("MosaicViewModel::onCleared");
        //mosaicController.close();
        mosaicController.removeListener(onMosaicControllerPropertyChangedListener);
    }

    private void onMosaicControllerPropertyChanged(PropertyChangeEvent ev) {
        // refire as android data binding event
        switch (ev.getPropertyName()) {
        case MosaicController.PROPERTY_COUNT_MINES_LEFT:
            topPanel.notifyPropertyChanged(BR.minesLeft);
            break;
        }
    }

    public long getTime() {
        return time;
    }

    public void onTimerCallback(ITimer timer) {
        // save
        this.time = timer.getTime();
        // reload UI
        topPanel.notifyPropertyChanged(BR.timeLeft);
    }

    public class TopPanel extends BaseObservable {

        @Bindable
        public String getMinesLeft() {
            return String.valueOf(MosaicViewModel.this.mosaicController.getCountMinesLeft());
        }

        @Bindable
        public String getTimeLeft() {
            if (mosaicController.getGameStatus() == EGameStatus.eGSEnd)
                return String.valueOf(time / 1000.0); // show time as float (with milliseconds)

            return String.valueOf(time / 1000);       // show time as int (only seconds)
        }
    }

}
