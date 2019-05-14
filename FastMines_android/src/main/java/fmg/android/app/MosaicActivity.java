package fmg.android.app;

import android.databinding.DataBindingUtil;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.function.Consumer;

import fmg.android.app.databinding.MosaicActivityBinding;
import fmg.android.app.model.MosaicInitDataExt;
import fmg.android.mosaic.MosaicViewController;
import fmg.core.mosaic.MosaicGameModel;
import fmg.core.mosaic.MosaicInitData;

/** general activity of project */
public class MosaicActivity extends AppCompatActivity {

    private MosaicViewController mosaicController;
    private MosaicActivityBinding binding;
    private final PropertyChangeListener onMosaicControllerPropertyChangedListener = this::onMosaicControllerPropertyChanged;

    public MosaicInitData getInitData() { return MosaicInitDataExt.getSharedData(); }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.mosaic_activity);
        binding.executePendingBindings();


        MosaicInitData initData = getInitData();
        MosaicViewController controller = getMosaicController();
        controller.setMinesCount(initData.getMinesCount());
        MosaicGameModel model = controller.getModel();
        model.setMosaicType(initData.getMosaicType());
        model.setSizeField(initData.getSizeField());
    }

    /** Mosaic controller */
    public MosaicViewController getMosaicController() {
        if (mosaicController == null) {
            //setMosaicController(new MosaicViewController(this));
            Consumer<Consumer<Canvas>> drawMethod = cdm -> ((MosaicView)binding.mosaicView).drawMethod = cdm;
            setMosaicController(new MosaicViewController(binding.mosaicView, drawMethod));
        }
        return mosaicController;
    }
    private void setMosaicController(MosaicViewController mosaicController) {
        if (this.mosaicController != null) {
            this.mosaicController.removeListener(onMosaicControllerPropertyChangedListener);
            this.mosaicController.close();
        }
        this.mosaicController = mosaicController;
        if (this.mosaicController != null)
            this.mosaicController.setBindSizeDirection(false);
            this.mosaicController.addListener(onMosaicControllerPropertyChangedListener);
    }

    private void onMosaicControllerPropertyChanged(PropertyChangeEvent ev) {
    }

}
