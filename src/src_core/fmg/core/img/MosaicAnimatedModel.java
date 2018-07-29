package fmg.core.img;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import fmg.common.Color;
import fmg.common.geom.Coord;
import fmg.common.geom.DoubleExt;
import fmg.common.geom.PointDouble;
import fmg.common.geom.SizeDouble;
import fmg.common.geom.util.FigureHelper;
import fmg.core.mosaic.MosaicDrawModel;
import fmg.core.mosaic.cells.BaseCell;
import fmg.core.mosaic.cells.BaseCell.BaseAttribute;
import fmg.core.types.draw.PenBorder;

/** Representable {@link fmg.core.types.EMosaic} as animated image */
public class MosaicAnimatedModel<TImageInner> extends MosaicDrawModel<TImageInner> implements IAnimatedModel {

   public enum ERotateMode {
      /** rotate full matrix (all cells) */
      fullMatrix,
      /** rotate some cells (independently of each other) */
      someCells
   }

   private ERotateMode _rotateMode = ERotateMode.fullMatrix;
   /** 0° .. +360° */
   private double _rotateAngle;
   /** list of offsets rotation angles prepared for cells */
   private final List<Double /* angle offset */ > _prepareList = new ArrayList<>();
   private final List<RotatedCellContext> _rotatedElements = new ArrayList<>();
   private boolean _disableCellAttributeListener = false;
   private boolean _disableListener = false;
   private final AnimatedInnerModel _innerModel = new AnimatedInnerModel();
   private PropertyChangeListener innerModelListener = ev -> onInnerModelPropertyChanged(ev);

   public MosaicAnimatedModel() {
      _innerModel.addListener(innerModelListener);
   }

   public static final String PROPERTY_ROTATE_ANGLE     = "RotateAngle";
   public static final String PROPERTY_ROTATE_MODE      = "RotateMode";
   public static final String PROPERTY_ROTATED_ELEMENTS = "RotatedElements";

   @Override
   public boolean isAnimated() { return _innerModel.isAnimated(); }
   @Override
   public void setAnimated(boolean value) { _innerModel.setAnimated(value); }

   /** Overall animation period (in milliseconds) */
   @Override
   public long getAnimatePeriod() { return _innerModel.getAnimatePeriod(); }
   /** Overall animation period (in milliseconds) */
   @Override
   public void setAnimatePeriod(long value) { _innerModel.setAnimatePeriod(value); }

   /** Total frames of the animated period */
   @Override
   public int getTotalFrames() { return _innerModel.getTotalFrames(); }
   @Override
   public void setTotalFrames(int value) { _innerModel.setTotalFrames(value); }

   @Override
   public int getCurrentFrame() { return _innerModel.getCurrentFrame(); }
   @Override
   public void setCurrentFrame(int value) { _innerModel.setCurrentFrame(value); }

   public ERotateMode getRotateMode() { return _rotateMode; }
   public void setRotateMode(ERotateMode value) { _notifier.setProperty(_rotateMode, value, PROPERTY_ROTATE_MODE); }

   /** 0° .. +360° */
   public double getRotateAngle() { return _rotateAngle; }
   public void setRotateAngle(double value) {
      _notifier.setProperty(_rotateAngle, AnimatedImageModel.fixAngle(value), PROPERTY_ROTATE_ANGLE);
   }

   public List<RotatedCellContext> getRotatedElements() { return _rotatedElements; }

   @Override
   protected void onPropertyChanged(Object oldValue, Object newValue, String propertyName) {
      if (_disableListener)
         return;
      super.onPropertyChanged(oldValue, newValue, propertyName);
      switch (propertyName) {
      case PROPERTY_ROTATE_MODE:
      case PROPERTY_SIZE_FIELD:
         if (getRotateMode() == ERotateMode.someCells)
            randomRotateElemenIndex();
         break;
      }
   }

   /** ///////////// ================= PART {@link ERotateMode#fullMatrix} ======================= ///////////// */

   public void rotateMatrix() { rotateMatrix(true); }
   private void rotateMatrix(boolean reinit) {
      SizeDouble size = getCellAttr().getSize(getSizeField());
      PointDouble center = new PointDouble(size.width  / 2,
                                           size.height / 2);
      double rotateAngle = getRotateAngle();
      for (BaseCell cell : getMatrix()) {
         cell.init(); // restore base coords

         FigureHelper.rotateCollection(cell.getRegion().getPoints(), rotateAngle, center);
      }
      _notifier.onPropertyChanged(PROPERTY_MATRIX);
   }

   /** ///////////// ================= PART {@link ERotateMode#someCells} ======================= ///////////// */

   private boolean _rotateCellAlterantive;

   public static final class RotatedCellContext {
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

         cell.init();
         PointDouble center = cell.getCenter();
         Coord coord = cell.getCoord();

         // modify
         _disableCellAttributeListener = true; // disable handling MosaicGameModel.onCellAttributePropertyChanged(where event.propertyName == BaseCell.BaseAttribute.PROPERTY_AREA)
         attr.setArea(cntxt.area);

