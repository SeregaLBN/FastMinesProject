package fmg.core.img;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import fmg.common.Color;
import fmg.common.HSV;
import fmg.common.geom.Bound;
import fmg.common.geom.PointDouble;
import fmg.common.geom.Rect;

/**
 * Abstract representable menu as horizontal or vertical lines
 * @param <TImage> plaform specific image
 */
public abstract class BurgerMenuImg<TImage> extends PolarLightFgTransformer<TImage> {

   protected BurgerMenuImg() {
      _showBurgerMenu = true;
      _layersInBurgerMenu = 3;
      _horizontalBurgerMenu = true;
      _rotateBurgerMenu = true;
   }

   public static final String PROPERTY_SHOW_BURGER_MENU = "ShowBurgerMenu";
   public static final String PROPERTY_HORIZONTAL_BURGER_MENU = "HorizontalBurgerMenu";
   public static final String PROPERTY_LAYERS_IN_BURGER_MENU = "LayersInBurgerMenu";
   public static final String PROPERTY_ROTATE_BURGER_MENU = "RotateBurgerMenu";
   public static final String PROPERTY_PADDING_BURGER_MENU = "PaddingBurgerMenu";

   private boolean _showBurgerMenu;
   public boolean isShowBurgerMenu() { return _showBurgerMenu; }
   public void   setShowBurgerMenu(boolean value) {
      if (setProperty(_showBurgerMenu, value, PROPERTY_SHOW_BURGER_MENU)) {
         invalidate();
      }
   }

   private boolean _horizontalBurgerMenu;
   public boolean isHorizontalBurgerMenu() { return _horizontalBurgerMenu; }
   public void   setHorizontalBurgerMenu(boolean value) {
      if (setProperty(_horizontalBurgerMenu, value, PROPERTY_HORIZONTAL_BURGER_MENU)) {
         invalidate();
      }
   }

   private int   _layersInBurgerMenu;
   public int  getLayersInBurgerMenu() { return _layersInBurgerMenu; }
   public void setLayersInBurgerMenu(int value) {
      if (setProperty(_layersInBurgerMenu, value, PROPERTY_LAYERS_IN_BURGER_MENU)) {
         invalidate();
      }
   }

   private boolean _rotateBurgerMenu;
   public boolean isRotateBurgerMenu() { return _rotateBurgerMenu; }
   public void   setRotateBurgerMenu(boolean value) {
      if (setProperty(_rotateBurgerMenu, value, PROPERTY_ROTATE_BURGER_MENU)) {
         invalidate();
      }
   }

   private Bound _paddingBurgerMenu;
   public Bound getPaddingBurgerMenu() {
      if (_paddingBurgerMenu == null)
         setPaddingBurgerMenu(new Bound(getSize().width / 2,
                                        getSize().height / 2,
                                        getPadding().right,
                                        getPadding().bottom));
      return _paddingBurgerMenu;
   }
   public void  setPaddingBurgerMenu(Bound value) {
      if (value.getLeftAndRight() >= getSize().width)
         throw new IllegalArgumentException("Padding size is very large. Should be less than Width.");
      if (value.getTopAndBottom() >= getSize().height)
         throw new IllegalArgumentException("Padding size is very large. Should be less than Height.");
      if (setProperty(_paddingBurgerMenu, value, PROPERTY_PADDING_BURGER_MENU)) {
         invalidate();
      }
   }
   public void resetPaddingBurgerMenu() {
      if (_paddingBurgerMenu == null)
         return;
      _paddingBurgerMenu = null;
      onSelfPropertyChanged(PROPERTY_PADDING_BURGER_MENU);
      invalidate();
   }

   protected static class LineInfo {
      public Color clr;
      public double penWidht;
      public PointDouble from; // start coord
      public PointDouble to;   // end   coord
   }

   protected Stream<LineInfo> getCoordsBurgerMenu() {
      if (!isShowBurgerMenu())
         return Stream.empty();

      boolean horizontal = isHorizontalBurgerMenu();
      int layers = getLayersInBurgerMenu();
      Bound pad = getPaddingBurgerMenu();
      Rect rc = new Rect(pad.left,
                         pad.top,
                         getSize().width  - pad.getLeftAndRight(),
                         getSize().height - pad.getTopAndBottom());
      double penWidth = Math.max(1, (horizontal ? rc.height : rc.width) / (2.0 * layers));
      double rotateAngle = isRotateBurgerMenu() ? getRotateAngle() : 0;
      double stepAngle = 360.0 / layers;

      return IntStream.range(0, layers)
         .mapToObj(layerNum -> {
            double layerAlignmentAngle = fixAngle(layerNum*stepAngle + rotateAngle);
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

}
