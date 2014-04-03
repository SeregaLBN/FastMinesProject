package ua.ksn.fmg.view.swing;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ComponentInputMap;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import ua.ksn.Pair;
import ua.ksn.fmg.controller.Mosaic;
import ua.ksn.fmg.controller.event.ActionToUser;
import ua.ksn.fmg.controller.event.MosaicEvent;
import ua.ksn.fmg.controller.event.MosaicListener;
import ua.ksn.fmg.controller.serializable.ChampionsModel;
import ua.ksn.fmg.controller.serializable.PlayersModel;
import ua.ksn.fmg.controller.swing.MosaicExt;
import ua.ksn.fmg.controller.swing.serializable.SerializeProjData;
import ua.ksn.fmg.controller.types.EGameStatus;
import ua.ksn.fmg.controller.types.ESkillLevel;
import ua.ksn.fmg.controller.types.User;
import ua.ksn.fmg.model.mosaics.EMosaic;
import ua.ksn.fmg.model.mosaics.EMosaicGroup;
import ua.ksn.fmg.view.draw.EShowElement;
import ua.ksn.fmg.view.draw.EZoomInterface;
import ua.ksn.fmg.view.swing.dialogs.AboutDlg;
import ua.ksn.fmg.view.swing.dialogs.ChampionDlg;
import ua.ksn.fmg.view.swing.dialogs.CustomSkillDlg;
import ua.ksn.fmg.view.swing.dialogs.ManageDlg;
import ua.ksn.fmg.view.swing.dialogs.SelectMosaicDlg;
import ua.ksn.fmg.view.swing.dialogs.StatisticDlg;
import ua.ksn.fmg.view.swing.draw.GraphicContext;
import ua.ksn.fmg.view.swing.res.Resources;
import ua.ksn.fmg.view.swing.res.Resources.EBtnNewGameState;
import ua.ksn.fmg.view.swing.res.Resources.EBtnPauseState;
import ua.ksn.geom.Rect;
import ua.ksn.geom.Size;
import ua.ksn.swing.geom.Cast;
import ua.ksn.swing.utils.GuiTools;
import ua.ksn.swing.utils.ImgUtils;

/** Главное окно программы */
public class Main extends JFrame  {
	public static final long serialVersionUID = 7923652871481566227L;

	private JPanel     contentPane;
	private MainMenu   menu;
	private Toolbar    toolbar;
	private MosaicExt  mosaic;
	private PausePanel pausePanel;
	private StatusBar  statusBar;

	private Resources resources;
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
	private SelectMosaicDlg getSelectMosaicDialog() {
		if (_selectMosaicDialog == null)
			_selectMosaicDialog = new SelectMosaicDlg(this, false, getResources());
		return _selectMosaicDialog;
	}
	private AboutDlg getAboutDialog() {
		if (_aboutDialog == null)
			_aboutDialog = new AboutDlg(this, false, getResources());
		return _aboutDialog;
	}
	private ChampionDlg getChampionDialog() {
		if (_championDialog == null)
			_championDialog = new ChampionDlg(this, false, getResources(), getChampions());
		return _championDialog;
	}
	private StatisticDlg getStatisticDialog() {
		if (_statisticDialog == null)
			_statisticDialog = new StatisticDlg(this, false, getResources(), getPlayers());
		return _statisticDialog;
	}

	class MainMenu extends JMenuBar {
		private static final long serialVersionUID = 1L;

		class Game extends JMenu {
			private static final long serialVersionUID = 1L;

			private JMenuItem anew;
			private Map<ESkillLevel, JRadioButtonMenuItem> skillLevel;
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
//				this.add(getMosaics());
//				this.add(new JSeparator());

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
					skillLevel = new HashMap<ESkillLevel, JRadioButtonMenuItem>(ESkillLevel.values().length);

					for (ESkillLevel val: ESkillLevel.values()) {
						JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem();

						switch (val) {
						case eCustom: menuItem.setText(val.getDescription() + "..."); break;
						default     : menuItem.setText(val.getDescription()); break;
						}

						menuItem.setMnemonic(Main.KeyCombo.getMnemonic_SkillLevel(val));
						menuItem.setAccelerator(Main.KeyCombo.getKeyStroke_SkillLevel(val));
						menuItem.addActionListener(Main.this.getHandlers().getSkillLevelAction(val));

						skillLevel.put(val, menuItem);
					}
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
		}
		class Mosaics extends JMenu {
			private static final long serialVersionUID = 1L;

			private Map<EMosaicGroup, JMenuItem> mosaicsGroup;
			private Map<EMosaic, JRadioButtonMenuItem> mosaics;

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

			private static final int icoMosaicWidht = 16, icoMosaicHeight = 16;
			final static boolean experimentalMenuMnemonic = true;
			private JMenuItem getMenuItemMosaicGroup(EMosaicGroup key) {
				if (mosaicsGroup == null) {
					mosaicsGroup = new HashMap<EMosaicGroup, JMenuItem>(EMosaicGroup.values().length);

					for (EMosaicGroup val: EMosaicGroup.values()) {
						JMenu menuItem = new JMenu(val.getDescription());// + (experimentalMenuMnemonic ?  "                      " : ""));
						for (EMosaic mosaic: val.getBind())
							menuItem.add(getMenuItemMosaic(mosaic));
//						menuItem.setMnemonic(Main.KeyCombo.getMnemonic_MenuMosaicGroup(val));
						menuItem.setIcon(Main.this.getResources().getImgMosaicGroup(val, icoMosaicWidht, icoMosaicHeight));

//						if (experimentalMenuMnemonic) {
//							menuItem.setLayout(new FlowLayout(FlowLayout.RIGHT));
//							menuItem.add(new JLabel("Num+111111111111"));// + (char)(Main.KeyCombo.getMnemonic_MenuMosaicGroup(val))));
//						}

						mosaicsGroup.put(val, menuItem);
					}
				}
				return mosaicsGroup.get(key);
			}

