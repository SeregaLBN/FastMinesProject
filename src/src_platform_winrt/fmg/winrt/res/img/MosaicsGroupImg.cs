using System;
using System.Collections.Generic;
using Windows.UI.Core;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Media.Imaging;
using fmg.core.types;
using FastMines.Common;
using fmg.common;
using fmg.common.geom;
using Point = Windows.Foundation.Point;
using Rect = Windows.Foundation.Rect;

namespace fmg.winrt.res.img {

   public class MosaicsGroupImg : IDisposable {
      internal struct Color16 {
         public const UInt16 MaxColorValue = 0xFFFF;
         public UInt16 R, G, B;
      }
      private static readonly double W = Window.Current.Bounds.Width / 3;
      private static readonly double H = Window.Current.Bounds.Height / 5;
      private static readonly double Dx = W / 10; // 10%
      private static readonly double Dy = H / 10; // 10%
      private static readonly double OffsetX = W * 1 / 100; // 1%
      private static readonly double OffsetY = H * 1 / 100; // 1%

      private readonly EMosaicGroup _eMosaicGroup;
      private readonly Point[] _points;
      private readonly Rect[] _limits;
      private readonly Random _random = new Random();
      public WriteableBitmap Image { get; private set; }
      public bool Animate { get; set; }
      public bool Dance { get; set; }
      private DispatcherTimer _timer;
      //private double _rotate;
      private Color16 _fillColor;

      /// <summary> ËÒÔÓÎ¸ÁÓ‚‡Ú¸ ‰Îˇ ÏË„‡¯ÍË Ó‰ËÌ Í‡Ì‡Î (R ËÎË G ËÎË B), ËÎË ‚ÒÂ (Ë R Ë G Ë B) </summary>
      private const bool Any—hannel = true;
      private int _channel;

