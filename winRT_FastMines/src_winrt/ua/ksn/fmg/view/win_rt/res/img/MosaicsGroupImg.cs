using System;
using Windows.Foundation;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Media.Imaging;
using ua.ksn.fmg.model.mosaics;

namespace ua.ksn.fmg.view.win_rt.res.img {
   public delegate void ImageChanged(WriteableBitmap newImg, EMosaicGroup eMosaicGroup);

   public class MosaicsGroupImg {
      private static readonly double W = Window.Current.Bounds.Width / 3;
      private static readonly double H = Window.Current.Bounds.Height / 5;
      private static readonly double dx = W / 10; // 10%
      private static readonly double dy = H / 10; // 10%
      private static readonly double offsetX = W * 1 / 100; // 3%
      private static readonly double offsetY = H * 1 / 100; // 3%

      private readonly EMosaicGroup _eMosaicGroup;
      private readonly Point[] _points;
      private readonly Rect[] _limits;
      private readonly Random _random = new Random();
      public WriteableBitmap Image { get; private set; }
      public bool Loop { get; set; }
      private double _rotate;

      public ImageChanged OnImageChanged = delegate { };

      public MosaicsGroupImg(EMosaicGroup group, bool loop) {
         _eMosaicGroup = group;
         Loop = loop;

         _points = new[] {new Point(dx, H - dy), new Point(W/2, dy), new Point(W - dx, H - dy)};
         _limits = new[] {
            new Rect(new Point(0, H/2+dy), new Point(W/2-dx, H)),
            new Rect(new Point(W/2-2*dx, 0), new Point(W/2+2*dx, H/2-dy)),
            new Rect(new Point(W/2+dx, H/2+dy), new Point(W, H))
         };

         NextIteration();
      }

      private void NextIteration() {
         var bmp = new WriteableBitmap((int)W, (int)H);

         bmp.FillPolygon(new[] { 0, 0, (int)W, 0, (int)W, (int)H, 0, (int)H, 0, 0 }, Windows.UI.Color.FromArgb(0xFF, 0xff, 0x8c, 0x00));

         var i = _random.Next() % _eMosaicGroup.VertexCount();
         if (((_random.Next()%2) == 1)) {
            _points[i].X += ((_random.Next()%2) == 1) ? offsetX : -offsetX;
            _points[i].X = Math.Min(Math.Max(_points[i].X, _limits[i].Left), _limits[i].Right);
         } else {
            _points[i].Y += ((_random.Next()%2) == 1) ? offsetY : -offsetY;
            _points[i].Y = Math.Min(Math.Max(_points[i].Y, _limits[i].Top), _limits[i].Bottom);
         }

         bmp.FillTriangle((int)_points[0].X, (int)_points[0].Y, (int)_points[1].X, (int)_points[1].Y,
            (int) _points[2].X, (int) _points[2].Y, Windows.UI.Colors.Red);

#if DEBUG
         var clrBk = Windows.UI.Color.FromArgb(128, 0x0F, 0xFF, 0x00);
         foreach (var rc in _limits)
            bmp.DrawRectangle((int)rc.Left, (int)rc.Top, (int)rc.Right, (int)rc.Bottom, clrBk);
#endif

         bmp = bmp.RotateFree(_rotate);
         _rotate += .4;
         if (_rotate > 360)
            _rotate -= 360;

         Image = bmp;
         OnImageChanged(bmp, _eMosaicGroup);

         if (!Loop)
            return;

         var timer = new DispatcherTimer { Interval = TimeSpan.FromMilliseconds(100) };
         timer.Tick += delegate { timer.Stop(); NextIteration(); };
         timer.Start();
      }

   }
}