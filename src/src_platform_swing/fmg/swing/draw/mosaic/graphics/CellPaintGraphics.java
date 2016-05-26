package fmg.swing.draw.mosaic.graphics;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;

import javax.swing.Icon;

import fmg.common.geom.BoundDouble;
import fmg.common.geom.PointDouble;
import fmg.common.geom.RectDouble;
import fmg.common.geom.RegionDouble;
import fmg.common.geom.SizeDouble;
import fmg.core.mosaic.cells.BaseCell;
import fmg.core.mosaic.draw.PaintContext;
import fmg.core.types.EClose;
import fmg.core.types.EOpen;
import fmg.core.types.EState;
import fmg.swing.Cast;
import fmg.swing.draw.mosaic.CellPaint;

/**
 * Class for drawing cell into {@link java.awt.Graphics}
 *
 * @param <TImage> SWING specific image: {@link java.awt.Image} or {@link javax.swing.Icon}
 */
public class CellPaintGraphics<TImage> extends CellPaint<PaintableGraphics, TImage> {

   /** @see javax.swing.JComponent.paint */
   @Override
   public void paint(BaseCell cell, PaintableGraphics p) {
//      Object obj = this;
//      if (obj instanceof JComponent) {
//         JComponent This = (JComponent)obj;
//         This.paint(g);
//      } else
      {
         Graphics2D g2d = (Graphics2D)p.getGraphics();;

         // save
         Shape shapeOld = g2d.getClip();

         // ограничиваю рисование только границами своей фигуры
         SizeDouble offset = new SizeDouble(paintContext.getPadding().left, paintContext.getPadding().top);
         g2d.setClip(Cast.toPolygon(RegionDouble.moveXY(cell.getRegion(), offset)));

         // all paint
         this.paintComponent(cell, p);
         this.paintBorder(cell, p);

         // restore
         g2d.setClip(shapeOld);
      }
   }

