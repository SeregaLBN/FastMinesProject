package fmg.core.mosaic;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.*;

import fmg.common.Logger;
import fmg.common.notifier.PropertyChangeExecutor;
import fmg.core.types.EMosaic;
import fmg.core.types.EMosaicGroup;
import fmg.core.types.ESkillLevel;
import io.reactivex.Flowable;

public class MosaicInitDataTest {

    @BeforeClass
    public static void setup() {
        Logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        Logger.info("> MosaicInitDataTest::setup");

        MosaicModelTest.ProjSettings();

        Flowable.just("UI factory inited...").subscribe(Logger::info);
    }

    @Before
    public void before() {
        Logger.info("======================================================");
    }

    @AfterClass
    public static void after() {
        Logger.info("======================================================");
        Logger.info("< MosaicInitDataTest closed");
        Logger.info("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
    }

    private static MosaicInitData createMosaicInitData() {
        MosaicInitData initData = new MosaicInitData();
        assertEquals(MosaicInitData.DEFAULT_MOSAIC_TYPE , initData.getMosaicType());
        assertEquals(MosaicInitData.DEFAULT_SIZE_FIELD_M, initData.getSizeField().m);
        assertEquals(MosaicInitData.DEFAULT_SIZE_FIELD_N, initData.getSizeField().n);
        assertEquals(MosaicInitData.DEFAULT_COUNT_MINES , initData.getCountMines());
        assertEquals(MosaicInitData.DEFAULT_SKILL_LEVEL , initData.getSkillLevel());

        assertEquals(MosaicInitData.DEFAULT_MOSAIC_TYPE.getGroup(), initData.getMosaicGroup());
        return initData;
    }

    @Test
    public void checkTheImpossibilitySetCustomSkillLevelTest() {
        Logger.info("> MosaicInitDataTest::checkTheImpossibilitySetCustomSkillLevelTest");
        try {
            new PropertyChangeExecutor<>(MosaicInitDataTest::createMosaicInitData).run(10, 1000,
               initData -> {
                   initData.setSkillLevel(ESkillLevel.eCustom);
                   Assert.fail();
               }, (initData, modifiedProperties) -> {});
        } catch (Exception ex) {
            assertEquals(IllegalArgumentException.class, ex.getClass());
        }
    }

    @Test
    public void checkIfMosaicTypeIsChangedThenCountMinesWillAlsoBeChangedTest() {
        Logger.info("> MosaicInitDataTest::checkIfMosaicTypeIsChangedThenCountMinesWillAlsoBeChangedTest");
        new PropertyChangeExecutor<>(MosaicInitDataTest::createMosaicInitData).run(300, 5000,
            initData -> {
                initData.setMosaicType(EMosaic.eMosaicRhombus1);
            }, (initData, modifiedProperties) -> {
                Assert.assertTrue  (   modifiedProperties.containsKey(MosaicInitData.PROPERTY_MOSAIC_TYPE));
                Assert.assertEquals(1, modifiedProperties.get(        MosaicInitData.PROPERTY_MOSAIC_TYPE).first.intValue());
                Assert.assertTrue  (   modifiedProperties.containsKey(MosaicInitData.PROPERTY_COUNT_MINES));
                Assert.assertEquals(1, modifiedProperties.get(        MosaicInitData.PROPERTY_COUNT_MINES).first.intValue());
                Assert.assertEquals(2, modifiedProperties.size());
                Assert.assertEquals(EMosaic.eMosaicRhombus1, initData.getMosaicType());
                Assert.assertEquals(initData.getSkillLevel().getNumberMines(EMosaic.eMosaicRhombus1), initData.getCountMines());
            });
    }

    @Test
    public void checkNoRepeatNotificationsTest() {
        Logger.info("> MosaicInitDataTest::checkNoRepeatNotificationsTest");
            new PropertyChangeExecutor<>(MosaicInitDataTest::createMosaicInitData).run(300, 5000,
                initData -> {
                    Logger.info("    initData.countMines={0}", initData.getCountMines());
                    initData.setMosaicType(EMosaic.eMosaicRhombus1);
                    Logger.info("    initData.countMines={0}", initData.getCountMines());
                    initData.setMosaicType(EMosaic.eMosaicHexagon1);
                    Logger.info("    initData.countMines={0}", initData.getCountMines());
                }, (initData, modifiedProperties) -> {
                    Assert.assertTrue  (   modifiedProperties.containsKey(MosaicInitData.PROPERTY_MOSAIC_TYPE));
                    Assert.assertEquals(1, modifiedProperties.get(        MosaicInitData.PROPERTY_MOSAIC_TYPE).first.intValue());
                    Assert.assertTrue  (   modifiedProperties.containsKey(MosaicInitData.PROPERTY_MOSAIC_GROUP));
                    Assert.assertEquals(1, modifiedProperties.get(        MosaicInitData.PROPERTY_MOSAIC_GROUP).first.intValue());
                    Assert.assertTrue  (  !modifiedProperties.containsKey(MosaicInitData.PROPERTY_COUNT_MINES));
                    Assert.assertEquals(2, modifiedProperties.size());
                    Assert.assertEquals(EMosaic.eMosaicHexagon1, initData.getMosaicType());
                    Assert.assertEquals(initData.getSkillLevel().getNumberMines(EMosaic.eMosaicHexagon1), initData.getCountMines());
                });
    }

    @Test
    public void checkChangedMosaicGroupTest() {
        Logger.info("> MosaicInitDataTest::checkChangedMosaicGroupTest");
        new PropertyChangeExecutor<>(MosaicInitDataTest::createMosaicInitData).run(300, 5000,
            initData -> {
                initData.setMosaicType(EMosaic.eMosaicHexagon1);
            }, (initData, modifiedProperties) -> {
                Assert.assertTrue(modifiedProperties.containsKey(MosaicInitData.PROPERTY_MOSAIC_GROUP));
            });
    }

    @Test
    public void checkNoChangedMosaicGroupTest() {
        Logger.info("> MosaicInitDataTest::checkNoChangedMosaicGroupTest");
        new PropertyChangeExecutor<>(MosaicInitDataTest::createMosaicInitData).run(300, 5000,
            initData -> {
                initData.setMosaicType(EMosaic.eMosaicRhombus1);
            }, (initData, modifiedProperties) -> {
                Assert.assertFalse(modifiedProperties.containsKey(MosaicInitData.PROPERTY_MOSAIC_GROUP));
            });
    }

    @Test
    public void checkRestoreIndexInGroupTest() {
        Logger.info("> MosaicInitDataTest::checkRestoreIndexInGroupTest");

        new PropertyChangeExecutor<>(MosaicInitDataTest::createMosaicInitData).run(10, 1000,
            initData -> {
                final int checkOrdinal = 3;

                // 1. select another mosaic in current group
                List<EMosaic> mosaicsInOldGroup = initData.getMosaicGroup().getMosaics();
                initData.setMosaicType(mosaicsInOldGroup.get(checkOrdinal));

                // 2. change group
                initData.setMosaicGroup(EMosaicGroup.eTriangles);

                // 3. check ordinal in new group
                Assert.assertEquals(checkOrdinal, initData.getMosaicType().getOrdinalInGroup());
            }, (initData, modifiedProperties) -> {});
    }
}
