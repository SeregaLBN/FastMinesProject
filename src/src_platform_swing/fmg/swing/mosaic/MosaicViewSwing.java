package fmg.swing.mosaic;

import java.awt.*;
import java.util.Collection;

import javax.swing.Icon;
import javax.swing.JPanel;

import fmg.common.geom.SizeDouble;
import fmg.core.mosaic.MosaicGameModel;
import fmg.core.mosaic.cells.BaseCell;
import fmg.core.mosaic.draw.MosaicDrawModel;
import fmg.swing.Cast;
import fmg.swing.draw.img.Flag;
import fmg.swing.draw.img.Mine;
import fmg.swing.utils.ImgUtils;

/** MVC: view. SWING implementation over control {@link JPanel} */ // TODO rename to MosaicViewSwingJPanel
public class MosaicViewSwing extends AMosaicViewSwing<JPanel, Icon, MosaicDrawModel<Icon>> {

   private JPanel _control;
   private Flag.ControllerIcon _flagImage = new Flag.ControllerIcon();
   private Mine.ControllerIcon _mineImage = new Mine.ControllerIcon();
   private Collection<BaseCell> _modifiedCells;

   public MosaicViewSwing() {
      super(new MosaicDrawModel<Icon>());
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
               MosaicViewSwing.this.draw(g2d, _modifiedCells, (clipBounds==null) ? null : Cast.toRectDouble(g.getClipBounds()), true);
               _modifiedCells = null;
            }

             @Override
             public Dimension getPreferredSize() {
                SizeDouble size = getModel().getSizeDouble();
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
   public void draw(Collection<BaseCell> modifiedCells) {
      JPanel control = getControl();

      assert !_alreadyPainted;

      _modifiedCells = modifiedCells;
      control.repaint();
    //control.invalidate();
   }

//   @Override
//   public void invalidate(Collection<BaseCell> modifiedCells) {
//      super.invalidate(modifiedCells);
//   }

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
      MosaicDrawModel<Icon> model = getModel();
      int sq = (int)model.getCellAttr().getSq(model.getPenBorder().getWidth());
      if (sq <= 0) {
         System.err.println("Error: too thick pen! There is no area for displaying the flag/mine image...");
         sq = 3; // ат балды...
      }

      final int max = 30;
      if (sq > max) {
         _flagImage.getModel().setSize(sq);
         _mineImage.getModel().setSize(sq);
         model.setImgFlag(_flagImage.getImage());
         model.setImgMine(_mineImage.getImage());
      } else {
         _flagImage.getModel().setSize(max);
         _mineImage.getModel().setSize(max);
         model.setImgFlag(ImgUtils.zoom(_flagImage.getImage(), sq, sq));
         model.setImgMine(ImgUtils.zoom(_mineImage.getImage(), sq, sq));
      }
   }

   @Override
   public void close() {
      super.close();
      _control = null;
      _flagImage.close();
      _mineImage.close();
   }

}
