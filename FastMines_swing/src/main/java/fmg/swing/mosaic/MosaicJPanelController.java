package fmg.swing.mosaic;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputListener;

import fmg.core.mosaic.MosaicController;
import fmg.core.mosaic.MosaicDrawModel;
import fmg.swing.utils.Cast;

/** MVC: controller. SWING implementation */
public class MosaicJPanelController extends MosaicController<JPanel, Icon, MosaicJPanelView, MosaicDrawModel<Icon>> {

    private MosaicMouseListener _mosaicMouseListener;

    public MosaicJPanelController() {
        super(new MosaicJPanelView());
        subscribeToViewControl();
    }

    public JPanel getViewPanel() {
        return getView().getControl();
    }

    private class MosaicMouseListener implements MouseInputListener, FocusListener {

        @Override
        public void mouseClicked(MouseEvent e) {}

        @Override
        public void mousePressed(MouseEvent e) {
            if (SwingUtilities.isLeftMouseButton(e)) {
                MosaicJPanelController.this.mousePressed(Cast.toPointDouble(e.getPoint()), true);
            } else
            if (SwingUtilities.isRightMouseButton(e)) {
                MosaicJPanelController.this.mousePressed(Cast.toPointDouble(e.getPoint()), false);
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
                MosaicJPanelController.this.mouseReleased(Cast.toPointDouble(e.getPoint()), true);
            } else
            if (SwingUtilities.isRightMouseButton(e)) {
                MosaicJPanelController.this.mouseReleased(Cast.toPointDouble(e.getPoint()), false);
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
            //Logger.info("Mosaic::MosaicMouseListeners::focusLost: " + e);
            MosaicJPanelController.this.mouseFocusLost();
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

    private void subscribeToViewControl() {
        JPanel control = this.getView().getControl();
        control.setFocusable(true); // иначе не будет срабатывать FocusListener

        MosaicMouseListener listener = getMosaicMouseListener();
        control.addMouseListener(listener);
        control.addMouseMotionListener(listener);
        control.addFocusListener(listener);

        control.setSize(control.getPreferredSize());
    }

    private void unsubscribeToViewControl() {
        JPanel control = this.getView().getControl();
        MosaicMouseListener listener = getMosaicMouseListener();
        control.removeMouseListener(listener);
        control.removeMouseMotionListener(listener);
        control.removeFocusListener(listener);
    }

    @Override
    public void close() {
        unsubscribeToViewControl();
        super.close();
        getView().close();
    }

}
