package fmg.swing.app.menu;

import java.awt.Container;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.*;

import fmg.common.Color;
import fmg.common.geom.SizeDouble;
import fmg.core.img.IImageController;
import fmg.core.img.MosaicSkillModel;
import fmg.core.types.ESkillLevel;
import fmg.swing.app.KeyCombo;
import fmg.swing.app.MainApp;
import fmg.swing.img.MosaicSkillImg;

public class GameMenu extends JMenu implements AutoCloseable {
    private static final long serialVersionUID = 1L;

    private final MainApp app;
    private JMenuItem anew;
    private Map<ESkillLevel, JRadioButtonMenuItem> skillLevel;
    private List<MosaicSkillImg.IconController> skillLevelImages;
    private JMenuItem playerManage;
    private JMenuItem exit;

    private final PropertyChangeListener onMosaicSkillImgPropertyChagedListener = (ev -> {
        MosaicSkillImg.IconController img = (MosaicSkillImg.IconController)ev.getSource();
        JRadioButtonMenuItem menuItem = skillLevel.get(img.getModel().getMosaicSkill());
        Container parent = menuItem.getParent();
        if ((parent == null) || !parent.isVisible())
            return;
        if (ev.getPropertyName().equalsIgnoreCase(IImageController.PROPERTY_IMAGE)) {
            MainMenu.setMenuItemIcon(menuItem, img.getImage());
        }
    });

    public GameMenu(MainApp app) {
        super("Game");
        this.app = app;
        initialize();
    }

    private void initialize() {
        this.setMnemonic(KeyCombo.getMnemonic_MenuGame());

        this.add(getAnew());
        this.add(new JSeparator());
//                this.add(getMosaics());
//                this.add(new JSeparator());

        for (ESkillLevel key: ESkillLevel.values())
            this.add(getMenuItemSkillLevel(key));
        this.add(new JSeparator());
        this.add(getPlayerManage());
        this.add(new JSeparator());
        this.add(getExit());

        ButtonGroup bg = new ButtonGroup();
        for (ESkillLevel key: ESkillLevel.values())
            bg.add(getMenuItemSkillLevel(key));
    }

    private JMenuItem getAnew() {
        if (anew == null) {
            anew = new JMenuItem("New game");
            anew.setMnemonic(KeyCombo.getMnemonic_NewGame());
            anew.setAccelerator(KeyCombo.getKeyStroke_NewGame());
            anew.addActionListener(app.getHandlers().getGameNewAction());
        }
        return anew;
    }
    private JRadioButtonMenuItem getMenuItemSkillLevel(ESkillLevel key) {
        if (skillLevel == null) {
            skillLevel = new HashMap<>(ESkillLevel.values().length);
            skillLevelImages = new ArrayList<>(ESkillLevel.values().length);

            for (ESkillLevel val: ESkillLevel.values()) {
                JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem();

                switch (val) {
                case eCustom: menuItem.setText(val.getDescription() + "..."); break;
                default     : menuItem.setText(val.getDescription()); break;
                }

                menuItem.setMnemonic(KeyCombo.getMnemonic_SkillLevel(val));
                menuItem.setAccelerator(KeyCombo.getKeyStroke_SkillLevel(val));
                menuItem.addActionListener(app.getHandlers().getSkillLevelAction(val));

                MosaicSkillImg.IconController img = new MosaicSkillImg.IconController(val);
                MosaicSkillModel imgModel = img.getModel();
                double sq = MainMenu.MenuHeightWithIcon * MainMenu.ZoomQualityFactor;
                imgModel.setSize(new SizeDouble(sq, sq));
                skillLevelImages.add(img);
                imgModel.setBorderWidth(1); // *ZoomQualityFactor);
                imgModel.setBorderColor(Color.RandomColor().darker(0.4));
                imgModel.setForegroundColor(Color.RandomColor().brighter(0.4));
                imgModel.setBackgroundColor(Color.Transparent());
                imgModel.setAnimated(true);
                imgModel.setAnimatePeriod(6400);
                imgModel.setTotalFrames(130);
                MainMenu.setMenuItemIcon(menuItem, img.getImage());
                img.addListener(onMosaicSkillImgPropertyChagedListener);

                skillLevel.put(val, menuItem);
            }

            recheckSelectedSkillLevel();
        }
        return skillLevel.get(key);
    }

    private JMenuItem getPlayerManage() {
        if (playerManage == null) {
            playerManage = new JMenuItem("Players...");
            playerManage.setMnemonic(KeyCombo.getMnemonic_PlayerManage());
            playerManage.setAccelerator(KeyCombo.getKeyStroke_PlayerManage());
            playerManage.addActionListener(app.getHandlers().getPlayerManageAction());
        }
        return playerManage;
    }

    private JMenuItem getExit() {
        if (exit == null) {
            exit = new JMenuItem("Exit");
            exit.setMnemonic(KeyCombo.getMnemonic_Exit());
            exit.setAccelerator(KeyCombo.getKeyStroke_Exit());
            exit.addActionListener(app.getHandlers().getGameExitAction());
        }
        return exit;
    }

    /** Выставить верный bullet (menu.setSelected) для меню skillLevel'a */
    public void recheckSelectedSkillLevel() {
        ESkillLevel skill = app.getSkillLevel();
        getMenuItemSkillLevel(skill).setSelected(true);
        skillLevelImages.forEach(img -> img.useRotateTransforming(img.getModel().getMosaicSkill() == skill));
    }

    @Override
    public void close() {
        skillLevelImages.forEach(img -> {
            img.removeListener(onMosaicSkillImgPropertyChagedListener);
            img.close();
        });
    }

}
