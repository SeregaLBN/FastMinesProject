package fmg.swing;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.PrintStream;
import java.util.*;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import fmg.common.Color;
import fmg.common.Pair;
import fmg.common.geom.Matrisize;
import fmg.common.geom.Rect;
import fmg.common.geom.SizeDouble;
import fmg.core.img.AMosaicsImg.ERotateMode;
import fmg.core.mosaic.MosaicBase;
import fmg.core.mosaic.MosaicHelper;
import fmg.core.types.EGameStatus;
import fmg.core.types.EMosaic;
import fmg.core.types.EMosaicGroup;
import fmg.core.types.click.ClickResult;
import fmg.data.controller.event.ActionToUser;
import fmg.data.controller.serializable.ChampionsModel;
import fmg.data.controller.serializable.PlayersModel;
import fmg.data.controller.types.ESkillLevel;
import fmg.data.controller.types.User;
import fmg.data.view.draw.EShowElement;
import fmg.data.view.draw.EZoomInterface;
import fmg.swing.dialogs.*;
import fmg.swing.draw.img.*;
import fmg.swing.draw.img.Smile.EType;
import fmg.swing.mosaic.Mosaic;
import fmg.swing.mosaic.Mosaic.MosaicController;
import fmg.swing.mosaic.Mosaic.MosaicView;
import fmg.swing.mosaic.MosaicControllerSwing;
import fmg.swing.serializable.SerializeProjData;
import fmg.swing.utils.GuiTools;
import fmg.swing.utils.ImgUtils;
import fmg.swing.utils.ScreenResolutionHelper;

/** Главное окно программы */
public class Main extends JFrame implements PropertyChangeListener {
   public static final long serialVersionUID = -3441735484862759425L;

   private JPanel     contentPane;
   private MainMenu   menu;
   private Toolbar    toolbar;
   private MosaicController mosaicCtrl;
   private PausePanel pausePanel;
   private StatusBar  statusBar;

   private Logo.Image _logo;
   private PlayersModel players;
   private UUID activeUserId; // current user
   private ChampionsModel champions;

   private ManageDlg       _playerManageDialog;
   private StatisticDlg    _statisticDialog;
   private ChampionDlg     _championDialog;
   private AboutDlg        _aboutDialog;
   private SelectMosaicDlg _selectMosaicDialog;
   private CustomSkillDlg  _customSkillDialog;

   private ManageDlg getPlayerManageDlg() {
      if (_playerManageDialog == null)
         _playerManageDialog = new ManageDlg(this, false, getPlayers());
      return _playerManageDialog;
   }

   private CustomSkillDlg getCustomSkillDialog() {
      if (_customSkillDialog == null)
         _customSkillDialog = new CustomSkillDlg(this, false);
      return _customSkillDialog;
   }

   private boolean isSelectMosaicDialogExist() { return _selectMosaicDialog != null; }
   private SelectMosaicDlg getSelectMosaicDialog() {
      if (_selectMosaicDialog == null)
         _selectMosaicDialog = new SelectMosaicDlg(this, false);
      return _selectMosaicDialog;
   }

   private boolean isAboutDialogExist() { return _aboutDialog != null; }
   private AboutDlg getAboutDialog() {
      if (_aboutDialog == null)
         _aboutDialog = new AboutDlg(this, false);
      return _aboutDialog;
   }

   private boolean isChampionDialogExist() { return _championDialog != null; }
   private ChampionDlg getChampionDialog() {
      if (_championDialog == null)
         _championDialog = new ChampionDlg(this, false, getChampions());
      return _championDialog;
   }

   private boolean isStatisticDialogExist() { return _statisticDialog != null; }
   private StatisticDlg getStatisticDialog() {
      if (_statisticDialog == null)
         _statisticDialog = new StatisticDlg(this, false, getPlayers());
      return _statisticDialog;
   }

   class MainMenu extends JMenuBar implements AutoCloseable {
      private static final long serialVersionUID = 1L;
      private static final int MenuHeightWithIcon = 32;
      private static final int ZoomQualityFactor = 2; // 1 - as is

      class Game extends JMenu implements AutoCloseable {
         private static final long serialVersionUID = 1L;

         private JMenuItem anew;
         private Map<ESkillLevel, JRadioButtonMenuItem> skillLevel;
         private Map<ESkillLevel, MosaicsSkillImg.Icon> skillLevelImages;
         private JMenuItem playerManage;
         private JMenuItem exit;

         public Game() {
            super("Game");
            initialize();
         }
         private void initialize() {
            this.setMnemonic(Main.KeyCombo.getMnemonic_MenuGame());

            this.add(getAnew());
            this.add(new JSeparator());
//            this.add(getMosaics());
//            this.add(new JSeparator());

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
               anew.setMnemonic(Main.KeyCombo.getMnemonic_NewGame());
               anew.setAccelerator(Main.KeyCombo.getKeyStroke_NewGame());
               anew.addActionListener(Main.this.getHandlers().getGameNewAction());
            }
            return anew;
         }
         private JRadioButtonMenuItem getMenuItemSkillLevel(ESkillLevel key) {
            if (skillLevel == null) {
               skillLevel = new HashMap<>(ESkillLevel.values().length);
               skillLevelImages = new HashMap<>(ESkillLevel.values().length);

               Random rnd = new Random(UUID.randomUUID().hashCode());
               for (ESkillLevel val: ESkillLevel.values()) {
                  JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem();

                  switch (val) {
                  case eCustom: menuItem.setText(val.getDescription() + "..."); break;
                  default     : menuItem.setText(val.getDescription()); break;
                  }

                  menuItem.setMnemonic(Main.KeyCombo.getMnemonic_SkillLevel(val));
                  menuItem.setAccelerator(Main.KeyCombo.getKeyStroke_SkillLevel(val));
                  menuItem.addActionListener(Main.this.getHandlers().getSkillLevelAction(val));

                  MosaicsSkillImg.Icon img = new MosaicsSkillImg.Icon(val);
                  img.setSize(MenuHeightWithIcon*ZoomQualityFactor);
                  skillLevelImages.put(val, img);
                  img.setBorderWidth(1*ZoomQualityFactor);
                  img.setBorderColor(Color.RandomColor(rnd).darker(0.4));
                  img.setForegroundColor(Color.RandomColor(rnd).brighter(0.4));
                  img.setBackgroundColor(Color.Transparent);
                  img.setRedrawInterval(50);
                  img.setRotateAngleDelta(2*img.getRotateAngleDelta());
                  setMenuItemIcon(menuItem, img.getImage());
                  img.addListener(ev -> {
                     if (!menuItem.getParent().isVisible())
                        return;
                     if (ev.getPropertyName().equalsIgnoreCase(MosaicsSkillImg.PROPERTY_IMAGE)) {
                        setMenuItemIcon(menuItem, img.getImage());
                     }
                  });

                  skillLevel.put(val, menuItem);
               }

               recheckSelectedSkillLevel();
            }
            return skillLevel.get(key);
         }

         private JMenuItem getPlayerManage() {
            if (playerManage == null) {
               playerManage = new JMenuItem("Players...");
               playerManage.setMnemonic(Main.KeyCombo.getMnemonic_PlayerManage());
               playerManage.setAccelerator(Main.KeyCombo.getKeyStroke_PlayerManage());
               playerManage.addActionListener(Main.this.getHandlers().getPlayerManageAction());
            }
            return playerManage;
         }

         private JMenuItem getExit() {
            if (exit == null) {
               exit = new JMenuItem("Exit");
               exit.setMnemonic(Main.KeyCombo.getMnemonic_Exit());
               exit.setAccelerator(Main.KeyCombo.getKeyStroke_Exit());
               exit.addActionListener(Main.this.getHandlers().getGameExitAction());
            }
            return exit;
         }

         /** Выставить верный bullet (menu.setSelected) для меню skillLevel'a */
         void recheckSelectedSkillLevel() {
            ESkillLevel skill = getSkillLevel();
            getMenuItemSkillLevel(skill).setSelected(true);
            skillLevelImages.forEach((key, img) -> {
               img.setRotate(key == skill);
               img.setPolarLights(key == skill); // не видно особо разницы - маленькая картинка
            });
         }

         @Override
         public void close() {
            skillLevelImages.forEach((key, img) -> img.close());
         }

      }

      class Mosaics extends JMenu implements AutoCloseable {
         private static final long serialVersionUID = 1L;

         private Map<EMosaicGroup, JMenuItem> mosaicsGroup;
         private Map<EMosaicGroup, MosaicsGroupImg.Icon> mosaicsGroupImages;
         private Map<EMosaic, JRadioButtonMenuItem> mosaics;
         private Map<EMosaic, MosaicsImg.Icon> mosaicsImages;

         Mosaics() {
            super("Mosaics");
            initialize();
         }
         private void initialize() {
            this.setMnemonic(Main.KeyCombo.getMnemonic_MenuMosaic());
            for (EMosaicGroup key: EMosaicGroup.values())
               this.add(getMenuItemMosaicGroup(key));

            ButtonGroup bg = new ButtonGroup();
            for (EMosaic key: EMosaic.values())
               bg.add(getMenuItemMosaic(key));
         }

         final static boolean experimentalMenuMnemonic = true;
         private JMenuItem getMenuItemMosaicGroup(EMosaicGroup key) {
            if (mosaicsGroup == null) {
               mosaicsGroup = new HashMap<>(EMosaicGroup.values().length);
               mosaicsGroupImages = new HashMap<>(EMosaicGroup.values().length);

               Random rnd = new Random(UUID.randomUUID().hashCode());
               for (EMosaicGroup val: EMosaicGroup.values()) {
                  JMenu menuItem = new JMenu(val.getDescription());// + (experimentalMenuMnemonic ?  "                      " : ""));
                  for (EMosaic mosaic: val.getBind()) {
                     menuItem.add(getMenuItemMosaic(mosaic));
                     //menuItem.add(Box.createRigidArea(new Dimension(100,25)));
                  }
//                  menuItem.setMnemonic(Main.KeyCombo.getMnemonic_MenuMosaicGroup(val));
                  MosaicsGroupImg.Icon img = new MosaicsGroupImg.Icon(val);
                  img.setSize(MenuHeightWithIcon*ZoomQualityFactor);
                  mosaicsGroupImages.put(val, img);
                  img.setPolarLights(true);
                  img.setBorderWidth(1*ZoomQualityFactor);
                  img.setBorderColor(Color.RandomColor(rnd).darker(0.4));
                  img.setForegroundColor(Color.RandomColor(rnd).brighter(0.7));
                  img.setBackgroundColor(Color.Transparent);
                  img.setRotateAngleDelta(-img.getRotateAngleDelta());
                  img.setRedrawInterval(50);
                  setMenuItemIcon(menuItem,  img.getImage());
                  img.addListener(ev -> {
                     if (!menuItem.getParent().isVisible())
                        return;
                     if (ev.getPropertyName().equalsIgnoreCase(MosaicsGroupImg.PROPERTY_IMAGE)) {
                        setMenuItemIcon(menuItem, img.getImage());
                     }
                  });

//                  if (experimentalMenuMnemonic) {
//                     menuItem.setLayout(new FlowLayout(FlowLayout.RIGHT));
//                     menuItem.add(new JLabel("Num+111111111111"));// + (char)(Main.KeyCombo.getMnemonic_MenuMosaicGroup(val))));
//                  }

                  mosaicsGroup.put(val, menuItem);
               }

               recheckSelectedMosaicType();
            }
            return mosaicsGroup.get(key);
         }

