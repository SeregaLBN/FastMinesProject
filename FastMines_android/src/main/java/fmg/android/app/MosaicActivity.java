package fmg.android.app;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import fmg.android.app.databinding.MosaicActivityBinding;
import fmg.android.app.model.MosaicActivityBackupData;
import fmg.android.app.presentation.MosaicViewModel;
import fmg.android.mosaic.MosaicViewController2;
import fmg.android.utils.AsyncRunner;
import fmg.android.utils.Timer;
import fmg.common.Logger;
import fmg.core.app.model.MosaicInitData;
import fmg.core.img.SmileModel2;
import fmg.core.types.ClickResult;
import fmg.core.types.EGameStatus;
import fmg.core.types.EMosaic;
import fmg.core.types.ESkillLevel;

import static fmg.core.img.PropertyConst.PROPERTY_COUNT_MINES_LEFT;
import static fmg.core.img.PropertyConst.PROPERTY_GAME_STATUS;

/** Game field activity of the project */
public class MosaicActivity extends AppCompatActivity {

    private MosaicActivityBinding binding;
    private MosaicViewModel viewModel;
    private Timer timer;
    private MosaicActivityBackupData backupData;

    public MosaicActivity() {
        Logger.info("MosaicActivity.ctor: ");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Logger.info("MosaicActivity.onCreate: ");
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

        var controller = getMosaicController();
        controller.setBindSizeDirection(false);
        controller.setExtendedManipulation(true);
        //ctrlr.getModel().setAutoFit(false);
        controller.setOnClickEvent(this::onMosaicClickHandler);
        binding.setViewModel(viewModel);
        binding.executePendingBindings();
        binding.btnNewGame.setOnClickListener(this::onBtnNewClick);
        binding.btnNewGame.setOnTouchListener(this::onBtnNewTouch);

        // init mosaic controller
        FastMinesApp app = FastMinesApp.get();
        if (app.hasMosaicActivityBackupData()) {
            AsyncRunner.invokeFromUiDelayed(() -> {
                    MosaicActivityBackupData mosaicActivityBackupData = app.getAndResetMosaicActivityBackupData();
                    controller.gameRestore(mosaicActivityBackupData.mosaicBackupData);
                    controller.getModel().setMosaicOffset(mosaicActivityBackupData.mosaicOffset);
                    timer.setTime(mosaicActivityBackupData.playTime);
                },
                300 // !large MosaicViewController: subjSizeChanged.debounce(200
            );
        } else {
            MosaicInitData initData = app.getMosaicInitData();
            var model = controller.getModel();
            model.setMosaicType(initData.getMosaicType());
            model.setSizeField(initData.getSizeField());
            controller.setCountMines(initData.getCountMines());
        }
    }

    /** Mosaic controller */
    public MosaicViewController2 getMosaicController() {
        return viewModel.getMosaicController();
    }

    @Override
    protected void onStart() {
        Logger.info("MosaicActivity.onStart: ");
        super.onStart();
    }

