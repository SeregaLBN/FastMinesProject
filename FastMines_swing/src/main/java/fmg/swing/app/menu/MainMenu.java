package fmg.swing.app.menu;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;

import javax.swing.*;

import fmg.swing.app.FastMinesSwing;
import fmg.swing.utils.ImgUtils;

public class MainMenu implements AutoCloseable {

    public static final int MENU_HEIGHT_WITH_ICON = 32;
    public static final int ZOOM_QUALITY_FACTOR = 2; // 1 - as is

    private final FastMinesSwing app;
    private final JMenuBar menuBar = new JMenuBar() {

        private static final long serialVersionUID = 1L;

        @Override
        public Dimension getPreferredSize() {
            Dimension dim = super.getPreferredSize();
            dim.width = app.getMosaicPanel().getPreferredSize().width;
            return dim;
        }

    };

    @SuppressWarnings("unused")
    public static void setMenuItemIcon(JMenuItem menuItem, Icon ico) {
        if (ZOOM_QUALITY_FACTOR != 1)
            ico = ImgUtils.zoom(ico, MENU_HEIGHT_WITH_ICON, MENU_HEIGHT_WITH_ICON);
        menuItem.setIcon(ico);
        if (ZOOM_QUALITY_FACTOR == 1)
            menuItem.repaint();
    }

    private GameMenu game;
    private MosaicsMenu mosaics;
    private OptionsMenu options;
    private HelpMenu help;

    public GameMenu getGame() {
        if (game == null)
            game = new GameMenu(app);
        return game;
    }

    public JMenuBar getMenuBar() {
        return menuBar;
    }

    public MosaicsMenu getMosaics() {
        if (mosaics == null)
            mosaics = new MosaicsMenu(app);
        return mosaics;
    }

    public OptionsMenu getOptions() {
        if (options == null)
            options = new OptionsMenu(app);
        return options;
    }
    private HelpMenu getHelp() {
        if (help == null)
            help = new HelpMenu(app);
        return help;
    }

    public MainMenu(FastMinesSwing app) {
        super();
        this.app = app;
        initialise();
    }
    void initialise() {
        menuBar.setLayout(new FlowLayout(FlowLayout.LEFT, 0,0));

        menuBar.add(getGame   ().getMenu());
        menuBar.add(getMosaics().getMenu());
        menuBar.add(getOptions().getMenu());
        menuBar.add(getHelp   ().getMenu());

//            menuBar.setToolTipText("main menu");

        // меняю вход в меню с F10 на Alt
        // TODO проверить нуна ли это делать не под виндами...
        InputMap menuBarInputMap = menuBar.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        Object keyBind = menuBarInputMap.get(KeyStroke.getKeyStroke(KeyEvent.VK_F10, 0));
        menuBarInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F10, 0), "none");
        menuBarInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ALT, 0, !true), keyBind);
    }

    @Override
    public void close() {
        this.getGame().close();
        this.getMosaics().close();
    }

}
