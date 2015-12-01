using System;
using Windows.UI.Xaml.Media.Imaging;
using fmg.core.types;
using fmg.common;
using fmg.common.geom;
using Point = Windows.Foundation.Point;
using Rect = Windows.Foundation.Rect;

namespace fmg.uwp.res.img
{
   /// <summary> representable fmg.core.types.EMosaicGroup as image </summary>
   public class MosaicsGroupImg : PolarLightsImg<EMosaicGroup, WriteableBitmap> {

      public MosaicsGroupImg(EMosaicGroup group, int widthAndHeight = DefaultImageSize, int? padding = null)
         : base(group, widthAndHeight, padding)
      {
      }

      public EMosaicGroup MosaicGroup => Entity;

      protected override void MakeCoords()
      {
         double s = Size - Padding * 2; // size inner Square
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

         base.MakeCoords(); // => Draw();
      }

      protected override void DrawBody()
      {
         var w = Width;
         var h = Height;
         var bmp = new WriteableBitmap(w, h);

         var rotate = Rotate || (Math.Abs(RotateAngle) > 0.5);
         Action<WriteableBitmap> funcFillBk = img =>
         {
            img.FillPolygon(new[] { 0, 0, w, 0, w, h, 0, h, 0, 0 }, BkColor.ToWinColor());
         };
         if (!rotate)
         {
            funcFillBk(bmp);
         }

         bmp.FillPolygon(RegionExt.PointsAsXyxyxySequence(_points, true), FillColorAttenuate.ToWinColor());

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
            bmp = bmp.RotateFree(RotateAngle);

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
      }

   }
}
