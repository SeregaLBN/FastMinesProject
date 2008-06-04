package ksn.fm.windows;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.JButton;


public class FMMainWindow extends JFrame {
	private static final long serialVersionUID = -1;

	private JMenuBar jMenu = null;
	private JMenu menuGame = null;
	private JMenuItem menuItemGameNew = null;
	private JMenuItem menuItemGameExit = null;
	private JMenu menuHelp = null;
	private JMenuItem menuItemAbout = null;

    private JPanel jContentPane = null;

	private JToolBar jToolBar = null;
	private JPanel jToolbarPane = null;

	private JTextField txtCountMines = null;
	private JToggleButton btnNewGame = null;
	private JToggleButton btnPause = null;
	private JTextField txtTimer = null;

	private CMosaic mosaicPanel = null;
	private JDialog dlgAbout = null;  //  @jve:decl-index=0:visual-constraint="750,94"
	private JPanel contentPaneAbout = null;
	private JToggleButton btnAboutImg = null;
	private JLabel lblAbout = null;
	private JButton bntAboutOk = null;

	/**
	 * This method initializes 
	 * 
	 */
	public FMMainWindow() {
		super();
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() {
        this.setSize(new Dimension(320, 290));
        this.setContentPane(getJContentPane());
        this.setTitle("jFastMines");
        this.setJMenuBar(getMenu());
			
	}

	/**
	 * This method initializes jMenu	
	 * 	
	 * @return javax.swing.JMenuBar	
	 */
	private JMenuBar getMenu() {
		if (jMenu == null) {
			jMenu = new JMenuBar();
			jMenu.add(getMenuGame());
			jMenu.add(getMenuHelp());
		}
		return jMenu;
	}

	/**
	 * This method initializes menuGame	
	 * 	
	 * @return javax.swing.JMenu	
	 */
	private JMenu getMenuGame() {
		if (menuGame == null) {
			menuGame = new JMenu();
			menuGame.setText("Game");
			menuGame.add(getMenuItemGameNew());
			menuGame.addSeparator();
			menuGame.add(getMenuItemGameExit());
		}
		return menuGame;
	}

	/**
	 * This method initializes jMenuItem_GameNew	
	 * 	
	 * @return javax.swing.JMenu	
	 */
	private JMenuItem getMenuItemGameNew() {
		if (menuItemGameNew == null) {
			menuItemGameNew = new JMenuItem();
			menuItemGameNew.setText("New game");
		}
		return menuItemGameNew;
	}

	/**
	 * This method initializes menuItemGameExit	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */
	private JMenuItem getMenuItemGameExit() {
		if (menuItemGameExit == null) {
			menuItemGameExit = new JMenuItem();
			menuItemGameExit.setText("Exit");
		}
		return menuItemGameExit;
	}

    /**
     * This method initializes jContentPane
     */
    private JPanel getJContentPane()
    {
        //Основной контейнер
        if (jContentPane == null)
        {
            jContentPane = new JPanel();
            jContentPane.setLayout(new BorderLayout());
            jContentPane.add(getJToolBar(), BorderLayout.NORTH);
            jContentPane.add(getMosaicPanel(), BorderLayout.CENTER);
        }
        return jContentPane;
    }

	/**
	 * This method initializes jToolBar	
	 * 	
	 * @return javax.swing.JToolBar	
	 */
	private JToolBar getJToolBar() {
		if (jToolBar == null) {
			jToolBar = new JToolBar();
			jToolBar.setFloatable(false);
			jToolBar.add(getJToolbarPane());
		}
		return jToolBar;
	}

	/**
	 * This method initializes txtCountMines	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getTxtCountMines() {
		if (txtCountMines == null) {
			txtCountMines = new JTextField();
			txtCountMines.setText("0 min.");
			txtCountMines.setEditable(false);
		}
		return txtCountMines;
	}

	/**
	 * This method initializes btnNewGame	
	 * 	
	 * @return javax.swing.JToggleButton	
	 */
	private JToggleButton getBtnNewGame() {
		if (btnNewGame == null) {
			btnNewGame = new JToggleButton();
//			btnNewGame.setText("N");
			btnNewGame.setToolTipText("New game");
			btnNewGame.setIcon(new ImageIcon("res/new0.gif"));
		}
		return btnNewGame;
	}

	/**
	 * This method initializes btnPause	
	 * 	
	 * @return javax.swing.JToggleButton	
	 */
	private JToggleButton getBtnPause() {
		if (btnPause == null) {
			btnPause = new JToggleButton();
//			btnPause.setText("P");
			btnPause.setToolTipText("Pause");
			btnPause.setIcon(new ImageIcon("res/pause2.gif"));
//			btnPause.setDisabledIcon(new ImageIcon("res/pause2.gif"));
//			btnPause.setDisabledSelectedIcon(new ImageIcon("res/pause2.gif"));
//			btnPause.setPressedIcon(new ImageIcon("res/pause0.gif"));
//			btnPause.setRolloverIcon(new ImageIcon("res/pause1.gif"));
//			btnPause.setRolloverSelectedIcon(new ImageIcon("res/pause1.gif"));
//			btnPause.setSelectedIcon(new ImageIcon("res/pause3.gif"));
		}
		return btnPause;
	}

	/**
	 * This method initializes txtTimer	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getTxtTimer() {
		if (txtTimer == null) {
			txtTimer = new JTextField();
			txtTimer.setText("0 sec.");
			txtTimer.setEditable(false);
		}
		return txtTimer;
	}

	/**
	 * This method initializes jToolbarPane	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJToolbarPane() {
		if (jToolbarPane == null) {
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
			gridBagConstraints1.fill = GridBagConstraints.VERTICAL;
			gridBagConstraints1.anchor = GridBagConstraints.WEST;
			gridBagConstraints1.gridwidth = 1;
			gridBagConstraints1.ipadx = 4;
			gridBagConstraints1.weightx = 1.0;
			gridBagConstraints2.fill = GridBagConstraints.BOTH;
			gridBagConstraints3.fill = GridBagConstraints.VERTICAL;
			gridBagConstraints4.fill = GridBagConstraints.VERTICAL;
			gridBagConstraints4.anchor = GridBagConstraints.EAST;
			gridBagConstraints4.ipadx = 4;
			gridBagConstraints4.weightx = 1.0;
			jToolbarPane = new JPanel();
			jToolbarPane.setLayout(new GridBagLayout());
			jToolbarPane.add(getTxtCountMines(), gridBagConstraints1);
			jToolbarPane.add(getBtnNewGame(), gridBagConstraints2);
			jToolbarPane.add(getBtnPause(), gridBagConstraints3);
			jToolbarPane.add(getTxtTimer(), gridBagConstraints4);
		}
		return jToolbarPane;
	}

	/**
	 * This method initializes mosaicPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getMosaicPanel() {
		if (mosaicPanel == null) {
			mosaicPanel = new CMosaic();
			mosaicPanel.setLayout(null);
		}
		return mosaicPanel;
	}

	/**
	 * This method initializes dlgAbout	
	 * 	
	 * @return javax.swing.JDialog	
	 */
	private JDialog getDlgAbout() {
		if (dlgAbout == null) {
			dlgAbout = new JDialog(this);
			dlgAbout.setSize(new Dimension(268, 157));
			dlgAbout.setTitle("About");
			dlgAbout.setContentPane(getContentPaneAbout());
			dlgAbout.setResizable(false);
		}
		return dlgAbout;
	}

	/**
	 * This method initializes contentPaneAbout	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getContentPaneAbout() {
		if (contentPaneAbout == null) {
			lblAbout = new JLabel();
			lblAbout.setText("jFastMines ver. 0.0.0.1");
			lblAbout.setBounds(new Rectangle(110, 22, 125, 16));
			contentPaneAbout = new JPanel();
			contentPaneAbout.setLayout(null);
			contentPaneAbout.add(getBtnAboutImg(), null);
			contentPaneAbout.add(lblAbout, null);
			contentPaneAbout.add(getBntAboutOk(), null);
		}
		return contentPaneAbout;
	}

	/**
	 * This method initializes btnAboutImg	
	 * 	
	 * @return javax.swing.JToggleButton	
	 */
	private JToggleButton getBtnAboutImg() {
		if (btnAboutImg == null) {
			btnAboutImg = new JToggleButton();
			btnAboutImg.setBounds(new Rectangle(18, 30, 62, 54));
			Icon ico = new ImageIcon("res/MinesWeeper.gif");
			btnAboutImg.setIcon(ico);
		}
		return btnAboutImg;
	}

	/**
	 * This method initializes bntAboutOk	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getBntAboutOk() {
		if (bntAboutOk == null) {
			bntAboutOk = new JButton();
			bntAboutOk.setText("Ok");
			bntAboutOk.setBounds(new Rectangle(123, 80, 75, 27));
			bntAboutOk.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					dlgAbout.setVisible(false);
				}
			});
		}
		return bntAboutOk;
	}

	/**
	 * This method initializes menuHelp	
	 * 	
	 * @return javax.swing.JMenu	
	 */
	private JMenu getMenuHelp() {
		if (menuHelp == null) {
			menuHelp = new JMenu();
			menuHelp.setText("Help");
			menuHelp.add(getMenuItemAbout());
		}
		return menuHelp;
	}

	/**
	 * This method initializes menuItemAbout	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */
	private JMenuItem getMenuItemAbout() {
		if (menuItemAbout == null) {
			menuItemAbout = new JMenuItem();
			menuItemAbout.setText("About");
			menuItemAbout.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					getDlgAbout().setVisible(!getDlgAbout().isVisible());
				}
			});
		}
		return menuItemAbout;
	}
}  //  @jve:decl-index=0:visual-constraint="212,51"
