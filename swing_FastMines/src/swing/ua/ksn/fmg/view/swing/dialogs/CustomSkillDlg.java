package ua.ksn.fmg.view.swing.dialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ua.ksn.fmg.controller.Mosaic;
import ua.ksn.fmg.controller.event.MosaicEvent;
import ua.ksn.fmg.controller.event.MosaicListener;
import ua.ksn.fmg.controller.types.ESkillLevel;
import ua.ksn.fmg.model.mosaics.CellFactory;
import ua.ksn.fmg.model.mosaics.cell.BaseCell;
import ua.ksn.fmg.view.swing.Main;
import ua.ksn.geom.Size;
import ua.ksn.swing.utils.GuiTools;

public class CustomSkillDlg extends JDialog {
	private static final long serialVersionUID = 1L;

	private JSpinner spinX, spinY, spinMines;
	private JButton btnPopup;
	private JButton btnCancel, btnOk;
	private JRadioButton radioFullScreenCurrSizeArea, radioFullScreenMiniSizeArea;
	private ButtonGroup radioGroup;
	private JPopupMenu popupMenu;
	private Main parent;

	public CustomSkillDlg(JFrame parent, boolean modal) {
		super(parent, "Select mosaic", modal);
		if (parent instanceof Main)
			this.parent = (Main) parent;
		initialize(parent);
	}

	private void initialize(JFrame parent) {
		Object keyBind = "OnOk";
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false), keyBind);
        getRootPane().getActionMap().put(keyBind, new AbstractAction() {
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) { OnOk(); }
		});

        keyBind = "CloseDialog";
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false), keyBind);
        getRootPane().getActionMap().put(keyBind, new AbstractAction() {
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) { OnClose(); }
		});

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) { OnClose(); }
		});

		this.setResizable(false);
		CreateComponents();
		// задаю предпочтительный размер
		pack();
		this.setLocationRelativeTo(parent);
	}

	// создаю панели с нужным расположением
	private void CreateComponents() {
		// 1. Создаю панель, которая будет содержать все остальные элементы и панели расположения
		Box boxCenter = Box.createHorizontalBox();
		Box boxBottom = Box.createHorizontalBox();
		// Чтобы интерфейс отвечал требованиям Java, необходимо отделить его содержимое от границ окна на 12 пикселов. 
		// использую пустую рамку 
		boxBottom.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
//		boxCenter.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
		boxCenter.setBorder(
				new javax.swing.border.CompoundBorder(
						BorderFactory.createEmptyBorder(12,12,0,12),
						new javax.swing.border.CompoundBorder(
								BorderFactory.createEtchedBorder(),
								BorderFactory.createEmptyBorder(0,5,5,5))));

		JLabel lblX = new JLabel("Width ");
		JLabel lblY = new JLabel("Height ");
		JLabel lblMines = new JLabel("Mines ");

		// spin
		spinX = new JSpinner();
		spinY = new JSpinner();

		spinMines = new JSpinner();
		spinMines.setToolTipText("Mines count");

		// отслеживаю изменения
		ChangeListener changeSizeListener = new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) { OnChangeSizeField(); }
		};
		spinX.addChangeListener(changeSizeListener);
		spinY.addChangeListener(changeSizeListener);

