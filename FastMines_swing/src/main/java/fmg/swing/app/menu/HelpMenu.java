package fmg.swing.app.menu;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;

import fmg.swing.app.KeyCombo;
import fmg.swing.app.FastMinesSwing;

public class HelpMenu {

    private final FastMinesSwing app;
    private final JMenu menu = new JMenu("Help");
    private JMenuItem champions;
    private JMenuItem statistics;
    private JMenuItem about;

    public HelpMenu(FastMinesSwing app) {
        this.app = app;
        initialize();
    }

    public JMenu getMenu() {
        return menu;
    }

    private JMenuItem getChampions() {
        if (champions == null) {
            champions = new JMenuItem("Champions");
            champions.setMnemonic(KeyCombo.getMnemonic_Champions());
            champions.setAccelerator(KeyCombo.getKeyStroke_Champions());
            champions.addActionListener(app.getHandlers().getChampionsAction());
        }
        return champions;
    }

    private JMenuItem getStatistics() {
        if (statistics == null) {
            statistics = new JMenuItem("Statistics");
            statistics.setMnemonic(KeyCombo.getMnemonic_Statistics());
            statistics.setAccelerator(KeyCombo.getKeyStroke_Statistics());
            statistics.addActionListener(app.getHandlers().getStatisticsAction());
        }
        return statistics;
    }
    private JMenuItem getAbout() {
        if (about == null) {
            about = new JMenuItem("About");
            about.setMnemonic(KeyCombo.getMnemonic_About());
            about.setAccelerator(KeyCombo.getKeyStroke_About());
            about.addActionListener(app.getHandlers().getAboutAction());
        }
        return about;
    }

    private void initialize() {
        menu.setMnemonic(KeyCombo.getMnemonic_MenuHelp());
        menu.add(getChampions());
        menu.add(getStatistics());
        menu.add(new JSeparator());
        menu.add(getAbout());
    }

}
