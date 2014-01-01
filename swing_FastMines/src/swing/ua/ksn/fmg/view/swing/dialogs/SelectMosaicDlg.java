package ua.ksn.fmg.view.swing.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;

import ua.ksn.fmg.model.mosaics.EMosaic;
import ua.ksn.fmg.model.mosaics.EMosaicGroup;
import ua.ksn.fmg.view.swing.Main;
import ua.ksn.fmg.view.swing.res.Resources;
import ua.ksn.swing.model.SpinNumberDocListener;
import ua.ksn.swing.model.SpinnerDiapasonModel;
import ua.ksn.swing.utils.GuiTools;

public class SelectMosaicDlg extends JDialog {
	private static final long serialVersionUID = 1L;

	private JSpinner spin;
	private JComboBox<?> cmbxMosaicTypes;
	private JButton btnOk;
	private Main parent;

	private Resources resources;
	private Resources getResources() {
		if (resources == null)
			resources = new Resources();
		return resources;
	}

	public SelectMosaicDlg(JFrame parent, boolean modal) {
		super(parent, "Select mosaic", modal);
		if (parent instanceof Main)
			this.parent = (Main) parent;
		initialize(parent);
	}
	public SelectMosaicDlg(JFrame parent, boolean modal, Resources resources) {
		super(parent, "Select mosaic", modal);
		if (parent instanceof Main)
			this.parent = (Main) parent;
		this.resources = resources;
		initialize(parent);
	}

	private void initialize(JFrame parent) {
		Object keyBind = "OnOk";
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false), keyBind);
        getRootPane().getActionMap().put(keyBind, new AbstractAction() {
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) { SelectMosaicDlg.this.OnOk(); }
		});

        keyBind = "CloseDialog";
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false), keyBind);
        getRootPane().getActionMap().put(keyBind, new AbstractAction() {
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) { SelectMosaicDlg.this.OnClose(); }
		});

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) { SelectMosaicDlg.this.OnClose(); }
		});

		this.setResizable(false);
		CreateComponents();
		// ����� ���������������� ������
		pack();
		this.setLocationRelativeTo(parent);
	}

	public void startSelect(EMosaicGroup initMosaicGroup) {
		String txt = (initMosaicGroup.ordinal()+3) + "0";
		spin.setValue(Integer.parseInt(txt));

//		spin.getEditor().requestFocusInWindow(); 
//		spin.requestFocusInWindow();

//		SwingUtilities.invokeLater(new Runnable() {
//			@Override
//			public void run() {
//				System.out.println(spin.getValue());
				JTextField txtField = ((JSpinner.DefaultEditor)spin.getEditor()).getTextField();
				txtField.setText(spin.getValue().toString()); // TODO ��... ����� select �� ������� :(
//				System.out.println(txtField.getText());
				txtField.select(1, 2);

//				System.out.println(KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner());
//				System.out.println(SelectMosaicDlg.this.getFocusOwner());
//				System.out.println(spin.hasFocus());
//				System.out.println(spin.getEditor().hasFocus());
//			}
//		});

		this.setVisible(true);
	}

	// ������ ������ � ������ �������������
	private void CreateComponents() {
		// 1. ������ ������, ������� ����� ��������� ��� ��������� �������� � ������ ������������
		Box boxCenter = Box.createVerticalBox();
		// ����� ��������� ������� ����������� Java, ���������� �������� ��� ���������� �� ������ ���� �� 12 ��������. 
		// ��������� ������ ����� 
		boxCenter.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
//		boxCenter.setBorder(
//				new javax.swing.border.CompoundBorder(
//						BorderFactory.createEmptyBorder(12,12,12,12),
//						new javax.swing.border.CompoundBorder(
//								BorderFactory.createEtchedBorder(),
//								BorderFactory.createEmptyBorder(12,12,12,12))));

		JLabel lbl1 = new JLabel("Number:");

		// spin
		spin = new JSpinner(new SpinnerDiapasonModel( EMosaic.getFastCodeValues() ));
		spin.setToolTipText("Fast code mosaic");

		// ���������� ��������� � ���������, ...
		JTextField txtField = ((JSpinner.DefaultEditor)spin.getEditor()).getTextField();
//		txtField.getDocument().addDocumentListener(new DocumentListener() {
//			@Override
//			public void removeUpdate(DocumentEvent e) { OnChangeMosaicNumber(); }
//			@Override
//			public void insertUpdate(DocumentEvent e) { OnChangeMosaicNumber(); }
//			@Override
//			public void changedUpdate(DocumentEvent e) {}
//
//		});
		txtField.getDocument().addDocumentListener(new SpinNumberDocListener(spin) {
			@Override
			protected boolean OnChangeTextSpin(DocumentEvent e) {
				boolean res = super.OnChangeTextSpin(e);
				if (res)
					OnChangeMosaicNumber();
				return res;
			}
		});
		// ... � �� � ����� ������ spin'�
//		spin.addChangeListener(new ChangeListener() {
//			@Override
//			public void stateChanged(ChangeEvent e) { OnChangeMosaicNumber(); }
//		});

//		final Object keyBind = "Enter pressed in txt field";
//		txtField.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false), keyBind);
//		txtField.getActionMap().put(keyBind, new AbstractAction() {
//			private static final long serialVersionUID = 1L;
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				System.out.println(keyBind.toString());
//			}
//		});

		JLabel lbl2 = new JLabel("Type:");

		cmbxMosaicTypes = new JComboBox<Object>(EMosaic.getDescriptionValues().toArray());
//		cmbxMosaicTypes.setPrototypeDisplayValue("aaaaaaaaaaaa");
		// ��������� ����� ���������� ��������
		cmbxMosaicTypes.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) { OnChangeMosaicType(e); }
		});

		btnOk = new JButton();
		setBtnOkIcons(EMosaic.eMosaicTriangle1);
		btnOk.setToolTipText("Ok");
		Insets margin = btnOk.getMargin();
		margin.left = margin.right = 2; margin.top = margin.bottom = 2;
		btnOk.setMargin(margin);
		btnOk.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) { OnOk(); }
		});

		// variant 1
		boxCenter.add(lbl1); 
		lbl1.setAlignmentX(Component.CENTER_ALIGNMENT);
		boxCenter.add(spin);
		boxCenter.add(Box.createVerticalStrut(12));
		boxCenter.add(lbl2);
		lbl2.setAlignmentX(Component.CENTER_ALIGNMENT);
		boxCenter.add(cmbxMosaicTypes);
		boxCenter.add(Box.createVerticalStrut(12));
		boxCenter.add(btnOk);
		btnOk.setAlignmentX(Component.CENTER_ALIGNMENT);
		GuiTools.makeSameWidth(new JComponent[] {lbl1, lbl2, spin, cmbxMosaicTypes});

