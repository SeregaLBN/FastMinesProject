package fmg.core.img;

import java.util.Arrays;
import java.util.List;

import fmg.common.Color;
import fmg.common.geom.PointDouble;
import fmg.common.notyfier.NotifyPropertyChanged;

/** main logos image */
public abstract class Logo<TImage> extends NotifyPropertyChanged {

   public static final int DefaultSize = 200;

   private double _zoom;
   private int _padding;
   private final boolean _useGradient;
   private TImage _img;

   protected Logo(boolean useGradient) {
      _zoom = 1;
      _padding = 3;
      _useGradient = useGradient;
   }

   public final Color[] Palette = {
         new Color(0xFFFF0000), new Color(0xFFFFD800), new Color(0xFF4CFF00), new Color(0xFF00FF90),
         new Color(0xFF0094FF), new Color(0xFF4800FF), new Color(0xFFB200FF), new Color(0xFFFF006E) };

   public static double CalcZoom(int desiredLogoWidhtHeight, int padding) {
      // desiredLogoWidhtHeight = DefaultHeight*zoom+2*padding
      return (desiredLogoWidhtHeight - 2.0 * padding) / DefaultSize;
   }

   public void MixLoopColor(int loop) {
//      Color[] copy = Palette.clone();
//      for (int i = 0; i < Palette.length; i++)
//         Palette[i] = copy[(i + loop) % 8];
   }

   public double getSize() {
      return DefaultSize * getZoom() + 2 * getPadding();
   }

   public double getZoom() {
      return _zoom;
   }

   public void setZoom(double zoom) {
      if (zoom == _zoom)
         return;
      double old = _zoom;
      _zoom = zoom;
      onPropertyChanged(old, zoom, "Zoom");
      disposeImage();
   }

   public int getPadding() {
      return _padding;
   }

   public void setPadding(int padding) {
      if (padding == _padding)
         return;
      double old = _padding;
      _padding = padding;
      onPropertyChanged(old, padding, "Padding");
      disposeImage();
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
      double zoom = getZoom();
      rays.addAll(Arrays.asList(new PointDouble[] { // owner rays points
         new PointDouble(padding + 100.0000*zoom, padding + 200.0000*zoom),
         new PointDouble(padding + 170.7107*zoom, padding +  29.2893*zoom),
         new PointDouble(padding +   0.0000*zoom, padding + 100.0000*zoom),
         new PointDouble(padding + 170.7107*zoom, padding + 170.7107*zoom),
         new PointDouble(padding + 100.0000*zoom, padding +   0.0000*zoom),
         new PointDouble(padding +  29.2893*zoom, padding + 170.7107*zoom),
         new PointDouble(padding + 200.0000*zoom, padding + 100.0000*zoom),
         new PointDouble(padding +  29.2893*zoom, padding +  29.2893*zoom)}));
      inn.addAll(Arrays.asList(new PointDouble[] { // inner  octahedron
         new PointDouble(padding + 100.0346*zoom, padding + 141.4070*zoom),
         new PointDouble(padding + 129.3408*zoom, padding +  70.7320*zoom),
         new PointDouble(padding +  58.5800*zoom, padding + 100.0000*zoom),
         new PointDouble(padding + 129.2500*zoom, padding + 129.2500*zoom),
         new PointDouble(padding +  99.9011*zoom, padding +  58.5377*zoom),
         new PointDouble(padding +  70.7233*zoom, padding + 129.3198*zoom),
         new PointDouble(padding + 141.4167*zoom, padding + 100.0000*zoom),
         new PointDouble(padding +  70.7500*zoom, padding +  70.7500*zoom)}));
      oct.addAll(Arrays.asList(new PointDouble[] { // centra l octahedron
         new PointDouble(padding + 120.7053*zoom, padding + 149.9897*zoom),
         new PointDouble(padding + 120.7269*zoom, padding +  50.0007*zoom),
         new PointDouble(padding +  50.0034*zoom, padding + 120.7137*zoom),
         new PointDouble(padding + 150.0000*zoom, padding + 120.6950*zoom),
         new PointDouble(padding +  79.3120*zoom, padding +  50.0007*zoom),
         new PointDouble(padding +  79.2624*zoom, padding + 149.9727*zoom),
         new PointDouble(padding + 150.0000*zoom, padding +  79.2737*zoom),
         new PointDouble(padding +  50.0034*zoom, padding +  79.3093*zoom)}));
   }

   protected void disposeImage() {
      if (_img instanceof AutoCloseable) {
         AutoCloseable clz = (AutoCloseable)_img;
         try {
            clz.close();
         } catch (Exception ex) {
            ex.printStackTrace(System.err);
         }
      }
      _img = null;
   }

   @Override
   public void close() {
      super.close();
      disposeImage();
   }

}
