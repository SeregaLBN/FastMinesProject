package fmg.android.app.presentation;

import android.arch.lifecycle.ViewModel;

import fmg.android.mosaic.MosaicViewController;

/** ViewModel for {@link fmg.android.app.MosaicActivity} */
public class MosaicViewModel extends ViewModel {

    private final MosaicViewController mosaicController;

    public MosaicViewModel() {
        mosaicController = new MosaicViewController(null);
    }

    public MosaicViewController getMosaicController() {
        return mosaicController;
    }

    @Override
    protected void onCleared() {
        //mosaicController.close();
    }

}
