package fmg.core.app.model;

import java.util.List;

import fmg.common.geom.Matrisize;
import fmg.core.mosaic.cells.BaseCell;
import fmg.core.types.EMosaic;

/** Mosaic controller backup data */
public class MosaicBackupData {

    public EMosaic mosaicType;

    public Matrisize sizeField;

    public List<BaseCell.StateCell> cellStates;

    public double area;

    public int clickCount;

}
