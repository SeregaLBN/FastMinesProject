using System;
using System.Linq;
using System.Collections.Generic;
using Windows.UI.Xaml;
using Microsoft.Graphics.Canvas;
using Microsoft.Graphics.Canvas.Geometry;
using fmg.common;
using fmg.common.geom;
using fmg.core.img;
using fmg.uwp.utils;
using fmg.uwp.utils.win2d;
using fmg.uwp.mosaic.win2d;

namespace fmg.uwp.img.win2d {

    /// <summary> MVC: view. Abstract Win2D representable <see cref="fmg.core.types.ESkillLevel"/> or <see cref="fmg.core.types.EMosaicGroup"/> as image. </summary>
    public abstract class MosaicSkillOrGroupView<TImage, TImageModel> : WithBurgerMenuView<TImage, TImageModel>
        where TImage      : DependencyObject, ICanvasResourceCreator
        where TImageModel : AnimatedImageModel
    {
        static MosaicSkillOrGroupView() {
            StaticInitializer.Init();
        }

        protected readonly ICanvasResourceCreator _rc;

        /// <param name="group">may be null. if Null - representable image of typeof(EMosaicGroup)</param>
        protected MosaicSkillOrGroupView(TImageModel imageModel, ICanvasResourceCreator resourceCreator)
            : base(imageModel)
        {
            _rc = resourceCreator;
        }

        /// <summary> get paint information of drawing basic image model </summary>
        protected abstract IEnumerable<Tuple<Color, IEnumerable<PointDouble>>> Coords { get; }

        protected void Draw(CanvasDrawingSession ds, bool fillBk) {
            ICanvasResourceCreator rc = Image;
            var m = Model;
            if (fillBk)
                ds.Clear(m.BackgroundColor.ToWinColor());

            var bw = (float)m.BorderWidth;
            var needDrawPerimeterBorder = (!m.BorderColor.IsTransparent && (bw > 0));
            var borderColor = m.BorderColor.ToWinColor();
            using (var css = new CanvasStrokeStyle {
                StartCap = CanvasCapStyle.Triangle,
                EndCap = CanvasCapStyle.Triangle
            }) {
                var shapes = Coords;
                foreach (var data in shapes) {
                    var points = data.Item2.ToArray();
                    using (var geom = rc.BuildLines(points)) {
                        if (!data.Item1.IsTransparent)
                            ds.FillGeometry(geom, data.Item1.ToWinColor());

                        // draw perimeter border
                        if (needDrawPerimeterBorder)
                            ds.DrawGeometry(geom, borderColor, bw, css);
                    }
                }
            }
            using (var css = new CanvasStrokeStyle {
                StartCap = CanvasCapStyle.Flat,
                EndCap = CanvasCapStyle.Flat
            }) {
                foreach (var li in BurgerMenuModel.Coords)
                    ds.DrawLine(li.from.ToVector2(), li.to.ToVector2(), li.clr.ToWinColor(), (float)li.penWidht, css);
            }

#if DEBUG
            //// test
            //using (var ctf = new Microsoft.Graphics.Canvas.Text.CanvasTextFormat { FontSize = 25 }) {
            //    ds.DrawText(string.Format($"{RotateAngle:0.##}"), 0f, 0f, Color.Black.ToWinColor(), ctf);
            //}
#endif
        }

    }

}
