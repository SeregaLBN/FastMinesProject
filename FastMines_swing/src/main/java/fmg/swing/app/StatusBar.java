package fmg.swing.app;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.border.BevelBorder;

class StatusBar extends JLabel {

    private static final long serialVersionUID = 1L;

    public StatusBar() {
        initialize();
    }

    private void initialize() {
        this.setText("Click count: 0");
        this.setToolTipText("Sensible clicks...");
        this.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
    }

    public void setClickCount(int cnt) {
        this.setText("Click count: " + Integer.toString(cnt));
    }

}
