package fmg.core.img;

import java.util.Arrays;
import java.util.List;

import fmg.common.Color;
import fmg.common.geom.PointDouble;

/** main logos image */
public abstract class Logo<TImage> {
   public static final int DefaultWidht = 200;
   public static final int DefaultHeight = 200;

   private double _zoomX;
   private double _zoomY;
   private int _padding;
   private final boolean _useGradient;
   private TImage _img;

   protected Logo(boolean useGradient) {
      _zoomX = 1;
      _zoomY = 1;
      _padding = 3;
      _useGradient = useGradient;
   }

   public final Color[] Palette = {
         new Color(0xFFFF0000), new Color(0xFFFFD800), new Color(0xFF4CFF00), new Color(0xFF00FF90),
         new Color(0xFF0094FF), new Color(0xFF4800FF), new Color(0xFFB200FF), new Color(0xFFFF006E) };

   public static double CalcZoom(int desiredLogoWidhtHeight, int padding) {
      // desiredLogoWidhtHeight = DefaultHeight*zoom+2*padding
      return (desiredLogoWidhtHeight - 2.0 * padding) / DefaultHeight;
   }

   public void MixLoopColor(int loop) {
//      Color[] copy = Palette.clone();
//      for (int i = 0; i < Palette.length; i++)
//         Palette[i] = copy[(i + loop) % 8];
   }

   public double getWidth() {
      return DefaultWidht * getZoomX() + 2 * getPadding();
   }

   public double getHeight() {
      return DefaultHeight * getZoomY() + 2 * getPadding();
   }

   public double getZoomX() {
      return _zoomX;
   }

   public void setZoomX(double zoomX) {
      _img = null;
      _zoomX = zoomX;
   }

   public double getZoomY() {
      return _zoomY;
   }

   public void setZoomY(double zoomY) {
      _img = null;
      _zoomY = zoomY;
   }

   public int getPadding() {
      return _padding;
   }

   public void setPadding(int padding) {
      _img = null;
      _padding = padding;
   }

   public boolean isUseGradient() {
      return _useGradient;
   }

   protected abstract TImage createImage();
   protected abstract void drawImage(TImage img);

   public TImage getImage() {
      if (_img == null) {
         TImage bmp = createImage();
         drawImage(bmp);
         _img = bmp;
      }
      return _img;
   }

   protected void getCoords(List<PointDouble> rays, List<PointDouble> inn, List<PointDouble> oct) {
      int padding = getPadding();
      double zoomX = getZoomX();
      double zoomY = getZoomY();
      rays.addAll(Arrays.asList(new PointDouble[] { // owner rays points
         new PointDouble(padding + 100.0000*zoomX, padding + 200.0000*zoomY),
         new PointDouble(padding + 170.7107*zoomX, padding +  29.2893*zoomY),
         new PointDouble(padding +   0.0000*zoomX, padding + 100.0000*zoomY),
         new PointDouble(padding + 170.7107*zoomX, padding + 170.7107*zoomY),
         new PointDouble(padding + 100.0000*zoomX, padding +   0.0000*zoomY),
         new PointDouble(padding +  29.2893*zoomX, padding + 170.7107*zoomY),
         new PointDouble(padding + 200.0000*zoomX, padding + 100.0000*zoomY),
         new PointDouble(padding +  29.2893*zoomX, padding +  29.2893*zoomY)}));
      inn.addAll(Arrays.asList(new PointDouble[] { // inner  octahedron
         new PointDouble(padding + 100.0346*zoomX, padding + 141.4070*zoomY),
         new PointDouble(padding + 129.3408*zoomX, padding +  70.7320*zoomY),
         new PointDouble(padding +  58.5800*zoomX, padding + 100.0000*zoomY),
         new PointDouble(padding + 129.2500*zoomX, padding + 129.2500*zoomY),
         new PointDouble(padding +  99.9011*zoomX, padding +  58.5377*zoomY),
         new PointDouble(padding +  70.7233*zoomX, padding + 129.3198*zoomY),
         new PointDouble(padding + 141.4167*zoomX, padding + 100.0000*zoomY),
         new PointDouble(padding +  70.7500*zoomX, padding +  70.7500*zoomY)}));
      oct.addAll(Arrays.asList(new PointDouble[] { // centra l octahedron
         new PointDouble(padding + 120.7053*zoomX, padding + 149.9897*zoomY),
         new PointDouble(padding + 120.7269*zoomX, padding +  50.0007*zoomY),
         new PointDouble(padding +  50.0034*zoomX, padding + 120.7137*zoomY),
         new PointDouble(padding + 150.0000*zoomX, padding + 120.6950*zoomY),
         new PointDouble(padding +  79.3120*zoomX, padding +  50.0007*zoomY),
         new PointDouble(padding +  79.2624*zoomX, padding + 149.9727*zoomY),
         new PointDouble(padding + 150.0000*zoomX, padding +  79.2737*zoomY),
         new PointDouble(padding +  50.0034*zoomX, padding +  79.3093*zoomY)}));
   }

}
