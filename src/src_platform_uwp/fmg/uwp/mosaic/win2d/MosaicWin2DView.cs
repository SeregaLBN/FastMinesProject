using System;
using System.ComponentModel;
using System.Collections.Generic;
using Windows.UI.Text;
using Windows.UI.Xaml;
using Microsoft.Graphics.Canvas;
using Microsoft.Graphics.Canvas.Text;
using Microsoft.Graphics.Canvas.Geometry;
using fmg.common;
using fmg.common.geom;
using fmg.core.types;
using fmg.core.mosaic;
using fmg.core.mosaic.cells;
using fmg.uwp.utils;

namespace fmg.uwp.mosaic.win2d {

    /// <summary> Class for drawing cell into (ower <see cref="CanvasBitmap"/>).
    /// MVC: view. Abstract UWP Win2D implementation </summary>
    /// <typeparam name="TImage">platform specific view/image/picture or other display context/canvas/window/panel</typeparam>
    /// <typeparam name="TImageInner">image type of flag/mine into mosaic field</typeparam>
    /// <typeparam name="TMosaicModel"> mosaic data model </typeparam>
    public abstract class MosaicWin2DView<TImage,
                                          TImageInner,
                                          TMosaicModel>
                        : MosaicView<TImage, TImageInner, TMosaicModel>
        where TImage       : DependencyObject//, ICanvasResourceCreator
        where TImageInner  : class
        where TMosaicModel : MosaicDrawModel<TImageInner>
    {

        private CanvasTextFormat _font;
        private CanvasStrokeStyle _cssBorderLine;
        protected bool _alreadyPainted = false;

        protected MosaicWin2DView(TMosaicModel mosaicModel)
            : base(mosaicModel)
        { }

        protected void DrawWin2D(CanvasDrawingSession ds, ICollection<BaseCell> toDrawCells, bool drawBk) {
            System.Diagnostics.Debug.Assert(!_alreadyPainted);
            _alreadyPainted = true;

            TMosaicModel model = Model;

            // 1. background color
            var bkClr = model.BackgroundColor;
            if (drawBk)
                ds.Clear(bkClr.ToWinColor());

            // 2. paint cells
            var pen = model.PenBorder;
            var offset = model.MosaicOffset;
            bool isSimpleDraw = (pen.ColorLight == pen.ColorShadow);
            var bkFill = model.BkFill;
            var font = Font;
            var cssBL = CssBorderLine;

#if DEBUG
            if (MosaicViewCfg.DEBUG_DRAW_FLOW)
                LoggerSimple.Put("MosaicWin2DView.Draw: " + (toDrawCells == null ? "all" : ("cnt=" + toDrawCells.Count))
                                                          + "; drawBk=" + drawBk);
#endif
            if (toDrawCells == null)
                toDrawCells = model.Matrix;
            foreach (BaseCell cell in toDrawCells) {
                var rcInner = cell.GetRcInner(pen.Width).MoveXY(offset);
                var region = cell.GetRegion();
                var bkClrCell = cell.GetBackgroundFillColor(bkFill.Mode,
                                                            bkClr,
                                                            bkFill.GetColor);
                using (var polygon =  isSimpleDraw ? null : ds.CreatePolygon(region, offset))
                using (var geom    = (isSimpleDraw || !drawBk || (bkClrCell != bkClr)) ? ds.BuildLines(region, offset) : null)
                {
                    // ограничиваю рисование только границами своей фигуры
                    using (var layer = (polygon==null) ? null : ds.CreateLayer(1, polygon)) {

                        { // 2.1. paint component
                            // 2.1.1. paint cell background
                            //if (!isSimpleDraw) // когда русуется иконка, а не игровое поле, - делаю попроще...
                            {
                                if (geom != null) // eq   if (!drawBk || (bkClrCell != bkClr))
                                    ds.FillGeometry(geom, bkClrCell.ToWinColor());
                            }

                            void paintImage(TImageInner img) {
                                if (img is CanvasBitmap bmp)
                                    ds.DrawImage(bmp, rcInner.ToWinRect(), new Windows.Foundation.Rect(0, 0, bmp.Size.Width, bmp.Size.Height));
                                else
                                    throw new Exception("Unsupported image type " + img.GetType().Name);
                            }
                            // 2.1.2. output pictures
                            if ((model.ImgFlag != null) &&
                                (cell.State.Status == EState._Close) &&
                                (cell.State.Close == EClose._Flag))
                            {
                                paintImage(model.ImgFlag);
                            } else
                            if ((model.ImgMine != null) &&
                                (cell.State.Status == EState._Open) &&
                                (cell.State.Open == EOpen._Mine))
                            {
                                paintImage(model.ImgMine);
                            } else
                            // 2.1.3. output text
                            {
                                string szCaption;
                                Color txtColor;
                                if (cell.State.Status == EState._Close) {
                                    txtColor = model.ColorText.GetColorClose((int)cell.State.Close.Ordinal());
                                    szCaption = cell.State.Close.ToCaption();
#if DEBUG
                                    //szCaption = cell.getCoord().x + ";" + cell.getCoord().y; // debug
                                    //szCaption = ""+cell.getDirection(); // debug
#endif
                                } else {
                                    txtColor = model.ColorText.GetColorOpen((int)cell.State.Open.Ordinal());
                                    szCaption = cell.State.Open.ToCaption();
                                }
                                if (!string.IsNullOrWhiteSpace(szCaption) && (font != null)) {
                                    if (cell.State.Down)
                                        rcInner.MoveXY(pen.Width, pen.Width);
                                    ds.DrawText(szCaption, rcInner.ToWinRect(), txtColor.ToWinColor(), font);
#if DEBUG
                                    //ds.DrawRectangle(rcInner.ToWinRect(), Color.Red.ToWinColor()); // debug
#endif
                                }
                            }

                            // 2.2. paint border
                            {
                                // draw border lines

                                var down = cell.State.Down || (cell.State.Status == EState._Open);
                                var color = (down ? pen.ColorLight : pen.ColorShadow).ToWinColor();
                                if (isSimpleDraw) {
                                    ds.DrawGeometry(geom, color, (float)pen.Width);
                                } else {
                                    var s = cell.GetShiftPointBorderIndex();
                                    var v = cell.Attr.GetVertexNumber(cell.GetDirection());
                                    for (var i = 0; i < v; i++) {
                                        var p1 = region.GetPoint(i);
                                        var p2 = (i != (v - 1)) ? region.GetPoint(i + 1) : region.GetPoint(0);
                                        if (i == s)
                                            color = (down ? pen.ColorShadow : pen.ColorLight).ToWinColor();
                                        ds.DrawLine(
                                            (float)(p1.X + offset.Width),
                                            (float)(p1.Y + offset.Height),
                                            (float)(p2.X + offset.Width),
                                            (float)(p2.Y + offset.Height),
                                            color, (float)pen.Width, cssBL);
                                    }
                                }

#if DEBUG
                                // debug - визуально проверяю верность вписанного квадрата (проверять при ширине пера около 21)
                                //var rcInner = cell.getRcInner(paintContext.PenBorder.Width);
                                //paint.DrawingSession.DrawRectangle(rcInner.ToWinRect(), Color.Magenta.ToWinColor(), 21);
#endif
                            }
                        }
                    }
                }
            }

#if DEBUG
            if (MosaicViewCfg.DEBUG_DRAW_FLOW)
                LoggerSimple.Put("-------------------------------");
#endif

            _alreadyPainted = false;
        }

        private CanvasTextFormat Font {
            get {
                var fi = Model.FontInfo;
                if (fi.Size < 1)
                    return null;
                if (_font is null) {
                    var ctf = new CanvasTextFormat() {
                        FontSize   = (float)fi.Size,
                        FontFamily = fi.Name,
                        FontStyle  = FontStyle.Normal,
                        FontWeight = fi.Bold ? FontWeights.Bold : FontWeights.Normal,
                        HorizontalAlignment = CanvasHorizontalAlignment.Center,
                        VerticalAlignment = CanvasVerticalAlignment.Center,
                    };
                    _font?.Dispose();
                    _font = ctf;
                }
                return _font;
            }
        }

        private CanvasStrokeStyle CssBorderLine {
            get {
                if (_cssBorderLine is null) {
                    var css = new CanvasStrokeStyle() {
                        StartCap = CanvasCapStyle.Triangle,
                        EndCap = CanvasCapStyle.Triangle,
                    };
                    _cssBorderLine?.Dispose();
                    _cssBorderLine = css;
                }
                return _cssBorderLine;
            }
        }


        protected override void OnPropertyModelChanged(object sender, PropertyChangedEventArgs ev) {
            base.OnPropertyModelChanged(sender, ev);
            switch (ev.PropertyName) {
            case nameof(MosaicDrawModel<TImageInner>.FontInfo):
                _font = null;
                break;
            }
        }

        protected override void Disposing() {
            _font?.Dispose();
            _cssBorderLine?.Dispose();
            base.Disposing();
        }

    }

}
