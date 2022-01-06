package fmg.swing.app.toolbar;

import java.awt.Insets;

import javax.swing.JToggleButton;

public class BtnPause {

    private final ToolBar toolbar;
    private final JToggleButton button;

    public BtnPause(ToolBar toolbar) {
        this.toolbar = toolbar;
        button = new JToggleButton(toolbar.getHandlers().getPauseAction()) {

            private static final long serialVersionUID = 1L;

            @Override
            public Insets getInsets() {
                Insets ins = super.getInsets();
                // иначе не виден текст (если нет картинки)
                ins.bottom=ins.left=ins.right=ins.top = 0;
                return ins;
            }

        };
        initialize();
    }

    public JToggleButton getButton() {
        return button;
    }

    private void initialize() {
        button.setFocusable(false);
        button.setEnabled(false);

        if (toolbar.getSmileIco(EBtnPauseState.eNormal) == null) {
            button.setText("P");
        } else {
            button.setIcon(                toolbar.getSmileIco(EBtnPauseState.eNormal));
            button.setPressedIcon(         toolbar.getSmileIco(EBtnPauseState.ePressed));
            button.setSelectedIcon(        toolbar.getSmileIco(EBtnPauseState.eSelected));
            button.setRolloverIcon(        toolbar.getSmileIco(EBtnPauseState.eRollover));
            button.setRolloverSelectedIcon(toolbar.getSmileIco(EBtnPauseState.eRolloverSelected));
            button.setRolloverEnabled(true);
            button.setDisabledIcon(        toolbar.getSmileIco(EBtnPauseState.eDisabled));
            button.setDisabledSelectedIcon(toolbar.getSmileIco(EBtnPauseState.eDisabledSelected));
        }
        button.setToolTipText("Pause");
    }

}
