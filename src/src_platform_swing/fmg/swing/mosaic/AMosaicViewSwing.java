package fmg.swing.mosaic;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.function.Consumer;

import javax.swing.UIDefaults;
import javax.swing.UIManager;

import fmg.common.Color;
import fmg.common.geom.*;
import fmg.core.mosaic.AMosaicView;
import fmg.core.mosaic.cells.BaseCell;
import fmg.core.mosaic.draw.MosaicDrawModel;
import fmg.core.mosaic.draw.MosaicDrawModel.BackgroundFill;
import fmg.core.types.EClose;
import fmg.core.types.EOpen;
import fmg.core.types.EState;
import fmg.data.view.draw.FontInfo;
import fmg.data.view.draw.PenBorder;
import fmg.swing.Cast;
import fmg.swing.draw.img.StaticInitilizer;

/** MVC: view. Abstract SWING implementation */
public abstract class AMosaicViewSwing<TImage,
                                       TImage2,
                                       TMosaicModel extends MosaicDrawModel<TImage2>>
                extends AMosaicView<TImage, TImage2, TMosaicModel>
{

   private Font _font;
   protected boolean _alreadyPainted = false;

   protected AMosaicViewSwing(TMosaicModel mosaicModel) {
      super(mosaicModel);
   }


   static {
      StaticInitilizer.init();

      UIDefaults uiDef = UIManager.getDefaults();
      java.awt.Color clr = uiDef.getColor("Panel.background");
      if (clr != null)
         MosaicDrawModel.setDefaultBackgroundColor(Cast.toColor(clr));
   }


   protected void draw(Graphics2D g, Collection<BaseCell> modifiedCells, RectDouble clipRegion, boolean drawBk) {
      assert !_alreadyPainted;
      _alreadyPainted = true;

      TMosaicModel model = getModel();

      // save
      Shape oldShape = g.getClip();
      java.awt.Color oldColor = g.getColor();
      Stroke oldStroke = g.getStroke();
      Font oldFont = g.getFont();

      // 1. background color
      Color bkClr = model.getBackgroundColor();
      if (drawBk && !bkClr.isTransparent()) {
         Consumer<java.awt.Color> fillBk = bkColor -> {
            g.setColor(bkColor);
            if (clipRegion == null) {
               Rectangle rcBounds = g.getClipBounds();
               if (rcBounds != null) {
                  g.fillRect(rcBounds.x, rcBounds.y, rcBounds.width, rcBounds.height);
               } else {
                  Size sz = model.getSize();
                  g.fillRect(0, 0, sz.width, sz.height);
               }
            } else {
               g.fillRect((int)clipRegion.x, (int)clipRegion.y, (int)clipRegion.width, (int)clipRegion.height);
            }
         };
         if (!bkClr.isOpaque())
            fillBk.accept(java.awt.Color.WHITE);
         fillBk.accept(Cast.toColor(bkClr));
      }

      // 2. paint cells
      g.setFont(getFont());
      PenBorder pen = model.getPenBorder();
      g.setStroke(new BasicStroke(pen.getWidth())); // TODO глянуть расширенные параметры конструктора пера
      BoundDouble padding = model.getPadding();
      BoundDouble margin  = model.getMargin();
      SizeDouble offset = new SizeDouble(margin.left + padding.left,
                                         margin.top  + padding.top);
      boolean isIconicMode = pen.getColorLight().equals(pen.getColorShadow());
      BackgroundFill bkFill = model.getBackgroundFill();

      if (modifiedCells == null)
         modifiedCells = model.getMatrix(); // check to redraw all mosaic cells
      for (BaseCell cell: modifiedCells)
         // redraw only when needed - when the cells and update region intersect
         if ((clipRegion == null) ||
              cell.getRcOuter().moveXY(offset.width, offset.height).intersection(clipRegion))
         {
            RectDouble rcInner = cell.getRcInner(pen.getWidth());

            // ограничиваю рисование только границами своей фигуры
            g.setClip(Cast.toPolygon(RegionDouble.moveXY(cell.getRegion(), offset)));

            { // 2.1. paint component

               // 2.1.1. paint background
               //if (isIconicMode) // когда русуется иконка, а не игровое поле, - делаю попроще...
               {
                  Color bkClrCell = cell.getBackgroundFillColor(bkFill.getMode(),
                                                                bkClr,
                                                                bkFill.getColors());
                  if (!bkClrCell.equals(bkClr)) {
                     g.setColor(Cast.toColor(bkClrCell));
                     g.fillPolygon(Cast.toPolygon(RegionDouble.moveXY(cell.getRegion(), offset)));
                  }
               }

             //g.setColor(java.awt.Color.MAGENTA);
             //g.drawRect((int)rcInner.x, (int)rcInner.y, (int)rcInner.width, (int)rcInner.height);

               Consumer<TImage2> paintImage = img -> {
                  int x = (int)(rcInner.x + offset.width);
                  int y = (int)(rcInner.y + offset.height);
                  if (img instanceof javax.swing.Icon) {
                     ((javax.swing.Icon)img).paintIcon(null/*p.getOwner()*/, g, x, y);
                  } else
                  if (img instanceof java.awt.Image) {
                     g.drawImage((java.awt.Image)img, x, y, null);
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
                  (cell.getState().getStatus() == EState._Open ) &&
                  (cell.getState().getOpen() == EOpen._Mine))
               {
                  paintImage.accept(model.getImgMine());
               } else
               // 2.1.3. output text
               {
                  String szCaption;
                  if (cell.getState().getStatus() == EState._Close) {
                     g.setColor(Cast.toColor(model.getColorText().getColorClose(cell.getState().getClose().ordinal())));
                     szCaption = cell.getState().getClose().toCaption();
                   //szCaption = cell.getCoord().x + ";" + cell.getCoord().y; // debug
                   //szCaption = ""+cell.getDirection(); // debug
                  } else {
                     g.setColor(Cast.toColor(model.getColorText().getColorOpen(cell.getState().getOpen().ordinal())));
                     szCaption = cell.getState().getOpen().toCaption();
                  }
                  if ((szCaption != null) && (szCaption.length() > 0))
                  {
                     rcInner.moveXY(offset.width, offset.height);
                     if (cell.getState().isDown())
                        rcInner.moveXY(1, 1);
                     drawText(g, szCaption, Cast.toRect(rcInner));
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
               g.setColor(Cast.toColor(down
                                          ? pen.getColorLight()
                                          : pen.getColorShadow()));
               if (isIconicMode) {
                  g.drawPolygon(Cast.toPolygon(RegionDouble.moveXY(cell.getRegion(), offset)));
               } else {
                  int s = cell.getShiftPointBorderIndex();
                  int v = cell.getAttr().getVertexNumber(cell.getDirection());
                  for (int i=0; i<v; i++) {
                     PointDouble p1 = cell.getRegion().getPoint(i);
                     PointDouble p2 = (i != (v-1))
                                          ? cell.getRegion().getPoint(i+1)
                                          : cell.getRegion().getPoint(0);
                     if (i==s)
                        g.setColor(Cast.toColor(down
                                                   ? pen.getColorShadow()
                                                   : pen.getColorLight()));
                     g.drawLine((int)(p1.x+offset.width), (int)(p1.y+offset.height), (int)(p2.x+offset.width), (int)(p2.y+offset.height));
                  }
               }

               // debug - визуально проверяю верность вписанного квадрата (проверять при ширине пера около 21)
             //g.setColor(java.awt.Color.MAGENTA);
             //g.drawRect((int)rcInner.x, (int)rcInner.y, (int)rcInner.width, (int)rcInner.height);
            }
         }

      // restore
      g.setFont(oldFont);
      g.setStroke(oldStroke);
      g.setColor(oldColor);
      g.setClip(oldShape);

      _alreadyPainted = false;
   }

   private static Rectangle2D getStringBounds(String text, Font font) {
      TextLayout tl = new TextLayout(text, font, new FontRenderContext(null, true, true));
      return tl.getBounds();
//      return font.getStringBounds(text, new FontRenderContext(null, true, true));
   }

   private static void drawText(Graphics g, String text, Rectangle rc) {
      if ((text == null) || text.trim().isEmpty())
         return;
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
