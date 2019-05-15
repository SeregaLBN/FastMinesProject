package fmg.android.app.presentation;

import android.arch.lifecycle.ViewModel;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.View;

import java.util.function.Consumer;

import fmg.core.mosaic.MosaicDrawModel;

/** ViewModel for {@link fmg.android.app.MosaicActivity} */
public class MosaicViewModel extends ViewModel {

    private final MosaicDrawModel<Bitmap> mosaicModel;

    public MosaicViewModel(View view, Consumer<Consumer<Canvas>> viewDrawMethod) {
        mosaicModel = new MosaicDrawModel<>();
    }

    public MosaicDrawModel<Bitmap> getMosaicModel() {
        return mosaicModel;
    }

    @Override
    protected void onCleared() {
        //mosaicController.close();
    }

}