   /** @see javax.swing.JComponent.paintBorder */
   @Override
   public void paintBorder(BaseCell cell, PaintableGraphics p) {
//      Object obj = this;
//      if (obj instanceof JComponent) {
//         JComponent This = (JComponent)obj;
//         This.paintBorder(g);
//         super.paintBorder(g);
//         return;
//      }

      Graphics2D g2 = (Graphics2D) p.getGraphics();
      // save
      Stroke strokeOld = g2.getStroke();
      Object oldValAntialiasing = g2.getRenderingHint(RenderingHints.KEY_ANTIALIASING);

      // set my custom params
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // TODO для релиза сменить на VALUE_ANTIALIAS_ON
      g2.setStroke(new BasicStroke(paintContext.getPenBorder().getWidth())); // TODO глянуть расширенные параметры конструктора пера

      // draw lines
      paintBorderLines(cell, p);

      // debug - визуально проверяю верность вписанного квадрата (проверять при ширине пера около 21)
//      Rect rcInner = cell.getRcInner(paintContext.getPenBorder().getWidth());
//      g.setColor(Color.MAGENTA);
//      g.drawRect(rcInner.x, rcInner.y, rcInner.width, rcInner.height);

      // restore
      g2.setStroke(strokeOld);
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldValAntialiasing);
   }

   /** draw border lines */
   @Override
   public void paintBorderLines(BaseCell cell, PaintableGraphics p) {
      SizeDouble offset = new SizeDouble(paintContext.getPadding().left, paintContext.getPadding().top);
      boolean down = cell.getState().isDown() || (cell.getState().getStatus() == EState._Open);
      Graphics g = p.getGraphics();
      if (paintContext.isIconicMode()) {
         g.setColor(Cast.toColor(down ? paintContext.getPenBorder().getColorLight() : paintContext.getPenBorder().getColorShadow()));
         g.drawPolygon(Cast.toPolygon(RegionDouble.moveXY(cell.getRegion(), offset)));
      } else {
         g.setColor(Cast.toColor(down ? paintContext.getPenBorder().getColorLight()  : paintContext.getPenBorder().getColorShadow()));
         int s = cell.getShiftPointBorderIndex();
         int v = cell.getAttr().getVertexNumber(cell.getDirection());
         for (int i=0; i<v; i++) {
            PointDouble p1 = cell.getRegion().getPoint(i);
            PointDouble p2 = (i != (v-1)) ? cell.getRegion().getPoint(i+1) : cell.getRegion().getPoint(0);
            if (i==s)
               g.setColor(Cast.toColor(down ? paintContext.getPenBorder().getColorShadow(): paintContext.getPenBorder().getColorLight()));
            g.drawLine((int)(p1.x+offset.width), (int)(p1.y+offset.height), (int)(p2.x+offset.width), (int)(p2.y+offset.height));
         }
      }
   }

   /** @see javax.swing.JComponent.paintComponent */
   @Override
   public void paintComponent(BaseCell cell, PaintableGraphics p) {
      Graphics g = p.getGraphics();
      Color colorOld = g.getColor();
      BoundDouble padding = paintContext.getPadding();

      paintComponentBackground(cell, p);

      RectDouble rcInner = cell.getRcInner(paintContext.getPenBorder().getWidth());
//      g.setColor(Color.MAGENTA);
//      g.drawRect(rcInner.x, rcInner.y, rcInner.width, rcInner.height);

      // output Pictures
      if ((paintContext.getImgFlag() != null) &&
         (cell.getState().getStatus() == EState._Close) &&
         (cell.getState().getClose() == EClose._Flag))
      {
         drawImage(paintContext.getOwner(), g, paintContext.getImgFlag(), (int)(rcInner.x+padding.left), (int)(rcInner.y+padding.top));
      } else
      if ((paintContext.getImgMine() != null) &&
         (cell.getState().getStatus() == EState._Open ) &&
         (cell.getState().getOpen() == EOpen._Mine))
      {
         drawImage(paintContext.getOwner(), g, paintContext.getImgMine(), (int)(rcInner.x+padding.left), (int)(rcInner.y+padding.top));
      } else
      // output text
      {
         String szCaption;
         if (cell.getState().getStatus() == EState._Close) {
            g.setColor(Cast.toColor(paintContext.getColorText().getColorClose(cell.getState().getClose().ordinal())));
            szCaption = cell.getState().getClose().toCaption();
//            szCaption = cell.getCoord().x + ";" + cell.getCoord().y; // debug
//            szCaption = ""+cell.getDirection(); // debug
         } else {
            g.setColor(Cast.toColor(paintContext.getColorText().getColorOpen(cell.getState().getOpen().ordinal())));
            szCaption = cell.getState().getOpen().toCaption();
         }
         if ((szCaption != null) && (szCaption.length() > 0))
         {
            rcInner.moveXY(padding.left, padding.top);
            if (cell.getState().isDown())
               rcInner.moveXY(1, 1);
            DrawText(g, szCaption, Cast.toRect(rcInner));
//            { // test
//               Color clrOld = g.getColor(); // test
//               g.setColor(Color.red);
//               g.drawRect(rcInner.x, rcInner.y, rcInner.width, rcInner.height);
//               g.setColor(clrOld);
//            }
         }
      }

      // restore
      g.setColor(colorOld);
   }

   private static <TImage> void drawImage(Component c, Graphics g, TImage img, int x, int y) {
      if (img instanceof Icon) {
         ((Icon)img).paintIcon(c, g, x, y);
         return;
      }
      if (img instanceof Image) {
         g.drawImage((Image)img, x, y, null);
      }
      throw new RuntimeException("How to draw image " + img.getClass().getName() + "?");
   }

   /** залить ячейку нужным цветом */
   @Override
   public void paintComponentBackground(BaseCell cell, PaintableGraphics p) {
      Graphics g = p.getGraphics();
//      if (paintContext.isIconicMode()) // когда русуется иконка, а не игровое поле, - делаю попроще...
//         return;
      g.setColor(Cast.toColor(cell.getBackgroundFillColor(
            paintContext.getBackgroundFill().getMode(),
            PaintContext.getDefaultBackgroundFillColor(),
            paintContext.getBackgroundFill().getColors()
            )));
      SizeDouble offset = new SizeDouble(paintContext.getPadding().left, paintContext.getPadding().top);
      g.fillPolygon(Cast.toPolygon(RegionDouble.moveXY(cell.getRegion(), offset)));
   }

   private static Rectangle2D getStringBounds(String text, Font font) {
      TextLayout tl = new TextLayout(text, font, new FontRenderContext(null, true, true));
      return tl.getBounds();
//      return font.getStringBounds(text, new FontRenderContext(null, true, true));
   }
   public static void DrawText(Graphics g, String text, Rectangle rc) {
      if ((text == null) || text.trim().isEmpty())
         return;
      //DrawText(m_paintContext.m_hDCTmp, szCaption, -1, &sq_tmp, DT_CENTER | DT_VCENTER | DT_SINGLELINE);
      Rectangle2D bnd = getStringBounds(text, g.getFont());
//      { // test
//         Color clrOld = g.getColor();
//         g.setColor(Color.BLUE);
//         g.fillRect(rc.x, rc.y, rc.width, rc.height);
//         g.setColor(clrOld);
//      }
      g.drawString(text,
            rc.x          +(int)((rc.width -bnd.getWidth ())/2.),
            rc.y+rc.height-(int)((rc.height-bnd.getHeight())/2.));
   }

}