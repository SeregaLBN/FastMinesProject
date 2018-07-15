using System;
using System.Collections.Generic;
using System.Linq;
using Windows.UI.Xaml.Media.Imaging;
using fmg.common;
using fmg.common.geom;
using fmg.core.types;
using fmg.core.img;
using fmg.uwp.utils;

namespace fmg.uwp.draw.img.wbmp {

   /// <summary> MVC: view. Abstract UWP representable <see cref="fmg.core.types.ESkillLevel"/> or <see cref="fmg.core.types.EMosaicGroup"/> as image.
   /// WriteableBitmap impl
   /// </summary>
   /// <typeparam name="TImageModel"><see cref="MosaicsSkillModel"/> or <see cref="MosaicsGroupModel"/></typeparam>
   abstract class MosaicSkillOrGroupView<TImageModel> : WithBurgerMenuView<WriteableBitmap, TImageModel>
      where TImageModel : AnimatedImageModel
   {

      static MosaicSkillOrGroupView() {
         StaticInitializer.Init();
      }

      protected MosaicSkillOrGroupView(TImageModel imageModel)
         : base(imageModel)
      { }

      /** get paint information of drawing basic image model */
      protected abstract IEnumerable<Tuple<Color, IEnumerable<PointDouble>>> GetCoords();

      protected override void DrawBody() {
         TImageModel m = Model;
         var bmp = Image;

         bmp.Clear(m.BackgroundColor.ToWinColor());

         var bw = m.BorderWidth;
         var needDrawPerimeterBorder = (!m.BorderColor.IsTransparent && (bw > 0));
         var borderColor = m.BorderColor.ToWinColor();
         var shapes = GetCoords();
         foreach (var data in shapes) {
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

         foreach (var li in BurgerMenuModel.GetCoords())
            try {
               bmp.DrawLineAa((int)li.from.X, (int)li.from.Y, (int)li.to.X, (int)li.to.Y, li.clr.ToWinColor(), (int)li.penWidht);
            } catch (IndexOutOfRangeException ex) {
               System.Diagnostics.Debug.WriteLine("WTF! " + ex);
               bmp.DrawLine((int)li.from.X, (int)li.from.Y, (int)li.to.X, (int)li.to.Y, li.clr.ToWinColor());
            }
         }

   }

}