         // rotate
         cell.init();
         PointDouble centerNew = cell.getCenter();
         PointDouble delta = new PointDouble(center.x - centerNew.x, center.y - centerNew.y);
         FigureHelper.moveCollection(FigureHelper.rotateCollection(cell.getRegion().getPoints(), (((coord.x + coord.y) & 1) == 0) ? +angle2 : -angle2, _rotateCellAlterantive ? center : centerNew), delta);

         // restore
         attr.setArea(area);
         _disableCellAttributeListener = false;
      });

      // Z-ordering
      Collections.sort(_rotatedElements, (e1, e2) -> Double.compare(e1.area, e2.area));
   }

   public List<BaseCell> getNotRotatedCells() {
      if (_rotatedElements.isEmpty())
         return getMatrix();

      List<BaseCell> matrix = getMatrix();
      List<Integer> indexes = _rotatedElements.stream().map(cntxt -> cntxt.index).collect(Collectors.toList());
      List<BaseCell> notRotated = new ArrayList<>(matrix.size() - indexes.size());
      int i = 0;
      for (BaseCell cell : matrix) {
         if (!indexes.contains(i))
            notRotated.add(cell);
         ++i;
      }
      return notRotated;
   }

   public void getRotatedCells(Consumer<List<BaseCell>> rotatedCellsFunctor) {
      if (_rotatedElements.isEmpty())
         return;

      PenBorder pb = getPenBorder();
      // save
      double borderWidth = pb.getWidth();
      Color colorLight  = pb.getColorLight();
      Color colorShadow = pb.getColorShadow();
      // modify
      _disableListener = true;
      pb.setWidth(2 * borderWidth);
      pb.setColorLight(colorLight.darker(0.5));
      pb.setColorShadow(colorShadow.darker(0.5));

      List<BaseCell> matrix = getMatrix();
      List<BaseCell> rotatedCells = new ArrayList<>(_rotatedElements.size());
      for (RotatedCellContext cntxt : _rotatedElements)
         rotatedCells.add(matrix.get(cntxt.index));
      rotatedCellsFunctor.accept(rotatedCells);

      // restore
      pb.setWidth(borderWidth);
      pb.setColorLight(colorLight);
      pb.setColorShadow(colorShadow);
      _disableListener = false;
   }

   private void randomRotateElemenIndex() {
      _prepareList.clear();
      if (!_rotatedElements.isEmpty()) {
         _rotatedElements.clear();
         _notifier.onPropertyChanged(PROPERTY_ROTATED_ELEMENTS);
      }

//      if (!isAnimated())
//         return;

      // create random cells indexes  and  base rotate offset (negative)
      int len = getMatrix().size();
      for (int i = 0; i < len/4.5; ++i) {
         addRandomToPrepareList(i==0);
      }
   }

   private void addRandomToPrepareList(boolean zero) {
      double offset = (zero ? 0 : ThreadLocalRandom.current().nextInt(360)) + getRotateAngle();
      if (offset > 360)
         offset -= 360;
      _prepareList.add(offset);
   }

   private int nextRandomIndex() {
      int len = getMatrix().size();
      assert (_rotatedElements.size() < len);
      Random rand = ThreadLocalRandom.current();
      do {
         int index = rand.nextInt(len);
         if (_rotatedElements.stream().anyMatch(ctxt -> ctxt.index == index))
            continue;
         return index;
      } while(true);
   }

   public void updateAnglesOffsets(double rotateAngleDelta) {
      double angleNew = getRotateAngle();
      double angleOld = angleNew - rotateAngleDelta;
      double rotateDelta = rotateAngleDelta;
      double area = getArea();

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
               _rotatedElements.add(new RotatedCellContext(nextRandomIndex(), angleOffset, area));
               _notifier.onPropertyChanged(PROPERTY_ROTATED_ELEMENTS);
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
                           matrix.get(cntxt.index).init(); // restore original region coords
                           _rotatedElements.remove(cntxt);
                           if (_rotatedElements.isEmpty())
                              _rotateCellAlterantive = !_rotateCellAlterantive;
                           addRandomToPrepareList(false);
                        });
         _notifier.onPropertyChanged(PROPERTY_ROTATED_ELEMENTS);
      }
   }

   @Override
   protected void onCellAttributePropertyChanged(PropertyChangeEvent ev) {
      if (_disableCellAttributeListener)
         return;
      super.onCellAttributePropertyChanged(ev);

      String propName = ev.getPropertyName();
      if (BaseCell.BaseAttribute.PROPERTY_AREA.equals(propName))
         switch (getRotateMode()) {
         case fullMatrix:
            if (!DoubleExt.hasMinDiff(_rotateAngle, 0))
               rotateMatrix(false);
            break;
         case someCells:
            //updateAnglesOffsets(rotateAngleDelta);
            //rotateCells();
            break;
         }
   }

   protected void onInnerModelPropertyChanged(PropertyChangeEvent ev) {
      // refire
      _notifier.onPropertyChanged(ev.getOldValue(), ev.getNewValue(), ev.getPropertyName());
   }

   @Override
   public void close() {
      _innerModel.removeListener(innerModelListener);
      super.close();
   }

}
