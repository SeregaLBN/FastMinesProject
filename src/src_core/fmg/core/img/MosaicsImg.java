package fmg.core.img;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
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

/** representable {@link fmg.core.types.EMosaic} as image */
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

   public enum ERotateMode {
      fullMatrix,
      someCells
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
   }

   /** ///////////// ================= PART {@link ERotateMode#someCells} ======================= ///////////// */

   protected static final class RotatedCellContext {
      public RotatedCellContext(int index, double angle, double area) {
         this.index = index;
         this.angle = angle;
         this.area = area;
      }
      public int index;
      public double angle;
      public double area;
   }

   private Stream<RotatedCellContext> getRotatedCellsContext() {
      double angle = getRotateAngle();
      double area = getArea();
      return _rotatedElements.stream()
            .map(cntxt -> {
               int index = cntxt.index;
               double angleOffset = cntxt.angle;
               assert (angleOffset >= 0);
               double angle2 = angle - angleOffset;
               if (angle2 < 0)
                  angle2 += 360;
               assert (angle2 < 360);
               assert (angle2 >= 0);
               // (un)comment next line to view result changes...
               angle2 = Math.sin(FigureHelper.toRadian((angle2 / 4))) * angle2; // ускоряшка..

               // (un)comment next line to view result changes...
               double area2 = area * (1 + Math.sin(FigureHelper.toRadian(angle2 / 2))); // zoom'ирую
               return new RotatedCellContext(index, angle2, area2);
            });
   }

   /** rotate BaseCell from original Matrix with modified Region */
   protected void rotatedCells() {
      BaseAttribute attr = getCellAttr();
      double area = getArea();
      List<BaseCell> matrix = getMatrix();

      getRotatedCellsContext().forEach(ctxt -> {
         int index = ctxt.index;
         double angle2 = ctxt.angle;
         double area2 = ctxt.area;

         BaseCell cell = matrix.get(index);

         cell.Init();
         PointDouble center = cell.getCenter();
         Coord coord = cell.getCoord();

         // modify
         attr.setArea(area2);

         // rotate
         cell.Init();
         PointDouble centerNew = cell.getCenter();
         RegionDouble reg = cell.getRegion();
         Stream<PointDouble> newReg = reg.getPoints().stream()
                         .map(p -> {
                            p.x -= centerNew.x;
                            p.y -= centerNew.y;
                            return p;
                         });
         newReg = FigureHelper.rotate(newReg, (((coord.x + coord.y) & 1) == 0) ? +angle2 : -angle2)
                         .map(p -> {
                            p.x += center.x;
                            p.y += center.y;
                            return p;
                         });
         int[] i = {0};
         newReg.forEach(p -> reg.setPoint(i[0]++, (int)p.x, (int)p.y));

         // restore
         attr.setArea(area);
      });
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
      double rotateDelta = getRotateAngleDelta();
      double angleNew = getRotateAngle();
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
               _rotatedElements.add(new RotatedCellContext(nextRandomIndex(rand), angleOffset, getArea()));
               onPropertyChanged("RotatedElements");
            }
         }
      }

      List<RotatedCellContext> toRemove = new ArrayList<>();
      _rotatedElements.forEach(cntxt -> {
         double angle = cntxt.angle;
         double angle2 = angleNew - angle;
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
