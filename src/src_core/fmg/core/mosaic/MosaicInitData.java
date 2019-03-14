package fmg.core.mosaic;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import fmg.common.geom.Matrisize;
import fmg.common.geom.SizeDouble;
import fmg.common.notyfier.INotifyPropertyChanged;
import fmg.common.notyfier.NotifyPropertyChanged;
import fmg.core.mosaic.cells.BaseCell;
import fmg.core.types.EMosaic;
import fmg.core.types.ESkillLevel;

/** Mosaic data */
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
    public static final int         DEFAULT_MINES_COUNT  = DEFAULT_SKILL_LEVEL.getNumberMines(DEFAULT_MOSAIC_TYPE);

    private EMosaic mosaicType;
    private Matrisize sizeField;
    private int minesCount;

    private boolean lockChanging = false;

    protected final NotifyPropertyChanged _notifier/*Sync*/ = new NotifyPropertyChanged(this, false);
    private   final NotifyPropertyChanged _notifierAsync    = new NotifyPropertyChanged(this, true);

    public static final String PROPERTY_MOSAIC_TYPE = "MosaicType";
    public static final String PROPERTY_SIZE_FIELD  = "SizeField";
    public static final String PROPERTY_MINES_COUNT = "MinesCount";
    public static final String PROPERTY_SKILL_LEVEL = "SkillLevel";

    public MosaicInitData() {
        mosaicType = DEFAULT_MOSAIC_TYPE;
        sizeField  = new Matrisize(DEFAULT_SIZE_FIELD_M, DEFAULT_SIZE_FIELD_N);
        minesCount = DEFAULT_MINES_COUNT;
    }

    public void copyFrom(MosaicInitData from) {
        if (from == this)
            return;
        setMosaicType(from.getMosaicType());
        setSizeField( from.getSizeField());
        setMinesCount(from.getMinesCount());
    }

    public EMosaic getMosaicType() { return mosaicType; }
    public void setMosaicType(EMosaic mosaicType) {
        if (mosaicType == null)
            throw new IllegalArgumentException("Mosaic type can not be null");
        ESkillLevel skillOld = getSkillLevel();
        if (_notifier.setProperty(this.mosaicType, mosaicType, PROPERTY_MOSAIC_TYPE)) {
            if (skillOld == ESkillLevel.eCustom) {
                ESkillLevel skillNew = getSkillLevel();
                if (skillNew != skillOld)
                    _notifier.firePropertyChanged(skillOld, skillNew, PROPERTY_SKILL_LEVEL);
            } else {
                setSkillLevel(skillOld);
            }
        }
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

        ESkillLevel skillOld = getSkillLevel();
        if (_notifier.setProperty(this.sizeField, new Matrisize(sizeField.m, sizeField.n), PROPERTY_SIZE_FIELD)) {
            ESkillLevel skillNew = getSkillLevel();
            if (!lockChanging && (skillNew != skillOld))
                _notifier.firePropertyChanged(skillOld, skillNew, PROPERTY_SKILL_LEVEL);
        }
    }

    public int getMinesCount() { return minesCount; }
    public void setMinesCount(int minesCount) {
        if (minesCount <= 0)
            throw new IllegalArgumentException("Mines count must be positive");

        ESkillLevel skillOld = getSkillLevel();
        if (_notifier.setProperty(this.minesCount, minesCount, PROPERTY_MINES_COUNT)) {
            ESkillLevel skillNew = getSkillLevel();
            if (!lockChanging && (skillNew != skillOld))
                _notifier.firePropertyChanged(skillOld, skillNew, PROPERTY_SKILL_LEVEL);
        }
    }

    public ESkillLevel getSkillLevel() {
        if (sizeField.equals(ESkillLevel.eBeginner.getDefaultSize()) && (minesCount == ESkillLevel.eBeginner.getNumberMines(mosaicType)))
            return ESkillLevel.eBeginner;
        if (sizeField.equals(ESkillLevel.eAmateur.getDefaultSize()) && (minesCount == ESkillLevel.eAmateur.getNumberMines(mosaicType)))
            return ESkillLevel.eAmateur;
        if (sizeField.equals(ESkillLevel.eProfi.getDefaultSize()) && (minesCount == ESkillLevel.eProfi.getNumberMines(mosaicType)))
            return ESkillLevel.eProfi;
        if (sizeField.equals(ESkillLevel.eCrazy.getDefaultSize()) && (minesCount == ESkillLevel.eCrazy.getNumberMines(mosaicType)))
            return ESkillLevel.eCrazy;
        return ESkillLevel.eCustom;
    }

    public void setSkillLevel(ESkillLevel skill) {
        if (mosaicType == null)
            throw new IllegalArgumentException("Mosaic skill level can not be null");

        ESkillLevel skillOld = getSkillLevel();
        if (skillOld == skill)
            return;

        lockChanging = true;
        {
            setMinesCount(skill.getNumberMines(getMosaicType()));
            setSizeField(skill.getDefaultSize());
        }
        lockChanging = false;

        ESkillLevel skillNew = getSkillLevel();
        assert (skill == skillNew);
        assert (skill != skillOld);
        _notifier.firePropertyChanged(skillOld, skillNew, PROPERTY_SKILL_LEVEL);
    }

    protected void onPropertyChanged(PropertyChangeEvent ev) {
        // refire as async event
        _notifierAsync.firePropertyChanged(ev.getOldValue(), ev.getNewValue(), ev.getPropertyName());
    }

    @Override
    public void close() {
        _notifier.removeListener(this::onPropertyChanged);
        _notifier.close();
        _notifierAsync.close();
    }

    @Override
    public void addListener(PropertyChangeListener listener) {
        _notifierAsync.addListener(listener);
    }
    @Override
    public void removeListener(PropertyChangeListener listener) {
        _notifierAsync.removeListener(listener);
    }

}
