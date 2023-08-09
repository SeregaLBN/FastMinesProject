package fmg.core.mosaic;

import static fmg.core.img.PropertyConst.PROPERTY_COUNT_MINES;
import static fmg.core.img.PropertyConst.PROPERTY_MOSAIC_GROUP;
import static fmg.core.img.PropertyConst.PROPERTY_MOSAIC_TYPE;
import static fmg.core.img.PropertyConst.PROPERTY_SIZE_FIELD;
import static fmg.core.img.PropertyConst.PROPERTY_SKILL_LEVEL;
import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.List;

import org.junit.*;

import fmg.common.Logger;
import fmg.common.geom.Matrisize;
import fmg.core.app.model.MosaicInitData;
import fmg.core.types.EMosaic;
import fmg.core.types.EMosaicGroup;
import fmg.core.types.ESkillLevel;

public class MosaicInitDataTest {

    @BeforeClass
    public static void setup() {
        Logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        Logger.info("> MosaicInitDataTest::setup");
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
            var initData = createMosaicInitData();
            initData.setSkillLevel(ESkillLevel.eCustom);
            Assert.fail();
        } catch (Exception ex) {
            assertEquals(IllegalArgumentException.class, ex.getClass());
        }
    }

    @Test
    public void checkIfMosaicTypeIsChangedThenCountMinesWillAlsoBeChangedTest() {
        Logger.info("> MosaicInitDataTest::checkIfMosaicTypeIsChangedThenCountMinesWillAlsoBeChangedTest");
        var initData = createMosaicInitData();

        var modifiedProperties = new HashMap<String, Integer>();
        initData.setListener(name -> modifiedProperties.compute(name, (k,v) -> v==null ? 1 : ++v));

        initData.setMosaicType(EMosaic.eMosaicRhombus1);

        Assert.assertTrue  (   modifiedProperties.containsKey(PROPERTY_MOSAIC_TYPE));
        Assert.assertEquals(1, modifiedProperties.get(        PROPERTY_MOSAIC_TYPE).intValue());
        Assert.assertTrue  (   modifiedProperties.containsKey(PROPERTY_COUNT_MINES));
        Assert.assertEquals(1, modifiedProperties.get(        PROPERTY_COUNT_MINES).intValue());
        Assert.assertEquals(2, modifiedProperties.size());
        Assert.assertEquals(EMosaic.eMosaicRhombus1, initData.getMosaicType());
        Assert.assertEquals(initData.getSkillLevel().getNumberMines(EMosaic.eMosaicRhombus1), initData.getCountMines());
    }

    @Test
    public void checkChangedMosaicGroupTest() {
        Logger.info("> MosaicInitDataTest::checkChangedMosaicGroupTest");
        var initData = createMosaicInitData();

        var modifiedProperties = new HashMap<String, Integer>();
        initData.setListener(name -> modifiedProperties.compute(name, (k,v) -> v==null ? 1 : ++v));

        initData.setMosaicType(EMosaic.eMosaicHexagon1);
        Assert.assertTrue(modifiedProperties.containsKey(PROPERTY_MOSAIC_GROUP));
    }

    @Test
    public void checkNoChangedMosaicGroupTest() {
        Logger.info("> MosaicInitDataTest::checkNoChangedMosaicGroupTest");
        var initData = createMosaicInitData();

        var modifiedProperties = new HashMap<String, Integer>();
        initData.setListener(name -> modifiedProperties.compute(name, (k,v) -> v==null ? 1 : ++v));

        initData.setMosaicType(EMosaic.eMosaicRhombus1);
        Assert.assertFalse(modifiedProperties.containsKey(PROPERTY_MOSAIC_GROUP));
    }

    @Test
    public void checkRestoreIndexInGroupTest() {
        Logger.info("> MosaicInitDataTest::checkRestoreIndexInGroupTest");

        var initData = createMosaicInitData();

        final int checkOrdinal = 3;

        // 1. select another mosaic in current group
        List<EMosaic> mosaicsInOldGroup = initData.getMosaicGroup().getMosaics();
        initData.setMosaicType(mosaicsInOldGroup.get(checkOrdinal));

        // 2. change group
        initData.setMosaicGroup(EMosaicGroup.eTriangles);

        // 3. check ordinal in new group
        Assert.assertEquals(checkOrdinal, initData.getMosaicType().getOrdinalInGroup());
    }

    @Test
    public void changeMosaicTypeTest() {
        Logger.info("> MosaicInitDataTest::changeMosaicTypeTest");
        var initData = createMosaicInitData();

        var modifiedProperties = new HashMap<String, Integer>();
        initData.setListener(name -> modifiedProperties.compute(name, (k,v) -> v==null ? 1 : ++v));

        initData.setMosaicType(EMosaic.eMosaicSquare2);
        Assert.assertEquals(EMosaic.eMosaicSquare2, initData.getMosaicType());
        Assert.assertTrue  (   modifiedProperties.containsKey(PROPERTY_MOSAIC_TYPE));
        Assert.assertEquals(1, modifiedProperties.get(        PROPERTY_MOSAIC_TYPE).intValue());
        Assert.assertEquals(1, modifiedProperties.size());

        modifiedProperties.clear();
        initData.setMosaicType(EMosaic.eMosaicRhombus1);
        Assert.assertEquals(EMosaic.eMosaicRhombus1, initData.getMosaicType());
        Assert.assertTrue  (   modifiedProperties.containsKey(PROPERTY_MOSAIC_TYPE));
        Assert.assertEquals(1, modifiedProperties.get(        PROPERTY_MOSAIC_TYPE).intValue());
        Assert.assertTrue  (   modifiedProperties.containsKey(PROPERTY_COUNT_MINES));
        Assert.assertEquals(1, modifiedProperties.get(        PROPERTY_COUNT_MINES).intValue());
        Assert.assertEquals(2, modifiedProperties.size());
        Assert.assertEquals(ESkillLevel.eBeginner.getNumberMines(EMosaic.eMosaicRhombus1), initData.getCountMines());

        modifiedProperties.clear();
        initData.setMosaicType(EMosaic.eMosaicTriangle1);
        Assert.assertEquals(EMosaic.eMosaicTriangle1, initData.getMosaicType());
        Assert.assertTrue  (   modifiedProperties.containsKey(PROPERTY_MOSAIC_TYPE));
        Assert.assertEquals(1, modifiedProperties.get(        PROPERTY_MOSAIC_TYPE).intValue());
        Assert.assertTrue  (   modifiedProperties.containsKey(PROPERTY_COUNT_MINES));
        Assert.assertEquals(1, modifiedProperties.get(        PROPERTY_COUNT_MINES).intValue());
        Assert.assertTrue  (   modifiedProperties.containsKey(PROPERTY_MOSAIC_GROUP));
        Assert.assertEquals(1, modifiedProperties.get(        PROPERTY_MOSAIC_GROUP).intValue());
        Assert.assertEquals(3, modifiedProperties.size());
        Assert.assertEquals(EMosaicGroup.eTriangles, initData.getMosaicGroup());
        Assert.assertEquals(ESkillLevel.eBeginner.getNumberMines(EMosaic.eMosaicTriangle1), initData.getCountMines());
    }

    @Test
    public void changeMosaicGroupTest() {
        Logger.info("> MosaicInitDataTest::changeMosaicGroupTest");
        var initData = createMosaicInitData();

        var modifiedProperties = new HashMap<String, Integer>();
        initData.setListener(name -> modifiedProperties.compute(name, (k,v) -> v==null ? 1 : ++v));

        initData.setMosaicGroup(EMosaicGroup.eQuadrangles);
        Assert.assertEquals(0, modifiedProperties.size());

        initData.setMosaicGroup(EMosaicGroup.eTriangles);
        Assert.assertEquals(EMosaicGroup.eTriangles, initData.getMosaicGroup());
        Assert.assertTrue  (   modifiedProperties.containsKey(PROPERTY_MOSAIC_GROUP));
        Assert.assertEquals(1, modifiedProperties.get(        PROPERTY_MOSAIC_GROUP).intValue());
        Assert.assertTrue  (   modifiedProperties.containsKey(PROPERTY_MOSAIC_TYPE));
        Assert.assertEquals(1, modifiedProperties.get(        PROPERTY_MOSAIC_TYPE).intValue());
        Assert.assertEquals(2, modifiedProperties.size());
        Assert.assertEquals(EMosaic.eMosaicTriangle1, initData.getMosaicType());
        Assert.assertEquals(ESkillLevel.eBeginner, initData.getSkillLevel());

        ///////////////////////
        initData.setSkillLevel(ESkillLevel.eCrazy);
        initData.setMosaicType(EMosaic.eMosaicSquare2);
        modifiedProperties.clear();

        initData.setMosaicGroup(EMosaicGroup.ePentagons);
        Assert.assertEquals(EMosaicGroup.ePentagons, initData.getMosaicGroup());
        Assert.assertTrue  (   modifiedProperties.containsKey(PROPERTY_MOSAIC_GROUP));
        Assert.assertEquals(1, modifiedProperties.get(        PROPERTY_MOSAIC_GROUP).intValue());
        Assert.assertTrue  (   modifiedProperties.containsKey(PROPERTY_MOSAIC_TYPE));
        Assert.assertEquals(1, modifiedProperties.get(        PROPERTY_MOSAIC_TYPE).intValue());
        Assert.assertTrue  (   modifiedProperties.containsKey(PROPERTY_COUNT_MINES));
        Assert.assertEquals(1, modifiedProperties.get(        PROPERTY_COUNT_MINES).intValue());
        Assert.assertEquals(3, modifiedProperties.size());
        Assert.assertEquals(EMosaic.eMosaicPentagonT5, initData.getMosaicType());
        Assert.assertEquals(ESkillLevel.eCrazy.getNumberMines(EMosaic.eMosaicPentagonT5), initData.getCountMines());
        Assert.assertEquals(ESkillLevel.eCrazy, initData.getSkillLevel());
    }

    @Test
    public void changeSizeFieldTest() {
        Logger.info("> MosaicInitDataTest::changeSizeFieldTest");
        var initData = createMosaicInitData();

        var modifiedProperties = new HashMap<String, Integer>();
        initData.setListener(name -> modifiedProperties.compute(name, (k,v) -> v==null ? 1 : ++v));

        initData.setSizeField(new Matrisize(3, 4));
        Assert.assertEquals(new Matrisize(3, 4), initData.getSizeField());
        Assert.assertTrue  (   modifiedProperties.containsKey(PROPERTY_COUNT_MINES));
        Assert.assertEquals(1, modifiedProperties.get(        PROPERTY_COUNT_MINES).intValue());
        Assert.assertTrue  (   modifiedProperties.containsKey(PROPERTY_SIZE_FIELD));
        Assert.assertEquals(1, modifiedProperties.get(        PROPERTY_SIZE_FIELD).intValue());
        Assert.assertTrue  (   modifiedProperties.containsKey(PROPERTY_SKILL_LEVEL));
        Assert.assertEquals(1, modifiedProperties.get(        PROPERTY_SKILL_LEVEL).intValue());
        Assert.assertEquals(3, modifiedProperties.size());
        Assert.assertEquals(ESkillLevel.eCustom, initData.getSkillLevel());
        Assert.assertEquals(3, initData.getCountMines());

        ///////////////////////
        initData.setSkillLevel(ESkillLevel.eBeginner);
        modifiedProperties.clear();

        initData.setSizeField(ESkillLevel.eCrazy.getDefaultSize());
        Assert.assertEquals(new Matrisize(45, 25), initData.getSizeField());
        Assert.assertTrue  (   modifiedProperties.containsKey(PROPERTY_SIZE_FIELD));
        Assert.assertEquals(1, modifiedProperties.get(        PROPERTY_SIZE_FIELD).intValue());
        Assert.assertTrue  (   modifiedProperties.containsKey(PROPERTY_SKILL_LEVEL));
        Assert.assertEquals(1, modifiedProperties.get(        PROPERTY_SKILL_LEVEL).intValue());
        Assert.assertEquals(2, modifiedProperties.size());
        Assert.assertEquals(ESkillLevel.eCustom, initData.getSkillLevel());
        Assert.assertEquals(ESkillLevel.eBeginner.getNumberMines(initData.getMosaicType()), initData.getCountMines());
    }

    @Test
    public void changeCountMinesTest() {
        Logger.info("> MosaicInitDataTest::changeCountMinesTest");
        var initData = createMosaicInitData();

        try {
            initData.setCountMines(0);
            Assert.fail();
        } catch (IllegalArgumentException ex) {
            // ok
        } catch (Exception ex) {
            Assert.fail();
        }

        var modifiedProperties = new HashMap<String, Integer>();
        initData.setListener(name -> modifiedProperties.compute(name, (k,v) -> v==null ? 1 : ++v));

        initData.setCountMines(1);
        Assert.assertEquals(1, initData.getCountMines());
        Assert.assertTrue  (   modifiedProperties.containsKey(PROPERTY_COUNT_MINES));
        Assert.assertEquals(1, modifiedProperties.get(        PROPERTY_COUNT_MINES).intValue());
        Assert.assertTrue  (   modifiedProperties.containsKey(PROPERTY_SKILL_LEVEL));
        Assert.assertEquals(1, modifiedProperties.get(        PROPERTY_SKILL_LEVEL).intValue());
        Assert.assertEquals(2, modifiedProperties.size());
        Assert.assertEquals(ESkillLevel.eCustom, initData.getSkillLevel());

        ///////////////////////
        initData.setSizeField(ESkillLevel.eProfi.getDefaultSize());
        modifiedProperties.clear();

        initData.setCountMines(ESkillLevel.eProfi.getNumberMines(initData.getMosaicType()));
        Assert.assertEquals(ESkillLevel.eProfi.getNumberMines(initData.getMosaicType()), initData.getCountMines());
        Assert.assertTrue  (   modifiedProperties.containsKey(PROPERTY_COUNT_MINES));
        Assert.assertEquals(1, modifiedProperties.get(        PROPERTY_COUNT_MINES).intValue());
        Assert.assertTrue  (   modifiedProperties.containsKey(PROPERTY_SKILL_LEVEL));
        Assert.assertEquals(1, modifiedProperties.get(        PROPERTY_SKILL_LEVEL).intValue());
        Assert.assertEquals(2, modifiedProperties.size());
        Assert.assertEquals(ESkillLevel.eProfi, initData.getSkillLevel());

        ///////////////////////
        modifiedProperties.clear();

        initData.setCountMines(9999999);
        int maxMines = MosaicHelper.getMaxNumberMines(initData.getSizeField(), initData.getMosaicType());
        Assert.assertEquals(maxMines, initData.getCountMines());
        Assert.assertTrue  (   modifiedProperties.containsKey(PROPERTY_COUNT_MINES));
        Assert.assertEquals(1, modifiedProperties.get(        PROPERTY_COUNT_MINES).intValue());
        Assert.assertTrue  (   modifiedProperties.containsKey(PROPERTY_SKILL_LEVEL));
        Assert.assertEquals(1, modifiedProperties.get(        PROPERTY_SKILL_LEVEL).intValue());
        Assert.assertEquals(2, modifiedProperties.size());
        Assert.assertEquals(ESkillLevel.eCustom, initData.getSkillLevel());
    }

    @Test
    public void changeSkillLevelTest() {
        Logger.info("> MosaicInitDataTest::changeSkillLevelTest");
        var initData = createMosaicInitData();

        var modifiedProperties = new HashMap<String, Integer>();
        initData.setListener(name -> modifiedProperties.compute(name, (k,v) -> v==null ? 1 : ++v));

        initData.setSkillLevel(ESkillLevel.eAmateur);
        Assert.assertEquals(ESkillLevel.eAmateur, initData.getSkillLevel());
        Assert.assertTrue  (   modifiedProperties.containsKey(PROPERTY_COUNT_MINES));
        Assert.assertEquals(1, modifiedProperties.get(        PROPERTY_COUNT_MINES).intValue());
        Assert.assertTrue  (   modifiedProperties.containsKey(PROPERTY_SKILL_LEVEL));
        Assert.assertEquals(1, modifiedProperties.get(        PROPERTY_SKILL_LEVEL).intValue());
        Assert.assertTrue  (   modifiedProperties.containsKey(PROPERTY_SIZE_FIELD));
        Assert.assertEquals(1, modifiedProperties.get(        PROPERTY_SIZE_FIELD).intValue());
        Assert.assertEquals(3, modifiedProperties.size());
        Assert.assertEquals(ESkillLevel.eAmateur.getNumberMines(initData.getMosaicType()), initData.getCountMines());
        Assert.assertEquals(ESkillLevel.eAmateur.getDefaultSize(), initData.getSizeField());

        try {
            initData.setSkillLevel(ESkillLevel.eCustom);
            Assert.fail();
        } catch (IllegalArgumentException ex) {
            // ok
        } catch (Exception ex) {
            Assert.fail();
        }

    }

}
