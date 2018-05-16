package fmg.jfx.mosaic;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

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
import fmg.jfx.Cast;
import fmg.jfx.draw.img.StaticInitilizer;

/** MVC: view. Abstract JFX implementation */
public abstract class AMosaicViewJfx<TImage,
                                     TImage2,
                                     TMosaicModel extends MosaicDrawModel<TImage2>>
                 extends AMosaicView<TImage, TImage2, TMosaicModel>
{

   private Font _font;
   /** cached Text for quick drawing */
   private final Map<String /* text */, Text> _mapText = new HashMap<>();
   protected boolean _alreadyPainted = false;

   protected AMosaicViewJfx(TMosaicModel mosaicModel) {
      super(mosaicModel);
   }


   static {
      StaticInitilizer.init();

//      panel.setBackground(new Background(new BackgroundFill(Color.RED, CornerRadii.EMPTY, Insets.EMPTY)));
//      UIDefaults uiDef = UIManager.getDefaults();
//      java.awt.Color clr = uiDef.getColor("Panel.background");
//      if (clr != null)
//         MosaicDrawModel.setDefaultBackgroundColor(Cast.toColor(clr));
   }


   protected void draw(GraphicsContext g, Collection<BaseCell> modifiedCells, RectDouble clipRegion, boolean drawBk) {
      assert !_alreadyPainted;
      _alreadyPainted = true;

      TMosaicModel model = getModel();
      Size size = model.getSize();

      // save
//      Shape oldShape = g.getClip();
      Paint oldFill = g.getFill();
      Paint oldStroke = g.getStroke();
      Font oldFont = g.getFont();

      // 1. background color
      Color bkClr = model.getBackgroundColor();
      if (drawBk) {
         if (!bkClr.isOpaque())
            g.clearRect(0, 0, size.width, size.height);
         if (!bkClr.isTransparent()) {
            g.setFill(Cast.toColor(bkClr));
            if (clipRegion == null)
               g.fillRect(0, 0, size.width, size.height);
            else
               g.fillRect(clipRegion.x, clipRegion.y, clipRegion.width, clipRegion.height);
         }
      }

      // 2. paint cells
      g.setFont(getFont());
      PenBorder pen = model.getPenBorder();
      g.setLineWidth(pen.getWidth());
      BoundDouble padding = model.getPadding();
      BoundDouble margin  = model.getMargin();
      SizeDouble offset = new SizeDouble(margin.left + padding.left,
                                         margin.top  + padding.top);
      boolean isIconicMode = pen.getColorLight().equals(pen.getColorShadow());
      BackgroundFill bkFill = model.getBackgroundFill();

      if ((modifiedCells == null) || modifiedCells.isEmpty())
         modifiedCells = model.getMatrix(); // check to redraw all mosaic cells
      for (BaseCell cell: modifiedCells)
         // redraw only when needed - when the cells and update region intersect
         if ((clipRegion == null) ||
              cell.getRcOuter().moveXY(offset.width, offset.height).intersection(clipRegion))
         {
            RectDouble rcInner = cell.getRcInner(pen.getWidth());

            // ограничиваю рисование только границами своей фигуры
//            g.setClip(Cast.toPolygon(RegionDouble.moveXY(cell.getRegion(), offset)));

            { // 2.1. paint component

               // 2.1.1. paint background
               //if (isIconicMode) // когда русуется иконка, а не игровое поле, - делаю попроще...
               {
                  Color bkClrCell = cell.getBackgroundFillColor(bkFill.getMode(),
                                                                bkClr,
                                                                bkFill.getColors());
                  if (!bkClrCell.equals(bkClr)) {
                     RegionDouble poly = RegionDouble.moveXY(cell.getRegion(), offset);
                     double[] polyX = Cast.toPolygon(poly, true);
                     double[] polyY = Cast.toPolygon(poly, false);
                     g.setFill(Cast.toColor(bkClrCell));
                     g.fillPolygon(polyX, polyY, polyX.length);
                  }
               }

//             g.setStroke(Cast.toColor(Color.Magenta));
//             g.strokeRect(rcInner.x, rcInner.y, rcInner.width, rcInner.height);

               Consumer<TImage2> paintImage = img -> {
                  double x = rcInner.x + offset.width;
                  double y = rcInner.y + offset.height;
                  if (img instanceof Image) {
                     g.drawImage((Image)img, x, y);
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
                     g.setFill(Cast.toColor(model.getColorText().getColorClose(cell.getState().getClose().ordinal())));
                     szCaption = cell.getState().getClose().toCaption();
                   //szCaption = cell.getCoord().x + ";" + cell.getCoord().y; // debug
                   //szCaption = ""+cell.getDirection(); // debug
                  } else {
                     g.setFill(Cast.toColor(model.getColorText().getColorOpen(cell.getState().getOpen().ordinal())));
                     szCaption = cell.getState().getOpen().toCaption();
                  }
                  if ((szCaption != null) && (szCaption.length() > 0))
                  {
                     rcInner.moveXY(offset.width, offset.height);
                     if (cell.getState().isDown())
                        rcInner.moveXY(1, 1);
                     drawText(g, szCaption, Cast.toRect(rcInner));
                 //{ // test
                 //   Paint clrOld = g.getStroke(); // test
                 //   g.setStroke(Cast.toColor(Color.Red));
                 //   g.strokeRect(rcInner.x, rcInner.y, rcInner.width, rcInner.height);
                 //   g.setStroke(clrOld);
                 //}
                  }
               }

            }

            // 2.2. paint border
            {
               // draw border lines
               boolean down = cell.getState().isDown() || (cell.getState().getStatus() == EState._Open);
               g.setStroke(Cast.toColor(down
                                          ? pen.getColorLight()
                                          : pen.getColorShadow()));
               if (isIconicMode) {
                  RegionDouble poly = RegionDouble.moveXY(cell.getRegion(), offset);
                  double[] polyX = Cast.toPolygon(poly, true);
                  double[] polyY = Cast.toPolygon(poly, false);
                  g.strokePolygon(polyX, polyY, polyX.length);
               } else {
                  int s = cell.getShiftPointBorderIndex();
                  int v = cell.getAttr().getVertexNumber(cell.getDirection());
                  for (int i=0; i<v; i++) {
                     PointDouble p1 = cell.getRegion().getPoint(i);
                     PointDouble p2 = (i != (v-1))
                                          ? cell.getRegion().getPoint(i+1)
                                          : cell.getRegion().getPoint(0);
                     if (i==s)
                        g.setStroke(Cast.toColor(down
                                                   ? pen.getColorShadow()
                                                   : pen.getColorLight()));
                     g.strokeLine(p1.x+offset.width, p1.y+offset.height, p2.x+offset.width, p2.y+offset.height);
                  }
               }

               // debug - визуально проверяю верность вписанного квадрата (проверять при ширине пера около 21)
             //g.setColor(java.awt.Color.MAGENTA);
             //g.drawRect(rcInner.x, rcInner.y, rcInner.width, rcInner.height);
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
         g.drawRect(padding.left,
                    padding.top,
                    size.width  - padding.getLeftAndRight(),
                    size.height - padding.getTopAndBottom());

         // test margin
         g.setStroke(new BasicStroke(3));
         clr = Color.DarkGreen.clone();
         clr.setA(120);
         g.setColor(Cast.toColor(clr));
         g.drawRect(padding.left + margin.left,
                    padding.top  + margin.top,
                    size.width  - padding.getLeftAndRight() - margin.getLeftAndRight(),
                    size.height - padding.getTopAndBottom() - margin.getTopAndBottom());
      }
      /**/

      // restore
      g.setFont(oldFont);
      g.setStroke(oldStroke);
      g.setFill(oldFill);
//      g.setClip(oldShape);

      _alreadyPainted = false;
   }

   private Bounds getStringBounds(String text) {
      Text t = _mapText.get(text);
      if (t == null) {
         t = new Text(text);
         t.setFont(getFont());
         _mapText.put(text, t);
      }
      return t.getLayoutBounds();
   }

   private void drawText(GraphicsContext g, String text, Rectangle2D rc) {
      if ((text == null) || text.trim().isEmpty())
         return;
      Bounds bnd = getStringBounds(text);
//      { // test
//         Paint clrOld = g.getFill();
//         g.setFill(Cast.toColor(Color.Blue));
//         g.fillRect(rc.getMinX(), rc.getMinY(), rc.getWidth(), rc.getHeight());
//         g.setFill(clrOld);
//      }
      g.setTextAlign(TextAlignment.CENTER);
      g.setTextBaseline(VPos.CENTER);
      g.fillText(text,
                 rc.getMinX() + (rc.getWidth() -bnd.getWidth ())/2.,
                 rc.getMaxY() - (rc.getHeight()-bnd.getHeight())/2.);
   }

   protected Font getFont() {
      if (_font == null) {
         FontInfo fi = getModel().getFontInfo();
         _font = new Font(fi.getName(), /*fi.isBold() ? Font.BOLD : Font.PLAIN, */fi.getSize());
      }
      return _font;
   }

   @Override
   protected void onPropertyModelChanged(Object oldValue, Object newValue, String propertyName) {
      super.onPropertyModelChanged(oldValue, newValue, propertyName);
      if (MosaicDrawModel.PROPERTY_FONT_INFO.equals(propertyName)) {
         _font = null;
         _mapText.clear();
      }
   }

   @Override
   public void close() {
      _mapText.clear();
      super.close();
   }

}