//		// отслеживаю изменения также и в редакторе
//		((JSpinner.DefaultEditor)spinX    .getEditor()).getTextField().getDocument().addDocumentListener(new SpinNumberDocListener(spinX    ));
//		((JSpinner.DefaultEditor)spinY    .getEditor()).getTextField().getDocument().addDocumentListener(new SpinNumberDocListener(spinY    ));
//		((JSpinner.DefaultEditor)spinMines.getEditor()).getTextField().getDocument().addDocumentListener(new SpinNumberDocListener(spinMines));

		btnPopup = new JButton();
		btnPopup.setText("\u25BC"); // http://www.fileformat.info/info/unicode/char/25bc/index.htm
		Insets margin = btnPopup.getMargin();
		margin.left = margin.right = 0; margin.top = margin.bottom = 0;
		btnPopup.setMargin(margin);
		btnPopup.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) { OnPopup(); }
		});

		btnOk = new JButton();
		btnOk.setText("Ok");
		margin = btnOk.getMargin();
		margin.left = margin.right = 5; margin.top = margin.bottom = 2;
		btnOk.setMargin(margin);
		btnOk.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) { OnOk(); }
		});

		btnCancel = new JButton();
		btnCancel.setText("Cancel");
		margin = btnCancel.getMargin();
		margin.left = margin.right = 5; margin.top = margin.bottom = 2;
		btnCancel.setMargin(margin);
		btnCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) { OnClose(); }
		});


		JPanel panel4Radio = new JPanel(new GridLayout(0, 1, 0, 5));
		panel4Radio.setBorder(BorderFactory.createTitledBorder("Full screen"));
		radioGroup = new ButtonGroup();
		radioFullScreenCurrSizeArea = new JRadioButton("Current cell area");
		radioFullScreenMiniSizeArea = new JRadioButton("Minimal cell area");
		panel4Radio.add(radioFullScreenCurrSizeArea);
		panel4Radio.add(radioFullScreenMiniSizeArea);
		radioGroup.add(radioFullScreenCurrSizeArea);
		radioGroup.add(radioFullScreenMiniSizeArea);
		radioFullScreenCurrSizeArea.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				OnFullScreenCurrArea();
			}
		});
		radioFullScreenMiniSizeArea.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				OnFullScreenMiniArea();
			}
		});

		JPanel panel4Left = new JPanel(new GridLayout(0, 1, 0, 5));
		panel4Left.setBorder(BorderFactory.createEmptyBorder(7,0,2,0));
		Box boxLine = Box.createHorizontalBox();
		//Component rigid1 = Box.createRigidArea(new Dimension(1,1));
		boxLine.add(lblX); boxLine.add(Box.createHorizontalStrut(2)); boxLine.add(spinX); //boxLine.add(rigid1);
		panel4Left.add(boxLine);
		boxLine = Box.createHorizontalBox();
		//Component rigid2 = Box.createRigidArea(new Dimension(1,1));
		boxLine.add(lblY); boxLine.add(Box.createHorizontalStrut(2)); boxLine.add(spinY); //boxLine.add(rigid2);
		panel4Left.add(boxLine);
		boxLine = Box.createHorizontalBox();
		boxLine.add(lblMines); boxLine.add(Box.createHorizontalStrut(2)); boxLine.add(spinMines); boxLine.add(btnPopup);
		panel4Left.add(boxLine);
		GuiTools.makeSameWidth(new Component[] {lblX, lblY, lblMines});

		Dimension prefSize;
		prefSize = spinX.getPreferredSize(); prefSize.width += 20; spinX.setPreferredSize(prefSize);
		prefSize = spinY.getPreferredSize(); prefSize.width += 20; spinY.setPreferredSize(prefSize);
		prefSize = spinMines.getPreferredSize(); prefSize.width += 10; spinMines.setPreferredSize(prefSize);
		GuiTools.makeSameWidth(new Component[] {spinX, spinY, spinMines});
		//GuiTools.makeSameWidth(new Component[] {rigid1, rigid2, btnPopup});

		boxBottom.add(btnCancel);
		boxBottom.add(Box.createHorizontalGlue());
		boxBottom.add(btnOk);
		GuiTools.makeSameWidth(new JComponent[] {btnCancel, btnOk});

		boxCenter.add(panel4Left);
		boxCenter.add(Box.createHorizontalStrut(5)); 
		boxCenter.add(panel4Radio);

		// добавляю расположение в центр окна и внизу
		getContentPane().add(boxCenter, BorderLayout.CENTER);
		getContentPane().add(boxBottom, BorderLayout.SOUTH);

		if (parent != null)
			parent.getMosaic().addMosaicListener(new MosaicListener() {
				@Override
				public void OnClick(MosaicEvent.ClickEvent e) {}
				@Override
				public void OnChangeMosaicType(MosaicEvent.ChangeMosaicTypeEvent e) { if (CustomSkillDlg.this.isVisible()) CustomSkillDlg.this.OnChangeMosaicType(); }
				@Override
				public void OnChangeGameStatus(MosaicEvent.ChangeGameStatusEvent e) {}
				@Override
				public void OnChangeCounters(MosaicEvent.ChangeCountersEvent e) {}
				@Override
				public void OnChangeArea(MosaicEvent.ChangeAreaEvent e) { if (radioFullScreenCurrSizeArea.isSelected()) radioGroup.clearSelection(); }
			});
	}

	private void OnChangeSizeField() {
//		System.out.println("OnChangeSizeField");
		int max = (Integer)spinX.getValue() * (Integer)spinY.getValue() - getNeighborNumber();
		((SpinnerNumberModel )spinMines.getModel()).setMaximum(max);
		if ((Integer)spinMines.getValue() > max)
			spinMines.setValue(max);

		radioGroup.clearSelection();
	}

	private void OnChangeMosaicType() {
		RecalcModelValueXY(false, false);
		RecalcModelValueMines();
		radioGroup.clearSelection();
	}

	private void OnPopup() {
//		System.out.println("CustomSkill::OnPopup: ");
		if (popupMenu == null) {
			popupMenu = new JPopupMenu();
			for (final ESkillLevel val: ESkillLevel.values()) {
				if (val == ESkillLevel.eCustom)
					continue;
	
				JMenuItem menuItem = new JMenuItem();
	
				menuItem.setText(val.getDescription());
					menuItem.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						OnPopupSetSize(val);
					}
				});
				popupMenu.add(menuItem);
			}
		}
		Rectangle rc = btnPopup.getBounds();
		popupMenu.show(this, rc.x, rc.y + rc.height);
	}

	private void OnOk() {
//		System.out.println("OnOk");

		if (parent != null) {
			final int x = (Integer)spinX.getValue();
			final int y = (Integer)spinY.getValue();
			final int m = (Integer)spinMines.getValue();
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					parent.SetGame(new Size(x,y), m);
				}
			});
		}

		OnClose();
	}
	private void OnClose() {
		// при выходе из диалогового окна - освобождаю ресурсы
		dispose();
//		System.exit(0);
	}

	// тестовый метод для проверки диалогового окна
	public static void main(String[] args) {
		CustomSkillDlg sm = new CustomSkillDlg(null, true);
		sm.setVisible(true);
	}

	private int getNeighborNumber() {
		if (parent == null)
			return 21;
		final int area = 200; // пох
		BaseCell.BaseAttribute attr = CellFactory.createAttributeInstance(parent.getMosaic().getMosaicType(), area);
		return attr.getNeighborNumber() + 1; // +thisCell
	}

	private void RecalcModelValueXY(boolean isFullScreen, boolean isFullScreenAtCurrArea) {
		int currSizeX, currSizeY, miniSizeX, miniSizeY, maxiSizeX, maxiSizeY;
		if (parent == null) {
			currSizeX = currSizeY = 10;
			miniSizeX = miniSizeY = 5;
			maxiSizeX = maxiSizeY = 50;
		} else {
			miniSizeX = miniSizeY = 5; 

			Size s = parent.CalcMaxMosaicSize(Mosaic.AREA_MINIMUM);
			maxiSizeX = s.width; maxiSizeY = s.height;

			if (isFullScreen) {
				if (isFullScreenAtCurrArea)
					s = parent.CalcMaxMosaicSize(parent.getMosaic().getArea());
			} else
				s = parent.getMosaic().getSizeField();
			currSizeX = s.width; currSizeY = s.height;
		}
//		// recheck
//		if (currSizeX < miniSizeX) currSizeX = miniSizeX;
//		if (currSizeY < miniSizeY) currSizeY = miniSizeY;
//		if (currSizeX > maxiSizeX) currSizeX = maxiSizeX;
//		if (currSizeY > maxiSizeY) currSizeY = maxiSizeY;

		spinX.setModel(new SpinnerNumberModel(currSizeX, miniSizeX, maxiSizeX, 1));
		spinY.setModel(new SpinnerNumberModel(currSizeY, miniSizeY, maxiSizeY, 1));
	}

	private void RecalcModelValueMines() {
		int minesCurr = (parent == null) ? 15 : parent.getMosaic().getMinesCount();
		int minesMin = 1;
		int minesMax = (Integer)spinX.getValue() * (Integer)spinY.getValue() - getNeighborNumber();
//		// recheck
//		if (minesCurr < minesMin) minesCurr = minesMin;
//		if (minesCurr > minesMax) minesCurr = minesMax;

		spinMines.setModel(new SpinnerNumberModel(minesCurr, minesMin, minesMax, 1));
	}

	@Override
	public void setVisible(boolean b) {
//		System.out.println("setVisible: " + b);
		if (b) {
			RecalcModelValueXY(false, false);
			RecalcModelValueMines();
			radioGroup.clearSelection();
		}
		super.setVisible(b);
	}

	private void OnFullScreenCurrArea() {
//		System.out.println("OnFullScreenCurrArea");
		RecalcModelValueXY(true, true);
	}
	private void OnFullScreenMiniArea() {
//		System.out.println("OnFullScreenMiniArea");
		RecalcModelValueXY(true, false);
	}

	private void OnPopupSetSize(ESkillLevel eSkill) {
		if (parent == null)
			return;
		Size size = new Size((Integer)spinX.getValue(), (Integer)spinY.getValue());
		int mines = eSkill.GetNumberMines(parent.getMosaic().getMosaicType(), size);
		spinMines.setValue(mines);
	}
}