      public MosaicsGroupImg(EMosaicGroup group, bool animate) {
         _eMosaicGroup = group;
         Animate = animate;
         _fillColor = new Color16 {
            R = Convert.ToUInt16(_random.Next(Color16.MaxColorValue)),
            G = Convert.ToUInt16(_random.Next(Color16.MaxColorValue)),
            B = Convert.ToUInt16(_random.Next(Color16.MaxColorValue))
         };
         if (Any—hannel)
            _channel = Next—hannel();

      #region Make Coords
         switch (_eMosaicGroup) {
            case EMosaicGroup.eTriangles:
               _points = new[] {new Point(Dx, H - Dy), new Point(W/2, Dy), new Point(W - Dx, H - Dy)};
               _limits = new[] {
                  new Rect(new Point(0, H/2 + Dy), new Point(W/2 - Dx, H)),
                  new Rect(new Point(W/2 - 2*Dx, 0), new Point(W/2 + 2*Dx, H/2 - Dy)),
                  new Rect(new Point(W/2 + Dx, H/2 + Dy), new Point(W, H))
               };
               break;
            case EMosaicGroup.eQuadrangles:
               _points = new[] {new Point(Dx, Dy), new Point(W - Dx, Dy), new Point(W - Dx, H - Dy), new Point(Dx, H - Dy)};
               _limits = new[] {
                  new Rect(new Point(0, 0), new Point(W/2 - Dx, H/2 - Dy)),
                  new Rect(new Point(W/2 + Dx, 0), new Point(W, H/2 - Dy)),
                  new Rect(new Point(W/2 + Dx, H/2 + Dy), new Point(W, H)),
                  new Rect(new Point(0, H/2 + Dy), new Point(W/2 - Dx, H))
               };
               break;
            case EMosaicGroup.ePentagons:
               // approximately
            #region http://upload.wikimedia.org/wikipedia/commons/thumb/5/55/Regular_pentagon_1.svg/220px-Regular_pentagon_1.svg.png
               _points = new[] {
                  new Point(W/2, H/20),
                  new Point(
                     W*.9272, // 204*100/220 = 2040/22 ~= 92.72
                     H/2.75), // 80*100/220 = 800/22  = 36.36363636363636  = 100 * 0.3636363636363636 =   100 * 1/2.75
                  new Point(
                     W*.7636, // 168*100/220 = 1680/22  ~= 76.36
                     H*.8636), // 190*100/220 = 1900/22 ~= 86.36
                  new Point(
                     W*.2363, // 52*100/220 = 520/22 ~= 23.63
                     H*.8636),
                  new Point(
                     W*.0727, // 16*100/220 = 160/22 ~= 7.27
                     H/2.75)
               };
               _limits = new[] {
                  new Rect(new Point(
                        W*.341, // 75*100/220 ~= 34.1
                        0),
                     new Point(
                        W*(1-0.341),
                        H*.318)), // 70*100/220 ~= 31.8
                  new Rect(new Point(
                        W*.672, // 149*100/220 ~= 67.2
                        H*.2727), // 60*100/220 ~= 27.27
                     new Point(
                        W,
                        H*.5909)), // 130*100/220 ~= 59.09
                  new Rect(new Point(
                        W*.5681, // 125*100/220 ~= 56.81
                        H*.6454), // 142*100/220 ~= 64.54
                     new Point(
                        W*.8863, // 195*100/220 ~= 88.63
                        H*.9636)), // 212*100/220 ~= 96.36
                  new Rect(new Point(
                        W*.1136, // 25*100/220 ~= 11.36
                        H*.6454), // 142*100/220 ~= 64.54
                     new Point(
                        W*.4318, // 95*100/220 ~= 43.18
                        H*.9636)), // 212*100/220 ~= 96.36
                  new Rect(new Point(
                        0,
                        H*.2727), // 60*100/220 ~= 27.27
                     new Point(
                        W*.3181, // 70*100/220 ~= 31.81
                        H*.5909)) // 130*100/220 ~= 59.09
               };
            #endregion
               break;
            case EMosaicGroup.eHexagons:
               // approximately
            #region http://upload.wikimedia.org/wikipedia/commons/thumb/b/b7/Regular_hexagon_1.svg/600px-Regular_hexagon_1.svg.png
               _points = new[] {
                  new Point(W/2, H/30),
                  new Point(W*530/600, H*165/600),
                  new Point(W*530/600, H*435/600),
                  new Point(W/2, H-H/30),
                  new Point(W*65/600, H*435/600),
                  new Point(W*65/600, H*165/600)
               };
               _limits = new[] {
                  new Rect(new Point(W*220/600, H*  0/600), new Point(W*380/600, H*160/600)),
                  new Rect(new Point(W*440/600, H*100/600), new Point(W*600/600, H*260/600)),
                  new Rect(new Point(W*440/600, H*340/600), new Point(W*600/600, H*500/600)),
                  new Rect(new Point(W*220/600, H*440/600), new Point(W*380/600, H*600/600)),
                  new Rect(new Point(W*  0/600, H*340/600), new Point(W*160/600, H*500/600)),
                  new Rect(new Point(W*  0/600, H*100/600), new Point(W*160/600, H*260/600))
               };
            #endregion
               break;
            case EMosaicGroup.eOthers:
               // approximately
            #region
               _points = new[] {
                  new Point(W*306/800, H* 63/800),
                  new Point(W*490/800, H*244/800),
                  new Point(W*737/800, H*310/800),
                  new Point(W*557/800, H*491/800),
                  new Point(W*490/800, H*737/800),
                  new Point(W*308/800, H*558/800),
                  new Point(W* 63/800, H*491/800),
                  new Point(W*243/800, H*310/800)};
               _limits = new[] {
                  new Rect(new Point(W*180/800, H*  0/800), new Point(W*380/800, H*200/800)),
                  new Rect(new Point(W*392/800, H*145/800), new Point(W*592/800, H*345/800)),
                  new Rect(new Point(W*600/800, H*184/800), new Point(W*800/800, H*384/800)),
                  new Rect(new Point(W*458/800, H*390/800), new Point(W*658/800, H*590/800)),
                  new Rect(new Point(W*420/800, H*600/800), new Point(W*620/800, H*800/800)),
                  new Rect(new Point(W*210/800, H*456/800), new Point(W*410/800, H*656/800)),
                  new Rect(new Point(W*  0/800, H*420/800), new Point(W*200/800, H*620/800)),
                  new Rect(new Point(W*140/800, H*212/800), new Point(W*340/800, H*412/800))
               };
            #endregion
               break;
            default:
               System.Diagnostics.Debug.Assert(false, "TODO...");
               break;
         }
      #endregion

         NextIteration();
      }

