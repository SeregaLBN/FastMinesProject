package fmg.android.mosaic;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import fmg.common.Color;
import fmg.common.geom.BoundDouble;
import fmg.common.geom.PointDouble;
import fmg.common.geom.RectDouble;
import fmg.common.geom.RegionDouble;
import fmg.common.geom.SizeDouble;
import fmg.core.mosaic.MosaicDrawModel;
import fmg.core.mosaic.MosaicDrawModel.BackgroundFill;
import fmg.core.mosaic.MosaicView;
import fmg.core.mosaic.cells.BaseCell;
import fmg.core.types.EClose;
import fmg.core.types.EOpen;
import fmg.core.types.EState;
import fmg.core.types.draw.FontInfo;
import fmg.core.types.draw.PenBorder;
import fmg.android.utils.Cast;
import fmg.android.utils.StaticInitializer;

/** MVC: view. Abstract android implementation
 * @param <TImage> plaform specific view/image/picture or other display context/canvas/window/panel
 * @param <TImageInner> image type of flag/mine into mosaic field
 * @param <TMosaicModel> mosaic data model
 */
public abstract class MosaicAndroidView<TImage,
                                       TImageInner,
                                       TMosaicModel extends MosaicDrawModel<TImageInner>>
                extends MosaicView<TImage, TImageInner, TMosaicModel>
{

   private final Paint _textPaint;
   protected boolean _alreadyPainted = false;

   protected MosaicAndroidView(TMosaicModel mosaicModel) {
      super(mosaicModel);
      _textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
      _textPaint.setStyle(Paint.Style.FILL);
   }


   static {
      StaticInitializer.init();
   }


   protected void drawAndroid(Canvas g, Collection<BaseCell> modifiedCells, RectDouble clipRegion, boolean drawBk) {
      assert !_alreadyPainted;
      _alreadyPainted = true;

      TMosaicModel model = getModel();
      SizeDouble size = model.getSize();

      // save
      g.save();

      Paint paintStroke = new Paint(Paint.ANTI_ALIAS_FLAG); paintStroke.setStyle(Paint.Style.STROKE);
      Paint paintFill   = new Paint(Paint.ANTI_ALIAS_FLAG); paintFill  .setStyle(Paint.Style.FILL);

      // 1. background color
      Color bkClr = model.getBackgroundColor();
      if (drawBk) {
         paintFill.setColor(Cast.toColor(bkClr));
         if (clipRegion == null)
            g.drawColor(Cast.toColor(bkClr), PorterDuff.Mode.CLEAR);
         else
            g.drawRect((float)clipRegion.x, (float)clipRegion.y, (float)clipRegion.width, (float)clipRegion.height, paintFill);
      }

      // 2. paint cells
      PenBorder pen = model.getPenBorder();
      paintStroke.setStrokeWidth((float)pen.getWidth());
      BoundDouble padding = model.getPadding();
      BoundDouble margin  = model.getMargin();
      SizeDouble offset = new SizeDouble(margin.left + padding.left,
                                         margin.top  + padding.top);
      boolean isIconicMode = pen.getColorLight().equals(pen.getColorShadow());
      BackgroundFill bkFill = model.getBackgroundFill();

      boolean redrawAll = (modifiedCells == null) || modifiedCells.isEmpty() || (modifiedCells.size() >= model.getMatrix().size());
      boolean recheckAll = (clipRegion != null); // check to redraw all mosaic cells
      Collection<BaseCell> toCheck = (redrawAll || recheckAll) ? model.getMatrix() : modifiedCells;

      if (_DEBUG_DRAW_FLOW) {
         System.out.println("> MosaicAndroidView.draw: " + (redrawAll ? "all" : ("cnt=" + modifiedCells.size()))
                                                       + "; clipReg=" + clipRegion
                                                       + "; drawBk=" + drawBk);
      }
      int tmp = 0;

      for (BaseCell cell: toCheck) {
         // redraw only when needed...
         if (redrawAll ||
             ((modifiedCells != null) && (modifiedCells.contains(cell))) || // ..when the cell is explicitly specified
             ((clipRegion != null) && cell.getRcOuter().moveXY(offset.width, offset.height).intersection(clipRegion))) // ...when the cells and update region intersect
         {
            ++tmp;
            RectDouble rcInner = cell.getRcInner(pen.getWidth());
            Path poly = Cast.toPolygon(RegionDouble.moveXY(cell.getRegion(), offset));

            //if (!isIconicMode)
            {
               g.save();
               // ограничиваю рисование только границами своей фигуры
               g.clipPath(poly);
            }

            { // 2.1. paint component

               // 2.1.1. paint cell background
               //if (!isIconicMode) // когда русуется иконка, а не игровое поле, - делаю попроще...
               {
                  Color bkClrCell = cell.getBackgroundFillColor(bkFill.getMode(),
                                                                bkClr,
                                                                bkFill.getColors());
                  if (!drawBk || !bkClrCell.equals(bkClr)) {
                     paintFill.setColor(Cast.toColor(bkClrCell));
                     g.drawPath(poly, paintFill);
                  }
               }

             //g.setColor(java.awt.Color.MAGENTA);
             //g.drawRect((int)rcInner.x, (int)rcInner.y, (int)rcInner.width, (int)rcInner.height);

               Consumer<TImageInner> paintImage = img -> {
                  int x = (int)(rcInner.x + offset.width);
                  int y = (int)(rcInner.y + offset.height);
                  if (img instanceof android.graphics.Bitmap) {
                     g.drawBitmap((android.graphics.Bitmap)img, x, y, null);
                  } else {
                     throw new RuntimeException("Unsupported image type " + img.getClass().getSimpleName());
                  }
               };

               // 2.1.2. output pictures
               if ((model.getImgFlag() != null) &&
                  (cell.getState().getStatus() == EState._Close) &&
                  (cell.getState().getClose() == EClose._Flag))
               {
                  paintImage.accept(model.getImgFlag());
               } else
               if ((model.getImgMine() != null) &&
                  (cell.getState().getStatus() == EState._Open) &&
                  (cell.getState().getOpen() == EOpen._Mine))
               {
                  paintImage.accept(model.getImgMine());
               } else
               // 2.1.3. output text
               {
                  String szCaption;
                  if (cell.getState().getStatus() == EState._Close) {
                     _textPaint.setColor(Cast.toColor(model.getColorText().getColorClose(cell.getState().getClose().ordinal())));
                     szCaption = cell.getState().getClose().toCaption();
                   //szCaption = cell.getCoord().x + ";" + cell.getCoord().y; // debug
                   //szCaption = ""+cell.getDirection(); // debug
                  } else {
                     _textPaint.setColor(Cast.toColor(model.getColorText().getColorOpen(cell.getState().getOpen().ordinal())));
                     szCaption = cell.getState().getOpen().toCaption();
                  }
                  if ((szCaption != null) && (szCaption.length() > 0))
                  {
                     rcInner.moveXY(offset.width, offset.height);
                     if (cell.getState().isDown())
                        rcInner.moveXY(1, 1);
                     drawText(g, szCaption, rcInner);
                   //{ // test
                   //   java.awt.Color clrOld = g.getColor(); // test
                   //   g.setColor(java.awt.Color.red);
                   //   g.drawRect((int)rcInner.x, (int)rcInner.y, (int)rcInner.width, (int)rcInner.height);
                   //   g.setColor(clrOld);
                   //}
                  }
               }

            }

            // 2.2. paint border
            {
               // draw border lines
               boolean down = cell.getState().isDown() || (cell.getState().getStatus() == EState._Open);
               paintStroke.setColor(Cast.toColor(down
                                          ? pen.getColorLight()
                                          : pen.getColorShadow()));
               if (isIconicMode) {
                  g.drawPath(poly, paintStroke);
               } else {
                  int s = cell.getShiftPointBorderIndex();
                  int v = cell.getAttr().getVertexNumber(cell.getDirection());
                  for (int i=0; i<v; i++) {
                     PointDouble p1 = cell.getRegion().getPoint(i);
                     PointDouble p2 = (i != (v-1))
                                          ? cell.getRegion().getPoint(i+1)
                                          : cell.getRegion().getPoint(0);
                     if (i==s)
                        paintStroke.setColor(Cast.toColor(down
                                                   ? pen.getColorShadow()
                                                   : pen.getColorLight()));
                     g.drawLine((int)(p1.x+offset.width), (int)(p1.y+offset.height), (int)(p2.x+offset.width), (int)(p2.y+offset.height), paintStroke);
                  }
               }

               // debug - визуально проверяю верность вписанного квадрата (проверять при ширине пера около 21)
             //g.setColor(java.awt.Color.MAGENTA);
             //g.drawRect((int)rcInner.x, (int)rcInner.y, (int)rcInner.width, (int)rcInner.height);
            }

            //if (!isIconicMode)
            {
               g.restore();
            }
         }
      }

      /** /
      // test
      {
         g.setClip(oldShape);
         //g.setComposite(AlphaComposite.SrcOver);

         // test padding
         g.setStroke(new BasicStroke(5));
         Color clr = Color.DarkRed.clone();
         clr.setA(120);
         g.setColor(Cast.toColor(clr));
         g.drawRect((int)padding.left,
                    (int)padding.top,
                    (int)(size.width  - padding.getLeftAndRight()),
                    (int)(size.height - padding.getTopAndBottom()));

         // test margin
         g.setStroke(new BasicStroke(3));
         clr = Color.DarkGreen.clone();
         clr.setA(120);
         g.setColor(Cast.toColor(clr));
         g.drawRect((int)(padding.left + margin.left),
                    (int)(padding.top  + margin.top),
                    (int)(size.width  - padding.getLeftAndRight() - margin.getLeftAndRight()),
                    (int)(size.height - padding.getTopAndBottom() - margin.getTopAndBottom()));
      }
      /**/

      if (_DEBUG_DRAW_FLOW) {
         System.out.println("< MosaicAndroidView.draw: cnt=" + tmp);
         System.out.println("-------------------------------");
      }

      // restore
      g.restore();

      _alreadyPainted = false;
   }

   private RectDouble getStringBounds(String text) {
      RectDouble r;
      if (true) {
         Rect r2 = new Rect();
         _textPaint.getTextBounds(text, 0, text.length(), r2);
         r = new RectDouble(r2.left, r2.top, r2.width(), r2.height());
      } else {
         float textWidth = _textPaint.measureText(text);
         r = new RectDouble(textWidth, getModel().getFontInfo().getSize());
      }
      return r;
   }

   private void drawText(Canvas g, String text, RectDouble rc) {
      if ((text == null) || text.trim().isEmpty())
         return;
      RectDouble bnd = getStringBounds(text);
//      { // test
//         java.awt.Color clrOld = g.getColor();
//         g.setColor(java.awt.Color.BLUE);
//         g.fillRect((int)rc.x, (int)rc.y, (int)rc.width, (int)rc.height);
//         g.setColor(clrOld);
//      }
      g.drawText(text, (float)(rc.x       +(rc.width -bnd.width )/2.),
                       (float)(rc.bottom()-(rc.height-bnd.height)/2.), _textPaint);
   }

   @Override
   protected void onPropertyModelChanged(Object oldValue, Object newValue, String propertyName) {
      super.onPropertyModelChanged(oldValue, newValue, propertyName);
      if (MosaicDrawModel.PROPERTY_FONT_INFO.equals(propertyName)) {
         FontInfo fi = getModel().getFontInfo();
         Typeface tf;
         try {
            tf = Typeface.create(fi.getName(), fi.isBold() ? Typeface.BOLD : Typeface.NORMAL);
         } catch(Throwable ex) {
            ex.printStackTrace(System.err);
            tf = Typeface.create(Typeface.DEFAULT, fi.isBold() ? Typeface.BOLD : Typeface.NORMAL);
         }
         _textPaint.setTypeface(tf);
         _textPaint.setTextSize((float)fi.getSize());
      }
   }

}
