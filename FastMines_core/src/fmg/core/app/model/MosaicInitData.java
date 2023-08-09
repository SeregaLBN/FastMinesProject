package fmg.core.app.model;

import static fmg.core.img.PropertyConst.PROPERTY_COUNT_MINES;
import static fmg.core.img.PropertyConst.PROPERTY_MOSAIC_GROUP;
import static fmg.core.img.PropertyConst.PROPERTY_MOSAIC_TYPE;
import static fmg.core.img.PropertyConst.PROPERTY_SIZE_FIELD;
import static fmg.core.img.PropertyConst.PROPERTY_SKILL_LEVEL;

import java.util.Objects;
import java.util.function.Consumer;

import fmg.common.Logger;
import fmg.common.geom.Matrisize;
import fmg.core.mosaic.MosaicHelper;
import fmg.core.types.EMosaic;
import fmg.core.types.EMosaicGroup;
import fmg.core.types.ESkillLevel;

/** Mosaic model (data for save/load) */
public class MosaicInitData implements AutoCloseable {

    public static final double AREA_MINIMUM = 230;

    public static final int MIN_SIZE_FIELD_M = 3;
    public static final int MAX_SIZE_FIELD_M = 3000;
    public static final int MIN_SIZE_FIELD_N = 3;
    public static final int MAX_SIZE_FIELD_N = 3000;

    public static final EMosaic     DEFAULT_MOSAIC_TYPE  = EMosaic.eMosaicSquare1;
    public static final ESkillLevel DEFAULT_SKILL_LEVEL  = ESkillLevel.eBeginner;
    public static final int         DEFAULT_SIZE_FIELD_M = DEFAULT_SKILL_LEVEL.getDefaultSize().m;
    public static final int         DEFAULT_SIZE_FIELD_N = DEFAULT_SKILL_LEVEL.getDefaultSize().n;
    public static final int         DEFAULT_COUNT_MINES  = DEFAULT_SKILL_LEVEL.getNumberMines(DEFAULT_MOSAIC_TYPE);

    private EMosaic mosaicType;

    private Matrisize sizeField;

    private int countMines;

    private Consumer<String> changedCallback;

    public MosaicInitData() {
        mosaicType = DEFAULT_MOSAIC_TYPE;
        sizeField  = new Matrisize(DEFAULT_SIZE_FIELD_M, DEFAULT_SIZE_FIELD_N);
        countMines = DEFAULT_COUNT_MINES;
    }

    public EMosaic getMosaicType() { return mosaicType; }
    public void setMosaicType(EMosaic mosaicType) {
        if (mosaicType == null)
            throw new IllegalArgumentException("Mosaic type can not be null");
        if (this.mosaicType == mosaicType)
            return;

        var oldValue = this.mosaicType;
        this.mosaicType = mosaicType;

        var mosaicGroupChanged = (oldValue.getGroup() != mosaicType.getGroup());
        var skillChanged = false;
        var countMinesChanged = false;
        ESkillLevel skillOld = ESkillLevel.calcSkillLevel(oldValue, sizeField, countMines);
        if (skillOld != ESkillLevel.eCustom) {
            // restore mines count for new mosaic type
            var newCountMines = skillOld.getNumberMines(mosaicType);
            countMinesChanged = (countMines != newCountMines);
            if (countMinesChanged)
                countMines = newCountMines;
        }

        if (changedCallback == null)
            return;

        ESkillLevel skillNew = getSkillLevel();
        skillChanged = (skillNew != skillOld);

        changedCallback.accept(PROPERTY_MOSAIC_TYPE);
        if (mosaicGroupChanged)
            changedCallback.accept(PROPERTY_MOSAIC_GROUP);
        if (skillChanged)
            changedCallback.accept(PROPERTY_SKILL_LEVEL);
        if (countMinesChanged)
            changedCallback.accept(PROPERTY_COUNT_MINES);
    }

    public EMosaicGroup getMosaicGroup() { return mosaicType.getGroup(); }
    public void setMosaicGroup(EMosaicGroup mosaicGroup) {
        Objects.requireNonNull(mosaicGroup, "Mosaic group can not be null");
        if (mosaicType.getGroup() == mosaicGroup)
            return;

        int ordinalInOldGroup = mosaicType.getOrdinalInGroup();
        int ordinalInNewGroup = Math.min(ordinalInOldGroup, mosaicGroup.getMosaics().size() - 1);
        setMosaicType(mosaicGroup.getMosaics().get(ordinalInNewGroup));
    }

