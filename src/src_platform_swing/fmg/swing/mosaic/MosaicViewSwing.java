package fmg.swing.mosaic;

import java.awt.*;
import java.util.Collection;

import javax.swing.Icon;
import javax.swing.JPanel;

import fmg.common.geom.RectDouble;
import fmg.common.geom.SizeDouble;
import fmg.core.mosaic.cells.BaseCell;
import fmg.swing.Cast;
import fmg.swing.draw.mosaic.PaintSwingContext;
import fmg.swing.draw.mosaic.graphics.CellPaintGraphics;
import fmg.swing.draw.mosaic.graphics.PaintableGraphics;

/** MVC: view. SWING implementation */
public class MosaicViewSwing extends AMosaicViewSwing {

   private JPanel _control;
   private MosaicControllerSwing _controller;

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

               MosaicViewSwing.this.repaint(g);
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

}
