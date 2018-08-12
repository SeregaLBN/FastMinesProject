package fmg.swing.mosaic;

import java.awt.*;
import java.util.Collection;
import java.util.HashSet;

import javax.swing.Icon;
import javax.swing.JPanel;

import fmg.common.geom.RectDouble;
import fmg.common.geom.SizeDouble;
import fmg.core.mosaic.MosaicDrawModel;
import fmg.core.mosaic.MosaicGameModel;
import fmg.core.mosaic.cells.BaseCell;
import fmg.swing.img.Flag;
import fmg.swing.img.Mine;
import fmg.swing.utils.Cast;
import fmg.swing.utils.ImgUtils;

/** MVC: view. SWING implementation over control {@link JPanel} */
public class MosaicJPanelView extends MosaicSwingView<JPanel, Icon, MosaicDrawModel<Icon>> {

   private JPanel _control;
   private Flag.ControllerIcon _imgFlag = new Flag.ControllerIcon();
   private Mine.ControllerIcon _imgMine = new Mine.ControllerIcon();
   private final Collection<BaseCell> _modifiedCells = new HashSet<>();

   public MosaicJPanelView() {
      super(new MosaicDrawModel<Icon>());
      changeSizeImagesMineFlag();
   }

   @Override
   protected JPanel createImage() {
      // will return once created window
      return getControl();
   }

   public JPanel getControl() {
      if (_control == null) {
         _control = new JPanel() {
            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(Graphics g) {
               //super.paintComponent(g);
               Graphics2D g2d = (Graphics2D) g;
               g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

               Rectangle clipBounds = g.getClipBounds();
               MosaicJPanelView.this.drawSwing(g2d,
                                         _modifiedCells.isEmpty()
                                            ? null
                                            : _modifiedCells,
                                         (clipBounds==null)
                                            ? null
                                            : Cast.toRectDouble(g.getClipBounds()),
                                         true/*_modifiedCells.isEmpty() || (_modifiedCells.size() == getModel().getMatrix().size())*/);
               _modifiedCells.clear();
            }

             @Override
             public Dimension getPreferredSize() {
                SizeDouble size = getModel().getSize();
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
   protected void drawModified(Collection<BaseCell> modifiedCells) {
      JPanel control = getControl();

      assert !_alreadyPainted;

      if (modifiedCells == null) { // mark NULL if all mosaic is changed
         _modifiedCells.clear();
         control.repaint();
      } else {
         _modifiedCells.addAll(modifiedCells);

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
         if (_DEBUG_DRAW_FLOW)
            System.out.println("MosaicViewSwing.draw: repaint={" + (int)minX +","+ (int)minY +","+ (int)(maxX-minX) +","+ (int)(maxY-minY) + "}");
         control.repaint((int)minX, (int)minY, (int)(maxX-minX), (int)(maxY-minY));
      }
    //control.invalidate();
   }

   @Override
   protected void onPropertyChanged(Object oldValue, Object newValue, String propertyName) {
      super.onPropertyChanged(oldValue, newValue, propertyName);
      if (propertyName.equals(PROPERTY_IMAGE))
         getImage(); // implicit call draw() -> drawBegin() -> drawModified() -> control.repaint() -> JPanel.paintComponent -> drawSwing()
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
      MosaicDrawModel<Icon> model = getModel();
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
         model.setImgFlag(ImgUtils.zoom(_imgFlag.getImage(), sq, sq));
         _imgMine.getModel().setSize(max);
         model.setImgMine(ImgUtils.zoom(_imgMine.getImage(), sq, sq));
      }
   }

   @Override
   public void close() {
      getModel().close();
      super.close();
      _control = null;
      _imgFlag.close();
      _imgMine.close();
   }

}
