package fmg.core.img;

import java.util.function.Supplier;

import fmg.common.geom.Bound;
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

   private boolean _horizontal;
   public boolean isHorizontal() { return _horizontal; }
   public void   setHorizontal(boolean value) { setProperty(_horizontal, value, PROPERTY_HORIZONTAL); }

   private int   _layers;
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

}
