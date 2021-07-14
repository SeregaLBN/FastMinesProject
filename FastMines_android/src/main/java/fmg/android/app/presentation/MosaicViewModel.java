package fmg.android.app.presentation;

import android.graphics.Bitmap;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.lifecycle.ViewModel;

import fmg.android.app.BR;
import fmg.android.img.Smile;
import fmg.android.mosaic.MosaicViewController;
import fmg.android.utils.Cast;
import fmg.common.Logger;
import fmg.common.geom.SizeDouble;
import fmg.common.ui.ITimer;
import fmg.core.img.SmileModel;
import fmg.core.types.EGameStatus;

/** ViewModel for {@link fmg.android.app.MosaicActivity} */
public class MosaicViewModel extends ViewModel {

    private final MosaicViewController mosaicController;
    private final TopPanel topPanel;
    private long time;
    private Smile.BitmapController btnNewImage;

    public MosaicViewModel() {
        mosaicController = new MosaicViewController(null);
        topPanel = new TopPanel();
        btnNewImage = new Smile.BitmapController(SmileModel.EFaceType.Face_WhiteSmiling);
        float size = Cast.dpToPx(25);
        btnNewImage.getModel().setSize(new SizeDouble(size, size));
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

    public void setBtnNewFaceType(SmileModel.EFaceType btnNewFaceType) {
        btnNewImage.getModel().setFaceType(btnNewFaceType);
        // reload UI
        topPanel.notifyPropertyChanged(BR.btnNewImg);
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

        @Bindable
        public Bitmap getBtnNewImg() {
            return btnNewImage.getImage();
        }

    }

}
