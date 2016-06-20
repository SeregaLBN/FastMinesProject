package fmg.core.img;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import fmg.common.geom.Bound;
import fmg.common.geom.BoundDouble;
import fmg.common.geom.Coord;
import fmg.common.geom.DoubleExt;
import fmg.common.geom.Matrisize;
import fmg.common.geom.PointDouble;
import fmg.common.geom.Size;
import fmg.common.geom.SizeDouble;
import fmg.common.geom.util.FigureHelper;
import fmg.core.mosaic.IMosaic;
import fmg.core.mosaic.MosaicHelper;
import fmg.core.mosaic.cells.BaseCell;
import fmg.core.mosaic.cells.BaseCell.BaseAttribute;
import fmg.core.mosaic.draw.IPaintable;
import fmg.core.mosaic.draw.PaintContext;
import fmg.core.types.EMosaic;

/**
 * Representable {@link fmg.core.types.EMosaic} as image
 *
 * @param <TPaintable> see {@link IPaintable}
 * @param <TImage> plaform specific image
 * @param <TPaintContext> see {@link PaintContext}
 */
public abstract class MosaicsImg<TPaintable extends IPaintable, TImage, TPaintContext extends PaintContext<TImage>>
      extends RotatedImg<EMosaic, TImage>
      implements IMosaic<TPaintable, TImage, TPaintContext>
{
   public enum ERotateMode {
      fullMatrix,
      someCells
   }

   protected MosaicsImg(EMosaic mosaicType, Matrisize sizeField) {
      super(mosaicType);
      _sizeField = sizeField;
   }
   protected MosaicsImg(EMosaic mosaicType, Matrisize sizeField, int widthAndHeight) {
      super(mosaicType, widthAndHeight);
      _sizeField = sizeField;
   }
   protected MosaicsImg(EMosaic mosaicType, Matrisize sizeField, int widthAndHeight, int padding) {
      super(mosaicType, widthAndHeight, padding);
      _sizeField = sizeField;
   }
   protected MosaicsImg(EMosaic mosaicType, Matrisize sizeField, Size sizeImage, Bound padding) {
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

   private final List<BaseCell> _matrix = new ArrayList<BaseCell>();
   /** матрица ячеек, представленная(развёрнута) в виде вектора */
   @Override
   public List<BaseCell> getMatrix() {
      if (_matrix.isEmpty()) {
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

   private ERotateMode _rotateMode = ERotateMode.fullMatrix;
   public ERotateMode getRotateMode() { return _rotateMode; }
   public void setRotateMode(ERotateMode value) { setProperty(_rotateMode, value, "RotateMode"); }

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
      case "Rotate":
      case "RotateMode":
      case "SizeField":
         if (getRotateMode() == ERotateMode.someCells)
            randomRotateElemenIndex();
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
      setCellAttr(null);
      onPropertyChanged(oldValue, newValue, "MosaicType");
   }
   ////////////// #endregion

   @Override
   protected void onTimer() {
      ERotateMode rotateMode = getRotateMode();

      Double anglePrevis = (rotateMode == ERotateMode.someCells) ? getRotateAngle() : null;
      super.onTimer();

      switch (rotateMode) {
      case fullMatrix:
         rotatedMatrix();
         break;
      case someCells:
         updateAnglesOffsets(anglePrevis);
         rotatedCells();
         break;
      }
   }

   /** ///////////// ================= PART {@link ERotateMode#fullMatrix} ======================= ///////////// */

   public void rotatedMatrix() {
      PointDouble center = new PointDouble(getWidth() / 2.0 - _paddingFull.left, getHeight() / 2.0 - _paddingFull.top);
      for (BaseCell cell : getMatrix()) {
         cell.Init(); // restore base coords

         FigureHelper.rotate(cell.getRegion().getPoints(), getRotateAngle(), center, new PointDouble());
      }
   }

   /** ///////////// ================= PART {@link ERotateMode#someCells} ======================= ///////////// */

   protected static final class RotatedCellContext {
      public RotatedCellContext(int index, double angleOffset, double area) {
         this.index = index;
         this.angleOffset = angleOffset;
         this.area = area;
      }
      public final int index;
      public final double angleOffset;
      public double area;
   }

   /** rotate BaseCell from original Matrix with modified Region */
   protected void rotatedCells() {
      BaseAttribute attr = getCellAttr();
      List<BaseCell> matrix = getMatrix();
      final double area = getArea();
      final double angle = getRotateAngle();

      _rotatedElements.forEach(cntxt -> {
         assert (cntxt.angleOffset >= 0);
         double angle2 = angle - cntxt.angleOffset;
         if (angle2 < 0)
            angle2 += 360;
         assert (angle2 < 360);
         assert (angle2 >= 0);
         // (un)comment next line to view result changes...
         angle2 = Math.sin(FigureHelper.toRadian((angle2 / 4))) * angle2; // accelerate / ускоряшка..

         // (un)comment next line to view result changes...
         cntxt.area = area * (1 + Math.sin(FigureHelper.toRadian(angle2 / 2))); // zoom'ирую



         BaseCell cell = matrix.get(cntxt.index);

         cell.Init();
         PointDouble center = cell.getCenter();
         Coord coord = cell.getCoord();

         // modify
         attr.setArea(cntxt.area);

         // rotate
         cell.Init();
         PointDouble centerNew = cell.getCenter();
         PointDouble delta = new PointDouble(center.x - centerNew.x, center.y - centerNew.y);
         FigureHelper.rotate(cell.getRegion().getPoints(), (((coord.x + coord.y) & 1) == 0) ? +angle2 : -angle2, centerNew, delta);

         // restore
         attr.setArea(area);
      });

      // Z-ordering
      Collections.sort(_rotatedElements, (e1, e2) -> Double.compare(e1.area, e2.area));
   }

   /** list of offsets rotation angles prepared for cells */
   private final List<Double /* angle offset */ > _prepareList = new ArrayList<>();
   protected final List<RotatedCellContext> _rotatedElements = new ArrayList<>();

   private void randomRotateElemenIndex() {
      _prepareList.clear();
      if (!_rotatedElements.isEmpty()) {
         _rotatedElements.clear();
         onPropertyChanged("RotatedElements");
      }

      if (!isRotate())
         return;

      // create random cells indexes  and  base rotate offset (negative)
      int len = getMatrix().size();
      Random rand = new Random(UUID.randomUUID().hashCode());
      for (int i = 0; i < len/4.5; ++i) {
         addRandomToPrepareList(i==0, rand);
      }
   }

   private void addRandomToPrepareList(boolean zero, Random rand) {
      double offset = (zero ? 0 : rand.nextInt(360)) + getRotateAngle();
      if (offset > 360)
         offset -= 360;
      _prepareList.add(offset);
   }

   private int nextRandomIndex(Random rand) {
      int len = getMatrix().size();
      assert (_rotatedElements.size() < len);
      do {
         int index = rand.nextInt(len);
         if (_rotatedElements.stream().anyMatch(ctxt -> ctxt.index == index))
            continue;
         return index;
      } while(true);
   }

   protected void updateAnglesOffsets(double angleOld) {
      double angleNew = getRotateAngle();
      double rotateDelta = getRotateAngleDelta();
      double area = getArea();
      Random rand = new Random(UUID.randomUUID().hashCode());

      if (!_prepareList.isEmpty()) {
         List<Double> copyList = new ArrayList<Double>(_prepareList);
         for (int i = copyList.size()-1; i >= 0; --i) {
            double angleOffset = copyList.get(i);
            if ((rotateDelta >= 0)
               ?  ((angleOld <= angleOffset && angleOffset <  angleNew && angleOld < angleNew) || // example: old=10   offset=15   new=20
                   (angleOld <= angleOffset && angleOffset >  angleNew && angleOld > angleNew) || // example: old=350  offset=355  new=0
                   (angleOld >  angleOffset && angleOffset <= angleNew && angleOld > angleNew))   // example: old=355  offset=0    new=5
               :  ((angleOld >= angleOffset && angleOffset >  angleNew && angleOld > angleNew) || // example: old=20   offset=15   new=10
                   (angleOld <  angleOffset && angleOffset >  angleNew && angleOld < angleNew) || // example: old=0    offset=355  new=350
                   (angleOld >= angleOffset && angleOffset <= angleNew && angleOld < angleNew)))  // example: old=5    offset=0    new=355
            {
               _prepareList.remove(i);
               _rotatedElements.add(new RotatedCellContext(nextRandomIndex(rand), angleOffset, area));
               onPropertyChanged("RotatedElements");
            }
         }
      }

      List<RotatedCellContext> toRemove = new ArrayList<>();
      _rotatedElements.forEach(cntxt -> {
         double angle2 = angleNew - cntxt.angleOffset;
         if (angle2 < 0)
            angle2 += 360;
         assert (angle2 < 360);
         assert (angle2 >= 0);

         // prepare to next step - exclude current cell from rotate and add next random cell
         double angle3 = angle2 + rotateDelta;
         if ((angle3 >= 360) || (angle3 < 0)) {
            toRemove.add(cntxt);
         }
      });
      if (!toRemove.isEmpty()) {
         List<BaseCell> matrix = getMatrix();
         toRemove.forEach(cntxt -> {
                           matrix.get(cntxt.index).Init(); // restore original region coords
                           _rotatedElements.remove(cntxt);
                           addRandomToPrepareList(false, rand);
                        });
         onPropertyChanged("RotatedElements");
      }
   }

}