//		// variant 2
//		Box boxLine = Box.createHorizontalBox();
////		boxLine.setBorder(GuiTools.getDummyBorder(Color.GREEN));
//		{
//			Box boxCol = Box.createVerticalBox();
//			{
//				JPanel flow = new JPanel(new FlowLayout( FlowLayout.LEFT ));
////				flow.setBorder(GuiTools.getDummyBorder(Color.RED));
//				flow.add(lbl1);
//				flow.add(spin);
//				boxCol.add(Box.createVerticalGlue());
//				boxCol.add(flow);
//				flow.setAlignmentX(Component.CENTER_ALIGNMENT);
//				boxCol.add(Box.createVerticalGlue());
//			}
//			boxLine.add(boxCol);
//		}
//		boxCenter.add(Box.createHorizontalStrut(12));
//		boxLine.add(btnOk);
////		btnOk.setAlignmentY(Component.CENTER_ALIGNMENT);
//		boxCenter.add(boxLine);
//		boxCenter.add(Box.createVerticalStrut(12));
//		boxCenter.add(lbl2);
//		lbl2.setAlignmentX(Component.CENTER_ALIGNMENT);
//		boxCenter.add(cmbxMosaicTypes);
//		GuiTools.makeSameWidth(new JComponent[] {boxLine, lbl2, cmbxMosaicTypes});

		// �������� ������������ � ����� ����
		getContentPane().add(boxCenter, BorderLayout.CENTER);
	}

	private void OnChangeMosaicType(ItemEvent e) {
		// �������, ��� ���������
		if ( e.getStateChange() == ItemEvent.SELECTED ) {
			// ��������� ��������� �����
			final EMosaic item = EMosaic.fromDescription(e.getItem().toString());
//			System.out.println(item);
			final int groupNumber = item.getFastCode();
			if (groupNumber != (Integer)spin.getValue()) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						setBtnOkIcons(item);
						spin.setValue(groupNumber);
					}
				});
			}
		}
	}
	private void OnChangeMosaicNumber() {
//		System.out.println("OnChangeMosaicNumber: getMosaicNumber()=" + getMosaicNumber());
		int val;
		try {
			val = Integer.parseInt(getMosaicNumber());
		} catch (NumberFormatException e) {
			spin.setBackground(new Color(0xF24D5C));
			return;
		}
		EMosaic mosaicType = EMosaic.fromFastCode(val);
		if (mosaicType == null) {
			spin.setBackground(new Color(0xF24D5C));
			return;
		}
		spin.setBackground( UIManager.getDefaults().getColor("Spin.background") ); // TextField.background

		if (mosaicType != getSelectedMosaicType())
			for (int i=0; i<cmbxMosaicTypes.getItemCount(); i++) {
				EMosaic item = EMosaic.fromDescription(cmbxMosaicTypes.getItemAt(i).toString());
				if (item == mosaicType) {
					setBtnOkIcons(item);
					cmbxMosaicTypes.setSelectedIndex(i);
					break;
				}
			}
	}
	private void setBtnOkIcons(EMosaic mosaicType) {
		btnOk.setIcon(getResources().getImgMosaic(mosaicType, false, 51,41));
		btnOk.setRolloverIcon(getResources().getImgMosaic(mosaicType, false, 50,40));
	}
	private void OnOk() {
//		System.out.println("OnOk");

		if (parent != null) {
			final EMosaic selectedMosaicType = getSelectedMosaicType();
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					parent.SetGame(selectedMosaicType, new ActionEvent(SelectMosaicDlg.this, 0, null));
				}
			});
		}

		OnClose();
	}
	private void OnClose() {
		// ��� ������ �� ����������� ���� - ���������� �������
		dispose();
//		System.exit(0);
	}
	private EMosaic getSelectedMosaicType() {
		EMosaic item = EMosaic.fromDescription(cmbxMosaicTypes.getSelectedItem().toString());
		return item;
	}
	/** ������ �� �� ������, � �� ��������� */
	private String getMosaicNumber() {
		//return spin.getValue().toString(); // �� SpinnerNumberModel

		// � �� ���� �� ��� ������������� ������ � editor'� (�� ������� Enter'� �� editbox'e)
		JTextField txtField = ((JSpinner.DefaultEditor)spin.getEditor()).getTextField();
		return txtField.getText(); 
	}

	// �������� ����� ��� �������� ����������� ����
	public static void main(String[] args) {
		SelectMosaicDlg sm = new SelectMosaicDlg(null, true);
		sm.startSelect(EMosaicGroup.eQuadrangles);
	}
}