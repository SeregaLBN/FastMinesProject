package fmg.swing.app.toolbar;

import java.awt.Insets;

import javax.swing.JButton;

public class BtnNew {

    private final ToolBar toolbar;
    private final JButton button = new JButton() {

        private static final long serialVersionUID = 1L;

        @Override
        public Insets getInsets() {
            Insets ins = super.getInsets();
            // иначе не виден текст (если нет картинки)
            ins.bottom=ins.left=ins.right=ins.top = 0;
            return ins;
        }

    };

    public BtnNew(ToolBar toolbar) {
        this.toolbar = toolbar;
        initialize();
    }

    public JButton getButton() {
        return button;
    }

    private void initialize() {
        button.setAction(toolbar.getHandlers().getGameNewAction());
        button.setFocusable(false);

        if (toolbar.getSmileIco(EBtnNewGameState.eNormal) == null) {
            button.setText("N");
        } else {
            button.setIcon(                toolbar.getSmileIco(EBtnNewGameState.eNormal));
            button.setPressedIcon(         toolbar.getSmileIco(EBtnNewGameState.ePressed));
            button.setSelectedIcon(        toolbar.getSmileIco(EBtnNewGameState.eSelected));
            button.setRolloverIcon(        toolbar.getSmileIco(EBtnNewGameState.eRollover));
            button.setRolloverSelectedIcon(toolbar.getSmileIco(EBtnNewGameState.eRolloverSelected));
            button.setRolloverEnabled(true);
            button.setDisabledIcon(        toolbar.getSmileIco(EBtnNewGameState.eDisabled));
            button.setDisabledSelectedIcon(toolbar.getSmileIco(EBtnNewGameState.eDisabledSelected));
        }
        button.setToolTipText("new Game");
    }

}
