package fmg.swing.app;

import fmg.swing.mosaic.MosaicJPanelController;
import fmg.swing.utils.ProjSettings;

@SuppressWarnings("deprecation")
public class Applet extends javax.swing.JApplet {

    private static final long serialVersionUID = 1;

    private MosaicJPanelController m;

    @Override
    public void init() {
        ProjSettings.init();

        m = new MosaicJPanelController();
        setContentPane(m.getViewPanel());
    }

    @Override
    public void stop() {
        m.close();
        super.stop();
    }

}
