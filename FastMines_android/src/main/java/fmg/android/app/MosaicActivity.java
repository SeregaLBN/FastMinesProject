package fmg.android.app;

import android.os.Bundle;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import fmg.android.app.databinding.MosaicActivityBinding;
import fmg.android.app.model.SharedData;
import fmg.android.app.presentation.MosaicViewModel;
import fmg.android.mosaic.MosaicViewController;
import fmg.android.utils.Timer;
import fmg.common.Logger;
import fmg.core.mosaic.MosaicController;
import fmg.core.mosaic.MosaicGameModel;
import fmg.core.mosaic.MosaicInitData;
import fmg.core.types.EGameStatus;

/** Game field activity of the project */
public class MosaicActivity extends AppCompatActivity {

    private MosaicActivityBinding binding;
    private MosaicViewModel viewModel;
    private Timer timer;
    private final PropertyChangeListener onMosaicControllerPropertyChangedListener = this::onMosaicControllerPropertyChanged;

    public MosaicInitData getInitData() { return SharedData.getMosaicInitData(); }

    public MosaicActivity() {
        Logger.info("MosaicActivity.ctor: this.hash={0}", this.hashCode());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Logger.info("MosaicActivity.onCreate: this.hash={0}", this.hashCode());
        super.onCreate(savedInstanceState);

        // Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        binding = DataBindingUtil.setContentView(this, R.layout.mosaic_activity);
        viewModel = ViewModelProviders
                .of(this)
                .get(MosaicViewModel.class);

        timer = new Timer();
        timer.setOffset(viewModel.getTime());
        timer.setInterval(1000);
        timer.setCallback(viewModel::onTimerCallback);

        MosaicViewController ctrl = viewModel.getMosaicController();
        ctrl.setBindSizeDirection(false);
        ctrl.setExtendedManipulation(true);
        ctrl.getModel().setAutoFit(false);
        binding.setViewModel(viewModel);
        binding.executePendingBindings();

        // init mosaic controller
        MosaicInitData initData = getInitData();
        MosaicViewController controller = getMosaicController();
        MosaicGameModel model = controller.getModel();
        model.setMosaicType(initData.getMosaicType());
        model.setSizeField(initData.getSizeField());
        controller.setCountMines(initData.getCountMines());
    }

    /** Mosaic controller */
    public MosaicViewController getMosaicController() {
        return viewModel.getMosaicController();
    }

    @Override
    protected void onStart() {
        Logger.info("MosaicActivity.onStart: this.hash={0}", this.hashCode());
        super.onStart();
    }

    @Override
    protected void onRestart() {
        Logger.info("MosaicActivity.onRestart: this.hash={0}", this.hashCode());
        super.onRestart();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        Logger.info("MosaicActivity.onSaveInstanceState: this.hash={0}", this.hashCode());
        //SharedData.save(savedInstanceState, getSomeData());
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onResume() {
        Logger.info("MosaicActivity.onResume: this.hash={0}", this.hashCode());
        super.onResume();
        getMosaicController().setViewControl(binding.mosaicView);
        getMosaicController().addListener(onMosaicControllerPropertyChangedListener);
        viewModel.onTimerCallback(timer);
        if (getMosaicController().getGameStatus() == EGameStatus.eGSPlay)
            timer.start();
    }

    @Override
    public void onPause() {
        Logger.info("MosaicActivity.onPause: this.hash={0}", this.hashCode());
        super.onPause();
        getMosaicController().removeListener(onMosaicControllerPropertyChangedListener);
        getMosaicController().setViewControl(null);
        timer.pause();
    }

    @Override
    protected void onStop() {
        Logger.info("MosaicActivity.onStop: this.hash={0}", this.hashCode());
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Logger.info("MosaicActivity.onDestroy: this.hash={0}", this.hashCode());
        super.onDestroy();
        //getMosaicController().close();
        timer.close();
    }

    private void onMosaicControllerPropertyChanged(PropertyChangeEvent ev) {
        switch (ev.getPropertyName()) {
        case MosaicController.PROPERTY_GAME_STATUS:
            switch ((EGameStatus)ev.getNewValue()) {
            case eGSCreateGame:
            case eGSReady:
                timer.reset();
                break;
            case eGSPlay:
                timer.start();
                break;
            case eGSEnd:
                timer.pause();
                break;
            }
            viewModel.onTimerCallback(timer); // reload UI
            break;
        }
    }

}
