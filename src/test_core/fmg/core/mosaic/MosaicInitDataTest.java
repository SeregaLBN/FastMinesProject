package fmg.core.mosaic;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.*;

import fmg.common.LoggerSimple;
import fmg.common.notifier.PropertyChangeExecutor;
import fmg.core.types.EMosaic;
import fmg.core.types.EMosaicGroup;
import fmg.core.types.ESkillLevel;
import io.reactivex.Flowable;

public class MosaicInitDataTest {

    @BeforeClass
    public static void setup() {
        LoggerSimple.put(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        LoggerSimple.put("> MosaicInitDataTest::setup");

        MosaicModelTest.ProjSettings();

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

        assertEquals(MosaicInitData.DEFAULT_MOSAIC_TYPE.getGroup(), initData.getMosaicGroup());
        return initData;
    }

    @Test
    public void checkTheImpossibilitySetCustomSkillLevelTest() {
        LoggerSimple.put("> MosaicInitDataTest::checkTheImpossibilitySetCustomSkillLevelTest");
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
    public void checkIfMosaicTypeIsChangedThenMinesCountWillAlsoBeChangedTest() {
        LoggerSimple.put("> MosaicInitDataTest::checkIfMosaicTypeIsChangedThenMinesCountWillAlsoBeChangedTest");
        new PropertyChangeExecutor<>(MosaicInitDataTest::createMosaicInitData).run(300, 5000,
            initData -> {
                initData.setMosaicType(EMosaic.eMosaicRhombus1);
            }, (initData, modifiedProperties) -> {
                Assert.assertTrue  (   modifiedProperties.containsKey(MosaicInitData.PROPERTY_MOSAIC_TYPE));
                Assert.assertEquals(1, modifiedProperties.get(        MosaicInitData.PROPERTY_MOSAIC_TYPE).first.intValue());
                Assert.assertTrue  (   modifiedProperties.containsKey(MosaicInitData.PROPERTY_MINES_COUNT));
                Assert.assertEquals(1, modifiedProperties.get(        MosaicInitData.PROPERTY_MINES_COUNT).first.intValue());
                Assert.assertEquals(2, modifiedProperties.size());
                Assert.assertEquals(EMosaic.eMosaicRhombus1, initData.getMosaicType());
                Assert.assertEquals(initData.getSkillLevel().getNumberMines(EMosaic.eMosaicRhombus1), initData.getMinesCount());
            });
    }

    @Test
    public void checkNoRepeatNotificationsTest() {
        LoggerSimple.put("> MosaicInitDataTest::checkNoRepeatNotificationsTest");
            new PropertyChangeExecutor<>(MosaicInitDataTest::createMosaicInitData).run(300, 5000,
                initData -> {
                    LoggerSimple.put("    initData.minesCount={0}", initData.getMinesCount());
                    initData.setMosaicType(EMosaic.eMosaicRhombus1);
                    LoggerSimple.put("    initData.minesCount={0}", initData.getMinesCount());
                    initData.setMosaicType(EMosaic.eMosaicHexagon1);
                    LoggerSimple.put("    initData.minesCount={0}", initData.getMinesCount());
                }, (initData, modifiedProperties) -> {
                    Assert.assertTrue  (   modifiedProperties.containsKey(MosaicInitData.PROPERTY_MOSAIC_TYPE));
                    Assert.assertEquals(1, modifiedProperties.get(        MosaicInitData.PROPERTY_MOSAIC_TYPE).first.intValue());
                    Assert.assertTrue  (   modifiedProperties.containsKey(MosaicInitData.PROPERTY_MOSAIC_GROUP));
                    Assert.assertEquals(1, modifiedProperties.get(        MosaicInitData.PROPERTY_MOSAIC_GROUP).first.intValue());
                    Assert.assertTrue  (  !modifiedProperties.containsKey(MosaicInitData.PROPERTY_MINES_COUNT));
                    Assert.assertEquals(2, modifiedProperties.size());
                    Assert.assertEquals(EMosaic.eMosaicHexagon1, initData.getMosaicType());
                    Assert.assertEquals(initData.getSkillLevel().getNumberMines(EMosaic.eMosaicHexagon1), initData.getMinesCount());
                });
    }

    @Test
    public void checkChangedMosaicGroupTest() {
        LoggerSimple.put("> MosaicInitDataTest::checkChangedMosaicGroupTest");
        new PropertyChangeExecutor<>(MosaicInitDataTest::createMosaicInitData).run(300, 5000,
            initData -> {
                initData.setMosaicType(EMosaic.eMosaicHexagon1);
            }, (initData, modifiedProperties) -> {
                Assert.assertTrue(modifiedProperties.containsKey(MosaicInitData.PROPERTY_MOSAIC_GROUP));
            });
    }

    @Test
    public void checkNoChangedMosaicGroupTest() {
        LoggerSimple.put("> MosaicInitDataTest::checkNoChangedMosaicGroupTest");
        new PropertyChangeExecutor<>(MosaicInitDataTest::createMosaicInitData).run(300, 5000,
            initData -> {
                initData.setMosaicType(EMosaic.eMosaicRhombus1);
            }, (initData, modifiedProperties) -> {
                Assert.assertFalse(modifiedProperties.containsKey(MosaicInitData.PROPERTY_MOSAIC_GROUP));
            });
    }

    @Test
    public void checkRestoreIndexInGroupTest() {
        LoggerSimple.put("> MosaicInitDataTest::checkRestoreIndexInGroupTest");

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
