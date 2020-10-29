package fmg.swing.app;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import javax.swing.*;
import javax.swing.Timer;

import fmg.common.Color;
import fmg.common.Pair;
import fmg.common.geom.BoundDouble;
import fmg.common.geom.Matrisize;
import fmg.common.geom.Rect;
import fmg.common.geom.SizeDouble;
import fmg.core.img.IImageController;
import fmg.core.img.LogoModel;
import fmg.core.mosaic.*;
import fmg.core.types.EGameStatus;
import fmg.core.types.EMosaic;
import fmg.core.types.EMosaicGroup;
import fmg.core.types.ESkillLevel;
import fmg.core.types.draw.EShowElement;
import fmg.core.types.draw.EZoomInterface;
import fmg.core.types.viewmodel.User;
import fmg.core.types.viewmodel.event.ActionToUser;
import fmg.core.types.viewmodel.serializable.ChampionsModel;
import fmg.core.types.viewmodel.serializable.PlayersModel;
import fmg.swing.app.dialog.*;
import fmg.swing.app.menu.MainMenu;
import fmg.swing.app.serializable.SerializeProjData;
import fmg.swing.app.toolbar.EBtnNewGameState;
import fmg.swing.app.toolbar.ToolBar;
import fmg.swing.img.Animator;
import fmg.swing.img.Logo;
import fmg.swing.mosaic.MosaicJPanelController;
import fmg.swing.utils.Cast;
import fmg.swing.utils.ProjSettings;
import fmg.swing.utils.ScreenResolutionHelper;

/** Main window (Главное окно программы)
 * <p>run from command line
 * <br> <code>

  gradle :FastMines_swing:run

 */
public class FastMinesSwing {

    private final JFrame frame = new JFrame();
    private int windowState;

    private JPanel     contentPane;
    private MainMenu   menu;
    private ToolBar    toolbar;
    private MosaicJPanelController mosaicController;
    private PausePanel pausePanel;
    private StatusBar  statusBar;

    private Logo.ImageAwtController logo;
    private PlayersModel players;
    private UUID activeUserId; // current user
    private ChampionsModel champions;

    private ManageDlg       playerManageDialog;
    private StatisticDlg    statisticDialog;
    private ChampionDlg     championDialog;
    private AboutDlg        aboutDialog;
    private SelectMosaicDlg selectMosaicDialog;
    private CustomSkillDlg  customSkillDialog;

    private Timer timerGame;
    private Handlers handlers;
    private Pair<InputMap, ActionMap> keyPairBindAsMenuAccelerator;
    private boolean shedulePack;

    private static final boolean IS_WIN_10 = System.getProperty("os.name").equalsIgnoreCase("Windows 10");

    private final PropertyChangeListener      onMosaicModelPropertyChangedListener = this::onMosaicModelPropertyChanged;
    private final PropertyChangeListener onMosaicControllerPropertyChangedListener = this::onMosaicControllerPropertyChanged;
    private final PropertyChangeListener     onLogoMainIconPropertyChangedListener = this::onLogoMainIconPropertyChanged;

    public FastMinesSwing() {
        super();
        initialize();
    }

    public JFrame getFrame() {
        return frame;
    }

    public ManageDlg getPlayerManageDlg() {
        if (playerManageDialog == null)
            playerManageDialog = new ManageDlg(this, false, getPlayers());
        return playerManageDialog;
    }

    private boolean isCustomSkillDialogExist() { return customSkillDialog != null; }
    private CustomSkillDlg getCustomSkillDialog() {
        if (customSkillDialog == null)
            customSkillDialog = new CustomSkillDlg(this, false);
        return customSkillDialog;
    }

    private boolean isSelectMosaicDialogExist() { return selectMosaicDialog != null; }
    public SelectMosaicDlg getSelectMosaicDialog() {
        if (selectMosaicDialog == null)
            selectMosaicDialog = new SelectMosaicDlg(this, false);
        return selectMosaicDialog;
    }

    private boolean isAboutDialogExist() { return aboutDialog != null; }
    public AboutDlg getAboutDialog() {
        if (aboutDialog == null)
            aboutDialog = new AboutDlg(frame, false);
        return aboutDialog;
    }

    private boolean isChampionDialogExist() { return championDialog != null; }
    public ChampionDlg getChampionDialog() {
        if (championDialog == null)
            championDialog = new ChampionDlg(this, false, getChampions());
        return championDialog;
    }

    private boolean isStatisticDialogExist() { return statisticDialog != null; }
    public StatisticDlg getStatisticDialog() {
        if (statisticDialog == null)
            statisticDialog = new StatisticDlg(this, false, getPlayers());
        return statisticDialog;
    }

/** /
    private Dimension getMinimumSize() {
        Insets in = getMosaicMargin();
        Dimension dim = getMosaicPanel().getMinimumSize();
        dim.width  += in.left + in.right;
        dim.height += in.top  + in.bottom;
        return dim;
    }
/**/

    private JPanel getContentPane() {
        if (contentPane == null) {
            contentPane = new JPanel();
            JPanel centerPanel = getPausePanel().getPanel();
            centerPanel.setLayout(new GridLayout());
            centerPanel.add(getMosaicPanel());

            //contentPane.setBorder(BorderFactory.createEmptyBorder());
            contentPane.setLayout(new BorderLayout());
            contentPane.add(getToolbar().getPanel(), BorderLayout.NORTH);
            contentPane.add(centerPanel, BorderLayout.CENTER);
            contentPane.add(getStatusBar().getLabel(), BorderLayout.SOUTH);
        }
        return contentPane;
    }

    public MainMenu getMenu() {
        if (menu == null)
            menu = new MainMenu(this);
        return menu;
    }

    public ToolBar getToolbar() {
        if (toolbar == null)
            toolbar = new ToolBar(getHandlers());
        return toolbar;
    }

