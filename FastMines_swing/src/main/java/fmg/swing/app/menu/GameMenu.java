package fmg.swing.app.menu;

import static fmg.core.img.PropertyConst.PROPERTY_IMAGE;

import java.awt.Container;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.*;

import fmg.common.Color;
import fmg.common.geom.SizeDouble;
import fmg.common.ui.UiInvoker;
import fmg.core.img.MosaicSkillModel;
import fmg.core.types.ESkillLevel;
import fmg.swing.app.FastMinesApp;
import fmg.swing.app.KeyCombo;
import fmg.swing.img.MosaicSkillImg;

public class GameMenu implements AutoCloseable {

    private final FastMinesApp app;
    private final JMenu menu = new JMenu("Game");
    private JMenuItem anew;
    private Map<ESkillLevel, JRadioButtonMenuItem> skillLevel;
    private List<MosaicSkillImg.MosaicSkillSwingIconController> skillLevelImages;
    private JMenuItem playerManage;
    private JMenuItem exit;

    public GameMenu(FastMinesApp app) {
        this.app = app;
        initialize();
    }

    public JMenu getMenu() {
        return menu;
    }

    private void onMosaicSkillImgPropertyChaged(int index, String propertyName) {
        UiInvoker.Deferred.accept(() -> onMosaicSkillImgPropertyChagedAsync(index, propertyName));
    }

    private void onMosaicSkillImgPropertyChagedAsync(int index, String propertyName) {
        var img = skillLevelImages.get(index);
        JRadioButtonMenuItem menuItem = skillLevel.get(img.getModel().getMosaicSkill());
        Container parent = menuItem.getParent();
        if ((parent == null) || !parent.isVisible())
            return;
        if (propertyName.equalsIgnoreCase(PROPERTY_IMAGE)) {
            MainMenu.setMenuItemIcon(menuItem, img.getImage());
        }
    }

    private void initialize() {
        menu.setMnemonic(KeyCombo.getMnemonic_MenuGame());

        menu.add(getAnew());
        menu.add(new JSeparator());
//                this.add(getMosaics());
//                this.add(new JSeparator());

        for (ESkillLevel key: ESkillLevel.values())
            menu.add(getMenuItemSkillLevel(key));
        menu.add(new JSeparator());
        menu.add(getPlayerManage());
        menu.add(new JSeparator());
        menu.add(getExit());

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

            int i = 0;
            for (ESkillLevel val: ESkillLevel.values()) {
                JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem();

                switch (val) {
                case eCustom: menuItem.setText(val.getDescription() + "..."); break;
                default     : menuItem.setText(val.getDescription()); break;
                }

                menuItem.setMnemonic(KeyCombo.getMnemonic_SkillLevel(val));
                menuItem.setAccelerator(KeyCombo.getKeyStroke_SkillLevel(val));
                menuItem.addActionListener(app.getHandlers().getSkillLevelAction(val));

                var img = new MosaicSkillImg.MosaicSkillSwingIconController(val);
                MosaicSkillModel imgModel = img.getModel();
                double sq = MainMenu.MENU_HEIGHT_WITH_ICON * MainMenu.ZOOM_QUALITY_FACTOR;
                imgModel.setSize(new SizeDouble(sq, sq));
                skillLevelImages.add(img);
                imgModel.setBorderWidth(1); // *ZoomQualityFactor);
                imgModel.setBorderColor(Color.RandomColor().darker(0.4));
                imgModel.setForegroundColor(Color.RandomColor().brighter(0.4));
                imgModel.setBackgroundColor(Color.Transparent());
                img.setRotateImage(true);
                img.setAnimatePeriod(6400);
                img.setFps(30);
                MainMenu.setMenuItemIcon(menuItem, img.getImage());
                int index = i;
                img.setListener(pn -> onMosaicSkillImgPropertyChaged(index, pn));

                skillLevel.put(val, menuItem);

                ++i;
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
        skillLevelImages.forEach(img -> img.setRotateImage(img.getModel().getMosaicSkill() == skill));
    }

    @Override
    public void close() {
        skillLevelImages.forEach(img -> {
            img.setListener(null);
            img.close();
        });
    }

}
