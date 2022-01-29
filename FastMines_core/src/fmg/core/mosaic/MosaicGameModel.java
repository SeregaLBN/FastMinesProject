////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
// file name: "MosaicGameModel.java"
//
// Data class: game model of mosaic field
// Author: 2011-2018  -  Serhii Kryvulia aka SeregaLBN
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
////////////////////////////////////////////////////////////////////////////////
package fmg.core.mosaic;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import fmg.common.Logger;
import fmg.common.geom.Coord;
import fmg.common.geom.Matrisize;
import fmg.common.notifier.INotifyPropertyChanged;
import fmg.common.notifier.NotifyPropertyChanged;
import fmg.core.app.model.MosaicInitData;
import fmg.core.mosaic.cells.BaseCell;
import fmg.core.types.EMosaic;
import fmg.core.types.Property;

/** MVC: game model of mosaic field. Default implementation. */
public class MosaicGameModel implements IMosaic, INotifyPropertyChanged, AutoCloseable {

    public static final String PROPERTY_AREA        = BaseCell.BaseAttribute.PROPERTY_AREA;
    public static final String PROPERTY_CELL_ATTR   = "CellAttr";
    public static final String PROPERTY_SIZE_FIELD  = "SizeField";
    public static final String PROPERTY_MATRIX      = "Matrix";
    public static final String PROPERTY_MOSAIC_TYPE = "MosaicType";

    @Property(PROPERTY_CELL_ATTR)
    private BaseCell.BaseAttribute cellAttr;

    /** Matrix of cells, is represented as a vector {@link List<BaseCell>}.
      * Матрица ячеек, представленная(развёрнута) в виде вектора */
    @Property(PROPERTY_MATRIX)
    private final List<BaseCell> matrix = new ArrayList<>();

    /** Field size in cells */
    @Property(PROPERTY_SIZE_FIELD)
    private Matrisize sizeField = new Matrisize(10, 10);

    /** из каких фигур состоит мозаика поля */
    @Property(PROPERTY_MOSAIC_TYPE)
    private EMosaic mosaicType = EMosaic.eMosaicSquare1;

    protected final NotifyPropertyChanged notifier/*Sync*/ = new NotifyPropertyChanged(this, false);
    private   final NotifyPropertyChanged notifierAsync    = new NotifyPropertyChanged(this, true);

    private final PropertyChangeListener              onPropertyChangedListener = this::onPropertyChanged;
    private final PropertyChangeListener onCellAttributePropertyChangedListener = this::onCellAttributePropertyChanged;

    public MosaicGameModel() {
        notifier.addListener(onPropertyChangedListener);
    }

    @Override
    public BaseCell.BaseAttribute getCellAttr() {
        if (cellAttr == null) {
            cellAttr = MosaicHelper.createAttributeInstance(getMosaicType());
            cellAttr.addListener(onCellAttributePropertyChangedListener);
        }
        return cellAttr;
    }
    private void resetCellAttr() {
        if (cellAttr == null)
            return;
        cellAttr.removeListener(onCellAttributePropertyChangedListener);
        cellAttr.close();
        cellAttr = null;
        matrix.clear();
        notifier.firePropertyChanged(PROPERTY_CELL_ATTR);
        notifier.firePropertyChanged(PROPERTY_MATRIX);
    }

    /** площадь ячеек */
    @Override
    public double getArea() {
        return getCellAttr().getArea();
    }
    /** установить новую площадь ячеек */
    @Override
    public void setArea(double area) {
        if (area <= 0)
            throw new IllegalArgumentException("Area must be positive");
        if (area < MosaicInitData.AREA_MINIMUM)
            Logger.warn("The area is very small = " + area);
      //getCellAttr().setArea(Math.max(MosaicInitData.AREA_MINIMUM, area));
        getCellAttr().setArea(area);
    }

    @Override
    public List<BaseCell> getMatrix() {
        if (matrix.isEmpty()) {
            BaseCell.BaseAttribute attr = getCellAttr();
            Matrisize size = getSizeField();
            EMosaic mosaicType = getMosaicType();
            //_matrix = new ArrayList<BaseCell>(size.width * size.height);
            for (int i=0; i < size.m; i++)
                for (int j=0; j < size.n; j++) {
                    BaseCell cell = MosaicHelper.createCellInstance(attr, mosaicType, new Coord(i, j));
                    matrix.add(/* i*_size.n + j, */ cell);
                }
        }
        return matrix;
    }

    /** get mosaic field size in cells */
    @Override
    public Matrisize getSizeField() { return sizeField; }
    /** set mosaic field size in cells */
    @Override
    public void setSizeField(Matrisize newSizeField) {
        Matrisize old = this.sizeField;
        if (old.equals(newSizeField))
            return;

        matrix.clear();
        this.sizeField = newSizeField;

        notifier.firePropertyChanged(old, newSizeField, PROPERTY_SIZE_FIELD);
        notifier.firePropertyChanged(PROPERTY_MATRIX);
    }

    /** get mosaic type
      * (из каких фигур состоит мозаика поля) */
    @Override
    public EMosaic getMosaicType() { return mosaicType; }
    /** set mosaic type */
    @Override
    public void setMosaicType(EMosaic newMosaicType) {
        EMosaic old = this.mosaicType;
        if (old == newMosaicType)
            return;

        this.mosaicType = newMosaicType;
        resetCellAttr();
        notifier.firePropertyChanged(old, newMosaicType, PROPERTY_MOSAIC_TYPE);
    }

    /** доступ к заданной ячейке */
    public BaseCell getCell(int x, int y) { return getMatrix().get(x*sizeField.n + y); }
    /** доступ к заданной ячейке */
    @Override
    public BaseCell getCell(Coord coord) { return getCell(coord.x, coord.y); }

    protected void onPropertyChanged(PropertyChangeEvent ev) {
        // refire as async event
        notifierAsync.firePropertyChanged(ev.getOldValue(), ev.getNewValue(), ev.getPropertyName());
    }

    protected void onCellAttributePropertyChanged(PropertyChangeEvent ev) {
        String propName = ev.getPropertyName();
        if (BaseCell.BaseAttribute.PROPERTY_AREA.equals(propName)) {
            getMatrix().forEach(BaseCell::init);

            notifier.firePropertyChanged(ev.getOldValue(), ev.getNewValue(), PROPERTY_AREA); // ! rethrow event - notify parent class
        }
        notifier.firePropertyChanged(PROPERTY_CELL_ATTR);
    }

    @Override
    public void close() {
        notifier.removeListener(onPropertyChangedListener);
        notifier.close();
        notifierAsync.close();
        resetCellAttr();
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
