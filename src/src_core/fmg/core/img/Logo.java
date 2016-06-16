package fmg.core.img;

import java.util.List;

import fmg.common.Color;
import fmg.common.geom.PointDouble;
import fmg.common.geom.util.FigureHelper;

/** main logos image */
public abstract class Logo<TImage> extends PolarLightsImg<Object, TImage> {

   private boolean _useGradient;

   protected Logo(boolean useGradient) { super(1); _useGradient = useGradient; }
   protected Logo(boolean useGradient, int widthAndHeight) { super(2, widthAndHeight); _useGradient = useGradient; }
   protected Logo(boolean useGradient, int widthAndHeight, Integer padding) { super(3, widthAndHeight, padding); _useGradient = useGradient; }

   public final Color[] Palette = {
         new Color(0xFFFF0000), new Color(0xFFFFD800), new Color(0xFF4CFF00), new Color(0xFF00FF90),
         new Color(0xFF0094FF), new Color(0xFF4800FF), new Color(0xFFB200FF), new Color(0xFFFF006E) };

   public boolean isUseGradient() {
      return _useGradient;
   }

   public void setUseGradient(boolean value) {
      if (setProperty(_useGradient, value, "UseGradient"))
         invalidate();
   }

   protected double getZoomX() { return (getWidth()  - getPadding().getLeftAndRight()) / 200.0; }
   protected double getZoomY() { return (getHeight() - getPadding().getTopAndBottom()) / 200.0; }

   protected void getCoords(List<PointDouble> rays, List<PointDouble> inn, List<PointDouble> oct) {
      int pl = getPadding().left;
      int pt = getPadding().top;
      double zx = getZoomX();
      double zy = getZoomX();
      PointDouble center = new PointDouble(getWidth()/2.0, getHeight()/2.0);

      rays.clear();
      rays.add(new PointDouble(pl + 100.0000*zx, pt + 200.0000*zy));
      rays.add(new PointDouble(pl + 170.7107*zx, pt +  29.2893*zy));
      rays.add(new PointDouble(pl +   0.0000*zx, pt + 100.0000*zy));
      rays.add(new PointDouble(pl + 170.7107*zx, pt + 170.7107*zy));
      rays.add(new PointDouble(pl + 100.0000*zx, pt +   0.0000*zy));
      rays.add(new PointDouble(pl +  29.2893*zx, pt + 170.7107*zy));
      rays.add(new PointDouble(pl + 200.0000*zx, pt + 100.0000*zy));
      rays.add(new PointDouble(pl +  29.2893*zx, pt +  29.2893*zy));
      FigureHelper.rotate(rays, getRotateAngle(), center);

      inn.clear();
      inn.add(new PointDouble(pl + 100.0346*zx, pt + 141.4070*zy));
      inn.add(new PointDouble(pl + 129.3408*zx, pt +  70.7320*zy));
      inn.add(new PointDouble(pl +  58.5800*zx, pt + 100.0000*zy));
      inn.add(new PointDouble(pl + 129.2500*zx, pt + 129.2500*zy));
      inn.add(new PointDouble(pl +  99.9011*zx, pt +  58.5377*zy));
      inn.add(new PointDouble(pl +  70.7233*zx, pt + 129.3198*zy));
      inn.add(new PointDouble(pl + 141.4167*zx, pt + 100.0000*zy));
      inn.add(new PointDouble(pl +  70.7500*zx, pt +  70.7500*zy));
      FigureHelper.rotate(inn, getRotateAngle(), center);

      oct.clear();
      oct.add(new PointDouble(pl + 120.7053*zx, pt + 149.9897*zy));
      oct.add(new PointDouble(pl + 120.7269*zx, pt +  50.0007*zy));
      oct.add(new PointDouble(pl +  50.0034*zx, pt + 120.7137*zy));
      oct.add(new PointDouble(pl + 150.0000*zx, pt + 120.6950*zy));
      oct.add(new PointDouble(pl +  79.3120*zx, pt +  50.0007*zy));
      oct.add(new PointDouble(pl +  79.2624*zx, pt + 149.9727*zy));
      oct.add(new PointDouble(pl + 150.0000*zx, pt +  79.2737*zy));
      oct.add(new PointDouble(pl +  50.0034*zx, pt +  79.3093*zy));
      FigureHelper.rotate(oct, getRotateAngle(), center);
   }

}
