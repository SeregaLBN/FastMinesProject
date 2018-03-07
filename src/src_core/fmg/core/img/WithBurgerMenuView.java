package fmg.core.img;

import java.beans.PropertyChangeListener;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import fmg.common.Color;
import fmg.common.HSV;
import fmg.common.Pair;
import fmg.common.geom.Bound;
import fmg.common.geom.PointDouble;
import fmg.common.geom.Rect;

/**
 * MVC: view of images with burger menu
 * @param <TImage> - platform specific image
 * @param <TImageModel> - model of image
 */
public abstract class BurgerMenuView<TImage, TImageModel extends ImageProperties> extends AImageView<TImage, TImageModel> {

   /** the second model of image */
   private final BurgerMenuModel _burgerMenuModel;
   private final PropertyChangeListener _burgerMenuModelListener;

   protected BurgerMenuView(TImageModel imageModel) {
      super(imageModel);
      _burgerMenuModel = new BurgerMenuModel(() -> imageModel.getSize());
      _burgerMenuModelListener = event -> {
         assert event.getSource() == _burgerMenuModel; // by reference
         onPropertyBurgerMenuModelChanged(event.getOldValue(), event.getNewValue(), event.getPropertyName());
      };
      _burgerMenuModel.addListener(_burgerMenuModelListener);
   }

   public BurgerMenuModel getBurgerMenuModel() { return _burgerMenuModel; }

   protected void onPropertyBurgerMenuModelChanged(Object oldValue, Object newValue, String propertyName) {
      invalidate();
   }

   /** get paint information of drawing basic image model */
   protected abstract Stream<Pair<Color, Stream<PointDouble>>> getCoords();


   protected static class LineInfo {
      public Color clr;
      public double penWidht;
      public PointDouble from; // start coord
      public PointDouble to;   // end   coord
   }

   /** get paint information of drawing burger menu model image */
   protected Stream<LineInfo> getCoordsBurgerMenu() {
      TImageModel m = getModel();
      BurgerMenuModel bm = getBurgerMenuModel();
      if (!bm.isShow())
         return Stream.empty();

      boolean horizontal = bm.isHorizontal();
      int layers = bm.getLayers();
      Bound pad = bm.getPadding();
      Rect rc = new Rect(pad.left,
                         pad.top,
                         getSize().width  - pad.getLeftAndRight(),
                         getSize().height - pad.getTopAndBottom());
      double penWidth = Math.max(1, (horizontal ? rc.height : rc.width) / (2.0 * layers));
      double rotateAngle = bm.isRotate() ? m.getRotateAngle() : 0;
      double stepAngle = 360.0 / layers;

      return IntStream.range(0, layers)
         .mapToObj(layerNum -> {
            double layerAlignmentAngle = ImageProperties.fixAngle(layerNum*stepAngle + rotateAngle);
            double offsetTop  = !horizontal ? 0 : layerAlignmentAngle*rc.height/360;
            double offsetLeft =  horizontal ? 0 : layerAlignmentAngle*rc.width /360;
            PointDouble start = new PointDouble(rc.left() + offsetLeft,
                                                rc.top()  + offsetTop);
            PointDouble end   = new PointDouble((horizontal ? rc.right() : rc.left()) + offsetLeft,
                                                (horizontal ? rc.top() : rc.bottom()) + offsetTop);

            HSV hsv = new HSV(Color.Gray);
            hsv.v *= Math.sin(layerNum*stepAngle / layers);

            LineInfo li = new LineInfo();
            li.clr = hsv.toColor();
            li.penWidht = penWidth;
            li.from = start;
            li.to = end;
            return li;
         });
   }

   @Override
   public void close() {
      _burgerMenuModel.removeListener(_burgerMenuModelListener);
      super.close();
   }

}
