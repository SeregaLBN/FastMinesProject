package fmg.swing.mosaic;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.MouseInputListener;

import fmg.core.mosaic.MosaicController;
import fmg.core.types.EMosaic;
import fmg.core.types.ESkillLevel;
import fmg.swing.Cast;
import fmg.swing.draw.mosaic.PaintSwingContext;
import fmg.swing.draw.mosaic.graphics.PaintableGraphics;

/** MVC: controller. SWING implementation */
public class MosaicControllerSwing extends MosaicController<MosaicViewSwing, PaintableGraphics, Icon, PaintSwingContext<Icon>> {

   private MosaicMouseListener _mosaicMouseListener;

   private class MosaicMouseListener implements MouseInputListener, FocusListener {

      @Override
      public void mouseClicked(MouseEvent e) {}

      @Override
      public void mousePressed(MouseEvent e) {
         if (SwingUtilities.isLeftMouseButton(e)) {
            MosaicControllerSwing.this.mousePressed(Cast.toPointDouble(e.getPoint()), true);
         } else
         if (SwingUtilities.isRightMouseButton(e)) {
            MosaicControllerSwing.this.mousePressed(Cast.toPointDouble(e.getPoint()), false);
         }
      }

      @Override
      public void mouseReleased(MouseEvent e) {
         // Получаю этот эвент на отпускание клавиши даже тогда, когда окно проги неактивно..
         // Избегаю срабатывания onClick'a
         Component rootFrame = SwingUtilities.getRoot((Component) e.getSource());
         if (rootFrame instanceof Window) {
            boolean rootFrameActive = ((Window)rootFrame).isActive();
            if (!rootFrameActive)
               return;
         }

         if (SwingUtilities.isLeftMouseButton(e)) {
            MosaicControllerSwing.this.mouseReleased(Cast.toPointDouble(e.getPoint()), true);
         } else
         if (SwingUtilities.isRightMouseButton(e)) {
            MosaicControllerSwing.this.mouseReleased(Cast.toPointDouble(e.getPoint()), false);
         }
       }

      @Override
      public void mouseEntered(MouseEvent e) {}
      @Override
      public void mouseExited(MouseEvent e) {}
      @Override
      public void mouseDragged(MouseEvent e) {}
      @Override
      public void mouseMoved(MouseEvent e) {}
      @Override
      public void focusLost(FocusEvent e) {
         //System.out.println("Mosaic::MosaicMouseListeners::focusLost: " + e);
         MosaicControllerSwing.this.mouseFocusLost();
      }
      @Override
      public void focusGained(FocusEvent e) {}
   }

   public MosaicMouseListener getMosaicMouseListener() {
      if (_mosaicMouseListener == null)
         _mosaicMouseListener = new MosaicMouseListener();
      return _mosaicMouseListener;
   }

   @Override
   protected boolean checkNeedRestoreLastGame() {
      int iRes = JOptionPane.showOptionDialog(getView().getControl(), "Restore last game?", "Question", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
      return (iRes == JOptionPane.NO_OPTION);
   }

   /** set view */
   @Override
   public void setView(MosaicViewSwing view) {
      if (_view != null) {
         _view.setMosaicController(null);
         unsubscribe();
      }
      super.setView(view);
      if (_view != null) {
         _view.setMosaicController(this);
         subscribe();
      }
   }

   private void subscribe() {
      JPanel control = this.getView().getControl();
      control.setFocusable(true); // иначе не будет срабатывать FocusListener

      MosaicMouseListener listener = getMosaicMouseListener();
      control.addMouseListener(listener);
      control.addMouseMotionListener(listener);
      control.addFocusListener(listener);

      control.setSize(control.getPreferredSize());
   }

   private void unsubscribe() {
      JPanel control = this.getView().getControl();
      MosaicMouseListener listener = getMosaicMouseListener();
      control.removeMouseListener(listener);
      control.removeMouseMotionListener(listener);
      control.removeFocusListener(listener);
   }

   /// TEST
   public static void main(String[] args) {
      JFrame frame = new JFrame();

      MosaicControllerSwing ctrllr = new MosaicControllerSwing();

      EMosaic mosaicType = EMosaic.eMosaicSquare1;
      ESkillLevel skill  = ESkillLevel.eBeginner;

      ctrllr.setArea(500);
      ctrllr.setMosaicType(mosaicType);
      ctrllr.setSizeField(skill.getDefaultSize());
      ctrllr.setMinesCount(skill.getNumberMines(mosaicType));
      ctrllr.GameNew();

      frame.add(ctrllr.getView().getControl());
      //frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.addWindowListener(new WindowAdapter() {
         @Override
         public void windowClosing(WindowEvent we) {
            ctrllr.close();
            frame.dispose();
         }
      });

      frame.pack();
      frame.setVisible(true);
   }

}
