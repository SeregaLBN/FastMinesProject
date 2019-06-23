package fmg.swing.app.menu;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;

import javax.swing.*;

import fmg.swing.app.MainApp;
import fmg.swing.utils.ImgUtils;

public class MainMenu extends JMenuBar implements AutoCloseable {

    private static final long serialVersionUID = 1L;
    public  static final int MenuHeightWithIcon = 32;
    public  static final int ZoomQualityFactor = 2; // 1 - as is

    private final MainApp app;

    @SuppressWarnings("unused")
    public static void setMenuItemIcon(JMenuItem menuItem, Icon ico) {
        if (ZoomQualityFactor != 1)
            ico = ImgUtils.zoom(ico, MenuHeightWithIcon, MenuHeightWithIcon);
        menuItem.setIcon(ico);
        if (ZoomQualityFactor == 1)
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

    public MainMenu(MainApp app) {
        super();
        this.app = app;
        initialise();
    }
    void initialise() {
        this.setLayout(new FlowLayout(FlowLayout.LEFT, 0,0));

        this.add(getGame());
        this.add(getMosaics());
        this.add(getOptions());
        this.add(getHelp());

//            this.setToolTipText("main menu");

        // меняю вход в меню с F10 на Alt
        // TODO проверить нуна ли это делать не под виндами...
        InputMap menuBarInputMap = this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        Object keyBind = menuBarInputMap.get(KeyStroke.getKeyStroke(KeyEvent.VK_F10, 0));
        menuBarInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F10, 0), "none");
        menuBarInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ALT, 0, !true), keyBind);
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension dim = super.getPreferredSize();
        dim.width = app.getMosaicPanel().getPreferredSize().width;
        return dim;
    }

    @Override
    public void close() {
        this.getGame().close();
        this.getMosaics().close();
    }

}
