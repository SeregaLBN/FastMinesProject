using System;
using Windows.UI.Core;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Media.Imaging;
using fmg.core.types;
using FastMines.Common;
using fmg.common;
using fmg.common.geom;
using FastMines.Presentation.Notyfier;
using Point = Windows.Foundation.Point;
using Rect = Windows.Foundation.Rect;

namespace fmg.uwp.res.img
{

   public class MosaicsGroupImg : NotifyPropertyChanged, IDisposable
   {
      public static readonly Windows.UI.Color DefaultBkColor = Resources.DefaultBkColor;
      public const int DefaultImageSize = 100;

      private struct Color16
      {
         public const UInt16 MaxColorValue = 0xFFFF;
         public UInt16 R, G, B;
      }

      private int _size;
      /// <summary> width and height in pixel </summary>
      public int Size
      {
         get { return _size; }
         set
         {
            if (SetProperty(ref _size, value))
            {
               _image = null;
               MakeCoords();
            }
         }
      }

      /// <summary> width image </summary>
      public int Width => Size;
      /// <summary> height image </summary>
      public int Height => Size;

      private int _padding;
      /// <summary> inside padding </summary>
      public int Padding
      {
         get { return _padding; }
         set
         {
            if (value*2 >= Size)
               throw new ArgumentException("Padding size is very large. Should be less than Size / 2.");
            if (SetProperty(ref _padding, value))
            {
               _image = null;
               MakeCoords();
            }
         }
      }

      public EMosaicGroup MosaicGroup { get; private set; }
      private Point[] _points;

      private WriteableBitmap _image;
      public WriteableBitmap Image { get { return _image; } private set { SetPropertyForce(ref _image, value); } }

      /// <summary> frequency of redrawing (in milliseconds) </summary>
      public double RedrawInterval { get; set; } = 100;

      private bool _polarLights;
      /// <summary> shimmering filling </summary>
      public bool PolarLights
      {
         get { return _polarLights; }
         set
         {
            if (SetProperty(ref _polarLights, value))
               Draw();
         }
      }

      private Color _bkColor = DefaultBkColor.ToFmColor();
      /// <summary> background fill color </summary>
      public Color BkColor
      {
         get { return _bkColor; }
         set
         {
            if (SetProperty(ref _bkColor, value))
               Draw();
         }
      }

      private Color _borderColor = Color.Red;
      public Color BorderColor
      {
         get { return _borderColor; }
         set
         {
            if (SetProperty(ref _borderColor, value))
               Draw();
         }
      }

      private int _borderWidth = 3;
      public int BorderWidth
      {
         get { return _borderWidth; }
         set
         {
            if (SetProperty(ref _borderWidth, value))
               Draw();
         }
      }
      private DispatcherTimer _timer;

      private bool _rotate;
      public bool Rotate
      {
         get { return _rotate; }
         set
         {
            if (SetProperty(ref _rotate, value) && value)
               Draw();
         }
      }

      private double _rotateAngle;
      /// <summary> -360° .. 0° .. +360° </summary>
      public double RotateAngle
      {
         get { return _rotateAngle; }
         set
         {
            if (SetProperty(ref _rotateAngle, value))
               Draw();
         }
      }

      private double _rotateAngleDelta = .4;
      public double RotateAngleDelta
      {
         get { return _rotateAngleDelta; }
         set
         {
            if (SetProperty(ref _rotateAngleDelta, value) && Rotate)
               Draw();
         }
      }

      private Color16 _fillColor;

      private readonly Random _random = new Random(Guid.NewGuid().GetHashCode());

