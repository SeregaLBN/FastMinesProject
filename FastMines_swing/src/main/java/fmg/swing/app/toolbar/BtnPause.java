package fmg.swing.app.toolbar;

import java.awt.Insets;

import javax.swing.JToggleButton;

public class BtnPause extends JToggleButton {

    private static final long serialVersionUID = 1L;
    private final ToolBar toolbar;

    public BtnPause(ToolBar toolbar) {
        super(toolbar.getHandlers().getPauseAction());
        this.toolbar = toolbar;
        initialize();
    }

    private void initialize() {
        this.setFocusable(false);
        this.setEnabled(false);

        if (toolbar.getSmileIco(EBtnPauseState.eNormal) == null) {
            this.setText("P");
        } else {
            this.setIcon(                toolbar.getSmileIco(EBtnPauseState.eNormal));
            this.setPressedIcon(         toolbar.getSmileIco(EBtnPauseState.ePressed));
            this.setSelectedIcon(        toolbar.getSmileIco(EBtnPauseState.eSelected));
            this.setRolloverIcon(        toolbar.getSmileIco(EBtnPauseState.eRollover));
            this.setRolloverSelectedIcon(toolbar.getSmileIco(EBtnPauseState.eRolloverSelected));
            this.setRolloverEnabled(true);
            this.setDisabledIcon(        toolbar.getSmileIco(EBtnPauseState.eDisabled));
            this.setDisabledSelectedIcon(toolbar.getSmileIco(EBtnPauseState.eDisabledSelected));
        }
        this.setToolTipText("Pause");
    }

    @Override
    public Insets getInsets() {
        Insets ins = super.getInsets();
        // иначе не виден текст (если нет картинки)
        ins.bottom=ins.left=ins.right=ins.top = 0;
        return ins;
    }

}
