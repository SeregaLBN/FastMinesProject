using System;
using System.Linq;
using System.Collections.Generic;
using Windows.UI.Xaml;
using Microsoft.Graphics.Canvas;
using Microsoft.Graphics.Canvas.Geometry;
using Fmg.Common;
using Fmg.Common.Geom;
using Fmg.Core.Img;
using Fmg.Uwp.Utils;
using Fmg.Uwp.Utils.Win2d;
using Fmg.Uwp.Mosaic.Win2d;

namespace Fmg.Uwp.Img.Win2d {

    /// <summary> MVC: view. Abstract Win2D representable <see cref="Fmg.Core.Types.ESkillLevel"/> or <see cref="Fmg.Core.Types.EMosaicGroup"/> as image. </summary>
    public abstract class MosaicSkillOrGroupView<TImage, TImageModel> : WithBurgerMenuView<TImage, TImageModel>
        where TImage      : DependencyObject, ICanvasResourceCreator
        where TImageModel : AnimatedImageModel
    {

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

            // draw burger menu
            var burgerModel = BurgerMenuModel;
            var coords = burgerModel.Coords.ToList();
            if (coords.Any()) {
                using (var css = new CanvasStrokeStyle {
                    StartCap = CanvasCapStyle.Flat,
                    EndCap = CanvasCapStyle.Flat
                }) {
                    bool simple = false;
                    if (simple) {
                        foreach (var li in coords)
                            ds.DrawLine(li.from.ToVector2(), li.to.ToVector2(), li.clr.ToWinColor(), (float)li.penWidht, css);
                    } else {
                        var size = burgerModel.Size;
                        var pad = burgerModel.Padding;
                        var width = size.Width - pad.LeftAndRight;
                        var height = size.Height - pad.TopAndBottom;
                        using (var crtBurger = new CanvasRenderTarget(rc, (float)width, (float)height, ds.Dpi)) {
                            using (var dsBurger = crtBurger.CreateDrawingSession()) {
                                dsBurger.Clear(Windows.UI.Colors.Transparent);
                                double penWidth = 0;
                                foreach (var li in coords) {
                                    penWidth = li.penWidht;
                                    dsBurger.DrawLine(
                                        li.from.Move(-pad.Left, -pad.Top).ToVector2(),
                                        li.to.Move(-pad.Left, -pad.Top).ToVector2(),
                                        li.clr.ToWinColor(),
                                        (float)li.penWidht, css);
                                }

                                var destinationRc = new RectDouble(pad.Left, pad.Top, width, height);
                                var offset = penWidth;
                                var horiz = burgerModel.Horizontal;
                                var sourceRc = new RectDouble(
                                    horiz ? 0 : offset / 2,
                                    horiz ? offset / 2 : 0,
                                    width + (horiz ? 0 : -offset),
                                    height + (horiz ? -offset : 0));
                                ds.DrawImage(crtBurger, destinationRc.ToWinRect(), sourceRc.ToWinRect());
                            }
                        }
                    }
                }
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
