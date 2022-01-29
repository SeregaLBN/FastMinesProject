package fmg.core.app.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import fmg.common.geom.Matrisize;
import fmg.common.notifier.INotifyPropertyChanged;
import fmg.common.notifier.NotifyPropertyChanged;
import fmg.core.types.EMosaic;
import fmg.core.types.EMosaicGroup;
import fmg.core.types.ESkillLevel;
import fmg.core.types.Property;

/** Mosaic model (data for save/load) */
public class MosaicInitData implements INotifyPropertyChanged, AutoCloseable {

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

    @Property(PROPERTY_MOSAIC_TYPE)
    private EMosaic mosaicType;

    @Property(PROPERTY_SIZE_FIELD)
    private Matrisize sizeField;

    @Property(PROPERTY_COUNT_MINES)
    private int countMines;

    private boolean lockChanging = false;

    protected final NotifyPropertyChanged notifier/*Sync*/ = new NotifyPropertyChanged(this, false);
    private   final NotifyPropertyChanged notifierAsync    = new NotifyPropertyChanged(this, true);
    private final PropertyChangeListener onPropertyChangedListener = this::onPropertyChanged;

    public MosaicInitData() {
        mosaicType = DEFAULT_MOSAIC_TYPE;
        sizeField  = new Matrisize(DEFAULT_SIZE_FIELD_M, DEFAULT_SIZE_FIELD_N);
        countMines = DEFAULT_COUNT_MINES;
        notifier.addListener(onPropertyChangedListener);
    }

    public EMosaic getMosaicType() { return mosaicType; }
    public void setMosaicType(EMosaic mosaicType) {
        if (mosaicType == null)
            throw new IllegalArgumentException("Mosaic type can not be null");
        notifier.setProperty(this.mosaicType, mosaicType, PROPERTY_MOSAIC_TYPE);
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

        notifier.setProperty(this.sizeField, new Matrisize(sizeField.m, sizeField.n), PROPERTY_SIZE_FIELD);
    }

    public int getCountMines() { return countMines; }
    public void setCountMines(int countMines) {
        if (countMines <= 0)
            throw new IllegalArgumentException("Mines count must be positive");

        notifier.setProperty(this.countMines, countMines, PROPERTY_COUNT_MINES);
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
        notifier.firePropertyChanged(skillOld, skillNew, PROPERTY_SKILL_LEVEL);
    }

    protected void onPropertyChanged(PropertyChangeEvent ev) {
        // refire as async event
        notifierAsync.firePropertyChanged(ev.getOldValue(), ev.getNewValue(), ev.getPropertyName());

        if (lockChanging)
            return;
        lockChanging = true;
        try {
            switch(ev.getPropertyName()) {
            case PROPERTY_MOSAIC_TYPE:
                {
                    EMosaic old = (EMosaic)ev.getOldValue();
                    if (old.getGroup() != getMosaicType().getGroup())
                        notifier.firePropertyChanged(old.getGroup(), getMosaicType().getGroup(), PROPERTY_MOSAIC_GROUP);

                    ESkillLevel skillOld = ESkillLevel.calcSkillLevel(old, sizeField, countMines);
                    if (skillOld == ESkillLevel.eCustom) {
                        ESkillLevel skillNew = getSkillLevel();
                        if (skillNew != skillOld)
                            notifier.firePropertyChanged(skillOld, skillNew, PROPERTY_SKILL_LEVEL);
                    } else {
                        // restore mines count for new mosaic type
                        setCountMines(skillOld.getNumberMines(getMosaicType()));
                    }
                }
                break;
            case PROPERTY_SIZE_FIELD:
                {
                    ESkillLevel skillOld = ESkillLevel.calcSkillLevel(mosaicType, (Matrisize)ev.getOldValue(), countMines);
                    ESkillLevel skillNew = getSkillLevel();
                    if (skillNew != skillOld)
                        notifier.firePropertyChanged(skillOld, skillNew, PROPERTY_SKILL_LEVEL);
                }
                break;
            case PROPERTY_COUNT_MINES:
                {
                    ESkillLevel skillOld = ESkillLevel.calcSkillLevel(mosaicType, sizeField, (int)ev.getOldValue());
                    ESkillLevel skillNew = getSkillLevel();
                    if (skillNew != skillOld)
                        notifier.firePropertyChanged(skillOld, skillNew, PROPERTY_SKILL_LEVEL);
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
        notifier.removeListener(onPropertyChangedListener);
        notifier.close();
        notifierAsync.close();
    }

    @Override
    public void addListener(PropertyChangeListener listener) {
        notifierAsync.addListener(listener);
    }
    @Override
    public void removeListener(PropertyChangeListener listener) {
        notifierAsync.removeListener(listener);
    }

}