         private JRadioButtonMenuItem getMenuItemMosaic(EMosaic mosaicType) {
            if (mosaics == null) {
               mosaics = new HashMap<>(EMosaic.values().length);
               mosaicsImages = new HashMap<>(EMosaic.values().length);

               Random rnd = new Random(UUID.randomUUID().hashCode());
               for (EMosaic val: EMosaic.values()) {
                  String menuItemTxt = val.getDescription(false);
                  if (experimentalMenuMnemonic)
                     menuItemTxt += "                      ";
                  JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem(menuItemTxt);
                  menuItem.setMnemonic(Main.KeyCombo.getMnemonic_Mosaic(val));
                  menuItem.setAccelerator(Main.KeyCombo.getKeyStroke_Mosaic(val));
                  menuItem.addActionListener(ev -> Main.this.changeGame(val));

                  MosaicsImg.Icon img = new MosaicsImg.Icon(val, val.sizeIcoField(true));
                  img.setSize(MenuHeightWithIcon*ZoomQualityFactor);
                  mosaicsImages.put(val, img);
                  img.setRotateMode(ERotateMode.someCells);
                  img.setBorderWidth(1*ZoomQualityFactor);
                  img.setBorderColor(Color.RandomColor(rnd).darker(0.4));
                  img.setBackgroundColor(Color.Transparent);
                  img.setRotateAngleDelta(3.333);
                  img.setRedrawInterval(50);
                  setMenuItemIcon(menuItem, img.getImage());
                  img.addListener(ev -> {
                     if (!menuItem.getParent().isVisible())
                        return;
                     if (ev.getPropertyName().equalsIgnoreCase(MosaicsImg.PROPERTY_IMAGE)) {
                        setMenuItemIcon(menuItem, img.getImage());
                     }
                  });

                  if (experimentalMenuMnemonic) {
                     menuItem.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, MenuHeightWithIcon/2 - 4));
                     menuItem.add(new JLabel("NumPad " + val.getFastCode()));
                  }

                  mosaics.put(val, menuItem);
               }
            }
            return mosaics.get(mosaicType);
         }

         /** Выставить верный bullet для меню мозаики */
         void recheckSelectedMosaicType() {
            EMosaic currentMosaicType = getMosaic().getMosaicType();
            getMenuItemMosaic(currentMosaicType).setSelected(true);

            mosaicsImages.forEach((eMosaic, img) -> img.setRotate(eMosaic == currentMosaicType));
            mosaicsGroupImages.forEach((mosaicGroup, img) -> {
               boolean isCurrentGroup = mosaicGroup.getBind().contains(currentMosaicType);
               img.setPolarLights(isCurrentGroup);
               img.setRotate(isCurrentGroup);
            });
         }

         @Override
         public void close() {
            mosaicsGroupImages.forEach((key, img) -> img.close() );
            mosaicsImages.forEach((key, img) -> img.close() );
         }

      }

      @SuppressWarnings("unused")
      private void setMenuItemIcon(JMenuItem menuItem, Icon ico) {
         if (ZoomQualityFactor != 1)
            ico = ImgUtils.zoom(ico, MenuHeightWithIcon, MenuHeightWithIcon);
         menuItem.setIcon(ico);
         if (ZoomQualityFactor == 1)
            menuItem.repaint();
      }

      class Options extends JMenu {
         private static final long serialVersionUID = 1L;

         private JMenu zoom;
         private Map<EZoomInterface, JMenuItem> zoomItems;
         private JMenu theme;
         private JRadioButtonMenuItem themeDefault, themeSystem;
         private JCheckBoxMenuItem useUnknown, usePause;
         private Map<EShowElement, JCheckBoxMenuItem> showElements;

         Options() {
            super("Options");
            initialize();
         }
         private void initialize() {
            this.setMnemonic(Main.KeyCombo.getMnemonic_MenuOptions());
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
               zoom.setMnemonic(Main.KeyCombo.getMnemonic_MenuZoom());
               for (EZoomInterface key: EZoomInterface.values())
                  zoom.add(getZoomItem(key));
            }
            return zoom;
         }
         private JMenuItem getZoomItem(EZoomInterface key) {
            if (zoomItems == null) {
               zoomItems = new HashMap<>(EZoomInterface.values().length);

               for (EZoomInterface val: EZoomInterface.values()) {
                  JMenuItem menuItem;
                  switch (val) {
                  case eAlwaysMax: menuItem = new JCheckBoxMenuItem(val.getDescription()); break;
                  default        : menuItem = new         JMenuItem(val.getDescription()); break;
                  }
                  zoomItems.put(val, menuItem);

                  menuItem.setMnemonic(Main.KeyCombo.getMnemonic_Zoom(val));
                  menuItem.setAccelerator(Main.KeyCombo.getKeyStroke_Zoom(val));
                  menuItem.addActionListener(Main.this.getHandlers().getZoomAction(val));
               }
            }
            return zoomItems.get(key);
         }

         private JMenu getTheme() {
            if (theme == null) {
               theme = new JMenu("Theme");
               theme.setMnemonic(Main.KeyCombo.getMnemonic_Theme());
               theme.add(getThemeDefault());
               theme.add(getThemeSystem());
               ButtonGroup bg = new ButtonGroup();
               bg.add(getThemeDefault());
               bg.add(getThemeSystem());
               getThemeSystem().setSelected(true);
            }
            return theme;
         }
         private JRadioButtonMenuItem getThemeDefault() {
            if (themeDefault == null) {
               themeDefault = new JRadioButtonMenuItem("Default");
               themeDefault.setMnemonic(Main.KeyCombo.getMnemonic_ThemeDefault());
               themeDefault.setAccelerator(Main.KeyCombo.getKeyStroke_ThemeDefault());
               themeDefault.addActionListener(Main.this.getHandlers().getThemeDefaultAction());
            }
            return themeDefault;
         }
         private JRadioButtonMenuItem getThemeSystem() {
            if (themeSystem == null) {
               themeSystem = new JRadioButtonMenuItem("System");
               themeSystem.setMnemonic(Main.KeyCombo.getMnemonic_ThemeSystem());
               themeSystem.setAccelerator(Main.KeyCombo.getKeyStroke_ThemeSystem());
               themeSystem.addActionListener(Main.this.getHandlers().getThemeSystemAction());
            }
            return themeSystem;
         }

         private JCheckBoxMenuItem getUseUnknown() {
            if (useUnknown == null) {
               useUnknown = new JCheckBoxMenuItem("Use '?'");
               useUnknown.setMnemonic(Main.KeyCombo.getMnemonic_UseUnknown());
               useUnknown.setAccelerator(Main.KeyCombo.getKeyStroke_UseUnknown());
               useUnknown.addActionListener(Main.this.getHandlers().getUseUnknownAction());
            }
            return useUnknown;
         }
         private JCheckBoxMenuItem getUsePause() {
            if (usePause == null) {
               usePause = new JCheckBoxMenuItem("Pause for a background?");
               usePause.setMnemonic(Main.KeyCombo.getMnemonic_UsePause());
               usePause.setAccelerator(Main.KeyCombo.getKeyStroke_UsePause());
               usePause.addActionListener(Main.this.getHandlers().getUsePauseAction());
            }
            return usePause;
         }

         private JCheckBoxMenuItem getShowElement(EShowElement key) {
            if (showElements == null) {
               showElements = new HashMap<>(EShowElement.values().length);

               for (EShowElement val: EShowElement.values()) {
                  JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(val.getDescription());
                  menuItem.setMnemonic(Main.KeyCombo.getMnemonic_ShowElements(val));
                  menuItem.setAccelerator(Main.KeyCombo.getKeyStroke_ShowElements(val));
                  menuItem.addActionListener(Main.this.getHandlers().getShowElementAction(val));

                  showElements.put(val, menuItem);
               }
            }
            return showElements.get(key);
         }
      }
      class Help extends JMenu {
         private static final long serialVersionUID = 1L;

         private JMenuItem champions, statistics, about;

         private JMenuItem getChampions() {
            if (champions == null) {
               champions = new JMenuItem("Champions");
               champions.setMnemonic(Main.KeyCombo.getMnemonic_Champions());
               champions.setAccelerator(Main.KeyCombo.getKeyStroke_Champions());
               champions.addActionListener(Main.this.getHandlers().getChampionsAction());
            }
            return champions;
         }
         private JMenuItem getStatistics() {
            if (statistics == null) {
               statistics = new JMenuItem("Statistics");
               statistics.setMnemonic(Main.KeyCombo.getMnemonic_Statistics());
               statistics.setAccelerator(Main.KeyCombo.getKeyStroke_Statistics());
               statistics.addActionListener(Main.this.getHandlers().getStatisticsAction());
            }
            return statistics;
         }
         private JMenuItem getAbout() {
            if (about == null) {
               about = new JMenuItem("About");
               about.setMnemonic(Main.KeyCombo.getMnemonic_About());
               about.setAccelerator(Main.KeyCombo.getKeyStroke_About());
               about.addActionListener(Main.this.getHandlers().getAboutAction());
            }
            return about;
         }

         Help() {
            super("Help");
            initialize();
         }
         private void initialize() {
            this.setMnemonic(Main.KeyCombo.getMnemonic_MenuHelp());
            this.add(getChampions());
            this.add(getStatistics());
            this.add(new JSeparator());
            this.add(getAbout());
         }
      }

      private Game game;
      private Mosaics mosaics;
      private Options options;
      private Help help;

      private Game getGame() {
         if (game == null)
            game = new Game();
         return game;
      }
      private Mosaics getMosaics() {
         if (mosaics == null)
            mosaics = new Mosaics();
         return mosaics;
      }
      private Options getOptions() {
         if (options == null)
            options = new Options();
         return options;
      }
      private Help getHelp() {
         if (help == null)
            help = new Help();
         return help;
      }

      public MainMenu() {
         super();
         initialise();
      }
      void initialise() {
         this.setLayout(new FlowLayout(FlowLayout.LEFT, 0,0));

         this.add(getGame());
         this.add(getMosaics());
         this.add(getOptions());
         this.add(getHelp());

//         this.setToolTipText("main menu");

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
         dim.width = getMosaicView().getControl().getPreferredSize().width;
         return dim;
      }

      @Override
      public void close() {
         this.getGame().close();
         this.getMosaics().close();
      }

   }

   public enum EBtnNewGameState {
      eNormal,
      ePressed,
      eSelected,
      eDisabled,
      eDisabledSelected,
      eRollover,
      eRolloverSelected,

      // addons
      eNormalMosaic,
      eNormalWin,
      eNormalLoss;

      public Smile.EType mapToSmileType() {
         switch (this) {
         case eNormal          : return EType.Face_WhiteSmiling;
         case ePressed         : return EType.Face_SavouringDeliciousFood;
         case eSelected        : return null;
         case eDisabled        : return null;
         case eDisabledSelected: return null;
         case eRollover        : return EType.Face_WhiteSmiling;
         case eRolloverSelected: return null;
         case eNormalMosaic    : return EType.Face_Grinning;
         case eNormalWin       : return EType.Face_SmilingWithSunglasses;
         case eNormalLoss      : return EType.Face_Disappointed;
         }
         throw new RuntimeException("Map me...");
      }
   }
   public enum EBtnPauseState {
      eNormal,
      ePressed,
      eSelected,
      eDisabled,
      eDisabledSelected,
      eRollover,
      eRolloverSelected,

      /** типа ход ассистента - задел на будущее */
      eAssistant;

      public Smile.EType mapToSmileType() {
         switch (this) {
         case eNormal          : return EType.Face_EyesOpen;
         case ePressed         : return EType.Face_WinkingEyeLeft;
         case eSelected        : return EType.Face_EyesClosed;
         case eDisabled        : return EType.Eyes_OpenDisabled;
         case eDisabledSelected: return EType.Eyes_ClosedDisabled;
         case eRollover        : return EType.Face_EyesOpen;
         case eRolloverSelected: return EType.Face_WinkingEyeRight;
         case eAssistant       : return EType.Face_Assistant;
         }
         throw new RuntimeException("Map me...");
      }
   }

   class Toolbar extends JPanel {
      private static final long serialVersionUID = 1L;

      private JTextField edtMinesLeft, edtTimePlay;
      private BtnNew btnNew;
      private BtnPause btnPause;

      private Icon getSmileIco(Smile.EType smileType, int sizeX, int sizeY) {
         return new Smile(smileType, sizeX, sizeY);
//         return ImgUtils.zoom(new Smile(smileType, 300, 300), sizeX, sizeY);
      }
      public Icon getSmileIco(EBtnNewGameState btnNewGameState) {
         Smile.EType smileType = btnNewGameState.mapToSmileType();
         if (smileType == null)
            return null;
         int size = (btnNewGameState == EBtnNewGameState.ePressed) ||
                    (btnNewGameState == EBtnNewGameState.eRollover)
                  ? 25 : 24;
         return getSmileIco(smileType, size, size);
      }
      public Icon getSmileIco(EBtnPauseState btnPauseState) {
         Smile.EType smileType = btnPauseState.mapToSmileType();
         if (smileType == null)
            return null;
         int size = (btnPauseState == EBtnPauseState.ePressed) ||
                    (btnPauseState == EBtnPauseState.eRollover) ||
                    (btnPauseState == EBtnPauseState.eRolloverSelected)
                  ? 25 : 24;
         return getSmileIco(smileType, size, size);
      }

      class BtnNew extends JButton {
         private static final long serialVersionUID = 1L;

         public BtnNew() {
            super();
            initialize();
         }
         private void initialize() {
            this.setAction(Main.this.getHandlers().getGameNewAction());
            this.setFocusable(false);

            if (getSmileIco(EBtnNewGameState.eNormal) == null) {
               this.setText("N");
            } else {
               this.setIcon(getSmileIco(EBtnNewGameState.eNormal));
               this.setPressedIcon(getSmileIco(EBtnNewGameState.ePressed));
               this.setSelectedIcon(getSmileIco(EBtnNewGameState.eSelected));
               this.setRolloverIcon(getSmileIco(EBtnNewGameState.eRollover));
               this.setRolloverSelectedIcon(getSmileIco(EBtnNewGameState.eRolloverSelected));
               this.setRolloverEnabled(true);
               this.setDisabledIcon(getSmileIco(EBtnNewGameState.eDisabled));
               this.setDisabledSelectedIcon(getSmileIco(EBtnNewGameState.eDisabledSelected));
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
      class BtnPause extends JToggleButton {
         private static final long serialVersionUID = 1L;

         public BtnPause() {
            super(Main.this.getHandlers().getPauseAction());
            initialize();
         }
         private void initialize() {
            this.setFocusable(false);
            this.setEnabled(false);

            if (getSmileIco(EBtnPauseState.eNormal) == null) {
               this.setText("P");
            } else {
               this.setIcon(getSmileIco(EBtnPauseState.eNormal));
               this.setPressedIcon(getSmileIco(EBtnPauseState.ePressed));
               this.setSelectedIcon(getSmileIco(EBtnPauseState.eSelected));
               this.setRolloverIcon(getSmileIco(EBtnPauseState.eRollover));
               this.setRolloverSelectedIcon(getSmileIco(EBtnPauseState.eRolloverSelected));
               this.setRolloverEnabled(true);
               this.setDisabledIcon(getSmileIco(EBtnPauseState.eDisabled));
               this.setDisabledSelectedIcon(getSmileIco(EBtnPauseState.eDisabledSelected));
            }
            this.setToolTipText("Pause");
         }
         @Override
         public Insets getInsets() {
            Insets ins = super.getInsets();
            // иначе не виден текст (если нет картинки)
            ins.bottom=ins.left=ins.right=ins.top = 0;
            return ins;
         }
      }

      private JTextField getEdtMinesLeft() {
         if (edtMinesLeft == null) {
            edtMinesLeft = new JTextField("MinesLeft");
//            edtMinesLeft.setBorder(BorderFactory.createLoweredBevelBorder());
//            edtMinesLeft.setBorder(BorderFactory.createEtchedBorder());
            edtMinesLeft.setFocusable(false);
            edtMinesLeft.setEditable(false);
            edtMinesLeft.setToolTipText("Mines left");
         }
         return edtMinesLeft;
      }
      private JTextField getEdtTimePlay() {
         if (edtTimePlay == null) {
            edtTimePlay = new JTextField("TimePlay");
//            edtTimePlay.setBorder(BorderFactory.createLoweredBevelBorder());
//            edtTimePlay.setBorder(BorderFactory.createEtchedBorder());
            edtTimePlay.setFocusable(false);
            edtTimePlay.setEditable(false);
            edtTimePlay.setToolTipText("time...");
         }
         return edtTimePlay;
      }
      private BtnNew getBtnNew() {
         if (btnNew == null)
            btnNew = new BtnNew();
         return btnNew;
      }
      private BtnPause getBtnPause() {
         if (btnPause == null)
            btnPause = new BtnPause();
         return btnPause;
      }

      public Toolbar() {
         super();
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
//            getEdtTimePlay().setMinimumSize(dimEdt);
            getEdtTimePlay().setMaximumSize(dimEdt);
            getEdtMinesLeft().setPreferredSize(dimEdt);
//            getEdtMinesLeft().setMinimumSize(dimEdt);
            getEdtMinesLeft().setMaximumSize(dimEdt);
         }
         {
            this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

            this.setBorder(new CompoundBorder(BorderFactory.createRaisedBevelBorder(), new EmptyBorder(2, 2, 2, 2)));
//            this.setBorder(new CompoundBorder(BorderFactory.createEtchedBorder(), new EmptyBorder(2, 2, 2, 2)));
//            this.setBorder(new CompoundBorder(BorderFactory.createMatteBorder(3, 3, 3, 3, new Color(255,0,0, 220)), new EmptyBorder(2, 2, 2, 2)));

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
   }

   class PausePanel extends JLabel implements AutoCloseable {
      private static final long serialVersionUID = 1L;

      public PausePanel(String text) {
         super(text);
         this.setHorizontalAlignment(SwingConstants.CENTER);
         Font font = this.getFont();
         font = new Font(font.getName(), font.getStyle(), 45);
         this.setFont(font);
         this.setBorder(BorderFactory.createRaisedBevelBorder());
//       this.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
//       this.setFocusable(true);
         this.addMouseListener(Main.this.getHandlers().getPausePanelMouseListener());
      }

      Logo.Icon _logo;
      private Logo.Icon getLogo() {
         if (_logo == null) {
            _logo = new Logo.Icon();
            _logo.setUseGradient(!true);
            _logo.setPadding(10);
            _logo.setRotateMode(Logo.ERotateMode.color);
            _logo.setRedrawInterval(50);

            _logo.addListener(ev -> {
               if (Logo.PROPERTY_IMAGE.equals(ev.getPropertyName())) {
                  PausePanel.this.repaint();
               }
            });
         }
         return _logo;
      }

      @Override
      protected void paintComponent(Graphics g) {
         Dimension sizeOutward = this.getSize();
         Logo.Icon logo = getLogo();
         logo.setSize((int)Math.min(sizeOutward.getWidth(), sizeOutward.getHeight()));

         logo.getImage().paintIcon(this, g,
               (sizeOutward.width -logo.getSize().width)>>1,
               (sizeOutward.height-logo.getSize().height)>>1);
      }

      public void animateLogo(boolean start) {
         getLogo().setRotate(start);
      }

      @Override
      public void close() {
         removeMouseListener(Main.this.getHandlers().getPausePanelMouseListener());
         getLogo().close();
      }

      @Override
      public Dimension getPreferredSize() {
//         return super.getPreferredSize();
         return getMosaicView().getControl().getPreferredSize();
      }
   }

   class StatusBar extends JLabel {
      private static final long serialVersionUID = 1L;

      public StatusBar() {
         super();
         initialize();
      }
      private void initialize() {
         this.setText("Click count: 0");
         this.setToolTipText("Sensible clicks...");
         this.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
      }
      public void setClickCount(int cnt) {
         this.setText("Click count: " + Integer.toString(cnt));
      }
   }

   @Override
   public JPanel getContentPane() {
      if (contentPane == null) {
         contentPane = new JPanel();
         JPanel centerPanel = new JPanel();

         contentPane.setBorder(
//               BorderFactory.createLoweredBevelBorder()
               BorderFactory.createEmptyBorder()
               );
         contentPane.setLayout(new BorderLayout());
         contentPane.add(getToolbar(), BorderLayout.NORTH);
         contentPane.add(centerPanel, BorderLayout.CENTER);
         contentPane.add(getStatusBar(), BorderLayout.SOUTH);

//         centerPanel.setBorder(BorderFactory.createRaisedBevelBorder());

         LayoutManager lm = centerPanel.getLayout();
         if (lm instanceof FlowLayout) {
            FlowLayout fl = (FlowLayout) lm;
            fl.setHgap(0);
            fl.setVgap(0);
         } else
            centerPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0,0));

         centerPanel.add(getMosaicView().getControl());
         centerPanel.add(getPausePanel());
      }
      return contentPane;
   }

   public MainMenu getMenu() {
      if (menu == null)
         menu = new MainMenu();
      return menu;
   }

   private Toolbar getToolbar() {
      if (toolbar == null)
         toolbar = new Toolbar();
      return toolbar;
   }

   public MosaicController getMosaicField() {
      if (mosaicCtrl == null) {
         mosaicCtrl = new MosaicControllerSwing();
         mosaicCtrl.getMosaic().addListener(this);
      }
      return mosaicCtrl;
   }
   /** мозаика */
   public MosaicBase getMosaic() {
      return getMosaicField().getMosaic();
   }
   public MosaicView getMosaicView() {
      return getMosaicField().getView();
   }

   private PausePanel getPausePanel() {
      if (pausePanel == null) {
         pausePanel = new PausePanel("Pause");
         pausePanel.setVisible(false);
      }
      return pausePanel;
   }

   private StatusBar getStatusBar() {
      if (statusBar == null)
         statusBar = new StatusBar();
      return statusBar;
   }

   public static void main(String[] args) {
      //ViewAllEvents();
      //setSysOut();
      //printSystemProperties();
      SwingUtilities.invokeLater(() ->
         new Main().setVisible(true)
      );
   }

   public Main() {
      super();
      initialize();
   }

   void iconify() {
      if (Toolkit.getDefaultToolkit().isFrameStateSupported(Frame.ICONIFIED)) {
         int state = this.getExtendedState();

         // Set the iconified bit
         state ^= Frame.ICONIFIED;

         // Iconify the frame
         this.setExtendedState(state);
      }
   }

   void toCenterScreen() {
//      Dimension desktopSize = getDesktopSize();
//      Dimension sizeWin = getRealSize();
//      setLocation((desktopSize.width - sizeWin.width) / 2, (desktopSize.height - sizeWin.height) / 2);
      this.setLocationRelativeTo(null);
   }

   private void initialize() {
//      System.out.println(getProperties());

      {
         ToolTipManager ttm = ToolTipManager.sharedInstance();
         ttm.setInitialDelay(ttm.getInitialDelay() + 3000);
      }

//      iconify();
      this.setResizable(false);

      boolean isZoomAlwaysMax;
      final Point startLocation = new Point();
      boolean defaultData;
      boolean doNotAskStartup;
      { // aplly data from SerializeProjModel
         final SerializeProjData spm = new SerializeProjData();
         defaultData = !spm.Load();

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
         this.setUndecorated(!spm.getShowElement(EShowElement.eCaption));

         MosaicBase mosaic = getMosaic();
         mosaic.setSizeField(spm.getSizeField());
         mosaic.setMosaicType(spm.getMosaicType());
         mosaic.setMinesCount(spm.getMinesCount());
         mosaic.setArea(spm.getArea());

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
         getToolbar().getBtnPause().setVisible(spm.isUsePause());
         getMosaic().setUseUnknown(            spm.isUseUnknown());

         for (EShowElement key: EShowElement.values()) {
            getMenu().getOptions().getShowElement(key).setSelected(spm.getShowElement(key));
         }
         getMenu().     setVisible(spm.getShowElement(EShowElement.eMenu));
         applyInputActionMenuMap  (spm.getShowElement(EShowElement.eMenu));
         getToolbar().  setVisible(spm.getShowElement(EShowElement.eToolbar));
         getStatusBar().setVisible(spm.getShowElement(EShowElement.eStatusbar));

         startLocation.x = spm.getLocation().x;
         startLocation.y = spm.getLocation().y;
      }

      this.setContentPane(getContentPane());

      this.setJMenuBar(getMenu());
      this.setTitle("FastMines");
      this._logo = new Logo.Image();
      this._logo.setUseGradient(true);
      this._logo.setSize(128);
      this._logo.setPadding(1);
      this._logo.setBackgroundColor(Logo.Image.DefaultBkColor);
      this._logo.setRotate(true);
      this._logo.setRotateMode(Logo.ERotateMode.combi);
      this.setIconImage(_logo.getImage());
      this._logo.addListener(ev -> {
         if (Logo.PROPERTY_IMAGE.equals(ev.getPropertyName()))
            this.setIconImage(_logo.getImage());
      });

      getMosaicField().setOnClickEvent(this.getHandlers().getMosaicClickHandler());
//      this.getHandlers().getMosaicListener().OnChangedArea(new MosaicEvent(getMosaic())); // TODO: это нужно только тогда, когда нет десериализации
      getToolbar().getEdtMinesLeft().setText(Integer.toString(getMosaic().getCountMinesLeft()));
      getToolbar().getEdtTimePlay().setText("0");

      //setDefaultCloseOperation(EXIT_ON_CLOSE);
      this.addWindowListener(this.getHandlers().getWindowListener());
//      this.addKeyListener(new KeyListener() {
//         @Override public void keyTyped   (KeyEvent e) { System.out.println("Main::KeyListener:keyTyped: "    + e); }
//         @Override public void keyReleased(KeyEvent e) { System.out.println("Main::KeyListener:keyReleased: " + e); }
//         @Override public void keyPressed (KeyEvent e) { System.out.println("Main::KeyListener:keyPressed: "  + e); }
//      });
      this.addWindowFocusListener(this.getHandlers().getWindowFocusListener());
      this.addMouseWheelListener(this.getHandlers().getMouseWheelListener());
//      this.addWindowListener(new WindowAdapter() {
//
//         @Override
//         public void windowActivated(WindowEvent e) {
////            if (Main.this.isAlwaysOnTopSupported())
//               try {
//                  System.out.println("windowActivated");
//                  Main.this.setAlwaysOnTop(true);
//               } catch (Exception ex) {
//                  ex.printStackTrace();
//               }
//            super.windowActivated(e);
//         }
//         @Override
//         public void windowDeactivated(WindowEvent e) {
////            System.out.println("windowDeactivated: " + e.getSource());
//            if (Main.this.isAlwaysOnTopSupported())
//               try {
//                  System.out.println("windowDeactivated");
//                  Main.this.setAlwaysOnTop(false);
//               } catch (Exception ex) {
//                  ex.printStackTrace();
//               }
//            super.windowDeactivated(e);
//         }
//      });

//      this.addComponentListener(new ComponentAdapter() {
//         @Override
//         public void componentShown(ComponentEvent ev) {
//            Main.this.RecheckLocation();
//         }
//         @Override
//         public void componentHidden(ComponentEvent ev) {
//            System.out.println ( "Component hidden" );
//         }
//      });

      customKeyBinding();

      pack();
//      System.out.println("ThreadId=" + Thread.currentThread().getId() + ": Main::initialize: after pack");
      if (defaultData)
         setLocationRelativeTo(null);
      else
         setLocation(startLocation);
//      System.out.println("Main::initialize: after setLocation");

      _initialized = true;
      if (isZoomAlwaysMax)
         invokeLater(() -> areaAlwaysMax(new ActionEvent(Main.this, 0, null)));
      if (!doNotAskStartup)
         invokeLater(() ->
            getHandlers().getPlayerManageAction().actionPerformed(new ActionEvent(Main.this, 0, "Main::initialize"))
         );
   }
   boolean _initialized;

   ESkillLevel getSkillLevel() {
      EMosaic eMosaic = getMosaic().getMosaicType();
      Matrisize sizeFld = getMosaic().getSizeField();
      int numberMines = getMosaic().getMinesCount();

      if (sizeFld.equals(ESkillLevel.eBeginner.DefaultSize()) && (numberMines == ESkillLevel.eBeginner.GetNumberMines(eMosaic)))
         return ESkillLevel.eBeginner;
      if (sizeFld.equals(ESkillLevel.eAmateur.DefaultSize()) && (numberMines == ESkillLevel.eAmateur.GetNumberMines(eMosaic)))
         return ESkillLevel.eAmateur;
      if (sizeFld.equals(ESkillLevel.eProfi.DefaultSize()) && (numberMines == ESkillLevel.eProfi.GetNumberMines(eMosaic)))
         return ESkillLevel.eProfi;
      if (sizeFld.equals(ESkillLevel.eCrazy.DefaultSize()) && (numberMines == ESkillLevel.eCrazy.GetNumberMines(eMosaic)))
         return ESkillLevel.eCrazy;
      return ESkillLevel.eCustom;
   }

   /** my key combinations */
   static final class KeyCombo {
      public static final KeyStroke getKeyStroke_Minimized      () { return KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false); }
      public static final KeyStroke getKeyStroke_CenterScreenPos() { return KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD5, Event.CTRL_MASK, false); }
      public static final KeyStroke getKeyStroke_NewGame        () { return KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0, !false); }
      public static final KeyStroke getKeyStroke_SkillLevel(ESkillLevel key) { return KeyStroke.getKeyStroke(KeyEvent.VK_1+key.ordinal(), 0, !false); }
      public static final KeyStroke getKeyStroke_PlayerManage   () { return KeyStroke.getKeyStroke(KeyEvent.VK_BACK_QUOTE, 0, false); }
      public static final KeyStroke getKeyStroke_Exit           () { return KeyStroke.getKeyStroke(KeyEvent.VK_Q, Event.ALT_MASK, false); }
      public static final KeyStroke getKeyStroke_Zoom(EZoomInterface key) {
         switch (key) {
         case eMax: return KeyStroke.getKeyStroke(KeyEvent.VK_MULTIPLY, 0, !false);
         case eMin: return KeyStroke.getKeyStroke(KeyEvent.VK_DIVIDE  , 0, !false);
         case eInc: return KeyStroke.getKeyStroke(KeyEvent.VK_ADD     , 0, !false);
         case eDec: return KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, 0, !false);
         default: return null; // throw new RuntimeException();
         }
      }
      public static final KeyStroke getKeyStroke_ZoomIncAlternative() { return KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, 0, !false); }
      public static final KeyStroke getKeyStroke_ZoomDecAlternative() { return KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, 0, !false); }
      public static final KeyStroke getKeyStroke_ThemeDefault   () { return KeyStroke.getKeyStroke(KeyEvent.VK_D, Event.CTRL_MASK, false); }
      public static final KeyStroke getKeyStroke_ThemeSystem    () { return KeyStroke.getKeyStroke(KeyEvent.VK_Y, Event.CTRL_MASK, false); }
      public static final KeyStroke getKeyStroke_UseUnknown     () { return null; }
      public static final KeyStroke getKeyStroke_UsePause       () { return null; }
      public static final KeyStroke getKeyStroke_ShowElements   (EShowElement key) {
//         switch (key) {
//         case eCaption  : return KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0, false);
//         case eMenu     : return KeyStroke.getKeyStroke(KeyEvent.VK_F10, 0, false);
//         case eToolbar  : return KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0, false);
//         case eStatusbar: return KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0, false);
//         default: return null; // throw new RuntimeException();
//         }
         return KeyStroke.getKeyStroke(KeyEvent.VK_F9 + key.ordinal(), 0, false);
      }
      public static final KeyStroke getKeyStroke_Pause1         () { return KeyStroke.getKeyStroke(KeyEvent.VK_PAUSE, 0, false); }
      public static final KeyStroke getKeyStroke_Pause2         () { return KeyStroke.getKeyStroke(KeyEvent.VK_P, 0, false); }
      public static final KeyStroke getKeyStroke_About          () { return KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0, false); }
      public static final KeyStroke getKeyStroke_Champions      () { return KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0, false); }
      public static final KeyStroke getKeyStroke_Statistics     () { return KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0, false); }

      public static final KeyStroke getKeyStroke_Mosaic(EMosaic key) { return null; }//KeyStroke.getKeyStroke(KeyEvent.VK_, 0, false); }

      public static final KeyStroke getKeyStroke_MosaicFieldXInc() { return KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, Event.ALT_MASK, true); }
      public static final KeyStroke getKeyStroke_MosaicFieldXDec() { return KeyStroke.getKeyStroke(KeyEvent.VK_LEFT , Event.ALT_MASK, true); }
      public static final KeyStroke getKeyStroke_MosaicFieldYInc() { return KeyStroke.getKeyStroke(KeyEvent.VK_DOWN , Event.ALT_MASK, true); }
      public static final KeyStroke getKeyStroke_MosaicFieldYDec() { return KeyStroke.getKeyStroke(KeyEvent.VK_UP   , Event.ALT_MASK, true); }

      public static final KeyStroke getKeyStroke_SelectMosaic(EMosaicGroup key) { return KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD3+key.ordinal(), 0, false); }

      // ------ Mnemonics
      private static final int VK_NULL = 0;//'\0';

      public static final int getMnemonic_MenuGame       () { return KeyEvent.VK_G; }
      public static final int getMnemonic_MenuMosaic     () { return KeyEvent.VK_M; }
      public static final int getMnemonic_MenuMosaicGroup(EMosaicGroup key) { return KeyEvent.VK_NUMPAD3 + key.ordinal(); }
      public static final int getMnemonic_MenuOptions    () { return KeyEvent.VK_O; }
      public static final int getMnemonic_MenuZoom       () { return KeyEvent.VK_Z; }
      public static final int getMnemonic_MenuHelp       () { return KeyEvent.VK_H; }

      public static final int getMnemonic_NewGame        () { return KeyEvent.VK_N; }
      public static final int getMnemonic_SkillLevel(ESkillLevel key) {
         switch (key) {
         case eBeginner: return KeyEvent.VK_B;
         case eAmateur : return KeyEvent.VK_A;
         case eProfi   : return KeyEvent.VK_P;
         case eCrazy   : return KeyEvent.VK_R;
         case eCustom  : return KeyEvent.VK_C;
         default       : return VK_NULL;
         }
      }
      public static final int getMnemonic_PlayerManage   () { return KeyEvent.VK_P; }
      public static final int getMnemonic_Exit           () { return KeyEvent.VK_E; }
      public static final int getMnemonic_Zoom(EZoomInterface key) { return VK_NULL; }
      public static final int getMnemonic_Theme          () { return KeyEvent.VK_T; }
      public static final int getMnemonic_ThemeDefault   () { return KeyEvent.VK_D; }
      public static final int getMnemonic_ThemeSystem    () { return KeyEvent.VK_Y; }
      public static final int getMnemonic_UseUnknown     () { return KeyEvent.VK_U; }
      public static final int getMnemonic_UsePause       () { return KeyEvent.VK_P; }
      public static final int getMnemonic_ShowElements   (EShowElement key) { return KeyEvent.VK_S; }
      public static final int getMnemonic_About          () { return VK_NULL; }
      public static final int getMnemonic_Champions      () { return VK_NULL; }
      public static final int getMnemonic_Statistics     () { return VK_NULL; }

      public static final int getMnemonic_Mosaic(EMosaic key) { return VK_NULL; }
}

   /** прочие комбинации клавиш (не из меню) */
   void customKeyBinding() {
      if (getRootPane().getInputMap().size() == 0) {
         // on ESC key iconic frame
         Object keyBind = "Minimized";
         getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyCombo.getKeyStroke_Minimized(), keyBind);
         getRootPane().getActionMap().put(keyBind, this.getHandlers().getMinimizedAction());

         // Num5 - center screen window
         keyBind = "CenterScreenPos";
         getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyCombo.getKeyStroke_CenterScreenPos(), keyBind);
         getRootPane().getActionMap().put(keyBind, this.getHandlers().getCenterScreenAction());

         keyBind = "Pause";
         getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyCombo.getKeyStroke_Pause1(), keyBind);
         getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyCombo.getKeyStroke_Pause2(), keyBind);
         getRootPane().getActionMap().put(keyBind, this.getHandlers().getPauseAction());

         // < > ^ V
         keyBind = "Increment by X mosaic field";
         getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyCombo.getKeyStroke_MosaicFieldXInc(), keyBind);
         getRootPane().getActionMap().put(keyBind, this.getHandlers().getMosaicSizeIncX());
         keyBind = "Decrement by X mosaic field";
         getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyCombo.getKeyStroke_MosaicFieldXDec(), keyBind);
         getRootPane().getActionMap().put(keyBind, this.getHandlers().getMosaicSizeDecX());
         keyBind = "Increment by Y mosaic field";
         getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyCombo.getKeyStroke_MosaicFieldYInc(), keyBind);
         getRootPane().getActionMap().put(keyBind, this.getHandlers().getMosaicSizeIncY());
         keyBind = "Decrement by Y mosaic field";
         getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyCombo.getKeyStroke_MosaicFieldYDec(), keyBind);
         getRootPane().getActionMap().put(keyBind, this.getHandlers().getMosaicSizeDecY());

         for (EMosaicGroup mosGr: EMosaicGroup.values()) {
            keyBind = "SelectMosaic_"+mosGr;
            getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyCombo.getKeyStroke_SelectMosaic(mosGr), keyBind);
            getRootPane().getActionMap().put(keyBind, this.getHandlers().getSelectMosaicAction(mosGr));
         }
      }
   }

   private Pair<InputMap, ActionMap> keyPairBindAsMenuAccelerator;
   Pair<InputMap, ActionMap> getKeyPairBindAsMenuAccelerator() {
      if (keyPairBindAsMenuAccelerator == null) {
         InputMap inputMap = new ComponentInputMap(getRootPane());
         ActionMap actionMap = new ActionMap();
         keyPairBindAsMenuAccelerator = new Pair<InputMap, ActionMap>(inputMap, actionMap);

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
      if (getMosaic().getGameStatus() != EGameStatus.eGSPlay)
         return;

//      System.out.println("> FMG::ChangePause: " + KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner() );

      boolean paused = isPaused();
      getToolbar().getBtnPause().setSelected(!paused);
      getPausePanel().animateLogo(!paused);
      if (paused) {
         getTimerGame().restart();

         getPausePanel().setVisible(false);
         getMosaicView().getControl().setVisible(true);
         getMosaicView().getControl().requestFocusInWindow();
      } else {
         getTimerGame().stop();

         getMosaicView().getControl().setVisible(false);
         getPausePanel().setVisible(true);
         getRootPane().requestFocusInWindow(); // ! иначе на компонентах нат фокуса, и mouse wheel не пашет...
      }
//      System.out.println("< FMG::ChangePause: " + KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner() );
   }

   void gameNew() {
      if (isPaused())
         changePause();
      else
         getMosaic().GameNew();
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

         spm.setSizeField(getMosaic().getSizeField());
         spm.setMosaicType(getMosaic().getMosaicType());
         spm.setMinesCount(getMosaic().getMinesCount());
         spm.setArea(getMosaic().getArea());

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
         spm.setLocation(Cast.toPoint(this.getLocation()));

         spm.Save();
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

//      setVisible(false);
      getMosaic().close();
      _logo.close();


      this.removeWindowListener     (this.getHandlers().getWindowListener());
      this.removeWindowFocusListener(this.getHandlers().getWindowFocusListener());
      this.removeMouseWheelListener (this.getHandlers().getMouseWheelListener());

      dispose();
//      System.exit(0);
   }

   /** Попытаться установить новый размер на мозаику (при возможности, сохраняя ESkillLevel) */
   void changeGame(Matrisize newSize) {
      ESkillLevel skill = getSkillLevel();
      changeGame(newSize,
            (skill == ESkillLevel.eCustom)
               ? getMosaic().getMinesCount()
               : skill.GetNumberMines(getMosaic().getMosaicType()));
   }

   /** Поменять игру на новую мозаику */
   public void changeGame(EMosaic mosaicType) {
      if (isPaused())
         changePause();

      ESkillLevel skill = getSkillLevel();
      getMosaic().setMosaicType(mosaicType);
      if (skill != ESkillLevel.eCustom) {
         int numberMines = skill.GetNumberMines(mosaicType);
         getMosaic().setMinesCount(numberMines);
      }
   }

   /** Поменять игру на новый размер & кол-во мин */
   public void changeGame(Matrisize newSize, int numberMines) {
      if ((newSize.m < 1) || (newSize.n < 1)) {
         Beep();
         return;
      }
      if (numberMines < 0) {
         Beep();
         return;
      }

      if (isPaused())
         changePause();

      getMosaic().setSizeField(newSize);
      getMosaic().setMinesCount(numberMines);
   }

   /** Поменять игру на новый уровень сложности */
   void changeGame(ESkillLevel skill) {
      if (skill == ESkillLevel.eCustom) {
         //System.out.println("... dialog box 'Select custom skill level...' ");
         getCustomSkillDialog().setVisible(!getCustomSkillDialog().isVisible());
         return;
      }

      if (isPaused())
         changePause();

      int numberMines = skill.GetNumberMines(getMosaic().getMosaicType());
      Matrisize sizeFld = skill.DefaultSize();

      getMosaic().setSizeField(sizeFld);
      getMosaic().setMinesCount(numberMines);
   }

   /** get margin around mosaic control */
   Insets getMosaicMargin() {
      Insets mainPadding = getMenu().getOptions().getShowElement(EShowElement.eCaption).isSelected()
            ? this.getInsets()
            : new Insets(0,0,0,0);
      Dimension menuSize = getMenu().getOptions().getShowElement(EShowElement.eMenu).isSelected()
            ? getMenu().getSize()
            : new Dimension();
      Dimension toolbarSize = getMenu().getOptions().getShowElement(EShowElement.eToolbar).isSelected()
            ? getToolbar().getSize()
            : new Dimension();
      Dimension statusBarSize = getMenu().getOptions().getShowElement(EShowElement.eStatusbar).isSelected()
            ? getStatusBar().getSize()
            : new Dimension();
      return new Insets(
            mainPadding.top + menuSize.height + toolbarSize.height,
            mainPadding.left,
            mainPadding.bottom + statusBarSize.height,
            mainPadding.right);
   }

//   /** узнать размер окна проекта при указанном размере окна мозаики */
//   Dimension calcMainSize(Size sizeMosaicInPixel) {
//      Insets mosaicMargin = getMosaicMargin();
//      return new Dimension(
//            mosaicMargin.left + sizeMosaicInPixel.width  + mosaicMargin.right,
//            mosaicMargin.top  + sizeMosaicInPixel.height + mosaicMargin.bottom);
//   }

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

   /** узнаю мах размер площади ячеек мозаики, при котором окно проекта вмещается в текущее разрешение экрана
    * @param mosaicSizeField - интересуемый размер поля мозаики
    * @return макс площадь ячейки
    */
   double calcMaxArea(Matrisize mosaicSizeField) {
      SizeDouble sizeMosaicIn = calcMosaicWindowSize(ScreenResolutionHelper.getDesktopSize(this.getGraphicsConfiguration()));
      SizeDouble sizeMosaicOut = new SizeDouble();
      double area = MosaicHelper.findAreaBySize(getMosaic().getMosaicType(), mosaicSizeField, sizeMosaicIn, sizeMosaicOut);
      //System.out.println("Main.calcMaxArea: area="+area);
      return area;
   }

   /**
    * узнаю max размер поля мозаики, при котором окно проекта вмещается в текущее разрешение экрана
    * @param area - интересуемая площадь ячеек мозаики
    * @return max размер поля мозаики
    */
   public Matrisize calcMaxMosaicSize(double area) {
      SizeDouble sizeMosaic = calcMosaicWindowSize(ScreenResolutionHelper.getDesktopSize(this.getGraphicsConfiguration()));
      return MosaicHelper.findSizeByArea(getMosaic().getCellAttr(), sizeMosaic);
   }

   /** проверить что находится в рамках экрана */
   void recheckLocation() {
      if (!_shedulePack) {
         _shedulePack = true;
         invokeLater(() -> {
            _shedulePack = false;

            {
               fmg.common.geom.Point center = Rect.getCenter(Cast.toRect(this.getBounds()));
//               Point center = new Rect(this.getBounds()).center();
               pack();
//               Rectangle newBounds = new Rect(this.getBounds()).center(center);
               Rectangle newBounds = Cast.toRect(Rect.setCenter(Cast.toRect(this.getBounds()), center));
               this.setBounds(newBounds);
               revalidate();
            }

//            if (!_sheduleCheckArea) {
//               _sheduleCheckArea = true;
//               invokeLater(() -> {
//                  _sheduleCheckArea = false;
//                  if (!this.isVisible())
//                     return;
                  if (getMenu().getOptions().getZoomItem(EZoomInterface.eAlwaysMax).isSelected()) {
                     areaMax();
                  } else {
                     double maxArea = calcMaxArea(getMosaic().getSizeField());
                     if (maxArea < getMosaic().getArea())
                        setArea(maxArea);
                  }

                  // check that within the screen
                  {
                     Dimension screenSize = ScreenResolutionHelper.getScreenSize();
                     Insets padding = ScreenResolutionHelper.getScreenPadding(this.getGraphicsConfiguration());
                     Rect rcThis = Cast.toRect(this.getBounds());

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
                        this.setLocation(Cast.toPoint(rcThis.PointLT()));
                  }
//               });
//            }

         });
      }
   }
   //private boolean _sheduleCheckArea;
   private boolean _shedulePack;

   /** getMosaic().setArea(...) */
   void setArea(double newArea) {
      //System.out.println("Mosaic.setArea: newArea=" + newArea);
      newArea = Math.min(newArea, calcMaxArea(getMosaic().getSizeField()));
      getMosaic().setArea(newArea);
   }

   /** Zoom + */
   void areaInc() {
      setArea(getMosaic().getArea() * 1.05);
   }
   /** Zoom - */
   void areaDec() {
      setArea(getMosaic().getArea() * 0.95);
   }
   /** Zoom minimum */
   void areaMin() {
      setArea(0);
   }

   /** Zoom maximum */
   void areaMax() {
      double maxArea = calcMaxArea(getMosaic().getSizeField());
      setArea(maxArea);

//      {
//         // Если до вызова AreaMax() меню окна распологалось в две строки, то после
//         // отработки этой ф-ции меню будет в одну строку, т.е., последующий вызов
//         // GetMaximalArea() будет возвращать ещё бОльший результат.
//         // Поэтому надо снова установить максимальное значение плошади ячеек.
//         if (maxArea < CalcMaxArea())
//            AreaMax(); // меню было в две  строки
//         else;          // меню было в одну строку
//      }
   }
   /** Zoom always maximum */
   public void areaAlwaysMax(ActionEvent e) {
//      if (!isMenuEvent(e))
//         getMenu().getOptions().getZoomItem(EZoomInterface.eAlwaysMax).setSelected(!getMenu().getOptions().getZoomItem(EZoomInterface.eAlwaysMax).isSelected());

      boolean checked = getMenu().getOptions().getZoomItem(EZoomInterface.eAlwaysMax).isSelected();

      if (checked)
         areaMax();

      for (EZoomInterface key: EZoomInterface.values())
         switch (key ) {
         case eAlwaysMax: break;
         default: getMenu().getOptions().getZoomItem(key).setEnabled(!checked);
         }
   }

   void optionsThemeDefault() {
      try {
         UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
         SwingUtilities.updateComponentTreeUI(Main.this);
         Main.this.pack();
      } catch (Exception ex) {
         ex.printStackTrace();
      }
   }

   void optionsThemeSystem() {
      try {
         UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
         SwingUtilities.updateComponentTreeUI(Main.this);
         Main.this.pack();
      } catch (Exception ex) {
         ex.printStackTrace();
      }
   }

   void optionsUseUnknown(ActionEvent e) {
      if (!isMenuEvent(e))
         getMenu().getOptions().getUseUnknown().setSelected(!getMenu().getOptions().getUseUnknown().isSelected());

      getMosaic().setUseUnknown(getMenu().getOptions().getUseUnknown().isSelected());
   }

   boolean isUsePause() {
      return getMenu().getOptions().getUsePause().isSelected();
   }
   void optionsUsePause(ActionEvent e) {
      if (!isMenuEvent(e))
         getMenu().getOptions().getUsePause().setSelected(!getMenu().getOptions().getUsePause().isSelected());

      boolean usePause = isUsePause();
      this.getToolbar().getBtnPause().setVisible(usePause);
//      this.getToolbar().revalidate();

      if (!usePause && isPaused())
         changePause();
   }

   void optionsShowElement(EShowElement key, final ActionEvent e) {
      if (!isMenuEvent(e))
         getMenu().getOptions().getShowElement(key).setSelected(!getMenu().getOptions().getShowElement(key).isSelected());

      switch (key) {
      case eCaption:
         {
            final Rectangle rc = getBounds();
            final Map<EShowElement, Boolean> mapShow = new HashMap<>(EShowElement.values().length);
            for (EShowElement val: EShowElement.values())
               mapShow.put(val, new Boolean(getMenu().getOptions().getShowElement(val).isSelected()));

            // вызов this.dispose(); приводит к потере фокуса, т.е, когда идёт игра, - к срабатыванию паузы
            // т.е. нужно позже снять паузу...
            final boolean isNotPaused = (getMosaic().getGameStatus() == EGameStatus.eGSPlay) && !isPaused();
            //if (this.isDisplayable())
               this.dispose();
            invokeLater(() -> {
                  Main.this.setUndecorated(!mapShow.get(EShowElement.eCaption).booleanValue());
                  Main.this.setBounds(rc);
                  Main.this.getMenu()     .setVisible(mapShow.get(EShowElement.eMenu).booleanValue());
                  Main.this.getToolbar()  .setVisible(mapShow.get(EShowElement.eToolbar).booleanValue());
                  Main.this.getStatusBar().setVisible(mapShow.get(EShowElement.eStatusbar).booleanValue());

                  if (isNotPaused && isUsePause())
                     Main.this.changePause();

                  Main.this.setVisible(true);
                  Main.this.pack();
            });
         }
         break;
      case eMenu:
         {
            boolean show = getMenu().getOptions().getShowElement(key).isSelected();
            getMenu().setVisible(show);
            applyInputActionMenuMap(show);
            pack();
         }
         break;
      case eToolbar:
         {
            getToolbar().setVisible(getMenu().getOptions().getShowElement(key).isSelected());
            pack();
         }
         break;
      case eStatusbar:
         {
            boolean sel = getMenu().getOptions().getShowElement(key).isSelected();
            getStatusBar().setVisible(sel);
            pack();
         }
         break;
      }
   }
   void applyInputActionMenuMap(boolean visibleMenu) {
//      if (visibleMenu) {
//         getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).setParent(null);
//         getRootPane().getActionMap().setParent(null);
//      } else {
         Pair<InputMap, ActionMap> bind = getKeyPairBindAsMenuAccelerator();
         getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).setParent(bind.first);
         getRootPane().getActionMap().setParent(bind.second);
//      }
   }

