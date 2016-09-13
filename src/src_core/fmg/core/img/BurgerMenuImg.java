package fmg.core.img;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import fmg.common.Color;
import fmg.common.HSV;
import fmg.common.geom.PointDouble;
import fmg.common.geom.RectDouble;

/**
 * Abstract representable menu as horizontal or vertical lines
 * @param <TImage> plaform specific image
 */
public abstract class BurgerMenuImg<TImage> extends PolarLightsImg<TImage> {

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

   private boolean _showBurgerMenu;
   public boolean isShowBurgerMenu() { return _showBurgerMenu; }
   public void   setShowBurgerMenu(boolean value) { setProperty(_showBurgerMenu, value, PROPERTY_SHOW_BURGER_MENU); }

   private boolean _horizontalBurgerMenu;
   public boolean isHorizontalBurgerMenu() { return _horizontalBurgerMenu; }
   public void   setHorizontalBurgerMenu(boolean value) { setProperty(_horizontalBurgerMenu, value, PROPERTY_HORIZONTAL_BURGER_MENU); }

   private int   _layersInBurgerMenu;
   public int  getLayersInBurgerMenu() { return _layersInBurgerMenu; }
   public void setLayersInBurgerMenu(int value) { setProperty(_layersInBurgerMenu, value, PROPERTY_LAYERS_IN_BURGER_MENU); }

   private boolean _rotateBurgerMenu;
   public boolean isRotateBurgerMenu() { return _rotateBurgerMenu; }
   public void   setRotateBurgerMenu(boolean value) { setProperty(_rotateBurgerMenu, value, PROPERTY_ROTATE_BURGER_MENU); }

   protected RectDouble getBurgerMenuRegion() {
      return new RectDouble(getWidth() /2.0,
                            getHeight()/2.0,
                            getWidth() /2.0 - getPadding().right,
                            getHeight()/2.0 - getPadding().bottom);
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
      RectDouble rc = getBurgerMenuRegion();
      double penWidth = Math.max(1, (horizontal ? rc.height : rc.width) / (2 * layers));
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
