using System;
using System.Linq;
using Windows.UI.Xaml.Media.Imaging;
using fmg.common.geom;
using fmg.core.img;
using fmg.core.types;
using fmg.uwp.utils;

namespace fmg.uwp.draw.img.wbmp {
#if false
   /// <summary> Representable <see cref="ESkillLevel"/> as image.
   /// <br/>
   /// WriteableBitmap impl
   /// </summary>
   public class MosaicSkillImg : AMosaicSkillImg<WriteableBitmap> {

      static MosaicSkillImg() {
         StaticInitializer.Init();
      }

      /// <param name="skill">may be null. if Null - representable image of typeof(ESkillLevel)</param>
      public MosaicSkillImg(ESkillLevel? group)
         : base(group)
      { }

      protected override WriteableBitmap CreateImage() {
         return new WriteableBitmap(Size.Width, Size.Height);
      }

      protected override void DrawBody() {
         var bmp = Image;
         bmp.Clear(BackgroundColor.ToWinColor());

         var bw = BorderWidth;
         var needDrawPerimeterBorder = (!BorderColor.IsTransparent && (bw > 0));
         var borderColor = BorderColor.ToWinColor();
         var stars = GetCoords();
         foreach (var data in stars) {
            var points = data.Item2.PointsAsXyxyxySequence(true).ToArray();
            if (!data.Item1.IsTransparent)
               bmp.FillPolygon(points, data.Item1.ToWinColor());

            // draw perimeter border
            if (needDrawPerimeterBorder) {
               for (var i = 0; i < points.Length - 2; i += 2)
                  try {
                     bmp.DrawLineAa(points[i], points[i + 1], points[i + 2], points[i + 3], borderColor, bw);
                  } catch (IndexOutOfRangeException ex) {
                     System.Diagnostics.Debug.WriteLine("WTF! " + ex);
                     bmp.DrawLine(points[i], points[i + 1], points[i + 2], points[i + 3], borderColor);
                  }
            }
         }

         foreach (var li in GetCoordsBurgerMenu())
            try {
               bmp.DrawLineAa((int)li.from.X, (int)li.from.Y, (int)li.to.X, (int)li.to.Y, li.clr.ToWinColor(), (int)li.penWidht);
            } catch (IndexOutOfRangeException ex) {
               System.Diagnostics.Debug.WriteLine("WTF! " + ex);
               bmp.DrawLine((int)li.from.X, (int)li.from.Y, (int)li.to.X, (int)li.to.Y, li.clr.ToWinColor());
            }
      }

   }
#endif
}
