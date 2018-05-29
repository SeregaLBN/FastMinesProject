package fmg.jfx.mosaic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javafx.collections.ObservableList;
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

/** MVC: view. JavaFX implementation over node-control {@link Canvas} or {@link Pane} */
public class MosaicNodeView extends AMosaicViewJfx<Node, Image, MosaicDrawModel<Image>> {

   private boolean _simpleDrawMode = false;

   private CanvasJfx _canvas = new CanvasJfx(this);
   private Pane _pane = new Pane();
   private List<Canvas> _cellCanvases;
   private boolean _updateClips = true;

   private Flag.ControllerImage _imgFlag = new Flag.ControllerImage();
   private Mine.ControllerImage _imgMine = new Mine.ControllerImage();

   public MosaicNodeView() {
      super(new MosaicDrawModel<Image>());
      changeSizeImagesMineFlag();
   }

   @Override
   protected Node createImage() { return _simpleDrawMode ? _canvas.create() : _pane; }

   private List<Canvas> getCellCanvases() {
       if (_cellCanvases == null) {
           int cellsSize = getModel().getMatrix().size();
           _cellCanvases = new ArrayList<>(cellsSize);
           for (int i=0; i < cellsSize; ++i)
               _cellCanvases.add(new Canvas());
           _pane.getChildren().addAll(_cellCanvases);
       }
       return _cellCanvases;
   }

   @Override
   public void draw(Collection<BaseCell> modifiedCells) {
      MosaicDrawModel<Image> model = getModel();
      if (!_simpleDrawMode) {
         BoundDouble padding = model.getPadding();
         BoundDouble margin  = model.getMargin();
         SizeDouble offset = new SizeDouble(margin.left + padding.left,
                                            margin.top  + padding.top);
         Matrisize sizeField = model.getSizeField();
         if (modifiedCells == null)
             modifiedCells = model.getMatrix();
         List<Canvas> cellCanvases = getCellCanvases();
         for (BaseCell cell : modifiedCells) {
            Coord c = cell.getCoord();
            Canvas canvas = cellCanvases.get(c.x*sizeField.n + c.y);

            RectDouble rc = cell.getRcOuter();
            if (_updateClips) {
                canvas.relocate(rc.x, rc.y);
                canvas. setWidth(rc.width);
                canvas.setHeight(rc.height);

                SizeDouble offset2 = new SizeDouble(offset.width-rc.x, offset.height-rc.y);
                RegionDouble poly = RegionDouble.moveXY(cell.getRegion(), offset2);

                // ограничиваю рисование только границами своей фигуры
                canvas.setClip(new Polygon(Cast.toPolygon(poly)));
            }

            draw(canvas.getGraphicsContext2D(), Arrays.asList(cell), null, false, -rc.x, -rc.y);
         }
         _updateClips = false;

         return;
      }


      if (modifiedCells == null) {
         draw(_canvas.getGraphics(), model.getMatrix(), null, true);
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
         case MosaicGameModel.PROPERTY_SIZE_FIELD:
       //case MosaicGameModel.PROPERTY_MATRIX:
            // update _cellCanvases
            {
                List<Canvas> cellCanvases = getCellCanvases();
                int cellCanvasesSize = cellCanvases.size();
                int cellsSize = getModel().getMatrix().size();
                if (cellsSize == cellCanvasesSize)
                    break;
                ObservableList<Node> childs = _pane.getChildren();
                if (cellsSize > cellCanvasesSize) {
                    // add new
                    List<Canvas> newCellCanvases = new ArrayList<>(cellsSize - cellCanvasesSize);
                    for (int i=cellCanvasesSize; i < cellsSize; ++i)
                        newCellCanvases.add(new Canvas());
                    cellCanvases.addAll(newCellCanvases);
                    childs.addAll(newCellCanvases);
                } else {
                    // if (cellsSize < cellCanvasesSize)
                    // remove olds
                    cellCanvases.subList(cellsSize, cellCanvasesSize).clear();
                    childs.remove(cellsSize, cellCanvasesSize);
                }
            }
            break;
         case MosaicGameModel.PROPERTY_AREA:
             SizeDouble size = getModel().getSizeDouble();
             _pane.setPrefSize(size.width, size.height);

             _updateClips = true;
            break;
         case MosaicGameModel.PROPERTY_MOSAIC_TYPE:
             _updateClips = true;
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
      getCellCanvases().clear();
      _cellCanvases = null;
      _pane.getChildren().clear();
      _pane = null;
      _canvas = null;
      _imgFlag.close();
      _imgMine.close();
   }

}
