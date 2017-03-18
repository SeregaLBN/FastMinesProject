package fmg.core.img;

import java.util.*;

import fmg.common.geom.*;
import fmg.common.geom.util.FigureHelper;
import fmg.core.mosaic.IMosaic;
import fmg.core.mosaic.MosaicHelper;
import fmg.core.mosaic.cells.BaseCell;
import fmg.core.mosaic.cells.BaseCell.BaseAttribute;
import fmg.core.types.EMosaic;

/**
 * Abstract representable {@link fmg.core.types.EMosaic} as image
 *
 * @param <TImage> plaform specific image
 */
public abstract class AMosaicsImg<TImage>
      extends RotatedImg<TImage>
      implements IMosaic
{
   public enum ERotateMode {
      fullMatrix,
      someCells
   }

   protected AMosaicsImg(EMosaic mosaicType, Matrisize sizeField) {
      _mosaicType = mosaicType;
      _sizeField = sizeField;
   }

   public static final String PROPERTY_MOSAIC_TYPE      = "MosaicType";
   public static final String PROPERTY_SIZE_FIELD       = "SizeField";
   public static final String PROPERTY_CELL_ATTR        = "CellAttr";
   public static final String PROPERTY_MATRIX           = "Matrix";
   public static final String PROPERTY_AREA             = "Area";
   public static final String PROPERTY_PADDING_FULL     = "PaddingFull";
   public static final String PROPERTY_ROTATE_MODE      = "RotateMode";
   public static final String PROPERTY_ROTATED_ELEMENTS = "RotatedElements";

   private EMosaic _mosaicType;
   /** из каких фигур состоит мозаика поля */
   @Override
   public EMosaic getMosaicType() { return _mosaicType; }
   @Override
   public void setMosaicType(EMosaic value) {
      if (setProperty(_mosaicType, value, PROPERTY_MOSAIC_TYPE)) {
         setArea(0); // mark to recalc
         _matrix.clear();
         setCellAttr(null);
      }
   }

   private Matrisize _sizeField;
   @Override
   public Matrisize getSizeField() { return _sizeField; }
   @Override
   public void setSizeField(Matrisize value) {
      if (setProperty(_sizeField, value, PROPERTY_SIZE_FIELD)) {
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
         setCellAttr(MosaicHelper.createAttributeInstance(getMosaicType()));
      return _cellAttr;
   }
   private void setCellAttr(BaseCell.BaseAttribute value) {
      if (setProperty(_cellAttr, value, PROPERTY_CELL_ATTR)) {
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
         onSelfPropertyChanged(PROPERTY_MATRIX);
         invalidate();
      }
      return _matrix;
   }

   private void recalcArea() {
      int w = getSize().width;
      int h = getSize().height;
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
      if (setProperty(_area, value, PROPERTY_AREA)) {
         dependency_CellAttribute_Area();
         invalidate();
      }
   }

   private BoundDouble _paddingFull;
   public BoundDouble getPaddingFull() { return _paddingFull; }
   protected void setPaddingFull(BoundDouble value) {
      if (setProperty(_paddingFull, value, PROPERTY_PADDING_FULL)) {
         invalidate();
      }
   }

   private ERotateMode _rotateMode = ERotateMode.fullMatrix;
   public ERotateMode getRotateMode() { return _rotateMode; }
   public void setRotateMode(ERotateMode value) { setProperty(_rotateMode, value, PROPERTY_ROTATE_MODE); }

   @Override
   protected void onSelfPropertyChanged(Object oldValue, Object newValue, String propertyName) {
      //LoggerSimple.Put("onSelfPropertyChanged: {0}: PropertyName={1}", Entity, ev.PropertyName);
      super.onSelfPropertyChanged(oldValue, newValue, propertyName);
      switch (propertyName) {
      case PROPERTY_SIZE:
      case PROPERTY_PADDING:
         recalcArea();
         break;
      case PROPERTY_ROTATE:
      case PROPERTY_ROTATE_MODE:
      case PROPERTY_SIZE_FIELD:
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
   ////////////// #endregion

   @Override
   protected void onTimer() {
      ERotateMode rotateMode = getRotateMode();

      Double anglePrevis = (rotateMode == ERotateMode.someCells) ? getRotateAngle() : null;
      super.onTimer();

      switch (rotateMode) {
      case fullMatrix:
         rotateMatrix();
         break;
      case someCells:
         updateAnglesOffsets(anglePrevis);
         rotateCells();
         break;
      }
   }

   /** ///////////// ================= PART {@link ERotateMode#fullMatrix} ======================= ///////////// */

   public void rotateMatrix() {
      PointDouble center = new PointDouble(getSize().width / 2.0 - _paddingFull.left, getSize().height / 2.0 - _paddingFull.top);
      for (BaseCell cell : getMatrix()) {
         cell.Init(); // restore base coords

         FigureHelper.rotateCollection(cell.getRegion().getPoints(), getRotateAngle(), center);
      }
   }

   /** ///////////// ================= PART {@link ERotateMode#someCells} ======================= ///////////// */

   private boolean rotateCellAlterantive;

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
   protected void rotateCells() {
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
         // (un)comment next line to look result changes...
         angle2 = Math.sin(FigureHelper.toRadian((angle2 / 4))) * angle2; // accelerate / ускоряшка..

         // (un)comment next line to look result changes...
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
         FigureHelper.moveCollection(FigureHelper.rotateCollection(cell.getRegion().getPoints(), (((coord.x + coord.y) & 1) == 0) ? +angle2 : -angle2, rotateCellAlterantive ? center : centerNew), delta);

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
         onSelfPropertyChanged(PROPERTY_ROTATED_ELEMENTS);
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
               onSelfPropertyChanged(PROPERTY_ROTATED_ELEMENTS);
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
                           if (_rotatedElements.isEmpty())
                              rotateCellAlterantive = !rotateCellAlterantive;
                           addRandomToPrepareList(false, rand);
                        });
         onSelfPropertyChanged(PROPERTY_ROTATED_ELEMENTS);
      }
   }

}
