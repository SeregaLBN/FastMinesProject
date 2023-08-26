package fmg.android.app.presentation;

import android.graphics.Bitmap;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.lifecycle.ViewModel;

import fmg.android.app.BR;
import fmg.android.img.Smile2;
import fmg.android.mosaic.MosaicViewController2;
import fmg.android.utils.Cast;
import fmg.common.Logger;
import fmg.common.geom.SizeDouble;
import fmg.common.ui.ITimer;
import fmg.core.img.SmileModel2;
import fmg.core.types.EGameStatus;

/** ViewModel for {@link fmg.android.app.MosaicActivity} */
public class MosaicViewModel extends ViewModel {

    private final MosaicViewController2 mosaicController;
    private final TopPanel topPanel;
    private long time;
    private Smile2.SmileAndroidBitmapController btnNewGameImage;

    public MosaicViewModel() {
        mosaicController = new MosaicViewController2(null);
        topPanel = new TopPanel();
        btnNewGameImage = new Smile2.SmileAndroidBitmapController(SmileModel2.EFaceType.Face_WhiteSmiling);
        float size = Cast.dpToPx(25);
        btnNewGameImage.getModel().setSize(new SizeDouble(size, size));
    }

    public MosaicViewController2 getMosaicController() {
        return mosaicController;
    }
    public TopPanel getTopPanel() {
        return topPanel;
    }

    @Override
    protected void onCleared() {
        Logger.info("MosaicViewModel::onCleared");
        mosaicController.close();
        btnNewGameImage.close();
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

    public void setBtnNewGameFaceType(SmileModel2.EFaceType faceType) {
        btnNewGameImage.getModel().setFaceType(faceType);
        // reload UI
        topPanel.notifyPropertyChanged(BR.btnNewGameImg);
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
        public Bitmap getBtnNewGameImg() {
            return btnNewGameImage.getImage();
        }

    }

}
