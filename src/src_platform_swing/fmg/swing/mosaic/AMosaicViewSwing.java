package fmg.swing.mosaic;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
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

/** MVC: view. Abstract SWING implementation
 * @param <TImage> plaform specific view/image/picture or other display context/canvas/window/panel
 * @param <TImage2> image type of flag/mine into mosaic field
 * @param <TMosaicModel> mosaic data model
 */
public abstract class AMosaicViewSwing<TImage,
                                       TImage2,
                                       TMosaicModel extends MosaicDrawModel<TImage2>>
                extends AMosaicView<TImage, TImage2, TMosaicModel>
{

   private Font _font;
   private static final FontRenderContext _frc = new FontRenderContext(null, true, true);
   /** cached TextLayout for quick drawing */
   private final Map<String /* text */, TextLayout> _mapTextLayout = new HashMap<>();
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
      Size size = model.getSize();

      // save
      Shape oldShape = g.getClip();
      java.awt.Color oldColor = g.getColor();
      Stroke oldStroke = g.getStroke();
      Font oldFont = g.getFont();

      // 1. background color
      Color bkClr = model.getBackgroundColor();
      if (drawBk) {
         g.setComposite(AlphaComposite.Src);
         g.setColor(Cast.toColor(bkClr));
         if (clipRegion == null)
            g.fillRect(0, 0, size.width, size.height);
         else
            g.fillRect((int)clipRegion.x, (int)clipRegion.y, (int)clipRegion.width, (int)clipRegion.height);
      }

      // 2. paint cells
      g.setComposite(AlphaComposite.SrcOver);
      g.setFont(getFont());
      PenBorder pen = model.getPenBorder();
      g.setStroke(new BasicStroke((float)pen.getWidth()));
      BoundDouble padding = model.getPadding();
      BoundDouble margin  = model.getMargin();
      SizeDouble offset = new SizeDouble(margin.left + padding.left,
                                         margin.top  + padding.top);
      boolean isIconicMode = pen.getColorLight().equals(pen.getColorShadow());
      BackgroundFill bkFill = model.getBackgroundFill();

      Collection<BaseCell> toCheck;
      if ((clipRegion != null) || (modifiedCells == null))
         toCheck = model.getMatrix(); // check to redraw all mosaic cells
      else
         toCheck = modifiedCells;

      if (_DEBUG_DRAW_FLOW) {
         String sufix = "; clipReg=" + clipRegion + "; drawBk=" + drawBk;
         if (modifiedCells == null)
            System.out.println("> AMosaicViewSwing.draw: all=" + toCheck.size() + sufix);
         else
         if ((modifiedCells == model.getMatrix()) || (modifiedCells.size() == model.getMatrix().size()))
            System.out.println("> AMosaicViewSwing.draw: all=" + modifiedCells.size() + sufix);
         else
            System.out.println("> AMosaicViewSwing.draw: cnt=" + modifiedCells.size() + sufix);
      }
      int tmp = 0;

      for (BaseCell cell: toCheck) {
         // redraw only when needed...
         if ((toCheck == modifiedCells) || // check reference equals
             ((modifiedCells != null) && (modifiedCells.contains(cell))) || // ..when the cell is explicitly specified
             ((clipRegion != null) && cell.getRcOuter().moveXY(offset.width, offset.height).intersection(clipRegion))) // ...when the cells and update region intersect
         {
            ++tmp;
            RectDouble rcInner = cell.getRcInner(pen.getWidth());
            Polygon poly = Cast.toPolygon(RegionDouble.moveXY(cell.getRegion(), offset));

            // ограничиваю рисование только границами своей фигуры
            g.setClip(poly);

            { // 2.1. paint component

               // 2.1.1. paint background
               //if (isIconicMode) // когда русуется иконка, а не игровое поле, - делаю попроще...
               {
                  Color bkClrCell = cell.getBackgroundFillColor(bkFill.getMode(),
                                                                bkClr,
                                                                bkFill.getColors());
                  if (!drawBk || !bkClrCell.equals(bkClr)) {
                     g.setColor(Cast.toColor(bkClrCell));
                     g.fillPolygon(poly);
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
                  (cell.getState().getStatus() == EState._Open) &&
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
               g.setColor(Cast.toColor(down
                                          ? pen.getColorLight()
                                          : pen.getColorShadow()));
               if (isIconicMode) {
                  g.drawPolygon(poly);
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
         System.out.println("< AMosaicViewSwing.draw: cnt=" + tmp);
         System.out.println("-------------------------------");
      }

      // restore
      g.setFont(oldFont);
      g.setStroke(oldStroke);
      g.setColor(oldColor);
      g.setClip(oldShape);

      _alreadyPainted = false;
   }

   private Rectangle2D getStringBounds(String text) {
      TextLayout tl = _mapTextLayout.get(text);
      if (tl == null) {
         tl = new TextLayout(text, getFont(), _frc);
         _mapTextLayout.put(text, tl);
      }
      return tl.getBounds();
//      return font.getStringBounds(text, new FontRenderContext(null, true, true));
   }

   private void drawText(Graphics g, String text, RectDouble rc) {
      if ((text == null) || text.trim().isEmpty())
         return;
      Rectangle2D bnd = getStringBounds(text);
//      { // test
//         java.awt.Color clrOld = g.getColor();
//         g.setColor(java.awt.Color.BLUE);
//         g.fillRect((int)rc.x, (int)rc.y, (int)rc.width, (int)rc.height);
//         g.setColor(clrOld);
//      }
      g.drawString(text,
            (int)(rc.x       +(rc.width -bnd.getWidth ())/2.),
            (int)(rc.bottom()-(rc.height-bnd.getHeight())/2.));
   }

   protected Font getFont() {
      if (_font == null) {
         FontInfo fi = getModel().getFontInfo();
         _font = new Font(fi.getName(), fi.isBold() ? Font.BOLD : Font.PLAIN, (int)fi.getSize());
      }
      return _font;
   }

   @Override
   protected void onPropertyModelChanged(Object oldValue, Object newValue, String propertyName) {
      super.onPropertyModelChanged(oldValue, newValue, propertyName);
      if (MosaicDrawModel.PROPERTY_FONT_INFO.equals(propertyName)) {
         _font = null;
         _mapTextLayout.clear();
      }
   }

   @Override
   public void close() {
      _mapTextLayout.clear();
      super.close();
   }

}
