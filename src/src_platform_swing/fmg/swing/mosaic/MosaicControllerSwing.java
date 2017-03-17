package fmg.swing.mosaic;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputListener;

import fmg.swing.mosaic.Mosaic.MosaicController;

public class MosaicControllerSwing extends MosaicController {

   private MosaicMouseListeners _mosaicMouseListener;

   private class MosaicMouseListeners implements MouseInputListener, FocusListener {

      @Override
      public void mouseClicked(MouseEvent e) {}

      @Override
      public void mousePressed(MouseEvent e) {
         if (SwingUtilities.isLeftMouseButton(e)) {
            MosaicControllerSwing.this.mousePressed(e.getPoint(), true);
         } else
         if (SwingUtilities.isRightMouseButton(e)) {
            MosaicControllerSwing.this.mousePressed(e.getPoint(), false);
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
            MosaicControllerSwing.this.mouseReleased(e.getPoint(), true);
         } else
         if (SwingUtilities.isRightMouseButton(e)) {
            MosaicControllerSwing.this.mouseReleased(e.getPoint(), false);
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

   public MosaicMouseListeners getMosaicMouseListeners() {
      if (_mosaicMouseListener == null)
         _mosaicMouseListener = new MosaicMouseListeners();
      return _mosaicMouseListener;
   }

   protected void initialize() {
      JPanel control = this.getView().getControl();
      control.setFocusable(true); // иначе не будет срабатывать FocusListener

      control.addMouseListener(getMosaicMouseListeners());
      control.addMouseMotionListener(getMosaicMouseListeners());
      control.addFocusListener(getMosaicMouseListeners());

      control.setSize(control.getPreferredSize()); // for run as java been
   }


   @Override
   public void close() {
      JPanel control = this.getView().getControl();
      control.removeMouseListener(getMosaicMouseListeners());
      control.removeMouseMotionListener(getMosaicMouseListeners());
      control.removeFocusListener(getMosaicMouseListeners());

      super.close();
   }

}
