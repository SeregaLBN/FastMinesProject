using System;
using System.Linq;
using Windows.UI.Xaml.Media.Imaging;
using fmg.core.types;
using fmg.common;
using fmg.common.geom;
using fmg.common.geom.util;
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
         double s = Size - Padding * 2; // size inner square
         var rays = 3 + MosaicGroup.Ordinal(); // rays count
         _points = (MosaicGroup != EMosaicGroup.eOthers)
            ? FigureHelper.GetRegularPolygonCoords(rays, s/2).ToArray()
            : FigureHelper.GetRegularStarCoords(4, s/2, s/5).ToArray();

         // adding offset
         for (var i = 0; i < _points.Length; i++) {
            _points[i].x += Padding + s / 2;
            _points[i].y += Padding + s / 2;
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

         bmp.FillPolygon(_points.PointsAsXyxyxySequence(true), FillColorAttenuate.ToWinColor());

         { // draw perimeter border
            var clr = BorderColor;
            if (clr.A != Color.Transparent.A)
            {
               for (var i = 0; i < _points.Length; i++)
               {
                  var p1 = _points[i];
                  var p2 = _points[(i == _points.Length - 1) ? 0 : i + 1];
                  bmp.DrawLineAa((int)p1.x, (int)p1.y, (int)p2.x, (int)p2.y, clr.ToWinColor(), BorderWidth);
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
