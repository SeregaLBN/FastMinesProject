package fmg.core.img;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import fmg.common.Color;
import fmg.common.HSV;
import fmg.common.geom.PointDouble;

/** MVC: model for FastMines logo image */
public class LogoModel extends AnimatedImageModel {

   public enum ERotateMode {
      /** rotate image */
      classic,

      /** rotate color Palette */
      color,

      /** {@link #color} + {@link #classic} */
      combi
   }

   private final HSV[] _palette = { new HSV(  0, 100, 100), new HSV( 45, 100, 100), new HSV( 90, 100, 100), new HSV(135, 100, 100),
                                    new HSV(180, 100, 100), new HSV(225, 100, 100), new HSV(270, 100, 100), new HSV(315, 100, 100) };
   private boolean _useGradient;
   private ERotateMode _rotateMode = ERotateMode.combi;
   /** owner rays points */
   private final List<PointDouble> _rays = new ArrayList<>();
   /** inner octahedron */
   private final List<PointDouble> _inn = new ArrayList<>();
   /** central octahedron */
   private final List<PointDouble> _oct = new ArrayList<>();

   private final PropertyChangeListener _selfListener = ev -> onPropertyChanged(ev.getOldValue(), ev.getNewValue(), ev.getPropertyName());

   public static final String PROPERTY_USE_GRADIENT = "UseGradient";
   public static final String PROPERTY_ROTATE_MODE  = "RotateMode";


   public LogoModel() {
      setBackgroundColor(Color.Transparent());
      _notifier.addListener(_selfListener);
   }


   public HSV[] getPalette() {
      return _palette;
   }

   public static void toMineModel(LogoModel m) {
      m.setUseGradient(false);
      for (HSV item : m.getPalette())
         //item.v = 75;
         item.grayscale();
   }

   public boolean isUseGradient() { return _useGradient; }
   public void setUseGradient(boolean value) {
      _notifier.setProperty(_useGradient, value, PROPERTY_USE_GRADIENT);
   }

   public ERotateMode getRotateMode() { return _rotateMode; }
   public void setRotateMode(ERotateMode value) {
      _notifier.setProperty(_rotateMode, value, PROPERTY_ROTATE_MODE);
   }

   public double getZoomX() { return (getSize().width  - getPadding().getLeftAndRight()) / 200.0; }
   public double getZoomY() { return (getSize().height - getPadding().getTopAndBottom()) / 200.0; }

   public List<PointDouble> getRays() {
      if (_rays.isEmpty()) {
         double pl = getPadding().left;
         double pt = getPadding().top;
         double zx = getZoomX();
         double zy = getZoomY();

         _rays.add(new PointDouble(pl + 100.0000*zx, pt + 200.0000*zy));
         _rays.add(new PointDouble(pl + 170.7107*zx, pt +  29.2893*zy));
         _rays.add(new PointDouble(pl +   0.0000*zx, pt + 100.0000*zy));
         _rays.add(new PointDouble(pl + 170.7107*zx, pt + 170.7107*zy));
         _rays.add(new PointDouble(pl + 100.0000*zx, pt +   0.0000*zy));
         _rays.add(new PointDouble(pl +  29.2893*zx, pt + 170.7107*zy));
         _rays.add(new PointDouble(pl + 200.0000*zx, pt + 100.0000*zy));
         _rays.add(new PointDouble(pl +  29.2893*zx, pt +  29.2893*zy));
      }
      return _rays;
   }

   public List<PointDouble> getInn() {
      if (_inn.isEmpty()) {
         double pl = getPadding().left;
         double pt = getPadding().top;
         double zx = getZoomX();
         double zy = getZoomY();

         _inn.add(new PointDouble(pl + 100.0346*zx, pt + 141.4070*zy));
         _inn.add(new PointDouble(pl + 129.3408*zx, pt +  70.7320*zy));
         _inn.add(new PointDouble(pl +  58.5800*zx, pt + 100.0000*zy));
         _inn.add(new PointDouble(pl + 129.2500*zx, pt + 129.2500*zy));
         _inn.add(new PointDouble(pl +  99.9011*zx, pt +  58.5377*zy));
         _inn.add(new PointDouble(pl +  70.7233*zx, pt + 129.3198*zy));
         _inn.add(new PointDouble(pl + 141.4167*zx, pt + 100.0000*zy));
         _inn.add(new PointDouble(pl +  70.7500*zx, pt +  70.7500*zy));
      }
      return _inn;
   }

   public List<PointDouble> getOct() {
      if (_oct.isEmpty()) {
         double pl = getPadding().left;
         double pt = getPadding().top;
         double zx = getZoomX();
         double zy = getZoomY();

         _oct.add(new PointDouble(pl + 120.7053*zx, pt + 149.9897*zy));
         _oct.add(new PointDouble(pl + 120.7269*zx, pt +  50.0007*zy));
         _oct.add(new PointDouble(pl +  50.0034*zx, pt + 120.7137*zy));
         _oct.add(new PointDouble(pl + 150.0000*zx, pt + 120.6950*zy));
         _oct.add(new PointDouble(pl +  79.3120*zx, pt +  50.0007*zy));
         _oct.add(new PointDouble(pl +  79.2624*zx, pt + 149.9727*zy));
         _oct.add(new PointDouble(pl + 150.0000*zx, pt +  79.2737*zy));
         _oct.add(new PointDouble(pl +  50.0034*zx, pt +  79.3093*zy));
      }
      return _oct;
   }

   protected void onPropertyChanged(Object oldValue, Object newValue, String propertyName) {
      switch (propertyName) {
      case PROPERTY_SIZE:
      case PROPERTY_PADDING:
         _rays.clear();
         _inn.clear();
         _oct.clear();
         break;
      }
   }

   @Override
   public void close() {
      _notifier.removeListener(_selfListener);
      super.close();
   }

}