    /** mosaic controller */
    private void setMosaicController(MosaicJPanelController mosaicController) {
        if (this.mosaicController != null) {
            this.mosaicController.getModel().removeListener(onMosaicModelPropertyChangedListener);
            this.mosaicController.removeListener(onMosaicControllerPropertyChangedListener);
            this.mosaicController.close();
        }
        this.mosaicController = mosaicController;
        if (mosaicController != null) {
            MosaicDrawModel<?> model = mosaicController.getModel();
            model.setPadding(new BoundDouble(0));
            model.setBackgroundColor(model.getBackgroundColor().darker(0.2));
            model.addListener(onMosaicModelPropertyChangedListener);
            mosaicController.addListener(onMosaicControllerPropertyChangedListener);
        }
    }
    /** mosaic controller */
    public MosaicJPanelController getMosaicController() {
        if (mosaicController == null)
            setMosaicController(new MosaicJPanelController());
        return mosaicController;
    }
//    /** mosaic data */
//    public Mosaic getMosaic() {
//        return getMosaicController().getMosaic();
//    }
    /** mosaic view panel */
    public JPanel getMosaicPanel() {
        return getMosaicController().getViewPanel();
    }

    private PausePanel getPausePanel() {
        if (pausePanel == null)
            pausePanel = new PausePanel(this);
        return pausePanel;
    }

    private StatusBar getStatusBar() {
        if (statusBar == null)
            statusBar = new StatusBar();
        return statusBar;
    }

    void iconify() {
        if (Toolkit.getDefaultToolkit().isFrameStateSupported(Frame.ICONIFIED)) {
            int state = frame.getExtendedState();

            // Set the iconified bit
            state ^= Frame.ICONIFIED;

            // Iconify the frame
            frame.setExtendedState(state);
        }
    }

    void toCenterScreen() {
//        Dimension desktopSize = getDesktopSize();
//        Dimension sizeWin = getRealSize();
//        setLocation((desktopSize.width - sizeWin.width) / 2, (desktopSize.height - sizeWin.height) / 2);
        frame.setLocationRelativeTo(null);
    }

    private void initialize() {
//        System.out.println(getProperties());

        {
            ToolTipManager ttm = ToolTipManager.sharedInstance();
            ttm.setInitialDelay(ttm.getInitialDelay() + 3000);
        }

//        iconify();
//        frame.setResizable(false);

        boolean isZoomAlwaysMax;
        final Point startLocation = new Point();
        boolean defaultData;
        boolean doNotAskStartup;
        MosaicJPanelController mosaicCtrllr;
        { // aplly data from SerializeProjModel
            final SerializeProjData spm = new SerializeProjData();
            defaultData = !spm.load();

            if (spm.isSystemTheme())
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); // OptionsThemeSystem();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            else
                try {
                    UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); // OptionsThemeDefault();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            frame.setUndecorated(!spm.getShowElement(EShowElement.eCaption));

            mosaicCtrllr = getMosaicController();
            mosaicCtrllr.setSizeField(spm.getSizeField());
            mosaicCtrllr.setMosaicType(spm.getMosaicType());
            mosaicCtrllr.setMinesCount(spm.getMinesCount());
            mosaicCtrllr.getModel().setSize(spm.getSizeMosaic());

            setActiveUserId(spm.getActiveUserId());
            getPlayerManageDlg().setDoNotAskStartupChecked(spm.isDoNotAskStartup());
            doNotAskStartup = spm.isDoNotAskStartup();

            getMenu().getOptions().getZoomItem(EZoomInterface.eAlwaysMax).setSelected(spm.isZoomAlwaysMax());
            isZoomAlwaysMax = spm.isZoomAlwaysMax();

            if (spm.isSystemTheme())
                getMenu().getOptions().getThemeSystem().setSelected(true);
            else
                getMenu().getOptions().getThemeDefault().setSelected(true);
            getMenu().getOptions().getUsePause()  .setSelected(spm.isUsePause());
            getMenu().getOptions().getUseUnknown().setSelected(spm.isUseUnknown());
            getToolbar().getBtnPause().getButton().setVisible( spm.isUsePause());
            mosaicCtrllr.setUseUnknown(                        spm.isUseUnknown());

            for (EShowElement key: EShowElement.values()) {
                getMenu().getOptions().getShowElement(key).setSelected(spm.getShowElement(key));
            }
            getMenu().getMenuBar()   .setVisible(spm.getShowElement(EShowElement.eMenu));
            applyInputActionMenuMap             (spm.getShowElement(EShowElement.eMenu));
            getToolbar().getPanel()  .setVisible(spm.getShowElement(EShowElement.eToolbar));
            getStatusBar().getLabel().setVisible(spm.getShowElement(EShowElement.eStatusbar));

            startLocation.x = spm.getLocation().x;
            startLocation.y = spm.getLocation().y;
        }

        frame.setContentPane(getContentPane());
        frame.setMinimumSize(new Dimension(400, 400));

        frame.setJMenuBar(getMenu().getMenuBar());
        frame.setTitle("FastMines");
        this.logo = new Logo.ImageAwtController();
        LogoModel logoModel = this.logo.getModel();
        logoModel.setUseGradient(true);
        logoModel.setSize(new SizeDouble(128, 128));
        logoModel.setPadding(new BoundDouble(1));
        logoModel.setBackgroundColor(Color.Transparent());//ImageProperties.DefaultBkColor);
        logoModel.setRotateMode(LogoModel.ERotateMode.combi);
        logoModel.setAnimatePeriod(25000);
        logoModel.setTotalFrames(260);
        this.logo.useRotateTransforming(true);
        this.logo.usePolarLightFgTransforming(true);
        /** /
         this.logo.addModelTransformer((currentFrame, totalFrames, model) -> {
             double angle = currentFrame * 180.0 * 2 / totalFrames;
            //System.out.println("sin("+angle+")=" + Math.sin(FigureHelper.toRadian(angle)));
//            logoModel.setPadding(1);
//            System.out.println(model.getSize());
//            System.out.println(logoModel.getPadding());
            double padding = -20 + 5 * Math.sin(FigureHelper.toRadian(angle));
            logoModel.setPadding((int)padding);
        });
        /**/
        logoModel.setAnimated(true);
        frame.setIconImage(logo.getImage());
        this.logo.addListener(onLogoMainIconPropertyChangedListener);

//        this.getHandlers().getMosaicListener().OnChangedArea(new MosaicEvent(getMosaic())); // TODO: это нужно только тогда, когда нет десериализации
        getToolbar().getEdtMinesLeft().setText(Integer.toString(mosaicCtrllr.getCountMinesLeft()));
        getToolbar().getEdtTimePlay().setText("0");