			private JRadioButtonMenuItem getMenuItemMosaic(EMosaic key) {
				if (mosaics == null) {
					mosaics = new HashMap<EMosaic, JRadioButtonMenuItem>(EMosaic.values().length);

					for (EMosaic val: EMosaic.values()) {
						String menuItemTxt = val.getDescription(false);
						if (experimentalMenuMnemonic)
							menuItemTxt += "                      ";
						JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem(menuItemTxt);
						menuItem.setMnemonic(Main.KeyCombo.getMnemonic_Mosaic(val));
						menuItem.setAccelerator(Main.KeyCombo.getKeyStroke_Mosaic(val));
						menuItem.addActionListener(Main.this.getHandlers().getMosaicAction(val));
						menuItem.setIcon(Main.this.getResources().getImgMosaic(val, true, icoMosaicWidht, icoMosaicHeight));

						if (experimentalMenuMnemonic) {
							menuItem.setLayout(new FlowLayout(FlowLayout.RIGHT));
							menuItem.add(new JLabel("NumPad " + val.getFastCode()));
						}

						mosaics.put(val, menuItem);
					}
				}
				return mosaics.get(key);
			}
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
					zoomItems = new HashMap<EZoomInterface, JMenuItem>(EZoomInterface.values().length);

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
					showElements = new HashMap<EShowElement, JCheckBoxMenuItem>(EShowElement.values().length);

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

//			this.setToolTipText("main menu");

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
			dim.width = getMosaic().getContainer().getPreferredSize().width;
			return dim;
		}
	}
	class Toolbar extends JPanel {
		private static final long serialVersionUID = 1L;

		private JTextField edtMinesLeft, edtTimePlay;
		private BtnNew btnNew;
		private BtnPause btnPause;

		class BtnNew extends JButton {
			private static final long serialVersionUID = 1L;
		
