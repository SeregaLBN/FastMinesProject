package ua.ksn.fmg.view.swing.dialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URI;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EtchedBorder;

import ua.ksn.fmg.view.swing.res.Resources;
import ua.ksn.fmg.view.swing.res.Resources.EBtnNewGameState;
import ua.ksn.swing.utils.GuiTools;
import ua.ksn.swing.utils.ImgUtils;

public class AboutDlg extends JDialog {
	private static final long serialVersionUID = 1L;
	private Resources resources;
	private Resources getResources() {
		if (resources == null)
			resources = new Resources();
		return resources;
	}

	public AboutDlg(JFrame parent, boolean modal) {
		super(parent, "About", modal);
		initialize(parent);
	}
	public AboutDlg(JFrame parent, boolean modal, Resources resources) {
		super(parent, "About", modal);
		this.resources = resources;
		initialize(parent);
	}
	private void initialize(JFrame parent) {
		Object keyBind = "CloseDialog";
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
		// ����� ���������������� ������
		pack();
		this.setLocationRelativeTo(parent);
	}

	private void OnClose() {
		// ��� ������ �� ����������� ���� - ���������� �������
		dispose();
//		System.exit(0);
	}

	// ������ ������ � ������ �������������
	private void CreateComponents() {
		// 1. ������ ������, ������� ����� ��������� ��� ��������� �������� � ������ ������������
		Box boxCenter = Box.createVerticalBox();
		// ����� ��������� ������� ����������� Java, ���������� �������� ��� ���������� �� ������ ���� �� 12 ��������. 
		// ��������� ������ ����� 
		//boxCenter.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
		boxCenter.setBorder(
				new CompoundBorder(
						BorderFactory.createEmptyBorder(12,12,0,12),
						new CompoundBorder(
								BorderFactory.createEtchedBorder(),
								BorderFactory.createEmptyBorder(12,12,12,12))));


		// 2. ���������� ��������� "������", �� ������� ��� ������ ���������
		// �) ��� ��� ������, ����������, ������, ��������� � ��������
		Box firstLine = Box.createHorizontalBox();
		{ 
//			firstLine.setBorder(GuiTools.getDummyBorder(Color.RED));

			// ����� - ������ ������
			JComponent logo = CreatePanelLogo();
			logo.setAlignmentY(Component.TOP_ALIGNMENT);
			firstLine.add(logo);

			firstLine.add(Box.createHorizontalStrut(5));

			// ������ - � ��������� �������� ������ ����������, ������, ��������� � ��������
			JComponent title = CreatePanelTitle();
			title.setAlignmentY(Component.TOP_ALIGNMENT);
			firstLine.add(title);
		}
		// �) ������ ������ - ��������
		JComponent secondLine = CreatePanelContatcs();

		// 4. ������������� "����" ����� � ���������
		boxCenter.add(firstLine);
		boxCenter.add(Box.createVerticalStrut(12));
		boxCenter.add(secondLine);
		boxCenter.add(Box.createVerticalStrut(12));

		// �������� ������������ � ����� ����
		getContentPane().add(boxCenter, BorderLayout.CENTER);
		// ��� ������ �����
		getContentPane().add(CreatePanelOk(), BorderLayout.SOUTH);
	}
	
	/** ������� */
	private JComponent CreatePanelLogo() {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEADING));
//		panel.setBorder(BorderFactory.createTitledBorder("Logos"));// getDefaultBorder());

		int icoSize = 48;
		JButton btnLogo = new JButton(ImgUtils.toImgIco(getResources().getImgLogo(), icoSize,icoSize));
		btnLogo.setPressedIcon(ImgUtils.zoom(getResources().getImgBtnNew(EBtnNewGameState.eNormalLoss), icoSize,icoSize));
		btnLogo.setFocusable(false);

		Insets margin = btnLogo.getMargin();
		margin.left = margin.right = margin.top = margin.bottom = 2;
		btnLogo.setMargin(margin);

		panel.add(btnLogo);
		return panel;
	}
	/** ������ ����������, ������, ��������� � �������� */
	private JComponent CreatePanelTitle() {
		Box panel = Box.createVerticalBox(); 
//		panel.setBorder(BorderFactory.createTitledBorder("titles"));// getDefaultBorder());

		String htmpWidth = "190px";
		JLabel lblTitle = new JLabel("<html><body " +
//				"bgcolor='#FEEF98'" +
				"><font size=6 color=dark face='serif'><center width='"+htmpWidth+"'>FastMines" ); // arial verdana
		/* ������������� ��������� �������:
		 *     serif � ������ � ��������� (�����������), ���� Times;
		 *     sans-serif � ��������� ������ (������ ��� ������� ��� ��������), �������� ������������� � Arial;
		 *     cursive � ��������� ������;
		 *     fantasy � ������������ ������;
		 *     monospace � ������������ ������, ������ ������� ������� � ����� ��������� ��������� (����� Courier).
		 **/
		lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);;
//		lblTitle.setBorder(BorderFactory.createEtchedBorder());
		panel.add(lblTitle);

		panel.add(Box.createVerticalStrut(2));

		JLabel lblVersion = new JLabel("Version 2011.09.30 (Java SE, SWING)");
		lblVersion.setAlignmentX(Component.CENTER_ALIGNMENT);
