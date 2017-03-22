package fmg.swing.mosaic;

import java.awt.*;
import java.util.Collection;

import javax.swing.Icon;
import javax.swing.JPanel;

import fmg.common.geom.RectDouble;
import fmg.common.geom.SizeDouble;
import fmg.core.mosaic.AMosaicView;
import fmg.core.mosaic.cells.BaseCell;
import fmg.swing.Cast;
import fmg.swing.draw.img.Flag;
import fmg.swing.draw.img.Mine;
import fmg.swing.draw.mosaic.PaintSwingContext;
import fmg.swing.draw.mosaic.graphics.CellPaintGraphics;
import fmg.swing.draw.mosaic.graphics.PaintableGraphics;
import fmg.swing.utils.ImgUtils;

public final class Mosaic {

/** MVC: view. SWING inplementation */
public static class MosaicView extends AMosaicView<PaintableGraphics, Icon, PaintSwingContext<Icon>> {

   private JPanel _control;
   private CellPaintGraphics<Icon> _cellPaint;

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

               MosaicView.this.repaint(g);
            }

             @Override
             public Dimension getPreferredSize() {
                SizeDouble size = getMosaic().getWindowSize();
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
   public CellPaintGraphics<Icon> getCellPaint() {
      if (_cellPaint == null) {
         _cellPaint = new CellPaintGraphics.Icon();
      }
      return _cellPaint;
   }

   @Override
   public void invalidate() { invalidate(null); }
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

   boolean _alreadyPainted = false;
   private void repaint(Graphics g) {
      _alreadyPainted = true;
      try {
         PaintSwingContext<Icon> pc = getPaintContext();

         // background color
         Rectangle rcFill = g.getClipBounds();
         g.setColor(Cast.toColor(pc.getBackgroundColor().darker(0.2)));
         g.fillRect(rcFill.x, rcFill.y, rcFill.width, rcFill.height);

         // paint cells
         g.setFont(pc.getFont());
         PaintableGraphics p = new PaintableGraphics(getControl(), g);
         RectDouble clipBounds = Cast.toRectDouble(g.getClipBounds());
         CellPaintGraphics<Icon> cellPaint = getCellPaint();
         for (BaseCell cell: getMosaic().getMatrix())
            if (cell.getRcOuter().Intersects(clipBounds)) // redraw only when needed - when the cells and update region intersect
               cellPaint.paint(cell, p, pc);
      } finally {
         _alreadyPainted = false;
      }
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
   }

}

}