			public BtnNew() {
				super();
				initialize();
			}
			private void initialize() {
				this.setAction(Main.this.getHandlers().getGameNewAction());
				this.setFocusable(false);

				if (getResources().getImgBtnNew(EBtnNewGameState.eNormal) == null) {
					this.setText("N");
				} else {
					this.setIcon(getResources().getImgBtnNew(EBtnNewGameState.eNormal));
					this.setPressedIcon(getResources().getImgBtnNew(EBtnNewGameState.ePressed));
					this.setSelectedIcon(getResources().getImgBtnNew(EBtnNewGameState.eSelected));
					this.setRolloverIcon(getResources().getImgBtnNew(EBtnNewGameState.eRollover));
					this.setRolloverSelectedIcon(getResources().getImgBtnNew(EBtnNewGameState.eRolloverSelected));
					this.setRolloverEnabled(true);
					this.setDisabledIcon(getResources().getImgBtnNew(EBtnNewGameState.eDisabled));
					this.setDisabledSelectedIcon(getResources().getImgBtnNew(EBtnNewGameState.eDisabledSelected));
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

				if (getResources().getImgBtnPause(EBtnPauseState.eNormal) == null) {
					this.setText("P");
				} else {
					this.setIcon(getResources().getImgBtnPause(EBtnPauseState.eNormal));
					this.setPressedIcon(getResources().getImgBtnPause(EBtnPauseState.ePressed));
					this.setSelectedIcon(getResources().getImgBtnPause(EBtnPauseState.eSelected));
					this.setRolloverIcon(getResources().getImgBtnPause(EBtnPauseState.eRollover));
					this.setRolloverSelectedIcon(getResources().getImgBtnPause(EBtnPauseState.eRolloverSelected));
					this.setRolloverEnabled(true);
					this.setDisabledIcon(getResources().getImgBtnPause(EBtnPauseState.eDisabled));
					this.setDisabledSelectedIcon(getResources().getImgBtnPause(EBtnPauseState.eDisabledSelected));
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
//				edtMinesLeft.setBorder(BorderFactory.createLoweredBevelBorder());
//				edtMinesLeft.setBorder(BorderFactory.createEtchedBorder());
				edtMinesLeft.setFocusable(false);
				edtMinesLeft.setEditable(false);
				edtMinesLeft.setToolTipText("Mines left");
			}
			return edtMinesLeft;
		}
		private JTextField getEdtTimePlay() {
			if (edtTimePlay == null) {
				edtTimePlay = new JTextField("TimePlay");
//				edtTimePlay.setBorder(BorderFactory.createLoweredBevelBorder());
//				edtTimePlay.setBorder(BorderFactory.createEtchedBorder());
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
//				getEdtTimePlay().setMinimumSize(dimEdt);
				getEdtTimePlay().setMaximumSize(dimEdt);
				getEdtMinesLeft().setPreferredSize(dimEdt);
//				getEdtMinesLeft().setMinimumSize(dimEdt);
				getEdtMinesLeft().setMaximumSize(dimEdt);
			}
			{
				this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

				this.setBorder(new CompoundBorder(BorderFactory.createRaisedBevelBorder(), new EmptyBorder(2, 2, 2, 2)));
//				this.setBorder(new CompoundBorder(BorderFactory.createEtchedBorder(), new EmptyBorder(2, 2, 2, 2)));
//				this.setBorder(new CompoundBorder(BorderFactory.createMatteBorder(3, 3, 3, 3, new Color(255,0,0, 220)), new EmptyBorder(2, 2, 2, 2)));

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
	class PausePanel extends JLabel {
		private static final long serialVersionUID = 1L;

		public PausePanel(String text) {
			super(text);
			this.setHorizontalAlignment(SwingConstants.CENTER);
			Font font = this.getFont();
			font = new Font(font.getName(), font.getStyle(), 45);
			this.setFont(font);
		}

		@Override
		protected void paintComponent(Graphics g) {
			ImageIcon img = Main.this.getResources().getImgPause();
			if (img == null) {
				super.paintComponent(g);
			} else {
				ImageIcon imgIco = getCachedImg(img);
				Dimension sizeOutward = this.getSize();
				imgIco.paintIcon(this, g,
						(sizeOutward.width -imgIco.getIconWidth())>>1,
						(sizeOutward.height-imgIco.getIconHeight())>>1);
			}
		}

		@Override
		public Dimension getPreferredSize() {
//			return super.getPreferredSize();
			return getMosaic().getContainer().getPreferredSize();
		}

		/** кэшированная картинка */
		private ImageIcon cachedImg;
		public ImageIcon getCachedImg(ImageIcon img) {
			if (cachedImg == null)
				cachedImg = img;

			Dimension sizeOutward = this.getSize();
			Dimension sizeInner = new Dimension(img.getIconWidth(), img.getIconHeight());

			Rect newRect = Rect.CalcInnerRect(Cast.toSize(sizeInner), Cast.toSize(sizeOutward));
			if ((cachedImg.getIconHeight() != newRect.height) ||
				(cachedImg.getIconWidth() != newRect.width))
				cachedImg = ImgUtils.zoom(img, newRect.width, newRect.height);

			return cachedImg;
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
//					BorderFactory.createLoweredBevelBorder()
					BorderFactory.createEmptyBorder()
					);
			contentPane.setLayout(new BorderLayout());
			contentPane.add(getToolbar(), BorderLayout.NORTH);
			contentPane.add(centerPanel, BorderLayout.CENTER);
			contentPane.add(getStatusBar(), BorderLayout.SOUTH);

//			centerPanel.setBorder(BorderFactory.createRaisedBevelBorder());

			LayoutManager lm = centerPanel.getLayout();
			if (lm instanceof FlowLayout) {
				FlowLayout fl = (FlowLayout) lm;
				fl.setHgap(0);
				fl.setVgap(0);
			} else
				centerPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0,0));

			centerPanel.add(getMosaic().getContainer());
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
	/** мозаика */
	public MosaicExt getMosaic() {
		if (mosaic == null)
			mosaic = new MosaicExt();
		return mosaic;
	}
	private PausePanel getPausePanel() {
		if (pausePanel == null) {
			pausePanel = new PausePanel("Pause");
			pausePanel.setBorder(BorderFactory.createRaisedBevelBorder());
//			pausePanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
//			pausePanel.setFocusable(true);
			pausePanel.setVisible(false);
			
			pausePanel.addMouseListener(this.getHandlers().getPausePanelMouseListener());
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
		new Main().setVisible(true);
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
//		Dimension sizeScreen = Toolkit.getDefaultToolkit().getScreenSize();
//		Dimension sizeWin = this.getSize();
//		setLocation((sizeScreen.width - sizeWin.width) / 2, (sizeScreen.height - sizeWin.height) / 2);
		this.setLocationRelativeTo(null);
	}

	private void initialize() {
//		System.out.println(getProperties());

		{
			ToolTipManager ttm = ToolTipManager.sharedInstance();
			ttm.setInitialDelay(ttm.getInitialDelay() + 3000);
		}

//		iconify();
		this.setResizable(false);

		boolean isZoomAlwaysMax;
		final Point startLocation = new Point();
		boolean defaultData = false;
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

			getMosaic().setParams(spm.getSizeField(), spm.getMosaicType(), spm.getMinesCount());
			getMosaic().setArea(spm.getArea());

			setActiveUserId(spm.getActiveUserId());
			getPlayerManageDlg().setDoNotAskStartupChecked(spm.isDoNotAskStartup());
			if (!spm.isDoNotAskStartup())
				SwingUtilities.invokeLater( new Runnable() {
					@Override
					public void run() {
						getHandlers().getPlayerManageAction().actionPerformed(new ActionEvent(Main.this, 0, "Main::initialize"));
					}
				});

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
			ApplyInputActionMenuMap(spm.getShowElement(EShowElement.eMenu));
			getToolbar().  setVisible(spm.getShowElement(EShowElement.eToolbar));
			getStatusBar().setVisible(spm.getShowElement(EShowElement.eStatusbar));

			startLocation.x = spm.getLocation().x;
			startLocation.y = spm.getLocation().y;
		}

		this.setContentPane(getContentPane());

		this.setJMenuBar(getMenu());
		this.setTitle("FastMines");
		this.setIconImage(getResources().getImgLogo());

		getMosaic().addMosaicListener(this.getHandlers().getMosaicListener());
//		this.getHandlers().getMosaicListener().OnChangeArea(new MosaicEvent(getMosaic())); // TODO: это нужно только тогда, когда нет десериализации
		getToolbar().getEdtMinesLeft().setText(Integer.toString(getMosaic().getCountMinesLeft()));
		getToolbar().getEdtTimePlay().setText("0");

		setLocationRelativeTo(null);
		//setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				Main.this.OnClose();
			}
		});
//		this.addKeyListener(new KeyListener() {
//			@Override public void keyTyped   (KeyEvent e) { System.out.println("Main::KeyListener:keyTyped: "    + e); }
//			@Override public void keyReleased(KeyEvent e) { System.out.println("Main::KeyListener:keyReleased: " + e); }
//			@Override public void keyPressed (KeyEvent e) { System.out.println("Main::KeyListener:keyPressed: "  + e); }
//		});
		this.addWindowFocusListener(this.getHandlers().getWindowFocusListener());
		this.addMouseWheelListener(this.getHandlers().getMouseWheelListener());
//		this.addWindowListener(new WindowAdapter() {
//			
//			@Override
//			public void windowActivated(WindowEvent e) {
////				if (Main.this.isAlwaysOnTopSupported())
//					try {
//						System.out.println("windowActivated");
//						Main.this.setAlwaysOnTop(true);
//					} catch (Exception ex) {
//						ex.printStackTrace();
//					}
//				super.windowActivated(e);
//			}
//			@Override
//			public void windowDeactivated(WindowEvent e) {
////				System.out.println("windowDeactivated: " + e.getSource());
//				if (Main.this.isAlwaysOnTopSupported())
//					try {
//						System.out.println("windowDeactivated");
//						Main.this.setAlwaysOnTop(false);
//					} catch (Exception ex) {
//						ex.printStackTrace();
//					}
//				super.windowDeactivated(e);
//			}
//		});

		RecheckSelectedMenuMosaicType();
		RecheckSelectedMenuSkillLevel();
		ChangeSizeImagesMineFlag();
		CustomKeyBinding();

		//pack();
		RecheckLocation(!true, true);

		if (isZoomAlwaysMax)
			SwingUtilities.invokeLater(new Runnable() { @Override public void run() { Main.this.AreaAlwaysMax(new ActionEvent(Main.this, 0, null)); } });
		if (!defaultData)
			SwingUtilities.invokeLater(new Runnable() { @Override public void run() { Main.this.setLocation(startLocation);  RecheckLocation(true, !true); } });
	}

	/** Выставить верный bullet для меню мозаики */
	void RecheckSelectedMenuMosaicType() {
		getMenu().getMosaics().getMenuItemMosaic(getMosaic().getMosaicType()).setSelected(true);
	}