//   @Override
//   protected void processKeyEvent(KeyEvent e) {
//      System.out.println(e);
//      super.processKeyEvent(e);
//   }

   boolean isPaused() {
      return !getMosaicView().getControl().isVisible();
   }
   boolean isMenuEvent(ActionEvent e) {
      Object src = e.getSource();
      return src instanceof JMenuItem;
   }

   private Timer _timerGame;
   Timer getTimerGame() {
      if (_timerGame == null)
         _timerGame = new Timer(1000, getHandlers().getTimePlayAction());
      return _timerGame;
   }

//   private static String getProperties() {
//      StringBuffer sb = new StringBuffer();
//      Enumeration<Object> keys = System.getProperties().keys();
//      while (keys.hasMoreElements()) {
//         Object key = keys.nextElement();
//         sb.append(key).
//            append("=").
//            append(System.getProperties().get(key)).
//            append("\r\n");
//      }
//      return sb.toString();
//   }

   private Handlers handlers;
   /** all Action handlers */
   private Handlers getHandlers() {
      if (handlers == null)
         handlers = new Handlers();
      return handlers;
   }

   /** В обработчиках минимум логики. Вся логика в соотв Main.this.ZZZ функциях... */
   class Handlers {
      private Action gameNewAction;
      public Action getGameNewAction() {
         if (gameNewAction == null)
            gameNewAction = new AbstractAction() {
               private static final long serialVersionUID = 1L;
               @Override
               public void actionPerformed(ActionEvent e) {
                  Main.this.gameNew();
               }
            };

         return gameNewAction;
      }

      private Map<ESkillLevel, Action> skillLevelActions;
      public Action getSkillLevelAction(ESkillLevel key) {
         if (skillLevelActions == null) {
            skillLevelActions = new HashMap<>(ESkillLevel.values().length);

            for (final ESkillLevel val: ESkillLevel.values())
               skillLevelActions.put(val, new AbstractAction() {
                  private static final long serialVersionUID = 1L;
                  @Override
                  public void actionPerformed(ActionEvent e) {
                     Main.this.changeGame(val);
                  }
               });
         }

         return skillLevelActions.get(key);
      }

      private Action playerManageAction;
      public Action getPlayerManageAction() {
         if (playerManageAction == null)
            playerManageAction = new AbstractAction() {
               private static final long serialVersionUID = 1L;
               @Override
               public void actionPerformed(ActionEvent e) {
                  Main.this.getPlayerManageDlg().setVisible(
                        !Main.this.getPlayerManageDlg().isVisible());
               }
            };

         return playerManageAction;
      }

      private Action gameExitAction;
      public Action getGameExitAction() {
         if (gameExitAction == null)
            gameExitAction = new AbstractAction() {
               private static final long serialVersionUID = 1L;
               @Override
               public void actionPerformed(ActionEvent e) {
                  Main.this.onClose();
               }
            };

         return gameExitAction;
      }

      private Action pauseAction;
      /** Action на нажатие кнопки/клавиши паузы */
      public Action getPauseAction() {
         if (pauseAction == null)
            pauseAction = new AbstractAction() {
               private static final long serialVersionUID = 1L;
               @Override
               public void actionPerformed(ActionEvent e) {
                  if (Main.this.isUsePause())
                     Main.this.changePause();
               }
            };
         return pauseAction;
      }

      private Action minimizedAction;
      public Action getMinimizedAction() {
         if (minimizedAction == null)
            minimizedAction = new AbstractAction() {
               private static final long serialVersionUID = 1L;
               @Override
               public void actionPerformed(ActionEvent e) {
                  Main.this.iconify();
               }
            };

         return minimizedAction;
      }

      private Action centerScreenAction;
      public Action getCenterScreenAction() {
         if (centerScreenAction == null)
            centerScreenAction = new AbstractAction() {
               private static final long serialVersionUID = 1L;
               @Override
               public void actionPerformed(ActionEvent e) {
                  Main.this.toCenterScreen();
               }
            };

         return centerScreenAction;
      }

      private Map<EShowElement, Action> showElementsAction;
      public Action getShowElementAction(EShowElement key) {
         if (showElementsAction == null) {
            showElementsAction = new HashMap<>(EShowElement.values().length);

            for (final EShowElement val: EShowElement.values())
               showElementsAction.put(val, new AbstractAction() {
                  private static final long serialVersionUID = 1L;
                  @Override
                  public void actionPerformed(ActionEvent e) {
                     Main.this.optionsShowElement(val, e);
                  }
               });
         }
         return showElementsAction.get(key);
      }

      private Action themeDefaultAction;
      public Action getThemeDefaultAction() {
         if (themeDefaultAction == null)
            themeDefaultAction = new AbstractAction() {
               private static final long serialVersionUID = 1L;
               @Override
               public void actionPerformed(ActionEvent e) {
                  optionsThemeDefault();
               }
            };

         return themeDefaultAction;
      }

      private Action themeSystemAction;
      public Action getThemeSystemAction() {
         if (themeSystemAction == null)
            themeSystemAction = new AbstractAction() {
               private static final long serialVersionUID = 1L;
               @Override
               public void actionPerformed(ActionEvent e) {
                  optionsThemeSystem();
               }
            };

         return themeSystemAction;
      }

      private Consumer<ClickResult> mosaicClickHandler;
      public Consumer<ClickResult> getMosaicClickHandler() {
         if (mosaicClickHandler == null)
            mosaicClickHandler = (clickResult) -> {
               //System.out.println("OnMosaicClick: down=" + clickResult.isDown() + "; leftClick=" + clickResult.isLeft());
               if (clickResult.isLeft() && (Main.this.getMosaic().getGameStatus() == EGameStatus.eGSPlay)) {
                  Icon img = Main.this.getToolbar().getSmileIco(
                        clickResult.isDown() ?
                           EBtnNewGameState.eNormalMosaic :
                           EBtnNewGameState.eNormal);
                  if (img != null)
                     Main.this.getToolbar().getBtnNew().setIcon(img);
               }
            };

         return mosaicClickHandler;
      }

      private MouseListener pausePanelMouseListener;
      public MouseListener getPausePanelMouseListener() {
         if (pausePanelMouseListener == null)
            pausePanelMouseListener = new MouseAdapter() {
               @Override
               public void mousePressed(MouseEvent e) {
                  if (SwingUtilities.isRightMouseButton(e) && Main.this.isPaused())
                     Main.this.changePause();
               }
            };
         return pausePanelMouseListener;
      }

      private WindowListener windowListener;
      public WindowListener getWindowListener() {
         if (windowListener == null)
            windowListener = new WindowAdapter() {
               @Override
               public void windowClosing(WindowEvent we) {
                  Main.this.onClose();
               }
            };
         return windowListener;
      }

      private WindowFocusListener windowFocusListener;
      public WindowFocusListener getWindowFocusListener() {
         if (windowFocusListener == null)
            windowFocusListener = new WindowFocusListener() {
               @Override
               public void windowLostFocus(WindowEvent e) {
                  if (!Main.this.isPaused()) {
                     if (Main.this.isUsePause())
                        Main.this.changePause();

                     Icon img = Main.this.getToolbar().getSmileIco(EBtnNewGameState.eNormal);
                     if (img != null)
                        Main.this.getToolbar().getBtnNew().setIcon(img);
                  }
//                  getRootPane().requestFocusInWindow();
               }
               @Override
               public void windowGainedFocus(WindowEvent e) {}
            };
         return windowFocusListener;
      }

      private MouseWheelListener mouseWheelListener;
      public MouseWheelListener getMouseWheelListener() {
         if (mouseWheelListener == null)
            mouseWheelListener = evt -> {
//                  System.out.println("FMG::mouseWheelMoved: " + evt);
               if (!Main.this.getMenu().getOptions().getZoomItem(EZoomInterface.eAlwaysMax).isSelected())
                  switch (evt.getWheelRotation()) {
                  case  1: Main.this.areaDec(); break;
                  case -1: Main.this.areaInc(); break;
                  }
            };

         return mouseWheelListener;
      }

      private Map<EZoomInterface, Action> zoomActions;
      public Action getZoomAction(EZoomInterface key) {
         if (zoomActions == null) {
            zoomActions = new HashMap<>(EZoomInterface.values().length);

            for (final EZoomInterface val: EZoomInterface.values())
               zoomActions.put(val, new AbstractAction() {
                  private static final long serialVersionUID = 1L;
                  @Override
                  public void actionPerformed(ActionEvent e) {
                     boolean alwaysMax = Main.this.getMenu().getOptions().getZoomItem(EZoomInterface.eAlwaysMax).isSelected();
                     switch (val) {
                     case eAlwaysMax:           Main.this.areaAlwaysMax(e); break;
                     case eMax: if (!alwaysMax) Main.this.areaMax(); break;
                     case eMin: if (!alwaysMax) Main.this.areaMin(); break;
                     case eInc: if (!alwaysMax) Main.this.areaInc(); break;
                     case eDec: if (!alwaysMax) Main.this.areaDec(); break;
                     }
                  }
               });
         }
         return zoomActions.get(key);
      }

      private Action championsAction;
      public Action getChampionsAction() {
         if (championsAction == null)
            championsAction = new AbstractAction() {
               private static final long serialVersionUID = 1L;
               @Override
               public void actionPerformed(ActionEvent e) {
                  if (Main.this.getChampionDialog().isVisible())
                     Main.this.getChampionDialog().setVisible(false);
                  else {
                     Main.this.getChampionDialog().showData(
                           Main.this.getSkillLevel(),
                           Main.this.getMosaic().getMosaicType());
                  }
               }
            };

         return championsAction;
      }

      private Action statisticsAction;
      public Action getStatisticsAction() {
         if (statisticsAction == null)
            statisticsAction = new AbstractAction() {
               private static final long serialVersionUID = 1L;
               @Override
               public void actionPerformed(ActionEvent e) {
                  if (Main.this.getStatisticDialog().isVisible())
                     Main.this.getStatisticDialog().setVisible(false);
                  else
                     Main.this.getStatisticDialog().showData(Main.this.getSkillLevel(), Main.this.getMosaic().getMosaicType());
               }
            };

         return statisticsAction;
      }

      private Action aboutAction;
      public Action getAboutAction() {
         if (aboutAction == null)
            aboutAction = new AbstractAction() {
               private static final long serialVersionUID = 1L;
               @Override
               public void actionPerformed(ActionEvent e) {
                  Main.this.getAboutDialog().setVisible(!Main.this.getAboutDialog().isVisible());
               }
            };

         return aboutAction;
      }

      private Map<EMosaicGroup, Action> selectMosaicActions;
      public Action getSelectMosaicAction(final EMosaicGroup key) {
         if (selectMosaicActions == null) {
            selectMosaicActions = new HashMap<>(EMosaicGroup.values().length);

            for (final EMosaicGroup val: EMosaicGroup.values())
               selectMosaicActions.put(val, new AbstractAction() {
                  private static final long serialVersionUID = 1L;
                  @Override
                  public void actionPerformed(ActionEvent e) {
                     Main.this.getSelectMosaicDialog().startSelect(val);
                  }
               });
         }

         return selectMosaicActions.get(key);
      }

      private Action useUnknownAction;
      public Action getUseUnknownAction() {
         if (useUnknownAction == null)
            useUnknownAction = new AbstractAction() {
               private static final long serialVersionUID = 1L;
               @Override
               public void actionPerformed(ActionEvent e) {
                  Main.this.optionsUseUnknown(e);
               }
            };

         return useUnknownAction;
      }
      private Action usePauseAction;
      public Action getUsePauseAction() {
         if (usePauseAction == null)
            usePauseAction = new AbstractAction() {
               private static final long serialVersionUID = 1L;
               @Override
               public void actionPerformed(ActionEvent e) {
                  Main.this.optionsUsePause(e);
               }
            };

         return usePauseAction;
      }

      private Action timePlayAction;
      public Action getTimePlayAction() {
         if (timePlayAction == null)
            timePlayAction = new AbstractAction() {
               private static final long serialVersionUID = 1L;
               @Override
               public void actionPerformed(ActionEvent e) {
                  try {
                     int val = Integer.parseInt(getToolbar().getEdtTimePlay().getText());
                     getToolbar().getEdtTimePlay().setText(Integer.toString(++val));
                  } catch (Exception ex) {
                     System.err.println(ex);
                  }
               }
            };

         return timePlayAction;
      }

      private Action mosaicSizeIncX, mosaicSizeDecX, mosaicSizeIncY, mosaicSizeDecY;
      public Action getMosaicSizeIncX() {
         if (mosaicSizeIncX == null)
            mosaicSizeIncX = new AbstractAction() {
               private static final long serialVersionUID = 1L;
               @Override
               public void actionPerformed(ActionEvent e) {
                  Matrisize size = Main.this.getMosaic().getSizeField();
                  size.m++;
                  Main.this.changeGame(size);
               }
            };

         return mosaicSizeIncX;
      }
      public Action getMosaicSizeDecX() {
         if (mosaicSizeDecX == null)
            mosaicSizeDecX = new AbstractAction() {
               private static final long serialVersionUID = 1L;
               @Override
               public void actionPerformed(ActionEvent e) {
                  Matrisize size = Main.this.getMosaic().getSizeField();
                  size.m--;
                  Main.this.changeGame(size);
               }
            };

         return mosaicSizeDecX;
      }
      public Action getMosaicSizeIncY() {
         if (mosaicSizeIncY == null)
            mosaicSizeIncY = new AbstractAction() {
               private static final long serialVersionUID = 1L;
               @Override
               public void actionPerformed(ActionEvent e) {
                  Matrisize size = Main.this.getMosaic().getSizeField();
                  size.n++;
                  Main.this.changeGame(size);
               }
            };

         return mosaicSizeIncY;
      }
      public Action getMosaicSizeDecY() {
         if (mosaicSizeDecY == null)
            mosaicSizeDecY = new AbstractAction() {
               private static final long serialVersionUID = 1L;
               @Override
               public void actionPerformed(ActionEvent e) {
                  Matrisize size = Main.this.getMosaic().getSizeField();
                  size.n--;
                  Main.this.changeGame(size);
               }
            };

         return mosaicSizeDecY;
      }
   }

   public static void Beep() {
      java.awt.Toolkit.getDefaultToolkit().beep();
      //ASCII value 7 is a beep. So just print that character
   }

   static void printSystemProperties() {
      Properties props = System.getProperties();
      for (Object key : props.keySet()) {
         Object val = props.get(key);
         System.out.println(key+"="+val);
      }
   }
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


