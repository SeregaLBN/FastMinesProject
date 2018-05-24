package fmg.jfx.mosaic;

import java.util.Collection;

import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;

import fmg.common.geom.RectDouble;
import fmg.core.mosaic.MosaicGameModel;
import fmg.core.mosaic.cells.BaseCell;
import fmg.core.mosaic.draw.MosaicDrawModel;
import fmg.jfx.draw.img.CanvasJfx;
import fmg.jfx.draw.img.Flag;
import fmg.jfx.draw.img.Mine;
import fmg.jfx.utils.ImgUtils;

/** MVC: view. JavaFX implementation over control {@link Canvas} */ // TODO ?? rename to MosaicViewJfxCanvas
public class MosaicViewJfx extends AMosaicViewJfx<Canvas, Image, MosaicDrawModel<Image>> {

   private CanvasJfx canvas = new CanvasJfx(this);

   private Flag.ControllerImage _imgFlag = new Flag.ControllerImage();
   private Mine.ControllerImage _imgMine = new Mine.ControllerImage();

   public MosaicViewJfx() {
      super(new MosaicDrawModel<Image>());
   }

   @Override
   protected javafx.scene.canvas.Canvas createImage() { return canvas.create(); }

   @Override
   public void draw(Collection<BaseCell> modifiedCells) {
      if (modifiedCells == null) {
         draw(canvas.getGraphics(), getModel().getMatrix(), null, true);
         return;
      }
      RectDouble rcClip = null;
      for (BaseCell cell : modifiedCells) {
         RectDouble rc = cell.getRcOuter();
         if (rcClip == null) {
            rcClip = rc;
         } else {
            rcClip.x    = Math.min(rcClip.x       , rc.x);
            rcClip.y    = Math.min(rcClip.y       , rc.y);
            rcClip.right( Math.max(rcClip.right() , rc.right()));
            rcClip.bottom(Math.max(rcClip.bottom(), rc.bottom()));
         }
      }
      draw(canvas.getGraphics(), modifiedCells, rcClip, true);
   }

   @Override
   public void invalidate() {
      super.invalidate();
      getImage(); // implicit call draw() -> drawBegin() -> this.draw(...)
   }

   @Override
   protected void onPropertyModelChanged(Object oldValue, Object newValue, String propertyName) {
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
      canvas = null;
      _imgFlag.close();
      _imgMine.close();
   }

}