	/** Выставить верный bullet (menu.setSelected) для меню skillLevel'a */
	void RecheckSelectedMenuSkillLevel() {
		getMenu().getGame().getMenuItemSkillLevel(getSkillLevel()).setSelected(true);
	}

	ESkillLevel getSkillLevel() {
		EMosaic eMosaic = getMosaic().getMosaicType();
		Size sizeFld    = getMosaic().getSizeField();
		int numberMines = getMosaic().getMinesCount();

		if (sizeFld.equals(ESkillLevel.eBeginner.DefaultSize()) && (numberMines == ESkillLevel.eBeginner.GetNumberMines(eMosaic)))
			return ESkillLevel.eBeginner;
		else
		if (sizeFld.equals(ESkillLevel.eAmateur.DefaultSize()) && (numberMines == ESkillLevel.eAmateur.GetNumberMines(eMosaic)))
			return ESkillLevel.eAmateur;
		else
		if (sizeFld.equals(ESkillLevel.eProfi.DefaultSize()) && (numberMines == ESkillLevel.eProfi.GetNumberMines(eMosaic)))
			return ESkillLevel.eProfi;
		else
		if (sizeFld.equals(ESkillLevel.eCrazy.DefaultSize()) && (numberMines == ESkillLevel.eCrazy.GetNumberMines(eMosaic)))
			return ESkillLevel.eCrazy;
		else
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
		public static final KeyStroke getKeyStroke_ThemeDefault   () { return KeyStroke.getKeyStroke(KeyEvent.VK_D, Event.CTRL_MASK, false); }
		public static final KeyStroke getKeyStroke_ThemeSystem    () { return KeyStroke.getKeyStroke(KeyEvent.VK_Y, Event.CTRL_MASK, false); }
		public static final KeyStroke getKeyStroke_UseUnknown     () { return null; }
		public static final KeyStroke getKeyStroke_UsePause       () { return null; }
		public static final KeyStroke getKeyStroke_ShowElements   (EShowElement key) {
//			switch (key) {
//			case eCaption  : return KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0, false);
//			case eMenu     : return KeyStroke.getKeyStroke(KeyEvent.VK_F10, 0, false);
//			case eToolbar  : return KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0, false);
//			case eStatusbar: return KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0, false);
//			default: return null; // throw new RuntimeException();
//			}
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
	void CustomKeyBinding() {
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

			inputMap.put(KeyCombo.getKeyStroke_About(), "About");
			actionMap.put("About", this.getHandlers().getAboutAction());

			inputMap.put(KeyCombo.getKeyStroke_Champions(), "Champions");
			actionMap.put("Champions", this.getHandlers().getChampionsAction());

			inputMap.put(KeyCombo.getKeyStroke_Statistics(), "Statistics");
			actionMap.put("Statistics", this.getHandlers().getStatisticsAction());

			inputMap.put(KeyCombo.getKeyStroke_NewGame(), "New game");
			actionMap.put("New game", this.getHandlers().getGameNewAction());

			for (ESkillLevel key: ESkillLevel.values()) {
				inputMap.put(KeyCombo.getKeyStroke_SkillLevel(key), key.getDescription());
				actionMap.put(key.getDescription(), this.getHandlers().getSkillLevelAction(key));
			}

			inputMap.put(KeyCombo.getKeyStroke_PlayerManage(), "Player manage");
			actionMap.put("Player manage", this.getHandlers().getPlayerManageAction());

			inputMap.put(KeyCombo.getKeyStroke_Exit(), "Exit");
			actionMap.put("Exit", this.getHandlers().getGameExitAction());

			for (EZoomInterface key: EZoomInterface.values()) {
				inputMap.put(KeyCombo.getKeyStroke_Zoom(key), key.getDescription());
				actionMap.put(key.getDescription(), this.getHandlers().getZoomAction(key));
			}

			inputMap.put(KeyCombo.getKeyStroke_ThemeDefault(), "Theme Default");
			actionMap.put("Theme Default", getHandlers().getThemeDefaultAction());
	
			inputMap.put(KeyCombo.getKeyStroke_ThemeSystem(), "Theme System");
			actionMap.put("Theme System", getHandlers().getThemeSystemAction());

			for (EShowElement key: EShowElement.values()) {
				inputMap.put(KeyCombo.getKeyStroke_ShowElements(key), key.getDescription());
				actionMap.put(key.getDescription(), this.getHandlers().getShowElementAction(key));
			}
		}
		return keyPairBindAsMenuAccelerator;
	}

	/** pause on/off */
	void ChangePause(AWTEvent e) {
		if (getMosaic().getGameStatus() != EGameStatus.eGSPlay)
			return;

//		System.out.println("> FMG::ChangePause: " + KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner() );

		boolean paused = isPaused();
		if (e.getSource() != getToolbar().getBtnPause())
			getToolbar().getBtnPause().setSelected(!paused);

		if (paused) {
			getTimerGame().restart();

			getPausePanel().setVisible(false);
			getMosaic().getContainer().setVisible(true);
			getMosaic().getContainer().requestFocusInWindow();
		} else {
			getTimerGame().stop();

			getMosaic().getContainer().setVisible(false);
			getPausePanel().setVisible(true);
			getRootPane().requestFocusInWindow(); // ! иначе на компонентах нат фокуса, и mouse wheel не пашет...
		}
//		System.out.println("< FMG::ChangePause: " + KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner() );
	}

	void GameNew(ActionEvent e) {
		if (isPaused())
			ChangePause(e);
		else
			getMosaic().GameNew();
	}
	void OnClose() {
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

		getStatisticDialog().CleanResource(); // без этого не завершиться java поцесс (под виндой глядел)
		getChampionDialog().CleanResource(); // без этого не завершиться java поцесс (под виндой глядел)

//		setVisible(false);
		dispose();
//		System.exit(0);
	}

	/** Попытаться установить новый размер на мозаику (при возможности, сохраняя ESkillLevel) */
	boolean setMosaicSizeField(Size newSize) {
		//System.out.println("setMosaicSizeField: newSize=" + newSize);

		// 1. Проверяю валидность нового размера
		if ((newSize.width < 1) || (newSize.height < 1)) {
			Beep();
			return false;
		}
		int area = CalcMaxArea(newSize);
		if (area <= Mosaic.AREA_MINIMUM) {
			Beep();
			return false;
		}

		// 2. Устанавливаю новый размер
		EMosaic mosaicType = getMosaic().getMosaicType();
		int numberMines;
		ESkillLevel skill = getSkillLevel();
		if (skill == ESkillLevel.eCustom)
			numberMines = getMosaic().getMinesCount();
		else
			numberMines = skill.GetNumberMines(mosaicType, newSize);

		getMosaic().setParams(newSize, mosaicType, numberMines);

		RecheckSelectedMenuSkillLevel();
		RecheckLocation(true, true);

		return true;
	}

	/** Поменять игру на новую мозаику */
	public void SetGame(EMosaic mosaicType, ActionEvent e) {
		if (isPaused())
			ChangePause(e);

		int numberMines;
		ESkillLevel skill = getSkillLevel();
		if (skill == ESkillLevel.eCustom)
			numberMines = getMosaic().getMinesCount();
		else
			numberMines = skill.GetNumberMines(mosaicType);

		getMosaic().setParams(null, mosaicType, numberMines);

		if (!isMenuEvent(e))
			RecheckSelectedMenuMosaicType();
		RecheckSelectedMenuSkillLevel();

		RecheckLocation(true, true);
	}

	/** Поменять игру на новый размер & кол-во мин */
	public void SetGame(Size sizeField, int numberMines) {
		if (isPaused())
			ChangePause(new AWTEvent(this, 0) { private static final long serialVersionUID = 1L; });

		getMosaic().setParams(sizeField, null, numberMines);

		RecheckSelectedMenuSkillLevel();

		RecheckLocation(true, true);
	}

	/** Поменять игру на новый уровень сложности */
	void SetGame(ESkillLevel skill, ActionEvent e) {
		if (isPaused())
			ChangePause(e);

		int numberMines;
		Size sizeFld;
		if (skill == ESkillLevel.eCustom) {
			//System.out.println("... dialog box 'Select custom skill level...' ");
			getCustomSkillDialog().setVisible(!getCustomSkillDialog().isVisible());
			return;
		} else {
			numberMines = skill.GetNumberMines(getMosaic().getMosaicType());
			sizeFld = skill.DefaultSize();
		}

		getMosaic().setParams(sizeFld, null, numberMines);

		if (getMenu().getOptions().getZoomItem(EZoomInterface.eAlwaysMax).isSelected()) {
			AreaMax();
			RecheckLocation(false, false);
		} else
			RecheckLocation(true, true);

		if (!isMenuEvent(e))
			RecheckSelectedMenuSkillLevel();
	}

	/** узнать размер окна проекта при указанном размере окна мозаики */
	Size CalcSize(Size sizeMosaicInPixel) {
		Dimension sizeWin = this.getSize();

		if ((sizeWin.height == 0) && (sizeWin.width == 0) && !this.isVisible()) {
			throw new RuntimeException("Invalid method call.  Нельзя высчитать размер окна, когда оно даже не выведено на экран...");
//			Dimension dummy = Toolkit.getDefaultToolkit().getScreenSize(); // заглушка
//			dummy.height++; dummy.width++;
//			return dummy;
		}

		Size currSizeMosaicInPixel = getMosaic().getWindowSize();
		return new Size(
				sizeWin.width  + (sizeMosaicInPixel.width  - currSizeMosaicInPixel.width),
				sizeWin.height + (sizeMosaicInPixel.height - currSizeMosaicInPixel.height));
	}

	/**
	 * Поиск больше-меньше
	 * @param baseMin - стартовое значение для поиска
	 * @param baseDelta - начало дельты приращения
	 * @param func - ф-ция сравнения
	 * @return что найдено
	 */
	static int Finder(int baseMin, int baseDelta, Comparable<Integer> func) {
		double res = baseMin;
		double d = baseDelta;
		boolean deltaUp = true, lastSmall = true;
		do {
			if (deltaUp)
				d *= 2;
			else
				d /= 2;

			if (lastSmall)
				res += d;
			else
				res -= d;

			int z = func.compareTo((int)res);
			if (z == 0)
				return (int)res;
			lastSmall = (z < 0);
			deltaUp = deltaUp && lastSmall;
		} while(d > 1);
		return (int)res;
	}

	/** узнаю мах размер площади ячеек мозаики, при котором окно проекта вмещается в текущее разрешение экрана
	 * @param mosaicSizeField - интересуемый размер поля мозаики
	 * @return макс площадь ячейки
	 */
	int CalcMaxArea(final Size mosaicSizeField) {
		final Size sizeScreen = Cast.toSize(Toolkit.getDefaultToolkit().getScreenSize());
		return Finder(Mosaic.AREA_MINIMUM, Mosaic.AREA_MINIMUM, new Comparable<Integer>() {
			@Override
			public int compareTo(Integer area) {
				Size sizeMosaic = getMosaic().CalcWindowSize(mosaicSizeField, area);
				Size sizeWnd = CalcSize(sizeMosaic);
				if ((sizeWnd.width == sizeScreen.width) &&
				   (sizeWnd.height == sizeScreen.height))
				  return 0;
				if ((sizeWnd.width <= sizeScreen.width) &&
					(sizeWnd.height <= sizeScreen.height))
					return -1;
				return +1;
			}
		});
	}

	/**
	 * узнаю max размер поля мозаики, при котором окно проекта вмещается в текущее разрешение экрана
	 * @param area - интересуемая площадь ячеек мозаики
	 * @return max размер поля мозаики
	 */
	public Size CalcMaxMosaicSize(final int area) {
		final Size sizeScreen = Cast.toSize(Toolkit.getDefaultToolkit().getScreenSize());
		final Size result = new Size();
		Finder(1, 10, new Comparable<Integer>() {
			@Override
			public int compareTo(Integer newWidth) {
				result.width = newWidth;
				Size sizeMosaic = getMosaic().CalcWindowSize(result, area);
				Size sizeWnd = CalcSize(sizeMosaic);
				if (sizeWnd.width == sizeScreen.width)
					return 0;
				if (sizeWnd.width <= sizeScreen.width)
					return -1;
				return +1;
			}
		});
		Finder(1, 10, new Comparable<Integer>() {
			@Override
			public int compareTo(Integer newHeight) {
				result.height = newHeight;
				Size sizeMosaic = getMosaic().CalcWindowSize(result, area);
				Size sizeWnd = CalcSize(sizeMosaic);
				if (sizeWnd.width == sizeScreen.height)
					return 0;
				if (sizeWnd.height <= sizeScreen.height)
					return -1;
				return +1;
			}
		});
		return result;
	}

	/**
	 * проверить что находится в рамках экрана	
	 * @param checkArea - заодно проверить что влазит в текущее разрешение экрана
	 * @param pack - call this.pack();
	 */
	void RecheckLocation(boolean checkArea, boolean pack) {
		if (checkArea) {
			int maxArea = CalcMaxArea(getMosaic().getSizeField());
			if (maxArea < getMosaic().getArea())
				setArea(maxArea);
		}

		if (pack) {
			ua.ksn.geom.Point center = Rect.getCenter(Cast.toRect(this.getBounds()));
//			Point center = new Rect(this.getBounds()).center();
			pack();
//			Rectangle newBounds = new Rect(this.getBounds()).center(center);
			Rectangle newBounds = Cast.toRect(Rect.setCenter(Cast.toRect(this.getBounds()), center));
			this.setBounds(newBounds);
		}

		SwingUtilities.invokeLater(new Runnable() { // спецом для Ubuntu Gnome
			@Override
			public void run() {
				Dimension sizeScreen = Toolkit.getDefaultToolkit().getScreenSize();
				Rectangle rcThis = Main.this.getBounds();
				if ((rcThis.x<0) || (rcThis.y<0))
					Main.this.setLocation(Math.max(0, rcThis.x), Math.max(0, rcThis.y));
				else
				if (((rcThis.x+rcThis.width ) > sizeScreen.width) ||
					((rcThis.y+rcThis.height) > sizeScreen.height))
					Main.this.setLocation(
							Math.min(rcThis.x, sizeScreen.width  - rcThis.width),
							Math.min(rcThis.y, sizeScreen.height - rcThis.height));
			}
		});
	}
	
	/** getMosaic().setArea(...) */
	void setArea(int newArea) {
		newArea = Math.min(newArea, CalcMaxArea(getMosaic().getSizeField())); // recheck

		int curArea = getMosaic().getArea();
		if (curArea == newArea)
			return;

		getMosaic().setArea(newArea);

		RecheckLocation(false, true);
	}

	/** Zoom + */
	void AreaInc() {
		setArea((int) (getMosaic().getArea() * 1.05));
	}
	/** Zoom - */
	void AreaDec() {
		setArea((int) (getMosaic().getArea() * 0.95));
	}
	/** Zoom minimum */
	void AreaMin() {
		setArea(0);
	}
	/** Zoom maximum */
	void AreaMax() {
		int maxArea = CalcMaxArea(getMosaic().getSizeField());
		if (maxArea == getMosaic().getArea()) return;
		setArea(maxArea);

//		{
//			// Если до вызова AreaMax() меню окна распологалось в две строки, то после
//			// отработки этой ф-ции меню будет в одну строку, т.е., последующий вызов
//			// GetMaximalArea() будет возвращать ещё бОльший результат.
//			// Поэтому надо снова установить максимальное значение плошади ячеек.
//			if (maxArea < CalcMaxArea())
//				AreaMax(); // меню было в две  строки
//			else;          // меню было в одну строку
//		}
	}
	/** Zoom always maximum */
	public void AreaAlwaysMax(ActionEvent e) {
//		if (!isMenuEvent(e))
//			getMenu().getOptions().getZoomItem(EZoomInterface.eAlwaysMax).setSelected(!getMenu().getOptions().getZoomItem(EZoomInterface.eAlwaysMax).isSelected());

		boolean checked = getMenu().getOptions().getZoomItem(EZoomInterface.eAlwaysMax).isSelected();

		if (checked)
			AreaMax();

		for (EZoomInterface key: EZoomInterface.values())
			switch (key ) {
			case eAlwaysMax: break;
			default: getMenu().getOptions().getZoomItem(key).setEnabled(!checked);
			}
	}

	void OptionsThemeDefault() {
		try {
			UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
			SwingUtilities.updateComponentTreeUI(Main.this);
			Main.this.pack();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	void OptionsThemeSystem() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			SwingUtilities.updateComponentTreeUI(Main.this);
			Main.this.pack();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	void OptionsUseUnknown(ActionEvent e) {
		if (!isMenuEvent(e))
			getMenu().getOptions().getUseUnknown().setSelected(!getMenu().getOptions().getUseUnknown().isSelected());

		getMosaic().setUseUnknown(getMenu().getOptions().getUseUnknown().isSelected());
	}

	boolean isUsePause() {
		return getMenu().getOptions().getUsePause().isSelected();
	}
	void OptionsUsePause(ActionEvent e) {
		if (!isMenuEvent(e))
			getMenu().getOptions().getUsePause().setSelected(!getMenu().getOptions().getUsePause().isSelected());

		boolean usePause = isUsePause();
		this.getToolbar().getBtnPause().setVisible(usePause);
//		this.getToolbar().revalidate();

		if (!usePause && isPaused())
			ChangePause(e);
	}

	void OptionsShowElement(EShowElement key, final ActionEvent e) {
		if (!isMenuEvent(e))
			getMenu().getOptions().getShowElement(key).setSelected(!getMenu().getOptions().getShowElement(key).isSelected());

		switch (key) {
		case eCaption:
			{
				final Rectangle rc = getBounds();
				final Map<EShowElement, Boolean> mapShow = new HashMap<EShowElement, Boolean>(EShowElement.values().length);
				for (EShowElement val: EShowElement.values())
					mapShow.put(val, new Boolean(getMenu().getOptions().getShowElement(val).isSelected()));

				// вызов this.dispose(); приводит к потере фокуса, т.е, когда идёт игра, - к срабатыванию паузы
				// т.е. нужно позже снять паузу...
				final boolean isNotPaused = (getMosaic().getGameStatus() == EGameStatus.eGSPlay) && !isPaused();
//				if (this.isDisplayable())
					this.dispose();
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						Main.this.setUndecorated(!mapShow.get(EShowElement.eCaption).booleanValue());
						Main.this.setBounds(rc);
						Main.this.getMenu()     .setVisible(mapShow.get(EShowElement.eMenu).booleanValue());
						Main.this.getToolbar()  .setVisible(mapShow.get(EShowElement.eToolbar).booleanValue());
						Main.this.getStatusBar().setVisible(mapShow.get(EShowElement.eStatusbar).booleanValue());

						if (isNotPaused && isUsePause())
							Main.this.ChangePause(e);

						Main.this.setVisible(true);
						Main.this.pack();
					}
				});
			}
			break;
		case eMenu:
			{
				boolean show = getMenu().getOptions().getShowElement(key).isSelected();
				getMenu().setVisible(show);
				ApplyInputActionMenuMap(show);
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
	void ApplyInputActionMenuMap(boolean visibleMenu) {
		if (visibleMenu) {
			getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).setParent(null);
			getRootPane().getActionMap().setParent(null);
		} else {
			Pair<InputMap, ActionMap> bind = getKeyPairBindAsMenuAccelerator();
			getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).setParent(bind.getFirst());
			getRootPane().getActionMap().setParent(bind.getSecond());
		}
	}

//	@Override
//	protected void processKeyEvent(KeyEvent e) {
//		System.out.println(e);
//		super.processKeyEvent(e);
//	}

	boolean isPaused() {
		return !getMosaic().getContainer().isVisible();
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

//	private static String getProperties() {
//		StringBuffer sb = new StringBuffer();
//		Enumeration<Object> keys = System.getProperties().keys();
//		while (keys.hasMoreElements()) {
//			Object key = keys.nextElement();
//			sb.append(key).
//				append("=").
//				append(System.getProperties().get(key)).
//				append("\r\n");
//		}
//		return sb.toString();
//	}

	private Resources getResources() {
		if (resources == null)
			resources = new Resources();
		return resources;
	}

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
						Main.this.GameNew(e);
					}
				};

