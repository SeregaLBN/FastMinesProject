package fmg.swing.app.toolbar;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import fmg.common.geom.SizeDouble;
import fmg.core.img.SmileModel;
import fmg.swing.app.Handlers;
import fmg.swing.img.Smile;
import fmg.swing.utils.ImgUtils;

public class ToolBar extends JPanel {
    private static final long serialVersionUID = 1L;

    private final Handlers handlers;
    private JTextField edtMinesLeft, edtTimePlay;
    private BtnNew btnNew;
    private BtnPause btnPause;

    public ToolBar(Handlers handlers) {
        super();
        this.handlers = handlers;
        initialize();
    }

    private void initialize() {
        {
            Dimension dimBtn = new Dimension(31, 31);
            getBtnNew().setPreferredSize(dimBtn);
            getBtnNew().setMinimumSize(dimBtn);
            getBtnNew().setMaximumSize(dimBtn);
            getBtnPause().setPreferredSize(dimBtn);
            getBtnPause().setMinimumSize(dimBtn);
            getBtnPause().setMaximumSize(dimBtn);

            Dimension dimEdt = new Dimension(40, 21);
            getEdtTimePlay().setPreferredSize(dimEdt);
//                getEdtTimePlay().setMinimumSize(dimEdt);
            getEdtTimePlay().setMaximumSize(dimEdt);
            getEdtMinesLeft().setPreferredSize(dimEdt);
//                getEdtMinesLeft().setMinimumSize(dimEdt);
            getEdtMinesLeft().setMaximumSize(dimEdt);
        }
        {
            this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

            this.setBorder(new CompoundBorder(BorderFactory.createRaisedBevelBorder(), new EmptyBorder(2, 2, 2, 2)));
//                this.setBorder(new CompoundBorder(BorderFactory.createEtchedBorder(), new EmptyBorder(2, 2, 2, 2)));
//                this.setBorder(new CompoundBorder(BorderFactory.createMatteBorder(3, 3, 3, 3, new Color(255,0,0, 220)), new EmptyBorder(2, 2, 2, 2)));

            this.add(getEdtMinesLeft());
            getEdtMinesLeft().setAlignmentX(Component.CENTER_ALIGNMENT);

            this.add(Box.createHorizontalGlue());

            this.add(getBtnNew());
            getBtnNew().setAlignmentX(Component.CENTER_ALIGNMENT);

            this.add(Box.createHorizontalStrut(1));

            this.add(getBtnPause());
            getBtnPause().setAlignmentX(Component.CENTER_ALIGNMENT);

            this.add(Box.createHorizontalGlue());

            this.add(getEdtTimePlay());
            getEdtTimePlay().setAlignmentX(Component.CENTER_ALIGNMENT);
        }
    }

    private Icon getSmileIco(SmileModel.EFaceType smileType, int size) {
        try (Smile.IconController img = new Smile.IconController(smileType)) {
            img.getModel().setSize(new SizeDouble(300, 300));//size, size);
//                return smileImages.get(key).getImage();
            return ImgUtils.zoom(img.getImage(), size, size);
        }
    }

    public Icon getSmileIco(EBtnNewGameState btnNewGameState) {
        SmileModel.EFaceType smileType = btnNewGameState.mapToSmileType();
        if (smileType == null)
            return null;
        int size = (btnNewGameState == EBtnNewGameState.ePressed) ||
                   (btnNewGameState == EBtnNewGameState.eRollover)
                ? 25 : 24;
        return getSmileIco(smileType, size);
    }

    public Icon getSmileIco(EBtnPauseState btnPauseState) {
        SmileModel.EFaceType smileType = btnPauseState.mapToSmileType();
        if (smileType == null)
            return null;
        int size = (btnPauseState == EBtnPauseState.ePressed) ||
                   (btnPauseState == EBtnPauseState.eRollover) ||
                   (btnPauseState == EBtnPauseState.eRolloverSelected)
                ? 25 : 24;
        return getSmileIco(smileType, size);
    }

    public JTextField getEdtMinesLeft() {
        if (edtMinesLeft == null) {
            edtMinesLeft = new JTextField("MinesLeft");
//                edtMinesLeft.setBorder(BorderFactory.createLoweredBevelBorder());
//                edtMinesLeft.setBorder(BorderFactory.createEtchedBorder());
            edtMinesLeft.setFocusable(false);
            edtMinesLeft.setEditable(false);
            edtMinesLeft.setToolTipText("Mines left");
        }
        return edtMinesLeft;
    }

    public JTextField getEdtTimePlay() {
        if (edtTimePlay == null) {
            edtTimePlay = new JTextField("TimePlay");
//                edtTimePlay.setBorder(BorderFactory.createLoweredBevelBorder());
//                edtTimePlay.setBorder(BorderFactory.createEtchedBorder());
            edtTimePlay.setFocusable(false);
            edtTimePlay.setEditable(false);
            edtTimePlay.setToolTipText("time...");
        }
        return edtTimePlay;
    }

    public BtnNew getBtnNew() {
        if (btnNew == null)
            btnNew = new BtnNew(this);
        return btnNew;
    }

    public BtnPause getBtnPause() {
        if (btnPause == null)
            btnPause = new BtnPause(this);
        return btnPause;
    }

    Handlers getHandlers() {
        return handlers;
    }

}
