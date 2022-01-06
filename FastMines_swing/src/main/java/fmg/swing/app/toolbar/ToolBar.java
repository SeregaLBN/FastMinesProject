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

public class ToolBar {

    private final Handlers handlers;
    private final JPanel panel;
    private JTextField edtMinesLeft, edtTimePlay;
    private BtnNew btnNew;
    private BtnPause btnPause;

    public ToolBar(Handlers handlers) {
        this.handlers = handlers;
        panel = new JPanel();
        initialize();
    }

    public JPanel getPanel() {
        return panel;
    }

    private void initialize() {
        {
            Dimension dimBtn = new Dimension(31, 31);
            getBtnNew  ().getButton().setPreferredSize(dimBtn);
            getBtnNew  ().getButton().setMinimumSize(dimBtn);
            getBtnNew  ().getButton().setMaximumSize(dimBtn);
            getBtnPause().getButton().setPreferredSize(dimBtn);
            getBtnPause().getButton().setMinimumSize(dimBtn);
            getBtnPause().getButton().setMaximumSize(dimBtn);

            Dimension dimEdt = new Dimension(60, 21);
            getEdtTimePlay().setPreferredSize(dimEdt);
//                getEdtTimePlay().setMinimumSize(dimEdt);
            getEdtTimePlay().setMaximumSize(dimEdt);
            getEdtMinesLeft().setPreferredSize(dimEdt);
//                getEdtMinesLeft().setMinimumSize(dimEdt);
            getEdtMinesLeft().setMaximumSize(dimEdt);
        }
        {
            panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

            panel.setBorder(new CompoundBorder(BorderFactory.createRaisedBevelBorder(), new EmptyBorder(2, 2, 2, 2)));
//            panel.setBorder(new CompoundBorder(BorderFactory.createEtchedBorder(), new EmptyBorder(2, 2, 2, 2)));
//            panel.setBorder(new CompoundBorder(BorderFactory.createMatteBorder(3, 3, 3, 3, new Color(255,0,0, 220)), new EmptyBorder(2, 2, 2, 2)));

            panel.add(getEdtMinesLeft());
            getEdtMinesLeft().setAlignmentX(Component.CENTER_ALIGNMENT);

            panel.add(Box.createHorizontalGlue());

            panel.add(getBtnNew().getButton());
            getBtnNew().getButton().setAlignmentX(Component.CENTER_ALIGNMENT);

            panel.add(Box.createHorizontalStrut(1));

            panel.add(getBtnPause().getButton());
            getBtnPause().getButton().setAlignmentX(Component.CENTER_ALIGNMENT);

            panel.add(Box.createHorizontalGlue());

            panel.add(getEdtTimePlay());
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
            edtTimePlay.setHorizontalAlignment(SwingConstants.RIGHT);
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
