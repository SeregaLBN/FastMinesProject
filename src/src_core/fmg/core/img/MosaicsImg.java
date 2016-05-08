package fmg.core.img;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import fmg.common.geom.Bound;
import fmg.common.geom.BoundDouble;
import fmg.common.geom.Coord;
import fmg.common.geom.DoubleExt;
import fmg.common.geom.Matrisize;
import fmg.common.geom.PointDouble;
import fmg.common.geom.RegionDouble;
import fmg.common.geom.Size;
import fmg.common.geom.SizeDouble;
import fmg.common.geom.util.FigureHelper;
import fmg.core.mosaic.IMosaic;
import fmg.core.mosaic.MosaicHelper;
import fmg.core.mosaic.cells.BaseCell;
import fmg.core.mosaic.cells.BaseCell.BaseAttribute;
import fmg.core.mosaic.draw.IPaintable;
import fmg.core.types.EMosaic;

/** картинка поля конкретной мозаики. Используется для меню, кнопок, etc... */
public abstract class MosaicsImg<TPaintable extends IPaintable, TImage extends Object> extends RotatedImg<EMosaic, TImage>
      implements IMosaic<TPaintable>
{
   public MosaicsImg(EMosaic mosaicType, Matrisize sizeField) {
      super(mosaicType);
      _sizeField = sizeField;
   }
   public MosaicsImg(EMosaic mosaicType, Matrisize sizeField, int widthAndHeight) {
      super(mosaicType, widthAndHeight);
      _sizeField = sizeField;
   }
   public MosaicsImg(EMosaic mosaicType, Matrisize sizeField, int widthAndHeight, int padding) {
      super(mosaicType, widthAndHeight, padding);
      _sizeField = sizeField;
   }
   public MosaicsImg(EMosaic mosaicType, Matrisize sizeField, Size sizeImage, Bound padding) {
      super(mosaicType, sizeImage, padding);
      _sizeField = sizeField;
   }

   /** из каких фигур состоит мозаика поля */
   @Override
   public EMosaic getMosaicType() { return getEntity(); }
   @Override
   public void setMosaicType(EMosaic value) {
      if (value != getEntity()) {
         EMosaic old = getEntity();
         setEntity(value);
         dependency_MosaicType_As_Entity(value, old);
      }
   }

   private Matrisize _sizeField;
   @Override
   public Matrisize getSizeField() { return _sizeField; }
   @Override
   public void setSizeField(Matrisize value) {
      if (setProperty(_sizeField, value, "SizeField")) {
         recalcArea();
         _matrix.clear();
         invalidate();
      }
   }

   @Override
   public BaseCell getCell(Coord coord) { return getMatrix().get(coord.x * getSizeField().n + coord.y); }

   private BaseCell.BaseAttribute _cellAttr;
   @Override
   public BaseCell.BaseAttribute getCellAttr() {
      if (_cellAttr == null)
         setCellAttr(MosaicHelper.createAttributeInstance(getMosaicType(), getArea()));
      return _cellAttr;
   }
   private void setCellAttr(BaseCell.BaseAttribute value) {
      if (setProperty(_cellAttr, value, "CellAttr")) {
         dependency_CellAttribute_Area();
         invalidate();
      }
   }

   /** caching rotated values */
   private final List<BaseCell> _matrixRotated = new ArrayList<BaseCell>();
   private final List<BaseCell> _matrix        = new ArrayList<BaseCell>();
   /** матрица ячеек, представленная(развёрнута) в виде вектора */
   @Override
   public List<BaseCell> getMatrix() {
      if (_matrix.isEmpty()) {
         _matrixRotated.clear();
         BaseAttribute attr = getCellAttr();
         EMosaic type = getMosaicType();
         Matrisize size = getSizeField();
         for (int i = 0; i < size.m; i++)
            for (int j = 0; j < size.n; j++)
               _matrix.add(MosaicHelper.createCellInstance(attr, type, new Coord(i, j)));
         onPropertyChanged("Matrix");
         invalidate();
      }
      return _matrix;
   }

   public List<BaseCell> getRotatedMatrix() {
      if (Math.abs(getRotateAngle()) < 0.1)
         return getMatrix();
      if (_matrixRotated.isEmpty()) {
         // create copy Matrix
         BaseCell.BaseAttribute attr = getCellAttr();
         EMosaic type = getMosaicType();
         Matrisize size = getSizeField();
         for (int i = 0; i < size.m; i++)
            for (int j = 0; j < size.n; j++)
               _matrixRotated.add(MosaicHelper.createCellInstance(attr, type, new Coord(i, j)));
      } else {
         // restore base coords
         for (BaseCell cell : _matrixRotated)
            cell.Init();
      }

      PointDouble center = new PointDouble(getWidth() / 2.0 - _paddingFull.left, getHeight() / 2.0 - _paddingFull.top);
      for (BaseCell cell : _matrixRotated) {
         RegionDouble reg = cell.getRegion();
         Stream<PointDouble> newReg = reg.getPoints()
               .stream()
               .map(p -> new PointDouble(p))
               .map(p -> {
                  p.x -= center.x;
                  p.y -= center.y;
                  return p;
               });
         newReg = FigureHelper
               .rotate(newReg, getRotateAngle())
               .map(p -> {
                  p.x += center.x;
                  p.y += center.y;
                  return p;
               });
         int[] i = { 0 };
         newReg.forEach(p -> reg.setPoint(i[0]++, (int) p.x, (int) p.y));
      }

      return _matrixRotated;
   }

   private void recalcArea() {
      int w = getWidth();
      int h = getHeight();
      Bound pad = getPadding();
      SizeDouble sizeImageIn = new SizeDouble(w - pad.getLeftAndRight(), h - pad.getTopAndBottom());
      SizeDouble sizeImageOut = new SizeDouble();
      double area = MosaicHelper.findAreaBySize(getMosaicType(), getSizeField(), sizeImageIn, sizeImageOut);
      setArea(area);
      assert (w >= (sizeImageOut.width + pad.getLeftAndRight()));
      assert (h >= (sizeImageOut.height + pad.getTopAndBottom()));
      BoundDouble paddingOut = new BoundDouble(
         (w - sizeImageOut.width)/2,
         (h - sizeImageOut.height)/2,
         (w - sizeImageOut.width)/2,
         (h - sizeImageOut.height)/2);
      assert DoubleExt.hasMinDiff(sizeImageOut.width + paddingOut.getLeftAndRight(), w);
      assert DoubleExt.hasMinDiff(sizeImageOut.height + paddingOut.getTopAndBottom(), h);

      setPaddingFull(paddingOut);
   }

   private double _area;
   @Override
   public double getArea() {
      if (_area <= 0)
         recalcArea();
      return _area;
   }
   @Override
   public void setArea(double value) {
      if (setProperty(_area, value, "Area")) {
         dependency_CellAttribute_Area();
         invalidate();
      }
   }

   private BoundDouble _paddingFull;
   public BoundDouble getPaddingFull() { return _paddingFull; }
   protected void setPaddingFull(BoundDouble value) {
      if (setProperty(_paddingFull, value, "PaddingFull")) {
         invalidate();
      }
   }

   @Override
   protected void onPropertyChanged(Object oldValue, Object newValue, String propertyName) {
      //LoggerSimple.Put("OnPropertyChanged: {0}: PropertyName={1}", Entity, ev.PropertyName);
      super.onPropertyChanged(oldValue, newValue, propertyName);
      switch (propertyName) {
      case "Entity":
         dependency_MosaicType_As_Entity((EMosaic) newValue, (EMosaic) oldValue);
         break;
      case "Size":
      case "Padding":
         recalcArea();
         break;
      }
   }

   ///////////// #region Dependencys
   void dependency_CellAttribute_Area() {
      if (_cellAttr == null)
         return;
      getCellAttr().setArea(getArea());
      if (!_matrix.isEmpty())
         for (BaseCell cell : getMatrix())
            cell.Init();
   }

   void dependency_MosaicType_As_Entity(EMosaic newValue, EMosaic oldValue) {
      setArea(0);
      _matrix.clear();
      _matrixRotated.clear();
      setCellAttr(null);
      onPropertyChanged(oldValue, newValue, "MosaicType");
   }
   ////////////// #endregion

}