			return gameNewAction;
		}

		private Map<ESkillLevel, Action> skillLevelActions;
		public Action getSkillLevelAction(ESkillLevel key) {
			if (skillLevelActions == null) {
				skillLevelActions = new HashMap<ESkillLevel, Action>(ESkillLevel.values().length); 
	
				for (final ESkillLevel val: ESkillLevel.values())
					skillLevelActions.put(val, new AbstractAction() {
						private static final long serialVersionUID = 1L;
						@Override
						public void actionPerformed(ActionEvent e) {
							Main.this.SetGame(val, e);
						}
					});
			}

			return skillLevelActions.get(key);
		}

		private Map<EMosaic, Action> mosaicAction;
		public ActionListener getMosaicAction(EMosaic key) {
			if (mosaicAction == null) {
				mosaicAction = new HashMap<EMosaic, Action>(EMosaic.values().length);
	
				for (final EMosaic val: EMosaic.values())
					mosaicAction.put(val, new AbstractAction() {
						private static final long serialVersionUID = 1L;
						@Override
						public void actionPerformed(ActionEvent e) {
							Main.this.SetGame(val, e);
						}
					});
			}

			return mosaicAction.get(key);
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
						Main.this.OnClose();
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
							Main.this.ChangePause(e);
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
				showElementsAction = new HashMap<EShowElement, Action>(EShowElement.values().length);
	
				for (final EShowElement val: EShowElement.values())
					showElementsAction.put(val, new AbstractAction() {
						private static final long serialVersionUID = 1L;
						@Override
						public void actionPerformed(ActionEvent e) {
							Main.this.OptionsShowElement(val, e);
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
						OptionsThemeDefault();
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
						OptionsThemeSystem();
					}
				};

			return themeSystemAction;
		}

		private MosaicListener mosaicListener;
		public MosaicListener getMosaicListener() {
			if (mosaicListener == null)
				mosaicListener = new MosaicListener() {
					@Override
					public void OnClick(MosaicEvent.ClickEvent e) {
//						System.out.println("OnMosaicClick: down=" + e.isDown() + "; leftClick=" + e.isLeftClick());
						if (e.isLeftClick()) {
							Icon img = Main.this.getResources().getImgBtnNew(
									e.isDown() ?
										EBtnNewGameState.eNormalMosaic :
										EBtnNewGameState.eNormal);
							if (img != null)
								Main.this.getToolbar().getBtnNew().setIcon(img);
						}
					}
					
					@Override
					public void OnChangeGameStatus(MosaicEvent.ChangeGameStatusEvent e) {
						getToolbar().getBtnPause().setEnabled(getMosaic().getGameStatus() == EGameStatus.eGSPlay);
//						System.out.println("OnChangeGameStatus: " + e.getSource().getGameStatus());
						switch (e.getSource().getGameStatus()) {
						case eGSCreateGame:
						case eGSReady:
							{
								Main.this.getTimerGame().stop();
								Main.this.getToolbar().getEdtTimePlay().setText("0");
								Icon img = Main.this.getResources().getImgBtnNew(EBtnNewGameState.eNormal);
								if (img != null)
									Main.this.getToolbar().getBtnNew().setIcon(img);
							}
							break;
						case eGSPlay:
							{
								Main.this.getTimerGame().restart();
							}
							break;
						case eGSEnd:
							{
								Main.this.getTimerGame().stop();
								Icon img = Main.this.getResources().getImgBtnNew(
										e.getSource().isVictory() ?
											EBtnNewGameState.eNormalWin :
											EBtnNewGameState.eNormalLoss);
								if (img != null)
									Main.this.getToolbar().getBtnNew().setIcon(img);

								if (Main.this.getSkillLevel() != ESkillLevel.eCustom)
									// сохраняю статистику и чемпиона
									Main.this.setStatisticAndChampion(e);
							}
							break;
						}
					}
					
					@Override
					public void OnChangeCounters(MosaicEvent.ChangeCountersEvent e) {
						Main.this.getToolbar().getEdtMinesLeft().setText(
								Integer.toString(e.getSource().getCountMinesLeft()));
						Main.this.getStatusBar().setClickCount(e.getSource().getCountClick());
					}

					@Override
					public void OnChangeArea(MosaicEvent.ChangeAreaEvent e) {
						Main.this.ChangeSizeImagesMineFlag();
					}

					@Override
					public void OnChangeMosaicType(MosaicEvent.ChangeMosaicTypeEvent e) {
						((MosaicExt)e.getSource()).changeFontSize();
						Main.this.ChangeSizeImagesMineFlag();
					}
			};
	
			return mosaicListener;
		}
	
		private MouseListener pausePanelMouseListener;
		public MouseListener getPausePanelMouseListener() {
			if (pausePanelMouseListener == null)
				pausePanelMouseListener = new MouseAdapter() {
					@Override
					public void mousePressed(MouseEvent e) {
						if (SwingUtilities.isRightMouseButton(e) && Main.this.isPaused())
							Main.this.ChangePause(e);
					}
				};
			return pausePanelMouseListener;
		}
	
		private WindowFocusListener windowFocusListener;
		public WindowFocusListener getWindowFocusListener() {
			if (windowFocusListener == null)
				windowFocusListener = new WindowFocusListener() {
					@Override
					public void windowLostFocus(WindowEvent e) {
						if (!Main.this.isPaused()) {
							if (Main.this.isUsePause())
								Main.this.ChangePause(e);

							Icon img = Main.this.getResources().getImgBtnNew(EBtnNewGameState.eNormal);
							if (img != null)
								Main.this.getToolbar().getBtnNew().setIcon(img);
						}
//						getRootPane().requestFocusInWindow();
					}
					@Override
					public void windowGainedFocus(WindowEvent e) {}
				};
			return windowFocusListener;
		}	
	
		private MouseWheelListener mouseWheelListener;
		public MouseWheelListener getMouseWheelListener() {
			if (mouseWheelListener == null)
				mouseWheelListener = new MouseWheelListener() {
					@Override
					public void mouseWheelMoved(MouseWheelEvent evt) {
//						System.out.println("FMG::mouseWheelMoved: " + evt);
						if (!Main.this.getMenu().getOptions().getZoomItem(EZoomInterface.eAlwaysMax).isSelected())
							switch (evt.getWheelRotation()) {
							case  1: Main.this.AreaDec(); break;
							case -1: Main.this.AreaInc(); break;
							}
					}
				};
	
			return mouseWheelListener;
		}
	
		private Map<EZoomInterface, Action> zoomActions;
		public Action getZoomAction(EZoomInterface key) {
			if (zoomActions == null) {
				zoomActions = new HashMap<EZoomInterface, Action>(EZoomInterface.values().length);
	
				for (final EZoomInterface val: EZoomInterface.values())
					zoomActions.put(val, new AbstractAction() {
						private static final long serialVersionUID = 1L;
						@Override
						public void actionPerformed(ActionEvent e) {
							boolean alwaysMax = Main.this.getMenu().getOptions().getZoomItem(EZoomInterface.eAlwaysMax).isSelected();
							switch (val) {
							case eAlwaysMax:           Main.this.AreaAlwaysMax(e); break;
							case eMax: if (!alwaysMax) Main.this.AreaMax(); break;
							case eMin: if (!alwaysMax) Main.this.AreaMin(); break;
							case eInc: if (!alwaysMax) Main.this.AreaInc(); break;
							case eDec: if (!alwaysMax) Main.this.AreaDec(); break;
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
							Main.this.getChampionDialog().ShowData(
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
							Main.this.getStatisticDialog().ShowData(Main.this.getSkillLevel(), Main.this.getMosaic().getMosaicType());
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
				selectMosaicActions = new HashMap<EMosaicGroup, Action>(EMosaicGroup.values().length);

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
						Main.this.OptionsUseUnknown(e);
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
						Main.this.OptionsUsePause(e);
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
						Size size = Main.this.getMosaic().getSizeField();
						size.width++;
						Main.this.setMosaicSizeField(size);
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
						Size size = Main.this.getMosaic().getSizeField();
						size.width--;
						Main.this.setMosaicSizeField(size);
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
						Size size = Main.this.getMosaic().getSizeField();
						size.height++;
						Main.this.setMosaicSizeField(size);
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
						Size size = Main.this.getMosaic().getSizeField();
						size.height--;
						Main.this.setMosaicSizeField(size);
					}
				};
		
			return mosaicSizeDecY;
		}
	}

	/** переустанавливаю заного размер мины/флага для мозаики */
	private void ChangeSizeImagesMineFlag() {
		MosaicExt m = getMosaic();
		GraphicContext gc = m.getGraphicContext();
		int sq = (int)m.getCellAttr().CalcSq(m.getArea(), gc.getPenBorder().getWidth());
		if (sq <= 0) {
			System.err.println("Error: слишком толстое перо! Нет области для вывода картиники флага/мины...");
			sq = 3; // ат балды...
		}
		gc.setImgFlag(getResources().getImgFlag(sq, sq));
		gc.setImgMine(getResources().getImgMine(sq, sq));
	}

	public static void Beep() {
		java.awt.Toolkit.getDefaultToolkit().beep();
		//ASCII value 7 is a beep. So just print that character

		// http://www.jfugue.org
		// Fugue is an open-source Java API for programming music without the complexities of MIDI.
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


//	@Override
//	protected void processKeyEvent(KeyEvent e) {
//		System.out.println(e);
//		super.processKeyEvent(e);
//	}
//
//	@Override
//	protected void processEvent(AWTEvent e) {
//		System.out.println(e);
//		super.processEvent(e);
//	}

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
	public void setStatisticAndChampion(MosaicEvent.ChangeGameStatusEvent e) {
		Mosaic mosaic = e.getSource();
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
		ActionToUser onActionToUser = new ActionToUser() {
			@Override
			public void applyToUser(UUID userId) {
				if (userId != null) {
					// ...статистики
					getPlayers().setStatistic(userId, eMosaic, eSkill, victory, realCountOpen, playTime, clickCount);
					if (getStatisticDialog().isVisible())
						// если окно открыто - сфокусируюсь на нужной закладке/скилле и пользователе
						getStatisticDialog().ShowData(eSkill, eMosaic);

					// ...чемпиона
					if (victory) {
						User user = Main.this.getPlayers().getUser(userId);
						int pos = Main.this.getChampions().add(user, playTime, eMosaic, eSkill);
						if (pos != -1)
							Main.this.getChampionDialog().ShowData(eSkill, eMosaic, pos);
					}
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
}
