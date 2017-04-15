package fmg.swing.mosaic;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Collection;

import javax.swing.Icon;
import javax.swing.JPanel;

import fmg.common.geom.SizeDouble;
import fmg.core.mosaic.cells.BaseCell;
import fmg.swing.Cast;
import fmg.swing.draw.img.Flag;
import fmg.swing.draw.img.Mine;
import fmg.swing.draw.mosaic.PaintSwingContext;
import fmg.swing.draw.mosaic.graphics.CellPaintGraphics;
import fmg.swing.draw.mosaic.graphics.PaintableGraphics;
import fmg.swing.utils.ImgUtils;

/** MVC: view. SWING implementation for {@link Icon}. View located into control {@link JPanel} */ // TODO rename to MosaicViewSwingIcon
public class MosaicViewSwing extends AMosaicViewSwing<Icon> {

   private JPanel _control;
   private MosaicControllerSwing _controller;
   private CellPaintGraphics<Icon> _cellPaint;

   @Override
   public CellPaintGraphics<Icon> getCellPaint() {
      if (_cellPaint == null) {
         _cellPaint = new CellPaintGraphics.Icon();
      }
      return _cellPaint;
   }

   @Override
   protected PaintSwingContext<Icon> createPaintContext() {
      return new PaintSwingContext<>();
   }

   @Override
   protected PaintableGraphics createPaintableGraphics(Graphics g) {
      return new PaintableGraphics(getControl(), g);
   }

   public void setMosaicController(MosaicControllerSwing controller) {
      this._controller = controller;
   }

   public JPanel getControl() {
      if (_control == null) {
         _control = new JPanel() {
            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(Graphics g) {
               {
                  Graphics2D g2d = (Graphics2D) g;
                  g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
               }

               MosaicViewSwing.this.setPaintable(g);
               MosaicViewSwing.this.repaint(null);
            }

             @Override
             public Dimension getPreferredSize() {
                if (_controller == null)
                   return super.getPreferredSize();

                SizeDouble size = _controller.getWindowSize();
                size.height++;
                size.width++;
//                System.out.println("Mosaic::getPreferredSize: size="+size);
                return Cast.toSize(size);
             }

             @Override
             public Dimension getMinimumSize() {
                return getPreferredSize();
             }

         };
      }
      return _control;
   }

   @Override
   public void invalidate(Collection<BaseCell> modifiedCells) {
      JPanel control = getControl();
      if (control == null)
         return;

      assert !_alreadyPainted;

      if (modifiedCells == null)
         control.repaint(); // redraw all of mosaic
      else
         modifiedCells.forEach(cell -> control.repaint(Cast.toRect(cell.getRcOuter())) );
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

   @Override
   public void close() {
      super.close();
      _controller = null;
      _control    = null;
      _cellPaint  = null;
   }

}