      public MosaicsGroupImg(EMosaicGroup group, int widthAndHeight = DefaultImageSize, int? padding = null)
      {
         //LoggerSimple.Put("> MosaicsGroupImg");
         MosaicGroup = group;
         _size = widthAndHeight;
         if (!padding.HasValue)
            _padding = (int)(widthAndHeight * 0.05); // 5%

         _fillColor = new Color16
         {
            R = Convert.ToUInt16(_random.Next(Color16.MaxColorValue)),
            G = Convert.ToUInt16(_random.Next(Color16.MaxColorValue)),
            B = Convert.ToUInt16(_random.Next(Color16.MaxColorValue))
         };
         MakeCoords();
      }

      private void MakeCoords()
      {
         double s = Size - Padding * 2; // size inner Square1
         var w = s;
         var h = s;
         switch (MosaicGroup)
         {
            case EMosaicGroup.eTriangles:
               {
                  // An equilateral triangle in a circle.
                  // The circle inscribed in a Square1.
                  var r = s / 2.0; // circle radius
                  var a = r * Math.Sqrt(3); // size triangle
                  _points = new[] { new Point(r, 0), new Point(r + a / 2, r * 1.5), new Point(r - a / 2, r * 1.5) };
               }
               break;
            case EMosaicGroup.eQuadrangles:
               {
                  // The circle inscribed in a Square1.
                  // The Square2 inscribed in a circle.
                  var x = s / Math.Sqrt(2); // size Square2
                  var d = (s - x) / 2; // delta offset
                  _points = new[] { new Point(d, d), new Point(w - d, d), new Point(w - d, h - d), new Point(d, h - d) };
               }
               break;
            case EMosaicGroup.ePentagons:
               // approximately
               #region http://upload.wikimedia.org/wikipedia/commons/thumb/5/55/Regular_pentagon_1.svg/220px-Regular_pentagon_1.svg.png
               _points = new[] {
                  new Point(w/2, h/20),
                  new Point(
                     w*.9272, // 204*100/220 = 2040/22 ~= 92.72
                     h/2.75), // 80*100/220 = 800/22  = 36.36363636363636  = 100 * 0.3636363636363636 =   100 * 1/2.75
                  new Point(
                     w*.7636, // 168*100/220 = 1680/22  ~= 76.36
                     h*.8636), // 190*100/220 = 1900/22 ~= 86.36
                  new Point(
                     w*.2363, // 52*100/220 = 520/22 ~= 23.63
                     h*.8636),
                  new Point(
                     w*.0727, // 16*100/220 = 160/22 ~= 7.27
                     h/2.75)
               };
               #endregion
               break;
            case EMosaicGroup.eHexagons:
               // approximately
               #region http://upload.wikimedia.org/wikipedia/commons/thumb/b/b7/Regular_hexagon_1.svg/600px-Regular_hexagon_1.svg.png
               _points = new[] {
                  new Point(w/2, h/30),
                  new Point(w*530/600, h*165/600),
                  new Point(w*530/600, h*435/600),
                  new Point(w/2, h-h/30),
                  new Point(w*65/600, h*435/600),
                  new Point(w*65/600, h*165/600)
               };
               #endregion
               break;
            case EMosaicGroup.eOthers:
               // approximately
               #region
               _points = new[] {
                  new Point(w*306/800, h* 63/800),
                  new Point(w*490/800, h*244/800),
                  new Point(w*737/800, h*310/800),
                  new Point(w*557/800, h*491/800),
                  new Point(w*490/800, h*737/800),
                  new Point(w*308/800, h*558/800),
                  new Point(w* 63/800, h*491/800),
                  new Point(w*243/800, h*310/800)};
               #endregion
               break;
            default:
               System.Diagnostics.Debug.Assert(false, "TODO...");
               break;
         }

         // adding offset
         for (var i = 0; i < _points.Length; i++)
         {
            _points[i].X += Padding;
            _points[i].Y += Padding;
         }

         Draw();
      }

