package fmg.swing.mosaic;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputListener;

import fmg.common.Logger;
import fmg.common.geom.SizeDouble;
import fmg.core.mosaic.MosaicController2;
import fmg.core.mosaic.MosaicModel2;
import fmg.swing.img.Flag2;
import fmg.swing.img.Logo2;
import fmg.swing.utils.Cast;

/** MVC: controller. SWING implementation */
public class MosaicJPanelController2 extends MosaicController2<JPanel, MosaicJPanelView2> {

    private final Flag2.FlagSwingIconController imgFlag;
    private final Logo2.LogoSwingIconController imgMine;
    private MosaicMouseListener mosaicMouseListener;

    public MosaicJPanelController2() {
        imgFlag = new Flag2.FlagSwingIconController();
        imgMine = new Logo2.LogoSwingIconController();
        imgMine.asMine();

        var m = new MosaicModel2(true);
        var v = new MosaicJPanelView2(m, imgFlag, imgMine);
        init(m, v);
    }

    public JPanel getViewPanel() {
        return view.getControl();
    }

    private class MosaicMouseListener implements MouseInputListener, FocusListener {

        @Override
        public void mouseClicked(MouseEvent e) {}

        @Override
        public void mousePressed(MouseEvent e) {
            if (SwingUtilities.isLeftMouseButton(e)) {
                MosaicJPanelController2.this.mousePressed(Cast.toPointDouble(e.getPoint()), true);
            } else
            if (SwingUtilities.isRightMouseButton(e)) {
                MosaicJPanelController2.this.mousePressed(Cast.toPointDouble(e.getPoint()), false);
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
                MosaicJPanelController2.this.mouseReleased(Cast.toPointDouble(e.getPoint()), true);
            } else
            if (SwingUtilities.isRightMouseButton(e)) {
                MosaicJPanelController2.this.mouseReleased(Cast.toPointDouble(e.getPoint()), false);
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
            MosaicJPanelController2.this.mouseFocusLost();
        }
        @Override
        public void focusGained(FocusEvent e) {}
    }

    public MosaicMouseListener getMosaicMouseListener() {
        if (mosaicMouseListener == null)
            mosaicMouseListener = new MosaicMouseListener();
        return mosaicMouseListener;
    }

    @Override
    protected boolean checkNeedRestoreLastGame() {
        int iRes = JOptionPane.showOptionDialog(
                 view.getControl(),
                 "Restore last game?", "Question",
                 JOptionPane.DEFAULT_OPTION,
                 JOptionPane.QUESTION_MESSAGE,
                 null, null, null);
        return (iRes == JOptionPane.NO_OPTION);
    }

    @Override
    protected void subscribeToViewControl() {
        JPanel control = view.getControl();
        control.setFocusable(true); // иначе не будет срабатывать FocusListener

        MosaicMouseListener listener = getMosaicMouseListener();
        control.addMouseListener(listener);
        control.addMouseMotionListener(listener);
        control.addFocusListener(listener);

        control.setSize(control.getPreferredSize());
    }

    @Override
    protected void unsubscribeToViewControl() {
        JPanel control = view.getControl();
        MosaicMouseListener listener = getMosaicMouseListener();
        control.removeMouseListener(listener);
        control.removeMouseMotionListener(listener);
        control.removeFocusListener(listener);
    }

    @Override
    protected void onModelChanged(String property) {
        view.onModelChanged(property);
        super.onModelChanged(property);
    }

    /** переустанавливаю заного размер мины/флага для мозаики */
    @Override
    protected void onChangeCellSquareSize() {
        double sq = model.getCellSquareSize();
        if (sq <= 0) {
            Logger.error("Error: too thick pen! There is no area for displaying the flag/mine image...");
            sq = 3; // ат балды...
        }

        model.getFontInfo().setSize(sq);

        final int max = 30;
        if (sq < max)
            sq = max;
        imgFlag.getModel().setSize(new SizeDouble(sq, sq));
        imgMine.getModel().setSize(new SizeDouble(sq, sq));
    }

    @Override
    public void close() {
        super.close();
        view.close();
    }

}