      private void NextIteration() {
         var bmp = new WriteableBitmap((int)W, (int)H);

         bmp.FillPolygon(new[] {0, 0, (int) W, 0, (int) W, (int) H, 0, (int) H, 0, 0},
            Windows.UI.Color.FromArgb(0xFF, 0xff, 0x8c, 0x00));

         if (Dance) {
            var i = _random.Next()%_eMosaicGroup.VertexCount(8);
            if (((_random.Next()%2) == 1)) {
               _points[i].X += ((_random.Next()%2) == 1) ? OffsetX : -OffsetX;
               _points[i].X = Math.Min(Math.Max(_points[i].X, _limits[i].Left), _limits[i].Right);
            } else {
               _points[i].Y += ((_random.Next()%2) == 1) ? OffsetY : -OffsetY;
               _points[i].Y = Math.Min(Math.Max(_points[i].Y, _limits[i].Top), _limits[i].Bottom);
            }
         }

         if (Animate) {
            Func<UInt16, UInt16> funcAddRandomBit = (val) => (UInt16)((((_random.Next() & 1) == 1) ? 0x0000 : 0x8000) | (val >> 1));
            switch (Any—hannel ? _channel : Next—hannel()) {
            case 0: _fillColor.R = funcAddRandomBit(_fillColor.R); break;
            case 1: _fillColor.G = funcAddRandomBit(_fillColor.G); break;
            case 2: _fillColor.B = funcAddRandomBit(_fillColor.B); break;
            }
         }
         Func<double, byte> funcCast_UInt16_to_Byte = (val) => (byte)(byte.MaxValue * val / Color16.MaxColorValue);
         var fillColor =
            new Color {
               R = funcCast_UInt16_to_Byte(_fillColor.R),
               G = funcCast_UInt16_to_Byte(_fillColor.G),
               B = funcCast_UInt16_to_Byte(_fillColor.B),
               A = byte.MaxValue
            }.Attenuate(160).ToWinColor();

         bmp.FillPolygon(RegionExt.PointsAsXyxyxySequence(_points, true), fillColor);

         for (var i = 0; i < _points.Length; i++) {
            var p1 = _points[i];
            var p2 = _points[(i == _points.Length - 1) ? 0 : i + 1];
            bmp.DrawLineAa((int) p1.X, (int) p1.Y, (int) p2.X, (int) p2.Y, Windows.UI.Colors.Red, 3);
         }

//#if DEBUG
//         var clrBk = Windows.UI.Color.FromArgb(128, 0x0F, 0xFF, 0x00);
//         foreach (var rc in _limits)
//            bmp.DrawRectangle((int) rc.Left, (int) rc.Top, (int) rc.Right, (int) rc.Bottom, clrBk);
//#endif

         //bmp = bmp.RotateFree(_rotate);
         //_rotate += .4;
         //if (_rotate > 360)
         //   _rotate -= 360;

         if (Image == null)
            Image = bmp;
         else {
            var rc = new Rect(0, 0, W, H);
            Image.Blit(rc, bmp, rc);
         }

         if (_timer == null) {
            _timer = new DispatcherTimer {Interval = TimeSpan.FromMilliseconds(100)};
            _timer.Tick += delegate {
               if (Animate || Dance) {
                  //NextIteration();
                  AsyncRunner.InvokeLater(NextIteration, CoreDispatcherPriority.Low);
               }
            };
            _timer.Start();
         }
      }

      public void Dispose() {
         _timer.Stop();
         Animate = false;
         Dance = false;
      }

      private int Next—hannel() {
         var i = _random.Next();
         return (i % 3);
      }
   }
}