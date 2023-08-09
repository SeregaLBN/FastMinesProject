package fmg.swing.app;

import static fmg.core.img.PropertyConst.PROPERTY_IMAGE;

import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JPanel;

import fmg.common.geom.BoundDouble;
import fmg.common.geom.SizeDouble;
import fmg.common.ui.UiInvoker;
import fmg.core.img.LogoModel2;
import fmg.swing.img.Logo2;

class PausePanel {

    private final FastMinesApp app;
    private final JPanel panel;
    private Logo2.LogoSwingIconController logo;

    public PausePanel(FastMinesApp app) {
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

    private void onLogoPausePropertyChanged(String propertyName) {
        UiInvoker.Deferred.accept(() -> onLogoPausePropertyChangedAsync(propertyName));
    }

    private void onLogoPausePropertyChangedAsync(String propertyName) {
        if (!panel.isVisible())
            return;
        if (PROPERTY_IMAGE.equals(propertyName))
            panel.repaint();
    }

    private void paintComponent(Graphics g) {
        Dimension sizeOutward = panel.getSize();
        var l = getLogo();
        double sq = Math.min(sizeOutward.getWidth(), sizeOutward.getHeight());
        var lModel = l.getModel();
        lModel.setSize(new SizeDouble(sq, sq));
        l.getImage().paintIcon(panel, g,
                               (int)((sizeOutward.width  - lModel.getSize().width ) / 2),
                               (int)((sizeOutward.height - lModel.getSize().height) / 2));
    }

    private Logo2.LogoSwingIconController getLogo() {
        if (logo == null) {
            logo = new Logo2.LogoSwingIconController();
            LogoModel2 model = logo.getModel();
            model.setUseGradient(true);
            model.setPadding(new BoundDouble(3));
            logo.setPolarLights(true);
            logo.setAnimatePeriod(12500);
            logo.setFps(30);
            logo.setListener(this::onLogoPausePropertyChanged);
        }
        return logo;
    }

    public void animateLogo(boolean start) {
        getLogo().setPolarLights(start);
    }

    void close() {
        panel.removeMouseListener(app.getHandlers().getPausePanelMouseListener());
        getLogo().setListener(null);
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
        return new Dimension(300, 300);
    }
    /**/

}