        mosaicCtrllr.setOnClickEvent(this.getHandlers().getMosaicClickHandler());
        //setDefaultCloseOperation(EXIT_ON_CLOSE);
        frame.addWindowListener(     this.getHandlers().getWindowListener());
        frame.addWindowStateListener(this.getHandlers().getWindowStateListener());
//        frame.addKeyListener(new KeyListener() {
//            @Override public void keyTyped   (KeyEvent e) { System.out.println("Main::KeyListener:keyTyped: "    + e); }
//            @Override public void keyReleased(KeyEvent e) { System.out.println("Main::KeyListener:keyReleased: " + e); }
//            @Override public void keyPressed (KeyEvent e) { System.out.println("Main::KeyListener:keyPressed: "  + e); }
//        });
        frame.addWindowFocusListener(this.getHandlers().getWindowFocusListener());
        frame.addMouseWheelListener(this.getHandlers().getMouseWheelListener());
//        frame.addWindowListener(new WindowAdapter() {
//
//           @Override
//           public void windowActivated(WindowEvent e) {
//             if (isAlwaysOnTopSupported())
//                  try {
//                      System.out.println("windowActivated");
//                      setAlwaysOnTop(true);
//                  } catch (Exception ex) {
//                      ex.printStackTrace();
//                  }
//               super.windowActivated(e);
//           }
//           @Override
//           public void windowDeactivated(WindowEvent e) {
////                System.out.println("windowDeactivated: " + e.getSource());
//                if (frame.isAlwaysOnTopSupported())
//                    try {
//                        System.out.println("windowDeactivated");
//                        frame.setAlwaysOnTop(false);
//                    } catch (Exception ex) {
//                        ex.printStackTrace();
//                    }
//                super.windowDeactivated(e);
//            }
//        });

//        this.addComponentListener(new ComponentAdapter() {
//            @Override
//            public void componentShown(ComponentEvent ev) {
//                frame.recheckLocation();
//            }
//            @Override
//            public void componentHidden(ComponentEvent ev) {
//                System.out.println ( "Component hidden" );
//            }
//        });

        customKeyBinding();

        frame.pack();
//        System.out.println("ThreadId=" + Thread.currentThread().getId() + ": Main::initialize: after pack");
        if (defaultData)
            frame.setLocationRelativeTo(null);
        else
            frame.setLocation(startLocation);

//        System.out.println("Main::initialize: after setLocation");

