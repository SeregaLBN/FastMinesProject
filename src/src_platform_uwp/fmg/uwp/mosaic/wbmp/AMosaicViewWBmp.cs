using System;
using System.Linq;
using System.Collections.Generic;
using Windows.UI.Xaml.Media.Imaging;
using fmg.common;
using fmg.common.geom;
using fmg.core.types;
using fmg.core.mosaic;
using fmg.core.mosaic.cells;
using fmg.uwp.utils;

namespace fmg.uwp.mosaic.wbmp {

   /// <summary> MVC: view. Abstract UWP over WriteableBitmap implementation </summary>
   /// <typeparam name="TMosaicModel">mosaic data model</typeparam>
   public abstract class AMosaicViewWBmp<TImageInner, TMosaicModel>
                       : AMosaicView<WriteableBitmap, TImageInner, TMosaicModel>
      where TImageInner  : class
      where TMosaicModel : MosaicDrawModel<TImageInner>
   {

      private WriteableBitmap _bmp;
      protected bool _alreadyPainted = false;

      protected AMosaicViewWBmp(TMosaicModel mosaicModel)
         : base(mosaicModel)
      { }


      static AMosaicViewWBmp() {
         StaticInitializer.Init();
      }

      protected override WriteableBitmap CreateImage() {
         var s = Model.Size;
         _bmp = new WriteableBitmap((int)s.Width, (int)s.Height);
         return _bmp;
      }

      protected void Draw(IEnumerable<BaseCell> modifiedCells, RectDouble? clipRegion, bool drawBk) {
         // TODO ограничиваю рисование только границами своей фигуры
         //...

         var wbmp = Image;

         var model = Model;
         var size = model.Size;

         // 1. background color
         var bkClr = model.BackgroundColor;
         if (drawBk) {
            if (!clipRegion.HasValue)
               wbmp.FillRectangle(0, 0, (int)size.Width, (int)size.Height, bkClr.ToWinColor());
            else
               wbmp.FillRectangle((int)clipRegion.Value.X, (int)clipRegion.Value.Y, (int)clipRegion.Value.Width, (int)clipRegion.Value.Height, bkClr.ToWinColor());
         }

         // 2. paint cells
         var pen = model.PenBorder;
         var padding = model.Padding;
         var margin = model.Margin;
         var offset = new SizeDouble(margin.Left + padding.Left,
                                     margin.Top  + padding.Top);
         var isIconicMode = (pen.ColorLight == pen.ColorShadow);
         var bkFill = model.BkFill;

         IEnumerable<BaseCell> toCheck;
         if ((clipRegion != null) || (modifiedCells == null))
            toCheck = model.Matrix; // check to redraw all mosaic cells
         else
            toCheck = modifiedCells;

#if DEBUG
         String sufix = "; clipReg=" + clipRegion + "; drawBk=" + drawBk;
         if (modifiedCells == null)
            LoggerSimple.Put("> AMosaicViewWBmp.Draw: all=" + toCheck.Count() + sufix);
         else
         if (ReferenceEquals(modifiedCells, model.Matrix) || (modifiedCells.Count() == model.Matrix.Count))
            LoggerSimple.Put("> AMosaicViewWBmp.Draw: all=" + modifiedCells.Count() + sufix);
         else
            LoggerSimple.Put("> AMosaicViewWBmp.Draw: cnt=" + modifiedCells.Count() + sufix);
         int tmp = 0;
#endif

         foreach (var cell in toCheck) {
            // redraw only when needed...
            if (ReferenceEquals(toCheck, modifiedCells) ||
                ((modifiedCells != null) && (modifiedCells.Contains(cell))) || // ..when the cell is explicitly specified
                ((clipRegion != null) && cell.getRcOuter().MoveXY(offset.Width, offset.Height).Intersection(clipRegion.Value))) // ...when the cells and update region intersect
            {
#if DEBUG
               ++tmp;
#endif
               var rcInner = cell.getRcInner(pen.Width);
               var region = cell.getRegion();
               var poly = region.RegionDoubleAsXyxyxySequence(offset, true).ToArray();

               // ограничиваю рисование только границами своей фигуры
               //g.setClip(poly);

               { // 2.1. paint component

                  // 2.1.1. paint background
                  //if (!isIconicMode) // когда русуется иконка, а не игровое поле, - делаю попроще...
                  {
                     var bkClrCell = cell.getBackgroundFillColor(bkFill.Mode,
                                                                 bkClr,
                                                                 bkFill.GetColor);
                     if (!drawBk || (bkClrCell != bkClr))
                        wbmp.FillPolygon(poly, bkClrCell.ToWinColor());
                  }

                  Action<TImageInner> paintImage = img => {
                     WriteableBitmap wImg = img as WriteableBitmap;
                     var destRc = rcInner.MoveXY(offset.Width, offset.Height).ToWinRect();
                     var srcRc = new Windows.Foundation.Rect(0, 0, wImg.PixelWidth, wImg.PixelHeight);
                     wbmp.Blit(destRc, wImg, srcRc);
                  };

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
                        //szCaption = cell.getCoord().x + ";" + cell.getCoord().y; // debug
                        //szCaption = ""+cell.getDirection(); // debug
                     } else {
                        txtColor = model.ColorText.GetColorOpen((int)cell.State.Open.Ordinal());
                        szCaption = cell.State.Open.ToCaption();
                     }
                     if (!string.IsNullOrWhiteSpace(szCaption)) {
                        if (cell.State.Down)
                           rcInner.MoveXY(1, 1);
                        wbmp.DrawString(szCaption, rcInner.ToWinRect(), model.FontInfo.Name, (int)model.FontInfo.Size, txtColor.ToWinColor());
                      //wmp.DrawRectangle(rcInner.Left, rcInner.Top, rcInner.Right, rcInner.Bottom, Color.Red.ToWinColor()); // debug
                     }
                  }

               }

               // 2.2. paint border
               {
                  // draw border lines
                  var down = cell.State.Down || (cell.State.Status == EState._Open);
                  var color = (down ? pen.ColorLight : pen.ColorShadow).ToWinColor();
                  if (isIconicMode) {

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
                     var s = cell.getShiftPointBorderIndex();
                     var v = cell.Attr.GetVertexNumber(cell.getDirection());
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
         }

#if DEBUG
         LoggerSimple.Put("< AMosaicViewSwing.draw: cnt=" + tmp);
         LoggerSimple.Put("-------------------------------");
#endif

         _alreadyPainted = false;
      }

   }

}
