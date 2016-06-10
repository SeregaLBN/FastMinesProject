using System.Collections.Generic;
using System.Linq;
using fmg.common;
using fmg.common.geom;

namespace fmg.core.img {

   /// <summary> main logos image </summary>
   public abstract class Logo<TImage> {

      public const uint DefaultWidht = 200;
      public const uint DefaultHeight = 200;

      private double _zoomX;
      private double _zoomY;
      private uint _padding;
      private readonly bool _useGradient;
      private TImage _img;
 
      protected Logo(bool useGradient) {
         _zoomX = 1;
         _zoomY = 1;
         _padding = 3;
         _useGradient = useGradient;
      }

      public double ZoomX {
         get { return _zoomX; }
         set {
            _img = default(TImage);
            _zoomX = value;
         }
      }
      public double ZoomY {
         get { return _zoomY; }
         set {
            _img = default(TImage);
            _zoomY = value;
         }
      }
      public uint Padding {
         get { return _padding; }
         set {
            _img = default(TImage);
            _padding = value;
         }
      }

      public readonly Color[] Palette = {
         new Color(0xFFFF0000), new Color(0xFFFFD800), new Color(0xFF4CFF00), new Color(0xFF00FF90),
         new Color(0xFF0094FF), new Color(0xFF4800FF), new Color(0xFFB200FF), new Color(0xFFFF006E)
      };

      public static double CalcZoom(int desiredLogoWidhtHeight, uint Padding = 3) {
         // desiredLogoWidhtHeight = DefaultHeight*zoom+2*Padding
         return (desiredLogoWidhtHeight - 2.0*Padding)/DefaultHeight;
      }

      public void MixLoopColor(uint loop) {
         var copy = Palette.ToList();
         for (var i = 0; i < Palette.Length; i++)
            Palette[i] = copy[(int)((i + loop)%8)];
      }

      public double Width => DefaultWidht * _zoomX + 2 * Padding;
      public double Height => DefaultHeight * _zoomY + 2 * Padding;

      protected abstract TImage CreateImage();
      protected abstract void DrawImage(TImage img);

      public TImage Image {
         get {
            if (_img == null) {
               var bmp = CreateImage();
               DrawImage(bmp);
               _img = bmp;
            }
            return _img;
         }
      }

      protected void GetCoords(out IList<PointDouble> rays, out IList<PointDouble> inn, out IList<PointDouble> oct) {
         uint padding = Padding;
         double zoomX = ZoomX;
         double zoomY = ZoomY;
         rays = new [] { // owner rays points
                  new PointDouble(padding + 100.0000*zoomX, padding + 200.0000*zoomY),
                  new PointDouble(padding + 170.7107*zoomX, padding +  29.2893*zoomY),
                  new PointDouble(padding +   0.0000*zoomX, padding + 100.0000*zoomY),
                  new PointDouble(padding + 170.7107*zoomX, padding + 170.7107*zoomY),
                  new PointDouble(padding + 100.0000*zoomX, padding +   0.0000*zoomY),
                  new PointDouble(padding +  29.2893*zoomX, padding + 170.7107*zoomY),
                  new PointDouble(padding + 200.0000*zoomX, padding + 100.0000*zoomY),
                  new PointDouble(padding +  29.2893*zoomX, padding +  29.2893*zoomY)};
         inn = new [] { // inner octahedron
                  new PointDouble(padding + 100.0346*zoomX, padding + 141.4070*zoomY),
                  new PointDouble(padding + 129.3408*zoomX, padding +  70.7320*zoomY),
                  new PointDouble(padding +  58.5800*zoomX, padding + 100.0000*zoomY),
                  new PointDouble(padding + 129.2500*zoomX, padding + 129.2500*zoomY),
                  new PointDouble(padding +  99.9011*zoomX, padding +  58.5377*zoomY),
                  new PointDouble(padding +  70.7233*zoomX, padding + 129.3198*zoomY),
                  new PointDouble(padding + 141.4167*zoomX, padding + 100.0000*zoomY),
                  new PointDouble(padding +  70.7500*zoomX, padding +  70.7500*zoomY)};
         oct = new [] { // central octahedron
                  new PointDouble(padding + 120.7053*zoomX, padding + 149.9897*zoomY),
                  new PointDouble(padding + 120.7269*zoomX, padding +  50.0007*zoomY),
                  new PointDouble(padding +  50.0034*zoomX, padding + 120.7137*zoomY),
                  new PointDouble(padding + 150.0000*zoomX, padding + 120.6950*zoomY),
                  new PointDouble(padding +  79.3120*zoomX, padding +  50.0007*zoomY),
                  new PointDouble(padding +  79.2624*zoomX, padding + 149.9727*zoomY),
                  new PointDouble(padding + 150.0000*zoomX, padding +  79.2737*zoomY),
                  new PointDouble(padding +  50.0034*zoomX, padding +  79.3093*zoomY)};
      }

   }

}
