package fmg.core.mosaic;

import fmg.common.geom.Matrisize;
import fmg.common.geom.SizeDouble;
import fmg.core.types.EMosaic;
import fmg.core.types.ESkillLevel;

/** Mosaic data */
public class MosaicInitData {

    public static final double AREA_MINIMUM = 230;

    private Matrisize sizeField;
    private EMosaic mosaicType;
    private int minesCount;
    private SizeDouble size;

    public MosaicInitData() {
        mosaicType = EMosaic.eMosaicSquare1;
        sizeField = ESkillLevel.eBeginner.getDefaultSize();
        minesCount = ESkillLevel.eBeginner.getNumberMines(mosaicType);
        size = new SizeDouble(500, 500);
    }

    public Matrisize getSizeField() { return sizeField; }
    public void setSizeField(Matrisize sizeField) { this.sizeField = sizeField; }

    public EMosaic getMosaicType() { return mosaicType; }
    public void setMosaicType(EMosaic mosaicType) { this.mosaicType = mosaicType; }

    public int getMinesCount() { return minesCount; }
    public void setMinesCount(int minesCount) { this.minesCount = minesCount; }

    public SizeDouble getSize() { return size; }
    public void setSize(SizeDouble size) { this.size = size; }

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
        minesCount = skill.getNumberMines(mosaicType);
        sizeField  = skill.getDefaultSize();
    }

}