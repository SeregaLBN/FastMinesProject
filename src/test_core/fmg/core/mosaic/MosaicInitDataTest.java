package fmg.core.mosaic;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.junit.*;

import fmg.common.LoggerSimple;
import fmg.common.notyfier.PropertyChangeExecutor;
import fmg.common.ui.Factory;
import fmg.core.types.EMosaic;
import fmg.core.types.ESkillLevel;
import io.reactivex.Flowable;

public class MosaicInitDataTest {


    public static void StaticInitializer() {
//        ExecutorService scheduler = Executors.newScheduledThreadPool(1);
//        Factory.DEFERR_INVOKER = scheduler::execute;

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        Factory.DEFERR_INVOKER = run -> scheduler.schedule(run, 10, TimeUnit.MILLISECONDS);
    }

    @BeforeClass
    public static void setup() {
        LoggerSimple.put(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        LoggerSimple.put("> MosaicInitDataTest::setup");

        StaticInitializer();

        Flowable.just("UI factory inited...").subscribe(LoggerSimple::put);
    }

    @Before
    public void before() {
        LoggerSimple.put("======================================================");
    }

    @AfterClass
    public static void after() {
        LoggerSimple.put("======================================================");
        LoggerSimple.put("< MosaicInitDataTest closed");
        LoggerSimple.put("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
    }

    private static MosaicInitData createMosaicInitData() {
        MosaicInitData initData = new MosaicInitData();
        assertEquals(MosaicInitData.DEFAULT_MOSAIC_TYPE , initData.getMosaicType());
        assertEquals(MosaicInitData.DEFAULT_SIZE_FIELD_M, initData.getSizeField().m);
        assertEquals(MosaicInitData.DEFAULT_SIZE_FIELD_N, initData.getSizeField().n);
        assertEquals(MosaicInitData.DEFAULT_MINES_COUNT , initData.getMinesCount());
        assertEquals(MosaicInitData.DEFAULT_SKILL_LEVEL , initData.getSkillLevel());
        return initData;
    }

    @Test
    public void checkTheImpossibilitySetCustomSkillLevelTest() {
        LoggerSimple.put("> checkTheImpossibilitySetCustomSkillLevelTest");
        try (MosaicInitData initData = createMosaicInitData()) {
            initData.setSkillLevel(ESkillLevel.eCustom);
            Assert.fail();
        } catch (Exception ex) {
            assertEquals(IllegalArgumentException.class, ex.getClass());
        }
    }

    @Test
    public void checkIfMosaicTypeIsChangedThenMinesCountWillAlsoBeChangedTest() {
        LoggerSimple.put("> checkIfMosaicTypeIsChangedThenMinesCountWillAlsoBeChangedTest");
        try (MosaicInitData initData = createMosaicInitData()) {
            new PropertyChangeExecutor<>(initData).run(100, 1000,
                () -> {
                    initData.setMosaicType(EMosaic.eMosaicRhombus1);
                }, modifiedProperties -> {
                    LoggerSimple.put("  checking...");
                    Assert.assertTrue  (   modifiedProperties.containsKey(MosaicInitData.PROPERTY_MOSAIC_TYPE));
                    Assert.assertEquals(1, modifiedProperties.get(        MosaicInitData.PROPERTY_MOSAIC_TYPE).first.intValue());
                    Assert.assertTrue  (   modifiedProperties.containsKey(MosaicInitData.PROPERTY_MINES_COUNT));
                    Assert.assertEquals(1, modifiedProperties.get(        MosaicInitData.PROPERTY_MINES_COUNT).first.intValue());
                    Assert.assertEquals(2, modifiedProperties.size());
                    Assert.assertEquals(EMosaic.eMosaicRhombus1, initData.getMosaicType());
                    Assert.assertEquals(initData.getSkillLevel().getNumberMines(EMosaic.eMosaicRhombus1), initData.getMinesCount());
                });
        }
    }

    @Test
    public void checkNoRepeatNotificationsTest() {
        LoggerSimple.put("> checkNoRepeatNotificationsTest");
        try (MosaicInitData initData = createMosaicInitData()) {
            new PropertyChangeExecutor<>(initData).run(100, 1000,
                () -> {
                    LoggerSimple.put("    initData.minesCount={0}", initData.getMinesCount());
                    initData.setMosaicType(EMosaic.eMosaicRhombus1);
                    LoggerSimple.put("    initData.minesCount={0}", initData.getMinesCount());
                    initData.setMosaicType(EMosaic.eMosaicHexagon1);
                    LoggerSimple.put("    initData.minesCount={0}", initData.getMinesCount());
                }, modifiedProperties -> {
                    LoggerSimple.put("  checking...");
                    Assert.assertTrue  (   modifiedProperties.containsKey(MosaicInitData.PROPERTY_MOSAIC_TYPE));
                    Assert.assertEquals(1, modifiedProperties.get(        MosaicInitData.PROPERTY_MOSAIC_TYPE).first.intValue());
                    Assert.assertTrue  (  !modifiedProperties.containsKey(MosaicInitData.PROPERTY_MINES_COUNT));
                    Assert.assertEquals(1, modifiedProperties.size());
                    Assert.assertEquals(EMosaic.eMosaicHexagon1, initData.getMosaicType());
                    Assert.assertEquals(initData.getSkillLevel().getNumberMines(EMosaic.eMosaicHexagon1), initData.getMinesCount());
                });
        }
    }

}
