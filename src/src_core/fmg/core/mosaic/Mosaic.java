////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                      © Sergey Krivulya (KSerg, aka SeregaLBN)
// file name: "Mosaic.java"
//
// реализация алгоритма Мозаики состоящей из ячеек
// Copyright (C) 2011 Sergey Krivulya
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

import fmg.common.geom.Coord;
import fmg.common.geom.Matrisize;
import fmg.common.notyfier.NotifyPropertyChanged;
import fmg.core.mosaic.cells.BaseCell;
import fmg.core.types.EMosaic;

/** MVC: model (mosaic field). Default implementation. */
public class Mosaic
       extends NotifyPropertyChanged
       implements IMosaic, PropertyChangeListener
{
   private BaseCell.BaseAttribute _cellAttr;
   /** Matrix of cells, is represented as a vector {@link List<BaseCell>}.
     * Матрица ячеек, представленная(развёрнута) в виде вектора */
   private final List<BaseCell> _matrix = new ArrayList<BaseCell>();
   /** Field size in cells */
   protected Matrisize _size = new Matrisize(10, 10);
   /** из каких фигур состоит мозаика поля */
   protected EMosaic _mosaicType = EMosaic.eMosaicSquare1;


   public static final String PROPERTY_AREA        = BaseCell.BaseAttribute.PROPERTY_AREA;
   public static final String PROPERTY_CELL_ATTR   = "CellAttr";
   public static final String PROPERTY_SIZE_FIELD  = "SizeField";
   public static final String PROPERTY_MATRIX      = "Matrix";
   public static final String PROPERTY_MOSAIC_TYPE = "MosaicType";

   private void setCellAttr(BaseCell.BaseAttribute newValue) {
      if (_cellAttr == null)
         return;
      if (newValue != null)
         throw new IllegalArgumentException("Bad argument - support only null value!");
      _cellAttr.removeListener(this);
      _cellAttr = null;
      _matrix.clear();
      onSelfPropertyChanged(PROPERTY_CELL_ATTR);
      onSelfPropertyChanged(PROPERTY_MATRIX);
   }
   @Override
   public BaseCell.BaseAttribute getCellAttr() {
      if (_cellAttr == null) {
         _cellAttr = MosaicHelper.createAttributeInstance(getMosaicType());
         _cellAttr.setArea(500);
         _cellAttr.addListener(this);
      }
      return _cellAttr;
   }

   /** площадь ячеек */
   @Override
   public double getArea() {
      return getCellAttr().getArea();
   }
   /** установить новую площадь ячеек */
   @Override
   public void setArea(double newArea)  {
      assert (newArea >= 1);
      getCellAttr().setArea(newArea);
   }

   @Override
   public List<BaseCell> getMatrix() {
      if (_matrix.isEmpty()) {
         BaseCell.BaseAttribute attr = getCellAttr();
         Matrisize size = getSizeField();
         EMosaic mosaicType = getMosaicType();
         //_matrix = new ArrayList<BaseCell>(size.width * size.height);
         for (int i=0; i < size.m; i++)
            for (int j=0; j < size.n; j++) {
               BaseCell cell = MosaicHelper.createCellInstance(attr, mosaicType, new Coord(i, j));
               _matrix.add(/* i*_size.n + j, */ cell);
            }
      }
      return _matrix;
   }

   /** размер поля в ячейках */
   @Override
   public Matrisize getSizeField() { return _size; }
   /** размер поля в ячейках */
   @Override
   public void setSizeField(Matrisize newSizeField) {
      Matrisize old = this._size;
      if (old.equals(newSizeField))
         return;

      _matrix.clear();
      this._size = newSizeField;

      onSelfPropertyChanged(old, newSizeField, PROPERTY_SIZE_FIELD);
      onSelfPropertyChanged(PROPERTY_MATRIX);
   }

   /** узнать тип мозаики
     * (из каких фигур состоит мозаика поля) */
   @Override
   public EMosaic getMosaicType() { return _mosaicType; }
   /** установить тип мозаики */
   @Override
   public void setMosaicType(EMosaic newMosaicType) {
      EMosaic old = this._mosaicType;
      if (old == newMosaicType)
         return;

      double saveArea = getArea(); // save

      this._mosaicType = newMosaicType;
      setCellAttr(null);

      onSelfPropertyChanged(old, newMosaicType, PROPERTY_MOSAIC_TYPE);

      setArea(saveArea); // restore
   }

   /** доступ к заданной ячейке */
   public BaseCell getCell(int x, int y) { return getMatrix().get(x*_size.n + y); }
   /** доступ к заданной ячейке */
   @Override
   public BaseCell getCell(Coord coord) { return getCell(coord.x, coord.y); }

   @Override
   public void propertyChange(PropertyChangeEvent ev) {
      if (ev.getSource() instanceof BaseCell.BaseAttribute)
         onCellAttributePropertyChanged((BaseCell.BaseAttribute)ev.getSource(), ev);
   }

   protected void onCellAttributePropertyChanged(BaseCell.BaseAttribute source, PropertyChangeEvent ev) {
      String propName = ev.getPropertyName();
      if (BaseCell.BaseAttribute.PROPERTY_AREA.equals(propName)) {
         getMatrix().forEach(cell -> cell.Init());

         onSelfPropertyChanged(ev.getOldValue(), ev.getNewValue(), PROPERTY_AREA); // ! rethrow event - notify parent class
      }
      onSelfPropertyChanged(PROPERTY_CELL_ATTR);
      onSelfPropertyChanged(PROPERTY_CELL_ATTR + "." + propName);
   }

   @Override
   public void close() {
      setCellAttr(null);
      super.close();
   }

}
