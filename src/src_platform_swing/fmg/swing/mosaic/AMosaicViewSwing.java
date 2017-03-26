package fmg.swing.mosaic;

import javax.swing.Icon;

import fmg.core.mosaic.AMosaicView;
import fmg.swing.draw.img.Flag;
import fmg.swing.draw.img.Mine;
import fmg.swing.draw.mosaic.PaintSwingContext;
import fmg.swing.draw.mosaic.graphics.CellPaintGraphics;
import fmg.swing.draw.mosaic.graphics.PaintableGraphics;
import fmg.swing.utils.ImgUtils;

/** MVC: view. SWING implementation */
public abstract class AMosaicViewSwing extends AMosaicView<PaintableGraphics, Icon, PaintSwingContext<Icon>> {

   private CellPaintGraphics<Icon> _cellPaint;

   @Override
   public CellPaintGraphics<Icon> getCellPaint() {
      if (_cellPaint == null) {
         _cellPaint = new CellPaintGraphics.Icon();
      }
      return _cellPaint;
   }

   /** переустанавливаю заного размер мины/флага для мозаики */
   @Override
   protected void changeSizeImagesMineFlag() {
      PaintSwingContext<Icon> pc = getPaintContext();
      int sq = (int)getMosaic().getCellAttr().getSq(pc.getPenBorder().getWidth());
      if (sq <= 0) {
         System.err.println("Error: слишком толстое перо! Нет области для вывода картиники флага/мины...");
         sq = 3; // ат балды...
      }
      pc.setImgFlag(ImgUtils.zoom(new Flag(), sq, sq));
      pc.setImgMine(ImgUtils.zoom(new Mine(), sq, sq));
   }

}