//		lblVersion.setBorder(BorderFactory.createEtchedBorder());
		panel.add(lblVersion);

		panel.add(Box.createVerticalStrut(2));

		JLabel lblAuthor = new JLabel("Author Sergey Krivulya (KSerg)");
		lblAuthor.setAlignmentX(Component.CENTER_ALIGNMENT);
//		lblAuthor.setBorder(BorderFactory.createEtchedBorder());
		panel.add(lblAuthor);

		panel.add(Box.createVerticalStrut(2));

		final String licenseUrl = "http://www.gnu.org/licenses/gpl.html";
		JLabel lblFreeSoft = new JLabel("<html><body " +
//				"bgcolor='#FEEF98'" +
				"><center width='"+htmpWidth+"'><a href='"+licenseUrl+"'>Free software, open source (GPL)");
		lblFreeSoft.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				OpenURI(licenseUrl);
			}});
		lblFreeSoft.setAlignmentX(Component.CENTER_ALIGNMENT);;
//		lblFreeSoft.setBorder(BorderFactory.createEtchedBorder());
		panel.add(lblFreeSoft);

		return panel;
	}
	/** �������� */
	private JComponent CreatePanelContatcs() {
		Border customBorder = new CompoundBorder(
				new EtchedBorder(EtchedBorder.RAISED), //BorderFactory.createBevelBorder(BevelBorder.RAISED),
				BorderFactory.createEmptyBorder(1,5,1,5));
		String htmpWidth = "200px";

		Box mail = Box.createHorizontalBox();
		JLabel lblLeftMail = new JLabel("Mail: ");
		{
			mail.add(Box.createHorizontalStrut(20));
			mail.add(lblLeftMail);
			mail.add(Box.createHorizontalStrut(2));
			final String mailTo = "FastMines@gmail.com";
			JLabel lblMail = new JLabel("<html><body " +
//					"bgcolor='#FEEF98'" +
					"><center width='"+htmpWidth+"'><a href='"+mailTo+"'>"+mailTo);
			lblMail.addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					OpenMail("mailto:"+mailTo);
				}});
			lblMail.setBorder(customBorder);
			mail.add(lblMail);
		}

		
		Box web = Box.createHorizontalBox();
		JLabel lblLeftWeb = new JLabel("Web: ");
		{
			web.add(Box.createHorizontalStrut(20));
			web.add(lblLeftWeb);
			web.add(Box.createHorizontalStrut(2));
			final String webPage = "http://kserg77.narod.ru/FastMines.html";
			JLabel lblWeb = new JLabel("<html><body " +
//					"bgcolor='#FEEF98'" +
					"><center width='"+htmpWidth+"'><a href='"+webPage+"'>"+webPage);
			lblWeb.addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					OpenURI(webPage);
				}});
			lblWeb.setBorder(customBorder);
			web.add(lblWeb);
		}
		GuiTools.makeSameWidth(new JComponent [] {lblLeftMail, lblLeftWeb});

		Box panel = Box.createVerticalBox();
//		JPanel panel = new JPanel(new GridLayout(2, 2, 5, 12));
//		panel.setBorder(BorderFactory.createTitledBorder("contacts"));// getDefaultBorder());

		panel.add(mail);
		panel.add(Box.createVerticalStrut(2));
		panel.add(web);

		return panel;
	}
	/** ������ �� */
	private JComponent CreatePanelOk() {
		JPanel panel = new JPanel( new FlowLayout(FlowLayout.CENTER, 12, 12) );
//		panel.setBorder(GuiTools.getDummyBorder(Color.LIGHT_GRAY));
		
		JButton ok = new JButton("OK");
		Insets margin = ok.getMargin();
		margin.left = margin.right = 12; margin.top = margin.bottom = 2;
		ok.setMargin(margin);

		// ����������� ��� ��� ������
//		createRecommendedMargin(new JButton[] { ok } );

		ok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				OnClose();
			}
		});
		
		panel.add(ok);
		return panel;
	}
	
	// �������� ����� ��� �������� ����������� ����
	public static void main(String[] args) {
		new AboutDlg(null, true).setVisible(true);
	}

	public static boolean OpenURI(String uri) {
		// http://johnbokma.com/mexit/2008/08/19/java-open-url-default-browser.html
		if (!Desktop.isDesktopSupported()) {
			System.err.println("Fail - Desktop is not supported.");
			return false;
		}
		Desktop desktop = Desktop.getDesktop();
		if (!desktop.isSupported(Desktop.Action.BROWSE)) {
			System.err.println("Fail - Desktop doesn't support the browse action");
			return false;
		}
		try {
			desktop.browse(new URI(uri));
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	public static boolean OpenMail(String mailTo) {
		if (!Desktop.isDesktopSupported()) {
			System.err.println("Fail - Desktop is not supported.");
			return false;
		}
		Desktop desktop = Desktop.getDesktop();
		if (!desktop.isSupported(Desktop.Action.MAIL)) {
			System.err.println("Fail - Desktop doesn't support the mail action");
			return false;
		}
		try {
			desktop.mail(new URI(mailTo));
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
}

