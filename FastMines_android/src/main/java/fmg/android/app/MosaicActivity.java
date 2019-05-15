package fmg.android.app;

import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.function.Consumer;

import fmg.android.app.databinding.MosaicActivityBinding;
import fmg.android.app.model.SharedData;
import fmg.android.app.presentation.MosaicViewModel;
import fmg.android.mosaic.MosaicViewController;
import fmg.common.LoggerSimple;
import fmg.core.mosaic.MosaicGameModel;
import fmg.core.mosaic.MosaicInitData;
import fmg.core.mosaic.cells.BaseCell;

/** general activity of project */
public class MosaicActivity extends AppCompatActivity {

    private MosaicViewController mosaicController;
    private MosaicActivityBinding binding;
    private MosaicViewModel viewModel;
    private final PropertyChangeListener onMosaicControllerPropertyChangedListener = this::onMosaicControllerPropertyChanged;

    public MosaicInitData getInitData() { return SharedData.getMosaicInitData(); }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LoggerSimple.put("MosaicActivity.onCreate: this.hash={0}", this.hashCode());
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.mosaic_activity);
        viewModel = ViewModelProviders.of(this).get(MosaicViewModel.class);
        binding.setViewModel(viewModel);
        binding.executePendingBindings();


        initController();
    }

    /** Mosaic controller */
    public MosaicViewController getMosaicController() {
        if (mosaicController == null) {
            //setMosaicController(new MosaicViewController(this));
            Consumer<Consumer<Canvas>> drawMethod = cdm -> ((DrawableView)binding.mosaicView).drawMethod = cdm;
            mosaicController = new MosaicViewController(binding.mosaicView, drawMethod);
            mosaicController.setBindSizeDirection(false);
        }
        return mosaicController;
    }

    private void initController() {
        MosaicInitData initData = getInitData();
        MosaicViewController controller = getMosaicController();
        controller.setMinesCount(initData.getMinesCount());
        MosaicGameModel model = controller.getModel();
        model.setMosaicType(initData.getMosaicType());
        model.setSizeField(initData.getSizeField());

        List<BaseCell> all = viewModel.getMosaicModel().getMatrix();
        // TODO... restrore data
    }

    @Override
    protected void onStart() {
        LoggerSimple.put("MosaicActivity.onStart: this.hash={0}", this.hashCode());
        super.onStart();
    }

    @Override
    protected void onRestart() {
        LoggerSimple.put("MosaicActivity.onRestart: this.hash={0}", this.hashCode());
        super.onRestart();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        LoggerSimple.put("MosaicActivity.onSaveInstanceState: this.hash={0}", this.hashCode());
        //SharedData.save(savedInstanceState, getSomeData());
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onResume() {
        LoggerSimple.put("MosaicActivity.onResume: this.hash={0}", this.hashCode());
        super.onResume();
        getMosaicController().addListener(onMosaicControllerPropertyChangedListener);
    }

    @Override
    public void onPause() {
        LoggerSimple.put("MosaicActivity.onPause: this.hash={0}", this.hashCode());
        super.onPause();
        getMosaicController().removeListener(onMosaicControllerPropertyChangedListener);
    }

    @Override
    protected void onStop() {
        LoggerSimple.put("MosaicActivity.onStop: this.hash={0}", this.hashCode());
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        LoggerSimple.put("MosaicActivity.onDestroy: this.hash={0}", this.hashCode());
        super.onDestroy();
        mosaicController.close();
    }

    private void onMosaicControllerPropertyChanged(PropertyChangeEvent ev) {
    }

}
