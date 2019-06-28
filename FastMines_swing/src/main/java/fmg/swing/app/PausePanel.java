package fmg.swing.app;

import java.awt.Dimension;
import java.awt.Graphics;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JPanel;

import fmg.common.geom.BoundDouble;
import fmg.common.geom.SizeDouble;
import fmg.core.img.IImageController;
import fmg.core.img.LogoModel;
import fmg.swing.img.Logo;

class PausePanel {

    private final MainApp app;
    private final JPanel panel;
    private Logo.IconController logo;
    private final PropertyChangeListener onLogoPausePropertyChangedListener = this::onLogoPausePropertyChanged;

    public PausePanel(MainApp app) {
        this.app = app;
        panel = new JPanel() {

            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(Graphics g) {
                if (!app.isPaused())
                    return;

                super.paintComponent(g);
                PausePanel.this.paintComponent(g);
            }
        };
        panel.addMouseListener(app.getHandlers().getPausePanelMouseListener());
    }

    public JPanel getPanel() {
        return panel;
    }

    private void onLogoPausePropertyChanged(PropertyChangeEvent ev) {
        if (!panel.isVisible())
            return;
        if (IImageController.PROPERTY_IMAGE.equals(ev.getPropertyName()))
            panel.repaint();
    }

    private void paintComponent(Graphics g) {
        Dimension sizeOutward = panel.getSize();
        Logo.IconController l = getLogo();
        double sq = Math.min(sizeOutward.getWidth(), sizeOutward.getHeight());
        l.getModel().setSize(new SizeDouble(sq, sq));
        l.getImage().paintIcon(panel, g,
                               (int)((sizeOutward.width  - l.getModel().getSize().width ) / 2),
                               (int)((sizeOutward.height - l.getModel().getSize().height) / 2));
    }

    private Logo.IconController getLogo() {
        if (logo == null) {
            logo = new Logo.IconController();
            LogoModel model = logo.getModel();
            model.setUseGradient(true);
            model.setPadding(new BoundDouble(3));
            model.setRotateMode(LogoModel.ERotateMode.color);
            model.setAnimatePeriod(12500);
            model.setTotalFrames(250);
            logo.usePolarLightFgTransforming(true);
            logo.addListener(onLogoPausePropertyChangedListener);
        }
        return logo;
    }

    public void animateLogo(boolean start) {
        getLogo().getModel().setAnimated(start);
    }

    void close() {
        panel.removeMouseListener(app.getHandlers().getPausePanelMouseListener());
        getLogo().removeListener(onLogoPausePropertyChangedListener);
        getLogo().close();
    }

    /** /
    @Override
    public Dimension getPreferredSize() {
//        return super.getPreferredSize();
        return app.getMosaicPanel().getPreferredSize();
    }
    @Override
    public Dimension getMinimumSize() {
        return app.getMosaicPanel().getMinimumSize();
    }
    /**/

}
