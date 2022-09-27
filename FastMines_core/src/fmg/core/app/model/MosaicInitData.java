package fmg.core.app.model;

import java.util.function.Consumer;

import fmg.common.geom.Matrisize;
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

    public static final String PROPERTY_MOSAIC_TYPE  = "MosaicType";
    public static final String PROPERTY_MOSAIC_GROUP = "MosaicGroup";
    public static final String PROPERTY_SIZE_FIELD   = "SizeField";
    public static final String PROPERTY_COUNT_MINES  = "CountMines";
    public static final String PROPERTY_SKILL_LEVEL  = "SkillLevel";

    private EMosaic mosaicType;

    private Matrisize sizeField;

    private int countMines;

    private boolean lockChanging = false;

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
        firePropertyChanged(oldValue, PROPERTY_MOSAIC_TYPE);
    }

    public EMosaicGroup getMosaicGroup() { return mosaicType.getGroup(); }
    public void setMosaicGroup(EMosaicGroup mosaicGroup) {
        if (mosaicGroup == null)
            throw new IllegalArgumentException("Mosaic group can not be null");
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
        firePropertyChanged(oldValue, PROPERTY_SIZE_FIELD);
    }

    public int getCountMines() { return countMines; }
    public void setCountMines(int countMines) {
        if (countMines <= 0)
            throw new IllegalArgumentException("Mines count must be positive");

        if (this.countMines == countMines)
            return;
        var oldValue = this.countMines;
        this.countMines = countMines;
        firePropertyChanged(oldValue, PROPERTY_COUNT_MINES);
    }

    public ESkillLevel getSkillLevel() {
        return ESkillLevel.calcSkillLevel(mosaicType, sizeField, countMines);
    }

    public void setSkillLevel(ESkillLevel skill) {
        if (skill == null)
            throw new IllegalArgumentException("Mosaic skill level can not be null");
        if (lockChanging)
            throw new UnsupportedOperationException("Illegal usage");

        ESkillLevel skillOld = getSkillLevel();
        if (skillOld == skill)
            return;

        lockChanging = true;
        try {
            setCountMines(skill.getNumberMines(getMosaicType()));
            setSizeField(skill.getDefaultSize());
        } finally {
            lockChanging = false;
        }

        ESkillLevel skillNew = getSkillLevel();
        assert (skill == skillNew);
        assert (skill != skillOld);
        firePropertyChanged(skillOld, PROPERTY_SKILL_LEVEL);
    }

    private <T> void firePropertyChanged(T oldValue, String propertyName) {
        if (lockChanging)
            return;

        onPropertyChanged(oldValue, propertyName);

        if (changedCallback != null)
            changedCallback.accept(propertyName);
    }

    protected <T> void onPropertyChanged(T oldValue, String propertyName) {
        if (lockChanging)
            return;

        lockChanging = true;
        try {
            switch(propertyName) {
            case PROPERTY_MOSAIC_TYPE:
                {
                    EMosaic old = (EMosaic)oldValue;
                    if (old.getGroup() != getMosaicType().getGroup())
                        firePropertyChanged(old.getGroup(), PROPERTY_MOSAIC_GROUP);

                    ESkillLevel skillOld = ESkillLevel.calcSkillLevel(old, sizeField, countMines);
                    if (skillOld == ESkillLevel.eCustom) {
                        ESkillLevel skillNew = getSkillLevel();
                        if (skillNew != skillOld)
                            firePropertyChanged(skillOld, PROPERTY_SKILL_LEVEL);
                    } else {
                        // restore mines count for new mosaic type
                        setCountMines(skillOld.getNumberMines(getMosaicType()));
                    }
                }
                break;
            case PROPERTY_SIZE_FIELD:
                {
                    ESkillLevel skillOld = ESkillLevel.calcSkillLevel(mosaicType, (Matrisize)oldValue, countMines);
                    ESkillLevel skillNew = getSkillLevel();
                    if (skillNew != skillOld)
                        firePropertyChanged(skillOld, PROPERTY_SKILL_LEVEL);
                }
                break;
            case PROPERTY_COUNT_MINES:
                {
                    ESkillLevel skillOld = ESkillLevel.calcSkillLevel(mosaicType, sizeField, (int)oldValue);
                    ESkillLevel skillNew = getSkillLevel();
                    if (skillNew != skillOld)
                        firePropertyChanged(skillOld, PROPERTY_SKILL_LEVEL);
                }
                break;
            default:
                // none
            }
        } finally {
            lockChanging = false;
        }
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