        if (isZoomAlwaysMax)
            SwingUtilities.invokeLater(() -> sizeAlwaysMax(new ActionEvent(FastMinesSwing.this, 0, null)));
        if (!doNotAskStartup)
            SwingUtilities.invokeLater(() ->
                getHandlers().getPlayerManageAction().actionPerformed(new ActionEvent(FastMinesSwing.this, 0, "Main::initialize"))
            );
    }

    public ESkillLevel getSkillLevel() {
        EMosaic eMosaic = getMosaicController().getMosaicType();
        Matrisize sizeFld = getMosaicController().getSizeField();
        int numberMines = getMosaicController().getMinesCount();

        if (sizeFld.equals(ESkillLevel.eBeginner.getDefaultSize()) && (numberMines == ESkillLevel.eBeginner.getNumberMines(eMosaic)))
            return ESkillLevel.eBeginner;
        if (sizeFld.equals(ESkillLevel.eAmateur.getDefaultSize()) && (numberMines == ESkillLevel.eAmateur.getNumberMines(eMosaic)))
            return ESkillLevel.eAmateur;
        if (sizeFld.equals(ESkillLevel.eProfi.getDefaultSize()) && (numberMines == ESkillLevel.eProfi.getNumberMines(eMosaic)))
            return ESkillLevel.eProfi;
        if (sizeFld.equals(ESkillLevel.eCrazy.getDefaultSize()) && (numberMines == ESkillLevel.eCrazy.getNumberMines(eMosaic)))
            return ESkillLevel.eCrazy;
        return ESkillLevel.eCustom;
    }

    /** прочие комбинации клавиш (не из меню) */
    void customKeyBinding() {
        JRootPane rootPane = frame.getRootPane();
        if (rootPane.getInputMap().size() == 0) {
            // on ESC key iconic frame
            Object keyBind = "Minimized";
            rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyCombo.getKeyStroke_Minimized(), keyBind);
            rootPane.getActionMap().put(keyBind, this.getHandlers().getMinimizedAction());

            // Num5 - center screen window
            keyBind = "CenterScreenPos";
            rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyCombo.getKeyStroke_CenterScreenPos(), keyBind);
            rootPane.getActionMap().put(keyBind, this.getHandlers().getCenterScreenAction());
            keyBind = "CenterScreenPos2";
            rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyCombo.getKeyStroke_CenterScreenPos2(), keyBind);
            rootPane.getActionMap().put(keyBind, this.getHandlers().getCenterScreenAction());


            keyBind = "MaxSize";
            rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyCombo.getKeyStroke_ZoomMaxAlternative(), keyBind);
            rootPane.getActionMap().put(keyBind, this.getHandlers().getZoomAction(EZoomInterface.eMax));

            keyBind = "MinSize";
            rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyCombo.getKeyStroke_ZoomMinAlternative(), keyBind);
            rootPane.getActionMap().put(keyBind, this.getHandlers().getZoomAction(EZoomInterface.eMin));


            keyBind = "Pause";
            rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyCombo.getKeyStroke_Pause1(), keyBind);
            rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyCombo.getKeyStroke_Pause2(), keyBind);
            rootPane.getActionMap().put(keyBind, this.getHandlers().getPauseAction());

            // < > ^ V
            keyBind = "Increment by X mosaic field";
            rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyCombo.getKeyStroke_MosaicFieldXInc(), keyBind);
            rootPane.getActionMap().put(keyBind, this.getHandlers().getMosaicSizeIncX());
            keyBind = "Decrement by X mosaic field";
            rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyCombo.getKeyStroke_MosaicFieldXDec(), keyBind);
            rootPane.getActionMap().put(keyBind, this.getHandlers().getMosaicSizeDecX());
            keyBind = "Increment by Y mosaic field";
            rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyCombo.getKeyStroke_MosaicFieldYInc(), keyBind);
            rootPane.getActionMap().put(keyBind, this.getHandlers().getMosaicSizeIncY());
            keyBind = "Decrement by Y mosaic field";
            rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyCombo.getKeyStroke_MosaicFieldYDec(), keyBind);
            rootPane.getActionMap().put(keyBind, this.getHandlers().getMosaicSizeDecY());

            for (EMosaicGroup mosGr: EMosaicGroup.values()) {
                keyBind = "SelectMosaic_"+mosGr;
                rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyCombo.getKeyStroke_SelectMosaic(mosGr), keyBind);
                rootPane.getActionMap().put(keyBind, this.getHandlers().getSelectMosaicAction(mosGr));
            }
        }
    }

    Pair<InputMap, ActionMap> getKeyPairBindAsMenuAccelerator() {
        if (keyPairBindAsMenuAccelerator == null) {
            InputMap inputMap = new ComponentInputMap(frame.getRootPane());
            ActionMap actionMap = new ActionMap();
            keyPairBindAsMenuAccelerator = new Pair<>(inputMap, actionMap);

            BiConsumer<KeyStroke, Action> bind = (key, action) -> {
                String name = UUID.randomUUID().toString();
                inputMap.put(key, name);
                actionMap.put(name, action);
            };

            bind.accept(KeyCombo.getKeyStroke_About()       , getHandlers().getAboutAction());
            bind.accept(KeyCombo.getKeyStroke_Champions()   , getHandlers().getChampionsAction());
            bind.accept(KeyCombo.getKeyStroke_Statistics()  , getHandlers().getStatisticsAction());
            bind.accept(KeyCombo.getKeyStroke_NewGame()     , getHandlers().getGameNewAction());

            for (ESkillLevel key: ESkillLevel.values()) {
                bind.accept(KeyCombo.getKeyStroke_SkillLevel(key), getHandlers().getSkillLevelAction(key));
            }

            bind.accept(KeyCombo.getKeyStroke_PlayerManage(), getHandlers().getPlayerManageAction());
            bind.accept(KeyCombo.getKeyStroke_Exit()        , getHandlers().getGameExitAction());

            for (EZoomInterface key: EZoomInterface.values()) {
                bind.accept(KeyCombo.getKeyStroke_Zoom(key), getHandlers().getZoomAction(key));
            }
            bind.accept(KeyCombo.getKeyStroke_ZoomIncAlternative(), getHandlers().getZoomAction(EZoomInterface.eInc));
            bind.accept(KeyCombo.getKeyStroke_ZoomDecAlternative(), getHandlers().getZoomAction(EZoomInterface.eDec));

            bind.accept(KeyCombo.getKeyStroke_ThemeDefault(), getHandlers().getThemeDefaultAction());
            bind.accept(KeyCombo.getKeyStroke_ThemeSystem() , getHandlers().getThemeSystemAction());

            for (EShowElement key: EShowElement.values()) {
                bind.accept(KeyCombo.getKeyStroke_ShowElements(key), getHandlers().getShowElementAction(key));
            }
        }
        return keyPairBindAsMenuAccelerator;
    }

    /** pause on/off */
    void changePause() {
        if (getMosaicController().getGameStatus() != EGameStatus.eGSPlay)
            return;

//        System.out.println("> FMG::ChangePause: " + KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner() );

        boolean paused = isPaused();
        getToolbar().getBtnPause().getButton().setSelected(!paused);
        getPausePanel().animateLogo(!paused);
        if (paused) {
            getTimerGame().restart();

            getMosaicPanel().setVisible(true);
            getMosaicPanel().requestFocusInWindow();
        } else {
            getTimerGame().stop();

            getMosaicPanel().setVisible(false);
            frame.getRootPane().requestFocusInWindow(); // ! иначе на компонентах нат фокуса, и mouse wheel не пашет...
        }
//        System.out.println("< FMG::ChangePause: " + KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner() );
    }

    void gameNew() {
        if (isPaused())
            changePause();
        else
            getMosaicController().gameNew();
    }

    void onClose() {
        try {
            getPlayers().Save();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            getChampions().Save();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            SerializeProjData spm = new SerializeProjData();

            spm.setSizeField(getMosaicController().getSizeField());
            spm.setMosaicType(getMosaicController().getMosaicType());
            spm.setMinesCount(getMosaicController().getMinesCount());
            spm.setSizeMosaic(getMosaicController().getSize());

            spm.setActiveUserId(getActiveUserId());
            spm.setDoNotAskStartup(getPlayerManageDlg().isDoNotAskStartupChecked());

            spm.setSystemTheme(getMenu().getOptions().getThemeSystem().isSelected());
            spm.setShowElement(EShowElement.eCaption  , getMenu().getOptions().getShowElement(EShowElement.eCaption  ).isSelected());
            spm.setShowElement(EShowElement.eMenu     , getMenu().getOptions().getShowElement(EShowElement.eMenu     ).isSelected());
            spm.setShowElement(EShowElement.eToolbar  , getMenu().getOptions().getShowElement(EShowElement.eToolbar  ).isSelected());
            spm.setShowElement(EShowElement.eStatusbar, getMenu().getOptions().getShowElement(EShowElement.eStatusbar).isSelected());
            spm.setZoomAlwaysMax(getMenu().getOptions().getZoomItem(EZoomInterface.eAlwaysMax).isSelected());
            spm.setUseUnknown(getMenu().getOptions().getUseUnknown().isSelected());
            spm.setUsePause(getMenu().getOptions().getUsePause().isSelected());
            spm.setLocation(Cast.toPoint(frame.getLocation()));

            spm.save();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        getPausePanel().close();
        getMenu().close();
        if (isStatisticDialogExist())
            getStatisticDialog().close();
        if (isChampionDialogExist())
            getChampionDialog().close();
        if (isSelectMosaicDialogExist())
            getSelectMosaicDialog().close();
        if (isAboutDialogExist())
            getAboutDialog().close();
        if (isCustomSkillDialogExist())
            getCustomSkillDialog().close();

//      frame.setVisible(false);
        setMosaicController(null);

        logo.removeListener(onLogoMainIconPropertyChangedListener);
        logo.close();


        frame.removeWindowListener     (this.getHandlers().getWindowListener());
        frame.removeWindowStateListener(this.getHandlers().getWindowStateListener());
        frame.removeWindowFocusListener(this.getHandlers().getWindowFocusListener());
        frame.removeMouseWheelListener (this.getHandlers().getMouseWheelListener());

        frame.dispose();
        Animator.getSingleton().close();
//        System.exit(0);
    }

    void onWindowStateChanged(WindowEvent ev) {
        windowState = ev.getNewState();
    }

    /*
    private boolean isWindowMinimized() {
        return (windowState & Frame.ICONIFIED) == Frame.ICONIFIED;
    }
    */
    private boolean isWindowMaximized() {
        return (windowState & Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH;
    }

    /** Попытаться установить новый размер на мозаику (при возможности, сохраняя ESkillLevel) */
    void changeGame(Matrisize newSize) {
        ESkillLevel skill = getSkillLevel();
        changeGame(newSize,
                    (skill == ESkillLevel.eCustom)
                        ? getMosaicController().getMinesCount()
                        : skill.getNumberMines(getMosaicController().getMosaicType()));
    }

    /** Поменять игру на новую мозаику */
    public void changeGame(EMosaic mosaicType) {
        if (isPaused())
            changePause();

        ESkillLevel skill = getSkillLevel();
        getMosaicController().setMosaicType(mosaicType);
        if (skill != ESkillLevel.eCustom) {
            int numberMines = skill.getNumberMines(mosaicType);
            getMosaicController().setMinesCount(numberMines);
        }
    }

    /** Поменять игру на новый размер & кол-во мин */
    public void changeGame(Matrisize newSize, int numberMines) {
        if ((newSize.m < 1) || (newSize.n < 1)) {
            beep();
            return;
        }
        if (numberMines < 0) {
            beep();
            return;
        }

        if (isPaused())
            changePause();

        getMosaicController().setSizeField(newSize);
        getMosaicController().setMinesCount(numberMines);
    }

    /** Поменять игру на новый уровень сложности */
    void changeGame(ESkillLevel skill) {
        if (skill == ESkillLevel.eCustom) {
            //System.out.println("... dialog box 'Select custom skill level...' ");
            getCustomSkillDialog().setVisible(!getCustomSkillDialog().getDialog().isVisible());
            return;
        }

        if (isPaused())
            changePause();

        MosaicJPanelController ctrlr = getMosaicController();
        int numberMines = skill.getNumberMines(ctrlr.getMosaicType());
        Matrisize sizeFld = skill.getDefaultSize();
        double oldArea = ctrlr.getModel().getArea();

        ctrlr.setSizeField(sizeFld);
        ctrlr.setMinesCount(numberMines);

        SizeDouble newSize = MosaicHelper.getSize(ctrlr.getMosaicType(), oldArea, sizeFld);
        setMosaicSize(newSize);
    }

    /** get margin around mosaic control */
    Insets getMosaicMargin() {
        Supplier<Insets> getInsets = () -> {
            Insets res = frame.getInsets(); // TODO wrong under Windows 10
            if (IS_WIN_10 &&
               (res.left    > 7) &&
               (res.top     > 7) &&
               (res.right   > 7) &&
               (res.bottom  > 7))
            { // 8-\
                res.left   -= 7;
                res.top    -= 7;
                res.right  -= 7;
                res.bottom -= 7;
            }
            return res;
        };
        Insets mainPadding = getMenu().getOptions().getShowElement(EShowElement.eCaption).isSelected()
                ? getInsets.get()
                : new Insets(0,0,0,0);
        Dimension menuSize = getMenu().getOptions().getShowElement(EShowElement.eMenu).isSelected()
                ? getMenu().getMenuBar().getSize()
                : new Dimension();
        Dimension toolbarSize = getMenu().getOptions().getShowElement(EShowElement.eToolbar).isSelected()
                ? getToolbar().getPanel().getSize()
                : new Dimension();
        Dimension statusBarSize = getMenu().getOptions().getShowElement(EShowElement.eStatusbar).isSelected()
                ? getStatusBar().getLabel().getSize()
                : new Dimension();
        return new Insets(
            mainPadding.top + menuSize.height + toolbarSize.height,
            mainPadding.left,
            mainPadding.bottom + statusBarSize.height,
            mainPadding.right);
    }

//     /** узнать размер окна проекта при указанном размере окна мозаики */
//     Dimension calcMainSize(Size sizeMosaicInPixel) {
//         Insets mosaicMargin = getMosaicMargin();
//         return new Dimension(
//               mosaicMargin.left + sizeMosaicInPixel.width  + mosaicMargin.right,
//               mosaicMargin.top  + sizeMosaicInPixel.height + mosaicMargin.bottom);
//     }

    /** узнать размер окна мозаики при указанном размере окна проекта */
    SizeDouble calcMosaicWindowSize(Dimension sizeMainWindow) {
        Insets mosaicMargin = getMosaicMargin();
        SizeDouble res = new SizeDouble(
                sizeMainWindow.width  - (mosaicMargin.left + mosaicMargin.right),
                sizeMainWindow.height - (mosaicMargin.top + mosaicMargin.bottom));
        if (res.height < 0 || res.width < 0)
            throw new RuntimeException("Bad algorithm... :(");
        return res;
    }

    /** узнаю мах размер мозаики в пикселях, при котором окно проекта вмещается в текущее разрешение экрана
     * @param mosaicSizeField - интересуемый размер поля мозаики
     * @return мах размер мозаики в пикселях
     */
    SizeDouble calcMaxMosaicSize(Matrisize mosaicSizeField) {
        SizeDouble sizeMosaicIn = calcMosaicWindowSize(ScreenResolutionHelper.getDesktopSize(frame.getGraphicsConfiguration()));
        SizeDouble sizeMosaicOut = new SizeDouble();
        MosaicHelper.findAreaBySize(getMosaicController().getMosaicType(), mosaicSizeField, sizeMosaicIn, sizeMosaicOut);
        return sizeMosaicOut;
    }

    /**
     * узнаю max размер поля мозаики, при котором окно проекта вмещается в текущее разрешение экрана
     * @param area - интересуемая площадь ячеек мозаики
     * @return max размер поля мозаики
     */
    public Matrisize calcMaxMosaicSize(double area) {
        SizeDouble sizeMosaic = calcMosaicWindowSize(ScreenResolutionHelper.getDesktopSize(frame.getGraphicsConfiguration()));
        return MosaicHelper.findSizeByArea(getMosaicController().getMosaicType(), area, sizeMosaic);
    }

    /** проверить что находится в рамках экрана */
    void recheckLocation() {
        if (shedulePack)
            return;

        shedulePack = true;
        SwingUtilities.invokeLater(() -> {
            shedulePack = false;

            if (isWindowMaximized())
                return;

//            {
//                fmg.common.geom.Point center = Rect.getCenter(Cast.toRect(frame.getBounds()));
////                    Point center = new Rect(frame.getBounds()).center();
//                frame.pack();
////                    Rectangle newBounds = new Rect(frame.getBounds()).center(center);
//                Rectangle newBounds = Cast.toRect(Rect.setCenter(Cast.toRect(frame.getBounds()), center));
//                frame.setBounds(newBounds);
//                frame.revalidate();
//            }

            if (getMenu().getOptions().getZoomItem(EZoomInterface.eAlwaysMax).isSelected()) {
                sizeMax();
            } else {
                MosaicJPanelController mosaicController = getMosaicController();
                SizeDouble maxSize = calcMaxMosaicSize(mosaicController.getSizeField());
                if ((maxSize.height < mosaicController.getSize().height) || (maxSize.width < mosaicController.getSize().width))
                    setMosaicSize(maxSize);
            }

            // check that within the screen
            {
                Dimension screenSize = ScreenResolutionHelper.getScreenSize();
                Insets padding = ScreenResolutionHelper.getScreenPadding(frame.getGraphicsConfiguration());
                Rect rcThis = Cast.toRect(frame.getBounds());

                boolean changed = false;
                { // check that the bottom-right boundary within the screen
                    fmg.common.geom.Point pRB = rcThis.PointRB();
                    int offsetX = 0, offsetY = 0;
                    if (pRB.x > (screenSize.width-padding.right))
                        offsetX = pRB.x - (screenSize.width - padding.right);
                    if (pRB.y > (screenSize.height-padding.bottom))
                        offsetY = pRB.y - (screenSize.height - padding.bottom);
                    if ((offsetX != 0) || (offsetY != 0)) {
                        rcThis.moveXY(-offsetX, -offsetY);
                        changed = true;
                    }
                }
                { // check that the top-left boundary within the screen
                    fmg.common.geom.Point pLT = rcThis.PointLT();
                    int offsetX = 0, offsetY = 0;
                    if (pLT.x < padding.left)
                        offsetX = padding.left - pLT.x;
                    if (pLT.y < padding.top)
                        offsetY = padding.top - pLT.y;
                    if ((offsetX != 0) || (offsetY != 0)) {
                        rcThis.moveXY(offsetX, offsetY);
                        changed = true;
                    }
                }
                if (changed)
                    frame.setLocation(Cast.toPoint(rcThis.PointLT()));
            }

        });
    }

    /** mosaic size in pixels */
    void setMosaicSize(SizeDouble size) {
        SizeDouble maxSize = calcMaxMosaicSize(getMosaicController().getSizeField());
        if ((maxSize.width < size.width) || (maxSize.height < size.height))
            size = maxSize;
        Insets o = getMosaicMargin();
        Dimension dim = new Dimension();
        dim.setSize(size.width + o.left + o.right, size.height + o.top + o.bottom);
        frame.setSize(dim);
    }

    /** Zoom + */
    void sizeInc() {
        MosaicGameModel model = getMosaicController().getModel();
        model.setArea(model.getArea() * 1.05);
        frame.pack();
    }

    /** Zoom - */
    void sizeDec() {
        MosaicGameModel model = getMosaicController().getModel();
        double newArea = Math.max(model.getArea() * 0.95, MosaicInitData.AREA_MINIMUM);
        model.setArea(newArea);
        frame.pack();
    }

    /** Zoom minimum */
    void sizeMin() {
        MosaicGameModel model = getMosaicController().getModel();
        model.setArea(MosaicInitData.AREA_MINIMUM);
        frame.pack();
    }

    /** Zoom maximum */
    void sizeMax() {
        SizeDouble sizeMosaicIn = calcMosaicWindowSize(ScreenResolutionHelper.getDesktopSize(frame.getGraphicsConfiguration()));
        SizeDouble sizeMosaicOut = new SizeDouble();
        MosaicJPanelController ctrllr = getMosaicController();
        MosaicGameModel model = ctrllr.getModel();
        double newArea = MosaicHelper.findAreaBySize(ctrllr.getMosaicType(), ctrllr.getSizeField(), sizeMosaicIn, sizeMosaicOut);
        model.setArea(newArea);
        frame.pack();
    }

    /** Zoom always maximum */
    public void sizeAlwaysMax(ActionEvent ev) {
//        if (!isMenuEvent(ev))
//            getMenu().getOptions().getZoomItem(EZoomInterface.eAlwaysMax).setSelected(!getMenu().getOptions().getZoomItem(EZoomInterface.eAlwaysMax).isSelected());

        boolean checked = getMenu().getOptions().getZoomItem(EZoomInterface.eAlwaysMax).isSelected();

        if (checked)
            sizeMax();

        for (EZoomInterface key: EZoomInterface.values())
            if (key != EZoomInterface.eAlwaysMax)
                getMenu().getOptions().getZoomItem(key).setEnabled(!checked);
    }

    public void optionsThemeDefault() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            SwingUtilities.updateComponentTreeUI(frame);
            frame.pack();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void optionsThemeSystem() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            SwingUtilities.updateComponentTreeUI(frame);
            frame.pack();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    void optionsUseUnknown(ActionEvent e) {
        if (!isMenuEvent(e))
            getMenu().getOptions().getUseUnknown().setSelected(!getMenu().getOptions().getUseUnknown().isSelected());

        getMosaicController().setUseUnknown(getMenu().getOptions().getUseUnknown().isSelected());
    }

    boolean isUsePause() {
        return getMenu().getOptions().getUsePause().isSelected();
    }
    void optionsUsePause(ActionEvent e) {
        if (!isMenuEvent(e))
            getMenu().getOptions().getUsePause().setSelected(!getMenu().getOptions().getUsePause().isSelected());

        boolean usePause = isUsePause();
        this.getToolbar().getBtnPause().getButton().setVisible(usePause);
//        this.getToolbar().revalidate();

        if (!usePause && isPaused())
            changePause();
    }

    void optionsShowElement(EShowElement key, final ActionEvent e) {
        if (!isMenuEvent(e))
            getMenu().getOptions().getShowElement(key).setSelected(!getMenu().getOptions().getShowElement(key).isSelected());

        switch (key) {
        case eCaption:
            {
                final Rectangle rc = frame.getBounds();
                final Map<EShowElement, Boolean> mapShow = new HashMap<>(EShowElement.values().length);
                for (EShowElement val: EShowElement.values())
                    mapShow.put(val, getMenu().getOptions().getShowElement(val).isSelected());

                // вызов frame.dispose(); приводит к потере фокуса, т.е, когда идёт игра, - к срабатыванию паузы
                // т.е. нужно позже снять паузу...
                final boolean isNotPaused = (getMosaicController().getGameStatus() == EGameStatus.eGSPlay) && !isPaused();
                //if (frame.isDisplayable())
                    frame.dispose();
                SwingUtilities.invokeLater(() -> {
                        frame.setUndecorated(               !mapShow.get(EShowElement.eCaption).booleanValue());
                        frame.setBounds(rc);
                        getMenu().getMenuBar()   .setVisible(mapShow.get(EShowElement.eMenu).booleanValue());
                        getToolbar().getPanel()  .setVisible(mapShow.get(EShowElement.eToolbar).booleanValue());
                        getStatusBar().getLabel().setVisible(mapShow.get(EShowElement.eStatusbar).booleanValue());

                        if (isNotPaused && isUsePause())
                            changePause();

                        frame.setVisible(true);
                        frame.pack();
                });
            }
            break;
        case eMenu:
            {
                boolean show = getMenu().getOptions().getShowElement(key).isSelected();
                getMenu().getMenuBar().setVisible(show);
                applyInputActionMenuMap(show);
                frame.pack();
            }
            break;
        case eToolbar:
            {
                getToolbar().getPanel().setVisible(getMenu().getOptions().getShowElement(key).isSelected());
                frame.pack();
            }
            break;
        case eStatusbar:
            {
                boolean sel = getMenu().getOptions().getShowElement(key).isSelected();
                getStatusBar().getLabel().setVisible(sel);
                frame.pack();
            }
            break;
        }
    }
    void applyInputActionMenuMap(boolean visibleMenu) {
//        if (visibleMenu) {
//           getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).setParent(null);
//           getRootPane().getActionMap().setParent(null);
//        } else {
            Pair<InputMap, ActionMap> bind = getKeyPairBindAsMenuAccelerator();
            frame.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).setParent(bind.first);
            frame.getRootPane().getActionMap().setParent(bind.second);
//        }
    }

    boolean isPaused() {
        return !getMosaicPanel().isVisible();
    }
    boolean isMenuEvent(ActionEvent e) {
        Object src = e.getSource();
        return src instanceof JMenuItem;
    }

    Timer getTimerGame() {
        if (timerGame == null)
            timerGame = new Timer(1000, getHandlers().getTimePlayAction());
        return timerGame;
    }

//    private static String getProperties() {
//        StringBuffer sb = new StringBuffer();
//        Enumeration<Object> keys = System.getProperties().keys();
//        while (keys.hasMoreElements()) {
//            Object key = keys.nextElement();
//            sb.append(key).
//               append("=").
//               append(System.getProperties().get(key)).
//               append("\r\n");
//        }
//        return sb.toString();
//    }

    /** all Action handlers */
    public Handlers getHandlers() {
        if (handlers == null)
            handlers = new Handlers(this);
        return handlers;
    }

    public PlayersModel getPlayers() {
        if (players == null) {
            players = new PlayersModel();
            players.Load();
        }
        return players;
    }
    public ChampionsModel getChampions() {
        if (champions == null) {
            champions = new ChampionsModel(getPlayers());
            champions.Load();
        }
        return champions;
    }

    public UUID getActiveUserId() {
        if (getPlayers().isExist(activeUserId))
            return activeUserId;
        return null;
    }
    /** Единоразовые callback методы, вызывамые после выбора и установки текущего юзера */
    private List<ActionToUser> oneTimeSelectActiveUserActions = new ArrayList<>();
    public void setActiveUserId(UUID userId) {
        if (getPlayers().isExist(userId)) {
            this.activeUserId = userId;
            for (ActionToUser action: oneTimeSelectActiveUserActions)
                action.applyToUser(userId);
        }
        oneTimeSelectActiveUserActions.clear();
    }

    /** Сохранить чемпиона && Установить статистику */
    public void setStatisticAndChampion(PropertyChangeEvent ev) {
        MosaicJPanelController mosaicCtrllr = (MosaicJPanelController)ev.getSource();
        if (mosaicCtrllr.getGameStatus() != EGameStatus.eGSEnd)
            throw new IllegalArgumentException("Invalid method state call");

        // сохраняю все нужные данные
        final boolean victory = mosaicCtrllr.isVictory();
        if (!victory && (getActiveUserId() == null))
            return; // не напрягаю игрока окном выбора пользователя, пока он не выиграет разок...

        final ESkillLevel eSkill = getSkillLevel();
        if (eSkill == ESkillLevel.eCustom)
            return;

        final EMosaic eMosaic = mosaicCtrllr.getMosaicType();
        final long realCountOpen = mosaicCtrllr.isVictory() ? mosaicCtrllr.getMinesCount() : mosaicCtrllr.getCountOpen();
        final long playTime = Long.parseLong(getToolbar().getEdtTimePlay().getText());
        final long clickCount = mosaicCtrllr.getCountClick();

        // логика сохранения...
        ActionToUser onActionToUser = userId -> {
            if (userId != null) {
                // ...статистики
                getPlayers().setStatistic(userId, eMosaic, eSkill, victory, realCountOpen, playTime, clickCount);
                if (getStatisticDialog().getDialog().isVisible())
                    // если окно открыто - сфокусируюсь на нужной закладке/скилле и пользователе
                    getStatisticDialog().showData(eSkill, eMosaic);

                // ...чемпиона
                if (victory) {
                    User user = getPlayers().getUser(userId);
                    int pos = getChampions().add(user, playTime, eMosaic, eSkill);
                    if (pos != -1)
                        getChampionDialog().showData(eSkill, eMosaic, pos);
                }
            }
        };

        // вызываю логику:
        if (getActiveUserId() != null) {
            // 1. явно
            onActionToUser.applyToUser(getActiveUserId());
        } else {
            // 2. или неявно, после дожидания выбора текущего пользователя
            oneTimeSelectActiveUserActions.add(onActionToUser);
            getPlayerManageDlg().setVisible(true);
        }
    }

    /** Aктивный (текущий) пользователь. Может быть null, если ещё не выбран. */
    public User getActiveUser() {
        UUID userId = getActiveUserId();
        if (userId == null)
            return null;
        return getPlayers().getUser(userId);
    }

    private void onLogoMainIconPropertyChanged(PropertyChangeEvent ev) {
        if (IImageController.PROPERTY_IMAGE.equals(ev.getPropertyName()))
            frame.setIconImage(logo.getImage());
    }

    private void onMosaicModelPropertyChanged(PropertyChangeEvent ev) {
        switch (ev.getPropertyName()) {
        case MosaicDrawModel.PROPERTY_SIZE:
            System.out.println(">>>>>>>>>>>>>>>>>>>>>");
            recheckLocation();
            break;
        case MosaicGameModel.PROPERTY_SIZE_FIELD:
            getMenu().getGame().recheckSelectedSkillLevel();
            break;
        case MosaicGameModel.PROPERTY_MOSAIC_TYPE:
            getMenu().getMosaics().recheckSelectedMosaicType();
            break;
        default:
            // none
        }
    }

    private void onMosaicControllerPropertyChanged(PropertyChangeEvent ev) {
//        System.out.println("Main::propertyChange: eventName=" + ev.getSource().getClass().getSimpleName() + "." + ev.getPropertyName());
//        MosaicControllerSwing source = (MosaicControllerSwing)ev.getSource();
        switch (ev.getPropertyName()) {
        case MosaicController.PROPERTY_MINES_COUNT:
            getMenu().getMosaics().recheckSelectedMosaicType();
            getMenu().getGame().recheckSelectedSkillLevel();
            break;
        case MosaicController.PROPERTY_GAME_STATUS:
            {
                getToolbar().getBtnPause().getButton().setEnabled(getMosaicController().getGameStatus() == EGameStatus.eGSPlay);
                //System.out.println("OnChangeGameStatus: " + e.getSource().getGameStatus());
                switch ((EGameStatus)ev.getNewValue()) {
                case eGSCreateGame:
                case eGSReady:
                    {
                        getTimerGame().stop();
                        getToolbar().getEdtTimePlay().setText("0");
                        Icon img = getToolbar().getSmileIco(EBtnNewGameState.eNormal);
                        if (img != null)
                            getToolbar().getBtnNew().getButton().setIcon(img);
                    }
                    break;
                case eGSPlay:
                    {
                        getTimerGame().restart();
                    }
                    break;
                case eGSEnd:
                    {
                        getTimerGame().stop();
                        Icon img = getToolbar().getSmileIco(
                            getMosaicController().isVictory() ?
                                EBtnNewGameState.eNormalWin :
                                EBtnNewGameState.eNormalLoss);
                        if (img != null)
                            getToolbar().getBtnNew().getButton().setIcon(img);

                        if (getSkillLevel() != ESkillLevel.eCustom)
                            // сохраняю статистику и чемпиона
                            setStatisticAndChampion(ev);
                    }
                    break;
                }
            }
            break;
        //case MosaicController.PROPERTY_COUNT_FLAG:
        //    break;
        //case MosaicController.PROPERTY_COUNT_OPEN:
        //    break;
        case MosaicController.PROPERTY_COUNT_MINES_LEFT:
            getToolbar().getEdtMinesLeft().setText(Integer.toString(getMosaicController().getCountMinesLeft()));
            break;
        case MosaicController.PROPERTY_COUNT_CLICK:
            getStatusBar().setClickCount(getMosaicController().getCountClick());
            break;
        default:
            // none
        }
    }


    /** /
    static void printSystemProperties() {
        System.getProperties().entrySet().forEach(kv -> System.out.println(kv.getKey() + "=" + kv.getValue()));
    }

    /** /
    static void setSysOut() {
        try {
            Properties props = System.getProperties();
            Object val = props.get("user.dir");
            if (val != null) {
                String file = val.toString();

                val = props.get("file.separator");
                if (val == null)
                    val = '/';

                file += val + "FastMines.log";
                GuiTools.alert(file);

                //new FileOutputStream(file);
                PrintStream ps = new PrintStream(file);
                System.setOut(ps);
                System.setErr(ps);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            GuiTools.alert(ex.toString());
        }
    }

    /** /
    static void ViewAllEvents() {
        EventQueue eq = Toolkit.getDefaultToolkit().getSystemEventQueue();
        EventQueue eq2 = new EventQueue() {
            @Override
            protected void dispatchEvent(AWTEvent event) {
                System.out.println(event);
                super.dispatchEvent(event);
            }
        };
        eq.push(eq2);
    }
    /**/

    public static void beep() {
        java.awt.Toolkit.getDefaultToolkit().beep();
        //ASCII value 7 is a beep. So just print that character
    }

    public static void main(String[] args) {
        ProjSettings.init();
        //ViewAllEvents();
        //setSysOut();
        //printSystemProperties();
        SwingUtilities.invokeLater(() ->
            new FastMinesSwing().getFrame().setVisible(true)
        );
    }

}
