package fmg.swing.app.menu;

import java.util.HashMap;
import java.util.Map;

import javax.swing.*;

import fmg.core.types.draw.EShowElement;
import fmg.core.types.draw.EZoomInterface;
import fmg.swing.app.KeyCombo;
import fmg.swing.app.MainApp;

public class OptionsMenu extends JMenu {

    private static final long serialVersionUID = 1L;

    private final MainApp app;
    private JMenu zoom;
    private Map<EZoomInterface, JMenuItem> zoomItems;
    private JMenu theme;
    private JRadioButtonMenuItem themeDefault, themeSystem;
    private JCheckBoxMenuItem useUnknown, usePause;
    private Map<EShowElement, JCheckBoxMenuItem> showElements;

    public OptionsMenu(MainApp app) {
        super("Options");
        this.app = app;
        initialize();
    }

    private void initialize() {
        this.setMnemonic(KeyCombo.getMnemonic_MenuOptions());
        this.add(getZoom());
        this.add(getTheme());
        this.add(getUsePause());
        this.add(new JSeparator());
        this.add(getUseUnknown());
        this.add(new JSeparator());
        for (EShowElement key: EShowElement.values())
            this.add(getShowElement(key));
    }

    private JMenu getZoom() {
        if (zoom == null) {
            zoom = new JMenu("Zoom");
            zoom.setMnemonic(KeyCombo.getMnemonic_MenuZoom());
            for (EZoomInterface key: EZoomInterface.values())
                zoom.add(getZoomItem(key));
        }
        return zoom;
    }

    public JMenuItem getZoomItem(EZoomInterface key) {
        if (zoomItems == null) {
            zoomItems = new HashMap<>(EZoomInterface.values().length);

            for (EZoomInterface val: EZoomInterface.values()) {
                JMenuItem menuItem;
                switch (val) {
                case eAlwaysMax: menuItem = new JCheckBoxMenuItem(val.getDescription()); break;
                default        : menuItem = new         JMenuItem(val.getDescription()); break;
                }
                zoomItems.put(val, menuItem);

                menuItem.setMnemonic(KeyCombo.getMnemonic_Zoom(val));
                menuItem.setAccelerator(KeyCombo.getKeyStroke_Zoom(val));
                menuItem.addActionListener(app.getHandlers().getZoomAction(val));
            }
        }
        return zoomItems.get(key);
    }

    private JMenu getTheme() {
        if (theme == null) {
            theme = new JMenu("Theme");
            theme.setMnemonic(KeyCombo.getMnemonic_Theme());
            theme.add(getThemeDefault());
            theme.add(getThemeSystem());
            ButtonGroup bg = new ButtonGroup();
            bg.add(getThemeDefault());
            bg.add(getThemeSystem());
            getThemeSystem().setSelected(true);
        }
        return theme;
    }

    public JRadioButtonMenuItem getThemeDefault() {
        if (themeDefault == null) {
            themeDefault = new JRadioButtonMenuItem("Default");
            themeDefault.setMnemonic(KeyCombo.getMnemonic_ThemeDefault());
            themeDefault.setAccelerator(KeyCombo.getKeyStroke_ThemeDefault());
            themeDefault.addActionListener(app.getHandlers().getThemeDefaultAction());
        }
        return themeDefault;
    }

    public JRadioButtonMenuItem getThemeSystem() {
        if (themeSystem == null) {
            themeSystem = new JRadioButtonMenuItem("System");
            themeSystem.setMnemonic(KeyCombo.getMnemonic_ThemeSystem());
            themeSystem.setAccelerator(KeyCombo.getKeyStroke_ThemeSystem());
            themeSystem.addActionListener(app.getHandlers().getThemeSystemAction());
        }
        return themeSystem;
    }

    public JCheckBoxMenuItem getUseUnknown() {
        if (useUnknown == null) {
            useUnknown = new JCheckBoxMenuItem("Use '?'");
            useUnknown.setMnemonic(KeyCombo.getMnemonic_UseUnknown());
            useUnknown.setAccelerator(KeyCombo.getKeyStroke_UseUnknown());
            useUnknown.addActionListener(app.getHandlers().getUseUnknownAction());
        }
        return useUnknown;
    }

    public JCheckBoxMenuItem getUsePause() {
        if (usePause == null) {
            usePause = new JCheckBoxMenuItem("Pause for a background?");
            usePause.setMnemonic(KeyCombo.getMnemonic_UsePause());
            usePause.setAccelerator(KeyCombo.getKeyStroke_UsePause());
            usePause.addActionListener(app.getHandlers().getUsePauseAction());
        }
        return usePause;
    }

    public JCheckBoxMenuItem getShowElement(EShowElement key) {
        if (showElements == null) {
            showElements = new HashMap<>(EShowElement.values().length);

            for (EShowElement val: EShowElement.values()) {
                JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(val.getDescription());
                menuItem.setMnemonic(KeyCombo.getMnemonic_ShowElements(val));
                menuItem.setAccelerator(KeyCombo.getKeyStroke_ShowElements(val));
                menuItem.addActionListener(app.getHandlers().getShowElementAction(val));

                showElements.put(val, menuItem);
            }
        }
        return showElements.get(key);
    }
}
