package fmg.core.img;

import java.util.List;

import fmg.common.Color;
import fmg.common.HSV;
import fmg.common.geom.PointDouble;
import fmg.common.geom.util.FigureHelper;

/** Abstract main logos image */
public abstract class ALogo<TImage> extends PolarLightsImg<Object, TImage> {

   public enum ERotateMode {
      /** rotate image */
      classic,

      /** rotate color Palette */
      color,

      /** {@link #color} + {@link #classic} */
      combi
   }

   protected ALogo() {
      super(0);
      setBackgroundColor(Color.Transparent);
   }

   public final HSV[] Palette = {
         new HSV(  0, 100, 100), new HSV( 45, 100, 100), new HSV( 90, 100, 100), new HSV(135, 100, 100),
         new HSV(180, 100, 100), new HSV(225, 100, 100), new HSV(270, 100, 100), new HSV(315, 100, 100) };

   private boolean _useGradient;
   public boolean isUseGradient() { return _useGradient; }
   public void setUseGradient(boolean value) {
      if (setProperty(_useGradient, value, "UseGradient"))
         invalidate();
   }

   private ERotateMode _rotateMode = ERotateMode.combi;
   public ERotateMode getRotateMode() { return _rotateMode; }
   public void setRotateMode(ERotateMode value) { setProperty(_rotateMode, value, "RotateMode"); }

   protected double getZoomX() { return (getWidth()  - getPadding().getLeftAndRight()) / 200.0; }
   protected double getZoomY() { return (getHeight() - getPadding().getTopAndBottom()) / 200.0; }

   protected void getCoords(List<PointDouble> rays, List<PointDouble> inn, List<PointDouble> oct) {
      int pl = getPadding().left;
      int pt = getPadding().top;
      double zx = getZoomX();
      double zy = getZoomY();
      PointDouble center = new PointDouble(getWidth()/2.0, getHeight()/2.0);
      PointDouble none = new PointDouble();

      rays.clear();
      rays.add(new PointDouble(pl + 100.0000*zx, pt + 200.0000*zy));
      rays.add(new PointDouble(pl + 170.7107*zx, pt +  29.2893*zy));
      rays.add(new PointDouble(pl +   0.0000*zx, pt + 100.0000*zy));
      rays.add(new PointDouble(pl + 170.7107*zx, pt + 170.7107*zy));
      rays.add(new PointDouble(pl + 100.0000*zx, pt +   0.0000*zy));
      rays.add(new PointDouble(pl +  29.2893*zx, pt + 170.7107*zy));
      rays.add(new PointDouble(pl + 200.0000*zx, pt + 100.0000*zy));
      rays.add(new PointDouble(pl +  29.2893*zx, pt +  29.2893*zy));

      inn.clear();
      inn.add(new PointDouble(pl + 100.0346*zx, pt + 141.4070*zy));
      inn.add(new PointDouble(pl + 129.3408*zx, pt +  70.7320*zy));
      inn.add(new PointDouble(pl +  58.5800*zx, pt + 100.0000*zy));
      inn.add(new PointDouble(pl + 129.2500*zx, pt + 129.2500*zy));
      inn.add(new PointDouble(pl +  99.9011*zx, pt +  58.5377*zy));
      inn.add(new PointDouble(pl +  70.7233*zx, pt + 129.3198*zy));
      inn.add(new PointDouble(pl + 141.4167*zx, pt + 100.0000*zy));
      inn.add(new PointDouble(pl +  70.7500*zx, pt +  70.7500*zy));

      oct.clear();
      oct.add(new PointDouble(pl + 120.7053*zx, pt + 149.9897*zy));
      oct.add(new PointDouble(pl + 120.7269*zx, pt +  50.0007*zy));
      oct.add(new PointDouble(pl +  50.0034*zx, pt + 120.7137*zy));
      oct.add(new PointDouble(pl + 150.0000*zx, pt + 120.6950*zy));
      oct.add(new PointDouble(pl +  79.3120*zx, pt +  50.0007*zy));
      oct.add(new PointDouble(pl +  79.2624*zx, pt + 149.9727*zy));
      oct.add(new PointDouble(pl + 150.0000*zx, pt +  79.2737*zy));
      oct.add(new PointDouble(pl +  50.0034*zx, pt +  79.3093*zy));

      if (getRotateMode() != ERotateMode.color) {
         double ra = getRotateAngle();
         FigureHelper.rotate(rays, ra, center, none);
         FigureHelper.rotate(inn, ra, center, none);
         FigureHelper.rotate(oct, ra, center, none);
      }
   }

   @Override
   protected void onPropertyChanged(Object oldValue, Object newValue, String propertyName) {
      if ((getRotateMode() != ERotateMode.classic) && "RotateAngle".equals(propertyName)) {
         double delta = getRotateAngleDelta();
         for (int i=0; i<Palette.length; ++i) {
            Palette[i].h += delta;
         }

      }
      super.onPropertyChanged(oldValue, newValue, propertyName);
   }

}
