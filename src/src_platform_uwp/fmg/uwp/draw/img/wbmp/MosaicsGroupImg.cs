using System.Linq;
using Windows.UI.Xaml.Media.Imaging;
using fmg.core.types;
using fmg.common;
using fmg.common.geom;
using fmg.core.img;
using fmg.uwp.utils;

namespace fmg.uwp.draw.img.wbmp {

   /// <summary> Representable <see cref="EMosaicGroup"/> as image.
   /// <br/>
   /// WriteableBitmap impl
   /// </summary>
   public class MosaicsGroupImg : AMosaicsGroupImg<WriteableBitmap> {

      static MosaicsGroupImg() {
         StaticRotateImgConsts.Init();
      }

      /// <param name="skill">may be null. if Null - representable image of typeof(EMosaicGroup)</param>
      public MosaicsGroupImg(EMosaicGroup? group)
         : base(group)
      { }

      protected override WriteableBitmap CreateImage() {
         //LoggerSimple.Put("CreateImage: Width={0}; Height={1}: {2}", Width, Height, Entity);
         return new WriteableBitmap(Width, Height);
      }

      protected override void DrawBody() {
         var bmp = Image;

         bmp.Clear(BackgroundColor.ToWinColor());

         var shapes = GetCoords();
         foreach (var data in shapes) {
            var points = data.Item2.PointsAsXyxyxySequence(true).ToArray();
            bmp.FillPolygon(points, data.Item1.ToWinColor());

            // draw perimeter border
            var clr = BorderColor;
            if (!clr.IsTransparent) {
               var clrWin = clr.ToWinColor();
               var bw = BorderWidth;
               for (var i = 0; i < points.Length - 2; i += 2) {
                  bmp.DrawLineAa(points[i], points[i + 1], points[i + 2], points[i + 3], clrWin, bw);
               }
            }
         }

         foreach (var li in GetCoordsBurgerMenu()) {
            bmp.DrawLineAa((int)li.from.X, (int)li.from.Y, (int)li.to.X, (int)li.to.Y, li.clr.ToWinColor(), (int)li.penWidht);
         }
      }

   }

}
