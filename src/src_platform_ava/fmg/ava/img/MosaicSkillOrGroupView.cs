using System;
using System.Linq;
using System.Collections.Generic;
using Avalonia.Controls;
using Avalonia.Media;
using Avalonia.Media.Imaging;
using Avalonia.Rendering;
using fmg.common.geom;
using fmg.core.img;
using fmg.core.types;
using fmg.ava.utils;

namespace fmg.ava.draw.img {

   /// <summary> MVC: view. Abstract Avalonia representable {@link fmg.core.types.ESkillLevel} or {@link fmg.core.types.EMosaicGroup} as image </summary>
   /// <typeparam name="TImage">plaform specific view/image/picture or other display context/canvas/window/panel</typeparam>
   /// <typeparam name="AnimatedImageModel">{@link MosaicsSkillModel} or {@link MosaicsGroupModel}</typeparam>
   public abstract class MosaicSkillOrGroupView<TImage, TImageModel>
                           : WithBurgerMenuView<TImage, TImageModel>
      where TImage : class
      where TImageModel : AnimatedImageModel
   {

      static MosaicSkillOrGroupView() {
         StaticInitializer.Init();
      }

      protected MosaicSkillOrGroupView(TImageModel imageModel)
         : base(imageModel)
      { }

      /// <summary> get paint information of drawing basic image model </summary>
      protected abstract IEnumerable<Tuple<fmg.common.Color, IEnumerable<PointDouble>>> Coords { get; }

      protected void DrawBody(DrawingContext dc) {
         var model = Model;
         dc.FillRectangle(new SolidColorBrush(model.BackgroundColor.ToAvaColor()), new Avalonia.Rect(Size.ToAvaSize()));

         var bw = model.BorderWidth;
         var needDrawPerimeterBorder = (!model.BorderColor.IsTransparent && (bw > 0));
         var borderColor = model.BorderColor.ToAvaColor();

         var shapes = Coords;
         foreach (var data in shapes) {
            IBrush brush = null;
            if (!data.Item1.IsTransparent)
               brush = new SolidColorBrush(data.Item1.ToAvaColor());
            Pen pen = null;
            if (needDrawPerimeterBorder)
               pen = new Pen(borderColor.ToUint32(), bw);

            var points = data.Item2.ToArray();
            var figure = new PathFigure {
               StartPoint = points[0].ToAvaPoint(),
               IsClosed = true,
               IsFilled = false // TODO ??
            };
            for (int i = 1; i < points.Length; ++i)
               figure.Segments.Add(new LineSegment {
                  Point = points[i].ToAvaPoint()
               });

            PathGeometry geom = new PathGeometry();
            geom.Figures.Add(figure);

            dc.DrawGeometry(brush, pen, geom);
         }

         foreach (var lu in BurgerMenuModel.Coords) {
            // g.setLineWidth(li.penWidht);
            // g.setStroke(Cast.toColor(li.clr));
            // g.strokeLine(li.from.x, li.from.y, li.to.x, li.to.y);
         }
      }

   }

}
