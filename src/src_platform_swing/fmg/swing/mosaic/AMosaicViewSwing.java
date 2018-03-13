package fmg.swing.mosaic;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.util.Collection;

import javax.swing.UIDefaults;
import javax.swing.UIManager;

import fmg.common.Color;
import fmg.common.geom.*;
import fmg.core.mosaic.AMosaicView;
import fmg.core.mosaic.cells.BaseCell;
import fmg.core.mosaic.draw.ICellPaint;
import fmg.core.mosaic.draw.MosaicDrawModel;
import fmg.core.types.EClose;
import fmg.core.types.EOpen;
import fmg.core.types.EState;
import fmg.data.view.draw.FontInfo;
import fmg.swing.Cast;
import fmg.swing.draw.mosaic.PaintSwingContext;
import fmg.swing.draw.mosaic.graphics.PaintableGraphics;

/** MVC: view. Abstract SWING implementation */
public abstract class AMosaicViewSwing<TImage,
                                       TImage2,
                                       TMosaicModel extends MosaicDrawModel<TImage2>>
                extends AMosaicView<TImage, TImage2, TMosaicModel>
{

   protected AMosaicViewSwing(TMosaicModel mosaicModel) {
      super(mosaicModel);
   }

   protected boolean _alreadyPainted = false;
   @Override
   public void repaint(Collection<BaseCell> modifiedCells, RectDouble clipRegion) {
      Graphics g = getPaintable();
      if (g == null)
         return;

      assert !_alreadyPainted;

      _alreadyPainted = true;
      try {
         PaintSwingContext<TImage> pc = getPaintContext();

         if (pc.isUseBackgroundColor()) {
            // background color
            g.setColor(Cast.toColor(pc.getBackgroundColor()));
            if (clipRegion == null) {
               Rectangle rcBounds = g.getClipBounds();
               if (rcBounds != null) {
                  g.fillRect(rcBounds.x, rcBounds.y, rcBounds.width, rcBounds.height);
               } else {
                  SizeDouble sz = getSize();
                  g.fillRect(0, 0, (int)sz.width, (int)sz.height);
               }
            } else {
               g.fillRect((int)clipRegion.x, (int)clipRegion.y, (int)clipRegion.width, (int)clipRegion.height);
            }
         }

         if (modifiedCells == null)
            modifiedCells = getMosaic().getMatrix(); // check to redraw all mosaic cells

         // paint cells
         g.setFont(pc.getFont());
         PaintableGraphics p = createPaintableGraphics(g);
         ICellPaint<PaintableGraphics, TImage, PaintSwingContext<TImage>> cellPaint = getCellPaint();
         double padX = pc.getPadding().left, padY = pc.getPadding().top;
         for (BaseCell cell: modifiedCells) {
            if ((clipRegion == null) || cell.getRcOuter().moveXY(padX, padY).intersection(clipRegion)) // redraw only when needed - when the cells and update region intersect
               cellPaint.paint(cell, p, pc);
         }
      } finally {
         _alreadyPainted = false;
      }
   }

   /** @see javax.swing.JComponent.paint */
   @Override
   public void paint(BaseCell cell, PaintableGraphics p, PaintSwingContext<TImage> paintContext) {
//      Object obj = this;
//      if (obj instanceof JComponent) {
//         JComponent This = (JComponent)obj;
//         This.paint(g);
//      } else
      {
         Graphics2D g2d = (Graphics2D)p.getGraphics();

         // save
         Shape shapeOld = g2d.getClip();

         // ограничиваю рисование только границами своей фигуры
         SizeDouble offset = new SizeDouble(paintContext.getPadding().left, paintContext.getPadding().top);
         g2d.setClip(Cast.toPolygon(RegionDouble.moveXY(cell.getRegion(), offset)));

         // all paint
         this.paintComponent(cell, p, paintContext);
         this.paintBorder(cell, p, paintContext);

         // restore
         g2d.setClip(shapeOld);
      }
   }

   /** @see javax.swing.JComponent.paintBorder */
   @Override
   protected void paintBorder(BaseCell cell, PaintableGraphics p, PaintSwingContext<TImage> paintContext) {
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
      paintBorderLines(cell, p, paintContext);

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
   protected void paintBorderLines(BaseCell cell, PaintableGraphics p, PaintSwingContext<TImage> paintContext) {
      SizeDouble offset = new SizeDouble(paintContext.getPadding().left, paintContext.getPadding().top);
      boolean down = cell.getState().isDown() || (cell.getState().getStatus() == EState._Open);
      Graphics g = p.getGraphics();
      g.setColor(Cast.toColor(down ? paintContext.getPenBorder().getColorLight() : paintContext.getPenBorder().getColorShadow()));
      if (paintContext.isIconicMode()) {
         g.drawPolygon(Cast.toPolygon(RegionDouble.moveXY(cell.getRegion(), offset)));
      } else {
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
   protected void paintComponent(BaseCell cell, PaintableGraphics p, PaintSwingContext<TImage> paintContext) {
      Graphics g = p.getGraphics();
      Color colorOld = g.getColor();
      BoundDouble padding = paintContext.getPadding();

      paintComponentBackground(cell, p, paintContext);

      RectDouble rcInner = cell.getRcInner(paintContext.getPenBorder().getWidth());
//      g.setColor(Color.MAGENTA);
//      g.drawRect(rcInner.x, rcInner.y, rcInner.width, rcInner.height);

      // output Pictures
      if ((paintContext.getImgFlag() != null) &&
         (cell.getState().getStatus() == EState._Close) &&
         (cell.getState().getClose() == EClose._Flag))
      {
         paintImage(cell, p, paintContext, paintContext.getImgFlag());
      } else
      if ((paintContext.getImgMine() != null) &&
         (cell.getState().getStatus() == EState._Open ) &&
         (cell.getState().getOpen() == EOpen._Mine))
      {
         paintImage(cell, p, paintContext, paintContext.getImgMine());
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

   /** залить ячейку нужным цветом */
   @Override
   protected void paintComponentBackground(BaseCell cell, PaintableGraphics p, PaintSwingContext<TImage> paintContext) {
      Graphics g = p.getGraphics();
//      if (paintContext.isIconicMode()) // когда русуется иконка, а не игровое поле, - делаю попроще...
//         return;
      g.setColor(Cast.toColor(cell.getBackgroundFillColor(
            paintContext.getBackgroundFill().getMode(),
            paintContext.getBackgroundColor(),
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

//   /////////////////////////////////////////////////////////////////////////////////////////////////////
//   //    custom implementations
//   /////////////////////////////////////////////////////////////////////////////////////////////////////
//
//   public static class Icon extends CellPaintGraphics<javax.swing.Icon> {
//
//      @Override
//      protected void paintImage(BaseCell cell, PaintableGraphics p, PaintSwingContext<javax.swing.Icon> paintContext, javax.swing.Icon img) {
//         Graphics g = p.getGraphics();
//         RectDouble rcInner = cell.getRcInner(paintContext.getPenBorder().getWidth());
//         BoundDouble padding = paintContext.getPadding();
//         int x = (int)(rcInner.x+padding.left);
//         int y = (int)(rcInner.y+padding.top);
//         img.paintIcon(p.getOwner(), g, x, y);
//      }
//
//   }
//
//   public static class Image extends CellPaintGraphics<java.awt.Image> {
//
//      @Override
//      protected void paintImage(BaseCell cell, PaintableGraphics p, PaintSwingContext<java.awt.Image> paintContext, java.awt.Image img) {
//         Graphics g = p.getGraphics();
//         RectDouble rcInner = cell.getRcInner(paintContext.getPenBorder().getWidth());
//         BoundDouble padding = paintContext.getPadding();
//         int x = (int)(rcInner.x+padding.left);
//         int y = (int)(rcInner.y+padding.top);
//         g.drawImage(img, x, y, null);
//      }
//
//   }
//

   @Override
   public void close() {
      super.close();
      setPaintable(null);
   }


   //public static final Font DEFAULT_FONT = new Font("SansSerif", Font.PLAIN, 10);

   private Font _font;

   public static Color getDefaultBackgroundColor() {
      return MosaicDrawModel.getDefaultBackgroundColor();
   }

   static {
      UIDefaults uiDef = UIManager.getDefaults();
      java.awt.Color clr = uiDef.getColor("Panel.background");
      if (clr == null)
         clr = java.awt.Color.GRAY;
      _defaultBkColor = Cast.toColor(clr);
   }

   protected Font getFont() {
      if (_font == null) {
         //setFont(DEFAULT_FONT);
         FontInfo fi = getModel().getFontInfo();
         _font = new Font(fi.getName(), fi.isBold() ? Font.BOLD : Font.PLAIN, fi.getSize());
      }
      return _font;
   }

   @Override
   protected void onPropertyModelChanged(Object oldValue, Object newValue, String propertyName) {
      super.onPropertyModelChanged(oldValue, newValue, propertyName);
      if (MosaicDrawModel.PROPERTY_FONT_INFO.equals(propertyName))
         _font = null;
   }

}
