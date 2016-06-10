using System;
using System.Linq;
using System.Collections.Generic;
using fmg.common;
using fmg.common.geom;
using fmg.common.notyfier;

namespace fmg.core.img {

   /// <summary> main logos image </summary>
   public abstract class Logo<TImage> : NotifyPropertyChanged {

      public const uint DefaultSize = 200;

      private double _zoom;
      private uint _padding;
      private TImage _img;
 
      protected Logo(bool useGradient) {
         _zoom = 1;
         _padding = 3;
         UseGradient = useGradient;
      }

      public double Zoom {
         get { return _zoom; }
         set {
            if (SetProperty(ref _zoom, value))
               DisposeImage();
         }
      }

      public uint Padding {
         get { return _padding; }
         set {
            if (SetProperty(ref _padding, value))
               DisposeImage();
         }
      }

      public bool UseGradient { get; }

      public readonly Color[] Palette = {
         new Color(0xFFFF0000), new Color(0xFFFFD800), new Color(0xFF4CFF00), new Color(0xFF00FF90),
         new Color(0xFF0094FF), new Color(0xFF4800FF), new Color(0xFFB200FF), new Color(0xFFFF006E)
      };

      public static double CalcZoom(int desiredLogoWidhtHeight, uint padding = 3) {
         // desiredLogoWidhtHeight = DefaultHeight*zoom+2*Padding
         return (desiredLogoWidhtHeight - 2.0*padding)/DefaultSize;
      }

      public void MixLoopColor(uint loop) {
         var copy = Palette.ToList();
         for (var i = 0; i < Palette.Length; i++)
            Palette[i] = copy[(int)((i + loop)%8)];
      }

      public double Size => DefaultSize * _zoom + 2 * Padding;

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
         var padding = Padding;
         var zoom = Zoom;
         rays = new [] { // owner rays points
                  new PointDouble(padding + 100.0000*zoom, padding + 200.0000*zoom),
                  new PointDouble(padding + 170.7107*zoom, padding +  29.2893*zoom),
                  new PointDouble(padding +   0.0000*zoom, padding + 100.0000*zoom),
                  new PointDouble(padding + 170.7107*zoom, padding + 170.7107*zoom),
                  new PointDouble(padding + 100.0000*zoom, padding +   0.0000*zoom),
                  new PointDouble(padding +  29.2893*zoom, padding + 170.7107*zoom),
                  new PointDouble(padding + 200.0000*zoom, padding + 100.0000*zoom),
                  new PointDouble(padding +  29.2893*zoom, padding +  29.2893*zoom)};
         inn = new [] { // inner octahedron
                  new PointDouble(padding + 100.0346*zoom, padding + 141.4070*zoom),
                  new PointDouble(padding + 129.3408*zoom, padding +  70.7320*zoom),
                  new PointDouble(padding +  58.5800*zoom, padding + 100.0000*zoom),
                  new PointDouble(padding + 129.2500*zoom, padding + 129.2500*zoom),
                  new PointDouble(padding +  99.9011*zoom, padding +  58.5377*zoom),
                  new PointDouble(padding +  70.7233*zoom, padding + 129.3198*zoom),
                  new PointDouble(padding + 141.4167*zoom, padding + 100.0000*zoom),
                  new PointDouble(padding +  70.7500*zoom, padding +  70.7500*zoom)};
         oct = new [] { // central octahedron
                  new PointDouble(padding + 120.7053*zoom, padding + 149.9897*zoom),
                  new PointDouble(padding + 120.7269*zoom, padding +  50.0007*zoom),
                  new PointDouble(padding +  50.0034*zoom, padding + 120.7137*zoom),
                  new PointDouble(padding + 150.0000*zoom, padding + 120.6950*zoom),
                  new PointDouble(padding +  79.3120*zoom, padding +  50.0007*zoom),
                  new PointDouble(padding +  79.2624*zoom, padding + 149.9727*zoom),
                  new PointDouble(padding + 150.0000*zoom, padding +  79.2737*zoom),
                  new PointDouble(padding +  50.0034*zoom, padding +  79.3093*zoom)};
      }

      protected virtual void DisposeImage() {
         (_img as IDisposable)?.Dispose();
         _img = default(TImage);
      }

      protected override void Dispose(bool disposing) {
         if (Disposed)
            return;
         base.Dispose(disposing);

         DisposeImage();
      }

   }

}
