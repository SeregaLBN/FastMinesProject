package ua.ksn.fmg.view.swing.dialogs;

import javax.swing.*;

import ua.ksn.swing.utils.BoxLayoutUtils;
import ua.ksn.swing.utils.GuiTools;

import java.awt.*;
import java.awt.event.*;

public class LoginDlg extends JDialog {
	private static final long serialVersionUID = 1L;

	private JTextField nameField, passwrdField;
	private ActionListener onOkActionListener, onCancelActionListener;

	public LoginDlg(
			Window parent,
			boolean modal,
			String username,
			boolean usePassword)
	{
		super(parent, (username==null) ? "New user" : "Confirm password", modal ? Dialog.DEFAULT_MODALITY_TYPE : ModalityType.MODELESS);
		initialize(parent, username, usePassword);
	}

	private void initialize(Window parent, String username, boolean usePassword) {
		Object keyBind = "OnOk";
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false), keyBind);
        getRootPane().getActionMap().put(keyBind, new AbstractAction() {
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) { LoginDlg.this.OnOk(e); }
		});

        keyBind = "CloseDialog";
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false), keyBind);
        getRootPane().getActionMap().put(keyBind, new AbstractAction() {
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) { LoginDlg.this.OnCancel(e); }
		});

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) { LoginDlg.this.OnCancel(new ActionEvent(we.getSource(), we.getID(), "windowClosing")); }
		});

		// ��������� ������������ � ����� ����
		getContentPane().add(CreateComponents(username, usePassword));
	
		// ������ ���������������� ������
		pack();
		this.setLocationRelativeTo(parent);
	}

	/** ���� ����� ����� ���������� ������ � ��������� ������������� */
	private JComponent CreateComponents(String username, boolean usePassword) {
		// 1. ��������� ������, ������� ����� ��������� ��� ��������� �������� � ������ ������������
		Box main = Box.createVerticalBox();

		// ����� ��������� ������� ����������� Java, ���������� �������� ��� ���������� �� ������ ���� �� 12 ��������.
		// ��� ����� ��������� ������ �����
		main.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

		// 2. ������ "������", �� ������� ��� ������ ��������� �� ����� �������
		// �) ������ ��������� ���� � ������� � ����
		Box name = Box.createHorizontalBox();

		JLabel nameLabel = new JLabel("Name:");
		name.add(nameLabel);
		name.add(Box.createHorizontalStrut(12));

		nameField = new JTextField(username, 15);
		name.add(nameField);
		if (username != null)
			nameField.setEditable(false);

		// �) ������ ��������� ���� � ������� � ����
		Box password = Box.createHorizontalBox();
		JLabel passwrdLabel = new JLabel("Password:");
		password.add(passwrdLabel);
		password.add(Box.createHorizontalStrut(12));
		passwrdField = new JTextField(15);
		password.add(passwrdField);

		// �) ��� ������
		JPanel flow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		JPanel grid = new JPanel(new GridLayout(1, 2, 5, 0));
		JButton ok = new JButton("Ok");
		JButton cancel = new JButton("Cancel");

		grid.add(ok);
		grid.add(cancel);
		flow.add(grid);

		ok.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				LoginDlg.this.OnOk(e);
			}
		});
		cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				LoginDlg.this.OnCancel(e);
			}
		});

		// 3. �������� �� ������������ �����������, ��������� �� ��������, �������� ���������� ��������
		// �) ������������� ������������ ��������� �������
		BoxLayoutUtils.setGroupAlignmentX(new JComponent[] { name, password, main, flow }, Component.LEFT_ALIGNMENT);
		// �) ����������� ������������ �������� � ��������� �����
		BoxLayoutUtils.setGroupAlignmentY(new JComponent[] { nameField, passwrdField, nameLabel, passwrdLabel }, Component.CENTER_ALIGNMENT);
		// �) ���������� ������� �������� � ��������� �����
		GuiTools.makeSameWidth(new Component[] { nameLabel, passwrdLabel });
		// �) ����������� ��� ��� ������
		GuiTools.createRecommendedMargin(new JButton[] { ok, cancel });
		// �) ���������� "�����������" ������ ��������� �����
		GuiTools.fixTextFieldSize(nameField);
		GuiTools.fixTextFieldSize(passwrdField);

		// 4. ���� ����� � ���������
		main.add(name);
		main.add(Box.createVerticalStrut(12));
		main.add(password);
		Component qwe;
		main.add(qwe = Box.createVerticalStrut(17));
		main.add(flow);

		if (!usePassword) {
			password.setVisible(false);
			qwe.setVisible(false);
		}

		// ������
		return main;
	}

	/** �������� ����� ��� �������� ����������� ���� */
	public static void main(String[] args) {
		LoginDlg dlg = new LoginDlg(null, true, "aasd", true);
		dlg.setVisible(true);
	}

	private void OnOk(ActionEvent e) {
//		System.out.println("OnOk");
		if (onOkActionListener != null)
			onOkActionListener.actionPerformed(e);
		OnClose();
	}
	private void OnCancel(ActionEvent e) {
//		System.out.println("OnCancel");
		nameField.setText(null);
		passwrdField.setText(null);
		if (onCancelActionListener != null)
			onCancelActionListener.actionPerformed(e);
		OnClose();
	}
	private void OnClose() {
		// ��� ������ �� ����������� ���� - ���������� �������
		dispose();
		//System.exit(0);
	}

	public String getName() { return nameField.getText(); }
	public String getPass() { return passwrdField.getText(); }

	public void setOkActionListener(ActionListener onOkActionListener) { this.onOkActionListener = onOkActionListener; }
	public void setCancelActionListener(ActionListener CancelOkActionListener) { this.onCancelActionListener = CancelOkActionListener; }
}