      private bool _scheduledDraw;
      /// <summary> schedule drawing (async operation) </summary>
      private void Draw()
      {
          if (_scheduledDraw)
            return;
         _scheduledDraw = true;
#pragma warning disable CS4014 // Because this call is not awaited, execution of the current method continues before the call is completed
         AsyncRunner.InvokeLater(DrawSync, CoreDispatcherPriority.Low);
#pragma warning restore CS4014 // Because this call is not awaited, execution of the current method continues before the call is completed
      }


      private void DrawSync()
      {
         //LoggerSimple.Put(" DrawSync: " + MosaicGroup + ": " + BkColor);
         _scheduledDraw = false;
         var w = Width;
         var h = Height;
         var bmp = new WriteableBitmap(w, h);

         var rotate = Rotate || (Math.Abs(RotateAngle) > 0.1);
         Action<WriteableBitmap> funcFillBk = img =>
         {
            img.FillPolygon(new[] { 0, 0, w, 0, w, h, 0, h, 0, 0 }, BkColor.ToWinColor());
         };
         if (!rotate)
         {
            funcFillBk(bmp);
         }

         if (PolarLights)
         {
            Func<UInt16, UInt16> funcAddRandomBit = val => (UInt16)((((_random.Next() & 1) == 1) ? 0x0000 : 0x8000) | (val >> 1));
            switch (_random.Next() % 3)
            {
               case 0: _fillColor.R = funcAddRandomBit(_fillColor.R); break;
               case 1: _fillColor.G = funcAddRandomBit(_fillColor.G); break;
               case 2: _fillColor.B = funcAddRandomBit(_fillColor.B); break;
            }
         }
         Func<double, byte> funcCastUInt16ToByte = val => (byte)(byte.MaxValue * val / Color16.MaxColorValue);
         var fillColor =
            new Color
            {
               R = funcCastUInt16ToByte(_fillColor.R),
               G = funcCastUInt16ToByte(_fillColor.G),
               B = funcCastUInt16ToByte(_fillColor.B),
               A = byte.MaxValue
            }.Attenuate(160).ToWinColor();

         bmp.FillPolygon(RegionExt.PointsAsXyxyxySequence(_points, true), fillColor);

         { // draw perimeter border
            var clr = BorderColor;
            if (clr.A != Color.Transparent.A)
            {
               for (var i = 0; i < _points.Length; i++)
               {
                  var p1 = _points[i];
                  var p2 = _points[(i == _points.Length - 1) ? 0 : i + 1];
                  bmp.DrawLineAa((int)p1.X, (int)p1.Y, (int)p2.X, (int)p2.Y, clr.ToWinColor(), BorderWidth);
               }
            }
         }

         if (rotate)
         {
            bmp = bmp.RotateFree(RotateAngle);

            _rotateAngle += RotateAngleDelta;
            if (RotateAngleDelta > 0)
            {
               if (RotateAngle >= 360)
                  _rotateAngle -= 360;
            }
            else
            {
               if (RotateAngle <= -360)
                  _rotateAngle += 360;
            }
         }

         if (Image == null)
         {
            if (rotate)
            {
               var tmp = new WriteableBitmap(w, h);
               funcFillBk(tmp);
               var rc = new Rect(0, 0, w, h);
               tmp.Blit(rc, bmp, rc);
               bmp = tmp;
            }
            Image = bmp;
         }
         else
         {
            var rc = new Rect(0, 0, w, h);
            if (rotate)
            {
               funcFillBk(Image);
            }
            Image.Blit(rc, bmp, rc);
         }

         if (PolarLights || Rotate)
         {
            if (_timer == null)
            {
               _timer = new DispatcherTimer { Interval = TimeSpan.FromMilliseconds(RedrawInterval) };
               _timer.Tick += delegate { Draw(); };
            }
            _timer.Start();
         }
         else
         {
            _timer?.Stop();
         }
      }

      public void Dispose()
      {
         Dispose(true);
      }

      protected virtual void Dispose(bool disposing)
      {
         if (disposing)
         {
            // free managed resources
            _timer?.Stop();
         }
         // free native resources if there are any.
      }

   }
}