    @Override
    protected void onRestart() {
        Logger.info("MosaicActivity.onRestart: ");
        super.onRestart();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        Logger.info("MosaicActivity.onSaveInstanceState: ");
        //SharedData.save(savedInstanceState, getSomeData());
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onResume() {
        Logger.info("MosaicActivity.onResume: ");
        super.onResume();
        var controller = getMosaicController();
        controller.setViewControl(binding.mosaicView);
        controller.setListener(this::onMosaicControllerPropertyChanged);
        viewModel.onTimerCallback(timer);
        if (controller.getGameStatus() == EGameStatus.eGSPlay)
            timer.start();
    }

    @Override
    public void onPause() {
        Logger.info("MosaicActivity.onPause: ");
        super.onPause();
        var controller = getMosaicController();
        controller.setListener(null);
        controller.setViewControl(null);
        timer.pause();
    }

    @Override
    protected void onStop() {
        Logger.info("MosaicActivity.onStop: ");

        var controller = getMosaicController();
        if (controller.getGameStatus() == EGameStatus.eGSPlay) {
            backupData = new MosaicActivityBackupData();
            backupData.mosaicBackupData = controller.gameBackup();
            backupData.mosaicOffset     = controller.getModel().getMosaicOffset();
            backupData.playTime         = timer.getTime();
        } else {
            backupData = null;
        }

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Logger.info("MosaicActivity.onDestroy: ");
        super.onDestroy();
        timer.close();
        getMosaicController().setOnClickEvent(null);
    }

    private void onMosaicControllerPropertyChanged(String propertyName) {
        switch (propertyName) {
        case PROPERTY_GAME_STATUS:
            switch (getMosaicController().getGameStatus()) {
            case eGSCreateGame:
            case eGSReady:
                timer.reset();
                viewModel.setBtnNewGameFaceType(SmileModel2.EFaceType.Face_WhiteSmiling);
                break;
            case eGSPlay:
                timer.start();
                break;
            case eGSEnd:
                timer.pause();
                viewModel.setBtnNewGameFaceType(getMosaicController().isVictory()
                                ? SmileModel2.EFaceType.Face_SmilingWithSunglasses
                                : SmileModel2.EFaceType.Face_Disappointed);

                if (getSkillLevel() != ESkillLevel.eCustom)
                    // сохраняю статистику и чемпиона
                    saveStatisticAndChampion();

                break;
            }
            viewModel.onTimerCallback(timer); // reload UI
            break;
        case PROPERTY_COUNT_MINES_LEFT:
            // refire as android data binding event
            viewModel.getTopPanel().notifyPropertyChanged(BR.minesLeft);
            break;
        }
    }

    private void onBtnNewClick(View view) {
        var controller = getMosaicController();
        if (controller.getGameStatus() != EGameStatus.eGSPlay) {
            controller.gameNew();
            return;
        }

        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("New game?")
                .setPositiveButton("Yes", (dialog, which) -> controller.gameNew())
                .setNegativeButton("No", null)
                .show();
    }

    private boolean onBtnNewTouch(View view, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            // Pressed
            viewModel.setBtnNewGameFaceType(SmileModel2.EFaceType.Face_SavouringDeliciousFood);
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            // Released
            viewModel.setBtnNewGameFaceType(SmileModel2.EFaceType.Face_WhiteSmiling);
        }
        return false;
    }

    public void onMosaicClickHandler(ClickResult clickResult) {
        EGameStatus gs = getMosaicController().getGameStatus();
        //Logger.info("OnMosaicClick: down=" + clickResult.isDown + "; leftClick=" + clickResult.isLeft + "; gameStatus=" + gs);
        if (clickResult.isLeft && ((gs == EGameStatus.eGSPlay) || (gs == EGameStatus.eGSReady))) {
            viewModel.setBtnNewGameFaceType(clickResult.isDown
                    ? SmileModel2.EFaceType.Face_Grinning
                    : SmileModel2.EFaceType.Face_WhiteSmiling);
        }
    }

    public ESkillLevel getSkillLevel() {
        var controller = getMosaicController();
        var model = controller.getModel();
        var mosaicType = model.getMosaicType();
        var sizeFld = model.getSizeField();
        var numberMines = controller.getCountMines();
        return ESkillLevel.calcSkillLevel(mosaicType, sizeFld, numberMines);
    }

    /** Сохранить чемпиона && Установить статистику */
    private void saveStatisticAndChampion() {
        var controller = getMosaicController();
        var model = controller.getModel();
        if (controller.getGameStatus() != EGameStatus.eGSEnd)
            throw new IllegalArgumentException("Invalid method state call");

        // сохраняю все нужные данные
        boolean victory = controller.isVictory();
        ESkillLevel eSkill = getSkillLevel();
        if (eSkill == ESkillLevel.eCustom)
            return;

        final EMosaic eMosaic = model.getMosaicType();
        final long realCountOpen = victory ? controller.getCountMines() : controller.getCountOpen();
        final long playTime = timer.getTime();
        final int clickCount = controller.getCountClick();

        int pos = FastMinesApp.get().updateStatistic(eMosaic, eSkill, victory, realCountOpen, playTime, clickCount);
        if (pos >= 0)
            Toast.makeText(this.getApplicationContext(),
                           "Your best result is position #" + (pos + 1),
                           Toast.LENGTH_LONG)
                 .show();
    }

    public MosaicActivityBackupData getBackupData() {
        return backupData;
    }

    @Override
    public void onBackPressed() {
        if (getMosaicController().getGameStatus() != EGameStatus.eGSPlay) {
            finish();
            return;
        }

        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Confirm exit")
//                .setMessage("Confirm exit")
                .setPositiveButton("Yes", (dialog, which) -> finish())
                .setNegativeButton("No", null)
                .show();
    }

}
