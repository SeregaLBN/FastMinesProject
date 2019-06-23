package fmg.swing.app.toolbar;

import java.awt.Insets;

import javax.swing.JButton;

public class BtnNew extends JButton {

    private static final long serialVersionUID = 1L;
    private final ToolBar toolbar;

    public BtnNew(ToolBar toolbar) {
        super();
        this.toolbar = toolbar;
        initialize();
    }

    private void initialize() {
        this.setAction(toolbar.getHandlers().getGameNewAction());
        this.setFocusable(false);

        if (toolbar.getSmileIco(EBtnNewGameState.eNormal) == null) {
            this.setText("N");
        } else {
            this.setIcon(                toolbar.getSmileIco(EBtnNewGameState.eNormal));
            this.setPressedIcon(         toolbar.getSmileIco(EBtnNewGameState.ePressed));
            this.setSelectedIcon(        toolbar.getSmileIco(EBtnNewGameState.eSelected));
            this.setRolloverIcon(        toolbar.getSmileIco(EBtnNewGameState.eRollover));
            this.setRolloverSelectedIcon(toolbar.getSmileIco(EBtnNewGameState.eRolloverSelected));
            this.setRolloverEnabled(true);
            this.setDisabledIcon(        toolbar.getSmileIco(EBtnNewGameState.eDisabled));
            this.setDisabledSelectedIcon(toolbar.getSmileIco(EBtnNewGameState.eDisabledSelected));
        }
        this.setToolTipText("new Game");
    }

    @Override
    public Insets getInsets() {
        Insets ins = super.getInsets();
        // иначе не виден текст (если нет картинки)
        ins.bottom=ins.left=ins.right=ins.top = 0;
        return ins;
    }

}