//   @Override
//   protected void processKeyEvent(KeyEvent e) {
//      System.out.println(e);
//      super.processKeyEvent(e);
//   }
//
//   @Override
//   protected void processEvent(AWTEvent e) {
//      System.out.println(e);
//      super.processEvent(e);
//   }

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

   public PlayersModel getPlayers() {
      if (players == null) {
         players = new PlayersModel(Main.serialVersionUID);
         players.Load();
      }
      return players;
   }
   public ChampionsModel getChampions() {
      if (champions == null) {
         champions = new ChampionsModel(Main.serialVersionUID, getPlayers());
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
   private List<ActionToUser> oneTimeSelectActiveUserActions = new ArrayList<ActionToUser>();
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
      MosaicBase mosaic = (MosaicBase)ev.getSource();
      if (mosaic.getGameStatus() != EGameStatus.eGSEnd)
         throw new RuntimeException("Invalid method state call");

      // сохраняю все нужные данные
      final boolean victory = mosaic.isVictory();
      if (!victory && (getActiveUserId() == null))
         return; // не напрягаю игрока окном выбора пользователя, пока он не выиграет разок...

      final ESkillLevel eSkill = Main.this.getSkillLevel();
      if (eSkill == ESkillLevel.eCustom)
         return;

      final EMosaic eMosaic = mosaic.getMosaicType();
      final long realCountOpen = mosaic.isVictory() ? mosaic.getMinesCount() : mosaic.getCountOpen();
      final long playTime = Long.parseLong(Main.this.getToolbar().getEdtTimePlay().getText());
      final long clickCount = mosaic.getCountClick();

      // логика сохранения...
      ActionToUser onActionToUser = userId -> {
         if (userId != null) {
            // ...статистики
            getPlayers().setStatistic(userId, eMosaic, eSkill, victory, realCountOpen, playTime, clickCount);
            if (getStatisticDialog().isVisible())
               // если окно открыто - сфокусируюсь на нужной закладке/скилле и пользователе
               getStatisticDialog().showData(eSkill, eMosaic);

            // ...чемпиона
            if (victory) {
               User user = Main.this.getPlayers().getUser(userId);
               int pos = Main.this.getChampions().add(user, playTime, eMosaic, eSkill);
               if (pos != -1)
                  Main.this.getChampionDialog().showData(eSkill, eMosaic, pos);
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

   @Override
   public void propertyChange(PropertyChangeEvent ev) {
//      System.out.println("Main::propertyChange: eventName=" + ev.getSource().getClass().getSimpleName() + "." + ev.getPropertyName());
      if (ev.getSource() instanceof Mosaic)
         onMosaicPropertyChanged((Mosaic)ev.getSource(), ev);
   }
   private void onMosaicPropertyChanged(Mosaic source, PropertyChangeEvent ev) {
      switch (ev.getPropertyName()) {
      case MosaicBase.PROPERTY_AREA:
      case MosaicBase.PROPERTY_SIZE_FIELD:
      case MosaicBase.PROPERTY_MOSAIC_TYPE:
         recheckLocation();
       //break; // no break
      case MosaicBase.PROPERTY_MINES_COUNT:
         getMenu().getMosaics().recheckSelectedMosaicType();
         getMenu().getGame().recheckSelectedSkillLevel();
         break;
      }

      switch (ev.getPropertyName()) {
      case MosaicBase.PROPERTY_GAME_STATUS:
         {
            getToolbar().getBtnPause().setEnabled(getMosaic().getGameStatus() == EGameStatus.eGSPlay);
          //System.out.println("OnChangeGameStatus: " + e.getSource().getGameStatus());
            switch ((EGameStatus)ev.getNewValue()) {
            case eGSCreateGame:
            case eGSReady:
               {
                  getTimerGame().stop();
                  getToolbar().getEdtTimePlay().setText("0");
                  Icon img = getToolbar().getSmileIco(EBtnNewGameState.eNormal);
                  if (img != null)
                     getToolbar().getBtnNew().setIcon(img);
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
                        getMosaic().isVictory() ?
                           EBtnNewGameState.eNormalWin :
                           EBtnNewGameState.eNormalLoss);
                  if (img != null)
                     getToolbar().getBtnNew().setIcon(img);

                  if (getSkillLevel() != ESkillLevel.eCustom)
                     // сохраняю статистику и чемпиона
                     setStatisticAndChampion(ev);
               }
               break;
            }
         }
         break;
      //case Mosaic.PROPERTY_SIZE_FIELD:
      //   break;
      //case Mosaic.PROPERTY_MINES_COUNT:
      //   break;
      //case Mosaic.PROPERTY_COUNT_FLAG:
      //   break;
      //case Mosaic.PROPERTY_COUNT_OPEN:
      //   break;
      case MosaicBase.PROPERTY_COUNT_MINES_LEFT:
         getToolbar().getEdtMinesLeft().setText(Integer.toString(getMosaic().getCountMinesLeft()));
         break;
      case MosaicBase.PROPERTY_COUNT_CLICK:
         getStatusBar().setClickCount(getMosaic().getCountClick());
         break;
      }
   }


   void invokeLater(Runnable doRun) {
      if (!_initialized)
         doRun.run();
      else
         SwingUtilities.invokeLater(doRun);
   }

}

/*

@Override
protected boolean checkNeedRestoreLastGame() {
   int iRes = JOptionPane.showOptionDialog(getContainer(), "Restore last game?", "Question", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
   return (iRes == JOptionPane.NO_OPTION);
}

*/