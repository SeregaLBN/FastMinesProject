package fmg.swing.app;

import java.awt.Dimension;
import java.awt.Graphics;
import java.beans.PropertyChangeListener;

import javax.swing.JPanel;

import fmg.common.geom.BoundDouble;
import fmg.common.geom.SizeDouble;
import fmg.core.img.IImageController;
import fmg.core.img.LogoModel;
import fmg.swing.img.Logo;

class PausePanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final MainApp app;
    private Logo.IconController logo;

    public PausePanel(MainApp app) {
        this.app = app;
        addMouseListener(app.getHandlers().getPausePanelMouseListener());
    }

    private final PropertyChangeListener onLogoPausePropertyChangedListener = ev -> {
        if (!this.isVisible())
            return;
        if (IImageController.PROPERTY_IMAGE.equals(ev.getPropertyName())) {
            this.repaint();
        }
    };

    @Override
    protected void paintComponent(Graphics g) {
        if (!app.isPaused())
            return;

        super.paintComponent(g);
        Dimension sizeOutward = this.getSize();
        Logo.IconController logo = getLogo();
        double sq = Math.min(sizeOutward.getWidth(), sizeOutward.getHeight());
        logo.getModel().setSize(new SizeDouble(sq, sq));

        logo.getImage().paintIcon(this, g,
                                  (int)((sizeOutward.width -logo.getModel().getSize().width)/2),
                                  (int)((sizeOutward.height-logo.getModel().getSize().height)/2));
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
        this.removeMouseListener(app.getHandlers().getPausePanelMouseListener());
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
