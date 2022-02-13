using System;
using System.Linq;
using System.Collections.Generic;
using Windows.UI.Xaml.Media.Imaging;
using Fmg.Common;
using Fmg.Common.Geom;
using Fmg.Core.Types;
using Fmg.Core.Mosaic;
using Fmg.Core.Mosaic.Cells;
using Fmg.Uwp.Utils;

namespace Fmg.Uwp.Mosaic.Wbmp {

    /// <summary> MVC: view. Abstract UWP over WriteableBitmap implementation </summary>
    /// <typeparam name="TMosaicModel">mosaic data model</typeparam>
    public abstract class MosaicWBmpView<TImageInner, TMosaicModel>
           : MosaicView<WriteableBitmap, TImageInner, TMosaicModel>
        where TImageInner  : class
        where TMosaicModel : MosaicDrawModel<TImageInner>
    {

        private WriteableBitmap _bmp;
        protected bool _alreadyPainted = false;

        protected MosaicWBmpView(TMosaicModel mosaicModel)
            : base(mosaicModel)
        { }

        protected override WriteableBitmap CreateImage() {
            var s = Model.Size;
            _bmp = new WriteableBitmap((int)s.Width, (int)s.Height);
            return _bmp;
        }

        protected void DrawWBmp(ICollection<BaseCell> toDrawCells, bool drawBk) {
            System.Diagnostics.Debug.Assert(!_alreadyPainted);
            _alreadyPainted = true;

            var wbmp = Image;

            var model = Model;
            var size = model.Size;

            // 1. background color
            var bkClr = model.BackgroundColor;
            if (drawBk)
                wbmp.FillRectangle(0, 0, (int)size.Width, (int)size.Height, bkClr.ToWinColor());

            // 2. paint cells
            var pen = model.PenBorder;
            var offset = model.MosaicOffset;
            var isSimpleDraw = (pen.ColorLight == pen.ColorShadow);
            var cellFill = model.CellFill;
            var cellColor = model.CellColor;

#if DEBUG
            if (MosaicViewCfg.DEBUG_DRAW_FLOW)
                Logger.Info($"MosaicWBmpView.Draw: {((toDrawCells==null) ? "all" : "cnt="+ toDrawCells.Count)}"
                                                        + $"; drawBk={drawBk}");
#endif

            if (toDrawCells == null)
                toDrawCells = model.Matrix;
            foreach (var cell in toDrawCells) {
                var rcInner = cell.GetRcInner(pen.Width).MoveXY(offset);
                var region = cell.GetRegion();
                var poly = region.RegionDoubleAsXyxyxySequence(offset, true).ToArray();

                // TODO ограничиваю рисование только границами своей фигуры
                //g.setClip(poly);

                { // 2.1. paint component

                    // 2.1.1. paint background
                    //if (!isSimpleDraw) // когда русуется иконка, а не игровое поле, - делаю попроще...
                    {
                        var bkClrCell = cell.GetCellFillColor(cellFill.Mode,
                                                              cellColor,
                                                              cellFill.GetColor);
                        if (!drawBk || (bkClrCell != bkClr))
                            wbmp.FillPolygon(poly, bkClrCell.ToWinColor());
                    }

                    Action<TImageInner> paintImage = img => {
                        WriteableBitmap wImg = img as WriteableBitmap;
                        var destRc = rcInner.ToWinRect();
                        var srcRc = new Windows.Foundation.Rect(0, 0, wImg.PixelWidth, wImg.PixelHeight);
                        wbmp.Blit(destRc, wImg, srcRc);
                    };

                    // 2.1.2. output pictures
                    if ((model.ImgFlag != null) &&
                        (cell.State.Status == EState._Close) &&
                        (cell.State.Close  == EClose._Flag))
                    {
                        paintImage(model.ImgFlag);
                    } else
                    if ((model.ImgMine != null) &&
                        (cell.State.Status == EState._Open) &&
                        (cell.State.Open   == EOpen._Mine))
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
                            //szCaption = cell.getCoord().x + ";" + cell.getCoord().y; // debug
                            //szCaption = ""+cell.getDirection(); // debug
                        } else {
                            txtColor = model.ColorText.GetColorOpen((int)cell.State.Open.Ordinal());
                            szCaption = cell.State.Open.ToCaption();
                        }
                        var fi = model.FontInfo;
                        if (!string.IsNullOrWhiteSpace(szCaption) && (fi.Size >= 1)) {
                            if (cell.State.Down)
                                rcInner.MoveXY(pen.Width, pen.Width);
                            wbmp.DrawString(szCaption, rcInner.ToWinRect(), fi.Name, (int)fi.Size, txtColor.ToWinColor());
                            //wmp.DrawRectangle(rcInner.Left, rcInner.Top, rcInner.Right, rcInner.Bottom, Color.Red.ToWinColor()); // debug
                        }
                    }

                }

                // 2.2. paint border
                {
                    // draw border lines
                    var down = cell.State.Down || (cell.State.Status == EState._Open);
                    var color = (down ? pen.ColorLight : pen.ColorShadow).ToWinColor();
                    if (isSimpleDraw) {

                        if (pen.Width.HasMinDiff(1))
                            wbmp.DrawPolyline(poly, color);
                        else
                            for (var i=0; i < poly.Length-2; i += 2)
                                try {
                                    wbmp.DrawLineAa(poly[i], poly[i + 1], poly[i + 2], poly[i + 3], color, (int)pen.Width);
                                } catch (IndexOutOfRangeException ex) {
                                    System.Diagnostics.Debug.WriteLine("WTF! " + ex);
                                    wbmp.DrawLine(poly[i], poly[i + 1], poly[i + 2], poly[i + 3], color);
                                }
                    } else {
                        var s = cell.GetShiftPointBorderIndex();
                        var v = cell.Attr.GetVertexNumber(cell.GetDirection());
                        for (var i = 0; i < v; i++) {
                            var p1 = region.GetPoint(i);
                            p1.Move(offset.Width, offset.Height);
                            var p2 = (i != (v - 1))
                                ? region.GetPoint(i + 1)
                                : region.GetPoint(0);
                            p2.Move(offset.Width, offset.Height);
                            if (i == s)
                                color = (down ? pen.ColorShadow : pen.ColorLight).ToWinColor();
                            try {
                                wbmp.DrawLineAa((int)p1.X, (int)p1.Y, (int)p2.X, (int)p2.Y, color, (int)pen.Width);
                            } catch (IndexOutOfRangeException ex) {
                                System.Diagnostics.Debug.WriteLine("WTF! " + ex);
                                wbmp.DrawLine((int)p1.X, (int)p1.Y, (int)p2.X, (int)p2.Y, color);
                            }
                        }
                    }

                    // debug - визуально проверяю верность вписанного квадрата (проверять при ширине пера около 21)
                    //var rcInner = cell.getRcInner(paintContext.PenBorder.Width);
                    //paint.Bmp.DrawRectangle(rcInner.ToWinRect(), Color.Magenta.ToWinColor());
                }
            }
#if DEBUG
            if (MosaicViewCfg.DEBUG_DRAW_FLOW)
                Logger.Info("-------------------------------");
#endif
        _alreadyPainted = false;
        }

    }

}
