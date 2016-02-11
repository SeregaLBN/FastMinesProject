using System.Linq;
using Windows.UI.Xaml.Media.Imaging;
using fmg.common;
using fmg.common.geom;

namespace fmg.uwp.res.img {

   /// <summary> main logos image </summary>
   public class Logo {
      public const uint DefaultWidht = 200;
      public const uint DefaultHeight = 200;
      private WriteableBitmap _img;

      public Logo() {
         ZoomX = 1;
         ZoomY = 1;
         Margin = 3;
      }

      public double ZoomX { get; set; }
      public double ZoomY { get; set; }
      public uint Margin { get; set; }

      public readonly Color[] Palette = {
         new Color(0xFFFF0000), new Color(0xFFFFD800), new Color(0xFF4CFF00), new Color(0xFF00FF90),
         new Color(0xFF0094FF), new Color(0xFF4800FF), new Color(0xFFB200FF), new Color(0xFFFF006E)
      };

      public static double CalcZoom(int desiredLogoWidhtHeight, uint margin = 3) {
         // desiredLogoWidhtHeight = DefaultHeight*zoom+2*margin
         return (desiredLogoWidhtHeight - 2.0*margin)/DefaultHeight;
      }

      public void MixLoopColor(uint loop) {
         var copy = Palette.ToList();
         for (var i = 0; i < Palette.Length; i++)
            Palette[i] = copy[(int)((i + loop)%8)];
      }

      public WriteableBitmap Image {
         get {
            if (_img != null)
               return _img;

            var size = new Size((int) (DefaultWidht*ZoomX+2*Margin), (int) (DefaultHeight*ZoomY+2*Margin));
            var bmp = BitmapFactory.New(size.width, size.height);

            { // draw star
               var rays = new [] { // owner rays points
                  new PointDouble(Margin+100.0000*ZoomX, Margin+200.0000*ZoomY),
                  new PointDouble(Margin+170.7107*ZoomX, Margin+ 29.2893*ZoomY),
                  new PointDouble(Margin+  0.0000*ZoomX, Margin+100.0000*ZoomY),
                  new PointDouble(Margin+170.7107*ZoomX, Margin+170.7107*ZoomY),
                  new PointDouble(Margin+100.0000*ZoomX, Margin+  0.0000*ZoomY),
                  new PointDouble(Margin+ 29.2893*ZoomX, Margin+170.7107*ZoomY),
                  new PointDouble(Margin+200.0000*ZoomX, Margin+100.0000*ZoomY),
                  new PointDouble(Margin+ 29.2893*ZoomX, Margin+ 29.2893*ZoomY)};
               var inn = new [] { // inner octahedron
                  new PointDouble(Margin+100.0346*ZoomX, Margin+141.4070*ZoomY),
                  new PointDouble(Margin+129.3408*ZoomX, Margin+ 70.7320*ZoomY),
                  new PointDouble(Margin+ 58.5800*ZoomX, Margin+100.0000*ZoomY),
                  new PointDouble(Margin+129.2500*ZoomX, Margin+129.2500*ZoomY),
                  new PointDouble(Margin+ 99.9011*ZoomX, Margin+ 58.5377*ZoomY),
                  new PointDouble(Margin+ 70.7233*ZoomX, Margin+129.3198*ZoomY),
                  new PointDouble(Margin+141.4167*ZoomX, Margin+100.0000*ZoomY),
                  new PointDouble(Margin+ 70.7500*ZoomX, Margin+ 70.7500*ZoomY)};
               var oct = new [] { // central octahedron
                  new PointDouble(Margin+120.7053*ZoomX, Margin+149.9897*ZoomY),
                  new PointDouble(Margin+120.7269*ZoomX, Margin+ 50.0007*ZoomY),
                  new PointDouble(Margin+ 50.0034*ZoomX, Margin+120.7137*ZoomY),
                  new PointDouble(Margin+150.0000*ZoomX, Margin+120.6950*ZoomY),
                  new PointDouble(Margin+ 79.3120*ZoomX, Margin+ 50.0007*ZoomY),
                  new PointDouble(Margin+ 79.2624*ZoomX, Margin+149.9727*ZoomY),
                  new PointDouble(Margin+150.0000*ZoomX, Margin+ 79.2737*ZoomY),
                  new PointDouble(Margin+ 50.0034*ZoomX, Margin+ 79.3093*ZoomY)};

               // paint owner rays
               for (var i=0; i<8; i++) {
                  bmp.FillQuad(
                     (int) rays[i].X, (int) rays[i].Y,
                     (int) oct[i].X, (int) oct[i].Y,
                     (int) inn[i].X, (int) inn[i].Y,
                     (int) oct[(i+5)%8].X, (int) oct[(i+5)%8].Y,
                     Palette[i].Darker().ToWinColor()
                  );
               }

               // paint star perimeter
               for (var i=0; i<8; i++) {
                  var p1 = rays[(i + 7)%8];
                  var p2 = rays[i];
                  bmp.DrawLineAa((int)p1.X, (int)p1.Y, (int)p2.X, (int)p2.Y, Palette[i].ToWinColor());
               }

               // paint inner gradient triangles
               for (var i=0; i<8; i++) {
                  bmp.FillTriangle(
                     (int) inn[(i + 0)%8].X, (int) inn[(i + 0)%8].Y,
                     (int) inn[(i + 3)%8].X, (int) inn[(i + 3)%8].Y,
                     size.width/2, size.height/2,
                     ((i & 1) == 0)
                        ? Palette[(i + 6)%8].Brighter().ToWinColor()
                        : Palette[(i + 6)%8].Darker().ToWinColor());
               }
            }

	         _img = bmp;
            return _img;
         }
      }
   }
}