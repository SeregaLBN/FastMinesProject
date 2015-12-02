using System;
using System.Linq;
using Windows.UI.Xaml.Media.Imaging;
using fmg.common;
using fmg.common.geom;
using fmg.data.controller.types;
using Point = Windows.Foundation.Point;
using Rect = Windows.Foundation.Rect;
using System.Collections.Generic;

namespace fmg.uwp.res.img
{
   /// <summary> representable fmg.data.controller.types.ESkillLevel as image </summary>
   public class MosaicsSkillImg : RotatedImg<ESkillLevel, WriteableBitmap>
   {

      public MosaicsSkillImg(ESkillLevel group, int widthAndHeight = DefaultImageSize, int? padding = null)
         : base(group, widthAndHeight, padding) {
      }

      public ESkillLevel MosaicGroup => Entity;

      protected override void MakeCoords() {
         double s = Size - Padding * 2; // size inner Square
         #region classic star
#if false
         {
            var alpha = (2 * Math.PI) / 10;
            var radius = s / 2;
            var center = new[] { s / 2, s / 2 }; // star center [x, y]

            _points = new Point[10];
            for (var i = 0; i < 10; i++) {
               var r = radius * (i % 2 + 1) / 2;
               var omega = alpha * i;
               _points[i] = new Point((r * Math.Sin(omega)) + center[0], (r * Math.Cos(omega)) + center[1]);
            }

            // adding offset
            for (var i = 0; i < _points.Length; i++) {
               _points[i].X += Padding;
               _points[i].Y += Padding;
            }
         }
#endif
         #endregion

         #region star from js code http://stackoverflow.com/questions/14580033/algorithm-for-drawing-a-5-point-star
#if true
         {
            Func<Point[], double, Point[]> funcRotate2D = (vecArr, byRads) => {
               var mat = new double[2, 2] {
                              { Math.Cos(byRads), -Math.Sin(byRads) },
                              { Math.Sin(byRads), Math.Cos(byRads) }};
               var result = new Point[vecArr.Length];
               for (var i = 0; i < vecArr.Length; ++i) {
                  result[i] = new Point(
                     mat[0, 0] * vecArr[i].X + mat[0, 1] * vecArr[i].Y,
                     mat[1, 0] * vecArr[i].X + mat[1, 1] * vecArr[i].Y);
               }
               return result;
            };
            Func<int, double, Point[][]> funcGenerateStarTriangles = (numPoints, r) => {
               var triangleBase = r * Math.Tan(Math.PI / numPoints);
               var triangle = new Point[] {
                  new Point(0, r)
                  , new Point(triangleBase / 2, 0)/*, new Point(-triangleBase / 2, 0), new Point(0, r) */};
               var result = new Point[numPoints][];
               for (var i = 0; i < numPoints; ++i) {
                  result[i] = funcRotate2D(triangle, i * (2 * Math.PI / numPoints));
               }
               return result;
            };
            var radius = s / 2;
            _points = funcGenerateStarTriangles(5, radius).SelectMany(x => x).ToArray();

            // adding offset
            for (var i = 0; i < _points.Length; i++) {
               _points[i].X += Padding + Size / 2;
               _points[i].Y += Padding + Size / 2;
            }
         }
#endif
         #endregion

         base.MakeCoords(); // => Draw();
      }

      protected override void DrawBody() {
         var w = Width;
         var h = Height;
         var bmp = new WriteableBitmap(w, h);

         var rotate = Rotate || (Math.Abs(RotateAngle) > 0.5);
         Action<WriteableBitmap> funcFillBk = img => {
            img.FillPolygon(new[] { 0, 0, w, 0, w, h, 0, h, 0, 0 }, BkColor.ToWinColor());
         };
         if (!rotate) {
            funcFillBk(bmp);
         }

         bmp.FillPolygon(RegionExt.PointsAsXyxyxySequence(_points, true), FillColorAttenuate.ToWinColor());

         { // draw perimeter border
            var clr = BorderColor;
            if (clr.A != Color.Transparent.A) {
               for (var i = 0; i < _points.Length; i++) {
                  var p1 = _points[i];
                  var p2 = _points[(i == _points.Length - 1) ? 0 : i + 1];
                  bmp.DrawLineAa((int)p1.X, (int)p1.Y, (int)p2.X, (int)p2.Y, clr.ToWinColor(), BorderWidth);
               }
            }
         }

         if (rotate)
            bmp = bmp.RotateFree(RotateAngle);

         if (Image == null) {
            if (rotate) {
               var tmp = new WriteableBitmap(w, h);
               funcFillBk(tmp);
               var rc = new Rect(0, 0, w, h);
               tmp.Blit(rc, bmp, rc);
               bmp = tmp;
            }
            Image = bmp;
         } else {
            var rc = new Rect(0, 0, w, h);
            if (rotate) {
               funcFillBk(Image);
            }
            Image.Blit(rc, bmp, rc);
         }
      }

   }
}
