package fmg.jfx.mosaic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Polygon;

import fmg.common.geom.*;
import fmg.core.mosaic.MosaicGameModel;
import fmg.core.mosaic.cells.BaseCell;
import fmg.core.mosaic.draw.MosaicDrawModel;
import fmg.jfx.Cast;
import fmg.jfx.draw.img.CanvasJfx;
import fmg.jfx.draw.img.Flag;
import fmg.jfx.draw.img.Mine;
import fmg.jfx.utils.ImgUtils;

/** MVC: view. JavaFX implementation over control {@link Canvas} or {@link Pane} */
public class MosaicNodeView extends AMosaicViewJfx<Node, Image, MosaicDrawModel<Image>> {

   private boolean _simpleDrawMode = !false;

   private CanvasJfx _canvas = new CanvasJfx(this);
   private Pane _pane = new Pane();
   private List<Canvas> _cellViews = new ArrayList<>();

   private Flag.ControllerImage _imgFlag = new Flag.ControllerImage();
   private Mine.ControllerImage _imgMine = new Mine.ControllerImage();

   public MosaicNodeView() {
      super(new MosaicDrawModel<Image>());
      changeSizeImagesMineFlag();
   }

   @Override
   protected Node createImage() { return _simpleDrawMode ? _canvas.create() : _pane; }

   @Override
   public void draw(Collection<BaseCell> modifiedCells) {
      if (!_simpleDrawMode) {
         MosaicDrawModel<Image> model = getModel();
         BoundDouble padding = model.getPadding();
         BoundDouble margin  = model.getMargin();
         SizeDouble offset = new SizeDouble(margin.left + padding.left,
                                            margin.top  + padding.top);
         Matrisize sizeField = model.getSizeField();
         for (BaseCell cell : modifiedCells) {
            Coord c = cell.getCoord();
            Canvas canvas = _cellViews.get(c.x*sizeField.n + c.y);

            RegionDouble poly = RegionDouble.moveXY(cell.getRegion(), offset);

            // ограничиваю рисование только границами своей фигуры
            canvas.setClip(new Polygon(Cast.toPolygon(poly)));

            draw(canvas.getGraphicsContext2D(), getModel().getMatrix(), null, false);
         }

         return;
      }


      if (modifiedCells == null) {
         draw(_canvas.getGraphics(), getModel().getMatrix(), null, true);
         return;
      }
      double minX=0, minY=0, maxX=0, maxY=0;
      boolean first = true;
      for (BaseCell cell : modifiedCells) {
         RectDouble rc = cell.getRcOuter();
         if (first) {
            first = false;
            minX = rc.x;
            minY = rc.y;
            maxX = rc.right();
            maxY = rc.bottom();
         } else {
            minX = Math.min(minX, rc.x);
            minY = Math.min(minY, rc.y);
            maxX = Math.max(maxX, rc.right());
            maxY = Math.max(maxY, rc.bottom());
         }
      }
      RectDouble rcClip = new RectDouble(minX, minY, maxX-minX, maxY-minY);
//      if (_DEBUG_DRAW_FLOW)
//         System.out.println("MosaicViewJfx.draw: repaint=" + rcClip);
      draw(_canvas.getGraphics(), modifiedCells, rcClip, true);
   }

   @Override
   public void invalidate() {
      super.invalidate();
      getImage(); // implicit call draw() -> drawBegin() -> this.draw(...)
   }

   @Override
   protected void onPropertyModelChanged(Object oldValue, Object newValue, String propertyName) {
      if (!_simpleDrawMode)
         switch (propertyName) {
         case MosaicGameModel.PROPERTY_MATRIX:
            // TODO uupdate _cellViews
            break;
         case MosaicGameModel.PROPERTY_AREA:
            break;
         }
      super.onPropertyModelChanged(oldValue, newValue, propertyName);
      switch (propertyName) {
      case MosaicGameModel.PROPERTY_MOSAIC_TYPE:
      case MosaicGameModel.PROPERTY_AREA:
         changeSizeImagesMineFlag();
         break;
      }
   }

   /** переустанавливаю заного размер мины/флага для мозаики */
   protected void changeSizeImagesMineFlag() {
      MosaicDrawModel<Image> model = getModel();
      int sq = (int)model.getCellAttr().getSq(model.getPenBorder().getWidth());
      if (sq <= 0) {
         System.err.println("Error: too thick pen! There is no area for displaying the flag/mine image...");
         sq = 3; // ат балды...
      }

      final int max = 30;
      if (sq > max) {
         _imgFlag.getModel().setSize(sq);
         _imgMine.getModel().setSize(sq);
         model.setImgFlag(_imgFlag.getImage());
         model.setImgMine(_imgMine.getImage());
      } else {
         _imgFlag.getModel().setSize(max);
         _imgMine.getModel().setSize(max);
         model.setImgFlag(ImgUtils.zoom(_imgFlag.getImage(), sq, sq));
         model.setImgMine(ImgUtils.zoom(_imgMine.getImage(), sq, sq));
      }
   }

   @Override
   public void close() {
      super.close();
      _pane = null;
      _pane.getChildren().clear();
      _cellViews.clear();
      _cellViews = null;
      _canvas = null;
      _imgFlag.close();
      _imgMine.close();
   }

}
