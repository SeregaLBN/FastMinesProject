package fmg.android.app;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import fmg.android.app.databinding.MosaicActivityBinding;
import fmg.android.app.presentation.MosaicViewModel;
import fmg.android.mosaic.MosaicViewController;
import fmg.android.utils.Timer;
import fmg.common.Logger;
import fmg.core.img.SmileModel;
import fmg.core.mosaic.MosaicController;
import fmg.core.mosaic.MosaicGameModel;
import fmg.core.types.ClickResult;
import fmg.core.types.EGameStatus;
import fmg.core.app.model.MosaicInitData;
import fmg.core.types.EMosaic;
import fmg.core.types.ESkillLevel;

/** Game field activity of the project */
public class MosaicActivity extends AppCompatActivity {

    private MosaicActivityBinding binding;
    private MosaicViewModel viewModel;
    private Timer timer;
    private final PropertyChangeListener onMosaicControllerPropertyChangedListener = this::onMosaicControllerPropertyChanged;

    public MosaicInitData getInitData() { return FastMinesApp.get().getMosaicInitData(); }

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
        ctrl.setOnClickEvent(this::onMosaicClickHandler);
        binding.setViewModel(viewModel);
        binding.executePendingBindings();
        binding.btnNewGame.setOnClickListener(this::onBtnNewClick);
        binding.btnNewGame.setOnTouchListener(this::onBtnNewTouch);

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
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
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
        timer.close();
        getMosaicController().setOnClickEvent(null);
    }

    private void onMosaicControllerPropertyChanged(PropertyChangeEvent ev) {
        switch (ev.getPropertyName()) {
        case MosaicController.PROPERTY_GAME_STATUS:
            switch ((EGameStatus)ev.getNewValue()) {
            case eGSCreateGame:
            case eGSReady:
                timer.reset();
                viewModel.setBtnNewGameFaceType(SmileModel.EFaceType.Face_WhiteSmiling);
                break;
            case eGSPlay:
                timer.start();
                break;
            case eGSEnd:
                timer.pause();
                viewModel.setBtnNewGameFaceType(getMosaicController().isVictory()
                                ? SmileModel.EFaceType.Face_SmilingWithSunglasses
                                : SmileModel.EFaceType.Face_Disappointed);

                if (getSkillLevel() != ESkillLevel.eCustom)
                    // сохраняю статистику и чемпиона
                    saveStatisticAndChampion();

                break;
            }
            viewModel.onTimerCallback(timer); // reload UI
            break;
        case MosaicController.PROPERTY_COUNT_MINES_LEFT:
            // refire as android data binding event
            viewModel.getTopPanel().notifyPropertyChanged(BR.minesLeft);
            break;
        }
    }

    private void onBtnNewClick(View view) {
        viewModel.getMosaicController().gameNew();
    }

    private boolean onBtnNewTouch(View view, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            // Pressed
            viewModel.setBtnNewGameFaceType(SmileModel.EFaceType.Face_SavouringDeliciousFood);
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            // Released
            viewModel.setBtnNewGameFaceType(SmileModel.EFaceType.Face_WhiteSmiling);
        }
        return false;
    }

    public void onMosaicClickHandler(ClickResult clickResult) {
        EGameStatus gs = getMosaicController().getGameStatus();
        //Logger.info("OnMosaicClick: down=" + clickResult.isDown + "; leftClick=" + clickResult.isLeft + "; gameStatus=" + gs);
        if (clickResult.isLeft && ((gs == EGameStatus.eGSPlay) || (gs == EGameStatus.eGSReady))) {
            viewModel.setBtnNewGameFaceType(clickResult.isDown
                    ? SmileModel.EFaceType.Face_Grinning
                    : SmileModel.EFaceType.Face_WhiteSmiling);
        }
    }

    public ESkillLevel getSkillLevel() {
        var mc = getMosaicController();
        var mosaicType = mc.getMosaicType();
        var sizeFld = mc.getSizeField();
        var numberMines = mc.getCountMines();
        return ESkillLevel.calcSkillLevel(mosaicType, sizeFld, numberMines);
    }

    /** Сохранить чемпиона && Установить статистику */
    private void saveStatisticAndChampion() {
        MosaicController<?,?,?,?> mc = getMosaicController();
        if (mc.getGameStatus() != EGameStatus.eGSEnd)
            throw new IllegalArgumentException("Invalid method state call");

        // сохраняю все нужные данные
        boolean victory = mc.isVictory();
        ESkillLevel eSkill = getSkillLevel();
        if (eSkill == ESkillLevel.eCustom)
            return;

        final EMosaic eMosaic = mc.getMosaicType();
        final long realCountOpen = victory ? mc.getCountMines() : mc.getCountOpen();
        final long playTime = timer.getTime();
        final int clickCount = mc.getCountClick();

        int pos = FastMinesApp.get().updateStatistic(eMosaic, eSkill, victory, realCountOpen, playTime, clickCount);
        if (pos >= 0)
            Toast.makeText(this.getApplicationContext(),
                           "Your best result is position #" + (pos + 1),
                           Toast.LENGTH_LONG)
                 .show();
    }

}
