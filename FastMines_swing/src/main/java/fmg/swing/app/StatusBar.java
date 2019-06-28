package fmg.swing.app;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.border.BevelBorder;

class StatusBar {

    private final JLabel label = new JLabel();

    public StatusBar() {
        initialize();
    }

    public JLabel getLabel() {
        return label;
    }

    private void initialize() {
        label.setText("Click count: 0");
        label.setToolTipText("Sensible clicks...");
        label.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
    }

    public void setClickCount(int cnt) {
        label.setText("Click count: " + Integer.toString(cnt));
    }

}
