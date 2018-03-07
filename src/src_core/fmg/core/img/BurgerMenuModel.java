package fmg.core.img;

import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import fmg.common.Color;
import fmg.common.HSV;
import fmg.common.geom.Bound;
import fmg.common.geom.PointDouble;
import fmg.common.geom.Rect;
import fmg.common.geom.Size;
import fmg.common.notyfier.NotifyPropertyChanged;

/** MVC: model of representable menu as horizontal or vertical lines */
public class BurgerMenuModel extends NotifyPropertyChanged implements IImageModel {

   /**
    * @param sizeGetter - getSize of another basic model
    */
   protected BurgerMenuModel(Supplier<Size> sizeGetter) {
      _show = true;
      _layers = 3;
      _horizontal = true;
      _rotate = true;

      _sizeGetter = sizeGetter;
   }

   @SuppressWarnings("deprecation")
   protected <TI>boolean setProperty(TI storage, TI value, String propertyName) {
      return super.setProperty(value, propertyName);
   }


   public static final String PROPERTY_SHOW = "Show";
   public static final String PROPERTY_HORIZONTAL = "Horizontal";
   public static final String PROPERTY_LAYERS = "LayersIn";
   public static final String PROPERTY_ROTATE = "Rotate";
   public static final String PROPERTY_PADDING = "Padding";

   private Supplier<Size> _sizeGetter;
   /** image width and height in pixel */
   @Override
   public Size getSize() { return _sizeGetter.get(); }
   @Override
   public void setSize(Size size) { throw new RuntimeException("Illegal call. Must usage another model"); }

   private boolean _show;
   public boolean isShow() { return _show; }
   public void   setShow(boolean value) { setProperty(_show, value, PROPERTY_SHOW); }

   private boolean _horizontal = true;
   public boolean isHorizontal() { return _horizontal; }
   public void   setHorizontal(boolean value) { setProperty(_horizontal, value, PROPERTY_HORIZONTAL); }

   private int   _layers = 3;
   public int  getLayers() { return _layers; }
   public void setLayers(int value) { setProperty(_layers, value, PROPERTY_LAYERS); }

   private boolean _rotate;
   public boolean isRotate() { return _rotate; }
   public void   setRotate(boolean value) { setProperty(_rotate, value, PROPERTY_ROTATE); }

   private Bound _padding;
   public Bound getPadding() {
      if (_padding == null)
         setPadding(new Bound(getSize().width / 2,
                              getSize().height / 2,
                              getPadding().right,
                              getPadding().bottom));
      return _padding;
   }
   public void  setPadding(Bound value) {
      if (value.getLeftAndRight() >= getSize().width)
         throw new IllegalArgumentException("Padding size is very large. Should be less than Width.");
      if (value.getTopAndBottom() >= getSize().height)
         throw new IllegalArgumentException("Padding size is very large. Should be less than Height.");
      setProperty(_padding, value, PROPERTY_PADDING);
   }
   public void resetPadding() {
      if (_padding == null)
         return;
      _padding = null;
   }

   public static class LineInfo {
      public Color clr;
      public double penWidht;
      public PointDouble from; // start coord
      public PointDouble to;   // end   coord
   }

   /** get paint information of drawing burger menu model image */
   public Stream<LineInfo> getCoords(ImageProperties generalModel) {
      if (!isShow())
         return Stream.empty();

      boolean horizontal = isHorizontal();
      int layers = getLayers();
      Bound pad = getPadding();
      Rect rc = new Rect(pad.left,
                         pad.top,
                         getSize().width  - pad.getLeftAndRight(),
                         getSize().height - pad.getTopAndBottom());
      double penWidth = Math.max(1, (horizontal ? rc.height : rc.width) / (2.0 * layers));
      double rotateAngle = isRotate() ? generalModel.getRotateAngle() : 0;
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

}