    public Matrisize getSizeField() { return sizeField; }
    public void setSizeField(Matrisize sizeField) {
        if (sizeField == null)
            throw new IllegalArgumentException("Size field can not be null");
        if (sizeField.m < MIN_SIZE_FIELD_M)
            throw new IllegalArgumentException("Size field M must be larger " + MIN_SIZE_FIELD_M);
        if (sizeField.n < MIN_SIZE_FIELD_N)
            throw new IllegalArgumentException("Size field N must be larger " + MIN_SIZE_FIELD_N);
        if (sizeField.m > MAX_SIZE_FIELD_M)
            throw new IllegalArgumentException("Size field M must be less "  + (MAX_SIZE_FIELD_M + 1));
        if (sizeField.n > MAX_SIZE_FIELD_N)
            throw new IllegalArgumentException("Size field N must be less "  + (MAX_SIZE_FIELD_N + 1));

        var newValue = new Matrisize(sizeField.m, sizeField.n);
        var oldValue = this.sizeField;
        if (oldValue.equals(newValue))
            return;

        this.sizeField = newValue;

        if (changedCallback == null)
            return;

        ESkillLevel skillOld = ESkillLevel.calcSkillLevel(mosaicType, oldValue, countMines);
        ESkillLevel skillNew = getSkillLevel();

        int maxMines = Math.max(1, sizeField.m * sizeField.n - (1 + MosaicHelper.getMaxNeighborNumber(mosaicType)));
        boolean countMinesChanged = (countMines > maxMines);
        if (countMinesChanged)
            countMines = maxMines;

        changedCallback.accept(PROPERTY_SIZE_FIELD);
        if (skillNew != skillOld)
            changedCallback.accept(PROPERTY_SKILL_LEVEL);
        if (countMinesChanged)
            changedCallback.accept(PROPERTY_COUNT_MINES);
    }

    public int getCountMines() { return countMines; }
    public void setCountMines(int countMines) {
        if (countMines < 1)
            throw new IllegalArgumentException("Mines count must be positive");

        int maxMines = MosaicHelper.getMaxNumberMines(sizeField, mosaicType);
        if (countMines > maxMines) {
            Logger.warn("Force set countMines to {0}. Input value is {1}", maxMines, countMines);
            countMines = maxMines;
        }

        if (this.countMines == countMines)
            return;

        var oldValue = this.countMines;
        this.countMines = countMines;

        if (changedCallback == null)
            return;

        changedCallback.accept(PROPERTY_COUNT_MINES);

        ESkillLevel skillOld = ESkillLevel.calcSkillLevel(mosaicType, sizeField, oldValue);
        ESkillLevel skillNew = getSkillLevel();
        if (skillNew != skillOld)
            changedCallback.accept(PROPERTY_SKILL_LEVEL);
    }

    public ESkillLevel getSkillLevel() {
        return ESkillLevel.calcSkillLevel(mosaicType, sizeField, countMines);
    }

    public void setSkillLevel(ESkillLevel skill) {
        Objects.requireNonNull(skill, "Mosaic skill level can not be null");

        ESkillLevel skillOld = getSkillLevel();
        if (skillOld == skill)
            return;

        countMines = skill.getNumberMines(mosaicType);
        sizeField  = skill.getDefaultSize();

        ESkillLevel skillNew = getSkillLevel();
        if (skill != skillNew) // recheck
            throw new IllegalArgumentException("skill != skillNew");

        if (changedCallback == null)
            return;

        changedCallback.accept(PROPERTY_SKILL_LEVEL);
        changedCallback.accept(PROPERTY_COUNT_MINES);
        changedCallback.accept(PROPERTY_SIZE_FIELD);
    }


    @Override
    public void close() {
        changedCallback = null;
    }

    public void setListener(Consumer<String> callback) {
        if ((callback != null) && (changedCallback != null))
            throw new IllegalArgumentException("Can set once");
        changedCallback = callback;
    }

}
