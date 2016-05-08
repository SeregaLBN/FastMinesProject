package fmg.core.img;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Stream;

import fmg.common.geom.Bound;
import fmg.common.geom.Coord;
import fmg.common.geom.Matrisize;
import fmg.common.geom.PointDouble;
import fmg.common.geom.RegionDouble;
import fmg.common.geom.Size;
import fmg.common.geom.util.FigureHelper;
import fmg.core.mosaic.MosaicHelper;
import fmg.core.mosaic.cells.BaseCell;
import fmg.core.mosaic.cells.BaseCell.BaseAttribute;
import fmg.core.mosaic.draw.IPaintable;
import fmg.core.types.EMosaic;

/** картинка поля конкретной мозаики, где могут вращаться отдельный ячейки */
public abstract class MosaicsAnimateImg<TPaintable extends IPaintable, TImage extends Object> extends MosaicsImg<TPaintable, TImage> {

   public MosaicsAnimateImg(EMosaic mosaicType, Matrisize sizeField) { super(mosaicType, sizeField); }
   public MosaicsAnimateImg(EMosaic mosaicType, Matrisize sizeField, int widthAndHeight) { super(mosaicType, sizeField, widthAndHeight); }
   public MosaicsAnimateImg(EMosaic mosaicType, Matrisize sizeField, int widthAndHeight, int padding) { super(mosaicType, sizeField, widthAndHeight, padding); }
   public MosaicsAnimateImg(EMosaic mosaicType, Matrisize sizeField, Size sizeImage, Bound padding) { super(mosaicType, sizeField, sizeImage, padding); }

   /** need redraw the static part of the cache */
   private boolean _invalidateCache = true;
   /**
    * Cached static part of the picture.
    * ! Recreated only when changing the original image size (minimizing CreateImage calls).
    **/
   private TImage _imageCache;
   protected TImage getImageCache() {
      if (_imageCache == null) {
         _imageCache = createImage();
         _invalidateCache = true;
      }
      if (_invalidateCache) {
         _invalidateCache = false;
         drawCache();
      }
      return _imageCache;
   }

   private static final class RotatedCellContext {
      public RotatedCellContext(int index, double rotateAngle, double area) {
         this.index = index;
         this.rotateAngle = rotateAngle;
         this.area = area;
      }
      public int index;
      public double rotateAngle;
      public double area;
   }

   private Stream<RotatedCellContext> getRotatedCells() {
      double angle = getRotateAngle();
      double area = getArea();
      return _rotatedElements.entrySet().stream()
            .map(pair -> {
               int index = pair.getKey();
               double angleOffset = pair.getValue();
               assert (angleOffset >= 0);
               double angle2 = angle - angleOffset;
               if (angle2 < 0)
                  angle2 += 360;
               assert (angle2 < 360);
               assert (angle2 >= 0);
               // (un)comment next line to view result changes...
               angle2 = FigureHelper.toRadian(Math.sin((angle2 / 4))) * angle2; // ускоряшка..

               // (un)comment next line to view result changes...
               double area2 = area * (1 + FigureHelper.toRadian(Math.sin(angle2 / 2))); // zoom'ирую
               return new RotatedCellContext(index, angle2, area2);
            })
            .sorted((e1, e2) -> Double.compare(e1.area, e2.area)); // order by area2
   }

   protected void drawRotatedCells(Consumer<BaseCell> drawCellFunc) {
      BaseAttribute attr = getCellAttr();
      EMosaic mosaicType = getMosaicType();
      // save
      double area = getArea();

      getRotatedCells().forEach(ctxt -> {
         int index = ctxt.index;
         double angle2 = ctxt.rotateAngle;
         double area2 = ctxt.area;

         BaseCell rotatedCell = getMatrix().get(index);

         PointDouble center = rotatedCell.getCenter();
         Coord coord = rotatedCell.getCoord();

         // modify
         attr.setArea(area2);

         // rotate
         BaseCell cellNew = MosaicHelper.createCellInstance(attr, mosaicType, new Coord(coord.x, coord.y)); // 'copy' rotatedCell with zoomed Area
         PointDouble centerNew = cellNew.getCenter();
         RegionDouble reg = cellNew.getRegion();
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

         // draw rotated cell
         drawCellFunc.accept(cellNew);

         // restore
         attr.setArea(area); // ! before next call IEnumerable<> RotatedCells
      });
   }

   protected abstract void drawCache();

   @Override
   protected void onPropertyChanged(Object oldValue, Object newValue, String propertyName) {
      super.onPropertyChanged(oldValue, newValue, propertyName);
      switch (propertyName) {
      case "Rotate":
      case "SizeField":
         randomRotateElemenIndex();
         break;
      case "Image":
         _invalidateCache = true;
         _imageCache = null;
         break;
      }
   }

   /** list of offsets rotation angles prepared for cells */
   private final List<Double> _prepareList = new ArrayList<Double>();
   protected final Map<Integer /* cell index */, Double /* rotate angle offset */> _rotatedElements = new HashMap<Integer, Double>();

   private void randomRotateElemenIndex() {
      _prepareList.clear();
      if (!_rotatedElements.isEmpty()) {
         _rotatedElements.clear();
         _invalidateCache = true;
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
         if (_rotatedElements.containsKey(index))
            continue;
         return index;
      } while(true);
   }

   @Override
   protected void onTimer() {
      double angleOld = getRotateAngle();
      super.onTimer();
      double angleNew = getRotateAngle();
      Random rand = new Random(UUID.randomUUID().hashCode());

      if (!_prepareList.isEmpty()) {
         List<Double> copyList = new ArrayList<Double>(_prepareList);
         for (int i = copyList.size()-1; i >= 0; --i) {
            double angleOffset = copyList.get(i);
            if ((angleOld <= angleOffset && angleOffset < angleNew && angleOld < angleNew) || // example: old=10   offset=15   new=20
                (angleOld <= angleOffset && angleOffset > angleNew && angleOld > angleNew) || // example: old=350  offset=355  new=0
                (angleOld > angleOffset && angleOffset <= angleNew && angleOld > angleNew))   // example: old=355  offset=0    new=5
            {
               _prepareList.remove(i);
               _rotatedElements.put(nextRandomIndex(rand), angleOffset);
               _invalidateCache = true;
            }
         }
      }

      double rotateDelta = getRotateAngleDelta();
      List<Integer> toRemove = new ArrayList<>();
      _rotatedElements.forEach((index, angleOffset) -> {
         double angle2 = angleNew - angleOffset;
         if (angle2 < 0)
            angle2 += 360;
         assert (angle2 < 360);
         assert (angle2 >= 0);

         // prepare to next step - exclude current cell from rotate and add next random cell
         double angle3 = angle2 + rotateDelta;
         if ((angle3 >= 360) || (angle3 < 0)) {
            toRemove.add(index);
         }
      });
      if (toRemove != null)
         toRemove.forEach(index -> {
                           _rotatedElements.remove(index);
                           addRandomToPrepareList(false, rand);
                           _invalidateCache = true;
                        });
   }

}
